package com.balugaq.netex.integrations.logitech;

import com.balugaq.netex.api.data.ItemContainer;
import com.balugaq.netex.api.data.ItemFlowRecord;
import com.balugaq.netex.api.data.StorageUnitData;
import com.balugaq.netex.api.enums.FeedbackType;
import com.balugaq.netex.api.helpers.Icon;
import com.balugaq.netex.utils.Debug;
import com.balugaq.netex.utils.InventoryUtil;
import com.balugaq.netex.utils.Lang;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.core.items.machines.AbstractGridNewStyle;
import com.ytdd9527.networksexpansion.implementation.ExpansionItems;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.api.DriveType;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.CellHandle;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.drive.CellDrive;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.ItemKey;
import com.ytdd9527.networksexpansion.implementation.machines.unit.NetworksDrawer;
import com.ytdd9527.networksexpansion.utils.ReflectionUtil;
import com.ytdd9527.networksexpansion.utils.TextUtil;
import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.NodeDefinition;
import io.github.sefiraat.networks.network.NodeType;
import io.github.sefiraat.networks.network.stackcaches.BarrelIdentity;
import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import io.github.sefiraat.networks.slimefun.network.NetworkController;
import io.github.sefiraat.networks.slimefun.network.NetworkObject;
import io.github.sefiraat.networks.slimefun.network.grid.AbstractGrid;
import io.github.sefiraat.networks.slimefun.network.grid.GridCache;
import io.github.sefiraat.networks.utils.StackUtils;
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
import java.util.Map.Entry;
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

public class LinkerGrid extends NetworkObject {
   public static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(3, 2);
   private static final Map<Location, GridCache> CACHE_MAP = new HashMap<>();
   private static final int TYPE_SWITCH_SLOT = 17;
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
   private static final int FILTER = 26;
   private static final int CHANGE_SORT = 35;
   private static final int PAGE_PREVIOUS = 44;
   private static final int PAGE_NEXT = 53;
   private static final String NAMESPACE_SF = "sf";
   private static final String NAMESPACE_MC = "mc";
   private static final String BS_LINKER_TYPE = "LinkerType";
   private static final String BS_LINKER_TARGET = "LinkerTarget";
   private static final String BS_GRID_FILTER = "GridFilter";
   private static final String BS_GRID_SORT = "GridSort";
   private static final int TARGET_SWITCH_SLOT = 8;
   @NotNull
   private final IntRangeSetting tickRate = new IntRangeSetting(this, "tick_rate", 1, 1, 10);
   private final ItemStack HyperLinkStack;
   private final ItemStack QuantumLinkStack;
   private static final Class<?> hyperLinkClass = ReflectionUtil.getClass(
      "me.matl114.logitech.core.Cargo.Links.HyperLink", "me.matl114.logitech.SlimefunItem.Cargo.Links.HyperLink"
   );
   private static final Class<?> storageLinkClass = ReflectionUtil.getClass(
      "me.matl114.logitech.core.Cargo.Links.StorageLink", "me.matl114.logitech.SlimefunItem.Cargo.Links.StorageLink"
   );
   public static boolean initialized = false;

   public LinkerGrid(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe) {
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

               LinkerGrid.this.addToRegistry(block);
               GridCache cache = LinkerGrid.this.getCacheMap().get(block.getLocation());
               cache.setEntriesCache(null);
               LinkerGrid.this.updateDisplay(blockMenu);
            }
         }

         public void uniqueTick() {
            this.tick = this.tick <= 1 ? (Integer)LinkerGrid.this.tickRate.getValue() : this.tick - 1;
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
      this.HyperLinkStack = SlimefunItem.getById("LOGITECH_HYPER_LINK").getItem();
      this.QuantumLinkStack = SlimefunItem.getById("LOGITECH_QUANTUM_LINK").getItem();
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
   public static List<String> getLoreAddition(@NotNull ItemStack itemStack) {
      List<String> list = new ArrayList<>();
      list.add("");
      list.addAll(Lang.getStringList("messages.normal-operation.viewer.linker-grid-click-behavior"));
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

   public void updateDisplay(@Nullable BlockMenu blockMenu) {
      if (blockMenu != null) {
         Location location = blockMenu.getLocation();
         NodeDefinition definition = NetworkStorage.getNode(location);
         if (definition != null && definition.getNode() != null) {
            NetworkRoot root = definition.getNode().getRoot();
            if (!blockMenu.hasViewer()) {
               this.sendFeedback(blockMenu.getLocation(), FeedbackType.AFK);
            } else {
               GridCache gridCache = this.getCacheMap().get(location);
               this.mainMenu(root, blockMenu, gridCache);
            }
         } else {
            this.sendFeedback(blockMenu.getLocation(), FeedbackType.NO_NETWORK_FOUND);
         }
      }
   }

   public void mainMenu(@NotNull NetworkRoot root, @NotNull BlockMenu blockMenu, @NotNull GridCache gridCache) {
      List<ItemStack> entries = AbstractGridNewStyle.getEntries0(root, gridCache).stream().map(Entry::getKey).toList();
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
         List<ItemStack> validEntries = entries.subList(start, end);
         this.getCacheMap().put(blockMenu.getLocation(), gridCache);

         for (int i = 0; i < this.getDisplaySlots().length; i++) {
            if (validEntries.size() > i) {
               ItemStack stack = validEntries.get(i);
               if (stack != null) {
                  ItemStack displayStack = null;
                  if (stack.getType() == Material.AIR) {
                     displayStack = new ItemStack(Material.BARRIER);
                  }

                  if (displayStack == null) {
                     displayStack = stack.clone();
                  }

                  ItemStack var14 = new CustomItemStack(displayStack, TextUtil.GRAY + ItemStackHelper.getDisplayName(stack), new String[0]);
                  ItemMeta itemMeta = var14.getItemMeta();
                  if (itemMeta != null) {
                     List<String> lore = getLoreAddition(stack);
                     itemMeta.setLore(lore);
                     var14.setItemMeta(itemMeta);
                     blockMenu.replaceExistingItem(this.getDisplaySlots()[i], var14);
                     blockMenu.addMenuClickHandler(this.getDisplaySlots()[i], (player, slot, item, action) -> {
                        if (!initialized) {
                           player.sendMessage(Lang.getString("messages.unsupported-operation.viewer.not-initialized"));
                           return false;
                        } else {
                           this.tryGetLink(blockMenu, player, stack);
                           return false;
                        }
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

   public void tryGetLink(@NotNull BlockMenu blockMenu, @NotNull Player player, @NotNull ItemStack itemStack) {
      NodeDefinition definition = NetworkStorage.getNode(blockMenu.getLocation());
      if (definition != null && definition.getNode() != null) {
         NetworkRoot root = definition.getNode().getRoot();
         LinkerType tp = getLinkerType(blockMenu.getLocation());
         Location location = getLocationOf(root, itemStack, tp, getLinkerTarget(blockMenu.getLocation()));
         if (location == null) {
            this.sendFeedback(blockMenu.getLocation(), FeedbackType.NO_LOCATION_FOUND);
            player.sendMessage(Lang.getString("messages.unsupported-operation.viewer.location-not-found"));
         } else {
            Interaction[] interactions = new Interaction[]{Interaction.INTERACT_BLOCK, Interaction.BREAK_BLOCK, Interaction.PLACE_BLOCK};

            for (Interaction interaction : interactions) {
               if (!Slimefun.getProtectionManager().hasPermission(player, location, interaction)) {
                  this.sendFeedback(blockMenu.getLocation(), FeedbackType.NO_PERMISSION);
                  player.sendMessage(Lang.getString("messages.unsupported-operation.viewer.no-permission"));
                  return;
               }
            }

            ItemStack link = this.getLinkStack(blockMenu, root, player);
            if (link == null) {
               this.sendFeedback(blockMenu.getLocation(), FeedbackType.NOT_ENOUGH_ITEMS);
               player.sendMessage(Lang.getString("messages.unsupported-operation.viewer.link-stack-not-found"));
            } else {
               ItemMeta meta = link.getItemMeta();
               if ((tp != LinkerType.HyperLink || canLink$HyperLink(meta)) && (tp != LinkerType.QuantumLink || canLink$QuantumLink(meta))) {
                  Location bindLocation = location;
                  if (tp == LinkerType.QuantumLink) {
                     SlimefunItem targetItem = StorageCacheUtils.getSfItem(location);
                     if (DriveType.of(targetItem) != null) {
                        ItemKey key = new ItemKey(itemStack);
                        CellVirtualCache.inject(location, key);
                        LinkBindingStore.record(location, key.getItemStack());
                        bindLocation = CellVirtualCache.virtualLocation(location, key);
                     } else if (targetItem instanceof NetworksDrawer) {
                        StorageUnitData drawerData = NetworkRoot.getCargoStorageUnitData(location);
                        if (drawerData != null) {
                           DrawerVirtualCache.inject(location, itemStack);
                           LinkBindingStore.record(location, itemStack);
                           bindLocation = DrawerVirtualCache.virtualLocation(location, itemStack);
                        }
                     }
                  }

                  if (tp == LinkerType.HyperLink) {
                     setLink$HyperLink(meta, bindLocation);
                  } else {
                     setLink$QuantumLink(meta, bindLocation);
                  }

                  link.setItemMeta(meta);
                  InventoryUtil.give(player, link);
                  if (tp == LinkerType.QuantumLink) {
                     this.sendFeedback(blockMenu.getLocation(), FeedbackType.WORKING);
                     player.sendMessage(Lang.getString("messages.completed-operation.viewer.linked-stack"));
                  }
               } else {
                  this.sendFeedback(blockMenu.getLocation(), FeedbackType.NOT_ENOUGH_ITEMS);
                  player.sendMessage(Lang.getString("messages.unsupported-operation.viewer.link-stack-not-found"));
               }
            }
         }
      } else {
         this.sendFeedback(blockMenu.getLocation(), FeedbackType.NO_NETWORK_FOUND);
      }
   }

   public static Location getLocationOf(NetworkRoot root, ItemStack itemStack, LinkerType type, LinkerTarget target) {
      Location location = null;
      if (type == LinkerType.QuantumLink) {
         if (target == LinkerTarget.AUTO || target == LinkerTarget.QUANTUM_STORAGE) {
            for (BarrelIdentity b : root.getBarrels()) {
               if (StackUtils.itemsMatch(b, itemStack)) {
                  location = b.getLocation();
                  break;
               }
            }
         }

         if (location == null && (target == LinkerTarget.AUTO || target == LinkerTarget.CELL)) {
            ItemKey key = new ItemKey(itemStack);

            for (BlockMenu driveMenu : root.getOutputAbleCellDriveMenus()) {
               for (CellHandle cell : CellDrive.getStorage().getCells(driveMenu)) {
                  if (cell.getAmount(key) > 0L) {
                     location = driveMenu.getLocation();
                     break;
                  }
               }

               if (location != null) {
                  break;
               }
            }
         }

         if (location == null && (target == LinkerTarget.AUTO || target == LinkerTarget.DRAWER)) {
            for (StorageUnitData c : root.getDrawerData().values()) {
               for (ItemContainer ic : c.getStoredItemsDirectly()) {
                  if (StackUtils.itemsMatch(ic, itemStack)) {
                     location = c.getLastLocation();
                     break;
                  }
               }

               if (location != null) {
                  break;
               }
            }
         }

         return location;
      } else {
         for (BarrelIdentity bx : root.getBarrels()) {
            if (StackUtils.itemsMatch(bx, itemStack)) {
               return bx.getLocation();
            }
         }

         for (StorageUnitData c : root.getDrawerData().values()) {
            for (ItemContainer icx : c.getStoredItemsDirectly()) {
               if (StackUtils.itemsMatch(icx, itemStack)) {
                  return c.getLastLocation();
               }
            }
         }

         for (BlockMenu g : root.getGreedyBlockMenus()) {
            int[] slots = g.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            ItemStack s = g.getItemInSlot(slots[0]);
            if (StackUtils.itemsMatch(s, itemStack)) {
               return g.getLocation();
            }
         }

         for (BlockMenu gx : root.getAdvancedGreedyBlockMenus()) {
            int[] slots = gx.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);

            for (int slot : slots) {
               ItemStack s = gx.getItemInSlot(slot);
               if (StackUtils.itemsMatch(s, itemStack)) {
                  return gx.getLocation();
               }
            }
         }

         for (BlockMenu c : root.getCellMenus()) {
            if (!NetworkRoot.isFakeCell(c)) {
               for (int slotx : NetworkRoot.CELL_AVAILABLE_SLOTS) {
                  if (StackUtils.itemsMatch(c.getItemInSlot(slotx), itemStack)) {
                     return c.getLocation();
                  }
               }
            }
         }

         for (BlockMenu cx : root.getCrafterOutputs()) {
            int[] slots = cx.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);

            for (int slotxx : slots) {
               ItemStack s = cx.getItemInSlot(slotxx);
               if (StackUtils.itemsMatch(s, itemStack)) {
                  return cx.getLocation();
               }
            }
         }

         return null;
      }
   }

   public ItemStack getLink(Location location) {
      return getLinkerType(location) == LinkerType.HyperLink ? this.HyperLinkStack : this.QuantumLinkStack;
   }

   @NotNull
   public static LinkerType getLinkerType(Location location) {
      String s = StorageCacheUtils.getData(location, "LinkerType");
      return s == null ? LinkerType.QuantumLink : LinkerType.valueOf(s);
   }

   @NotNull
   public static LinkerTarget getLinkerTarget(Location location) {
      return LinkerTarget.of(StorageCacheUtils.getData(location, "LinkerTarget"));
   }

   @NotNull
   private static GridCache.SortOrder restoredSort(@NotNull Location location) {
      String s = StorageCacheUtils.getData(location, "GridSort");
      if (s != null) {
         try {
            return GridCache.SortOrder.valueOf(s);
         } catch (IllegalArgumentException var3) {
            Debug.debug("Invalid sort record, falling back to default: " + s);
         }
      }

      return GridCache.SortOrder.ALPHABETICAL;
   }

   private void setLinkerTarget(Location location, LinkerTarget target) {
      StorageCacheUtils.setData(location, "LinkerTarget", target.name());
   }

   @NotNull
   public ItemStack getLinkerTargetStack(@NotNull Location location) {
      LinkerTarget target = getLinkerTarget(location);
      ItemStack stack = Icon.LINKER_TARGET_SWITCH.clone();
      stack.editMeta(
         meta -> {
            List<String> lore = meta.getLore();
            if (lore == null) {
               lore = new ArrayList<>();
            }

            lore.add(
               Lang.getString(
                  "messages.normal-operation.viewer.linker-target-current",
                  Lang.getString("messages.normal-operation.viewer.linker-target-" + target.name().toLowerCase(Locale.ROOT))
               )
            );
            meta.setLore(lore);
         }
      );
      return stack;
   }

   @Nullable
   private ItemStack getLinkStack(@NotNull BlockMenu menu, @NotNull NetworkRoot root, @NotNull Player player) {
      ItemStack link = root.getItemStack(menu.getLocation(), new ItemRequest(this.getLink(menu.getLocation()), 1));
      if (link == null) {
         for (ItemStack stack : player.getInventory()) {
            if (StackUtils.itemsMatch(stack, this.getLink(menu.getLocation()))) {
               link = StackUtils.getAsQuantity(stack, 1);
               stack.setAmount(stack.getAmount() - 1);
               break;
            }
         }
      }

      return link;
   }

   public void postRegister() {
      this.getPreset();
   }

   @NotNull
   protected BlockMenuPreset getPreset() {
      return new BlockMenuPreset(this.getId(), this.getItemName()) {
         public void init() {
            this.drawBackground(LinkerGrid.this.getBackgroundSlots());
            this.drawBackground(LinkerGrid.this.getDisplaySlots());
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
            GridCache cache = new GridCache(0, 0, LinkerGrid.restoredSort(menu.getLocation()));
            String filter = StorageCacheUtils.getData(menu.getLocation(), "GridFilter");
            if (filter != null && !filter.isEmpty()) {
               cache.setFilter(filter);
            }

            LinkerGrid.this.getCacheMap().put(menu.getLocation(), cache);
            menu.replaceExistingItem(LinkerGrid.this.getPagePrevious(), LinkerGrid.this.getPagePreviousStack());
            menu.addMenuClickHandler(LinkerGrid.this.getPagePrevious(), (p, slot, item, action) -> {
               GridCache gridCache = LinkerGrid.this.getCacheMap().get(menu.getLocation());
               gridCache.setPage(gridCache.getPage() <= 0 ? 0 : gridCache.getPage() - 1);
               LinkerGrid.this.getCacheMap().put(menu.getLocation(), gridCache);
               LinkerGrid.this.updateDisplay(menu);
               return false;
            });
            menu.replaceExistingItem(LinkerGrid.this.getPageNext(), LinkerGrid.this.getPageNextStack());
            menu.addMenuClickHandler(LinkerGrid.this.getPageNext(), (p, slot, item, action) -> {
               GridCache gridCache = LinkerGrid.this.getCacheMap().get(menu.getLocation());
               gridCache.setPage(gridCache.getPage() >= gridCache.getMaxPages() ? gridCache.getMaxPages() : gridCache.getPage() + 1);
               LinkerGrid.this.getCacheMap().put(menu.getLocation(), gridCache);
               LinkerGrid.this.updateDisplay(menu);
               return false;
            });
            menu.replaceExistingItem(LinkerGrid.this.getChangeSort(), LinkerGrid.this.getChangeSortStack());
            menu.addMenuClickHandler(LinkerGrid.this.getChangeSort(), (p, slot, item, action) -> {
               GridCache gridCache = LinkerGrid.this.getCacheMap().get(menu.getLocation());
               AbstractGrid.updateSortOrder(gridCache, action, 4);
               LinkerGrid.this.getCacheMap().put(menu.getLocation(), gridCache);
               StorageCacheUtils.setData(menu.getLocation(), "GridSort", gridCache.getSortOrder().name());
               LinkerGrid.this.updateDisplay(menu);
               return false;
            });
            menu.replaceExistingItem(LinkerGrid.this.getFilterSlot(), LinkerGrid.this.getFilterStack());
            menu.addMenuClickHandler(LinkerGrid.this.getFilterSlot(), (p, slot, item, action) -> {
               GridCache gridCache = LinkerGrid.this.getCacheMap().get(menu.getLocation());
               LinkerGrid.this.setFilter(p, menu, gridCache, action);
               return false;
            });

            for (int displaySlot : LinkerGrid.this.getDisplaySlots()) {
               menu.replaceExistingItem(displaySlot, ChestMenuUtils.getBackground());
               menu.addMenuClickHandler(displaySlot, (p, slot, item, action) -> false);
            }

            menu.addItem(17, LinkerGrid.this.getLinkerTypeStack(menu.getLocation()));
            menu.addMenuClickHandler(17, (p, slot, item, action) -> {
               LinkerGrid.this.setLinkerType(menu.getLocation(), switch (LinkerGrid.getLinkerType(menu.getLocation())) {
                  case HyperLink -> LinkerType.QuantumLink;
                  case QuantumLink -> LinkerType.HyperLink;
               });
               menu.replaceExistingItem(17, LinkerGrid.this.getLinkerTypeStack(menu.getLocation()));
               return false;
            });
            menu.addItem(8, LinkerGrid.this.getLinkerTargetStack(menu.getLocation()));
            menu.addMenuClickHandler(8, (p, slot, item, action) -> {
               LinkerGrid.this.setLinkerTarget(menu.getLocation(), LinkerGrid.getLinkerTarget(menu.getLocation()).next());
               menu.replaceExistingItem(8, LinkerGrid.this.getLinkerTargetStack(menu.getLocation()));
               return false;
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
         this.getCacheMap().put(blockMenu.getLocation(), gridCache);
         StorageCacheUtils.removeData(blockMenu.getLocation(), "GridFilter");
      } else {
         player.closeInventory();
         player.sendMessage(Lang.getString("messages.normal-operation.grid.waiting_for_filter"));
         ChatUtils.awaitInput(player, s -> {
            if (!s.isBlank()) {
               s = s.toLowerCase(Locale.ROOT);
               gridCache.setFilter(s);
               this.getCacheMap().put(blockMenu.getLocation(), gridCache);
               StorageCacheUtils.setData(blockMenu.getLocation(), "GridFilter", s);
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

   public ItemStack getLinkerTypeStack(@NotNull Location location) {
      return switch (getLinkerType(location)) {
         case HyperLink -> Icon.LINKER_TYPE_HYPER_LINK;
         case QuantumLink -> Icon.LINKER_TYPE_QUANTUM_LINK;
      };
   }

   public int getChangeSort() {
      return 35;
   }

   @NotNull
   protected ItemStack getChangeSortStack() {
      return Icon.CHANGE_SORT_STACK;
   }

   private void setLinkerType(Location location, LinkerType linkerType) {
      StorageCacheUtils.setData(location, "LinkerType", linkerType.name());
   }

   public static boolean canLink$HyperLink(ItemMeta meta) {
      return (Boolean)ReflectionUtil.invokeStaticMethod(hyperLinkClass, "canLink", meta);
   }

   public static boolean canLink$QuantumLink(ItemMeta meta) {
      return (Boolean)ReflectionUtil.invokeStaticMethod(storageLinkClass, "canLink", meta);
   }

   public static void setLink$HyperLink(ItemMeta meta, Location location) {
      ReflectionUtil.invokeStaticMethod(hyperLinkClass, "setLink", meta, location);
   }

   public static void setLink$QuantumLink(ItemMeta meta, Location location) {
      ReflectionUtil.invokeStaticMethod(storageLinkClass, "setLink", meta, location);
   }

   static {
      initialized = hyperLinkClass != null && storageLinkClass != null;
   }
}
