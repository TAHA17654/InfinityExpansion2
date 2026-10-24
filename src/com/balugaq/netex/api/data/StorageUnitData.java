package com.balugaq.netex.api.data;

import com.balugaq.netex.api.enums.StorageUnitType;
import com.ytdd9527.networksexpansion.implementation.machines.unit.NetworksDrawer;
import com.ytdd9527.networksexpansion.utils.databases.DataStorage;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import io.github.sefiraat.networks.utils.StackUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class StorageUnitData {
   public static final Map<Location, Map<Integer, Integer>> observingAccessHistory = new ConcurrentHashMap<>();
   public static final Map<Location, Map<Integer, Integer>> persistentAccessHistory = new ConcurrentHashMap<>();
   private final int id;
   private final OfflinePlayer owner;
   private final ConcurrentHashMap<Integer, ItemContainer> storedItems;
   private boolean isPlaced;
   private StorageUnitType sizeType;
   private Location lastLocation;

   public StorageUnitData(int id, String ownerUUID, StorageUnitType sizeType, boolean isPlaced, Location lastLocation) {
      this(id, Bukkit.getOfflinePlayer(UUID.fromString(ownerUUID)), sizeType, isPlaced, lastLocation, new ConcurrentHashMap<>());
   }

   public StorageUnitData(
      int id, String ownerUUID, StorageUnitType sizeType, boolean isPlaced, Location lastLocation, ConcurrentHashMap<Integer, ItemContainer> storedItems
   ) {
      this(id, Bukkit.getOfflinePlayer(UUID.fromString(ownerUUID)), sizeType, isPlaced, lastLocation, storedItems);
   }

   public StorageUnitData(
      int id, OfflinePlayer owner, StorageUnitType sizeType, boolean isPlaced, Location lastLocation, ConcurrentHashMap<Integer, ItemContainer> storedItems
   ) {
      this.id = id;
      this.owner = owner;
      this.sizeType = sizeType;
      this.isPlaced = isPlaced;
      this.lastLocation = lastLocation;
      this.storedItems = storedItems;
   }

   public static void addPersistentAccessHistory(@Nullable Location accessor, int accessLocation) {
      if (accessor != null) {
         Map<Integer, Integer> locations = persistentAccessHistory.getOrDefault(accessor, new ConcurrentHashMap<>());
         locations.put(accessLocation, 0);
         persistentAccessHistory.put(accessor, locations);
      }
   }

   public static void addCacheMiss(@Nullable Location accessor, int accessLocation) {
      if (accessor != null) {
         Map<Integer, Integer> locations = persistentAccessHistory.getOrDefault(accessor, new ConcurrentHashMap<>());
         int value = locations.getOrDefault(accessLocation, 0) + 1;
         if (value > NetworkRoot.cacheMissThreshold) {
            removePersistentAccessHistory(accessor, accessLocation);
         } else {
            locations.put(accessLocation, value);
            persistentAccessHistory.put(accessor, locations);
         }
      }
   }

   public static void minusCacheMiss(Location accessor, int accessLocation) {
      Map<Integer, Integer> locations = persistentAccessHistory.getOrDefault(accessor, new ConcurrentHashMap<>());
      int value = Math.max(locations.getOrDefault(accessLocation, 0) - 1, 0);
      locations.put(accessLocation, value);
   }

   @Nullable
   public static Map<Integer, Integer> getPersistentAccessHistory(@Nullable Location accessor) {
      return accessor == null ? null : persistentAccessHistory.getOrDefault(accessor, new ConcurrentHashMap<>());
   }

   public static void removePersistentAccessHistory(Location accessor) {
      persistentAccessHistory.remove(accessor);
   }

   public static void removePersistentAccessHistory(@Nullable Location accessor, int accessLocation) {
      if (accessor != null) {
         Map<Integer, Integer> locations = persistentAccessHistory.getOrDefault(accessor, new ConcurrentHashMap<>());
         locations.remove(accessLocation);
         persistentAccessHistory.put(accessor, locations);
      }
   }

   public static void addCountObservingAccessHistory(@Nullable Location accessor, int accessLocation) {
      if (accessor != null) {
         Map<Integer, Integer> locations = observingAccessHistory.getOrDefault(accessor, new HashMap<>());
         int count = locations.getOrDefault(accessLocation, 0);
         if (count >= NetworkRoot.persistentThreshold) {
            removeCountObservingAccessHistory(accessor, accessLocation);
            addPersistentAccessHistory(accessor, accessLocation);
         } else {
            locations.put(accessLocation, count + 1);
            observingAccessHistory.put(accessor, locations);
         }
      }
   }

   public static Map<Integer, Integer> getCountObservingAccessHistory(Location location) {
      return observingAccessHistory.getOrDefault(location, new ConcurrentHashMap<>());
   }

   public static void removeCountObservingAccessHistory(Location location) {
      observingAccessHistory.remove(location);
   }

   public static void removeCountObservingAccessHistory(Location location, Integer accessLocation) {
      Map<Integer, Integer> locations = observingAccessHistory.getOrDefault(location, new ConcurrentHashMap<>());
      locations.remove(accessLocation);
      observingAccessHistory.put(location, locations);
   }

   public static boolean isBlacklisted(ItemStack itemStack) {
      return StackUtils.isBlacklisted(itemStack);
   }

   public synchronized void setPlaced(boolean isPlaced) {
      if (this.isPlaced != isPlaced) {
         this.isPlaced = isPlaced;
         DataStorage.setContainerStatus(this.id, isPlaced);
      }
   }

   public synchronized void setSizeType(StorageUnitType sizeType) {
      if (this.sizeType != sizeType) {
         this.sizeType = sizeType;
         DataStorage.setContainerSizeType(this.id, sizeType);
      }
   }

   public synchronized void setLastLocation(Location lastLocation) {
      if (this.lastLocation != lastLocation) {
         this.lastLocation = lastLocation;
         DataStorage.setContainerLocation(this.id, lastLocation);
      }
   }

   public void removeItem(int itemId) {
      if (this.storedItems.remove(itemId) != null) {
         DataStorage.deleteStoredItem(this.id, itemId);
      }
   }

   public synchronized void setItemAmount(int itemId, int amount) {
      if (amount < 0) {
         this.removeItem(itemId);
      } else {
         ItemContainer container = this.storedItems.get(itemId);
         if (container != null) {
            container.setAmount(amount);
            DataStorage.setStoredAmount(this.id, itemId, amount);
         }
      }
   }

   public synchronized void removeAmount(int itemId, int amount) {
      ItemContainer container = this.storedItems.get(itemId);
      if (container != null) {
         container.removeAmount(amount);
         if (container.getAmount() <= 0 && !NetworksDrawer.isLocked(this.getLastLocation())) {
            this.removeItem(itemId);
            return;
         }

         DataStorage.setStoredAmount(this.id, itemId, container.getAmount());
      }
   }

   public int getStoredTypeCount() {
      return this.storedItems.size();
   }

   public int getTotalAmount() {
      int re = 0;

      for (ItemContainer each : this.storedItems.values()) {
         re += each.getAmount();
      }

      return re;
   }

   public long getTotalAmountLong() {
      long re = 0L;

      for (ItemContainer each : this.storedItems.values()) {
         re += (long)each.getAmount();
      }

      return re;
   }

   @Deprecated
   public List<ItemContainer> getStoredItems() {
      return this.copyStoredItems();
   }

   public List<ItemContainer> copyStoredItems() {
      return new ArrayList<>(this.storedItems.values());
   }

   public Collection<ItemContainer> getStoredItemsDirectly() {
      return this.storedItems.values();
   }

   public Map<Integer, ItemContainer> copyStoredItemsMap() {
      return new ConcurrentHashMap<>(this.storedItems);
   }

   public Map<Integer, ItemContainer> getStoredItemsMap() {
      return this.storedItems;
   }

   @Nullable
   public synchronized ItemStack requestItem(@Nullable Location accessor, ItemRequest itemRequest) {
      return this.requestItem(accessor, itemRequest, true);
   }

   @Nullable
   public synchronized ItemStack requestItem(@Nullable Location accessor, ItemRequest itemRequest, boolean contentLocked) {
      ItemStack item = itemRequest.getItemStack();
      if (item == null) {
         return null;
      } else {
         int amount = itemRequest.getAmount();
         List<ItemContainer> stored = this.getStoredItems();
         Map<Integer, Integer> m = getPersistentAccessHistory(accessor);
         if (m != null) {
            boolean found = false;
            List<Integer> misses = new ArrayList<>();

            for (Integer i : m.keySet()) {
               if (i >= stored.size()) {
                  removePersistentAccessHistory(accessor, i);
               } else {
                  ItemContainer itemContainer = stored.get(i);
                  int containerAmount = itemContainer.getAmount();
                  if (StackUtils.itemsMatch(itemRequest, itemContainer.getSampleDirectly())) {
                     int take = Math.min(amount, containerAmount);
                     if (take > 0) {
                        itemContainer.removeAmount(take);
                        DataStorage.setStoredAmount(this.id, itemContainer.getId(), itemContainer.getAmount());
                        minusCacheMiss(accessor, i);
                        found = true;
                        ItemStack clone = item.clone();
                        clone.setAmount(take);
                        return clone;
                     }

                     if (!contentLocked) {
                        this.removeItem(itemContainer.getId());
                        removePersistentAccessHistory(accessor, i);
                     }
                     break;
                  }

                  misses.add(i);
               }
            }

            if (!found) {
               for (int miss : misses) {
                  minusCacheMiss(accessor, miss);
               }
            }
         }

         for (int ix = 0; ix < stored.size(); ix++) {
            ItemContainer itemContainer = stored.get(ix);
            int containerAmount = itemContainer.getAmount();
            if (StackUtils.itemsMatch(itemRequest, itemContainer.getSampleDirectly())) {
               int takex = Math.min(amount, containerAmount);
               if (takex > 0) {
                  itemContainer.removeAmount(takex);
                  addCountObservingAccessHistory(accessor, ix);
                  DataStorage.setStoredAmount(this.id, itemContainer.getId(), itemContainer.getAmount());
                  ItemStack clone = item.clone();
                  clone.setAmount(takex);
                  return clone;
               }

               if (!contentLocked) {
                  this.removeItem(itemContainer.getId());
               }
               break;
            }
         }

         return null;
      }
   }

   public synchronized void depositItemStacks(@Nullable Location accessor, Map<ItemStack, Long> itemsToDeposit, boolean contentLocked) {
      for (Entry<ItemStack, Long> entry : itemsToDeposit.entrySet()) {
         if (entry.getValue() > 2147483647L) {
            long before = entry.getValue();
            ItemStack item = StackUtils.getAsQuantity(entry.getKey(), Integer.MAX_VALUE);
            this.depositItemStack(accessor, item, contentLocked);
            long leftover = (long)item.getAmount();
            entry.setValue(before - 2147483647L + leftover);
         } else {
            ItemStack item = StackUtils.getAsQuantity(entry.getKey(), Math.toIntExact(entry.getValue()));
            this.depositItemStack(accessor, item, contentLocked);
            long rest = (long)item.getAmount();
            entry.setValue(rest);
         }
      }
   }

   public synchronized void depositItemStack(@Nullable Location accessor, Entry<ItemStack, Integer> entry, boolean contentLocked) {
      ItemStack item = StackUtils.getAsQuantity(entry.getKey(), entry.getValue());
      this.depositItemStack(accessor, item, contentLocked);
      int leftover = item.getAmount();
      entry.setValue(leftover);
   }

   public synchronized void depositItemStack(@Nullable Location accessor, Map<ItemStack, Integer> itemsToDeposit, boolean contentLocked) {
      for (Entry<ItemStack, Integer> entry : itemsToDeposit.entrySet()) {
         this.depositItemStack(accessor, entry, contentLocked);
      }
   }

   public void depositItemStack(@Nullable Location accessor, ItemStack[] itemsToDeposit, boolean contentLocked) {
      for (ItemStack item : itemsToDeposit) {
         this.depositItemStack(accessor, item, contentLocked);
      }
   }

   public synchronized void depositItemStack(@Nullable Location accessor, @Nullable ItemStack itemsToDeposit, boolean contentLocked, boolean force) {
      if (itemsToDeposit != null && !isBlacklisted(itemsToDeposit)) {
         int actualAdded = this.addStoredItem(accessor, itemsToDeposit, itemsToDeposit.getAmount(), contentLocked, force);
         if (actualAdded > 0) {
            itemsToDeposit.setAmount(itemsToDeposit.getAmount() - actualAdded);
         }
      }
   }

   public void depositItemStack(@Nullable Location accessor, ItemStack item, boolean contentLocked) {
      this.depositItemStack(accessor, item, contentLocked, false);
   }

   public synchronized int addStoredItem(@Nullable Location accessor, ItemStack item, int amount, boolean contentLocked, boolean force) {
      int add = 0;
      boolean isVoidExcess = NetworksDrawer.isVoidExcess(this.getLastLocation());
      List<ItemContainer> stored = this.getStoredItems();
      Map<Integer, Integer> m = getPersistentAccessHistory(accessor);
      if (m != null) {
         boolean found = false;
         List<Integer> misses = new ArrayList<>();

         for (Integer i : m.keySet()) {
            if (i >= stored.size()) {
               removePersistentAccessHistory(accessor, i);
            } else {
               ItemContainer each = stored.get(i);
               if (each.isSimilar(item)) {
                  int raw = this.sizeType.getEachMaxSize() - each.getAmount();
                  if (raw < 0) {
                     each.setAmount(this.sizeType.getEachMaxSize());
                     return 0;
                  }

                  add = Math.max(0, Math.min(amount, raw));
                  if (isVoidExcess) {
                     if (add > 0) {
                        each.addAmount(add);
                        DataStorage.setStoredAmount(this.id, each.getId(), each.getAmount());
                     } else {
                        item.setAmount(0);
                     }
                  } else {
                     each.addAmount(add);
                     DataStorage.setStoredAmount(this.id, each.getId(), each.getAmount());
                  }

                  minusCacheMiss(accessor, i);
                  found = true;
                  return add;
               }

               misses.add(i);
            }
         }

         if (!found) {
            for (int ix : misses) {
               addCacheMiss(accessor, ix);
            }
         }
      }

      for (int ix = 0; ix < stored.size(); ix++) {
         ItemContainer each = stored.get(ix);
         if (each.isSimilar(item)) {
            int rawx = this.sizeType.getEachMaxSize() - each.getAmount();
            if (rawx < 0) {
               each.setAmount(this.sizeType.getEachMaxSize());
               return 0;
            }

            add = Math.max(0, Math.min(amount, rawx));
            if (isVoidExcess) {
               if (add > 0) {
                  each.addAmount(add);
                  DataStorage.setStoredAmount(this.id, each.getId(), each.getAmount());
               } else {
                  item.setAmount(0);
               }
            } else {
               each.addAmount(add);
               DataStorage.setStoredAmount(this.id, each.getId(), each.getAmount());
            }

            addCountObservingAccessHistory(accessor, ix);
            return add;
         }
      }

      if (force || !contentLocked && !NetworksDrawer.isLocked(this.getLastLocation())) {
         if (this.storedItems.size() < this.sizeType.getMaxItemCount()) {
            add = Math.min(amount, this.sizeType.getEachMaxSize());
            int itemId = DataStorage.getItemId(item);
            this.storedItems.put(itemId, new ItemContainer(itemId, item, add));
            DataStorage.addStoredItem(this.id, itemId, add);
            return add;
         } else {
            return add;
         }
      } else {
         return 0;
      }
   }

   public int addStoredItem(@Nullable Location accessor, ItemStack item, boolean contentLocked) {
      return this.addStoredItem(accessor, item, item.getAmount(), contentLocked, false);
   }

   public int addStoredItem(@Nullable Location accessor, ItemStack item, boolean contentLocked, boolean force) {
      return this.addStoredItem(accessor, item, item.getAmount(), contentLocked, force);
   }

   public int addStoredItem(@Nullable Location accessor, ItemStack item, int amount, boolean contentLocked) {
      return this.addStoredItem(accessor, item, amount, contentLocked, false);
   }

   @Deprecated
   public synchronized int addStoredItem0(@Nullable Location accessor, ItemStack item, int amount, boolean contentLocked, boolean force) {
      return this.addStoredItem(accessor, item, amount, contentLocked, force);
   }

   @Deprecated
   public int addStoredItem0(Location accessor, ItemStack item, boolean contentLocked) {
      return this.addStoredItem(accessor, item, contentLocked);
   }

   @Deprecated
   public int addStoredItem0(Location accessor, ItemStack item, boolean contentLocked, boolean force) {
      return this.addStoredItem(accessor, item, contentLocked, force);
   }

   @Deprecated
   public int addStoredItem0(Location accessor, ItemStack item, int amount, boolean contentLocked) {
      return this.addStoredItem(accessor, item, amount, contentLocked);
   }

   @Deprecated
   public int addStoredItem(ItemStack item, boolean contentLocked) {
      return this.addStoredItem(null, item, item.getAmount(), contentLocked, false);
   }

   @Deprecated
   public int addStoredItem(ItemStack item, boolean contentLocked, boolean force) {
      return this.addStoredItem(null, item, item.getAmount(), contentLocked, force);
   }

   @Deprecated
   public int addStoredItem(ItemStack item, int amount, boolean contentLocked) {
      return this.addStoredItem(null, item, amount, contentLocked, false);
   }

   @Deprecated
   public int addStoredItem0(ItemStack item, boolean contentLocked) {
      return this.addStoredItem(null, item, contentLocked);
   }

   @Deprecated
   public int addStoredItem0(ItemStack item, boolean contentLocked, boolean force) {
      return this.addStoredItem(null, item, contentLocked, force);
   }

   @Deprecated
   public int addStoredItem0(ItemStack item, int amount, boolean contentLocked) {
      return this.addStoredItem(null, item, amount, contentLocked);
   }

   @Deprecated
   @Nullable
   public synchronized ItemStack requestItem(ItemRequest itemRequest) {
      return this.requestItem(null, itemRequest);
   }

   @Deprecated
   @Nullable
   public synchronized ItemStack requestItem0(@Nullable Location accessor, ItemRequest itemRequest) {
      return this.requestItem(accessor, itemRequest);
   }

   @Deprecated
   @Nullable
   public synchronized ItemStack requestItem0(@Nullable Location accessor, ItemRequest itemRequest, boolean contentLocked) {
      return this.requestItem(accessor, itemRequest, contentLocked);
   }

   @Deprecated
   public synchronized void depositItemStacks0(@Nullable Location accessor, Map<ItemStack, Long> itemsToDeposit, boolean contentLocked) {
      this.depositItemStacks(accessor, itemsToDeposit, contentLocked);
   }

   @Deprecated
   public synchronized void depositItemStack0(@Nullable Location accessor, Entry<ItemStack, Integer> entry, boolean contentLocked) {
      this.depositItemStack(accessor, entry, contentLocked);
   }

   @Deprecated
   public synchronized void depositItemStack0(@Nullable Location accessor, Map<ItemStack, Integer> itemsToDeposit, boolean contentLocked) {
      this.depositItemStack(accessor, itemsToDeposit, contentLocked);
   }

   @Deprecated
   public void depositItemStack0(@Nullable Location accessor, ItemStack[] itemsToDeposit, boolean contentLocked) {
      this.depositItemStack(accessor, itemsToDeposit, contentLocked);
   }

   @Deprecated
   public synchronized void depositItemStack0(@Nullable Location accessor, @Nullable ItemStack itemsToDeposit, boolean contentLocked, boolean force) {
      this.depositItemStack(accessor, itemsToDeposit, contentLocked, force);
   }

   @Deprecated
   public void depositItemStack0(@Nullable Location accessor, ItemStack item, boolean contentLocked) {
      this.depositItemStack(accessor, item, contentLocked);
   }

   @Deprecated
   public void depositItemStack(@Nullable ItemStack item, boolean contentLocked) {
      this.depositItemStack(null, item, contentLocked, false);
   }

   @Deprecated
   public void depositItemStack0(@Nullable ItemStack item, boolean contentLocked) {
      this.depositItemStack(null, item, contentLocked, false);
   }

   @Generated
   @Override
   public String toString() {
      return "StorageUnitData(id="
         + this.getId()
         + ", owner="
         + this.getOwner()
         + ", storedItems="
         + this.getStoredItems()
         + ", isPlaced="
         + this.isPlaced()
         + ", sizeType="
         + this.getSizeType()
         + ", lastLocation="
         + this.getLastLocation()
         + ")";
   }

   @Generated
   public int getId() {
      return this.id;
   }

   @Generated
   public OfflinePlayer getOwner() {
      return this.owner;
   }

   @Generated
   public boolean isPlaced() {
      return this.isPlaced;
   }

   @Generated
   public StorageUnitType getSizeType() {
      return this.sizeType;
   }

   @Generated
   public Location getLastLocation() {
      return this.lastLocation;
   }
}
