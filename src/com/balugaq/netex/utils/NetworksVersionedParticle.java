package com.balugaq.netex.utils;

import com.balugaq.netex.api.enums.MinecraftVersion;
import io.github.sefiraat.networks.Networks;
import java.lang.reflect.Field;
import lombok.Generated;
import org.bukkit.Particle;
import org.jetbrains.annotations.NotNull;

public final class NetworksVersionedParticle {
   @NotNull
   public static final Particle DUST;
   @NotNull
   public static final Particle EXPLOSION;
   @NotNull
   public static final Particle SMOKE;

   @NotNull
   private static Particle getKey(@NotNull String key) {
      try {
         Field field = Particle.class.getDeclaredField(key);
         return (Particle)field.get(null);
      } catch (Exception var2) {
         return null;
      }
   }

   @Generated
   private NetworksVersionedParticle() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   static {
      MinecraftVersion version = Networks.getInstance().getMCVersion();
      DUST = version.isAtLeast(MinecraftVersion.V1_20_5) ? Particle.DUST : getKey("REDSTONE");
      EXPLOSION = version.isAtLeast(MinecraftVersion.V1_20_5) ? Particle.EXPLOSION : getKey("EXPLOSION_LARGE");
      SMOKE = version.isAtLeast(MinecraftVersion.V1_20_5) ? Particle.SMOKE : getKey("SMOKE_NORMAL");
   }
}
