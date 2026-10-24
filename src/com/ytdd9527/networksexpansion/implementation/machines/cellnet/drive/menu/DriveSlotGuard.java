package com.ytdd9527.networksexpansion.implementation.machines.cellnet.drive.menu;

import com.ytdd9527.networksexpansion.implementation.machines.cellnet.drive.CellDrive;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class DriveSlotGuard {
   private DriveSlotGuard() {
   }

   static boolean isCellSlot(int rawSlot) {
      for (int slot : CellDrive.CELL_SLOTS) {
         if (slot == rawSlot) {
            return true;
         }
      }

      return false;
   }

   @Nullable
   static Inventory topInventory(@NotNull InventoryEvent event) {
      return event.getView().getTopInventory();
   }

   @Nullable
   static ItemStack resolvePlacedItem(@NotNull InventoryClickEvent event) {
      Inventory top = topInventory(event);
      if (top == null) {
         return null;
      } else {
         int rawSlot = event.getRawSlot();
         boolean clickedTop = rawSlot >= 0 && rawSlot < top.getSize();
         ClickType click = event.getClick();
         if (click == ClickType.NUMBER_KEY) {
            int hotbar = event.getHotbarButton();
            return hotbar >= 0 && clickedTop && isCellSlot(rawSlot) ? event.getWhoClicked().getInventory().getItem(hotbar) : null;
         } else if (click == ClickType.SWAP_OFFHAND) {
            return clickedTop && isCellSlot(rawSlot) ? event.getWhoClicked().getInventory().getItemInOffHand() : null;
         } else if (click == ClickType.DOUBLE_CLICK) {
            return event.getCursor();
         } else if (click.isShiftClick()) {
            return clickedTop ? null : event.getCurrentItem();
         } else {
            return clickedTop && isCellSlot(rawSlot) ? event.getCursor() : null;
         }
      }
   }
}
