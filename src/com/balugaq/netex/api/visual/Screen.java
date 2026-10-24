package com.balugaq.netex.api.visual;

import com.balugaq.bim.grid.ActiveGrid;
import com.ytdd9527.networksexpansion.implementation.machines.networks.grids.visual.ActiveGridScreen;
import com.ytdd9527.networksexpansion.implementation.machines.networks.grids.visual.BlockMenuScreen;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu.MenuClickHandler;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface Screen {
   Map<Location, Screen> activeScreens = new ConcurrentHashMap<>();

   Location getLocation();

   boolean hasViewer();

   boolean isAlive();

   void setItem(int var1, ItemStack var2);

   void setItem(int var1, ItemStack var2, MenuClickHandler var3);

   SlimefunItem source();

   void close();

   void open(Player... var1);

   static Screen of(BlockMenu blockMenu) {
      Screen cached = activeScreens.get(blockMenu.getLocation());
      if (cached != null && cached.isAlive()) {
         return cached;
      } else {
         BlockMenuScreen screen = new BlockMenuScreen(blockMenu);
         activeScreens.put(blockMenu.getLocation(), screen);
         return screen;
      }
   }

   static Screen of(ActiveGrid active) {
      Screen cached = activeScreens.get(active.getLocation());
      if (cached != null && cached.isAlive()) {
         return cached;
      } else {
         ActiveGridScreen screen = new ActiveGridScreen(active);
         activeScreens.put(active.getLocation(), screen);
         return screen;
      }
   }

   static Screen delete(Location location) {
      return activeScreens.remove(location);
   }

   @Nullable
   static Screen of(Location location) {
      return activeScreens.get(location);
   }
}
