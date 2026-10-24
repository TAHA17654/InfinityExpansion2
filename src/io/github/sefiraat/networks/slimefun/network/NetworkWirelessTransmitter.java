package io.github.sefiraat.networks.slimefun.network;

import com.balugaq.netex.api.enums.FeedbackType;
import com.balugaq.netex.api.helpers.Icon;
import com.balugaq.netex.utils.BlockMenuUtil;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.network.NodeDefinition;
import io.github.sefiraat.networks.network.NodeType;
import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import io.github.sefiraat.networks.slimefun.NetworkSlimefunItems;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemHandler;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import java.util.HashMap;
import java.util.Map;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class NetworkWirelessTransmitter extends NetworkObject {
   public static final int TEMPLATE_SLOT = 13;
   private static final int[] BACKGROUND_SLOTS = new int[]{0, 1, 2, 6, 7, 8, 9, 10, 11, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26};
   private static final int[] BACKGROUND_SLOTS_TEMPLATE = new int[]{3, 4, 5, 12, 14, 21, 22, 23};
   private static final String LINKED_LOCATION_KEY_X = "linked-location-x";
   private static final String LINKED_LOCATION_KEY_Y = "linked-location-y";
   private static final String LINKED_LOCATION_KEY_Z = "linked-location-z";
   private static final int REQUIRED_POWER = 500;
   private static final int TICKS_PER = 2;
   private final Map<Location, Location> linkedLocations = new HashMap<>();

   public NetworkWirelessTransmitter(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe, NodeType.WIRELESS_TRANSMITTER);
      this.getSlotsToDrop().add(13);
      this.addItemHandler(
         new ItemHandler[]{
            new BlockTicker() {
               private final Map<Location, Integer> tickMap = new HashMap<>();
               private final Map<Location, Boolean> firstTick = new HashMap<>();

               public boolean isSynchronized() {
                  return false;
               }

               public void tick(@NotNull Block block, SlimefunItem slimefunItem, @NotNull SlimefunBlockData data) {
                  BlockMenu blockMenu = data.getBlockMenu();
                  if (blockMenu != null) {
                     NetworkWirelessTransmitter.this.addToRegistry(block);
                     boolean isFirstTick = this.firstTick.getOrDefault(block.getLocation(), true);
                     if (isFirstTick) {
                        String xString = data.getData("linked-location-x");
                        String yString = data.getData("linked-location-y");
                        String zString = data.getData("linked-location-z");
                        if (xString != null && yString != null && zString != null) {
                           Location linkedLocation = new Location(
                              block.getWorld(), (double)Integer.parseInt(xString), (double)Integer.parseInt(yString), (double)Integer.parseInt(zString)
                           );
                           NetworkWirelessTransmitter.this.linkedLocations.put(block.getLocation(), linkedLocation);
                        }

                        this.firstTick.put(block.getLocation(), false);
                     }

                     int tick = this.tickMap.getOrDefault(block.getLocation(), 0);
                     if (tick >= 2) {
                        NetworkWirelessTransmitter.this.onTick(blockMenu);
                        this.tickMap.remove(block.getLocation());
                        tick = 0;
                     } else {
                        tick++;
                     }

                     this.tickMap.put(block.getLocation(), tick + 1);
                  }
               }
            }
         }
      );
   }

   private void onTick(@NotNull BlockMenu blockMenu) {
      NodeDefinition definition = NetworkStorage.getNode(blockMenu.getLocation());
      if (definition != null && definition.getNode() != null) {
         Location location = blockMenu.getLocation();
         Location linkedLocation = this.linkedLocations.get(location);
         if (linkedLocation == null) {
            this.sendFeedback(location, FeedbackType.NO_LINKED_LOCATION_FOUND);
         } else {
            SlimefunItem slimefunItem = StorageCacheUtils.getSfItem(linkedLocation);
            if (!(slimefunItem instanceof NetworkWirelessReceiver)) {
               this.linkedLocations.remove(location);
            } else {
               BlockMenu linkedBlockMenu = StorageCacheUtils.getMenu(linkedLocation);
               if (linkedBlockMenu == null) {
                  this.sendFeedback(location, FeedbackType.NO_LINKED_BLOCK_MENU_FOUND);
               } else {
                  ItemStack itemStack = linkedBlockMenu.getItemInSlot(13);
                  if (itemStack == null || itemStack.getType() == Material.AIR) {
                     ItemStack templateStack = blockMenu.getItemInSlot(13);
                     if (templateStack == null || templateStack.getType() == Material.AIR) {
                        this.sendFeedback(location, FeedbackType.NO_TEMPLATE_FOUND);
                        return;
                     }

                     if (definition.getNode().getRoot().getRootPower() < 500L) {
                        this.sendFeedback(location, FeedbackType.NOT_ENOUGH_POWER);
                        return;
                     }

                     ItemStack stackToPush = definition.getNode()
                        .getRoot()
                        .getItemStack(blockMenu.getLocation(), new ItemRequest(templateStack.clone(), templateStack.getMaxStackSize()));
                     if (stackToPush != null) {
                        definition.getNode().getRoot().removeRootPower(500L);
                        BlockMenuUtil.pushItem(linkedBlockMenu, stackToPush, 13);
                        this.sendFeedback(location, FeedbackType.WORKING);
                        if (definition.getNode().getRoot().isDisplayParticles()) {
                           Location particleLocation = blockMenu.getLocation().clone().add(0.5, 1.1, 0.5);
                           Location particleLocation2 = linkedBlockMenu.getLocation().clone().add(0.5, 2.1, 0.5);
                           particleLocation.getWorld().spawnParticle(Particle.WAX_ON, particleLocation, 0, 0.0, 4.0, 0.0);
                           particleLocation2.getWorld().spawnParticle(Particle.WAX_OFF, particleLocation2, 0, 0.0, -4.0, 0.0);
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

   public void postRegister() {
      new BlockMenuPreset(this.getId(), this.getItemName()) {
         public void init() {
            this.drawBackground(NetworkWirelessTransmitter.BACKGROUND_SLOTS);
            this.drawBackground(Icon.TRANSMITTER_TEMPLATE_BACKGROUND_STACK, NetworkWirelessTransmitter.BACKGROUND_SLOTS_TEMPLATE);
         }

         public boolean canOpen(@NotNull Block block, @NotNull Player player) {
            return player.hasPermission("slimefun.inventory.bypass")
               || NetworkSlimefunItems.NETWORK_WIRELESS_TRANSMITTER.canUse(player, false)
                  && Slimefun.getProtectionManager().hasPermission(player, block.getLocation(), Interaction.INTERACT_BLOCK);
         }

         public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
            return new int[0];
         }
      };
   }

   @Override
   protected void onBreak(@NotNull BlockBreakEvent event) {
      super.onBreak(event);
      this.linkedLocations.remove(event.getBlock().getLocation());
   }

   public void addLinkedLocation(@NotNull Block block, @NotNull Location linkedLocation) {
      this.linkedLocations.put(block.getLocation(), linkedLocation);
      SlimefunBlockData blockData = StorageCacheUtils.getBlock(block.getLocation());
      if (blockData != null) {
         blockData.setData("linked-location-x", String.valueOf(linkedLocation.getBlockX()));
         blockData.setData("linked-location-y", String.valueOf(linkedLocation.getBlockY()));
         blockData.setData("linked-location-z", String.valueOf(linkedLocation.getBlockZ()));
      }
   }
}
