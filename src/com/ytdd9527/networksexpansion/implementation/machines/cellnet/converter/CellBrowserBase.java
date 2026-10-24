package com.ytdd9527.networksexpansion.implementation.machines.cellnet.converter;

import com.balugaq.netex.utils.Lang;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.core.items.SpecialSlimefunItem;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.StorageCell;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.ledger.CellLedger;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.ChatInput;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.ItemSearch;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.ui.CellSlotUI;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.ui.Icons;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import net.guizhanss.minecraft.guizhanlib.gugu.minecraft.helpers.inventory.ItemStackHelper;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class CellBrowserBase extends SpecialSlimefunItem {
   public static final int CELL_SLOT = 4;
   public static final int PREV = 0;
   public static final int NEXT = 8;
   public static final int INFO = 45;
   public static final int SEARCH_SLOT = 53;
   protected static final Map<Location, String> SEARCH_TERMS = new ConcurrentHashMap<>();
   protected final Map<Location, Integer> pageCache = new ConcurrentHashMap<>();

   public CellBrowserBase(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe);
   }

   public abstract int[] displaySlots();

   public abstract int[] backgroundSlots();

   @NotNull
   public abstract String langPrefix();

   @NotNull
   public abstract ChatInput.SearchTarget searchTarget();

   @NotNull
   public abstract ItemStack createCellSlotMarker();

   public abstract boolean isCellSlotMarker(@Nullable ItemStack var1);

   public abstract void onDisplayClick(@NotNull BlockMenu var1, @NotNull Player var2, int var3, @Nullable ItemStack var4, @NotNull ClickAction var5);

   public abstract void onCellInserted(@NotNull BlockMenu var1, @NotNull ItemStack var2, @NotNull Player var3);

   @Nullable
   public abstract ItemStack onCellEjected(@NotNull BlockMenu var1, int var2);

   public abstract void refresh(@NotNull BlockMenu var1);

   protected boolean shouldHandleCellSlotClick(@NotNull ClickAction action) {
      return true;
   }

   protected void setupMachineHandlers(@NotNull BlockMenu menu) {
   }

   protected int itemsPerPage() {
      return this.displaySlots().length;
   }

   protected void onBreakCleanup(@NotNull Location location) {
      this.pageCache.remove(location);
      SEARCH_TERMS.remove(location);
   }

   @NotNull
   protected List<CellLedger.CellEntry> visibleEntries(@NotNull BlockMenu menu, @NotNull CellLedger cache) {
      return ItemSearch.filterByDisplayName(SEARCH_TERMS.get(menu.getLocation()), cache.getStoredItems(), entry -> ItemStackHelper.getDisplayName(entry.sample));
   }

   protected int normalizePage(@NotNull BlockMenu menu, int total) {
      return CellSlotUI.normalizedPage(this.pageCache, menu.getLocation(), total, this.itemsPerPage());
   }

   protected int maxPages(@NotNull BlockMenu menu) {
      ItemStack cellItem = menu.getItemInSlot(4);
      if (cellItem != null && StorageCell.isStorageCell(cellItem)) {
         long per = StorageCell.getPerTypeLimit(cellItem);
         CellLedger cache = StorageCell.loadCellCache(cellItem, per);
         int count = this.visibleEntries(menu, cache).size();
         return Math.max(1, (int)Math.ceil((double)count / (double)this.itemsPerPage()));
      } else {
         return 1;
      }
   }

   @NotNull
   protected ItemStack pageButton(boolean enabled, boolean next) {
      String p = this.langPrefix();
      return CellSlotUI.pageButton(enabled, next, p + ".prev_page", p + ".first_page", p + ".next_page", p + ".last_page");
   }

   protected void wireSearchButton(@NotNull BlockMenu menu) {
      menu.replaceExistingItem(53, ItemSearch.searchIcon(SEARCH_TERMS.get(menu.getLocation())));
      menu.addMenuClickHandler(53, (player, slot, item, action) -> {
         if (action.isRightClicked()) {
            SEARCH_TERMS.remove(menu.getLocation());
            this.pageCache.put(menu.getLocation(), 0);
            this.refresh(menu);
            player.sendMessage(Lang.getString("messages.cellnet.search.cleared"));
         } else {
            ItemSearch.requestSearch(player, this.searchTarget(), menu.getLocation(), null, null);
         }

         return false;
      });
   }

   protected static void applySearchFromChat(
      @NotNull Player player,
      @NotNull Location location,
      @NotNull String term,
      @NotNull Function<Location, BlockMenu> menuGetter,
      @NotNull Predicate<SlimefunItem> typeCheck,
      @NotNull Consumer<BlockMenu> onFound
   ) {
      if (term.isEmpty()) {
         SEARCH_TERMS.remove(location);
      } else {
         SEARCH_TERMS.put(location.clone(), term);
      }

      player.sendMessage(Lang.getString(term.isEmpty() ? "messages.cellnet.search.cleared" : "messages.cellnet.search.set", term));
      BlockMenu menu = menuGetter.apply(location);
      SlimefunItem sf = StorageCacheUtils.getSfItem(location);
      if (menu != null && typeCheck.test(sf)) {
         onFound.accept(menu);
         menu.open(new Player[]{player});
      }
   }

   protected void setupSharedHandlers(@NotNull BlockMenu menu) {
      menu.addMenuClickHandler(
         4,
         (player, slot, item, action) -> !this.shouldHandleCellSlotClick(action)
               ? true
               : CellSlotUI.handleCellSlotClick(
                  menu, slot, item, player, this::isCellSlotMarker, (m, placed) -> this.onCellInserted(m, placed, player), this::onCellEjected
               )
      );

      for (int displaySlot : this.displaySlots()) {
         menu.addMenuClickHandler(displaySlot, (player, slot, item, action) -> {
            if (!isPlaceholder(item)) {
               this.onDisplayClick(menu, player, slot, item, action);
            }

            return false;
         });
      }

      this.setupMachineHandlers(menu);
      CellSlotUI.wirePageButtons(menu, 0, 8, this.pageCache, this::maxPages, this::refresh);
      menu.addMenuClickHandler(45, (player, slot, item, action) -> false);
   }

   protected static boolean isPlaceholder(@Nullable ItemStack item) {
      return item == null || item.getType().isAir() || item.getType() == Icons.CLEANER_DISPLAY.getType();
   }

   protected int displayIndex(@NotNull BlockMenu menu, int displaySlot) {
      int page = this.pageCache.getOrDefault(menu.getLocation(), 0);
      return CellSlotUI.pageIndex(this.displaySlots(), displaySlot, page, this.itemsPerPage());
   }
}
