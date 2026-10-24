package com.ytdd9527.networksexpansion.utils.itemstacks;

import com.balugaq.netex.api.data.ItemAmountWrapper;
import com.balugaq.netex.api.data.ItemWrapper;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class MachineUtil {
   public static final BlockPlaceHandler BLOCK_PLACE_HANDLER_PLACER_ALLOW = new BlockPlaceHandler(true) {
      public void onPlayerPlace(@NotNull BlockPlaceEvent blockPlaceEvent) {
      }
   };
   public static final BlockPlaceHandler BLOCK_PLACE_HANDLER_PLACER_DENY = new BlockPlaceHandler(false) {
      public void onPlayerPlace(@NotNull BlockPlaceEvent blockPlaceEvent) {
      }
   };
   public static final BlockPlaceHandler BLOCK_PLACE_HANDLER_DENY = new BlockPlaceHandler(false) {
      public void onPlayerPlace(@NotNull BlockPlaceEvent blockPlaceEvent) {
         blockPlaceEvent.setCancelled(true);
      }
   };

   @NotNull
   public static BlockBreakHandler simpleBlockBreakerHandler() {
      return new BlockBreakHandler(false, true) {
         public void onPlayerBreak(@NotNull BlockBreakEvent blockBreakEvent, @NotNull ItemStack itemStack, @NotNull List<ItemStack> list) {
         }
      };
   }

   @NotNull
   public static BlockBreakHandler simpleBlockBreakerHandler(@NotNull final int... slot) {
      return new BlockBreakHandler(false, true) {
         public void onPlayerBreak(@NotNull BlockBreakEvent blockBreakEvent, @NotNull ItemStack itemStack, @NotNull List<ItemStack> list) {
            Location location = blockBreakEvent.getBlock().getLocation();
            BlockMenu blockMenu = BlockStorage.getInventory(location);
            blockMenu.dropItems(location, slot);
         }
      };
   }

   public static int slotCount(@NotNull Inventory inventory, @NotNull int[] slots) {
      int count = 0;

      for (int slot : slots) {
         ItemStack itemStack = inventory.getItem(slot);
         if (!ItemStackUtil.isItemNull(itemStack)) {
            count++;
         }
      }

      return count;
   }

   public static boolean isFull(@NotNull Inventory inventory, @NotNull int[] slots) {
      for (int slot : slots) {
         ItemStack itemStack = inventory.getItem(slot);
         if (ItemStackUtil.isItemNull(itemStack) || itemStack.getAmount() < itemStack.getMaxStackSize()) {
            return false;
         }
      }

      return true;
   }

   public static boolean isEmpty(@NotNull Inventory inventory, @NotNull int[] slots) {
      for (int slot : slots) {
         ItemStack itemStack = inventory.getItem(slot);
         if (!ItemStackUtil.isItemNull(itemStack)) {
            return false;
         }
      }

      return true;
   }

   public static void stockSlots(@NotNull Inventory inventory, @NotNull int[] slots) {
      List<ItemWrapper> items = new ArrayList<>(slots.length);
      ItemWrapper itemWrapper = new ItemWrapper();

      for (int slot : slots) {
         ItemStack stockingItem = inventory.getItem(slot);
         if (!ItemStackUtil.isItemNull(stockingItem)) {
            itemWrapper.newWrap(stockingItem);

            for (ItemWrapper stockedItem : items) {
               ItemStackUtil.stack(itemWrapper, stockedItem);
            }

            if (stockingItem.getAmount() > 0 && stockingItem.getAmount() < stockingItem.getMaxStackSize()) {
               items.add(itemWrapper.shallowClone());
            }
         }
      }
   }

   @NotNull
   public static List<ItemWrapper> getItemList(@NotNull Inventory inventory, @NotNull int[] slots) {
      List<ItemWrapper> itemWrapperList = new ArrayList<>();

      for (int filterSlot : slots) {
         if (!ItemStackUtil.isItemNull(inventory.getItem(filterSlot))) {
            itemWrapperList.add(new ItemWrapper(inventory.getItem(filterSlot)));
         }
      }

      return itemWrapperList;
   }

   @NotNull
   public static Map<Integer, ItemWrapper> getSlotItemWrapperMap(@NotNull Inventory inventory, @NotNull int[] slots) {
      Map<Integer, ItemWrapper> itemMap = new LinkedHashMap<>(slots.length);

      for (int slot : slots) {
         ItemStack item = inventory.getItem(slot);
         if (!ItemStackUtil.isItemNull(item)) {
            itemMap.put(slot, new ItemWrapper(item));
         }
      }

      return itemMap;
   }

   @NotNull
   public static List<ItemAmountWrapper> calItemListWithAmount(@NotNull Inventory inventory, @NotNull int[] slots) {
      List<ItemAmountWrapper> itemAmountWrapperList = new ArrayList<>(slots.length);
      ItemAmountWrapper itemAmountWrapper = new ItemAmountWrapper();

      for (int slot : slots) {
         ItemStack item = inventory.getItem(slot);
         if (!ItemStackUtil.isItemNull(item)) {
            itemAmountWrapper.newWrap(item);
            boolean find = false;

            for (ItemAmountWrapper existedItemWrapper : itemAmountWrapperList) {
               if (StackUtils.itemsMatch(itemAmountWrapper.getItemStack(), existedItemWrapper.getItemStack())) {
                  existedItemWrapper.addAmount(item.getAmount());
                  find = true;
                  break;
               }
            }

            if (!find) {
               itemAmountWrapperList.add(itemAmountWrapper.shallowClone());
            }
         }
      }

      return itemAmountWrapperList;
   }

   public static int calMaxMatch(@NotNull Inventory inventory, @NotNull int[] slots, @NotNull List<ItemAmountWrapper> itemAmountWrapperList) {
      List<Integer> countList = new ArrayList<>(itemAmountWrapperList.size());
      List<Integer> stackList = new ArrayList<>(itemAmountWrapperList.size());
      int[] counts = new int[itemAmountWrapperList.size()];
      int[] stacks = new int[itemAmountWrapperList.size()];

      for (int i = 0; i < itemAmountWrapperList.size(); i++) {
         countList.add(0);
         stackList.add(0);
      }

      int emptySlot = 0;
      ItemWrapper itemWrapper = new ItemWrapper();

      for (int slot : slots) {
         ItemStack item = inventory.getItem(slot);
         if (ItemStackUtil.isItemNull(item)) {
            emptySlot++;
         } else if (item.getAmount() < item.getMaxStackSize()) {
            itemWrapper.newWrap(item);

            for (int i = 0; i < itemAmountWrapperList.size(); i++) {
               if (StackUtils.itemsMatch(itemWrapper.getItemStack(), itemAmountWrapperList.get(i).getItemStack())) {
                  counts[i] = counts[i] + item.getMaxStackSize() - item.getAmount();
                  stacks[i] = counts[i] / itemAmountWrapperList.get(i).getAmount();
                  break;
               }
            }
         }
      }

      while (emptySlot > 0) {
         int minStackP = 0;
         int minStack = stackList.get(0);

         for (int ix = 1; ix < itemAmountWrapperList.size(); ix++) {
            if (minStack > stackList.get(ix)) {
               minStack = stackList.get(ix);
               minStackP = ix;
            }
         }

         counts[minStackP] += itemAmountWrapperList.get(minStackP).getItemStack().getMaxStackSize();
         countList.set(minStackP, countList.get(minStackP) + itemAmountWrapperList.get(minStackP).getItemStack().getMaxStackSize());
         stacks[minStackP] = counts[minStackP] / itemAmountWrapperList.get(minStackP).getAmount();
         stackList.set(minStackP, countList.get(minStackP) / itemAmountWrapperList.get(minStackP).getAmount());
         emptySlot--;
      }

      int min = stackList.get(0);

      for (int stack : stackList) {
         min = Math.min(min, stack);
      }

      return min;
   }

   public static int calMaxMatch(@NotNull Inventory inventory, @NotNull int[] slots, @NotNull ItemAmountWrapper[] itemAmountWrapperList) {
      List<Integer> countList = new ArrayList<>(itemAmountWrapperList.length);
      List<Integer> stackList = new ArrayList<>(itemAmountWrapperList.length);

      for (int i = 0; i < itemAmountWrapperList.length; i++) {
         countList.add(0);
         stackList.add(0);
      }

      int emptySlot = 0;
      ItemWrapper itemWrapper = new ItemWrapper();

      for (int slot : slots) {
         ItemStack item = inventory.getItem(slot);
         if (ItemStackUtil.isItemNull(item)) {
            emptySlot++;
         } else if (item.getAmount() < item.getMaxStackSize()) {
            itemWrapper.newWrap(item);

            for (int i = 0; i < itemAmountWrapperList.length; i++) {
               if (StackUtils.itemsMatch(itemWrapper.getItemStack(), itemAmountWrapperList[i].getItemStack())) {
                  countList.set(i, countList.get(i) + (item.getMaxStackSize() - item.getAmount()));
                  stackList.set(i, countList.get(i) / itemAmountWrapperList[i].getAmount());
                  break;
               }
            }
         }
      }

      while (emptySlot > 0) {
         int minStackP = 0;
         int minStack = stackList.get(0);

         for (int ix = 1; ix < itemAmountWrapperList.length; ix++) {
            if (minStack > stackList.get(ix)) {
               minStack = stackList.get(ix);
               minStackP = ix;
            }
         }

         countList.set(minStackP, countList.get(minStackP) + itemAmountWrapperList[minStackP].getItemStack().getMaxStackSize());
         stackList.set(minStackP, countList.get(minStackP) / itemAmountWrapperList[minStackP].getAmount());
         emptySlot--;
      }

      int min = stackList.get(0);

      for (int stack : stackList) {
         min = Math.min(min, stack);
      }

      return min;
   }

   public static int calMaxMatch(@NotNull Inventory inventory, @NotNull int[] slots, @NotNull ItemAmountWrapper itemAmountWrapper) {
      int count = 0;
      int maxStack = itemAmountWrapper.getItemStack().getMaxStackSize();

      for (int slot : slots) {
         ItemStack item = inventory.getItem(slot);
         if (ItemStackUtil.isItemNull(item)) {
            count += maxStack;
         } else if (item.getAmount() < maxStack && StackUtils.itemsMatch(itemAmountWrapper.getItemStack(), item)) {
            count += maxStack - item.getAmount();
         }
      }

      return count / itemAmountWrapper.getAmount();
   }

   public static int calMaxMatch(@NotNull Inventory inventory, @NotNull int[] slots, @NotNull ItemStack item) {
      return calMaxMatch(inventory, slots, new ItemAmountWrapper(item));
   }

   @Generated
   private MachineUtil() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
