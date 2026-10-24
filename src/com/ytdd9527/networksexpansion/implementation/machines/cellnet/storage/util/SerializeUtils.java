package com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage.util;

import com.balugaq.netex.utils.Debug;
import java.util.Base64;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SerializeUtils {
   private SerializeUtils() {
   }

   @NotNull
   public static String object2String(@NotNull ItemStack item) {
      return Base64.getEncoder().encodeToString(item.serializeAsBytes());
   }

   @Nullable
   public static ItemStack string2Object(@NotNull String base64) {
      try {
         ItemStack item = ItemStack.deserializeBytes(Base64.getDecoder().decode(base64));
         return item != null && !item.getType().isAir() ? item : null;
      } catch (RuntimeException var2) {
         Debug.trace(var2, "Failed to deserialize item base64");
         return null;
      }
   }
}
