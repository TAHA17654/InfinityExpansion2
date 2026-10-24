package com.ytdd9527.networksexpansion.implementation.machines.networks.grids;

import com.balugaq.netex.api.helpers.Icon;
import com.balugaq.netex.api.helpers.SupportedCraftingTableRecipes;
import com.balugaq.netex.api.keybind.Keybinds;
import com.balugaq.netex.api.visual.Screen;
import com.balugaq.netex.utils.BlockMenuUtil;
import com.balugaq.netex.utils.Lang;
import com.ytdd9527.networksexpansion.core.items.machines.AbstractGridNewStyle;
import com.ytdd9527.networksexpansion.implementation.ExpansionItems;
import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.events.NetworkCraftEvent;
import io.github.sefiraat.networks.network.GridItemRequest;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.NodeDefinition;
import io.github.sefiraat.networks.slimefun.network.grid.AbstractGrid;
import io.github.sefiraat.networks.slimefun.network.grid.GridCache;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
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
public class NetworkCraftingGridNewStyle extends AbstractGridNewStyle {
   private static final int[] BACKGROUND_SLOTS = new int[]{5, 14, 23, 32, 41, 43, 50, 51};
   private static final int[] DISPLAY_SLOTS = new int[]{
      0, 1, 2, 3, 4, 9, 10, 11, 12, 13, 18, 19, 20, 21, 22, 27, 28, 29, 30, 31, 36, 37, 38, 39, 40, 45, 46, 47, 48, 49
   };
   private static final int KEYBIND_BUTTON_SLOT = 43;
   private static final int CHANGE_SORT = 35;
   private static final int FILTER = 42;
   private static final int PAGE_PREVIOUS = 44;
   private static final int PAGE_NEXT = 53;
   private static final int TOGGLE_MODE_SLOT = 52;
   private static final int CRAFT_BUTTON_SLOT = 33;
   private static final int OUTPUT_SLOT = 34;
   private static final int[] INGREDIENT_SLOTS = new int[]{6, 7, 8, 15, 16, 17, 24, 25, 26};
   private static final Map<Location, GridCache> CACHE_MAP = new HashMap<>();

   public NetworkCraftingGridNewStyle(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe);
   }

   @Override
   protected BlockMenuPreset getPreset() {
      return new BlockMenuPreset(this.getId(), this.getItemName()) {
         public void init() {
            this.drawBackground(NetworkCraftingGridNewStyle.this.getDisplaySlots());
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
            NetworkCraftingGridNewStyle.this.getCacheMap().put(menu.getLocation(), new GridCache(0, 0, GridCache.SortOrder.ALPHABETICAL));
            menu.replaceExistingItem(NetworkCraftingGridNewStyle.this.getPagePrevious(), NetworkCraftingGridNewStyle.this.getPagePreviousStack());
            menu.addMenuClickHandler(NetworkCraftingGridNewStyle.this.getPagePrevious(), (p, slot, item, action) -> {
               GridCache gridCache = NetworkCraftingGridNewStyle.this.getCacheMap().get(menu.getLocation());
               gridCache.setPage(gridCache.getPage() <= 0 ? 0 : gridCache.getPage() - 1);
               NetworkCraftingGridNewStyle.this.getCacheMap().put(menu.getLocation(), gridCache);
               NetworkCraftingGridNewStyle.this.updateDisplay(menu);
               return false;
            });
            menu.replaceExistingItem(NetworkCraftingGridNewStyle.this.getPageNext(), NetworkCraftingGridNewStyle.this.getPageNextStack());
            menu.addMenuClickHandler(NetworkCraftingGridNewStyle.this.getPageNext(), (p, slot, item, action) -> {
               GridCache gridCache = NetworkCraftingGridNewStyle.this.getCacheMap().get(menu.getLocation());
               gridCache.setPage(gridCache.getPage() >= gridCache.getMaxPages() ? gridCache.getMaxPages() : gridCache.getPage() + 1);
               NetworkCraftingGridNewStyle.this.getCacheMap().put(menu.getLocation(), gridCache);
               NetworkCraftingGridNewStyle.this.updateDisplay(Screen.of(menu));
               return false;
            });
            menu.replaceExistingItem(NetworkCraftingGridNewStyle.this.getChangeSort(), NetworkCraftingGridNewStyle.this.getChangeSortStack());
            menu.addMenuClickHandler(NetworkCraftingGridNewStyle.this.getChangeSort(), (p, slot, item, action) -> {
               GridCache gridCache = NetworkCraftingGridNewStyle.this.getCacheMap().get(menu.getLocation());
               AbstractGrid.updateSortOrder(gridCache, action, 4);
               NetworkCraftingGridNewStyle.this.getCacheMap().put(menu.getLocation(), gridCache);
               NetworkCraftingGridNewStyle.this.updateDisplay(Screen.of(menu));
               return false;
            });
            menu.replaceExistingItem(NetworkCraftingGridNewStyle.this.getFilterSlot(), NetworkCraftingGridNewStyle.this.getFilterStack());
            menu.addMenuClickHandler(NetworkCraftingGridNewStyle.this.getFilterSlot(), (p, slot, item, action) -> {
               GridCache gridCache = NetworkCraftingGridNewStyle.this.getCacheMap().get(menu.getLocation());
               NetworkCraftingGridNewStyle.this.setFilter(p, menu, gridCache, action);
               return false;
            });
            menu.replaceExistingItem(
               NetworkCraftingGridNewStyle.this.getToggleModeSlot(), NetworkCraftingGridNewStyle.this.getModeStack(GridCache.DisplayMode.DISPLAY)
            );
            menu.addMenuClickHandler(NetworkCraftingGridNewStyle.this.getToggleModeSlot(), (p, slot, item, action) -> {
               if (!action.isRightClicked()) {
                  GridCache gridCache = NetworkCraftingGridNewStyle.this.getCacheMap().get(menu.getLocation());
                  gridCache.toggleDisplayMode();
                  menu.replaceExistingItem(NetworkCraftingGridNewStyle.this.getToggleModeSlot(), NetworkCraftingGridNewStyle.this.getModeStack(gridCache));
                  NetworkCraftingGridNewStyle.this.updateDisplay(menu);
               }

               return false;
            });
            menu.replaceExistingItem(33, Icon.CRAFT_BUTTON_NEW_STYLE);
            menu.addMenuClickHandler(33, (p, slot, item, action) -> {
               NetworkCraftingGridNewStyle.this.tryCraft(menu, p, action);
               return false;
            });

            for (int displaySlot : NetworkCraftingGridNewStyle.this.getDisplaySlots()) {
               menu.replaceExistingItem(displaySlot, ChestMenuUtils.getBackground());
               menu.addMenuClickHandler(displaySlot, (p, slot, item, action) -> false);
            }

            for (int backgroundSlot : NetworkCraftingGridNewStyle.this.getBackgroundSlots()) {
               menu.replaceExistingItem(backgroundSlot, ChestMenuUtils.getBackground());
               menu.addMenuClickHandler(backgroundSlot, (p, slot, item, action) -> false);
            }

            menu.addPlayerInventoryClickHandler(NetworkCraftingGridNewStyle.this.outsideKeybinds());
            NetworkCraftingGridNewStyle.this.addKeybindSettingsButton(menu, NetworkCraftingGridNewStyle.this.getKeybindButtonSlot());
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
      return 35;
   }

   @Override
   public int getPagePrevious() {
      return 44;
   }

   @Override
   public int getPageNext() {
      return 53;
   }

   @Override
   public int getKeybindButtonSlot() {
      return 43;
   }

   @Override
   public int getToggleModeSlot() {
      return 52;
   }

   @Override
   protected int getFilterSlot() {
      return 42;
   }

   private synchronized void tryCraft(BlockMenu menu, Player player, ClickAction action) {
      int times = 1;
      if (action.isRightClicked()) {
         times = 64;
      }

      NodeDefinition definition = NetworkStorage.getNode(menu.getLocation());
      if (definition.getNode() != null) {
         NetworkRoot root = definition.getNode().getRoot();

         for (int k = 0; k < times; k++) {
            ItemStack[] inputs = new ItemStack[INGREDIENT_SLOTS.length];
            int i = 0;

            for (int recipeSlot : INGREDIENT_SLOTS) {
               ItemStack stack = menu.getItemInSlot(recipeSlot);
               inputs[i] = stack;
               i++;
            }

            ItemStack crafted = null;

            for (Entry<ItemStack[], ItemStack> entry : SupportedCraftingTableRecipes.getRecipes().entrySet()) {
               if (SupportedCraftingTableRecipes.testRecipe(inputs, entry.getKey())) {
                  crafted = entry.getValue();
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
               crafted = Bukkit.craftItem(inputs, player.getWorld(), player);
            }

            if (crafted.getType() == Material.AIR || !BlockMenuUtil.fits(menu, crafted, 34)) {
               return;
            }

            NetworkCraftEvent event = new NetworkCraftEvent(player, this, inputs, crafted);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
               return;
            }

            crafted = event.getOutput();
            if (crafted != null) {
               BlockMenuUtil.pushItem(menu, crafted.clone(), 34);
            }

            for (int recipeSlot : INGREDIENT_SLOTS) {
               ItemStack itemInSlot = menu.getItemInSlot(recipeSlot);
               if (itemInSlot != null) {
                  ItemStack itemInSlotClone = itemInSlot.clone();
                  itemInSlotClone.setAmount(1);
                  BlockMenuUtil.consumeItem(menu, recipeSlot, 1, true);
                  if (menu.getItemInSlot(recipeSlot) == null) {
                     GridItemRequest request = new GridItemRequest(itemInSlotClone, 1, player);
                     ItemStack requestingStack = root.getItemStack(menu.getLocation(), request);
                     if (requestingStack != null) {
                        menu.replaceExistingItem(recipeSlot, requestingStack);
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   public List<Keybinds> keybinds() {
      return List.of(this.displayKeybinds(), this.outsideKeybinds());
   }
}
