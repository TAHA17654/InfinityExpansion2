package io.github.sefiraat.networks.slimefun.network.grid;

import com.balugaq.netex.utils.Lang;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import lombok.Generated;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public class GridCache {
   @NotNull
   private final List<ItemStack> pullItemHistory = new ArrayList<>();
   private int page;
   private int maxPages;
   @NotNull
   private GridCache.DisplayMode displayMode;
   @NotNull
   private GridCache.SortOrder sortOrder;
   @Nullable
   private String filter;
   @Nullable
   private List<Entry<ItemStack, Long>> entriesCache;

   public GridCache(int page, int maxPages, @NotNull GridCache.SortOrder sortOrder) {
      this.page = page;
      this.maxPages = maxPages;
      this.sortOrder = sortOrder;
      this.displayMode = GridCache.DisplayMode.DISPLAY;
   }

   public void addPullItemHistory(@Nullable ItemStack itemStack) {
      if (itemStack != null) {
         this.getPullItemHistory().remove(itemStack);
         this.getPullItemHistory().add(0, itemStack);
      }
   }

   public void toggleDisplayMode() {
      if (this.displayMode == GridCache.DisplayMode.DISPLAY) {
         this.displayMode = GridCache.DisplayMode.HISTORY;
      } else {
         this.displayMode = GridCache.DisplayMode.DISPLAY;
      }
   }

   @NotNull
   @Generated
   public List<ItemStack> getPullItemHistory() {
      return this.pullItemHistory;
   }

   @NotNull
   @Generated
   public GridCache.DisplayMode getDisplayMode() {
      return this.displayMode;
   }

   @NotNull
   @Generated
   public GridCache.SortOrder getSortOrder() {
      return this.sortOrder;
   }

   @Nullable
   @Generated
   public String getFilter() {
      return this.filter;
   }

   @Nullable
   @Generated
   public List<Entry<ItemStack, Long>> getEntriesCache() {
      return this.entriesCache;
   }

   @Generated
   public void setDisplayMode(@NotNull GridCache.DisplayMode displayMode) {
      if (displayMode == null) {
         throw new NullPointerException("displayMode is marked non-null but is null");
      } else {
         this.displayMode = displayMode;
      }
   }

   @Generated
   public void setSortOrder(@NotNull GridCache.SortOrder sortOrder) {
      if (sortOrder == null) {
         throw new NullPointerException("sortOrder is marked non-null but is null");
      } else {
         this.sortOrder = sortOrder;
      }
   }

   @Generated
   public void setFilter(@Nullable String filter) {
      this.filter = filter;
   }

   @Generated
   public void setEntriesCache(@Nullable List<Entry<ItemStack, Long>> entriesCache) {
      this.entriesCache = entriesCache;
   }

   @Generated
   public void setPage(int page) {
      this.page = page;
   }

   @Generated
   public int getPage() {
      return this.page;
   }

   @Generated
   public void setMaxPages(int maxPages) {
      this.maxPages = maxPages;
   }

   @Generated
   public int getMaxPages() {
      return this.maxPages;
   }

   public static enum DisplayMode {
      DISPLAY,
      HISTORY;
   }

   public static enum SortOrder {
      ALPHABETICAL,
      NUMBER,
      NUMBER_REVERSE,
      ADDON;

      @NotNull
      public GridCache.SortOrder next(@Range(from = 1L,to = 4L) int limit) {
         return this.next().ordinal() + 1 >= limit ? ALPHABETICAL : this.next();
      }

      @NotNull
      public GridCache.SortOrder next() {
         return switch (this) {
            case ALPHABETICAL -> NUMBER;
            case NUMBER -> NUMBER_REVERSE;
            case NUMBER_REVERSE -> ADDON;
            case ADDON -> ALPHABETICAL;
         };
      }

      @NotNull
      public GridCache.SortOrder previous(@Range(from = 1L,to = 4L) int limit) {
         return this.previous().ordinal() + 1 >= limit ? values()[limit - 1] : this.previous();
      }

      @NotNull
      public GridCache.SortOrder previous() {
         return switch (this) {
            case ALPHABETICAL -> ADDON;
            case NUMBER -> ALPHABETICAL;
            case NUMBER_REVERSE -> NUMBER;
            case ADDON -> NUMBER_REVERSE;
         };
      }

      public String getTranslationName() {
         return Lang.getString("messages.completed-operation.grid.sort_orders." + this.name().toLowerCase());
      }
   }
}
