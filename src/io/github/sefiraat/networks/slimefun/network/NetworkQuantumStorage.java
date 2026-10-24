package io.github.sefiraat.networks.slimefun.network;

import com.balugaq.netex.api.algorithm.Calculator;
import com.balugaq.netex.api.enums.FeedbackType;
import com.balugaq.netex.api.helpers.Icon;
import com.balugaq.netex.utils.BlockMenuUtil;
import com.balugaq.netex.utils.Lang;
import com.balugaq.netex.utils.RootWriteLock;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.core.items.SpecialSlimefunItem;
import com.ytdd9527.networksexpansion.utils.itemstacks.ItemStackUtil;
import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.network.NodeDefinition;
import io.github.sefiraat.networks.network.stackcaches.ItemStackCache;
import io.github.sefiraat.networks.network.stackcaches.QuantumCache;
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
import io.github.thebusybiscuit.slimefun4.libraries.dough.data.persistent.PersistentDataAPI;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.utils.ChatUtils;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.ParametersAreNonnullByDefault;
import lombok.Generated;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NetworkQuantumStorage extends SpecialSlimefunItem implements DistinctiveItem {
   public static final long MAX_AMOUNT = 140737488355328L;
   public static final String BS_AMOUNT = "stored_amount";
   public static final String BS_VOID = "void_excess";
   public static final String BS_CUSTOM_MAX_AMOUNT = "custom_max_amount";
   public static final int INPUT_SLOT = 1;
   public static final int ITEM_SLOT = 4;
   public static final int ITEM_SET_SLOT = 13;
   public static final int OUTPUT_SLOT = 7;
   private static final long[] SIZES = new long[]{
      64L,
      256L,
      1024L,
      4096L,
      32768L,
      262144L,
      2097152L,
      16777216L,
      134217728L,
      1073741824L,
      2147483647L,
      34359738352L,
      549755813632L,
      8796093018112L,
      140737488289792L
   };
   private static final String WIKI_PAGE = "network-storage/quantum-storage";
   private static final int[] INPUT_SLOTS = new int[]{0, 2};
   private static final int[] ITEM_SLOTS = new int[]{3, 5};
   private static final int[] OUTPUT_SLOTS = new int[]{6, 8};
   private static final int[] BACKGROUND_SLOTS = new int[]{9, 10, 11, 12, 14, 15, 16, 17};
   private static final Map<Location, QuantumCache> CACHES = new ConcurrentHashMap<>();
   private final List<Integer> slotsToDrop = new ArrayList<>();
   private final long maxAmount;
   private boolean supportsCustomMaxAmount = false;

   public NetworkQuantumStorage(
      @NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe, long maxAmount
   ) {
      super(itemGroup, item, recipeType, recipe);
      this.maxAmount = maxAmount;
      this.slotsToDrop.add(1);
      this.slotsToDrop.add(7);
   }

   public static void setItem(@NotNull BlockMenu blockMenu, @NotNull ItemStack itemStack, int amount) {
      setItem(blockMenu, itemStack, (long)amount);
   }

   public static void setItem(@NotNull BlockMenu blockMenu, @NotNull ItemStack itemStack, long amount) {
      if (!StackUtils.isBlacklisted(itemStack)) {
         if (!Networks.getConfigManager().isBanQuantumInQuantum() || !(SlimefunItem.getByItem(itemStack) instanceof NetworkQuantumStorage)) {
            QuantumCache cache = CACHES.get(blockMenu.getLocation());
            if (cache != null && cache.getAmountLong() <= 0L) {
               itemStack.setAmount(1);
               cache.setItemStack(itemStack);
               cache.setAmount(amount);
               updateDisplayItem(blockMenu, cache);
               syncBlock(blockMenu.getLocation(), cache);
               CACHES.put(blockMenu.getLocation(), cache);
            }
         }
      }
   }

   @ParametersAreNonnullByDefault
   public static void tryInputItem(Location location, ItemStack[] input, QuantumCache cache) {
      if (cache.getItemStack() != null) {
         for (ItemStack itemStack : input) {
            if (!StackUtils.isBlacklisted(itemStack) && StackUtils.itemsMatch(cache, itemStack)) {
               int leftover = cache.increaseAmount(itemStack.getAmount());
               itemStack.setAmount(leftover);
            }
         }

         syncBlock(location, cache);
      }
   }

   @ParametersAreNonnullByDefault
   @Nullable
   public static ItemStack getItemStack(@NotNull QuantumCache cache, @NotNull BlockMenu blockMenu) {
      return cache.getItemStack() != null && cache.getAmountLong() > 0L ? getItemStack(cache, blockMenu, cache.getItemStack().getMaxStackSize()) : null;
   }

   @ParametersAreNonnullByDefault
   @Nullable
   public static synchronized ItemStack getItemStack(@NotNull QuantumCache cache, @NotNull BlockMenu blockMenu, int amount) {
      if (cache.getAmountLong() >= (long)amount) {
         ItemStack fetched = cache.withdrawItem(amount);
         syncBlock(blockMenu.getLocation(), cache);
         return fetched;
      } else {
         ItemStack output = blockMenu.getItemInSlot(7);
         ItemStack fetched = cache.withdrawItem(amount);
         if (output != null && output.getType() != Material.AIR && StackUtils.itemsMatch(cache, output)) {
            if (fetched != null && fetched.getType() != Material.AIR) {
               int additional = Math.min(amount - fetched.getAmount(), output.getAmount());
               output.setAmount(output.getAmount() - additional);
               fetched.setAmount(fetched.getAmount() + additional);
            } else {
               fetched = output.clone();
               if (fetched.getAmount() > amount) {
                  fetched.setAmount(amount);
               }

               output.setAmount(output.getAmount() - fetched.getAmount());
            }
         }

         syncBlock(blockMenu.getLocation(), cache);
         return fetched;
      }
   }

   public static void updateDisplayItem(@NotNull BlockMenu menu, @NotNull QuantumCache cache) {
      if (cache.getItemStack() == null) {
         menu.replaceExistingItem(4, Icon.QUANTUM_STORAGE_NO_ITEM);
      } else {
         ItemStack itemStack = cache.getItemStack().clone();
         if (itemStack.getType() == Material.AIR) {
            menu.replaceExistingItem(4, Icon.QUANTUM_STORAGE_NO_ITEM);
            return;
         }

         ItemMeta itemMeta = itemStack.getItemMeta();
         if (itemMeta == null) {
            menu.replaceExistingItem(4, Icon.QUANTUM_STORAGE_NO_ITEM);
            return;
         }

         List<String> lore = (List<String>)(itemMeta.hasLore() ? itemMeta.getLore() : new ArrayList<>());
         if (lore == null) {
            lore = new ArrayList<>();
         }

         lore.add("");
         lore.add(
            String.format(
               Lang.getString("displays.quantum_storage.void_excess"),
               cache.isVoidExcess()
                  ? Lang.getString("displays.quantum_storage.enabled_void_excess")
                  : Lang.getString("displays.quantum_storage.disabled_void_excess")
            )
         );
         lore.add(String.format(Lang.getString("displays.quantum_storage.stored_amount"), cache.getAmountLong()));
         if (cache.supportsCustomMaxAmount()) {
            lore.add(String.format(Lang.getString("displays.quantum_storage.custom_max_amount"), cache.getLimitLong()));
         }

         itemMeta.setLore(lore);
         itemStack.setItemMeta(itemMeta);
         itemStack.setAmount(1);
         menu.replaceExistingItem(4, itemStack);
      }
   }

   public static void syncBlock(@NotNull Location location, @NotNull QuantumCache cache) {
      SlimefunBlockData blockData = StorageCacheUtils.getBlock(location);
      if (blockData != null) {
         blockData.setData("stored_amount", String.valueOf(cache.getAmountLong()));
         blockData.setData("void_excess", String.valueOf(cache.isVoidExcess()));
         if (cache.supportsCustomMaxAmount()) {
            StorageCacheUtils.setData(location, "custom_max_amount", String.valueOf(cache.getLimitLong()));
         }
      }
   }

   @NotNull
   public static Map<Location, QuantumCache> getCaches() {
      return CACHES;
   }

   public static int[] getSizes() {
      return Arrays.stream(SIZES).map(a -> a > 2147483647L ? 2147483647L : a).mapToInt(i -> (int)i).toArray();
   }

   public static long[] getSizesLong() {
      return SIZES;
   }

   public static void setItem(@NotNull BlockMenu blockMenu, @NotNull Player player) {
      synchronized (RootWriteLock.get()) {
         ItemStack itemStack = player.getItemOnCursor().clone();
         if (!StackUtils.isBlacklisted(itemStack)) {
            QuantumCache cache = CACHES.get(blockMenu.getLocation());
            if (cache != null && cache.getAmountLong() <= 0L) {
               itemStack.setAmount(1);
               cache.setItemStack(itemStack);
               updateDisplayItem(blockMenu, cache);
               syncBlock(blockMenu.getLocation(), cache);
               CACHES.put(blockMenu.getLocation(), cache);
            } else {
               player.sendMessage(Lang.getString("messages.unsupported-operation.quantum_storage.quantum_storage_not_empty"));
            }
         }
      }
   }

   public void preRegister() {
      this.addItemHandler(new ItemHandler[]{new BlockTicker() {
         public boolean isSynchronized() {
            return false;
         }

         public void tick(@NotNull Block b, SlimefunItem item, SlimefunBlockData data) {
            NetworkQuantumStorage.this.onTick(b);
         }
      }, new BlockBreakHandler(false, false) {
         @ParametersAreNonnullByDefault
         public void onPlayerBreak(BlockBreakEvent event, ItemStack item, List<ItemStack> drops) {
            NetworkQuantumStorage.this.onBreak(event);
         }
      }, new BlockPlaceHandler(false) {
         public void onPlayerPlace(@NotNull BlockPlaceEvent event) {
            NetworkQuantumStorage.this.onPlace(event);
         }
      }});
   }

   private void onTick(@NotNull Block block) {
      BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
      if (blockMenu == null) {
         CACHES.remove(block.getLocation());
      } else {
         QuantumCache cache = CACHES.get(blockMenu.getLocation());
         if (cache != null) {
            if (blockMenu.hasViewer()) {
               updateDisplayItem(blockMenu, cache);
            }

            ItemStack input = blockMenu.getItemInSlot(1);
            if (input != null && input.getType() != Material.AIR) {
               tryInputItem(blockMenu.getLocation(), new ItemStack[]{input}, cache);
               blockMenu.markDirty();
            }

            ItemStack output = blockMenu.getItemInSlot(7);
            ItemStack fetched = null;
            if (output == null || output.getType() == Material.AIR) {
               fetched = cache.withdrawItem();
            } else if (output.getAmount() < output.getMaxStackSize() && StackUtils.itemsMatch(cache, output)) {
               int requestAmount = output.getMaxStackSize() - output.getAmount();
               fetched = cache.withdrawItem(requestAmount);
            }

            if (fetched != null && fetched.getType() != Material.AIR) {
               BlockMenuUtil.pushItem(blockMenu, fetched, 7);
               blockMenu.markDirty();
               syncBlock(blockMenu.getLocation(), cache);
            }

            CACHES.put(blockMenu.getLocation().clone(), cache);
            this.sendFeedback(blockMenu.getLocation(), FeedbackType.WORKING);
         }
      }
   }

   private void toggleVoid(@NotNull BlockMenu blockMenu) {
      QuantumCache cache = CACHES.get(blockMenu.getLocation());
      cache.setVoidExcess(!cache.isVoidExcess());
      updateDisplayItem(blockMenu, cache);
      syncBlock(blockMenu.getLocation(), cache);
      CACHES.put(blockMenu.getLocation(), cache);
   }

   private void setCustomMaxAmount(@NotNull BlockMenu blockMenu, @NotNull Player player, long newMaxAmount) {
      QuantumCache cache = CACHES.get(blockMenu.getLocation());
      if (cache != null && cache.supportsCustomMaxAmount()) {
         cache.setLimit(newMaxAmount);
         updateDisplayItem(blockMenu, cache);
         syncBlock(blockMenu.getLocation(), cache);
         CACHES.put(blockMenu.getLocation(), cache);
         player.sendMessage(String.format(Lang.getString("messages.completed-operation.quantum_storage.changed_custom_max_amount"), newMaxAmount));
      } else {
         ItemStackUtil.send(player, Lang.getString("messages.unsupported-operation.quantum_storage.custom_max_amount_not_supported_new"));
      }
   }

   public void postRegister() {
      this.addWikiPage("network-storage/quantum-storage");
      new BlockMenuPreset(this.getId(), this.getItemName()) {
         public void init() {
            for (int i : NetworkQuantumStorage.INPUT_SLOTS) {
               this.addItem(i, Icon.QUANTUM_STORAGE_BACK_INPUT, (p, slot, item, action) -> false);
            }

            for (int i : NetworkQuantumStorage.ITEM_SLOTS) {
               this.addItem(i, Icon.QUANTUM_STORAGE_BACK_ITEM, (p, slot, item, action) -> false);
            }

            for (int i : NetworkQuantumStorage.OUTPUT_SLOTS) {
               this.addItem(i, Icon.QUANTUM_STORAGE_BACK_OUTPUT, (p, slot, item, action) -> false);
            }

            ItemStack setItemItemstack = NetworkQuantumStorage.this.supportsCustomMaxAmount
               ? Icon.QUANTUM_STORAGE_SET_ITEM_SUPPORTING_CUSTOM_MAX
               : Icon.QUANTUM_STORAGE_SET_ITEM;
            this.addItem(13, setItemItemstack, (p, slot, item, action) -> false);
            this.addMenuClickHandler(4, ChestMenuUtils.getEmptyClickHandler());
            this.drawBackground(NetworkQuantumStorage.BACKGROUND_SLOTS);
         }

         public boolean canOpen(@NotNull Block block, @NotNull Player player) {
            return player.hasPermission("slimefun.inventory.bypass")
               || Slimefun.getProtectionManager().hasPermission(player, block.getLocation(), Interaction.INTERACT_BLOCK);
         }

         public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
            if (flow == ItemTransportFlow.INSERT) {
               return new int[]{1};
            } else {
               return flow == ItemTransportFlow.WITHDRAW ? new int[]{7} : new int[0];
            }
         }

         public void newInstance(@NotNull BlockMenu menu, @NotNull Block block) {
            menu.addMenuClickHandler(13, (p, slot, item, action) -> {
               QuantumCache cachex = NetworkQuantumStorage.CACHES.get(menu.getLocation());
               if (action.isShiftClicked()) {
                  NetworkQuantumStorage.this.toggleVoid(menu);
               } else if (cachex != null && cachex.supportsCustomMaxAmount() && p.getItemOnCursor().getType() == Material.AIR) {
                  p.closeInventory();
                  p.sendMessage(Lang.getString("messages.normal-operation.quantum_storage.waiting_for_input_custom_max_amount_new"));
                  ChatUtils.awaitInput(p, s -> {
                     try {
                        if (s.isBlank()) {
                           return;
                        }

                        long newMax = Math.max(1L, Math.min(Calculator.calculate(s).longValue(), NetworkQuantumStorage.this.maxAmount));
                        if (newMax > 140737488355328L) {
                           p.sendMessage(Lang.getString("messages.unsupported-operation.quantum_storage.invalid_custom_max_amount_new"));
                           return;
                        }

                        NetworkQuantumStorage.this.setCustomMaxAmount(menu, p, newMax);
                     } catch (NumberFormatException var6x) {
                        p.sendMessage(Lang.getString("messages.unsupported-operation.quantum_storage.invalid_custom_max_amount_new"));
                        p.sendMessage(var6x.getMessage());
                     }
                  });
               } else {
                  NetworkQuantumStorage.setItem(menu, p);
               }

               return false;
            });
            if (Networks.getConfigManager().isFastInteractQuantum()) {
               int INSERT_ALL_SLOT = 16;
               menu.replaceExistingItem(INSERT_ALL_SLOT, Icon.QUICK_INPUT);
               menu.addMenuClickHandler(INSERT_ALL_SLOT, (pl, slot, item, action) -> {
                  NetworkQuantumStorage.this.insertAll(pl, menu, block);
                  return false;
               });
               int EXTRACT_SLOT = 17;
               menu.replaceExistingItem(EXTRACT_SLOT, Icon.QUICK_OUTPUT);
               menu.addMenuClickHandler(EXTRACT_SLOT, (pl, slot, item, action) -> {
                  NetworkQuantumStorage.this.extract(pl, menu, block, action);
                  return false;
               });
            }

            QuantumCache cache = NetworkQuantumStorage.CACHES.get(block.getLocation());
            if (cache == null) {
               cache = NetworkQuantumStorage.this.addCache(menu);
            }

            NetworkQuantumStorage.updateDisplayItem(menu, cache);
         }
      };
   }

   public synchronized void insertAll(@NotNull Player p, @NotNull BlockMenu menu, @NotNull Block b) {
      PlayerInventory inv = p.getInventory();
      QuantumCache cache = CACHES.get(menu.getLocation());
      if (cache != null) {
         ItemStack storedItem = cache.getItemStack();
         if (storedItem != null) {
            long capacity = cache.getLimitLong();
            ItemStack[] contents = inv.getContents();

            for (int i = 0; i < contents.length; i++) {
               ItemStack item = contents[i];
               if (item != null && item.getType() != Material.AIR) {
                  ItemStackCache storedItemCache = new ItemStackCache(storedItem);
                  if (StackUtils.itemsMatch(storedItemCache, item)) {
                     int toAdd = Math.toIntExact(Math.min((long)item.getAmount(), capacity - cache.getAmountLong()));
                     if (toAdd > 0) {
                        NetworkUtils.clearNearbyNetworks(menu.getLocation());
                        item.setAmount(item.getAmount() - toAdd);
                        cache.increaseAmount(toAdd);
                        if (item.getAmount() == 0) {
                           inv.setItem(i, null);
                        }
                     }
                  }
               }
            }

            syncBlock(b.getLocation(), cache);
            updateDisplayItem(menu, cache);
         }
      }
   }

   public synchronized void extract(@NotNull Player p, @NotNull BlockMenu menu, @NotNull Block b, @NotNull ClickAction action) {
      QuantumCache cache = CACHES.get(menu.getLocation());
      if (cache != null) {
         ItemStack storedItem = cache.getItemStack();
         if (storedItem != null) {
            long stored = cache.getAmountLong();
            if (action.isShiftClicked() && action.isRightClicked()) {
               ItemStack extractedItem = cache.withdrawItem(64);
               if (extractedItem != null) {
                  ItemStackUtil.giveOrDropItem(p, extractedItem);
               }
            } else if (action.isRightClicked()) {
               ItemStack extractedItem = cache.withdrawItem(1);
               if (extractedItem != null) {
                  ItemStackUtil.giveOrDropItem(p, extractedItem);
               }
            } else {
               PlayerInventory inv = p.getInventory();
               ItemStack[] contents = inv.getStorageContents();

               for (int i = 0; i < contents.length; i++) {
                  if (contents[i] == null || contents[i].getType() == Material.AIR) {
                     if (stored == 0L) {
                        break;
                     }

                     long amountToExtractr = Math.min(stored, (long)storedItem.getMaxStackSize());
                     int amountToExtract = amountToExtractr > 2147483647L ? Integer.MAX_VALUE : (int)amountToExtractr;
                     ItemStack itemToInsert = storedItem.clone();
                     itemToInsert.setAmount(amountToExtract);
                     contents[i] = itemToInsert;
                     NetworkUtils.clearNearbyNetworks(menu.getLocation());
                     cache.reduceAmount(amountToExtract);
                     stored -= (long)amountToExtract;
                     if (stored == 0L) {
                        break;
                     }
                  }
               }

               p.getInventory().setStorageContents(contents);
               updateDisplayItem(menu, cache);
            }

            syncBlock(b.getLocation(), cache);
         }
      }
   }

   @NotNull
   private QuantumCache addCache(@NotNull BlockMenu blockMenu) {
      Location location = blockMenu.getLocation();
      SlimefunBlockData blockData = StorageCacheUtils.getBlock(location);
      if (blockData == null) {
         return new QuantumCache(null, 0L, this.maxAmount, false, this.supportsCustomMaxAmount);
      } else {
         String amountString = blockData.getData("stored_amount");
         String voidString = blockData.getData("void_excess");
         long amount = amountString == null ? 0L : Long.parseLong(amountString);
         boolean voidExcess = Boolean.parseBoolean(voidString);
         long maxAmount = this.maxAmount;
         if (this.supportsCustomMaxAmount) {
            String customMaxAmountString = BlockStorage.getLocationInfo(blockMenu.getLocation(), "custom_max_amount");
            if (customMaxAmountString != null) {
               maxAmount = Long.parseLong(customMaxAmountString);
            }
         }

         ItemStack itemStack = blockMenu.getItemInSlot(4);
         QuantumCache cache = this.createCache(itemStack, blockMenu, amount, maxAmount, voidExcess, this.supportsCustomMaxAmount);
         CACHES.put(location, cache);
         return cache;
      }
   }

   @NotNull
   private QuantumCache createCache(
      @Nullable ItemStack itemStack, @NotNull BlockMenu menu, long amount, long maxAmount, boolean voidExcess, boolean supportsCustomMaxAmount
   ) {
      if (itemStack != null && itemStack.getType() != Material.AIR && !this.isDisplayItem(itemStack) && !StackUtils.isBlacklisted(itemStack)) {
         ItemStack clone = itemStack.clone();
         ItemMeta itemMeta = clone.getItemMeta();
         List<String> lore = itemMeta.getLore();

         for (int i = 0; i < 3 && lore != null && !lore.isEmpty(); i++) {
            lore.remove(lore.size() - 1);
         }

         if (supportsCustomMaxAmount && lore != null && !lore.isEmpty()) {
            lore.remove(lore.size() - 1);
         }

         itemMeta.setLore(lore != null && !lore.isEmpty() ? lore : null);
         clone.setItemMeta(itemMeta);
         QuantumCache cache = new QuantumCache(clone, amount, maxAmount, voidExcess, this.supportsCustomMaxAmount);
         updateDisplayItem(menu, cache);
         return cache;
      } else {
         menu.addItem(4, ItemStackUtil.getCleanItem(Icon.QUANTUM_STORAGE_NO_ITEM));
         return new QuantumCache(null, 0L, maxAmount, false, this.supportsCustomMaxAmount);
      }
   }

   private boolean isDisplayItem(@NotNull ItemStack itemStack) {
      return PersistentDataAPI.getBoolean(itemStack.getItemMeta(), Keys.newKey("display"));
   }

   protected void onBreak(@NotNull BlockBreakEvent event) {
      Location location = event.getBlock().getLocation();
      BlockMenu blockMenu = StorageCacheUtils.getMenu(event.getBlock().getLocation());
      if (blockMenu != null) {
         QuantumCache cache = CACHES.remove(blockMenu.getLocation());
         if (cache != null && cache.getAmountLong() > 0L && cache.getItemStack() != null) {
            ItemStack itemToDrop = this.getItem().clone();
            ItemMeta itemMeta = itemToDrop.getItemMeta();
            DataTypeMethods.setCustom(itemMeta, Keys.QUANTUM_STORAGE_INSTANCE, PersistentQuantumStorageType.TYPE, cache);
            cache.addMetaLore(itemMeta);
            itemToDrop.setItemMeta(itemMeta);
            location.getWorld().dropItem(location.clone().add(0.5, 0.5, 0.5), itemToDrop);
            event.setDropItems(false);
         }

         for (int i : this.slotsToDrop) {
            blockMenu.dropItems(location, new int[]{i});
         }
      }
   }

   protected void onPlace(@NotNull BlockPlaceEvent event) {
      ItemStack itemStack = event.getItemInHand();
      ItemMeta itemMeta = itemStack.getItemMeta();
      QuantumCache cache = Keys.getQuantumCache(itemMeta);
      if (cache != null) {
         if (cache.getItemStack() == null || !StackUtils.isBlacklisted(cache.getItemStack())) {
            syncBlock(event.getBlock().getLocation(), cache);
            CACHES.put(event.getBlock().getLocation(), cache);
         }
      }
   }

   public boolean supportsCustomMaxAmount() {
      return this.supportsCustomMaxAmount;
   }

   public boolean canStack(@NotNull ItemMeta sfItemMeta, @NotNull ItemMeta itemMeta) {
      return sfItemMeta.getPersistentDataContainer().equals(itemMeta.getPersistentDataContainer());
   }

   public static boolean isBlacklisted(@NotNull ItemStack itemStack) {
      return StackUtils.isBlacklisted(itemStack);
   }

   private static boolean dupeDetect(Block storage, Player player) {
      for (BlockFace face : NetworkDirectional.VALID_FACES) {
         NodeDefinition definition = NetworkStorage.getNode(storage.getRelative(face).getLocation());
         if (definition != null && definition.getNode() != null && definition.getNode().getRoot().getCellsSize() != 0) {
            Networks.getInstance()
               .getLogger()
               .warning(String.format(Lang.getString("messages.unsupported-operation.quantum_storage.may_duping"), player.getName(), storage.getLocation()));
            player.sendMessage(Lang.getString("messages.unsupported-operation.quantum_storage.dangerous_operation"));
            return true;
         }
      }

      return false;
   }

   @Generated
   public long getMaxAmount() {
      return this.maxAmount;
   }

   @Generated
   public void setSupportsCustomMaxAmount(boolean supportsCustomMaxAmount) {
      this.supportsCustomMaxAmount = supportsCustomMaxAmount;
   }

   static {
      ItemMeta itemMeta = Icon.QUANTUM_STORAGE_NO_ITEM.getItemMeta();
      PersistentDataAPI.setBoolean(itemMeta, Keys.newKey("display"), true);
      Icon.QUANTUM_STORAGE_NO_ITEM.setItemMeta(itemMeta);
   }
}
