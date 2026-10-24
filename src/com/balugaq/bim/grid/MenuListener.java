package com.balugaq.bim.grid;

import com.balugaq.bim.BIMLoader;
import com.balugaq.bim.events.PlayerInteractUnitEvent;
import com.balugaq.bim.events.PlayerScrollGridEvent;
import lombok.Generated;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class MenuListener implements Listener {
   private final Plugin plugin;

   public BIMLoader getLoader() {
      return BIMLoader.get(this.plugin);
   }

   @EventHandler(
      ignoreCancelled = false
   )
   public void onInteract(PlayerInteractEvent event) {
      if (event.getHand() != EquipmentSlot.OFF_HAND) {
         InteractUnit unit = this.getLoader().getGridUtil().rayTraceUnit(event.getPlayer());
         if (unit != null) {
            ClickDTO dto = new ClickDTO(event, unit);
            if (new PlayerInteractUnitEvent(dto).callEvent()) {
               ((ClickHandler)unit.grid.clickHandlers.getOrDefault(unit.idx, ClickHandler.cancel)).onClick(dto);
            }
         }
      }
   }

   @EventHandler(
      ignoreCancelled = true
   )
   public void onScroll(PlayerItemHeldEvent event) {
      InteractUnit unit = this.getLoader().getGridUtil().rayTraceUnit(event.getPlayer());
      if (unit != null) {
         int p = event.getPreviousSlot();
         int n = event.getNewSlot();
         ScrollResult r;
         int delta;
         if (p == 0 && n == 8) {
            r = ScrollResult.DOWN;
            delta = 1;
         } else if (p == 8 && n == 0) {
            r = ScrollResult.UP;
            delta = 1;
         } else if (p > n) {
            r = ScrollResult.DOWN;
            delta = p - n;
         } else {
            if (p >= n) {
               return;
            }

            r = ScrollResult.UP;
            delta = n - p;
         }

         if (new PlayerScrollGridEvent(event.getPlayer(), r, delta).callEvent()) {
            unit.grid.option.onScroll(unit.grid, r, delta, event);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void offGrid(PlayerDeathEvent event) {
      this.getLoader().getGridUtil().offGrid(event.getPlayer());
   }

   @EventHandler
   public void offGrid(PlayerQuitEvent event) {
      this.getLoader().getGridUtil().offGrid(event.getPlayer());
   }

   @Generated
   public MenuListener(Plugin plugin) {
      this.plugin = plugin;
   }
}
