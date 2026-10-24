package com.ytdd9527.networksexpansion.implementation.machines.cellnet.drive;

import com.balugaq.netex.utils.Lang;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.api.DriveType;
import io.github.sefiraat.networks.Networks;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public final class DriveGuide {
   private static final long MAX_DURATION_MS = 120000L;
   private static final double ARRIVE_DISTANCE = 2.5;
   private static final int PERIOD_TICKS = 2;
   private static final double ARROW_LEAD = 2.0;
   private static final double ARROW_HEAD = 0.7;
   private static final double ARROW_HEAD_HALF_WIDTH = 0.4;
   private static final double SHAFT_LENGTH = 1.2;
   private static final double ARROW_Y_OFFSET = 0.2;
   private static final int LINE_POINTS = 6;
   private static final long ACTIONBAR_INTERVAL_MS = 500L;
   private static final DustOptions ARROW_DUST = new DustOptions(Color.fromRGB(16755200), 1.4F);
   private static final Map<UUID, DriveGuide.GuideSession> ACTIVE = new ConcurrentHashMap<>();

   private DriveGuide() {
   }

   public static void start(@NotNull Player player, @NotNull Location target) {
      boolean replaced = stop(player);
      Location anchor = target.clone().add(0.5, 0.5, 0.5);
      UUID playerId = player.getUniqueId();
      long startedAt = System.currentTimeMillis();
      BukkitTask task = Networks.getInstance().getServer().getScheduler().runTaskTimer(Networks.getInstance(), () -> tick(player, anchor, startedAt), 0L, 2L);
      ACTIVE.put(playerId, new DriveGuide.GuideSession(task));
      player.sendMessage(Lang.getString(replaced ? "messages.cellnet.manager.guide_replaced" : "messages.cellnet.manager.guide_started"));
   }

   public static boolean stop(@NotNull Player player) {
      DriveGuide.GuideSession session = ACTIVE.remove(player.getUniqueId());
      if (session == null) {
         return false;
      } else {
         session.task.cancel();
         return true;
      }
   }

   private static void tick(@NotNull Player player, @NotNull Location target, long startedAt) {
      if (player.isOnline()
         && !player.isDead()
         && System.currentTimeMillis() - startedAt <= 120000L
         && DriveType.of(StorageCacheUtils.getSfItem(target)) != null
         && player.getWorld().equals(target.getWorld())) {
         Location feet = player.getLocation();
         double distance = feet.distance(target);
         if (distance <= 2.5) {
            stop(player);
            player.sendMessage(Lang.getString("messages.cellnet.manager.guide_arrived"));
         } else {
            drawArrow(player, feet, target);
            sendDistance(player, feet, target, distance);
         }
      } else {
         stop(player);
      }
   }

   private static void drawArrow(@NotNull Player player, @NotNull Location feet, @NotNull Location target) {
      Vector direction = target.toVector().subtract(feet.toVector());
      direction.setY(0);
      if (!(direction.lengthSquared() < 1.0E-4)) {
         direction.normalize();
         Vector side = new Vector(-direction.getZ(), 0.0, direction.getX());
         double baseY = feet.getY() + 0.2;
         Vector base = feet.toVector().add(direction.clone().multiply(2.0));
         base.setY(baseY);
         Vector tip = base.clone().add(direction.clone().multiply(0.7));
         Vector cornerLeft = base.clone().add(side.clone().multiply(0.4));
         Vector cornerRight = base.clone().subtract(side.clone().multiply(0.4));
         Vector shaftEnd = base.clone().subtract(direction.clone().multiply(1.2));
         drawLine(player, cornerLeft, tip);
         drawLine(player, cornerRight, tip);
         drawLine(player, base, shaftEnd);
      }
   }

   private static void drawLine(@NotNull Player player, @NotNull Vector from, @NotNull Vector to) {
      for (int i = 0; i < 6; i++) {
         double ratio = (double)i / 6.0;
         Vector point = from.clone().multiply(1.0 - ratio).add(to.clone().multiply(ratio));
         player.spawnParticle(Particle.DUST, point.getX(), point.getY(), point.getZ(), 1, 0.0, 0.0, 0.0, 0.0, ARROW_DUST);
      }
   }

   private static void sendDistance(@NotNull Player player, @NotNull Location feet, @NotNull Location target, double distance) {
      DriveGuide.GuideSession session = ACTIVE.get(player.getUniqueId());
      if (session != null) {
         long now = System.currentTimeMillis();
         if (now - session.lastActionBar >= 500L) {
            session.lastActionBar = now;
            double heightDelta = target.getY() - feet.getY();
            String heightHint = heightDelta >= 2.0
               ? Lang.getString("messages.cellnet.manager.guide_up")
               : (heightDelta <= -2.0 ? Lang.getString("messages.cellnet.manager.guide_down") : "");
            Lang.get().sendActionbarMessage(player, "cellnet.manager.guide_distance", Math.round(distance), heightHint);
         }
      }
   }

   private static final class GuideSession {
      private final BukkitTask task;
      private long lastActionBar;

      private GuideSession(@NotNull BukkitTask task) {
         this.task = task;
      }
   }
}
