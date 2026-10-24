package com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell;

import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.ledger.CellLedger;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.ItemHashMap;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.ItemKey;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CellHandle implements Cell {
   private final CellLedger cache;

   protected CellHandle(@NotNull CellLedger cache) {
      this.cache = cache;
   }

   @NotNull
   public static CellHandle create(@NotNull ItemStack cellItem) {
      if (VoidCellSupport.isVoidCell(cellItem)) {
         VoidCellSupport.ensureInitialized(cellItem);
         long perTypeLimit = StorageCell.getPerTypeLimit(cellItem);
         CellLedger cache = StorageCell.loadCellCache(cellItem, perTypeLimit);
         cache.setVoidCell(true);
         return new VoidCellHandle(cache, VoidCellSupport.filtersView(cellItem));
      } else {
         long perTypeLimit = StorageCell.getPerTypeLimit(cellItem);
         CellLedger cache = StorageCell.loadCellCache(cellItem, perTypeLimit);
         return new CellHandle(cache);
      }
   }

   @NotNull
   public static CellHandle wrap(@NotNull CellLedger existingCache) {
      return new CellHandle(existingCache);
   }

   @NotNull
   @Override
   public UUID getUuid() {
      return this.cache.getUuid();
   }

   @Override
   public long getAmount(@NotNull ItemKey key) {
      return this.cache.getAmount(key);
   }

   @Override
   public boolean canReceiveItem(@NotNull ItemKey key) {
      return this.cache.canReceiveItem(key);
   }

   @Override
   public long receiveCapacity(@NotNull ItemKey key) {
      return this.cache.receiveCapacity(key);
   }

   @Override
   public boolean contains(@NotNull ItemKey key) {
      return this.cache.contains(key, 1L);
   }

   @Override
   public int pushItem(@NotNull ItemKey key, int amount) {
      return this.cache.pushItem(key, amount);
   }

   @Nullable
   @Override
   public ItemStack takeItem(@NotNull ItemKey key, long amount) {
      return this.cache.takeItem(key, amount);
   }

   @Override
   public long takeItemAmount(@NotNull ItemKey key, long amount) {
      return this.cache.takeItemAmount(key, amount);
   }

   @NotNull
   @Override
   public Map<ItemStack, Long> getAllItems() {
      return this.cache.getAllItems();
   }

   @Override
   public void accumulateInto(@NotNull ItemHashMap<Long> target, @Nullable Map<ItemKey, List<UUID>> index) {
      this.cache.accumulateInto(target, index);
   }

   @Override
   public long getStoredCount() {
      return this.cache.getStored();
   }
}
