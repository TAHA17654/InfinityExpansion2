package com.ytdd9527.networksexpansion.implementation.machines.cellnet.drive;

import com.balugaq.netex.integrations.logitech.CellVirtualCache;
import com.balugaq.netex.utils.Lang;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.CellHandle;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.StorageCell;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.ledger.CellLedger;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.drive.ender.EnderChannelController;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.drive.menu.DriveWhitelistMenu;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.ItemHashMap;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.ItemKey;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.NumberFormat;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.ui.Icons;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.ui.MenuShells;
import io.github.sefiraat.networks.Networks;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemHandler;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import net.guizhanss.minecraft.guizhanlib.gugu.minecraft.helpers.inventory.ItemStackHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EnderDrive extends CellDrive {
   public static final String CHANNEL_KEY = "ender_channel";
   private static final long DISPLAY_REFRESH_INTERVAL_MS = 2000L;
   private static final String ENDER_DISPLAY_NAME_TEMPLATE = Lang.getString("messages.cellnet.ender.display_name");
   private static final String CHANNEL_NONE_TEXT = Lang.getString("messages.cellnet.ender.channel_none");
   private static final String DISPLAY_SHARED_TEXT = Lang.getString("messages.cellnet.ender.display_shared");
   private static final String DISPLAY_INDEPENDENT_TEXT = Lang.getString("messages.cellnet.ender.display_independent");
   private static final String DISPLAY_MEMBERS_TEMPLATE = Lang.getString("messages.cellnet.ender.display_members");
   private static final String SNEAK_HINT_TEXT = Lang.getString("messages.cellnet.ender.display_sneak_hint");
   private static final long SYNC_PERIOD_TICKS = 20L;
   private static final int MAX_STATUS_ENTRIES = 6;
   private static final Map<Location, EnderDrive.EnderDriveState> STATES = new ConcurrentHashMap<>();
   private static final Map<String, Integer> CHANNEL_MEMBER_COUNTS = new ConcurrentHashMap<>();
   private static final AtomicBoolean SYNC_TIMER_STARTED = new AtomicBoolean();

   private static EnderDrive.EnderDriveState state(@NotNull Location location) {
      return STATES.computeIfAbsent(location.clone(), k -> new EnderDrive.EnderDriveState());
   }

   private static void dropState(@NotNull Location location) {
      STATES.remove(location);
   }

   public EnderDrive(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe);
   }

   @Nullable
   public static String getChannel(@NotNull Location location) {
      EnderDrive.EnderDriveState state = STATES.get(location);
      if (state != null && state.channel != null) {
         return state.channel;
      } else {
         String stored = StorageCacheUtils.getData(location, "ender_channel");
         if (stored != null && !stored.isEmpty()) {
            state(location).channel = stored;
            return stored;
         } else {
            return null;
         }
      }
   }

   public static long channelStock(@NotNull String channel, @NotNull ItemStack sample) {
      ItemKey key = new ItemKey(sample);
      long total = 0L;

      for (Location location : List.copyOf(STATES.keySet())) {
         if (channel.equals(getChannel(location))) {
            BlockMenu menu = StorageCacheUtils.getMenu(location);
            if (menu != null) {
               for (int slot : CELL_SLOTS) {
                  ItemStack cell = menu.getItemInSlot(slot);
                  if (cell != null && !cell.getType().isAir() && StorageCell.isStorageCell(cell)) {
                     CellLedger cache = StorageCell.loadCellCache(cell, StorageCell.getPerTypeLimit(cell));
                     total += Math.max(0L, cache.getAmount(key));
                  }
               }
            }
         }
      }

      return total;
   }

   @NotNull
   public static String generateChannel() {
      return "ch-" + UUID.randomUUID().toString().substring(0, 8);
   }

   public static void handleChannelInput(@NotNull Location location, @NotNull String rawInput) {
      SlimefunBlockData blockData = StorageCacheUtils.getBlock(location);
      BlockMenu menu = StorageCacheUtils.getMenu(location);
      if (blockData == null) {
         if (menu != null) {
            broadcastViewers(menu, Lang.getString("messages.cellnet.ender.bind_failed"));
         }
      } else {
         EnderDrive.EnderDriveState state = state(location);
         if (!rawInput.isEmpty() && !"-".equals(rawInput)) {
            String normalized = rawInput.trim();
            if (!normalized.isEmpty() && normalized.codePoints().count() <= 16L) {
               blockData.setData("ender_channel", normalized);
               state.channel = normalized;
               state.syncedMembers = Set.of();
               if (menu != null) {
                  CellDrive.getStorage().invalidateCellCache(location);
                  notifyResult(menu, Lang.getString("messages.cellnet.ender.channel_bound", normalized));
                  enqueueImmediateSync(location);
               }

               invalidateChannelPeers(normalized);
            } else {
               if (menu != null) {
                  broadcastViewers(menu, Lang.getString("messages.cellnet.input.channel_invalid", 16));
               }
            }
         } else {
            String previous = getChannel(location);
            state.channel = null;
            blockData.setData("ender_channel", "");
            EnderChannelController controller = EnderChannelController.getInstance();

            for (String uuid : state.syncedMembers) {
               controller.unbindCell(uuid);
            }

            state.syncedMembers = Set.of();
            if (previous != null) {
               CHANNEL_MEMBER_COUNTS.put(previous, controller.getCellUuidsOfChannel(previous).size());
            }

            if (menu != null) {
               CellDrive.getStorage().invalidateCellCache(location);
               notifyResult(menu, Lang.getString("messages.cellnet.ender.channel_unbound"));
               enqueueImmediateSync(location);
            }

            invalidateChannelPeers(previous);
         }
      }
   }

   private static void notifyResult(@NotNull BlockMenu menu, @NotNull String message) {
      for (HumanEntity human : menu.getInventory().getViewers()) {
         if (human instanceof Player player) {
            player.sendTitle("", message, 5, 60, 10);
            player.sendMessage(message);
         }
      }
   }

   private static void broadcastViewers(@NotNull BlockMenu menu, @NotNull String message) {
      for (HumanEntity human : menu.getInventory().getViewers()) {
         if (human instanceof Player player) {
            player.sendMessage(message);
         }
      }
   }

   @Override
   public void preRegister() {
      this.addItemHandler(new ItemHandler[]{this.placeHandler()});
      this.addItemHandler(new ItemHandler[]{this.displayTicker()});
      this.addItemHandler(new ItemHandler[]{this.breakHandler()});
   }

   @NotNull
   private BlockPlaceHandler placeHandler() {
      return new BlockPlaceHandler(false) {
         public void onPlayerPlace(@NotNull BlockPlaceEvent event) {
            Location location = event.getBlock().getLocation();
            DriveOwnership.writeOwner(location, event.getPlayer().getUniqueId());
            SlimefunBlockData blockData = StorageCacheUtils.getBlock(location);
            if (blockData != null) {
               String restored = blockData.getData("ender_channel");
               if (restored != null && !restored.isEmpty()) {
                  EnderDrive.state(location).channel = restored;
               }
            }
         }
      };
   }

   @NotNull
   private BlockTicker displayTicker() {
      return new BlockTicker() {
         public boolean isSynchronized() {
            return false;
         }

         public void tick(@NotNull Block b, SlimefunItem item, SlimefunBlockData data) {
            Location location = b.getLocation();
            long now = System.currentTimeMillis();
            EnderDrive.EnderDriveState state = EnderDrive.state(location);
            long last = state.displayStamp;
            if (last == 0L || now - last >= 2000L) {
               state.displayStamp = now;
               Bukkit.getScheduler().runTask(Networks.getInstance(), () -> {
                  BlockMenu blockMenu = StorageCacheUtils.getMenu(location);
                  if (blockMenu != null && blockMenu.hasViewer()) {
                     EnderDrive.refreshMainDisplay(blockMenu);
                  }
               });
            }
         }
      };
   }

   @NotNull
   private BlockBreakHandler breakHandler() {
      return new BlockBreakHandler(false, false) {
         public void onPlayerBreak(@NotNull BlockBreakEvent event, @NotNull ItemStack item, @NotNull List<ItemStack> drops) {
            Player breaker = event.getPlayer();
            Location location = event.getBlock().getLocation();
            if (!DriveOwnership.bypasses(breaker)
               && !DriveOwnership.passesGates(breaker, location, Interaction.BREAK_BLOCK, EnderDrive.this.canUse(breaker, false))) {
               event.setCancelled(true);
               breaker.sendMessage(Lang.getString("messages.cellnet.drive.break_not_allowed"));
            } else {
               String channel = EnderDrive.getChannel(location);
               EnderDrive.dropState(location);
               CellDrive.clearLocalState(location);
               CellUniqueness.unregisterDrive(location);
               CellDrive.getStorage().dropCellCache(location);
               CellVirtualCache.removeDrive(location);
               BlockMenu blockMenu = StorageCacheUtils.getMenu(location);
               if (blockMenu != null) {
                  EnderChannelController controller = EnderChannelController.getInstance();

                  for (int slot : CellDrive.CELL_SLOTS) {
                     ItemStack cellItem = blockMenu.getItemInSlot(slot);
                     String cellUuid = cellItem == null ? null : CellUniqueness.getUuidString(cellItem);
                     if (cellUuid != null) {
                        controller.unbindCell(cellUuid);
                     }
                  }

                  blockMenu.dropItems(blockMenu.getLocation(), CellDrive.CELL_SLOTS);
               }

               if (channel != null) {
                  EnderDrive.CHANNEL_MEMBER_COUNTS.put(channel, EnderChannelController.getInstance().getCellUuidsOfChannel(channel).size());
               }

               EnderDrive.invalidateChannelPeers(channel);
            }
         }
      };
   }

   @Override
   public void postRegister() {
      MenuShells.create(
         this,
         45,
         CellDrive.MAIN_BACKGROUND_SLOTS,
         (block, player) -> DriveOwnership.bypasses(player)
               || DriveOwnership.passesGates(player, block.getLocation(), Interaction.INTERACT_BLOCK, this.canUse(player, false)),
         MenuShells::emptyTransport,
         (menu, block) -> {
            this.setupHandlers(menu, block.getLocation());
            refreshMainDisplay(menu);
            startSyncTimerOnce();
         }
      );
   }

   private void setupHandlers(@NotNull BlockMenu menu, @NotNull Location location) {
      menu.addMenuClickHandler(4, (player, slotx, item, action) -> {
         if (getChannel(location) == null) {
            player.sendMessage(Lang.getString("messages.cellnet.ender.unbind_nothing"));
            return false;
         } else if (!DriveOwnership.bypasses(player) && !player.getUniqueId().equals(DriveOwnership.getOwnerUuid(location))) {
            player.sendMessage(Lang.getString("messages.cellnet.ender.unbind_not_owner"));
            return false;
         } else {
            handleChannelInput(location, "-");
            refreshMainDisplay(menu);
            return false;
         }
      });
      menu.addMenuClickHandler(13, (player, clickedSlot, item, action) -> {
         openItemAccessMenu(menu, location, player);
         return false;
      });
      menu.addMenuClickHandler(39, (player, clickedSlot, item, action) -> {
         DriveWhitelistMenu.open(menu, location, player);
         return false;
      });

      for (int slot : CELL_SLOTS) {
         menu.addMenuClickHandler(slot, (player, clickedSlot, item, action) -> {
            enqueueImmediateSync(location);
            return true;
         });
      }

      menu.addMenuOpeningHandler(player -> {
         for (int slotx : CELL_SLOTS) {
            DriveCellSlots.refreshCellLore(menu, slotx);
         }

         enqueueImmediateSync(location);
      });
      menu.addMenuCloseHandler(p -> {
         CellDrive.getStorage().invalidateCellCache(location);
         enqueueImmediateSync(location);
      });
   }

   private static void enqueueImmediateSync(@NotNull Location location) {
      Bukkit.getScheduler().runTask(Networks.getInstance(), () -> {
         BlockMenu menu = StorageCacheUtils.getMenu(location);
         if (menu != null) {
            syncMenuMembers(menu);
         }
      });
   }

   private static void invalidateChannelPeers(@Nullable String channel) {
      if (channel != null) {
         DriveStorage storage = CellDrive.getStorage();

         for (Entry<Location, EnderDrive.EnderDriveState> entry : STATES.entrySet()) {
            if (channel.equals(entry.getValue().channel)) {
               storage.invalidateCellCache(entry.getKey());
            }
         }
      }
   }

   private static void startSyncTimerOnce() {
      if (SYNC_TIMER_STARTED.compareAndSet(false, true)) {
         Bukkit.getScheduler().runTaskTimer(Networks.getInstance(), () -> {
            if (Networks.getInstance().isEnabled() && !STATES.isEmpty()) {
               for (Location location : List.copyOf(STATES.keySet())) {
                  BlockMenu menu = StorageCacheUtils.getMenu(location);
                  if (menu == null) {
                     dropState(location);
                  } else {
                     syncMenuMembers(menu);
                  }
               }
            }
         }, 20L, 20L);
      }
   }

   static void syncMenuMembers(@NotNull BlockMenu menu) {
      Location location = menu.getLocation();
      EnderDrive.EnderDriveState state = state(location);
      if (slotsChanged(state, menu)) {
         Set<String> current = collectCellUuids(menu, getChannel(location));
         if (!current.equals(state.syncedMembers)) {
            applyMemberDiff(state, location, current);
         }
      }
   }

   private static boolean slotsChanged(@NotNull EnderDrive.EnderDriveState state, @NotNull BlockMenu menu) {
      for (int i = 0; i < CELL_SLOTS.length; i++) {
         if (menu.getItemInSlot(CELL_SLOTS[i]) != state.slotRefs[i]) {
            for (int j = 0; j < CELL_SLOTS.length; j++) {
               state.slotRefs[j] = menu.getItemInSlot(CELL_SLOTS[j]);
            }

            return true;
         }
      }

      return false;
   }

   @NotNull
   private static Set<String> collectCellUuids(@NotNull BlockMenu menu, @Nullable String channel) {
      Set<String> current = new HashSet<>();

      for (int slot : CELL_SLOTS) {
         ItemStack item = menu.getItemInSlot(slot);
         if (item != null && !item.getType().isAir()) {
            String uuid = CellUniqueness.getUuidString(item);
            if (uuid != null) {
               if (channel != null) {
                  StorageCell.loadCellCache(item, StorageCell.getPerTypeLimit(item));
               }

               current.add(uuid);
            }
         }
      }

      return current;
   }

   private static void applyMemberDiff(@NotNull EnderDrive.EnderDriveState state, @NotNull Location location, @NotNull Set<String> current) {
      Set<String> syncedBefore = state.syncedMembers;
      String channel = getChannel(location);
      EnderChannelController controller = EnderChannelController.getInstance();
      boolean allOk = true;
      if (channel == null) {
         for (String uuid : syncedBefore) {
            allOk &= controller.unbindCell(uuid);
         }
      } else {
         for (String uuid : current) {
            if (!syncedBefore.contains(uuid)) {
               allOk &= controller.bind(channel, uuid);
            }
         }

         for (String uuidx : syncedBefore) {
            if (!current.contains(uuidx)) {
               allOk &= controller.unbindCell(uuidx);
            }
         }
      }

      if (allOk) {
         state.syncedMembers = Set.copyOf(current);
         CellDrive.getStorage().invalidateCellCache(location);
         if (channel != null) {
            CHANNEL_MEMBER_COUNTS.put(channel, EnderChannelController.getInstance().getCellUuidsOfChannel(channel).size());
            invalidateChannelPeers(channel);
         }
      }
   }

   private static void refreshMainDisplay(@NotNull BlockMenu menu) {
      String channel = getChannel(menu.getLocation());
      long totalStored = 0L;
      int cellCount = 0;
      List<CellHandle> handles = CellDrive.getStorage().getCells(menu);

      for (CellHandle cell : handles) {
         totalStored += cell.getStoredCount();
         cellCount++;
      }

      ItemHashMap<Long> aggregated = CellDrive.getStorage().getAllCellItemsKeyed(handles);
      menu.replaceExistingItem(4, buildStatusDisplay(cellCount, totalStored, aggregated, channel));
      menu.replaceExistingItem(13, Icons.DRIVE_BROWSE);
      menu.replaceExistingItem(39, DriveWhitelistMenu.whitelistButton(menu.getLocation()));
   }

   @NotNull
   private static ItemStack buildStatusDisplay(int cellCount, long totalStored, @Nullable ItemHashMap<Long> aggregated, @Nullable String channel) {
      ItemStack display = new ItemStack(Material.PAPER);
      display.editMeta(meta -> {
         meta.setDisplayName(MessageFormat.format(ENDER_DISPLAY_NAME_TEMPLATE, channel != null ? channel : CHANNEL_NONE_TEXT));
         List<String> lore = new ArrayList<>();
         lore.add(MessageFormat.format(CellDrive.CELL_COUNT_TEMPLATE, cellCount, CELL_SLOT_COUNT));
         lore.add(MessageFormat.format(CellDrive.TOTAL_ITEMS_TEMPLATE, NumberFormat.formatNumber(totalStored)));
         if (aggregated != null) {
            lore.add(MessageFormat.format(CellDrive.ITEM_TYPES_TEMPLATE, aggregated.size()));
            int shown = 0;

            for (Entry<ItemKey, Long> entry : aggregated.keyEntrySet()) {
               if (shown >= 6) {
                  lore.add(CellDrive.MORE_ITEMS_TEXT);
                  break;
               }

               String name = CellDrive.ITEM_DISPLAY_NAMES.computeIfAbsent(entry.getKey(), k -> ItemStackHelper.getDisplayName(k.getItemStack()));
               lore.add(MessageFormat.format(CellDrive.ITEM_ENTRY_TEMPLATE, name, NumberFormat.formatNumber(entry.getValue())));
               shown++;
            }
         }

         lore.add("");
         lore.add(channel != null ? DISPLAY_SHARED_TEXT : DISPLAY_INDEPENDENT_TEXT);
         if (channel != null) {
            Integer members = CHANNEL_MEMBER_COUNTS.get(channel);
            if (members == null) {
               members = EnderChannelController.getInstance().getCellUuidsOfChannel(channel).size();
               CHANNEL_MEMBER_COUNTS.put(channel, members);
            }

            lore.add(MessageFormat.format(DISPLAY_MEMBERS_TEMPLATE, members));
         }

         lore.add(SNEAK_HINT_TEXT);
         meta.setLore(lore);
      });
      return display;
   }

   private static final class EnderDriveState {
      volatile String channel;
      volatile Set<String> syncedMembers = Set.of();
      volatile long displayStamp;
      final ItemStack[] slotRefs = new ItemStack[CellDrive.CELL_SLOTS.length];
   }
}
