package com.ytdd9527.networksexpansion.core.items.machines;

import com.balugaq.jeg.api.groups.SearchGroup;
import com.balugaq.jeg.api.objects.enums.FilterType;
import com.balugaq.netex.api.algorithm.Sorters;
import com.balugaq.netex.api.enums.FeedbackType;
import com.balugaq.netex.api.helpers.Icon;
import com.balugaq.netex.api.interfaces.BaseGrid;
import com.balugaq.netex.api.keybind.Keybindable;
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
import io.github.sefiraat.networks.slimefun.network.grid.AbstractGrid;
import io.github.sefiraat.networks.slimefun.network.grid.GridCache;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.sefiraat.networks.utils.Theme;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import net.guizhanss.minecraft.guizhanlib.gugu.minecraft.helpers.inventory.ItemStackHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract class AbstractGridNewStyle extends AbstractGrid implements Keybindable, BaseGrid {
   private static final Map<GridCache.SortOrder, Comparator<? super Entry<ItemStack, Long>>> SORT_MAP = new HashMap<>();

   protected AbstractGridNewStyle(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe);
   }

   public AbstractGridNewStyle(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, NodeType nodeType) {
      super(itemGroup, item, recipeType, recipe, nodeType);
   }

   @Nullable
   @Override
   public ItemStack precheck(NodeDefinition definition, BlockMenu blockMenu, Player player, ItemStack itemStack) {
      if (definition != null && definition.getNode() != null) {
         if (itemStack != null && itemStack.getType() != Material.AIR) {
            ItemStack clone = itemStack.clone();
            ItemMeta cloneMeta = clone.getItemMeta();
            if (cloneMeta == null) {
               return null;
            } else {
               List<String> cloneLore = cloneMeta.getLore();
               if (cloneLore != null && cloneLore.size() >= 2) {
                  cloneLore.remove(cloneLore.size() - 1);
                  cloneLore.remove(cloneLore.size() - 1);
                  cloneMeta.setLore(cloneLore);
                  clone.setItemMeta(cloneMeta);
                  return clone;
               } else {
                  return null;
               }
            }
         } else {
            return null;
         }
      } else {
         this.clearDisplay(Screen.of(blockMenu));
         blockMenu.close();
         Networks.getInstance()
            .getLogger()
            .warning(String.format(Lang.getString("messages.unsupported-operation.grid.may_duping"), player.getName(), blockMenu.getLocation()));
         return null;
      }
   }

   @Override
   public void addToInventory(Player player, NodeDefinition definition, GridItemRequest request, BlockMenu menu) {
      InventoryUtil.give(player, definition.getNode().getRoot().getItemStack(menu.getLocation(), request));
   }

   @Override
   public void addToCursor(Player player, NodeDefinition definition, GridItemRequest request, boolean rightClick, BlockMenu blockMenu) {
      ItemStack cursor = player.getItemOnCursor();
      if (cursor.getType() == Material.AIR || this.canAddMore(rightClick, cursor, request)) {
         ItemStack requestingStack = definition.getNode().getRoot().getItemStack(blockMenu.getLocation(), request);
         this.setCursor(player, cursor, requestingStack);
      }
   }

   protected static List<String> getLoreAddition(Long long1) {
      MessageFormat format = new MessageFormat(Lang.getString("messages.normal-operation.grid.item_amount"), Locale.ROOT);
      return List.of("", format.format(new Object[]{Theme.CLICK_INFO.getColor(), Theme.PASSIVE.getColor(), long1}, new StringBuffer(), null).toString());
   }

   private boolean canAddMore(boolean rightClick, ItemStack cursor, GridItemRequest request) {
      return !rightClick && request.getAmount() == 1 && cursor.getAmount() < cursor.getMaxStackSize() && StackUtils.itemsMatch(request, cursor);
   }

   private void setCursor(Player player, ItemStack cursor, @Nullable ItemStack requestingStack) {
      if (requestingStack != null) {
         if (cursor.getType() != Material.AIR) {
            requestingStack.setAmount(cursor.getAmount() + 1);
         }

         player.setItemOnCursor(requestingStack);
      }
   }

   protected abstract int getKeybindButtonSlot();

   protected static List<String> getHistoryLoreAddition() {
      return List.of(" ", Lang.getString("messages.normal-operation.grid_new_style.click_to_withdraw"));
   }

   protected abstract int getToggleModeSlot();

   public ItemStack getModeStack(GridCache gridCache) {
      return this.getModeStack(gridCache.getDisplayMode());
   }

   public ItemStack getModeStack(GridCache.DisplayMode displayMode) {
      return displayMode == GridCache.DisplayMode.DISPLAY ? Icon.DISPLAY_MODE_STACK : Icon.HISTORY_MODE_STACK;
   }

   @Override
   public void updateDisplay(Screen screen) {
      Location location = screen.getLocation();
      if (!screen.hasViewer()) {
         this.sendFeedback(location, FeedbackType.AFK);
      } else {
         NodeDefinition definition = NetworkStorage.getNode(location);
         if (definition != null && definition.getNode() != null) {
            BlockMenu bm = StorageCacheUtils.getMenu(location);
            if (bm != null) {
               NetworkRoot root = definition.getNode().getRoot();
               GridCache gridCache = this.getCacheMap().get(screen.getLocation());
               SlimefunBlockData data = StorageCacheUtils.getBlock(screen.getLocation());
               if (data != null) {
                  String filter = data.getData("filter");
                  if (filter != null) {
                     gridCache.setFilter(filter);
                  }

                  screen.setItem(this.getChangeSort(), this.getSortOrderStack(gridCache.getSortOrder()));
                  screen.setItem(this.getFilterSlot(), this.getFilterStack(gridCache.getFilter()));
                  if (gridCache.getDisplayMode() == GridCache.DisplayMode.DISPLAY) {
                     List<Entry<ItemStack, Long>> entries = this.getEntries(root, gridCache);
                     int pages = (int)Math.ceil((double)entries.size() / (double)this.getDisplaySlots().length) - 1;
                     gridCache.setMaxPages(pages);
                     if (pages < 0) {
                        this.clearDisplay(screen);
                        return;
                     }

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
                              screen.setItem(this.getDisplaySlots()[i], displayStack, this.displayKeybinds());
                           }
                        } else {
                           screen.setItem(this.getDisplaySlots()[i], this.getBlankSlotStack(), (p, slot, item, action) -> {
                              this.receiveItem(p, p.getItemOnCursor(), action, screen);
                              return false;
                           });
                        }
                     }
                  } else {
                     List<ItemStack> history = new ArrayList<>();

                     for (ItemStack ix : gridCache.getPullItemHistory()) {
                        history.add(ix.clone());
                     }

                     int pagesx = (int)Math.ceil((double)history.size() / (double)this.getDisplaySlots().length) - 1;
                     gridCache.setMaxPages(pagesx);
                     if (pagesx < 0) {
                        this.clearDisplay(screen);
                        return;
                     }

                     if (gridCache.getPage() > pagesx) {
                        gridCache.setPage(pagesx);
                     }

                     int start = gridCache.getPage() * this.getDisplaySlots().length;
                     int end = Math.min(start + this.getDisplaySlots().length, history.size());
                     List<ItemStack> validHistory = history.subList(start, end);
                     this.getCacheMap().put(screen.getLocation(), gridCache);

                     for (int ix = 0; ix < this.getDisplaySlots().length; ix++) {
                        if (validHistory.size() > ix) {
                           ItemStack displayStack = validHistory.get(ix);
                           ItemMeta itemMeta = displayStack.getItemMeta();
                           if (itemMeta != null) {
                              List<String> lore = itemMeta.getLore();
                              if (lore == null) {
                                 lore = getHistoryLoreAddition();
                              } else {
                                 lore.addAll(getHistoryLoreAddition());
                              }

                              itemMeta.setLore(lore);
                              displayStack.setItemMeta(itemMeta);
                              screen.setItem(this.getDisplaySlots()[ix], displayStack, this.displayKeybinds());
                           }
                        } else {
                           screen.setItem(this.getDisplaySlots()[ix], this.getBlankSlotStack(), (p, slot, item, action) -> {
                              this.receiveItem(p, p.getItemOnCursor(), action, screen);
                              return false;
                           });
                        }
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

   @Override
   protected List<Entry<ItemStack, Long>> getEntries(NetworkRoot networkRoot, GridCache cache) {
      return getEntries0(networkRoot, cache);
   }

   public static List<Entry<ItemStack, Long>> getEntries0(NetworkRoot networkRoot, GridCache cache) {
      if (cache.getEntriesCache() != null) {
         return cache.getEntriesCache();
      } else {
         HashSet<SlimefunItem> pass = new HashSet<>();
         String searchTerm = cache.getFilter();
         if (searchTerm != null && Networks.getSupportedPluginManager().isJustEnoughGuide()) {
            try {
               Bukkit.getOnlinePlayers().stream().findFirst().ifPresent(player -> {
                  List<SlimefunItem> sfs = new ArrayList<>();

                  for (ItemStack item : networkRoot.getAllNetworkItemsLongTypeView().keySet()) {
                     if (item != null && item.getType() != Material.AIR) {
                        SlimefunItem sf = SlimefunItem.getByItem(item);
                        if (sf != null) {
                           sfs.add(sf);
                        }
                     }
                  }

                  List<FilterType> types = new ArrayList<>(Arrays.stream(FilterType.values()).toList());
                  types.remove(FilterType.BY_DISPLAY_ITEM_NAME);
                  types.remove(FilterType.BY_RECIPE_ITEM_NAME);

                  for (FilterType type : types) {
                     pass.addAll(SearchGroup.filterItems(player, type, searchTerm, true, sfs));
                  }
               });
            } catch (Exception var5) {
            }
         }

         List<Entry<ItemStack, Long>> result = networkRoot.getAllNetworkItemsLongTypeView().entrySet().stream().filter(entry -> {
            if (searchTerm == null) {
               return true;
            } else {
               ItemStack itemStack = entry.getKey();
               if (pass != null) {
                  SlimefunItem slimefunItem = SlimefunItem.getByItem(itemStack);
                  if (slimefunItem != null && pass.contains(slimefunItem)) {
                     return true;
                  }
               }

               String name = TextUtil.stripColor(ItemStackHelper.getDisplayName(itemStack).toLowerCase(Locale.ROOT));
               if (searchTerm.matches("^[a-zA-Z]+$")) {
                  boolean pinyinEnabled = Networks.getConfigManager().isPinyinSearchEnabled();
                  String pinyinName = pinyinEnabled ? PinyinHelper.toPinyin(name, PinyinStyleEnum.INPUT, "") : "";
                  String pinyinFirstLetter = pinyinEnabled ? PinyinHelper.toPinyin(name, PinyinStyleEnum.FIRST_LETTER, "") : "";
                  return name.contains(searchTerm) || pinyinName.contains(searchTerm) || pinyinFirstLetter.contains(searchTerm);
               } else {
                  return name.contains(searchTerm);
               }
            }
         }).sorted(SORT_MAP.get(cache.getSortOrder())).toList();
         cache.setEntriesCache(result);
         return result;
      }
   }

   @Override
   protected int getInputSlot() {
      return -1;
   }

   static {
      SORT_MAP.put(GridCache.SortOrder.ALPHABETICAL, Sorters.ITEMSTACK_ALPHABETICAL_SORT);
      SORT_MAP.put(GridCache.SortOrder.NUMBER, Sorters.ITEMSTACK_NUMERICAL_SORT.reversed());
      SORT_MAP.put(GridCache.SortOrder.NUMBER_REVERSE, Sorters.ITEMSTACK_NUMERICAL_SORT);
      SORT_MAP.put(GridCache.SortOrder.ADDON, Sorters.ITEMSTACK_ADDON_SORT);
   }
}
