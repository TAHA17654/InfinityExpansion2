package com.balugaq.netex.api.data;

import io.github.sefiraat.networks.network.stackcaches.ItemStackCache;
import io.github.sefiraat.networks.utils.StackUtils;
import lombok.Generated;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemContainer extends ItemStackCache {
   private final int id;
   private volatile int amount;

   public synchronized void setAmount(int amount) {
      this.amount = amount;
   }

   public ItemContainer(int id, @NotNull ItemStack item, int amount) {
      super(StackUtils.getAsQuantity(item, 1));
      this.id = id;
      this.amount = amount;
   }

   @NotNull
   public ItemStack getSample() {
      return this.itemStack.clone();
   }

   @NotNull
   public ItemStack getSampleDirectly() {
      return this.itemStack;
   }

   public boolean isSimilar(ItemStack other) {
      return StackUtils.itemsMatch(this, other);
   }

   public synchronized void addAmount(int amount) {
      this.amount += amount;
   }

   public synchronized int removeAmount(int amount) {
      if (this.amount > amount) {
         this.amount -= amount;
         return amount;
      } else {
         int re = this.amount;
         this.amount = 0;
         return re;
      }
   }

   @Generated
   public int getId() {
      return this.id;
   }

   @Generated
   @Override
   public String toString() {
      return "ItemContainer(id=" + this.getId() + ", amount=" + this.getAmount() + ")";
   }

   @Generated
   public int getAmount() {
      return this.amount;
   }
}
