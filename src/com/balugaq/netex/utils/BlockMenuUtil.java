package com.balugaq.netex.utils;

import com.ytdd9527.networksexpansion.utils.itemstacks.ItemStackUtil;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public final class BlockMenuUtil {
   @Nullable
   public static ItemStack pushItem(@NotNull BlockMenu blockMenu, @Nullable ItemStack item, @NotNull int... slots) {
      if (item != null && item.getType() != Material.AIR) {
         int leftAmount = item.getAmount();

         for (int slot : slots) {
            if (leftAmount <= 0) {
               break;
            }

            ItemStack existing = blockMenu.getItemInSlot(slot);
            if (existing != null && existing.getType() != Material.AIR) {
               int existingAmount = existing.getAmount();
               if (existingAmount < item.getMaxStackSize() && StackUtils.itemsMatch(item, existing)) {
                  int received = Math.max(0, Math.min(item.getMaxStackSize() - existingAmount, leftAmount));
                  leftAmount -= received;
                  existing.setAmount(existingAmount + received);
                  item.setAmount(leftAmount);
               }
            } else {
               int received = Math.min(leftAmount, item.getMaxStackSize());
               blockMenu.replaceExistingItem(slot, StackUtils.getAsQuantity(item, received));
               leftAmount -= received;
               item.setAmount(Math.max(0, leftAmount));
            }
         }

         return leftAmount > 0 ? new CustomItemStack(item, leftAmount) : null;
      } else {
         return null;
      }
   }

   @NotNull
   public static Map<ItemStack, Integer> pushItem(@NotNull BlockMenu blockMenu, @Nullable @NotNull ItemStack[] items, @NotNull int... slots) {
      if (items != null && items.length != 0) {
         List<ItemStack> listItems = new ArrayList<>();

         for (ItemStack item : items) {
            if (item != null && item.getType() != Material.AIR) {
               listItems.add(item);
            }
         }

         return pushItem(blockMenu, listItems, slots);
      } else {
         return new HashMap<>();
      }
   }

   @NotNull
   public static Map<ItemStack, Integer> pushItem(@NotNull BlockMenu blockMenu, @Nullable List<ItemStack> items, @NotNull int... slots) {
      if (items != null && !items.isEmpty()) {
         Map<ItemStack, Integer> itemMap = new HashMap<>();

         for (ItemStack item : items) {
            if (item != null && item.getType() != Material.AIR) {
               ItemStack leftOver = pushItem(blockMenu, item, slots);
               if (leftOver != null) {
                  itemMap.put(leftOver, itemMap.getOrDefault(leftOver, 0) + leftOver.getAmount());
               }
            }
         }

         return itemMap;
      } else {
         return new HashMap<>();
      }
   }

   public static boolean fits(@NotNull BlockMenu blockMenu, @Nullable ItemStack item, @NotNull int... slots) {
      if (item != null && item.getType() != Material.AIR) {
         int incoming = item.getAmount();

         for (int slot : slots) {
            ItemStack stack = blockMenu.getItemInSlot(slot);
            if (stack == null || stack.getType() == Material.AIR) {
               incoming -= item.getMaxStackSize();
            } else if (stack.getMaxStackSize() > stack.getAmount() && StackUtils.itemsMatch(item, stack)) {
               incoming -= stack.getMaxStackSize() - stack.getAmount();
            }

            if (incoming <= 0) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   public static boolean fits(@NotNull BlockMenu blockMenu, @Nullable ItemStack[] items, @NotNull int... slots) {
      if (items != null && items.length != 0) {
         List<ItemStack> listItems = new ArrayList<>();

         for (ItemStack item : items) {
            if (item != null && item.getType() != Material.AIR) {
               listItems.add(item.clone());
            }
         }

         return fits(blockMenu, listItems, slots);
      } else {
         return false;
      }
   }

   public static boolean fits(@NotNull BlockMenu blockMenu, @Nullable List<ItemStack> items, @NotNull int... slots) {
      if (items != null && !items.isEmpty()) {
         List<ItemStack> cloneMenu = new ArrayList<>();

         for (int i = 0; i < 54; i++) {
            cloneMenu.add(null);
         }

         for (int slot : slots) {
            ItemStack stack = blockMenu.getItemInSlot(slot);
            if (stack != null && stack.getType() != Material.AIR) {
               cloneMenu.set(slot, stack.clone());
            } else {
               cloneMenu.set(slot, null);
            }
         }

         for (ItemStack rawItem : items) {
            ItemStack item = rawItem.clone();
            int leftAmount = item.getAmount();

            for (int slotx : slots) {
               if (leftAmount <= 0) {
                  break;
               }

               ItemStack existing = cloneMenu.get(slotx);
               if (existing != null && existing.getType() != Material.AIR) {
                  int existingAmount = existing.getAmount();
                  if (existingAmount < item.getMaxStackSize() && StackUtils.itemsMatch(item, existing)) {
                     int received = Math.max(0, Math.min(item.getMaxStackSize() - existingAmount, leftAmount));
                     leftAmount -= received;
                     existing.setAmount(existingAmount + received);
                     item.setAmount(leftAmount);
                  }
               } else {
                  int received = Math.min(leftAmount, item.getMaxStackSize());
                  cloneMenu.set(slotx, StackUtils.getAsQuantity(item, leftAmount));
                  leftAmount -= received;
                  item.setAmount(Math.max(0, leftAmount));
               }
            }

            if (leftAmount > 0) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public static void consumeItem(@NotNull BlockMenu blockMenu, @Range(from = 0L,to = 64L) int slot) {
      consumeItem(blockMenu, slot, 1);
   }

   public static void consumeItem(@NotNull BlockMenu blockMenu, @Range(from = 0L,to = 53L) int slot, boolean replaceConsumables) {
      consumeItem(blockMenu, slot, 1, replaceConsumables);
   }

   public static void consumeItem(@NotNull BlockMenu blockMenu, @Range(from = 0L,to = 53L) int slot, @Range(from = 0L,to = 64L) int amount) {
      consumeItem(blockMenu, slot, amount, false);
   }

   public static void consumeItem(
      @NotNull BlockMenu blockMenu, @Range(from = 0L,to = 53L) int slot, @Range(from = 0L,to = 64L) int amount, boolean replaceConsumables
   ) {
      if (amount != 0) {
         ItemStack item = blockMenu.getItemInSlot(slot);
         if (item != null && item.getType() != Material.AIR) {
            ItemStackUtil.consumeItem(item, amount, replaceConsumables);
            if (item.getAmount() <= 0) {
               blockMenu.replaceExistingItem(slot, null);
            }
         }
      }
   }

   @Generated
   private BlockMenuUtil() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
