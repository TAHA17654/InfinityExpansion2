package com.ytdd9527.networksexpansion.implementation.machines.cellnet.listener;

import com.balugaq.netex.api.events.NetworkRootReadyEvent;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;

public class ControllerDisplayListener implements Listener {
   private final Map<Location, ControllerDisplayListener.DisplayState> states = new ConcurrentHashMap<>();
   private final Map<Location, Long> lastSeen = new ConcurrentHashMap<>();
   private final long timeoutMillis = ((long)Slimefun.getTickerTask().getTickRate() * 4L + 20L) * 50L;

   public ControllerDisplayListener() {
      Bukkit.getScheduler().runTaskTimer(Networks.getInstance(), this::heartbeat, 20L, 20L);
   }

   @EventHandler
   public void onRootReady(@NotNull NetworkRootReadyEvent event) {
      if (Networks.getInstance().isEnabled()) {
         NetworkRoot root = event.getRoot();
         ControllerDisplayListener.DisplayState state = resolve(root);
         Location location = root.getNodePosition();
         this.lastSeen.put(location, System.currentTimeMillis());
         this.states.put(location, state);
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onBlockBreak(@NotNull BlockBreakEvent event) {
      Location location = event.getBlock().getLocation();
      if (this.states.containsKey(location)) {
         this.states.remove(location);
         this.lastSeen.remove(location);
      }
   }

   private static ControllerDisplayListener.DisplayState resolve(@NotNull NetworkRoot root) {
      if (root.isOverburdened() || root.getNodeCount() >= root.getMaxNodes()) {
         return ControllerDisplayListener.DisplayState.FULL;
      } else {
         return root.getNodeCount() <= 1 ? ControllerDisplayListener.DisplayState.OFFLINE : ControllerDisplayListener.DisplayState.ONLINE;
      }
   }

   private void heartbeat() {
      long now = System.currentTimeMillis();
      Iterator<Entry<Location, Long>> iterator = this.lastSeen.entrySet().iterator();

      while (iterator.hasNext()) {
         Entry<Location, Long> entry = iterator.next();
         if (now - entry.getValue() > this.timeoutMillis) {
            iterator.remove();
            this.states.remove(entry.getKey());
         }
      }
   }

   private static enum DisplayState {
      OFFLINE,
      ONLINE,
      FULL;
   }
}
