package com.ytdd9527.networksexpansion.implementation.machines.cellnet.assembly;

import com.balugaq.netex.utils.Lang;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.GhostItems;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.NumberFormat;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.ui.Icons;
import io.github.sefiraat.networks.network.NetworkRoot;
import java.util.ArrayList;
import java.util.List;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class AssemblyDisplay {
   private AssemblyDisplay() {
   }

   static void refreshIdleStatusIcon(@NotNull BlockMenu blockMenu, @NotNull AssemblyRound.MachineState state, @Nullable NetworkRoot root) {
      boolean hasRoot = root != null;
      if (state.lastStatusEnabled == null || state.lastStatusHadRoot == null || state.lastStatusEnabled != state.enabled || state.lastStatusHadRoot != hasRoot) {
         refreshStatusIcon(blockMenu, state.enabled, root);
         state.lastStatusEnabled = state.enabled;
         state.lastStatusHadRoot = hasRoot;
      }
   }

   static void refreshDisplays(
      @NotNull BlockMenu blockMenu, @NotNull AssemblyRound.MachineState state, @Nullable NetworkRoot root, @Nullable AssemblyRound.CachedSnapshot snapshot
   ) {
      if (state.lastStatusEnabled == null
         || state.lastStatusHadRoot == null
         || state.lastStatusEnabled != state.enabled
         || state.lastStatusHadRoot != (root != null)) {
         refreshStatusIcon(blockMenu, state.enabled, root);
         state.lastStatusEnabled = state.enabled;
         state.lastStatusHadRoot = root != null;
      }

      refreshTaskRows(blockMenu, state, snapshot);
      if (state.lastToggleEnabled == null || state.lastToggleEnabled != state.enabled) {
         refreshToggleIcon(blockMenu, state.enabled);
         state.lastToggleEnabled = state.enabled;
      }
   }

   private static void refreshStatusIcon(@NotNull BlockMenu blockMenu, boolean enabled, @Nullable NetworkRoot root) {
      ItemStack display = new ItemStack(Material.OBSERVER);
      display.editMeta(
         meta -> {
            meta.setDisplayName(Lang.getString("messages.cellnet.assembly.status_name"));
            List<String> lore = new ArrayList<>();
            if (enabled) {
               lore.add(
                  Lang.getString(
                     "messages.cellnet.assembly.status_phase",
                     Lang.getString(root == null ? "messages.cellnet.assembly.phase_no_network" : "messages.cellnet.assembly.phase_active")
                  )
               );
            }

            meta.setLore(lore);
         }
      );
      GhostItems.mark(display);
      blockMenu.replaceExistingItem(4, display);
   }

   private static void refreshTaskRows(@NotNull BlockMenu blockMenu, @NotNull AssemblyRound.MachineState state, @Nullable AssemblyRound.CachedSnapshot snapshot) {
      for (int i = 0; i < AssemblyDrive.RECIPE_SLOTS.length; i++) {
         int slot = AssemblyDrive.RECIPE_SLOTS[i];
         ItemStack blueprint = blockMenu.getItemInSlot(slot);
         AssemblyRound.Recipe recipe = AssemblyRound.readRecipe(blockMenu, i, blueprint, state);
         boolean hasLine = recipe != null && blueprint != null;
         long target = hasLine ? state.cachedTarget[i] : 0L;
         long mode = hasLine ? state.cachedMode[i] : 0L;
         long remaining = hasLine ? state.cachedRemaining[i] : 0L;
         long keep = hasLine && mode == 2L ? state.cachedKeep[i] : 0L;
         long lineCrafted = Math.max(0L, state.craftedSince[i]);
         long craftedSig = mode == 2L && keep > 0L && snapshot != null ? lineCrafted : 0L;
         int gear = hasLine ? state.cachedGear[i] : 1;
         boolean blocked = recipe != null && state.blocked[i];
         boolean empty = recipe == null || target > Long.MAX_VALUE;
         AssemblyRound.RowDisplay cache = state.rowDisplays[i];
         if (cache == null) {
            cache = new AssemblyRound.RowDisplay();
            state.rowDisplays[i] = cache;
         }

         if (cache.icon == null
            || cache.empty != empty
            || cache.recipe != recipe
            || cache.snapshotRef != snapshot
            || cache.mode != mode
            || cache.target != target
            || cache.remaining != remaining
            || cache.keep != keep
            || cache.crafted != craftedSig
            || cache.gear != gear
            || cache.blocked != blocked) {
            ItemStack display = empty
               ? Icons.ASSEMBLY_TASK_EMPTY
               : buildTaskRowIcon(recipe, mode, target, remaining, keep, lineCrafted, gear, blocked, snapshot);
            GhostItems.mark(display);
            blockMenu.replaceExistingItem(AssemblyDrive.TASK_DISPLAY_SLOTS[i], display);
            cache.recipe = recipe;
            cache.snapshotRef = snapshot;
            cache.mode = mode;
            cache.target = target;
            cache.remaining = remaining;
            cache.keep = keep;
            cache.crafted = craftedSig;
            cache.gear = gear;
            cache.blocked = blocked;
            cache.empty = empty;
            cache.icon = display;
         }
      }
   }

   @NotNull
   private static ItemStack buildTaskRowIcon(
      @NotNull AssemblyRound.Recipe recipe,
      long mode,
      long target,
      long remaining,
      long keep,
      long lineCrafted,
      int gear,
      boolean blocked,
      @Nullable AssemblyRound.CachedSnapshot snapshot
   ) {
      ItemStack display = recipe.output().clone();
      display.setAmount(1);
      display.editMeta(
         meta -> {
            meta.setDisplayName(Lang.getString("messages.cellnet.assembly.task_row_title"));
            List<String> lore = new ArrayList<>();
            lore.add(Lang.getString("messages.cellnet.assembly.task_gear", gearScale(gear), gearWord(gear)));
            if (gear >= 13) {
               lore.add(Lang.getString("messages.cellnet.assembly.task_gear_max_note"));
            }

            if (blocked) {
               lore.add(Lang.getString("messages.cellnet.assembly.task_blocked"));
            } else if (mode == 1L) {
               if (target <= 0L) {
                  lore.add(Lang.getString("messages.cellnet.assembly.task_plan_unset"));
               } else if (remaining <= 0L) {
                  lore.add(Lang.getString("messages.cellnet.assembly.task_plan_done"));
               } else {
                  lore.add(Lang.getString("messages.cellnet.assembly.task_plan", NumberFormat.formatNumber(remaining)));
               }
            } else if (mode == 2L) {
               lore.add(
                  Lang.getString(
                     keep > 0L ? "messages.cellnet.assembly.task_replenish" : "messages.cellnet.assembly.task_replenish_unset", NumberFormat.formatNumber(keep)
                  )
               );
            } else {
               lore.add(Lang.getString("messages.cellnet.assembly.task_continuous"));
            }

            lore.add(Lang.getString("messages.cellnet.assembly.task_hint"));
            if (mode == 2L && keep > 0L && snapshot != null) {
               long stock = AssemblyRound.snapshotStock(snapshot, recipe.outputExactKey()) + lineCrafted;
               String status = Lang.getString(stock >= keep ? "messages.cellnet.assembly.task_replenish_ok" : "messages.cellnet.assembly.task_replenish_doing");
               lore.add(
                  Lang.getString("messages.cellnet.assembly.task_replenish_stock", NumberFormat.formatNumber(stock), NumberFormat.formatNumber(keep), status)
               );
            } else if (snapshot != null) {
               long craftable = AssemblyRound.craftableFromSnapshot(recipe, snapshot);
               lore.add(
                  craftable == Long.MAX_VALUE
                     ? Lang.getString("messages.cellnet.assembly.task_available_infinite")
                     : Lang.getString("messages.cellnet.assembly.task_available", NumberFormat.formatNumber(craftable))
               );
            }

            meta.setLore(lore);
         }
      );
      return display;
   }

   @NotNull
   private static String gearScale(int gear) {
      StringBuilder sb = new StringBuilder();

      for (int i = 0; i < AssemblyRound.GEAR_LADDER.length; i++) {
         sb.append(Lang.getString(i < gear ? "messages.cellnet.assembly.gear_on" : "messages.cellnet.assembly.gear_off"));
      }

      return sb.toString();
   }

   @NotNull
   private static String gearWord(int gear) {
      if (gear <= 0) {
         return Lang.getString("messages.cellnet.assembly.gear_stop");
      } else {
         return gear >= 13
            ? Lang.getString("messages.cellnet.assembly.gear_max")
            : Lang.getString("messages.cellnet.assembly.gear_level", AssemblyRound.GEAR_LADDER[gear - 1]);
      }
   }

   private static void refreshToggleIcon(@NotNull BlockMenu blockMenu, boolean enabled) {
      ItemStack icon = (enabled ? Icons.ASSEMBLY_SWITCH_ON : Icons.ASSEMBLY_SWITCH_OFF).clone();
      GhostItems.mark(icon);
      blockMenu.replaceExistingItem(31, icon);
   }
}
