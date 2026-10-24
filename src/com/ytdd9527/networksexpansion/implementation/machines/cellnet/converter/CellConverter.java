package com.ytdd9527.networksexpansion.implementation.machines.cellnet.converter;

import com.balugaq.netex.utils.Lang;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.CellTier;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.StorageCell;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.ledger.CellLedger;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.ledger.CellPersistence;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.ChatInput;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.ItemKey;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.ItemSearch;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.ui.CellSlotUI;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.ui.Icons;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.ui.MenuShells;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.network.stackcaches.QuantumCache;
import io.github.sefiraat.networks.slimefun.network.NetworkQuantumStorage;
import io.github.sefiraat.networks.utils.Keys;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemHandler;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import net.guizhanss.minecraft.guizhanlib.gugu.minecraft.helpers.inventory.ItemStackHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CellConverter extends CellBrowserBase {
   public static final int MODE_SLOT = 48;
   public static final int[] DISPLAY_SLOTS = new int[]{9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26};
   public static final int[] OUTPUT_SLOTS = new int[]{27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38};
   public static final int[] BACKGROUND_SLOTS = new int[]{1, 2, 3, 5, 6, 7, 39, 40, 41, 42, 43, 44, 46, 47, 49, 50, 51, 52, 53};
   private static final NamespacedKey CELL_SLOT_MARKER_KEY = new NamespacedKey("networks", "converter_cell_slot_marker");
   private final Map<Location, Boolean> exportMode = new ConcurrentHashMap<>();
   private final Map<Location, Long> refreshSignature = new ConcurrentHashMap<>();

   public CellConverter(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe);
   }

   @Override
   public int[] displaySlots() {
      return DISPLAY_SLOTS;
   }

   @Override
   public int[] backgroundSlots() {
      return BACKGROUND_SLOTS;
   }

   @NotNull
   @Override
   public String langPrefix() {
      return "messages.cellnet.converter";
   }

   @NotNull
   @Override
   public ChatInput.SearchTarget searchTarget() {
      return ChatInput.SearchTarget.CELL_CONVERTER;
   }

   @NotNull
   @Override
   public ItemStack createCellSlotMarker() {
      ItemStack marker = Icons.CONVERTER_CELL_SLOT.clone();
      ItemMeta meta = marker.getItemMeta();
      if (meta != null) {
         meta.getPersistentDataContainer().set(CELL_SLOT_MARKER_KEY, PersistentDataType.BOOLEAN, true);
         marker.setItemMeta(meta);
      }

      return marker;
   }

   @Override
   public boolean isCellSlotMarker(@Nullable ItemStack itemStack) {
      if (itemStack != null && itemStack.hasItemMeta()) {
         ItemMeta meta = itemStack.getItemMeta();
         return meta != null && meta.getPersistentDataContainer().has(CELL_SLOT_MARKER_KEY, PersistentDataType.BOOLEAN);
      } else {
         return false;
      }
   }

   @Override
   public void onDisplayClick(@NotNull BlockMenu menu, @NotNull Player player, int slot, @Nullable ItemStack item, @NotNull ClickAction action) {
      if (this.isExportMode(menu)) {
         this.transferItem(menu, player, slot);
      } else {
         this.importItem(menu, player, slot);
      }
   }

   @Override
   public void onCellInserted(@NotNull BlockMenu menu, @NotNull ItemStack cellItem, @NotNull Player player) {
      if (CellSlotUI.prepareInsertedCell(player, cellItem)) {
         this.pageCache.put(menu.getLocation(), 0);
         this.refresh(menu);
      }
   }

   @Nullable
   @Override
   public ItemStack onCellEjected(@NotNull BlockMenu menu, int slot) {
      return CellSlotUI.ejectCell(menu, slot, this::createCellSlotMarker, DISPLAY_SLOTS, this.pageCache, () -> this.refresh(menu));
   }

   @Override
   public void refresh(@NotNull BlockMenu menu) {
      long sig = this.computeSignature(menu);
      if (sig != this.refreshSignature.getOrDefault(menu.getLocation(), 0L)) {
         this.refreshSignature.put(menu.getLocation(), sig);
         boolean exporting = this.isExportMode(menu);
         ItemStack cellItem = menu.getItemInSlot(4);
         menu.replaceExistingItem(48, buildModeButton(exporting));
         if (cellItem != null && StorageCell.isStorageCell(cellItem)) {
            long per = StorageCell.getPerTypeLimit(cellItem);
            CellLedger cache = StorageCell.loadCellCache(cellItem, per);
            StorageCell.applyLore(cellItem, per, StorageCell.getCurrentPerTypeLimit(cellItem));
            menu.replaceExistingItem(4, cellItem);
            if (exporting) {
               this.renderCellItems(menu, cache);
            } else {
               this.renderQuantumItems(menu);
            }

            menu.replaceExistingItem(0, this.pageButton(this.pageCache.getOrDefault(menu.getLocation(), 0) > 0, false));
            menu.replaceExistingItem(8, this.pageButton(this.pageCache.getOrDefault(menu.getLocation(), 0) < this.maxPages(menu) - 1, true));
            menu.replaceExistingItem(45, buildInfoItem(cellItem, cache.getStoredItems().size(), CellSlotUI.countOccupied(menu, OUTPUT_SLOTS)));
            this.wireSearchButton(menu);
         } else {
            menu.replaceExistingItem(4, this.createCellSlotMarker());
            CellSlotUI.clearDisplay(menu, DISPLAY_SLOTS);
            menu.replaceExistingItem(45, buildInfoItem(null, 0, 0));
            menu.replaceExistingItem(0, this.pageButton(false, false));
            menu.replaceExistingItem(8, this.pageButton(false, true));
         }
      }
   }

   private long computeSignature(@NotNull BlockMenu menu) {
      ItemStack cellItem = menu.getItemInSlot(4);
      boolean hasCell = cellItem != null && StorageCell.isStorageCell(cellItem);
      int page = this.pageCache.getOrDefault(menu.getLocation(), 0);
      boolean exporting = this.isExportMode(menu);
      String search = SEARCH_TERMS.get(menu.getLocation());
      long outputHash = 0L;

      for (int slot : OUTPUT_SLOTS) {
         ItemStack onSlot = menu.getItemInSlot(slot);
         long slotHash = 0L;
         if (onSlot != null && !onSlot.getType().isAir()) {
            QuantumCache qc = Keys.getQuantumCache(onSlot.getItemMeta());
            slotHash = qc != null
               ? (long)Objects.hash(onSlot.getType(), qc.getAmountLong())
               : (long)Objects.hash(onSlot.getType(), onSlot.getAmount(), onSlot.getItemMeta());
         }

         outputHash = 31L * outputHash + slotHash;
      }

      int entriesCount = 0;
      long totalAmount = 0L;
      if (hasCell) {
         long per = StorageCell.getPerTypeLimit(cellItem);
         CellLedger cache = StorageCell.loadCellCache(cellItem, per);
         List<CellLedger.CellEntry> entries = cache.getStoredItems();
         entriesCount = entries.size();

         for (CellLedger.CellEntry entry : entries) {
            totalAmount += entry.amount;
         }
      }

      return (long)Objects.hash(hasCell, page, exporting, search, outputHash, entriesCount, totalAmount);
   }

   @Override
   protected void setupMachineHandlers(@NotNull BlockMenu menu) {
      menu.addMenuClickHandler(48, (player, slot, item, action) -> {
         boolean exporting = this.isExportMode(menu);
         this.exportMode.put(menu.getLocation(), !exporting);
         this.pageCache.put(menu.getLocation(), 0);
         this.refresh(menu);
         return false;
      });
   }

   @Override
   protected int maxPages(@NotNull BlockMenu menu) {
      ItemStack cellItem = menu.getItemInSlot(4);
      if (cellItem == null || !StorageCell.isStorageCell(cellItem)) {
         return 1;
      } else if (!this.isExportMode(menu)) {
         return 1;
      } else {
         long per = StorageCell.getPerTypeLimit(cellItem);
         CellLedger cache = StorageCell.loadCellCache(cellItem, per);
         int count = this.visibleEntries(menu, cache).size();
         return Math.max(1, (int)Math.ceil((double)count / (double)this.itemsPerPage()));
      }
   }

   public void preRegister() {
      this.addItemHandler(new ItemHandler[]{new BlockTicker() {
         public boolean isSynchronized() {
            return false;
         }

         public void tick(@NotNull Block b, SlimefunItem item, SlimefunBlockData data) {
            BlockMenu blockMenu = data.getBlockMenu();
            if (blockMenu != null && blockMenu.hasViewer()) {
               Bukkit.getScheduler().runTask(Networks.getInstance(), () -> {
                  if (blockMenu.hasViewer()) {
                     CellConverter.this.refresh(blockMenu);
                  }
               });
            }
         }
      }, new BlockBreakHandler(false, false) {
         public void onPlayerBreak(@NotNull BlockBreakEvent event, @NotNull ItemStack item, @NotNull List<ItemStack> drops) {
            CellConverter.this.onBreak(event);
         }
      }});
   }

   public void postRegister() {
      MenuShells.create(
         this,
         54,
         BACKGROUND_SLOTS,
         Icons.CONVERTER_BORDER,
         (block, player) -> player.hasPermission("slimefun.inventory.bypass")
               || this.canUse(player, false) && Slimefun.getProtectionManager().hasPermission(player, block.getLocation(), Interaction.INTERACT_BLOCK),
         flow -> flow == ItemTransportFlow.WITHDRAW ? OUTPUT_SLOTS : new int[0],
         (menu, block) -> {
            menu.replaceExistingItem(4, this.createCellSlotMarker());

            for (int slot : DISPLAY_SLOTS) {
               menu.replaceExistingItem(slot, Icons.CLEANER_DISPLAY);
            }

            this.setupSharedHandlers(menu);
            this.refresh(menu);
            menu.addMenuOpeningHandler(p -> this.refresh(menu));
            menu.addMenuCloseHandler(p -> {
               for (int slotx : DISPLAY_SLOTS) {
                  menu.replaceExistingItem(slotx, Icons.CLEANER_DISPLAY);
               }
            });
         }
      );
   }

   private void onBreak(@NotNull BlockBreakEvent event) {
      Location location = event.getBlock().getLocation();
      this.onBreakCleanup(location);
      this.exportMode.remove(location);
      this.refreshSignature.remove(location);
      BlockMenu blockMenu = StorageCacheUtils.getMenu(location);
      if (blockMenu != null) {
         ItemStack cellItem = blockMenu.getItemInSlot(4);
         if (cellItem != null && !this.isCellSlotMarker(cellItem)) {
            blockMenu.dropItems(location, new int[]{4});
         }

         blockMenu.dropItems(location, OUTPUT_SLOTS);
      }
   }

   private boolean isExportMode(@NotNull BlockMenu menu) {
      return this.exportMode.getOrDefault(menu.getLocation(), true);
   }

   @Nullable
   private CellConverter.CellContext resolveCellContext(@NotNull BlockMenu menu, int displaySlot) {
      ItemStack cellItem = menu.getItemInSlot(4);
      if (cellItem != null && StorageCell.isStorageCell(cellItem)) {
         long per = StorageCell.getPerTypeLimit(cellItem);
         CellLedger cache = StorageCell.loadCellCache(cellItem, per);
         int idx = this.displayIndex(menu, displaySlot);
         if (idx < 0) {
            return null;
         } else {
            List<CellLedger.CellEntry> entries = this.visibleEntries(menu, cache);
            return idx < entries.size() ? new CellConverter.CellContext(cellItem, per, cache, entries.get(idx)) : null;
         }
      } else {
         return null;
      }
   }

   @Nullable
   private static NetworkQuantumStorage resolveTargetStorage(long amount, long per) {
      return CellPersistence.isUnlimited(per) ? findStorageForAmount(amount) : CellTier.upgradeMaterialOf(per);
   }

   private void transferItem(@NotNull BlockMenu menu, @NotNull Player player, int displaySlot) {
      CellConverter.CellContext ctx = this.resolveCellContext(menu, displaySlot);
      if (ctx != null) {
         int outputSlot = findEmptyOutputSlot(menu);
         if (outputSlot < 0) {
            player.sendMessage(Lang.getString("messages.cellnet.converter.output_full"));
         } else {
            NetworkQuantumStorage qs = resolveTargetStorage(ctx.entry().amount, ctx.per());
            if (qs == null) {
               player.sendMessage(Lang.getString("messages.cellnet.converter.no_matching_storage"));
            } else {
               ItemStack qsItem = CellQuantumConverter.buildQuantumStorage(ctx.entry().sample, ctx.entry().amount, qs);
               menu.replaceExistingItem(outputSlot, qsItem);
               ctx.cache().takeItem(ctx.entry().sample, ctx.entry().amount);
               if (!CellPersistence.isUnlimited(ctx.per())) {
                  long current = StorageCell.getCurrentPerTypeLimit(ctx.cellItem());
                  if (current > 1L) {
                     CellPersistence.setCurrentPerTypeLimit(ctx.cellItem(), current - 1L);
                  }
               }

               this.refresh(menu);
               CellPersistence.saveAsync();
            }
         }
      }
   }

   @Nullable
   private static NetworkQuantumStorage findStorageForAmount(long amount) {
      return CellTier.storageForAmount(amount);
   }

   @Nullable
   private CellConverter.QuantumImport resolveQuantumImport(@NotNull BlockMenu menu, @NotNull Player player, int displaySlot) {
      ItemStack cellItem = menu.getItemInSlot(4);
      if (cellItem != null && StorageCell.isStorageCell(cellItem)) {
         long per = StorageCell.getPerTypeLimit(cellItem);
         CellLedger cache = StorageCell.loadCellCache(cellItem, per);
         int idx = this.displayIndex(menu, displaySlot);
         if (idx < 0) {
            return null;
         } else {
            List<Integer> qsSlots = this.visibleQuantumSlots(menu, listQuantumSlots(menu));
            if (idx >= qsSlots.size()) {
               return null;
            } else {
               int sourceSlot = qsSlots.get(idx);
               ItemStack qsItem = menu.getItemInSlot(sourceSlot);
               QuantumCache qc = Keys.getQuantumCache(qsItem.getItemMeta());
               if (qc != null && qc.getItemStack() != null && qc.getAmountLong() > 0L) {
                  NetworkQuantumStorage storage = asQuantumStorage(qsItem);
                  if (storage == null) {
                     player.sendMessage(Lang.getString("messages.cellnet.converter.tier_mismatch"));
                     return null;
                  } else {
                     return new CellConverter.QuantumImport(cellItem, cache, per, sourceSlot, qc, storage);
                  }
               } else {
                  player.sendMessage(Lang.getString("messages.cellnet.converter.no_quantum_storage"));
                  return null;
               }
            }
         }
      } else {
         return null;
      }
   }

   private static boolean passesImportLimits(@NotNull Player player, @NotNull CellLedger cache, @NotNull QuantumCache qc, long transferable, boolean brandNew) {
      if (transferable <= 0L) {
         player.sendMessage(Lang.getString("messages.cellnet.converter.item_capacity_full"));
         return false;
      } else if (brandNew && (long)cache.getStoredItems().size() >= Math.min(cache.getCurrentPerTypeLimit(), cache.getMaxUnits())) {
         player.sendMessage(Lang.getString("messages.cellnet.converter.units_full"));
         return false;
      } else if (cache.isWhitelistEnabled() && !isInCellWhitelist(cache, qc.getItemStack())) {
         player.sendMessage(Lang.getString("messages.cellnet.converter.whitelist_rejected"));
         return false;
      } else {
         return true;
      }
   }

   private static void writeBackQuantumSlot(
      @NotNull BlockMenu menu, @NotNull CellConverter.QuantumImport target, boolean upgrade, long transferable, long amount
   ) {
      if (upgrade) {
         CellPersistence.setCurrentPerTypeLimit(target.cellItem(), StorageCell.getCurrentPerTypeLimit(target.cellItem()) + 1L);
         menu.replaceExistingItem(target.sourceSlot(), null);
      } else if (amount - transferable <= 0L) {
         menu.replaceExistingItem(target.sourceSlot(), target.storage().getItem().clone());
      } else {
         menu.replaceExistingItem(
            target.sourceSlot(), CellQuantumConverter.buildQuantumStorage(target.qc().getItemStack(), amount - transferable, target.storage())
         );
      }
   }

   private void importItem(@NotNull BlockMenu menu, @NotNull Player player, int displaySlot) {
      CellConverter.QuantumImport target = this.resolveQuantumImport(menu, player, displaySlot);
      if (target != null) {
         CellLedger cache = target.cache();
         long per = target.per();
         ItemKey targetKey = new ItemKey(target.qc().getItemStack());
         long existing = cache.getAmount(targetKey);
         long amount = target.qc().getAmountLong();
         boolean brandNew = existing <= 0L;
         long remaining = existing > 0L ? per - existing : per;
         long transferable = Math.min(amount, remaining);
         if (passesImportLimits(player, cache, target.qc(), transferable, brandNew)) {
            boolean sameTier = target.storage() == CellTier.upgradeMaterialOf(per);
            boolean upgrade = brandNew && sameTier && amount <= remaining && cache.getCurrentPerTypeLimit() < cache.getMaxUnits();
            cache.pushItemLong(targetKey, transferable);
            writeBackQuantumSlot(menu, target, upgrade, transferable, amount);
            this.refresh(menu);
            CellPersistence.saveAsync();
         }
      }
   }

   private static int findEmptyOutputSlot(@NotNull BlockMenu menu) {
      for (int slot : OUTPUT_SLOTS) {
         ItemStack onSlot = menu.getItemInSlot(slot);
         if (onSlot == null || onSlot.getType().isAir()) {
            return slot;
         }
      }

      return -1;
   }

   private static List<Integer> listQuantumSlots(@NotNull BlockMenu menu) {
      List<Integer> slots = new ArrayList<>();

      for (int slot : OUTPUT_SLOTS) {
         ItemStack onSlot = menu.getItemInSlot(slot);
         if (onSlot != null && !onSlot.getType().isAir()) {
            QuantumCache qc = Keys.getQuantumCache(onSlot.getItemMeta());
            if (qc != null && qc.getAmountLong() > 0L) {
               slots.add(slot);
            }
         }
      }

      return slots;
   }

   private static boolean isInCellWhitelist(@NotNull CellLedger cache, @NotNull ItemStack sample) {
      for (ItemStack template : cache.getWhitelist()) {
         if (StackUtils.itemsMatch(template, sample)) {
            return true;
         }
      }

      return false;
   }

   @Nullable
   private static NetworkQuantumStorage asQuantumStorage(@NotNull ItemStack item) {
      return SlimefunItem.getByItem(item) instanceof NetworkQuantumStorage storage ? storage : null;
   }

   @NotNull
   private List<Integer> visibleQuantumSlots(@NotNull BlockMenu menu, @NotNull List<Integer> qsSlots) {
      String search = SEARCH_TERMS.get(menu.getLocation());
      if (search == null) {
         return qsSlots;
      } else {
         List<Integer> filtered = new ArrayList<>(qsSlots.size());

         for (int slot : qsSlots) {
            ItemStack qsItem = menu.getItemInSlot(slot);
            QuantumCache qc = qsItem == null ? null : Keys.getQuantumCache(qsItem.getItemMeta());
            if (qc != null && qc.getItemStack() != null && ItemSearch.matches(search, ItemStackHelper.getDisplayName(qc.getItemStack()))) {
               filtered.add(slot);
            }
         }

         return filtered;
      }
   }

   private void renderCellItems(@NotNull BlockMenu menu, @NotNull CellLedger cache) {
      List<CellLedger.CellEntry> entries = this.visibleEntries(menu, cache);
      int page = this.normalizePage(menu, entries.size());
      int start = page * this.itemsPerPage();
      int end = Math.min(start + this.itemsPerPage(), entries.size());

      for (int i = 0; i < DISPLAY_SLOTS.length; i++) {
         int slot = DISPLAY_SLOTS[i];
         if (i < end - start) {
            CellLedger.CellEntry entry = entries.get(start + i);
            menu.replaceExistingItem(
               slot, CellSlotUI.displayItem(entry.sample, entry.amount, "messages.cellnet.converter.item_count", "messages.cellnet.converter.transfer_hint")
            );
         } else {
            menu.replaceExistingItem(slot, entries.isEmpty() ? Icons.SEARCH_EMPTY : Icons.CLEANER_DISPLAY);
         }
      }
   }

   private void renderQuantumItems(@NotNull BlockMenu menu) {
      List<Integer> qsSlots = this.visibleQuantumSlots(menu, listQuantumSlots(menu));
      int page = this.normalizePage(menu, qsSlots.size());

      for (int i = 0; i < DISPLAY_SLOTS.length; i++) {
         int index = page * this.itemsPerPage() + i;
         if (index < qsSlots.size()) {
            ItemStack qsItem = menu.getItemInSlot(qsSlots.get(index));
            QuantumCache qc = qsItem == null ? null : Keys.getQuantumCache(qsItem.getItemMeta());
            if (qc != null && qc.getItemStack() != null && qc.getAmountLong() > 0L) {
               menu.replaceExistingItem(
                  DISPLAY_SLOTS[i],
                  CellSlotUI.displayItem(
                     qc.getItemStack(), qc.getAmountLong(), "messages.cellnet.converter.item_count", "messages.cellnet.converter.transfer_hint"
                  )
               );
               continue;
            }
         }

         menu.replaceExistingItem(DISPLAY_SLOTS[i], Icons.CLEANER_DISPLAY);
      }
   }

   @NotNull
   private static ItemStack buildInfoItem(@Nullable ItemStack cellItem, int types, int outputUsed) {
      ItemStack info = new ItemStack(Material.PAPER);
      ItemMeta meta = info.getItemMeta();
      if (meta != null) {
         List<String> lore = new ArrayList<>();
         meta.setDisplayName(Lang.getString("messages.cellnet.converter.info_title"));
         if (cellItem != null && StorageCell.isStorageCell(cellItem)) {
            lore.add(Lang.getString("messages.cellnet.converter.cell_name", ItemStackHelper.getDisplayName(cellItem)));
            lore.add(Lang.getString("messages.cellnet.converter.item_types", types));
         } else {
            lore.add(Lang.getString("messages.cellnet.converter.no_cell"));
         }

         lore.add(Lang.getString("messages.cellnet.converter.output_count", outputUsed, OUTPUT_SLOTS.length));
         meta.setLore(lore);
         info.setItemMeta(meta);
      }

      return info;
   }

   @NotNull
   private static ItemStack buildModeButton(boolean exporting) {
      return exporting ? Icons.CONVERTER_MODE_EXPORT : Icons.CONVERTER_MODE_IMPORT;
   }

   public static void applySearchFromChat(@NotNull Player player, @NotNull Location location, @NotNull String term) {
      applySearchFromChat(player, location, term, StorageCacheUtils::getMenu, sf -> sf instanceof CellConverter, menu -> {
         CellConverter converter = (CellConverter)StorageCacheUtils.getSfItem(location);
         if (converter != null) {
            converter.pageCache.put(location, 0);
            converter.refresh(menu);
         }
      });
   }

   private static record CellContext(@NotNull ItemStack cellItem, long per, @NotNull CellLedger cache, @NotNull CellLedger.CellEntry entry) {
   }

   private static record QuantumImport(
      @NotNull ItemStack cellItem, @NotNull CellLedger cache, long per, int sourceSlot, @NotNull QuantumCache qc, @NotNull NetworkQuantumStorage storage
   ) {
   }
}
