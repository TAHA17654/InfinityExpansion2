package com.ytdd9527.networksexpansion.implementation.machines.networks.grids;

import com.balugaq.netex.api.keybind.Keybindable;
import com.balugaq.netex.api.keybind.Keybinds;
import com.balugaq.netex.api.visual.Screen;
import com.ytdd9527.networksexpansion.core.items.machines.AbstractGridNewStyle;
import com.ytdd9527.networksexpansion.implementation.ExpansionItems;
import io.github.sefiraat.networks.network.NodeType;
import io.github.sefiraat.networks.slimefun.network.grid.AbstractGrid;
import io.github.sefiraat.networks.slimefun.network.grid.GridCache;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemHandler;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class NetworkGridNewStyle extends AbstractGridNewStyle implements Keybindable {
   private static final int[] BACKGROUND_SLOTS = new int[]{8};
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
   private static final int KEYBIND_BUTTON_SLOT = 8;
   private static final int CHANGE_SORT = 35;
   private static final int FILTER = 26;
   private static final int PAGE_PREVIOUS = 44;
   private static final int PAGE_NEXT = 53;
   private static final int TOGGLE_MODE_SLOT = 17;
   private static final Map<Location, GridCache> CACHE_MAP = new HashMap<>();

   public NetworkGridNewStyle(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe);
      this.addItemHandler(new ItemHandler[]{new BlockBreakHandler(false, false) {
         public void onPlayerBreak(@NotNull BlockBreakEvent event, @NotNull ItemStack item, @NotNull List<ItemStack> drops) {
         }
      }});
   }

   public NetworkGridNewStyle(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, NodeType nodeType) {
      super(itemGroup, item, recipeType, recipe, nodeType);
   }

   @Override
   protected BlockMenuPreset getPreset() {
      return new BlockMenuPreset(this.getId(), this.getItemName()) {
         public void init() {
            this.drawBackground(NetworkGridNewStyle.this.getDisplaySlots());
            this.setSize(54);
         }

         public boolean canOpen(Block block, Player player) {
            return player.hasPermission("slimefun.inventory.bypass")
               || ExpansionItems.NETWORK_GRID_NEW_STYLE.canUse(player, false)
                  && Slimefun.getProtectionManager().hasPermission(player, block.getLocation(), Interaction.INTERACT_BLOCK);
         }

         public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
            return new int[0];
         }

         public void newInstance(BlockMenu menu, Block b) {
            NetworkGridNewStyle.this.getCacheMap().put(menu.getLocation(), new GridCache(0, 0, GridCache.SortOrder.ALPHABETICAL));
            menu.replaceExistingItem(NetworkGridNewStyle.this.getPagePrevious(), NetworkGridNewStyle.this.getPagePreviousStack());
            menu.addMenuClickHandler(NetworkGridNewStyle.this.getPagePrevious(), (p, slot, item, action) -> {
               GridCache gridCache = NetworkGridNewStyle.this.getCacheMap().get(menu.getLocation());
               gridCache.setPage(gridCache.getPage() <= 0 ? 0 : gridCache.getPage() - 1);
               NetworkGridNewStyle.this.getCacheMap().put(menu.getLocation(), gridCache);
               NetworkGridNewStyle.this.updateDisplay(Screen.of(menu));
               return false;
            });
            menu.replaceExistingItem(NetworkGridNewStyle.this.getPageNext(), NetworkGridNewStyle.this.getPageNextStack());
            menu.addMenuClickHandler(NetworkGridNewStyle.this.getPageNext(), (p, slot, item, action) -> {
               GridCache gridCache = NetworkGridNewStyle.this.getCacheMap().get(menu.getLocation());
               gridCache.setPage(gridCache.getPage() >= gridCache.getMaxPages() ? gridCache.getMaxPages() : gridCache.getPage() + 1);
               NetworkGridNewStyle.this.getCacheMap().put(menu.getLocation(), gridCache);
               NetworkGridNewStyle.this.updateDisplay(Screen.of(menu));
               return false;
            });
            menu.replaceExistingItem(NetworkGridNewStyle.this.getChangeSort(), NetworkGridNewStyle.this.getChangeSortStack());
            menu.addMenuClickHandler(NetworkGridNewStyle.this.getChangeSort(), (p, slot, item, action) -> {
               GridCache gridCache = NetworkGridNewStyle.this.getCacheMap().get(menu.getLocation());
               AbstractGrid.updateSortOrder(gridCache, action, 4);
               NetworkGridNewStyle.this.getCacheMap().put(menu.getLocation(), gridCache);
               NetworkGridNewStyle.this.updateDisplay(Screen.of(menu));
               return false;
            });
            menu.replaceExistingItem(NetworkGridNewStyle.this.getFilterSlot(), NetworkGridNewStyle.this.getFilterStack());
            menu.addMenuClickHandler(NetworkGridNewStyle.this.getFilterSlot(), (p, slot, item, action) -> {
               GridCache gridCache = NetworkGridNewStyle.this.getCacheMap().get(menu.getLocation());
               NetworkGridNewStyle.this.setFilter(p, Screen.of(menu), gridCache, action);
               return false;
            });
            menu.replaceExistingItem(NetworkGridNewStyle.this.getToggleModeSlot(), NetworkGridNewStyle.this.getModeStack(GridCache.DisplayMode.DISPLAY));
            menu.addMenuClickHandler(NetworkGridNewStyle.this.getToggleModeSlot(), (p, slot, item, action) -> {
               if (!action.isRightClicked()) {
                  GridCache gridCache = NetworkGridNewStyle.this.getCacheMap().get(menu.getLocation());
                  gridCache.toggleDisplayMode();
                  menu.replaceExistingItem(NetworkGridNewStyle.this.getToggleModeSlot(), NetworkGridNewStyle.this.getModeStack(gridCache));
                  NetworkGridNewStyle.this.updateDisplay(Screen.of(menu));
               }

               return false;
            });

            for (int displaySlot : NetworkGridNewStyle.this.getDisplaySlots()) {
               menu.replaceExistingItem(displaySlot, ChestMenuUtils.getBackground());
               menu.addMenuClickHandler(displaySlot, (p, slot, item, action) -> false);
            }

            for (int backgroundSlot : NetworkGridNewStyle.this.getBackgroundSlots()) {
               menu.replaceExistingItem(backgroundSlot, ChestMenuUtils.getBackground());
               menu.addMenuClickHandler(backgroundSlot, (p, slot, item, action) -> false);
            }

            menu.addPlayerInventoryClickHandler(NetworkGridNewStyle.this.outsideKeybinds());
            NetworkGridNewStyle.this.addKeybindSettingsButton(menu, NetworkGridNewStyle.this.getKeybindButtonSlot());
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
      return 8;
   }

   @Override
   public int getToggleModeSlot() {
      return 17;
   }

   @Override
   protected int getFilterSlot() {
      return 26;
   }

   @Override
   public List<Keybinds> keybinds() {
      return List.of(this.displayKeybinds(), this.outsideKeybinds());
   }
}
