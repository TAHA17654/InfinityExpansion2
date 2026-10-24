package com.balugaq.netex.utils;

import com.balugaq.netex.api.enums.MinecraftVersion;
import io.github.sefiraat.networks.Networks;
import java.lang.reflect.Field;
import lombok.Generated;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;

public final class NetworksVersionedEnchantment {
   @NotNull
   public static final Enchantment GLOW;
   @NotNull
   public static final Enchantment LUCK_OF_THE_SEA;

   @NotNull
   private static Enchantment getKey(@NotNull String key) {
      try {
         Field field = Enchantment.class.getDeclaredField(key);
         return (Enchantment)field.get(null);
      } catch (Exception var2) {
         Debug.trace(var2);
         return null;
      }
   }

   @Generated
   private NetworksVersionedEnchantment() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   static {
      MinecraftVersion version = Networks.getInstance().getMCVersion();
      GLOW = version.isAtLeast(MinecraftVersion.V1_20_5) ? Enchantment.POWER : getKey("ARROW_DAMAGE");
      LUCK_OF_THE_SEA = version.isAtLeast(MinecraftVersion.V1_20_5) ? Enchantment.LUCK_OF_THE_SEA : getKey("LUCK");
   }
}
