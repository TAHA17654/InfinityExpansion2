package io.github.sefiraat.networks.network.stackcaches;

import com.balugaq.netex.utils.Lang;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import net.guizhanss.minecraft.guizhanlib.gugu.minecraft.helpers.inventory.ItemStackHelper;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QuantumCache extends ItemStackCache {
   private final boolean supportsCustomMaxAmount;
   private long limit;
   private volatile long amount;
   private volatile boolean voidExcess;

   public int getLimit() {
      return this.limit > 2147483647L ? Integer.MAX_VALUE : (int)this.limit;
   }

   public long getLimitLong() {
      return this.limit;
   }

   public int getAmountInt() {
      return this.amount > 2147483647L ? Integer.MAX_VALUE : (int)this.amount;
   }

   public long getAmountLong() {
      return this.amount;
   }

   public QuantumCache(@Nullable ItemStack storedItem, long amount, int limit, boolean voidExcess, boolean supportsCustomMaxAmount) {
      this(storedItem, amount, (long)limit, voidExcess, supportsCustomMaxAmount);
   }

   public QuantumCache(@Nullable ItemStack storedItem, long amount, long limit, boolean voidExcess, boolean supportsCustomMaxAmount) {
      super(storedItem);
      this.amount = amount;
      this.limit = limit;
      this.voidExcess = voidExcess;
      this.supportsCustomMaxAmount = supportsCustomMaxAmount;
   }

   public synchronized void setAmount(int amount) {
      if (amount < -2000000000) {
         this.amount = (long)(-amount);
      } else {
         this.amount = (long)amount;
      }
   }

   public synchronized void setAmount(long amount) {
      if (amount < -2000000000L) {
         this.amount = -amount;
      } else {
         this.amount = amount;
      }
   }

   public boolean supportsCustomMaxAmount() {
      return this.supportsCustomMaxAmount;
   }

   public synchronized int increaseAmount(int amount) {
      long total = this.amount + (long)amount;
      if (total > this.limit) {
         this.amount = this.limit;
         if (!this.voidExcess) {
            return (int)(total - this.limit);
         }
      } else {
         this.amount += (long)amount;
      }

      return 0;
   }

   public synchronized void reduceAmount(int amount) {
      this.amount -= (long)amount;
   }

   @Nullable
   public synchronized ItemStack withdrawItem(int amount) {
      if (this.getItemStack() == null) {
         return null;
      } else {
         ItemStack clone = this.getItemStack().clone();
         int amt = (int)Math.min(this.amount, (long)amount);
         this.reduceAmount(amt);
         clone.setAmount(amt);
         return clone;
      }
   }

   @Nullable
   public synchronized ItemStack withdrawItem() {
      return this.getItemStack() == null ? null : this.withdrawItem(this.getItemStack().getMaxStackSize());
   }

   public void addMetaLore(@NotNull ItemMeta itemMeta) {
      List<String> old = itemMeta.getLore();
      List<String> lore = old != null ? new ArrayList<>(old) : new ArrayList<>();
      String itemName = Lang.getString("messages.normal-operation.quantum_cache.empty");
      if (this.getItemStack() != null) {
         itemName = ItemStackHelper.getDisplayName(this.getItemStack());
      }

      lore.add("");
      lore.add(String.format(Lang.getString("messages.normal-operation.quantum_cache.stored_item"), itemName));
      lore.add(String.format(Lang.getString("messages.normal-operation.quantum_cache.stored_amount"), this.getAmountLong()));
      if (this.supportsCustomMaxAmount) {
         lore.add(String.format(Lang.getString("messages.normal-operation.quantum_cache.custom_max_limit"), this.getLimit()));
      }

      itemMeta.setLore(lore);
   }

   public void updateMetaLore(@NotNull ItemMeta itemMeta) {
      List<String> lore = (List<String>)(itemMeta.hasLore() ? itemMeta.getLore() : new ArrayList<>());
      if (lore == null) {
         lore = new ArrayList<>();
      }

      String itemName = Lang.getString("messages.normal-operation.quantum_cache.empty");
      if (this.getItemStack() != null) {
         itemName = ItemStackHelper.getDisplayName(this.getItemStack());
      }

      int loreIndexModifier = this.supportsCustomMaxAmount ? 1 : 0;
      lore.set(lore.size() - 2 - loreIndexModifier, String.format(Lang.getString("messages.normal-operation.quantum_cache.stored_item"), itemName));
      lore.set(
         lore.size() - 1 - loreIndexModifier, String.format(Lang.getString("messages.normal-operation.quantum_cache.stored_amount"), this.getAmountLong())
      );
      if (this.supportsCustomMaxAmount) {
         lore.set(
            lore.size() - loreIndexModifier, String.format(Lang.getString("messages.normal-operation.quantum_cache.custom_max_limit"), this.getLimitLong())
         );
      }

      itemMeta.setLore(lore);
   }

   @Nullable
   @Override
   public ItemStack getItemStack() {
      return super.getItemStack();
   }

   @Override
   public synchronized void setItemStack(ItemStack itemStack) {
      super.setItemStack(itemStack);
   }

   @Generated
   public void setLimit(long limit) {
      this.limit = limit;
   }

   @Generated
   public long getAmount() {
      return this.amount;
   }

   @Generated
   public void setVoidExcess(boolean voidExcess) {
      this.voidExcess = voidExcess;
   }

   @Generated
   public boolean isVoidExcess() {
      return this.voidExcess;
   }
}
