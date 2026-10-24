package io.github.sefiraat.networks.slimefun.network;

import com.balugaq.netex.api.enums.FeedbackType;
import com.balugaq.netex.api.enums.MinecraftVersion;
import com.balugaq.netex.api.interfaces.SoftCellBannable;
import com.balugaq.netex.utils.Lang;
import com.bgsoftware.wildchests.api.WildChestsAPI;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.network.NodeDefinition;
import io.github.sefiraat.networks.network.NodeType;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.paperlib.PaperLib;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.BrewingStand;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NetworkVanillaGrabber extends NetworkDirectional implements SoftCellBannable {
   private static final int[] BACKGROUND_SLOTS = new int[]{
      0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 13, 15, 16, 17, 18, 20, 22, 23, 24, 26, 27, 28, 30, 31, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44
   };
   private static final int OUTPUT_SLOT = 25;
   private static final int NORTH_SLOT = 11;
   private static final int SOUTH_SLOT = 29;
   private static final int EAST_SLOT = 21;
   private static final int WEST_SLOT = 19;
   private static final int UP_SLOT = 14;
   private static final int DOWN_SLOT = 32;

   public NetworkVanillaGrabber(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe, NodeType.GRABBER);
      this.getSlotsToDrop().add(25);
   }

   @Override
   protected void onTick(@Nullable BlockMenu blockMenu, @NotNull Block block) {
      super.onTick(blockMenu, block);
      if (blockMenu != null) {
         this.tryGrabItem(blockMenu);
      }
   }

   private void tryGrabItem(@NotNull BlockMenu blockMenu) {
      ItemStack itemInSlot = blockMenu.getItemInSlot(25);
      if (itemInSlot != null && itemInSlot.getType() != Material.AIR) {
         this.sendFeedback(blockMenu.getLocation(), FeedbackType.ALREADY_HAS_ITEM);
      } else {
         NodeDefinition definition = NetworkStorage.getNode(blockMenu.getLocation());
         if (definition != null && definition.getNode() != null) {
            if (!this.checkSoftCellBan(blockMenu.getLocation(), definition.getNode().getRoot())) {
               BlockFace direction = this.getCurrentDirection(blockMenu);
               Block block = blockMenu.getBlock();
               Block targetBlock = block.getRelative(direction);
               if (StorageCacheUtils.getMenu(targetBlock.getLocation()) == null) {
                  BlockState blockState = PaperLib.getBlockState(targetBlock, false).getState();
                  if (!(blockState instanceof InventoryHolder holder)) {
                     this.sendFeedback(block.getLocation(), FeedbackType.NO_INVENTORY_FOUND);
                  } else {
                     boolean wildChests = Networks.getSupportedPluginManager().isWildChests();
                     boolean isChest = wildChests && WildChestsAPI.getChest(targetBlock.getLocation()) != null;
                     this.sendDebugMessage(block.getLocation(), String.format(Lang.getString("messages.debug.wildchests"), wildChests));
                     this.sendDebugMessage(block.getLocation(), String.format(Lang.getString("messages.debug.ischest"), isChest));
                     if (wildChests && isChest) {
                        this.sendDebugMessage(block.getLocation(), Lang.getString("messages.debug.wildchests-trigger-failed"));
                        this.sendFeedback(block.getLocation(), FeedbackType.PROTECTED_BLOCK);
                     } else {
                        this.sendDebugMessage(block.getLocation(), Lang.getString("messages.debug.wildchests-trigger-success"));
                        Inventory inventory = holder.getInventory();
                        if (inventory instanceof FurnaceInventory furnaceInventory) {
                           ItemStack furnaceInventoryResult = furnaceInventory.getResult();
                           ItemStack furnaceInventoryFuel = furnaceInventory.getFuel();
                           this.grabItem(blockMenu, furnaceInventoryResult);
                           if (furnaceInventoryFuel != null && furnaceInventoryFuel.getType() == Material.BUCKET) {
                              this.grabItem(blockMenu, furnaceInventoryFuel);
                           }
                        } else if (inventory instanceof BrewerInventory brewerInventory) {
                           if (!(blockState instanceof BrewingStand brewingStand)) {
                              return;
                           }

                           if (brewingStand.getBrewingTime() > 0) {
                              return;
                           }

                           for (int i = 0; i < 3; i++) {
                              ItemStack stack = brewerInventory.getContents()[i];
                              if (stack != null && stack.getType() != Material.AIR) {
                                 ItemMeta bpd = stack.getItemMeta();
                                 if (!(bpd instanceof PotionMeta)) {
                                    this.grabItem(blockMenu, stack);
                                    break;
                                 }

                                 PotionMeta potionMeta = (PotionMeta)bpd;
                                 if (Networks.getInstance().getMCVersion().isAtLeast(MinecraftVersion.V1_20_5)) {
                                    if (potionMeta.getBasePotionType() != PotionType.WATER) {
                                       this.grabItem(blockMenu, stack);
                                       break;
                                    }
                                 } else {
                                    PotionData bpdx = potionMeta.getBasePotionData();
                                    if (bpdx != null && bpdx.getType() != PotionType.WATER) {
                                       this.grabItem(blockMenu, stack);
                                       break;
                                    }
                                 }
                              }
                           }
                        } else {
                           for (ItemStack stack : inventory.getContents()) {
                              if (this.grabItem(blockMenu, stack)) {
                                 break;
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
   }

   private boolean grabItem(@NotNull BlockMenu blockMenu, @Nullable ItemStack stack) {
      if (stack != null && stack.getType() != Material.AIR) {
         blockMenu.replaceExistingItem(25, stack.clone());
         stack.setAmount(0);
         this.sendFeedback(blockMenu.getLocation(), FeedbackType.WORKING);
         return true;
      } else {
         return false;
      }
   }

   @NotNull
   @Override
   protected int[] getBackgroundSlots() {
      return BACKGROUND_SLOTS;
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
   public boolean runSync() {
      return true;
   }

   @Override
   public int[] getOutputSlots() {
      return new int[]{25};
   }

   @NotNull
   @Override
   protected DustOptions getDustOptions() {
      return new DustOptions(Color.MAROON, 1.0F);
   }
}
