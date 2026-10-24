package com.balugaq.netex.api.data;

import com.ytdd9527.networksexpansion.utils.itemstacks.ItemStackUtil;
import io.github.sefiraat.networks.utils.StackUtils;
import java.util.List;
import lombok.Generated;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemAmountWrapper extends ItemWrapper {
   private int amount;

   public ItemAmountWrapper() {
      this.amount = 0;
   }

   public ItemAmountWrapper(@NotNull ItemStack itemStack) {
      super(itemStack);
      this.amount = itemStack.getAmount();
   }

   public ItemAmountWrapper(@NotNull ItemStack itemStack, int amount) {
      super(itemStack);
      this.amount = amount;
   }

   public ItemAmountWrapper(@NotNull ItemStack itemStack, @Nullable ItemMeta itemMeta) {
      super(itemStack, itemMeta);
      this.amount = itemStack.getAmount();
   }

   public ItemAmountWrapper(@NotNull ItemStack itemStack, @Nullable ItemMeta itemMeta, int amount) {
      super(itemStack, itemMeta);
      this.amount = amount;
   }

   public ItemAmountWrapper(@NotNull ItemWrapper itemWrapper) {
      super(itemWrapper.getItemStack(), itemWrapper.getItemMeta());
      this.amount = itemWrapper.getItemStack().getAmount();
   }

   public ItemAmountWrapper(@NotNull ItemWrapper itemWrapper, int amount) {
      super(itemWrapper.getItemStack(), itemWrapper.getItemMeta());
      this.amount = amount;
   }

   public static void addToList(@NotNull List<ItemAmountWrapper> list, @NotNull ItemAmountWrapper item) {
      for (ItemAmountWrapper existedItem : list) {
         if (StackUtils.itemsMatch(existedItem.getItemStack(), item.getItemStack())) {
            existedItem.addAmount(item.amount);
            return;
         }
      }

      list.add(item.shallowClone());
   }

   public static void addToList(@NotNull List<ItemAmountWrapper> list, @NotNull ItemAmountWrapper item, int mul) {
      for (ItemAmountWrapper existedItem : list) {
         if (StackUtils.itemsMatch(existedItem.getItemStack(), item.getItemStack())) {
            existedItem.addAmount(item.amount * mul);
            return;
         }
      }

      list.add(new ItemAmountWrapper(item.getItemStack(), item.getItemMeta(), item.amount * mul));
   }

   public void addAmount(int amount) {
      this.amount += amount;
   }

   public void subAmount(int amount) {
      this.amount -= amount;
   }

   @Override
   public void newWrap(@NotNull ItemStack itemStack) {
      super.newWrap(itemStack);
      this.amount = itemStack.getAmount();
   }

   public void newWrap(@NotNull ItemStack itemStack, int amount) {
      super.newWrap(itemStack);
      this.amount = amount;
   }

   @Override
   public void newWrap(@NotNull ItemStack itemStack, @Nullable ItemMeta itemMeta) {
      super.newWrap(itemStack, itemMeta);
      this.amount = itemStack.getAmount();
   }

   public void newWrap(@NotNull ItemStack itemStack, @Nullable ItemMeta itemMeta, int amount) {
      super.newWrap(itemStack, itemMeta);
      this.amount = amount;
   }

   @NotNull
   public ItemAmountWrapper shallowClone() {
      return new ItemAmountWrapper(this.getItemStack(), this.getItemMeta(), this.amount);
   }

   @NotNull
   public ItemAmountWrapper deepClone() {
      ItemStack itemStack = ItemStackUtil.cloneItem(this.getItemStack());
      itemStack.setAmount(this.getItemStack().getAmount());
      return new ItemAmountWrapper(ItemStackUtil.cloneItem(this.getItemStack()), this.amount);
   }

   @Override
   public int hashCode() {
      int hash = 31 + this.getItemStack().getType().hashCode();
      hash = hash * 31 + this.amount;
      hash = hash * 31 + (this.getItemStack().getDurability() & '\uffff');
      return hash * 31 + (this.getItemMeta() != null ? this.getItemMeta().hashCode() : 0);
   }

   @Generated
   public void setAmount(int amount) {
      this.amount = amount;
   }

   @Generated
   public int getAmount() {
      return this.amount;
   }
}
