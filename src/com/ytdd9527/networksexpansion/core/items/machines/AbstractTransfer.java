package com.ytdd9527.networksexpansion.core.items.machines;

import com.balugaq.netex.api.enums.FeedbackType;
import com.balugaq.netex.api.enums.TransferType;
import com.balugaq.netex.api.enums.TransportMode;
import com.balugaq.netex.api.factories.TransferConfigFactory;
import com.balugaq.netex.api.helpers.Icon;
import com.balugaq.netex.api.interfaces.GrabTickOnly;
import com.balugaq.netex.api.interfaces.PushTickOnly;
import com.balugaq.netex.api.interfaces.SoftCellBannable;
import com.balugaq.netex.api.interfaces.VanillaTransfer;
import com.balugaq.netex.api.transfer.TransferConfiguration;
import com.balugaq.netex.utils.LineOperationUtil;
import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.NodeDefinition;
import io.github.sefiraat.networks.network.NodeType;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.RecipeDisplayItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.ParametersAreNonnullByDefault;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractTransfer extends AdvancedDirectional implements RecipeDisplayItem {
   private static final Map<Location, Integer> PUSH_TICKER_MAP = new HashMap<>();
   private static final Map<Location, Integer> GRAB_TICKER_MAP = new HashMap<>();
   private final TransferConfiguration config;

   protected AbstractTransfer(
      @NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe, NodeType type
   ) {
      this(itemGroup, item, recipeType, recipe, 1, type);
   }

   protected AbstractTransfer(
      @NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, ItemStack[] recipe, int outputAmount, NodeType type
   ) {
      super(itemGroup, item, recipeType, recipe, type);
      this.config = TransferConfigFactory.getTransferConfiguration(this.getTransferType(), this.checkPlus(item.getItemId()));
   }

   public String checkPlus(String id) {
      return id.contains("PLUS") ? id : null;
   }

   public abstract TransferType getTransferType();

   @Override
   public void postPlace(@NotNull BlockPlaceEvent e) {
      for (int slot : this.config.templateSlots) {
         this.getSlotsToDrop().add(slot);
      }
   }

   @Override
   public boolean isExceedLimit(int quantity) {
      return quantity > this.config.maxTransportLimit;
   }

   @Override
   public int getMaxLimit() {
      return this.config.maxTransportLimit;
   }

   @Override
   protected void onTick(@Nullable BlockMenu blockMenu, @NotNull Block block) {
      super.onTick(blockMenu, block);
      Location location = block.getLocation();
      this.sendFeedback(location, FeedbackType.TRANSFER_TICKING);
      if (blockMenu == null) {
         this.sendFeedback(block.getLocation(), FeedbackType.INVALID_BLOCK);
      } else {
         NodeDefinition definition = NetworkStorage.getNode(blockMenu.getLocation());
         if (definition != null && definition.getNode() != null) {
            BlockFace direction = this.getCurrentDirection(blockMenu);
            if (direction == BlockFace.SELF) {
               this.sendFeedback(blockMenu.getLocation(), FeedbackType.NO_DIRECTION_SET);
            } else {
               NetworkRoot root = definition.getNode().getRoot();
               if (this instanceof SoftCellBannable scb && scb.checkSoftCellBan(blockMenu.getLocation(), root)) {
                  return;
               }

               TransportMode currentTransportMode = this.getCurrentTransportMode(blockMenu.getLocation());
               int limitQuantity = this.getLimitQuantity(blockMenu.getLocation());
               if (!(this instanceof GrabTickOnly)) {
                  if (this.config.defaultPushTick > 1) {
                     this.sendFeedback(location, FeedbackType.TRANSFER_TRY_PUSH_ITEM_WITH_COUNTER);
                     int currentPushTick = this.getPushTickCounter(location);
                     if (currentPushTick == 0) {
                        if (this instanceof VanillaTransfer) {
                           this.tryVanillaPushItem(blockMenu, root, direction, currentTransportMode, limitQuantity);
                        } else {
                           this.tryPushItem(blockMenu, root, direction, currentTransportMode, limitQuantity);
                        }
                     }

                     currentPushTick = (currentPushTick + 1) % this.config.defaultPushTick;
                     this.updatePushTickCounter(location, currentPushTick);
                  } else {
                     this.sendFeedback(location, FeedbackType.TRANSFER_TRY_PUSH_ITEM);
                     if (this instanceof VanillaTransfer) {
                        this.tryVanillaPushItem(blockMenu, root, direction, currentTransportMode, limitQuantity);
                     } else {
                        this.tryPushItem(blockMenu, root, direction, currentTransportMode, limitQuantity);
                     }
                  }
               }

               if (!(this instanceof PushTickOnly)) {
                  if (this.config.defaultGrabTick > 1) {
                     this.sendFeedback(location, FeedbackType.TRANSFER_TRY_GRAB_ITEM_WITH_COUNTER);
                     int currentGrabTick = this.getGrabTickCounter(location);
                     if (currentGrabTick == 0) {
                        if (this instanceof VanillaTransfer) {
                           this.tryVanillaGrabItem(blockMenu, root, direction, currentTransportMode, limitQuantity);
                        } else {
                           this.tryGrabItem(blockMenu, root, direction, currentTransportMode, limitQuantity);
                        }
                     }

                     currentGrabTick = (currentGrabTick + 1) % this.config.defaultGrabTick;
                     this.updateGrabTickCounter(location, currentGrabTick);
                  } else {
                     this.sendFeedback(location, FeedbackType.TRANSFER_TRY_GRAB_ITEM);
                     if (this instanceof VanillaTransfer) {
                        this.tryVanillaGrabItem(blockMenu, root, direction, currentTransportMode, limitQuantity);
                     } else {
                        this.tryGrabItem(blockMenu, root, direction, currentTransportMode, limitQuantity);
                     }
                  }
               }
            }
         } else {
            this.sendFeedback(blockMenu.getLocation(), FeedbackType.NO_NETWORK_FOUND);
         }
      }
   }

   private int getPushTickCounter(Location location) {
      Integer ticker = PUSH_TICKER_MAP.get(location);
      if (ticker != null) {
         return ticker;
      } else {
         PUSH_TICKER_MAP.put(location, 0);
         return 0;
      }
   }

   private int getGrabTickCounter(Location location) {
      Integer ticker = GRAB_TICKER_MAP.get(location);
      if (ticker != null) {
         return ticker;
      } else {
         GRAB_TICKER_MAP.put(location, 0);
         return 0;
      }
   }

   private void updatePushTickCounter(Location location, int pushTick) {
      PUSH_TICKER_MAP.put(location, pushTick);
   }

   private void updateGrabTickCounter(Location location, int grabTick) {
      GRAB_TICKER_MAP.put(location, grabTick);
   }

   private void tryPushItem(
      @NotNull BlockMenu blockMenu, @NotNull NetworkRoot root, @NotNull BlockFace direction, @NotNull TransportMode mode, int limitQuantity
   ) {
      if (root.getRootPower() < (long)this.config.defaultRequiredPower) {
         this.sendFeedback(blockMenu.getLocation(), FeedbackType.NOT_ENOUGH_POWER);
      } else {
         List<ItemStack> templates = new ArrayList<>();

         for (int slot : this.getItemSlots()) {
            templates.add(blockMenu.getItemInSlot(slot));
         }

         LineOperationUtil.doOperation(
            blockMenu.getLocation(),
            direction,
            this.config.maxDistance,
            false,
            false,
            targetMenu -> LineOperationUtil.pushItem(targetMenu.getLocation(), root, targetMenu, templates, mode, limitQuantity)
         );
         root.removeRootPower((long)this.config.defaultRequiredPower);
         this.sendFeedback(blockMenu.getLocation(), FeedbackType.WORKING);
      }
   }

   @ParametersAreNonnullByDefault
   private void tryGrabItem(BlockMenu blockMenu, NetworkRoot root, BlockFace direction, TransportMode mode, int limitQuantity) {
      if (root.getRootPower() < (long)this.config.defaultRequiredPower) {
         this.sendFeedback(blockMenu.getLocation(), FeedbackType.NOT_ENOUGH_POWER);
      } else {
         LineOperationUtil.doOperation(
            blockMenu.getLocation(),
            direction,
            this.config.maxDistance,
            false,
            false,
            targetMenu -> LineOperationUtil.grabItem(targetMenu.getLocation(), root, targetMenu, mode, limitQuantity)
         );
         root.removeRootPower((long)this.config.defaultRequiredPower);
      }
   }

   private void tryVanillaPushItem(
      @NotNull BlockMenu blockMenu, @NotNull NetworkRoot root, @NotNull BlockFace direction, @NotNull TransportMode mode, int limitQuantity
   ) {
      if (root.getRootPower() < (long)this.config.defaultRequiredPower) {
         this.sendFeedback(blockMenu.getLocation(), FeedbackType.NOT_ENOUGH_POWER);
      } else {
         List<ItemStack> templates = new ArrayList<>();

         for (int slot : this.getItemSlots()) {
            templates.add(blockMenu.getItemInSlot(slot));
         }

         LineOperationUtil.doVanillaOperation(
            blockMenu.getLocation(),
            direction,
            this.config.maxDistance,
            false,
            false,
            menu -> LineOperationUtil.pushItem(
                  menu.getLocation() == null ? blockMenu.getLocation() : menu.getLocation(), root, menu, templates, mode, limitQuantity
               )
         );
         root.removeRootPower((long)this.config.defaultRequiredPower);
         this.sendFeedback(blockMenu.getLocation(), FeedbackType.WORKING);
      }
   }

   @ParametersAreNonnullByDefault
   private void tryVanillaGrabItem(BlockMenu blockMenu, NetworkRoot root, BlockFace direction, TransportMode mode, int limitQuantity) {
      if (root.getRootPower() < (long)this.config.defaultRequiredPower) {
         this.sendFeedback(blockMenu.getLocation(), FeedbackType.NOT_ENOUGH_POWER);
      } else {
         LineOperationUtil.doVanillaOperation(
            blockMenu.getLocation(),
            direction,
            this.config.maxDistance,
            false,
            false,
            menu -> LineOperationUtil.grabItem(menu.getLocation() == null ? blockMenu.getLocation() : menu.getLocation(), root, menu, mode, limitQuantity)
         );
         root.removeRootPower((long)this.config.defaultRequiredPower);
      }
   }

   @NotNull
   @Override
   protected int[] getBackgroundSlots() {
      return this.config.backgroundSlots;
   }

   @Nullable
   @Override
   protected int[] getOtherBackgroundSlots() {
      return this.config.templateBackgroundSlots;
   }

   @Nullable
   @Override
   protected ItemStack getOtherBackgroundStack() {
      return Icon.PUSHER_TEMPLATE_BACKGROUND_STACK;
   }

   @Override
   public int getNorthSlot() {
      return this.config.northSlot;
   }

   @Override
   public int getSouthSlot() {
      return this.config.southSlot;
   }

   @Override
   public int getEastSlot() {
      return this.config.eastSlot;
   }

   @Override
   public int getWestSlot() {
      return this.config.westSlot;
   }

   @Override
   public int getUpSlot() {
      return this.config.upSlot;
   }

   @Override
   public int getDownSlot() {
      return this.config.downSlot;
   }

   @NotNull
   @Override
   public int[] getItemSlots() {
      return this.config.templateSlots;
   }

   @NotNull
   public List<ItemStack> getDisplayRecipes() {
      List<ItemStack> displayRecipes = new ArrayList<>(6);
      displayRecipes.add(this.config.getInformationIcon());
      return displayRecipes;
   }

   @Override
   protected int getMinusSlot() {
      return this.config.minusSlot;
   }

   @Override
   protected int getCargoNumberSlot() {
      return this.config.cargoNumberSlot;
   }

   @Override
   protected int getAddSlot() {
      return this.config.addSlot;
   }

   @Override
   protected int getTransportModeSlot() {
      return this.config.transportModeSlot;
   }
}
