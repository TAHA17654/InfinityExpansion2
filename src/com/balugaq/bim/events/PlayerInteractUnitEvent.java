package com.balugaq.bim.events;

import com.balugaq.bim.grid.ClickDTO;
import lombok.Generated;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class PlayerInteractUnitEvent extends PlayerEvent implements Cancellable {
   private static final HandlerList HANDLERS = new HandlerList();
   private final ClickDTO dto;
   private boolean cancelled;

   public PlayerInteractUnitEvent(ClickDTO dto) {
      super(dto.player());
      this.dto = dto;
   }

   public HandlerList getHandlers() {
      return HANDLERS;
   }

   public static HandlerList getHandlerList() {
      return HANDLERS;
   }

   @Generated
   public ClickDTO getDto() {
      return this.dto;
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
