package com.balugaq.netex.api.events;

import io.github.sefiraat.networks.network.NetworkRoot;
import lombok.Generated;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class NetworkRootReadyEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private final NetworkRoot root;

   public NetworkRootReadyEvent(NetworkRoot root) {
      super(true);
      this.root = root;
   }

   @NotNull
   public static HandlerList getHandlerList() {
      return handlers;
   }

   @NotNull
   public HandlerList getHandlers() {
      return handlers;
   }

   @Generated
   public NetworkRoot getRoot() {
      return this.root;
   }
}
