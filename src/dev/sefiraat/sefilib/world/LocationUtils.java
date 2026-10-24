package dev.sefiraat.sefilib.world;

import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nonnull;
import org.bukkit.Location;

public final class LocationUtils {
   private LocationUtils() {
      throw new IllegalStateException("Utility class");
   }

   @Nonnull
   public static Location randomLocation(@Nonnull Location centreLocation, int range) {
      return randomLocation(centreLocation, range, range, range);
   }

   @Nonnull
   public static Location randomLocation(@Nonnull Location centreLocation, int rangeX, int rangeY, int rangeZ) {
      double randomX = (double)ThreadLocalRandom.current().nextInt(-rangeX, rangeX + 1);
      double randomY = (double)ThreadLocalRandom.current().nextInt(-rangeY, rangeY + 1);
      double randomZ = (double)ThreadLocalRandom.current().nextInt(-rangeZ, rangeZ + 1);
      return centreLocation.clone().add(randomX, randomY, randomZ);
   }

   public static Location centre(@Nonnull Location location) {
      return location.clone().add(0.5, 0.5, 0.5);
   }
}
