package io.github.sefiraat.networks.slimefun.network.grid;

import com.balugaq.netex.api.visual.Screen;
import io.github.sefiraat.networks.slimefun.NetworkSlimefunItems;
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

public class NetworkGrid extends AbstractGrid {
   private static final int[] BACKGROUND_SLOTS = new int[]{17, 26};
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
   private static final int INPUT_SLOT = 8;
   private static final int CHANGE_SORT = 26;
   private static final int FILTER = 35;
   private static final int PAGE_PREVIOUS = 44;
   private static final int PAGE_NEXT = 53;
   private static final Map<Location, GridCache> CACHE_MAP = new HashMap<>();

   public NetworkGrid(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe);
      this.addItemHandler(new ItemHandler[]{new BlockBreakHandler(false, false) {
         public void onPlayerBreak(@NotNull BlockBreakEvent event, @NotNull ItemStack item, @NotNull List<ItemStack> drops) {
         }
      }});
   }

   @NotNull
   @Override
   protected BlockMenuPreset getPreset() {
      return new BlockMenuPreset(this.getId(), this.getItemName()) {
         public void init() {
            this.drawBackground(NetworkGrid.this.getBackgroundSlots());
            this.drawBackground(NetworkGrid.this.getDisplaySlots());
            this.setSize(54);
         }

         public boolean canOpen(@NotNull Block block, @NotNull Player player) {
            return player.hasPermission("slimefun.inventory.bypass")
               || NetworkSlimefunItems.NETWORK_GRID.canUse(player, false)
                  && Slimefun.getProtectionManager().hasPermission(player, block.getLocation(), Interaction.INTERACT_BLOCK);
         }

         public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
            return new int[0];
         }

         public void newInstance(@NotNull BlockMenu menu, @NotNull Block b) {
            NetworkGrid.this.getCacheMap().put(menu.getLocation(), new GridCache(0, 0, GridCache.SortOrder.ALPHABETICAL));
            menu.replaceExistingItem(NetworkGrid.this.getPagePrevious(), NetworkGrid.this.getPagePreviousStack());
            menu.addMenuClickHandler(NetworkGrid.this.getPagePrevious(), (p, slot, item, action) -> {
               GridCache gridCache = NetworkGrid.this.getCacheMap().get(menu.getLocation());
               gridCache.setPage(gridCache.getPage() <= 0 ? 0 : gridCache.getPage() - 1);
               NetworkGrid.this.getCacheMap().put(menu.getLocation(), gridCache);
               NetworkGrid.this.updateDisplay(menu);
               return false;
            });
            menu.replaceExistingItem(NetworkGrid.this.getPageNext(), NetworkGrid.this.getPageNextStack());
            menu.addMenuClickHandler(NetworkGrid.this.getPageNext(), (p, slot, item, action) -> {
               GridCache gridCache = NetworkGrid.this.getCacheMap().get(menu.getLocation());
               gridCache.setPage(gridCache.getPage() >= gridCache.getMaxPages() ? gridCache.getMaxPages() : gridCache.getPage() + 1);
               NetworkGrid.this.getCacheMap().put(menu.getLocation(), gridCache);
               NetworkGrid.this.updateDisplay(Screen.of(menu));
               return false;
            });
            menu.replaceExistingItem(NetworkGrid.this.getChangeSort(), NetworkGrid.this.getChangeSortStack());
            menu.addMenuClickHandler(NetworkGrid.this.getChangeSort(), (p, slot, item, action) -> {
               GridCache gridCache = NetworkGrid.this.getCacheMap().get(menu.getLocation());
               AbstractGrid.updateSortOrder(gridCache, action, 2);
               NetworkGrid.this.getCacheMap().put(menu.getLocation(), gridCache);
               NetworkGrid.this.updateDisplay(Screen.of(menu));
               return false;
            });
            menu.replaceExistingItem(NetworkGrid.this.getFilterSlot(), NetworkGrid.this.getFilterStack());
            menu.addMenuClickHandler(NetworkGrid.this.getFilterSlot(), (p, slot, item, action) -> {
               GridCache gridCache = NetworkGrid.this.getCacheMap().get(menu.getLocation());
               NetworkGrid.this.setFilter(p, menu, gridCache, action);
               NetworkGrid.this.getCacheMap().put(menu.getLocation(), gridCache);
               return false;
            });

            for (int displaySlot : NetworkGrid.this.getDisplaySlots()) {
               menu.replaceExistingItem(displaySlot, ChestMenuUtils.getBackground());
               menu.addMenuClickHandler(displaySlot, (p, slot, item, action) -> false);
            }

            menu.addPlayerInventoryClickHandler((p, s, i, a) -> {
               if (a.isShiftClicked() && !a.isRightClicked()) {
                  NetworkGrid.this.receiveItem(p, i, a, Screen.of(menu));
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
   public int getInputSlot() {
      return 8;
   }

   @Override
   public int getChangeSort() {
      return 26;
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
   protected int getFilterSlot() {
      return 35;
   }
}
