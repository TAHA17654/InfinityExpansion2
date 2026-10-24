package com.ytdd9527.networksexpansion.implementation.machines.cellnet.drive;

import com.balugaq.netex.utils.Lang;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.api.DriveType;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.CellHandle;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.ChatInput;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.GhostItems;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.ItemSearch;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.NetworkUtil;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.NumberFormat;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.ui.BrowseUI;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.NodeType;
import io.github.sefiraat.networks.slimefun.network.NetworkObject;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntFunction;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DriveMonitor extends NetworkObject {
   private static final String NAME_KEY = "drive_name";
   private static final int PAGE_SIZE = 45;
   private static final int PREV_SLOT = 47;
   private static final int REFRESH_SLOT = 49;
   private static final int NEXT_SLOT = 51;
   private static final int SEARCH_SLOT = 52;
   private static final int[] GRID_SLOTS = new int[]{
      0,
      1,
      2,
      3,
      4,
      5,
      6,
      7,
      8,
      9,
      10,
      11,
      12,
      13,
      14,
      15,
      16,
      17,
      18,
      19,
      20,
      21,
      22,
      23,
      24,
      25,
      26,
      27,
      28,
      29,
      30,
      31,
      32,
      33,
      34,
      35,
      36,
      37,
      38,
      39,
      40,
      41,
      42,
      43,
      44
   };
   private static final Map<Location, DriveMonitor.Session> SESSIONS = new ConcurrentHashMap<>();

   public DriveMonitor(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe, NodeType.BRIDGE);
   }

   public void postRegister() {
      new BlockMenuPreset(this.getId(), this.getItemName()) {
         public void init() {
            this.setSize(54);
         }

         public boolean canOpen(@NotNull Block block, @NotNull Player player) {
            return player.hasPermission("slimefun.inventory.bypass")
               || DriveMonitor.this.canUse(player, false)
                  && Slimefun.getProtectionManager().hasPermission(player, block.getLocation(), Interaction.INTERACT_BLOCK);
         }

         public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
            return new int[0];
         }

         public void newInstance(@NotNull BlockMenu menu, @NotNull Block block) {
            DriveMonitor.SESSIONS.put(block.getLocation(), new DriveMonitor.Session());
            DriveMonitor.this.setupHandlers(menu);
            DriveMonitor.render(menu);
            menu.addMenuOpeningHandler(p -> DriveMonitor.render(menu));
            menu.addMenuCloseHandler(p -> DriveMonitor.SESSIONS.remove(menu.getLocation()));
         }
      };
   }

   private static void nextTick(@NotNull Runnable action) {
      Bukkit.getScheduler().runTask(Networks.getInstance(), action);
   }

   private void setupHandlers(@NotNull BlockMenu menu) {
      for (int slot = 0; slot < 45; slot++) {
         int pos = slot;
         menu.addMenuClickHandler(pos, (player, s, clicked, action) -> {
            nextTick(() -> this.handleGridClick(player, menu, pos, action.isRightClicked(), action.isShiftClicked()));
            return false;
         });
      }

      menu.addMenuClickHandler(47, (player, s, clicked, action) -> {
         nextTick(() -> {
            DriveMonitor.Session session = session(menu);
            if (session.page > 0) {
               session.page--;
               render(menu);
            }
         });
         return false;
      });
      menu.addMenuClickHandler(51, (player, s, clicked, action) -> {
         nextTick(() -> {
            DriveMonitor.Session session = session(menu);
            session.page++;
            render(menu);
         });
         return false;
      });
      menu.addMenuClickHandler(49, (player, s, clicked, action) -> {
         nextTick(() -> render(menu));
         return false;
      });
      menu.addMenuClickHandler(52, (player, s, clicked, action) -> {
         nextTick(() -> this.handleSearchClick(player, menu, action.isRightClicked()));
         return false;
      });

      for (int slot = 45; slot < 54; slot++) {
         if (slot != 47 && slot != 49 && slot != 51 && slot != 52) {
            menu.addMenuClickHandler(slot, (p, s, i, a) -> false);
         }
      }
   }

   private void handleSearchClick(@NotNull Player player, @NotNull BlockMenu menu, boolean rightClick) {
      DriveMonitor.Session session = session(menu);
      if (rightClick) {
         if (session.search != null) {
            session.search = null;
            session.page = 0;
            render(menu);
            player.sendMessage(Lang.getString("messages.cellnet.manager.search_cleared"));
         }
      } else {
         ItemSearch.requestSearch(player, ChatInput.SearchTarget.DRIVE_MANAGER, menu.getLocation(), null, null);
      }
   }

   public static void applySearchFromChat(@NotNull Player player, @NotNull Location location, @NotNull String term) {
      BlockMenu menu = StorageCacheUtils.getMenu(location);
      if (menu != null) {
         DriveMonitor.Session session = session(menu);
         session.search = term.isEmpty() ? null : term;
         session.page = 0;
         render(menu);
         player.sendMessage(Lang.getString(term.isEmpty() ? "messages.cellnet.manager.search_cleared" : "messages.cellnet.manager.search_set", term));
         menu.open(new Player[]{player});
      }
   }

   public static void applyNameFromChat(@NotNull Player player, @NotNull Location location, @NotNull String name) {
      if (!DriveOwnership.bypasses(player) && !DriveOwnership.isOwnerOrWhitelisted(player, location)) {
         player.sendMessage(Lang.getString("messages.cellnet.monitor.no_permission"));
      } else {
         String trimmed = name.trim();
         if (trimmed.length() > 16) {
            player.sendMessage(Lang.getString("messages.cellnet.monitor.name_invalid"));
         } else {
            SlimefunBlockData blockData = StorageCacheUtils.getBlock(location);
            if (blockData != null) {
               if (!trimmed.isEmpty() && !"-".equals(trimmed)) {
                  blockData.setData("drive_name", trimmed);
                  player.sendMessage(Lang.getString("messages.cellnet.manager.name_set", trimmed));
               } else {
                  blockData.setData("drive_name", "");
                  player.sendMessage(Lang.getString("messages.cellnet.manager.name_cleared"));
               }
            }
         }
      }
   }

   @Nullable
   public static String displayName(@NotNull Location location) {
      String stored = StorageCacheUtils.getData(location, "drive_name");
      return stored != null && !stored.isEmpty() ? stored : null;
   }

   private void handleGridClick(@NotNull Player player, @NotNull BlockMenu menu, int pos, boolean rightClick, boolean shift) {
      DriveMonitor.Session session = session(menu);
      List<DriveMonitor.DriveView> drives = collectDriveViews(menu);
      if (session.search != null) {
         drives = filterDrives(drives, session.search);
      }

      int index = session.page * 45 + pos;
      if (index >= 0 && index < drives.size()) {
         Location drive = drives.get(index).location();
         if (shift) {
            if (rightClick) {
               this.clearName(player, drive);
            } else {
               this.requestRename(player, drive);
            }
         } else if (rightClick) {
            DriveGuide.start(player, drive);
         } else {
            openDrive(player, drive);
         }
      }
   }

   private void requestRename(@NotNull Player player, @NotNull Location drive) {
      if (!DriveOwnership.bypasses(player) && !DriveOwnership.isOwnerOrWhitelisted(player, drive)) {
         player.sendMessage(Lang.getString("messages.cellnet.monitor.no_permission"));
      } else {
         player.closeInventory();
         boolean replaced = ChatInput.request(player, ChatInput.InputType.MANAGER_DRIVE_NAME, new ChatInput.DriveNameContext(drive));
         if (replaced) {
            player.sendMessage(Lang.getString("messages.cellnet.input.previous_cancelled"));
         }

         player.sendMessage(Lang.getString("messages.cellnet.monitor.name_hint"));
      }
   }

   private void clearName(@NotNull Player player, @NotNull Location drive) {
      if (!DriveOwnership.bypasses(player) && !DriveOwnership.isOwnerOrWhitelisted(player, drive)) {
         player.sendMessage(Lang.getString("messages.cellnet.monitor.no_permission"));
      } else {
         SlimefunBlockData blockData = StorageCacheUtils.getBlock(drive);
         if (blockData != null) {
            blockData.setData("drive_name", "");
            player.sendMessage(Lang.getString("messages.cellnet.manager.name_cleared"));
         }
      }
   }

   private static void openDrive(@NotNull Player player, @NotNull Location drive) {
      if (!DriveOwnership.bypasses(player) && !DriveOwnership.isOwnerOrWhitelisted(player, drive)) {
         player.sendMessage(Lang.getString("messages.cellnet.monitor.no_permission"));
      } else {
         BlockMenu menu = StorageCacheUtils.getMenu(drive);
         if (menu == null) {
            player.sendMessage(Lang.getString("messages.cellnet.monitor.drive_not_loaded"));
         } else if (!menu.getPreset().canOpen(drive.getBlock(), player)) {
            player.sendMessage(Lang.getString("messages.cellnet.monitor.no_permission"));
         } else {
            menu.open(new Player[]{player});
         }
      }
   }

   private static void render(@NotNull BlockMenu menu) {
      DriveMonitor.Session session = session(menu);
      List<DriveMonitor.DriveView> drives = collectDriveViews(menu);
      if (session.search != null) {
         drives = filterDrives(drives, session.search);
      }

      List<DriveMonitor.DriveView> shownDrives = drives;
      int pages = Math.max(1, (drives.size() + 45 - 1) / 45);
      session.page = Math.min(session.page, pages - 1);
      session.page = Math.max(session.page, 0);
      menu.replaceExistingItem(
         47, BrowseUI.pageButton(Lang.getString(session.page > 0 ? "messages.cellnet.monitor.prev_page" : "messages.cellnet.monitor.first_page"))
      );
      menu.replaceExistingItem(49, simpleIcon(Material.SUNFLOWER, Lang.getString("messages.cellnet.monitor.refresh_name")));
      menu.replaceExistingItem(
         51, BrowseUI.pageButton(Lang.getString(session.page < pages - 1 ? "messages.cellnet.monitor.next_page" : "messages.cellnet.monitor.last_page"))
      );
      menu.replaceExistingItem(52, searchIcon(session.search));

      for (int slot = 45; slot < 54; slot++) {
         if (slot != 47 && slot != 49 && slot != 51 && slot != 52) {
            menu.replaceExistingItem(slot, ChestMenuUtils.getBackground());
         }
      }

      if (shownDrives.isEmpty()) {
         menu.replaceExistingItem(
            GRID_SLOTS[22],
            simpleIcon(Material.BARRIER, Lang.getString(session.search != null ? "messages.cellnet.manager.search_empty" : "messages.cellnet.manager.empty"))
         );
      }

      fillGrid(menu, session.page, shownDrives.size(), i -> driveIcon(shownDrives.get(i)));
   }

   private static void fillGrid(@NotNull BlockMenu menu, int page, int size, @NotNull IntFunction<ItemStack> factory) {
      for (int i = 0; i < 45; i++) {
         int slot = GRID_SLOTS[i];
         int index = page * 45 + i;
         menu.replaceExistingItem(slot, index < size ? factory.apply(index) : ChestMenuUtils.getBackground());
      }
   }

   @NotNull
   private static ItemStack driveIcon(@NotNull DriveMonitor.DriveView view) {
      ItemStack icon = view.icon().clone();
      icon.setAmount(1);
      icon.editMeta(
         meta -> {
            String title = view.name() != null
               ? view.name()
               : Lang.getString("messages.cellnet.manager.drive_name", view.location().getBlockX(), view.location().getBlockY(), view.location().getBlockZ());
            meta.setDisplayName(title);
            List<String> lore = new ArrayList<>();
            lore.add(Lang.getString(view.type() == DriveType.ENDER ? "messages.cellnet.manager.type_ender" : "messages.cellnet.manager.type_standard"));
            lore.add(Lang.getString("messages.cellnet.manager.cells", view.cells()));
            lore.add(Lang.getString("messages.cellnet.manager.stored", NumberFormat.formatNumber(view.stored())));
            lore.add(Lang.getString("messages.cellnet.manager.drive_hint"));
            lore.add(Lang.getString("messages.cellnet.manager.drive_hint_name"));
            meta.setLore(lore);
         }
      );
      GhostItems.mark(icon);
      return icon;
   }

   @NotNull
   private static ItemStack searchIcon(@Nullable String search) {
      ItemStack icon = simpleIcon(Material.SPYGLASS, Lang.getString("messages.cellnet.manager.search_name"));
      icon.editMeta(meta -> {
         List<String> lore = new ArrayList<>();
         lore.add(Lang.getString(search == null ? "messages.cellnet.manager.search_none" : "messages.cellnet.manager.search_current", search));
         lore.add(Lang.getString("messages.cellnet.manager.search_hint_button"));
         meta.setLore(lore);
      });
      return icon;
   }

   @NotNull
   private static List<DriveMonitor.DriveView> filterDrives(@NotNull List<DriveMonitor.DriveView> drives, @NotNull String search) {
      List<DriveMonitor.DriveView> filtered = new ArrayList<>(drives.size());

      for (DriveMonitor.DriveView view : drives) {
         Location location = view.location();
         String coords = location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ();
         if (ItemSearch.matches(search, view.name(), coords)) {
            filtered.add(view);
         }
      }

      return filtered;
   }

   @NotNull
   private static List<DriveMonitor.DriveView> collectDriveViews(@NotNull BlockMenu menu) {
      List<DriveMonitor.DriveView> list = new ArrayList<>();
      NetworkRoot root = NetworkUtil.findRoot(menu.getLocation());
      if (root == null) {
         return list;
      } else {
         Set<Location> visited = new HashSet<>();

         for (Location nodeLocation : root.getNodeLocations()) {
            for (BlockFace face : BlockFace.values()) {
               if (face.isCartesian()) {
                  Location neighbor = nodeLocation.clone().add(face.getDirection());
                  if (visited.add(neighbor)) {
                     SlimefunItem sfItem = StorageCacheUtils.getSfItem(neighbor);
                     DriveType type = DriveType.of(sfItem);
                     if (type != null) {
                        int cells = 0;
                        long stored = 0L;
                        BlockMenu driveMenu = StorageCacheUtils.getMenu(neighbor);
                        if (driveMenu != null) {
                           List<CellHandle> handles = CellDrive.getStorage().getCells(driveMenu);
                           cells = handles.size();

                           for (long amount : CellDrive.getStorage().getAllCellItems(handles).values()) {
                              stored += amount;
                           }
                        }

                        list.add(new DriveMonitor.DriveView(neighbor, displayName(neighbor), type, cells, stored, sfItem.getItem()));
                     }
                  }
               }
            }
         }

         list.sort((a, b) -> {
            if (a.name() == null != (b.name() == null)) {
               return a.name() == null ? 1 : -1;
            } else if (a.name() != null) {
               return a.name().compareToIgnoreCase(b.name());
            } else {
               Location la = a.location();
               Location lb = b.location();
               int cmp = Integer.compare(la.getBlockX(), lb.getBlockX());
               if (cmp != 0) {
                  return cmp;
               } else {
                  cmp = Integer.compare(la.getBlockY(), lb.getBlockY());
                  return cmp != 0 ? cmp : Integer.compare(la.getBlockZ(), lb.getBlockZ());
               }
            }
         });
         return list;
      }
   }

   @NotNull
   private static ItemStack simpleIcon(@NotNull Material material, @NotNull String name) {
      ItemStack item = new ItemStack(material);
      item.editMeta(meta -> meta.setDisplayName(name));
      GhostItems.mark(item);
      return item;
   }

   @NotNull
   private static DriveMonitor.Session session(@NotNull BlockMenu menu) {
      return SESSIONS.computeIfAbsent(menu.getLocation(), k -> new DriveMonitor.Session());
   }

   private static record DriveView(@NotNull Location location, @Nullable String name, @NotNull DriveType type, int cells, long stored, @NotNull ItemStack icon) {
   }

   private static final class Session {
      int page;
      @Nullable
      String search;
   }
}
