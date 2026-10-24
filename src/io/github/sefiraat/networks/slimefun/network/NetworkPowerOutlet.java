package io.github.sefiraat.networks.slimefun.network;

import com.balugaq.netex.api.enums.FeedbackType;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.NodeDefinition;
import io.github.sefiraat.networks.network.NodeType;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NetworkPowerOutlet extends NetworkDirectional {
   private final int rate;

   public NetworkPowerOutlet(
      @NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe, int rate
   ) {
      super(itemGroup, item, recipeType, recipe, NodeType.POWER_OUTLET);
      this.rate = rate;
   }

   @Override
   public void onTick(@Nullable BlockMenu menu, @NotNull Block b) {
      super.onTick(menu, b);
      if (menu == null) {
         this.sendFeedback(b.getLocation(), FeedbackType.INVALID_BLOCK);
      } else {
         NodeDefinition definition = NetworkStorage.getNode(b.getLocation());
         if (definition != null && definition.getNode() != null) {
            BlockFace blockFace = this.getCurrentDirection(menu);
            Block targetBlock = b.getRelative(blockFace);
            SlimefunBlockData blockData = StorageCacheUtils.getBlock(targetBlock.getLocation());
            if (blockData == null) {
               this.sendFeedback(menu.getLocation(), FeedbackType.INVALID_BLOCK);
            } else if (!blockData.isDataLoaded()) {
               StorageCacheUtils.requestLoad(blockData);
               this.sendFeedback(menu.getLocation(), FeedbackType.LOADING_DATA);
            } else {
               SlimefunItem slimefunItem = SlimefunItem.getById(blockData.getSfId());
               if (slimefunItem instanceof EnergyNetComponent component && !(slimefunItem instanceof NetworkObject)) {
                  String charge = blockData.getData("energy-charge");
                  int chargeInt = 0;
                  if (charge != null) {
                     chargeInt = Integer.parseInt(charge);
                  }

                  int capacity = component.getCapacity();
                  int space = capacity - chargeInt;
                  if (space <= 0) {
                     this.sendFeedback(menu.getLocation(), FeedbackType.FULL_ENERGY_BUFFER);
                     return;
                  }

                  int possibleGeneration = Math.min(this.rate, space);
                  NetworkRoot root = definition.getNode().getRoot();
                  long power = root.getRootPower();
                  if (power <= 0L) {
                     this.sendFeedback(menu.getLocation(), FeedbackType.NOT_ENOUGH_POWER);
                     return;
                  }

                  int gen = power < (long)possibleGeneration ? (int)power : possibleGeneration;
                  component.addCharge(targetBlock.getLocation(), gen);
                  root.removeRootPower((long)gen);
                  this.sendFeedback(menu.getLocation(), FeedbackType.WORKING);
                  return;
               }

               this.sendFeedback(menu.getLocation(), FeedbackType.CANNOT_OUTPUT_ENERGY);
            }
         } else {
            this.sendFeedback(menu.getLocation(), FeedbackType.NO_NETWORK_FOUND);
         }
      }
   }
}
