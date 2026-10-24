package io.github.sefiraat.networks.network.stackcaches;

import lombok.Generated;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemRequest extends ItemStackCache {
   private final int originalAmount;
   private int amount;

   public ItemRequest(@NotNull ItemStack itemStack, int amount) {
      super(itemStack);
      this.originalAmount = amount;
      this.amount = amount;
   }

   public void receiveAmount(int amount) {
      this.amount -= amount;
   }

   public int getReceivedAmount() {
      return this.originalAmount - this.amount;
   }

   @NotNull
   @Override
   public String toString() {
      return "ItemRequest{itemStack=" + this.getItemStack() + ", originalAmount=" + this.originalAmount + ", amount=" + this.amount + "}";
   }

   @Generated
   public void setAmount(int amount) {
      this.amount = amount;
   }

   @Generated
   public int getAmount() {
      return this.amount;
   }

   @Generated
   public int getOriginalAmount() {
      return this.originalAmount;
   }
}
