package com.balugaq.bim.events;

import com.balugaq.bim.grid.ScrollResult;
import lombok.Generated;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class PlayerScrollGridEvent extends PlayerEvent implements Cancellable {
   private static final HandlerList HANDLERS = new HandlerList();
   private final ScrollResult result;
   private final int delta;
   private boolean cancelled;

   public PlayerScrollGridEvent(Player p, ScrollResult result, int delta) {
      super(p);
      this.result = result;
      this.delta = delta;
   }

   public HandlerList getHandlers() {
      return HANDLERS;
   }

   public static HandlerList getHandlerList() {
      return HANDLERS;
   }

   @Generated
   public ScrollResult getResult() {
      return this.result;
   }

   @Generated
   public int getDelta() {
      return this.delta;
   }

   @Generated
   public boolean isCancelled() {
      return this.cancelled;
   }

   @Generated
   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }
}
