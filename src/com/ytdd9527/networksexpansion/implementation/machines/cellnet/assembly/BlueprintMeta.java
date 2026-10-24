package com.ytdd9527.networksexpansion.implementation.machines.cellnet.assembly;

import io.github.sefiraat.networks.network.stackcaches.BlueprintInstance;
import io.github.sefiraat.networks.utils.Keys;
import io.github.thebusybiscuit.slimefun4.libraries.dough.data.persistent.PersistentDataAPI;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BlueprintMeta {
   private BlueprintMeta() {
   }

   public static long getMetaLong(@Nullable ItemStack itemStack, @NotNull NamespacedKey key, long def) {
      return itemStack != null && itemStack.hasItemMeta() ? PersistentDataAPI.getLong(itemStack.getItemMeta(), key, def) : def;
   }

   public static void setMetaLong(@NotNull ItemStack itemStack, @NotNull NamespacedKey key, long value) {
      itemStack.editMeta(meta -> PersistentDataAPI.setLong(meta, key, value));
   }

   @Nullable
   public static BlueprintInstance readBlueprint(@Nullable ItemStack blueprintItem) {
      if (blueprintItem != null && !blueprintItem.getType().isAir() && blueprintItem.hasItemMeta()) {
         ItemMeta meta = blueprintItem.getItemMeta();
         if (meta == null) {
            return BlueprintInstance.INVALID;
         } else {
            BlueprintInstance instance = Keys.getBlueprintInstance(meta);
            return instance == null ? BlueprintInstance.INVALID : instance;
         }
      } else {
         return BlueprintInstance.INVALID;
      }
   }
}
