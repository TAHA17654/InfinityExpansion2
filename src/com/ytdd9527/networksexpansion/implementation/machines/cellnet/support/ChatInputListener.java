package com.ytdd9527.networksexpansion.implementation.machines.cellnet.support;

import com.balugaq.netex.utils.Lang;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.assembly.AssemblyMonitor;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.assembly.AssemblyMonitorBridge;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.menu.CellMenu;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.converter.CellCleaner;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.converter.CellConverter;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.drive.CellDrive;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.drive.DriveMonitor;
import io.github.sefiraat.networks.Networks;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.Locale;
import java.util.UUID;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChatInputListener implements Listener {
   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onChat(@NotNull AsyncChatEvent e) {
      Player player = e.getPlayer();
      ChatInput.Pending pending = ChatInput.pollPending(player.getUniqueId());
      if (pending != null) {
         e.setCancelled(true);
         String raw = PlainTextComponentSerializer.plainText().serialize(e.message()).trim();
         switch (pending.type()) {
            case TARGET:
               this.handleTarget(player, pending, raw);
               break;
            case DRIVE_NAME:
               this.handleDriveName(player, pending, raw);
               break;
            case MANAGER_DRIVE_NAME:
               this.handleManagerDriveName(player, pending, raw);
               break;
            case MONITOR_SEARCH:
               this.handleMonitorSearch(player, pending, raw);
         }
      }
   }

   private void handleMonitorSearch(@NotNull Player player, @NotNull ChatInput.Pending pending, @NotNull String raw) {
      if (pending.context() instanceof ChatInput.SearchContext searchContext) {
         String term = raw.toLowerCase(Locale.ROOT).trim();
         if (term.length() > 16) {
            player.sendMessage(Lang.getString("messages.cellnet.search.invalid"));
         } else {
            Location finalLocation = searchContext.location();
            UUID finalCellUuid = searchContext.cellUuid();
            ChatInput.SearchTarget target = searchContext.target();
            Bukkit.getScheduler().runTask(Networks.getInstance(), () -> {
               if (player.isOnline()) {
                  switch (target) {
                     case MONITOR:
                        AssemblyMonitor.applySearchFromChat(player, finalLocation, term);
                        break;
                     case DRIVE_BROWSER:
                        CellDrive.applyBrowserSearchFromChat(player, finalLocation, term);
                        break;
                     case DRIVE_MANAGER:
                        DriveMonitor.applySearchFromChat(player, finalLocation, term);
                        break;
                     case CELL_MENU:
                        CellMenu.applySearchFromChat(player, finalCellUuid, term);
                        break;
                     case CELL_CLEANER:
                        CellCleaner.applySearchFromChat(player, finalLocation, term);
                        break;
                     case CELL_CONVERTER:
                        CellConverter.applySearchFromChat(player, finalLocation, term);
                  }
               }
            });
         }
      }
   }

   private void handleDriveName(@NotNull Player player, @NotNull ChatInput.Pending pending, @NotNull String raw) {
      if (pending.context() instanceof ChatInput.DriveNameContext nameContext) {
         String name = raw.trim();
         if (!name.isEmpty() && name.length() <= 16) {
            Location finalLocation = nameContext.location();
            Bukkit.getScheduler().runTask(Networks.getInstance(), () -> {
               if (player.isOnline()) {
                  AssemblyMonitorBridge.setNameFromMonitor(player, finalLocation, name);
               }
            });
         } else {
            player.sendMessage(Lang.getString("messages.cellnet.monitor.name_invalid"));
         }
      }
   }

   private void handleManagerDriveName(@NotNull Player player, @NotNull ChatInput.Pending pending, @NotNull String raw) {
      if (pending.context() instanceof ChatInput.DriveNameContext nameContext) {
         String name = raw.trim();
         if (name.length() > 16) {
            player.sendMessage(Lang.getString("messages.cellnet.monitor.name_invalid"));
         } else {
            Location finalLocation = nameContext.location();
            Bukkit.getScheduler().runTask(Networks.getInstance(), () -> {
               if (player.isOnline()) {
                  DriveMonitor.applyNameFromChat(player, finalLocation, name);
               }
            });
         }
      }
   }

   @EventHandler
   public void onQuit(@NotNull PlayerQuitEvent e) {
      ChatInput.clear(e.getPlayer().getUniqueId());
   }

   private void handleTarget(@NotNull Player player, @NotNull ChatInput.Pending pending, @NotNull String raw) {
      if (pending.context() instanceof ChatInput.TargetContext targetContext) {
         Long value = parseNonNegativeLong(raw);
         if (value != null && value <= Long.MAX_VALUE) {
            Location finalLocation = targetContext.location();
            int finalSlot = targetContext.blueprintSlot();
            boolean finalKeep = targetContext.keep();
            long target = value;
            Bukkit.getScheduler().runTask(Networks.getInstance(), () -> {
               if (player.isOnline()) {
                  AssemblyMonitorBridge.handleTargetInput(finalLocation, finalSlot, target, finalKeep);
               }
            });
         } else {
            player.sendMessage(Lang.getString("messages.cellnet.input.target_invalid"));
         }
      }
   }

   @Nullable
   private static Long parseNonNegativeLong(@Nullable String input) {
      if (input != null && !input.isEmpty()) {
         for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c < '0' || c > '9') {
               return null;
            }
         }

         try {
            return Long.parseLong(input);
         } catch (NumberFormatException var3) {
            return null;
         }
      } else {
         return null;
      }
   }
}
