package io.github.sefiraat.networks.slimefun.network;

import com.balugaq.netex.api.enums.FeedbackType;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import dev.sefiraat.sefilib.misc.ParticleUtils;
import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.managers.SupportedPluginManager;
import io.github.sefiraat.networks.network.NodeDefinition;
import io.github.sefiraat.networks.network.NodeType;
import io.github.sefiraat.networks.slimefun.NetworkSlimefunItems;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemHandler;
import io.github.thebusybiscuit.slimefun4.api.items.ItemSetting;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.items.settings.IntRangeSetting;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.util.Collection;
import java.util.UUID;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class NetworkVacuum extends NetworkObject {
   private static final int[] INPUT_SLOTS = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8};
   @NotNull
   private final ItemSetting<Integer> tickRate = new IntRangeSetting(this, "tick_rate", 1, 1, 10);
   @NotNull
   private final ItemSetting<Integer> vacuumRange = new IntRangeSetting(this, "vacuum_range", 1, 2, 5);

   public NetworkVacuum(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe, NodeType.VACUUM);
      this.addItemSetting(new ItemSetting[]{this.tickRate, this.vacuumRange});

      for (int inputSlot : INPUT_SLOTS) {
         this.getSlotsToDrop().add(inputSlot);
      }

      this.addItemHandler(new ItemHandler[]{new BlockTicker() {
         private int tick = 1;

         public boolean isSynchronized() {
            return false;
         }

         public void tick(@NotNull Block block, SlimefunItem item, @NotNull SlimefunBlockData data) {
            if (this.tick <= 1) {
               BlockMenu blockMenu = data.getBlockMenu();
               if (blockMenu == null) {
                  return;
               }

               NetworkVacuum.this.addToRegistry(block);
               NetworkVacuum.this.tryAddItem(blockMenu);
               Bukkit.getScheduler().runTask(Networks.getInstance(), bukkitTask -> NetworkVacuum.this.findItem(blockMenu));
            }
         }

         public void uniqueTick() {
            this.tick = this.tick <= 1 ? (Integer)NetworkVacuum.this.tickRate.getValue() : this.tick - 1;
         }
      }});
      this.addItemHandler(new ItemHandler[]{new BlockPlaceHandler(false) {
         public void onPlayerPlace(@NotNull BlockPlaceEvent event) {
            NetworkStorage.removeNode(event.getBlock().getLocation());
            SlimefunBlockData blockData = StorageCacheUtils.getBlock(event.getBlock().getLocation());
            if (blockData != null) {
               blockData.setData("uuid", event.getPlayer().getUniqueId().toString());
            }
         }
      }});
   }

   private void findItem(@NotNull BlockMenu blockMenu) {
      for (int inputSlot : INPUT_SLOTS) {
         ItemStack inSlot = blockMenu.getItemInSlot(inputSlot);
         if (inSlot == null || inSlot.getType() == Material.AIR) {
            Location location = blockMenu.getLocation().clone().add(0.5, 0.5, 0.5);
            int range = (Integer)this.vacuumRange.getValue();
            Collection<Item> items = location.getWorld().getNearbyEntitiesByType(Item.class, location, (double)range, (double)range, (double)range);
            if (items.isEmpty()) {
               this.sendFeedback(blockMenu.getLocation(), FeedbackType.NO_ITEM_FOUND);
               return;
            }

            for (Item item : items) {
               String ownerUUID = StorageCacheUtils.getData(blockMenu.getLocation(), "uuid");
               if (ownerUUID != null) {
                  UUID uuid = UUID.fromString(ownerUUID);
                  OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                  if (!Slimefun.getProtectionManager().hasPermission(offlinePlayer, item.getLocation(), Interaction.INTERACT_ENTITY)) {
                     this.sendFeedback(blockMenu.getLocation(), FeedbackType.NO_PERMISSION);
                     return;
                  }
               }

               if (item.getPickupDelay() <= 0 && !SlimefunUtils.hasNoPickupFlag(item)) {
                  ItemStack finalPush = item.getItemStack().clone();
                  int amount = SupportedPluginManager.getStackAmount(item);
                  int maxAmount = item.getItemStack().getMaxStackSize();
                  if (amount <= 0) {
                     return;
                  }

                  if (amount > maxAmount) {
                     SupportedPluginManager.setStackAmount(item, amount - maxAmount);
                     finalPush.setAmount(maxAmount);
                  } else {
                     finalPush.setAmount(amount);
                     item.remove();
                  }

                  blockMenu.replaceExistingItem(inputSlot, finalPush);
                  ParticleUtils.displayParticleRandomly(item, 1.0, 5, new DustOptions(Color.BLUE, 1.0F));
                  return;
               }
            }
         }
      }

      this.sendFeedback(blockMenu.getLocation(), FeedbackType.WORKING);
   }

   private void tryAddItem(@NotNull BlockMenu blockMenu) {
      NodeDefinition definition = NetworkStorage.getNode(blockMenu.getLocation());
      if (definition.getNode() == null) {
         this.sendFeedback(blockMenu.getLocation(), FeedbackType.NO_NETWORK_FOUND);
      } else {
         for (int inputSlot : INPUT_SLOTS) {
            ItemStack itemStack = blockMenu.getItemInSlot(inputSlot);
            if (itemStack != null && itemStack.getType() != Material.AIR) {
               definition.getNode().getRoot().addItemStack(blockMenu.getLocation(), itemStack);
            }
         }

         this.sendFeedback(blockMenu.getLocation(), FeedbackType.WORKING);
      }
   }

   public void postRegister() {
      new BlockMenuPreset(this.getId(), this.getItemName()) {
         public void init() {
            this.setSize(9);
         }

         public boolean canOpen(@NotNull Block block, @NotNull Player player) {
            return player.hasPermission("slimefun.inventory.bypass")
               || NetworkSlimefunItems.NETWORK_VACUUM.canUse(player, false)
                  && Slimefun.getProtectionManager().hasPermission(player, block.getLocation(), Interaction.INTERACT_BLOCK);
         }

         public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
            return flow == ItemTransportFlow.INSERT ? NetworkVacuum.INPUT_SLOTS : new int[0];
         }
      };
   }
}
