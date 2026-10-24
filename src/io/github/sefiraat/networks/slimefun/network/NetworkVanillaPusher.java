package io.github.sefiraat.networks.slimefun.network;

import com.balugaq.netex.api.enums.FeedbackType;
import com.balugaq.netex.api.enums.MinecraftVersion;
import com.balugaq.netex.api.interfaces.SoftCellBannable;
import com.balugaq.netex.utils.InventoryUtil;
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
import io.github.thebusybiscuit.slimefun4.libraries.dough.inventory.InvUtils;
import io.github.thebusybiscuit.slimefun4.libraries.paperlib.PaperLib;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.CrafterInventory;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NetworkVanillaPusher extends NetworkDirectional implements SoftCellBannable {
   private static final int[] BACKGROUND_SLOTS = new int[]{
      0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 13, 15, 16, 17, 18, 20, 22, 23, 24, 26, 27, 28, 30, 31, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44
   };
   private static final int INPUT_SLOT = 25;
   private static final int NORTH_SLOT = 11;
   private static final int SOUTH_SLOT = 29;
   private static final int EAST_SLOT = 21;
   private static final int WEST_SLOT = 19;
   private static final int UP_SLOT = 14;
   private static final int DOWN_SLOT = 32;

   public NetworkVanillaPusher(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe, NodeType.PUSHER);
      this.getSlotsToDrop().add(25);
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
            Block block = blockMenu.getBlock();
            Block targetBlock = blockMenu.getBlock().getRelative(direction);
            if (StorageCacheUtils.getMenu(targetBlock.getLocation()) == null) {
               BlockState blockState = PaperLib.getBlockState(targetBlock, false).getState();
               if (blockState instanceof InventoryHolder holder) {
                  if (Networks.getInstance().getMCVersion().isAtLeast(MinecraftVersion.V1_21) && blockState instanceof CrafterInventory) {
                     this.sendFeedback(block.getLocation(), FeedbackType.NOT_ALLOWED_BLOCK);
                  } else {
                     Inventory inventory = holder.getInventory();
                     ItemStack stack = blockMenu.getItemInSlot(25);
                     if (stack != null && stack.getType() != Material.AIR) {
                        boolean wildChests = Networks.getSupportedPluginManager().isWildChests();
                        boolean isChest = wildChests && WildChestsAPI.getChest(targetBlock.getLocation()) != null;
                        this.sendDebugMessage(block.getLocation(), String.format(Lang.getString("messages.debug.wildchests"), wildChests));
                        this.sendDebugMessage(block.getLocation(), String.format(Lang.getString("messages.debug.ischest"), isChest));
                        if (inventory instanceof FurnaceInventory furnace) {
                           this.handleFurnace(blockMenu, stack, furnace);
                        } else if (inventory instanceof BrewerInventory brewer) {
                           this.handleBrewingStand(blockMenu, stack, brewer);
                        } else if (wildChests && isChest) {
                           this.sendDebugMessage(block.getLocation(), Lang.getString("messages.debug.wildchests-trigger-failed"));
                        } else if (InvUtils.fits(holder.getInventory(), stack, new int[0])) {
                           this.sendDebugMessage(block.getLocation(), Lang.getString("messages.debug.wildchests-trigger-success"));
                           InventoryUtil.addItem(holder.getInventory(), stack);
                        }
                     } else {
                        this.sendFeedback(block.getLocation(), FeedbackType.NO_ITEM_FOUND);
                     }
                  }
               } else {
                  this.sendFeedback(block.getLocation(), FeedbackType.NO_INVENTORY_FOUND);
               }
            }
         }
      } else {
         this.sendFeedback(blockMenu.getLocation(), FeedbackType.NO_NETWORK_FOUND);
      }
   }

   private void handleFurnace(@NotNull BlockMenu blockMenu, @NotNull ItemStack stack, @NotNull FurnaceInventory furnace) {
      if (!stack.getType().isFuel() || furnace.getFuel() != null && furnace.getFuel().getType() != Material.AIR) {
         if (furnace.canSmelt(stack) && (furnace.getSmelting() == null || furnace.getSmelting().getType() == Material.AIR)) {
            furnace.setSmelting(stack.clone());
            stack.setAmount(0);
            this.sendFeedback(blockMenu.getLocation(), FeedbackType.WORKING);
         }
      } else {
         furnace.setFuel(stack.clone());
         stack.setAmount(0);
         this.sendFeedback(blockMenu.getLocation(), FeedbackType.WORKING);
      }
   }

   private void handleBrewingStand(@NotNull BlockMenu blockMenu, @NotNull ItemStack stack, @NotNull BrewerInventory brewer) {
      if (stack.getType() == Material.BLAZE_POWDER) {
         if (brewer.getFuel() == null || brewer.getFuel().getType() == Material.AIR) {
            brewer.setFuel(stack.clone());
            stack.setAmount(0);
            this.sendFeedback(blockMenu.getLocation(), FeedbackType.WORKING);
         } else if (brewer.getIngredient() == null || brewer.getIngredient().getType() == Material.AIR) {
            brewer.setIngredient(stack.clone());
            stack.setAmount(0);
            this.sendFeedback(blockMenu.getLocation(), FeedbackType.WORKING);
         }
      } else if (stack.getType() != Material.POTION && stack.getType() != Material.SPLASH_POTION && stack.getType() != Material.LINGERING_POTION) {
         if (brewer.getIngredient() == null || brewer.getIngredient().getType() == Material.AIR) {
            brewer.setIngredient(stack.clone());
            stack.setAmount(0);
            this.sendFeedback(blockMenu.getLocation(), FeedbackType.WORKING);
         }
      } else {
         for (int i = 0; i < 3; i++) {
            ItemStack stackInSlot = brewer.getContents()[i];
            if (stackInSlot == null || stackInSlot.getType() == Material.AIR) {
               ItemStack[] contents = brewer.getContents();
               contents[i] = stack.clone();
               brewer.setContents(contents);
               stack.setAmount(0);
               this.sendFeedback(blockMenu.getLocation(), FeedbackType.WORKING);
               return;
            }
         }
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
   public int[] getInputSlots() {
      return new int[]{25};
   }

   @NotNull
   @Override
   protected DustOptions getDustOptions() {
      return new DustOptions(Color.MAROON, 1.0F);
   }
}
