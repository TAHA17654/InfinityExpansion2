package com.balugaq.netex.utils;

import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.utils.StackUtils;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class InventoryUtil {
   public static void addItem(Player player, ItemStack... toAdd) {
      addItem(player.getInventory(), toAdd);
      player.updateInventory();
   }

   public static void addItem(InventoryHolder holder, ItemStack... toAdds) {
      addItem(holder.getInventory(), toAdds);
   }

   public static void addItem(Inventory inventory, ItemStack... toAdds) {
      ItemStack[] storage = inventory.getStorageContents();
      if (storage != null) {
         for (ItemStack toAdd : toAdds) {
            if (toAdd != null && toAdd.getType() != Material.AIR && toAdd.getAmount() > 0) {
               int remaining = toAdd.getAmount();

               while (remaining > 0) {
                  int index = firstSimilar(storage, toAdd);
                  if (index == -1) {
                     break;
                  }

                  ItemStack exist = storage[index];
                  int canAdd = Math.min(exist.getMaxStackSize() - exist.getAmount(), remaining);
                  if (canAdd <= 0) {
                     break;
                  }

                  exist.setAmount(exist.getAmount() + canAdd);
                  remaining -= canAdd;
               }

               while (remaining > 0) {
                  int indexx = firstEmpty(storage);
                  if (indexx == -1) {
                     break;
                  }

                  int putAmount = Math.min(remaining, toAdd.getMaxStackSize());
                  storage[indexx] = toAdd.asQuantity(putAmount);
                  remaining -= putAmount;
               }

               toAdd.setAmount(remaining);
            }
         }

         inventory.setStorageContents(storage);
      }
   }

   public static int firstSimilar(ItemStack[] storage, ItemStack item) {
      return firstSimilar(storage, item, true);
   }

   public static int firstSimilar(ItemStack[] storage, ItemStack item, boolean withoutAmount) {
      for (int i = 0; i < storage.length; i++) {
         ItemStack stack = storage[i];
         if (stack != null && stack.getAmount() < stack.getMaxStackSize() && StackUtils.itemsMatch(stack, item, true, !withoutAmount)) {
            return i;
         }
      }

      return -1;
   }

   public static int firstEmpty(ItemStack[] storage) {
      for (int i = 0; i < storage.length; i++) {
         ItemStack stack = storage[i];
         if (stack == null || stack.getType() == Material.AIR) {
            return i;
         }
      }

      return -1;
   }

   public static void give(Player player, @Nullable ItemStack stack) {
      if (stack != null) {
         addItem(player, stack);
         if (stack.getAmount() > 0) {
            Bukkit.getScheduler().runTask(Networks.getInstance(), () -> player.getWorld().dropItem(player.getLocation(), stack));
         }
      }
   }

   @Generated
   private InventoryUtil() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
