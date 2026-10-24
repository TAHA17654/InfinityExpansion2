package io.github.sefiraat.networks.network;

import com.balugaq.netex.api.data.ItemContainer;
import com.balugaq.netex.api.data.ItemFlowRecord;
import com.balugaq.netex.api.data.StorageUnitData;
import com.balugaq.netex.api.enums.AccessMode;
import com.balugaq.netex.api.enums.FeedbackType;
import com.balugaq.netex.api.enums.StorageType;
import com.balugaq.netex.api.events.NetworkRootLocateStorageEvent;
import com.balugaq.netex.api.interfaces.FeedbackSendable;
import com.balugaq.netex.utils.BlockMenuUtil;
import com.balugaq.netex.utils.NetworksVersionedParticle;
import com.balugaq.netex.utils.RootWriteLock;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.drive.CellDrive;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.drive.DriveCache;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.ItemHashMap;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.ItemKey;
import com.ytdd9527.networksexpansion.implementation.machines.networks.advanced.AdvancedGreedyBlock;
import com.ytdd9527.networksexpansion.implementation.machines.unit.NetworksDrawer;
import io.github.mooy1.infinityexpansion.items.storage.StorageCache;
import io.github.mooy1.infinityexpansion.items.storage.StorageUnit;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.network.barrel.FluffyBarrel;
import io.github.sefiraat.networks.network.barrel.InfinityBarrel;
import io.github.sefiraat.networks.network.barrel.NetworkStorage;
import io.github.sefiraat.networks.network.barrel.TsingshanBarrel;
import io.github.sefiraat.networks.network.stackcaches.BarrelIdentity;
import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import io.github.sefiraat.networks.network.stackcaches.QuantumCache;
import io.github.sefiraat.networks.slimefun.network.NetworkCell;
import io.github.sefiraat.networks.slimefun.network.NetworkDirectional;
import io.github.sefiraat.networks.slimefun.network.NetworkGreedyBlock;
import io.github.sefiraat.networks.slimefun.network.NetworkPowerNode;
import io.github.sefiraat.networks.slimefun.network.NetworkQuantumStorage;
import io.github.sefiraat.networks.utils.MatchOption;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.ncbpfluffybear.fluffymachines.items.Barrel;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.Generated;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class NetworkRoot extends NetworkNode {
   public static final int persistentThreshold = Networks.getConfigManager().getPersistentThreshold();
   public static final int cacheMissThreshold = Networks.getConfigManager().getCacheMissThreshold();
   public static final int reduceMs = Networks.getConfigManager().getReduceMs();
   public static final int transportMissThreshold = Networks.getConfigManager().getTransportMissThreshold();
   private static final Map<Location, Map<Location, Integer>> observingAccessHistory = new ConcurrentHashMap<>();
   private static final Map<Location, Map<Location, Integer>> persistentAccessHistory = new ConcurrentHashMap<>();
   private static final Map<Location, Integer> transportMissInputHistory = new ConcurrentHashMap<>();
   private static final Map<Location, Integer> transportMissOutputHistory = new ConcurrentHashMap<>();
   private static final Map<Location, Long> controlledAccessInputHistory = new ConcurrentHashMap<>();
   private static final Map<Location, Long> controlledAccessOutputHistory = new ConcurrentHashMap<>();
   private final Set<Location> nodeLocations = ConcurrentHashMap.newKeySet();
   public static final int[] CELL_AVAILABLE_SLOTS = NetworkCell.SLOTS.stream().mapToInt(i -> i).toArray();
   public static final int[] ADVANCED_GREEDY_BLOCK_AVAILABLE_SLOTS = AdvancedGreedyBlock.INPUT_SLOTS;
   public static final int GREEDY_BLOCK_AVAILABLE_SLOT = 16;
   private final int maxNodes;
   private final boolean recordFlow;
   @Nullable
   private final ItemFlowRecord itemFlowRecord;
   private final Location controller;
   private boolean isOverburdened = false;
   @Nullable
   private volatile Set<BarrelIdentity> barrels = null;
   @Nullable
   private volatile Set<BarrelIdentity> inputAbleBarrels = null;
   @Nullable
   private volatile Set<BarrelIdentity> outputAbleBarrels = null;
   @Nullable
   private volatile Map<Location, StorageUnitData> drawerData = null;
   @Nullable
   private volatile Map<Location, StorageUnitData> inputAbleDrawerData = null;
   @Nullable
   private volatile Map<Location, StorageUnitData> outputAbleDrawerData = null;
   @Nullable
   private volatile Map<Location, BarrelIdentity> mapInputAbleBarrels = null;
   @Nullable
   private volatile Map<Location, BarrelIdentity> mapOutputAbleBarrels = null;
   @Nullable
   private volatile Set<BlockMenu> cellDriveMenus = null;
   @Nullable
   private volatile Set<BlockMenu> inputAbleCellDriveMenus = null;
   @Nullable
   private volatile Set<BlockMenu> outputAbleCellDriveMenus = null;
   private final DriveCache driveCache = new DriveCache();
   private static final Map<Location, NetworkRoot.ItemSnapshot> SHARED_ITEM_SNAPSHOTS = new ConcurrentHashMap<>();
   private static final Map<Location, AtomicLong> INVALIDATION_EPOCHS = new ConcurrentHashMap<>();
   private static final Map<Location, AtomicLong> WRITE_EPOCHS = new ConcurrentHashMap<>();
   private static final long SNAPSHOT_MAX_AGE_MS = 500L;
   private static final long SNAPSHOT_IDLE_MS = 3600000L;
   private final EnumMap<NodeType, NetworkRoot.NodeIndex> indexes = new EnumMap<>(NodeType.class);
   private final AtomicLong rootPower = new AtomicLong(0L);
   private boolean displayParticles = false;

   public NetworkRoot(Location location, int maxNodes, boolean recordFlow, @Nullable ItemFlowRecord itemFlowRecord) {
      super(location, NodeType.CONTROLLER);
      this.maxNodes = maxNodes;
      this.controller = location;
      this.root = this;
      this.recordFlow = recordFlow;
      this.itemFlowRecord = itemFlowRecord;

      for (NodeType type : NodeType.values()) {
         this.indexes.put(type, new NetworkRoot.NodeIndex());
      }

      this.registerNode(location, NodeType.CONTROLLER);
   }

   public static void addPersistentAccessHistory(Location location, Location accessLocation) {
      Map<Location, Integer> locations = persistentAccessHistory.getOrDefault(location, new ConcurrentHashMap<>());
      locations.put(accessLocation, 0);
      persistentAccessHistory.put(location, locations);
   }

   public static void addCacheMiss(Location location, Location accessLocation) {
      Map<Location, Integer> locations = persistentAccessHistory.getOrDefault(location, new ConcurrentHashMap<>());
      int value = locations.getOrDefault(accessLocation, 0) + 1;
      if (value > cacheMissThreshold) {
         removePersistentAccessHistory(location, accessLocation);
      } else {
         locations.put(accessLocation, value);
         persistentAccessHistory.put(location, locations);
      }
   }

   public static void minusCacheMiss(Location location, Location accessLocation) {
      Map<Location, Integer> locations = persistentAccessHistory.getOrDefault(location, new ConcurrentHashMap<>());
      int value = Math.max(locations.getOrDefault(accessLocation, 0) - 1, 0);
      locations.put(accessLocation, value);
   }

   @Contract("null -> null")
   @Nullable
   public static Map<Location, Integer> getPersistentAccessHistory(@Nullable Location location) {
      return location == null ? null : persistentAccessHistory.get(location);
   }

   public static void removePersistentAccessHistory(Location location) {
      persistentAccessHistory.remove(location);
   }

   public static void removePersistentAccessHistory(Location location, Location accessLocation) {
      Map<Location, Integer> locations = persistentAccessHistory.getOrDefault(location, new ConcurrentHashMap<>());
      locations.remove(accessLocation);
      persistentAccessHistory.put(location, locations);
   }

   public static void addCountObservingAccessHistory(@Nullable Location accessor, Location accessLocation) {
      if (accessor != null) {
         Map<Location, Integer> locations = observingAccessHistory.getOrDefault(accessor, new ConcurrentHashMap<>());
         int count = locations.getOrDefault(accessLocation, 0);
         if (count >= persistentThreshold) {
            removeCountObservingAccessHistory(accessor, accessLocation);
            addPersistentAccessHistory(accessor, accessLocation);
         } else {
            locations.put(accessLocation, count + 1);
            observingAccessHistory.put(accessor, locations);
         }
      }
   }

   public static Map<Location, Integer> getCountObservingAccessHistory(Location location) {
      return observingAccessHistory.getOrDefault(location, new ConcurrentHashMap<>());
   }

   public static void removeCountObservingAccessHistory(Location location) {
      observingAccessHistory.remove(location);
   }

   public static void removeCountObservingAccessHistory(Location location, Location accessLocation) {
      Map<Location, Integer> locations = observingAccessHistory.getOrDefault(location, new ConcurrentHashMap<>());
      locations.remove(accessLocation);
      observingAccessHistory.put(location, locations);
   }

   @Nullable
   public static InfinityBarrel getInfinityBarrel(BlockMenu blockMenu, StorageUnit storageUnit) {
      return getInfinityBarrel(blockMenu, storageUnit, false);
   }

   @Nullable
   public static InfinityBarrel getInfinityBarrel(BlockMenu blockMenu, StorageUnit storageUnit, boolean includeEmpty) {
      ItemStack itemStack = blockMenu.getItemInSlot(16);
      String storedString = StorageCacheUtils.getData(blockMenu.getLocation(), "stored");
      if (storedString == null) {
         return null;
      } else {
         int storedInt = Integer.parseInt(storedString);
         if (includeEmpty || itemStack != null && itemStack.getType() != Material.AIR) {
            StorageCache cache = storageUnit.getCache(blockMenu.getLocation());
            if (cache == null) {
               return null;
            } else {
               ItemStack clone = null;
               if (itemStack != null) {
                  clone = itemStack.clone();
                  clone.setAmount(1);
               }

               return new InfinityBarrel(blockMenu.getLocation(), clone, (long)(storedInt + (itemStack == null ? 0 : itemStack.getAmount())), cache);
            }
         } else {
            return null;
         }
      }
   }

   @Nullable
   public static TsingshanBarrel getTsingshanBarrel(BlockMenu blockMenu, io.gitee.crankygoudan.tsingshantechnology.items.chucun.StorageUnit storageUnit) {
      return getTsingshanBarrel(blockMenu, storageUnit, false);
   }

   @Nullable
   public static TsingshanBarrel getTsingshanBarrel(
      BlockMenu blockMenu, io.gitee.crankygoudan.tsingshantechnology.items.chucun.StorageUnit storageUnit, boolean includeEmpty
   ) {
      ItemStack itemStack = blockMenu.getItemInSlot(16);
      String storedString = StorageCacheUtils.getData(blockMenu.getLocation(), "sstored");
      if (storedString == null) {
         return null;
      } else {
         int storedInt;
         try {
            storedInt = Integer.parseInt(storedString);
         } catch (NumberFormatException var8) {
            return null;
         }

         if (includeEmpty || itemStack != null && itemStack.getType() != Material.AIR) {
            io.gitee.crankygoudan.tsingshantechnology.items.chucun.StorageCache cache = storageUnit.getCache(blockMenu.getLocation());
            if (cache == null) {
               return null;
            } else {
               ItemStack clone = null;
               if (itemStack != null) {
                  clone = itemStack.clone();
                  clone.setAmount(1);
               }

               return new TsingshanBarrel(blockMenu.getLocation(), clone, (long)(storedInt + (itemStack == null ? 0 : itemStack.getAmount())), cache);
            }
         } else {
            return null;
         }
      }
   }

   @Nullable
   public static FluffyBarrel getFluffyBarrel(BlockMenu blockMenu, Barrel barrel) {
      return getFluffyBarrel(blockMenu, barrel, false);
   }

   @Nullable
   public static FluffyBarrel getFluffyBarrel(BlockMenu blockMenu, Barrel barrel, boolean includeEmpty) {
      Block block = blockMenu.getBlock();

      ItemStack itemStack;
      try {
         itemStack = barrel.getStoredItem(block);
      } catch (NullPointerException var9) {
         return null;
      }

      if (includeEmpty || itemStack != null && itemStack.getType() != Material.AIR) {
         ItemStack clone = null;
         if (itemStack != null) {
            clone = itemStack.clone();
            clone.setAmount(1);
         }

         int stored = barrel.getStored(block);
         if (stored <= 0) {
            return null;
         } else {
            int limit = barrel.getCapacity(block);
            boolean voidExcess = Boolean.parseBoolean(StorageCacheUtils.getData(blockMenu.getLocation(), "trash"));
            return new FluffyBarrel(blockMenu.getLocation(), clone, stored, limit, voidExcess);
         }
      } else {
         return null;
      }
   }

   @Nullable
   public static NetworkStorage getNetworkStorage(BlockMenu blockMenu) {
      return getNetworkStorage(blockMenu, false);
   }

   @Nullable
   public static NetworkStorage getNetworkStorage(BlockMenu blockMenu, boolean includeEmpty) {
      QuantumCache cache = NetworkQuantumStorage.getCaches().get(blockMenu.getLocation());
      if (cache == null) {
         return null;
      } else {
         ItemStack itemStack = cache.getItemStack();
         if ((itemStack == null || itemStack.getType() == Material.AIR) && !includeEmpty) {
            return null;
         } else {
            ItemStack output = blockMenu.getItemInSlot(7);
            long storedInt = cache.getAmountLong();
            if (output != null && output.getType() != Material.AIR && StackUtils.itemsMatch(cache, output)) {
               storedInt += (long)output.getAmount();
            }

            ItemStack clone = null;
            if (itemStack != null) {
               clone = itemStack.clone();
               clone.setAmount(1);
            }

            return new NetworkStorage(blockMenu.getLocation(), clone, storedInt);
         }
      }
   }

   @Nullable
   public static BarrelIdentity getBarrel(Location barrelLocation) {
      return getBarrel(barrelLocation, false);
   }

   @Nullable
   public static BarrelIdentity getBarrel(Location barrelLocation, boolean includeEmpty) {
      SlimefunItem item = StorageCacheUtils.getSfItem(barrelLocation);
      BlockMenu menu = StorageCacheUtils.getMenu(barrelLocation);
      if (menu == null) {
         return null;
      } else if (item instanceof NetworkQuantumStorage) {
         return getNetworkStorage(menu, includeEmpty);
      } else if (Networks.getSupportedPluginManager().isFluffyMachines() && item instanceof Barrel barrel) {
         return getFluffyBarrel(menu, barrel, includeEmpty);
      } else if (Networks.getSupportedPluginManager().isInfinityExpansion() && item instanceof StorageUnit storageUnit) {
         return getInfinityBarrel(menu, storageUnit, includeEmpty);
      } else {
         return Networks.getSupportedPluginManager().isTsingshanTechnology()
               && item instanceof io.gitee.crankygoudan.tsingshantechnology.items.chucun.StorageUnit unit
            ? getTsingshanBarrel(menu, unit, includeEmpty)
            : null;
      }
   }

   @Nullable
   public static StorageUnitData getCargoStorageUnitData(BlockMenu blockMenu) {
      return getCargoStorageUnitData(blockMenu.getLocation());
   }

   @Nullable
   public static StorageUnitData getCargoStorageUnitData(Location location) {
      return NetworksDrawer.getStorageData(location);
   }

   public void registerNode(Location location, NodeType type) {
      this.nodeLocations.add(location);
      switch (type) {
         case CELL:
            BlockMenu blockMenu = StorageCacheUtils.getMenu(location);
            if (blockMenu == null || isFakeCell(location)) {
               return;
            }
            break;
         case GREEDY_BLOCK:
            BlockMenu blockMenux = StorageCacheUtils.getMenu(location);
            if (blockMenux == null || !(StorageCacheUtils.getSfItem(location) instanceof NetworkGreedyBlock)) {
               return;
            }
            break;
         case ADVANCED_GREEDY_BLOCK:
            BlockMenu blockMenuxx = StorageCacheUtils.getMenu(location);
            if (blockMenuxx == null || !(StorageCacheUtils.getSfItem(location) instanceof AdvancedGreedyBlock)) {
               return;
            }
      }

      NetworkRoot.NodeIndex index = this.indexes.get(type);
      index.addLocation(location);
   }

   public int getNodeCount() {
      return this.nodeLocations.size();
   }

   public int getNodeCount(NodeType type) {
      return this.indexes.get(type).locations.size();
   }

   private void bumpInvalidationEpoch() {
      INVALIDATION_EPOCHS.computeIfAbsent(this.nodePosition, k -> new AtomicLong()).incrementAndGet();
   }

   private void bumpWriteEpoch() {
      WRITE_EPOCHS.computeIfAbsent(this.nodePosition, k -> new AtomicLong()).incrementAndGet();
   }

   private static long currentInvalidationEpoch(Location networkId) {
      AtomicLong counter = INVALIDATION_EPOCHS.get(networkId);
      return counter == null ? 0L : counter.get();
   }

   private static long currentWriteEpoch(Location networkId) {
      AtomicLong counter = WRITE_EPOCHS.get(networkId);
      return counter == null ? 0L : counter.get();
   }

   public void markDirty() {
      this.bumpInvalidationEpoch();
   }

   public void setOverburdened(boolean overburdened) {
      if (overburdened && !this.isOverburdened) {
         Location loc = this.nodePosition.clone();

         for (int x = 0; x <= 1; x++) {
            for (int y = 0; y <= 1; y++) {
               for (int z = 0; z <= 1; z++) {
                  loc.getWorld().spawnParticle(NetworksVersionedParticle.EXPLOSION, loc.clone().add((double)x, (double)y, (double)z), 0);
               }
            }
         }
      }

      this.isOverburdened = overburdened;
   }

   public Map<ItemStack, Long> getAllNetworkItemsLongTypeView() {
      return this.getAllNetworkItemsLongType();
   }

   public Map<ItemStack, Long> getAllNetworkItemsLongType() {
      return this.obtainItemSnapshot().items();
   }

   public Map<ItemKey, Long> getAllNetworkItemsKeyedView() {
      return this.obtainItemSnapshot().keyed();
   }

   private NetworkRoot.ItemSnapshot obtainItemSnapshot() {
      Location networkId = this.nodePosition;
      NetworkRoot.ItemSnapshot snapshot = SHARED_ITEM_SNAPSHOTS.get(networkId);
      long invalidationEpoch = currentInvalidationEpoch(networkId);
      long writeEpoch = currentWriteEpoch(networkId);
      int currentTick = Bukkit.getCurrentTick();
      if (snapshot != null) {
         if (snapshot.tick == currentTick) {
            if (snapshot.invalidationEpoch == invalidationEpoch) {
               return snapshot;
            }
         } else if (snapshot.invalidationEpoch == invalidationEpoch
            && snapshot.writeEpoch == writeEpoch
            && System.currentTimeMillis() - snapshot.publishedAtMs <= 500L) {
            return snapshot;
         }
      }

      ItemHashMap<Long> itemStacks = new ItemHashMap<>();

      for (BarrelIdentity barrelIdentity : this.getOutputAbleBarrels()) {
         addKeyedAmount(itemStacks, barrelIdentity.getItemStack(), barrelIdentity.getAmount());
      }

      for (StorageUnitData cache : this.getOutputAbleDrawerData().values()) {
         for (ItemContainer itemContainer : cache.getStoredItems()) {
            addKeyedAmount(itemStacks, itemContainer.getSample(), (long)itemContainer.getAmount());
         }
      }

      for (BlockMenu blockMenu : this.getAdvancedGreedyBlockMenus()) {
         ItemStack template = blockMenu.getItemInSlot(10);
         if (template != null && template.getType() != Material.AIR) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);

            for (int slot : slots) {
               ItemStack itemStack = blockMenu.getItemInSlot(slot);
               ItemStack identity = itemStack != null && itemStack.getType() != Material.AIR ? itemStack : template;
               addKeyedAmount(itemStacks, identity, itemStack);
            }
         }
      }

      for (BlockMenu blockMenux : this.getGreedyBlockMenus()) {
         ItemStack template = blockMenux.getItemInSlot(10);
         if (template != null && template.getType() != Material.AIR) {
            int[] slots = blockMenux.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            ItemStack itemStack = blockMenux.getItemInSlot(slots[0]);
            ItemStack identity = itemStack != null && itemStack.getType() != Material.AIR ? itemStack : template;
            addKeyedAmount(itemStacks, identity, itemStack);
         }
      }

      for (BlockMenu blockMenuxx : this.getCrafterOutputs()) {
         int[] slots = blockMenuxx.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);

         for (int slot : slots) {
            ItemStack itemStack = blockMenuxx.getItemInSlot(slot);
            if (itemStack != null && itemStack.getType() != Material.AIR) {
               addKeyedAmount(itemStacks, itemStack, itemStack);
            }
         }
      }

      for (BlockMenu blockMenuxx : this.getCellMenus()) {
         if (!isFakeCell(blockMenuxx)) {
            for (int slotx : CELL_AVAILABLE_SLOTS) {
               ItemStack itemStack = blockMenuxx.getItemInSlot(slotx);
               if (itemStack != null && itemStack.getType() != Material.AIR) {
                  addKeyedAmount(itemStacks, itemStack, itemStack);
               }
            }
         }
      }

      ItemHashMap<Long> cellItems = CellDrive.getStorage().getAllCellItemsKeyed(this.driveCache, this.getOutputAbleCellDriveMenus());

      for (Entry<ItemKey, Long> entry : cellItems.keyEntrySet()) {
         ItemKey key = entry.getKey();
         Long current = itemStacks.getKey(key);
         itemStacks.putKey(key, (current == null ? 0L : current) + entry.getValue());
      }

      NetworkRoot.ItemSnapshot published = new NetworkRoot.ItemSnapshot(
         itemStacks.keyedView(), invalidationEpoch, writeEpoch, currentTick, System.currentTimeMillis()
      );
      SHARED_ITEM_SNAPSHOTS.put(networkId, published);
      evictStaleSnapshotNetworks();
      return published;
   }

   private static void addKeyedAmount(ItemHashMap<Long> all, @Nullable ItemStack key, long amount) {
      if (key != null && key.getType() != Material.AIR) {
         ItemKey itemKey = ItemKey.exact(key);
         Long current = all.getKey(itemKey);
         all.putKey(itemKey, (current == null ? 0L : current) + amount);
      }
   }

   private static void addKeyedAmount(ItemHashMap<Long> all, @Nullable ItemStack key, @Nullable ItemStack item) {
      if (item != null && item.getType() != Material.AIR) {
         addKeyedAmount(all, key, (long)item.getAmount());
      }
   }

   private static void evictStaleSnapshotNetworks() {
      if (SHARED_ITEM_SNAPSHOTS.size() > 1) {
         long now = System.currentTimeMillis();
         SHARED_ITEM_SNAPSHOTS.entrySet().removeIf(entry -> {
            if (now - entry.getValue().publishedAtMs() > 3600000L) {
               INVALIDATION_EPOCHS.remove(entry.getKey());
               WRITE_EPOCHS.remove(entry.getKey());
               return true;
            } else {
               return false;
            }
         });
      }
   }

   public Map<ItemStack, Integer> getAllNetworkItems() {
      return this.getAllNetworkItemsLongType()
         .entrySet()
         .stream()
         .map(e -> Map.entry(e.getKey(), e.getValue() > 2147483647L ? Integer.MAX_VALUE : e.getValue().intValue()))
         .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (a, b) -> a));
   }

   public Set<BarrelIdentity> getBarrels() {
      if (this.barrels != null) {
         return this.barrels;
      } else {
         this.barrels = this.searchBarrels(AccessMode.ALL);
         new NetworkRootLocateStorageEvent(this, StorageType.BARREL, true, true, Bukkit.isPrimaryThread()).callEvent();
         return this.barrels;
      }
   }

   public Set<Location> getNodes(NodeType type) {
      return this.indexes.get(type).locations;
   }

   public Set<BlockMenu> getNodeMenus(NodeType type) {
      return this.indexes.get(type).getMenus();
   }

   private Map<Location, StorageUnitData> searchDrawers(AccessMode mode) {
      Set<Location> addedLocations = new HashSet<>();
      Map<Location, StorageUnitData> dataSet = new HashMap<>();
      HashSet<Location> monitors = new HashSet<>(this.getNodes(NodeType.STORAGE_MONITOR));
      if (mode.isInputAccess()) {
         monitors.addAll(this.getNodes(NodeType.INPUT_ONLY_MONITOR));
      }

      if (mode.isOutputAccess()) {
         monitors.addAll(this.getNodes(NodeType.OUTPUT_ONLY_MONITOR));
      }

      for (Location cellLocation : monitors) {
         BlockFace face = NetworkDirectional.getSelectedFace(cellLocation);
         if (face != null) {
            Location testLocation = cellLocation.clone().add(face.getDirection());
            if (addedLocations.add(testLocation)) {
               SlimefunItem slimefunItem = StorageCacheUtils.getSfItem(testLocation);
               if (slimefunItem instanceof NetworksDrawer) {
                  StorageUnitData data = getCargoStorageUnitData(testLocation);
                  if (data != null) {
                     dataSet.put(testLocation, data);
                  }
               }
            }
         }
      }

      return dataSet;
   }

   public Map<Location, StorageUnitData> getDrawerData() {
      if (this.drawerData != null) {
         return this.drawerData;
      } else {
         this.drawerData = this.searchDrawers(AccessMode.ALL);
         new NetworkRootLocateStorageEvent(this, StorageType.DRAWER, true, true, Bukkit.isPrimaryThread()).callEvent();
         return this.drawerData;
      }
   }

   public Set<BlockMenu> getCellMenus() {
      return this.getNodeMenus(NodeType.CELL);
   }

   public Set<BlockMenu> getCellDriveMenus() {
      if (this.cellDriveMenus != null) {
         return this.cellDriveMenus;
      } else {
         this.cellDriveMenus = this.searchCellDriveMenus(AccessMode.ALL);
         return this.cellDriveMenus;
      }
   }

   public Set<BlockMenu> getInputAbleCellDriveMenus() {
      if (this.inputAbleCellDriveMenus != null) {
         return this.inputAbleCellDriveMenus;
      } else {
         this.inputAbleCellDriveMenus = this.searchCellDriveMenus(AccessMode.INPUT);
         return this.inputAbleCellDriveMenus;
      }
   }

   public Set<BlockMenu> getOutputAbleCellDriveMenus() {
      if (this.outputAbleCellDriveMenus != null) {
         return this.outputAbleCellDriveMenus;
      } else {
         this.outputAbleCellDriveMenus = this.searchCellDriveMenus(AccessMode.OUTPUT);
         return this.outputAbleCellDriveMenus;
      }
   }

   private Set<BlockMenu> searchCellDriveMenus(AccessMode mode) {
      Set<BlockMenu> menus = new HashSet<>();
      Set<Location> addedLocations = new HashSet<>();
      HashSet<Location> monitors = new HashSet<>(this.getNodes(NodeType.STORAGE_MONITOR));
      if (mode.isInputAccess()) {
         monitors.addAll(this.getNodes(NodeType.INPUT_ONLY_MONITOR));
      }

      if (mode.isOutputAccess()) {
         monitors.addAll(this.getNodes(NodeType.OUTPUT_ONLY_MONITOR));
      }

      for (Location monitorLocation : monitors) {
         BlockFace face = NetworkDirectional.getSelectedFace(monitorLocation);
         if (face != null) {
            Location testLocation = monitorLocation.clone().add(face.getDirection());
            if (!addedLocations.contains(testLocation)) {
               addedLocations.add(testLocation);
               SlimefunItem slimefunItem = StorageCacheUtils.getSfItem(testLocation);
               if (slimefunItem instanceof CellDrive) {
                  BlockMenu menu = StorageCacheUtils.getMenu(testLocation);
                  if (menu != null) {
                     menus.add(menu);
                  }
               }
            }
         }
      }

      return menus;
   }

   public Set<BlockMenu> getCrafterOutputs() {
      return this.getNodeMenus(NodeType.CRAFTER);
   }

   public Set<BlockMenu> getGreedyBlockMenus() {
      return this.getNodeMenus(NodeType.GREEDY_BLOCK);
   }

   public Set<BlockMenu> getAdvancedGreedyBlockMenus() {
      return this.getNodeMenus(NodeType.ADVANCED_GREEDY_BLOCK);
   }

   public static void addAmount(Map<ItemStack, Long> all, ItemStack key, @Nullable ItemStack item) {
      if (item != null && item.getType() != Material.AIR) {
         long current = all.getOrDefault(key, 0L);
         all.put(key, current + (long)item.getAmount());
      }
   }

   public static void addAmount(Map<ItemStack, Long> all, ItemStack key, long amount) {
      all.put(key, all.getOrDefault(key, 0L) + amount);
   }

   public boolean contains(ItemStack itemStack) {
      return this.contains(new ItemRequest(itemStack, 1));
   }

   public boolean contains(ItemRequest request) {
      if (request.getItemStack() != null && !request.getItemStack().getType().isAir()) {
         long found = 0L;

         for (BarrelIdentity barrelIdentity : this.getOutputAbleBarrels()) {
            if (StackUtils.itemsMatch(request, barrelIdentity.getItemStack())) {
               if (barrelIdentity instanceof InfinityBarrel) {
                  found += Math.max(0L, barrelIdentity.getAmount() - 2L);
               } else {
                  found += barrelIdentity.getAmount();
               }

               if (found >= (long)request.getAmount()) {
                  return true;
               }
            }
         }

         for (BlockMenu blockMenu : this.getCrafterOutputs()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);

            for (int slot : slots) {
               ItemStack itemStack = blockMenu.getItemInSlot(slot);
               if (StackUtils.itemsMatch(request, itemStack)) {
                  found += (long)itemStack.getAmount();
                  if (found >= (long)request.getAmount()) {
                     return true;
                  }
               }
            }
         }

         for (StorageUnitData cache : this.getOutputAbleDrawerData().values()) {
            for (ItemContainer itemContainer : cache.getStoredItems()) {
               if (StackUtils.itemsMatch(request, itemContainer.getItemStack())) {
                  found += (long)itemContainer.getAmount();
                  if (found >= (long)request.getAmount()) {
                     return true;
                  }
               }
            }
         }

         for (BlockMenu blockMenu : this.getAdvancedGreedyBlockMenus()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);

            for (int slotx : slots) {
               ItemStack itemStack = blockMenu.getItemInSlot(slotx);
               if (StackUtils.itemsMatch(request, itemStack)) {
                  found += (long)itemStack.getAmount();
                  if (found >= (long)request.getAmount()) {
                     return true;
                  }
               }
            }
         }

         for (BlockMenu blockMenu : this.getGreedyBlockMenus()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            ItemStack itemStack = blockMenu.getItemInSlot(slots[0]);
            if (StackUtils.itemsMatch(request, itemStack)) {
               found += (long)itemStack.getAmount();
               if (found >= (long)request.getAmount()) {
                  return true;
               }
            }
         }

         for (BlockMenu blockMenux : this.getCellMenus()) {
            if (!isFakeCell(blockMenux)) {
               for (int slotxx : CELL_AVAILABLE_SLOTS) {
                  ItemStack itemStack = blockMenux.getItemInSlot(slotxx);
                  if (StackUtils.itemsMatch(request, itemStack)) {
                     found += (long)itemStack.getAmount();
                     if (found >= (long)request.getAmount()) {
                        return true;
                     }
                  }
               }
            }
         }

         found += (long)CellDrive.getStorage().getAmount(this.driveCache, this.getOutputAbleCellDriveMenus(), request.getItemStack());
         return found >= (long)request.getAmount();
      } else {
         return false;
      }
   }

   public int getAmount(ItemStack itemStack) {
      long totalAmount = 0L;

      for (BlockMenu blockMenu : this.getAdvancedGreedyBlockMenus()) {
         int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);

         for (int slot : slots) {
            ItemStack inputSlotItem = blockMenu.getItemInSlot(slot);
            if (inputSlotItem != null && StackUtils.itemsMatch(inputSlotItem, itemStack)) {
               totalAmount += (long)inputSlotItem.getAmount();
            }
         }
      }

      for (BlockMenu blockMenu : this.getGreedyBlockMenus()) {
         int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
         ItemStack inputSlotItem = blockMenu.getItemInSlot(slots[0]);
         if (inputSlotItem != null && StackUtils.itemsMatch(inputSlotItem, itemStack)) {
            totalAmount += (long)inputSlotItem.getAmount();
         }
      }

      for (BarrelIdentity barrelIdentity : this.getOutputAbleBarrels()) {
         if (StackUtils.itemsMatch(barrelIdentity, itemStack)) {
            totalAmount += barrelIdentity.getAmount();
            if (barrelIdentity instanceof InfinityBarrel) {
               totalAmount -= 2L;
            }
         }
      }

      for (StorageUnitData cache : this.getOutputAbleDrawerData().values()) {
         for (ItemContainer itemContainer : cache.getStoredItems()) {
            if (StackUtils.itemsMatch(itemContainer, itemStack)) {
               totalAmount += (long)itemContainer.getAmount();
            }
         }
      }

      for (BlockMenu blockMenux : this.getCellMenus()) {
         if (!isFakeCell(blockMenux)) {
            for (int slotx : CELL_AVAILABLE_SLOTS) {
               ItemStack cellItem = blockMenux.getItemInSlot(slotx);
               if (cellItem != null && StackUtils.itemsMatch(cellItem, itemStack)) {
                  totalAmount += (long)cellItem.getAmount();
               }
            }
         }
      }

      totalAmount += (long)CellDrive.getStorage().getAmount(this.driveCache, this.getOutputAbleCellDriveMenus(), itemStack);
      return totalAmount > 2147483647L ? Integer.MAX_VALUE : (int)totalAmount;
   }

   public HashMap<ItemStack, Long> getAmount(Set<ItemStack> itemStacks) {
      HashMap<ItemStack, Long> totalAmounts = new HashMap<>();

      for (BlockMenu menu : this.getAdvancedGreedyBlockMenus()) {
         int[] slots = menu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);

         for (int slot : slots) {
            ItemStack inputSlotItem = menu.getItemInSlot(slot);
            if (inputSlotItem != null) {
               for (ItemStack itemStack : itemStacks) {
                  if (StackUtils.itemsMatch(inputSlotItem, itemStack)) {
                     totalAmounts.put(itemStack, totalAmounts.getOrDefault(itemStack, 0L) + (long)inputSlotItem.getAmount());
                  }
               }
            }
         }
      }

      for (BlockMenu blockMenu : this.getGreedyBlockMenus()) {
         int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
         ItemStack inputSlotItem = blockMenu.getItemInSlot(slots[0]);
         if (inputSlotItem != null) {
            for (ItemStack itemStackx : itemStacks) {
               if (StackUtils.itemsMatch(inputSlotItem, itemStackx)) {
                  totalAmounts.put(itemStackx, totalAmounts.getOrDefault(itemStackx, 0L) + (long)inputSlotItem.getAmount());
               }
            }
         }
      }

      for (BarrelIdentity barrelIdentity : this.getOutputAbleBarrels()) {
         for (ItemStack itemStackxx : itemStacks) {
            if (StackUtils.itemsMatch(barrelIdentity, itemStackxx)) {
               long totalAmount = barrelIdentity.getAmount();
               if (barrelIdentity instanceof InfinityBarrel) {
                  totalAmount -= 2L;
               }

               totalAmounts.put(itemStackxx, totalAmounts.getOrDefault(itemStackxx, 0L) + totalAmount);
            }
         }
      }

      for (StorageUnitData cache : this.getOutputAbleDrawerData().values()) {
         for (ItemContainer itemContainer : cache.getStoredItems()) {
            for (ItemStack itemStackxxx : itemStacks) {
               if (StackUtils.itemsMatch(itemContainer, itemStackxxx)) {
                  long totalAmount = (long)itemContainer.getAmount();
                  totalAmounts.put(itemStackxxx, totalAmounts.getOrDefault(itemStackxxx, 0L) + totalAmount);
               }
            }
         }
      }

      for (BlockMenu blockMenux : this.getCellMenus()) {
         if (!isFakeCell(blockMenux)) {
            for (int slotx : CELL_AVAILABLE_SLOTS) {
               ItemStack cellItem = blockMenux.getItemInSlot(slotx);
               if (cellItem != null) {
                  for (ItemStack itemStackxxxx : itemStacks) {
                     if (StackUtils.itemsMatch(cellItem, itemStackxxxx)) {
                        totalAmounts.put(itemStackxxxx, totalAmounts.getOrDefault(itemStackxxxx, 0L) + (long)cellItem.getAmount());
                     }
                  }
               }
            }
         }
      }

      for (ItemStack itemStackxxxxx : itemStacks) {
         long cellAmount = (long)CellDrive.getStorage().getAmount(this.driveCache, this.getOutputAbleCellDriveMenus(), itemStackxxxxx);
         if (cellAmount > 0L) {
            totalAmounts.put(itemStackxxxxx, totalAmounts.getOrDefault(itemStackxxxxx, 0L) + cellAmount);
         }
      }

      return totalAmounts;
   }

   @Override
   public long retrieveBlockCharge() {
      return 0L;
   }

   public void addRootPower(long power) {
      this.rootPower.getAndAdd(power);
   }

   public long getRootPower() {
      return this.rootPower.get();
   }

   public void removeRootPower(long power) {
      if (power <= 0L) {
         this.addRootPower(-power);
      } else {
         long removed = 0L;

         for (Location node : this.getNodes(NodeType.POWER_NODE)) {
            if (StorageCacheUtils.getSfItem(node) instanceof NetworkPowerNode powerNode) {
               int charge = powerNode.getCharge(node);
               if (charge <= 0) {
                  continue;
               }

               int toRemove = (int)Math.min(power - removed, (long)charge);
               powerNode.removeCharge(node, toRemove);
               removed += (long)toRemove;
            }

            if (removed >= power) {
               break;
            }
         }

         this.rootPower.getAndAdd(-removed);
      }
   }

   public List<ItemStack> getItemStacks(@Nullable Location accesor, List<ItemRequest> itemRequests) {
      List<ItemStack> retrievedItems = new ArrayList<>();

      for (ItemRequest request : itemRequests) {
         ItemStack retrieved = this.getItemStack(accesor, request);
         if (retrieved != null) {
            retrievedItems.add(retrieved);
         }
      }

      return retrievedItems;
   }

   public List<BarrelIdentity> getBarrels(Predicate<BarrelIdentity> filter, NetworkRootLocateStorageEvent.Strategy strategy, boolean includeEmpty) {
      Set<Location> addedLocations = ConcurrentHashMap.newKeySet();
      List<BarrelIdentity> barrelSet = new ArrayList<>();
      Set<Location> monitors = new HashSet<>(this.getNodes(NodeType.STORAGE_MONITOR));
      monitors.addAll(this.getNodes(NodeType.INPUT_ONLY_MONITOR));
      monitors.addAll(this.getNodes(NodeType.OUTPUT_ONLY_MONITOR));

      for (Location cellLocation : monitors) {
         BlockFace face = NetworkDirectional.getSelectedFace(cellLocation);
         if (face != null) {
            Location testLocation = cellLocation.clone().add(face.getDirection());
            if (addedLocations.add(testLocation)) {
               SlimefunItem slimefunItem = StorageCacheUtils.getSfItem(testLocation);
               if (Networks.getSupportedPluginManager().isInfinityExpansion() && slimefunItem instanceof StorageUnit) {
                  StorageUnit unit = (StorageUnit)slimefunItem;
                  BlockMenu menu = StorageCacheUtils.getMenu(testLocation);
                  if (menu != null) {
                     InfinityBarrel infinityBarrel = getInfinityBarrel(menu, unit, includeEmpty);
                     if (infinityBarrel != null && filter.test(infinityBarrel)) {
                        barrelSet.add(infinityBarrel);
                     }
                  }
               } else if (Networks.getSupportedPluginManager().isTsingshanTechnology()
                  && slimefunItem instanceof io.gitee.crankygoudan.tsingshantechnology.items.chucun.StorageUnit) {
                  io.gitee.crankygoudan.tsingshantechnology.items.chucun.StorageUnit unit = (io.gitee.crankygoudan.tsingshantechnology.items.chucun.StorageUnit)slimefunItem;
                  BlockMenu menu = StorageCacheUtils.getMenu(testLocation);
                  if (menu != null) {
                     TsingshanBarrel tsingshanBarrel = getTsingshanBarrel(menu, unit, includeEmpty);
                     if (tsingshanBarrel != null && filter.test(tsingshanBarrel)) {
                        barrelSet.add(tsingshanBarrel);
                     }
                  }
               } else if (Networks.getSupportedPluginManager().isFluffyMachines() && slimefunItem instanceof Barrel) {
                  Barrel barrel = (Barrel)slimefunItem;
                  BlockMenu menu = StorageCacheUtils.getMenu(testLocation);
                  if (menu != null) {
                     FluffyBarrel fluffyBarrel = getFluffyBarrel(menu, barrel, includeEmpty);
                     if (fluffyBarrel != null && filter.test(fluffyBarrel)) {
                        barrelSet.add(fluffyBarrel);
                     }
                  }
               } else if (slimefunItem instanceof NetworkQuantumStorage) {
                  BlockMenu menu = StorageCacheUtils.getMenu(testLocation);
                  if (menu != null) {
                     NetworkStorage storage = getNetworkStorage(menu, includeEmpty);
                     if (storage != null && filter.test(storage)) {
                        barrelSet.add(storage);
                     }
                  }
               }
            }
         }
      }

      new NetworkRootLocateStorageEvent(this, StorageType.BARREL, strategy, Bukkit.isPrimaryThread()).callEvent();
      return barrelSet;
   }

   public Map<Location, StorageUnitData> getDrawerData(NetworkRootLocateStorageEvent.Strategy strategy) {
      Set<Location> addedLocations = ConcurrentHashMap.newKeySet();
      Map<Location, StorageUnitData> dataSet = new HashMap<>();
      Set<Location> monitor = new HashSet<>(this.getNodes(NodeType.STORAGE_MONITOR));
      monitor.addAll(this.getNodes(NodeType.INPUT_ONLY_MONITOR));
      monitor.addAll(this.getNodes(NodeType.OUTPUT_ONLY_MONITOR));

      for (Location cellLocation : monitor) {
         BlockFace face = NetworkDirectional.getSelectedFace(cellLocation);
         if (face != null) {
            Location testLocation = cellLocation.clone().add(face.getDirection());
            if (addedLocations.add(testLocation)) {
               SlimefunItem slimefunItem = StorageCacheUtils.getSfItem(testLocation);
               if (slimefunItem instanceof NetworksDrawer) {
                  StorageUnitData data = getCargoStorageUnitData(testLocation);
                  if (data != null) {
                     dataSet.put(testLocation, data);
                  }
               }
            }
         }
      }

      new NetworkRootLocateStorageEvent(this, StorageType.DRAWER, strategy, Bukkit.isPrimaryThread()).callEvent();
      return dataSet;
   }

   public Set<BarrelIdentity> getInputAbleBarrels() {
      if (this.inputAbleBarrels != null) {
         return this.inputAbleBarrels;
      } else {
         this.inputAbleBarrels = this.searchBarrels(AccessMode.INPUT);
         new NetworkRootLocateStorageEvent(this, StorageType.BARREL, true, false, Bukkit.isPrimaryThread()).callEvent();
         return this.inputAbleBarrels;
      }
   }

   private Set<BarrelIdentity> searchBarrels(AccessMode mode) {
      Set<Location> addedLocations = new HashSet<>();
      Set<BarrelIdentity> barrelSet = new HashSet<>();
      Set<Location> monitor = new HashSet<>(this.getNodes(NodeType.STORAGE_MONITOR));
      if (mode.isInputAccess()) {
         monitor.addAll(this.getNodes(NodeType.INPUT_ONLY_MONITOR));
      }

      if (mode.isOutputAccess()) {
         monitor.addAll(this.getNodes(NodeType.OUTPUT_ONLY_MONITOR));
      }

      for (Location cellLocation : monitor) {
         BlockFace face = NetworkDirectional.getSelectedFace(cellLocation);
         if (face != null) {
            Location testLocation = cellLocation.clone().add(face.getDirection());
            if (addedLocations.add(testLocation)) {
               SlimefunItem slimefunItem = StorageCacheUtils.getSfItem(testLocation);
               BlockMenu menu = StorageCacheUtils.getMenu(testLocation);
               if (menu != null) {
                  if (Networks.getSupportedPluginManager().isInfinityExpansion() && slimefunItem instanceof StorageUnit) {
                     StorageUnit unit = (StorageUnit)slimefunItem;
                     InfinityBarrel infinityBarrel = getInfinityBarrel(menu, unit);
                     if (infinityBarrel != null) {
                        barrelSet.add(infinityBarrel);
                     }
                  } else if (Networks.getSupportedPluginManager().isTsingshanTechnology()
                     && slimefunItem instanceof io.gitee.crankygoudan.tsingshantechnology.items.chucun.StorageUnit) {
                     io.gitee.crankygoudan.tsingshantechnology.items.chucun.StorageUnit unit = (io.gitee.crankygoudan.tsingshantechnology.items.chucun.StorageUnit)slimefunItem;
                     TsingshanBarrel tsingshanBarrel = getTsingshanBarrel(menu, unit);
                     if (tsingshanBarrel != null) {
                        barrelSet.add(tsingshanBarrel);
                     }
                  } else if (Networks.getSupportedPluginManager().isFluffyMachines() && slimefunItem instanceof Barrel) {
                     Barrel barrel = (Barrel)slimefunItem;
                     FluffyBarrel fluffyBarrel = getFluffyBarrel(menu, barrel);
                     if (fluffyBarrel != null) {
                        barrelSet.add(fluffyBarrel);
                     }
                  } else if (slimefunItem instanceof NetworkQuantumStorage) {
                     NetworkStorage storage = getNetworkStorage(menu);
                     if (storage != null) {
                        barrelSet.add(storage);
                     }
                  }
               }
            }
         }
      }

      return barrelSet;
   }

   public Set<BarrelIdentity> getOutputAbleBarrels() {
      if (this.outputAbleBarrels != null) {
         return this.outputAbleBarrels;
      } else {
         this.outputAbleBarrels = this.searchBarrels(AccessMode.OUTPUT);
         new NetworkRootLocateStorageEvent(this, StorageType.BARREL, false, true, Bukkit.isPrimaryThread()).callEvent();
         return this.outputAbleBarrels;
      }
   }

   public Map<Location, StorageUnitData> getInputAbleDrawerData() {
      if (this.inputAbleDrawerData != null) {
         return this.inputAbleDrawerData;
      } else {
         this.inputAbleDrawerData = this.searchDrawers(AccessMode.INPUT);
         new NetworkRootLocateStorageEvent(this, StorageType.DRAWER, true, false, Bukkit.isPrimaryThread()).callEvent();
         return this.inputAbleDrawerData;
      }
   }

   public Map<Location, StorageUnitData> getOutputAbleDrawerData() {
      if (this.outputAbleDrawerData != null) {
         return this.outputAbleDrawerData;
      } else {
         this.outputAbleDrawerData = this.searchDrawers(AccessMode.OUTPUT);
         new NetworkRootLocateStorageEvent(this, StorageType.DRAWER, false, true, Bukkit.isPrimaryThread()).callEvent();
         return this.outputAbleDrawerData;
      }
   }

   @Nullable
   public BarrelIdentity accessInputAbleBarrel(Location barrelLocation) {
      return this.getMapInputAbleBarrels().get(barrelLocation);
   }

   @Nullable
   public BarrelIdentity accessOutputAbleBarrel(Location barrelLocation) {
      return this.getMapOutputAbleBarrels().get(barrelLocation);
   }

   @Nullable
   public StorageUnitData accessInputAbleDrawerData(Location drawerLocation) {
      return this.accessInputAbleCargoStorageUnitData(drawerLocation);
   }

   @Nullable
   public StorageUnitData accessOutputAbleDrawerData(Location drawerLocation) {
      return this.accessOutputAbleCargoStorageUnitData(drawerLocation);
   }

   @Nullable
   public StorageUnitData accessInputAbleCargoStorageUnitData(Location storageUnitLocation) {
      return this.getMapInputAbleCargoStorageUnits().get(storageUnitLocation);
   }

   @Nullable
   public StorageUnitData accessOutputAbleCargoStorageUnitData(Location storageUnitLocation) {
      return this.getMapOutputAbleCargoStorageUnits().get(storageUnitLocation);
   }

   @Nullable
   public ItemStack requestItem(@Nullable Location accessor, ItemRequest request) {
      return this.getItemStack(accessor, request);
   }

   @Nullable
   public ItemStack requestItem(@Nullable Location accessor, ItemStack itemStack) {
      return this.requestItem(accessor, new ItemRequest(itemStack, itemStack.getAmount()));
   }

   public void tryRecord(@Nullable Location accessor, ItemRequest request) {
      if (accessor != null) {
         if (this.recordFlow && this.itemFlowRecord != null) {
            this.itemFlowRecord.addAction(accessor, request);
         }
      }
   }

   @Nullable
   public ItemStack getItemStack(@Nullable Location accessor, ItemRequest request) {
      return this.getItemStack(accessor, request, null);
   }

   @Nullable
   public ItemStack getItemStack(@Nullable Location accessor, ItemRequest request, @Nullable MatchOption option) {
      ItemStack stackToReturn = null;
      if (request.getAmount() <= 0) {
         FeedbackSendable.sendFeedback0(accessor, FeedbackType.ROOT_REQUEST_0);
         return null;
      } else if (!this.allowAccessOutput(accessor)) {
         FeedbackSendable.sendFeedback0(accessor, FeedbackType.ROOT_LIMITING_ACCESS_OUTPUT);
         return null;
      } else {
         synchronized (RootWriteLock.get()) {
            this.bumpWriteEpoch();
            Map<Location, Integer> m = getPersistentAccessHistory(accessor);
            if (m != null) {
               boolean found = false;
               List<Location> misses = new ArrayList<>();

               for (Entry<Location, Integer> entry : m.entrySet()) {
                  BarrelIdentity barrelIdentity = this.accessOutputAbleBarrel(entry.getKey());
                  if (barrelIdentity != null) {
                     ItemStack itemStack = barrelIdentity.getItemStack();
                     if (!StackUtils.itemsMatch(request, itemStack, option)) {
                        misses.add(entry.getKey());
                     } else {
                        minusCacheMiss(accessor, entry.getKey());
                        found = true;
                        boolean infinity = barrelIdentity instanceof InfinityBarrel;
                        ItemStack fetched = barrelIdentity.requestItem(request);
                        if (fetched != null
                           && fetched.getType() != Material.AIR
                           && (!infinity || fetched.getAmount() != 1)
                           && StackUtils.itemsMatch(request, fetched, option)) {
                           if (stackToReturn == null) {
                              stackToReturn = fetched.clone();
                              stackToReturn.setAmount(0);
                           }

                           int preserveAmount = infinity ? fetched.getAmount() - 1 : fetched.getAmount();
                           if (request.getAmount() <= preserveAmount) {
                              this.uncontrolAccessOutput(accessor);
                              stackToReturn.setAmount(stackToReturn.getAmount() + request.getAmount());
                              fetched.setAmount(fetched.getAmount() - request.getAmount());
                              this.tryRecord(accessor, request);
                              return stackToReturn;
                           }

                           stackToReturn.setAmount(stackToReturn.getAmount() + preserveAmount);
                           request.receiveAmount(preserveAmount);
                           fetched.setAmount(fetched.getAmount() - preserveAmount);
                        }
                     }
                  } else {
                     StorageUnitData data = this.accessOutputAbleCargoStorageUnitData(entry.getKey());
                     if (data != null) {
                        ItemStack take = data.requestItem(accessor, request);
                        if (take != null) {
                           minusCacheMiss(accessor, entry.getKey());
                           found = true;
                           if (stackToReturn == null) {
                              stackToReturn = take.clone();
                           } else {
                              stackToReturn.setAmount(stackToReturn.getAmount() + take.getAmount());
                           }

                           request.receiveAmount(take.getAmount());
                           if (request.getAmount() <= 0) {
                              this.uncontrolAccessOutput(accessor);
                              this.tryRecord(accessor, request);
                              return stackToReturn;
                           }
                        } else {
                           misses.add(entry.getKey());
                        }
                     } else {
                        misses.add(entry.getKey());
                     }
                  }
               }

               if (!found) {
                  for (Location miss : misses) {
                     minusCacheMiss(accessor, miss);
                  }
               }
            }

            for (BarrelIdentity barrelIdentity : this.getOutputAbleBarrels()) {
               ItemStack itemStack = barrelIdentity.getItemStack();
               if (itemStack != null && StackUtils.itemsMatch(request, itemStack, option)) {
                  addCountObservingAccessHistory(accessor, barrelIdentity.getLocation());
                  boolean infinity = barrelIdentity instanceof InfinityBarrel;
                  ItemStack fetched = barrelIdentity.requestItem(request);
                  if (fetched != null
                     && fetched.getType() != Material.AIR
                     && (!infinity || fetched.getAmount() != 1)
                     && StackUtils.itemsMatch(request, fetched, option)) {
                     if (stackToReturn == null) {
                        stackToReturn = fetched.clone();
                        stackToReturn.setAmount(0);
                     }

                     int preserveAmount = infinity ? fetched.getAmount() - 1 : fetched.getAmount();
                     if (request.getAmount() <= preserveAmount) {
                        this.uncontrolAccessOutput(accessor);
                        stackToReturn.setAmount(stackToReturn.getAmount() + request.getAmount());
                        fetched.setAmount(fetched.getAmount() - request.getAmount());
                        this.tryRecord(accessor, request);
                        return stackToReturn;
                     }

                     stackToReturn.setAmount(stackToReturn.getAmount() + preserveAmount);
                     request.receiveAmount(preserveAmount);
                     fetched.setAmount(fetched.getAmount() - preserveAmount);
                  }
               }
            }

            for (StorageUnitData cache : this.getOutputAbleDrawerData().values()) {
               ItemStack take = cache.requestItem(accessor, request);
               if (take != null) {
                  addCountObservingAccessHistory(accessor, cache.getLastLocation());
                  if (stackToReturn == null) {
                     stackToReturn = take.clone();
                  } else {
                     stackToReturn.setAmount(stackToReturn.getAmount() + take.getAmount());
                  }

                  request.receiveAmount(take.getAmount());
                  if (request.getAmount() <= 0) {
                     this.uncontrolAccessOutput(accessor);
                     this.tryRecord(accessor, request);
                     return stackToReturn;
                  }
               }
            }

            ItemStack take = CellDrive.getStorage().takeItem(this.driveCache, this.getOutputAbleCellDriveMenus(), request);
            if (take != null) {
               if (stackToReturn == null) {
                  stackToReturn = take.clone();
               } else {
                  stackToReturn.setAmount(stackToReturn.getAmount() + take.getAmount());
               }

               request.receiveAmount(take.getAmount());
               if (request.getAmount() <= 0) {
                  this.tryRecord(accessor, request);
                  return stackToReturn;
               }
            }

            for (BlockMenu blockMenu : this.getCellMenus()) {
               if (!isFakeCell(blockMenu)) {
                  for (int slot : CELL_AVAILABLE_SLOTS) {
                     ItemStack itemStack = blockMenu.getItemInSlot(slot);
                     if (itemStack != null && itemStack.getType() != Material.AIR && StackUtils.itemsMatch(request, itemStack, option)) {
                        blockMenu.markDirty();
                        if (stackToReturn == null) {
                           stackToReturn = itemStack.clone();
                           stackToReturn.setAmount(0);
                        }

                        if (request.getAmount() <= itemStack.getAmount()) {
                           this.uncontrolAccessOutput(accessor);
                           stackToReturn.setAmount(stackToReturn.getAmount() + request.getAmount());
                           itemStack.setAmount(itemStack.getAmount() - request.getAmount());
                           this.tryRecord(accessor, request);
                           return stackToReturn;
                        }

                        stackToReturn.setAmount(stackToReturn.getAmount() + itemStack.getAmount());
                        request.receiveAmount(itemStack.getAmount());
                        itemStack.setAmount(0);
                     }
                  }
               }
            }

            for (BlockMenu blockMenux : this.getCrafterOutputs()) {
               int[] slots = blockMenux.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);

               for (int slotx : slots) {
                  ItemStack itemStack = blockMenux.getItemInSlot(slotx);
                  if (itemStack != null && itemStack.getType() != Material.AIR && StackUtils.itemsMatch(request, itemStack, option)) {
                     if (stackToReturn == null) {
                        stackToReturn = itemStack.clone();
                        stackToReturn.setAmount(0);
                     }

                     if (request.getAmount() <= itemStack.getAmount()) {
                        this.uncontrolAccessOutput(accessor);
                        stackToReturn.setAmount(stackToReturn.getAmount() + request.getAmount());
                        itemStack.setAmount(itemStack.getAmount() - request.getAmount());
                        this.tryRecord(accessor, request);
                        return stackToReturn;
                     }

                     stackToReturn.setAmount(stackToReturn.getAmount() + itemStack.getAmount());
                     request.receiveAmount(itemStack.getAmount());
                     itemStack.setAmount(0);
                  }
               }
            }

            for (BlockMenu blockMenux : this.getAdvancedGreedyBlockMenus()) {
               int[] slots = blockMenux.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);

               for (int slotxx : slots) {
                  ItemStack itemStack = blockMenux.getItemInSlot(slotxx);
                  if (itemStack != null && itemStack.getType() != Material.AIR && StackUtils.itemsMatch(request, itemStack, option)) {
                     if (stackToReturn == null) {
                        stackToReturn = itemStack.clone();
                        stackToReturn.setAmount(0);
                     }

                     if (request.getAmount() <= itemStack.getAmount()) {
                        this.uncontrolAccessOutput(accessor);
                        stackToReturn.setAmount(stackToReturn.getAmount() + request.getAmount());
                        itemStack.setAmount(itemStack.getAmount() - request.getAmount());
                        this.tryRecord(accessor, request);
                        return stackToReturn;
                     }

                     stackToReturn.setAmount(stackToReturn.getAmount() + itemStack.getAmount());
                     request.receiveAmount(itemStack.getAmount());
                     itemStack.setAmount(0);
                  }
               }
            }

            for (BlockMenu blockMenux : this.getGreedyBlockMenus()) {
               int[] slots = blockMenux.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
               ItemStack itemStack = blockMenux.getItemInSlot(slots[0]);
               if (itemStack != null && itemStack.getType() != Material.AIR && StackUtils.itemsMatch(request, itemStack, option)) {
                  blockMenux.markDirty();
                  if (stackToReturn == null) {
                     stackToReturn = itemStack.clone();
                     stackToReturn.setAmount(0);
                  }

                  if (request.getAmount() <= itemStack.getAmount()) {
                     this.uncontrolAccessOutput(accessor);
                     stackToReturn.setAmount(stackToReturn.getAmount() + request.getAmount());
                     itemStack.setAmount(itemStack.getAmount() - request.getAmount());
                     this.tryRecord(accessor, request);
                     return stackToReturn;
                  }

                  stackToReturn.setAmount(stackToReturn.getAmount() + itemStack.getAmount());
                  request.receiveAmount(itemStack.getAmount());
                  itemStack.setAmount(0);
               }
            }

            if (stackToReturn != null && stackToReturn.getAmount() != 0) {
               this.uncontrolAccessOutput(accessor);
               this.tryRecord(accessor, request);
               return stackToReturn;
            } else {
               return null;
            }
         }
      }
   }

   public List<ItemStack> getItemStacksBatch(Location accessor, List<ItemRequest> requests) {
      if (!this.allowAccessOutput(accessor)) {
         FeedbackSendable.sendFeedback0(accessor, FeedbackType.ROOT_LIMITING_ACCESS_OUTPUT);
         return List.of();
      } else {
         List<ItemStack> results = new ArrayList<>(requests.size());
         List<NetworkRoot.BatchTake> pending = new ArrayList<>(requests.size());

         for (int i = 0; i < requests.size(); i++) {
            results.add(null);
            ItemRequest request = requests.get(i);
            if (request.getAmount() <= 0) {
               FeedbackSendable.sendFeedback0(accessor, FeedbackType.ROOT_REQUEST_0);
            } else {
               pending.add(new NetworkRoot.BatchTake(request, i));
            }
         }

         if (pending.isEmpty()) {
            return results;
         } else {
            synchronized (RootWriteLock.get()) {
               this.bumpWriteEpoch();
               Map<Location, Integer> m = getPersistentAccessHistory(accessor);
               if (m != null) {
                  for (Entry<Location, Integer> entry : m.entrySet()) {
                     Location historyLocation = entry.getKey();
                     BarrelIdentity barrelIdentity = this.accessOutputAbleBarrel(historyLocation);
                     if (barrelIdentity != null) {
                        ItemStack barrelItem = barrelIdentity.getItemStack();
                        if (barrelItem == null) {
                           for (NetworkRoot.BatchTake take : pending) {
                              take.historyMisses.add(historyLocation);
                           }
                           continue;
                        }

                        boolean infinity = barrelIdentity instanceof InfinityBarrel;
                        Iterator<NetworkRoot.BatchTake> iterator = pending.iterator();

                        while (iterator.hasNext()) {
                           NetworkRoot.BatchTake take = iterator.next();
                           ItemRequest request = take.request;
                           if (!StackUtils.itemsMatch(request, barrelItem)) {
                              take.historyMisses.add(historyLocation);
                           } else {
                              minusCacheMiss(accessor, historyLocation);
                              take.historyFound = true;
                              ItemStack fetched = barrelIdentity.requestItem(request);
                              if (fetched != null
                                 && fetched.getType() != Material.AIR
                                 && (!infinity || fetched.getAmount() != 1)
                                 && StackUtils.itemsMatch(request, fetched)) {
                                 if (take.collected == null) {
                                    take.collected = fetched.clone();
                                    take.collected.setAmount(0);
                                 }

                                 int preserveAmount = infinity ? fetched.getAmount() - 1 : fetched.getAmount();
                                 if (request.getAmount() <= preserveAmount) {
                                    this.uncontrolAccessOutput(accessor);
                                    take.collected.setAmount(take.collected.getAmount() + request.getAmount());
                                    fetched.setAmount(fetched.getAmount() - request.getAmount());
                                    this.tryRecord(accessor, request);
                                    results.set(take.index, take.collected);
                                    iterator.remove();
                                 } else {
                                    take.collected.setAmount(take.collected.getAmount() + preserveAmount);
                                    request.receiveAmount(preserveAmount);
                                    fetched.setAmount(fetched.getAmount() - preserveAmount);
                                 }
                              }
                           }
                        }
                     } else {
                        StorageUnitData data = this.accessOutputAbleCargoStorageUnitData(historyLocation);
                        if (data != null) {
                           Iterator<NetworkRoot.BatchTake> iterator = pending.iterator();

                           while (iterator.hasNext()) {
                              NetworkRoot.BatchTake take = iterator.next();
                              ItemStack takeStack = data.requestItem(accessor, take.request);
                              if (takeStack != null) {
                                 minusCacheMiss(accessor, historyLocation);
                                 take.historyFound = true;
                                 if (take.collected == null) {
                                    take.collected = takeStack.clone();
                                 } else {
                                    take.collected.setAmount(take.collected.getAmount() + takeStack.getAmount());
                                 }

                                 take.request.receiveAmount(takeStack.getAmount());
                                 if (take.request.getAmount() <= 0) {
                                    this.uncontrolAccessOutput(accessor);
                                    this.tryRecord(accessor, take.request);
                                    results.set(take.index, take.collected);
                                    iterator.remove();
                                 }
                              } else {
                                 take.historyMisses.add(historyLocation);
                              }
                           }
                        } else {
                           for (NetworkRoot.BatchTake take : pending) {
                              take.historyMisses.add(historyLocation);
                           }
                        }
                     }

                     if (pending.isEmpty()) {
                        break;
                     }
                  }

                  for (NetworkRoot.BatchTake take : pending) {
                     if (!take.historyFound) {
                        for (Location miss : take.historyMisses) {
                           minusCacheMiss(accessor, miss);
                        }
                     }
                  }
               }

               if (pending.isEmpty()) {
                  return results;
               } else {
                  for (BarrelIdentity barrelIdentityx : this.getOutputAbleBarrels()) {
                     ItemStack barrelItem = barrelIdentityx.getItemStack();
                     if (barrelItem != null) {
                        boolean infinity = barrelIdentityx instanceof InfinityBarrel;
                        Iterator<NetworkRoot.BatchTake> iterator = pending.iterator();

                        while (iterator.hasNext()) {
                           NetworkRoot.BatchTake takex = iterator.next();
                           ItemRequest request = takex.request;
                           if (StackUtils.itemsMatch(request, barrelItem)) {
                              addCountObservingAccessHistory(accessor, barrelIdentityx.getLocation());
                              ItemStack fetched = barrelIdentityx.requestItem(request);
                              if (fetched != null
                                 && fetched.getType() != Material.AIR
                                 && (!infinity || fetched.getAmount() != 1)
                                 && StackUtils.itemsMatch(request, fetched)) {
                                 if (takex.collected == null) {
                                    takex.collected = fetched.clone();
                                    takex.collected.setAmount(0);
                                 }

                                 int preserveAmount = infinity ? fetched.getAmount() - 1 : fetched.getAmount();
                                 if (request.getAmount() <= preserveAmount) {
                                    this.uncontrolAccessOutput(accessor);
                                    takex.collected.setAmount(takex.collected.getAmount() + request.getAmount());
                                    fetched.setAmount(fetched.getAmount() - request.getAmount());
                                    this.tryRecord(accessor, request);
                                    results.set(takex.index, takex.collected);
                                    iterator.remove();
                                 } else {
                                    takex.collected.setAmount(takex.collected.getAmount() + preserveAmount);
                                    request.receiveAmount(preserveAmount);
                                    fetched.setAmount(fetched.getAmount() - preserveAmount);
                                 }
                              }
                           }
                        }

                        if (pending.isEmpty()) {
                           return results;
                        }
                     }
                  }

                  for (StorageUnitData cache : this.getOutputAbleDrawerData().values()) {
                     Iterator<NetworkRoot.BatchTake> iterator = pending.iterator();

                     while (iterator.hasNext()) {
                        NetworkRoot.BatchTake takex = iterator.next();
                        ItemStack takeStack = cache.requestItem(accessor, takex.request);
                        if (takeStack != null) {
                           addCountObservingAccessHistory(accessor, cache.getLastLocation());
                           if (takex.collected == null) {
                              takex.collected = takeStack.clone();
                           } else {
                              takex.collected.setAmount(takex.collected.getAmount() + takeStack.getAmount());
                           }

                           takex.request.receiveAmount(takeStack.getAmount());
                           if (takex.request.getAmount() <= 0) {
                              this.uncontrolAccessOutput(accessor);
                              this.tryRecord(accessor, takex.request);
                              results.set(takex.index, takex.collected);
                              iterator.remove();
                           }
                        }
                     }

                     if (pending.isEmpty()) {
                        return results;
                     }
                  }

                  Iterator<NetworkRoot.BatchTake> iterator = pending.iterator();

                  while (iterator.hasNext()) {
                     NetworkRoot.BatchTake takex = iterator.next();
                     ItemStack takeStack = CellDrive.getStorage().takeItem(this.driveCache, this.getOutputAbleCellDriveMenus(), takex.request);
                     if (takeStack != null) {
                        if (takex.collected == null) {
                           takex.collected = takeStack.clone();
                        } else {
                           takex.collected.setAmount(takex.collected.getAmount() + takeStack.getAmount());
                        }

                        takex.request.receiveAmount(takeStack.getAmount());
                        if (takex.request.getAmount() <= 0) {
                           this.tryRecord(accessor, takex.request);
                           results.set(takex.index, takex.collected);
                           iterator.remove();
                        }
                     }
                  }

                  if (pending.isEmpty()) {
                     return results;
                  } else {
                     for (BlockMenu blockMenu : this.getCellMenus()) {
                        if (!isFakeCell(blockMenu)) {
                           for (int slot : CELL_AVAILABLE_SLOTS) {
                              ItemStack itemStack = blockMenu.getItemInSlot(slot);
                              if (itemStack != null && itemStack.getType() != Material.AIR) {
                                 Iterator<NetworkRoot.BatchTake> iteratorx = pending.iterator();

                                 while (iteratorx.hasNext()) {
                                    NetworkRoot.BatchTake takex = iteratorx.next();
                                    if (StackUtils.itemsMatch(takex.request, itemStack)) {
                                       blockMenu.markDirty();
                                       if (takex.collected == null) {
                                          takex.collected = itemStack.clone();
                                          takex.collected.setAmount(0);
                                       }

                                       if (takex.request.getAmount() <= itemStack.getAmount()) {
                                          this.uncontrolAccessOutput(accessor);
                                          takex.collected.setAmount(takex.collected.getAmount() + takex.request.getAmount());
                                          itemStack.setAmount(itemStack.getAmount() - takex.request.getAmount());
                                          this.tryRecord(accessor, takex.request);
                                          results.set(takex.index, takex.collected);
                                          iteratorx.remove();
                                       } else {
                                          takex.collected.setAmount(takex.collected.getAmount() + itemStack.getAmount());
                                          takex.request.receiveAmount(itemStack.getAmount());
                                          itemStack.setAmount(0);
                                       }
                                    }
                                 }
                              }
                           }

                           if (pending.isEmpty()) {
                              return results;
                           }
                        }
                     }

                     for (BlockMenu blockMenux : this.getCrafterOutputs()) {
                        int[] slots = blockMenux.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);

                        for (int slotx : slots) {
                           ItemStack itemStack = blockMenux.getItemInSlot(slotx);
                           if (itemStack != null && itemStack.getType() != Material.AIR) {
                              Iterator<NetworkRoot.BatchTake> iteratorx = pending.iterator();

                              while (iteratorx.hasNext()) {
                                 NetworkRoot.BatchTake takex = iteratorx.next();
                                 if (StackUtils.itemsMatch(takex.request, itemStack)) {
                                    if (takex.collected == null) {
                                       takex.collected = itemStack.clone();
                                       takex.collected.setAmount(0);
                                    }

                                    if (takex.request.getAmount() <= itemStack.getAmount()) {
                                       this.uncontrolAccessOutput(accessor);
                                       takex.collected.setAmount(takex.collected.getAmount() + takex.request.getAmount());
                                       itemStack.setAmount(itemStack.getAmount() - takex.request.getAmount());
                                       this.tryRecord(accessor, takex.request);
                                       results.set(takex.index, takex.collected);
                                       iteratorx.remove();
                                    } else {
                                       takex.collected.setAmount(takex.collected.getAmount() + itemStack.getAmount());
                                       takex.request.receiveAmount(itemStack.getAmount());
                                       itemStack.setAmount(0);
                                    }
                                 }
                              }
                           }
                        }

                        if (pending.isEmpty()) {
                           return results;
                        }
                     }

                     for (BlockMenu blockMenux : this.getAdvancedGreedyBlockMenus()) {
                        int[] slots = blockMenux.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);

                        for (int slotxx : slots) {
                           ItemStack itemStack = blockMenux.getItemInSlot(slotxx);
                           if (itemStack != null && itemStack.getType() != Material.AIR) {
                              Iterator<NetworkRoot.BatchTake> iteratorx = pending.iterator();

                              while (iteratorx.hasNext()) {
                                 NetworkRoot.BatchTake takex = iteratorx.next();
                                 if (StackUtils.itemsMatch(takex.request, itemStack)) {
                                    if (takex.collected == null) {
                                       takex.collected = itemStack.clone();
                                       takex.collected.setAmount(0);
                                    }

                                    if (takex.request.getAmount() <= itemStack.getAmount()) {
                                       this.uncontrolAccessOutput(accessor);
                                       takex.collected.setAmount(takex.collected.getAmount() + takex.request.getAmount());
                                       itemStack.setAmount(itemStack.getAmount() - takex.request.getAmount());
                                       this.tryRecord(accessor, takex.request);
                                       results.set(takex.index, takex.collected);
                                       iteratorx.remove();
                                    } else {
                                       takex.collected.setAmount(takex.collected.getAmount() + itemStack.getAmount());
                                       takex.request.receiveAmount(itemStack.getAmount());
                                       itemStack.setAmount(0);
                                    }
                                 }
                              }
                           }
                        }

                        if (pending.isEmpty()) {
                           return results;
                        }
                     }

                     for (BlockMenu blockMenux : this.getGreedyBlockMenus()) {
                        int[] slots = blockMenux.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
                        ItemStack itemStack = blockMenux.getItemInSlot(slots[0]);
                        if (itemStack != null && itemStack.getType() != Material.AIR) {
                           Iterator<NetworkRoot.BatchTake> iteratorx = pending.iterator();

                           while (iteratorx.hasNext()) {
                              NetworkRoot.BatchTake takex = iteratorx.next();
                              if (StackUtils.itemsMatch(takex.request, itemStack)) {
                                 blockMenux.markDirty();
                                 if (takex.collected == null) {
                                    takex.collected = itemStack.clone();
                                    takex.collected.setAmount(0);
                                 }

                                 if (takex.request.getAmount() <= itemStack.getAmount()) {
                                    this.uncontrolAccessOutput(accessor);
                                    takex.collected.setAmount(takex.collected.getAmount() + takex.request.getAmount());
                                    itemStack.setAmount(itemStack.getAmount() - takex.request.getAmount());
                                    this.tryRecord(accessor, takex.request);
                                    results.set(takex.index, takex.collected);
                                    iteratorx.remove();
                                 } else {
                                    takex.collected.setAmount(takex.collected.getAmount() + itemStack.getAmount());
                                    takex.request.receiveAmount(itemStack.getAmount());
                                    itemStack.setAmount(0);
                                 }
                              }
                           }
                        }
                     }

                     for (NetworkRoot.BatchTake takex : pending) {
                        if (takex.collected != null && takex.collected.getAmount() != 0) {
                           this.uncontrolAccessOutput(accessor);
                           this.tryRecord(accessor, takex.request);
                           results.set(takex.index, takex.collected);
                        }
                     }

                     return results;
                  }
               }
            }
         }
      }
   }

   public void addItem(@Nullable Location accessor, ItemStack incoming) {
      this.addItemStack(accessor, incoming);
   }

   public void tryRecord(@Nullable Location accessor, @Nullable ItemStack before, int after) {
      if (accessor != null) {
         if (this.recordFlow && this.itemFlowRecord != null && before != null) {
            this.itemFlowRecord.addAction(accessor, before, after);
         }
      }
   }

   public void addItemStack(@Nullable Location accessor, ItemStack incoming) {
      if (!this.allowAccessInput(accessor)) {
         FeedbackSendable.sendFeedback0(accessor, FeedbackType.ROOT_LIMITING_ACCESS_INPUT);
      } else if (!StackUtils.isBlacklisted(incoming)) {
         this.bumpWriteEpoch();
         ItemStack beforeItemStack = null;
         if (this.recordFlow && this.itemFlowRecord != null) {
            beforeItemStack = incoming.clone();
         }

         int before = incoming.getAmount();
         Map<Location, Integer> m = getPersistentAccessHistory(accessor);
         if (m != null) {
            boolean found = false;
            List<Location> misses = new ArrayList<>();

            for (Entry<Location, Integer> entry : m.entrySet()) {
               BarrelIdentity barrelIdentity = this.accessInputAbleBarrel(entry.getKey());
               if (barrelIdentity != null) {
                  if (StackUtils.itemsMatch(barrelIdentity, incoming)) {
                     minusCacheMiss(accessor, entry.getKey());
                     found = true;
                     barrelIdentity.depositItemStack(incoming);
                     if (incoming.getAmount() == 0) {
                        this.uncontrolAccessInput(accessor);
                        this.tryRecord(accessor, beforeItemStack, 0);
                        return;
                     }
                  } else {
                     misses.add(entry.getKey());
                  }
               } else {
                  StorageUnitData data = this.accessInputAbleCargoStorageUnitData(entry.getKey());
                  if (data != null) {
                     int before2 = incoming.getAmount();
                     data.depositItemStack(accessor, incoming, true);
                     if (incoming.getAmount() != before2) {
                        minusCacheMiss(accessor, entry.getKey());
                        found = true;
                     } else {
                        misses.add(entry.getKey());
                     }

                     if (incoming.getAmount() == 0) {
                        this.uncontrolAccessInput(accessor);
                        this.tryRecord(accessor, beforeItemStack, 0);
                        return;
                     }
                  }
               }
            }

            if (!found) {
               for (Location miss : misses) {
                  addCacheMiss(accessor, miss);
               }
            }
         }

         for (BlockMenu blockMenu : this.getAdvancedGreedyBlockMenus()) {
            ItemStack template = blockMenu.getItemInSlot(10);
            if (StackUtils.itemsMatch(incoming, template)) {
               blockMenu.markDirty();
               BlockMenuUtil.pushItem(blockMenu, incoming, ADVANCED_GREEDY_BLOCK_AVAILABLE_SLOTS);
               this.uncontrolAccessInput(accessor);
               this.tryRecord(accessor, beforeItemStack, incoming.getAmount());
               return;
            }
         }

         for (BlockMenu blockMenux : this.getGreedyBlockMenus()) {
            ItemStack template = blockMenux.getItemInSlot(10);
            if (StackUtils.itemsMatch(incoming, template)) {
               blockMenux.markDirty();
               BlockMenuUtil.pushItem(blockMenux, incoming, 16);
               this.uncontrolAccessInput(accessor);
               this.tryRecord(accessor, beforeItemStack, incoming.getAmount());
               return;
            }
         }

         for (BarrelIdentity barrelIdentity : this.getInputAbleBarrels()) {
            if (StackUtils.itemsMatch(barrelIdentity, incoming)) {
               addCountObservingAccessHistory(accessor, barrelIdentity.getLocation());
               barrelIdentity.depositItemStack(incoming);
               if (incoming.getAmount() == 0) {
                  this.uncontrolAccessInput(accessor);
                  this.tryRecord(accessor, beforeItemStack, 0);
                  return;
               }
            }
         }

         for (StorageUnitData cache : this.getMapInputAbleCargoStorageUnits().values()) {
            int before2x = incoming.getAmount();
            cache.depositItemStack(accessor, incoming, true);
            if (incoming.getAmount() != before2x) {
               this.uncontrolAccessInput(accessor);
               addCountObservingAccessHistory(accessor, cache.getLastLocation());
            }

            if (incoming.getAmount() == 0) {
               this.uncontrolAccessInput(accessor);
               this.tryRecord(accessor, beforeItemStack, 0);
               return;
            }
         }

         long cellRemaining = CellDrive.getStorage().pushSingle(this.driveCache, this.getInputAbleCellDriveMenus(), incoming, (long)incoming.getAmount());
         incoming.setAmount((int)Math.min(cellRemaining, 2147483647L));
         if (incoming.getAmount() == 0) {
            this.tryRecord(accessor, beforeItemStack, 0);
         } else {
            for (BlockMenu blockMenuxx : this.getCellMenus()) {
               if (!isFakeCell(blockMenuxx)) {
                  blockMenuxx.markDirty();
                  BlockMenuUtil.pushItem(blockMenuxx, incoming, CELL_AVAILABLE_SLOTS);
                  if (incoming.getAmount() == 0) {
                     this.uncontrolAccessInput(accessor);
                     this.tryRecord(accessor, beforeItemStack, 0);
                     return;
                  }
               }
            }

            if (before == incoming.getAmount()) {
               this.addTransportInputMiss(accessor);
            } else {
               this.uncontrolAccessInput(accessor);
            }

            this.tryRecord(accessor, beforeItemStack, incoming.getAmount());
         }
      }
   }

   public void addItemStacks(@Nullable Location accessor, List<ItemStack> incomings) {
      if (!incomings.isEmpty()) {
         for (ItemStack item : incomings) {
            this.addItemStack(accessor, item);
         }
      }
   }

   public Map<Location, BarrelIdentity> getMapInputAbleBarrels() {
      if (this.mapInputAbleBarrels != null) {
         return this.mapInputAbleBarrels;
      } else {
         Map<Location, BarrelIdentity> map = new ConcurrentHashMap<>();

         for (BarrelIdentity barrel : this.getInputAbleBarrels()) {
            map.put(barrel.getLocation(), barrel);
         }

         this.mapInputAbleBarrels = map;
         return map;
      }
   }

   public Map<Location, BarrelIdentity> getMapOutputAbleBarrels() {
      if (this.mapOutputAbleBarrels != null) {
         return this.mapOutputAbleBarrels;
      } else {
         Map<Location, BarrelIdentity> map = new ConcurrentHashMap<>();

         for (BarrelIdentity barrel : this.getOutputAbleBarrels()) {
            map.put(barrel.getLocation(), barrel);
         }

         this.mapOutputAbleBarrels = map;
         return map;
      }
   }

   public Map<Location, StorageUnitData> getMapInputAbleCargoStorageUnits() {
      return this.getInputAbleDrawerData();
   }

   public Map<Location, StorageUnitData> getMapOutputAbleCargoStorageUnits() {
      return this.getOutputAbleDrawerData();
   }

   public boolean allowAccessInput(@Nullable Location accessor) {
      if (accessor == null) {
         return true;
      } else {
         Long lastTime = controlledAccessInputHistory.get(accessor);
         return lastTime == null ? true : System.currentTimeMillis() - lastTime > (long)reduceMs;
      }
   }

   public boolean allowAccessOutput(@Nullable Location accessor) {
      if (accessor == null) {
         return true;
      } else {
         Long lastTime = controlledAccessOutputHistory.get(accessor);
         return lastTime == null ? true : System.currentTimeMillis() - lastTime > (long)reduceMs;
      }
   }

   public void addTransportInputMiss(@Nullable Location accessor) {
      if (accessor != null) {
         transportMissInputHistory.merge(accessor, 1, (a, b) -> {
            if (a + b > transportMissThreshold) {
               this.controlAccessInput(accessor);
               return transportMissThreshold;
            } else {
               return a + b;
            }
         });
      }
   }

   public void addTransportOutputMiss(@Nullable Location accessor) {
      if (accessor != null) {
         transportMissOutputHistory.merge(accessor, 1, (a, b) -> {
            if (a + b > transportMissThreshold) {
               this.controlAccessOutput(accessor);
               return transportMissThreshold;
            } else {
               return a + b;
            }
         });
      }
   }

   public void reduceTransportInputMiss(@Nullable Location accessor) {
      if (accessor != null) {
         transportMissInputHistory.merge(accessor, -1, (a, b) -> Math.max(a + b, 0));
      }
   }

   public void reduceTransportOutputMiss(@Nullable Location accessor) {
      if (accessor != null) {
         transportMissOutputHistory.merge(accessor, -1, (a, b) -> Math.max(a + b, 0));
      }
   }

   public void controlAccessInput(@Nullable Location accessor) {
      if (accessor != null) {
         controlledAccessInputHistory.put(accessor, System.currentTimeMillis());
      }
   }

   public void controlAccessOutput(@Nullable Location accessor) {
      if (accessor != null) {
         controlledAccessOutputHistory.put(accessor, System.currentTimeMillis());
      }
   }

   public void uncontrolAccessInput(@Nullable Location accessor) {
      if (accessor != null) {
         controlledAccessInputHistory.remove(accessor);
         this.reduceTransportInputMiss(accessor);
      }
   }

   public void uncontrolAccessOutput(@Nullable Location accessor) {
      if (accessor != null) {
         controlledAccessOutputHistory.remove(accessor);
         this.reduceTransportOutputMiss(accessor);
      }
   }

   public int getCellsSize() {
      return this.getNodes(NodeType.CELL).size();
   }

   public static boolean isFakeCell(BlockMenu menu) {
      return !isRealCell(menu.getLocation());
   }

   public static boolean isRealCell(BlockMenu menu) {
      return isRealCell(menu.getLocation());
   }

   public static boolean isFakeCell(Location location) {
      return !isRealCell(location);
   }

   public static boolean isRealCell(Location location) {
      return StorageCacheUtils.getSfItem(location) instanceof NetworkCell;
   }

   private <K, V> HashMap<V, K> reverseMap(Map<K, V> map) {
      HashMap<V, K> newMap = new HashMap<>();

      for (Entry<K, V> e : map.entrySet()) {
         newMap.put(e.getValue(), e.getKey());
      }

      return newMap;
   }

   @Deprecated
   @Nullable
   public ItemStack getItemStack(ItemRequest request) {
      return this.getItemStack(null, request);
   }

   @Deprecated
   public void addItemStack(ItemStack incoming) {
      this.addItemStack(null, incoming);
   }

   @Deprecated
   public Map<StorageUnitData, Location> getCargoStorageUnitDatas() {
      return this.reverseMap(this.getDrawerData());
   }

   @Deprecated
   public Map<StorageUnitData, Location> getInputAbleCargoStorageUnitDatas() {
      return this.reverseMap(this.getInputAbleDrawerData());
   }

   @Deprecated
   public Map<StorageUnitData, Location> getOutputAbleCargoStorageUnitDatas() {
      return this.reverseMap(this.getOutputAbleDrawerData());
   }

   @Deprecated
   public List<ItemStack> getItemStacks(List<ItemRequest> itemRequests) {
      return this.getItemStacks(null, itemRequests);
   }

   @Deprecated
   public List<ItemStack> getItemStacks0(@Nullable Location accesor, List<ItemRequest> itemRequests) {
      return this.getItemStacks(accesor, itemRequests);
   }

   @Deprecated
   @Nullable
   public ItemStack getItemStack0(@Nullable Location accessor, ItemRequest request) {
      return this.getItemStack(accessor, request, null);
   }

   @Deprecated
   @Nullable
   public ItemStack getItemStack0(@Nullable Location accessor, ItemRequest request, @Nullable MatchOption option) {
      return this.getItemStack(accessor, request, option);
   }

   @Deprecated
   @Nullable
   public ItemStack requestItem(ItemRequest request) {
      return this.getItemStack(null, request);
   }

   @Deprecated
   @Nullable
   public ItemStack requestItem(ItemStack itemStack) {
      return this.requestItem(null, new ItemRequest(itemStack, itemStack.getAmount()));
   }

   @Deprecated
   public void addItemStack0(@Nullable Location accessor, ItemStack incoming) {
      this.addItemStack(accessor, incoming);
   }

   @Deprecated
   public void addItemStacks0(@Nullable Location accessor, List<ItemStack> incomings) {
      this.addItemStacks(accessor, incomings);
   }

   @Deprecated
   public List<ItemStack> getItemStacksBatch0(Location accessor, List<ItemRequest> requests) {
      return this.getItemStacksBatch(accessor, requests);
   }

   @Generated
   public DriveCache getDriveCache() {
      return this.driveCache;
   }

   @Generated
   public EnumMap<NodeType, NetworkRoot.NodeIndex> getIndexes() {
      return this.indexes;
   }

   @Generated
   public boolean isDisplayParticles() {
      return this.displayParticles;
   }

   @Generated
   public static Map<Location, Map<Location, Integer>> getObservingAccessHistory() {
      return observingAccessHistory;
   }

   @Generated
   public static Map<Location, Map<Location, Integer>> getPersistentAccessHistory() {
      return persistentAccessHistory;
   }

   @Generated
   public static Map<Location, Integer> getTransportMissInputHistory() {
      return transportMissInputHistory;
   }

   @Generated
   public static Map<Location, Integer> getTransportMissOutputHistory() {
      return transportMissOutputHistory;
   }

   @Generated
   public static Map<Location, Long> getControlledAccessInputHistory() {
      return controlledAccessInputHistory;
   }

   @Generated
   public static Map<Location, Long> getControlledAccessOutputHistory() {
      return controlledAccessOutputHistory;
   }

   @Generated
   public Set<Location> getNodeLocations() {
      return this.nodeLocations;
   }

   @Generated
   public int getMaxNodes() {
      return this.maxNodes;
   }

   @Generated
   public boolean isRecordFlow() {
      return this.recordFlow;
   }

   @Nullable
   @Generated
   public ItemFlowRecord getItemFlowRecord() {
      return this.itemFlowRecord;
   }

   @Generated
   public Location getController() {
      return this.controller;
   }

   @Generated
   public boolean isOverburdened() {
      return this.isOverburdened;
   }

   @Generated
   public void setDisplayParticles(boolean displayParticles) {
      this.displayParticles = displayParticles;
   }

   private static final class BatchTake {
      private final ItemRequest request;
      private final int index;
      @Nullable
      private ItemStack collected;
      private boolean historyFound;
      private final List<Location> historyMisses = new ArrayList<>();

      private BatchTake(ItemRequest request, int index) {
         this.request = request;
         this.index = index;
      }
   }

   private static final class ItemSnapshot {
      private final Map<ItemKey, Long> keyed;
      private final long invalidationEpoch;
      private final long writeEpoch;
      private final int tick;
      private final long publishedAtMs;
      @Nullable
      private volatile Map<ItemStack, Long> exported;

      private long invalidationEpoch() {
         return this.invalidationEpoch;
      }

      private long writeEpoch() {
         return this.writeEpoch;
      }

      private int tick() {
         return this.tick;
      }

      private long publishedAtMs() {
         return this.publishedAtMs;
      }

      private Map<ItemKey, Long> keyed() {
         return this.keyed;
      }

      private Map<ItemStack, Long> items() {
         Map<ItemStack, Long> items = this.exported;
         if (items != null) {
            return items;
         } else {
            synchronized (this) {
               items = this.exported;
               if (items == null) {
                  items = new HashMap<>(this.keyed.size() * 2);

                  for (Entry<ItemKey, Long> entry : this.keyed.entrySet()) {
                     items.put(entry.getKey().getItemStack(), entry.getValue());
                  }

                  this.exported = items;
               }

               return items;
            }
         }
      }

      @Generated
      public ItemSnapshot(Map<ItemKey, Long> keyed, long invalidationEpoch, long writeEpoch, int tick, long publishedAtMs) {
         this.keyed = keyed;
         this.invalidationEpoch = invalidationEpoch;
         this.writeEpoch = writeEpoch;
         this.tick = tick;
         this.publishedAtMs = publishedAtMs;
      }
   }

   public static class NodeIndex {
      private final Set<Location> locations = new HashSet<>();
      @Nullable
      private Set<BlockMenu> blockMenus = null;

      public void addLocation(Location location) {
         this.locations.add(location);
      }

      public Set<BlockMenu> getMenus() {
         if (this.blockMenus != null) {
            return this.blockMenus;
         } else {
            Set<BlockMenu> menus = new HashSet<>();

            for (Location loc : this.locations) {
               BlockMenu menu = StorageCacheUtils.getMenu(loc);
               if (menu != null) {
                  menus.add(menu);
               }
            }

            this.blockMenus = menus;
            return menus;
         }
      }
   }
}
