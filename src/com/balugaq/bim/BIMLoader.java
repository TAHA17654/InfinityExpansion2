package com.balugaq.bim;

import com.balugaq.bim.grid.GridDataCache;
import com.balugaq.bim.grid.GridTickTask;
import com.balugaq.bim.grid.GridUtil;
import com.balugaq.bim.grid.MenuListener;
import java.util.HashMap;
import java.util.Map;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;

public class BIMLoader {
   public static final Map<Plugin, BIMLoader> loaders = new HashMap<>();
   private final Plugin instance;
   private final GridUtil gridUtil;
   private final GridDataCache cache;
   private final MenuListener listener;

   public BIMLoader(Plugin instance) {
      this.instance = instance;
      loaders.put(instance, this);
      this.gridUtil = new GridUtil(instance);
      this.cache = new GridDataCache();
      this.listener = new MenuListener(instance);
   }

   public static BIMLoader get(Plugin instance) {
      try {
         BIMLoader loader = loaders.get(instance);
         if (loader == null) {
            throw new IllegalAccessException("You should call BIMLoader#new(Plugin) first!");
         } else {
            return loader;
         }
      } catch (Throwable var2) {
         throw var2;
      }
   }

   public void load() {
      Bukkit.getPluginManager().registerEvents(this.listener, this.instance);
      Bukkit.getScheduler().runTaskTimer(this.instance, new GridTickTask(this.instance), 0L, 1L);
   }

   public void shutdown() {
      HandlerList.unregisterAll(this.listener);
      this.cache.activeGrids().values().forEach(g -> g.getOption().onDestroy(g));
      this.cache.activeGrids().clear();
      loaders.remove(this.instance);
   }

   @Generated
   public Plugin getInstance() {
      return this.instance;
   }

   @Generated
   public GridUtil getGridUtil() {
      return this.gridUtil;
   }

   @Generated
   public GridDataCache getCache() {
      return this.cache;
   }

   @Generated
   public MenuListener getListener() {
      return this.listener;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof BIMLoader other)) {
         return false;
      } else if (!other.canEqual(this)) {
         return false;
      } else {
         Object this$instance = this.getInstance();
         Object other$instance = other.getInstance();
         if (this$instance == null ? other$instance == null : this$instance.equals(other$instance)) {
            Object this$gridUtil = this.getGridUtil();
            Object other$gridUtil = other.getGridUtil();
            if (this$gridUtil == null ? other$gridUtil == null : this$gridUtil.equals(other$gridUtil)) {
               Object this$cache = this.getCache();
               Object other$cache = other.getCache();
               if (this$cache == null ? other$cache == null : this$cache.equals(other$cache)) {
                  Object this$listener = this.getListener();
                  Object other$listener = other.getListener();
                  return this$listener == null ? other$listener == null : this$listener.equals(other$listener);
               } else {
                  return false;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof BIMLoader;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $instance = this.getInstance();
      result = result * 59 + ($instance == null ? 43 : $instance.hashCode());
      Object $gridUtil = this.getGridUtil();
      result = result * 59 + ($gridUtil == null ? 43 : $gridUtil.hashCode());
      Object $cache = this.getCache();
      result = result * 59 + ($cache == null ? 43 : $cache.hashCode());
      Object $listener = this.getListener();
      return result * 59 + ($listener == null ? 43 : $listener.hashCode());
   }

   @Generated
   @Override
   public String toString() {
      return "BIMLoader(instance="
         + this.getInstance()
         + ", gridUtil="
         + this.getGridUtil()
         + ", cache="
         + this.getCache()
         + ", listener="
         + this.getListener()
         + ")";
   }
}
