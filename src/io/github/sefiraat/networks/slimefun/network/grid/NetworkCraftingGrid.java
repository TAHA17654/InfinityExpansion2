package io.github.sefiraat.networks.slimefun.network.grid;

import com.balugaq.netex.api.helpers.Icon;
import com.balugaq.netex.api.helpers.SupportedCraftingTableRecipes;
import com.balugaq.netex.api.visual.Screen;
import com.balugaq.netex.utils.BlockMenuUtil;
import com.balugaq.netex.utils.Lang;
import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.events.NetworkCraftEvent;
import io.github.sefiraat.networks.network.GridItemRequest;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.NodeDefinition;
import io.github.sefiraat.networks.slimefun.NetworkSlimefunItems;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class NetworkCraftingGrid extends AbstractGrid {
   private static final int[] BACKGROUND_SLOTS = new int[]{0, 1, 3, 4, 5, 14, 23, 32, 33, 35, 41, 42, 44, 45, 47, 49, 50, 51, 52, 53};
   private static final int[] DISPLAY_SLOTS = new int[]{9, 10, 11, 12, 13, 18, 19, 20, 21, 22, 27, 28, 29, 30, 31, 36, 37, 38, 39, 40};
   private static final int[] CRAFT_ITEMS = new int[]{6, 7, 8, 15, 16, 17, 24, 25, 26};
   private static final int INPUT_SLOT = 2;
   private static final int FILTER = 45;
   private static final int PAGE_PREVIOUS = 46;
   private static final int CHANGE_SORT = 47;
   private static final int PAGE_NEXT = 48;
   private static final int CRAFT_BUTTON_SLOT = 34;
   private static final int CRAFT_OUTPUT_SLOT = 43;
   private static final Map<Location, GridCache> CACHE_MAP = new HashMap<>();

   public NetworkCraftingGrid(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe);

      for (int craftItem : CRAFT_ITEMS) {
         this.getSlotsToDrop().add(craftItem);
      }

      this.getSlotsToDrop().add(43);
   }

   @Override
   public void postRegister() {
      this.getPreset();
   }

   @NotNull
   @Override
   public BlockMenuPreset getPreset() {
      return new BlockMenuPreset(this.getId(), this.getItemName()) {
         public void init() {
            this.drawBackground(NetworkCraftingGrid.BACKGROUND_SLOTS);
            this.drawBackground(NetworkCraftingGrid.this.getDisplaySlots());
            this.setSize(54);
         }

         public boolean canOpen(@NotNull Block block, @NotNull Player player) {
            return player.hasPermission("slimefun.inventory.bypass")
               || NetworkSlimefunItems.NETWORK_CRAFTING_GRID.canUse(player, false)
                  && Slimefun.getProtectionManager().hasPermission(player, block.getLocation(), Interaction.INTERACT_BLOCK);
         }

         public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
            return new int[0];
         }

         public void newInstance(@NotNull BlockMenu menu, @NotNull Block b) {
            NetworkCraftingGrid.CACHE_MAP.put(menu.getLocation(), new GridCache(0, 0, GridCache.SortOrder.ALPHABETICAL));
            menu.replaceExistingItem(NetworkCraftingGrid.this.getPagePrevious(), NetworkCraftingGrid.this.getPagePreviousStack());
            menu.addMenuClickHandler(NetworkCraftingGrid.this.getPagePrevious(), (p, slot, item, action) -> {
               GridCache gridCache = NetworkCraftingGrid.this.getCacheMap().get(menu.getLocation());
               gridCache.setPage(gridCache.getPage() <= 0 ? 0 : gridCache.getPage() - 1);
               NetworkCraftingGrid.CACHE_MAP.put(menu.getLocation(), gridCache);
               NetworkCraftingGrid.this.updateDisplay(menu);
               return false;
            });
            menu.replaceExistingItem(NetworkCraftingGrid.this.getPageNext(), NetworkCraftingGrid.this.getPageNextStack());
            menu.addMenuClickHandler(NetworkCraftingGrid.this.getPageNext(), (p, slot, item, action) -> {
               GridCache gridCache = NetworkCraftingGrid.this.getCacheMap().get(menu.getLocation());
               gridCache.setPage(gridCache.getPage() >= gridCache.getMaxPages() ? gridCache.getMaxPages() : gridCache.getPage() + 1);
               NetworkCraftingGrid.this.getCacheMap().put(menu.getLocation(), gridCache);
               NetworkCraftingGrid.this.updateDisplay(menu);
               return false;
            });
            menu.replaceExistingItem(NetworkCraftingGrid.this.getChangeSort(), NetworkCraftingGrid.this.getChangeSortStack());
            menu.addMenuClickHandler(NetworkCraftingGrid.this.getChangeSort(), (p, slot, item, action) -> {
               GridCache gridCache = NetworkCraftingGrid.this.getCacheMap().get(menu.getLocation());
               AbstractGrid.updateSortOrder(gridCache, action, 2);
               NetworkCraftingGrid.this.getCacheMap().put(menu.getLocation(), gridCache);
               NetworkCraftingGrid.this.updateDisplay(menu);
               return false;
            });
            menu.replaceExistingItem(NetworkCraftingGrid.this.getFilterSlot(), NetworkCraftingGrid.this.getFilterStack());
            menu.addMenuClickHandler(NetworkCraftingGrid.this.getFilterSlot(), (p, slot, item, action) -> {
               GridCache gridCache = NetworkCraftingGrid.this.getCacheMap().get(menu.getLocation());
               NetworkCraftingGrid.this.setFilter(p, menu, gridCache, action);
               return false;
            });

            for (int displaySlot : NetworkCraftingGrid.this.getDisplaySlots()) {
               menu.replaceExistingItem(displaySlot, ChestMenuUtils.getBackground());
               menu.addMenuClickHandler(displaySlot, (p, slot, item, action) -> false);
            }

            menu.replaceExistingItem(34, Icon.CRAFT_BUTTON);
            menu.addMenuClickHandler(34, (player, slot, item, action) -> {
               if (action.isShiftClicked()) {
                  NetworkCraftingGrid.this.tryReturnItems(menu);
               } else {
                  NetworkCraftingGrid.this.tryCraft(menu, player);
               }

               return false;
            });
            menu.addPlayerInventoryClickHandler((p, s, i, a) -> {
               if (a.isShiftClicked() && !a.isRightClicked()) {
                  NetworkCraftingGrid.this.receiveItem(p, i, a, Screen.of(menu));
                  return false;
               } else {
                  return true;
               }
            });
         }
      };
   }

   @NotNull
   @Override
   protected Map<Location, GridCache> getCacheMap() {
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
   public int getInputSlot() {
      return 2;
   }

   @Override
   public int getChangeSort() {
      return 47;
   }

   @Override
   public int getPagePrevious() {
      return 46;
   }

   @Override
   public int getPageNext() {
      return 48;
   }

   @Override
   protected int getFilterSlot() {
      return 45;
   }

   private void tryCraft(@NotNull BlockMenu menu, @NotNull Player player) {
      NodeDefinition definition = NetworkStorage.getNode(menu.getLocation());
      if (definition.getNode() != null) {
         ItemStack[] inputs = new ItemStack[CRAFT_ITEMS.length];
         int i = 0;

         for (int recipeSlot : CRAFT_ITEMS) {
            ItemStack stack = menu.getItemInSlot(recipeSlot);
            inputs[i] = stack;
            i++;
         }

         ItemStack crafted = null;

         for (Entry<ItemStack[], ItemStack> entry : SupportedCraftingTableRecipes.getRecipes().entrySet()) {
            if (SupportedCraftingTableRecipes.testRecipe(inputs, entry.getKey())) {
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
            crafted = Bukkit.craftItem(inputs, player.getWorld(), player);
         }

         if (crafted.getType() != Material.AIR && BlockMenuUtil.fits(menu, crafted, 43)) {
            NetworkCraftEvent event = new NetworkCraftEvent(player, this, inputs, crafted);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
               crafted = event.getOutput();
               if (crafted != null) {
                  BlockMenuUtil.pushItem(menu, crafted, 43);
               }

               NetworkRoot root = definition.getNode().getRoot();

               for (int recipeSlot : CRAFT_ITEMS) {
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
   }

   private void tryReturnItems(@NotNull BlockMenu menu) {
      NodeDefinition definition = NetworkStorage.getNode(menu.getLocation());
      if (definition.getNode() != null) {
         for (int recipeSlot : CRAFT_ITEMS) {
            ItemStack stack = menu.getItemInSlot(recipeSlot);
            if (stack != null && stack.getType() != Material.AIR) {
               definition.getNode().getRoot().addItemStack(menu.getLocation(), stack);
            }
         }
      }
   }
}
