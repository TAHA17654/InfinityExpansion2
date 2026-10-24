package com.ytdd9527.networksexpansion.utils;

import io.github.sefiraat.networks.Networks;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class ParticleUtil {
   private static final double[] BLOCK_CUBE_OFFSET_X = new double[]{0.0, 1.0, 0.0, 0.0, 1.0, 1.0, 0.0, 1.0};
   private static final double[] BLOCK_CUBE_OFFSET_Y = new double[]{0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 1.0, 1.0};
   private static final double[] BLOCK_CUBE_OFFSET_Z = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, 1.0, 1.0, 1.0};

   public static void drawLineByTotalAmount(@NotNull Particle particle, int totalAmount, @NotNull Location... locations) {
      for (int i = 0; i < locations.length; i++) {
         if (i + 1 < locations.length) {
            Location location1 = locations[i];
            Location location2 = locations[i + 1];
            if (totalAmount < 1 || location1.getWorld() == null || location1.getWorld() != location2.getWorld()) {
               return;
            }

            World world = location1.getWorld();
            double[] x = JavaUtil.disperse(totalAmount, location1.getX(), location2.getX());
            double[] y = JavaUtil.disperse(totalAmount, location1.getY(), location2.getY());
            double[] z = JavaUtil.disperse(totalAmount, location1.getZ(), location2.getZ());

            for (int j = 0; j < totalAmount; j++) {
               world.spawnParticle(particle, x[j], y[j], z[j], 1, 0.0, 0.0, 0.0, 0.0);
            }
         }
      }
   }

   public static void drawLineByTotalAmount(@NotNull Particle particle, int totalAmount, @NotNull List<Location> locationList) {
      Location[] locations = new Location[locationList.size()];

      for (int i = 0; i < locations.length; i++) {
         locations[i] = locationList.get(i);
      }

      drawLineByTotalAmount(particle, totalAmount, locations);
   }

   public static void drawLineByDistance(@NotNull Plugin plugin, @NotNull Particle particle, long interval, double distance, @NotNull Location... locations) {
      int time = 0;

      for (int i = 0; i + 1 < locations.length; i++) {
         Location location1 = locations[i];
         Location location2 = locations[i + 1];
         if (distance == 0.0 || location1.getWorld() == null || location1.getWorld() != location2.getWorld()) {
            return;
         }

         World world = location1.getWorld();
         double x = location1.getX();
         double y = location1.getY();
         double z = location1.getZ();
         double d = location1.distance(location2);
         int particleCount = (int)(d / distance);
         double px = (location2.getX() - x) / (double)particleCount;
         double py = (location2.getY() - y) / (double)particleCount;
         double pz = (location2.getZ() - z) / (double)particleCount;
         double t = (double)time;
         int tick = (int)(t / 50.0);
         List<Runnable> runnableList = new ArrayList<>();

         for (int j = 0; j < particleCount; j++) {
            x += px;
            y += py;
            z += pz;
            runnableList.add(() -> world.spawnParticle(particle, x, y, z, 1, 0.0, 0.0, 0.0, 0.0));
            t += (double)interval / (double)particleCount;
            int lastTick = (int)(t / 50.0);
            if (tick != lastTick) {
               List<Runnable> finalRunnableList = runnableList;
               plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> finalRunnableList.forEach(Runnable::run), (long)tick);
               tick = lastTick;
               runnableList = new ArrayList<>();
            }
         }

         if (!runnableList.isEmpty()) {
            List<Runnable> finalRunnableList = runnableList;
            plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> finalRunnableList.forEach(Runnable::run), (long)tick);
         }

         time += (int)interval;
      }
   }

   public static void drawLineByDistance(
      @NotNull Plugin plugin, @NotNull Particle particle, long interval, double distance, @NotNull List<Location> locationList
   ) {
      Location[] locations = new Location[locationList.size()];

      for (int i = 0; i < locations.length; i++) {
         locations[i] = locationList.get(i);
      }

      drawLineByDistance(plugin, particle, interval, distance, locations);
   }

   public static void drawCubeByLocations(@NotNull Plugin plugin, @NotNull Particle particle, long interval, @NotNull Location... locations) {
      int time = 0;

      for (Location location : locations) {
         World world = location.getWorld();
         if (world != null) {
            int x = location.getBlockX();
            int y = location.getBlockY();
            int z = location.getBlockZ();
            if (time < 50) {
               for (int i = 0; i < BLOCK_CUBE_OFFSET_X.length; i++) {
                  world.spawnParticle(
                     particle,
                     (double)x + BLOCK_CUBE_OFFSET_X[i],
                     (double)y + BLOCK_CUBE_OFFSET_Y[i],
                     (double)z + BLOCK_CUBE_OFFSET_Z[i],
                     1,
                     0.0,
                     0.0,
                     0.0,
                     0.0
                  );
               }
            } else {
               plugin.getServer()
                  .getScheduler()
                  .runTaskLaterAsynchronously(
                     plugin,
                     () -> {
                        for (int ix = 0; ix < BLOCK_CUBE_OFFSET_X.length; ix++) {
                           world.spawnParticle(
                              particle,
                              (double)x + BLOCK_CUBE_OFFSET_X[ix],
                              (double)y + BLOCK_CUBE_OFFSET_Y[ix],
                              (double)z + BLOCK_CUBE_OFFSET_Z[ix],
                              1,
                              0.0,
                              0.0,
                              0.0,
                              0.0
                           );
                        }
                     },
                     (long)(time / 50)
                  );
            }

            time += (int)interval;
         }
      }
   }

   public static void drawCubeByLocations(@NotNull Plugin plugin, @NotNull Particle particle, long interval, @NotNull List<Location> locationList) {
      Location[] locations = new Location[locationList.size()];

      for (int i = 0; i < locationList.size(); i++) {
         locations[i] = locationList.get(i);
      }

      drawCubeByLocations(plugin, particle, interval, locations);
   }

   public static void drawRegionOutline(@NotNull Plugin plugin, @NotNull Particle particle, long interval, @NotNull Location corner1, @NotNull Location corner2) {
      World world = corner1.getWorld();
      if (world != null && corner1.getWorld() == corner2.getWorld()) {
         double minX = Math.min(corner1.getX(), corner2.getX());
         double minY = Math.min(corner1.getY(), corner2.getY());
         double minZ = Math.min(corner1.getZ(), corner2.getZ());
         double maxX = Math.max(corner1.getX(), corner2.getX());
         double maxY = Math.max(corner1.getY(), corner2.getY());
         double maxZ = Math.max(corner1.getZ(), corner2.getZ());
         Location[] corners = new Location[]{
            new Location(world, minX, minY, minZ),
            new Location(world, minX, minY, maxZ + 1.0),
            new Location(world, maxX + 1.0, minY, maxZ + 1.0),
            new Location(world, maxX + 1.0, minY, minZ),
            new Location(world, minX, maxY + 1.0, minZ),
            new Location(world, minX, maxY + 1.0, maxZ + 1.0),
            new Location(world, maxX + 1.0, maxY + 1.0, maxZ + 1.0),
            new Location(world, maxX + 1.0, maxY + 1.0, minZ)
         };
         drawLineByDistance(plugin, particle, interval, 0.25, corners[0], corners[1]);
         drawLineByDistance(plugin, particle, interval, 0.25, corners[0], corners[3]);
         drawLineByDistance(plugin, particle, interval, 0.25, corners[0], corners[4]);
         drawLineByDistance(plugin, particle, interval, 0.25, corners[1], corners[2]);
         drawLineByDistance(plugin, particle, interval, 0.25, corners[1], corners[5]);
         drawLineByDistance(plugin, particle, interval, 0.25, corners[2], corners[3]);
         drawLineByDistance(plugin, particle, interval, 0.25, corners[2], corners[6]);
         drawLineByDistance(plugin, particle, interval, 0.25, corners[3], corners[7]);
         drawLineByDistance(plugin, particle, interval, 0.25, corners[4], corners[5]);
         drawLineByDistance(plugin, particle, interval, 0.25, corners[4], corners[7]);
         drawLineByDistance(plugin, particle, interval, 0.25, corners[5], corners[6]);
         drawLineByDistance(plugin, particle, interval, 0.25, corners[6], corners[7]);
      }
   }

   public static void drawCubeByBlock(@NotNull Plugin plugin, @NotNull Particle particle, long interval, @NotNull Block... blocks) {
      int time = 0;

      for (Block block : blocks) {
         Location location = block.getLocation();
         World world = location.getWorld();
         if (world != null) {
            int x = location.getBlockX();
            int y = location.getBlockY();
            int z = location.getBlockZ();
            if (time < 50) {
               for (int i = 0; i < BLOCK_CUBE_OFFSET_X.length; i++) {
                  world.spawnParticle(
                     particle,
                     (double)x + BLOCK_CUBE_OFFSET_X[i],
                     (double)y + BLOCK_CUBE_OFFSET_Y[i],
                     (double)z + BLOCK_CUBE_OFFSET_Z[i],
                     1,
                     0.0,
                     0.0,
                     0.0,
                     0.0
                  );
               }
            } else {
               plugin.getServer()
                  .getScheduler()
                  .runTaskLaterAsynchronously(
                     plugin,
                     () -> {
                        for (int ix = 0; ix < BLOCK_CUBE_OFFSET_X.length; ix++) {
                           world.spawnParticle(
                              particle,
                              (double)x + BLOCK_CUBE_OFFSET_X[ix],
                              (double)y + BLOCK_CUBE_OFFSET_Y[ix],
                              (double)z + BLOCK_CUBE_OFFSET_Z[ix],
                              1,
                              0.0,
                              0.0,
                              0.0,
                              0.0
                           );
                        }
                     },
                     (long)(time / 50)
                  );
            }

            time += (int)interval;
         }
      }
   }

   public static void drawCubeByBlock(@NotNull Plugin plugin, @NotNull Particle particle, long interval, @NotNull List<Block> blockList) {
      Block[] blocks = new Block[blockList.size()];

      for (int i = 0; i < blockList.size(); i++) {
         blocks[i] = blockList.get(i);
      }

      drawCubeByBlock(plugin, particle, interval, blocks);
   }

   public static void drawLineFrom(@NotNull Location location1, @NotNull Location location2) {
      drawLineByTotalAmount(Particle.WAX_OFF, (int)location1.distance(location2) * 4, location1, location2);
   }

   public static void highlightBlock(@NotNull Location location) {
      highlightBlock(location.getBlock());
   }

   public static void highlightBlock(Block block) {
      drawCubeByBlock(Networks.getInstance(), Particle.WAX_ON, 1L, block);
   }

   public static void highlightBlock(@NotNull Player player, @NotNull Location location, int shrinkTimes) {
      for (int i = 0; i < shrinkTimes; i++) {
         Bukkit.getScheduler().runTaskLaterAsynchronously(Networks.getInstance(), () -> {
            drawLineFrom(player.getEyeLocation().clone().add(0.0, -0.5, 0.0), location);
            highlightBlock(location);
         }, 20L * (long)i);
      }
   }
}
