package com.ytdd9527.networksexpansion.implementation.machines.cellnet.assembly;

import com.balugaq.netex.utils.Lang;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.assembly.core.OverclockCore;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.assembly.core.SmartCore;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.drive.DriveOwnership;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.drive.EnderDrive;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.drive.ender.ChannelConfigurator;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.NetworkUtil;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.ui.CellSlotUI;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.ui.Icons;
import com.ytdd9527.networksexpansion.utils.itemstacks.ItemStackUtil;
import io.github.sefiraat.networks.utils.Keys;
import io.github.thebusybiscuit.slimefun4.libraries.dough.data.persistent.PersistentDataAPI;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

final class AssemblyMenu {
   private AssemblyMenu() {
   }

   static void createPreset(@NotNull final AssemblyDrive drive) {
      new BlockMenuPreset(drive.getId(), drive.getItemName()) {
         public void init() {
            this.setSize(45);
            this.setPlayerInventoryClickable(true);
            this.drawBackground(AssemblyDrive.STATUS_BACKGROUND);
            this.drawBackground(AssemblyDrive.MAIN_BACKGROUND);
         }

         public boolean canOpen(@NotNull Block block, @NotNull Player player) {
            return DriveOwnership.bypasses(player)
               || DriveOwnership.passesGates(player, block.getLocation(), Interaction.INTERACT_BLOCK, drive.canUse(player, false));
         }

         public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
            return new int[0];
         }

         public void newInstance(@NotNull BlockMenu menu, @NotNull Block block) {
            AssemblyRound.DriveRuntimeState rs = AssemblyRound.runtimeState(block.getLocation());
            if (rs.machineState == null) {
               rs.machineState = new AssemblyRound.MachineState();
               rs.machineState.enabled = AssemblyDrive.isEnabled(block.getLocation());
            }

            AssemblyRound.MachineState state = rs.machineState;

            for (int slot : AssemblyDrive.RECIPE_SLOTS) {
               ItemStack existing = menu.getItemInSlot(slot);
               if (existing == null || existing.getType().isAir()) {
                  menu.replaceExistingItem(slot, AssemblyMenu.buildSlotMarker());
               }
            }

            AssemblyMenu.restoreCoreSlot(menu, 32, Icons.ASSEMBLY_UPGRADE_SLOT, OverclockCore::isOverclockCore);
            AssemblyMenu.restoreCoreSlot(menu, 33, Icons.ASSEMBLY_SMART_SLOT, SmartCore::isSmartCore);
            state.maxGear = OverclockCore.isOverclockCore(menu.getItemInSlot(32)) ? 13 : 12;
            state.nextRun = System.currentTimeMillis() + ThreadLocalRandom.current().nextLong(1000L);

            for (int i = 0; i < AssemblyDrive.RECIPE_SLOTS.length; i++) {
               AssemblyRound.updateStockFlag(state, i, menu.getItemInSlot(AssemblyDrive.RECIPE_SLOTS[i]));
            }

            AssemblyRound.ACTIVE_DRIVES.add(block.getLocation());
            AssemblyMenu.setupHandlers(menu, block.getLocation(), state);
            Arrays.fill(state.rowDisplays, null);
            state.lastStatusEnabled = null;
            state.lastStatusHadRoot = null;
            state.lastToggleEnabled = null;
            AssemblyDisplay.refreshDisplays(menu, state, NetworkUtil.findRoot(block.getLocation()), null);
         }
      };
   }

   private static void restoreCoreSlot(@NotNull BlockMenu menu, int slot, @NotNull ItemStack marker, @NotNull Predicate<ItemStack> coreTest) {
      ItemStack current = menu.getItemInSlot(slot);
      if (current == null || current.getType().isAir() || !coreTest.test(current)) {
         menu.replaceExistingItem(slot, marker);
      }
   }

   private static void setupHandlers(@NotNull BlockMenu menu, @NotNull Location location, @NotNull AssemblyRound.MachineState state) {
      menu.addMenuClickHandler(4, (p, s, i, a) -> false);

      for (int slot : AssemblyDrive.RECIPE_SLOTS) {
         menu.addMenuClickHandler(slot, (player, s, clicked, action) -> {
            handleRecipeSlotClick(player, menu, state, s);
            return false;
         });
      }

      menu.replaceExistingItem(44, buildClearSlotsButton());
      menu.addMenuClickHandler(44, (player, slotx, clicked, action) -> {
         clearRecipeSlots(player, menu, state);
         return false;
      });

      for (int i = 0; i < AssemblyDrive.TASK_DISPLAY_SLOTS.length; i++) {
         int row = i;
         menu.addMenuClickHandler(AssemblyDrive.TASK_DISPLAY_SLOTS[row], (player, slotx, clicked, action) -> {
            handleTaskRowClick(player, menu, state, row, action.isRightClicked(), action.isShiftClicked());
            AssemblyDisplay.refreshDisplays(menu, state, NetworkUtil.findRoot(location), null);
            return false;
         });
      }

      menu.addMenuClickHandler(31, (player, slotx, clicked, action) -> {
         boolean newValue = !AssemblyDrive.isEnabled(location);
         AssemblyDrive.setEnabled(location, newValue);
         state.enabled = newValue;
         player.sendMessage(Lang.getString(newValue ? "messages.cellnet.assembly.switch_on" : "messages.cellnet.assembly.switch_off"));
         AssemblyDisplay.refreshDisplays(menu, state, newValue ? NetworkUtil.findRoot(location) : null, null);
         return false;
      });
      menu.addMenuClickHandler(
         32,
         (player, slotx, clicked, action) -> {
            handleCoreSlotClick(
               player, menu, location, state, 32, Icons.ASSEMBLY_UPGRADE_SLOT, OverclockCore::isOverclockCore, "messages.cellnet.assembly.upgrade_invalid"
            );
            return false;
         }
      );
      menu.addMenuClickHandler(33, (player, slotx, clicked, action) -> {
         handleSmartSlotClick(player, menu, location, state);
         return false;
      });
      menu.addMenuOpeningHandler(p -> {
         AssemblyDisplay.refreshDisplays(menu, state, NetworkUtil.findRoot(location), null);
         menu.replaceExistingItem(44, buildClearSlotsButton());
      });
      menu.addMenuCloseHandler(p -> {
         menu.replaceExistingItem(4, null);

         for (int slotx : AssemblyDrive.TASK_DISPLAY_SLOTS) {
            menu.replaceExistingItem(slotx, null);
         }

         menu.replaceExistingItem(31, null);

         for (int i = 0; i < state.rowDisplays.length; i++) {
            state.rowDisplays[i] = null;
         }

         state.lastStatusEnabled = null;
         state.lastStatusHadRoot = null;
         state.lastToggleEnabled = null;
      });
   }

   private static void handleCoreSlotClick(
      @NotNull Player player,
      @NotNull BlockMenu menu,
      @NotNull Location location,
      @NotNull AssemblyRound.MachineState state,
      int slot,
      @NotNull ItemStack marker,
      @NotNull Predicate<ItemStack> filter,
      @NotNull String invalidKey
   ) {
      ItemStack cursor = player.getItemOnCursor();
      ItemStack current = menu.getItemInSlot(slot);
      boolean cursorCore = cursor != null && !cursor.getType().isAir() && filter.test(cursor);
      boolean slotCore = current != null && !current.getType().isAir() && filter.test(current);
      if (cursorCore) {
         if (slotCore) {
            ItemStackUtil.giveOrDropItem(player, current);
         }

         CellSlotUI.placeOneFromCursor(player, menu, slot, cursor);
      } else if (cursor != null && !cursor.getType().isAir()) {
         player.sendMessage(Lang.getString(invalidKey));
      } else if (slotCore) {
         menu.replaceExistingItem(slot, marker);
         player.setItemOnCursor(current);
      }

      state.maxGear = OverclockCore.isOverclockCore(menu.getItemInSlot(32)) ? 13 : 12;

      for (int i = 0; i < AssemblyDrive.RECIPE_SLOTS.length; i++) {
         AssemblyRound.updateStockFlag(state, i, menu.getItemInSlot(AssemblyDrive.RECIPE_SLOTS[i]));
      }

      AssemblyDisplay.refreshDisplays(menu, state, NetworkUtil.findRoot(location), null);
   }

   private static void handleSmartSlotClick(
      @NotNull Player player, @NotNull BlockMenu menu, @NotNull Location location, @NotNull AssemblyRound.MachineState state
   ) {
      ItemStack cursor = player.getItemOnCursor();
      ItemStack current = menu.getItemInSlot(33);
      if (!ChannelConfigurator.isConfigurator(cursor)) {
         handleCoreSlotClick(player, menu, location, state, 33, Icons.ASSEMBLY_SMART_SLOT, SmartCore::isSmartCore, "messages.cellnet.assembly.smart_invalid");
      } else if (current != null && SmartCore.isSmartCore(current)) {
         String channel = ChannelConfigurator.getStoredChannel(cursor);
         boolean fresh = channel == null || channel.isEmpty();
         if (fresh) {
            channel = EnderDrive.generateChannel();
            ChannelConfigurator.setStoredChannel(cursor, channel);
         }

         SmartCore.setChannel(current, channel);
         player.sendMessage(Lang.getString(fresh ? "messages.cellnet.configurator.bound_new" : "messages.cellnet.configurator.bound", channel));
         AssemblyDisplay.refreshDisplays(menu, state, NetworkUtil.findRoot(location), null);
      } else {
         player.sendMessage(Lang.getString("messages.cellnet.assembly.smart_slot_no_core"));
      }
   }

   private static void clearRecipeSlots(@NotNull Player player, @NotNull BlockMenu menu, @NotNull AssemblyRound.MachineState state) {
      boolean hasCard = false;

      for (int slot : AssemblyDrive.RECIPE_SLOTS) {
         ItemStack item = menu.getItemInSlot(slot);
         if (item != null && !item.getType().isAir() && !AssemblyRound.isSlotMarker(item)) {
            hasCard = true;
            break;
         }
      }

      if (!hasCard) {
         player.sendMessage(Lang.getString("messages.cellnet.assembly.clear_slots_empty"));
      } else {
         for (int i = 0; i < AssemblyDrive.RECIPE_SLOTS.length; i++) {
            ItemStack item = menu.getItemInSlot(AssemblyDrive.RECIPE_SLOTS[i]);
            if (item != null && !item.getType().isAir() && !AssemblyRound.isSlotMarker(item)) {
               menu.replaceExistingItem(AssemblyDrive.RECIPE_SLOTS[i], null);
               ItemStackUtil.giveOrDropItem(player, item);
            }

            menu.replaceExistingItem(AssemblyDrive.RECIPE_SLOTS[i], buildSlotMarker());
            AssemblyRound.updateStockFlag(state, i, menu.getItemInSlot(AssemblyDrive.RECIPE_SLOTS[i]));
         }

         AssemblyDisplay.refreshDisplays(menu, state, NetworkUtil.findRoot(menu.getLocation()), null);
         player.sendMessage(Lang.getString("messages.cellnet.assembly.slots_cleared"));
      }
   }

   private static void handleRecipeSlotClick(@NotNull Player player, @NotNull BlockMenu menu, @NotNull AssemblyRound.MachineState state, int slot) {
      ItemStack cursor = player.getItemOnCursor();
      ItemStack slotItem = menu.getItemInSlot(slot);
      boolean slotHasCard = slotItem != null && !slotItem.getType().isAir() && !AssemblyRound.isSlotMarker(slotItem);
      if (cursor != null && !cursor.getType().isAir()) {
         if (!AssemblyRound.isAcceptableRecipeItem(cursor)) {
            player.sendMessage(Lang.getString("messages.cellnet.assembly.blueprint_invalid"));
         } else {
            if (slotHasCard) {
               menu.replaceExistingItem(slot, null);
               ItemStackUtil.giveOrDropItem(player, slotItem);
            } else {
               menu.replaceExistingItem(slot, null);
            }

            ItemStack placed = CellSlotUI.placeOneFromCursor(player, menu, slot, cursor);
            AssemblyRound.updateStockFlag(state, CellSlotUI.indexOf(AssemblyDrive.RECIPE_SLOTS, slot), placed);
         }
      } else {
         if (slotHasCard) {
            menu.replaceExistingItem(slot, buildSlotMarker());
            player.setItemOnCursor(slotItem);
            AssemblyRound.updateStockFlag(state, CellSlotUI.indexOf(AssemblyDrive.RECIPE_SLOTS, slot), null);
         }
      }
   }

   @NotNull
   private static ItemStack buildSlotMarker() {
      ItemStack marker = Icons.ASSEMBLY_SLOT_MARKER.clone();
      ItemMeta meta = marker.getItemMeta();
      if (meta != null) {
         PersistentDataAPI.setByte(meta, Keys.ASSEMBLY_SLOT_MARKER, (byte)1);
         marker.setItemMeta(meta);
      }

      return marker;
   }

   @NotNull
   private static ItemStack buildClearSlotsButton() {
      return Icons.ASSEMBLY_DRIVE_CLEAR_SLOTS;
   }

   private static void handleTaskRowClick(
      @NotNull Player player, @NotNull BlockMenu menu, @NotNull AssemblyRound.MachineState state, int rowIndex, boolean rightClick, boolean shift
   ) {
      int slot = AssemblyDrive.RECIPE_SLOTS[rowIndex];
      ItemStack blueprint = menu.getItemInSlot(slot);
      if (blueprint != null && !blueprint.getType().isAir() && AssemblyRound.isAcceptableRecipeItem(blueprint)) {
         if (shift && rightClick) {
            long mode = BlueprintMeta.getMetaLong(blueprint, Keys.CRAFT_MODE, 0L);
            if (mode != 0L) {
               AssemblyMonitorBridge.handleTargetRequest(player, menu, rowIndex, mode == 2L);
            }
         } else if (shift) {
            AssemblyMonitorBridge.toggleMode(player, menu, rowIndex, blueprint, state);
         } else {
            int current = Math.min(AssemblyRound.gearOf(blueprint), state.maxGear);
            if (rightClick) {
               AssemblyMonitorBridge.applyGear(menu, rowIndex, blueprint, state, Math.max(0, current - 1));
            } else {
               AssemblyMonitorBridge.applyGear(menu, rowIndex, blueprint, state, Math.min(state.maxGear, current + 1));
            }
         }
      }
   }
}
