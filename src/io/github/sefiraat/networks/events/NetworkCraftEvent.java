package io.github.sefiraat.networks.events;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import lombok.Generated;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NetworkCraftEvent extends PlayerEvent implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private final SlimefunItem machine;
   private final ItemStack[] input;
   @Nullable
   private ItemStack output;
   private boolean cancelled;

   public NetworkCraftEvent(@NotNull Player p, SlimefunItem machine, ItemStack[] input, @Nullable ItemStack output) {
      super(p);
      this.input = input;
      this.output = output;
      this.machine = machine;
   }

   @NotNull
   public static HandlerList getHandlerList() {
      return handlers;
   }

   @NotNull
   public ItemStack[] getInput() {
      return this.input;
   }

   @Nullable
   public ItemStack getOutput() {
      return this.output;
   }

   @Nullable
   public ItemStack setOutput(@Nullable ItemStack output) {
      ItemStack oldOutput = this.output;
      this.output = output;
      return oldOutput;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(boolean cancel) {
      this.cancelled = cancel;
   }

   @NotNull
   public HandlerList getHandlers() {
      return getHandlerList();
   }

   @Generated
   public SlimefunItem getMachine() {
      return this.machine;
   }
}
