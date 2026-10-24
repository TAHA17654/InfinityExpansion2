package com.ytdd9527.networksexpansion.implementation.machines.cellnet.assembly;

import com.balugaq.netex.utils.Lang;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.assembly.core.OverclockCore;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.assembly.core.SmartCore;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.drive.DriveOwnership;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.ChatInput;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.NetworkUtil;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.ui.CellSlotUI;
import io.github.sefiraat.networks.utils.Keys;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class AssemblyMonitorBridge {
   public static final long MODE_CONTINUOUS = 0L;
   public static final long MODE_PLAN = 1L;
   public static final long MODE_REPLENISH = 2L;

   private AssemblyMonitorBridge() {
   }

   @Nullable
   public static AssemblyMonitorBridge.DriveOverview readOverview(@NotNull Location location) {
      BlockMenu menu = StorageCacheUtils.getMenu(location);
      if (menu == null) {
         return null;
      } else {
         AssemblyRound.MachineState state = AssemblyRound.runtimeState(location).machineState;
         boolean enabled = state != null && state.enabled;
         int maxGear = state != null ? state.maxGear : 12;
         boolean overclock = OverclockCore.isOverclockCore(menu.getItemInSlot(32));
         boolean smart = SmartCore.isSmartCore(menu.getItemInSlot(33));
         List<AssemblyMonitorBridge.MonitorLine> lines = new ArrayList<>();

         for (int i = 0; i < AssemblyDrive.RECIPE_SLOTS.length; i++) {
            ItemStack blueprint = menu.getItemInSlot(AssemblyDrive.RECIPE_SLOTS[i]);
            AssemblyRound.Recipe recipe = AssemblyRound.readRecipe(menu, i, blueprint, state);
            if (recipe != null && blueprint != null) {
               long mode = state != null ? state.cachedMode[i] : BlueprintMeta.getMetaLong(blueprint, Keys.CRAFT_MODE, 0L);
               int gear = state != null ? state.cachedGear[i] : Math.min(AssemblyRound.gearOf(blueprint), maxGear);
               long target = mode == 1L ? (state != null ? state.cachedTarget[i] : BlueprintMeta.getMetaLong(blueprint, Keys.CRAFT_TARGET, 0L)) : 0L;
               long remaining = target > 0L
                  ? (state != null ? state.cachedRemaining[i] : BlueprintMeta.getMetaLong(blueprint, Keys.CRAFT_REMAINING, target))
                  : 0L;
               long keep = mode == 2L ? (state != null ? state.cachedKeep[i] : BlueprintMeta.getMetaLong(blueprint, Keys.CRAFT_KEEP, 0L)) : 0L;
               boolean blocked = state != null && state.blocked[i];
               lines.add(new AssemblyMonitorBridge.MonitorLine(recipe.output().clone(), mode, gear, remaining, keep, blocked, mode == 1L && target > 0L));
            }
         }

         return new AssemblyMonitorBridge.DriveOverview(location, displayName(location), enabled, overclock, smart, lines);
      }
   }

   @NotNull
   public static List<AssemblyMonitorBridge.MonitorLine> readLines(@NotNull Location location) {
      AssemblyMonitorBridge.DriveOverview overview = readOverview(location);
      return overview == null ? List.of() : overview.lines();
   }

   @NotNull
   public static Set<Location> activeDrives() {
      return AssemblyRound.ACTIVE_DRIVES;
   }

   @Nullable
   public static String displayName(@NotNull Location location) {
      String stored = StorageCacheUtils.getData(location, "assembly_name");
      return stored != null && !stored.isEmpty() ? stored : null;
   }

   public static long gearMultiplier(int gear) {
      if (gear <= 0) {
         return 0L;
      } else {
         return gear >= 13 ? -1L : AssemblyRound.GEAR_LADDER[gear - 1];
      }
   }

   public static void setNameFromMonitor(@NotNull Player player, @NotNull Location location, @NotNull String name) {
      if (!DriveOwnership.bypasses(player) && !DriveOwnership.isOwnerOrWhitelisted(player, location)) {
         player.sendMessage(Lang.getString("messages.cellnet.monitor.no_permission"));
      } else {
         String trimmed = name.trim();
         if (!trimmed.isEmpty() && !"-".equals(trimmed)) {
            if (trimmed.length() > 16) {
               player.sendMessage(Lang.getString("messages.cellnet.monitor.name_invalid"));
            } else {
               SlimefunBlockData blockData = StorageCacheUtils.getBlock(location);
               if (blockData != null) {
                  if (isNameTaken(trimmed, location)) {
                     player.sendMessage(Lang.getString("messages.cellnet.monitor.name_duplicate", trimmed));
                  } else {
                     blockData.setData("assembly_name", trimmed);
                     player.sendMessage(Lang.getString("messages.cellnet.monitor.name_set", trimmed));
                  }
               }
            }
         } else {
            clearNameFromMonitor(player, location);
         }
      }
   }

   public static void clearNameFromMonitor(@NotNull Player player, @NotNull Location location) {
      if (!DriveOwnership.bypasses(player) && !DriveOwnership.isOwnerOrWhitelisted(player, location)) {
         player.sendMessage(Lang.getString("messages.cellnet.monitor.no_permission"));
      } else {
         SlimefunBlockData blockData = StorageCacheUtils.getBlock(location);
         if (blockData != null) {
            blockData.setData("assembly_name", "");
            player.sendMessage(Lang.getString("messages.cellnet.monitor.name_cleared"));
         }
      }
   }

   private static boolean isNameTaken(@NotNull String name, @NotNull Location self) {
      for (Location other : AssemblyRound.ACTIVE_DRIVES) {
         if (!other.equals(self)) {
            String existing = displayName(other);
            if (name.equals(existing)) {
               return true;
            }
         }
      }

      return false;
   }

   public static void cycleLineFromMonitor(@NotNull Player player, @NotNull Location location, int index) {
      BlockMenu menu = StorageCacheUtils.getMenu(location);
      AssemblyRound.MachineState state = AssemblyRound.runtimeState(location).machineState;
      if (menu != null && state != null && index >= 0 && index < AssemblyDrive.RECIPE_SLOTS.length) {
         if (!DriveOwnership.bypasses(player) && !DriveOwnership.isOwnerOrWhitelisted(player, location)) {
            player.sendMessage(Lang.getString("messages.cellnet.monitor.no_permission"));
         } else {
            ItemStack blueprint = menu.getItemInSlot(AssemblyDrive.RECIPE_SLOTS[index]);
            if (blueprint != null && !blueprint.getType().isAir() && AssemblyRound.isAcceptableRecipeItem(blueprint)) {
               toggleMode(player, menu, index, blueprint, state);
            }
         }
      }
   }

   public static void requestQuantityFromMonitor(@NotNull Player player, @NotNull Location location, int index) {
      BlockMenu menu = StorageCacheUtils.getMenu(location);
      if (menu != null && index >= 0 && index < AssemblyDrive.RECIPE_SLOTS.length) {
         if (!DriveOwnership.bypasses(player) && !DriveOwnership.isOwnerOrWhitelisted(player, location)) {
            player.sendMessage(Lang.getString("messages.cellnet.monitor.no_permission"));
         } else {
            ItemStack blueprint = menu.getItemInSlot(AssemblyDrive.RECIPE_SLOTS[index]);
            if (blueprint != null && !blueprint.getType().isAir() && AssemblyRound.isAcceptableRecipeItem(blueprint)) {
               long mode = BlueprintMeta.getMetaLong(blueprint, Keys.CRAFT_MODE, 0L);
               if (mode != 0L) {
                  handleTargetRequest(player, menu, index, mode == 2L);
               }
            }
         }
      }
   }

   public static void changeGearFromMonitor(@NotNull Player player, @NotNull Location location, int index, int delta) {
      BlockMenu menu = StorageCacheUtils.getMenu(location);
      AssemblyRound.MachineState state = AssemblyRound.runtimeState(location).machineState;
      if (menu != null && state != null && index >= 0 && index < AssemblyDrive.RECIPE_SLOTS.length) {
         if (!DriveOwnership.bypasses(player) && !DriveOwnership.isOwnerOrWhitelisted(player, location)) {
            player.sendMessage(Lang.getString("messages.cellnet.monitor.no_permission"));
         } else {
            ItemStack blueprint = menu.getItemInSlot(AssemblyDrive.RECIPE_SLOTS[index]);
            if (blueprint != null && !blueprint.getType().isAir() && AssemblyRound.isAcceptableRecipeItem(blueprint)) {
               int gear = AssemblyRound.gearOf(blueprint);
               int next = delta > 0 ? Math.min(state.maxGear, gear + 1) : Math.max(0, gear - 1);
               if (next != gear) {
                  BlueprintMeta.setMetaLong(blueprint, Keys.CRAFT_GEAR, (long)next);
                  AssemblyRound.updateStockFlag(state, index, blueprint);
                  menu.replaceExistingItem(AssemblyDrive.RECIPE_SLOTS[index], blueprint);
                  AssemblyDisplay.refreshDisplays(menu, state, NetworkUtil.findRoot(location), state.snapshot);
               }
            }
         }
      }
   }

   static void toggleMode(
      @NotNull Player player, @NotNull BlockMenu menu, int rowIndex, @NotNull ItemStack blueprint, @NotNull AssemblyRound.MachineState state
   ) {
      long mode = BlueprintMeta.getMetaLong(blueprint, Keys.CRAFT_MODE, 0L);
      long next = (mode + 1L) % 3L;
      BlueprintMeta.setMetaLong(blueprint, Keys.CRAFT_MODE, next);
      if (next == 1L) {
         long remaining = BlueprintMeta.getMetaLong(blueprint, Keys.CRAFT_REMAINING, 0L);
         if (remaining <= 0L) {
            BlueprintMeta.setMetaLong(blueprint, Keys.CRAFT_TARGET, 0L);
            BlueprintMeta.setMetaLong(blueprint, Keys.CRAFT_REMAINING, 0L);
         }
      }

      AssemblyRound.updateStockFlag(state, rowIndex, blueprint);

      player.sendMessage(Lang.getString(switch ((int)next) {
         case 1 -> "messages.cellnet.assembly.plan_on";
         case 2 -> "messages.cellnet.assembly.replenish_on";
         default -> "messages.cellnet.assembly.continuous_on";
      }));
      menu.replaceExistingItem(AssemblyDrive.RECIPE_SLOTS[rowIndex], blueprint);
      AssemblyDisplay.refreshDisplays(menu, state, NetworkUtil.findRoot(menu.getLocation()), state.snapshot);
   }

   static void applyGear(@NotNull BlockMenu menu, int rowIndex, @NotNull ItemStack blueprint, @NotNull AssemblyRound.MachineState state, int gear) {
      BlueprintMeta.setMetaLong(blueprint, Keys.CRAFT_GEAR, (long)gear);
      AssemblyRound.updateStockFlag(state, rowIndex, blueprint);
      menu.replaceExistingItem(AssemblyDrive.RECIPE_SLOTS[rowIndex], blueprint);
      AssemblyDisplay.refreshDisplays(menu, state, NetworkUtil.findRoot(menu.getLocation()), state.snapshot);
   }

   static void handleTargetRequest(@NotNull Player player, @NotNull BlockMenu menu, int rowIndex, boolean keep) {
      int blueprintSlot = AssemblyDrive.RECIPE_SLOTS[rowIndex];
      ItemStack blueprint = menu.getItemInSlot(blueprintSlot);
      if (blueprint != null && !blueprint.getType().isAir() && AssemblyRound.isAcceptableRecipeItem(blueprint)) {
         boolean replaced = ChatInput.request(player, ChatInput.InputType.TARGET, new ChatInput.TargetContext(menu.getLocation().clone(), blueprintSlot, keep));
         if (replaced) {
            player.sendMessage(Lang.getString("messages.cellnet.input.previous_cancelled"));
         }

         player.sendMessage(Lang.getString(keep ? "messages.cellnet.input.keep_hint" : "messages.cellnet.input.target_hint"));
      }
   }

   public static void handleTargetInput(@NotNull Location location, int slot, long target, boolean keep) {
      BlockMenu menu = StorageCacheUtils.getMenu(location);
      if (menu != null) {
         ItemStack blueprint = menu.getItemInSlot(slot);
         if (blueprint != null && !blueprint.getType().isAir() && AssemblyRound.isAcceptableRecipeItem(blueprint)) {
            long capped = Math.max(0L, Math.min(target, Long.MAX_VALUE));
            AssemblyRound.MachineState state = AssemblyRound.runtimeState(location).machineState;
            if (keep) {
               BlueprintMeta.setMetaLong(blueprint, Keys.CRAFT_KEEP, capped);
            } else {
               BlueprintMeta.setMetaLong(blueprint, Keys.CRAFT_TARGET, capped);
               BlueprintMeta.setMetaLong(blueprint, Keys.CRAFT_REMAINING, capped);
               if (capped <= 0L) {
                  BlueprintMeta.setMetaLong(blueprint, Keys.CRAFT_MODE, 0L);
               }
            }

            if (state != null) {
               AssemblyRound.updateStockFlag(state, CellSlotUI.indexOf(AssemblyDrive.RECIPE_SLOTS, slot), blueprint);
            }

            menu.replaceExistingItem(slot, blueprint);

            for (HumanEntity human : menu.getInventory().getViewers()) {
               if (human instanceof Player p) {
                  p.sendMessage(Lang.getString(keep ? "messages.cellnet.assembly.keep_set" : "messages.cellnet.assembly.target_set", capped));
               }
            }
         } else {
            for (HumanEntity humanx : menu.getInventory().getViewers()) {
               if (humanx instanceof Player p) {
                  p.sendMessage(Lang.getString("messages.cellnet.assembly.target_slot_lost"));
               }
            }
         }
      }
   }

   public static record DriveOverview(
      @NotNull Location location,
      @Nullable String name,
      boolean enabled,
      boolean overclock,
      boolean smart,
      @NotNull List<AssemblyMonitorBridge.MonitorLine> lines
   ) {
   }

   public static record MonitorLine(@NotNull ItemStack output, long mode, int gear, long remaining, long keep, boolean blocked, boolean planArmed) {
   }
}
