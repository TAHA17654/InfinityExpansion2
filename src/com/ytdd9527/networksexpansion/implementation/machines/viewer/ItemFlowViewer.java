package com.ytdd9527.networksexpansion.implementation.machines.viewer;

import com.balugaq.netex.api.data.ItemFlowRecord;
import com.balugaq.netex.api.enums.FeedbackType;
import com.balugaq.netex.api.helpers.Icon;
import com.balugaq.netex.utils.Lang;
import com.github.houbb.pinyin.constant.enums.PinyinStyleEnum;
import com.github.houbb.pinyin.util.PinyinHelper;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.implementation.ExpansionItems;
import com.ytdd9527.networksexpansion.utils.ParticleUtil;
import com.ytdd9527.networksexpansion.utils.TextUtil;
import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.NodeDefinition;
import io.github.sefiraat.networks.network.NodeType;
import io.github.sefiraat.networks.slimefun.network.NetworkController;
import io.github.sefiraat.networks.slimefun.network.NetworkObject;
import io.github.sefiraat.networks.slimefun.network.grid.GridCache;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemHandler;
import io.github.thebusybiscuit.slimefun4.api.items.ItemSetting;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.items.settings.IntRangeSetting;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.utils.ChatUtils;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.ParametersAreNonnullByDefault;
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
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemFlowViewer extends NetworkObject {
   public static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(3, 2);
   private static final Map<Location, GridCache> CACHE_MAP = new HashMap<>();
   private static final int BACK_SLOT = 8;
   private static final int FORCE_CLEAN_SLOT = 17;
   private static final int[] BACKGROUND_SLOTS = new int[]{8, 17, 35};
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
   private static final int FILTER = 26;
   private static final int PAGE_PREVIOUS = 44;
   private static final int PAGE_NEXT = 53;
   private static final String BS_SUB_MENU = "sub-menu";
   private static final String NAMESPACE_SF = "sf";
   private static final String NAMESPACE_MC = "mc";
   @NotNull
   private final IntRangeSetting tickRate = new IntRangeSetting(this, "tick_rate", 1, 1, 10);

   public ItemFlowViewer(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe, NodeType.FLOW_VIEWER);
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

               ItemFlowViewer.this.addToRegistry(block);
               ItemFlowViewer.this.updateDisplay(blockMenu);
            }
         }

         public void uniqueTick() {
            this.tick = this.tick <= 1 ? (Integer)ItemFlowViewer.this.tickRate.getValue() : this.tick - 1;
         }
      }});
      this.addItemHandler(new ItemHandler[]{new BlockBreakHandler(false, false) {
         @ParametersAreNonnullByDefault
         public void onPlayerBreak(BlockBreakEvent blockBreakEvent, ItemStack itemStack, List<ItemStack> list) {
            NodeDefinition definition = NetworkStorage.getNode(blockBreakEvent.getBlock().getLocation());
            if (definition != null && definition.getNode() != null) {
               NetworkController.disableRecord(definition.getNode().getRoot().getController());
            }
         }
      }});
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

   @NotNull
   public static List<ItemFlowViewer.DisplayEntry> getRecords(@NotNull NetworkRoot root, @NotNull GridCache cache) {
      return (List<ItemFlowViewer.DisplayEntry>)(root.isRecordFlow() && root.getItemFlowRecord() != null
         ? root.getItemFlowRecord().getActions().entrySet().stream().filter(entry -> {
            if (cache.getFilter() == null) {
               return true;
            } else {
               ItemStack itemStack = entry.getKey();
               if (itemStack == null) {
                  return false;
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
         }).map(entry -> new ItemFlowViewer.DisplayEntry(entry.getKey(), entry.getValue())).toList()
         : new ArrayList<>());
   }

   @NotNull
   public static List<ItemFlowRecord.TransportAction> getSubMenu(@NotNull NetworkRoot root, @NotNull GridCache cache, @Nullable ItemStack itemStack) {
      return (List<ItemFlowRecord.TransportAction>)(root.isRecordFlow() && root.getItemFlowRecord() != null
         ? root.getItemFlowRecord().getActions().getOrDefault(itemStack, new ArrayList<>()).stream().filter(action -> {
            if (cache.getFilter() == null) {
               return true;
            } else if (itemStack == null) {
               return false;
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
         }).toList()
         : new ArrayList<>());
   }

   @NotNull
   public static List<String> getLoreAddition(@NotNull ItemFlowViewer.DisplayEntry entry) {
      long change = entry.actions().stream().map(ItemFlowRecord.TransportAction::amount).mapToLong(i -> i).sum();
      List<String> list = new ArrayList<>();
      list.add("");
      list.add(
         (change > 0L ? TextUtil.GREEN : (change < 0L ? TextUtil.RED : TextUtil.GRAY))
            + String.format(Lang.getString("messages.normal-operation.viewer.change"), change > 0L ? "+" + change : change)
      );
      list.add("");
      list.addAll(Lang.getStringList("messages.normal-operation.viewer.item-flow-viewer-click-behavior"));
      return list;
   }

   @NotNull
   public static List<String> getLoreAddition(@NotNull ItemFlowRecord.TransportAction entry) {
      Location loc = entry.accessor();
      long change = entry.amount();
      List<String> list = new ArrayList<>();
      list.add("");
      list.add(String.format(Lang.getString("messages.normal-operation.viewer.location"), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
      list.add(String.format(Lang.getString("messages.normal-operation.viewer.when"), humanizeTime(entry.milliSecond())));
      list.add(
         (change > 0L ? TextUtil.GREEN : (change < 0L ? TextUtil.RED : TextUtil.GRAY))
            + String.format(Lang.getString("messages.normal-operation.viewer.change"), change > 0L ? "+" + change : change)
      );
      list.add("");
      list.addAll(Lang.getStringList("messages.normal-operation.viewer.item-flow-viewer-sub-click-behavior"));
      return list;
   }

   @NotNull
   public static String humanizeTime(long milliSecond) {
      Date date = new Date(milliSecond);
      return DATE_FORMAT.format(date);
   }

   @NotNull
   public static ItemStack getIcon(@NotNull ItemFlowRecord.TransportAction action) {
      SlimefunItem sf = StorageCacheUtils.getSfItem(action.accessor());
      return sf == null ? Icon.UNKNOWN_ITEM.clone() : sf.getItem().clone();
   }

   public void setSubMenu(@NotNull BlockMenu menu, @NotNull GridCache cache, @Nullable ItemStack itemStack) {
      SlimefunBlockData data = StorageCacheUtils.getBlock(menu.getLocation());
      if (data != null) {
         cache.setFilter(null);
         if (itemStack != null) {
            data.setData("sub-menu", serializeIcon(itemStack));
         } else {
            data.removeData("sub-menu");
         }
      }
   }

   @Nullable
   public String getSubMenu(@NotNull BlockMenu menu) {
      SlimefunBlockData data = StorageCacheUtils.getBlock(menu.getLocation());
      return data == null ? null : data.getData("sub-menu");
   }

   public void updateDisplay(@Nullable BlockMenu blockMenu) {
      if (blockMenu != null) {
         Location location = blockMenu.getLocation();
         NodeDefinition definition = NetworkStorage.getNode(location);
         if (definition != null && definition.getNode() != null) {
            NetworkRoot root = definition.getNode().getRoot();
            if (!blockMenu.hasViewer()) {
               ItemFlowRecord flow = root.getItemFlowRecord();
               if (flow != null && System.currentTimeMillis() - flow.getLastChangeTime() > ItemFlowRecord.DEADLINE) {
                  NetworkController.disableRecord(root.getController());
               } else {
                  this.sendFeedback(blockMenu.getLocation(), FeedbackType.AFK);
               }
            } else {
               GridCache gridCache = this.getCacheMap().get(location);
               String subMenu = this.getSubMenu(blockMenu);
               if (subMenu != null) {
                  this.subMenu(root, blockMenu, gridCache, subMenu);
               } else {
                  this.mainMenu(root, blockMenu, gridCache);
               }
            }
         } else {
            this.sendFeedback(blockMenu.getLocation(), FeedbackType.NO_NETWORK_FOUND);
         }
      }
   }

   public void subMenu(@NotNull NetworkRoot root, @NotNull BlockMenu blockMenu, @NotNull GridCache gridCache, @NotNull String subMenu) {
      List<ItemFlowRecord.TransportAction> entries = getSubMenu(root, gridCache, deserializeIcon(subMenu));
      int pages = (int)Math.ceil((double)entries.size() / (double)this.getDisplaySlots().length) - 1;
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

         int end = Math.min(start + this.getDisplaySlots().length, entries.size());
         List<ItemFlowRecord.TransportAction> validActions = entries.subList(start, end);
         this.getCacheMap().put(blockMenu.getLocation(), gridCache);

         for (int i = 0; i < this.getDisplaySlots().length; i++) {
            if (validActions.size() > i) {
               ItemFlowRecord.TransportAction action = validActions.get(i);
               ItemStack displayItemStack = getIcon(action);
               ItemStack displayStack = new CustomItemStack(
                  displayItemStack.clone(), TextUtil.GRAY + ItemStackHelper.getDisplayName(displayItemStack), new String[0]
               );
               ItemMeta itemMeta = displayStack.getItemMeta();
               if (itemMeta != null) {
                  List<String> lore = getLoreAddition(action);
                  itemMeta.setLore(lore);
                  displayStack.setItemMeta(itemMeta);
                  blockMenu.replaceExistingItem(this.getDisplaySlots()[i], displayStack);
                  blockMenu.addMenuClickHandler(this.getDisplaySlots()[i], (player, slot, item, a) -> {
                     ParticleUtil.highlightBlock(player, action.accessor(), 10);
                     return false;
                  });
               }
            } else {
               blockMenu.replaceExistingItem(this.getDisplaySlots()[i], this.getBlankSlotStack());
               blockMenu.addMenuClickHandler(this.getDisplaySlots()[i], (p, slot, item, actionx) -> false);
            }
         }

         blockMenu.replaceExistingItem(
            this.getPagePrevious(), Icon.getPageStack(this.getPagePreviousStack(), gridCache.getPage() + 1, gridCache.getMaxPages() + 1)
         );
         blockMenu.replaceExistingItem(this.getPageNext(), Icon.getPageStack(this.getPageNextStack(), gridCache.getPage() + 1, gridCache.getMaxPages() + 1));
         this.sendFeedback(blockMenu.getLocation(), FeedbackType.WORKING);
      }
   }

   public void mainMenu(@NotNull NetworkRoot root, @NotNull BlockMenu blockMenu, @NotNull GridCache gridCache) {
      List<ItemFlowViewer.DisplayEntry> entries = getRecords(root, gridCache);
      int pages = (int)Math.ceil((double)entries.size() / (double)this.getDisplaySlots().length) - 1;
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

         int end = Math.min(start + this.getDisplaySlots().length, entries.size());
         List<ItemFlowViewer.DisplayEntry> validEntries = entries.subList(start, end);
         this.getCacheMap().put(blockMenu.getLocation(), gridCache);

         for (int i = 0; i < this.getDisplaySlots().length; i++) {
            if (validEntries.size() > i) {
               ItemFlowViewer.DisplayEntry entry = validEntries.get(i);
               ItemStack displayItemStack = entry.itemStack();
               if (displayItemStack != null) {
                  ItemStack displayStack = null;
                  if (displayItemStack.getType() == Material.AIR) {
                     displayStack = new ItemStack(Material.BARRIER);
                  }

                  if (displayStack == null) {
                     displayStack = displayItemStack.clone();
                  }

                  ItemStack var15 = new CustomItemStack(displayStack, TextUtil.GRAY + ItemStackHelper.getDisplayName(displayItemStack), new String[0]);
                  ItemMeta itemMeta = var15.getItemMeta();
                  if (itemMeta != null) {
                     List<String> lore = getLoreAddition(entry);
                     itemMeta.setLore(lore);
                     var15.setItemMeta(itemMeta);
                     blockMenu.replaceExistingItem(this.getDisplaySlots()[i], var15);
                     blockMenu.addMenuClickHandler(this.getDisplaySlots()[i], (player, slot, item, action) -> {
                        this.setSubMenu(blockMenu, gridCache, displayItemStack);
                        return false;
                     });
                  }
               }
            } else {
               blockMenu.replaceExistingItem(this.getDisplaySlots()[i], this.getBlankSlotStack());
               blockMenu.addMenuClickHandler(this.getDisplaySlots()[i], (p, slot, item, action) -> false);
            }
         }

         blockMenu.replaceExistingItem(
            this.getPagePrevious(), Icon.getPageStack(this.getPagePreviousStack(), gridCache.getPage() + 1, gridCache.getMaxPages() + 1)
         );
         blockMenu.replaceExistingItem(this.getPageNext(), Icon.getPageStack(this.getPageNextStack(), gridCache.getPage() + 1, gridCache.getMaxPages() + 1));
         this.sendFeedback(blockMenu.getLocation(), FeedbackType.WORKING);
      }
   }

   public void postRegister() {
      this.getPreset();
   }

   @NotNull
   protected BlockMenuPreset getPreset() {
      return new BlockMenuPreset(this.getId(), this.getItemName()) {
         public void init() {
            this.drawBackground(ItemFlowViewer.this.getBackgroundSlots());
            this.drawBackground(ItemFlowViewer.this.getDisplaySlots());
            this.setSize(54);
         }

         public boolean canOpen(@NotNull Block block, @NotNull Player player) {
            return player.hasPermission("slimefun.inventory.bypass")
               || ExpansionItems.ITEM_FLOW_VIEWER.canUse(player, false)
                  && Slimefun.getProtectionManager().hasPermission(player, block.getLocation(), Interaction.INTERACT_BLOCK);
         }

         public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
            return new int[0];
         }

         public void newInstance(@NotNull BlockMenu menu, @NotNull Block b) {
            ItemFlowViewer.this.getCacheMap().put(menu.getLocation(), new GridCache(0, 0, GridCache.SortOrder.ALPHABETICAL));
            menu.replaceExistingItem(ItemFlowViewer.this.getPagePrevious(), ItemFlowViewer.this.getPagePreviousStack());
            menu.addMenuClickHandler(ItemFlowViewer.this.getPagePrevious(), (p, slot, item, action) -> {
               GridCache gridCache = ItemFlowViewer.this.getCacheMap().get(menu.getLocation());
               gridCache.setPage(gridCache.getPage() <= 0 ? 0 : gridCache.getPage() - 1);
               ItemFlowViewer.this.getCacheMap().put(menu.getLocation(), gridCache);
               ItemFlowViewer.this.updateDisplay(menu);
               return false;
            });
            menu.replaceExistingItem(ItemFlowViewer.this.getPageNext(), ItemFlowViewer.this.getPageNextStack());
            menu.addMenuClickHandler(ItemFlowViewer.this.getPageNext(), (p, slot, item, action) -> {
               GridCache gridCache = ItemFlowViewer.this.getCacheMap().get(menu.getLocation());
               gridCache.setPage(gridCache.getPage() >= gridCache.getMaxPages() ? gridCache.getMaxPages() : gridCache.getPage() + 1);
               ItemFlowViewer.this.getCacheMap().put(menu.getLocation(), gridCache);
               ItemFlowViewer.this.updateDisplay(menu);
               return false;
            });
            menu.replaceExistingItem(ItemFlowViewer.this.getFilterSlot(), ItemFlowViewer.this.getFilterStack());
            menu.addMenuClickHandler(ItemFlowViewer.this.getFilterSlot(), (p, slot, item, action) -> {
               GridCache gridCache = ItemFlowViewer.this.getCacheMap().get(menu.getLocation());
               ItemFlowViewer.this.setFilter(p, menu, gridCache, action);
               return false;
            });

            for (int displaySlot : ItemFlowViewer.this.getDisplaySlots()) {
               menu.replaceExistingItem(displaySlot, ChestMenuUtils.getBackground());
               menu.addMenuClickHandler(displaySlot, (p, slot, item, action) -> false);
            }

            menu.addItem(8, Icon.ITEM_FLOW_VIEWER_BACK_TO_MAIN);
            menu.addMenuClickHandler(8, (p, slot, item, action) -> {
               GridCache gridCache = ItemFlowViewer.this.getCacheMap().get(menu.getLocation());
               ItemFlowViewer.this.setSubMenu(menu, gridCache, null);
               return false;
            });
            menu.addItem(17, Icon.ITEM_FLOW_VIEWER_FORCE_CLEAN);
            menu.addMenuClickHandler(17, (p, slot, item, action) -> {
               NodeDefinition definition = NetworkStorage.getNode(menu.getLocation());
               if (definition != null && definition.getNode() != null) {
                  ItemFlowViewer.this.forceCleanHistory(definition.getNode().getRoot());
               }

               return false;
            });
            menu.addMenuCloseHandler(p -> {
               NodeDefinition definition = NetworkStorage.getNode(menu.getLocation());
               if (definition != null && definition.getNode() != null) {
                  NetworkController.disableRecord(definition.getNode().getRoot().getController());
               }
            });
            menu.addMenuOpeningHandler(p -> {
               NodeDefinition definition = NetworkStorage.getNode(menu.getLocation());
               if (definition != null && definition.getNode() != null) {
                  NetworkController.enableRecord(definition.getNode().getRoot().getController());
               }
            });
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
   protected ItemStack getFilterStack() {
      return Icon.FILTER_STACK;
   }

   protected void clearDisplay(@NotNull BlockMenu blockMenu) {
      for (int displaySlot : this.getDisplaySlots()) {
         blockMenu.replaceExistingItem(displaySlot, this.getBlankSlotStack());
         blockMenu.addMenuClickHandler(displaySlot, (p, slot, item, action) -> false);
      }
   }

   public void forceCleanHistory(@NotNull NetworkRoot root) {
      ItemFlowRecord r = NetworkController.getRecords().get(root.getController());
      if (r != null) {
         r.forceGC();
      }
   }

   public static record DisplayEntry(ItemStack itemStack, List<ItemFlowRecord.TransportAction> actions) {
   }
}
