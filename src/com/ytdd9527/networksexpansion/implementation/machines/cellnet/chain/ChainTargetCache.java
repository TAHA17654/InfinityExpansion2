package com.ytdd9527.networksexpansion.implementation.machines.cellnet.chain;

import com.balugaq.netex.api.data.VanillaInventoryWrapper;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ChainTargetCache {
   private static final long TICK_MS = 50L;
   private static final int MAX_EMPTY_SCANS = 3;
   private static final int CACHE_CAPACITY = 4096;
   private static final Map<ChainTargetCache.CacheKey, ChainTargetCache.CachedLine> CACHE = new ConcurrentHashMap<>();

   private ChainTargetCache() {
   }

   @NotNull
   public static List<Location> targets(
      @NotNull Location origin, @NotNull BlockFace direction, int distance, boolean vanillaTargets, int workTtlTicks, int idleTtlTicks
   ) {
      long nowMs = System.currentTimeMillis();
      ChainTargetCache.CacheKey key = new ChainTargetCache.CacheKey(origin, direction, vanillaTargets);
      ChainTargetCache.CachedLine line = CACHE.computeIfAbsent(key, k -> new ChainTargetCache.CachedLine(distance));
      if (line.distance != distance) {
         line = new ChainTargetCache.CachedLine(distance);
         CACHE.put(key, line);
      }

      if (nowMs < line.expiresAtMs) {
         return line.locationsView;
      } else {
         List<ChainTargetCache.Target> scanned = scan(origin, direction, distance, vanillaTargets);
         line.targets = scanned;
         List<Location> view = new ArrayList<>(scanned.size());

         for (ChainTargetCache.Target target : scanned) {
            view.add(target.location());
         }

         line.locationsView = view;
         if (scanned.isEmpty()) {
            line.emptyScans++;
         } else {
            line.emptyScans = 0;
         }

         long ttlTicks = line.emptyScans >= 3 ? (long)idleTtlTicks : (long)workTtlTicks;
         line.expiresAtMs = nowMs + ttlTicks * 50L;
         if (CACHE.size() > 4096) {
            CACHE.values().removeIf(entry -> nowMs >= entry.expiresAtMs);
         }

         return view;
      }
   }

   @Nullable
   public static BlockMenu resolveMenu(@NotNull Location location, boolean vanilla) {
      if (!vanilla) {
         return StorageCacheUtils.getMenu(location);
      } else {
         BlockState state = location.getBlock().getState(false);
         return state instanceof InventoryHolder holder ? new VanillaInventoryWrapper(holder.getInventory(), state) : null;
      }
   }

   @NotNull
   private static List<ChainTargetCache.Target> scan(@NotNull Location origin, @NotNull BlockFace direction, int distance, boolean vanillaTargets) {
      List<ChainTargetCache.Target> targets = new ArrayList<>();
      Location cursor = origin.clone();

      for (int i = 0; i < distance; i++) {
         cursor.set(
            (double)(cursor.getBlockX() + direction.getModX()),
            (double)(cursor.getBlockY() + direction.getModY()),
            (double)(cursor.getBlockZ() + direction.getModZ())
         );
         if (StorageCacheUtils.getMenu(cursor) != null) {
            targets.add(new ChainTargetCache.Target(cursor.clone(), false));
         } else {
            if (!vanillaTargets || !isVanillaContainer(cursor)) {
               break;
            }

            targets.add(new ChainTargetCache.Target(cursor.clone(), true));
         }
      }

      return targets;
   }

   private static boolean isVanillaContainer(@NotNull Location location) {
      BlockState state = location.getBlock().getState(false);
      return state instanceof InventoryHolder;
   }

   private static record CacheKey(@NotNull Location origin, @NotNull BlockFace direction, boolean vanilla) {
   }

   private static final class CachedLine {
      private final int distance;
      private volatile List<ChainTargetCache.Target> targets = List.of();
      private volatile List<Location> locationsView = List.of();
      private volatile long expiresAtMs;
      private int emptyScans;

      private CachedLine(int distance) {
         this.distance = distance;
      }
   }

   private static record Target(@NotNull Location location, boolean vanilla) {
   }
}
