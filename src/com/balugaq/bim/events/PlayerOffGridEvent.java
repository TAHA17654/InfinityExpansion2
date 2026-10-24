package com.balugaq.bim.events;

import com.balugaq.bim.grid.ActiveGrid;
import lombok.Generated;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class PlayerOffGridEvent extends PlayerEvent {
   private static final HandlerList HANDLERS = new HandlerList();
   private final ActiveGrid grid;

   public PlayerOffGridEvent(Player p, ActiveGrid active) {
      super(p);
      this.grid = active;
   }

   public HandlerList getHandlers() {
      return HANDLERS;
   }

   public static HandlerList getHandlerList() {
      return HANDLERS;
   }

   @Generated
   public ActiveGrid getGrid() {
      return this.grid;
   }
}
