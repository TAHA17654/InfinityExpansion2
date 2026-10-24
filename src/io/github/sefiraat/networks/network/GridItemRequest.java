package io.github.sefiraat.networks.network;

import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import lombok.Generated;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GridItemRequest extends ItemRequest {
   private final Player player;

   public GridItemRequest(@NotNull ItemStack itemStack, int amount, Player player) {
      super(itemStack, amount);
      this.player = player;
   }

   @Generated
   public Player getPlayer() {
      return this.player;
   }
}
