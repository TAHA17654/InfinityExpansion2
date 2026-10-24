package com.ytdd9527.networksexpansion.implementation.machines.cellnet.drive;

import com.balugaq.netex.utils.Debug;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.api.DriveType;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.CellHandle;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.VoidCellHandle;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.VoidCellSupport;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.ledger.CellLedger;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.rule.CellAcceptRules;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.drive.ender.EnderChannelController;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.ItemHashMap;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.ItemKey;
import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.core.attributes.DistinctiveItem;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Generated;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DriveStorage {
   private static final Map<Material, ItemKey> PLAIN_KEY_CACHE = new ConcurrentHashMap<>();
   private static final Map<SlimefunItem, ItemKey> SLIMEFUN_KEY_CACHE = new ConcurrentHashMap<>();
   private static final Map<ItemStack, ItemKey> REQUEST_KEY_CACHE = Collections.synchronizedMap(new IdentityHashMap<>());
   private static final int REQUEST_KEY_CACHE_LIMIT = 512;
   private static final Object INDEX_LOCK = new Object();
   private final Map<Location, DriveStorage.CellCacheEntry> cellCache = new ConcurrentHashMap<>();
   private final AtomicLong generationCounter = new AtomicLong();

   @NotNull
   private static ItemKey getKey(@NotNull ItemStack itemStack) {
      if (itemStack.hasItemMeta()) {
         SlimefunItem sfItem = SlimefunItem.getByItem(itemStack);
         return sfItem != null && !(sfItem instanceof DistinctiveItem)
            ? SLIMEFUN_KEY_CACHE.computeIfAbsent(sfItem, s -> new ItemKey(itemStack))
            : new ItemKey(itemStack);
      } else {
         return PLAIN_KEY_CACHE.computeIfAbsent(itemStack.getType(), m -> new ItemKey(new ItemStack(m)));
      }
   }

   @NotNull
   private static ItemKey cachedKey(@NotNull ItemStack itemStack) {
      ItemKey cached = REQUEST_KEY_CACHE.get(itemStack);
      if (cached != null) {
         return cached;
      } else {
         ItemKey key = getKey(itemStack);
         if (REQUEST_KEY_CACHE.size() >= 512) {
            REQUEST_KEY_CACHE.clear();
         }

         REQUEST_KEY_CACHE.put(itemStack, key);
         return key;
      }
   }

   @NotNull
   public List<CellHandle> getCells(@NotNull BlockMenu menu) {
      Location location = menu.getLocation();
      SlimefunItem sfItem = StorageCacheUtils.getSfItem(location);
      boolean isEnderDrive = DriveType.of(sfItem) == DriveType.ENDER;
      if (!isEnderDrive) {
         DriveStorage.CellCacheEntry entry = this.cellCache.get(location);
         if (entry != null && entry.handles != null) {
            return entry.handles;
         }
      }

      List<CellHandle> cells = isEnderDrive ? collectWithRemoteMembers(menu) : DriveCellSlots.collectCells(menu);
      if (!isEnderDrive) {
         this.cellCache.computeIfAbsent(location, k -> new DriveStorage.CellCacheEntry()).handles = cells;
         CellUniqueness.registerDrive(menu);
      }

      return cells;
   }

   @NotNull
   private static List<CellHandle> collectWithRemoteMembers(@NotNull BlockMenu menu) {
      Set<UUID> seen = new HashSet<>();
      List<CellHandle> cells = new ArrayList<>();

      for (CellHandle local : DriveCellSlots.collectCells(menu)) {
         if (seen.add(local.getUuid())) {
            cells.add(local);
         }
      }

      String channel = EnderDrive.getChannel(menu.getLocation());
      if (channel == null) {
         return cells;
      } else {
         for (String uuidString : EnderChannelController.getInstance().getCellUuidsOfChannel(channel)) {
            UUID remoteUuid;
            try {
               remoteUuid = UUID.fromString(uuidString);
            } catch (IllegalArgumentException var8) {
               Debug.debug("Invalid ender channel member UUID, skipped: " + uuidString);
               continue;
            }

            if (seen.add(remoteUuid)) {
               CellLedger remoteCache = CellLedger.getActiveCaches().get(remoteUuid);
               if (remoteCache != null && !remoteCache.isVoidCell()) {
                  cells.add(CellHandle.wrap(remoteCache));
               }
            }
         }

         return cells;
      }
   }

   public void invalidateCellCache(@NotNull Location location) {
      DriveStorage.CellCacheEntry entry = this.cellCache.computeIfAbsent(location, k -> new DriveStorage.CellCacheEntry());
      entry.handles = null;
      entry.generation = this.generationCounter.incrementAndGet();
   }

   public void dropCellCache(@NotNull Location location) {
      this.cellCache.remove(location);
   }

   @NotNull
   private DriveStorage.FlatView getFlatView(@NotNull DriveCache cache, @NotNull Collection<BlockMenu> menus, @NotNull DriveStorage.DriveViewRole role) {
      DriveStorage.FlatView cached = cache.getFlatView(role);
      return this.isFlatViewCurrent(cached, menus) ? cached : this.rebuildFlatView(cache, menus, role);
   }

   private boolean isFlatViewCurrent(@Nullable DriveStorage.FlatView cached, @NotNull Collection<BlockMenu> menus) {
      if (cached != null && cached.getLocations().size() == menus.size()) {
         if (cached.getLastMenus() == menus) {
            return true;
         } else {
            long fingerprint = 0L;

            for (BlockMenu menu : menus) {
               Location location = menu.getLocation();
               if (!cached.getLocations().contains(location)) {
                  return false;
               }

               DriveStorage.CellCacheEntry cacheEntry = this.cellCache.get(location);
               long gen = cacheEntry != null ? cacheEntry.generation : 0L;
               fingerprint += gen;
            }

            if (fingerprint == cached.getGenerationFingerprint()) {
               cached.setLastMenus(menus);
               return true;
            } else {
               return false;
            }
         }
      } else {
         return false;
      }
   }

   @NotNull
   private DriveStorage.FlatView rebuildFlatView(@NotNull DriveCache cache, @NotNull Collection<BlockMenu> menus, @NotNull DriveStorage.DriveViewRole role) {
      Set<Location> driveLocations = new HashSet<>();

      for (BlockMenu menu : menus) {
         driveLocations.add(menu.getLocation());
      }

      long fingerprint = 0L;

      for (Location location : driveLocations) {
         DriveStorage.CellCacheEntry cacheEntry = this.cellCache.get(location);
         long gen = cacheEntry != null ? cacheEntry.generation : 0L;
         fingerprint += gen;
      }

      List<CellHandle> cells = new ArrayList<>();
      Map<UUID, CellHandle> byUuid = new HashMap<>();
      Set<UUID> seenUuids = new HashSet<>();

      for (BlockMenu menu : menus) {
         for (CellHandle cell : this.getCells(menu)) {
            if (seenUuids.add(cell.getUuid())) {
               cells.add(cell);
               byUuid.put(cell.getUuid(), cell);
            }
         }
      }

      cache.clearItemCaches();
      DriveStorage.FlatView view = new DriveStorage.FlatView(driveLocations, cells, byUuid, fingerprint, hasVoidSink(cells));
      cache.setFlatView(role, view);
      return view;
   }

   public long pushSingle(@NotNull DriveCache cache, @NotNull Collection<BlockMenu> menus, @NotNull ItemStack template, long amount) {
      if (amount <= 0L) {
         return amount;
      } else {
         DriveStorage.FlatView view = this.getFlatView(cache, menus, DriveStorage.DriveViewRole.INPUT);
         List<CellHandle> cells = view.cells;
         if (cells.isEmpty()) {
            return amount;
         } else {
            boolean voidActive = view.hasVoidSink && !VoidCellSupport.isSuspended();
            return pushSingleEntry(cells, voidActive, cache, cache.getItemToStorageIndex(), template, amount);
         }
      }
   }

   public void pushMany(@NotNull DriveCache cache, @NotNull Collection<BlockMenu> menus, @NotNull List<ItemStack> items) {
      if (!items.isEmpty()) {
         DriveStorage.FlatView view = this.getFlatView(cache, menus, DriveStorage.DriveViewRole.INPUT);
         List<CellHandle> cells = view.cells;
         if (!cells.isEmpty()) {
            boolean voidActive = view.hasVoidSink && !VoidCellSupport.isSuspended();
            Map<ItemKey, List<UUID>> index = cache.getItemToStorageIndex();

            for (ItemStack item : items) {
               int amount = item.getAmount();
               if (amount > 0) {
                  long remaining = pushSingleEntry(cells, voidActive, cache, index, item, (long)amount);
                  item.setAmount((int)Math.min(remaining, 2147483647L));
               }
            }
         }
      }
   }

   private static long pushSingleEntry(
      @NotNull List<CellHandle> cells,
      boolean voidActive,
      @NotNull DriveCache cache,
      @Nullable Map<ItemKey, List<UUID>> index,
      @NotNull ItemStack template,
      long amount
   ) {
      long remaining = amount;
      if (amount <= 0L) {
         return amount;
      } else if (template.getType().isAir()) {
         return amount;
      } else {
         ItemKey itemKey = getKey(template);
         if (!itemKey.hasMeta() || !CellAcceptRules.isUsedCell(template) && !CellAcceptRules.isFilledContainer(template) && !itemKey.isOversized()) {
            cache.getNotIncluded().remove(itemKey);
            long pushedTotal = 0L;
            UUID lastProvider = null;

            for (CellHandle cell : cells) {
               if (remaining <= 0L) {
                  break;
               }

               if (cell.canReceiveItem(itemKey)) {
                  int toPush = (int)Math.min(remaining, 2147483647L);
                  int pushed = cell.pushItem(itemKey, toPush);
                  if (pushed > 0) {
                     remaining -= (long)pushed;
                     pushedTotal += (long)pushed;
                     lastProvider = cell.getUuid();
                     indexAdd(index, itemKey, cell.getUuid());
                  }
               }
            }

            if (lastProvider != null) {
               cache.getPushCache().put(itemKey, lastProvider);
               cache.adjust(itemKey, pushedTotal);
            }

            if (voidActive && remaining > 0L) {
               long destroyed = destroyViaVoidSinks(cells, itemKey, remaining);
               if (destroyed > 0L) {
                  remaining -= destroyed;
                  cache.adjust(itemKey, -destroyed);
               }
            }

            return remaining;
         } else {
            return amount;
         }
      }
   }

   private static long destroyViaVoidSinks(@NotNull List<CellHandle> cells, @NotNull ItemKey itemKey, long amount) {
      for (CellHandle cell : cells) {
         if (cell instanceof VoidCellHandle voidSink && voidSink.acceptsForDestroy(itemKey.getItemStack())) {
            return amount;
         }
      }

      return 0L;
   }

   @Nullable
   public ItemStack takeItem(@NotNull DriveCache cache, @NotNull Collection<BlockMenu> menus, @NotNull ItemRequest request) {
      ItemStack requested = request.getItemStack();
      if (requested == null) {
         return null;
      } else {
         int requestedAmount = request.getAmount();
         return requestedAmount <= 0 ? null : this.takeByKey(cache, menus, cachedKey(requested), requestedAmount, DriveStorage.DriveViewRole.OUTPUT);
      }
   }

   @Nullable
   public ItemStack takeItemDirect(@Nullable DriveCache cache, @NotNull Collection<BlockMenu> menus, @NotNull ItemKey itemKey, int amount) {
      return cache != null && amount > 0 ? this.takeByKey(cache, menus, itemKey, amount, DriveStorage.DriveViewRole.INPUT) : null;
   }

   public long takeItemDirectAmount(@Nullable DriveCache cache, @NotNull Collection<BlockMenu> menus, @NotNull ItemKey itemKey, long amount) {
      if (cache != null && amount > 0L) {
         if (cache.getNotIncluded().contains(itemKey)) {
            return 0L;
         } else {
            DriveStorage.FlatView view = this.getFlatView(cache, menus, DriveStorage.DriveViewRole.INPUT);
            List<CellHandle> cells = view.cells;
            if (cells.isEmpty()) {
               cache.getNotIncluded().add(itemKey);
               return 0L;
            } else {
               long taken = 0L;
               CellHandle lastProvider = null;
               CellHandle first = resolveSingleCandidate(cache, view, itemKey);
               if (first != null) {
                  long got = first.takeItemAmount(itemKey, amount);
                  if (got > 0L) {
                     taken += got;
                     lastProvider = first;
                  }
               }

               for (CellHandle cell : cells) {
                  if (taken >= amount) {
                     break;
                  }

                  if (cell != first) {
                     long got = cell.takeItemAmount(itemKey, amount - taken);
                     if (got > 0L) {
                        taken += got;
                        lastProvider = cell;
                     }
                  }
               }

               if (lastProvider != null) {
                  cache.getTakeCache().put(itemKey, lastProvider.getUuid());
                  cache.adjust(itemKey, taken);
                  cache.getNotIncluded().remove(itemKey);
               } else {
                  cache.getNotIncluded().add(itemKey);
               }

               return taken;
            }
         }
      } else {
         return 0L;
      }
   }

   @Nullable
   private ItemStack takeByKey(
      @NotNull DriveCache cache, @NotNull Collection<BlockMenu> menus, @NotNull ItemKey itemKey, int requestedAmount, @NotNull DriveStorage.DriveViewRole role
   ) {
      if (cache.getNotIncluded().contains(itemKey)) {
         return null;
      } else {
         DriveStorage.FlatView view = this.getFlatView(cache, menus, role);
         List<CellHandle> cells = view.cells;
         if (cells.isEmpty()) {
            cache.getNotIncluded().add(itemKey);
            return null;
         } else {
            DriveStorage.TakeProgress progress = new DriveStorage.TakeProgress((long)requestedAmount);
            Map<ItemKey, List<UUID>> index = cache.getItemToStorageIndex();
            CellHandle singleCandidate = resolveSingleCandidate(cache, view, itemKey);
            if (singleCandidate != null) {
               takeFromCell(singleCandidate, itemKey, progress, index);
               if (progress.remaining > 0L) {
                  takeFromCells(cells, itemKey, progress, index, Set.of(singleCandidate), true);
               }
            } else {
               Set<CellHandle> candidates = collectCandidateCells(cache, view, itemKey);
               takeFromCells(candidates, itemKey, progress, index, null, false);
               if (progress.remaining > 0L) {
                  takeFromCells(cells, itemKey, progress, index, candidates, true);
               }
            }

            if (progress.lastProvider != null) {
               cache.getTakeCache().put(itemKey, progress.lastProvider.getUuid());
               cache.adjust(itemKey, (long)requestedAmount - progress.remaining);
               cache.getNotIncluded().remove(itemKey);
            } else {
               cache.getNotIncluded().add(itemKey);
            }

            return progress.result;
         }
      }
   }

   @Nullable
   private static CellHandle resolveSingleCandidate(@NotNull DriveCache cache, @NotNull DriveStorage.FlatView view, @NotNull ItemKey itemKey) {
      UUID cachedUuid = cache.getTakeCache().get(itemKey);
      if (cachedUuid == null) {
         return null;
      } else {
         CellHandle cell = view.byUuid.get(cachedUuid);
         if (cell == null) {
            return null;
         } else {
            Map<ItemKey, List<UUID>> index = cache.getItemToStorageIndex();
            if (index != null) {
               List<UUID> uuids = index.get(itemKey);
               if (uuids != null) {
                  for (UUID uuid : uuids) {
                     if (!uuid.equals(cachedUuid)) {
                        return null;
                     }
                  }
               }
            }

            return cell;
         }
      }
   }

   private static void takeFromCell(
      @NotNull CellHandle cell, @NotNull ItemKey itemKey, @NotNull DriveStorage.TakeProgress progress, @Nullable Map<ItemKey, List<UUID>> index
   ) {
      ItemStack taken = cell.takeItem(itemKey, progress.remaining);
      if (taken != null) {
         if (progress.result == null) {
            progress.result = taken;
         } else {
            progress.result.setAmount(progress.result.getAmount() + taken.getAmount());
         }

         progress.remaining = progress.remaining - (long)taken.getAmount();
         progress.lastProvider = cell;
         indexAdd(index, itemKey, cell.getUuid());
      }
   }

   @NotNull
   private static Set<CellHandle> collectCandidateCells(@NotNull DriveCache cache, @NotNull DriveStorage.FlatView view, @NotNull ItemKey itemKey) {
      Map<UUID, CellHandle> byUuid = view.byUuid;
      Set<CellHandle> candidates = new LinkedHashSet<>();
      UUID cachedUuid = cache.getTakeCache().get(itemKey);
      if (cachedUuid != null) {
         CellHandle cell = byUuid.get(cachedUuid);
         if (cell != null && cell.contains(itemKey)) {
            candidates.add(cell);
         }
      }

      Map<ItemKey, List<UUID>> index = cache.getItemToStorageIndex();
      if (index != null) {
         List<UUID> uuids = index.get(itemKey);
         if (uuids != null) {
            for (UUID uuid : uuids) {
               CellHandle cell = byUuid.get(uuid);
               if (cell != null && cell.contains(itemKey)) {
                  candidates.add(cell);
               }
            }
         }
      }

      return candidates;
   }

   private static void takeFromCells(
      @NotNull Iterable<CellHandle> cells,
      @NotNull ItemKey itemKey,
      @NotNull DriveStorage.TakeProgress progress,
      @Nullable Map<ItemKey, List<UUID>> index,
      @Nullable Set<CellHandle> skip,
      boolean checkContains
   ) {
      for (CellHandle cell : cells) {
         if (progress.remaining <= 0L) {
            return;
         }

         if ((skip == null || !skip.contains(cell)) && (!checkContains || cell.contains(itemKey))) {
            ItemStack taken = cell.takeItem(itemKey, progress.remaining);
            if (taken != null) {
               if (progress.result == null) {
                  progress.result = taken;
               } else {
                  progress.result.setAmount(progress.result.getAmount() + taken.getAmount());
               }

               progress.remaining = progress.remaining - (long)taken.getAmount();
               progress.lastProvider = cell;
               indexAdd(index, itemKey, cell.getUuid());
            }
         }
      }
   }

   @NotNull
   public DriveStorage.CapacityProbe probeReceive(@NotNull DriveCache cache, @NotNull Collection<BlockMenu> menus, @NotNull ItemKey key, long amount) {
      DriveStorage.FlatView view = this.getFlatView(cache, menus, DriveStorage.DriveViewRole.INPUT);
      long total = 0L;

      for (CellHandle cell : view.cells) {
         if (total >= amount) {
            break;
         }

         if (!(cell instanceof VoidCellHandle)) {
            total += cell.receiveCapacity(key);
         }
      }

      if (total < amount && view.hasVoidSink) {
         ItemStack template = key.getItemStack();

         for (CellHandle cell : view.cells) {
            if (cell instanceof VoidCellHandle voidSink && voidSink.acceptsForDestroy(template)) {
               return new DriveStorage.CapacityProbe(total, true);
            }
         }

         return new DriveStorage.CapacityProbe(total, false);
      } else {
         return new DriveStorage.CapacityProbe(total, false);
      }
   }

   private static void indexAdd(@Nullable Map<ItemKey, List<UUID>> index, @NotNull ItemKey key, @NotNull UUID uuid) {
      if (index != null) {
         synchronized (INDEX_LOCK) {
            List<UUID> uuids = index.computeIfAbsent(key, k -> new ArrayList<>());
            if (!uuids.contains(uuid)) {
               uuids.add(uuid);
            }
         }
      }
   }

   @NotNull
   public ItemHashMap<Long> getAllCellItemsKeyed(@NotNull DriveCache cache, @NotNull Collection<BlockMenu> menus) {
      DriveStorage.FlatView view = this.getFlatView(cache, menus, DriveStorage.DriveViewRole.OUTPUT);
      return cache.getStorage(view.cells);
   }

   @NotNull
   public Map<ItemStack, Long> getAllCellItems(@NotNull List<CellHandle> cells) {
      ItemHashMap<Long> snapshot = this.getAllCellItemsKeyed(cells);
      Map<ItemStack, Long> result = new HashMap<>();

      for (Entry<ItemKey, Long> entry : snapshot.keyEntrySet()) {
         result.put(entry.getKey().getItemStack(), entry.getValue());
      }

      return result;
   }

   @NotNull
   public ItemHashMap<Long> getAllCellItemsKeyed(@NotNull List<CellHandle> cells) {
      ItemHashMap<Long> snapshot = new ItemHashMap<>();

      for (CellHandle cell : cells) {
         cell.accumulateInto(snapshot, null);
      }

      return snapshot;
   }

   public int getAmount(@NotNull DriveCache cache, @NotNull Collection<BlockMenu> menus, @NotNull ItemStack itemStack) {
      ItemKey itemKey = cachedKey(itemStack);
      if (cache.getNotIncluded().contains(itemKey)) {
         return 0;
      } else {
         DriveStorage.FlatView view = this.getFlatView(cache, menus, DriveStorage.DriveViewRole.OUTPUT);
         long total = 0L;

         for (CellHandle cell : view.cells) {
            total += cell.getAmount(itemKey);
         }

         return total > 2147483647L ? Integer.MAX_VALUE : (int)total;
      }
   }

   private static boolean hasVoidSink(@NotNull List<CellHandle> cells) {
      for (CellHandle cell : cells) {
         if (cell instanceof VoidCellHandle) {
            return true;
         }
      }

      return false;
   }

   public static record CapacityProbe(long capacity, boolean voidAccepts) {
   }

   private static final class CellCacheEntry {
      List<CellHandle> handles;
      long generation;
   }

   public static enum DriveViewRole {
      INPUT,
      OUTPUT;
   }

   public static final class FlatView {
      private final Set<Location> locations;
      private final List<CellHandle> cells;
      private final Map<UUID, CellHandle> byUuid;
      private final long generationFingerprint;
      private final boolean hasVoidSink;
      private volatile Collection<BlockMenu> lastMenus;

      @Generated
      public Set<Location> getLocations() {
         return this.locations;
      }

      @Generated
      public List<CellHandle> getCells() {
         return this.cells;
      }

      @Generated
      public Map<UUID, CellHandle> getByUuid() {
         return this.byUuid;
      }

      @Generated
      public long getGenerationFingerprint() {
         return this.generationFingerprint;
      }

      @Generated
      public boolean isHasVoidSink() {
         return this.hasVoidSink;
      }

      @Generated
      public Collection<BlockMenu> getLastMenus() {
         return this.lastMenus;
      }

      @Generated
      public FlatView(Set<Location> locations, List<CellHandle> cells, Map<UUID, CellHandle> byUuid, long generationFingerprint, boolean hasVoidSink) {
         this.locations = locations;
         this.cells = cells;
         this.byUuid = byUuid;
         this.generationFingerprint = generationFingerprint;
         this.hasVoidSink = hasVoidSink;
      }

      @Generated
      public void setLastMenus(Collection<BlockMenu> lastMenus) {
         this.lastMenus = lastMenus;
      }
   }

   private static final class TakeProgress {
      @Nullable
      ItemStack result;
      long remaining;
      @Nullable
      CellHandle lastProvider;

      TakeProgress(long requestedAmount) {
         this.remaining = requestedAmount;
      }
   }
}
