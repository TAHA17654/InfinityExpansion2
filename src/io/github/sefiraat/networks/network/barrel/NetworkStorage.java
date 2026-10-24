package io.github.sefiraat.networks.network.barrel;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.sefiraat.networks.network.stackcaches.BarrelIdentity;
import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import io.github.sefiraat.networks.network.stackcaches.QuantumCache;
import io.github.sefiraat.networks.slimefun.network.NetworkQuantumStorage;
import io.github.sefiraat.networks.utils.StackUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NetworkStorage extends BarrelIdentity {
   public NetworkStorage(@NotNull Location location, ItemStack itemStack, long amount) {
      super(location, itemStack, amount, amount, BarrelType.NETWORKS);
   }

   public NetworkStorage(@NotNull Location location, ItemStack itemStack, long amount, long limit) {
      super(location, itemStack, amount, limit, BarrelType.NETWORKS);
   }

   @Nullable
   @Override
   public ItemStack requestItem(@NotNull ItemRequest itemRequest) {
      BlockMenu blockMenu = StorageCacheUtils.getMenu(this.getLocation());
      if (blockMenu == null) {
         return null;
      } else {
         QuantumCache cache = NetworkQuantumStorage.getCaches().get(blockMenu.getLocation());
         if (cache == null) {
            return null;
         } else {
            ItemStack cacheItem = cache.getItemStack();
            return cacheItem != null && !StackUtils.itemsMatch(itemRequest.getItemStack(), cacheItem)
               ? null
               : NetworkQuantumStorage.getItemStack(cache, blockMenu, itemRequest.getAmount());
         }
      }
   }

   @Override
   public void depositItemStack(@NotNull ItemStack[] itemsToDeposit) {
      if (StorageCacheUtils.getSfItem(this.getLocation()) instanceof NetworkQuantumStorage) {
         BlockMenu blockMenu = StorageCacheUtils.getMenu(this.getLocation());
         if (blockMenu == null) {
            return;
         }

         QuantumCache cache = NetworkQuantumStorage.getCaches().get(this.getLocation());
         if (cache != null) {
            NetworkQuantumStorage.tryInputItem(blockMenu.getLocation(), itemsToDeposit, cache);
         }
      }
   }

   @Override
   public int[] getInputSlot() {
      return new int[]{1};
   }

   @Override
   public int[] getOutputSlot() {
      return new int[]{7};
   }
}
