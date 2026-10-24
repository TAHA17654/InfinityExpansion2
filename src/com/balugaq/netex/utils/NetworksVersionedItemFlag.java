package com.balugaq.netex.utils;

import com.balugaq.netex.api.enums.MinecraftVersion;
import io.github.sefiraat.networks.Networks;
import java.lang.reflect.Field;
import lombok.Generated;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class NetworksVersionedItemFlag {
   @Nullable
   public static final ItemFlag HIDE_ADDITIONAL_TOOLTIP;

   @Nullable
   private static ItemFlag getKey(@NotNull String key) {
      try {
         Field field = ItemFlag.class.getDeclaredField(key);
         return (ItemFlag)field.get(null);
      } catch (Exception var2) {
         return null;
      }
   }

   @Generated
   private NetworksVersionedItemFlag() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   static {
      MinecraftVersion version = Networks.getInstance().getMCVersion();
      HIDE_ADDITIONAL_TOOLTIP = version.isAtLeast(MinecraftVersion.V1_20_5) ? ItemFlag.HIDE_ADDITIONAL_TOOLTIP : getKey("HIDE_POTION_EFFECTS");
   }
}
