package com.ytdd9527.networksexpansion.implementation.machines.cellnet.drive.ender;

import com.balugaq.netex.utils.Lang;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.core.items.SpecialSlimefunItem;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.api.DriveType;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.drive.DriveOwnership;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.drive.EnderDrive;
import io.github.sefiraat.networks.Networks;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemHandler;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.data.persistent.PersistentDataAPI;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import java.util.Optional;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChannelConfigurator extends SpecialSlimefunItem {
   private static final NamespacedKey CHANNEL_KEY = new NamespacedKey(Networks.getInstance(), "ender_config_channel");

   public ChannelConfigurator(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe);
   }

   public void preRegister() {
      this.addItemHandler(new ItemHandler[]{(ItemUseHandler)e -> {
         e.cancel();
         Player player = e.getPlayer();
         Optional<Block> optional = e.getClickedBlock();
         if (optional.isEmpty()) {
            if (player.isSneaking()) {
               setStoredChannel(player.getInventory().getItemInMainHand(), null);
               player.sendMessage(Lang.getString("messages.cellnet.configurator.cleared"));
            }
         } else {
            Block block = optional.get();
            SlimefunItem target = StorageCacheUtils.getSfItem(block.getLocation());
            if (DriveType.of(target) != DriveType.ENDER) {
               player.sendMessage(Lang.getString("messages.cellnet.configurator.not_ender_drive"));
            } else if (!Slimefun.getProtectionManager().hasPermission(player, block, Interaction.INTERACT_BLOCK)) {
               player.sendMessage(Lang.getString("messages.unsupported-operation.comprehensive.no_permission"));
            } else if (!DriveOwnership.isOwnerOrWhitelisted(player, block.getLocation())) {
               player.sendMessage(Lang.getString("messages.cellnet.configurator.not_owner"));
            } else {
               ItemStack held = player.getInventory().getItemInMainHand();
               if (player.isSneaking()) {
                  copyFromDrive(player, held, block.getLocation());
               } else {
                  pasteToDrive(player, held, block.getLocation());
               }
            }
         }
      }});
   }

   private static void copyFromDrive(@NotNull Player player, @NotNull ItemStack configurator, @NotNull Location location) {
      String channel = EnderDrive.getChannel(location);
      if (channel == null) {
         player.sendMessage(Lang.getString("messages.cellnet.configurator.drive_no_channel"));
      } else {
         setStoredChannel(configurator, channel);
         player.sendMessage(Lang.getString("messages.cellnet.configurator.copied", channel));
      }
   }

   private static void pasteToDrive(@NotNull Player player, @NotNull ItemStack configurator, @NotNull Location location) {
      String stored = getStoredChannel(configurator);
      boolean fresh = stored == null || stored.isEmpty();
      if (fresh) {
         stored = EnderDrive.generateChannel();
         setStoredChannel(configurator, stored);
      }

      EnderDrive.handleChannelInput(location, stored);
      player.sendMessage(Lang.getString(fresh ? "messages.cellnet.configurator.bound_new" : "messages.cellnet.configurator.bound", stored));
   }

   public static boolean isConfigurator(@Nullable ItemStack itemStack) {
      return itemStack != null && !itemStack.getType().isAir() && SlimefunItem.getByItem(itemStack) instanceof ChannelConfigurator;
   }

   public static void setStoredChannel(@NotNull ItemStack itemStack, @Nullable String channel) {
      if (!itemStack.getType().isAir()) {
         ItemMeta meta = itemStack.getItemMeta();
         if (meta != null) {
            if (channel != null && !channel.isEmpty()) {
               PersistentDataAPI.setString(meta, CHANNEL_KEY, channel);
            } else {
               PersistentDataAPI.remove(meta, CHANNEL_KEY);
            }

            itemStack.setItemMeta(meta);
         }
      }
   }

   @Nullable
   public static String getStoredChannel(@NotNull ItemStack itemStack) {
      if (!itemStack.hasItemMeta()) {
         return null;
      } else {
         ItemMeta meta = itemStack.getItemMeta();
         return meta == null ? null : PersistentDataAPI.getString(meta, CHANNEL_KEY);
      }
   }
}
