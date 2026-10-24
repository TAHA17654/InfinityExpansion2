package com.ytdd9527.networksexpansion.implementation.machines.cargo.transfer.line.basic;

import com.balugaq.netex.api.atrributes.WhitelistedGrabber;
import com.balugaq.netex.api.enums.FeedbackType;
import com.balugaq.netex.api.enums.TransferType;
import com.balugaq.netex.api.factories.TransferConfigFactory;
import com.balugaq.netex.api.helpers.Icon;
import com.balugaq.netex.api.interfaces.SoftCellBannable;
import com.balugaq.netex.api.transfer.TransferConfiguration;
import com.balugaq.netex.utils.Lang;
import com.balugaq.netex.utils.LineOperationUtil;
import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.NodeDefinition;
import io.github.sefiraat.networks.network.NodeType;
import io.github.sefiraat.networks.slimefun.network.NetworkDirectional;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.RecipeDisplayItem;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
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
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WhitelistedLineTransferGrabber extends NetworkDirectional implements RecipeDisplayItem, SoftCellBannable, WhitelistedGrabber {
   private static final TransferConfiguration config = TransferConfigFactory.getTransferConfiguration(TransferType.WHITELISTED_LINE_TRANSFER_GRABBER);
   private static final int maxDistance;
   private static final int grabItemTick = config.defaultGrabTick;
   private final HashMap<Location, Integer> TICKER_MAP = new HashMap<>();

   public WhitelistedLineTransferGrabber(
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
         BlockFace direction = this.getCurrentDirection(blockMenu);
         if (direction == BlockFace.SELF) {
            this.sendFeedback(blockMenu.getLocation(), FeedbackType.NO_DIRECTION_SET);
         } else {
            NetworkRoot root = definition.getNode().getRoot();
            if (!this.checkSoftCellBan(blockMenu.getLocation(), root)) {
               List<ItemStack> templates = this.getClonedTemplateItems(blockMenu);
               LineOperationUtil.doOperation(
                  blockMenu.getLocation(), direction, config.maxDistance, false, false, targetMenu -> this.grabMenu(blockMenu, targetMenu, root, templates)
               );
               this.sendFeedback(blockMenu.getLocation(), FeedbackType.WORKING);
            }
         }
      } else {
         this.sendFeedback(blockMenu.getLocation(), FeedbackType.NO_NETWORK_FOUND);
      }
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

   static {
      maxDistance = config.maxDistance;
   }
}
