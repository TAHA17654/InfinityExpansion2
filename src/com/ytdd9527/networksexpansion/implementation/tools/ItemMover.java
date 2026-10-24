package com.ytdd9527.networksexpansion.implementation.tools;

import com.balugaq.netex.utils.Lang;
import com.jeff_media.morepersistentdatatypes.DataType;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.core.items.SpecialSlimefunItem;
import com.ytdd9527.networksexpansion.implementation.ExpansionItemStacks;
import com.ytdd9527.networksexpansion.implementation.machines.unit.NetworksDrawer;
import io.gitee.crankygoudan.tsingshantechnology.items.chucun.StorageUnit;
import io.github.mooy1.infinityexpansion.items.storage.StorageCache;
import io.github.sefiraat.networks.managers.SupportedPluginManager;
import io.github.sefiraat.networks.network.barrel.FluffyBarrel;
import io.github.sefiraat.networks.network.barrel.InfinityBarrel;
import io.github.sefiraat.networks.network.barrel.NetworkStorage;
import io.github.sefiraat.networks.network.barrel.TsingshanBarrel;
import io.github.sefiraat.networks.network.stackcaches.BarrelIdentity;
import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import io.github.sefiraat.networks.network.stackcaches.QuantumCache;
import io.github.sefiraat.networks.slimefun.network.NetworkQuantumStorage;
import io.github.sefiraat.networks.utils.Keys;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.sefiraat.networks.utils.datatypes.DataTypeMethods;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemHandler;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.DistinctiveItem;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.ncbpfluffybear.fluffymachines.items.Barrel;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import net.guizhanss.minecraft.guizhanlib.gugu.minecraft.helpers.inventory.ItemStackHelper;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemMover extends SpecialSlimefunItem implements DistinctiveItem {
   @NotNull
   public static final Interaction[] CHECK_INTERACTIONS = new Interaction[]{Interaction.PLACE_BLOCK, Interaction.BREAK_BLOCK, Interaction.INTERACT_BLOCK};
   @NotNull
   public static final List<String> DEFAULT_LORE = (List<String>)(ExpansionItemStacks.ITEM_MOVER.getItemMeta() == null
      ? new ArrayList<>()
      : (
         ExpansionItemStacks.ITEM_MOVER.getItemMeta().hasLore() && ExpansionItemStacks.ITEM_MOVER.getItemMeta().getLore() != null
            ? ExpansionItemStacks.ITEM_MOVER.getItemMeta().getLore()
            : new ArrayList<>()
      ));

   public ItemMover(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe);
      this.addItemHandler(new ItemHandler[]{(ItemUseHandler)e -> {
         try {
            if (this.isDisabled()) {
               return;
            }
         } catch (Exception var6) {
            return;
         }

         Player player = e.getPlayer();
         Optional<Block> optional = e.getClickedBlock();
         if (optional.isPresent()) {
            ItemStack itemStack = e.getItem();
            if (itemStack.getType() != Material.DEBUG_STICK) {
               player.sendMessage(Lang.getString("messages.unsupported-operation.item_mover.invalid_item_mover"));
               return;
            }

            if (itemStack.getAmount() != 1) {
               player.sendMessage(Lang.getString("messages.unsupported-operation.item_mover.invalid_item_mover_amount"));
               return;
            }

            if (!itemStack.hasItemMeta() || itemStack.getItemMeta() == null) {
               player.sendMessage(Lang.getString("messages.unsupported-operation.item_mover.invalid_item_mover_meta"));
               return;
            }

            Location location = optional.get().getLocation();
            if (hasPermission(player, location)) {
               if (!player.isSneaking()) {
                  tryDepositIntoMover(player, itemStack, location);
               } else {
                  tryWithdrawFromMover(player, itemStack, location);
               }
            }
         }

         e.cancel();
      }});
   }

   @Nullable
   public static ItemStack getStoredItemStack(@Nullable PersistentDataHolder holder) {
      return holder == null ? null : DataTypeMethods.getCustom(holder, Keys.ITEM_MOVER_ITEM, DataType.ITEM_STACK);
   }

   @Nullable
   public static ItemStack getStoredItemStack(@Nullable ItemStack mover) {
      if (mover != null && mover.getType() != Material.AIR) {
         ItemMeta itemMeta = mover.getItemMeta();
         return mover.hasItemMeta() && itemMeta != null ? getStoredItemStack(itemMeta) : null;
      } else {
         return null;
      }
   }

   public static long getStoredAmount(@Nullable PersistentDataHolder holder) {
      if (holder == null) {
         return 0L;
      } else {
         Integer amountInt = DataTypeMethods.getCustom(holder, Keys.ITEM_MOVER_AMOUNT, PersistentDataType.INTEGER);
         Long amountLong = DataTypeMethods.getCustom(holder, Keys.ITEM_MOVER_AMOUNT_LONG, PersistentDataType.LONG);
         return amountInt == null ? (amountLong == null ? 0L : amountLong) : (long)amountInt.intValue();
      }
   }

   public static long getStoredAmount(@Nullable ItemStack mover) {
      if (mover != null && mover.getType() != Material.AIR) {
         ItemMeta itemMeta = mover.getItemMeta();
         return mover.hasItemMeta() && itemMeta != null ? getStoredAmount(itemMeta) : 0L;
      } else {
         return 0L;
      }
   }

   public static void setStoredItemStack(@Nullable ItemStack mover, @Nullable ItemStack itemStack) {
      if (mover != null && mover.getType() != Material.AIR) {
         ItemMeta itemMeta = mover.getItemMeta();
         if (mover.hasItemMeta() && itemMeta != null) {
            if (itemStack != null && itemStack.getType() != Material.AIR && itemStack.getAmount() == 1) {
               DataTypeMethods.setCustom(itemMeta, Keys.ITEM_MOVER_ITEM, DataType.ITEM_STACK, itemStack);
               mover.setItemMeta(itemMeta);
            } else {
               clearPDC(mover);
            }
         }
      }
   }

   public static void setStoredAmount(@Nullable ItemStack mover, long amount) {
      if (mover != null && mover.getType() != Material.AIR) {
         ItemMeta itemMeta = mover.getItemMeta();
         if (mover.hasItemMeta() && itemMeta != null) {
            if (amount <= 0L) {
               clearPDC(mover);
            } else {
               if (itemMeta.getPersistentDataContainer().getKeys().contains(Keys.ITEM_MOVER_AMOUNT)) {
                  DataTypeMethods.removeCustom(itemMeta, Keys.ITEM_MOVER_AMOUNT);
               }

               DataTypeMethods.setCustom(itemMeta, Keys.ITEM_MOVER_AMOUNT_LONG, PersistentDataType.LONG, amount);
               mover.setItemMeta(itemMeta);
            }
         }
      }
   }

   public static void depositItem(ItemStack mover, @Nullable ItemStack incoming) {
      if (incoming != null && incoming.getType() != Material.AIR) {
         if (incoming.getAmount() > 0) {
            ItemStack stored = getStoredItemStack(mover);
            long storedAmount = getStoredAmount(mover);
            if (stored == null || stored.getType() == Material.AIR) {
               setStoredItemStack(mover, StackUtils.getAsQuantity(incoming, 1));
               setStoredAmount(mover, (long)incoming.getAmount());
               incoming.setAmount(0);
            } else if (StackUtils.itemsMatch(stored, incoming)) {
               long maxCanReceive = Long.MAX_VALUE - storedAmount;
               int incomingAmount = incoming.getAmount();
               long canReceive = Math.min(maxCanReceive, (long)incomingAmount);
               setStoredAmount(mover, getStoredAmount(mover) + canReceive);
               incoming.setAmount(Math.max(0, (int)((long)incomingAmount - canReceive)));
            }
         }
      }
   }

   private static void clearPDC(@Nullable ItemStack mover) {
      if (mover != null && mover.getType() != Material.AIR) {
         ItemMeta itemMeta = mover.getItemMeta();
         if (mover.hasItemMeta() && itemMeta != null) {
            DataTypeMethods.removeCustom(itemMeta, Keys.ITEM_MOVER_ITEM);
            DataTypeMethods.removeCustom(itemMeta, Keys.ITEM_MOVER_AMOUNT);
            DataTypeMethods.removeCustom(itemMeta, Keys.ITEM_MOVER_AMOUNT_LONG);
            mover.setItemMeta(itemMeta);
         }
      }
   }

   public static void updateLore(@Nullable ItemStack itemStack) {
      if (itemStack != null && itemStack.getType() != Material.AIR) {
         if (itemStack.hasItemMeta() && itemStack.getItemMeta() != null) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            ItemStack storedItemStack = getStoredItemStack(itemMeta);
            long amount = getStoredAmount(itemMeta);
            List<String> lore = cloneDefaultLore();
            if (storedItemStack != null && amount > 0L) {
               lore.add(String.format(Lang.getString("messages.normal-operation.item_mover.stored_item"), ItemStackHelper.getDisplayName(storedItemStack)));
               lore.add(String.format(Lang.getString("messages.normal-operation.item_mover.stored_amount"), amount));
            } else {
               clearPDC(itemStack);
            }

            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);
         }
      }
   }

   @Nullable
   public static BarrelIdentity getBarrel(@NotNull Player player, @NotNull Location location) {
      SlimefunItem sfitem = StorageCacheUtils.getSfItem(location);
      if (sfitem == null) {
         return null;
      } else {
         boolean infinityEnabled = SupportedPluginManager.getInstance().isInfinityExpansion();
         boolean tsingshanEnabled = SupportedPluginManager.getInstance().isTsingshanTechnology();
         boolean fluffyEnabled = SupportedPluginManager.getInstance().isFluffyMachines();
         if (tsingshanEnabled && sfitem instanceof StorageUnit unit) {
            return getTsingshanBarrel(location, unit);
         } else if (fluffyEnabled && sfitem instanceof Barrel barrel) {
            return getFluffyBarrel(location, barrel);
         } else if (sfitem instanceof NetworkQuantumStorage) {
            return getNetworkStorage(location);
         } else if (sfitem instanceof NetworksDrawer) {
            player.sendMessage(Lang.getString("messages.unsupported-operation.item_mover.suggest_use_drawers"));
            return null;
         } else {
            return null;
         }
      }
   }

   @Nullable
   public static InfinityBarrel getInfinityBarrel(@NotNull Location location, @NotNull io.github.mooy1.infinityexpansion.items.storage.StorageUnit unit) {
      StorageCache cache = unit.getCache(location);
      if (cache == null) {
         return null;
      } else {
         BlockMenu blockMenu = StorageCacheUtils.getMenu(location);
         if (blockMenu == null) {
            return null;
         } else {
            SlimefunBlockData data = StorageCacheUtils.getBlock(blockMenu.getLocation());
            if (data == null) {
               return null;
            } else {
               String storedString = data.getData("stored");
               if (storedString != null && !storedString.isEmpty()) {
                  int stored;
                  try {
                     stored = Integer.parseInt(storedString);
                  } catch (NumberFormatException var8) {
                     return null;
                  }

                  return stored <= 0 ? null : new InfinityBarrel(location, InfinityBarrel.getActualItemStack(blockMenu), (long)stored, cache);
               } else {
                  return null;
               }
            }
         }
      }
   }

   @Nullable
   public static TsingshanBarrel getTsingshanBarrel(@NotNull Location location, @NotNull StorageUnit unit) {
      io.gitee.crankygoudan.tsingshantechnology.items.chucun.StorageCache cache = unit.getCache(location);
      if (cache == null) {
         return null;
      } else {
         BlockMenu blockMenu = StorageCacheUtils.getMenu(location);
         if (blockMenu == null) {
            return null;
         } else {
            SlimefunBlockData data = StorageCacheUtils.getBlock(blockMenu.getLocation());
            if (data == null) {
               return null;
            } else {
               String storedString = data.getData("sstored");
               if (storedString != null && !storedString.isEmpty()) {
                  int stored;
                  try {
                     stored = Integer.parseInt(storedString);
                  } catch (NumberFormatException var8) {
                     return null;
                  }

                  return stored <= 0 ? null : new TsingshanBarrel(location, TsingshanBarrel.getActualItemStack(blockMenu), (long)stored, cache);
               } else {
                  return null;
               }
            }
         }
      }
   }

   @Nullable
   private static FluffyBarrel getFluffyBarrel(@NotNull Location location, @NotNull Barrel barrel) {
      Block block = location.getBlock();

      ItemStack itemStack;
      try {
         itemStack = barrel.getStoredItem(block);
      } catch (NullPointerException var8) {
         return null;
      }

      if (itemStack != null && itemStack.getType() != Material.AIR) {
         ItemStack clone = itemStack.clone();
         int stored = barrel.getStored(block);
         if (stored <= 0) {
            return null;
         } else {
            int limit = barrel.getCapacity(block);
            boolean voidExcess = Boolean.parseBoolean(StorageCacheUtils.getData(location, "trash"));
            return new FluffyBarrel(location, clone, stored, limit, voidExcess);
         }
      } else {
         return null;
      }
   }

   @Nullable
   public static NetworkStorage getNetworkStorage(@NotNull Location location) {
      QuantumCache cache = NetworkQuantumStorage.getCaches().get(location);
      if (cache == null) {
         return null;
      } else {
         ItemStack stored = cache.getItemStack();
         if (stored != null && stored.getType() != Material.AIR) {
            long storedAmount = cache.getAmountLong();
            return storedAmount < 0L ? null : new NetworkStorage(location, stored, storedAmount, cache.getLimitLong());
         } else {
            return null;
         }
      }
   }

   private static void tryDepositIntoMover(@NotNull Player player, ItemStack mover, @NotNull Location clickedLocation) {
      if (hasPermission(player, clickedLocation)) {
         long storedAmount = getStoredAmount(mover);
         ItemStack storedItemStack = getStoredItemStack(mover);
         BarrelIdentity barrel = getBarrel(player, clickedLocation);
         if (barrel != null && barrel.getItemStack() != null && barrel.getAmount() > 0L) {
            int have = barrel.getAmount() > 2147483647L ? Integer.MAX_VALUE : (int)barrel.getAmount();
            if (have <= 0) {
               player.sendMessage(Lang.getString("messages.unsupported-operation.item_mover.empty_storage"));
            } else {
               ItemRequest itemRequest = new ItemRequest(barrel.getItemStack(), 0);
               if (storedItemStack == null && storedAmount <= 0L) {
                  itemRequest.setAmount(have);
               } else if (StackUtils.itemsMatch(storedItemStack, barrel.getItemStack())) {
                  long maxCanReceive = Long.MAX_VALUE - storedAmount;
                  itemRequest.setAmount((int)Math.min(maxCanReceive, (long)have));
               }

               ItemStack fetched = barrel.requestItem(itemRequest);
               if (fetched != null && fetched.getType() != Material.AIR) {
                  int before = fetched.getAmount();
                  String name = ItemStackHelper.getDisplayName(fetched);
                  depositItem(mover, fetched);
                  int after = fetched.getAmount();
                  player.sendMessage(String.format(Lang.getString("messages.completed-operation.item_mover.deposit_success"), name, before - after));
                  updateLore(mover);
               } else {
                  player.sendMessage(Lang.getString("messages.unsupported-operation.item_mover.invalid_item"));
               }
            }
         } else {
            player.sendMessage(Lang.getString("messages.unsupported-operation.item_mover.invalid_storage"));
         }
      }
   }

   private static void tryWithdrawFromMover(@NotNull Player player, ItemStack mover, @NotNull Location clickedLocation) {
      if (hasPermission(player, clickedLocation)) {
         long storedAmountLong = getStoredAmount(mover);
         int storedAmount = storedAmountLong > 2147483647L ? Integer.MAX_VALUE : (int)storedAmountLong;
         ItemStack storedItemStack = getStoredItemStack(mover);
         BarrelIdentity barrel = getBarrel(player, clickedLocation);
         if (barrel == null) {
            player.sendMessage(Lang.getString("messages.unsupported-operation.item_mover.invalid_storage"));
         } else {
            int have = barrel.getAmount() > 2147483647L ? Integer.MAX_VALUE : (int)barrel.getAmount();
            if (storedItemStack != null && storedAmount > 0) {
               if ((long)have >= barrel.getLimit()) {
                  player.sendMessage(Lang.getString("messages.unsupported-operation.item_mover.full_storage"));
               } else {
                  ItemStack clone = StackUtils.getAsQuantity(storedItemStack, storedAmount);
                  String name = ItemStackHelper.getDisplayName(clone);
                  int before = clone.getAmount();
                  barrel.depositItemStack(clone);
                  int after = clone.getAmount();
                  setStoredAmount(mover, storedAmountLong - (long)(before - after));
                  player.sendMessage(String.format(Lang.getString("messages.completed-operation.item_mover.withdraw_success"), name, before - after));
                  updateLore(mover);
               }
            } else {
               player.sendMessage(Lang.getString("messages.unsupported-operation.item_mover.empty_mover"));
            }
         }
      }
   }

   @NotNull
   private static List<String> cloneDefaultLore() {
      return new ArrayList<>(DEFAULT_LORE);
   }

   public static boolean hasPermission(@NotNull Player player, @NotNull Location location) {
      for (Interaction interaction : CHECK_INTERACTIONS) {
         if (!Slimefun.getProtectionManager().hasPermission(player, location, interaction)) {
            return false;
         }
      }

      SlimefunItem sfitem = StorageCacheUtils.getSfItem(location);
      return Slimefun.getPermissionsService().hasPermission(player, sfitem);
   }

   public boolean canStack(@NotNull ItemMeta itemMeta, @NotNull ItemMeta itemMeta1) {
      return itemMeta.getPersistentDataContainer().equals(itemMeta1.getPersistentDataContainer());
   }
}
