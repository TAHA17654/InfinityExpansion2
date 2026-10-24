package com.balugaq.netex.integrations.logitech;

import com.balugaq.netex.api.data.ItemContainer;
import com.balugaq.netex.api.data.StorageUnitData;
import com.ytdd9527.networksexpansion.implementation.machines.unit.NetworksDrawer;
import io.github.sefiraat.networks.network.stackcaches.QuantumCache;
import io.github.sefiraat.networks.slimefun.network.NetworkQuantumStorage;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DrawerVirtualCache extends QuantumCache {
   private static final long VIRTUAL_Y_BASE = -1000001000L;
   private static final long VIRTUAL_Y_RANGE = 1000000000L;
   private final Location drawerLocation;
   private final ItemStack sample;

   private DrawerVirtualCache(@NotNull Location drawerLocation, @NotNull ItemStack sample) {
      super(sample, 0L, Long.MAX_VALUE, false, false);
      this.drawerLocation = drawerLocation;
      this.sample = sample;
   }

   @Nullable
   private StorageUnitData drawerData() {
      return NetworksDrawer.getStorageData(this.drawerLocation);
   }

   @Nullable
   private ItemContainer findContainer() {
      StorageUnitData data = this.drawerData();
      if (data == null) {
         return null;
      } else {
         for (ItemContainer ic : data.getStoredItemsDirectly()) {
            if (ic.isSimilar(this.sample)) {
               return ic;
            }
         }

         return null;
      }
   }

   @Override
   public long getAmount() {
      ItemContainer ic = this.findContainer();
      return ic == null ? 0L : (long)ic.getAmount();
   }

   @Override
   public long getAmountLong() {
      return this.getAmount();
   }

   @Override
   public int getAmountInt() {
      return (int)Math.min(this.getAmount(), 2147483647L);
   }

   @Override
   public synchronized void setAmount(long newAmount) {
      ItemContainer container = this.findContainer();
      if (container != null) {
         StorageUnitData data = this.drawerData();
         long max = data == null ? 0L : (long)data.getSizeType().getEachMaxSize();
         long target = Math.min(newAmount, max);
         long current = (long)container.getAmount();
         long delta = target - current;
         if (delta > 0L) {
            container.addAmount((int)Math.min(delta, 2147483647L));
         } else if (delta < 0L) {
            container.removeAmount((int)Math.min(-delta, 2147483647L));
         }
      }
   }

   @Override
   public synchronized void setAmount(int newAmount) {
      this.setAmount((long)newAmount);
   }

   @Override
   public long getLimitLong() {
      StorageUnitData data = this.drawerData();
      return data == null ? 0L : (long)data.getSizeType().getEachMaxSize();
   }

   @NotNull
   public static Location virtualLocation(@NotNull Location drawerLocation, @NotNull ItemStack sample) {
      long combined = CellVirtualCache.stableLocationHash(drawerLocation) * 1000003L + CellVirtualCache.stableItemHash(sample);
      return new Location(
         drawerLocation.getWorld(), drawerLocation.getX(), (double)(-1000001000L - Math.floorMod(combined, 1000000000L)), drawerLocation.getZ()
      );
   }

   public static void inject(@NotNull Location drawerLocation, @NotNull ItemStack sample) {
      CellVirtualCache.preWarmResolvers();
      Location virtual = virtualLocation(drawerLocation, sample);
      Map<Location, QuantumCache> caches = NetworkQuantumStorage.getCaches();
      if (!caches.containsKey(virtual)) {
         caches.put(virtual, new DrawerVirtualCache(drawerLocation, sample));
      }
   }

   public static void removeDrawer(@NotNull Location drawerLocation) {
      NetworkQuantumStorage.getCaches().values().removeIf(cache -> {
         if (cache instanceof DrawerVirtualCache virtual && virtual.drawerLocation.equals(drawerLocation)) {
            return true;
         }

         return false;
      });
   }
}
