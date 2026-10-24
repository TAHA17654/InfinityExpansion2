package io.github.sefiraat.networks.network.stackcaches;

import com.balugaq.netex.utils.Lang;
import com.ytdd9527.networksexpansion.utils.TextUtil;
import io.github.sefiraat.networks.utils.Theme;
import java.util.List;
import lombok.Generated;
import net.guizhanss.minecraft.guizhanlib.gugu.minecraft.helpers.inventory.ItemStackHelper;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CardInstance extends ItemStackCache {
   private final int limit;
   private int amount;

   public CardInstance(@Nullable ItemStack itemStack, int amount, int limit) {
      super(itemStack);
      this.amount = amount;
      this.limit = limit;
   }

   @Nullable
   public ItemStack withdrawItem(int amount) {
      if (this.getItemStack() == null) {
         return null;
      } else {
         ItemStack clone = this.getItemStack().clone();
         clone.setAmount(Math.min(this.amount, amount));
         this.reduceAmount(clone.getAmount());
         return clone;
      }
   }

   @Nullable
   public ItemStack withdrawItem() {
      return this.getItemStack() == null ? null : this.withdrawItem(this.getItemStack().getMaxStackSize());
   }

   public void increaseAmount(int amount) {
      long total = (long)this.amount + (long)amount;
      if (total > (long)this.limit) {
         this.amount = this.limit;
      } else {
         this.amount += amount;
      }
   }

   public void reduceAmount(int amount) {
      this.amount -= amount;
   }

   public void updateLore(@NotNull ItemMeta itemMeta) {
      List<String> lore = itemMeta.getLore();
      if (lore != null) {
         lore.set(10, this.getLoreLine());
         itemMeta.setLore(lore);
      }
   }

   @NotNull
   public String getLoreLine() {
      if (this.getItemStack() == null) {
         return Lang.getString("messages.normal-operation.memory_card.empty");
      } else {
         String name = TextUtil.stripColor(ItemStackHelper.getDisplayName(this.getItemStack()));
         return Theme.CLICK_INFO + name + ": " + Theme.PASSIVE + this.amount;
      }
   }

   @Generated
   public int getLimit() {
      return this.limit;
   }

   @Generated
   public int getAmount() {
      return this.amount;
   }

   @Generated
   public void setAmount(int amount) {
      this.amount = amount;
   }
}
