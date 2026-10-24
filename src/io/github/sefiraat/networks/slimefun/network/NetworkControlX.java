package io.github.sefiraat.networks.slimefun.network;

import com.balugaq.netex.api.enums.FeedbackType;
import com.balugaq.netex.api.helpers.Icon;
import com.balugaq.netex.api.interfaces.SoftCellBannable;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import dev.sefiraat.sefilib.misc.ParticleUtils;
import dev.sefiraat.sefilib.world.LocationUtils;
import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.network.NodeDefinition;
import io.github.sefiraat.networks.network.NodeType;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.blocks.BlockPosition;
import io.github.thebusybiscuit.slimefun4.libraries.paperlib.PaperLib;
import io.github.thebusybiscuit.slimefun4.libraries.paperlib.features.blockstatesnapshot.BlockStateSnapshotResult;
import io.github.thebusybiscuit.slimefun4.utils.tags.SlimefunTag;
import java.util.HashSet;
import java.util.Set;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NetworkControlX extends NetworkDirectional implements SoftCellBannable {
   private static final int[] BACKGROUND_SLOTS = new int[]{
      0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 13, 15, 17, 18, 20, 22, 23, 24, 26, 27, 28, 30, 31, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44
   };
   private static final int[] TEMPLATE_BACKGROUND = new int[]{16};
   private static final int TEMPLATE_SLOT = 25;
   private static final int NORTH_SLOT = 11;
   private static final int SOUTH_SLOT = 29;
   private static final int EAST_SLOT = 21;
   private static final int WEST_SLOT = 19;
   private static final int UP_SLOT = 14;
   private static final int DOWN_SLOT = 32;
   private static final int REQUIRED_POWER = 100;
   private static final DustOptions DUST_OPTIONS = new DustOptions(Color.GRAY, 1.0F);
   private final Set<BlockPosition> blockCache = new HashSet<>();

   public NetworkControlX(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe, NodeType.CUTTER);
   }

   @Override
   protected void onTick(@Nullable BlockMenu blockMenu, @NotNull Block block) {
      super.onTick(blockMenu, block);
      if (blockMenu != null) {
         this.tryBreakBlock(blockMenu);
      }
   }

   @Override
   protected void onUniqueTick() {
      this.blockCache.clear();
   }

   private void tryBreakBlock(@NotNull BlockMenu blockMenu) {
      NodeDefinition definition = NetworkStorage.getNode(blockMenu.getLocation());
      if (definition != null && definition.getNode() != null) {
         if (!this.checkSoftCellBan(blockMenu.getLocation(), definition.getNode().getRoot())) {
            if (definition.getNode().getRoot().getRootPower() < 100L) {
               this.sendFeedback(blockMenu.getLocation(), FeedbackType.NOT_ENOUGH_POWER);
            } else {
               BlockFace direction = this.getCurrentDirection(blockMenu);
               if (direction == BlockFace.SELF) {
                  this.sendFeedback(blockMenu.getLocation(), FeedbackType.NO_DIRECTION_SET);
               } else {
                  Block targetBlock = blockMenu.getBlock().getRelative(direction);
                  BlockPosition targetPosition = new BlockPosition(targetBlock);
                  if (this.blockCache.contains(targetPosition)) {
                     this.sendFeedback(blockMenu.getLocation(), FeedbackType.BLOCK_ALREADY_CUT);
                  } else {
                     Material material = targetBlock.getType();
                     if (material.getHardness() < 0.0F || material.isAir() || !material.isItem()) {
                        this.sendFeedback(blockMenu.getLocation(), FeedbackType.BLOCK_CANNOT_BE_CUT);
                     } else if (SlimefunTag.CARGO_SUPPORTED_STORAGE_BLOCKS.isTagged(material)) {
                        this.sendFeedback(blockMenu.getLocation(), FeedbackType.BLOCK_CANNOT_BE_CUT);
                     } else {
                        SlimefunItem slimefunItem = StorageCacheUtils.getSfItem(targetBlock.getLocation());
                        if (slimefunItem != null) {
                           this.sendFeedback(blockMenu.getLocation(), FeedbackType.BLOCK_CANNOT_BE_CUT);
                        } else {
                           ItemStack templateStack = blockMenu.getItemInSlot(25);
                           boolean mustMatch = templateStack != null && templateStack.getType() != Material.AIR;
                           if ((!mustMatch || targetBlock.getType() == templateStack.getType()) && SlimefunItem.getByItem(templateStack) == null) {
                              Bukkit.getScheduler().runTask(Networks.getInstance(), bukkitTask -> {
                                 ItemStack resultStack = new ItemStack(material, 1);
                                 definition.getNode().getRoot().addItemStack(blockMenu.getLocation(), resultStack);
                                 if (resultStack.getAmount() == 0) {
                                    this.blockCache.add(targetPosition);
                                    BlockStateSnapshotResult blockState = PaperLib.getBlockState(targetBlock, true);
                                    if (blockState.getState() instanceof InventoryHolder) {
                                       this.sendFeedback(blockMenu.getLocation(), FeedbackType.BLOCK_CANNOT_BE_CUT);
                                       return;
                                    }

                                    targetBlock.setType(Material.AIR, true);
                                    ParticleUtils.displayParticleRandomly(LocationUtils.centre(targetBlock.getLocation()), 1.0, 5, DUST_OPTIONS);
                                    definition.getNode().getRoot().removeRootPower(100L);
                                    this.sendFeedback(blockMenu.getLocation(), FeedbackType.WORKING);
                                 }
                              });
                           } else {
                              this.sendFeedback(blockMenu.getLocation(), FeedbackType.BLOCK_NOT_MATCH_TEMPLATE);
                           }
                        }
                     }
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
   protected int[] getBackgroundSlots() {
      return BACKGROUND_SLOTS;
   }

   @Nullable
   @Override
   protected int[] getOtherBackgroundSlots() {
      return TEMPLATE_BACKGROUND;
   }

   @Nullable
   @Override
   protected ItemStack getOtherBackgroundStack() {
      return Icon.CONTROL_X_TEMPLATE_BACKGROUND_STACK;
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

   @Override
   public int[] getItemSlots() {
      return new int[]{25};
   }

   @Override
   protected DustOptions getDustOptions() {
      return DUST_OPTIONS;
   }
}
