package com.ytdd9527.networksexpansion.implementation.machines.networks.advanced;

import com.balugaq.netex.api.enums.FeedbackType;
import com.balugaq.netex.utils.BlockMenuUtil;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.core.items.SpecialSlimefunItem;
import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.NodeDefinition;
import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import io.github.sefiraat.networks.slimefun.network.AdminDebuggable;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemHandler;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SmartPusher extends SpecialSlimefunItem implements AdminDebuggable {
   private static final Map<Location, BlockFace> DIRECTIONS = new HashMap<>();
   private static final Set<BlockFace> VALID_FACES = EnumSet.of(BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST);
   private static final int[] BACKGROUND_SLOTS = new int[]{0, 1, 2, 3, 5, 6, 7, 8};
   private static final int[] TEMPLATE_SLOTS = new int[]{4};

   public SmartPusher(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe);
      this.addItemHandler(new ItemHandler[]{new BlockBreakHandler(false, false) {
         @ParametersAreNonnullByDefault
         public void onPlayerBreak(BlockBreakEvent event, ItemStack item, List<ItemStack> drops) {
            Location location = event.getBlock().getLocation();
            BlockMenu blockMenu = StorageCacheUtils.getMenu(location);
            if (blockMenu != null) {
               blockMenu.dropItems(location, SmartPusher.TEMPLATE_SLOTS);
            }

            Slimefun.getDatabaseManager().getBlockDataController().removeBlock(location);
         }
      }});
   }

   @Nullable
   public static BlockFace getDirection(Location location) {
      return DIRECTIONS.get(location);
   }

   public static void setDirection(Location location, BlockFace face) {
      DIRECTIONS.put(location, face);
   }

   public static void removeDirection(Location location) {
      DIRECTIONS.remove(location);
   }

   public void preRegister() {
      this.addItemHandler(new ItemHandler[]{new BlockTicker() {
         public boolean isSynchronized() {
            return false;
         }

         public void tick(@NotNull Block block, SlimefunItem slimefunItem, @NotNull SlimefunBlockData slimefunBlockData) {
            Location location = block.getLocation();
            BlockFace cachedFace = SmartPusher.getDirection(location);
            if (cachedFace != null && SmartPusher.VALID_FACES.contains(cachedFace)) {
               BlockMenu blockMenu = slimefunBlockData.getBlockMenu();
               if (blockMenu != null) {
                  SmartPusher.this.onTick(blockMenu, cachedFace);
               } else {
                  SmartPusher.this.sendFeedback(location, FeedbackType.INVALID_BLOCK);
               }
            } else if (block.getBlockData() instanceof Directional directional) {
               BlockFace face = directional.getFacing();
               SmartPusher.setDirection(location, face);
               SmartPusher.this.sendFeedback(block.getLocation(), FeedbackType.INITIALIZATION);
            } else {
               Slimefun.getDatabaseManager().getBlockDataController().removeBlock(location);
               SmartPusher.this.sendFeedback(block.getLocation(), FeedbackType.INVALID_BLOCK);
            }
         }
      }, new BlockBreakHandler(false, false) {
         public void onPlayerBreak(@NotNull BlockBreakEvent blockBreakEvent, @NotNull ItemStack itemStack, @NotNull List<ItemStack> list) {
            SmartPusher.removeDirection(blockBreakEvent.getBlock().getLocation());
            Location location = blockBreakEvent.getBlock().getLocation();
            BlockMenu blockMenu = StorageCacheUtils.getMenu(location);
            if (blockMenu != null) {
               blockMenu.dropItems(location, SmartPusher.this.getTemplateSlots());
            }
         }
      }, new BlockPlaceHandler(false) {
         public void onPlayerPlace(@NotNull BlockPlaceEvent blockPlaceEvent) {
            if (blockPlaceEvent.getBlock().getBlockData() instanceof Directional directional) {
               BlockFace face = directional.getFacing();
               SmartPusher.setDirection(blockPlaceEvent.getBlock().getLocation(), face);
            }
         }
      }});
   }

   public void postRegister() {
      new BlockMenuPreset(this.getId(), this.getItemName()) {
         public void init() {
            this.setSize(9);
            this.drawBackground(SmartPusher.this.getBackgroundSlots());
         }

         public void newInstance(@NotNull BlockMenu blockMenu, @NotNull Block b) {
            for (int backgroundSlot : SmartPusher.this.getBackgroundSlots()) {
               blockMenu.addMenuClickHandler(backgroundSlot, ChestMenuUtils.getEmptyClickHandler());
            }
         }

         public boolean canOpen(@NotNull Block block, @NotNull Player player) {
            return player.hasPermission("slimefun.inventory.bypass")
               || this.getSlimefunItem().canUse(player, false)
                  && Slimefun.getProtectionManager().hasPermission(player, block.getLocation(), Interaction.INTERACT_BLOCK);
         }

         public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
            return new int[0];
         }
      };
   }

   public void onTick(@NotNull BlockMenu blockMenu, @NotNull BlockFace bridgeFace) {
      BlockFace containerFace = bridgeFace.getOppositeFace();
      Block thisBlock = blockMenu.getBlock();
      Block bridge = thisBlock.getRelative(bridgeFace);
      Block container = thisBlock.getRelative(containerFace);
      NodeDefinition definition = NetworkStorage.getNode(bridge.getLocation());
      if (definition != null && definition.getNode() != null) {
         BlockMenu targetMenu = StorageCacheUtils.getMenu(container.getLocation());
         if (targetMenu != null) {
            NetworkRoot root = definition.getNode().getRoot();
            int[] slots = targetMenu.getPreset().getSlotsAccessedByItemTransport(targetMenu, ItemTransportFlow.INSERT, null);

            for (ItemStack template : this.getTemplateItems(blockMenu)) {
               ItemStack clone = StackUtils.getAsQuantity(template, 1);
               ItemRequest itemRequest = new ItemRequest(clone, clone.getMaxStackSize());
               int limitQuantity = this.getLimitQuantity();
               if (slots.length > 0) {
                  ItemStack delta = targetMenu.getItemInSlot(slots[0]);
                  if (delta == null || delta.getType() == Material.AIR) {
                     int freeSpace = 0;

                     for (int slot : slots) {
                        ItemStack itemStack = targetMenu.getItemInSlot(slot);
                        if (itemStack == null || itemStack.getType() == Material.AIR) {
                           freeSpace += clone.getMaxStackSize();
                        } else if (itemStack.getAmount() < clone.getMaxStackSize() && StackUtils.itemsMatch(itemRequest, itemStack)) {
                           int availableSpace = itemStack.getMaxStackSize() - itemStack.getAmount();
                           if (availableSpace > 0) {
                              freeSpace += availableSpace;
                           }
                        }
                     }

                     if (freeSpace > 0) {
                        itemRequest.setAmount(Math.min(freeSpace, limitQuantity));
                        ItemStack retrieved = root.getItemStack(blockMenu.getLocation(), itemRequest);
                        if (retrieved != null && retrieved.getType() != Material.AIR) {
                           BlockMenuUtil.pushItem(targetMenu, retrieved, slots);
                        }
                     }
                  }
               }
            }

            this.sendFeedback(blockMenu.getLocation(), FeedbackType.WORKING);
         } else {
            this.sendFeedback(blockMenu.getLocation(), FeedbackType.NO_TARGET_BLOCK);
         }
      } else {
         this.sendFeedback(blockMenu.getLocation(), FeedbackType.NO_NETWORK_FOUND);
      }
   }

   public int getLimitQuantity() {
      return 3456;
   }

   @NotNull
   public List<ItemStack> getTemplateItems(@NotNull BlockMenu blockMenu) {
      List<ItemStack> items = new ArrayList<>();

      for (int slot : this.getTemplateSlots()) {
         ItemStack itemStack = blockMenu.getItemInSlot(slot);
         if (itemStack != null && itemStack.getType() != Material.AIR) {
            items.add(itemStack);
         }
      }

      return items;
   }

   public int[] getTemplateSlots() {
      return TEMPLATE_SLOTS;
   }

   public int[] getBackgroundSlots() {
      return BACKGROUND_SLOTS;
   }
}
