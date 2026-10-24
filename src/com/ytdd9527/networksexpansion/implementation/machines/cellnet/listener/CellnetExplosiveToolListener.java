package com.ytdd9527.networksexpansion.implementation.machines.cellnet.listener;

import com.balugaq.netex.utils.Debug;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.converter.CellCleaner;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.drive.CellDrive;
import io.github.thebusybiscuit.slimefun4.api.events.ExplosiveToolBreakBlocksEvent;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class CellnetExplosiveToolListener implements Listener {
   @EventHandler
   public void onExplosiveBlockBreak(@NotNull ExplosiveToolBreakBlocksEvent event) {
      List<Block> blocksToRemove = new ArrayList<>();

      for (Block block : event.getAdditionalBlocks()) {
         Location location = block.getLocation();
         SlimefunItem item = StorageCacheUtils.getSfItem(location);
         if (item instanceof CellDrive || item instanceof CellCleaner) {
            blocksToRemove.add(block);
            Debug.debug("Blocked a blasting tool from breaking a Cell Network block: " + location);
         }
      }

      event.getAdditionalBlocks().removeAll(blocksToRemove);
   }
}
