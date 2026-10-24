package io.github.sefiraat.networks.slimefun.network;

import io.github.sefiraat.networks.network.NodeType;
import io.github.sefiraat.networks.slimefun.NetworkSlimefunItems;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class NetworkCell extends NetworkObject {
   public static final List<Integer> SLOTS = new ArrayList<>();
   private static final String KEY_UUID = "display-uuid";
   private boolean useSpecialModel = false;

   public NetworkCell(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe, NodeType.CELL);

      for (int slot : SLOTS) {
         this.getSlotsToDrop().add(slot);
      }
   }

   public void postRegister() {
      new BlockMenuPreset(this.getId(), this.getItemName()) {
         public void init() {
            this.setSize(54);
         }

         public boolean canOpen(@NotNull Block block, @NotNull Player player) {
            return player.hasPermission("slimefun.inventory.bypass")
               || NetworkSlimefunItems.NETWORK_CELL.canUse(player, false)
                  && Slimefun.getProtectionManager().hasPermission(player, block.getLocation(), Interaction.INTERACT_BLOCK);
         }

         public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
            return new int[0];
         }
      };
   }

   @Generated
   public void setUseSpecialModel(boolean useSpecialModel) {
      this.useSpecialModel = useSpecialModel;
   }

   static {
      for (int i = 0; i < 54; i++) {
         SLOTS.add(i);
      }
   }
}
