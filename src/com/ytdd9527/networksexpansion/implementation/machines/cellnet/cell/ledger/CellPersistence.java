package com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.ledger;

import com.google.common.base.Preconditions;
import com.jeff_media.morepersistentdatatypes.DataType;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.StorageCell;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage.CellStorageDatabase;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage.dao.CellDao;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.ItemKey;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.utils.Keys;
import io.github.sefiraat.networks.utils.datatypes.DataTypeMethods;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.libraries.dough.data.persistent.PersistentDataAPI;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;
import me.ddggdd135.guguslimefunlib.GuguSlimefunLib;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CellPersistence {
   private CellPersistence() {
   }

   public static void saveAsync() {
      CellStorageDatabase db = Networks.getCellStorageDatabase();
      if (db != null) {
         db.saveAllAsync();
      }
   }

   public static boolean isStorageCell(@Nullable ItemStack itemStack) {
      return itemStack != null && !itemStack.getType().isAir() ? SlimefunItem.getByItem(itemStack) instanceof StorageCell : false;
   }

   @Nullable
   public static UUID getCellUUID(@Nullable ItemStack itemStack) {
      if (itemStack != null && !itemStack.getType().isAir()) {
         ItemMeta meta = itemStack.getItemMeta();
         if (meta == null) {
            return null;
         } else {
            String uuidStr = PersistentDataAPI.getString(meta, Keys.CELL_UUID);
            if (uuidStr != null && !uuidStr.isEmpty()) {
               try {
                  return UUID.fromString(uuidStr);
               } catch (IllegalArgumentException var4) {
                  return null;
               }
            } else {
               return null;
            }
         }
      } else {
         return null;
      }
   }

   @Nullable
   public static String getCellUUIDString(@Nullable ItemStack itemStack) {
      if (itemStack != null && !itemStack.getType().isAir()) {
         ItemMeta meta = itemStack.getItemMeta();
         if (meta == null) {
            return null;
         } else {
            String uuidStr = PersistentDataAPI.getString(meta, Keys.CELL_UUID);
            return uuidStr != null && !uuidStr.isEmpty() ? uuidStr : null;
         }
      } else {
         return null;
      }
   }

   @NotNull
   public static UUID getOrCreateCellUUID(@NotNull ItemStack itemStack) {
      Preconditions.checkArgument(!itemStack.getType().isAir(), "Cannot assign cell UUID to air");
      UUID existing = getCellUUID(itemStack);
      if (existing != null) {
         return existing;
      } else {
         ItemMeta meta = itemStack.getItemMeta();
         Preconditions.checkNotNull(meta, "ItemMeta cannot be null");
         UUID uuid = UUID.randomUUID();
         PersistentDataAPI.setString(meta, Keys.CELL_UUID, uuid.toString());
         setServerUUID(meta);
         itemStack.setItemMeta(meta);
         return uuid;
      }
   }

   public static long getPerTypeLimit(@NotNull ItemStack itemStack) {
      ItemMeta meta = itemStack.getItemMeta();
      if (meta != null) {
         Long cap = PersistentDataAPI.getLong(meta, Keys.CELL_CAPACITY);
         if (cap != null && cap > 0L) {
            return cap;
         }
      }

      return SlimefunItem.getByItem(itemStack) instanceof StorageCell cell ? cell.getPerTypeLimit() : 0L;
   }

   public static long getCurrentPerTypeLimit(@NotNull ItemStack itemStack) {
      ItemMeta meta = itemStack.getItemMeta();
      if (meta != null) {
         Long current = PersistentDataAPI.getLong(meta, Keys.CELL_CURRENT_CAPACITY);
         if (current != null && current > 0L) {
            return current;
         }
      }

      return 1L;
   }

   public static void setCurrentPerTypeLimit(@NotNull ItemStack itemStack, long current) {
      ItemMeta meta = itemStack.getItemMeta();
      if (meta != null) {
         long per = getPerTypeLimit(itemStack);
         if (!isUnlimited(per)) {
            long maxUnits = getMaxUnitsFor(per);
            long safeCurrent = Math.max(1L, Math.min(current, maxUnits));
            PersistentDataAPI.setLong(meta, Keys.CELL_CURRENT_CAPACITY, safeCurrent);
            itemStack.setItemMeta(meta);
            UUID uuid = getCellUUID(itemStack);
            if (uuid != null) {
               CellLedger cache = CellLedger.getActiveCaches().get(uuid);
               if (cache != null) {
                  cache.setCurrentPerTypeLimit(safeCurrent);
               }
            }
         }
      }
   }

   public static boolean isUnlimited(long perTypeLimit) {
      return CellLedger.isUnlimitedPerType(perTypeLimit);
   }

   public static long getMaxUnitsFor(long perTypeLimit) {
      return isUnlimited(perTypeLimit) ? Long.MAX_VALUE : Math.min(perTypeLimit, CellLedger.getMaxUnitCount());
   }

   @Nullable
   public static String getCustomName(@NotNull UUID uuid) {
      CellLedger cache = CellLedger.getActiveCaches().get(uuid);
      return cache != null ? cache.getCustomName() : null;
   }

   public static void setCustomName(@NotNull ItemStack itemStack, @Nullable String name) {
      ItemMeta meta = itemStack.getItemMeta();
      if (meta != null) {
         if (name != null && !name.isEmpty()) {
            PersistentDataAPI.setString(meta, Keys.CELL_CUSTOM_NAME, name);
         } else {
            PersistentDataAPI.remove(meta, Keys.CELL_CUSTOM_NAME);
         }

         itemStack.setItemMeta(meta);
         UUID uuid = getCellUUID(itemStack);
         if (uuid != null) {
            CellLedger cache = CellLedger.getActiveCaches().get(uuid);
            if (cache != null) {
               cache.setCustomName(name);
            }
         }
      }
   }

   public static void initializeCell(@NotNull ItemStack itemStack, long perTypeLimit) {
      ItemMeta meta = itemStack.getItemMeta();
      if (meta != null) {
         String uuidStr = PersistentDataAPI.getString(meta, Keys.CELL_UUID);
         if (uuidStr == null || uuidStr.isEmpty()) {
            PersistentDataAPI.setString(meta, Keys.CELL_UUID, UUID.randomUUID().toString());
         }

         setServerUUID(meta);
         PersistentDataAPI.setLong(meta, Keys.CELL_CAPACITY, perTypeLimit);
         Long cur = PersistentDataAPI.getLong(meta, Keys.CELL_CURRENT_CAPACITY);
         if (cur == null || cur <= 0L) {
            long initial = isUnlimited(perTypeLimit) ? Long.MAX_VALUE : 1L;
            PersistentDataAPI.setLong(meta, Keys.CELL_CURRENT_CAPACITY, initial);
         }

         itemStack.setItemMeta(meta);
      }
   }

   @NotNull
   public static CellLedger loadCellCache(@NotNull ItemStack itemStack, long perTypeLimit) {
      UUID uuid = getOrCreateCellUUID(itemStack);
      CellLedger cache = CellLedger.getActiveCaches().get(uuid);
      if (cache != null) {
         loadReservedFromItem(cache, itemStack);
         loadVoidExcessFromItem(cache, itemStack);
         return cache;
      } else {
         boolean unlimited = isUnlimited(perTypeLimit);
         long currentPerTypeLimit = unlimited
            ? Long.MAX_VALUE
            : Math.min(getCurrentPerTypeLimit(itemStack), Math.min(perTypeLimit, CellLedger.getMaxUnitCount()));
         return CellLedger.getActiveCaches().computeIfAbsent(uuid, k -> {
            CellLedger created = new CellLedger(uuid, perTypeLimit, currentPerTypeLimit);
            created.setUnlimited(unlimited);
            loadMetaFromItem(created, itemStack);
            restoreStoredItems(created, uuid);
            loadReservedFromItem(created, itemStack);
            loadVoidExcessFromItem(created, itemStack);
            if (hasLegacyVoidExcess(itemStack)) {
               created.migrateLegacyVoidExcess();
            }

            return created;
         });
      }
   }

   public static void saveReservedItems(@NotNull ItemStack itemStack) {
      ItemMeta meta = itemStack.getItemMeta();
      if (meta != null) {
         UUID uuid = getCellUUID(itemStack);
         CellLedger cache = uuid == null ? null : CellLedger.getActiveCaches().get(uuid);
         List<ItemStack> templates = cache == null ? List.of() : cache.getReservedTemplates();
         if (templates.isEmpty()) {
            PersistentDataAPI.remove(meta, Keys.CELL_RESERVED_ITEMS);
         } else {
            DataTypeMethods.setCustom(meta, Keys.CELL_RESERVED_ITEMS, DataType.ITEM_STACK_ARRAY, templates.toArray(new ItemStack[0]));
         }

         itemStack.setItemMeta(meta);
      }
   }

   private static void loadReservedFromItem(@NotNull CellLedger cache, @NotNull ItemStack itemStack) {
      ItemMeta meta = itemStack.getItemMeta();
      if (meta != null) {
         ItemStack[] templates = DataTypeMethods.getCustom(meta, Keys.CELL_RESERVED_ITEMS, DataType.ITEM_STACK_ARRAY);
         if (templates != null) {
            for (ItemStack template : templates) {
               if (template != null && !template.getType().isAir()) {
                  cache.restoreReserved(new ItemKey(template));
               }
            }
         }
      }
   }

   public static void saveVoidExcessItems(@NotNull ItemStack itemStack) {
      ItemMeta meta = itemStack.getItemMeta();
      if (meta != null) {
         UUID uuid = getCellUUID(itemStack);
         CellLedger cache = uuid == null ? null : CellLedger.getActiveCaches().get(uuid);
         List<ItemStack> templates = cache == null ? List.of() : cache.getVoidExcessTemplates();
         if (templates.isEmpty()) {
            PersistentDataAPI.remove(meta, Keys.CELL_VOID_EXCESS_ITEMS);
         } else {
            DataTypeMethods.setCustom(meta, Keys.CELL_VOID_EXCESS_ITEMS, DataType.ITEM_STACK_ARRAY, templates.toArray(new ItemStack[0]));
         }

         itemStack.setItemMeta(meta);
      }
   }

   private static void loadVoidExcessFromItem(@NotNull CellLedger cache, @NotNull ItemStack itemStack) {
      ItemMeta meta = itemStack.getItemMeta();
      if (meta != null) {
         ItemStack[] templates = DataTypeMethods.getCustom(meta, Keys.CELL_VOID_EXCESS_ITEMS, DataType.ITEM_STACK_ARRAY);
         if (templates != null) {
            for (ItemStack template : templates) {
               if (template != null && !template.getType().isAir()) {
                  cache.restoreVoidExcessUnit(new ItemKey(template));
               }
            }
         }
      }
   }

   private static boolean hasLegacyVoidExcess(@NotNull ItemStack itemStack) {
      ItemMeta meta = itemStack.getItemMeta();
      return meta != null && PersistentDataAPI.getByte(meta, Keys.CELL_VOID_EXCESS) == 1;
   }

   private static void loadMetaFromItem(@NotNull CellLedger cache, @NotNull ItemStack itemStack) {
      ItemMeta meta = itemStack.getItemMeta();
      if (meta != null) {
         String customName = PersistentDataAPI.getString(meta, Keys.CELL_CUSTOM_NAME);
         cache.setCustomName(customName);
      }
   }

   private static void restoreStoredItems(@NotNull CellLedger cache, @NotNull UUID uuid) {
      CellStorageDatabase db = Networks.getCellStorageDatabase();
      if (db != null) {
         CellDao.CellData data = db.getStorageController().loadData(uuid);

         for (Entry<ItemStack, Long> entry : data.storage.entrySet()) {
            cache.loadItemSilently(entry.getKey(), entry.getValue());
         }

         cache.loadMetaSilently(data.whitelistEnabled, data.whitelist);
      }
   }

   private static void setServerUUID(@NotNull ItemMeta meta) {
      if (Networks.getSupportedPluginManager().isGuguSlimefunLib()) {
         meta.getPersistentDataContainer().set(Keys.CELL_SERVER, DataType.UUID, GuguSlimefunLib.getServerUUID());
      }
   }

   @Nullable
   public static UUID getServerUUID(@Nullable ItemStack itemStack) {
      return itemStack != null && itemStack.hasItemMeta()
         ? (UUID)itemStack.getItemMeta().getPersistentDataContainer().get(Keys.CELL_SERVER, DataType.UUID)
         : null;
   }

   public static boolean isWrongServer(@NotNull Player player, @NotNull ItemStack itemStack) {
      if (!Networks.getSupportedPluginManager().isGuguSlimefunLib()) {
         return false;
      } else {
         UUID suuid = getServerUUID(itemStack);
         return suuid != null && !player.isOp() && !suuid.equals(GuguSlimefunLib.getServerUUID());
      }
   }
}
