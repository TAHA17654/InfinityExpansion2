package com.ytdd9527.networksexpansion.implementation.machines.cellnet.filler;

import com.balugaq.netex.utils.Lang;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.core.items.SpecialSlimefunItem;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.drive.DriveOwnership;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.drive.WhitelistStore;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.ItemKey;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.NetworkUtil;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.ui.Icons;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.ui.MenuShells;
import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.NodeDefinition;
import io.github.sefiraat.networks.network.NodeType;
import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import io.github.sefiraat.networks.utils.Keys;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemHandler;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.data.persistent.PersistentDataAPI;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu.AdvancedMenuClickHandler;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ContainerFiller extends SpecialSlimefunItem {
   public static final int STATUS_SLOT = 45;
   public static final int BOX_MODE_SLOT = 46;
   public static final int PAUSE_SLOT = 49;
   public static final int[] TEMPLATE_SLOTS = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8};
   public static final int[] CONTAINER_SLOTS = new int[]{9, 10, 11, 12, 13, 14, 15, 16, 17};
   public static final int[] OUTPUT_SLOTS = new int[18];
   private static final int[] BACKGROUND = new int[]{36, 37, 38, 39, 40, 41, 42, 43, 44, 47, 48, 50, 51, 52, 53};
   private static final long ROUND_INTERVAL_MS = 1000L;
   private static final long BACKOFF_INTERVAL_MS = 3000L;
   private static final NamespacedKey MARKER_KEY;
   private static final Map<Location, ContainerFiller.FillerState> STATES;

   private static ContainerFiller.FillerState state(@NotNull Location loc) {
      return STATES.computeIfAbsent(loc, k -> new ContainerFiller.FillerState());
   }

   public ContainerFiller(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe);
   }

   public void preRegister() {
      this.addItemHandler(new ItemHandler[]{new BlockPlaceHandler(false) {
         public void onPlayerPlace(@NotNull BlockPlaceEvent event) {
            ContainerFiller.ensureNetworkNode(event.getBlock().getLocation());
         }
      }});
      this.addItemHandler(new ItemHandler[]{new BlockTicker() {
         public boolean isSynchronized() {
            return false;
         }

         public void tick(@NotNull Block b, SlimefunItem item, @NotNull SlimefunBlockData data) {
            long now = System.currentTimeMillis();
            Location loc = b.getLocation();
            ContainerFiller.FillerState s = ContainerFiller.state(loc);
            if (s.nextRun <= 0L || now >= s.nextRun) {
               ContainerFiller.ensureNetworkNode(loc);
               s.nextRun = now + 1000L;
               BlockMenu menu = data.getBlockMenu();
               if (menu != null) {
                  Location location = b.getLocation();
                  Bukkit.getScheduler().runTask(Networks.getInstance(), () -> ContainerFiller.this.processRound(menu, location));
               }
            }
         }
      }});
      this.addItemHandler(new ItemHandler[]{new BlockBreakHandler(false, false) {
         public void onPlayerBreak(@NotNull BlockBreakEvent event, @NotNull ItemStack item, @NotNull List<ItemStack> drops) {
            Player breaker = event.getPlayer();
            Location location = event.getBlock().getLocation();
            if (!ContainerFiller.isAllowed(breaker, location, Interaction.BREAK_BLOCK)) {
               event.setCancelled(true);
               breaker.sendMessage(Lang.getString("messages.cellnet.storage_assembler.break_not_allowed"));
            } else {
               NetworkStorage.removeNode(location);
               NetworkUtil.invalidate(location);
               DriveOwnership.clearOwnerCache(location);
               ContainerFiller.STATES.remove(location);
               BlockMenu blockMenu = StorageCacheUtils.getMenu(location);
               if (blockMenu != null) {
                  ContainerFiller.dropSlots(blockMenu, location, ContainerFiller.TEMPLATE_SLOTS);
                  ContainerFiller.dropSlots(blockMenu, location, ContainerFiller.CONTAINER_SLOTS);
                  ContainerFiller.dropSlots(blockMenu, location, ContainerFiller.OUTPUT_SLOTS);
               }
            }
         }
      }});
   }

   public void postRegister() {
      MenuShells.create(
         this,
         54,
         BACKGROUND,
         (block, player) -> isAllowed(player, block.getLocation(), Interaction.INTERACT_BLOCK),
         flow -> {
            if (flow == ItemTransportFlow.INSERT) {
               return CONTAINER_SLOTS;
            } else {
               return flow == ItemTransportFlow.WITHDRAW ? OUTPUT_SLOTS : new int[0];
            }
         },
         (menu, block) -> {
            Location location = block.getLocation();

            for (int slot : TEMPLATE_SLOTS) {
               if (!realItem(menu, slot)) {
                  menu.replaceExistingItem(slot, buildSlotMarker());
               }
            }

            for (int slotx : TEMPLATE_SLOTS) {
               menu.addMenuClickHandler(slotx, (player, si, item, action) -> {
                  if (item != null && !item.getType().isAir() && !isSlotMarker(item)) {
                     Bukkit.getScheduler().runTask(Networks.getInstance(), () -> syncTemplateMarkers(menu));
                     return true;
                  } else {
                     ItemStack cursor = player.getItemOnCursor();
                     if (!cursor.getType().isAir() && !isSlotMarker(cursor)) {
                        player.setItemOnCursor(null);
                        menu.replaceExistingItem(si, cursor);
                        return false;
                     } else {
                        return false;
                     }
                  }
               });
            }

            for (int slotx : CONTAINER_SLOTS) {
               menu.addMenuClickHandler(
                  slotx,
                  new AdvancedMenuClickHandler() {
                     public boolean onClick(
                        @NotNull InventoryClickEvent event, @NotNull Player player, int slot, @NotNull ItemStack item, @NotNull ClickAction action
                     ) {
                        ItemStack incoming = event.getClick() == ClickType.NUMBER_KEY
                           ? player.getInventory().getItem(event.getHotbarButton())
                           : player.getItemOnCursor();
                        if (incoming != null && !incoming.getType().isAir()) {
                           String error = FillStrategies.containerError(incoming);
                           if (error != null) {
                              player.sendMessage(Lang.getString(error));
                              return false;
                           } else {
                              return true;
                           }
                        } else {
                           return true;
                        }
                     }

                     public boolean onClick(@NotNull Player player, int slot, @NotNull ItemStack item, @NotNull ClickAction action) {
                        return true;
                     }
                  }
               );
            }

            for (int slotx : OUTPUT_SLOTS) {
               menu.addMenuClickHandler(slotx, (player, si, item, action) -> true);
            }

            menu.addMenuClickHandler(45, (p, s, i, a) -> false);
            menu.addMenuClickHandler(46, (p, s, i, a) -> false);
            boolean paused = FillerPdc.isPausedPersisted(location);
            state(location).paused = paused;
            boolean boxMode = FillerPdc.isBoxModePersisted(location);
            menu.replaceExistingItem(49, buildPauseButton(paused));
            menu.addMenuClickHandler(49, (player, s, i, a) -> {
               ContainerFiller.FillerState st = state(location);
               boolean was = st.paused;
               st.paused = !was;
               FillerPdc.writePaused(location, !was);
               menu.replaceExistingItem(49, buildPauseButton(!was));
               player.sendMessage(Lang.getString(!was ? "messages.cellnet.storage_assembler.paused" : "messages.cellnet.storage_assembler.resumed"));
               return false;
            });
            menu.replaceExistingItem(46, buildBoxModeButton(boxMode));
            menu.addMenuClickHandler(46, (player, s, i, a) -> {
               boolean was = FillerPdc.isBoxModePersisted(location);
               FillerPdc.writeBoxMode(location, !was);
               menu.replaceExistingItem(46, buildBoxModeButton(!was));
               return false;
            });
            refreshStatus(menu, location, "phase_no_template");
         }
      );
   }

   private void processRound(@NotNull BlockMenu menu, @NotNull Location location) {
      ContainerFiller.FillerState st = state(location);
      if (st.paused) {
         refreshStatus(menu, location, "phase_paused");
      } else {
         NetworkRoot root = NetworkUtil.findRoot(location);
         if (root == null) {
            refreshStatus(menu, location, "phase_no_network");
            st.nextRun = System.currentTimeMillis() + 3000L;
         } else {
            List<ItemStack> templates = readTemplates(menu);
            syncTemplateMarkers(menu);
            if (templates.isEmpty()) {
               refreshStatus(menu, location, "phase_no_template");
            } else {
               this.drainLegacyBuffer(location, root);
               int outputBudget = countEmptyOutputs(menu);
               if (outputBudget <= 0) {
                  refreshStatus(menu, location, "phase_full");
               } else {
                  Map<ItemKey, Long> availability = new HashMap<>(root.getAllNetworkItemsKeyedView());
                  long filledTotal = 0L;
                  int processed = 0;
                  boolean hasContainer = false;
                  boolean boxMode = FillerPdc.isBoxModePersisted(location);
                  int[] var13 = CONTAINER_SLOTS;
                  int var14 = var13.length;

                  for (int var15 = 0; var15 < var14; var15++) {
                     for (int slot = var13[var15]; outputBudget > 0; outputBudget--) {
                        ItemStack stack = menu.getItemInSlot(slot);
                        if (stack == null || stack.getType().isAir()) {
                           break;
                        }

                        hasContainer = true;
                        ItemStack unit = stack.asOne();
                        FillStrategies.Strategy strategy = FillStrategies.create(unit, location, boxMode);
                        if (strategy == null) {
                           int unloadedDrawer = FillStrategies.unloadedDrawerId(unit);
                           if (unloadedDrawer != -1) {
                              FillStrategies.requestDrawerLoad(unloadedDrawer);
                           }
                           break;
                        }

                        List<ItemStack> scope;
                        if (strategy.singleType()) {
                           if (stack.getAmount() > 1) {
                              scope = rotateTemplates(templates, location);
                           } else {
                              ItemStack positional = positionalTemplate(menu, slot);
                              if (positional == null && !strategy.hasContent()) {
                                 break;
                              }

                              scope = positional == null ? templates : prepend(positional, templates);
                           }
                        } else {
                           scope = templates;
                        }

                        List<FillStrategies.TemplateFill> plan = strategy.plan(scope);
                        List<ItemRequest> requests = new ArrayList<>();
                        List<ItemStack> requestTemplate = new ArrayList<>();

                        for (FillStrategies.TemplateFill fill : plan) {
                           ItemKey key = new ItemKey(fill.template());
                           long avail = availability.getOrDefault(key, 0L);
                           long amount = Math.min(fill.request(), avail);
                           if (amount > 0L) {
                              availability.put(key, avail - amount);
                              ItemStack req = fill.template().clone();
                              req.setAmount(1);
                              requests.add(new ItemRequest(req, (int)Math.min(amount, 2147483647L)));
                              requestTemplate.add(fill.template());
                           }
                        }

                        if (requests.isEmpty()) {
                           if (!strategy.hasContent()) {
                              break;
                           }
                        } else {
                           List<ItemStack> results = root.getItemStacksBatch(location, requests);

                           for (int r = 0; r < requests.size(); r++) {
                              ItemStack got = results.get(r);
                              long amount = got != null && !got.getType().isAir() ? (long)got.getAmount() : 0L;
                              if (amount > 0L) {
                                 long acc = strategy.apply(requestTemplate.get(r), amount);
                                 long leftover = amount - acc;
                                 if (leftover > 0L) {
                                    ItemStack back = requestTemplate.get(r).clone();
                                    back.setAmount((int)Math.min(leftover, 2147483647L));
                                    root.addItemStack(location, back);
                                 }

                                 filledTotal += acc;
                              }
                           }
                        }

                        processed++;
                        strategy.refreshDisplay(unit);
                        if (stack.getAmount() <= 1) {
                           menu.replaceExistingItem(slot, null);
                        } else {
                           stack.setAmount(stack.getAmount() - 1);
                        }

                        if (!tryPlaceOutput(menu, unit)) {
                           if (location.getWorld() != null) {
                              location.getWorld().dropItemNaturally(location, unit);
                           }
                           break;
                        }
                     }

                     if (outputBudget <= 0) {
                        break;
                     }
                  }

                  if (filledTotal > 0L) {
                     st.lastFilled = filledTotal;
                  }

                  st.lastProcessed = processed;
                  if (processed > 0) {
                     refreshStatus(menu, location, "phase_run");
                  } else if (outputBudget <= 0) {
                     refreshStatus(menu, location, "phase_full");
                  } else if (hasContainer) {
                     refreshStatus(menu, location, "phase_no_material");
                     st.nextRun = System.currentTimeMillis() + 3000L;
                  } else {
                     refreshStatus(menu, location, "phase_no_container");
                  }
               }
            }
         }
      }
   }

   @NotNull
   private static List<ItemStack> rotateTemplates(@NotNull List<ItemStack> templates, @NotNull Location location) {
      int count = templates.size();
      if (count <= 1) {
         return templates;
      } else {
         int cursor = state(location).singleTypeCursor++;
         List<ItemStack> rotated = new ArrayList<>(count);

         for (int i = 0; i < count; i++) {
            rotated.add(templates.get(Math.floorMod(cursor + i, count)));
         }

         return rotated;
      }
   }

   @Nullable
   private static ItemStack positionalTemplate(@NotNull BlockMenu menu, int slot) {
      int index = slot - CONTAINER_SLOTS[0];
      if (index >= 0 && index < TEMPLATE_SLOTS.length) {
         ItemStack item = menu.getItemInSlot(TEMPLATE_SLOTS[index]);
         return item != null && !item.getType().isAir() && !isSlotMarker(item) ? item : null;
      } else {
         return null;
      }
   }

   @NotNull
   private static List<ItemStack> prepend(@NotNull ItemStack item, @NotNull List<ItemStack> list) {
      List<ItemStack> scope = new ArrayList<>(list.size() + 1);
      scope.add(item.asOne());
      scope.addAll(list);
      return scope;
   }

   private void drainLegacyBuffer(@NotNull Location location, @NotNull NetworkRoot root) {
      ItemStack buffer = FillerPdc.loadBuffer(location);
      if (buffer != null) {
         root.addItemStack(location, buffer);
         if (buffer.getAmount() > 0) {
            FillerPdc.saveBuffer(location, buffer);
         } else {
            FillerPdc.removeBuffer(location);
         }
      }
   }

   private static int countEmptyOutputs(@NotNull BlockMenu menu) {
      int empty = 0;

      for (int slot : OUTPUT_SLOTS) {
         ItemStack item = menu.getItemInSlot(slot);
         if (item == null || item.getType().isAir()) {
            empty++;
         }
      }

      return empty;
   }

   @NotNull
   private static List<ItemStack> readTemplates(@NotNull BlockMenu menu) {
      List<ItemStack> templates = new ArrayList<>(TEMPLATE_SLOTS.length);
      Set<ItemKey> seen = new HashSet<>();

      for (int slot : TEMPLATE_SLOTS) {
         ItemStack item = menu.getItemInSlot(slot);
         if (item != null && !item.getType().isAir() && !isSlotMarker(item)) {
            ItemStack template = item.clone();
            template.setAmount(1);
            if (seen.add(new ItemKey(template))) {
               templates.add(template);
            }
         }
      }

      return templates;
   }

   private static void syncTemplateMarkers(@NotNull BlockMenu menu) {
      for (int slot : TEMPLATE_SLOTS) {
         ItemStack item = menu.getItemInSlot(slot);
         if (item == null || item.getType().isAir()) {
            menu.replaceExistingItem(slot, buildSlotMarker());
         }
      }
   }

   private static boolean tryPlaceOutput(@NotNull BlockMenu menu, @NotNull ItemStack item) {
      int max = Math.max(1, item.getMaxStackSize());

      for (int slot : OUTPUT_SLOTS) {
         ItemStack target = menu.getItemInSlot(slot);
         if (target != null && !target.getType().isAir() && target.isSimilar(item)) {
            int room = max - target.getAmount();
            if (room > 0) {
               int put = Math.min(room, item.getAmount());
               target.setAmount(target.getAmount() + put);
               item.setAmount(item.getAmount() - put);
               if (item.getAmount() <= 0) {
                  return true;
               }
            }
         }
      }

      for (int slotx : OUTPUT_SLOTS) {
         ItemStack target = menu.getItemInSlot(slotx);
         if (target == null || target.getType().isAir()) {
            menu.replaceExistingItem(slotx, item);
            return true;
         }
      }

      return false;
   }

   private static boolean realItem(@NotNull BlockMenu menu, int slot) {
      ItemStack item = menu.getItemInSlot(slot);
      return item != null && !item.getType().isAir();
   }

   private static void dropSlots(@NotNull BlockMenu menu, @NotNull Location location, @NotNull int[] slots) {
      for (int slot : slots) {
         ItemStack item = menu.getItemInSlot(slot);
         if (item != null && !item.getType().isAir() && !isSlotMarker(item)) {
            menu.replaceExistingItem(slot, null);
            if (location.getWorld() != null) {
               location.getWorld().dropItemNaturally(location, item);
            }
         }
      }
   }

   private static boolean isAllowed(@NotNull Player player, @NotNull Location location, @NotNull Interaction interaction) {
      if (DriveOwnership.bypasses(player)) {
         return true;
      } else if (!Slimefun.getProtectionManager().hasPermission(player, location, interaction)) {
         return false;
      } else {
         UUID ownerUuid = DriveOwnership.getOwnerUuid(location);
         return ownerUuid == null ? true : ownerUuid.equals(player.getUniqueId()) || WhitelistStore.isWhitelisted(ownerUuid, player.getUniqueId());
      }
   }

   private static void refreshStatus(@NotNull BlockMenu menu, @NotNull Location location, @NotNull String phaseKey) {
      if (menu.hasViewer()) {
         writeStatus(menu, location, phaseKey);
      }
   }

   private static void writeStatus(@NotNull BlockMenu menu, @NotNull Location location, @NotNull String phaseKey) {
      ItemStack status = Icons.ASSEMBLER_STATUS.clone();
      status.editMeta(meta -> {
         meta.setDisplayName(Lang.getString("messages.cellnet.storage_assembler.status_name"));
         List<String> lore = new ArrayList<>();
         lore.add(Lang.getString("messages.cellnet.storage_assembler." + phaseKey));
         lore.add(Lang.getString("messages.cellnet.storage_assembler.templates_line", countFilled(menu, TEMPLATE_SLOTS, true)));
         int containers = countFilled(menu, CONTAINER_SLOTS, false);
         String filling = Lang.getString("messages.cellnet.storage_assembler.state_empty");
         int processed = state(location).lastProcessed;
         if (processed > 0) {
            filling = Lang.getString("messages.cellnet.storage_assembler.filling_line", processed);
         }

         int outputs = countFilled(menu, OUTPUT_SLOTS, false);
         lore.add(Lang.getString("messages.cellnet.storage_assembler.storage_line", containers, CONTAINER_SLOTS.length, filling, outputs, OUTPUT_SLOTS.length));
         long filled = state(location).lastFilled;
         if (filled > 0L) {
            lore.add(Lang.getString("messages.cellnet.storage_assembler.filled_line", filled));
         }

         lore.add(Lang.getString("messages.cellnet.storage_assembler.hint_line"));
         meta.setLore(lore);
      });
      menu.replaceExistingItem(45, status);
   }

   private static int countFilled(@NotNull BlockMenu menu, @NotNull int[] slots, boolean skipMarkers) {
      int count = 0;

      for (int slot : slots) {
         ItemStack item = menu.getItemInSlot(slot);
         if (item != null && !item.getType().isAir() && (!skipMarkers || !isSlotMarker(item))) {
            count++;
         }
      }

      return count;
   }

   @NotNull
   private static ItemStack buildPauseButton(boolean paused) {
      return (paused ? Icons.ASSEMBLER_RESUME : Icons.ASSEMBLER_PAUSE).clone();
   }

   @NotNull
   private static ItemStack buildBoxModeButton(boolean boxMode) {
      return (boxMode ? Icons.ASSEMBLER_BOX_ON : Icons.ASSEMBLER_BOX_OFF).clone();
   }

   private static boolean isSlotMarker(@Nullable ItemStack item) {
      if (item != null && item.hasItemMeta()) {
         ItemMeta meta = item.getItemMeta();
         return meta != null && PersistentDataAPI.getByte(meta, MARKER_KEY) == 1;
      } else {
         return false;
      }
   }

   @NotNull
   private static ItemStack buildSlotMarker() {
      ItemStack marker = Icons.ASSEMBLER_TEMPLATE_SLOT.clone();
      marker.editMeta(meta -> PersistentDataAPI.setByte(meta, MARKER_KEY, (byte)1));
      return marker;
   }

   private static void ensureNetworkNode(@NotNull Location location) {
      if (!NetworkStorage.containsKey(location)) {
         NetworkStorage.registerNode(location, new NodeDefinition(NodeType.BRIDGE));
      }
   }

   static {
      for (int i = 0; i < OUTPUT_SLOTS.length; i++) {
         OUTPUT_SLOTS[i] = 18 + i;
      }

      MARKER_KEY = Keys.newKey("container_filler_marker");
      STATES = new ConcurrentHashMap<>();
   }

   private static final class FillerState {
      boolean paused;
      long nextRun;
      long lastFilled;
      int lastProcessed;
      int singleTypeCursor;
   }
}
