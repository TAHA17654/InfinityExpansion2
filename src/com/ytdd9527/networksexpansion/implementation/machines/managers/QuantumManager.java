package com.ytdd9527.networksexpansion.implementation.machines.managers;

import com.balugaq.netex.api.algorithm.Sorters;
import com.balugaq.netex.api.enums.FeedbackType;
import com.balugaq.netex.api.events.NetworkRootLocateStorageEvent;
import com.balugaq.netex.api.helpers.Icon;
import com.balugaq.netex.utils.Lang;
import com.github.houbb.pinyin.constant.enums.PinyinStyleEnum;
import com.github.houbb.pinyin.util.PinyinHelper;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.implementation.ExpansionItems;
import com.ytdd9527.networksexpansion.utils.ParticleUtil;
import com.ytdd9527.networksexpansion.utils.TextUtil;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.NodeDefinition;
import io.github.sefiraat.networks.network.NodeType;
import io.github.sefiraat.networks.network.barrel.NetworkStorage;
import io.github.sefiraat.networks.network.stackcaches.BarrelIdentity;
import io.github.sefiraat.networks.network.stackcaches.QuantumCache;
import io.github.sefiraat.networks.slimefun.network.NetworkObject;
import io.github.sefiraat.networks.slimefun.network.NetworkQuantumStorage;
import io.github.sefiraat.networks.slimefun.network.grid.GridCache;
import io.github.sefiraat.networks.utils.Theme;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemHandler;
import io.github.thebusybiscuit.slimefun4.api.items.ItemSetting;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.items.settings.IntRangeSetting;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.utils.ChatUtils;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
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

public class QuantumManager extends NetworkObject {
   public static final String MANAGER_TAG = "quantum-manager";
   public static final NetworkRootLocateStorageEvent.Strategy MANAGER_STRATEGY = NetworkRootLocateStorageEvent.Strategy.custom("quantum-manager");
   private static final Map<Location, GridCache> CACHE_MAP = new HashMap<>();
   private static final int[] BACKGROUND_SLOTS = new int[]{8, 17};
   private static final int[] DISPLAY_SLOTS = new int[]{
      0,
      1,
      2,
      3,
      4,
      5,
      6,
      7,
      9,
      10,
      11,
      12,
      13,
      14,
      15,
      16,
      18,
      19,
      20,
      21,
      22,
      23,
      24,
      25,
      27,
      28,
      29,
      30,
      31,
      32,
      33,
      34,
      36,
      37,
      38,
      39,
      40,
      41,
      42,
      43,
      45,
      46,
      47,
      48,
      49,
      50,
      51,
      52
   };
   private static final int CHANGE_SORT = 35;
   private static final int FILTER = 26;
   private static final int PAGE_PREVIOUS = 44;
   private static final int PAGE_NEXT = 53;
   private static final Map<GridCache.SortOrder, Comparator<? super BarrelIdentity>> SORT_MAP = new HashMap<>();
   private static final String BS_TOP = "netex-top";
   private static final String BS_NAME = "netex-name";
   private static final String BS_ICON = "netex-icon";
   private static final String BS_TOP_1B = "1";
   private static final String BS_TOP_0B = "0";
   private static final String NAMESPACE_SF = "sf";
   private static final String NAMESPACE_MC = "mc";
   @NotNull
   private final IntRangeSetting tickRate = new IntRangeSetting(this, "tick_rate", 1, 1, 10);

   public QuantumManager(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe, NodeType.QUANTUM_MANAGER);
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

               QuantumManager.this.addToRegistry(block);
               QuantumManager.this.updateDisplay(blockMenu);
            }
         }

         public void uniqueTick() {
            this.tick = this.tick <= 1 ? (Integer)QuantumManager.this.tickRate.getValue() : this.tick - 1;
         }
      }});
   }

   @Nullable
   public static String getStorageName(@NotNull Location barrelLocation) {
      return StorageCacheUtils.getData(barrelLocation, "netex-name");
   }

   public static void setStorageIcon(@NotNull Player player, @NotNull Location barrelLocation, @NotNull ItemStack cursor) {
      StorageCacheUtils.setData(barrelLocation, "netex-icon", serializeIcon(cursor));
      player.sendMessage(Lang.getString("messages.completed-operation.manager.set_icon"));
   }

   @Nullable
   public static ItemStack getStorageIcon(@NotNull Location barrelLocation) {
      String icon = StorageCacheUtils.getData(barrelLocation, "netex-icon");
      return icon == null ? null : deserializeIcon(icon);
   }

   @NotNull
   public static String serializeIcon(@NotNull ItemStack itemStack) {
      SlimefunItem sf = SlimefunItem.getByItem(itemStack);
      return sf != null ? "sf:" + sf.getId() : "mc:" + itemStack.getType().name();
   }

   @Nullable
   public static ItemStack deserializeIcon(@NotNull String icon) {
      if (icon.startsWith("sf")) {
         String id = icon.split(":")[1];
         SlimefunItem sf = SlimefunItem.getById(id);
         if (sf != null) {
            return sf.getItem();
         }
      } else if (icon.startsWith("mc")) {
         Material type = Material.valueOf(icon.split(":")[1]);
         return new ItemStack(type);
      }

      return null;
   }

   public static void topOrUntopStorage(@NotNull Player player, @NotNull Location barrelLocation) {
      if (Objects.equals(StorageCacheUtils.getData(barrelLocation, "netex-top"), "1")) {
         StorageCacheUtils.setData(barrelLocation, "netex-top", "0");
         player.sendMessage(Lang.getString("messages.completed-operation.manager.top_storage_off"));
      } else {
         StorageCacheUtils.setData(barrelLocation, "netex-top", "1");
         player.sendMessage(Lang.getString("messages.completed-operation.manager.top_storage_on"));
      }
   }

   public static boolean isTopStorage(@NotNull Location barrelLocation) {
      String top = StorageCacheUtils.getData(barrelLocation, "netex-top");
      return top != null && top.equals("1");
   }

   public static void setItem(@NotNull BarrelIdentity barrel, @NotNull Location barrelLocation, @NotNull Player player) {
      if (!(barrel instanceof NetworkStorage)) {
         player.sendMessage(Lang.getString("messages.unsupported-operation.manager.support_quantum_only"));
      }

      QuantumCache cache = NetworkQuantumStorage.getCaches().get(barrelLocation);
      if (cache == null) {
         player.sendMessage(Lang.getString("messages.unsupported-operation.manager.support_quantum_only"));
      } else {
         ItemStack exist = cache.getItemStack();
         if (exist != null) {
            player.sendMessage(Lang.getString("messages.unsupported-operation.manager.quantum_storage_not_empty"));
         } else {
            BlockMenu menu = StorageCacheUtils.getMenu(barrel.getLocation());
            if (menu != null) {
               NetworkQuantumStorage.setItem(menu, player);
            }
         }
      }
   }

   public static void openMenu(@NotNull BarrelIdentity barrel, @NotNull Player player) {
      BlockMenu menu = StorageCacheUtils.getMenu(barrel.getLocation());
      if (menu != null) {
         menu.open(new Player[]{player});
      }
   }

   @NotNull
   public static List<BarrelIdentity> getBarrels(@NotNull NetworkRoot root, @NotNull GridCache cache) {
      return root.getBarrels(barrel -> barrel instanceof NetworkStorage, MANAGER_STRATEGY, true).stream().filter(entry -> {
         if (cache.getFilter() == null) {
            return true;
         } else {
            ItemStack itemStack = entry.getItemStack();
            if (itemStack == null) {
               return true;
            } else {
               String name = TextUtil.stripColor(ItemStackHelper.getDisplayName(itemStack).toLowerCase(Locale.ROOT));
               if (cache.getFilter().matches("^[a-zA-Z]+$")) {
                  boolean pinyinEnabled = Networks.getConfigManager().isPinyinSearchEnabled();
                  String pinyinName = pinyinEnabled ? PinyinHelper.toPinyin(name, PinyinStyleEnum.INPUT, "") : "";
                  String pinyinFirstLetter = pinyinEnabled ? PinyinHelper.toPinyin(name, PinyinStyleEnum.FIRST_LETTER, "") : "";
                  return name.contains(cache.getFilter()) || pinyinName.contains(cache.getFilter()) || pinyinFirstLetter.contains(cache.getFilter());
               } else {
                  return name.contains(cache.getFilter());
               }
            }
         }
      }).sorted(SORT_MAP.get(cache.getSortOrder())).toList();
   }

   @NotNull
   private static String getAmountLore(Long long1) {
      MessageFormat format = new MessageFormat(Lang.getString("messages.normal-operation.grid.item_amount"), Locale.ROOT);
      return format.format(new Object[]{Theme.CLICK_INFO.getColor(), Theme.PASSIVE.getColor(), long1}, new StringBuffer(), null).toString();
   }

   public void handleClick(
      @NotNull NetworkRoot root,
      @NotNull BlockMenu blockMenu,
      @NotNull Location barrelLocation,
      @NotNull Player player,
      @Range(from = 0L,to = 53L) int slot,
      @NotNull ItemStack item,
      @NotNull ClickAction action
   ) {
      BarrelIdentity barrel = NetworkRoot.getBarrel(barrelLocation, true);
      if (barrel != null) {
         ItemStack cursor = player.getItemOnCursor();
         if (!action.isRightClicked()) {
            if (action.isShiftClicked()) {
               topOrUntopStorage(player, barrelLocation);
            } else if (cursor.getType() == Material.AIR) {
               openMenu(barrel, player);
            } else {
               setItem(barrel, barrelLocation, player);
            }
         } else if (action.isShiftClicked()) {
            if (cursor.getType() == Material.AIR) {
               this.setStorageName(blockMenu, player, barrelLocation);
            } else {
               setStorageIcon(player, barrelLocation, cursor);
            }
         } else {
            ParticleUtil.highlightBlock(player, barrelLocation, 10);
         }
      }
   }

   public void setStorageName(@NotNull BlockMenu blockMenu, @NotNull Player player, @NotNull Location barrelLocation) {
      player.sendMessage(Lang.getString("messages.normal-operation.manager.set_name"));
      player.closeInventory();
      ChatUtils.awaitInput(player, s -> {
         StorageCacheUtils.setData(barrelLocation, "netex-name", s);
         player.sendMessage(Lang.getString("messages.completed-operation.manager.set_name"));
         SlimefunBlockData data = StorageCacheUtils.getBlock(blockMenu.getLocation());
         if (data != null) {
            if (blockMenu.getPreset().getID().equals(data.getSfId())) {
               BlockMenu actualMenu = data.getBlockMenu();
               if (actualMenu != null) {
                  this.updateDisplay(actualMenu);
                  actualMenu.open(new Player[]{player});
               }
            }
         }
      });
   }

   public void updateDisplay(@Nullable BlockMenu blockMenu) {
      if (blockMenu != null) {
         if (!blockMenu.hasViewer()) {
            this.sendFeedback(blockMenu.getLocation(), FeedbackType.AFK);
         } else {
            Location location = blockMenu.getLocation();
            NodeDefinition definition = io.github.sefiraat.networks.NetworkStorage.getNode(location);
            if (definition != null && definition.getNode() != null) {
               GridCache gridCache = this.getCacheMap().get(location);
               NetworkRoot root = definition.getNode().getRoot();
               List<BarrelIdentity> barrels = getBarrels(root, gridCache);
               int pages = (int)Math.ceil((double)barrels.size() / (double)this.getDisplaySlots().length) - 1;
               gridCache.setMaxPages(pages);
               if (pages < 0) {
                  this.clearDisplay(blockMenu);
               } else {
                  if (gridCache.getPage() > pages) {
                     gridCache.setPage(0);
                  }

                  int start = gridCache.getPage() * this.getDisplaySlots().length;
                  if (start < 0) {
                     start = 0;
                  }

                  int end = Math.min(start + this.getDisplaySlots().length, barrels.size());
                  barrels = barrels.stream().sorted((a, b) -> isTopStorage(a.getLocation()) ? -1 : (isTopStorage(b.getLocation()) ? 1 : 0)).toList();
                  List<BarrelIdentity> validBarrels = barrels.subList(start, end);
                  this.getCacheMap().put(blockMenu.getLocation(), gridCache);

                  for (int i = 0; i < this.getDisplaySlots().length; i++) {
                     if (validBarrels.size() > i) {
                        BarrelIdentity barrel = validBarrels.get(i);
                        ItemStack barrelItemStack = barrel.getItemStack();
                        boolean isEmpty = false;
                        ItemStack displayStack = null;
                        if (barrelItemStack == null || barrelItemStack.getType() == Material.AIR) {
                           displayStack = new ItemStack(Material.BARRIER);
                           isEmpty = true;
                        }

                        Location barrelLocation = barrel.getLocation();
                        ItemStack custom = getStorageIcon(barrelLocation);
                        if (custom != null) {
                           displayStack = custom;
                        } else if (displayStack == null) {
                           displayStack = barrelItemStack.clone();
                        }

                        String name = getStorageName(barrelLocation);
                        if (name != null) {
                           displayStack = new CustomItemStack(displayStack, TextUtil.color(name), new String[0]);
                        } else if (!isEmpty) {
                           displayStack = new CustomItemStack(displayStack, TextUtil.GRAY + ItemStackHelper.getDisplayName(barrelItemStack), new String[0]);
                        } else {
                           SlimefunItem sf = StorageCacheUtils.getSfItem(barrelLocation);
                           if (sf == null) {
                              displayStack = new CustomItemStack(displayStack, Sorters.NO_ITEM, new String[0]);
                           } else {
                              displayStack = sf.getItem().clone();
                           }
                        }

                        ItemMeta itemMeta = displayStack.getItemMeta();
                        if (itemMeta != null) {
                           List<String> lore = this.getLoreAddition(barrel);
                           itemMeta.setLore(lore);
                           displayStack.setItemMeta(itemMeta);
                           blockMenu.replaceExistingItem(this.getDisplaySlots()[i], displayStack);
                           blockMenu.addMenuClickHandler(this.getDisplaySlots()[i], (player, slot, item, action) -> {
                              this.handleClick(root, blockMenu, barrelLocation, player, slot, item, action);
                              return false;
                           });
                        }
                     } else {
                        blockMenu.replaceExistingItem(this.getDisplaySlots()[i], this.getBlankSlotStack());
                        blockMenu.addMenuClickHandler(this.getDisplaySlots()[i], (p, slot, item, action) -> false);
                     }
                  }

                  blockMenu.replaceExistingItem(
                     this.getPagePrevious(), Icon.getPageStack(this.getPagePreviousStack(), gridCache.getPage() + 1, gridCache.getMaxPages() + 1)
                  );
                  blockMenu.replaceExistingItem(
                     this.getPageNext(), Icon.getPageStack(this.getPageNextStack(), gridCache.getPage() + 1, gridCache.getMaxPages() + 1)
                  );
                  this.sendFeedback(blockMenu.getLocation(), FeedbackType.WORKING);
               }
            } else {
               this.sendFeedback(blockMenu.getLocation(), FeedbackType.NO_NETWORK_FOUND);
            }
         }
      }
   }

   @NotNull
   public List<String> getLoreAddition(@NotNull BarrelIdentity barrel) {
      Location loc = barrel.getLocation();
      List<String> list = new ArrayList<>();
      list.add("");
      list.add(String.format(Lang.getString("messages.normal-operation.manager.location"), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
      list.add(getAmountLore(barrel.getAmount()));
      list.add("");
      list.addAll(Lang.getStringList("messages.normal-operation.manager.quantum-manager-click-behavior"));
      return list;
   }

   public void postRegister() {
      this.getPreset();
   }

   @NotNull
   protected BlockMenuPreset getPreset() {
      return new BlockMenuPreset(this.getId(), this.getItemName()) {
         public void init() {
            this.drawBackground(QuantumManager.this.getBackgroundSlots());
            this.drawBackground(QuantumManager.this.getDisplaySlots());
            this.setSize(54);
         }

         public boolean canOpen(@NotNull Block block, @NotNull Player player) {
            return player.hasPermission("slimefun.inventory.bypass")
               || ExpansionItems.QUANTUM_MANAGER.canUse(player, false)
                  && Slimefun.getProtectionManager().hasPermission(player, block.getLocation(), Interaction.INTERACT_BLOCK);
         }

         public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
            return new int[0];
         }

         public void newInstance(@NotNull BlockMenu menu, @NotNull Block b) {
            QuantumManager.this.getCacheMap().put(menu.getLocation(), new GridCache(0, 0, GridCache.SortOrder.ALPHABETICAL));
            menu.replaceExistingItem(QuantumManager.this.getPagePrevious(), QuantumManager.this.getPagePreviousStack());
            menu.addMenuClickHandler(QuantumManager.this.getPagePrevious(), (p, slot, item, action) -> {
               GridCache gridCache = QuantumManager.this.getCacheMap().get(menu.getLocation());
               gridCache.setPage(gridCache.getPage() <= 0 ? 0 : gridCache.getPage() - 1);
               QuantumManager.this.getCacheMap().put(menu.getLocation(), gridCache);
               QuantumManager.this.updateDisplay(menu);
               return false;
            });
            menu.replaceExistingItem(QuantumManager.this.getPageNext(), QuantumManager.this.getPageNextStack());
            menu.addMenuClickHandler(QuantumManager.this.getPageNext(), (p, slot, item, action) -> {
               GridCache gridCache = QuantumManager.this.getCacheMap().get(menu.getLocation());
               gridCache.setPage(gridCache.getPage() >= gridCache.getMaxPages() ? gridCache.getMaxPages() : gridCache.getPage() + 1);
               QuantumManager.this.getCacheMap().put(menu.getLocation(), gridCache);
               QuantumManager.this.updateDisplay(menu);
               return false;
            });
            menu.replaceExistingItem(QuantumManager.this.getChangeSort(), QuantumManager.this.getChangeSortStack());
            menu.addMenuClickHandler(QuantumManager.this.getChangeSort(), (p, slot, item, action) -> {
               GridCache gridCache = QuantumManager.this.getCacheMap().get(menu.getLocation());
               if (gridCache.getSortOrder() == GridCache.SortOrder.ALPHABETICAL) {
                  gridCache.setSortOrder(GridCache.SortOrder.NUMBER);
               } else if (gridCache.getSortOrder() == GridCache.SortOrder.NUMBER) {
                  gridCache.setSortOrder(GridCache.SortOrder.NUMBER_REVERSE);
               } else {
                  gridCache.setSortOrder(GridCache.SortOrder.ALPHABETICAL);
               }

               QuantumManager.this.getCacheMap().put(menu.getLocation(), gridCache);
               QuantumManager.this.updateDisplay(menu);
               return false;
            });
            menu.replaceExistingItem(QuantumManager.this.getFilterSlot(), QuantumManager.this.getFilterStack());
            menu.addMenuClickHandler(QuantumManager.this.getFilterSlot(), (p, slot, item, action) -> {
               GridCache gridCache = QuantumManager.this.getCacheMap().get(menu.getLocation());
               QuantumManager.this.setFilter(p, menu, gridCache, action);
               return false;
            });

            for (int displaySlot : QuantumManager.this.getDisplaySlots()) {
               menu.replaceExistingItem(displaySlot, ChestMenuUtils.getBackground());
               menu.addMenuClickHandler(displaySlot, (p, slot, item, action) -> false);
            }
         }
      };
   }

   @NotNull
   public Map<Location, GridCache> getCacheMap() {
      return CACHE_MAP;
   }

   public int[] getBackgroundSlots() {
      return BACKGROUND_SLOTS;
   }

   public int[] getDisplaySlots() {
      return DISPLAY_SLOTS;
   }

   public int getChangeSort() {
      return 35;
   }

   public int getPagePrevious() {
      return 44;
   }

   public int getPageNext() {
      return 53;
   }

   protected int getFilterSlot() {
      return 26;
   }

   protected void setFilter(@NotNull Player player, @NotNull BlockMenu blockMenu, @NotNull GridCache gridCache, @NotNull ClickAction action) {
      if (action.isRightClicked()) {
         gridCache.setFilter(null);
      } else {
         player.closeInventory();
         player.sendMessage(Lang.getString("messages.normal-operation.grid.waiting_for_filter"));
         ChatUtils.awaitInput(player, s -> {
            if (!s.isBlank()) {
               s = s.toLowerCase(Locale.ROOT);
               gridCache.setFilter(s);
               this.getCacheMap().put(blockMenu.getLocation(), gridCache);
               player.sendMessage(Lang.getString("messages.completed-operation.grid.filter_set"));
               SlimefunBlockData data = StorageCacheUtils.getBlock(blockMenu.getLocation());
               if (data != null) {
                  if (blockMenu.getPreset().getID().equals(data.getSfId())) {
                     BlockMenu actualMenu = data.getBlockMenu();
                     if (actualMenu != null) {
                        this.updateDisplay(actualMenu);
                        actualMenu.open(new Player[]{player});
                     }
                  }
               }
            }
         });
      }
   }

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

   protected void clearDisplay(@NotNull BlockMenu blockMenu) {
      for (int displaySlot : this.getDisplaySlots()) {
         blockMenu.replaceExistingItem(displaySlot, this.getBlankSlotStack());
         blockMenu.addMenuClickHandler(displaySlot, (p, slot, item, action) -> false);
      }
   }

   static {
      SORT_MAP.put(GridCache.SortOrder.ALPHABETICAL, Sorters.BARREL_ALPHABETICAL_SORT);
      SORT_MAP.put(GridCache.SortOrder.NUMBER, Sorters.BARREL_NUMERICAL_SORT);
      SORT_MAP.put(GridCache.SortOrder.NUMBER_REVERSE, Sorters.BARREL_NUMERICAL_SORT.reversed());
   }
}
