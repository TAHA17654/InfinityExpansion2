package com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.ledger;

import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.rule.CellAcceptRules;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage.CellStorageDatabase;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.ItemHashMap;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.ItemKey;
import com.ytdd9527.networksexpansion.utils.itemstacks.ItemStackUtil;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.utils.StackUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Generated;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CellLedger {
   private static volatile long cachedMaxUnitCount = 0L;
   private static final Map<UUID, CellLedger> activeCellCaches = new ConcurrentHashMap<>();
   private final UUID uuid;
   private final long perTypeLimit;
   private final ItemHashMap<Long> storage = new ItemHashMap<>();
   private final List<ItemStack> whitelist = new ArrayList<>();
   private Map<ItemStack, Long> allItemsView = null;
   private long totalStored;
   private volatile long currentPerTypeLimit;
   private boolean whitelistEnabled;
   private volatile String customName;
   private final Set<ItemKey> reservedUnits = ConcurrentHashMap.newKeySet();
   private final Set<ItemKey> voidExcessUnits = ConcurrentHashMap.newKeySet();
   private volatile boolean unlimited;
   private volatile boolean voidCell;

   public CellLedger(@NotNull UUID uuid, long perTypeLimit, long currentPerTypeLimit) {
      this.uuid = uuid;
      this.perTypeLimit = perTypeLimit;
      this.currentPerTypeLimit = currentPerTypeLimit;
      this.totalStored = 0L;
   }

   @NotNull
   public static CellLedger getOrCreate(@NotNull UUID uuid, long perTypeLimit, long currentPerTypeLimit) {
      return activeCellCaches.computeIfAbsent(uuid, k -> new CellLedger(uuid, perTypeLimit, currentPerTypeLimit));
   }

   @NotNull
   public static Map<UUID, CellLedger> getActiveCaches() {
      return activeCellCaches;
   }

   public static long getMaxUnitCount() {
      long cached = cachedMaxUnitCount;
      if (cached <= 0L) {
         cached = Math.max(1L, Math.min(64L, (long)Networks.getConfigManager().getCellnetMaxItemTypes()));
         cachedMaxUnitCount = cached;
      }

      return cached;
   }

   public long getMaxUnits() {
      return this.unlimited ? Long.MAX_VALUE : Math.min(this.perTypeLimit, getMaxUnitCount());
   }

   public static boolean isUnlimitedPerType(long perTypeLimit) {
      return perTypeLimit >= 4611686018427387903L;
   }

   public synchronized long getStored() {
      return this.totalStored;
   }

   @Nullable
   public synchronized ItemStack takeItem(@NotNull ItemStack itemStack, long amount) {
      return this.takeItem(new ItemKey(itemStack), amount);
   }

   @NotNull
   public synchronized List<CellLedger.CellEntry> getStoredItems() {
      List<CellLedger.CellEntry> result = new ArrayList<>(this.storage.size());

      for (Entry<ItemKey, Long> entry : this.storage.keyEntrySet()) {
         if (entry.getValue() > 0L || this.reservedUnits.contains(entry.getKey())) {
            result.add(new CellLedger.CellEntry(entry.getKey().getItemStack(), entry.getValue()));
         }
      }

      return result;
   }

   @NotNull
   public synchronized Map<ItemStack, Long> getAllItems() {
      if (this.allItemsView == null) {
         Map<ItemStack, Long> result = new HashMap<>();

         for (Entry<ItemKey, Long> entry : this.storage.keyEntrySet()) {
            result.put(entry.getKey().getItemStack(), entry.getValue());
         }

         this.allItemsView = result;
      }

      return new HashMap<>(this.allItemsView);
   }

   private void invalidateItemsView() {
      this.allItemsView = null;
   }

   public synchronized void accumulateInto(@NotNull ItemHashMap<Long> target, @Nullable Map<ItemKey, List<UUID>> index) {
      for (Entry<ItemKey, Long> entry : this.storage.keyEntrySet()) {
         if (entry.getValue() > 0L || this.reservedUnits.contains(entry.getKey())) {
            ItemKey key = entry.getKey();
            Long existing = target.getKey(key);
            target.putKey(key, (existing != null ? existing : 0L) + entry.getValue());
            if (index != null) {
               index.computeIfAbsent(key, k -> new ArrayList<>()).add(this.uuid);
            }
         }
      }
   }

   public synchronized void loadItemSilently(@NotNull ItemStack sample, long amount) {
      if (amount > 0L && !sample.getType().isAir()) {
         ItemKey key = new ItemKey(sample);
         long existing = this.storage.getOrDefault(key, 0L);
         this.storage.putKey(key, existing + amount);
         this.totalStored += amount;
         this.invalidateItemsView();
      }
   }

   public synchronized void loadMetaSilently(boolean enabled, @NotNull List<ItemStack> whitelistItems) {
      this.whitelistEnabled = enabled;
      this.whitelist.clear();

      for (ItemStack item : whitelistItems) {
         ItemStack normalized = item.clone();
         normalized.setAmount(1);
         this.whitelist.add(normalized);
      }
   }

   public synchronized boolean isWhitelistEnabled() {
      return this.whitelistEnabled;
   }

   public synchronized void updateWhitelist(boolean whitelistEnabled, @NotNull List<ItemStack> items) {
      this.whitelistEnabled = whitelistEnabled;
      this.whitelist.clear();

      for (ItemStack item : items) {
         ItemStack normalized = item.clone();
         normalized.setAmount(1);
         this.whitelist.add(normalized);
      }

      this.saveWhitelist();
   }

   @NotNull
   public synchronized List<ItemStack> getWhitelist() {
      List<ItemStack> result = new ArrayList<>(this.whitelist.size());

      for (ItemStack item : this.whitelist) {
         result.add(item.clone());
      }

      return result;
   }

   @Nullable
   public String getCustomName() {
      return this.customName;
   }

   public void setCustomName(@Nullable String customName) {
      this.customName = customName;
   }

   private void markItemDirty(@NotNull ItemKey key, long finalAmount) {
      CellStorageDatabase db = Networks.getCellStorageDatabase();
      if (db != null) {
         db.getStorageController().markDirty(this.uuid, key, finalAmount);
      }
   }

   private void saveWhitelist() {
      CellStorageDatabase db = Networks.getCellStorageDatabase();
      if (db != null) {
         db.getStorageController().saveWhitelist(this.uuid, this.isWhitelistEnabled(), this.getWhitelist());
      }
   }

   private boolean inWhitelist(@NotNull ItemStack sample) {
      for (ItemStack template : this.whitelist) {
         if (StackUtils.itemsMatch(template, sample)) {
            return true;
         }
      }

      return false;
   }

   public synchronized boolean canReceiveItem(@NotNull ItemKey key) {
      Long existing = this.storage.getKey(key);
      if (existing != null) {
         return this.voidExcessUnits.contains(key) ? true : existing < this.perTypeLimit;
      } else {
         return this.whitelistEnabled && !this.inWhitelist(key.getItemStack())
            ? false
            : (long)this.storage.size() < Math.min(this.currentPerTypeLimit, this.getMaxUnits());
      }
   }

   public synchronized long receiveCapacity(@NotNull ItemKey key) {
      Long existing = this.storage.getKey(key);
      return existing != null && existing > 0L ? Math.max(0L, this.perTypeLimit - existing) : 0L;
   }

   public synchronized int pushItem(@NotNull ItemKey key, int amount) {
      return (int)Math.min(this.pushItemLong(key, (long)amount), 2147483647L);
   }

   public synchronized long pushItemLong(@NotNull ItemKey key, long amount) {
      long existing = this.storage.getOrDefault(key, 0L);
      if (existing > 0L) {
         if (this.voidExcessUnits.contains(key)) {
            this.topUp(key, existing, amount);
            return amount;
         } else {
            return this.topUp(key, existing, amount);
         }
      } else {
         if (key.hasMeta()) {
            ItemStack sample = key.getItemStack();
            if (key.isOversized() || CellAcceptRules.isUsedCell(sample) || CellAcceptRules.isFilledContainer(sample)) {
               return 0L;
            }
         }

         if (this.reservedUnits.contains(key)) {
            return this.topUp(key, existing, amount);
         } else if (this.whitelistEnabled && !this.inWhitelist(key.getItemStack())) {
            return 0L;
         } else {
            return (long)this.storage.size() >= Math.min(this.currentPerTypeLimit, this.getMaxUnits()) ? 0L : this.topUp(key, existing, amount);
         }
      }
   }

   private long topUp(@NotNull ItemKey key, long existing, long amount) {
      long toAdd = Math.min(amount, Math.max(0L, this.perTypeLimit - existing));
      if (toAdd <= 0L) {
         return 0L;
      } else {
         this.storage.putKey(key, existing + toAdd);
         this.totalStored += toAdd;
         this.markItemDirty(key, existing + toAdd);
         this.invalidateItemsView();
         return toAdd;
      }
   }

   @Nullable
   public synchronized ItemStack takeItem(@NotNull ItemKey key, long amount) {
      long take = this.takeItemInternal(key, amount);
      if (take <= 0L) {
         return null;
      } else {
         ItemStack result = key.getItemStack();
         result.setAmount((int)Math.min(take, 2147483647L));
         return result;
      }
   }

   public synchronized long takeItemAmount(@NotNull ItemKey key, long amount) {
      return this.takeItemInternal(key, amount);
   }

   private long takeItemInternal(@NotNull ItemKey key, long amount) {
      Long existing = this.storage.getKey(key);
      if (existing != null && existing > 0L && amount > 0L) {
         long take = Math.min(amount, existing);
         long remaining = existing - take;
         if (remaining > 0L) {
            this.storage.putKey(key, remaining);
         } else if (this.reservedUnits.contains(key)) {
            this.storage.putKey(key, 0L);
         } else {
            this.storage.removeKey(key);
         }

         this.totalStored -= take;
         this.markItemDirty(key, remaining);
         this.invalidateItemsView();
         return take;
      } else {
         return 0L;
      }
   }

   public synchronized boolean contains(@NotNull ItemKey key, long amount) {
      Long existing = this.storage.getKey(key);
      return existing != null && existing >= amount;
   }

   public synchronized long getAmount(@NotNull ItemKey key) {
      Long existing = this.storage.getKey(key);
      return existing == null ? 0L : existing;
   }

   public synchronized boolean isReserved(@NotNull ItemKey key) {
      return this.reservedUnits.contains(key);
   }

   public synchronized void setReserved(@NotNull ItemKey key, boolean reserved) {
      if (reserved) {
         if (CellAcceptRules.isNbtOversized(key.getItemStack())) {
            return;
         }

         this.reservedUnits.add(key);
         if (!this.storage.containsKey(key)) {
            this.storage.putKey(key, 0L);
         }
      } else {
         this.reservedUnits.remove(key);
         Long existing = this.storage.getKey(key);
         if (existing != null && existing <= 0L) {
            this.storage.removeKey(key);
         }
      }

      this.invalidateItemsView();
   }

   public synchronized void restoreReserved(@NotNull ItemKey key) {
      this.reservedUnits.add(key);
      if (!this.storage.containsKey(key)) {
         this.storage.putKey(key, 0L);
      }

      this.invalidateItemsView();
   }

   @NotNull
   public synchronized List<ItemStack> getReservedTemplates() {
      List<ItemStack> result = new ArrayList<>(this.reservedUnits.size());

      for (ItemKey key : this.reservedUnits) {
         result.add(key.getItemStack());
      }

      return result;
   }

   public synchronized boolean isVoidExcessUnit(@NotNull ItemKey key) {
      return this.voidExcessUnits.contains(key);
   }

   public synchronized void setVoidExcessUnit(@NotNull ItemKey key, boolean voidExcess) {
      if (voidExcess) {
         if (CellAcceptRules.isNbtOversized(key.getItemStack())) {
            return;
         }

         this.voidExcessUnits.add(key);
      } else {
         this.voidExcessUnits.remove(key);
      }
   }

   public synchronized void restoreVoidExcessUnit(@NotNull ItemKey key) {
      this.voidExcessUnits.add(key);
   }

   public synchronized void migrateLegacyVoidExcess() {
      for (Entry<ItemKey, Long> entry : this.storage.keyEntrySet()) {
         this.voidExcessUnits.add(entry.getKey());
      }
   }

   @NotNull
   public synchronized List<ItemStack> getVoidExcessTemplates() {
      List<ItemStack> result = new ArrayList<>(this.voidExcessUnits.size());

      for (ItemKey key : this.voidExcessUnits) {
         result.add(key.getItemStack());
      }

      return result;
   }

   @Generated
   public UUID getUuid() {
      return this.uuid;
   }

   @Generated
   public long getPerTypeLimit() {
      return this.perTypeLimit;
   }

   @Generated
   public long getCurrentPerTypeLimit() {
      return this.currentPerTypeLimit;
   }

   @Generated
   public void setCurrentPerTypeLimit(long currentPerTypeLimit) {
      this.currentPerTypeLimit = currentPerTypeLimit;
   }

   @Generated
   public void setUnlimited(boolean unlimited) {
      this.unlimited = unlimited;
   }

   @Generated
   public boolean isVoidCell() {
      return this.voidCell;
   }

   @Generated
   public void setVoidCell(boolean voidCell) {
      this.voidCell = voidCell;
   }

   public static class CellEntry {
      public final ItemStack sample;
      public final long amount;

      public CellEntry(@NotNull ItemStack sample, long amount) {
         this.sample = ItemStackUtil.getCleanItem(sample);
         this.amount = amount;
      }
   }
}
