package io.github.sefiraat.networks.listeners;

import com.balugaq.netex.api.events.NetworksBlockBreakEvent;
import com.balugaq.netex.api.events.NetworksBlockPlaceEvent;
import com.balugaq.netex.api.gui.MatchOptionMenu;
import com.balugaq.netex.api.visual.Screen;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.utils.NetworkUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import java.text.MessageFormat;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.jetbrains.annotations.NotNull;

public class SyncListener implements Listener {
   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onBlockBreak(@NotNull BlockBreakEvent e) {
      Networks.getInstance().debug(MessageFormat.format("Listened BlockBreakEvent at {0}", e.getBlock().getLocation()));
      NetworkUtils.clearNetwork(e.getBlock().getLocation());
      SlimefunItem slimefunItem = StorageCacheUtils.getSfItem(e.getBlock().getLocation());
      if (slimefunItem != null && slimefunItem.getAddon() instanceof Networks) {
         NetworksBlockBreakEvent event = new NetworksBlockBreakEvent(e.getBlock(), e.getPlayer());
         Bukkit.getPluginManager().callEvent(event);
         if (event.isCancelled()) {
            e.setCancelled(true);
         }

         MatchOptionMenu.MATCH_OPTION_MAP.remove(e.getBlock().getLocation());
         Screen.delete(e.getBlock().getLocation());
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onBlockPlace(@NotNull BlockPlaceEvent e) {
      Networks.getInstance().debug(MessageFormat.format("Listened BlockPlaceEvent at {0}", e.getBlock().getLocation()));
      NetworkUtils.clearNetwork(e.getBlock().getLocation());
      SlimefunItem slimefunItem = StorageCacheUtils.getSfItem(e.getBlock().getLocation());
      if (slimefunItem != null && slimefunItem.getAddon() instanceof Networks) {
         NetworksBlockPlaceEvent event = new NetworksBlockPlaceEvent(e.getBlock(), e.getPlayer());
         Bukkit.getPluginManager().callEvent(event);
         if (event.isCancelled()) {
            e.setCancelled(true);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onChunkUnload(@NotNull ChunkUnloadEvent e) {
      Networks.getInstance()
         .debug(
            MessageFormat.format("Listened ChunkUnloadEvent at world: {0}, x: {1}, z: {2}", e.getWorld().getName(), e.getChunk().getX(), e.getChunk().getZ())
         );
      NetworkStorage.unregisterChunk(e.getChunk());
   }
}
