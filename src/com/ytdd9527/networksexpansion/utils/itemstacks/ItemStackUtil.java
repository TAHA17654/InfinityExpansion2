package com.ytdd9527.networksexpansion.utils.itemstacks;

import com.balugaq.netex.api.data.ItemAmountWrapper;
import com.balugaq.netex.api.data.ItemWrapper;
import com.balugaq.netex.utils.Debug;
import com.balugaq.netex.utils.InventoryUtil;
import com.balugaq.netex.utils.NetworksVersionedEnchantment;
import com.ytdd9527.networksexpansion.utils.TextUtil;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.Pair;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.nms.ItemNameAdapter;
import io.github.thebusybiscuit.slimefun4.utils.itemstack.ItemStackWrapper;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ItemStackUtil {
   public static final ItemStack AIR = new ItemStack(Material.AIR);
   @Nullable
   public static final ItemNameAdapter itemNameAdapter = ItemNameAdapter.get();
   @Nullable
   private static final VarHandle API_ITEM_STACK_CRAFT_DELEGATE_FIELD;
   private static final boolean COMPONENTS_BRIDGE_OK;
   @Nullable
   private static final MethodHandle CRAFT_HANDLE_GETTER;
   @Nullable
   private static final MethodHandle NMS_GET_COMPONENTS_PATCH;
   @Nullable
   private static final MethodHandle NMS_PATCH_IS_EMPTY;
   @Nullable
   private static final MethodHandle NMS_HASH_ITEM_AND_COMPONENTS;

   @NotNull
   public static ItemStack cloneItem(@NotNull ItemStack item) {
      return item instanceof ItemStackWrapper ? getCleanItem(item) : item.clone();
   }

   @NotNull
   public static ItemStack getCleanItem(@Nullable ItemStack item) {
      if (item == null) {
         return new ItemStack(Material.AIR);
      } else {
         ItemStack cleanItem = new ItemStack(item.getType());
         cleanItem.setAmount(item.getAmount());
         if (item.hasItemMeta()) {
            cleanItem.setItemMeta(item.getItemMeta());
         }

         return cleanItem;
      }
   }

   @NotNull
   public static ItemStack cloneItem(@NotNull ItemStack item, int amount) {
      ItemStack itemStack = item instanceof ItemStackWrapper ? getCleanItem(item) : item.clone();
      itemStack.setAmount(amount);
      return itemStack;
   }

   private static ItemStack getDelegate(ItemStack bukkit) {
      return StackUtils.IS_1_21 ? (ItemStack)API_ITEM_STACK_CRAFT_DELEGATE_FIELD.get((ItemStack)bukkit) : bukkit;
   }

   @NotNull
   public static ItemStack asCraftItemStack(@NotNull ItemStack stack) {
      if (StackUtils.IS_1_21) {
         if (stack instanceof SlimefunItemStack) {
            return getCleanItem(stack);
         }

         if (stack instanceof ItemStackWrapper) {
            return getCleanItem(stack);
         }

         ItemStack delegate = getDelegate(stack);
         if (delegate instanceof SlimefunItemStack) {
            return getCleanItem(stack);
         }

         if (delegate instanceof ItemStackWrapper) {
            return getCleanItem(stack);
         }
      }

      return stack;
   }

   public static boolean componentsBridgeAvailable() {
      return COMPONENTS_BRIDGE_OK;
   }

   public static boolean hasCustomComponents(@Nullable ItemStack item) {
      if (item == null) {
         return false;
      } else if (!COMPONENTS_BRIDGE_OK) {
         return item.hasItemMeta();
      } else {
         try {
            Object nms = nmsHandle(item);
            if (nms == null) {
               return item.hasItemMeta();
            } else {
               Object patch = (Object)NMS_GET_COMPONENTS_PATCH.invoke((Object)nms);
               return !(boolean)NMS_PATCH_IS_EMPTY.invoke((Object)patch);
            }
         } catch (Throwable var3) {
            Debug.debug(var3);
            return item.hasItemMeta();
         }
      }
   }

   public static int hashItemComponents(@NotNull ItemStack item) {
      try {
         Object nms = nmsHandle(item);
         if (nms != null) {
            return (int)NMS_HASH_ITEM_AND_COMPONENTS.invoke((Object)nms);
         }
      } catch (Throwable var2) {
         Debug.debug(var2);
      }

      return item.getType().hashCode();
   }

   @Nullable
   public static Object nmsIdentity(@NotNull ItemStack item) {
      return nmsHandle(item);
   }

   @Nullable
   private static Object nmsHandle(@NotNull ItemStack item) {
      try {
         ItemStack target = item;
         if (StackUtils.IS_1_21) {
            ItemStack delegate = (ItemStack)API_ITEM_STACK_CRAFT_DELEGATE_FIELD.get((ItemStack)item);
            if (delegate != null) {
               target = delegate;
            }
         }

         return (Object)CRAFT_HANDLE_GETTER.invoke((ItemStack)target);
      } catch (Throwable var3) {
         return null;
      }
   }

   public static boolean isItemNull(@Nullable ItemStack item) {
      return item == null || item.getType() == Material.AIR || item.getAmount() == 0;
   }

   public static boolean isItemSimilar(@Nullable ItemStack item1, @Nullable ItemStack item2) {
      if (item1 == item2) {
         return true;
      } else {
         boolean itemNull1 = isItemNull(item1);
         boolean itemNull2 = isItemNull(item2);
         if (itemNull1 && itemNull2) {
            return true;
         } else if (!itemNull1 && !itemNull2) {
            if (!item1.getType().equals(item2.getType())) {
               return false;
            } else {
               return item1.hasItemMeta() && item2.hasItemMeta()
                  ? isItemMetaSame(item1.getItemMeta(), item2.getItemMeta())
                  : !item1.hasItemMeta() && !item2.hasItemMeta();
            }
         } else {
            return false;
         }
      }
   }

   public static boolean isItemSimilar(@Nullable ItemStack item1, @NotNull ItemWrapper item2) {
      if (item1 == item2.getItemStack()) {
         return true;
      } else if (isItemNull(item1)) {
         return false;
      } else if (!item1.getType().equals(item2.getItemStack().getType())) {
         return false;
      } else {
         return item1.hasItemMeta() && item2.hasItemMeta()
            ? isItemMetaSame(item1.getItemMeta(), item2.getItemMeta())
            : !item1.hasItemMeta() && !item2.hasItemMeta();
      }
   }

   public static boolean isItemSimilar(@NotNull ItemWrapper item1, @Nullable ItemStack item2) {
      if (item1.getItemStack() == item2) {
         return true;
      } else if (isItemNull(item2)) {
         return false;
      } else if (!item1.getItemStack().getType().equals(item2.getType())) {
         return false;
      } else {
         return item1.hasItemMeta() && item2.hasItemMeta()
            ? isItemMetaSame(item1.getItemMeta(), item2.getItemMeta())
            : !item1.hasItemMeta() && !item2.hasItemMeta();
      }
   }

   public static boolean isItemSimilar(@NotNull ItemWrapper item1, @NotNull ItemWrapper item2) {
      if (item1.getItemStack() == item2.getItemStack()) {
         return true;
      } else if (!item1.getItemStack().getType().equals(item2.getItemStack().getType())) {
         return false;
      } else {
         return item1.hasItemMeta() && item2.hasItemMeta()
            ? isItemMetaSame(item1.getItemMeta(), item2.getItemMeta())
            : !item1.hasItemMeta() && !item2.hasItemMeta();
      }
   }

   public static boolean isItemMetaSame(@Nullable ItemMeta itemMeta1, @Nullable ItemMeta itemMeta2) {
      if (itemMeta1 != null && itemMeta2 != null) {
         if (itemMeta1.hasDisplayName() && itemMeta2.hasDisplayName()) {
            if (!itemMeta1.getDisplayName().equals(itemMeta2.getDisplayName())) {
               return false;
            }
         } else if (itemMeta1.hasDisplayName() || itemMeta2.hasDisplayName()) {
            return false;
         }

         return isLoreSame(itemMeta1, itemMeta2) && isContainerSame(itemMeta1, itemMeta2);
      } else {
         return itemMeta1 == itemMeta2;
      }
   }

   public static boolean isLoreSame(@NotNull ItemMeta itemMeta1, @NotNull ItemMeta itemMeta2) {
      if (itemMeta1.hasLore() && itemMeta2.hasLore()) {
         List<String> lore1 = itemMeta1.getLore();
         List<String> lore2 = itemMeta2.getLore();
         if (lore1 != null && lore2 != null) {
            if (lore1.size() != lore2.size()) {
               return false;
            } else {
               for (int i = 0; i < lore1.size(); i++) {
                  if (!lore1.get(i).equals(lore2.get(i))) {
                     return false;
                  }
               }

               return true;
            }
         } else {
            return lore1 == lore2;
         }
      } else {
         return !itemMeta1.hasLore() && !itemMeta2.hasLore();
      }
   }

   public static boolean isContainerSame(@NotNull ItemMeta itemMeta1, @NotNull ItemMeta itemMeta2) {
      PersistentDataContainer persistentDataContainer1 = itemMeta1.getPersistentDataContainer();
      PersistentDataContainer persistentDataContainer2 = itemMeta2.getPersistentDataContainer();
      Set<NamespacedKey> keys1 = persistentDataContainer1.getKeys();
      Set<NamespacedKey> keys2 = persistentDataContainer2.getKeys();
      if (keys1.size() != keys2.size()) {
         return false;
      } else {
         for (NamespacedKey namespacedKey : keys1) {
            if (!persistentDataContainer2.has(namespacedKey, PersistentDataType.STRING)) {
               return false;
            }

            if (!Objects.equals(
               persistentDataContainer1.get(namespacedKey, PersistentDataType.STRING), persistentDataContainer2.get(namespacedKey, PersistentDataType.STRING)
            )) {
               return false;
            }
         }

         return true;
      }
   }

   public static boolean isEnchantmentSame(@NotNull ItemStack itemStack1, @NotNull ItemStack itemStack2) {
      if (itemStack1 == itemStack2) {
         return true;
      } else {
         Map<Enchantment, Integer> enchantments1 = itemStack1.getEnchantments();
         Map<Enchantment, Integer> enchantments2 = itemStack2.getEnchantments();
         if (enchantments1.size() != enchantments2.size()) {
            return false;
         } else {
            for (Entry<Enchantment, Integer> entry : enchantments1.entrySet()) {
               if (!enchantments2.containsKey(entry.getKey()) || !Objects.equals(enchantments2.get(entry.getKey()), entry.getValue())) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   @NotNull
   public static ItemStack[] getNoNullItemArray(@NotNull List<ItemStack> itemList) {
      List<ItemStack> noNullItemList = new ArrayList<>(itemList.size());

      for (ItemStack item : itemList) {
         if (!isItemNull(item)) {
            noNullItemList.add(item);
         }
      }

      return getItemArray(noNullItemList);
   }

   @NotNull
   public static ItemStack[] getItemArray(@NotNull List<ItemStack> itemList) {
      ItemStack[] items = new ItemStack[itemList.size()];

      for (int i = 0; i < items.length; i++) {
         items[i] = itemList.get(i);
      }

      return items;
   }

   @NotNull
   public static List<ItemStack> getNoNullItemList(@NotNull ItemStack[] items) {
      List<ItemStack> itemList = new ArrayList<>(items.length);

      for (ItemStack item : items) {
         if (!isItemNull(item)) {
            itemList.add(item);
         }
      }

      return itemList;
   }

   @NotNull
   public static List<ItemStack> calMergeItems(@NotNull List<ItemStack> itemList) {
      List<ItemWrapper> itemWrapperList = new ArrayList<>(itemList.size());
      ItemWrapper itemWrapper = new ItemWrapper();

      for (ItemStack item : itemList) {
         if (!isItemNull(item)) {
            itemWrapper.newWrap(item);
            int amount = item.getAmount();

            for (ItemWrapper resultItem : itemWrapperList) {
               ItemStack itemStack = resultItem.getItemStack();
               if (itemStack.getAmount() < itemStack.getMaxStackSize() && StackUtils.itemsMatch(itemWrapper.getItemStack(), resultItem.getItemStack())) {
                  int count = Math.min(amount, itemStack.getMaxStackSize() - itemStack.getAmount());
                  itemStack.setAmount(itemStack.getAmount() + count);
                  amount -= count;
                  if (amount == 0) {
                     break;
                  }
               }
            }

            if (amount != 0) {
               ItemStack itemstack = cloneItem(item);
               itemstack.setAmount(amount);
               itemWrapperList.add(new ItemWrapper(itemstack, itemWrapper.getItemMeta()));
            }
         }
      }

      return ItemWrapper.getItemList(itemWrapperList);
   }

   @NotNull
   public static ItemStack[] calMergeItems(@NotNull ItemStack[] items) {
      List<ItemWrapper> itemWrapperList = new ArrayList<>(items.length);
      ItemWrapper itemWrapper = new ItemWrapper();

      for (ItemStack item : items) {
         if (!isItemNull(item)) {
            itemWrapper.newWrap(item);
            int amount = item.getAmount();

            for (ItemWrapper resultItem : itemWrapperList) {
               ItemStack itemStack = resultItem.getItemStack();
               if (itemStack.getAmount() < itemStack.getMaxStackSize() && StackUtils.itemsMatch(itemWrapper.getItemStack(), resultItem.getItemStack())) {
                  int count = Math.min(amount, itemStack.getMaxStackSize() - itemStack.getAmount());
                  itemStack.setAmount(itemStack.getAmount() + count);
                  amount -= count;
                  if (amount == 0) {
                     break;
                  }
               }
            }

            if (amount != 0) {
               ItemStack itemstack = cloneItem(item);
               itemstack.setAmount(amount);
               itemWrapperList.add(new ItemWrapper(itemstack, itemWrapper.getItemMeta()));
            }
         }
      }

      return ItemWrapper.getItemArray(itemWrapperList);
   }

   @NotNull
   public static ItemStack[] calEnlargeItemArray(@NotNull ItemStack[] items, int amount) {
      int slot = 0;

      for (ItemStack item : items) {
         slot = slot + 1 + item.getAmount() * amount / item.getMaxStackSize();
         if (item.getAmount() * amount % item.getMaxStackSize() == 0) {
            slot--;
         }
      }

      ItemStack[] result = new ItemStack[slot];
      int pointer = 0;

      for (ItemStack itemx : items) {
         int resultAmount;
         for (resultAmount = itemx.getAmount() * amount; resultAmount > itemx.getMaxStackSize(); resultAmount -= itemx.getMaxStackSize()) {
            result[pointer] = cloneItem(itemx);
            result[pointer++].setAmount(itemx.getMaxStackSize());
         }

         if (resultAmount != 0) {
            result[pointer] = cloneItem(itemx);
            result[pointer++].setAmount(resultAmount);
         }
      }

      return result;
   }

   @NotNull
   public static ItemStack[] calEnlargeItemArray(@NotNull List<ItemAmountWrapper> itemAmountWrapperList, int amount) {
      int slot = 0;

      for (ItemAmountWrapper itemAmountWrapper : itemAmountWrapperList) {
         slot = slot + 1 + itemAmountWrapper.getAmount() * amount / itemAmountWrapper.getItemStack().getMaxStackSize();
         if (itemAmountWrapper.getAmount() * amount % itemAmountWrapper.getItemStack().getMaxStackSize() == 0) {
            slot--;
         }
      }

      ItemStack[] result = new ItemStack[slot];
      int pointer = 0;

      for (ItemAmountWrapper itemAmountWrapperx : itemAmountWrapperList) {
         int resultAmount;
         for (resultAmount = itemAmountWrapperx.getAmount() * amount;
            resultAmount > itemAmountWrapperx.getItemStack().getMaxStackSize();
            resultAmount -= itemAmountWrapperx.getItemStack().getMaxStackSize()
         ) {
            result[pointer] = cloneItem(itemAmountWrapperx.getItemStack());
            result[pointer++].setAmount(itemAmountWrapperx.getItemStack().getMaxStackSize());
         }

         if (resultAmount != 0) {
            result[pointer] = cloneItem(itemAmountWrapperx.getItemStack());
            result[pointer++].setAmount(resultAmount);
         }
      }

      return result;
   }

   @NotNull
   public static ItemStack[] calEnlargeItemArray(@NotNull ItemAmountWrapper[] itemAmountWrapperList, int amount) {
      int slot = 0;

      for (ItemAmountWrapper itemAmountWrapper : itemAmountWrapperList) {
         slot = slot + 1 + itemAmountWrapper.getAmount() * amount / itemAmountWrapper.getItemStack().getMaxStackSize();
         if (itemAmountWrapper.getAmount() * amount % itemAmountWrapper.getItemStack().getMaxStackSize() == 0) {
            slot--;
         }
      }

      ItemStack[] result = new ItemStack[slot];
      int pointer = 0;

      for (ItemAmountWrapper itemAmountWrapperx : itemAmountWrapperList) {
         int resultAmount;
         for (resultAmount = itemAmountWrapperx.getAmount() * amount;
            resultAmount > itemAmountWrapperx.getItemStack().getMaxStackSize();
            resultAmount -= itemAmountWrapperx.getItemStack().getMaxStackSize()
         ) {
            result[pointer] = cloneItem(itemAmountWrapperx.getItemStack());
            result[pointer++].setAmount(itemAmountWrapperx.getItemStack().getMaxStackSize());
         }

         if (resultAmount != 0) {
            result[pointer] = cloneItem(itemAmountWrapperx.getItemStack());
            result[pointer++].setAmount(resultAmount);
         }
      }

      return result;
   }

   @NotNull
   public static List<ItemAmountWrapper> calItemListWithAmount(@NotNull ItemStack[] items) {
      List<ItemAmountWrapper> itemWithWrapperList = new ArrayList<>(items.length);
      ItemAmountWrapper itemAmountWrapper = new ItemAmountWrapper();

      for (ItemStack item : items) {
         if (!isItemNull(item)) {
            itemAmountWrapper.newWrap(cloneItem(item));
            boolean find = false;

            for (ItemAmountWrapper existedItemWrapper : itemWithWrapperList) {
               if (StackUtils.itemsMatch(itemAmountWrapper.getItemStack(), existedItemWrapper.getItemStack())) {
                  existedItemWrapper.addAmount(item.getAmount());
                  find = true;
                  break;
               }
            }

            if (!find) {
               itemWithWrapperList.add(itemAmountWrapper.shallowClone());
            }
         }
      }

      return itemWithWrapperList;
   }

   @NotNull
   public static List<ItemAmountWrapper> calItemListWithAmount(@NotNull List<ItemStack> itemList) {
      List<ItemAmountWrapper> itemWithWrapperList = new ArrayList<>(itemList.size());
      ItemAmountWrapper itemAmountWrapper = new ItemAmountWrapper();

      for (ItemStack item : itemList) {
         if (!isItemNull(item)) {
            itemAmountWrapper.newWrap(cloneItem(item));
            boolean find = false;

            for (ItemAmountWrapper existedItemWrapper : itemWithWrapperList) {
               if (StackUtils.itemsMatch(itemAmountWrapper.getItemStack(), existedItemWrapper.getItemStack())) {
                  existedItemWrapper.addAmount(item.getAmount());
                  find = true;
                  break;
               }
            }

            if (!find) {
               itemWithWrapperList.add(itemAmountWrapper.shallowClone());
            }
         }
      }

      return itemWithWrapperList;
   }

   @NotNull
   public static ItemAmountWrapper[] calItemArrayWithAmount(@NotNull ItemStack[] items) {
      return calItemListWithAmount(items).toArray(new ItemAmountWrapper[0]);
   }

   @NotNull
   public static ItemAmountWrapper[] calItemArrayWithAmount(@NotNull List<ItemStack> itemList) {
      return calItemListWithAmount(itemList).toArray(new ItemAmountWrapper[0]);
   }

   public static int stack(@Nullable ItemStack input, @Nullable ItemStack output) {
      if (isItemNull(output) || output.getAmount() >= output.getMaxStackSize() || !StackUtils.itemsMatch(input, output)) {
         return 0;
      } else if (input == null) {
         return output.getAmount();
      } else {
         int amount = Math.min(input.getAmount(), output.getMaxStackSize() - output.getAmount());
         input.setAmount(input.getAmount() - amount);
         output.setAmount(output.getAmount() + amount);
         return amount;
      }
   }

   public static int stack(@NotNull ItemWrapper input, @Nullable ItemStack output) {
      if (!isItemNull(output) && output.getAmount() < output.getMaxStackSize() && StackUtils.itemsMatch(input.getItemStack(), output)) {
         int amount = Math.min(input.getItemStack().getAmount(), output.getMaxStackSize() - output.getAmount());
         input.getItemStack().setAmount(input.getItemStack().getAmount() - amount);
         output.setAmount(output.getAmount() + amount);
         return amount;
      } else {
         return 0;
      }
   }

   public static int stack(@Nullable ItemStack input, @NotNull ItemWrapper output) {
      if (!isItemNull(input)
         && output.getItemStack().getAmount() < output.getItemStack().getMaxStackSize()
         && StackUtils.itemsMatch(input, output.getItemStack())) {
         int amount = Math.min(input.getAmount(), output.getItemStack().getMaxStackSize() - output.getItemStack().getAmount());
         input.setAmount(input.getAmount() - amount);
         output.getItemStack().setAmount(output.getItemStack().getAmount() + amount);
         return amount;
      } else {
         return 0;
      }
   }

   public static int stack(@NotNull ItemWrapper input, @NotNull ItemWrapper output) {
      if (output.getItemStack().getAmount() < output.getItemStack().getMaxStackSize() && StackUtils.itemsMatch(input.getItemStack(), output.getItemStack())) {
         int amount = Math.min(input.getItemStack().getAmount(), output.getItemStack().getMaxStackSize() - output.getItemStack().getAmount());
         input.getItemStack().setAmount(input.getItemStack().getAmount() - amount);
         output.getItemStack().setAmount(output.getItemStack().getAmount() + amount);
         return amount;
      } else {
         return 0;
      }
   }

   public static int stack(@Nullable ItemStack input, @Nullable ItemStack output, int maxAmount) {
      if (isItemNull(output) || output.getMaxStackSize() <= output.getAmount() || !StackUtils.itemsMatch(input, output)) {
         return 0;
      } else if (input == null) {
         return output.getAmount();
      } else {
         int amount = Math.min(maxAmount, Math.min(input.getAmount(), output.getMaxStackSize() - output.getAmount()));
         input.setAmount(input.getAmount() - amount);
         output.setAmount(output.getAmount() + amount);
         return amount;
      }
   }

   public static int stack(@NotNull ItemWrapper input, @Nullable ItemStack output, int maxAmount) {
      if (!isItemNull(output) && output.getAmount() < output.getMaxStackSize() && StackUtils.itemsMatch(input.getItemStack(), output)) {
         int amount = Math.min(maxAmount, Math.min(input.getItemStack().getAmount(), output.getMaxStackSize() - output.getAmount()));
         input.getItemStack().setAmount(input.getItemStack().getAmount() - amount);
         output.setAmount(output.getAmount() + amount);
         return amount;
      } else {
         return 0;
      }
   }

   public static int stack(@Nullable ItemStack input, @NotNull ItemWrapper output, int maxAmount) {
      if (!isItemNull(input)
         && output.getItemStack().getAmount() < output.getItemStack().getMaxStackSize()
         && StackUtils.itemsMatch(input, output.getItemStack())) {
         int amount = Math.min(maxAmount, Math.min(input.getAmount(), output.getItemStack().getMaxStackSize() - output.getItemStack().getAmount()));
         input.setAmount(input.getAmount() - amount);
         output.getItemStack().setAmount(output.getItemStack().getAmount() + amount);
         return amount;
      } else {
         return 0;
      }
   }

   public static int stack(@NotNull ItemWrapper input, @NotNull ItemWrapper output, int maxAmount) {
      if (output.getItemStack().getAmount() < output.getItemStack().getMaxStackSize() && StackUtils.itemsMatch(input.getItemStack(), output.getItemStack())) {
         int amount = Math.min(
            maxAmount, Math.min(input.getItemStack().getAmount(), output.getItemStack().getMaxStackSize() - output.getItemStack().getAmount())
         );
         input.getItemStack().setAmount(input.getItemStack().getAmount() - amount);
         output.getItemStack().setAmount(output.getItemStack().getAmount() + amount);
         return amount;
      } else {
         return 0;
      }
   }

   @NotNull
   public static String getItemName(@Nullable ItemStack item) {
      if (isItemNull(item)) {
         return "null";
      } else {
         item = getCleanItem(item);
         if (item.hasItemMeta()) {
            ItemMeta itemMeta = item.getItemMeta();
            return itemMeta.hasDisplayName() ? itemMeta.getDisplayName() : "unknown";
         } else {
            try {
               return itemNameAdapter != null ? itemNameAdapter.getName(item) : "unknown";
            } catch (IllegalAccessException | InvocationTargetException var2) {
               Debug.trace(var2);
               return "unknown";
            }
         }
      }
   }

   public static void setItemName(@NotNull ItemStack item, @NotNull String itemName) {
      if (item.hasItemMeta()) {
         ItemMeta itemMeta = item.getItemMeta();
         itemMeta.setDisplayName(itemName);
         item.setItemMeta(itemMeta);
      }
   }

   public static void addLoreToFirst(@Nullable ItemStack item, @NotNull String s) {
      if (!isItemNull(item)) {
         ItemMeta itemMeta = item.getItemMeta();
         List<String> lore = itemMeta.getLore();
         if (lore == null) {
            List<String> var5 = new ArrayList(8);
            var5.add(s);
            itemMeta.setLore(var5);
            item.setItemMeta(itemMeta);
         } else {
            List<String> newLore = new ArrayList<>(lore.size() + 1);
            newLore.add(s);
            newLore.addAll(lore);
            itemMeta.setLore(newLore);
            item.setItemMeta(itemMeta);
         }
      }
   }

   public static void addLoreToLast(@Nullable ItemStack item, @NotNull String s) {
      if (!isItemNull(item)) {
         ItemMeta itemMeta = item.getItemMeta();
         List<String> lore = itemMeta.getLore();
         if (lore == null) {
            lore = new ArrayList<>(8);
         }

         lore.add(s);
         itemMeta.setLore(lore);
         item.setItemMeta(itemMeta);
      }
   }

   public static void addLoreToLast(@NotNull ItemMeta itemMeta, @NotNull String s) {
      List<String> lore = itemMeta.getLore();
      if (lore == null) {
         lore = new ArrayList<>(8);
      }

      lore.add(s);
      itemMeta.setLore(lore);
   }

   public static void addLoresToLast(@Nullable ItemStack item, @NotNull String... s) {
      if (!isItemNull(item)) {
         ItemMeta itemMeta = item.getItemMeta();
         List<String> lore = itemMeta.getLore();
         if (lore == null) {
            lore = new ArrayList<>(8);
         }

         lore.addAll(Arrays.stream(s).toList());
         itemMeta.setLore(lore);
         item.setItemMeta(itemMeta);
      }
   }

   public static void addLoresToLast(@NotNull ItemMeta itemMeta, @NotNull String... s) {
      List<String> lore = itemMeta.getLore();
      if (lore == null) {
         lore = new ArrayList<>(8);
      }

      lore.addAll(Arrays.stream(s).toList());
      itemMeta.setLore(lore);
   }

   public static void removeLastLore(@Nullable ItemStack item) {
      if (!isItemNull(item)) {
         ItemMeta itemMeta = item.getItemMeta();
         List<String> lore = itemMeta.getLore();
         if (lore != null && !lore.isEmpty()) {
            lore = lore.subList(0, Math.max(lore.size() - 1, 0));
            itemMeta.setLore(lore);
            item.setItemMeta(itemMeta);
         }
      }
   }

   public static void removeLastLore(@NotNull ItemMeta itemMeta) {
      List<String> lore = itemMeta.getLore();
      if (lore != null && !lore.isEmpty()) {
         lore = lore.subList(0, Math.max(lore.size() - 1, 0));
         itemMeta.setLore(lore);
      }
   }

   public static void setLastLore(@NotNull ItemStack item, @NotNull String s) {
      if (!isItemNull(item)) {
         ItemMeta itemMeta = item.getItemMeta();
         List<String> lore = itemMeta.getLore();
         if (lore != null && !lore.isEmpty()) {
            lore.set(lore.size() - 1, s);
         } else {
            lore = new ArrayList<>();
            lore.add(s);
         }

         itemMeta.setLore(lore);
         item.setItemMeta(itemMeta);
      }
   }

   public static void setLastLore(@NotNull ItemMeta itemMeta, @NotNull String s) {
      List<String> lore = itemMeta.getLore();
      if (lore != null && !lore.isEmpty()) {
         lore.set(lore.size() - 1, s);
      } else {
         lore = new ArrayList<>();
         lore.add(s);
      }

      itemMeta.setLore(lore);
   }

   @Nullable
   public static String getLastLore(@NotNull ItemStack item) {
      if (isItemNull(item)) {
         return null;
      } else {
         ItemMeta itemMeta = item.getItemMeta();
         List<String> lore = itemMeta.getLore();
         return lore != null && !lore.isEmpty() ? lore.get(lore.size() - 1) : null;
      }
   }

   @Nullable
   public static String getLastLore(@NotNull ItemMeta itemMeta) {
      List<String> lore = itemMeta.getLore();
      return lore != null && !lore.isEmpty() ? lore.get(lore.size() - 1) : null;
   }

   public static void setLore(@NotNull ItemStack item, @NotNull String... lore) {
      if (!isItemNull(item)) {
         ItemMeta itemMeta = item.getItemMeta();
         itemMeta.setLore(Arrays.stream(lore).toList());
         item.setItemMeta(itemMeta);
      }
   }

   public static void setLore(@NotNull ItemStack item, @NotNull List<String> lore) {
      if (!isItemNull(item)) {
         ItemMeta itemMeta = item.getItemMeta();
         itemMeta.setLore(lore);
         item.setItemMeta(itemMeta);
      }
   }

   public static void replaceLore(@NotNull ItemStack item, int loreOffset, @NotNull String... lore) {
      if (loreOffset < 0) {
         setLore(item, lore);
      } else if (!isItemNull(item)) {
         ItemMeta itemMeta = item.getItemMeta();
         List<String> oldLore = itemMeta.getLore();
         if (oldLore == null) {
            oldLore = new ArrayList<>();
         }

         while (oldLore.size() < loreOffset) {
            oldLore.add("");
         }

         for (int i = 0; i < lore.length; i++) {
            if (oldLore.size() <= loreOffset + i) {
               oldLore.add(loreOffset + i, lore[i]);
            } else {
               oldLore.set(loreOffset + i, lore[i]);
            }
         }

         itemMeta.setLore(oldLore);
         item.setItemMeta(itemMeta);
      }
   }

   public static void replaceLore(@NotNull ItemStack item, int loreOffset, @NotNull List<String> lore) {
      if (loreOffset < 0) {
         setLore(item, lore);
      } else if (!isItemNull(item)) {
         ItemMeta itemMeta = item.getItemMeta();
         List<String> oldLore = itemMeta.getLore();
         if (oldLore == null) {
            oldLore = new ArrayList<>();
         }

         while (oldLore.size() < loreOffset) {
            oldLore.add("");
         }

         for (int i = 0; i < lore.size(); i++) {
            if (oldLore.size() <= loreOffset + i) {
               oldLore.add(loreOffset + i, lore.get(i));
            } else {
               oldLore.set(loreOffset + i, lore.get(i));
            }
         }

         itemMeta.setLore(oldLore);
         item.setItemMeta(itemMeta);
      }
   }

   public static void addItemFlag(@NotNull ItemStack item, @NotNull ItemFlag itemFlag) {
      if (item.hasItemMeta()) {
         ItemMeta itemMeta = item.getItemMeta();
         itemMeta.addItemFlags(new ItemFlag[]{itemFlag});
         item.setItemMeta(itemMeta);
      }
   }

   public static void addNBT(@NotNull ItemStack item, @NotNull NamespacedKey namespacedKey, @NotNull String value) {
      if (item.hasItemMeta()) {
         ItemMeta itemMeta = item.getItemMeta();
         PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();
         if (!persistentDataContainer.has(namespacedKey, PersistentDataType.STRING)) {
            persistentDataContainer.set(namespacedKey, PersistentDataType.STRING, value);
            item.setItemMeta(itemMeta);
         }
      }
   }

   public static void setNBT(@NotNull ItemStack item, @NotNull NamespacedKey namespacedKey, @NotNull String value) {
      if (item.hasItemMeta()) {
         ItemMeta itemMeta = item.getItemMeta();
         PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();
         persistentDataContainer.set(namespacedKey, PersistentDataType.STRING, value);
         item.setItemMeta(itemMeta);
      }
   }

   public static void clearNBT(@NotNull ItemStack itemStack) {
      if (itemStack.hasItemMeta()) {
         ItemMeta itemMeta = itemStack.getItemMeta();
         PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();

         for (NamespacedKey namespacedKey : persistentDataContainer.getKeys()) {
            persistentDataContainer.remove(namespacedKey);
         }

         itemStack.setItemMeta(itemMeta);
      }
   }

   @Nullable
   public static ItemStack cloneWithoutNBT(@Nullable ItemStack itemStack) {
      if (itemStack == null) {
         return null;
      } else if (!itemStack.hasItemMeta()) {
         return getCleanItem(itemStack);
      } else {
         ItemStack result = getCleanItem(itemStack);
         clearNBT(result);
         return result;
      }
   }

   @Nullable
   public static ItemStack getDried(@NotNull ItemStack item) {
      if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
         return null;
      } else {
         return switch (item.getType()) {
            case POTION, DRAGON_BREATH, HONEY_BOTTLE -> new ItemStack(Material.GLASS_BOTTLE, 1);
            case WATER_BUCKET, LAVA_BUCKET, MILK_BUCKET -> new ItemStack(Material.BUCKET, 1);
            default -> null;
         };
      }
   }

   @NotNull
   public static String itemStackToString(@NotNull ItemStack itemStack) {
      YamlConfiguration yamlConfiguration = new YamlConfiguration();
      yamlConfiguration.set("item", itemStack);
      return yamlConfiguration.saveToString();
   }

   @Nullable
   public static ItemStack stringToItemStack(@NotNull String local) {
      YamlConfiguration yamlConfiguration = new YamlConfiguration();

      try {
         yamlConfiguration.loadFromString(local);
      } catch (InvalidConfigurationException var3) {
         Debug.trace(var3);
      }

      return yamlConfiguration.getItemStack("item");
   }

   public static String color(@Nullable String str) {
      return str == null ? null : TextUtil.color(str);
   }

   public static void giveOrDropItem(@NotNull Player p, @NotNull ItemStack toGive) {
      for (int i = 0; i < 64; i++) {
         if (toGive.getAmount() <= 0) {
            return;
         }

         ItemStack incoming = toGive.clone();
         incoming.setAmount(Math.min(toGive.getMaxStackSize(), toGive.getAmount()));
         toGive.setAmount(toGive.getAmount() - incoming.getAmount());
         InventoryUtil.addItem(p, incoming);
         if (incoming.getAmount() > 0) {
            p.getWorld().dropItemNaturally(p.getLocation(), incoming);
         }
      }
   }

   public static void giveOrDropOnCursor(@NotNull Player player, @NotNull ItemStack item) {
      ItemStack onCursor = player.getItemOnCursor();
      if (onCursor != null && !onCursor.getType().isAir()) {
         giveOrDropItem(player, item);
      } else {
         player.setItemOnCursor(item);
      }
   }

   public static void send(@NotNull CommandSender p, String message) {
      p.sendMessage(color(TextUtil.GRAY + "[" + TextUtil.GOLD + "NetworksExpansion" + TextUtil.GRAY + "] " + TextUtil.RESET + message));
   }

   @NotNull
   public static ItemStack getPreEnchantedItemStack(@NotNull Material material) {
      return getPreEnchantedItemStack(material, true);
   }

   @NotNull
   public static ItemStack getPreEnchantedItemStack(@NotNull Material material, boolean hide) {
      return getPreEnchantedItemStack(material, hide, new Pair(NetworksVersionedEnchantment.GLOW, 1));
   }

   @SafeVarargs
   @NotNull
   public static ItemStack getPreEnchantedItemStack(@NotNull Material material, boolean hide, @NotNull Pair<Enchantment, Integer>... enchantments) {
      ItemStack itemStack = new ItemStack(material);
      ItemMeta itemMeta = itemStack.getItemMeta();

      for (Pair<Enchantment, Integer> pair : enchantments) {
         Enchantment v1 = (Enchantment)pair.getFirstValue();
         Integer v2 = (Integer)pair.getSecondValue();
         if (v1 != null && v2 != null) {
            itemMeta.addEnchant(v1, v2, true);
         }
      }

      if (hide) {
         itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
      }

      itemStack.setItemMeta(itemMeta);
      return itemStack;
   }

   public static void consumeItem(ItemStack item, int amount, boolean replaceConsumables) {
      if (item != null && item.getType() != Material.AIR) {
         if (replaceConsumables && item.getAmount() == 1 && StackUtils.itemsMatch(item, new ItemStack(item.getType()))) {
            switch (item.getType()) {
               case POTION:
               case DRAGON_BREATH:
               case HONEY_BOTTLE:
               case SPLASH_POTION:
               case LINGERING_POTION:
                  item.setType(Material.GLASS_BOTTLE);
                  break;
               case WATER_BUCKET:
               case LAVA_BUCKET:
               case MILK_BUCKET:
               case COD_BUCKET:
               case SALMON_BUCKET:
               case PUFFERFISH_BUCKET:
               case TROPICAL_FISH_BUCKET:
               case AXOLOTL_BUCKET:
               case POWDER_SNOW_BUCKET:
               case TADPOLE_BUCKET:
                  item.setType(Material.BUCKET);
                  break;
               case MUSHROOM_STEW:
               case BEETROOT_SOUP:
               case RABBIT_STEW:
               case SUSPICIOUS_STEW:
                  item.setType(Material.BOWL);
                  break;
               default:
                  item.setAmount(0);
            }
         } else if (item.getAmount() <= amount) {
            item.setAmount(0);
         } else {
            item.setAmount(item.getAmount() - amount);
         }
      }
   }

   static {
      if (StackUtils.IS_1_21) {
         try {
            API_ITEM_STACK_CRAFT_DELEGATE_FIELD = MethodHandles.privateLookupIn(ItemStack.class, MethodHandles.lookup())
               .findVarHandle(ItemStack.class, "craftDelegate", ItemStack.class);
         } catch (NoSuchFieldException | IllegalAccessException var10) {
            throw new RuntimeException(var10);
         }
      } else {
         API_ITEM_STACK_CRAFT_DELEGATE_FIELD = null;
      }

      boolean ok = false;
      MethodHandle handleGetter = null;
      MethodHandle patchGetter = null;
      MethodHandle patchIsEmpty = null;
      MethodHandle hashItemAndComponents = null;
      if (StackUtils.IS_1_20_5) {
         try {
            Lookup lookup = MethodHandles.lookup();
            Class<?> craftClass = Class.forName("org.bukkit.craftbukkit.inventory.CraftItemStack");
            Class<?> nmsClass = Class.forName("net.minecraft.world.item.ItemStack");
            Class<?> patchClass = Class.forName("net.minecraft.core.component.DataComponentPatch");
            handleGetter = lookup.findGetter(craftClass, "handle", nmsClass);
            patchGetter = lookup.findVirtual(nmsClass, "getComponentsPatch", MethodType.methodType(patchClass));
            patchIsEmpty = lookup.findVirtual(patchClass, "isEmpty", MethodType.methodType(boolean.class));
            hashItemAndComponents = lookup.findStatic(nmsClass, "hashItemAndComponents", MethodType.methodType(int.class, nmsClass));
            ok = true;
         } catch (IllegalAccessException | NoSuchFieldException | NoSuchMethodException | ClassNotFoundException var9) {
            Debug.debug("Components bridge unavailable: NMS reflection lookup failed, hasCustomComponents() falls back to hasItemMeta()");
            Debug.trace(var9);
         }
      }

      COMPONENTS_BRIDGE_OK = ok;
      CRAFT_HANDLE_GETTER = handleGetter;
      NMS_GET_COMPONENTS_PATCH = patchGetter;
      NMS_PATCH_IS_EMPTY = patchIsEmpty;
      NMS_HASH_ITEM_AND_COMPONENTS = hashItemAndComponents;
   }
}
