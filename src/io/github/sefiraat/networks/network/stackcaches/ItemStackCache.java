package io.github.sefiraat.networks.network.stackcaches;

import lombok.Generated;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

public class ItemStackCache {
   @UnknownNullability
   protected ItemStack itemStack;
   @Nullable
   protected ItemMeta itemMeta = null;
   protected boolean metaCached = false;

   public ItemStackCache(@Nullable ItemStack itemStack) {
      this.itemStack = itemStack;
   }

   @Nullable
   public ItemStack getItemStack() {
      return this.itemStack;
   }

   public void setItemStack(ItemStack itemStack) {
      this.itemStack = itemStack;
      this.metaCached = false;
      this.itemMeta = null;
   }

   @Nullable
   public ItemMeta getItemMeta() {
      if (this.itemMeta == null && !this.metaCached) {
         this.itemMeta = this.itemStack == null ? null : (this.itemStack.hasItemMeta() ? this.itemStack.getItemMeta() : null);
         this.metaCached = !this.metaCached;
      }

      return this.itemMeta;
   }

   @Nullable
   public Material getItemType() {
      return this.itemStack == null ? null : this.itemStack.getType();
   }

   @Generated
   @Override
   public String toString() {
      return "ItemStackCache(itemStack=" + this.getItemStack() + ", itemMeta=" + this.getItemMeta() + ", metaCached=" + this.metaCached + ")";
   }
}
