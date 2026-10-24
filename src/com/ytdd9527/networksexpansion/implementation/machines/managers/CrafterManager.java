package com.ytdd9527.networksexpansion.implementation.machines.managers;

import com.balugaq.netex.api.algorithm.Sorters;
import com.balugaq.netex.api.enums.CraftType;
import com.balugaq.netex.api.enums.FeedbackType;
import com.balugaq.netex.api.helpers.Icon;
import com.balugaq.netex.api.interfaces.CraftTyped;
import com.balugaq.netex.utils.Lang;
import com.balugaq.netex.utils.LocationUtil;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.core.items.machines.AdvancedAutoCrafter;
import com.ytdd9527.networksexpansion.core.items.machines.AutoCrafter;
import com.ytdd9527.networksexpansion.core.items.unusable.Blueprint;
import com.ytdd9527.networksexpansion.implementation.ExpansionItems;
import com.ytdd9527.networksexpansion.utils.ParticleUtil;
import com.ytdd9527.networksexpansion.utils.TextUtil;
import com.ytdd9527.networksexpansion.utils.itemstacks.ItemStackUtil;
import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.NodeDefinition;
import io.github.sefiraat.networks.network.NodeType;
import io.github.sefiraat.networks.network.stackcaches.BlueprintInstance;
import io.github.sefiraat.networks.slimefun.network.NetworkObject;
import io.github.sefiraat.networks.slimefun.network.grid.GridCache;
import io.github.sefiraat.networks.utils.Keys;
import io.github.sefiraat.networks.utils.StackUtils;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu.AdvancedMenuClickHandler;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import net.guizhanss.minecraft.guizhanlib.gugu.minecraft.helpers.inventory.ItemStackHelper;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public class CrafterManager extends NetworkObject {
   private static final Map<Location, GridCache> CACHE_MAP = new HashMap<>();
   private static final int[] BACKGROUND_SLOTS = new int[]{8, 17, 26, 35};
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
   private static final int PAGE_PREVIOUS = 44;
   private static final int PAGE_NEXT = 53;
   private static final String BS_TOP = "netex-top";
   private static final String BS_NAME = "netex-name";
   private static final String BS_ICON = "netex-icon";
   private static final String BS_TOP_1B = "1";
   private static final String BS_TOP_0B = "0";
   private static final String NAMESPACE_SF = "sf";
   private static final String NAMESPACE_MC = "mc";
   @NotNull
   private final IntRangeSetting tickRate = new IntRangeSetting(this, "tick_rate", 1, 1, 10);

   public CrafterManager(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe, NodeType.CRAFTER_MANAGER);
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

               CrafterManager.this.addToRegistry(block);
               CrafterManager.this.updateDisplay(blockMenu);
            }
         }

         public void uniqueTick() {
            this.tick = this.tick <= 1 ? (Integer)CrafterManager.this.tickRate.getValue() : this.tick - 1;
         }
      }});
   }

   @Nullable
   public static String getCrafterName(@NotNull Location crafterLocation) {
      return StorageCacheUtils.getData(crafterLocation, "netex-name");
   }

   public static void setCrafterIcon(@NotNull Player player, @NotNull Location crafterLocation, @NotNull ItemStack cursor) {
      StorageCacheUtils.setData(crafterLocation, "netex-icon", serializeIcon(cursor));
      player.sendMessage(Lang.getString("messages.completed-operation.manager.set_icon"));
   }

   @Nullable
   public static ItemStack getCrafterIcon(@NotNull Location crafterLocation) {
      String icon = StorageCacheUtils.getData(crafterLocation, "netex-icon");
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

   public static void topOrUntopCrafter(@NotNull Player player, @NotNull Location crafterLocation) {
      if (Objects.equals(StorageCacheUtils.getData(crafterLocation, "netex-top"), "1")) {
         StorageCacheUtils.setData(crafterLocation, "netex-top", "0");
         player.sendMessage(Lang.getString("messages.completed-operation.manager.top_storage_off"));
      } else {
         StorageCacheUtils.setData(crafterLocation, "netex-top", "1");
         player.sendMessage(Lang.getString("messages.completed-operation.manager.top_storage_on"));
      }
   }

   public static boolean isTopCrafter(@NotNull Location crafterLocation) {
      String top = StorageCacheUtils.getData(crafterLocation, "netex-top");
      return top != null && top.equals("1");
   }

   @NotNull
   public static ItemStack getItemStack(@NotNull CrafterManager.CrafterMetaData data) {
      ItemStack raw = null;
      if (data.instance() != null) {
         raw = data.instance().getItemStack();
      }

      return raw == null ? Icon.BLANK_SLOT_STACK : StackUtils.getAsQuantity(raw, Math.min(raw.getAmount() * data.blueprintAmount(), raw.getMaxStackSize()));
   }

   @ParametersAreNonnullByDefault
   public static void openMenu(Player player, Location location, Location managerLocation) {
      BlockMenu menu = StorageCacheUtils.getMenu(location);
      if (menu != null) {
         menu.addMenuCloseHandler(p -> {
            menu.addMenuCloseHandler(p2 -> {
            });
            BlockMenu managerMenu = StorageCacheUtils.getMenu(managerLocation);
            if (managerMenu != null) {
               managerMenu.open(new Player[]{p});
            }
         });
         menu.open(new Player[]{player});
      }
   }

   public void tryInsertBlueprint(@NotNull BlockMenu managerMenu, @NotNull ItemStack blueprint, @NotNull BlueprintInstance instance) {
      Location location = managerMenu.getLocation();
      GridCache gridCache = this.getCacheMap().get(location);
      NodeDefinition definition = NetworkStorage.getNode(location);
      if (definition != null && definition.getNode() != null) {
         NetworkRoot root = definition.getNode().getRoot();
         List<CrafterManager.CrafterMetaData> datas = CrafterManager.CrafterMetaData.getMetaDatas(root);
         int pages = (int)Math.ceil((double)datas.size() / (double)this.getDisplaySlots().length) - 1;
         gridCache.setMaxPages(pages);
         if (pages < 0) {
            this.clearDisplay(managerMenu);
         } else {
            if (gridCache.getPage() > pages) {
               gridCache.setPage(0);
            }

            int start = gridCache.getPage() * this.getDisplaySlots().length;
            if (start < 0) {
               start = 0;
            }

            int end = Math.min(start + this.getDisplaySlots().length, datas.size());
            datas = datas.stream().sorted((a, b) -> isTopCrafter(a.location()) ? -1 : (isTopCrafter(b.location()) ? 1 : 0)).toList();
            List<CrafterManager.CrafterMetaData> validdatas = datas.subList(start, end);
            CrafterManager.CrafterMetaData empty = null;

            for (CrafterManager.CrafterMetaData data : validdatas) {
               if (suitable(data, blueprint)) {
                  if (data.instance() != null) {
                     ItemStack output = data.instance().getItemStack();
                     if (output != null && StackUtils.itemsMatch(output, instance.getItemStack(), true, false)) {
                        SlimefunBlockData blockData = StorageCacheUtils.getBlock(data.location());
                        if (blockData != null) {
                           BlockMenu crafterMenu = blockData.getBlockMenu();
                           if (crafterMenu != null) {
                              SlimefunItem sf = SlimefunItem.getById(blockData.getSfId());
                              ItemStack existing = crafterMenu.getItemInSlot(10);
                              if (!(sf instanceof AutoCrafter) || existing != null && existing.getType() != Material.AIR) {
                                 if (!(sf instanceof AdvancedAutoCrafter)
                                    || existing != null
                                       && existing.getType() != Material.AIR
                                       && (existing.getAmount() >= output.getMaxStackSize() / output.getAmount() || !StackUtils.itemsMatch(existing, blueprint))
                                    )
                                  {
                                    continue;
                                 }

                                 this.tryInsertBlueprint(crafterMenu, blueprint, Math.min(output.getMaxStackSize() / output.getAmount(), blueprint.getAmount()));
                                 return;
                              }

                              this.tryInsertBlueprint(crafterMenu, blueprint, 1);
                              return;
                           }
                        }
                     }
                  } else if (empty == null) {
                     empty = data;
                  }
               }
            }

            if (empty != null) {
               SlimefunBlockData blockData = StorageCacheUtils.getBlock(empty.location());
               if (blockData == null) {
                  return;
               }

               BlockMenu crafterMenu = blockData.getBlockMenu();
               if (crafterMenu == null) {
                  return;
               }

               SlimefunItem sf = SlimefunItem.getById(blockData.getSfId());
               if (sf instanceof AutoCrafter) {
                  this.tryInsertBlueprint(crafterMenu, blueprint, 1);
               } else if (sf instanceof AdvancedAutoCrafter) {
                  ItemMeta blueprintMeta = blueprint.getItemMeta();
                  BlueprintInstance instance2 = Keys.getBlueprintInstance(blueprintMeta);
                  if (instance2 != null) {
                     ItemStack output = instance.getItemStack();
                     if (output != null) {
                        this.tryInsertBlueprint(crafterMenu, blueprint, Math.min(output.getMaxStackSize() / output.getAmount(), blueprint.getAmount()));
                     } else {
                        this.tryInsertBlueprint(crafterMenu, blueprint, blueprint.getAmount());
                     }
                  } else {
                     this.tryInsertBlueprint(crafterMenu, blueprint, blueprint.getAmount());
                  }
               }
            }
         }
      }
   }

   public void tryInsertBlueprint(@NotNull BlockMenu crafterMenu, @NotNull ItemStack blueprint, int maxAmount) {
      ItemStack existingBlueprint = crafterMenu.getItemInSlot(10);
      int v;
      if (existingBlueprint != null && existingBlueprint.getType() != Material.AIR) {
         v = Math.max(0, Math.min(existingBlueprint.getMaxStackSize() - existingBlueprint.getAmount(), Math.min(maxAmount, blueprint.getAmount())));
         existingBlueprint.setAmount(existingBlueprint.getAmount() + v);
      } else {
         v = Math.min(maxAmount, blueprint.getAmount());
         crafterMenu.replaceExistingItem(10, StackUtils.getAsQuantity(blueprint, v));
      }

      blueprint.setAmount(blueprint.getAmount() - v);
   }

   public void updateDisplay(@Nullable final BlockMenu managerMenu) {
      if (managerMenu != null) {
         if (!managerMenu.hasViewer()) {
            this.sendFeedback(managerMenu.getLocation(), FeedbackType.AFK);
         } else {
            Location location = managerMenu.getLocation();
            NodeDefinition definition = NetworkStorage.getNode(location);
            if (definition != null && definition.getNode() != null) {
               GridCache gridCache = this.getCacheMap().get(location);
               final NetworkRoot root = definition.getNode().getRoot();
               List<CrafterManager.CrafterMetaData> datas = CrafterManager.CrafterMetaData.getMetaDatas(root);
               int pages = (int)Math.ceil((double)datas.size() / (double)this.getDisplaySlots().length) - 1;
               gridCache.setMaxPages(pages);
               if (pages < 0) {
                  this.clearDisplay(managerMenu);
               } else {
                  if (gridCache.getPage() > pages) {
                     gridCache.setPage(0);
                  }

                  int start = gridCache.getPage() * this.getDisplaySlots().length;
                  if (start < 0) {
                     start = 0;
                  }

                  int end = Math.min(start + this.getDisplaySlots().length, datas.size());
                  datas = datas.stream().sorted((a, b) -> isTopCrafter(a.location()) ? -1 : (isTopCrafter(b.location()) ? 1 : 0)).toList();
                  List<CrafterManager.CrafterMetaData> validdatas = datas.subList(start, end);
                  this.getCacheMap().put(managerMenu.getLocation(), gridCache);

                  for (int i = 0; i < this.getDisplaySlots().length; i++) {
                     if (validdatas.size() > i) {
                        CrafterManager.CrafterMetaData data = validdatas.get(i);
                        ItemStack rawDisplayStack = getItemStack(data);
                        boolean isEmpty = false;
                        ItemStack displayStack = null;
                        if (rawDisplayStack == null || rawDisplayStack.getType() == Material.AIR) {
                           displayStack = new ItemStack(Material.BARRIER);
                           isEmpty = true;
                        }

                        final Location crafterLocation = data.location();
                        BlockMenu menu = StorageCacheUtils.getMenu(crafterLocation);
                        if (menu != null) {
                           ItemStack blueprint = menu.getItemInSlot(10);
                           if (blueprint != null && blueprint.getType() != Material.AIR) {
                              SlimefunItem itemMeta = SlimefunItem.getByItem(blueprint);
                              if (itemMeta instanceof CraftTyped) {
                                 CraftTyped craftTyped = (CraftTyped)itemMeta;
                                 if (!suitable(data, blueprint)) {
                                    displayStack = generateUnsuitableStack(data.craftType(), craftTyped.craftType());
                                    List<String> lore = this.getLoreAddition(data);
                                    ItemMeta itemMetax = displayStack.getItemMeta();
                                    itemMetax.setLore(lore);
                                    displayStack.setItemMeta(itemMetax);
                                    managerMenu.replaceExistingItem(this.getDisplaySlots()[i], displayStack);
                                    continue;
                                 }
                              }
                           }
                        }

                        ItemStack custom = getCrafterIcon(crafterLocation);
                        if (custom != null) {
                           displayStack = custom;
                        } else if (displayStack == null) {
                           displayStack = rawDisplayStack.clone();
                        }

                        String name = getCrafterName(crafterLocation);
                        CustomItemStack var23;
                        if (name != null) {
                           var23 = new CustomItemStack(displayStack, TextUtil.color(name), new String[0]);
                        } else if (!isEmpty) {
                           var23 = new CustomItemStack(displayStack, TextUtil.GRAY + ItemStackHelper.getDisplayName(rawDisplayStack), new String[0]);
                        } else {
                           var23 = new CustomItemStack(displayStack, Sorters.NO_ITEM, new String[0]);
                        }

                        ItemMeta itemMeta = var23.getItemMeta();
                        if (itemMeta != null) {
                           List<String> lore = this.getLoreAddition(data);
                           itemMeta.setLore(lore);
                           var23.setItemMeta(itemMeta);
                           managerMenu.replaceExistingItem(this.getDisplaySlots()[i], var23);
                           managerMenu.addMenuClickHandler(
                              this.getDisplaySlots()[i],
                              new AdvancedMenuClickHandler() {
                                 public boolean onClick(InventoryClickEvent event, Player player, int slot, ItemStack item, ClickAction action) {
                                    CrafterManager.this.handleClick(
                                       root,
                                       managerMenu,
                                       crafterLocation,
                                       player,
                                       slot,
                                       item,
                                       action,
                                       event.getClick() == ClickType.DROP || event.getClick() == ClickType.CONTROL_DROP
                                    );
                                    return false;
                                 }

                                 public boolean onClick(Player player, int slot, ItemStack item, ClickAction action) {
                                    CrafterManager.this.handleClick(root, managerMenu, crafterLocation, player, slot, item, action, false);
                                    return false;
                                 }
                              }
                           );
                        }
                     } else {
                        managerMenu.replaceExistingItem(this.getDisplaySlots()[i], this.getBlankSlotStack());
                        managerMenu.addMenuClickHandler(this.getDisplaySlots()[i], (p, slot, item, action) -> false);
                     }
                  }

                  managerMenu.replaceExistingItem(
                     this.getPagePrevious(), Icon.getPageStack(this.getPagePreviousStack(), gridCache.getPage() + 1, gridCache.getMaxPages() + 1)
                  );
                  managerMenu.replaceExistingItem(
                     this.getPageNext(), Icon.getPageStack(this.getPageNextStack(), gridCache.getPage() + 1, gridCache.getMaxPages() + 1)
                  );
                  this.sendFeedback(managerMenu.getLocation(), FeedbackType.WORKING);
               }
            } else {
               this.sendFeedback(managerMenu.getLocation(), FeedbackType.NO_NETWORK_FOUND);
            }
         }
      }
   }

   @NotNull
   public List<String> getLoreAddition(@NotNull CrafterManager.CrafterMetaData data) {
      Location loc = data.location();
      List<String> list = new ArrayList<>();
      list.add("");
      list.add(String.format(Lang.getString("messages.normal-operation.manager.location"), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
      list.add(
         String.format(
            Lang.getString("messages.normal-operation.manager.advance"),
            data.advance() ? Lang.getString("messages.normal-operation.manager.yes") : Lang.getString("messages.normal-operation.manager.no")
         )
      );
      list.add(String.format(Lang.getString("messages.normal-operation.manager.crafter_type"), data.craftType().translate()));
      list.add("");
      list.addAll(Lang.getStringList("messages.normal-operation.manager.crafter-manager-click-behavior"));
      return list;
   }

   public void postRegister() {
      this.getPreset();
   }

   @NotNull
   protected BlockMenuPreset getPreset() {
      return new BlockMenuPreset(this.getId(), this.getItemName()) {
         public void init() {
            this.drawBackground(CrafterManager.this.getBackgroundSlots());
            this.drawBackground(CrafterManager.this.getDisplaySlots());
            this.setSize(54);
         }

         public boolean canOpen(@NotNull Block block, @NotNull Player player) {
            return player.hasPermission("slimefun.inventory.bypass")
               || ExpansionItems.CRAFTER_MANAGER.canUse(player, false)
                  && Slimefun.getProtectionManager().hasPermission(player, block.getLocation(), Interaction.INTERACT_BLOCK);
         }

         public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
            return new int[0];
         }

         public void newInstance(@NotNull BlockMenu menu, @NotNull Block b) {
            CrafterManager.this.getCacheMap().put(menu.getLocation(), new GridCache(0, 0, GridCache.SortOrder.ALPHABETICAL));
            menu.replaceExistingItem(CrafterManager.this.getPagePrevious(), CrafterManager.this.getPagePreviousStack());
            menu.addMenuClickHandler(CrafterManager.this.getPagePrevious(), (p, slot, item, action) -> {
               GridCache gridCache = CrafterManager.this.getCacheMap().get(menu.getLocation());
               gridCache.setPage(gridCache.getPage() <= 0 ? 0 : gridCache.getPage() - 1);
               CrafterManager.this.getCacheMap().put(menu.getLocation(), gridCache);
               CrafterManager.this.updateDisplay(menu);
               return false;
            });
            menu.replaceExistingItem(CrafterManager.this.getPageNext(), CrafterManager.this.getPageNextStack());
            menu.addMenuClickHandler(CrafterManager.this.getPageNext(), (p, slot, item, action) -> {
               GridCache gridCache = CrafterManager.this.getCacheMap().get(menu.getLocation());
               gridCache.setPage(gridCache.getPage() >= gridCache.getMaxPages() ? gridCache.getMaxPages() : gridCache.getPage() + 1);
               CrafterManager.this.getCacheMap().put(menu.getLocation(), gridCache);
               CrafterManager.this.updateDisplay(menu);
               return false;
            });

            for (int displaySlot : CrafterManager.this.getDisplaySlots()) {
               menu.replaceExistingItem(displaySlot, ChestMenuUtils.getBackground());
               menu.addMenuClickHandler(displaySlot, (p, slot, item, action) -> false);
            }

            menu.addPlayerInventoryClickHandler((p, s, blueprint, a) -> {
               if (!a.isShiftClicked()) {
                  return true;
               } else if (blueprint != null && blueprint.getType() != Material.AIR) {
                  ItemMeta blueprintMeta = blueprint.getItemMeta();
                  BlueprintInstance instance = Keys.getBlueprintInstance(blueprintMeta);
                  if (instance == null) {
                     return true;
                  } else {
                     CrafterManager.this.tryInsertBlueprint(menu, blueprint, instance);
                     return false;
                  }
               } else {
                  return true;
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

   @NotNull
   protected ItemStack getBlankSlotStack() {
      return Icon.DARK_BLANK_SLOT_STACK;
   }

   @NotNull
   protected ItemStack getPagePreviousStack() {
      return Icon.PAGE_PREVIOUS_STACK;
   }

   @NotNull
   protected ItemStack getPageNextStack() {
      return Icon.PAGE_NEXT_STACK;
   }

   protected void clearDisplay(@NotNull BlockMenu blockMenu) {
      for (int displaySlot : this.getDisplaySlots()) {
         blockMenu.replaceExistingItem(displaySlot, this.getBlankSlotStack());
         blockMenu.addMenuClickHandler(displaySlot, (p, slot, item, action) -> false);
      }
   }

   public void swapItem(@NotNull BlockMenu crafterMenu, @NotNull Player player) {
      ItemStack cursor = player.getItemOnCursor();
      ItemStack existing = crafterMenu.getItemInSlot(10);
      existing = existing == null ? null : existing.clone();
      crafterMenu.replaceExistingItem(10, cursor);
      player.setItemOnCursor(existing);
      AutoCrafter.updateCache(crafterMenu);
   }

   public void setCrafterName(@NotNull BlockMenu managerMenu, @NotNull Player player, @NotNull Location crafterLocation) {
      player.sendMessage(Lang.getString("messages.normal-operation.manager.set_name"));
      player.closeInventory();
      ChatUtils.awaitInput(player, s -> {
         StorageCacheUtils.setData(crafterLocation, "netex-name", s);
         player.sendMessage(Lang.getString("messages.completed-operation.manager.set_name"));
         SlimefunBlockData data = StorageCacheUtils.getBlock(managerMenu.getLocation());
         if (data != null) {
            if (managerMenu.getPreset().getID().equals(data.getSfId())) {
               BlockMenu actualMenu = data.getBlockMenu();
               if (actualMenu != null) {
                  this.updateDisplay(actualMenu);
                  actualMenu.open(new Player[]{player});
               }
            }
         }
      });
   }

   @ParametersAreNonnullByDefault
   public void handleClick(
      NetworkRoot root,
      BlockMenu managerMenu,
      Location crafterLocation,
      Player player,
      int clickedSlot,
      ItemStack clickedItemStack,
      ClickAction clickAction,
      boolean dropAction
   ) {
      BlockMenu crafterMenu = StorageCacheUtils.getMenu(crafterLocation);
      if (crafterMenu != null) {
         CrafterManager.CrafterMetaData data = CrafterManager.CrafterMetaData.getMetaData(root, crafterMenu);
         ItemStack blueprint = crafterMenu.getItemInSlot(10);
         ItemStack cursor = player.getItemOnCursor();
         if (dropAction) {
            openMenu(player, crafterLocation, managerMenu.getLocation());
         } else {
            if (!clickAction.isRightClicked()) {
               if (clickAction.isShiftClicked()) {
                  topOrUntopCrafter(player, crafterLocation);
               } else if (blueprint == null) {
                  if (SlimefunItem.getByItem(cursor) instanceof Blueprint) {
                     this.swapItem(crafterMenu, player);
                  }
               } else {
                  this.swapItem(crafterMenu, player);
               }
            } else if (clickAction.isShiftClicked()) {
               if (cursor != null && cursor.getType() != Material.AIR) {
                  setCrafterIcon(player, data.location(), cursor);
               } else {
                  this.setCrafterName(managerMenu, player, data.location());
               }
            } else {
               ParticleUtil.highlightBlock(player, data.location(), 10);
            }
         }
      }
   }

   @Contract(
      pure = true
   )
   public static boolean suitable(CrafterManager.CrafterMetaData crafter, ItemStack blueprint) {
      return SlimefunItem.getByItem(blueprint) instanceof CraftTyped craftTyped ? craftTyped.craftType() == crafter.craftType() : false;
   }

   public static ItemStack generateUnsuitableStack(CraftType expected, CraftType but) {
      return ItemStackUtil.getCleanItem(
         new CustomItemStack(
            Material.BARRIER,
            Lang.getString("messages.unsupported-operation.crafter_manager.different_type_1"),
            new String[]{
               String.format(Lang.getString("messages.unsupported-operation.crafter_manager.different_type_2"), expected.translate(), but.translate())
            }
         )
      );
   }

   public static record CrafterMetaData(
      @NotNull Location location, @Nullable BlueprintInstance instance, @Range(from = 0L,to = 64L) int blueprintAmount, boolean advance, CraftType craftType
   ) {
      @NotNull
      public static List<CrafterManager.CrafterMetaData> getMetaDatas(@NotNull NetworkRoot root) {
         List<CrafterManager.CrafterMetaData> metaDataList = new ArrayList<>();

         for (BlockMenu blockMenu : root.getCrafterOutputs()) {
            metaDataList.add(getMetaData(root, blockMenu));
         }

         return metaDataList.stream().sorted(Comparator.comparingLong(d -> (long)LocationUtil.toStableHash(d.location()))).toList();
      }

      @ParametersAreNonnullByDefault
      @NotNull
      public static CrafterManager.CrafterMetaData getMetaData(NetworkRoot root, BlockMenu crafterMenu) {
         Location location = crafterMenu.getLocation();
         CraftType craftType = CraftType.CRAFTING;
         if (StorageCacheUtils.getSfItem(location) instanceof CraftTyped craftTyped) {
            craftType = craftTyped.craftType();
         }

         ItemStack blueprint = crafterMenu.getItemInSlot(10);
         if (blueprint != null && blueprint.getType() != Material.AIR) {
            BlueprintInstance instance = AutoCrafter.INSTANCE_MAP.get(crafterMenu.getLocation());
            if (instance == null) {
               ItemMeta blueprintMeta = blueprint.getItemMeta();
               instance = Keys.getBlueprintInstance(blueprintMeta);
            }

            if (instance != null) {
               SlimefunItem sf = StorageCacheUtils.getSfItem(location);
               return sf instanceof AdvancedAutoCrafter
                  ? new CrafterManager.CrafterMetaData(location, instance, blueprint.getAmount(), true, craftType)
                  : new CrafterManager.CrafterMetaData(location, instance, 1, false, craftType);
            } else {
               return new CrafterManager.CrafterMetaData(location, null, 0, false, craftType);
            }
         } else {
            return new CrafterManager.CrafterMetaData(location, null, 0, false, craftType);
         }
      }
   }
}
