package io.github.sefiraat.networks.managers;

import com.balugaq.netex.core.listeners.HangingBlockInteractListener;
import com.balugaq.netex.core.listeners.JEGCompatibleListener;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.menu.CellMenuListener;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.collect.CollectListener;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.drive.menu.DriveCellOpenListener;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.drive.menu.DriveUniquenessListener;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.drive.menu.DriveWhitelistListener;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.drive.menu.EnderVoidGuardListener;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.listener.CellnetExplosiveToolListener;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.listener.ControllerDisplayListener;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.listener.NetworkCacheInvalidationListener;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.listener.PersistenceCleanupListener;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.ChatInputListener;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.GhostItems;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.listeners.ExplosiveToolListener;
import io.github.sefiraat.networks.listeners.SyncListener;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class ListenerManager {
   public ListenerManager() {
      this.addListener(new ExplosiveToolListener());
      this.addListener(new SyncListener());
      if (Networks.getSupportedPluginManager().isJustEnoughGuide()) {
         try {
            this.addListener(new JEGCompatibleListener());
         } catch (Throwable var2) {
            Networks.getSupportedPluginManager().setJustEnoughGuide(false);
         }
      }

      this.addListener(new HangingBlockInteractListener());
      this.addListener(new CellMenuListener());
      this.addListener(new DriveUniquenessListener());
      this.addListener(new DriveWhitelistListener());
      this.addListener(new CellnetExplosiveToolListener());
      this.addListener(new ChatInputListener());
      this.addListener(new NetworkCacheInvalidationListener());
      this.addListener(new DriveCellOpenListener());
      this.addListener(new EnderVoidGuardListener());
      this.addListener(new PersistenceCleanupListener());
      this.addListener(new GhostItems());
      this.addListener(new CollectListener());
      this.addListener(new ControllerDisplayListener());
   }

   private void addListener(@NotNull Listener listener) {
      Networks.getPluginManager().registerEvents(listener, Networks.getInstance());
   }
}
