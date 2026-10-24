package io.github.sefiraat.networks.slimefun.network.grid;

import com.balugaq.netex.api.algorithm.Sorters;
import com.balugaq.netex.api.enums.FeedbackType;
import com.balugaq.netex.api.helpers.Icon;
import com.balugaq.netex.api.visual.Screen;
import com.balugaq.netex.utils.InventoryUtil;
import com.balugaq.netex.utils.Lang;
import com.github.houbb.pinyin.constant.enums.PinyinStyleEnum;
import com.github.houbb.pinyin.util.PinyinHelper;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.utils.TextUtil;
import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.network.GridItemRequest;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.NodeDefinition;
import io.github.sefiraat.networks.network.NodeType;
import io.github.sefiraat.networks.slimefun.network.NetworkObject;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.sefiraat.networks.utils.Theme;
import io.github.thebusybiscuit.slimefun4.api.exceptions.IncompatibleItemHandlerException;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemHandler;
import io.github.thebusybiscuit.slimefun4.api.items.ItemSetting;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.items.settings.IntRangeSetting;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.utils.ChatUtils;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import javax.annotation.ParametersAreNonnullByDefault;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import net.guizhanss.minecraft.guizhanlib.gugu.minecraft.helpers.inventory.ItemStackHelper;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public abstract class AbstractGrid extends NetworkObject {
   protected static final String BS_FILTER_KEY = "filter";
   private static final Map<GridCache.SortOrder, Comparator<? super Entry<ItemStack, Long>>> SORT_MAP = new HashMap<>();
   @NotNull
   protected final ItemSetting<Integer> tickRate;

   protected AbstractGrid(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe) {
      this(itemGroup, item, recipeType, recipe, NodeType.GRID);
   }

   protected AbstractGrid(
      @NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe, NodeType type
   ) {
      super(itemGroup, item, recipeType, recipe, type);
      if (this.getInputSlot() != -1) {
         this.getSlotsToDrop().add(this.getInputSlot());
      }

      this.tickRate = new IntRangeSetting(this, "tick_rate", 1, 1, 10);
      this.addItemSetting(new ItemSetting[]{this.tickRate});
      this.addItemHandler(new ItemHandler[]{new BlockTicker() {
         private int tick = 1;

         public boolean isSynchronized() {
            return false;
         }

         public void tick(@NotNull Block block, SlimefunItem item, @NotNull SlimefunBlockData data) {
            if (this.tick <= 1) {
               BlockMenu blockMenu = data.getBlockMenu();
               if (blockMenu == null) {
                  return;
               }

               AbstractGrid.this.addToRegistry(block);
               AbstractGrid.this.syncSkin(block);
               AbstractGrid.this.tryAddItem(blockMenu);
               GridCache cache = AbstractGrid.this.getCacheMap().get(block.getLocation());
               if (cache != null) {
                  cache.setEntriesCache(null);
                  AbstractGrid.this.updateDisplay(Screen.of(blockMenu));
               }
            }
         }

         public void uniqueTick() {
            this.tick = this.tick <= 1 ? (Integer)AbstractGrid.this.tickRate.getValue() : this.tick - 1;
         }

         @NotNull
         public Optional<IncompatibleItemHandlerException> validate(SlimefunItem item) {
            return Optional.empty();
         }
      }});
   }

   protected void syncSkin(@NotNull Block block) {
   }

   @NotNull
   private static List<String> getLoreAddition(long amount) {
      MessageFormat format = new MessageFormat(Lang.getString("messages.normal-operation.grid.item_amount"), Locale.ROOT);
      return List.of("", format.format(new Object[]{Theme.CLICK_INFO.getColor(), Theme.PASSIVE.getColor(), amount}, new StringBuffer(), null).toString());
   }

   protected void tryAddItem(@NotNull BlockMenu blockMenu) {
      if (this.getInputSlot() != -1) {
         ItemStack itemStack = blockMenu.getItemInSlot(this.getInputSlot());
         if (itemStack != null && itemStack.getType() != Material.AIR) {
            NodeDefinition definition = NetworkStorage.getNode(blockMenu.getLocation());
            if (definition != null && definition.getNode() != null) {
               definition.getNode().getRoot().addItemStack(blockMenu.getLocation(), itemStack);
            }
         }
      }
   }

   public ItemStack getFilterStack(@Nullable String filter) {
      if (filter == null) {
         return this.getFilterStack();
      } else {
         ItemStack clone = this.getFilterStack().clone();
         clone.setLore(List.of(String.format(Lang.getString("messages.normal-operation.grid.filter"), filter)));
         return clone;
      }
   }

   public ItemStack getSortOrderStack(GridCache.SortOrder sortOrder) {
      ItemStack clone = this.getChangeSortStack().clone();
      clone.setLore(List.of(sortOrder.getTranslationName()));
      return clone;
   }

   protected void updateDisplay(@NotNull BlockMenu menu) {
      this.updateDisplay(Screen.of(menu));
   }

   protected void updateDisplay(@NotNull Screen screen) {
      Location location = screen.getLocation();
      if (!screen.hasViewer()) {
         this.sendFeedback(location, FeedbackType.AFK);
      } else {
         NodeDefinition definition = NetworkStorage.getNode(location);
         if (definition != null && definition.getNode() != null) {
            BlockMenu bm = StorageCacheUtils.getMenu(location);
            if (bm != null) {
               NetworkRoot root = definition.getNode().getRoot();
               GridCache gridCache = this.getCacheMap().get(screen.getLocation().clone());
               List<Entry<ItemStack, Long>> entries = this.getEntries(root, gridCache);
               int pages = (int)Math.ceil((double)entries.size() / (double)this.getDisplaySlots().length) - 1;
               gridCache.setMaxPages(pages);
               if (pages < 0) {
                  this.clearDisplay(screen);
               } else {
                  screen.setItem(this.getChangeSort(), this.getSortOrderStack(gridCache.getSortOrder()));
                  screen.setItem(this.getFilterSlot(), this.getFilterStack(gridCache.getFilter()));
                  if (gridCache.getPage() > pages) {
                     gridCache.setPage(pages);
                  }

                  int start = gridCache.getPage() * this.getDisplaySlots().length;
                  if (start < 0) {
                     start = 0;
                  }

                  int end = Math.min(start + this.getDisplaySlots().length, entries.size());
                  List<Entry<ItemStack, Long>> validEntries = entries.subList(start, end);
                  this.getCacheMap().put(screen.getLocation(), gridCache);

                  for (int i = 0; i < this.getDisplaySlots().length; i++) {
                     if (validEntries.size() > i) {
                        Entry<ItemStack, Long> entry = validEntries.get(i);
                        ItemStack displayStack = entry.getKey().clone();
                        ItemMeta itemMeta = displayStack.getItemMeta();
                        if (itemMeta != null) {
                           List<String> lore = itemMeta.getLore();
                           if (lore == null) {
                              lore = getLoreAddition(entry.getValue());
                           } else {
                              lore.addAll(getLoreAddition(entry.getValue()));
                           }

                           itemMeta.setLore(lore);
                           displayStack.setItemMeta(itemMeta);
                           screen.setItem(this.getDisplaySlots()[i], displayStack, (player, slot, item, action) -> {
                              this.takeItem(player, item, action, screen);
                              return false;
                           });
                        }
                     } else {
                        screen.setItem(this.getDisplaySlots()[i], Icon.BLANK_SLOT_STACK, (p, slot, item, action) -> {
                           this.receiveItem(p, p.getItemOnCursor(), action, screen);
                           return false;
                        });
                     }
                  }

                  screen.setItem(this.getPagePrevious(), Icon.getPageStack(this.getPagePreviousStack(), gridCache.getPage() + 1, gridCache.getMaxPages() + 1));
                  screen.setItem(this.getPageNext(), Icon.getPageStack(this.getPageNextStack(), gridCache.getPage() + 1, gridCache.getMaxPages() + 1));
                  this.sendFeedback(screen.getLocation(), FeedbackType.WORKING);
               }
            }
         } else {
            this.clearDisplay(screen);
            this.sendFeedback(location, FeedbackType.NO_NETWORK_FOUND);
         }
      }
   }

   protected void clearDisplay(@NotNull Screen blockMenu) {
      for (int displaySlot : this.getDisplaySlots()) {
         blockMenu.setItem(displaySlot, this.getBlankSlotStack(), (p, slot, item, action) -> false);
      }
   }

   @NotNull
   protected List<Entry<ItemStack, Long>> getEntries(@NotNull NetworkRoot networkRoot, @NotNull GridCache cache) {
      if (cache.getEntriesCache() != null) {
         return cache.getEntriesCache();
      } else {
         List<Entry<ItemStack, Long>> entries = filterAndSort(networkRoot.getAllNetworkItemsLongTypeView(), cache.getFilter(), cache.getSortOrder());
         cache.setEntriesCache(entries);
         return entries;
      }
   }

   @NotNull
   public static List<Entry<ItemStack, Long>> filterAndSort(
      @NotNull Map<ItemStack, Long> items, @Nullable String filter, @NotNull GridCache.SortOrder sortOrder
   ) {
      return items.entrySet().stream().filter(entry -> {
         if (filter == null) {
            return true;
         } else {
            ItemStack itemStack = entry.getKey();
            String name = TextUtil.stripColor(ItemStackHelper.getDisplayName(itemStack).toLowerCase(Locale.ROOT));
            if (filter.matches("^[a-zA-Z]+$")) {
               boolean pinyinEnabled = Networks.getConfigManager().isPinyinSearchEnabled();
               String pinyinName = pinyinEnabled ? PinyinHelper.toPinyin(name, PinyinStyleEnum.INPUT, "") : "";
               String pinyinFirstLetter = pinyinEnabled ? PinyinHelper.toPinyin(name, PinyinStyleEnum.FIRST_LETTER, "") : "";
               return name.contains(filter) || pinyinName.contains(filter) || pinyinFirstLetter.contains(filter);
            } else {
               return name.contains(filter);
            }
         }
      }).sorted(SORT_MAP.get(sortOrder)).toList();
   }

   public void setFilter(Player player, BlockMenu menu, GridCache gridCache, ClickAction action) {
      this.setFilter(player, Screen.of(menu), gridCache, action);
   }

   public void setFilter(Player player, Screen screen, GridCache gridCache, ClickAction action) {
      if (action.isRightClicked()) {
         gridCache.setFilter(null);
         SlimefunBlockData data = StorageCacheUtils.getBlock(screen.getLocation());
         if (data != null) {
            data.removeData("filter");
            this.updateDisplay(screen);
         }
      } else {
         player.closeInventory();
         player.sendMessage(Lang.getString("messages.normal-operation.grid.waiting_for_filter"));
         ChatUtils.awaitInput(player, s -> {
            if (!s.isBlank()) {
               s = s.toLowerCase(Locale.ROOT);
               gridCache.setFilter(s);
               this.getCacheMap().put(screen.getLocation(), gridCache);
               player.sendMessage(Lang.getString("messages.completed-operation.grid.filter_set"));
               StorageCacheUtils.setData(screen.getLocation(), "filter", s);
               if (screen.isAlive()) {
                  screen.open(player);
                  if (gridCache.getMaxPages() >= 1) {
                     gridCache.setPage(1);
                  }

                  this.updateDisplay(screen);
               }
            }
         });
      }
   }

   @ParametersAreNonnullByDefault
   protected synchronized void takeItem(Player player, @Nullable ItemStack itemStack, ClickAction action, Screen screen) {
      NodeDefinition definition = NetworkStorage.getNode(screen.getLocation());
      if (definition != null && definition.getNode() != null) {
         if (itemStack != null && itemStack.getType() != Material.AIR) {
            ItemStack clone = itemStack.clone();
            ItemMeta cloneMeta = clone.getItemMeta();
            if (cloneMeta != null) {
               List<String> cloneLore = cloneMeta.getLore();
               if (cloneLore != null && cloneLore.size() >= 2) {
                  cloneLore.removeLast();
                  cloneLore.removeLast();
                  cloneMeta.setLore(cloneLore);
                  clone.setItemMeta(cloneMeta);
                  NetworkRoot root = definition.getNode().getRoot();
                  ItemStack cursor = player.getItemOnCursor();
                  if (cursor.getType() != Material.AIR && !StackUtils.itemsMatch(clone, StackUtils.getAsQuantity(player.getItemOnCursor(), 1))) {
                     root.addItemStack(screen.getLocation(), player.getItemOnCursor());
                  } else {
                     int amount = 1;
                     if (action.isRightClicked()) {
                        amount = clone.getMaxStackSize();
                     }

                     GridItemRequest request = new GridItemRequest(clone, amount, player);
                     if (action.isShiftClicked()) {
                        this.addToInventory(player, definition, request, action, screen);
                     } else {
                        this.addToCursor(player, definition, request, action, screen);
                     }

                     this.updateDisplay(screen);
                  }
               }
            }
         }
      } else {
         this.clearDisplay(screen);
         screen.close();
         Networks.getInstance()
            .getLogger()
            .warning(String.format(Lang.getString("messages.unsupported-operation.grid.may_duping"), player.getName(), screen.getLocation()));
      }
   }

   @ParametersAreNonnullByDefault
   private void addToInventory(Player player, NodeDefinition definition, GridItemRequest request, ClickAction action, Screen screen) {
      ItemStack requestingStack = definition.getNode().getRoot().getItemStack(screen.getLocation(), request);
      if (requestingStack != null) {
         InventoryUtil.addItem(player, requestingStack);
         if (requestingStack.getAmount() > 0) {
            definition.getNode().getRoot().addItemStack(screen.getLocation(), requestingStack);
         }
      }
   }

   @ParametersAreNonnullByDefault
   private void addToCursor(Player player, NodeDefinition definition, GridItemRequest request, ClickAction action, Screen screen) {
      ItemStack cursor = player.getItemOnCursor();
      if (cursor.getType() == Material.AIR || this.canAddMore(action, cursor, request)) {
         ItemStack requestingStack = definition.getNode().getRoot().getItemStack(screen.getLocation(), request);
         this.setCursor(player, cursor, requestingStack);
      }
   }

   private void setCursor(@NotNull Player player, @NotNull ItemStack cursor, @Nullable ItemStack requestingStack) {
      if (requestingStack != null) {
         if (cursor.getType() != Material.AIR) {
            requestingStack.setAmount(cursor.getAmount() + 1);
         }

         player.setItemOnCursor(requestingStack);
      }
   }

   private boolean canAddMore(@NotNull ClickAction action, @NotNull ItemStack cursor, @NotNull GridItemRequest request) {
      return !action.isRightClicked() && request.getAmount() == 1 && cursor.getAmount() < cursor.getMaxStackSize() && StackUtils.itemsMatch(request, cursor);
   }

   public void postRegister() {
      this.getPreset();
   }

   @Nullable
   protected abstract BlockMenuPreset getPreset();

   @NotNull
   protected abstract Map<Location, GridCache> getCacheMap();

   protected abstract int[] getBackgroundSlots();

   protected abstract int[] getDisplaySlots();

   protected abstract int getInputSlot();

   protected abstract int getChangeSort();

   protected abstract int getPagePrevious();

   protected abstract int getPageNext();

   protected abstract int getFilterSlot();

   @NotNull
   protected ItemStack getBlankSlotStack() {
      return Icon.BLANK_SLOT_STACK;
   }

   @NotNull
   protected ItemStack getPagePreviousStack() {
      return Icon.PAGE_PREVIOUS_STACK;
   }

   @NotNull
   protected ItemStack getPageNextStack() {
      return Icon.PAGE_NEXT_STACK;
   }

   @NotNull
   protected ItemStack getChangeSortStack() {
      return Icon.CHANGE_SORT_STACK;
   }

   @NotNull
   protected ItemStack getFilterStack() {
      return Icon.FILTER_STACK;
   }

   public void receiveItem(@NotNull Player player, ItemStack itemStack, ClickAction action, @NotNull Screen screen) {
      NodeDefinition definition = NetworkStorage.getNode(screen.getLocation());
      if (definition != null && definition.getNode() != null) {
         if (itemStack != null && itemStack.getType() != Material.AIR && !StackUtils.isBlacklisted(itemStack)) {
            definition.getNode().getRoot().addItemStack(screen.getLocation(), itemStack);
         }
      } else {
         this.clearDisplay(screen);
         screen.close();
         Networks.getInstance()
            .getLogger()
            .warning(String.format(Lang.getString("messages.unsupported-operation.grid.may_duping"), player.getName(), screen.getLocation()));
      }
   }

   public static void updateSortOrder(GridCache gridCache, ClickAction action, @Range(from = 1L,to = 4L) int limit) {
      if (action.isShiftClicked() && !action.isRightClicked()) {
         gridCache.setSortOrder(GridCache.SortOrder.ALPHABETICAL);
      } else if (action.isRightClicked()) {
         gridCache.setSortOrder(gridCache.getSortOrder().previous(limit));
      } else {
         gridCache.setSortOrder(gridCache.getSortOrder().next(limit));
      }
   }

   static {
      SORT_MAP.put(GridCache.SortOrder.ALPHABETICAL, Sorters.ITEMSTACK_ALPHABETICAL_SORT);
      SORT_MAP.put(GridCache.SortOrder.NUMBER, Sorters.ITEMSTACK_NUMERICAL_SORT.reversed());
      SORT_MAP.put(GridCache.SortOrder.NUMBER_REVERSE, Sorters.ITEMSTACK_NUMERICAL_SORT);
      SORT_MAP.put(GridCache.SortOrder.ADDON, Sorters.ITEMSTACK_ADDON_SORT);
   }
}
