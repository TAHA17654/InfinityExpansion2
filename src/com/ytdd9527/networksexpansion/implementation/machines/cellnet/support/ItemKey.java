package com.ytdd9527.networksexpansion.implementation.machines.cellnet.support;

import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.rule.CellAcceptRules;
import com.ytdd9527.networksexpansion.utils.itemstacks.ItemStackUtil;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.core.attributes.DistinctiveItem;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemKey {
   private final ItemStack template;
   private final int hash;
   private volatile Boolean oversized;

   public ItemKey(@NotNull ItemStack itemStack) {
      this(itemStack, ItemKey.Mode.CANONICAL);
   }

   private ItemKey(@NotNull ItemStack itemStack, @NotNull ItemKey.Mode mode) {
      if (mode == ItemKey.Mode.LIVE) {
         this.template = itemStack;
         this.hash = ItemStackUtil.hashItemComponents(itemStack);
      } else {
         ItemStack unit = itemStack.asOne();
         if (mode == ItemKey.Mode.CANONICAL) {
            SlimefunItem sfItem = SlimefunItem.getByItem(unit);
            if (sfItem != null && !(sfItem instanceof DistinctiveItem)) {
               unit = toPlainCopy(sfItem.getItem());
            }
         }

         this.template = unit;
         this.hash = unit.hashCode();
      }
   }

   @NotNull
   public static ItemKey exact(@NotNull ItemStack itemStack) {
      return new ItemKey(itemStack, ItemKey.Mode.EXACT);
   }

   @NotNull
   public static ItemKey exactLive(@NotNull ItemStack itemStack) {
      return !ItemStackUtil.componentsBridgeAvailable() ? exact(itemStack) : new ItemKey(itemStack, ItemKey.Mode.LIVE);
   }

   @NotNull
   private static ItemStack toPlainCopy(@NotNull ItemStack slimefunStack) {
      ItemStack plain = new ItemStack(slimefunStack.getType());
      ItemMeta meta = slimefunStack.getItemMeta();
      if (meta != null) {
         plain.setItemMeta(meta);
      }

      return plain;
   }

   @NotNull
   public ItemStack getItemStack() {
      return this.template.clone();
   }

   public boolean hasMeta() {
      return this.template.hasItemMeta();
   }

   public boolean isOversized() {
      Boolean cached = this.oversized;
      if (cached == null) {
         cached = CellAcceptRules.isNbtOversized(this.template);
         this.oversized = cached;
      }

      return cached;
   }

   public boolean matches(@Nullable ItemStack other) {
      return other != null && this.template.isSimilar(other);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         if (o instanceof ItemKey that && this.hash == that.hash && this.template.isSimilar(that.template)) {
            return true;
         }

         return false;
      }
   }

   @Override
   public int hashCode() {
      return this.hash;
   }

   private static enum Mode {
      CANONICAL,
      EXACT,
      LIVE;
   }
}
