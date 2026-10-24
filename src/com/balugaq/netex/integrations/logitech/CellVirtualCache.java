package com.balugaq.netex.integrations.logitech;

import com.balugaq.netex.utils.Debug;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.CellHandle;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.drive.CellDrive;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.ItemKey;
import com.ytdd9527.networksexpansion.utils.ReflectionUtil;
import io.github.sefiraat.networks.network.stackcaches.QuantumCache;
import io.github.sefiraat.networks.slimefun.network.NetworkQuantumStorage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CellVirtualCache extends QuantumCache {
   private static final long VIRTUAL_Y_BASE = -1000000L;
   private static final long VIRTUAL_Y_RANGE = 1000000000L;
   private static volatile boolean resolversPrewarmed = false;
   private final Location driveLocation;
   private final ItemKey itemKey;

   private CellVirtualCache(@NotNull Location driveLocation, @NotNull ItemKey itemKey) {
      super(itemKey.getItemStack(), 0L, Long.MAX_VALUE, false, false);
      this.driveLocation = driveLocation;
      this.itemKey = itemKey;
   }

   @Override
   public long getAmount() {
      return this.realAmount();
   }

   @Override
   public long getAmountLong() {
      return this.realAmount();
   }

   @Override
   public int getAmountInt() {
      return (int)Math.min(this.realAmount(), 2147483647L);
   }

   @Override
   public synchronized void setAmount(long newAmount) {
      long current = this.realAmount();
      long delta = newAmount - current;
      if (delta > 0L) {
         this.pushToCells(delta);
      } else if (delta < 0L) {
         this.pullFromCells(-delta);
      }
   }

   @Override
   public synchronized void setAmount(int newAmount) {
      this.setAmount((long)newAmount);
   }

   @Override
   public long getLimitLong() {
      return this.realAmount() + this.safeRemaining();
   }

   private long realAmount() {
      BlockMenu menu = StorageCacheUtils.getMenu(this.driveLocation);
      if (menu == null) {
         return 0L;
      } else {
         long total = 0L;

         for (CellHandle cell : CellDrive.getStorage().getCells(menu)) {
            total += cell.getAmount(this.itemKey);
         }

         return total;
      }
   }

   private long safeRemaining() {
      BlockMenu menu = StorageCacheUtils.getMenu(this.driveLocation);
      if (menu == null) {
         return 0L;
      } else {
         long remaining = 0L;

         for (CellHandle cell : CellDrive.getStorage().getCells(menu)) {
            long cap = cell.receiveCapacity(this.itemKey);
            if (cap == 0L && cell.canReceiveItem(this.itemKey)) {
               cap = 1L;
            }

            remaining += cap;
         }

         return remaining;
      }
   }

   private void pushToCells(long amount) {
      BlockMenu menu = StorageCacheUtils.getMenu(this.driveLocation);
      if (menu != null) {
         long remaining = amount;

         for (CellHandle cell : CellDrive.getStorage().getCells(menu)) {
            if (remaining <= 0L) {
               break;
            }

            int pushed = cell.pushItem(this.itemKey, (int)Math.min(remaining, 2147483647L));
            remaining -= (long)pushed;
         }
      }
   }

   private void pullFromCells(long amount) {
      BlockMenu menu = StorageCacheUtils.getMenu(this.driveLocation);
      if (menu != null) {
         long remaining = amount;

         for (CellHandle cell : CellDrive.getStorage().getCells(menu)) {
            if (remaining <= 0L) {
               break;
            }

            long taken = cell.takeItemAmount(this.itemKey, remaining);
            remaining -= taken;
         }
      }
   }

   @NotNull
   public static Location virtualLocation(@NotNull Location driveLocation, @NotNull ItemKey itemKey) {
      long combined = stableLocationHash(driveLocation) * 1000003L + stableItemHash(itemKey.getItemStack());
      return new Location(driveLocation.getWorld(), driveLocation.getX(), (double)(-1000000L - Math.floorMod(combined, 1000000000L)), driveLocation.getZ());
   }

   public static long stableLocationHash(@NotNull Location location) {
      World world = location.getWorld();
      long h = (world != null ? (long)world.getName().hashCode() : 0L) * 1000000007L;
      h = h * 31L + (long)location.getBlockX();
      h = h * 31L + (long)location.getBlockY();
      return h * 31L + (long)location.getBlockZ();
   }

   public static long stableItemHash(@NotNull ItemStack template) {
      byte[] bytes = template.serializeAsBytes();
      return (long)bytes.length * 1000003L + (long)Arrays.hashCode(bytes);
   }

   public static void inject(@NotNull Location driveLocation, @NotNull ItemKey itemKey) {
      preWarmResolvers();
      Location virtual = virtualLocation(driveLocation, itemKey);
      Map<Location, QuantumCache> caches = NetworkQuantumStorage.getCaches();
      if (!caches.containsKey(virtual)) {
         caches.put(virtual, new CellVirtualCache(driveLocation, itemKey));
      }
   }

   public static void removeDrive(@NotNull Location driveLocation) {
      NetworkQuantumStorage.getCaches().values().removeIf(cache -> {
         if (cache instanceof CellVirtualCache virtual && virtual.driveLocation.equals(driveLocation)) {
            return true;
         }

         return false;
      });
   }

   static void preWarmResolvers() {
      if (!resolversPrewarmed) {
         resolversPrewarmed = true;

         try {
            Class<?> storages = ReflectionUtil.getClass("me.matl114.logitech.core.Cargo.Storages", "me.matl114.logitech.SlimefunItem.Cargo.Storages");
            if (storages == null) {
               return;
            }

            Field adapterField = storages.getDeclaredField("NTWSTORAGE_PROXY");
            adapterField.setAccessible(true);
            Object adapter = adapterField.get(null);
            if (adapter == null) {
               return;
            }

            Location dummy = new Location((World)Bukkit.getWorlds().get(0), 0.0, -2.1E9, 0.0);
            QuantumCache probe = new QuantumCache(new ItemStack(Material.STONE), 1L, 16, false, false);
            Map<Location, QuantumCache> caches = NetworkQuantumStorage.getCaches();
            caches.put(dummy, probe);

            try {
               invokeAdapter(adapter, "setAmount", new Class[]{Location.class, long.class}, dummy, 1L);
               invokeAdapter(adapter, "getAmount", new Class[]{Location.class}, dummy);
               invokeAdapter(adapter, "getMaxAmount", new Class[]{Location.class}, dummy);
               invokeAdapter(adapter, "getItemStack", new Class[]{Location.class}, dummy);
            } finally {
               caches.remove(dummy);
            }
         } catch (Throwable var10) {
            Debug.trace(var10, "Warming up LogiTech2 Quantum proxy reflection lookup");
         }
      }
   }

   private static void invokeAdapter(@NotNull Object adapter, @NotNull String name, @NotNull Class<?>[] params, @NotNull Object... args) throws ReflectiveOperationException {
      Method method = adapter.getClass().getMethod(name, params);
      method.invoke(adapter, args);
   }
}
