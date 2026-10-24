package com.ytdd9527.networksexpansion.implementation.machines.cellnet.converter;

import com.balugaq.netex.utils.Lang;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.StorageCell;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.ledger.CellLedger;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.ledger.CellPersistence;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.ChatInput;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.ui.CellSlotUI;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.ui.Icons;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.ui.MenuShells;
import io.github.sefiraat.networks.utils.Keys;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemHandler;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import java.util.ArrayList;
import java.util.List;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import net.guizhanss.minecraft.guizhanlib.gugu.minecraft.helpers.inventory.ItemStackHelper;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CellCleaner extends CellBrowserBase {
   public static final int[] DISPLAY_SLOTS = new int[]{
      9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44
   };
   public static final int[] BACKGROUND_SLOTS = new int[]{1, 2, 3, 5, 6, 7, 46, 47, 48, 49, 50, 51, 52, 53};
   private static final ItemStack CELL_SLOT_MARKER = buildCellSlotMarker();

   public CellCleaner(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe) {
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
      return "messages.cellnet.cleaner";
   }

   @Override
   protected boolean shouldHandleCellSlotClick(@NotNull ClickAction action) {
      return !action.isShiftClicked();
   }

   @NotNull
   @Override
   public ChatInput.SearchTarget searchTarget() {
      return ChatInput.SearchTarget.CELL_CLEANER;
   }

   @NotNull
   @Override
   public ItemStack createCellSlotMarker() {
      return CELL_SLOT_MARKER.clone();
   }

   @Override
   public boolean isCellSlotMarker(@Nullable ItemStack itemStack) {
      if (itemStack != null && itemStack.hasItemMeta()) {
         ItemMeta meta = itemStack.getItemMeta();
         return meta != null && meta.getPersistentDataContainer().has(Keys.CELL_CLEANER_MARKER, PersistentDataType.BOOLEAN);
      } else {
         return false;
      }
   }

   @NotNull
   private static ItemStack buildCellSlotMarker() {
      ItemStack marker = Icons.CLEANER_CELL_SLOT.clone();
      ItemMeta meta = marker.getItemMeta();
      if (meta != null) {
         meta.getPersistentDataContainer().set(Keys.CELL_CLEANER_MARKER, PersistentDataType.BOOLEAN, true);
         marker.setItemMeta(meta);
      }

      return marker;
   }

   @Override
   public void onDisplayClick(@NotNull BlockMenu menu, @NotNull Player player, int slot, @Nullable ItemStack item, @NotNull ClickAction action) {
      ItemStack cursor = player.getItemOnCursor();
      if (cursor == null || cursor.getType().isAir()) {
         if (action.isShiftClicked()) {
            this.deleteItem(menu, slot);
         }
      }
   }

   @Override
   public void onCellInserted(@NotNull BlockMenu menu, @NotNull ItemStack cellItem, @NotNull Player player) {
      if (CellSlotUI.prepareInsertedCell(player, cellItem)) {
         this.pageCache.put(menu.getLocation(), 0);
         menu.replaceExistingItem(4, cellItem);
         this.refresh(menu);
      }
   }

   @Nullable
   @Override
   public ItemStack onCellEjected(@NotNull BlockMenu menu, int slot) {
      return CellSlotUI.ejectCell(menu, slot, () -> CELL_SLOT_MARKER.clone(), DISPLAY_SLOTS, this.pageCache, () -> this.refresh(menu));
   }

   @Override
   public void refresh(@NotNull BlockMenu menu) {
      ItemStack cellItem = menu.getItemInSlot(4);
      if (cellItem != null && StorageCell.isStorageCell(cellItem)) {
         long per = StorageCell.getPerTypeLimit(cellItem);
         CellLedger cache = StorageCell.loadCellCache(cellItem, per);
         StorageCell.applyLore(cellItem, per, StorageCell.getCurrentPerTypeLimit(cellItem));
         menu.replaceExistingItem(4, cellItem);
         List<CellLedger.CellEntry> entries = this.visibleEntries(menu, cache);
         int page = this.normalizePage(menu, entries.size());
         int maxPages = Math.max(1, (int)Math.ceil((double)entries.size() / (double)this.itemsPerPage()));
         int start = page * this.itemsPerPage();
         int end = Math.min(start + this.itemsPerPage(), entries.size());

         for (int i = 0; i < DISPLAY_SLOTS.length; i++) {
            int slot = DISPLAY_SLOTS[i];
            if (i < end - start) {
               CellLedger.CellEntry entry = entries.get(start + i);
               menu.replaceExistingItem(
                  slot, CellSlotUI.displayItem(entry.sample, entry.amount, "messages.cellnet.cleaner.item_count", "messages.cellnet.cleaner.delete_hint")
               );
            } else {
               menu.replaceExistingItem(slot, entries.isEmpty() ? Icons.SEARCH_EMPTY : Icons.CLEANER_DISPLAY);
            }
         }

         menu.replaceExistingItem(0, this.pageButton(page > 0, false));
         menu.replaceExistingItem(8, this.pageButton(page < maxPages - 1, true));
         menu.replaceExistingItem(45, buildInfoItem(cellItem, entries.size()));
         this.wireSearchButton(menu);
      } else {
         menu.replaceExistingItem(4, CELL_SLOT_MARKER.clone());
         CellSlotUI.clearDisplay(menu, DISPLAY_SLOTS);
         menu.replaceExistingItem(45, buildInfoItem(null, 0));
         menu.replaceExistingItem(0, this.pageButton(false, false));
         menu.replaceExistingItem(8, this.pageButton(false, true));
      }
   }

   public void preRegister() {
      this.addItemHandler(new ItemHandler[]{new BlockBreakHandler(false, false) {
         public void onPlayerBreak(@NotNull BlockBreakEvent event, @NotNull ItemStack item, @NotNull List<ItemStack> drops) {
            CellCleaner.this.onBreak(event);
         }
      }});
   }

   public void postRegister() {
      MenuShells.create(
         this,
         54,
         BACKGROUND_SLOTS,
         (block, player) -> player.hasPermission("slimefun.inventory.bypass")
               || this.canUse(player, false) && Slimefun.getProtectionManager().hasPermission(player, block.getLocation(), Interaction.INTERACT_BLOCK),
         MenuShells::emptyTransport,
         (menu, block) -> {
            ItemStack existing = menu.getItemInSlot(4);
            if (existing == null || existing.getType().isAir()) {
               menu.replaceExistingItem(4, CELL_SLOT_MARKER.clone());
            }

            for (int slot : DISPLAY_SLOTS) {
               menu.replaceExistingItem(slot, Icons.CLEANER_DISPLAY);
            }

            this.setupSharedHandlers(menu);
            this.refresh(menu);
            menu.addMenuOpeningHandler(p -> this.refresh(menu));
            menu.addMenuCloseHandler(p -> this.clearDisplaySlots(menu));
         }
      );
   }

   private void onBreak(@NotNull BlockBreakEvent event) {
      Location location = event.getBlock().getLocation();
      this.onBreakCleanup(location);
      BlockMenu blockMenu = StorageCacheUtils.getMenu(location);
      if (blockMenu != null) {
         ItemStack cellItem = blockMenu.getItemInSlot(4);
         if (cellItem != null && StorageCell.isStorageCell(cellItem)) {
            blockMenu.dropItems(location, new int[]{4});
         }
      }
   }

   private void clearDisplaySlots(@NotNull BlockMenu menu) {
      for (int slot : DISPLAY_SLOTS) {
         menu.replaceExistingItem(slot, Icons.CLEANER_DISPLAY);
      }
   }

   private void deleteItem(@NotNull BlockMenu menu, int displaySlot) {
      ItemStack cellItem = menu.getItemInSlot(4);
      if (cellItem != null && StorageCell.isStorageCell(cellItem)) {
         long per = StorageCell.getPerTypeLimit(cellItem);
         CellLedger cache = StorageCell.loadCellCache(cellItem, per);
         int idx = this.displayIndex(menu, displaySlot);
         if (idx >= 0) {
            List<CellLedger.CellEntry> entries = this.visibleEntries(menu, cache);
            if (idx < entries.size()) {
               CellLedger.CellEntry target = entries.get(idx);
               cache.takeItem(target.sample, target.amount);
               menu.replaceExistingItem(4, cellItem);
               this.refresh(menu);
               CellPersistence.saveAsync();
            }
         }
      }
   }

   @NotNull
   private static ItemStack buildInfoItem(@Nullable ItemStack cellItem, int types) {
      ItemStack info = new ItemStack(Material.PAPER);
      ItemMeta meta = info.getItemMeta();
      if (meta != null) {
         List<String> lore = new ArrayList<>();
         meta.setDisplayName(Lang.getString("messages.cellnet.cleaner.info_title"));
         if (cellItem != null && StorageCell.isStorageCell(cellItem)) {
            lore.add(Lang.getString("messages.cellnet.cleaner.cell_name", ItemStackHelper.getDisplayName(cellItem)));
            lore.add(Lang.getString("messages.cellnet.cleaner.item_types", types));
         } else {
            lore.add(Lang.getString("messages.cellnet.cleaner.no_cell"));
         }

         meta.setLore(lore);
         info.setItemMeta(meta);
      }

      return info;
   }

   public static void applySearchFromChat(@NotNull Player player, @NotNull Location location, @NotNull String term) {
      applySearchFromChat(player, location, term, StorageCacheUtils::getMenu, sf -> sf instanceof CellCleaner, menu -> {
         CellCleaner cleaner = (CellCleaner)StorageCacheUtils.getSfItem(location);
         if (cleaner != null) {
            cleaner.pageCache.put(location, 0);
            cleaner.refresh(menu);
         }
      });
   }
}
