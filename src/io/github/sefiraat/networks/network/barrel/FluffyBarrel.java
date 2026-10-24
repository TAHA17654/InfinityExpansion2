package io.github.sefiraat.networks.network.barrel;

import com.balugaq.netex.utils.BlockMenuUtil;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.sefiraat.networks.network.stackcaches.BarrelIdentity;
import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import io.github.sefiraat.networks.utils.StackUtils;
import io.ncbpfluffybear.fluffymachines.items.Barrel;
import lombok.Generated;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FluffyBarrel extends BarrelIdentity {
   private final boolean voidExcess;

   public FluffyBarrel(@NotNull Location location, ItemStack itemStack, int amount, int limit, boolean voidExcess) {
      super(location, itemStack, (long)amount, (long)limit, BarrelType.FLUFFY);
      this.voidExcess = voidExcess;
      BlockMenu menu = StorageCacheUtils.getMenu(this.getLocation());
      Barrel barrel = (Barrel)StorageCacheUtils.getSfItem(this.getLocation());
      if (barrel != null) {
         barrel.updateMenu(this.getLocation().getBlock(), menu, true, (int)this.getLimit());
      }
   }

   @Nullable
   @Override
   public ItemStack requestItem(@NotNull ItemRequest itemRequest) {
      BlockMenu menu = StorageCacheUtils.getMenu(this.getLocation());
      if (menu == null) {
         return null;
      } else {
         int received = 0;
         ItemStack targetItem = itemRequest.getItemStack();

         for (int slot : this.getOutputSlot()) {
            ItemStack item = menu.getItemInSlot(slot);
            if (item != null && item.getType() != Material.AIR && StackUtils.itemsMatch(item, targetItem)) {
               int max = Math.min(item.getAmount(), itemRequest.getAmount() - received);
               BlockMenuUtil.consumeItem(menu, slot, max);
               received += max;
            }
         }

         return received <= 0 ? null : StackUtils.getAsQuantity(targetItem, received);
      }
   }

   @Override
   public void depositItemStack(@NotNull ItemStack[] itemsToDeposit) {
      BlockMenu menu = StorageCacheUtils.getMenu(this.getLocation());
      if (menu != null) {
         BlockMenuUtil.pushItem(menu, itemsToDeposit, this.getInputSlot());
      }
   }

   @Override
   public int[] getInputSlot() {
      BlockMenu menu = StorageCacheUtils.getMenu(this.getLocation());
      return menu == null ? new int[0] : menu.getPreset().getSlotsAccessedByItemTransport(menu, ItemTransportFlow.INSERT, null);
   }

   @Override
   public int[] getOutputSlot() {
      BlockMenu menu = StorageCacheUtils.getMenu(this.getLocation());
      return menu == null ? new int[0] : menu.getPreset().getSlotsAccessedByItemTransport(menu, ItemTransportFlow.WITHDRAW, null);
   }

   @Generated
   public boolean isVoidExcess() {
      return this.voidExcess;
   }
}
