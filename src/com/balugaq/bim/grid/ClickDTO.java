package com.balugaq.bim.grid;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record ClickDTO(PlayerInteractEvent event, InteractUnit unit) {
   public Player player() {
      return this.event.getPlayer();
   }

   public int slot() {
      return this.unit.idx;
   }

   public ItemStack clicked() {
      return this.unit.itemDisplay.getItemStack();
   }

   public boolean isShiftClick() {
      return this.player().isSneaking();
   }

   public boolean isRightClick() {
      return this.event.getAction().isRightClick();
   }

   public boolean isLeftClick() {
      return this.event.getAction().isLeftClick();
   }
}
