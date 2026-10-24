package com.balugaq.netex.api.atrributes;

import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.utils.StackUtils;
import java.util.ArrayList;
import java.util.List;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface WhitelistedGrabber {
   @NotNull
   default List<ItemStack> getClonedTemplateItems(@NotNull BlockMenu menu) {
      List<ItemStack> items = new ArrayList<>();

      for (int slot : this.getTemplateSlots()) {
         ItemStack item = menu.getItemInSlot(slot);
         if (item != null && item.getType() != Material.AIR) {
            items.add(item.clone());
         }
      }

      return items;
   }

   default boolean inTemplates(@NotNull List<ItemStack> templates, @Nullable ItemStack stack) {
      if (stack != null && stack.getType() != Material.AIR) {
         for (ItemStack template : templates) {
            if (StackUtils.itemsMatch(template, stack)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   default void grabMenu(@NotNull BlockMenu blockMenu, @NotNull BlockMenu targetMenu, @NotNull NetworkRoot root, @NotNull List<ItemStack> templates) {
      int[] slots = targetMenu.getPreset().getSlotsAccessedByItemTransport(targetMenu, ItemTransportFlow.WITHDRAW, null);

      for (int slot : slots) {
         ItemStack itemInSlot = targetMenu.getItemInSlot(slot);
         if (itemInSlot != null && itemInSlot.getType() != Material.AIR) {
            boolean found = false;

            for (ItemStack template : templates) {
               if (StackUtils.itemsMatch(template, itemInSlot)) {
                  int before = itemInSlot.getAmount();
                  root.addItemStack(blockMenu.getLocation(), itemInSlot);
                  found = true;
                  break;
               }
            }

            if (found) {
               break;
            }
         }
      }
   }

   int[] getTemplateSlots();
}
