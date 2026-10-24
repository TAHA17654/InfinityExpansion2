package com.ytdd9527.networksexpansion.implementation.machines.cellnet.assembly;

import com.balugaq.netex.utils.Lang;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.drive.DriveOwnership;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.ChatInput;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.GhostItems;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.ItemSearch;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.NetworkUtil;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.NumberFormat;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.ui.BrowseUI;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.ui.CellSlotUI;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.ui.Icons;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.NodeType;
import io.github.sefiraat.networks.slimefun.network.NetworkObject;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
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
import net.guizhanss.minecraft.guizhanlib.gugu.minecraft.helpers.inventory.ItemStackHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AssemblyMonitor extends NetworkObject {
   private static final int PAGE_SIZE = 45;
   private static final int VIEW_SLOT = 45;
   private static final int PREV_SLOT = 47;
   private static final int REFRESH_BACK_SLOT = 49;
   private static final int NEXT_SLOT = 51;
   private static final int SEARCH_SLOT = 52;
   private static final int DETAIL_OPEN = 4;
   private static final int DETAIL_NAME = 2;
   private static final int[] LINE_SLOTS = new int[]{9, 10, 11, 12, 13, 14, 15, 16, 17};
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
   private static final Map<Location, AssemblyMonitor.Session> SESSIONS = new ConcurrentHashMap<>();

   public AssemblyMonitor(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe, NodeType.BRIDGE);
   }

   public void postRegister() {
      new BlockMenuPreset(this.getId(), this.getItemName()) {
         public void init() {
            this.setSize(54);
         }

         public boolean canOpen(@NotNull Block block, @NotNull Player player) {
            return player.hasPermission("slimefun.inventory.bypass")
               || AssemblyMonitor.this.canUse(player, false)
                  && Slimefun.getProtectionManager().hasPermission(player, block.getLocation(), Interaction.INTERACT_BLOCK);
         }

         public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
            return new int[0];
         }

         public void newInstance(@NotNull BlockMenu menu, @NotNull Block block) {
            AssemblyMonitor.SESSIONS.put(block.getLocation(), new AssemblyMonitor.Session());
            AssemblyMonitor.this.setupHandlers(menu);
            AssemblyMonitor.render(menu);
            menu.addMenuOpeningHandler(p -> AssemblyMonitor.render(menu));
            menu.addMenuCloseHandler(p -> AssemblyMonitor.SESSIONS.remove(menu.getLocation()));
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

      this.wireNavigationHandlers(menu);

      for (int slot = 0; slot < 54; slot++) {
         if (slot >= 45 && slot != 45 && slot != 47 && slot != 49 && slot != 51 && slot != 52) {
            menu.addMenuClickHandler(slot, (p, s, i, a) -> false);
         }
      }
   }

   private void wireNavigationHandlers(@NotNull BlockMenu menu) {
      menu.addMenuClickHandler(45, (player, s, clicked, action) -> {
         nextTick(() -> {
            AssemblyMonitor.Session session = session(menu);
            if (session.detail == null) {
               session.view = (session.view + 1) % 2;
               session.page = 0;
               render(menu);
            }
         });
         return false;
      });
      menu.addMenuClickHandler(47, (player, s, clicked, action) -> {
         nextTick(() -> {
            AssemblyMonitor.Session session = session(menu);
            if (session.page > 0) {
               session.page--;
               render(menu);
            }
         });
         return false;
      });
      menu.addMenuClickHandler(51, (player, s, clicked, action) -> {
         nextTick(() -> {
            AssemblyMonitor.Session session = session(menu);
            session.page++;
            render(menu);
         });
         return false;
      });
      menu.addMenuClickHandler(49, (player, s, clicked, action) -> {
         nextTick(() -> {
            AssemblyMonitor.Session session = session(menu);
            if (session.detail != null) {
               session.detail = null;
               session.page = 0;
            }

            render(menu);
         });
         return false;
      });
      menu.addMenuClickHandler(52, (player, s, clicked, action) -> {
         nextTick(() -> this.handleSearchClick(player, menu, action.isRightClicked()));
         return false;
      });
   }

   private void handleSearchClick(@NotNull Player player, @NotNull BlockMenu menu, boolean rightClick) {
      AssemblyMonitor.Session session = session(menu);
      if (session.detail == null) {
         if (rightClick) {
            if (session.search != null) {
               session.search = null;
               session.page = 0;
               render(menu);
               player.sendMessage(Lang.getString("messages.cellnet.monitor.search_cleared"));
            }
         } else {
            ItemSearch.requestSearch(player, ChatInput.SearchTarget.MONITOR, menu.getLocation(), null, null);
         }
      }
   }

   public static void applySearchFromChat(@NotNull Player player, @NotNull Location location, @NotNull String term) {
      BlockMenu menu = StorageCacheUtils.getMenu(location);
      if (menu != null) {
         AssemblyMonitor.Session session = session(menu);
         session.search = term.isEmpty() ? null : term;
         session.page = 0;
         render(menu);
         player.sendMessage(Lang.getString(term.isEmpty() ? "messages.cellnet.monitor.search_cleared" : "messages.cellnet.monitor.search_set", term));
         menu.open(new Player[]{player});
      }
   }

   private static void lineAction(@NotNull Player player, @NotNull Location drive, int index, boolean rightClick, boolean shift) {
      if (shift) {
         if (rightClick) {
            AssemblyMonitorBridge.requestQuantityFromMonitor(player, drive, index);
         } else {
            AssemblyMonitorBridge.cycleLineFromMonitor(player, drive, index);
         }
      } else {
         AssemblyMonitorBridge.changeGearFromMonitor(player, drive, index, rightClick ? -1 : 1);
      }
   }

   private void handleGridClick(@NotNull Player player, @NotNull BlockMenu menu, int pos, boolean rightClick, boolean shift) {
      AssemblyMonitor.Session session = session(menu);
      if (session.detail != null) {
         if (pos == 4) {
            openDrive(player, session.detail);
         } else if (pos == 2) {
            if (rightClick) {
               AssemblyMonitorBridge.clearNameFromMonitor(player, session.detail);
               render(menu);
            } else {
               boolean replaced = ChatInput.request(player, ChatInput.InputType.DRIVE_NAME, new ChatInput.DriveNameContext(session.detail));
               if (replaced) {
                  player.sendMessage(Lang.getString("messages.cellnet.input.previous_cancelled"));
               }

               player.sendMessage(Lang.getString("messages.cellnet.monitor.name_hint"));
            }
         } else {
            int line = CellSlotUI.indexOf(LINE_SLOTS, pos);
            if (line >= 0) {
               List<AssemblyMonitorBridge.MonitorLine> lines = AssemblyMonitorBridge.readLines(session.detail);
               if (line < lines.size()) {
                  lineAction(player, session.detail, line, rightClick, shift);
               }
            }

            render(menu);
         }
      } else if (session.view == 1) {
         List<AssemblyMonitor.LineEntry> entries = collectLines(menu);
         int index = session.page * 45 + pos;
         if (index >= 0 && index < entries.size()) {
            AssemblyMonitor.LineEntry entry = entries.get(index);
            lineAction(player, entry.drive(), entry.index(), rightClick, shift);
         }

         render(menu);
      } else {
         List<AssemblyMonitor.DriveView> drives = collectDriveViews(menu);
         int index = session.page * 45 + pos;
         if (index >= 0 && index < drives.size()) {
            Location drive = drives.get(index).location();
            if (rightClick) {
               openDrive(player, drive);
            } else {
               session.detail = drive;
               session.page = 0;
               render(menu);
            }
         }
      }
   }

   private static void render(@NotNull BlockMenu menu) {
      AssemblyMonitor.Session session = session(menu);
      if (session.detail != null) {
         renderDetail(menu, session);
      } else if (session.view == 1) {
         renderAllLines(menu, session);
      } else {
         renderOverview(menu, session);
      }
   }

   private static void renderOverview(@NotNull BlockMenu menu, @NotNull AssemblyMonitor.Session session) {
      List<AssemblyMonitor.DriveView> drives = collectDriveViews(menu);
      if (session.search != null) {
         drives = filterDrives(drives, session.search);
      }

      List<AssemblyMonitor.DriveView> shownDrives = drives;
      int pages = Math.max(1, (drives.size() + 45 - 1) / 45);
      session.page = Math.min(session.page, pages - 1);
      session.page = Math.max(session.page, 0);
      menu.replaceExistingItem(45, simpleIcon(Material.COMPASS, Lang.getString("messages.cellnet.monitor.view_all")));
      menu.replaceExistingItem(
         47, BrowseUI.pageButton(Lang.getString(session.page > 0 ? "messages.cellnet.monitor.prev_page" : "messages.cellnet.monitor.first_page"))
      );
      menu.replaceExistingItem(49, simpleIcon(Material.SUNFLOWER, Lang.getString("messages.cellnet.monitor.refresh_name")));
      menu.replaceExistingItem(
         51, BrowseUI.pageButton(Lang.getString(session.page < pages - 1 ? "messages.cellnet.monitor.next_page" : "messages.cellnet.monitor.last_page"))
      );
      menu.replaceExistingItem(52, searchIcon(session.search));
      fillGrid(menu, session.page, shownDrives.size(), i -> driveIcon(shownDrives.get(i)));
   }

   private static void renderAllLines(@NotNull BlockMenu menu, @NotNull AssemblyMonitor.Session session) {
      List<AssemblyMonitor.LineEntry> entries = collectLines(menu);
      if (session.search != null) {
         entries = filterLines(entries, session.search);
      }

      List<AssemblyMonitor.LineEntry> shownEntries = entries;
      int pages = Math.max(1, (entries.size() + 45 - 1) / 45);
      session.page = Math.min(session.page, pages - 1);
      session.page = Math.max(session.page, 0);
      menu.replaceExistingItem(45, simpleIcon(Material.WRITABLE_BOOK, Lang.getString("messages.cellnet.monitor.view_lines")));
      menu.replaceExistingItem(
         47, BrowseUI.pageButton(Lang.getString(session.page > 0 ? "messages.cellnet.monitor.prev_page" : "messages.cellnet.monitor.first_page"))
      );
      menu.replaceExistingItem(49, simpleIcon(Material.SUNFLOWER, Lang.getString("messages.cellnet.monitor.refresh_name")));
      menu.replaceExistingItem(
         51, BrowseUI.pageButton(Lang.getString(session.page < pages - 1 ? "messages.cellnet.monitor.next_page" : "messages.cellnet.monitor.last_page"))
      );
      menu.replaceExistingItem(52, searchIcon(session.search));
      if (entries.isEmpty()) {
         menu.replaceExistingItem(
            GRID_SLOTS[4],
            simpleIcon(Material.BARRIER, Lang.getString(session.search != null ? "messages.cellnet.monitor.search_empty" : "messages.cellnet.monitor.all_none"))
         );
      }

      fillGrid(menu, session.page, shownEntries.size(), i -> {
         AssemblyMonitor.LineEntry entry = shownEntries.get(i);
         return lineItem(entry.line(), entry.driveName());
      });
   }

   private static void renderDetail(@NotNull BlockMenu menu, @NotNull AssemblyMonitor.Session session) {
      Location driveLocation = session.detail;
      AssemblyMonitorBridge.DriveOverview overview = driveLocation == null ? null : AssemblyMonitorBridge.readOverview(driveLocation);
      if (overview == null) {
         session.detail = null;
         renderOverview(menu, session);
      } else {
         for (int slot = 0; slot < 54; slot++) {
            if (slot != 4 && slot != 2 && (slot < 9 || slot > 17) && slot != 49) {
               menu.replaceExistingItem(slot, ChestMenuUtils.getBackground());
            }
         }

         String title = overview.name() != null
            ? overview.name()
            : Lang.getString("messages.cellnet.monitor.drive_name", driveLocation.getBlockX(), driveLocation.getBlockY(), driveLocation.getBlockZ());
         ItemStack header = simpleIcon(Material.ENDER_EYE, title);
         header.editMeta(meta -> {
            List<String> lore = new ArrayList<>();
            lore.add(Lang.getString(overview.enabled() ? "messages.cellnet.monitor.drive_status_on" : "messages.cellnet.monitor.drive_status_off"));
            if (overview.overclock()) {
               lore.add(Lang.getString("messages.cellnet.monitor.core_overclock"));
            }

            if (overview.smart()) {
               lore.add(Lang.getString("messages.cellnet.monitor.core_smart"));
            }

            if (!overview.overclock() && !overview.smart()) {
               lore.add(Lang.getString("messages.cellnet.monitor.core_none"));
            }

            meta.setLore(lore);
         });
         menu.replaceExistingItem(4, header);
         menu.replaceExistingItem(2, buildNameButton(overview.name()));
         menu.replaceExistingItem(49, BrowseUI.pageButton(Lang.getString("messages.cellnet.monitor.back")));
         List<AssemblyMonitorBridge.MonitorLine> lines = overview.lines();

         for (int i = 0; i < LINE_SLOTS.length; i++) {
            int slotx = LINE_SLOTS[i];
            if (i < lines.size()) {
               menu.replaceExistingItem(slotx, lineItem(lines.get(i), overview.name()));
            } else {
               menu.replaceExistingItem(slotx, ChestMenuUtils.getBackground());
            }
         }
      }
   }

   private static void fillGrid(@NotNull BlockMenu menu, int page, int size, @NotNull IntFunction<ItemStack> factory) {
      for (int i = 0; i < 45; i++) {
         int slot = GRID_SLOTS[i];
         int index = page * 45 + i;
         menu.replaceExistingItem(slot, index < size ? factory.apply(index) : ChestMenuUtils.getBackground());
      }
   }

   @NotNull
   private static ItemStack buildNameButton(@Nullable String name) {
      ItemStack icon = simpleIcon(Material.NAME_TAG, Lang.getString("messages.cellnet.monitor.name_button"));
      icon.editMeta(meta -> {
         List<String> lore = new ArrayList<>();
         lore.add(name != null ? Lang.getString("messages.cellnet.monitor.name_current", name) : Lang.getString("messages.cellnet.monitor.name_none"));
         lore.add(Lang.getString("messages.cellnet.monitor.name_action_gear"));
         lore.add(Lang.getString("messages.cellnet.monitor.name_action_clear"));
         meta.setLore(lore);
      });
      return icon;
   }

   @NotNull
   private static ItemStack searchIcon(@Nullable String search) {
      ItemStack icon = simpleIcon(Material.SPYGLASS, Lang.getString("messages.cellnet.monitor.search_name"));
      icon.editMeta(meta -> {
         List<String> lore = new ArrayList<>();
         lore.add(Lang.getString(search == null ? "messages.cellnet.monitor.search_none" : "messages.cellnet.monitor.search_current", search));
         lore.add(Lang.getString("messages.cellnet.monitor.search_hint_button"));
         meta.setLore(lore);
      });
      return icon;
   }

   @NotNull
   private static List<AssemblyMonitor.DriveView> filterDrives(@NotNull List<AssemblyMonitor.DriveView> drives, @NotNull String search) {
      List<AssemblyMonitor.DriveView> filtered = new ArrayList<>(drives.size());

      for (AssemblyMonitor.DriveView view : drives) {
         Location location = view.location();
         String coords = location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ();
         if (matchesSearch(search, view.name(), coords)) {
            filtered.add(view);
         }
      }

      return filtered;
   }

   @NotNull
   private static List<AssemblyMonitor.LineEntry> filterLines(@NotNull List<AssemblyMonitor.LineEntry> entries, @NotNull String search) {
      List<AssemblyMonitor.LineEntry> filtered = new ArrayList<>(entries.size());

      for (AssemblyMonitor.LineEntry entry : entries) {
         String itemName = ItemStackHelper.getDisplayName(entry.line().output());
         if (matchesSearch(search, entry.driveName(), itemName)) {
            filtered.add(entry);
         }
      }

      return filtered;
   }

   private static boolean matchesSearch(@NotNull String search, @Nullable String... candidates) {
      return ItemSearch.matches(search, candidates);
   }

   @NotNull
   private static ItemStack lineItem(@NotNull AssemblyMonitorBridge.MonitorLine line, @Nullable String driveName) {
      ItemStack icon = line.output().clone();
      icon.setAmount(1);
      icon.editMeta(
         meta -> {
            meta.setDisplayName(ItemStackHelper.getDisplayName(line.output()));
            List<String> lore = new ArrayList<>();
            if (line.mode() == 1L) {
               if (!line.planArmed()) {
                  lore.add(Lang.getString("messages.cellnet.monitor.line_mode_plan_unset"));
               } else {
                  lore.add(
                     Lang.getString(
                        line.remaining() > 0L ? "messages.cellnet.monitor.line_mode_plan" : "messages.cellnet.monitor.line_mode_plan_done",
                        NumberFormat.formatNumber(Math.max(0L, line.remaining()))
                     )
                  );
               }
            } else if (line.mode() == 2L) {
               lore.add(
                  Lang.getString(
                     line.keep() > 0L ? "messages.cellnet.monitor.line_mode_replenish" : "messages.cellnet.monitor.line_mode_replenish_unset",
                     NumberFormat.formatNumber(Math.max(0L, line.keep()))
                  )
               );
            } else {
               lore.add(Lang.getString("messages.cellnet.monitor.line_mode_continuous"));
            }

            lore.add(Lang.getString(line.gear() > 0 ? "messages.cellnet.monitor.line_gear" : "messages.cellnet.monitor.line_gear_stop", speedText(line.gear())));
            if (line.blocked()) {
               lore.add(Lang.getString("messages.cellnet.monitor.line_blocked"));
            }

            if (driveName != null) {
               lore.add(Lang.getString("messages.cellnet.monitor.line_belongs", driveName));
            }

            lore.add(Lang.getString("messages.cellnet.monitor.line_hint_gear"));
            lore.add(Lang.getString("messages.cellnet.monitor.line_hint_mode"));
            meta.setLore(lore);
         }
      );
      GhostItems.mark(icon);
      return icon;
   }

   @NotNull
   private static String speedText(int gear) {
      long multiplier = AssemblyMonitorBridge.gearMultiplier(gear);
      return multiplier < 0L ? "\u00d7\u221e" : "\u00d7" + NumberFormat.formatNumber(multiplier);
   }

   @NotNull
   private static ItemStack driveIcon(@NotNull AssemblyMonitor.DriveView view) {
      ItemStack icon = Icons.DRIVE_BROWSE.clone();
      icon.editMeta(
         meta -> {
            String title = view.name() != null
               ? view.name()
               : Lang.getString("messages.cellnet.monitor.drive_name", view.location.getBlockX(), view.location.getBlockY(), view.location.getBlockZ());
            meta.setDisplayName(title);
            List<String> lore = new ArrayList<>();
            lore.add(Lang.getString(view.enabled ? "messages.cellnet.monitor.drive_status_on" : "messages.cellnet.monitor.drive_status_off"));
            List<String> cores = new ArrayList<>();
            if (view.overclock) {
               cores.add(Lang.getString("messages.cellnet.monitor.core_overclock"));
            }

            if (view.smart) {
               cores.add(Lang.getString("messages.cellnet.monitor.core_smart"));
            }

            lore.add(cores.isEmpty() ? Lang.getString("messages.cellnet.monitor.core_none") : String.join(" \u00b7 ", cores));
            lore.add(Lang.getString("messages.cellnet.monitor.drive_lines", view.activeLines()));
            if (view.blockedLines() > 0) {
               lore.add(Lang.getString("messages.cellnet.monitor.drive_blocked", NumberFormat.formatNumber((long)view.blockedLines())));
            }

            lore.add(Lang.getString("messages.cellnet.monitor.drive_hint"));
            meta.setLore(lore);
         }
      );
      return icon;
   }

   @NotNull
   private static ItemStack simpleIcon(@NotNull Material material, @NotNull String name) {
      ItemStack item = new ItemStack(material);
      item.editMeta(meta -> meta.setDisplayName(name));
      GhostItems.mark(item);
      return item;
   }

   @NotNull
   private static AssemblyMonitor.Session session(@NotNull BlockMenu menu) {
      return SESSIONS.computeIfAbsent(menu.getLocation(), k -> new AssemblyMonitor.Session());
   }

   @NotNull
   private static List<AssemblyMonitorBridge.DriveOverview> collectOverviews(@NotNull BlockMenu menu) {
      List<AssemblyMonitorBridge.DriveOverview> list = new ArrayList<>();
      NetworkRoot root = NetworkUtil.findRoot(menu.getLocation());
      if (root == null) {
         return list;
      } else {
         Set<Location> visited = new HashSet<>();

         for (Location nodeLocation : root.getNodeLocations()) {
            for (BlockFace face : BlockFace.values()) {
               if (face.isCartesian()) {
                  Location neighbor = nodeLocation.clone().add(face.getDirection());
                  if (AssemblyMonitorBridge.activeDrives().contains(neighbor) && visited.add(neighbor)) {
                     AssemblyMonitorBridge.DriveOverview overview = AssemblyMonitorBridge.readOverview(neighbor);
                     if (overview != null) {
                        list.add(overview);
                     }
                  }
               }
            }
         }

         return list;
      }
   }

   @NotNull
   private static List<AssemblyMonitor.DriveView> collectDriveViews(@NotNull BlockMenu menu) {
      List<AssemblyMonitor.DriveView> list = new ArrayList<>();

      for (AssemblyMonitorBridge.DriveOverview overview : collectOverviews(menu)) {
         int active = 0;
         int blocked = 0;

         for (AssemblyMonitorBridge.MonitorLine line : overview.lines()) {
            if (line.gear() > 0) {
               active++;
            }

            if (line.blocked()) {
               blocked++;
            }
         }

         list.add(
            new AssemblyMonitor.DriveView(overview.location(), overview.name(), overview.enabled(), overview.overclock(), overview.smart(), active, blocked)
         );
      }

      list.sort((a, b) -> Integer.compare(b.blockedLines() * 2 + (b.enabled() ? 0 : 1), a.blockedLines() * 2 + (a.enabled() ? 0 : 1)));
      return list;
   }

   @NotNull
   private static List<AssemblyMonitor.LineEntry> collectLines(@NotNull BlockMenu menu) {
      List<AssemblyMonitor.LineEntry> entries = new ArrayList<>();

      for (AssemblyMonitorBridge.DriveOverview overview : collectOverviews(menu)) {
         if (overview.name() != null) {
            List<AssemblyMonitorBridge.MonitorLine> lines = overview.lines();

            for (int i = 0; i < lines.size(); i++) {
               entries.add(new AssemblyMonitor.LineEntry(overview.location(), overview.name(), i, lines.get(i)));
            }
         }
      }

      entries.sort((a, b) -> a.driveName().compareToIgnoreCase(b.driveName()));
      return entries;
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

   private static record DriveView(
      @NotNull Location location, @Nullable String name, boolean enabled, boolean overclock, boolean smart, int activeLines, int blockedLines
   ) {
   }

   private static record LineEntry(@NotNull Location drive, @NotNull String driveName, int index, @NotNull AssemblyMonitorBridge.MonitorLine line) {
   }

   private static final class Session {
      int page;
      int view;
      @Nullable
      Location detail;
      @Nullable
      String search;
   }
}
