package com.ytdd9527.networksexpansion.implementation.machines.cellnet.assembly;

import com.balugaq.netex.utils.Debug;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.assembly.core.SmartCore;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.StorageCell;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.VoidCellSupport;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.drive.CellDrive;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.drive.DriveStorage;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.drive.EnderDrive;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.ItemKey;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import io.github.sefiraat.networks.utils.Keys;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.thebusybiscuit.slimefun4.libraries.dough.data.persistent.PersistentDataAPI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class AssemblyRound {
   static final long MIN_INTERVAL_MS = 1000L;
   static final long BACKOFF_INTERVAL_MS = 3000L;
   static final long PROBE_RETRY_MS = 2000L;
   static final int GEAR_MIN = 0;
   static final int GEAR_MAX = 13;
   static final long[] GEAR_LADDER = new long[]{1L, 4L, 8L, 16L, 32L, 64L, 128L, 256L, 512L, 1024L, 1728L, 3456L};
   static final Map<Location, AssemblyRound.DriveRuntimeState> RUNTIME_STATES = new ConcurrentHashMap<>();
   static final Set<Location> ACTIVE_DRIVES = ConcurrentHashMap.newKeySet();
   private static final long SNAPSHOT_TTL_MS = 5000L;
   private static final Map<Location, AssemblyRound.SharedSnapshot> SHARED_SNAPSHOT_BY_NETWORK = new ConcurrentHashMap<>();
   private static final long SHARED_SNAPSHOT_IDLE_MS = 3600000L;

   private AssemblyRound() {
   }

   static AssemblyRound.DriveRuntimeState runtimeState(@NotNull Location loc) {
      return RUNTIME_STATES.computeIfAbsent(loc, k -> new AssemblyRound.DriveRuntimeState());
   }

   @NotNull
   static AssemblyRound.CachedSnapshot sharedSnapshot(@NotNull NetworkRoot root) {
      long now = System.currentTimeMillis();
      Location networkId = root.getNodePosition();
      AssemblyRound.SharedSnapshot shared = SHARED_SNAPSHOT_BY_NETWORK.compute(
         networkId,
         (k, old) -> old != null && now < old.expiresAtMs()
               ? new AssemblyRound.SharedSnapshot(old.keyed(), old.expiresAtMs(), now)
               : new AssemblyRound.SharedSnapshot(root.getAllNetworkItemsKeyedView(), now + 5000L, now)
      );
      evictStaleNetworks(now);
      return new AssemblyRound.CachedSnapshot(shared.expiresAtMs(), shared.keyed());
   }

   private static void evictStaleNetworks(long now) {
      if (SHARED_SNAPSHOT_BY_NETWORK.size() > 1) {
         SHARED_SNAPSHOT_BY_NETWORK.values().removeIf(e -> now - e.lastAccessAtMs() > 3600000L);
      }
   }

   @Nullable
   private static AssemblyRound.CraftLimit resolveCraftLimit(
      @NotNull BlockMenu blockMenu,
      int index,
      @NotNull AssemblyRound.MachineState state,
      @Nullable AssemblyRound.CachedSnapshot snapshot,
      @NotNull AssemblyRound.Recipe recipe
   ) {
      int gear = state.cachedGear[index];
      if (gear <= 0) {
         return null;
      } else {
         long mode = state.cachedMode[index];
         long target = mode == 1L ? state.cachedTarget[index] : 0L;
         if (target > Long.MAX_VALUE) {
            return null;
         } else {
            long remaining = mode == 1L ? state.cachedRemaining[index] : 0L;
            long keep = mode == 2L ? state.cachedKeep[index] : 0L;
            long units = Math.max(1L, (long)recipe.output().getAmount());
            long materialCap = snapshot == null ? Long.MAX_VALUE : craftableFromSnapshot(recipe, snapshot);
            long cap;
            if (mode == 1L) {
               cap = (remaining + units - 1L) / units;
            } else if (mode == 2L) {
               if (keep <= 0L || snapshot == null) {
                  return null;
               }

               long stock = snapshotStock(snapshot, recipe.outputExactKey()) + Math.max(0L, state.craftedSince[index]);
               String channel = smartChannel(blockMenu);
               if (channel != null) {
                  stock += EnderDrive.channelStock(channel, recipe.output());
               }

               if (stock >= keep) {
                  state.blocked[index] = false;
                  state.nextProbeAt[index] = System.currentTimeMillis() + 2000L;
                  return null;
               }

               cap = (keep - stock) / units;
            } else {
               cap = Long.MAX_VALUE;
            }

            long gearLim = gearCap(gear);
            long want = Math.min(materialCap, cap);
            if (gearLim >= 0L) {
               want = Math.min(want, gearLim);
            }

            return want <= 0L ? null : new AssemblyRound.CraftLimit(mode, remaining, want);
         }
      }
   }

   static boolean craftSlot(
      @NotNull BlockMenu blockMenu,
      @NotNull NetworkRoot root,
      int index,
      @Nullable AssemblyRound.CachedSnapshot snapshot,
      @NotNull AssemblyRound.MachineState state
   ) {
      long now = System.currentTimeMillis();
      if (now < state.nextProbeAt[index]) {
         return false;
      } else {
         int slot = AssemblyDrive.RECIPE_SLOTS[index];
         ItemStack blueprint = blockMenu.getItemInSlot(slot);
         AssemblyRound.Recipe recipe = readRecipe(blockMenu, index, blueprint, state);
         if (recipe != null && blueprint != null) {
            AssemblyRound.CraftLimit limit = resolveCraftLimit(blockMenu, index, state, snapshot, recipe);
            if (limit == null) {
               return false;
            } else {
               Location location = blockMenu.getLocation();
               long units = Math.max(1L, (long)recipe.output().getAmount());
               long batch = probeBatchCapacity(root, location, recipe, units, limit.want(), state, index, now);
               if (batch <= 0L) {
                  return false;
               } else {
                  long feasible = fetchMaterials(root, location, recipe, batch);
                  if (feasible <= 0L) {
                     state.nextProbeAt[index] = now + 2000L;
                     return false;
                  } else {
                     boolean fullyAbsorbed = pushOutput(root, location, recipe.output(), feasible * units);
                     state.craftedSince[index] = state.craftedSince[index] + feasible;
                     if (!fullyAbsorbed) {
                        state.snapshot = null;
                        SHARED_SNAPSHOT_BY_NETWORK.computeIfPresent(
                           root.getNodePosition(), (k, old) -> new AssemblyRound.SharedSnapshot(old.keyed(), 0L, old.lastAccessAtMs())
                        );
                     }

                     if (limit.mode() == 1L) {
                        settlePlanProgress(blockMenu, slot, blueprint, limit.remaining(), feasible, units, state, index);
                     }

                     return true;
                  }
               }
            }
         } else {
            return false;
         }
      }
   }

   private static void settlePlanProgress(
      @NotNull BlockMenu blockMenu,
      int slot,
      @NotNull ItemStack blueprint,
      long remaining,
      long feasible,
      long units,
      @NotNull AssemblyRound.MachineState state,
      int index
   ) {
      long left = Math.max(0L, remaining - feasible * units);
      BlueprintMeta.setMetaLong(blueprint, Keys.CRAFT_REMAINING, left);
      if (left <= 0L) {
         BlueprintMeta.setMetaLong(blueprint, Keys.CRAFT_TARGET, 0L);
      }

      state.cachedRemaining[index] = left;
      if (left <= 0L) {
         state.cachedTarget[index] = 0L;
      }

      blockMenu.replaceExistingItem(slot, blueprint);
      state.slotItemRefs[index] = blockMenu.getItemInSlot(slot);
   }

   @Nullable
   private static String smartChannel(@NotNull BlockMenu blockMenu) {
      ItemStack core = blockMenu.getItemInSlot(33);
      return core != null && SmartCore.isSmartCore(core) ? SmartCore.getChannel(core) : null;
   }

   static void updateStockFlag(@NotNull AssemblyRound.MachineState state, int index, @Nullable ItemStack blueprint) {
      state.nextProbeAt[index] = 0L;
      state.slotItemRefs[index] = null;
      state.slotRecipes[index] = null;
      if (blueprint != null && !blueprint.getType().isAir() && AssemblyCard.isCard(blueprint)) {
         state.cachedMode[index] = BlueprintMeta.getMetaLong(blueprint, Keys.CRAFT_MODE, 0L);
         state.cachedTarget[index] = BlueprintMeta.getMetaLong(blueprint, Keys.CRAFT_TARGET, 0L);
         state.cachedKeep[index] = BlueprintMeta.getMetaLong(blueprint, Keys.CRAFT_KEEP, 0L);
         state.cachedGear[index] = Math.min(gearOf(blueprint), state.maxGear);
         long target = state.cachedTarget[index];
         state.cachedRemaining[index] = target > 0L ? BlueprintMeta.getMetaLong(blueprint, Keys.CRAFT_REMAINING, target) : 0L;
         state.lineNeedsStock[index] = state.cachedMode[index] == 2L && state.cachedGear[index] > 0;
      } else {
         state.lineNeedsStock[index] = false;
         state.cachedMode[index] = 0L;
         state.cachedTarget[index] = 0L;
         state.cachedKeep[index] = 0L;
         state.cachedRemaining[index] = 0L;
         state.cachedGear[index] = 0;
      }

      state.needsStockSnapshot = anyNeedsStock(state);
   }

   private static boolean anyNeedsStock(@NotNull AssemblyRound.MachineState state) {
      for (boolean need : state.lineNeedsStock) {
         if (need) {
            return true;
         }
      }

      return false;
   }

   private static long gearCap(int gear) {
      if (gear <= 0) {
         return 0L;
      } else {
         return gear > GEAR_LADDER.length ? -1L : GEAR_LADDER[gear - 1];
      }
   }

   static int gearOf(@NotNull ItemStack blueprint) {
      long raw = BlueprintMeta.getMetaLong(blueprint, Keys.CRAFT_GEAR, 1L);
      return (int)Math.max(0L, Math.min(13L, raw));
   }

   static long craftableFromSnapshot(@NotNull AssemblyRound.Recipe recipe, @NotNull AssemblyRound.CachedSnapshot snapshot) {
      long craftable = Long.MAX_VALUE;

      for (AssemblyRound.Ingredient ingredient : recipe.ingredients()) {
         Long have = snapshot.keyed().get(ingredient.exactKey());
         long count = have == null ? 0L : Math.max(0L, have);
         craftable = Math.min(craftable, count / (long)ingredient.amountPerCraft());
         if (craftable == 0L) {
            return 0L;
         }
      }

      return craftable;
   }

   static long snapshotStock(@NotNull AssemblyRound.CachedSnapshot snapshot, @NotNull ItemKey key) {
      Long have = snapshot.keyed().get(key);
      return have == null ? 0L : Math.max(0L, have);
   }

   private static long probeCapacity(@NotNull NetworkRoot root, @NotNull Location accessor, @NotNull AssemblyRound.Recipe recipe, long amount) {
      if (amount <= 0L) {
         return 0L;
      } else {
         DriveStorage.CapacityProbe capacityProbe = CellDrive.getStorage()
            .probeReceive(root.getDriveCache(), root.getInputAbleCellDriveMenus(), recipe.outputKey(), amount);
         if (capacityProbe.capacity() < amount && !capacityProbe.voidAccepts()) {
            ItemStack template = recipe.output();
            ItemStack probe = template.clone();
            probe.setAmount((int)Math.min(amount, 2147483647L));
            return withVoidSuspended(() -> {
               root.addItemStack(accessor, probe);
               long absorbed = amount - Math.max(0L, (long)probe.getAmount());
               if (absorbed <= 0L) {
                  return 0L;
               } else {
                  ItemStack got = root.getItemStack(accessor, new ItemRequest(template.clone(), (int)Math.min(absorbed, 2147483647L)));
                  long gotAmount = got != null && !got.getType().isAir() ? (long)got.getAmount() : 0L;
                  if (gotAmount < absorbed) {
                     Debug.debug("Assembly Drive capacity probe retrieved too little");
                  }

                  return gotAmount;
               }
            });
         } else {
            return amount;
         }
      }
   }

   private static long fetchMaterials(@NotNull NetworkRoot root, @NotNull Location accessor, @NotNull AssemblyRound.Recipe recipe, long batch) {
      return withVoidSuspended(() -> {
         List<AssemblyRound.Ingredient> ingredients = recipe.ingredients();
         ItemStack[] taken = new ItemStack[ingredients.size()];
         long[] per = new long[ingredients.size()];
         long feasible = batch;

         for (int i = 0; i < ingredients.size(); i++) {
            AssemblyRound.Ingredient ingredient = ingredients.get(i);
            per[i] = Math.max(1L, (long)ingredient.amountPerCraft());
            long need = Math.min(batch * per[i], 2147483647L);
            ItemStack stack = takeMaterial(root, accessor, ingredient, (int)need);
            taken[i] = stack;
            long gotAmount = stack != null && !stack.getType().isAir() ? (long)stack.getAmount() : 0L;
            feasible = Math.min(feasible, gotAmount / per[i]);
         }

         if (feasible <= 0L) {
            returnTaken(root, accessor, taken);
            return 0L;
         } else {
            List<ItemStack> excess = new ArrayList<>();

            for (int i = 0; i < ingredients.size(); i++) {
               ItemStack stack = taken[i];
               if (stack != null && !stack.getType().isAir()) {
                  long keep = feasible * per[i];
                  if ((long)stack.getAmount() > keep) {
                     ItemStack extra = stack.clone();
                     extra.setAmount((int)((long)stack.getAmount() - keep));
                     excess.add(extra);
                  }
               }
            }

            returnAll(root, accessor, excess);
            return feasible;
         }
      });
   }

   static boolean craftRound(
      @NotNull BlockMenu blockMenu, @NotNull NetworkRoot root, @Nullable AssemblyRound.CachedSnapshot snapshot, @NotNull AssemblyRound.MachineState state
   ) {
      long now = System.currentTimeMillis();
      Location location = blockMenu.getLocation();
      List<AssemblyRound.RoundLine> lines = collectRoundLines(blockMenu, root, location, snapshot, state, now);
      if (lines.isEmpty()) {
         return false;
      } else {
         AssemblyRound.RoundFetch fetch = withVoidSuspended(() -> fetchRoundMaterials(root, location, lines));
         boolean active = settleRoundProgress(blockMenu, lines, fetch, state, now);
         if (!pushRoundOutputs(root, location, fetch)) {
            state.snapshot = null;
            SHARED_SNAPSHOT_BY_NETWORK.computeIfPresent(
               root.getNodePosition(), (k, old) -> new AssemblyRound.SharedSnapshot(old.keyed(), 0L, old.lastAccessAtMs())
            );
         }

         return active;
      }
   }

   @NotNull
   private static List<AssemblyRound.RoundLine> collectRoundLines(
      @NotNull BlockMenu blockMenu,
      @NotNull NetworkRoot root,
      @NotNull Location location,
      @Nullable AssemblyRound.CachedSnapshot snapshot,
      @NotNull AssemblyRound.MachineState state,
      long now
   ) {
      List<AssemblyRound.RoundLine> lines = new ArrayList<>();

      for (int i = 0; i < AssemblyDrive.RECIPE_SLOTS.length; i++) {
         if (now >= state.nextProbeAt[i]) {
            ItemStack blueprint = blockMenu.getItemInSlot(AssemblyDrive.RECIPE_SLOTS[i]);
            AssemblyRound.Recipe recipe = readRecipe(blockMenu, i, blueprint, state);
            if (recipe != null && blueprint != null) {
               AssemblyRound.CraftLimit limit = resolveCraftLimit(blockMenu, i, state, snapshot, recipe);
               if (limit != null) {
                  long units = Math.max(1L, (long)recipe.output().getAmount());
                  long batch = probeBatchCapacity(root, location, recipe, units, limit.want(), state, i, now);
                  if (batch > 0L) {
                     lines.add(new AssemblyRound.RoundLine(i, blueprint, recipe, limit, units, batch));
                  }
               }
            }
         }
      }

      return lines;
   }

   private static long probeBatchCapacity(
      @NotNull NetworkRoot root,
      @NotNull Location location,
      @NotNull AssemblyRound.Recipe recipe,
      long units,
      long want,
      @NotNull AssemblyRound.MachineState state,
      int index,
      long now
   ) {
      long maxWant = 2147483647L / Math.max(1L, units);
      long probeUnits = Math.min(want, maxWant) * units;
      long absorbed = probeCapacity(root, location, recipe, probeUnits);
      if (absorbed <= 0L) {
         state.blocked[index] = true;
         state.nextProbeAt[index] = now + 2000L;
         return 0L;
      } else {
         state.blocked[index] = false;
         state.nextProbeAt[index] = 0L;
         long batch = Math.min(want, absorbed / units);
         if (batch <= 0L) {
            state.blocked[index] = true;
            state.nextProbeAt[index] = now + 2000L;
            return 0L;
         } else {
            return batch;
         }
      }
   }

   private static boolean settleRoundProgress(
      @NotNull BlockMenu blockMenu,
      @NotNull List<AssemblyRound.RoundLine> lines,
      @NotNull AssemblyRound.RoundFetch fetch,
      @NotNull AssemblyRound.MachineState state,
      long now
   ) {
      boolean active = false;

      for (AssemblyRound.RoundLine line : lines) {
         long feasible = fetch.feasible()[line.index()];
         if (feasible <= 0L) {
            state.nextProbeAt[line.index()] = now + 2000L;
         } else {
            active = true;
            state.craftedSince[line.index()] += feasible;
            if (line.limit().mode() == 1L) {
               settlePlanProgress(
                  blockMenu, AssemblyDrive.RECIPE_SLOTS[line.index()], line.blueprint(), line.limit().remaining(), feasible, line.units(), state, line.index()
               );
            }
         }
      }

      return active;
   }

   private static boolean pushRoundOutputs(@NotNull NetworkRoot root, @NotNull Location location, @NotNull AssemblyRound.RoundFetch fetch) {
      boolean allAbsorbed = true;

      for (Entry<ItemKey, Long> entry : fetch.outputs().entrySet()) {
         long remaining = entry.getValue();
         ItemStack template = fetch.outputTemplates().get(entry.getKey());
         long cellRemaining = CellDrive.getStorage().pushSingle(root.getDriveCache(), root.getInputAbleCellDriveMenus(), template, remaining);
         remaining = Math.max(0L, cellRemaining);

         while (remaining > 0L) {
            long chunk = Math.min(remaining, 2147483647L);
            ItemStack out = template.clone();
            out.setAmount((int)chunk);
            root.addItemStack(location, out);
            long leftover = Math.max(0L, (long)out.getAmount());
            if (leftover > 0L) {
               bufferPending(location, template, leftover);
               allAbsorbed = false;
            }

            remaining -= chunk;
         }
      }

      return allAbsorbed;
   }

   private static long ingredientWant(@NotNull AssemblyRound.RoundLine line, @NotNull AssemblyRound.Ingredient ingredient) {
      return Math.min(line.batch() * (long)ingredient.amountPerCraft(), 2147483647L);
   }

   @NotNull
   private static AssemblyRound.RoundFetch fetchRoundMaterials(
      @NotNull NetworkRoot root, @NotNull Location accessor, @NotNull List<AssemblyRound.RoundLine> lines
   ) {
      Map<ItemKey, Long> needTotals = new LinkedHashMap<>();
      Map<ItemKey, ItemStack> templates = new HashMap<>();

      for (AssemblyRound.RoundLine line : lines) {
         for (AssemblyRound.Ingredient ingredient : line.recipe().ingredients()) {
            needTotals.merge(ingredient.itemKey(), ingredientWant(line, ingredient), Long::sum);
            templates.putIfAbsent(ingredient.itemKey(), ingredient.template());
         }
      }

      Map<ItemKey, Long> pool = new HashMap<>();

      for (Entry<ItemKey, Long> entry : needTotals.entrySet()) {
         long got = CellDrive.getStorage().takeItemDirectAmount(root.getDriveCache(), root.getInputAbleCellDriveMenus(), entry.getKey(), entry.getValue());
         if (got > 0L) {
            pool.put(entry.getKey(), got);
         }
      }

      Map<ItemKey, Long> shortfalls = new LinkedHashMap<>();

      for (Entry<ItemKey, Long> entryx : needTotals.entrySet()) {
         long missing = entryx.getValue() - pool.getOrDefault(entryx.getKey(), 0L);
         if (missing > 0L) {
            shortfalls.put(entryx.getKey(), missing);
         }
      }

      if (!shortfalls.isEmpty()) {
         List<ItemRequest> requests = new ArrayList<>(shortfalls.size());
         List<ItemKey> order = new ArrayList<>(shortfalls.size());

         for (Entry<ItemKey, Long> entryxx : shortfalls.entrySet()) {
            for (long missing = entryxx.getValue(); missing > 0L; missing -= 2147483647L) {
               requests.add(new ItemRequest(templates.get(entryxx.getKey()).clone(), (int)Math.min(missing, 2147483647L)));
               order.add(entryxx.getKey());
            }
         }

         List<ItemStack> fetched = root.getItemStacksBatch(accessor, requests);

         for (int i = 0; i < fetched.size(); i++) {
            ItemStack got = fetched.get(i);
            long amount = got != null && !got.getType().isAir() ? (long)got.getAmount() : 0L;
            if (amount > 0L) {
               pool.merge(order.get(i), amount, Long::sum);
            }
         }
      }

      long[] feasible = new long[AssemblyDrive.RECIPE_SLOTS.length];

      for (AssemblyRound.RoundLine line : lines) {
         long cap = line.batch();

         for (AssemblyRound.Ingredient ingredient : line.recipe().ingredients()) {
            long want = ingredientWant(line, ingredient);
            long avail = pool.getOrDefault(ingredient.itemKey(), 0L);
            long take = Math.min(want, avail);
            pool.put(ingredient.itemKey(), avail - take);
            cap = Math.min(cap, take / (long)ingredient.amountPerCraft());
         }

         feasible[line.index()] = Math.max(0L, cap);
      }

      Map<ItemKey, Long> outputs = new LinkedHashMap<>();
      Map<ItemKey, ItemStack> outputTemplates = new HashMap<>();

      for (AssemblyRound.RoundLine line : lines) {
         long feasibleAmount = feasible[line.index()];
         if (feasibleAmount > 0L) {
            long units = line.units();
            outputs.merge(line.recipe().outputKey(), feasibleAmount * units, Long::sum);
            outputTemplates.putIfAbsent(line.recipe().outputKey(), line.recipe().output());
         }
      }

      List<ItemStack> excess = new ArrayList<>();

      for (Entry<ItemKey, Long> entryxx : pool.entrySet()) {
         for (long leftover = entryxx.getValue(); leftover > 0L; leftover -= 2147483647L) {
            ItemStack stack = templates.get(entryxx.getKey()).clone();
            stack.setAmount((int)Math.min(leftover, 2147483647L));
            excess.add(stack);
         }
      }

      returnAll(root, accessor, excess);
      return new AssemblyRound.RoundFetch(feasible, outputs, outputTemplates);
   }

   @Nullable
   private static ItemStack takeMaterial(@NotNull NetworkRoot root, @NotNull Location accessor, @NotNull AssemblyRound.Ingredient ingredient, int need) {
      ItemStack direct = CellDrive.getStorage().takeItemDirect(root.getDriveCache(), root.getInputAbleCellDriveMenus(), ingredient.itemKey(), need);
      int directAmount = direct != null && !direct.getType().isAir() ? direct.getAmount() : 0;
      if (directAmount >= need) {
         return direct;
      } else {
         int shortfall = need - directAmount;
         ItemStack fallback = root.getItemStack(accessor, new ItemRequest(ingredient.template().clone(), shortfall));
         if (fallback == null || fallback.getType().isAir()) {
            return direct;
         } else if (direct != null && !direct.getType().isAir()) {
            direct.setAmount(directAmount + fallback.getAmount());
            return direct;
         } else {
            return fallback;
         }
      }
   }

   @Nullable
   private static <T> T withVoidSuspended(@NotNull Supplier<T> action) {
      VoidCellSupport.suspend();

      Object var1;
      try {
         var1 = action.get();
      } finally {
         VoidCellSupport.resume();
      }

      return (T)var1;
   }

   private static void returnTaken(@NotNull NetworkRoot root, @NotNull Location accessor, @Nullable @NotNull ItemStack[] stacks) {
      List<ItemStack> items = new ArrayList<>(stacks.length);

      for (ItemStack stack : stacks) {
         if (stack != null && !stack.getType().isAir() && stack.getAmount() > 0) {
            items.add(stack);
         }
      }

      returnAll(root, accessor, items);
   }

   private static boolean pushOutput(@NotNull NetworkRoot root, @NotNull Location accessor, @NotNull ItemStack template, long total) {
      ItemStack out = template.clone();
      out.setAmount((int)Math.min(total, 2147483647L));
      root.addItemStack(accessor, out);
      if (!out.getType().isAir() && out.getAmount() > 0) {
         bufferPending(accessor, template, (long)out.getAmount());
         return false;
      } else {
         return true;
      }
   }

   private static void returnAll(@NotNull NetworkRoot root, @NotNull Location accessor, @NotNull List<ItemStack> items) {
      for (ItemStack item : items) {
         if (!item.getType().isAir() && item.getAmount() > 0) {
            root.addItemStack(accessor, item);
            if (!item.getType().isAir() && item.getAmount() > 0) {
               bufferPending(accessor, item, (long)item.getAmount());
            }
         }
      }
   }

   private static void bufferPending(@NotNull Location accessor, @NotNull ItemStack template, long amount) {
      if (amount > 0L && !template.getType().isAir()) {
         AssemblyRound.DriveRuntimeState s = runtimeState(accessor);
         if (s.pendingOutputs == null) {
            s.pendingOutputs = new ConcurrentHashMap<>();
         }

         s.pendingOutputs.merge(ItemKey.exact(template), amount, Long::sum);
      }
   }

   static boolean flushPendingOutputs(@NotNull NetworkRoot root, @NotNull Location accessor) {
      Map<ItemKey, Long> pending = runtimeState(accessor).pendingOutputs;
      if (pending != null && !pending.isEmpty()) {
         Iterator<Entry<ItemKey, Long>> iterator = pending.entrySet().iterator();

         while (iterator.hasNext()) {
            Entry<ItemKey, Long> entry = iterator.next();
            long remaining = entry.getValue();
            if (remaining <= 0L) {
               iterator.remove();
            } else {
               long left = returnToNetwork(root, accessor, entry.getKey(), remaining);
               if (left > 0L) {
                  entry.setValue(left);
                  return false;
               }

               iterator.remove();
            }
         }

         return true;
      } else {
         return true;
      }
   }

   private static long returnToNetwork(@NotNull NetworkRoot root, @NotNull Location accessor, @NotNull ItemKey key, long amount) {
      return amount <= 0L ? 0L : withVoidSuspended(() -> {
         long remaining = amount;

         while (remaining > 0L) {
            long push = Math.min(remaining, 2147483647L);
            ItemStack stack = key.getItemStack();
            stack.setAmount((int)push);
            root.addItemStack(accessor, stack);
            long absorbed = push - Math.max(0L, (long)stack.getAmount());
            if (absorbed <= 0L) {
               break;
            }

            remaining -= absorbed;
         }

         return remaining;
      });
   }

   static void dropPending(@NotNull Location location, @Nullable NetworkRoot root, @Nullable Map<ItemKey, Long> pending) {
      if (pending != null && !pending.isEmpty()) {
         for (Entry<ItemKey, Long> entry : pending.entrySet()) {
            long remaining = entry.getValue();
            if (root != null) {
               remaining = returnToNetwork(root, location, entry.getKey(), remaining);
            }

            if (remaining > 0L && location.getWorld() != null) {
               while (remaining > 0L) {
                  ItemStack stack = entry.getKey().getItemStack();
                  stack.setAmount((int)Math.min(remaining, 2147483647L));
                  location.getWorld().dropItemNaturally(location.clone().add(0.5, 1.0, 0.5), stack);
                  remaining -= (long)stack.getAmount();
               }
            }
         }
      }
   }

   @Nullable
   static AssemblyRound.Recipe readRecipe(@NotNull BlockMenu blockMenu, int index, @Nullable ItemStack item, @Nullable AssemblyRound.MachineState state) {
      if (item != null && !item.getType().isAir()) {
         if (state != null) {
            if (state.slotItemRefs[index] == item) {
               return state.slotRecipes[index];
            }

            ItemStack cachedRef = state.slotItemRefs[index];
            if (cachedRef != null && item.isSimilar(cachedRef)) {
               state.slotItemRefs[index] = item;
               return state.slotRecipes[index];
            }
         }

         AssemblyRound.Recipe recipe = readRecipeSlow(blockMenu, index, item);
         if (state != null) {
            state.slotItemRefs[index] = item;
            state.slotRecipes[index] = recipe;
         }

         return recipe;
      } else {
         return null;
      }
   }

   @Nullable
   private static AssemblyRound.Recipe readRecipeSlow(@NotNull BlockMenu blockMenu, int index, @NotNull ItemStack item) {
      if (isSlotMarker(item)) {
         return null;
      } else {
         String token = cardToken(item);
         if (token == null) {
            return null;
         } else {
            AssemblyRound.DriveRuntimeState s = runtimeState(blockMenu.getLocation().clone());
            if (s.recipeCache == null) {
               s.recipeCache = new AssemblyRound.SlotCache();
            }

            AssemblyRound.SlotCache cache = s.recipeCache;
            if (token.equals(cache.tokens[index])) {
               return cache.recipes[index];
            } else {
               for (int other = 0; other < AssemblyDrive.RECIPE_SLOTS.length; other++) {
                  if (other != index && token.equals(cache.tokens[other])) {
                     cache.tokens[index] = token;
                     cache.recipes[index] = cache.recipes[other];
                     return cache.recipes[other];
                  }
               }

               AssemblyRound.Recipe recipe = parseRecipe(item);
               cache.tokens[index] = token;
               cache.recipes[index] = recipe;
               return recipe;
            }
         }
      }
   }

   @Nullable
   private static String cardToken(@NotNull ItemStack item) {
      if (item.getType() == Material.PAPER && item.hasItemMeta()) {
         ItemMeta meta = item.getItemMeta();
         if (meta == null) {
            return null;
         } else {
            String token = PersistentDataAPI.getString(meta, Keys.ASSEMBLY_CARD_HASH);
            return token != null && !token.isEmpty() ? token : null;
         }
      } else {
         return null;
      }
   }

   @Nullable
   private static AssemblyRound.Recipe parseRecipe(@NotNull ItemStack item) {
      AssemblyCard.RecipeEntry entry = AssemblyCard.readCard(item);
      return entry == null ? null : toRecipe(entry.ingredients(), entry.output());
   }

   @Nullable
   private static AssemblyRound.Recipe toRecipe(@NotNull List<ItemStack> rawIngredients, @NotNull ItemStack rawOutput) {
      if (!rawIngredients.isEmpty() && !rawOutput.getType().isAir()) {
         List<AssemblyRound.Ingredient> ingredients = new ArrayList<>(rawIngredients.size());

         for (ItemStack requested : rawIngredients) {
            if (StackUtils.isBlacklisted(requested) || StorageCell.isStorageCell(requested) || VoidCellSupport.isVoidCell(requested)) {
               return null;
            }

            ingredients.add(
               new AssemblyRound.Ingredient(requested.clone(), Math.max(1, requested.getAmount()), new ItemKey(requested), ItemKey.exact(requested))
            );
         }

         ItemStack output = rawOutput.clone();
         output.setAmount(Math.max(1, rawOutput.getAmount()));
         return new AssemblyRound.Recipe(output, List.copyOf(ingredients), new ItemKey(output), ItemKey.exact(output));
      } else {
         return null;
      }
   }

   static boolean isAcceptableRecipeItem(@Nullable ItemStack item) {
      return AssemblyCard.isCard(item);
   }

   static boolean isSlotMarker(@Nullable ItemStack item) {
      if (item != null && item.getType() == Material.LIME_STAINED_GLASS_PANE && item.hasItemMeta()) {
         ItemMeta meta = item.getItemMeta();
         return meta != null && PersistentDataAPI.getByte(meta, Keys.ASSEMBLY_SLOT_MARKER) == 1;
      } else {
         return false;
      }
   }

   static record CachedSnapshot(long expiresAtMs, @NotNull Map<ItemKey, Long> keyed) {
   }

   private static record CraftLimit(long mode, long remaining, long want) {
   }

   static final class DriveRuntimeState {
      AssemblyRound.MachineState machineState;
      AssemblyRound.SlotCache recipeCache;
      Map<ItemKey, Long> pendingOutputs;
   }

   private static record Ingredient(@NotNull ItemStack template, int amountPerCraft, @NotNull ItemKey itemKey, @NotNull ItemKey exactKey) {
   }

   static final class MachineState {
      final boolean[] blocked = new boolean[AssemblyDrive.RECIPE_SLOTS.length];
      final long[] craftedSince = new long[AssemblyDrive.RECIPE_SLOTS.length];
      final long[] nextProbeAt = new long[AssemblyDrive.RECIPE_SLOTS.length];
      final long[] cachedMode = new long[AssemblyDrive.RECIPE_SLOTS.length];
      final long[] cachedTarget = new long[AssemblyDrive.RECIPE_SLOTS.length];
      final long[] cachedKeep = new long[AssemblyDrive.RECIPE_SLOTS.length];
      final long[] cachedRemaining = new long[AssemblyDrive.RECIPE_SLOTS.length];
      final int[] cachedGear = new int[AssemblyDrive.RECIPE_SLOTS.length];
      final ItemStack[] slotItemRefs = new ItemStack[AssemblyDrive.RECIPE_SLOTS.length];
      final AssemblyRound.Recipe[] slotRecipes = new AssemblyRound.Recipe[AssemblyDrive.RECIPE_SLOTS.length];
      final boolean[] lineNeedsStock = new boolean[AssemblyDrive.RECIPE_SLOTS.length];
      boolean enabled = true;
      boolean needsStockSnapshot;
      int maxGear = 12;
      volatile long nextRun;
      @Nullable
      Boolean lastStatusEnabled;
      @Nullable
      Boolean lastStatusHadRoot;
      @Nullable
      Boolean lastToggleEnabled;
      final AssemblyRound.RowDisplay[] rowDisplays = new AssemblyRound.RowDisplay[AssemblyDrive.RECIPE_SLOTS.length];
      @Nullable
      AssemblyRound.CachedSnapshot snapshot;
   }

   static record Recipe(
      @NotNull ItemStack output, @NotNull List<AssemblyRound.Ingredient> ingredients, @NotNull ItemKey outputKey, @NotNull ItemKey outputExactKey
   ) {
   }

   private static record RoundFetch(@NotNull long[] feasible, @NotNull Map<ItemKey, Long> outputs, @NotNull Map<ItemKey, ItemStack> outputTemplates) {
   }

   private static record RoundLine(
      int index, @NotNull ItemStack blueprint, @NotNull AssemblyRound.Recipe recipe, @NotNull AssemblyRound.CraftLimit limit, long units, long batch
   ) {
   }

   static final class RowDisplay {
      @Nullable
      AssemblyRound.Recipe recipe;
      @Nullable
      Object snapshotRef;
      long mode;
      long target;
      long remaining;
      long keep;
      long crafted;
      int gear;
      boolean blocked;
      boolean empty;
      @Nullable
      ItemStack icon;
   }

   private static record SharedSnapshot(@NotNull Map<ItemKey, Long> keyed, long expiresAtMs, long lastAccessAtMs) {
   }

   private static final class SlotCache {
      final String[] tokens = new String[AssemblyDrive.RECIPE_SLOTS.length];
      final AssemblyRound.Recipe[] recipes = new AssemblyRound.Recipe[AssemblyDrive.RECIPE_SLOTS.length];
   }
}
