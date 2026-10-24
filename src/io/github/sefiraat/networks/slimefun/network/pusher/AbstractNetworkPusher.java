package io.github.sefiraat.networks.slimefun.network.pusher;

import com.balugaq.netex.api.enums.FeedbackType;
import com.balugaq.netex.api.helpers.Icon;
import com.balugaq.netex.api.interfaces.SoftCellBannable;
import com.balugaq.netex.utils.BlockMenuUtil;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.network.NodeDefinition;
import io.github.sefiraat.networks.network.NodeType;
import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import io.github.sefiraat.networks.slimefun.network.NetworkDirectional;
import io.github.sefiraat.networks.utils.StackUtils;
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

public abstract class AbstractNetworkPusher extends NetworkDirectional implements SoftCellBannable {
   private static final int NORTH_SLOT = 11;
   private static final int SOUTH_SLOT = 29;
   private static final int EAST_SLOT = 21;
   private static final int WEST_SLOT = 19;
   private static final int UP_SLOT = 14;
   private static final int DOWN_SLOT = 32;

   public AbstractNetworkPusher(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe, NodeType.PUSHER);

      for (int slot : this.getItemSlots()) {
         this.getSlotsToDrop().add(slot);
      }
   }

   @Override
   protected void onTick(@Nullable BlockMenu blockMenu, @NotNull Block block) {
      super.onTick(blockMenu, block);
      if (blockMenu != null) {
         this.tryPushItem(blockMenu);
      }
   }

   private void tryPushItem(@NotNull BlockMenu blockMenu) {
      NodeDefinition definition = NetworkStorage.getNode(blockMenu.getLocation());
      if (definition != null && definition.getNode() != null) {
         if (!this.checkSoftCellBan(blockMenu.getLocation(), definition.getNode().getRoot())) {
            BlockFace direction = this.getCurrentDirection(blockMenu);
            BlockMenu targetMenu = StorageCacheUtils.getMenu(blockMenu.getBlock().getRelative(direction).getLocation());
            if (targetMenu == null) {
               this.sendFeedback(blockMenu.getLocation(), FeedbackType.NO_TARGET_BLOCK);
            } else {
               label57:
               for (int itemSlot : this.getItemSlots()) {
                  ItemStack testItem = blockMenu.getItemInSlot(itemSlot);
                  if (testItem != null && testItem.getType() != Material.AIR) {
                     ItemStack clone = testItem.clone();
                     clone.setAmount(1);
                     ItemRequest itemRequest = new ItemRequest(clone, clone.getMaxStackSize());
                     int[] slots = targetMenu.getPreset().getSlotsAccessedByItemTransport(targetMenu, ItemTransportFlow.INSERT, clone);
                     int[] var13 = slots;
                     int var14 = slots.length;
                     int var15 = 0;

                     while (true) {
                        if (var15 >= var14) {
                           continue label57;
                        }

                        int slot = var13[var15];
                        ItemStack itemStack = targetMenu.getItemInSlot(slot);
                        if (itemStack == null || itemStack.getType() == Material.AIR) {
                           break;
                        }

                        int space = itemStack.getMaxStackSize() - itemStack.getAmount();
                        if (space > 0 && StackUtils.itemsMatch(itemRequest, itemStack)) {
                           itemRequest.setAmount(space);
                           break;
                        }

                        var15++;
                     }

                     ItemStack retrieved = definition.getNode().getRoot().getItemStack(blockMenu.getLocation(), itemRequest);
                     if (retrieved != null) {
                        BlockMenuUtil.pushItem(targetMenu, retrieved, slots);
                        this.sendFeedback(blockMenu.getLocation(), FeedbackType.WORKING);
                        if (definition.getNode().getRoot().isDisplayParticles()) {
                           this.showParticle(blockMenu.getLocation(), direction);
                        }
                     } else {
                        this.sendFeedback(blockMenu.getLocation(), FeedbackType.NO_ITEM_FOUND);
                     }
                  }
               }
            }
         }
      } else {
         this.sendFeedback(blockMenu.getLocation(), FeedbackType.NO_NETWORK_FOUND);
      }
   }

   @Override
   public int getNorthSlot() {
      return 11;
   }

   @Override
   public int getSouthSlot() {
      return 29;
   }

   @Override
   public int getEastSlot() {
      return 21;
   }

   @Override
   public int getWestSlot() {
      return 19;
   }

   @Override
   public int getUpSlot() {
      return 14;
   }

   @Override
   public int getDownSlot() {
      return 32;
   }

   @NotNull
   @Override
   protected DustOptions getDustOptions() {
      return new DustOptions(Color.MAROON, 1.0F);
   }

   @Nullable
   @Override
   protected ItemStack getOtherBackgroundStack() {
      return Icon.PUSHER_TEMPLATE_BACKGROUND_STACK;
   }

   @NotNull
   @Override
   public abstract int[] getBackgroundSlots();

   @NotNull
   @Override
   public abstract int[] getOtherBackgroundSlots();

   @NotNull
   @Override
   public abstract int[] getItemSlots();
}
