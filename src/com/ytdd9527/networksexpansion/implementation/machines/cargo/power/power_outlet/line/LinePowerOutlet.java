package com.ytdd9527.networksexpansion.implementation.machines.cargo.power.power_outlet.line;

import com.balugaq.netex.api.enums.FeedbackType;
import com.balugaq.netex.utils.LineOperationUtil;
import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.NodeDefinition;
import io.github.sefiraat.networks.network.NodeType;
import io.github.sefiraat.networks.slimefun.network.NetworkDirectional;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LinePowerOutlet extends NetworkDirectional {
   private static final int DEFAULT_RATE = 2000;
   private static final int DEFAULT_MAX_DISTANCE = 32;
   private int maxDistance;
   private int rate;

   public LinePowerOutlet(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe, NodeType.LINE_POWER_OUTLET);
      this.loadConfigurations();
   }

   public void loadConfigurations() {
      String configKey = this.getId();
      FileConfiguration config = Networks.getInstance().getConfig();
      this.rate = config.getInt("items." + configKey + ".rate", 2000);
      this.maxDistance = config.getInt("items." + configKey + ".max-distance", 32);
   }

   @Override
   public void onTick(@Nullable BlockMenu blockMenu, @NotNull Block b) {
      super.onTick(blockMenu, b);
      if (blockMenu == null) {
         this.sendFeedback(b.getLocation(), FeedbackType.INVALID_BLOCK);
      } else {
         this.outPower(blockMenu);
      }
   }

   private void outPower(@NotNull BlockMenu blockMenu) {
      NodeDefinition definition = NetworkStorage.getNode(blockMenu.getLocation());
      if (definition != null && definition.getNode() != null) {
         NetworkRoot root = definition.getNode().getRoot();
         BlockFace blockFace = this.getCurrentDirection(blockMenu);
         LineOperationUtil.doEnergyOperation(
            blockMenu.getLocation(), blockFace, this.maxDistance, true, false, location -> LineOperationUtil.outPower(location, root, this.rate)
         );
      } else {
         this.sendFeedback(blockMenu.getLocation(), FeedbackType.NO_NETWORK_FOUND);
      }
   }
}
