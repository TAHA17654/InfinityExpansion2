package com.balugaq.netex.api.events;

import lombok.Generated;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class NetworksBlockBreakEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   @NotNull
   private final Block block;
   @NotNull
   private final Player player;
   private boolean cancelled = false;

   public NetworksBlockBreakEvent(@NotNull Block theBlock, @NotNull Player player) {
      this.block = theBlock;
      this.player = player;
   }

   @NotNull
   public static HandlerList getHandlerList() {
      return handlers;
   }

   @NotNull
   public HandlerList getHandlers() {
      return handlers;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(boolean b) {
      this.cancelled = b;
   }

   @NotNull
   @Generated
   public Block getBlock() {
      return this.block;
   }

   @NotNull
   @Generated
   public Player getPlayer() {
      return this.player;
   }
}
