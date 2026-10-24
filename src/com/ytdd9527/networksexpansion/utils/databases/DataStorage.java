package com.ytdd9527.networksexpansion.utils.databases;

import com.balugaq.netex.api.data.StorageUnitData;
import com.balugaq.netex.api.enums.StorageUnitType;
import com.balugaq.netex.utils.Lang;
import io.github.sefiraat.networks.Networks;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class DataStorage {
   private static final DataSource dataSource = Networks.getDataSource();
   private static final Map<Integer, Boolean> state = new ConcurrentHashMap<>(4096);
   private static final Map<Integer, Optional<StorageUnitData>> cache = new ConcurrentHashMap<>(4096);
   @NotNull
   private static Map<Integer, Map<Integer, Integer>> changes = new ConcurrentHashMap<>(4096);

   public static void requestStorageData(int id) {
      if (!state.containsKey(id)) {
         Networks.getQueryQueue().scheduleQuery(() -> {
            loadContainer(id);
            return true;
         });
      }
   }

   public static void restoreFromLocation(@NotNull final Location l, @NotNull final Consumer<Optional<StorageUnitData>> usage) {
      (new BukkitRunnable() {
         public void run() {
            int id = DataStorage.dataSource.getIdFromLocation(l);
            if (id == -1) {
               usage.accept(Optional.empty());
            } else {
               if (!DataStorage.isContainerLoaded(id)) {
                  DataStorage.loadContainer(id);
               }

               usage.accept(DataStorage.getCachedStorageData(id));
            }
         }
      }).runTaskAsynchronously(Networks.getInstance());
   }

   @NotNull
   public static Optional<StorageUnitData> getCachedStorageData(int id) {
      return cache.getOrDefault(id, Optional.empty());
   }

   public static int getItemId(@NotNull ItemStack item) {
      return dataSource.getItemId(item);
   }

   @NotNull
   public static synchronized StorageUnitData createStorageUnitData(@NotNull OfflinePlayer owner, StorageUnitType sizeType, Location placedLocation) {
      StorageUnitData re = new StorageUnitData(dataSource.getNextContainerId(), owner.getUniqueId().toString(), sizeType, true, placedLocation);
      dataSource.saveNewStorageData(re);
      cache.put(re.getId(), Optional.of(re));
      state.put(re.getId(), true);
      return re;
   }

   public static void setContainerStatus(int id, boolean isPlaced) {
      if (isContainerLoaded(id)) {
         getCachedStorageData(id).ifPresent(data -> data.setPlaced(isPlaced));
      }

      dataSource.updateContainer(id, "IsPlaced", String.valueOf(isPlaced ? 1 : 0));
   }

   public static void setContainerSizeType(int id, @NotNull StorageUnitType type) {
      if (isContainerLoaded(id)) {
         getCachedStorageData(id).ifPresent(data -> data.setSizeType(type));
      }

      dataSource.updateContainer(id, "SizeType", String.valueOf(type.ordinal()));
   }

   public static void setContainerLocation(int id, @NotNull Location l) {
      if (isContainerLoaded(id)) {
         getCachedStorageData(id).ifPresent(data -> data.setLastLocation(l));
      }

      dataSource.updateContainer(id, "LastLocation", formatLocation(l));
   }

   public static void addStoredItem(int containerId, int itemId, int amount) {
      dataSource.addStoredItem(containerId, itemId, amount);
   }

   public static void deleteStoredItem(int containerId, int itemId) {
      dataSource.deleteStoredItem(containerId, itemId);
   }

   public static void setStoredAmount(int containerId, int itemId, int amount) {
      Map<Integer, Integer> items = changes.computeIfAbsent(containerId, k -> new HashMap<>());
      items.put(itemId, amount);
   }

   public static void saveAmountChange() {
      Networks.getInstance().getLogger().info(Lang.getString("messages.data-saving.saving-drawer"));
      Map<Integer, Map<Integer, Integer>> lastChanges = changes;
      changes = new ConcurrentHashMap<>();

      for (Entry<Integer, Map<Integer, Integer>> each : lastChanges.entrySet()) {
         for (Entry<Integer, Integer> eachItem : each.getValue().entrySet()) {
            dataSource.updateItemAmount(each.getKey(), eachItem.getKey(), eachItem.getValue());
         }
      }

      Networks.getInstance().getLogger().info(Lang.getString("messages.data-saving.saved-drawer"));
      Networks.getInstance().debug("Task amount: " + Networks.getQueryQueue().getTaskAmount());
   }

   public static boolean isContainerLoaded(int id) {
      return id == -1 ? true : state.getOrDefault(id, false);
   }

   static void setContainerLoaded(int id) {
      state.put(id, true);
   }

   @NotNull
   static String formatLocation(@NotNull Location l) {
      return Objects.requireNonNull(l.getWorld()).getUID() + ";" + l.getBlockX() + ";" + l.getBlockY() + ";" + l.getBlockZ();
   }

   private static void loadContainer(int id) {
      StorageUnitData data = dataSource.getStorageData(id);
      cache.put(id, Optional.ofNullable(data));
      state.put(id, data == null);
   }
}
