package com.ytdd9527.networksexpansion.implementation.machines.cellnet.drive;

import com.balugaq.netex.integrations.logitech.CellVirtualCache;
import com.balugaq.netex.integrations.logitech.LinkBindingStore;
import com.balugaq.netex.integrations.logitech.LinkerGrid;
import com.balugaq.netex.utils.Lang;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.core.items.SpecialSlimefunItem;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.CellHandle;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.ledger.CellPersistence;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.drive.menu.DriveWhitelistMenu;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.ChatInput;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.ItemHashMap;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.ItemKey;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.ItemSearch;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.NumberFormat;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.ui.BrowseUI;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.ui.CellMenuCommon;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.ui.Icons;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.ui.MenuShells;
import io.github.sefiraat.networks.Networks;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemHandler;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import net.guizhanss.minecraft.guizhanlib.gugu.minecraft.helpers.inventory.ItemStackHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CellDrive extends SpecialSlimefunItem {
   public static final int[] CELL_SLOTS = new int[]{10, 11, 12, 19, 20, 21, 28, 29, 30};
   public static final int CELL_SLOT_COUNT = CELL_SLOTS.length;
   public static final int DISPLAY_SLOT = 4;
   public static final int ACCESS_SLOT = 13;
   public static final int[] MAIN_BACKGROUND_SLOTS = new int[]{
      0, 1, 2, 3, 5, 6, 7, 8, 9, 14, 15, 16, 17, 18, 22, 23, 24, 25, 26, 27, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44
   };
   public static final int[] PREVIEW_BACKGROUND_SLOTS = new int[]{1, 2, 3, 5, 6, 7, 46, 47, 48, 49, 50, 52, 53};
   public static final int PREVIEW_BACK = 45;
   public static final int WHITELIST_BUTTON_SLOT = 39;
   private static final long DISPLAY_REFRESH_INTERVAL_MS = 2000L;
   static final Map<ItemKey, String> ITEM_DISPLAY_NAMES = new ConcurrentHashMap<>();
   private static final Map<ChestMenu, Map<Integer, ItemKey>> PREVIEW_KEYS = Collections.synchronizedMap(new WeakHashMap<>());
   static final String CELL_COUNT_TEMPLATE = Lang.getString("messages.cellnet.drive.cell_count");
   static final String TOTAL_ITEMS_TEMPLATE = Lang.getString("messages.cellnet.drive.total_items");
   static final String ITEM_TYPES_TEMPLATE = Lang.getString("messages.cellnet.drive.item_types");
   static final String ITEM_ENTRY_TEMPLATE = Lang.getString("messages.cellnet.drive.item_entry");
   static final String MORE_ITEMS_TEXT = Lang.getString("messages.cellnet.drive.more_items");
   private static final String DISPLAY_NAME_TEXT = Lang.getString("messages.cellnet.drive.display_name");
   private static final DriveStorage storage = new DriveStorage();
   private static final Map<Location, CellDrive.DriveState> STATES = new ConcurrentHashMap<>();

   private static CellDrive.DriveState state(@NotNull Location loc) {
      return STATES.computeIfAbsent(loc, k -> new CellDrive.DriveState());
   }

   @NotNull
   public static DriveStorage getStorage() {
      return storage;
   }

   public CellDrive(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe);
   }

   public void preRegister() {
      this.addItemHandler(new ItemHandler[]{new BlockPlaceHandler(false) {
         public void onPlayerPlace(@NotNull BlockPlaceEvent event) {
            DriveOwnership.writeOwner(event.getBlock().getLocation(), event.getPlayer().getUniqueId());
         }
      }});
      this.addItemHandler(new ItemHandler[]{new BlockTicker() {
         public boolean isSynchronized() {
            return false;
         }

         public void tick(@NotNull Block b, SlimefunItem item, SlimefunBlockData data) {
            Location location = b.getLocation();
            long now = System.currentTimeMillis();
            CellDrive.DriveState driveState = CellDrive.state(location);
            long last = driveState.displayRefreshTimestamp;
            boolean shouldRefresh = last == 0L || now - last >= 2000L;
            if (shouldRefresh) {
               driveState.displayRefreshTimestamp = now;
            }

            if (shouldRefresh) {
               Bukkit.getScheduler().runTask(Networks.getInstance(), () -> {
                  BlockMenu blockMenu = StorageCacheUtils.getMenu(location);
                  if (blockMenu != null && blockMenu.hasViewer()) {
                     CellDrive.this.updateMainDisplay(blockMenu);
                  }
               });
            }
         }
      }, new BlockBreakHandler(false, false) {
         public void onPlayerBreak(@NotNull BlockBreakEvent event, @NotNull ItemStack item, @NotNull List<ItemStack> drops) {
            if (!CellDrive.this.canBreak(event.getPlayer(), event.getBlock())) {
               event.setCancelled(true);
               event.getPlayer().sendMessage(Lang.getString("messages.cellnet.drive.break_not_allowed"));
            } else {
               CellDrive.this.onBreak(event);
            }
         }
      }});
   }

   public void postRegister() {
      MenuShells.create(
         this,
         45,
         MAIN_BACKGROUND_SLOTS,
         (block, player) -> DriveOwnership.bypasses(player)
               || DriveOwnership.passesGates(player, block.getLocation(), Interaction.INTERACT_BLOCK, this.canUse(player, false)),
         MenuShells::emptyTransport,
         (menu, block) -> {
            warmOwnerCache(block.getLocation());
            LinkBindingStore.replayDrive(block.getLocation());
            this.setupMainMenuHandlers(menu, block);
            this.updateMainDisplay(menu);
         }
      );
   }

   private static void warmOwnerCache(@NotNull Location location) {
      getOwnerUuid(location);
   }

   private void onBreak(@NotNull BlockBreakEvent event) {
      Location location = event.getBlock().getLocation();
      clearLocalState(location);
      CellUniqueness.unregisterDrive(location);
      storage.dropCellCache(location);
      CellVirtualCache.removeDrive(location);
      BlockMenu blockMenu = StorageCacheUtils.getMenu(location);
      if (blockMenu != null) {
         blockMenu.dropItems(location, CELL_SLOTS);
      }
   }

   static void clearLocalState(@NotNull Location location) {
      STATES.remove(location);
      DriveWhitelistMenu.invalidateButton(location);
      DriveOwnership.clearOwnerCache(location);
      LinkBindingStore.forget(location);
   }

   public static void saveAllDriveCells() {
      Networks.getInstance().getLogger().info(Lang.getString("messages.cellnet.drive.saving"));
      CellPersistence.saveAsync();
   }

   private void setupMainMenuHandlers(@NotNull BlockMenu menu, @NotNull Block block) {
      menu.addMenuClickHandler(4, (p, s, i, a) -> false);
      menu.addMenuClickHandler(13, (player, clickedSlot, item, action) -> {
         openItemAccessMenu(menu, block.getLocation(), player);
         return false;
      });
      menu.addMenuClickHandler(39, (player, clickedSlot, item, action) -> {
         DriveWhitelistMenu.open(menu, block.getLocation(), player);
         return false;
      });
      menu.addMenuCloseHandler(p -> {
         storage.invalidateCellCache(menu.getLocation());
         CellUniqueness.registerDrive(menu);
      });
      menu.addMenuOpeningHandler(player -> {
         for (int slot : CELL_SLOTS) {
            DriveCellSlots.refreshCellLore(menu, slot);
         }

         CellUniqueness.scanAndEjectDuplicates(menu, player);
      });
   }

   public static void openItemAccessMenu(@NotNull BlockMenu driveMenu, @NotNull Location location, @NotNull Player player) {
      state(location).page = 0;
      ChestMenu menu = new ChestMenu(Lang.getString("messages.cellnet.drive.preview_title"));
      menu.setPlayerInventoryClickable(true);

      for (int slot : PREVIEW_BACKGROUND_SLOTS) {
         menu.addItem(slot, ChestMenuUtils.getBackground(), (p, s, i, a) -> false);
      }

      menu.addItem(45, BrowseUI.backButton(), (p, s, i, a) -> {
         BlockMenu dm = StorageCacheUtils.getMenu(location);
         if (dm != null) {
            dm.open(new Player[]{p});
         }

         return false;
      });
      renderBrowser(menu, location);
      menu.setEmptySlotsClickable(false);
      menu.open(new Player[]{player});
   }

   private static void renderBrowser(@NotNull ChestMenu menu, @NotNull Location location) {
      BlockMenu driveMenu = StorageCacheUtils.getMenu(location);
      List<CellHandle> cells = driveMenu != null ? storage.getCells(driveMenu) : List.of();
      Map<ItemStack, Long> allItems = storage.getAllCellItems(cells);
      CellDrive.DriveState driveState = state(location);
      List<Entry<ItemStack, Long>> itemList = filterBrowserEntries(new ArrayList<>(allItems.entrySet()), driveState.search);
      int page = driveState.page;
      int totalPages = BrowseUI.totalPages(itemList.size());
      if (page >= totalPages) {
         page = totalPages - 1;
         driveState.page = page;
      }

      int start = page * BrowseUI.PAGE_SIZE;
      int end = Math.min(start + BrowseUI.PAGE_SIZE, itemList.size());
      Map<Integer, ItemKey> slotKeys = new ConcurrentHashMap<>();

      for (int i = 0; i < end - start; i++) {
         slotKeys.put(BrowseUI.LIST_SLOTS[i], new ItemKey(itemList.get(start + i).getKey()));
      }

      PREVIEW_KEYS.put(menu, slotKeys);
      BrowseUI.renderEntries(
         menu,
         itemList,
         start,
         end,
         entry -> CellMenuCommon.browseEntry(cells, entry.getKey(), entry.getValue()),
         (p, s, it, a) -> handleEntryToggle(p, s, a, driveMenu, menu, location)
      );
      BrowseUI.wirePager(menu, page, totalPages, target -> {
         driveState.page = target;
         renderBrowser(menu, location);
      });
      wireBrowserSearch(menu, location);
   }

   private static void handleEntryToggle(
      @NotNull Player player, int slot, @NotNull ClickAction action, @Nullable BlockMenu driveMenu, @NotNull ChestMenu menu, @NotNull Location location
   ) {
      if (action.isShiftClicked() && driveMenu != null) {
         ItemKey key = PREVIEW_KEYS.getOrDefault(menu, Map.of()).get(slot);
         if (key != null) {
            CellHandle owner = CellMenuCommon.findCellHolding(storage.getCells(driveMenu), key);
            if (owner != null && CellMenuCommon.toggleEntry(player, driveMenu, owner.getUuid(), key, action.isRightClicked())) {
               renderBrowser(menu, location);
            }
         }
      }
   }

   @NotNull
   private static List<Entry<ItemStack, Long>> filterBrowserEntries(@NotNull List<Entry<ItemStack, Long>> entries, @Nullable String search) {
      return ItemSearch.filterByDisplayName(search, entries, entry -> ItemStackHelper.getDisplayName(entry.getKey()));
   }

   private static void wireBrowserSearch(@NotNull ChestMenu menu, @NotNull Location location) {
      CellDrive.DriveState driveState = state(location);
      menu.replaceExistingItem(51, ItemSearch.searchIcon(driveState.search));
      menu.addMenuClickHandler(51, (player, s, i, a) -> {
         if (a.isRightClicked()) {
            driveState.search = null;
            driveState.page = 0;
            renderBrowser(menu, location);
            player.sendMessage(Lang.getString("messages.cellnet.search.cleared"));
         } else {
            ItemSearch.requestSearch(player, ChatInput.SearchTarget.DRIVE_BROWSER, location, null, null);
         }

         return false;
      });
   }

   public static void applyBrowserSearchFromChat(@NotNull Player player, @NotNull Location location, @NotNull String term) {
      if (term.isEmpty()) {
         state(location).search = null;
      } else {
         state(location).search = term;
      }

      player.sendMessage(Lang.getString(term.isEmpty() ? "messages.cellnet.search.cleared" : "messages.cellnet.search.set", term));
      BlockMenu driveMenu = StorageCacheUtils.getMenu(location);
      if (driveMenu != null) {
         openItemAccessMenu(driveMenu, location, player);
      }
   }

   private void updateMainDisplay(@NotNull BlockMenu menu) {
      long totalStored = 0L;
      int cellCount = 0;
      List<CellHandle> handles = storage.getCells(menu);

      for (CellHandle cell : handles) {
         totalStored += cell.getStoredCount();
         cellCount++;
      }

      ItemHashMap<Long> aggregatedItems = storage.getAllCellItemsKeyed(handles);
      menu.replaceExistingItem(4, buildDisplayItem(cellCount, totalStored, aggregatedItems));
      menu.replaceExistingItem(13, buildAccessButton());
      menu.replaceExistingItem(39, DriveWhitelistMenu.whitelistButton(menu.getLocation()));
   }

   private static ItemStack buildDisplayItem(int cellCount, long totalStored, @NotNull ItemHashMap<Long> aggregatedItems) {
      ItemStack display = new ItemStack(Material.PAPER);
      ItemMeta meta = display.getItemMeta();
      if (meta != null) {
         List<String> lore = new ArrayList<>();
         lore.add(MessageFormat.format(CELL_COUNT_TEMPLATE, cellCount, CELL_SLOT_COUNT));
         lore.add(MessageFormat.format(TOTAL_ITEMS_TEMPLATE, NumberFormat.formatNumber(totalStored)));
         lore.add(MessageFormat.format(ITEM_TYPES_TEMPLATE, aggregatedItems.size()));
         if (LinkerGrid.initialized) {
            lore.add(Lang.getString("messages.cellnet.drive.link_support"));
         }

         lore.add("");
         int count = 0;

         for (Entry<ItemKey, Long> entry : aggregatedItems.keyEntrySet()) {
            if (count >= 8) {
               lore.add(MORE_ITEMS_TEXT);
               break;
            }

            String name = ITEM_DISPLAY_NAMES.computeIfAbsent(entry.getKey(), k -> ItemStackHelper.getDisplayName(k.getItemStack()));
            lore.add(MessageFormat.format(ITEM_ENTRY_TEMPLATE, name, NumberFormat.formatNumber(entry.getValue())));
            count++;
         }

         meta.setLore(lore);
         meta.setDisplayName(DISPLAY_NAME_TEXT);
         display.setItemMeta(meta);
      }

      return display;
   }

   @NotNull
   private static ItemStack buildAccessButton() {
      return Icons.DRIVE_BROWSE;
   }

   @Nullable
   public static UUID getOwnerUuid(@NotNull Location location) {
      return DriveOwnership.getOwnerUuid(location);
   }

   private boolean canBreak(@NotNull Player player, @NotNull Block block) {
      return DriveOwnership.bypasses(player) || DriveOwnership.passesGates(player, block.getLocation(), Interaction.BREAK_BLOCK, this.canUse(player, false));
   }

   private static final class DriveState {
      int page;
      String search;
      long displayRefreshTimestamp;
   }
}
