package com.ytdd9527.networksexpansion.implementation.machines.cellnet.drive;

import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.CellHandle;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.ItemHashMap;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.ItemKey;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DriveCache {
   public static final long STORAGE_CACHE_INTERVAL_MS = 200L;
   private final Set<ItemKey> notIncluded = ConcurrentHashMap.newKeySet();
   private final Map<ItemKey, UUID> pushCache = new ConcurrentHashMap<>();
   private final Map<ItemKey, UUID> takeCache = new ConcurrentHashMap<>();
   private volatile ItemHashMap<Long> cachedStorage = null;
   private volatile Map<ItemKey, List<UUID>> itemToStorageIndex = null;
   private volatile long lastCacheTime = 0L;
   private volatile DriveStorage.FlatView inputFlatView = null;
   private volatile DriveStorage.FlatView outputFlatView = null;

   public Set<ItemKey> getNotIncluded() {
      return this.notIncluded;
   }

   public Map<ItemKey, UUID> getPushCache() {
      return this.pushCache;
   }

   public Map<ItemKey, UUID> getTakeCache() {
      return this.takeCache;
   }

   @Nullable
   public Map<ItemKey, List<UUID>> getItemToStorageIndex() {
      return this.itemToStorageIndex;
   }

   @Nullable
   public DriveStorage.FlatView getFlatView(@NotNull DriveStorage.DriveViewRole role) {
      return role == DriveStorage.DriveViewRole.INPUT ? this.inputFlatView : this.outputFlatView;
   }

   public void setFlatView(@NotNull DriveStorage.DriveViewRole role, @NotNull DriveStorage.FlatView flatView) {
      if (role == DriveStorage.DriveViewRole.INPUT) {
         this.inputFlatView = flatView;
      } else {
         this.outputFlatView = flatView;
      }
   }

   public synchronized ItemHashMap<Long> getStorage(@NotNull List<CellHandle> cells) {
      ItemHashMap<Long> cached = this.cachedStorage;
      if (cached != null && System.currentTimeMillis() - this.lastCacheTime < 200L) {
         return cached;
      } else {
         ItemHashMap<Long> result = new ItemHashMap<>();
         Map<ItemKey, List<UUID>> newIndex = new ConcurrentHashMap<>();

         for (CellHandle cell : cells) {
            cell.accumulateInto(result, newIndex);
         }

         this.itemToStorageIndex = newIndex;
         this.cachedStorage = result;
         this.lastCacheTime = System.currentTimeMillis();
         return result;
      }
   }

   public synchronized void adjust(@NotNull ItemKey key, long delta) {
      ItemHashMap<Long> cached = this.cachedStorage;
      if (cached != null) {
         Long current = cached.getKey(key);
         long newValue = (current != null ? current : 0L) + delta;
         if (newValue <= 0L) {
            cached.removeKey(key);
         } else {
            cached.putKey(key, newValue);
         }
      }
   }

   public synchronized void clearItemCaches() {
      this.cachedStorage = null;
      this.itemToStorageIndex = null;
      this.lastCacheTime = 0L;
      this.notIncluded.clear();
   }
}
