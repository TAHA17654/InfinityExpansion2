package com.balugaq.bim.grid;

import com.balugaq.bim.BIMLoader;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class GridTickTask implements Runnable {
   private final Plugin plugin;

   @Override
   public void run() {
      this.hoverCheck();
      this.gridCheck();
   }

   public BIMLoader getLoader() {
      return BIMLoader.get(this.plugin);
   }

   private void hoverCheck() {
      for (Player p : Bukkit.getOnlinePlayers()) {
         GridDataCache cache = this.getLoader().getCache();
         InteractUnit unit = this.getLoader().getGridUtil().rayTraceUnit(p);
         InteractUnit old = cache.watching().get(p.getUniqueId());
         if (unit == null) {
            if (old != null) {
               this.getLoader().getGridUtil().offGrid(p);
               GridUtil.offHover(old, p);
            }
         } else {
            if (unit != old) {
               this.getLoader().getGridUtil().offGrid(p);
            }

            GridUtil.offHover(old, p);
            ActiveGrid grid = unit.grid;
            grid.option.onHover(unit, p);
            grid.waitTicks++;
            if (grid.waitTicks % grid.option.tickInterval(grid) == 0) {
               grid.option.tick(grid);
            }
         }
      }
   }

   private void gridCheck() {
      for (ActiveGrid active : this.getLoader().getCache().activeGrids().values()) {
         if (active.isDisplaying) {
            if (active.location.getWorld().getNearbyEntities(active.getHidingBoundingBox(), e -> e instanceof Player).isEmpty()) {
               active.option.onHide(active);
            }
         } else if (!active.location.getWorld().getNearbyEntities(active.getShowingBoundingBox(), e -> e instanceof Player).isEmpty()) {
            active.option.onShow(active);
         }
      }
   }

   @Generated
   public GridTickTask(Plugin plugin) {
      this.plugin = plugin;
   }
}
