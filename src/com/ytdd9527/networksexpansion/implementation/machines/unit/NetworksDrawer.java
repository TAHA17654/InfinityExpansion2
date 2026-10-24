package com.ytdd9527.networksexpansion.implementation.machines.unit;

import com.balugaq.netex.api.data.ItemContainer;
import com.balugaq.netex.api.data.StorageUnitData;
import com.balugaq.netex.api.enums.QuickTransferMode;
import com.balugaq.netex.api.enums.StorageUnitType;
import com.balugaq.netex.api.helpers.Icon;
import com.balugaq.netex.api.interfaces.ModellableItem;
import com.balugaq.netex.integrations.logitech.DrawerVirtualCache;
import com.balugaq.netex.integrations.logitech.LinkBindingStore;
import com.balugaq.netex.utils.InventoryUtil;
import com.balugaq.netex.utils.Lang;
import com.jeff_media.morepersistentdatatypes.DataType;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.core.items.SpecialSlimefunItem;
import com.ytdd9527.networksexpansion.implementation.tools.ItemMover;
import com.ytdd9527.networksexpansion.utils.DisplayGroupGenerators;
import com.ytdd9527.networksexpansion.utils.TextUtil;
import com.ytdd9527.networksexpansion.utils.databases.DataStorage;
import com.ytdd9527.networksexpansion.utils.itemstacks.ItemStackUtil;
import dev.sefiraat.sefilib.entity.display.DisplayGroup;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import io.github.sefiraat.networks.network.stackcaches.QuantumCache;
import io.github.sefiraat.networks.slimefun.network.NetworkQuantumStorage;
import io.github.sefiraat.networks.utils.Keys;
import io.github.sefiraat.networks.utils.NetworkUtils;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.sefiraat.networks.utils.datatypes.DataTypeMethods;
import io.github.sefiraat.networks.utils.datatypes.PersistentQuantumStorageType;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemHandler;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.DistinctiveItem;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import me.ddggdd135.guguslimefunlib.GuguSlimefunLib;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import net.guizhanss.minecraft.guizhanlib.gugu.minecraft.helpers.inventory.ItemStackHelper;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NetworksDrawer extends SpecialSlimefunItem implements DistinctiveItem, ModellableItem {
   private static final Map<Location, StorageUnitData> storages = new HashMap<>();
   private static final Map<Location, QuickTransferMode> quickTransferModes = new HashMap<>();
   private static final Set<Location> locked = new HashSet<>();
   private static final Set<Location> voidExcesses = new HashSet<>();
   private static final String KEY_UUID = "display-uuid";
   private static final int[] DISPLAY_SLOTS = new int[]{
      10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43, 46, 47, 48, 49, 50, 51, 52
   };
   private static final int STORAGE_INFO_SLOT = 4;
   private static final NamespacedKey idKey = Keys.newKey("CONTAINER_ID");
   private static final NamespacedKey lockKey = Keys.newKey("CONTAINER_LOCK");
   private static final NamespacedKey voidExcessKey = Keys.newKey("CONTAINER_VOID_EXCESS");
   private static final NamespacedKey serverKey = Keys.newKey("CONTAINER_SERVER");
   private static final int QUANTUM_SLOT = 9;
   private static final int QUICK_TRANSFER_SLOT = 18;
   private static final int ITEM_CHOOSE_SLOT = 27;
   private final StorageUnitType sizeType;
   private final int[] BORDER = new int[]{0, 1, 2, 3, 5, 6, 17, 26, 35, 36, 44, 45, 53};
   private final int VOID_MODE_SLOT = 7;
   private final int LOCK_MODE_SLOT = 8;
   @Nullable
   private Function<Location, DisplayGroup> displayGroupGenerator = null;
   private boolean useSpecialModel;

   public NetworksDrawer(
      @NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe, StorageUnitType sizeType
   ) {
      super(itemGroup, item, recipeType, recipe);
      this.sizeType = sizeType;
      new BlockMenuPreset(this.getId(), this.getItemName()) {
         public void init() {
            for (int slot : NetworksDrawer.this.BORDER) {
               this.addItem(slot, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
            }

            this.addItem(4, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
            this.addItem(8, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
            this.addItem(7, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
         }

         public void newInstance(@NotNull BlockMenu menu, @NotNull Block b) {
            Location l = b.getLocation();
            NetworksDrawer.requestData(l, NetworksDrawer.getContainerId(l));
            LinkBindingStore.replayDrawer(l);
            SlimefunBlockData blockData = StorageCacheUtils.getBlock(l);
            String lock = null;
            String voidExcess = null;
            String quickModeStr = null;
            if (blockData != null) {
               lock = blockData.getData("locked");
               voidExcess = blockData.getData("voidExcess");
               quickModeStr = blockData.getData("quickTransferMode");
            }

            QuickTransferMode quickTransferMode = quickModeStr == null ? QuickTransferMode.FROM_QUANTUM : QuickTransferMode.valueOf(quickModeStr);
            NetworksDrawer.quickTransferModes.put(l, quickTransferMode);
            if (lock != null) {
               NetworksDrawer.locked.add(l);
               menu.replaceExistingItem(8, NetworksDrawer.this.getContentLockItem(true));
            } else {
               menu.replaceExistingItem(8, NetworksDrawer.this.getContentLockItem(false));
            }

            if (voidExcess != null) {
               NetworksDrawer.voidExcesses.add(l);
               menu.replaceExistingItem(7, NetworksDrawer.this.getVoidExcessItem(true));
            } else {
               menu.replaceExistingItem(7, NetworksDrawer.this.getVoidExcessItem(false));
            }

            menu.replaceExistingItem(18, NetworksDrawer.getQuickTransferItem(quickTransferMode));
            menu.addMenuClickHandler(8, (p, slot, item1, action) -> {
               NetworksDrawer.this.switchLock(menu, l);
               return false;
            });
            menu.addMenuClickHandler(7, (p, slot, item1, action) -> {
               NetworksDrawer.this.switchVoidExcess(menu, l);
               return false;
            });
            menu.addMenuClickHandler(18, (p, slot, item1, action) -> {
               if (action.isRightClicked()) {
                  NetworksDrawer.switchQuickTransferMode(menu, l);
               } else {
                  NetworksDrawer.quickTransfer(menu, l, p);
               }

               return false;
            });
            StorageUnitData data = NetworksDrawer.storages.get(l);
            if (data != null) {
               NetworksDrawer.update(l, true);
            }
         }

         public boolean canOpen(@NotNull Block b, @NotNull Player p) {
            return p.hasPermission("slimefun.inventory.bypass")
               || NetworksDrawer.this.canUse(p, false) && Slimefun.getProtectionManager().hasPermission(p, b, Interaction.INTERACT_BLOCK);
         }

         public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
            return new int[0];
         }
      };
      if (item.getItemId().endsWith("MODEL")) {
         this.loadConfigurations();
      }
   }

   @Nullable
   public static StorageUnitData getStorageData(Location l) {
      return storages.get(l);
   }

   @NotNull
   public static Map<Location, StorageUnitData> getAllStorageData() {
      return storages;
   }

   public static void setStorageData(Location l, StorageUnitData data) {
      storages.put(l, data);
   }

   public static void update(@NotNull Location l, boolean force) {
      BlockMenu menu = StorageCacheUtils.getMenu(l);
      if (menu != null && (force || menu.hasViewer())) {
         StorageUnitData data = storages.get(l);
         if (data == null) {
            requestData(l, getContainerId(l));
            return;
         }

         StorageUnitType sizeType = data.getSizeType();
         int maxEach = sizeType.getEachMaxSize();
         menu.replaceExistingItem(
            4, getStorageInfoItem(data.getId(), data.getStoredTypeCount(), sizeType.getMaxItemCount(), maxEach, isLocked(l), isVoidExcess(l))
         );
         List<ItemContainer> itemStored = storages.get(l).getStoredItems();

         for (int i = 0; i < DISPLAY_SLOTS.length; i++) {
            if (i < itemStored.size()) {
               ItemContainer each = itemStored.get(i);
               menu.replaceExistingItem(DISPLAY_SLOTS[i], getDisplayItem(each.getSampleDirectly(), each.getAmount(), maxEach));
            } else {
               menu.replaceExistingItem(DISPLAY_SLOTS[i], Icon.ERROR_BORDER);
            }
         }
      }
   }

   public static boolean contains(Location l, ItemStack itemStack) {
      for (ItemContainer each : storages.get(l).getStoredItems()) {
         if (StackUtils.itemsMatch(each.getSampleDirectly(), itemStack)) {
            return true;
         }
      }

      return false;
   }

   public static int getBoundId(@NotNull ItemStack item) {
      ItemMeta meta = item.getItemMeta();
      Integer id = null;
      if (meta != null && meta.getPersistentDataContainer().has(idKey, PersistentDataType.INTEGER)) {
         id = (Integer)meta.getPersistentDataContainer().get(idKey, PersistentDataType.INTEGER);
      }

      if (id == null) {
         id = -1;
      }

      return id;
   }

   public static boolean getLock(@NotNull ItemStack item) {
      ItemMeta meta = item.getItemMeta();
      Boolean lock = null;
      if (meta != null && meta.getPersistentDataContainer().has(lockKey, PersistentDataType.BOOLEAN)) {
         lock = (Boolean)meta.getPersistentDataContainer().get(lockKey, PersistentDataType.BOOLEAN);
      }

      if (lock == null) {
         lock = false;
      }

      return lock;
   }

   public static boolean getVoidExcess(@NotNull ItemStack item) {
      ItemMeta meta = item.getItemMeta();
      Boolean voidExcess = null;
      if (meta != null && meta.getPersistentDataContainer().has(voidExcessKey, PersistentDataType.BOOLEAN)) {
         voidExcess = (Boolean)meta.getPersistentDataContainer().get(voidExcessKey, PersistentDataType.BOOLEAN);
      }

      if (voidExcess == null) {
         voidExcess = false;
      }

      return voidExcess;
   }

   @NotNull
   public static ItemStack bindId(@NotNull ItemStack itemSample, int id) {
      ItemStack item = itemSample.clone();
      ItemMeta meta = item.getItemMeta();
      if (meta != null) {
         List<String> lore = meta.getLore();
         if (lore == null) {
            lore = new ArrayList<>();
         }

         lore.add(String.format(Lang.getString("messages.completed-operation.drawer.bound_id"), id));
         if (Networks.getSupportedPluginManager().isGuguSlimefunLib()) {
            lore.add(String.format(Lang.getString("messages.completed-operation.drawer.server-uuid"), GuguSlimefunLib.getServerUUID()));
         }

         meta.setLore(lore);
         meta.getPersistentDataContainer().set(idKey, PersistentDataType.INTEGER, id);
         if (Networks.getSupportedPluginManager().isGuguSlimefunLib()) {
            meta.getPersistentDataContainer().set(serverKey, DataType.UUID, GuguSlimefunLib.getServerUUID());
         }
      }

      item.setItemMeta(meta);
      return item;
   }

   @NotNull
   public static ItemStack bindIdNew(@NotNull ItemStack itemSample, int id, boolean lock, boolean voidExcess) {
      ItemStack item = itemSample.clone();
      ItemMeta meta = item.getItemMeta();
      if (meta != null) {
         List<String> lore = meta.getLore();
         if (lore == null) {
            lore = new ArrayList<>();
         }

         lore.add(String.format(Lang.getString("messages.completed-operation.drawer.bound_id"), id));
         if (Networks.getSupportedPluginManager().isGuguSlimefunLib()) {
            lore.add(String.format(Lang.getString("messages.completed-operation.drawer.server-uuid"), GuguSlimefunLib.getServerUUID()));
         }

         meta.setLore(lore);
         meta.getPersistentDataContainer().set(idKey, PersistentDataType.INTEGER, id);
         meta.getPersistentDataContainer().set(lockKey, PersistentDataType.BOOLEAN, lock);
         meta.getPersistentDataContainer().set(voidExcessKey, PersistentDataType.BOOLEAN, voidExcess);
         if (Networks.getSupportedPluginManager().isGuguSlimefunLib()) {
            meta.getPersistentDataContainer().set(serverKey, DataType.UUID, GuguSlimefunLib.getServerUUID());
         }
      }

      item.setItemMeta(meta);
      return item;
   }

   public static void addBlockInfo(@NotNull Location l, int id, boolean lock, boolean voidExcess) {
      StorageCacheUtils.setData(l, "containerId", String.valueOf(id));
      setLock(l, lock);
      setVoidExcess(l, voidExcess);
   }

   public static boolean isLocked(Location l) {
      return locked.contains(l);
   }

   public static boolean isLocked(int containerId) {
      for (Location l : locked) {
         if (getContainerId(l) == containerId) {
            return true;
         }
      }

      return false;
   }

   public static boolean isVoidExcess(Location l) {
      return voidExcesses.contains(l);
   }

   public static boolean isVoidExcess(int containerId) {
      for (Location l : voidExcesses) {
         if (getContainerId(l) == containerId) {
            return true;
         }
      }

      return false;
   }

   @NotNull
   private static ItemStack getDisplayItem(@Nullable ItemStack item, int amount, int max) {
      if (item == null) {
         return Icon.ERROR_BORDER;
      } else {
         try {
            return new CustomItemStack(item, (String)null, new String[]{"", String.format(Lang.getString("icons.drawer.stored_item"), amount, max)});
         } catch (NullPointerException var4) {
            return item.clone();
         }
      }
   }

   public static void requestData(@NotNull Location l, int id) {
      if (id != -1) {
         if (DataStorage.isContainerLoaded(id)) {
            DataStorage.getCachedStorageData(id).ifPresent(data -> storages.put(l, data));
         } else {
            DataStorage.requestStorageData(id);
         }

         addClickHandler(l);
      }
   }

   private static void addClickHandler(@NotNull Location l) {
      BlockMenu blockMenu = StorageCacheUtils.getMenu(l);
      if (blockMenu != null) {
         StorageUnitData data = storages.get(l);

         for (int s : DISPLAY_SLOTS) {
            blockMenu.addMenuClickHandler(s, (player, slot, clickItem, action) -> {
               ItemStack itemOnCursor = player.getItemOnCursor();
               if (itemOnCursor != null && itemOnCursor.getType() != Material.AIR && StackUtils.isBlacklisted(itemOnCursor)) {
                  return false;
               } else {
                  NetworkUtils.clearNearbyNetworks(blockMenu.getLocation());
                  if (StackUtils.itemsMatch(clickItem, Icon.ERROR_BORDER)) {
                     if (itemOnCursor.getType() != Material.AIR) {
                        data.depositItemStack(l, itemOnCursor, false, true);
                     }
                  } else {
                     List<Integer> display = new ArrayList<>();

                     for (int i : DISPLAY_SLOTS) {
                        display.add(i);
                     }

                     int index = display.indexOf(slot);
                     List<ItemContainer> stored = storages.get(l).getStoredItems();
                     if (stored.size() <= index) {
                        return false;
                     }

                     ItemStack take = stored.get(index).getSampleDirectly();
                     ItemRequest itemRequest = new ItemRequest(take, 1);
                     if (action.isShiftClicked() && action.isRightClicked()) {
                        for (ItemStack each : player.getInventory().getStorageContents()) {
                           if (StackUtils.itemsMatch(each, take)) {
                              data.depositItemStack(l, each, true);
                           }
                        }
                     } else {
                        if (action.isRightClicked()) {
                           itemRequest.setAmount(take.getMaxStackSize());
                        } else if (action.isShiftClicked()) {
                           itemRequest.setAmount(take.getMaxStackSize() * 36);
                        }

                        ItemStack requested = data.requestItem(l, itemRequest, false);
                        if (requested != null) {
                           do {
                              int max = Math.min(requested.getAmount(), requested.getMaxStackSize());
                              ItemStack clone = StackUtils.getAsQuantity(requested, max);
                              requested.setAmount(requested.getAmount() - max);
                              InventoryUtil.addItem(player, clone);
                              if (clone.getAmount() > 0) {
                                 data.depositItemStack(l, clone, false);
                              }
                           } while (requested.getAmount() > 0);
                        }
                     }
                  }

                  return false;
               }
            });
         }
      }
   }

   @NotNull
   private static ItemStack getStorageInfoItem(int id, int typeCount, int maxType, int maxEach, boolean locked, boolean voidExcess) {
      return new CustomItemStack(
         Material.LIGHT_BLUE_STAINED_GLASS_PANE,
         Lang.getString("icons.drawer.storage_info.name"),
         new String[]{
            "",
            String.format(Lang.getString("icons.drawer.storage_info.id"), id),
            String.format(Lang.getString("icons.drawer.storage_info.type_count"), typeCount, maxType),
            String.format(Lang.getString("icons.drawer.storage_info.max_each"), maxType, maxEach),
            String.format(
               Lang.getString("icons.drawer.storage_info.locked"),
               locked ? Lang.getString("icons.drawer.storage_info.enabled") : Lang.getString("icons.drawer.storage_info.disabled")
            ),
            String.format(
               Lang.getString("icons.drawer.storage_info.void_excess"),
               voidExcess ? Lang.getString("icons.drawer.storage_info.enabled") : Lang.getString("icons.drawer.storage_info.disabled")
            )
         }
      );
   }

   private static void setLock(@NotNull Location l, boolean lock) {
      if (lock) {
         locked.add(l);
         StorageCacheUtils.setData(l, "locked", "enable");
      }
   }

   private static void setVoidExcess(@NotNull Location l, boolean voidExcess) {
      if (voidExcess) {
         voidExcesses.add(l);
         StorageCacheUtils.setData(l, "voidExcess", "enable");
      }
   }

   private static void switchQuickTransferMode(@NotNull BlockMenu blockMenu, @NotNull Location location) {
      QuickTransferMode mode = quickTransferModes.get(location);
      if (mode != null && mode != QuickTransferMode.TO_QUANTUM) {
         mode = QuickTransferMode.TO_QUANTUM;
      } else {
         mode = QuickTransferMode.FROM_QUANTUM;
      }

      quickTransferModes.put(location, mode);
      blockMenu.replaceExistingItem(18, getQuickTransferItem(mode));
      StorageCacheUtils.setData(location, "quickTransferMode", mode.name());
   }

   private static synchronized void quickTransfer(@NotNull BlockMenu blockMenu, @NotNull Location location, @NotNull Player player) {
      ItemStack itemStack = blockMenu.getItemInSlot(9);
      if (itemStack == null || itemStack.getType() == Material.AIR) {
         player.sendMessage(Lang.getString("messages.unsupported-operation.drawer.invalid_container"));
      } else if (itemStack.getAmount() != 1) {
         player.sendMessage(Lang.getString("messages.unsupported-operation.drawer.invalid_container_amount"));
      } else {
         ItemStack toTransfer = blockMenu.getItemInSlot(27);
         if (toTransfer != null && toTransfer.getType() != Material.AIR) {
            boolean isQuantum = false;
            boolean isMover = false;
            SlimefunItem slimefunItem = SlimefunItem.getByItem(itemStack);
            if (slimefunItem instanceof NetworkQuantumStorage) {
               isQuantum = true;
            } else if (slimefunItem instanceof ItemMover) {
               isMover = true;
            }

            StorageUnitData thisStorage = storages.get(location);
            QuickTransferMode mode = quickTransferModes.get(location);
            if (isQuantum) {
               for (ItemContainer each : thisStorage.getStoredItems()) {
                  ItemStack sample = each.getSampleDirectly();
                  if (StackUtils.itemsMatch(sample, toTransfer)) {
                     ItemMeta meta = itemStack.getItemMeta();
                     QuantumCache quantumCache = Keys.getQuantumCache(meta);
                     switch (mode) {
                        case FROM_QUANTUM:
                           if (quantumCache != null && quantumCache.getItemStack() != null && quantumCache.getAmountLong() > 0L) {
                              if (!StackUtils.itemsMatch(quantumCache.getItemStack(), sample)) {
                                 player.sendMessage(Lang.getString("messages.unsupported-operation.drawer.quantum_storage_item_mismatch"));
                                 return;
                              }

                              long quantumAmount = quantumCache.getAmountLong();
                              int canAddx = (int)Math.min(quantumAmount, (long)(thisStorage.getSizeType().getEachMaxSize() - each.getAmount()));
                              if (canAddx <= 0) {
                                 player.sendMessage(Lang.getString("messages.unsupported-operation.drawer.quantum_storage_full"));
                                 return;
                              }

                              long left = quantumAmount - (long)canAddx;
                              if (left > 0L) {
                                 quantumCache.setAmount(left);
                                 DataTypeMethods.setCustom(meta, Keys.QUANTUM_STORAGE_INSTANCE, PersistentQuantumStorageType.TYPE, quantumCache);
                                 quantumCache.updateMetaLore(meta);
                                 itemStack.setItemMeta(meta);
                              } else {
                                 blockMenu.replaceExistingItem(9, slimefunItem.getItem());
                              }

                              ItemStack clone = quantumCache.getItemStack().clone();
                              clone.setAmount(canAddx);
                              thisStorage.depositItemStack(location, clone, true);
                              player.sendMessage(Lang.getString("messages.completed-operation.drawer.deposited_item"));
                              return;
                           }

                           player.sendMessage(Lang.getString("messages.unsupported-operation.drawer.invalid_quantum_storage"));
                           return;
                        case TO_QUANTUM:
                           if (each.getAmount() == 0 && locked.contains(location)) {
                              player.sendMessage(Lang.getString("messages.unsupported-operation.drawer.not_enough_item"));
                              return;
                           }

                           if (quantumCache == null) {
                              NetworkQuantumStorage nqs = (NetworkQuantumStorage)slimefunItem;
                              long quantumLimit = nqs.getMaxAmount();
                              int unitAmount = each.getAmount();
                              long canAddr = Math.min((long)unitAmount, quantumLimit);
                              int canAdd;
                              if (canAddr > 2147483647L) {
                                 canAdd = Integer.MAX_VALUE;
                              } else {
                                 canAdd = (int)canAddr;
                              }

                              if (canAdd <= 0) {
                                 player.sendMessage(Lang.getString("messages.unsupported-operation.drawer.each_not_enough_item"));
                                 return;
                              }

                              ItemStack clone = sample.clone();
                              thisStorage.requestItem(location, new ItemRequest(clone, canAdd), false);
                              storages.put(location, thisStorage);
                              quantumCache = new QuantumCache(clone, (long)canAdd, quantumLimit, false, false);
                              DataTypeMethods.setCustom(meta, Keys.QUANTUM_STORAGE_INSTANCE, PersistentQuantumStorageType.TYPE, quantumCache);
                              quantumCache.updateMetaLore(meta);
                              itemStack.setItemMeta(meta);
                              player.sendMessage(Lang.getString("messages.completed-operation.drawer.transferred_to_quantum_storage"));
                              return;
                           }

                           if (StackUtils.itemsMatch(quantumCache.getItemStack(), sample)) {
                              long quantumLimitx = quantumCache.getLimitLong();
                              int quantumAmount = (int)quantumCache.getAmountLong();
                              int unitAmountx = each.getAmount();
                              int canAddx = Math.toIntExact(Math.min((long)unitAmountx, quantumLimitx - (long)quantumAmount));
                              if (canAddx <= 0) {
                                 player.sendMessage(Lang.getString("messages.unsupported-operation.drawer.each_not_enough_item"));
                                 return;
                              }

                              ItemStack clone = sample.clone();
                              thisStorage.requestItem(location, new ItemRequest(clone, canAddx));
                              storages.put(location, thisStorage);
                              quantumCache = new QuantumCache(clone, (long)(quantumAmount + canAddx), quantumLimitx, false, false);
                              DataTypeMethods.setCustom(meta, Keys.QUANTUM_STORAGE_INSTANCE, PersistentQuantumStorageType.TYPE, quantumCache);
                              quantumCache.updateMetaLore(meta);
                              itemStack.setItemMeta(meta);
                              player.sendMessage(Lang.getString("messages.completed-operation.drawer.transferred_to_quantum_storage"));
                              return;
                           }

                           return;
                     }
                  }
               }

               player.sendMessage(
                  String.format(Lang.getString("messages.unsupported-operation.drawer.not_found_chosen_item"), ItemStackHelper.getDisplayName(toTransfer))
               );
            } else if (isMover) {
               ItemStack moverStored = ItemMover.getStoredItemStack(itemStack);
               if (mode == QuickTransferMode.FROM_QUANTUM) {
                  if (moverStored == null) {
                     player.sendMessage(Lang.getString("messages.unsupported-operation.drawer.item_mover_empty"));
                     return;
                  }

                  if (!StackUtils.itemsMatch(moverStored, toTransfer)) {
                     player.sendMessage(Lang.getString("messages.unsupported-operation.drawer.item_mover_item_mismatch"));
                     return;
                  }
               }

               if (mode == QuickTransferMode.TO_QUANTUM && moverStored != null && !StackUtils.itemsMatch(moverStored, toTransfer)) {
                  player.sendMessage(Lang.getString("messages.unsupported-operation.drawer.item_mover_item_mismatch"));
                  return;
               }

               for (ItemContainer eachx : thisStorage.getStoredItems()) {
                  ItemStack sample = eachx.getSampleDirectly();
                  if (StackUtils.itemsMatch(sample, toTransfer)) {
                     switch (mode) {
                        case FROM_QUANTUM:
                           long exist = ItemMover.getStoredAmount(itemStack);
                           ItemStack stored = StackUtils.getAsQuantity(
                              ItemMover.getStoredItemStack(itemStack), exist > 2147483647L ? Integer.MAX_VALUE : (int)exist
                           );
                           if (stored == null || stored.getType() == Material.AIR) {
                              player.sendMessage(Lang.getString("messages.unsupported-operation.drawer.item_mover_empty"));
                           }

                           int beforex = stored.getAmount();
                           String name = ItemStackHelper.getDisplayName(stored);
                           thisStorage.depositItemStack(location, stored, true);
                           int left = stored.getAmount();
                           ItemMover.setStoredAmount(itemStack, exist - (long)(beforex - left));
                           player.sendMessage(String.format(Lang.getString("messages.completed-operation.drawer.transferred_to_drawer"), name, beforex - left));
                           break;
                        case TO_QUANTUM:
                           ItemRequest itemRequest = new ItemRequest(sample, eachx.getAmount());
                           int before = eachx.getAmount();
                           ItemStack fetched = thisStorage.requestItem(location, itemRequest, false);
                           if (fetched != null) {
                              String name = ItemStackHelper.getDisplayName(fetched);
                              ItemMover.depositItem(itemStack, fetched);
                              int left = fetched.getAmount();
                              if (fetched.getAmount() > 0) {
                                 thisStorage.depositItemStack(location, fetched, false);
                              }

                              player.sendMessage(
                                 String.format(Lang.getString("messages.completed-operation.drawer.transferred_to_item_mover"), name, before - left)
                              );
                           }
                     }

                     ItemMover.updateLore(itemStack);
                     return;
                  }
               }

               player.sendMessage(
                  String.format(Lang.getString("messages.unsupported-operation.drawer.not_found_chosen_item"), ItemStackHelper.getDisplayName(toTransfer))
               );
            } else {
               player.sendMessage(Lang.getString("messages.unsupported-operation.drawer.invalid_container"));
            }
         } else {
            player.sendMessage(Lang.getString("messages.unsupported-operation.drawer.invalid_chosen_item"));
         }
      }
   }

   private static int getContainerId(@NotNull Location l) {
      String str = StorageCacheUtils.getData(l, "containerId");
      return str == null ? -1 : Integer.parseInt(str);
   }

   @NotNull
   private static ItemStack getQuickTransferItem(QuickTransferMode mode) {
      List<String> lore = new ArrayList<>(Lang.getStringList("icons.drawer.quick_transfer.lore_before_status"));
      lore.add(
         String.format(
            Lang.getString("icons.drawer.quick_transfer.status"),
            mode == QuickTransferMode.FROM_QUANTUM
               ? Lang.getString("icons.drawer.quick_transfer.from_quantum")
               : Lang.getString("icons.drawer.quick_transfer.to_quantum")
         )
      );
      lore.addAll(Lang.getStringList("icons.drawer.quick_transfer.lore_after_status"));
      CustomItemStack cis = new CustomItemStack(
         mode == QuickTransferMode.FROM_QUANTUM ? Material.GREEN_CONCRETE_POWDER : Material.BLUE_CONCRETE_POWDER,
         Lang.getString("icons.drawer.quick_transfer.name"),
         lore
      );
      return ItemStackUtil.getCleanItem(cis);
   }

   @Contract("null -> null")
   public static UUID getServerUUID(@Nullable ItemStack itemStack) {
      if (itemStack == null) {
         return null;
      } else {
         ItemMeta meta = itemStack.getItemMeta();
         return meta == null ? null : (UUID)meta.getPersistentDataContainer().get(serverKey, DataType.UUID);
      }
   }

   public void loadConfigurations() {
      String configKey = this.getId();
      FileConfiguration config = Networks.getInstance().getConfig();
      if (config.getBoolean("items." + configKey + ".use-special-model.enable", false)) {
         this.displayGroupGenerator = location -> DisplayGroupGenerators.generateStorageUnit(location, this.getItem(), this.sizeType);
      }
   }

   public void onPlace(@NotNull BlockPlaceEvent e) {
      Location l = e.getBlock().getLocation();
      ItemStack itemInHand = e.getItemInHand();
      Player p = e.getPlayer();
      if (p.hasPermission("slimefun.inventory.bypass")
         || this.canUse(p, false) && Slimefun.getProtectionManager().hasPermission(p, e.getBlock(), Interaction.PLACE_BLOCK)) {
         boolean a = false;
         boolean b = false;
         UUID suuid = getServerUUID(itemInHand);
         if (Networks.getSupportedPluginManager().isGuguSlimefunLib() && suuid != null && !p.isOp() && !suuid.equals(GuguSlimefunLib.getServerUUID())) {
            p.sendMessage(String.format(Lang.getString("messages.unsupported-operation.drawer.wrong_server"), suuid, GuguSlimefunLib.getServerUUID()));
            e.setCancelled(true);
            if (this.useSpecialModel) {
               this.removeDisplay(l);
            }

            Slimefun.getDatabaseManager().getBlockDataController().removeBlock(l);
         } else {
            int id = getBoundId(itemInHand);
            if (id != -1) {
               StorageUnitData data = DataStorage.getCachedStorageData(id).orElse(null);
               if (data != null && data.isPlaced() && !l.equals(data.getLastLocation())) {
                  p.sendMessage(Lang.getString("messages.unsupported-operation.drawer.already_exists"));
                  Location currLoc = data.getLastLocation();
                  p.sendMessage(
                     TextUtil.YELLOW
                        + (currLoc.getWorld() == null ? Lang.getString("messages.unsupported-operation.drawer.unknown-world") : currLoc.getWorld().getName())
                        + " "
                        + TextUtil.GRAY
                        + "| "
                        + TextUtil.YELLOW
                        + currLoc.getBlockX()
                        + TextUtil.GRAY
                        + "/"
                        + TextUtil.YELLOW
                        + currLoc.getBlockY()
                        + TextUtil.GRAY
                        + "/"
                        + TextUtil.YELLOW
                        + currLoc.getBlockZ()
                        + TextUtil.GRAY
                        + ";"
                  );
                  e.setCancelled(true);
                  if (this.useSpecialModel) {
                     this.removeDisplay(l);
                  }

                  Slimefun.getDatabaseManager().getBlockDataController().removeBlock(l);
                  return;
               }

               if (data != null) {
                  Location lastLoc = data.getLastLocation();
                  a = locked.contains(lastLoc);
                  if (!a) {
                     a = getLock(itemInHand);
                  }

                  b = voidExcesses.contains(lastLoc);
                  if (!b) {
                     b = getVoidExcess(itemInHand);
                  }

                  if (a) {
                     locked.remove(lastLoc);
                  }

                  if (b) {
                     voidExcesses.remove(lastLoc);
                  }
               }

               requestData(l, id);
               if (p.getGameMode() == GameMode.CREATIVE) {
                  itemInHand.setAmount(itemInHand.getAmount() - 1);
                  p.getEquipment().setItem(e.getHand(), itemInHand);
               }
            } else {
               StorageUnitData datax = DataStorage.createStorageUnitData(p, this.sizeType, l);
               id = datax.getId();
               storages.put(l, datax);
            }

            BlockMenu menu = StorageCacheUtils.getMenu(l);
            if (menu != null) {
               update(l, true);
               addClickHandler(l);
            }

            if (this.useSpecialModel) {
               e.getBlock().setType(Material.BARRIER);
               this.setupDisplay(e.getBlock().getLocation());
            }

            addBlockInfo(l, id, a, b);
         }
      }
   }

   public void onBreak(@NotNull BlockBreakEvent e) {
      Block b = e.getBlock();
      Location l = b.getLocation();
      if (this.useSpecialModel) {
         this.removeDisplay(l);
      }

      e.setCancelled(true);
      BlockMenu menu = StorageCacheUtils.getMenu(l);
      if (menu != null) {
         menu.dropItems(l, new int[]{9});
         menu.dropItems(l, new int[]{27});
      }

      StorageUnitData data = storages.remove(l);
      DrawerVirtualCache.removeDrawer(l);
      b.setType(Material.AIR);
      if (data != null) {
         data.setPlaced(false);
         int id = data.getId();
         b.getWorld().dropItemNaturally(l, bindIdNew(this.getItem(), id, isLocked(id), isVoidExcess(id)));
      } else {
         int id = getContainerId(l);
         if (id != -1) {
            DataStorage.setContainerStatus(id, false);
            b.getWorld().dropItemNaturally(l, bindIdNew(this.getItem(), id, isLocked(id), isVoidExcess(id)));
         }
      }

      Slimefun.getDatabaseManager().getBlockDataController().removeBlock(l);
   }

   public void preRegister() {
      this.addItemHandler(new ItemHandler[]{new BlockTicker() {
         public boolean isSynchronized() {
            return false;
         }

         public void tick(@NotNull Block block, SlimefunItem item, SlimefunBlockData blockData) {
            NetworksDrawer.this.onTick(block);
         }
      }});
      this.addItemHandler(new ItemHandler[]{new BlockPlaceHandler(false) {
         public void onPlayerPlace(@NotNull BlockPlaceEvent blockPlaceEvent) {
            NetworksDrawer.this.onPlace(blockPlaceEvent);
         }
      }});
      this.addItemHandler(new ItemHandler[]{new BlockBreakHandler(false, false) {
         public void onPlayerBreak(@NotNull BlockBreakEvent blockBreakEvent, @NotNull ItemStack itemStack, @NotNull List<ItemStack> list) {
            NetworksDrawer.this.onBreak(blockBreakEvent);
         }
      }});
   }

   private void onTick(@NotNull Block block) {
      Location l = block.getLocation();
      StorageUnitData data = storages.get(l);
      if (data == null) {
         requestData(l, getContainerId(l));
      } else {
         if (!isLocked(l)) {
            for (ItemContainer each : data.getStoredItems()) {
               if (each.getAmount() == 0) {
                  data.removeItem(each.getId());
               }
            }
         }

         BlockMenu menu = StorageCacheUtils.getMenu(l);
         if (menu != null) {
            if (!l.equals(data.getLastLocation())) {
               ItemStack itemInBorder = menu.getItemInSlot(0);
               if (data.isPlaced() && itemInBorder != null) {
                  menu.replaceExistingItem(4, this.getLocationErrorItem(data.getId(), data.getLastLocation()));

                  for (int slot : this.BORDER) {
                     menu.replaceExistingItem(slot, Icon.ERROR_BORDER);
                  }

                  return;
               }

               data.setPlaced(true);
               data.setLastLocation(l);
            }

            if (menu.hasViewer()) {
               update(l, false);
            }
         }
      }
   }

   @NotNull
   private ItemStack getLocationErrorItem(int id, @Nullable Location lastLoc) {
      List<String> lore = new ArrayList<>(Lang.getStringList("icons.drawer.location_error.lore_before_info"));
      lore.add(String.format(Lang.getString("icons.drawer.location_error.id"), id));
      lore.add(
         lastLoc == null
            ? Lang.getString("icons.drawer.location_error.unknown")
            : String.format(Lang.getString("icons.drawer.location_error.world"), lastLoc.getWorld().getName())
      );
      lore.add(
         lastLoc == null
            ? Lang.getString("icons.drawer.location_error.unknown")
            : String.format(Lang.getString("icons.drawer.location_error.location"), lastLoc.getBlockX(), lastLoc.getBlockY(), lastLoc.getBlockZ())
      );
      return new CustomItemStack(Material.REDSTONE_TORCH, Lang.getString("icons.drawer.location_error.name"), lore);
   }

   private void switchLock(@NotNull BlockMenu menu, @NotNull Location l) {
      if (locked.contains(l)) {
         StorageCacheUtils.removeData(l, "locked");
         locked.remove(l);
         menu.replaceExistingItem(8, this.getContentLockItem(false));
      } else {
         StorageCacheUtils.setData(l, "locked", "enable");
         locked.add(l);
         menu.replaceExistingItem(8, this.getContentLockItem(true));
      }
   }

   private void switchVoidExcess(@NotNull BlockMenu menu, @NotNull Location l) {
      if (voidExcesses.contains(l)) {
         StorageCacheUtils.removeData(l, "voidExcess");
         voidExcesses.remove(l);
         menu.replaceExistingItem(7, this.getVoidExcessItem(false));
      } else {
         StorageCacheUtils.setData(l, "voidExcess", "enable");
         voidExcesses.add(l);
         menu.replaceExistingItem(7, this.getVoidExcessItem(true));
      }
   }

   @NotNull
   private ItemStack getContentLockItem(boolean locked) {
      List<String> lore = new ArrayList<>(Lang.getStringList("icons.drawer.content_lock.lore_before_status"));
      lore.add(
         String.format(
            Lang.getString("icons.drawer.content_lock.status"),
            locked ? Lang.getString("icons.drawer.content_lock.locked") : Lang.getString("icons.drawer.content_lock.unlocked")
         )
      );
      lore.addAll(Lang.getStringList("icons.drawer.content_lock.lore_after_status"));
      lore.add(locked ? Lang.getString("icons.drawer.click_to_disable") : Lang.getString("icons.drawer.click_to_enable"));
      return new CustomItemStack(
         locked ? Material.RED_STAINED_GLASS_PANE : Material.LIME_STAINED_GLASS_PANE, Lang.getString("icons.drawer.content_lock.name"), lore
      );
   }

   @NotNull
   private ItemStack getVoidExcessItem(boolean voidExcess) {
      List<String> lore = new ArrayList<>(Lang.getStringList("icons.drawer.void_excess.lore_before_status"));
      lore.add(
         String.format(
            Lang.getString("icons.drawer.void_excess.status"),
            voidExcess ? Lang.getString("icons.drawer.void_excess.enabled") : Lang.getString("icons.drawer.void_excess.disabled")
         )
      );
      lore.addAll(Lang.getStringList("icons.drawer.void_excess.lore_after_status"));
      lore.add(voidExcess ? Lang.getString("icons.drawer.click_to_disable") : Lang.getString("icons.drawer.click_to_enable"));
      return new CustomItemStack(
         voidExcess ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE, Lang.getString("icons.drawer.void_excess.name"), lore
      );
   }

   private void setupDisplay(@NotNull Location location) {
      if (this.displayGroupGenerator != null) {
         DisplayGroup displayGroup = this.displayGroupGenerator.apply(location.clone().add(0.5, 0.0, 0.5));
         StorageCacheUtils.setData(location, "display-uuid", displayGroup.getParentUUID().toString());
      }
   }

   private void removeDisplay(@NotNull Location location) {
      DisplayGroup group = this.getDisplayGroup(location);
      if (group != null) {
         group.remove();
      }
   }

   @Nullable
   private UUID getDisplayGroupUUID(@NotNull Location location) {
      String uuid = StorageCacheUtils.getData(location, "display-uuid");
      return uuid == null ? null : UUID.fromString(uuid);
   }

   @Nullable
   private DisplayGroup getDisplayGroup(@NotNull Location location) {
      UUID uuid = this.getDisplayGroupUUID(location);
      return uuid == null ? null : DisplayGroup.fromUUID(uuid);
   }

   public boolean canStack(@NotNull ItemMeta meta1, @NotNull ItemMeta meta2) {
      return meta1.getPersistentDataContainer().equals(meta2.getPersistentDataContainer());
   }
}
