package io.github.sefiraat.networks.slimefun.network;

import com.balugaq.netex.api.helpers.Icon;
import io.github.sefiraat.networks.network.NodeType;
import io.github.sefiraat.networks.slimefun.NetworkSlimefunItems;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class NetworkGreedyBlock extends NetworkObject {
   public static final int TEMPLATE_SLOT = 10;
   public static final int INPUT_SLOT = 16;
   private static final int[] BACKGROUND_SLOTS = new int[]{3, 4, 5, 12, 13, 14, 21, 22, 23};
   private static final int[] BACKGROUND_SLOTS_TEMPLATE = new int[]{0, 1, 2, 9, 11, 18, 19, 20};
   private static final int[] BACKGROUND_SLOTS_INPUT = new int[]{6, 7, 8, 15, 17, 24, 25, 26};

   public NetworkGreedyBlock(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe, NodeType.GREEDY_BLOCK);
      this.getSlotsToDrop().add(16);
      this.getSlotsToDrop().add(10);
   }

   public void postRegister() {
      new BlockMenuPreset(this.getId(), this.getItemName()) {
         public void init() {
            this.drawBackground(NetworkGreedyBlock.BACKGROUND_SLOTS);
            this.drawBackground(Icon.GREEDY_TEMPLATE_BACKGROUND_STACK, NetworkGreedyBlock.BACKGROUND_SLOTS_TEMPLATE);
            this.drawBackground(Icon.STORAGE_BACKGROUND_STACK, NetworkGreedyBlock.BACKGROUND_SLOTS_INPUT);
         }

         public boolean canOpen(@NotNull Block block, @NotNull Player player) {
            return player.hasPermission("slimefun.inventory.bypass")
               || NetworkSlimefunItems.NETWORK_GREEDY_BLOCK.canUse(player, false)
                  && Slimefun.getProtectionManager().hasPermission(player, block.getLocation(), Interaction.INTERACT_BLOCK);
         }

         public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
            return flow == ItemTransportFlow.WITHDRAW ? new int[]{16} : new int[0];
         }
      };
   }
}
