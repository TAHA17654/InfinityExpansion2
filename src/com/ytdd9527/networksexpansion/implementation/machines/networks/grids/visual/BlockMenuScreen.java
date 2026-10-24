package com.ytdd9527.networksexpansion.implementation.machines.networks.grids.visual;

import com.balugaq.netex.api.visual.Screen;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import lombok.Generated;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu.MenuClickHandler;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class BlockMenuScreen implements Screen {
   final BlockMenu blockMenu;

   @Override
   public Location getLocation() {
      return this.blockMenu.getLocation();
   }

   @Override
   public boolean hasViewer() {
      return this.blockMenu.hasViewer();
   }

   @Override
   public boolean isAlive() {
      return StorageCacheUtils.getMenu(this.getLocation()) == this.blockMenu;
   }

   @Override
   public void setItem(int slot, ItemStack stack) {
      this.blockMenu.replaceExistingItem(slot, stack);
   }

   @Override
   public void setItem(int slot, ItemStack stack, MenuClickHandler clickHandler) {
      this.setItem(slot, stack);
      this.blockMenu.addMenuClickHandler(slot, clickHandler);
   }

   @Override
   public SlimefunItem source() {
      return this.blockMenu.getPreset().getSlimefunItem();
   }

   @Override
   public void close() {
      this.blockMenu.close();
   }

   @Override
   public void open(Player... players) {
      this.blockMenu.open(players);
   }

   @Generated
   public BlockMenuScreen(BlockMenu blockMenu) {
      this.blockMenu = blockMenu;
   }

   @Generated
   public BlockMenu getBlockMenu() {
      return this.blockMenu;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof BlockMenuScreen other)) {
         return false;
      } else if (!other.canEqual(this)) {
         return false;
      } else {
         Object this$blockMenu = this.getBlockMenu();
         Object other$blockMenu = other.getBlockMenu();
         return this$blockMenu == null ? other$blockMenu == null : this$blockMenu.equals(other$blockMenu);
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof BlockMenuScreen;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $blockMenu = this.getBlockMenu();
      return result * 59 + ($blockMenu == null ? 43 : $blockMenu.hashCode());
   }

   @Generated
   @Override
   public String toString() {
      return "BlockMenuScreen(blockMenu=" + this.getBlockMenu() + ")";
   }
}
