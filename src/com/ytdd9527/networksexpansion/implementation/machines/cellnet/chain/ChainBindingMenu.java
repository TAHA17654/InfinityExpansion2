package com.ytdd9527.networksexpansion.implementation.machines.cellnet.chain;

import com.balugaq.netex.api.enums.TransportMode;
import com.balugaq.netex.utils.Lang;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.rule.CellAcceptRules;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.GhostItems;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.ui.BrowseUI;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.ui.Icons;
import io.github.sefiraat.networks.slimefun.network.NetworkDirectional;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ChainBindingMenu {
   private static final int INFO_SLOT = 4;
   private static final int BACK_SLOT = 45;
   static final int[] DIRECTION_SLOTS = new int[]{10, 11, 12, 14, 15, 16};
   private static final int[] MODE_SLOTS = new int[]{19, 20, 21, 23, 24, 25};
   static final BlockFace[] SLOT_FACES = new BlockFace[]{BlockFace.WEST, BlockFace.NORTH, BlockFace.UP, BlockFace.DOWN, BlockFace.SOUTH, BlockFace.EAST};
   private static final int[] EDIT_SLOTS = new int[]{9, 10, 11, 12, 13, 14, 15, 16, 17};
   private static final int EDIT_INFO_SLOT = 0;
   private static final int EDIT_BACK_SLOT = 8;
   private static final int LIST_PREVIEW_LIMIT = 3;
   private static final LegacyComponentSerializer LEGACY_HEX = LegacyComponentSerializer.builder().hexColors().useUnusualXRepeatedCharacterHexFormat().build();

   private ChainBindingMenu() {
   }

   static boolean in(@NotNull int[] slots, int slot) {
      for (int s : slots) {
         if (s == slot) {
            return true;
         }
      }

      return false;
   }

   public static void openDirections(@NotNull Player player, @NotNull Location machine) {
      ChainBindingMenu.Session session = new ChainBindingMenu.Session(machine, null, 0);
      ChestMenu menu = new ChestMenu(Lang.getString("messages.cellnet.line.binding-title-directions"));
      menu.setPlayerInventoryClickable(false);
      menu.setEmptySlotsClickable(false);

      for (int slot = 0; slot < 54; slot++) {
         if (slot != 4 && slot != 45 && !in(DIRECTION_SLOTS, slot) && !in(MODE_SLOTS, slot)) {
            menu.addItem(slot, ChestMenuUtils.getBackground(), (p, s, i, a) -> false);
         }
      }

      menu.addItem(4, infoIcon(), (p, s, i, a) -> false);
      menu.addItem(45, BrowseUI.backButton(), (p, s, i, a) -> {
         BlockMenu machineMenu = StorageCacheUtils.getMenu(machine);
         if (machineMenu != null) {
            machineMenu.open(new Player[]{p});
         }

         return false;
      });
      renderDirections(menu, session);
      menu.open(new Player[]{player});
   }

   private static void renderDirections(@NotNull ChestMenu menu, @NotNull ChainBindingMenu.Session session) {
      AbstractChainMachine machine = AbstractChainMachine.machineAt(session.machine);

      for (int i = 0; i < SLOT_FACES.length; i++) {
         BlockFace face = SLOT_FACES[i];
         boolean enabled = machine != null && isEnabled(session.machine, face);
         int boundCount = ChainBindingStore.load(session.machine, face).size();
         int lineLength = machine == null ? 0 : targetLine(machine, session.machine, face).size();
         ItemStack icon = directionIcon(session.machine, face, enabled);
         icon.editMeta(meta -> {
            meta.setDisplayName(Lang.getString("messages.cellnet.line.binding-direction-name", directionName(face)));
            List<String> lore = new ArrayList<>();
            lore.add(Lang.getString("messages.cellnet.line.binding-direction-lore", lineLength, boundCount));
            lore.add("");
            lore.add(Lang.getString("messages.cellnet.line.binding-hint-edit"));
            lore.add(Lang.getString("messages.cellnet.line.brush-dir-hint-drag"));
            meta.setLore(lore);
         });
         GhostItems.mark(icon);
         menu.replaceExistingItem(DIRECTION_SLOTS[i], icon);
         menu.addMenuClickHandler(DIRECTION_SLOTS[i], (p, s, it, a) -> {
            if (!enabled) {
               p.sendMessage(Lang.getString("messages.cellnet.line.binding-direction-closed"));
               return false;
            } else {
               if (a.isRightClicked()) {
                  openDrag(p, session.machine, face);
               } else {
                  openTargets(p, session.machine, face);
               }

               return false;
            }
         });
         boolean modeUnlocked = AbstractChainMachine.hasModule(session.machine, ChainModule.MODE);
         TransportMode override = modeUnlocked ? ChainBindingStore.loadDirMode(session.machine, face) : null;
         ItemStack modeIcon = (override != null ? Icons.CHAIN_BINDING_MODE_ON : Icons.LINE_MODE_OFF).clone();
         modeIcon.editMeta(meta -> {
            meta.setDisplayName(Lang.getString("messages.cellnet.line.binding-mode-name"));
            List<String> lore = new ArrayList<>();
            lore.add(modeRow(override == null, Lang.getString("messages.cellnet.line.binding-mode-follow")));

            for (TransportMode mode : TransportMode.values()) {
               lore.add(modeRow(override == mode, Lang.getString("messages.cellnet.line.modes." + mode.name())));
            }

            lore.add(Lang.getString(modeUnlocked ? "messages.cellnet.line.binding-mode-hint" : "messages.cellnet.line.mode-locked"));
            meta.setLore(lore);
         });
         GhostItems.mark(modeIcon);
         menu.replaceExistingItem(MODE_SLOTS[i], modeIcon);
         menu.addMenuClickHandler(MODE_SLOTS[i], (p, s, it, a) -> {
            cycleDirMode(p, session, menu, face, !a.isRightClicked());
            return false;
         });
      }
   }

   private static void cycleDirMode(
      @NotNull Player player, @NotNull ChainBindingMenu.Session session, @NotNull ChestMenu menu, @NotNull BlockFace face, boolean forward
   ) {
      if (!validate(player, session, false)) {
         renderDirections(menu, session);
      } else if (!AbstractChainMachine.hasModule(session.machine, ChainModule.MODE)) {
         player.sendMessage(Lang.getString("messages.cellnet.line.mode-locked"));
         renderDirections(menu, session);
      } else {
         TransportMode current = ChainBindingStore.loadDirMode(session.machine, face);
         TransportMode next = cycleMode(current, forward);
         ChainBindingStore.saveDirMode(session.machine, face, next);
         AbstractChainMachine.LineState state = AbstractChainMachine.state(session.machine);
         Map<BlockFace, TransportMode> overrides = state.dirModes;
         if (overrides != null) {
            if (next == null) {
               overrides.remove(face);
            } else {
               overrides.put(face, next);
            }
         }

         renderDirections(menu, session);
      }
   }

   @Nullable
   private static TransportMode cycleMode(@Nullable TransportMode current, boolean forward) {
      TransportMode[] modes = TransportMode.values();
      if (current == null) {
         return forward ? modes[0] : modes[modes.length - 1];
      } else if (forward) {
         return current.ordinal() + 1 >= modes.length ? null : modes[current.ordinal() + 1];
      } else {
         return current.ordinal() == 0 ? null : modes[current.ordinal() - 1];
      }
   }

   @NotNull
   private static String modeRow(boolean current, @NotNull String name) {
      return Lang.getString(current ? "messages.cellnet.line.mode-row-current" : "messages.cellnet.line.mode-row-other", name);
   }

   public static void openTargets(@NotNull Player player, @NotNull Location machine, @NotNull BlockFace face) {
      ChainBindingMenu.Session session = new ChainBindingMenu.Session(machine, face, 0);
      ChestMenu menu = new ChestMenu(Lang.getString("messages.cellnet.line.binding-title-targets", directionName(face)));
      menu.setPlayerInventoryClickable(false);
      menu.setEmptySlotsClickable(false);

      for (int slot = 0; slot < 54; slot++) {
         if (slot != 0 && slot != 8 && slot != 4 && slot != 45 && !in(BrowseUI.LIST_SLOTS, slot)) {
            menu.addItem(slot, ChestMenuUtils.getBackground(), (p, s, i, a) -> false);
         }
      }

      menu.addItem(45, BrowseUI.backButton(), (p, s, i, a) -> {
         openDirections(p, machine);
         return false;
      });
      renderTargets(menu, session);
      menu.open(new Player[]{player});
   }

   private static void renderTargets(@NotNull ChestMenu menu, @NotNull ChainBindingMenu.Session session) {
      AbstractChainMachine machine = AbstractChainMachine.machineAt(session.machine);
      if (machine != null) {
         List<Location> targets = targetLine(machine, session.machine, session.face);
         Map<Integer, List<ItemStack>> bindings = ChainBindingStore.load(session.machine, session.face);
         int total = targets.size();
         int effDistance = machine.effectiveDistance(AbstractChainMachine.state(session.machine));
         ItemStack info = infoIcon();
         info.editMeta(meta -> {
            meta.setDisplayName(Lang.getString("messages.cellnet.line.binding-direction-name", directionName(session.face)));
            meta.setLore(List.of(Lang.getString("messages.cellnet.line.binding-direction-lore", targets.size(), bindings.size())));
         });
         GhostItems.mark(info);
         menu.replaceExistingItem(4, info);
         menu.addMenuClickHandler(4, (p, s, ix, a) -> false);
         int totalPages = BrowseUI.totalPages(Math.max(1, total));
         if (session.page >= totalPages) {
            session.page = totalPages - 1;
         }

         int start = session.page * BrowseUI.PAGE_SIZE;

         for (int i = 0; i < BrowseUI.LIST_SLOTS.length; i++) {
            int slot = BrowseUI.LIST_SLOTS[i];
            int distance = start + i + 1;
            if (distance > total) {
               menu.replaceExistingItem(slot, total == 0 && i == 0 ? Icons.SEARCH_EMPTY : Icons.PREVIEW_FILL);
               menu.addMenuClickHandler(slot, (p, s, it, a) -> false);
            } else {
               List<ItemStack> bound = bindings.get(distance);
               menu.replaceExistingItem(slot, targetIcon(session, targets, distance, bound, effDistance));
               menu.addMenuClickHandler(slot, (p, s, it, a) -> {
                  handleTargetClick(p, menu, session, distance, a.isRightClicked(), a.isShiftClicked());
                  return false;
               });
            }
         }

         BrowseUI.wirePager(menu, session.page, totalPages, page -> {
            session.page = page;
            renderTargets(menu, session);
         });
      }
   }

   private static void handleTargetClick(
      @NotNull Player player, @NotNull ChestMenu menu, @NotNull ChainBindingMenu.Session session, int distance, boolean rightClick, boolean shiftClick
   ) {
      AbstractChainMachine machine = AbstractChainMachine.machineAt(session.machine);
      if (machine != null) {
         List<Location> targets = targetLine(machine, session.machine, session.face);
         if (rightClick && shiftClick) {
            if (!validate(player, session, false)) {
               renderTargets(menu, session);
            } else {
               AbstractChainMachine.LineState state = AbstractChainMachine.state(session.machine);
               if (state.modeModule) {
                  TransportMode next = cycleMode(ChainBindingStore.loadDirMode(session.machine, session.face), true);
                  ChainBindingStore.saveDirMode(session.machine, session.face, next);
                  Map<BlockFace, TransportMode> overrides = state.dirModes;
                  if (overrides != null) {
                     if (next == null) {
                        overrides.remove(session.face);
                     } else {
                        overrides.put(session.face, next);
                     }
                  }

                  AbstractChainMachine.invalidateBindings(state, session.face);
               }

               renderTargets(menu, session);
            }
         } else if (rightClick) {
            if (distance > targets.size()) {
               renderTargets(menu, session);
            } else {
               openTarget(player, targets.get(distance - 1));
            }
         } else if (shiftClick) {
            clearBinding(player, session.machine, session.face, distance);
            renderTargets(menu, session);
         } else if (distance > targets.size()) {
            renderTargets(menu, session);
         } else {
            openEdit(player, session.machine, session.face, distance);
         }
      }
   }

   @NotNull
   private static ItemStack targetIcon(
      @NotNull ChainBindingMenu.Session session, @NotNull List<Location> targets, int distance, @Nullable List<ItemStack> bound, int effDistance
   ) {
      Location target = targets.get(distance - 1);
      SlimefunItem sfItem = StorageCacheUtils.getSfItem(target);
      ItemStack icon;
      Component machineName;
      if (sfItem != null) {
         icon = sfItem.getItem().clone();
         machineName = displayComponent(icon);
      } else {
         Material material = target.getBlock().getType();
         icon = new ItemStack(material.isItem() && material != Material.AIR ? material : Material.COMPASS);
         machineName = Component.translatable(icon.getType().translationKey());
      }

      icon.setAmount(Math.max(1, Math.min(distance, 64)));
      boolean hasBinding = bound != null;
      icon.editMeta(
         meta -> {
            if (hasBinding) {
               meta.addEnchant(Enchantment.UNBREAKING, 1, true);
               meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
            }

            meta.displayName(machineName);
            List<Component> lore = new ArrayList<>();
            lore.add(legacy(Lang.getString("messages.cellnet.line.binding-target-position", distance, directionName(session.face))));
            lore.add(legacy(statusLine(session, distance, targets.size(), effDistance, bound)));
            AbstractChainMachine.LineState state = AbstractChainMachine.state(session.machine);
            TransportMode override = state.modeModule ? ChainBindingStore.loadDirMode(session.machine, session.face) : null;
            String modeName = override == null
               ? Lang.getString("messages.cellnet.line.binding-mode-follow")
               : Lang.getString("messages.cellnet.line.modes." + override.name());
            lore.add(legacy(Lang.getString("messages.cellnet.line.binding-mode-line", modeName)));
            if (bound != null) {
               int shown = Math.min(3, bound.size());

               for (int i = 0; i < shown; i++) {
                  lore.add(legacy(Lang.getString("messages.cellnet.line.binding-list-item")).append(displayComponent(bound.get(i))));
               }

               if (bound.size() > shown) {
                  lore.add(legacy(Lang.getString("messages.cellnet.line.binding-and-more", bound.size() - shown)));
               }
            }

            lore.add(Component.empty());
            lore.add(legacy(Lang.getString("messages.cellnet.line.binding-hint-edit")));
            lore.add(legacy(Lang.getString("messages.cellnet.line.binding-hint-open")));
            lore.add(legacy(Lang.getString("messages.cellnet.line.binding-hint-unbind")));
            if (state.modeModule) {
               lore.add(legacy(Lang.getString("messages.cellnet.line.binding-hint-cycle-mode")));
            }

            meta.lore(lore);
         }
      );
      GhostItems.mark(icon);
      return icon;
   }

   @NotNull
   static Component displayComponent(@NotNull ItemStack item) {
      ItemMeta meta = item.getItemMeta();
      return (Component)(meta != null && meta.hasDisplayName()
         ? LEGACY_HEX.deserialize(meta.getDisplayName())
         : Component.translatable(item.getType().translationKey()));
   }

   @NotNull
   static Component legacy(@Nullable String text) {
      return LEGACY_HEX.deserialize(text == null ? "" : text);
   }

   @NotNull
   private static String statusLine(@NotNull ChainBindingMenu.Session session, int distance, int lineLength, int effDistance, @Nullable List<ItemStack> bound) {
      if (distance > lineLength) {
         return Lang.getString("messages.cellnet.line.binding-status-unreachable");
      } else if (distance > effDistance) {
         return Lang.getString("messages.cellnet.line.binding-status-distance");
      } else if (bound == null) {
         return Lang.getString("messages.cellnet.line.binding-status-shared");
      } else if (bound.isEmpty()) {
         return Lang.getString("messages.cellnet.line.binding-status-empty");
      } else {
         return isNegCached(session, distance)
            ? Lang.getString("messages.cellnet.line.binding-status-starving")
            : Lang.getString("messages.cellnet.line.binding-status-ok");
      }
   }

   private static boolean isNegCached(@NotNull ChainBindingMenu.Session session, int distance) {
      AbstractChainMachine.LineState state = AbstractChainMachine.state(session.machine);
      Map<BlockFace, Map<Integer, Integer>> neg = state.negCache;
      if (neg == null) {
         return false;
      } else {
         Map<Integer, Integer> rounds = neg.get(session.face);
         return rounds != null && rounds.containsKey(distance);
      }
   }

   public static void openEdit(@NotNull Player player, @NotNull Location machine, @NotNull BlockFace face, int distance) {
      ChainBindingMenu.Session session = new ChainBindingMenu.Session(machine, face, distance);
      ChestMenu menu = new ChestMenu(Lang.getString("messages.cellnet.line.binding-title-edit", directionName(face), distance));
      menu.setPlayerInventoryClickable(true);

      for (int slot = 0; slot < 27; slot++) {
         if (slot != 0 && slot != 8 && !in(EDIT_SLOTS, slot)) {
            menu.addItem(slot, ChestMenuUtils.getBackground(), (p, s, i, a) -> false);
         }
      }

      menu.addItem(8, BrowseUI.backButton(), (p, s, i, a) -> {
         openTargets(p, machine, face);
         return false;
      });
      renderEdit(menu, session);
      menu.open(new Player[]{player});
   }

   private static void renderEdit(@NotNull ChestMenu menu, @NotNull ChainBindingMenu.Session session) {
      Map<Integer, List<ItemStack>> bindings = ChainBindingStore.load(session.machine, session.face);
      List<ItemStack> list = bindings.getOrDefault(session.distance, List.of());
      ItemStack info = infoIcon();
      info.editMeta(meta -> {
         meta.setDisplayName(Lang.getString("messages.cellnet.line.binding-target-name", directionName(session.face), session.distance));
         List<String> lore = new ArrayList<>();
         lore.add(Lang.getString("messages.cellnet.line.binding-info-lore", list.size(), 9));
         lore.add("");
         lore.add(Lang.getString("messages.cellnet.line.binding-edit-hint-add"));
         lore.add(Lang.getString("messages.cellnet.line.binding-edit-hint-remove"));
         meta.setLore(lore);
      });
      GhostItems.mark(info);
      menu.replaceExistingItem(0, info);
      menu.addMenuClickHandler(0, (p, s, ix, a) -> false);

      for (int i = 0; i < EDIT_SLOTS.length; i++) {
         int slot = EDIT_SLOTS[i];
         if (i < list.size()) {
            ItemStack ghost = list.get(i).clone();
            ghost.setAmount(1);
            GhostItems.mark(ghost);
            menu.replaceExistingItem(slot, ghost);
            int index = i;
            menu.addMenuClickHandler(slot, (p, s, it, a) -> {
               ItemStack cursor = p.getItemOnCursor();
               if (cursor != null && !cursor.getType().isAir()) {
                  addSample(p, menu, session, cursor);
               } else {
                  removeSample(p, menu, session, index);
               }

               return false;
            });
         } else {
            menu.replaceExistingItem(slot, Icons.PREVIEW_FILL);
            menu.addMenuClickHandler(slot, (p, s, it, a) -> {
               ItemStack cursor = p.getItemOnCursor();
               if (cursor != null && !cursor.getType().isAir()) {
                  addSample(p, menu, session, cursor);
               }

               return false;
            });
         }
      }
   }

   private static void addSample(@NotNull Player player, @NotNull ChestMenu menu, @NotNull ChainBindingMenu.Session session, @Nullable ItemStack clicked) {
      if (clicked != null && !clicked.getType().isAir() && !GhostItems.is(clicked)) {
         tryAddBinding(player, session.machine, session.face, session.distance, clicked);
         renderEdit(menu, session);
      }
   }

   static boolean tryAddBinding(@NotNull Player player, @NotNull Location machine, @NotNull BlockFace face, int distance, @NotNull ItemStack clicked) {
      AbstractChainMachine m = AbstractChainMachine.machineAt(machine);
      if (m == null) {
         player.sendMessage(Lang.getString("messages.cellnet.line.binding-machine-gone"));
         return false;
      } else if (!AbstractChainMachine.hasModule(machine, ChainModule.BINDING)) {
         player.sendMessage(Lang.getString("messages.cellnet.line.binding-module-required"));
         return false;
      } else {
         List<Location> targets = targetLine(m, machine, face);
         if (distance > targets.size()) {
            player.sendMessage(Lang.getString("messages.cellnet.line.binding-target-gone"));
            return false;
         } else {
            ItemStack sample = clicked.asOne();
            if (StackUtils.isBlacklisted(sample)) {
               return false;
            } else if (CellAcceptRules.isNbtOversized(sample)) {
               player.sendMessage(Lang.getString("messages.cellnet.line.binding-save-rejected"));
               return false;
            } else {
               Map<Integer, List<ItemStack>> bindings = ChainBindingStore.load(machine, face);
               List<ItemStack> list = bindings.computeIfAbsent(distance, d -> new ArrayList<>());
               if (ChainBindingStore.containsSimilar(list, sample)) {
                  player.sendMessage(Lang.getString("messages.cellnet.line.binding-duplicate"));
                  return false;
               } else if (list.size() >= 9) {
                  player.sendMessage(Lang.getString("messages.cellnet.line.binding-full"));
                  return false;
               } else {
                  list.add(sample);
                  if (!ChainBindingStore.save(machine, face, bindings)) {
                     player.sendMessage(Lang.getString("messages.cellnet.line.binding-save-rejected"));
                     return false;
                  } else {
                     AbstractChainMachine.invalidateBindings(AbstractChainMachine.state(machine), face);
                     return true;
                  }
               }
            }
         }
      }
   }

   static boolean clearBinding(@NotNull Player player, @NotNull Location machine, @NotNull BlockFace face, int distance) {
      AbstractChainMachine m = AbstractChainMachine.machineAt(machine);
      if (m == null) {
         player.sendMessage(Lang.getString("messages.cellnet.line.binding-machine-gone"));
         return false;
      } else if (!AbstractChainMachine.hasModule(machine, ChainModule.BINDING)) {
         player.sendMessage(Lang.getString("messages.cellnet.line.binding-module-required"));
         return false;
      } else {
         Map<Integer, List<ItemStack>> bindings = ChainBindingStore.load(machine, face);
         if (bindings.remove(distance) == null) {
            return false;
         } else if (!ChainBindingStore.save(machine, face, bindings)) {
            player.sendMessage(Lang.getString("messages.cellnet.line.binding-save-rejected"));
            return false;
         } else {
            AbstractChainMachine.invalidateBindings(AbstractChainMachine.state(machine), face);
            return true;
         }
      }
   }

   private static void removeSample(@NotNull Player player, @NotNull ChestMenu menu, @NotNull ChainBindingMenu.Session session, int index) {
      if (!validate(player, session, true)) {
         renderEdit(menu, session);
      } else {
         Map<Integer, List<ItemStack>> bindings = ChainBindingStore.load(session.machine, session.face);
         List<ItemStack> list = bindings.get(session.distance);
         if (list != null && index < list.size()) {
            list.remove(index);
            if (!ChainBindingStore.save(session.machine, session.face, bindings)) {
               player.sendMessage(Lang.getString("messages.cellnet.line.binding-save-rejected"));
               renderEdit(menu, session);
            } else {
               AbstractChainMachine.invalidateBindings(AbstractChainMachine.state(session.machine), session.face);
               player.sendMessage(Lang.getString("messages.cellnet.line.binding-unbind-item-removed"));
               renderEdit(menu, session);
            }
         } else {
            renderEdit(menu, session);
         }
      }
   }

   public static void openDrag(@NotNull Player player, @NotNull Location machine, @NotNull BlockFace face) {
      ChainBindingMenu.Session session = new ChainBindingMenu.Session(machine, face, 0);
      ChestMenu menu = new ChestMenu(Lang.getString("messages.cellnet.line.brush-drag-title", directionName(face)));
      menu.setPlayerInventoryClickable(true);
      menu.setEmptySlotsClickable(false);

      for (int slot = 0; slot < 54; slot++) {
         if (slot != 0 && slot != 8 && slot != 4 && slot != 45 && !in(BrowseUI.LIST_SLOTS, slot)) {
            menu.addItem(slot, ChestMenuUtils.getBackground(), (p, s, i, a) -> false);
         }
      }

      menu.addItem(45, BrowseUI.backButton(), (p, s, i, a) -> {
         openDirections(p, machine);
         return false;
      });
      renderDrag(menu, session);
      menu.open(new Player[]{player});
   }

   private static void renderDrag(@NotNull ChestMenu menu, @NotNull ChainBindingMenu.Session session) {
      AbstractChainMachine machine = AbstractChainMachine.machineAt(session.machine);
      if (machine != null) {
         List<Location> targets = targetLine(machine, session.machine, session.face);
         Map<Integer, List<ItemStack>> bindings = ChainBindingStore.load(session.machine, session.face);
         int total = targets.size();
         ItemStack info = infoIcon();
         info.editMeta(
            meta -> {
               meta.setDisplayName(Lang.getString("messages.cellnet.line.binding-direction-name", directionName(session.face)));
               meta.setLore(
                  List.of(
                     Lang.getString("messages.cellnet.line.binding-direction-lore", total, bindings.size()),
                     Lang.getString("messages.cellnet.line.brush-drag-hint-add"),
                     Lang.getString("messages.cellnet.line.brush-drag-hint-clear"),
                     Lang.getString("messages.cellnet.line.brush-drag-hint-edit")
                  )
               );
            }
         );
         GhostItems.mark(info);
         menu.replaceExistingItem(4, info);
         menu.addMenuClickHandler(4, (p, s, ix, a) -> false);
         int totalPages = BrowseUI.totalPages(Math.max(1, total));
         if (session.page >= totalPages) {
            session.page = totalPages - 1;
         }

         int start = session.page * BrowseUI.PAGE_SIZE;

         for (int i = 0; i < BrowseUI.LIST_SLOTS.length; i++) {
            int slot = BrowseUI.LIST_SLOTS[i];
            int distance = start + i + 1;
            if (distance > total) {
               menu.replaceExistingItem(slot, total == 0 && i == 0 ? Icons.SEARCH_EMPTY : Icons.PREVIEW_FILL);
               menu.addMenuClickHandler(slot, (p, s, it, a) -> false);
            } else {
               List<ItemStack> bound = bindings.get(distance);
               menu.replaceExistingItem(slot, dragIcon(session, distance, bound));
               menu.addMenuClickHandler(slot, (p, s, it, a) -> {
                  handleDragClick(p, menu, session, distance, a.isRightClicked(), a.isShiftClicked());
                  return false;
               });
            }
         }

         BrowseUI.wirePager(menu, session.page, totalPages, page -> {
            session.page = page;
            renderDrag(menu, session);
         });
      }
   }

   private static void handleDragClick(
      @NotNull Player player, @NotNull ChestMenu menu, @NotNull ChainBindingMenu.Session session, int distance, boolean rightClick, boolean shiftClick
   ) {
      if (rightClick) {
         AbstractChainMachine machine = AbstractChainMachine.machineAt(session.machine);
         if (machine != null) {
            List<Location> targets = targetLine(machine, session.machine, session.face);
            if (distance > targets.size()) {
               renderDrag(menu, session);
            } else {
               openEdit(player, session.machine, session.face, distance);
            }
         }
      } else if (shiftClick) {
         clearBinding(player, session.machine, session.face, distance);
         renderDrag(menu, session);
      } else {
         ItemStack cursor = player.getItemOnCursor();
         if (cursor != null && !cursor.getType().isAir()) {
            tryAddBinding(player, session.machine, session.face, distance, cursor);
         }

         renderDrag(menu, session);
      }
   }

   @NotNull
   private static ItemStack dragIcon(@NotNull ChainBindingMenu.Session session, int distance, @Nullable List<ItemStack> bound) {
      ItemStack icon;
      if (bound != null && !bound.isEmpty()) {
         icon = bound.get(0).clone();
         icon.setAmount(1);
      } else {
         icon = Icons.PREVIEW_FILL.clone();
      }

      icon.editMeta(meta -> {
         meta.setDisplayName(Lang.getString("messages.cellnet.line.binding-target-position", distance, directionName(session.face)));
         List<String> lore = new ArrayList<>();
         if (bound == null) {
            lore.add(Lang.getString("messages.cellnet.line.binding-status-shared"));
         } else if (bound.isEmpty()) {
            lore.add(Lang.getString("messages.cellnet.line.binding-status-empty"));
         } else {
            lore.add(Lang.getString("messages.cellnet.line.binding-info-lore", bound.size(), 9));
         }

         lore.add("");
         lore.add(Lang.getString("messages.cellnet.line.brush-drag-hint-add"));
         lore.add(Lang.getString("messages.cellnet.line.brush-drag-hint-clear"));
         lore.add(Lang.getString("messages.cellnet.line.brush-drag-hint-edit"));
         meta.setLore(lore);
      });
      GhostItems.mark(icon);
      return icon;
   }

   private static boolean validate(@NotNull Player player, @NotNull ChainBindingMenu.Session session, boolean requireTargetOnline) {
      AbstractChainMachine machine = AbstractChainMachine.machineAt(session.machine);
      if (machine == null) {
         player.sendMessage(Lang.getString("messages.cellnet.line.binding-machine-gone"));
         return false;
      } else if (!AbstractChainMachine.hasModule(session.machine, ChainModule.BINDING)) {
         player.sendMessage(Lang.getString("messages.cellnet.line.binding-module-required"));
         return false;
      } else {
         if (requireTargetOnline && session.face != null) {
            List<Location> targets = targetLine(machine, session.machine, session.face);
            if (session.distance > targets.size()) {
               player.sendMessage(Lang.getString("messages.cellnet.line.binding-target-gone"));
               return false;
            }
         }

         return true;
      }
   }

   private static boolean isEnabled(@NotNull Location machine, @NotNull BlockFace face) {
      AbstractChainMachine.LineState state = AbstractChainMachine.state(machine);
      return state.effectiveDirections().contains(face);
   }

   @NotNull
   private static List<Location> targetLine(@NotNull AbstractChainMachine machine, @NotNull Location location, @NotNull BlockFace face) {
      return ChainTargetCache.targets(
         location, face, machine.maxDistance(), AbstractChainMachine.state(location).vanilla, machine.workTtlTicks(), machine.idleTtlTicks()
      );
   }

   static void openTarget(@NotNull Player player, @NotNull Location target) {
      if (!Slimefun.getProtectionManager().hasPermission(player, target, Interaction.INTERACT_BLOCK)) {
         player.sendMessage(Lang.getString("messages.cellnet.line.direction-no-permission"));
      } else {
         BlockMenu targetMenu = StorageCacheUtils.getMenu(target);
         if (targetMenu != null) {
            targetMenu.open(new Player[]{player});
         } else if (target.getBlock().getState() instanceof Container container) {
            player.openInventory(container.getInventory());
         } else {
            player.sendMessage(Lang.getString("messages.cellnet.line.direction-no-target"));
         }
      }
   }

   @NotNull
   static ItemStack directionIcon(@NotNull Location machine, @NotNull BlockFace face, boolean enabled) {
      return enabled ? directionPane(machine, face, true) : Icons.LINE_DIRECTION_OFF.clone();
   }

   @NotNull
   static ItemStack directionPane(@NotNull Location machine, @NotNull BlockFace face, boolean selected) {
      Block neighbor = machine.getBlock().getRelative(face);
      SlimefunItem sfItem = StorageCacheUtils.getSfItem(neighbor.getLocation());
      ItemStack pane;
      if (sfItem != null) {
         pane = NetworkDirectional.getDirectionalSlotPane(face, sfItem, selected);
      } else {
         Material material = neighbor.getType();
         if (material.isItem() && material != Material.AIR) {
            pane = NetworkDirectional.getDirectionalSlotPane(face, material, selected);
         } else {
            pane = (selected ? Icons.LINE_DIRECTION_ON : Icons.LINE_DIRECTION_OFF).clone();
            pane.editMeta(meta -> meta.setDisplayName(String.format(Lang.getString("messages.normal-operation.directional.display_empty"), face.name())));
         }
      }

      localizeDirectionName(pane, face);
      GhostItems.mark(pane);
      return pane;
   }

   private static void localizeDirectionName(@NotNull ItemStack pane, @NotNull BlockFace face) {
      String english = face.name();
      pane.editMeta(meta -> {
         String display = meta.getDisplayName();
         int index = display.indexOf(english);
         if (index >= 0) {
            meta.setDisplayName(display.substring(0, index) + directionName(face) + display.substring(index + english.length()));
         }
      });
   }

   @NotNull
   private static ItemStack infoIcon() {
      ItemStack icon = Icons.CHAIN_BINDING_INFO.clone();
      icon.editMeta(meta -> {
         meta.setDisplayName(Lang.getString("messages.cellnet.line.binding-info-name"));
         meta.setLore(List.of());
      });
      return icon;
   }

   @NotNull
   static String directionName(@NotNull BlockFace face) {
      return Lang.getString("messages.cellnet.line.direction-" + face.name().toLowerCase(Locale.ROOT));
   }

   private static final class Session {
      final Location machine;
      final BlockFace face;
      final int distance;
      int page;

      Session(@NotNull Location machine, @Nullable BlockFace face, int distance) {
         this.machine = machine;
         this.face = face;
         this.distance = distance;
      }
   }
}
