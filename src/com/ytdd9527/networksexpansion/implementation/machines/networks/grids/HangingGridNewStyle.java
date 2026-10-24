package com.ytdd9527.networksexpansion.implementation.machines.networks.grids;

import com.balugaq.netex.api.enums.FeedbackType;
import com.balugaq.netex.api.helpers.Icon;
import com.balugaq.netex.api.interfaces.HangingBlock;
import com.balugaq.netex.api.visual.Screen;
import com.balugaq.netex.utils.Lang;
import com.balugaq.netex.utils.MapUtil;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.implementation.ExpansionItems;
import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.NodeDefinition;
import io.github.sefiraat.networks.network.NodeType;
import io.github.sefiraat.networks.slimefun.network.NetworkDirectional;
import io.github.sefiraat.networks.slimefun.network.grid.GridCache;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.Placeable;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.utils.ChatUtils;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import io.papermc.paper.event.player.PlayerItemFrameChangeEvent;
import io.papermc.paper.event.player.PlayerItemFrameChangeEvent.ItemFrameChangeAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.ParametersAreNonnullByDefault;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class HangingGridNewStyle extends NetworkGridNewStyle implements HangingBlock, Placeable {
   public static final String layer = "/textures/layer/terminal.png";
   public static final Map<Location, BlockMenu> menus = new HashMap<>();
   @Nullable
   public BlockMenuPreset preset = null;

   public HangingGridNewStyle(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe, NodeType.HANGING_GRID);
      HangingBlock.registerHangingBlock(this);
   }

   @NotNull
   public static String fixedKey(String key, @NotNull BlockFace attachSide) {
      return "netex-hanging-" + attachSide.name().toLowerCase() + "-" + key;
   }

   @NotNull
   public BlockMenu getOrCreateMenu(@NotNull Location fixedLocation) {
      BlockMenu menu = menus.get(fixedLocation);
      if (menu != null) {
         return menu;
      } else {
         menu = new BlockMenu(this.getPreset(), fixedLocation);
         menus.put(fixedLocation, menu);
         return menu;
      }
   }

   @ParametersAreNonnullByDefault
   @Override
   public void onPlace(HangingPlaceEvent event, Location attachon, ItemFrame entityBlock, BlockFace attachSide) {
      HangingBlock.super.onPlace(event, attachon, entityBlock, attachSide);
      this.onFirstTick0(attachon, entityBlock);
   }

   @ParametersAreNonnullByDefault
   @Override
   public void onInteract(Location attachon, PlayerItemFrameChangeEvent event) {
      event.setCancelled(true);
      if (event.getAction() == ItemFrameChangeAction.ROTATE) {
         Location fixed = this.getFixedLocation(attachon, event.getItemFrame().getAttachedFace());
         BlockMenu menu = this.getOrCreateMenu(fixed);
         menu.open(new Player[]{event.getPlayer()});
      } else if (event.getAction() == ItemFrameChangeAction.REMOVE) {
         this.breakBlock(attachon, event.getItemFrame());
      }
   }

   @ParametersAreNonnullByDefault
   @Override
   public void onTick(Location attachon, BlockFace attachSide, ItemFrame entityBlock) {
      Location fixed = this.getFixedLocation(attachon, attachSide);
      BlockMenu menu = this.getOrCreateMenu(fixed);
      this.updateDisplay(Screen.of(menu), attachon, attachSide);
   }

   @ParametersAreNonnullByDefault
   @Override
   public void onFirstTick(Location attachon, SlimefunBlockData attachonData, BlockFace attachSide, ItemFrame entityBlock) {
      this.onFirstTick0(attachon, entityBlock);
   }

   @ParametersAreNonnullByDefault
   public void onFirstTick0(Location attachon, ItemFrame entityBlock) {
      entityBlock.setItem((ItemStack)MapUtil.getImageItem("/textures/layer/terminal.png").getFirstValue(), false);
      this.getCacheMap().put(this.getFixedLocation(attachon, entityBlock.getAttachedFace()), new GridCache(0, 0, GridCache.SortOrder.ALPHABETICAL));
   }

   protected void updateDisplay(@NotNull Screen screen, @NotNull Location attachon, @NotNull BlockFace attachSide) {
      if (!screen.hasViewer()) {
         this.sendFeedback(screen.getLocation(), FeedbackType.AFK);
      } else {
         NodeDefinition definition = NetworkStorage.getNode(attachon);
         if (definition != null && definition.getNode() != null) {
            NetworkRoot root = definition.getNode().getRoot();
            GridCache gridCache = this.getCacheMap().get(screen.getLocation().clone());
            SlimefunBlockData data = StorageCacheUtils.getBlock(attachon);
            if (data != null) {
               String filter = data.getData(fixedKey("filter", attachSide));
               if (filter != null) {
                  gridCache.setFilter(filter);
               }

               if (gridCache.getDisplayMode() == GridCache.DisplayMode.DISPLAY) {
                  List<Entry<ItemStack, Long>> entries = this.getEntries(root, gridCache);
                  int pages = (int)Math.ceil((double)entries.size() / (double)this.getDisplaySlots().length) - 1;
                  gridCache.setMaxPages(pages);
                  if (pages < 0) {
                     this.clearDisplay(screen);
                     return;
                  }

                  if (gridCache.getPage() > pages) {
                     gridCache.setPage(0);
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
                              this.displayKeybinds().onClick(player, slot, item, action);
                              this.updateDisplay(screen, attachon, attachSide);
                              return false;
                           });
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
                     gridCache.setPage(0);
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
                           screen.setItem(this.getDisplaySlots()[ix], displayStack, (player, slot, item, action) -> {
                              this.displayKeybinds().onClick(player, slot, item, action);
                              this.updateDisplay(screen, attachon, attachSide);
                              return false;
                           });
                        }
                     } else {
                        screen.setItem(this.getDisplaySlots()[ix], this.getBlankSlotStack(), (p, slot, item, action) -> {
                           this.receiveItem(p, action, screen, attachon, attachSide);
                           return false;
                        });
                     }
                  }
               }

               screen.setItem(this.getPagePrevious(), Icon.getPageStack(this.getPagePreviousStack(), gridCache.getPage() + 1, gridCache.getMaxPages() + 1));
               screen.setItem(this.getPageNext(), Icon.getPageStack(this.getPageNextStack(), gridCache.getPage() + 1, gridCache.getMaxPages() + 1));
               this.sendFeedback(screen.getLocation(), FeedbackType.WORKING);
            }
         } else {
            this.clearDisplay(screen);
            this.sendFeedback(screen.getLocation(), FeedbackType.NO_NETWORK_FOUND);
         }
      }
   }

   protected void setFilter(
      @NotNull Player player,
      @NotNull BlockMenu blockMenu,
      @NotNull GridCache gridCache,
      @NotNull ClickAction action,
      @NotNull Location attachon,
      @NotNull BlockFace attachSide
   ) {
      if (action.isRightClicked()) {
         gridCache.setFilter(null);
         SlimefunBlockData data = StorageCacheUtils.getBlock(attachon);
         if (data == null) {
            return;
         }

         data.removeData(fixedKey("filter", attachSide));
      } else {
         player.closeInventory();
         player.sendMessage(Lang.getString("messages.normal-operation.grid.waiting_for_filter"));
         ChatUtils.awaitInput(player, s -> {
            if (!s.isBlank()) {
               s = s.toLowerCase(Locale.ROOT);
               gridCache.setFilter(s);
               this.getCacheMap().put(blockMenu.getLocation(), gridCache);
               player.sendMessage(Lang.getString("messages.completed-operation.grid.filter_set"));
               SlimefunBlockData datax = StorageCacheUtils.getBlock(attachon);
               if (datax != null) {
                  datax.setData(fixedKey("filter", attachSide), s);
                  this.updateDisplay(Screen.of(blockMenu), attachon, attachSide);
                  blockMenu.open(new Player[]{player});
               }
            }
         });
      }
   }

   @NonNull
   @Override
   protected BlockMenuPreset getPreset() {
      if (this.preset != null) {
         return this.preset;
      } else {
         this.preset = new BlockMenuPreset(this.getId(), this.getItemName()) {
            public void init() {
               this.drawBackground(HangingGridNewStyle.this.getDisplaySlots());
               this.setSize(54);
            }

            public boolean canOpen(@NotNull Block block, @NotNull Player player) {
               return player.hasPermission("slimefun.inventory.bypass")
                  || ExpansionItems.HANGING_GRID_NEW_STYLE.canUse(player, false)
                     && Slimefun.getProtectionManager().hasPermission(player, block.getLocation(), Interaction.INTERACT_BLOCK);
            }

            public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
               return new int[0];
            }

            public void newInstance(@NotNull BlockMenu menu, @NotNull Block b) {
               HangingGridNewStyle.this.getCacheMap().put(menu.getLocation(), new GridCache(0, 0, GridCache.SortOrder.ALPHABETICAL));
               menu.replaceExistingItem(HangingGridNewStyle.this.getPagePrevious(), HangingGridNewStyle.this.getPagePreviousStack());
               menu.addMenuClickHandler(HangingGridNewStyle.this.getPagePrevious(), (p, slot, item, action) -> {
                  GridCache gridCache = HangingGridNewStyle.this.getCacheMap().get(menu.getLocation());
                  gridCache.setPage(gridCache.getPage() <= 0 ? 0 : gridCache.getPage() - 1);
                  HangingGridNewStyle.this.getCacheMap().put(menu.getLocation(), gridCache);
                  Location attachon = HangingGridNewStyle.this.getAttachon(menu.getLocation());
                  BlockFace attachSide = HangingGridNewStyle.this.getAttachSideFromFixed(menu.getLocation());
                  HangingGridNewStyle.this.updateDisplay(Screen.of(menu), attachon, attachSide);
                  return false;
               });
               menu.replaceExistingItem(HangingGridNewStyle.this.getPageNext(), HangingGridNewStyle.this.getPageNextStack());
               menu.addMenuClickHandler(HangingGridNewStyle.this.getPageNext(), (p, slot, item, action) -> {
                  GridCache gridCache = HangingGridNewStyle.this.getCacheMap().get(menu.getLocation());
                  gridCache.setPage(gridCache.getPage() >= gridCache.getMaxPages() ? gridCache.getMaxPages() : gridCache.getPage() + 1);
                  HangingGridNewStyle.this.getCacheMap().put(menu.getLocation(), gridCache);
                  Location attachon = HangingGridNewStyle.this.getAttachon(menu.getLocation());
                  BlockFace attachSide = HangingGridNewStyle.this.getAttachSideFromFixed(menu.getLocation());
                  HangingGridNewStyle.this.updateDisplay(Screen.of(menu), attachon, attachSide);
                  return false;
               });
               menu.replaceExistingItem(HangingGridNewStyle.this.getChangeSort(), HangingGridNewStyle.this.getChangeSortStack());
               menu.addMenuClickHandler(HangingGridNewStyle.this.getChangeSort(), (p, slot, item, action) -> {
                  GridCache gridCache = HangingGridNewStyle.this.getCacheMap().get(menu.getLocation());
                  gridCache.setSortOrder(gridCache.getSortOrder().next());
                  HangingGridNewStyle.this.getCacheMap().put(menu.getLocation(), gridCache);
                  Location attachon = HangingGridNewStyle.this.getAttachon(menu.getLocation());
                  BlockFace attachSide = HangingGridNewStyle.this.getAttachSideFromFixed(menu.getLocation());
                  HangingGridNewStyle.this.updateDisplay(Screen.of(menu), attachon, attachSide);
                  return false;
               });
               menu.replaceExistingItem(HangingGridNewStyle.this.getFilterSlot(), HangingGridNewStyle.this.getFilterStack());
               menu.addMenuClickHandler(
                  HangingGridNewStyle.this.getFilterSlot(),
                  (p, slot, item, action) -> {
                     GridCache gridCache = HangingGridNewStyle.this.getCacheMap().get(menu.getLocation());
                     HangingGridNewStyle.this.setFilter(
                        p, menu, gridCache, action, menu.getLocation(), HangingGridNewStyle.this.getAttachSideFromFixed(menu.getLocation())
                     );
                     return false;
                  }
               );
               menu.replaceExistingItem(HangingGridNewStyle.this.getToggleModeSlot(), HangingGridNewStyle.this.getModeStack(GridCache.DisplayMode.DISPLAY));
               menu.addMenuClickHandler(HangingGridNewStyle.this.getToggleModeSlot(), (p, slot, item, action) -> {
                  if (!action.isRightClicked()) {
                     GridCache gridCache = HangingGridNewStyle.this.getCacheMap().get(menu.getLocation());
                     gridCache.toggleDisplayMode();
                     menu.replaceExistingItem(HangingGridNewStyle.this.getToggleModeSlot(), HangingGridNewStyle.this.getModeStack(gridCache));
                     Location attachon = HangingGridNewStyle.this.getAttachon(menu.getLocation());
                     BlockFace attachSide = HangingGridNewStyle.this.getAttachSideFromFixed(menu.getLocation());
                     HangingGridNewStyle.this.updateDisplay(Screen.of(menu), attachon, attachSide);
                  }

                  return false;
               });

               for (int displaySlot : HangingGridNewStyle.this.getDisplaySlots()) {
                  menu.replaceExistingItem(displaySlot, ChestMenuUtils.getBackground());
                  menu.addMenuClickHandler(displaySlot, (p, slot, item, action) -> false);
               }

               for (int backgroundSlot : HangingGridNewStyle.this.getBackgroundSlots()) {
                  menu.replaceExistingItem(backgroundSlot, ChestMenuUtils.getBackground());
                  menu.addMenuClickHandler(backgroundSlot, (p, slot, item, action) -> false);
               }

               menu.addPlayerInventoryClickHandler((p, s, i, a) -> {
                  if (a.isShiftClicked() && !a.isRightClicked()) {
                     Location fixedLocation = menu.getLocation();
                     Location attachon = HangingGridNewStyle.this.getAttachon(fixedLocation);
                     BlockFace attachSide = HangingGridNewStyle.this.getAttachSideFromFixed(fixedLocation);
                     HangingGridNewStyle.this.receiveItem(p, i, a, Screen.of(menu), attachon);
                     return false;
                  } else {
                     return true;
                  }
               });
            }
         };
         return this.preset;
      }
   }

   @ParametersAreNonnullByDefault
   @Override
   public <T extends HangingBlock> void onBreak(Location attachon, ItemFrame entityBlock, T hangingBlock) {
      HangingBlock.super.onBreak(attachon, entityBlock, hangingBlock);
      SlimefunBlockData data = StorageCacheUtils.getBlock(attachon);
      if (data != null) {
         BlockFace attachSide = entityBlock.getAttachedFace();
         data.removeData(fixedKey("filter", attachSide));
         Location location = entityBlock.getLocation();
         location.getWorld().dropItemNaturally(location, ExpansionItems.HANGING_GRID_NEW_STYLE.getItem());
      }
   }

   @ParametersAreNonnullByDefault
   @NotNull
   public BlockFace getAttachSideFromFixed(Location fixedLocation) {
      for (BlockFace side : NetworkDirectional.VALID_FACES) {
         if (side.getDirection().equals(fixedLocation.getDirection())) {
            return side;
         }
      }

      return BlockFace.SELF;
   }

   @ParametersAreNonnullByDefault
   @NotNull
   public Location getAttachon(Location fixedLocation) {
      Location loc = fixedLocation.clone();
      loc.setYaw(0.0F);
      loc.setPitch(0.0F);
      return loc;
   }

   public void receiveItem(@NotNull Player player, ClickAction action, @NotNull Screen screen, @NotNull Location attachon, BlockFace attachSide) {
      NodeDefinition definition = NetworkStorage.getNode(attachon);
      if (definition != null && definition.getNode() != null) {
         ItemStack cursor = player.getItemOnCursor();
         this.receiveItem(definition.getNode().getRoot(), cursor, screen);
      } else {
         this.clearDisplay(screen);
         screen.close();
         Networks.getInstance()
            .getLogger()
            .warning(String.format(Lang.getString("messages.unsupported-operation.grid.may_duping"), player.getName(), screen.getLocation()));
      }
   }

   public void receiveItem(@NotNull Player player, ItemStack itemStack, ClickAction action, @NotNull Screen screen, @NotNull Location attachon) {
      NodeDefinition definition = NetworkStorage.getNode(attachon);
      if (definition != null && definition.getNode() != null) {
         this.receiveItem(definition.getNode().getRoot(), itemStack, screen);
      } else {
         this.clearDisplay(screen);
         screen.close();
         Networks.getInstance()
            .getLogger()
            .warning(String.format(Lang.getString("messages.unsupported-operation.grid.may_duping"), player.getName(), attachon));
      }
   }

   public void receiveItem(@NotNull NetworkRoot root, @Nullable ItemStack itemStack, @NotNull Screen screen) {
      if (itemStack != null && itemStack.getType() != Material.AIR) {
         root.addItemStack(screen.getLocation(), itemStack);
      }
   }
}
