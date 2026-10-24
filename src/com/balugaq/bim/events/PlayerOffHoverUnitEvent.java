package com.balugaq.bim.events;

import com.balugaq.bim.grid.InteractUnit;
import lombok.Generated;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class PlayerOffHoverUnitEvent extends PlayerEvent {
   private static final HandlerList HANDLERS = new HandlerList();
   private final InteractUnit unit;

   public PlayerOffHoverUnitEvent(Player p, InteractUnit unit) {
      super(p);
      this.unit = unit;
   }

   public HandlerList getHandlers() {
      return HANDLERS;
   }

   public static HandlerList getHandlerList() {
      return HANDLERS;
   }

   @Generated
   public InteractUnit getUnit() {
      return this.unit;
   }
}
