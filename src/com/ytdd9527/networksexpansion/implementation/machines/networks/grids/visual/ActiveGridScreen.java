package com.ytdd9527.networksexpansion.implementation.machines.networks.grids.visual;

import com.balugaq.bim.grid.ActiveGrid;
import com.balugaq.netex.api.visual.Screen;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.implementation.ExpansionItems;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import lombok.Generated;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu.MenuClickHandler;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ActiveGridScreen implements Screen {
   final ActiveGrid active;

   @Override
   public Location getLocation() {
      return this.active.getLocation();
   }

   @Override
   public boolean hasViewer() {
      return this.active.isDisplaying();
   }

   @Override
   public boolean isAlive() {
      return StorageCacheUtils.getSfItem(this.getLocation()) instanceof VisualGrid;
   }

   @Override
   public void setItem(int slot, ItemStack stack) {
      this.active.setItem(slot, stack);
   }

   @Override
   public void setItem(int slot, ItemStack stack, MenuClickHandler clickHandler) {
      this.setItem(slot, stack);
      this.active
         .setClickHandler(slot, dto -> clickHandler.onClick(dto.player(), dto.slot(), dto.clicked(), new ClickAction(dto.isRightClick(), dto.isShiftClick())));
   }

   @Override
   public SlimefunItem source() {
      return ExpansionItems.VISUAL_GRID;
   }

   @Override
   public void close() {
   }

   @Override
   public void open(Player... players) {
   }

   @Generated
   public ActiveGridScreen(ActiveGrid active) {
      this.active = active;
   }

   @Generated
   public ActiveGrid getActive() {
      return this.active;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof ActiveGridScreen other)) {
         return false;
      } else if (!other.canEqual(this)) {
         return false;
      } else {
         Object this$active = this.getActive();
         Object other$active = other.getActive();
         return this$active == null ? other$active == null : this$active.equals(other$active);
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof ActiveGridScreen;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $active = this.getActive();
      return result * 59 + ($active == null ? 43 : $active.hashCode());
   }

   @Generated
   @Override
   public String toString() {
      return "ActiveGridScreen(active=" + this.getActive() + ")";
   }
}
