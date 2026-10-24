package com.ytdd9527.networksexpansion.implementation.machines.cellnet.assembly.core;

import io.github.sefiraat.networks.utils.Keys;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.data.persistent.PersistentDataAPI;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SmartCore extends GearCore {
   public SmartCore(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe);
   }

   @Override
   public int gearBonus() {
      return 0;
   }

   public static boolean isSmartCore(@Nullable ItemStack itemStack) {
      return itemStack != null && !itemStack.getType().isAir() && SlimefunItem.getByItem(itemStack) instanceof SmartCore;
   }

   @Nullable
   public static String getChannel(@NotNull ItemStack core) {
      if (!core.hasItemMeta()) {
         return null;
      } else {
         ItemMeta meta = core.getItemMeta();
         if (meta == null) {
            return null;
         } else {
            String channel = PersistentDataAPI.getString(meta, Keys.SMART_CHANNEL);
            return channel != null && !channel.isEmpty() ? channel : null;
         }
      }
   }

   public static void setChannel(@NotNull ItemStack core, @Nullable String channel) {
      if (core.hasItemMeta()) {
         ItemMeta meta = core.getItemMeta();
         if (meta != null) {
            if (channel != null && !channel.isEmpty()) {
               PersistentDataAPI.setString(meta, Keys.SMART_CHANNEL, channel);
            } else {
               PersistentDataAPI.remove(meta, Keys.SMART_CHANNEL);
            }

            core.setItemMeta(meta);
         }
      }
   }
}
