package com.ytdd9527.networksexpansion.implementation.machines.networks.grids.visual;

import com.balugaq.bim.grid.ActiveGrid;
import com.balugaq.bim.grid.ActiveGridPreset;
import com.balugaq.netex.api.helpers.Icon;
import com.balugaq.netex.api.visual.Screen;
import com.ytdd9527.networksexpansion.implementation.ExpansionItems;
import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.NodeDefinition;
import io.github.sefiraat.networks.slimefun.network.grid.GridCache;
import io.github.sefiraat.networks.utils.Keys;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class VisualGridPreset extends ActiveGridPreset {
   public VisualGridPreset() {
      super(Networks.getInstance(), Keys.newKey("visual_grid"), 9, 9);
   }

   @Override
   public void postInit(ActiveGrid active) {
      VisualGrid v = ExpansionItems.VISUAL_GRID;
      Screen screen = Screen.of(active);

      for (int s : VisualGrid.BACKGROUND_SLOTS) {
         screen.setItem(s, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
      }

      v.addKeybindSettingsButton(screen, v.getKeybindButtonSlot());
      screen.setItem(v.getFilterSlot(), Icon.FILTER_STACK, (p, sx, i, a) -> {
         GridCache gridCache = v.getCacheMap().get(screen.getLocation());
         v.setFilter(p, screen, gridCache, a);
         return false;
      });
   }

   @Override
   public void tick(ActiveGrid active) {
      GridCache cache = ExpansionItems.VISUAL_GRID.getCacheMap().get(active.getLocation());
      if (cache != null) {
         cache.setEntriesCache(null);
         ExpansionItems.VISUAL_GRID.updateDisplay(Screen.of(active));
      }
   }

   @Override
   public int tickInterval(ActiveGrid active) {
      return 2;
   }

   @Override
   public int entriesSize(ActiveGrid active) {
      NodeDefinition node = NetworkStorage.getNode(active.getLocation());
      if (node != null && node.getNode() != null) {
         NetworkRoot root = node.getNode().getRoot();
         return root.getAllNetworkItemsLongTypeView().size();
      } else {
         return 0;
      }
   }
}
