package com.ytdd9527.networksexpansion.implementation.machines.cellnet.support;

import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.NodeDefinition;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class NetworkUtil {
   public static final long CACHE_TTL_MS = 5000L;
   private static final Map<Location, NetworkUtil.CacheEntry> CACHE = new ConcurrentHashMap<>();

   private NetworkUtil() {
   }

   @Nullable
   public static NetworkRoot findRoot(@NotNull Location location) {
      NodeDefinition self = NetworkStorage.getNode(location);
      NetworkRoot directRoot = extractRoot(self);
      if (directRoot != null) {
         return directRoot;
      } else {
         Location key = location.clone();
         NetworkUtil.CacheEntry cached = CACHE.get(key);
         if (cached != null && System.currentTimeMillis() - cached.cachedAt() <= 5000L) {
            return cached.root();
         } else {
            NetworkRoot scanned = scanNeighbors(location);
            if (scanned != null) {
               CACHE.put(key, new NetworkUtil.CacheEntry(scanned, System.currentTimeMillis()));
            } else {
               CACHE.remove(key);
            }

            return scanned;
         }
      }
   }

   public static void invalidate(@Nullable Location location) {
      if (location != null) {
         CACHE.remove(location);
      }
   }

   @Nullable
   private static NetworkRoot scanNeighbors(@NotNull Location location) {
      for (BlockFace face : BlockFace.values()) {
         if (face.isCartesian()) {
            Location neighbor = location.clone().add(face.getDirection());
            NodeDefinition definition = NetworkStorage.getNode(neighbor);
            NetworkRoot root = extractRoot(definition);
            if (root != null) {
               return root;
            }
         }
      }

      return null;
   }

   @Nullable
   private static NetworkRoot extractRoot(@Nullable NodeDefinition definition) {
      if (definition != null && definition.getNode() != null) {
         NetworkRoot root = definition.getNode().getRoot();
         return root != null ? root : null;
      } else {
         return null;
      }
   }

   private static record CacheEntry(@NotNull NetworkRoot root, long cachedAt) {
   }
}
