package com.ytdd9527.networksexpansion.implementation.machines.cellnet.drive.menu;

import com.balugaq.netex.utils.Lang;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.drive.CellDrive;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.drive.DriveOwnership;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.drive.WhitelistStore;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.ui.BrowseUI;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.ui.Icons;
import io.github.sefiraat.networks.Networks;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DriveWhitelistMenu {
   public static final int MAX_WHITELIST_SIZE = 14;
   private static final long WHITELIST_ADD_TIMEOUT_TICKS = 1200L;
   private static final int[] WHITELIST_MENU_BACKGROUND = new int[]{
      0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 14, 15, 16, 17, 18, 19, 20, 21, 23, 24, 25, 26, 27, 35, 36, 44, 45, 46, 47, 48, 50, 51, 52, 53
   };
   private static final int WHITELIST_OWNER_SLOT = 13;
   private static final int WHITELIST_ADD_BUTTON_SLOT = 22;
   private static final int[] WHITELIST_PLAYER_SLOTS = new int[]{28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
   private static final int WHITELIST_BACK_BUTTON_SLOT = 49;
   private static final Map<UUID, Location> ADDING_WHITELIST = new ConcurrentHashMap<>();
   private static final Map<UUID, Integer> ADDING_TIMERS = new ConcurrentHashMap<>();
   private static final Map<Location, DriveWhitelistMenu.CachedWhitelistButton> BUTTON_CACHE = new ConcurrentHashMap<>();

   private DriveWhitelistMenu() {
   }

   public static boolean canManageWhitelist(@NotNull Player player, @NotNull Location location) {
      if (DriveOwnership.bypasses(player)) {
         return true;
      } else {
         UUID ownerUuid = CellDrive.getOwnerUuid(location);
         return ownerUuid != null && ownerUuid.equals(player.getUniqueId());
      }
   }

   public static void open(@NotNull BlockMenu driveMenu, @NotNull Location location, @NotNull Player player) {
      if (!canManageWhitelist(player, location)) {
         player.sendMessage(Lang.getString("messages.cellnet.drive.whitelist_not_owner"));
      } else {
         ChestMenu menu = new ChestMenu(Lang.getString("messages.cellnet.drive.whitelist_title"));
         menu.setPlayerInventoryClickable(true);

         for (int slot : WHITELIST_MENU_BACKGROUND) {
            menu.addItem(slot, ChestMenuUtils.getBackground(), (p, s, i, a) -> false);
         }

         menu.addItem(22, buildAddButton(), (p, s, i, a) -> {
            startAddingWhitelist(p, location);
            return false;
         });
         menu.addItem(49, BrowseUI.backButton(), (p, s, i, a) -> {
            driveMenu.open(new Player[]{p});
            return false;
         });
         menu.setEmptySlotsClickable(false);
         render(menu, location);
         menu.open(new Player[]{player});
      }
   }

   @NotNull
   public static ItemStack whitelistButton(@NotNull Location location) {
      UUID ownerUuid = CellDrive.getOwnerUuid(location);
      String ownerName = ownerUuid != null ? String.valueOf(ownerUuid) : "";
      int size = ownerUuid != null ? WhitelistStore.getWhitelistSize(ownerUuid) : 0;
      String signature = ownerName + ":" + size;
      DriveWhitelistMenu.CachedWhitelistButton cached = BUTTON_CACHE.get(location);
      if (cached != null && cached.signature().equals(signature)) {
         return cached.button();
      } else {
         ItemStack button = buildWhitelistButton(location);
         BUTTON_CACHE.put(location, new DriveWhitelistMenu.CachedWhitelistButton(signature, button));
         return button;
      }
   }

   public static void invalidateButton(@NotNull Location location) {
      BUTTON_CACHE.remove(location);
   }

   @NotNull
   public static ItemStack buildWhitelistButton(@NotNull Location location) {
      UUID ownerUuid = CellDrive.getOwnerUuid(location);
      ItemStack item = new ItemStack(Material.PLAYER_HEAD);
      item.editMeta(meta -> {
         List<String> lore = new ArrayList<>();
         meta.setDisplayName(Lang.getString("messages.cellnet.drive.whitelist_button"));
         if (ownerUuid != null) {
            OfflinePlayer op = Bukkit.getOfflinePlayer(ownerUuid);
            if (op != null && op.getName() != null && meta instanceof SkullMeta skull) {
               skull.setOwningPlayer(op);
            }

            String ownerName = op != null && op.getName() != null ? op.getName() : ownerUuid.toString();
            lore.add(Lang.getString("messages.cellnet.drive.whitelist_owner", ownerName));
            lore.add(Lang.getString("messages.cellnet.drive.whitelist_count", WhitelistStore.getWhitelistSize(ownerUuid)));
         } else {
            lore.add(Lang.getString("messages.cellnet.drive.whitelist_no_owner"));
         }

         lore.add("");
         lore.add(Lang.getString("messages.cellnet.drive.whitelist_open_hint"));
         meta.setLore(lore);
      });
      return item;
   }

   public static void addWhitelistPlayer(@NotNull Player player, @NotNull Location location, @NotNull String name) {
      if (!canManageWhitelist(player, location)) {
         player.sendMessage(Lang.getString("messages.cellnet.drive.whitelist_not_owner"));
      } else {
         UUID ownerUuid = CellDrive.getOwnerUuid(location);
         if (ownerUuid != null) {
            OfflinePlayer target = Bukkit.getPlayerExact(name);
            if (target == null) {
               target = Bukkit.getOfflinePlayer(name);
               if (!target.hasPlayedBefore()) {
                  player.sendMessage(Lang.getString("messages.cellnet.drive.whitelist_player_not_found"));
                  return;
               }
            }

            UUID targetUuid = target.getUniqueId();
            if (ownerUuid.equals(targetUuid)) {
               player.sendMessage(Lang.getString("messages.cellnet.drive.whitelist_is_owner"));
            } else if (WhitelistStore.isWhitelisted(ownerUuid, targetUuid)) {
               player.sendMessage(Lang.getString("messages.cellnet.drive.whitelist_already"));
            } else if (WhitelistStore.getWhitelistSize(ownerUuid) >= 14) {
               player.sendMessage(Lang.getString("messages.cellnet.drive.whitelist_full"));
               reopenWhitelistMenu(player, location);
            } else {
               WhitelistStore.add(ownerUuid, targetUuid);
               String targetName = target.getName() == null ? name : target.getName();
               player.sendMessage(Lang.getString("messages.cellnet.drive.whitelist_added", targetName));
               reopenWhitelistMenu(player, location);
            }
         }
      }
   }

   @Nullable
   public static Location getAddingLocation(@NotNull UUID playerUuid) {
      return ADDING_WHITELIST.get(playerUuid);
   }

   public static void stopAdding(@NotNull UUID playerUuid) {
      ADDING_WHITELIST.remove(playerUuid);
      Integer taskId = ADDING_TIMERS.remove(playerUuid);
      if (taskId != null) {
         Bukkit.getScheduler().cancelTask(taskId);
      }
   }

   private static void startAddingWhitelist(@NotNull Player player, @NotNull Location location) {
      UUID playerUuid = player.getUniqueId();
      stopAdding(playerUuid);
      ADDING_WHITELIST.put(playerUuid, location);
      player.closeInventory();
      player.sendMessage(Lang.getString("messages.cellnet.drive.whitelist_enter_name"));
      int taskId = Bukkit.getScheduler().runTaskLater(Networks.getInstance(), () -> {
         if (ADDING_WHITELIST.remove(playerUuid) != null) {
            ADDING_TIMERS.remove(playerUuid);
            Player timeoutPlayer = Bukkit.getPlayer(playerUuid);
            if (timeoutPlayer != null) {
               timeoutPlayer.sendMessage(Lang.getString("messages.cellnet.drive.whitelist_add_timeout"));
            }
         }
      }, 1200L).getTaskId();
      ADDING_TIMERS.put(playerUuid, taskId);
   }

   private static void reopenWhitelistMenu(@NotNull Player player, @NotNull Location location) {
      BlockMenu driveMenu = StorageCacheUtils.getMenu(location);
      if (driveMenu != null) {
         open(driveMenu, location, player);
      }
   }

   private static void render(@NotNull ChestMenu menu, @NotNull Location location) {
      menu.replaceExistingItem(13, buildOwnerInfo(location));
      menu.addMenuClickHandler(13, (p, s, ix, a) -> false);
      UUID ownerUuid = CellDrive.getOwnerUuid(location);
      List<UUID> whitelist = ownerUuid == null ? List.of() : WhitelistStore.getWhitelist(ownerUuid);

      for (int i = 0; i < WHITELIST_PLAYER_SLOTS.length; i++) {
         int slot = WHITELIST_PLAYER_SLOTS[i];
         if (i < whitelist.size()) {
            UUID targetUuid = whitelist.get(i);
            menu.replaceExistingItem(slot, buildPlayerHead(targetUuid));
            menu.addMenuClickHandler(slot, (p, s, it, a) -> {
               removeWhitelistPlayer(menu, location, p, targetUuid);
               return false;
            });
         } else {
            menu.replaceExistingItem(slot, new ItemStack(Material.AIR));
            menu.addMenuClickHandler(slot, (p, s, it, a) -> false);
         }
      }
   }

   private static void removeWhitelistPlayer(@NotNull ChestMenu menu, @NotNull Location location, @NotNull Player player, @NotNull UUID targetUuid) {
      if (!canManageWhitelist(player, location)) {
         player.sendMessage(Lang.getString("messages.cellnet.drive.whitelist_not_owner"));
      } else {
         UUID ownerUuid = CellDrive.getOwnerUuid(location);
         if (ownerUuid != null) {
            if (WhitelistStore.isWhitelisted(ownerUuid, targetUuid)) {
               WhitelistStore.remove(ownerUuid, targetUuid);
               OfflinePlayer removedPlayer = Bukkit.getOfflinePlayer(targetUuid);
               String removedName = removedPlayer.getName() == null ? targetUuid.toString() : removedPlayer.getName();
               player.sendMessage(Lang.getString("messages.cellnet.drive.whitelist_removed", removedName));
               render(menu, location);
            }
         }
      }
   }

   @NotNull
   private static ItemStack buildOwnerInfo(@NotNull Location location) {
      UUID ownerUuid = CellDrive.getOwnerUuid(location);
      OfflinePlayer op = ownerUuid == null ? null : Bukkit.getOfflinePlayer(ownerUuid);
      String ownerName = ownerUuid == null ? "?" : (op != null && op.getName() != null ? op.getName() : ownerUuid.toString());
      ItemStack item = new ItemStack(Material.PLAYER_HEAD);
      item.editMeta(meta -> {
         if (op != null && op.getName() != null && meta instanceof SkullMeta skull) {
            skull.setOwningPlayer(op);
         }

         meta.setDisplayName(Lang.getString("messages.cellnet.drive.whitelist_owner_head", ownerName));
         meta.setLore(List.of(Lang.getString("messages.cellnet.drive.whitelist_owner_head_lore")));
      });
      return item;
   }

   @NotNull
   private static ItemStack buildAddButton() {
      return Icons.DRIVE_WHITELIST_ADD;
   }

   @NotNull
   private static ItemStack buildPlayerHead(@NotNull UUID uuid) {
      OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
      ItemStack item = new ItemStack(Material.PLAYER_HEAD);
      String name = player.getName() == null ? uuid.toString() : player.getName();
      item.editMeta(meta -> {
         if (player.getName() != null && meta instanceof SkullMeta skull) {
            skull.setOwningPlayer(player);
         }

         meta.setDisplayName(name);
         meta.setLore(List.of(Lang.getString("messages.cellnet.drive.whitelist_remove_hint")));
      });
      return item;
   }

   private static record CachedWhitelistButton(String signature, ItemStack button) {
   }
}
