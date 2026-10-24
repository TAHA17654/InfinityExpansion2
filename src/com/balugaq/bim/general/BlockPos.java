package com.balugaq.bim.general;

import io.papermc.paper.math.BlockPosition;
import java.lang.ref.WeakReference;
import org.bukkit.Location;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record BlockPos(WeakReference<World> world, int blockX, int blockY, int blockZ) implements BlockPosition {
   public static BlockPos from(Location location) {
      return new BlockPos(new WeakReference<>(location.getWorld()), location.getBlockX(), location.getBlockY(), location.getBlockZ());
   }

   public Location toLocation() {
      return this.toLocation(this.world.get());
   }

   public String getTag() {
      return this.blockX + ":" + this.blockY + ":" + this.blockZ;
   }
}
