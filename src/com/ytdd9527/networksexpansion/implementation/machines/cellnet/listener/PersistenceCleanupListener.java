package com.ytdd9527.networksexpansion.implementation.machines.cellnet.listener;

import com.balugaq.netex.utils.Debug;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.drive.ender.EnderChannelController;
import io.github.sefiraat.networks.Networks;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.jetbrains.annotations.NotNull;

public class PersistenceCleanupListener implements Listener {
   @EventHandler
   public void onPluginDisable(@NotNull PluginDisableEvent event) {
      if (event.getPlugin() == Networks.getInstance()) {
         try {
            EnderChannelController.getInstance().flushAndClose();
         } catch (Throwable var3) {
            Debug.trace(var3, "Cell Network cleanup on shutdown failed");
         }
      }
   }
}
