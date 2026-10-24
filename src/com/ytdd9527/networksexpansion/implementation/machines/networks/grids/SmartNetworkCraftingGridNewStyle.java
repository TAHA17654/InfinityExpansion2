package com.ytdd9527.networksexpansion.implementation.machines.networks.grids;

import com.balugaq.jeg.api.recipe_complete.RecipeCompletableRegistry;
import com.balugaq.netex.api.enums.AmountHandleStrategy;
import com.balugaq.netex.api.enums.FeedbackType;
import com.balugaq.netex.api.helpers.Icon;
import com.balugaq.netex.api.helpers.SupportedCraftingTableRecipes;
import com.balugaq.netex.api.keybind.Action;
import com.balugaq.netex.api.keybind.ActionResult;
import com.balugaq.netex.api.keybind.Keybind;
import com.balugaq.netex.api.keybind.Keybinds;
import com.balugaq.netex.api.keybind.MultiActionHandle;
import com.balugaq.netex.api.visual.Screen;
import com.balugaq.netex.utils.BlockMenuUtil;
import com.balugaq.netex.utils.Debug;
import com.balugaq.netex.utils.InventoryUtil;
import com.balugaq.netex.utils.Lang;
import com.ytdd9527.networksexpansion.core.items.machines.AbstractGridNewStyle;
import com.ytdd9527.networksexpansion.implementation.ExpansionItems;
import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.events.NetworkCraftEvent;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.NodeDefinition;
import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import io.github.sefiraat.networks.slimefun.network.grid.AbstractGrid;
import io.github.sefiraat.networks.slimefun.network.grid.GridCache;
import io.github.sefiraat.networks.utils.Keys;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class SmartNetworkCraftingGridNewStyle extends AbstractGridNewStyle {
   private static final int[] BACKGROUND_SLOTS = new int[]{12, 29};
   private static final int[] DISPLAY_SLOTS = new int[]{4, 5, 6, 7, 13, 14, 15, 16, 22, 23, 24, 25, 31, 32, 33, 34};
   private static final int RETURN_INGREDIENT_SLOT = 3;
   private static final int RETURN_OUTPUT_SLOT = 21;
   private static final int KEYBIND_BUTTON_SLOT = 28;
   private static final int CHANGE_SORT = 17;
   private static final int FILTER = 8;
   private static final int PAGE_PREVIOUS = 26;
   private static final int PAGE_NEXT = 35;
   private static final int TOGGLE_MODE_SLOT = 30;
   private static final int CRAFT_BUTTON_SLOT = 27;
   private static final int[] OUTPUT_SLOTS = new int[]{36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53};
   private static final int[] TEMPLATE_SLOTS = new int[]{0, 1, 2, 9, 10, 11, 18, 19, 20};
   private static final Map<Location, GridCache> CACHE_MAP = new HashMap<>();
   public static int THRESHOLD = 4096;
   private final Keybinds smartOutsideKeybinds = Keybinds.create(
         Keys.newKey("smart-outside-keybinds"),
         it -> {
            it.usableKeybinds(Keybind.leftClick, Keybind.rightClick, Keybind.shiftLeftClick, Keybind.shiftRightClick, Keybind.shiftClick);
            Action storeItem = Action.of(Keys.newKey("store-item"), (p, s, stack, a, menu) -> {
               NodeDefinition definition = NetworkStorage.getNode(menu.getLocation());
               if (definition != null && definition.getNode() != null) {
                  NetworkRoot root = definition.getNode().getRoot();
                  if (stack != null && stack.getType() != Material.AIR && stack.getAmount() > 0) {
                     root.addItemStack(menu.getLocation(), stack);
                     if (stack.getAmount() != 0) {
                        BlockMenuUtil.pushItem(menu, stack, OUTPUT_SLOTS);
                        if (stack.getAmount() != 0) {
                           BlockMenuUtil.pushItem(menu, stack, this.getIngredientSlots());
                        }
                     }
                  }

                  return ActionResult.of(MultiActionHandle.BREAK, false);
               } else {
                  return ActionResult.of(MultiActionHandle.CONTINUE, false);
               }
            });
            it.usableActions(
               storeItem,
               Keybind.gridActionGenerate(this, AmountHandleStrategy.ONE, true),
               Keybind.gridActionGenerate(this, AmountHandleStrategy.STACK, true),
               Keybind.gridActionGenerate(this, AmountHandleStrategy.CUSTOM, true),
               Keybind.gridActionGenerate(this, AmountHandleStrategy.ASK, true)
            );
            it.defaultKeybinds(Keybind.shiftLeftClick, storeItem);
            it.defaultActionResult(ActionResult.of(MultiActionHandle.CONTINUE, true));
         }
      )
      .generate();

   public SmartNetworkCraftingGridNewStyle(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe);

      for (int i : TEMPLATE_SLOTS) {
         this.getSlotsToDrop().add(i);
      }

      for (int i : OUTPUT_SLOTS) {
         this.getSlotsToDrop().add(i);
      }

      if (Networks.getSupportedPluginManager().isJustEnoughGuide()) {
         try {
            RecipeCompletableRegistry.registerRecipeCompletable(this, this.getIngredientSlots(), false);
         } catch (Exception var9) {
            Debug.trace(var9);
         }
      }
   }

   @Override
   protected BlockMenuPreset getPreset() {
      return new BlockMenuPreset(this.getId(), this.getItemName()) {
         public void init() {
            this.drawBackground(SmartNetworkCraftingGridNewStyle.this.getBackgroundSlots());
            this.drawBackground(SmartNetworkCraftingGridNewStyle.this.getDisplaySlots());
            this.setSize(54);
         }

         public boolean canOpen(Block block, Player player) {
            return player.hasPermission("slimefun.inventory.bypass")
               || ExpansionItems.NETWORK_CRAFTING_GRID_NEW_STYLE.canUse(player, false)
                  && Slimefun.getProtectionManager().hasPermission(player, block.getLocation(), Interaction.INTERACT_BLOCK);
         }

         public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
            return new int[0];
         }

         public void newInstance(BlockMenu menu, Block b) {
            SmartNetworkCraftingGridNewStyle.this.getCacheMap().put(menu.getLocation(), new GridCache(0, 0, GridCache.SortOrder.ALPHABETICAL));
            menu.replaceExistingItem(SmartNetworkCraftingGridNewStyle.this.getPagePrevious(), SmartNetworkCraftingGridNewStyle.this.getPagePreviousStack());
            menu.addMenuClickHandler(SmartNetworkCraftingGridNewStyle.this.getPagePrevious(), (p, slot, item, action) -> {
               GridCache gridCache = SmartNetworkCraftingGridNewStyle.this.getCacheMap().get(menu.getLocation());
               gridCache.setPage(gridCache.getPage() <= 0 ? 0 : gridCache.getPage() - 1);
               SmartNetworkCraftingGridNewStyle.this.getCacheMap().put(menu.getLocation(), gridCache);
               SmartNetworkCraftingGridNewStyle.this.updateDisplay(Screen.of(menu));
               return false;
            });
            menu.replaceExistingItem(SmartNetworkCraftingGridNewStyle.this.getPageNext(), SmartNetworkCraftingGridNewStyle.this.getPageNextStack());
            menu.addMenuClickHandler(SmartNetworkCraftingGridNewStyle.this.getPageNext(), (p, slot, item, action) -> {
               GridCache gridCache = SmartNetworkCraftingGridNewStyle.this.getCacheMap().get(menu.getLocation());
               gridCache.setPage(gridCache.getPage() >= gridCache.getMaxPages() ? gridCache.getMaxPages() : gridCache.getPage() + 1);
               SmartNetworkCraftingGridNewStyle.this.getCacheMap().put(menu.getLocation(), gridCache);
               SmartNetworkCraftingGridNewStyle.this.updateDisplay(Screen.of(menu));
               return false;
            });
            menu.replaceExistingItem(SmartNetworkCraftingGridNewStyle.this.getChangeSort(), SmartNetworkCraftingGridNewStyle.this.getChangeSortStack());
            menu.addMenuClickHandler(SmartNetworkCraftingGridNewStyle.this.getChangeSort(), (p, slot, item, action) -> {
               GridCache gridCache = SmartNetworkCraftingGridNewStyle.this.getCacheMap().get(menu.getLocation());
               AbstractGrid.updateSortOrder(gridCache, action, 4);
               SmartNetworkCraftingGridNewStyle.this.getCacheMap().put(menu.getLocation(), gridCache);
               SmartNetworkCraftingGridNewStyle.this.updateDisplay(Screen.of(menu));
               return false;
            });
            menu.replaceExistingItem(SmartNetworkCraftingGridNewStyle.this.getFilterSlot(), SmartNetworkCraftingGridNewStyle.this.getFilterStack());
            menu.addMenuClickHandler(SmartNetworkCraftingGridNewStyle.this.getFilterSlot(), (p, slot, item, action) -> {
               GridCache gridCache = SmartNetworkCraftingGridNewStyle.this.getCacheMap().get(menu.getLocation());
               SmartNetworkCraftingGridNewStyle.this.setFilter(p, Screen.of(menu), gridCache, action);
               return false;
            });
            menu.replaceExistingItem(
               SmartNetworkCraftingGridNewStyle.this.getToggleModeSlot(), SmartNetworkCraftingGridNewStyle.this.getModeStack(GridCache.DisplayMode.DISPLAY)
            );
            menu.addMenuClickHandler(
               SmartNetworkCraftingGridNewStyle.this.getToggleModeSlot(),
               (p, slot, item, action) -> {
                  if (!action.isRightClicked()) {
                     GridCache gridCache = SmartNetworkCraftingGridNewStyle.this.getCacheMap().get(menu.getLocation());
                     gridCache.toggleDisplayMode();
                     menu.replaceExistingItem(
                        SmartNetworkCraftingGridNewStyle.this.getToggleModeSlot(), SmartNetworkCraftingGridNewStyle.this.getModeStack(gridCache)
                     );
                     SmartNetworkCraftingGridNewStyle.this.updateDisplay(Screen.of(menu));
                  }

                  return false;
               }
            );
            menu.replaceExistingItem(27, Icon.CRAFT);
            menu.addMenuClickHandler(27, (p, slot, item, action) -> {
               SmartNetworkCraftingGridNewStyle.this.tryCraft(menu, p, action);
               return false;
            });

            for (int displaySlot : SmartNetworkCraftingGridNewStyle.this.getDisplaySlots()) {
               menu.replaceExistingItem(displaySlot, ChestMenuUtils.getBackground());
               menu.addMenuClickHandler(displaySlot, (p, slot, item, action) -> false);
            }

            for (int backgroundSlot : SmartNetworkCraftingGridNewStyle.this.getBackgroundSlots()) {
               menu.replaceExistingItem(backgroundSlot, ChestMenuUtils.getBackground());
               menu.addMenuClickHandler(backgroundSlot, (p, slot, item, action) -> false);
            }

            menu.addItem(3, Icon.RETURN_INGREDIENT);
            menu.addMenuClickHandler(3, (p, s, i, a) -> {
               NodeDefinition definition = NetworkStorage.getNode(menu.getLocation());
               if (definition != null && definition.getNode() != null) {
                  NetworkRoot root = definition.getNode().getRoot();

                  for (int slot : SmartNetworkCraftingGridNewStyle.this.getIngredientSlots()) {
                     ItemStack stack = menu.getItemInSlot(slot);
                     if (stack != null && stack.getType() != Material.AIR && stack.getAmount() > 0) {
                        root.addItemStack(menu.getLocation(), stack);
                        if (stack.getAmount() != 0) {
                           BlockMenuUtil.pushItem(menu, stack, SmartNetworkCraftingGridNewStyle.OUTPUT_SLOTS);
                        }
                     }
                  }

                  return false;
               } else {
                  return false;
               }
            });
            menu.addItem(21, Icon.RETURN_OUTPUT);
            menu.addMenuClickHandler(21, (p, s, i, a) -> {
               NodeDefinition definition = NetworkStorage.getNode(menu.getLocation());
               if (definition != null && definition.getNode() != null) {
                  NetworkRoot root = definition.getNode().getRoot();

                  for (int slot : SmartNetworkCraftingGridNewStyle.OUTPUT_SLOTS) {
                     ItemStack stack = menu.getItemInSlot(slot);
                     if (stack != null && stack.getType() != Material.AIR && stack.getAmount() > 0) {
                        root.addItemStack(menu.getLocation(), stack);
                     }
                  }

                  return false;
               } else {
                  return false;
               }
            });
            menu.addPlayerInventoryClickHandler(SmartNetworkCraftingGridNewStyle.this.smartOutsideKeybinds);
            SmartNetworkCraftingGridNewStyle.this.addKeybindSettingsButton(menu, SmartNetworkCraftingGridNewStyle.this.getKeybindButtonSlot());
         }
      };
   }

   @Override
   public Map<Location, GridCache> getCacheMap() {
      return CACHE_MAP;
   }

   @Override
   public int[] getBackgroundSlots() {
      return BACKGROUND_SLOTS;
   }

   @Override
   public int[] getDisplaySlots() {
      return DISPLAY_SLOTS;
   }

   @Override
   public int getChangeSort() {
      return 17;
   }

   @Override
   public int getPagePrevious() {
      return 26;
   }

   @Override
   public int getPageNext() {
      return 35;
   }

   @Override
   public int getKeybindButtonSlot() {
      return 28;
   }

   @Override
   public int getToggleModeSlot() {
      return 30;
   }

   @Override
   protected int getFilterSlot() {
      return 8;
   }

   private synchronized void tryCraft(BlockMenu menu, Player player, ClickAction action) {
      if (player.getWorld().getNearbyEntities(player.getLocation(), 5.0, 5.0, 5.0).size() < THRESHOLD) {
         this.sendFeedback(menu.getLocation(), FeedbackType.TOO_MANY_ENTITIES);
      } else {
         int times = 1;
         if (action.isRightClicked()) {
            times = 64;
         }

         NodeDefinition definition = NetworkStorage.getNode(menu.getLocation());
         if (definition != null && definition.getNode() != null) {
            NetworkRoot root = definition.getNode().getRoot();
            ItemStack[] templates = new ItemStack[TEMPLATE_SLOTS.length];
            int i = 0;

            for (int templateSlot : TEMPLATE_SLOTS) {
               ItemStack stack = menu.getItemInSlot(templateSlot);
               templates[i++] = stack == null ? null : StackUtils.getAsQuantity(stack, 1);
            }

            ItemStack crafted = null;

            for (Entry<ItemStack[], ItemStack> entry : SupportedCraftingTableRecipes.getRecipes().entrySet()) {
               if (SupportedCraftingTableRecipes.testRecipe(templates, entry.getKey())) {
                  crafted = entry.getValue().clone();
                  break;
               }
            }

            if (crafted != null) {
               SlimefunItem sfi2 = SlimefunItem.getByItem(crafted);
               if (sfi2 != null && sfi2.isDisabled()) {
                  player.sendMessage(Lang.getString("messages.unsupported-operation.encoder.disabled_output"));
                  return;
               }
            }

            if (crafted == null) {
               crafted = Bukkit.craftItem(this.copyStacks(templates), player.getWorld(), player);
            }

            if (crafted.getType() != Material.AIR) {
               for (int k = 0; k < times; k++) {
                  for (ItemStack template : templates) {
                     if (template != null && !root.contains(template)) {
                        return;
                     }
                  }

                  List<ItemStack> got = new ArrayList<>();

                  for (ItemStack templatex : templates) {
                     if (templatex != null) {
                        ItemStack item = root.getItemStack(menu.getLocation(), new ItemRequest(templatex, 1));
                        if (item == null || item.getType() == Material.AIR) {
                           for (ItemStack i2 : got) {
                              root.addItemStack(menu.getLocation(), i2);
                           }

                           return;
                        }

                        got.add(item);
                     }
                  }

                  NetworkCraftEvent event = new NetworkCraftEvent(player, this, templates, crafted.clone());
                  Bukkit.getPluginManager().callEvent(event);
                  if (event.isCancelled()) {
                     return;
                  }

                  ItemStack c2 = event.getOutput();
                  if (c2 != null && c2 != null && c2.getType() != Material.AIR && c2.getAmount() > 0) {
                     BlockMenuUtil.pushItem(menu, c2, OUTPUT_SLOTS);
                     if (c2.getAmount() != 0) {
                        root.addItemStack(menu.getLocation(), c2);
                        if (c2.getAmount() != 0) {
                           BlockMenuUtil.pushItem(menu, c2, this.getIngredientSlots());
                           if (c2.getAmount() != 0) {
                              InventoryUtil.addItem(player, c2);
                              if (c2.getAmount() != 0) {
                                 player.getWorld().dropItem(player.getLocation(), c2);
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public int[] getIngredientSlots() {
      return TEMPLATE_SLOTS;
   }

   @Override
   public List<Keybinds> keybinds() {
      return List.of(this.displayKeybinds(), this.smartOutsideKeybinds);
   }

   public ItemStack[] copyStacks(ItemStack[] array) {
      return Arrays.stream(array).map(i -> i == null ? null : i.clone()).toArray(ItemStack[]::new);
   }
}
