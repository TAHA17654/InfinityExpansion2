package com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.menu;

import com.balugaq.netex.utils.Lang;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.CellTier;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.StorageCell;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.ledger.CellLedger;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.ledger.CellPersistence;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.rule.CellAcceptRules;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.ChatInput;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.ItemKey;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.ItemSearch;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.NumberFormat;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.ui.BrowseUI;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.ui.CellMenuCommon;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.ui.CellUI;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.ui.Icons;
import io.github.sefiraat.networks.network.stackcaches.QuantumCache;
import io.github.sefiraat.networks.slimefun.network.NetworkQuantumStorage;
import io.github.sefiraat.networks.utils.Keys;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import net.guizhanss.minecraft.guizhanlib.gugu.minecraft.helpers.inventory.ItemStackHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CellMenu {
   public static final int[] MAIN_BACKGROUND = new int[]{1, 2, 3, 4, 5, 6, 7, 46, 47, 48, 49, 50};
   public static final int[] LIST_SLOTS = new int[]{
      9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44
   };
   public static final int PAGE_SIZE = LIST_SLOTS.length;
   public static final int WHITELIST_BUTTON = 45;
   public static final int UPGRADE = 52;
   public static final int RENAME = 53;
   public static final int SEARCH_SLOT = 51;
   public static final int WHITELIST_TOGGLE = 4;
   public static final int WHITELIST_BACK = 49;
   public static final int[] WHITELIST_BACKGROUND = new int[]{0, 1, 2, 3, 5, 6, 7, 8, 45, 46, 47, 48, 50, 51, 52, 53};
   private static final Map<UUID, Integer> ITEM_PAGE = new HashMap<>();
   private static final Map<UUID, String> SEARCH_TERMS = new HashMap<>();
   private static final Map<UUID, UUID> OPENING_CELL = new ConcurrentHashMap<>();
   private static final Map<UUID, ChestMenu> OPENING_MENUS = new ConcurrentHashMap<>();
   private static final Map<UUID, UUID> RENAMING = new ConcurrentHashMap<>();
   private static final Map<UUID, Map<Integer, ItemKey>> RENDER_KEYS = new ConcurrentHashMap<>();

   private CellMenu() {
   }

   public static void markOpening(@NotNull UUID playerUuid, @NotNull UUID cellUuid) {
      OPENING_CELL.put(playerUuid, cellUuid);
   }

   public static void stopOpening(@NotNull UUID playerUuid) {
      UUID cellUuid = OPENING_CELL.remove(playerUuid);
      OPENING_MENUS.remove(playerUuid);
      if (cellUuid != null) {
         SEARCH_TERMS.remove(cellUuid);
         CellLedger cache = CellLedger.getActiveCaches().get(cellUuid);
         if (cache != null) {
            refreshHeldCell(playerUuid, cellUuid, cache);
         }

         removePage(cellUuid);
      }
   }

   @Nullable
   public static UUID getOpeningCell(@NotNull UUID playerUuid) {
      return OPENING_CELL.get(playerUuid);
   }

   private static boolean isOpenedByAnother(@NotNull UUID playerUuid, @NotNull UUID cellUuid) {
      for (Entry<UUID, UUID> entry : OPENING_CELL.entrySet()) {
         if (entry.getValue().equals(cellUuid) && !entry.getKey().equals(playerUuid)) {
            return true;
         }
      }

      return false;
   }

   private static void refreshHeldCell(@NotNull UUID playerUuid, @NotNull UUID cellUuid, @NotNull CellLedger cache) {
      Player player = Bukkit.getPlayer(playerUuid);
      if (player != null) {
         CellMenuCommon.refreshAndWriteBack(
            player, cellUuid, cell -> StorageCell.applyLore(cell, cache.getPerTypeLimit(), cache.getCurrentPerTypeLimit()), CellMenuCommon::writeBackToHand
         );
      }
   }

   @Nullable
   public static UUID getRenamingCell(@NotNull UUID playerUuid) {
      return RENAMING.get(playerUuid);
   }

   public static void stopRenaming(@NotNull UUID playerUuid) {
      RENAMING.remove(playerUuid);
   }

   public static void open(@NotNull Player player, @NotNull ItemStack cellItem) {
      UUID uuid = StorageCell.getOrCreateCellUUID(cellItem);
      initPages(uuid);
      openView(player, uuid);
   }

   private static void openView(@NotNull Player player, @NotNull UUID uuid) {
      if (isOpenedByAnother(player.getUniqueId(), uuid)) {
         player.sendMessage(Lang.getString("messages.cellnet.cell.menu.opened_by_another"));
      } else {
         CellLedger cache = getCache(uuid);
         if (cache != null) {
            ChestMenu menu = new ChestMenu(Lang.getString("messages.cellnet.cell.menu.title_view"));
            menu.setPlayerInventoryClickable(true);

            for (int slot : MAIN_BACKGROUND) {
               menu.addItem(slot, ChestMenuUtils.getBackground(), (p, s, i, a) -> false);
            }

            menu.addItem(45, CellUI.whitelistButton(cache), (p, s, i, a) -> {
               openWhitelist(p, uuid);
               return false;
            });
            menu.addItem(52, CellUI.upgradeButton(cache), (p, s, i, a) -> {
               handleUpgradeClick(menu, p, uuid);
               menu.replaceExistingItem(45, CellUI.whitelistButton(cache));
               return false;
            });
            menu.addItem(53, CellUI.renameButton(cache), (p, s, i, a) -> {
               p.closeInventory();
               RENAMING.put(p.getUniqueId(), uuid);
               p.sendMessage(Lang.getString("messages.cellnet.cell.rename_prompt"));
               return false;
            });
            menu.setEmptySlotsClickable(false);
            renderItems(menu, uuid);
            markOpening(player.getUniqueId(), uuid);
            registerCloseHandler(menu, player.getUniqueId());
            menu.open(new Player[]{player});
         }
      }
   }

   private static void openWhitelist(@NotNull Player player, @NotNull UUID uuid) {
      CellLedger cache = getCache(uuid);
      if (cache != null) {
         ChestMenu menu = new ChestMenu(Lang.getString("messages.cellnet.cell.menu.title_whitelist"));
         menu.setPlayerInventoryClickable(true);
         menu.addItem(51, ChestMenuUtils.getBackground(), (p, s, i, a) -> false);

         for (int slot : WHITELIST_BACKGROUND) {
            menu.addItem(slot, ChestMenuUtils.getBackground(), (p, s, i, a) -> false);
         }

         menu.addItem(4, CellUI.toggleButton(cache), (p, s, i, a) -> {
            boolean enabled = !cache.isWhitelistEnabled();
            applyWhitelist(p, uuid, enabled, cache.getWhitelist());
            menu.replaceExistingItem(4, CellUI.toggleButton(cache));
            renderWhitelist(menu, uuid);
            return false;
         });
         menu.addItem(49, Icons.BACK, (p, s, i, a) -> {
            openView(p, uuid);
            return false;
         });

         for (int slot : LIST_SLOTS) {
            menu.addMenuClickHandler(slot, (p, s, i, a) -> {
               handleWhitelistSlotClick(menu, p, slot, uuid);
               return false;
            });
         }

         menu.setEmptySlotsClickable(false);
         renderWhitelist(menu, uuid);
         registerCloseHandler(menu, player.getUniqueId());
         menu.open(new Player[]{player});
      }
   }

   private static void registerCloseHandler(@NotNull ChestMenu menu, @NotNull UUID playerUuid) {
      OPENING_MENUS.put(playerUuid, menu);
      menu.addMenuCloseHandler(p -> {
         if (OPENING_MENUS.get(playerUuid) == menu) {
            stopOpening(playerUuid);
         }
      });
   }

   private static void renderItems(@NotNull ChestMenu menu, @NotNull UUID uuid) {
      CellLedger cache = getCache(uuid);
      if (cache != null) {
         List<Entry<ItemStack, Long>> items = new ArrayList<>(cache.getAllItems().entrySet());
         String search = SEARCH_TERMS.get(uuid);
         if (search != null) {
            items = filterEntries(items, search);
         }

         int totalPages = BrowseUI.totalPages(items.size());
         int page = Math.min(getItemPage(uuid), totalPages - 1);
         setItemPage(uuid, page);
         int start = page * PAGE_SIZE;
         int end = Math.min(start + PAGE_SIZE, items.size());
         renderListSlots(menu, uuid, cache, items, start, end);
         wirePageButtons(menu, uuid, page, totalPages);
         wireSearchButton(menu, uuid, search, items.isEmpty());
      }
   }

   @NotNull
   private static List<Entry<ItemStack, Long>> filterEntries(@NotNull List<Entry<ItemStack, Long>> entries, @NotNull String search) {
      List<Entry<ItemStack, Long>> filtered = new ArrayList<>(entries.size());

      for (Entry<ItemStack, Long> entry : entries) {
         if (ItemSearch.matches(search, ItemStackHelper.getDisplayName(entry.getKey()))) {
            filtered.add(entry);
         }
      }

      return filtered;
   }

   private static void wireSearchButton(@NotNull ChestMenu menu, @NotNull UUID cellUuid, @Nullable String search, boolean empty) {
      if (empty) {
         menu.replaceExistingItem(LIST_SLOTS[0], Icons.SEARCH_EMPTY);
      }

      menu.replaceExistingItem(51, ItemSearch.searchIcon(search));
      menu.addMenuClickHandler(51, (player, s, i, a) -> {
         if (a.isRightClicked()) {
            SEARCH_TERMS.remove(cellUuid);
            setItemPage(cellUuid, 0);
            renderItems(menu, cellUuid);
            player.sendMessage(Lang.getString("messages.cellnet.search.cleared"));
         } else {
            ItemSearch.requestSearch(player, ChatInput.SearchTarget.CELL_MENU, player.getLocation(), player.getUniqueId(), cellUuid);
         }

         return false;
      });
   }

   public static void applySearchFromChat(@NotNull Player player, @Nullable UUID cellUuid, @NotNull String term) {
      if (cellUuid != null) {
         if (term.isEmpty()) {
            SEARCH_TERMS.remove(cellUuid);
         } else {
            SEARCH_TERMS.put(cellUuid, term);
         }

         player.sendMessage(Lang.getString(term.isEmpty() ? "messages.cellnet.search.cleared" : "messages.cellnet.search.set", term));
         ItemStack cell = CellMenuCommon.findCellInPlayer(player, cellUuid);
         if (cell != null) {
            open(player, cell);
         }
      }
   }

   private static void renderListSlots(
      @NotNull ChestMenu menu, @NotNull UUID uuid, @NotNull CellLedger cache, @NotNull List<Entry<ItemStack, Long>> items, int start, int end
   ) {
      Map<Integer, ItemKey> slotKeys = RENDER_KEYS.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>());

      for (int i = 0; i < LIST_SLOTS.length; i++) {
         int slot = LIST_SLOTS[i];
         if (i < end - start) {
            Entry<ItemStack, Long> entry = items.get(start + i);
            slotKeys.put(slot, new ItemKey(entry.getKey()));
            menu.replaceExistingItem(slot, CellUI.displayItem(cache, entry.getKey(), entry.getValue()));
         } else {
            slotKeys.remove(slot);
            menu.replaceExistingItem(slot, Icons.PREVIEW_FILL);
         }

         menu.addMenuClickHandler(slot, (p, s, it, a) -> {
            if (a.isShiftClicked()) {
               ItemKey key = RENDER_KEYS.getOrDefault(uuid, Map.of()).get(s);
               if (key != null) {
                  CellMenuCommon.toggleEntry(p, null, uuid, key, a.isRightClicked());
               }

               renderItems(menu, uuid);
            }

            return false;
         });
      }
   }

   private static void wirePageButtons(@NotNull ChestMenu menu, @NotNull UUID uuid, int page, int totalPages) {
      BrowseUI.wirePager(menu, page, totalPages, target -> {
         setItemPage(uuid, target);
         renderItems(menu, uuid);
      });
   }

   private static void handleUpgradeClick(@NotNull ChestMenu menu, @NotNull Player player, @NotNull UUID uuid) {
      CellLedger cache = getCache(uuid);
      if (cache != null) {
         if (cache.getMaxUnits() != Long.MAX_VALUE) {
            long maxUnits = cache.getMaxUnits();
            if (cache.getCurrentPerTypeLimit() >= maxUnits) {
               player.sendMessage(Lang.getString("messages.cellnet.cell.upgrade_maxed", NumberFormat.formatNumber(maxUnits)));
            } else {
               NetworkQuantumStorage required = CellTier.upgradeMaterialOf(cache.getPerTypeLimit());
               if (required == null) {
                  player.sendMessage(Lang.getString("messages.cellnet.cell.upgrade_no_material"));
               } else {
                  ItemStack cell = CellMenuCommon.findCellInPlayer(player, uuid);
                  if (cell == null) {
                     player.sendMessage(Lang.getString("messages.cellnet.cell.cell_not_in_inventory"));
                  } else if (!consumeUpgradeMaterial(player, required)) {
                     player.sendMessage(Lang.getString("messages.cellnet.cell.upgrade_missing", ItemStackHelper.getDisplayName(required.getItem())));
                  } else {
                     long newCurrent = applyUpgrade(cache, cell, maxUnits);
                     CellMenuCommon.writeBackLocated(player, cell);
                     menu.replaceExistingItem(52, CellUI.upgradeButton(cache));
                     player.sendMessage(
                        Lang.getString("messages.cellnet.cell.upgrade_success", NumberFormat.formatNumber(newCurrent), NumberFormat.formatNumber(maxUnits))
                     );
                  }
               }
            }
         }
      }
   }

   private static long applyUpgrade(@NotNull CellLedger cache, @NotNull ItemStack cell, long maxUnits) {
      long newCurrent = Math.min(maxUnits, cache.getCurrentPerTypeLimit() + 1L);
      CellPersistence.setCurrentPerTypeLimit(cell, newCurrent);
      CellLore.applySpecLore(cell, CellPersistence.getPerTypeLimit(cell), newCurrent);
      return newCurrent;
   }

   private static boolean consumeUpgradeMaterial(@NotNull Player player, @NotNull NetworkQuantumStorage required) {
      ItemStack[] contents = player.getInventory().getStorageContents();

      for (int i = 0; i < contents.length; i++) {
         ItemStack stack = contents[i];
         if (stack != null && !stack.getType().isAir()) {
            SlimefunItem stackSf = SlimefunItem.getByItem(stack);
            if (stackSf == required && StackUtils.itemsMatch(required.getItem(), stack, true) && isEmptyQuantumStorage(stack)) {
               stack.setAmount(stack.getAmount() - 1);
               player.getInventory().setItem(i, stack);
               return true;
            }
         }
      }

      return false;
   }

   private static boolean isEmptyQuantumStorage(@NotNull ItemStack stack) {
      if (!stack.hasItemMeta()) {
         return true;
      } else {
         ItemMeta meta = stack.getItemMeta();
         if (meta == null) {
            return true;
         } else {
            QuantumCache quantumCache = Keys.getQuantumCache(meta);
            return quantumCache == null || quantumCache.getItemStack() == null || quantumCache.getAmountLong() == 0L;
         }
      }
   }

   private static void renderWhitelist(@NotNull ChestMenu menu, @NotNull UUID uuid) {
      CellLedger cache = getCache(uuid);
      if (cache != null) {
         List<ItemStack> whitelist = cache.getWhitelist();

         for (int i = 0; i < LIST_SLOTS.length; i++) {
            int slot = LIST_SLOTS[i];
            if (i < whitelist.size()) {
               menu.replaceExistingItem(slot, CellUI.whitelistSlotItem(whitelist.get(i)));
            } else {
               menu.replaceExistingItem(slot, CellUI.settingSlotItem());
            }
         }

         menu.replaceExistingItem(4, CellUI.toggleButton(cache));
         menu.replaceExistingItem(49, Icons.BACK);
      }
   }

   private static void handleWhitelistSlotClick(@NotNull ChestMenu menu, @NotNull Player player, int slot, @NotNull UUID uuid) {
      CellLedger cache = getCache(uuid);
      if (cache != null) {
         int index = whitelistIndexForSlot(slot);
         if (index >= 0) {
            List<ItemStack> whitelist = cache.getWhitelist();
            ItemStack cursor = player.getItemOnCursor();
            boolean cursorAir = cursor == null || cursor.getType().isAir();
            if (index < whitelist.size()) {
               whitelist.remove(index);
               applyWhitelist(player, uuid, cache.isWhitelistEnabled(), whitelist);
            } else if (!cursorAir) {
               long limit = Math.min(cache.getCurrentPerTypeLimit(), (long)LIST_SLOTS.length);
               if ((long)whitelist.size() >= limit) {
                  player.sendMessage(Lang.getString("messages.cellnet.cell.whitelist_limit", NumberFormat.formatNumber(limit)));
               } else if (!inWhitelist(whitelist, cursor)) {
                  if (!CellAcceptRules.isUsedCell(cursor) && !CellAcceptRules.isFilledContainer(cursor) && !CellAcceptRules.isNbtOversized(cursor)) {
                     ItemStack template = cursor.clone();
                     template.setAmount(1);
                     whitelist.add(template);
                     applyWhitelist(player, uuid, cache.isWhitelistEnabled(), whitelist);
                  } else {
                     player.sendMessage(Lang.getString("messages.cellnet.cell.whitelist_not_allowed"));
                  }
               }
            }

            renderWhitelist(menu, uuid);
         }
      }
   }

   private static void applyWhitelist(@NotNull Player player, @NotNull UUID uuid, boolean enabled, @NotNull List<ItemStack> whitelist) {
      CellLedger cache = getCache(uuid);
      if (cache != null) {
         cache.updateWhitelist(enabled, whitelist);
      }

      ItemStack cell = CellMenuCommon.findCellInPlayer(player, uuid);
      if (cell != null) {
         CellMenuCommon.writeBackLocated(player, cell);
      }
   }

   private static boolean inWhitelist(@NotNull List<ItemStack> list, @NotNull ItemStack sample) {
      for (ItemStack item : list) {
         if (StackUtils.itemsMatch(item, sample)) {
            return true;
         }
      }

      return false;
   }

   @Nullable
   private static CellLedger getCache(@NotNull UUID uuid) {
      return CellLedger.getActiveCaches().get(uuid);
   }

   private static int getItemPage(@NotNull UUID uuid) {
      return ITEM_PAGE.getOrDefault(uuid, 0);
   }

   private static void setItemPage(@NotNull UUID uuid, int page) {
      ITEM_PAGE.put(uuid, page);
   }

   private static void initPages(@NotNull UUID uuid) {
      ITEM_PAGE.put(uuid, 0);
   }

   private static void removePage(@NotNull UUID uuid) {
      ITEM_PAGE.remove(uuid);
      RENDER_KEYS.remove(uuid);
   }

   private static int whitelistIndexForSlot(int slot) {
      for (int i = 0; i < LIST_SLOTS.length; i++) {
         if (LIST_SLOTS[i] == slot) {
            return i;
         }
      }

      return -1;
   }
}
