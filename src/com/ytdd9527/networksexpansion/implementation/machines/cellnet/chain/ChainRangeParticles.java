package com.ytdd9527.networksexpansion.implementation.machines.cellnet.chain;

import io.github.sefiraat.networks.Networks;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

public final class ChainRangeParticles {
   private static final long FADE_DELAY_TICKS = 100L;
   private static final int PERIOD_TICKS = 5;
   private static final double EDGE_STEP = 0.5;
   private static final int[][] CORNERS = new int[][]{{0, 0}, {1, 0}, {0, 1}, {1, 1}};
   private static final Map<Location, ChainRangeParticles.ParticleState> STATES = new ConcurrentHashMap<>();

   private ChainRangeParticles() {
   }

   public static void show(@NotNull Location origin, @NotNull Set<BlockFace> directions, int distance) {
      ChainRangeParticles.ParticleState s = STATES.get(origin);
      if (s != null) {
         if (s.pendingFade != null) {
            s.pendingFade.cancel();
            s.pendingFade = null;
         }

         if (s.active != null) {
            s.active.cancel();
            s.active = null;
         }
      }

      if (!directions.isEmpty()) {
         List<BlockFace> dirs = List.copyOf(directions);
         int dist = Math.max(1, distance);
         World world = origin.getWorld();
         if (world != null) {
            int[] spans = new int[dirs.size()];

            for (int i = 0; i < dirs.size(); i++) {
               BlockFace d = dirs.get(i);

               for (int j = 1; j <= dist; spans[i] = j++) {
                  Block block = world.getBlockAt(
                     origin.getBlockX() + d.getModX() * j, origin.getBlockY() + d.getModY() * j, origin.getBlockZ() + d.getModZ() * j
                  );
                  if (block.getType().isAir()) {
                     break;
                  }
               }
            }

            BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(Networks.getInstance(), () -> draw(world, origin, dirs, spans, dist), 1L, 5L);
            STATES.computeIfAbsent(origin, k -> new ChainRangeParticles.ParticleState()).active = task;
         }
      }
   }

   public static void fadeLater(@NotNull Location origin) {
      ChainRangeParticles.ParticleState s = STATES.get(origin);
      if (s != null && s.active != null) {
         if (s.pendingFade == null) {
            BukkitTask faded = s.active;
            s.pendingFade = Bukkit.getScheduler().runTaskLaterAsynchronously(Networks.getInstance(), () -> {
               ChainRangeParticles.ParticleState st = STATES.get(origin);
               if (st != null && st.active == faded) {
                  if (st.active != null) {
                     st.active.cancel();
                     st.active = null;
                  }

                  st.pendingFade = null;
               }
            }, 100L);
         }
      }
   }

   public static void stop(@NotNull Location origin) {
      ChainRangeParticles.ParticleState s = STATES.remove(origin);
      if (s != null) {
         if (s.active != null) {
            s.active.cancel();
         }

         if (s.pendingFade != null) {
            s.pendingFade.cancel();
         }
      }
   }

   private static void draw(@NotNull World world, @NotNull Location origin, @NotNull List<BlockFace> dirs, @NotNull int[] spans, int distance) {
      for (int i = 0; i < dirs.size(); i++) {
         drawTunnel(world, origin, dirs.get(i), spans[i]);
      }
   }

   private static void drawTunnel(@NotNull World world, @NotNull Location origin, @NotNull BlockFace direction, int span) {
      double dx = (double)direction.getModX();
      double dy = (double)direction.getModY();
      double dz = (double)direction.getModZ();
      double ux;
      double uy;
      double uz;
      double vx;
      double vy;
      double vz;
      if (dy != 0.0) {
         ux = 1.0;
         uy = 0.0;
         uz = 0.0;
         vx = 0.0;
         vy = 0.0;
         vz = 1.0;
      } else if (dx != 0.0) {
         ux = 0.0;
         uy = 1.0;
         uz = 0.0;
         vx = 0.0;
         vy = 0.0;
         vz = 1.0;
      } else {
         ux = 1.0;
         uy = 0.0;
         uz = 0.0;
         vx = 0.0;
         vy = 1.0;
         vz = 0.0;
      }

      double ox = (double)origin.getBlockX();
      double oy = (double)origin.getBlockY();
      double oz = (double)origin.getBlockZ();

      for (int[] corner : CORNERS) {
         double cx = ox + ux * (double)corner[0] + vx * (double)corner[1];
         double cy = oy + uy * (double)corner[0] + vy * (double)corner[1];
         double cz = oz + uz * (double)corner[0] + vz * (double)corner[1];

         for (double t = 1.0; t <= (double)span; t += 0.5) {
            spawnEdge(world, cx + dx * t, cy + dy * t, cz + dz * t, Math.abs(dx), Math.abs(dy), Math.abs(dz));
         }
      }
   }

   private static void spawnEdge(@NotNull World world, double x, double y, double z, double ax, double ay, double az) {
      world.spawnParticle(Particle.COMPOSTER, x, y, z, 3, ax * 0.33, ay * 0.33, az * 0.33, 0.0);
   }

   private static final class ParticleState {
      volatile BukkitTask active;
      volatile BukkitTask pendingFade;
   }
}
