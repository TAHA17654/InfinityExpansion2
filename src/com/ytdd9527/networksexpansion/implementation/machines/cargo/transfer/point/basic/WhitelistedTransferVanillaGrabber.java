package com.ytdd9527.networksexpansion.implementation.machines.cargo.transfer.point.basic;

import com.balugaq.netex.api.atrributes.WhitelistedVanillaGrabber;
import com.balugaq.netex.api.enums.FeedbackType;
import com.balugaq.netex.api.enums.TransferType;
import com.balugaq.netex.api.factories.TransferConfigFactory;
import com.balugaq.netex.api.helpers.Icon;
import com.balugaq.netex.api.interfaces.SoftCellBannable;
import com.balugaq.netex.api.transfer.TransferConfiguration;
import com.bgsoftware.wildchests.api.WildChestsAPI;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.NodeDefinition;
import io.github.sefiraat.networks.network.NodeType;
import io.github.sefiraat.networks.slimefun.network.NetworkDirectional;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.paperlib.PaperLib;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Color;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WhitelistedTransferVanillaGrabber extends NetworkDirectional implements SoftCellBannable, WhitelistedVanillaGrabber {
   private static final TransferConfiguration config = TransferConfigFactory.getTransferConfiguration(TransferType.WHITELISTED_TRANSFER_VANILLA_GRABBER);

   public WhitelistedTransferVanillaGrabber(
      @NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe
   ) {
      super(itemGroup, item, recipeType, recipe, NodeType.GRABBER);

      for (int slot : this.getItemSlots()) {
         this.getSlotsToDrop().add(slot);
      }
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
         NetworkRoot root = definition.getNode().getRoot();
         if (!this.checkSoftCellBan(blockMenu.getLocation(), root)) {
            BlockFace direction = this.getCurrentDirection(blockMenu);
            Block block = blockMenu.getBlock();
            Block targetBlock = block.getRelative(direction);
            if (StorageCacheUtils.getMenu(targetBlock.getLocation()) == null) {
               BlockState blockState = PaperLib.getBlockState(targetBlock, false).getState();
               if (!(blockState instanceof InventoryHolder holder)) {
                  this.sendFeedback(blockMenu.getLocation(), FeedbackType.NO_INVENTORY_FOUND);
               } else {
                  boolean wildChests = Networks.getSupportedPluginManager().isWildChests();
                  boolean isChest = wildChests && WildChestsAPI.getChest(targetBlock.getLocation()) != null;
                  if (wildChests && isChest) {
                     this.sendFeedback(blockMenu.getLocation(), FeedbackType.PROTECTED_BLOCK);
                  } else {
                     Inventory inventory = holder.getInventory();
                     this.grabInventory(blockMenu, blockState, inventory, root, this.getClonedTemplateItems(blockMenu));
                     this.sendFeedback(blockMenu.getLocation(), FeedbackType.WORKING);
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

   @Override
   public int getNorthSlot() {
      return config.getNorthSlot();
   }

   @Override
   public int getSouthSlot() {
      return config.getSouthSlot();
   }

   @Override
   public int getEastSlot() {
      return config.getEastSlot();
   }

   @Override
   public int getWestSlot() {
      return config.getWestSlot();
   }

   @Override
   public int getUpSlot() {
      return config.getUpSlot();
   }

   @Override
   public int getDownSlot() {
      return config.getDownSlot();
   }

   @Nullable
   @Override
   protected ItemStack getOtherBackgroundStack() {
      return Icon.GRABBER_TEMPLATE_BACKGROUND_STACK;
   }

   @NotNull
   @Override
   public int[] getBackgroundSlots() {
      return config.getBackgroundSlots();
   }

   @NotNull
   @Override
   public int[] getOtherBackgroundSlots() {
      return config.getTemplateBackgroundSlots();
   }

   @NotNull
   @Override
   public int[] getItemSlots() {
      return config.getTemplateSlots();
   }

   @Override
   public boolean runSync() {
      return true;
   }

   @Override
   public int[] getTemplateSlots() {
      return config.getTemplateSlots();
   }
}
