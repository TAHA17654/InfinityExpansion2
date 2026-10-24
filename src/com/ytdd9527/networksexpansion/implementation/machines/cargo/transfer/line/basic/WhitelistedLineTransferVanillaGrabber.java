package com.ytdd9527.networksexpansion.implementation.machines.cargo.transfer.line.basic;

import com.balugaq.netex.api.atrributes.WhitelistedVanillaGrabber;
import com.balugaq.netex.api.enums.FeedbackType;
import com.balugaq.netex.api.enums.TransferType;
import com.balugaq.netex.api.factories.TransferConfigFactory;
import com.balugaq.netex.api.interfaces.SoftCellBannable;
import com.balugaq.netex.api.transfer.TransferConfiguration;
import com.balugaq.netex.utils.Lang;
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
import io.github.thebusybiscuit.slimefun4.core.attributes.RecipeDisplayItem;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.paperlib.PaperLib;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WhitelistedLineTransferVanillaGrabber extends NetworkDirectional implements RecipeDisplayItem, SoftCellBannable, WhitelistedVanillaGrabber {
   private static final TransferConfiguration config = TransferConfigFactory.getTransferConfiguration(TransferType.WHITELISTED_LINE_TRANSFER_VANILLA_GRABBER);
   private static final int maxDistance;
   private static final int grabItemTick = config.defaultGrabTick;
   private final HashMap<Location, Integer> TICKER_MAP = new HashMap<>();

   public WhitelistedLineTransferVanillaGrabber(
      @NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe
   ) {
      super(itemGroup, item, recipeType, recipe, NodeType.LINE_TRANSFER_VANILLA_GRABBER);
   }

   @Override
   protected void onTick(@Nullable BlockMenu blockMenu, @NotNull Block block) {
      super.onTick(blockMenu, block);
      if (blockMenu == null) {
         this.sendFeedback(block.getLocation(), FeedbackType.INVALID_BLOCK);
      } else {
         Location location = blockMenu.getLocation();
         if (grabItemTick != 1) {
            int tickCounter = this.getTickCounter(location);
            tickCounter = (tickCounter + 1) % grabItemTick;
            if (tickCounter == 0) {
               this.tryGrabItem(blockMenu);
            }

            this.updateTickCounter(location, tickCounter);
         } else {
            this.tryGrabItem(blockMenu);
         }
      }
   }

   private int getTickCounter(Location location) {
      Integer ticker = this.TICKER_MAP.get(location);
      if (ticker == null) {
         this.TICKER_MAP.put(location, 0);
         return 0;
      } else {
         return ticker;
      }
   }

   private void updateTickCounter(Location location, int tickCounter) {
      this.TICKER_MAP.put(location, tickCounter);
   }

   private void tryGrabItem(@NotNull BlockMenu blockMenu) {
      NodeDefinition definition = NetworkStorage.getNode(blockMenu.getLocation());
      if (definition != null && definition.getNode() != null) {
         NetworkRoot root = definition.getNode().getRoot();
         if (!this.checkSoftCellBan(blockMenu.getLocation(), root)) {
            BlockFace direction = this.getCurrentDirection(blockMenu);
            Block block = blockMenu.getBlock();
            Block targetBlock = block.getRelative(direction);

            for (int d = 0; d <= maxDistance; d++) {
               if (StorageCacheUtils.getMenu(targetBlock.getLocation()) != null) {
                  targetBlock = targetBlock.getRelative(direction);
               } else {
                  BlockState blockState = PaperLib.getBlockState(targetBlock, false).getState();
                  if (!(blockState instanceof InventoryHolder)) {
                     this.sendFeedback(blockMenu.getLocation(), FeedbackType.NO_INVENTORY_FOUND);
                     return;
                  }

                  InventoryHolder holder = (InventoryHolder)blockState;
                  boolean wildChests = Networks.getSupportedPluginManager().isWildChests();
                  boolean isChest = wildChests && WildChestsAPI.getChest(targetBlock.getLocation()) != null;
                  if (wildChests && isChest) {
                     this.sendFeedback(blockMenu.getLocation(), FeedbackType.PROTECTED_BLOCK);
                  } else {
                     List<ItemStack> templates = this.getClonedTemplateItems(blockMenu);
                     Inventory inventory = holder.getInventory();
                     this.grabInventory(blockMenu, blockState, inventory, root, templates);
                     targetBlock = targetBlock.getRelative(direction);
                  }
               }
            }

            this.sendFeedback(blockMenu.getLocation(), FeedbackType.WORKING);
         }
      } else {
         this.sendFeedback(blockMenu.getLocation(), FeedbackType.NO_NETWORK_FOUND);
      }
   }

   @Override
   public boolean runSync() {
      return true;
   }

   @NotNull
   @Override
   protected DustOptions getDustOptions() {
      return new DustOptions(Color.MAROON, 1.0F);
   }

   @NotNull
   public List<ItemStack> getDisplayRecipes() {
      List<ItemStack> displayRecipes = new ArrayList<>(6);
      displayRecipes.add(
         new CustomItemStack(
            Material.BOOK,
            Lang.getString("icons.mechanism.transfers.data_title"),
            new String[]{
               "",
               String.format(Lang.getString("icons.mechanism.transfers.max_distance"), maxDistance),
               String.format(Lang.getString("icons.mechanism.transfers.grab_item_tick"), grabItemTick)
            }
         )
      );
      return displayRecipes;
   }

   @Override
   public int[] getTemplateSlots() {
      return config.getTemplateSlots();
   }

   static {
      maxDistance = config.maxDistance;
   }
}
