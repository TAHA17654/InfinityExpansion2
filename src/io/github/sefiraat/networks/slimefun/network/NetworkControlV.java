package io.github.sefiraat.networks.slimefun.network;

import com.balugaq.netex.api.enums.FeedbackType;
import com.balugaq.netex.api.helpers.Icon;
import com.balugaq.netex.api.interfaces.SoftCellBannable;
import com.gmail.nossr50.mcMMO;
import dev.sefiraat.sefilib.misc.ParticleUtils;
import dev.sefiraat.sefilib.world.LocationUtils;
import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.managers.SupportedPluginManager;
import io.github.sefiraat.networks.network.NodeDefinition;
import io.github.sefiraat.networks.network.NodeType;
import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.blocks.BlockPosition;
import io.github.thebusybiscuit.slimefun4.utils.tags.SlimefunTag;
import java.util.HashSet;
import java.util.Set;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NetworkControlV extends NetworkDirectional implements SoftCellBannable {
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
   private final Set<BlockPosition> blockCache = new HashSet<>();

   public NetworkControlV(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe, NodeType.PASTER);
      this.getSlotsToDrop().add(25);
   }

   @Override
   protected void onTick(@Nullable BlockMenu blockMenu, @NotNull Block block) {
      super.onTick(blockMenu, block);
      if (blockMenu != null) {
         this.tryPasteBlock(blockMenu);
      }
   }

   @Override
   protected void onUniqueTick() {
      this.blockCache.clear();
   }

   private void tryPasteBlock(@NotNull BlockMenu blockMenu) {
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
                     this.sendFeedback(blockMenu.getLocation(), FeedbackType.BLOCK_ALREADY_PASTED);
                  } else {
                     Material material = targetBlock.getType();
                     if (material != Material.AIR) {
                        this.sendFeedback(blockMenu.getLocation(), FeedbackType.BLOCK_CANNOT_BE_AIR);
                     } else {
                        ItemStack templateStack = blockMenu.getItemInSlot(25);
                        if (templateStack != null) {
                           Material templateMaterial = templateStack.getType();
                           if (templateMaterial.isBlock() && !SlimefunTag.SENSITIVE_MATERIALS.isTagged(templateMaterial)) {
                              SlimefunItem slimefunItem = SlimefunItem.getByItem(templateStack);
                              if (slimefunItem != null) {
                                 this.sendFeedback(blockMenu.getLocation(), FeedbackType.INVALID_TEMPLATE);
                              } else {
                                 ItemRequest request = new ItemRequest(templateStack.clone(), 1);
                                 ItemStack fetchedStack = definition.getNode().getRoot().getItemStack(blockMenu.getLocation(), request);
                                 if (fetchedStack != null && fetchedStack.getAmount() >= 1) {
                                    this.blockCache.add(targetPosition);
                                    Bukkit.getScheduler().runTask(Networks.getInstance(), bukkitTask -> {
                                       targetBlock.setType(fetchedStack.getType(), true);
                                       if (SupportedPluginManager.getInstance().isMcMMO()) {
                                          try {
                                             mcMMO.getChunkManager().setTrue(targetBlock);
                                          } catch (NoClassDefFoundError var6x) {
                                             mcMMO.getPlaceStore().setTrue(targetBlock);
                                          }
                                       }

                                       ParticleUtils.displayParticleRandomly(LocationUtils.centre(targetBlock.getLocation()), Particle.ELECTRIC_SPARK, 1.0, 5);
                                       this.sendFeedback(blockMenu.getLocation(), FeedbackType.WORKING);
                                    });
                                 } else {
                                    this.sendFeedback(blockMenu.getLocation(), FeedbackType.NOT_ENOUGH_RESOURCES);
                                 }
                              }
                           } else {
                              this.sendFeedback(blockMenu.getLocation(), FeedbackType.INVALID_TEMPLATE);
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
      return Icon.CONTROL_V_TEMPLATE_BACKGROUND_STACK;
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

   @NotNull
   @Override
   protected DustOptions getDustOptions() {
      return new DustOptions(Color.MAROON, 1.0F);
   }
}
