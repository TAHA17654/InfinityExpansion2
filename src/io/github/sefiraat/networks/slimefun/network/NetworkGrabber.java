package io.github.sefiraat.networks.slimefun.network;

import com.balugaq.netex.api.enums.FeedbackType;
import com.balugaq.netex.api.interfaces.SoftCellBannable;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.network.NodeDefinition;
import io.github.sefiraat.networks.network.NodeType;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NetworkGrabber extends NetworkDirectional implements SoftCellBannable {
   public NetworkGrabber(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe, NodeType.GRABBER);
   }

   @Override
   protected void onTick(@Nullable BlockMenu blockMenu, @NotNull Block block) {
      super.onTick(blockMenu, block);
      if (blockMenu != null) {
         this.tryGrabItem(blockMenu);
      }
   }

   private void tryGrabItem(@NotNull BlockMenu blockMenu) {
      NodeDefinition definition = NetworkStorage.getNode(blockMenu.getLocation());
      if (definition != null && definition.getNode() != null) {
         if (!this.checkSoftCellBan(blockMenu.getLocation(), definition.getNode().getRoot())) {
            BlockFace direction = this.getCurrentDirection(blockMenu);
            BlockMenu targetMenu = StorageCacheUtils.getMenu(blockMenu.getBlock().getRelative(direction).getLocation());
            if (targetMenu == null) {
               this.sendFeedback(blockMenu.getLocation(), FeedbackType.NO_TARGET_BLOCK);
            } else {
               int[] slots = targetMenu.getPreset().getSlotsAccessedByItemTransport(targetMenu, ItemTransportFlow.WITHDRAW, null);

               for (int slot : slots) {
                  ItemStack itemStack = targetMenu.getItemInSlot(slot);
                  if (itemStack != null && itemStack.getType() != Material.AIR) {
                     int before = itemStack.getAmount();
                     definition.getNode().getRoot().addItemStack(blockMenu.getLocation(), itemStack);
                     this.sendFeedback(blockMenu.getLocation(), FeedbackType.WORKING);
                     if (definition.getNode().getRoot().isDisplayParticles() && itemStack.getAmount() < before) {
                        this.showParticle(blockMenu.getLocation(), direction);
                     }
                     break;
                  }
               }
            }
         }
      } else {
         this.sendFeedback(blockMenu.getLocation(), FeedbackType.NO_NETWORK_FOUND);
      }
   }

   @NotNull
   @Override
   protected DustOptions getDustOptions() {
      return new DustOptions(Color.FUCHSIA, 1.0F);
   }
}
