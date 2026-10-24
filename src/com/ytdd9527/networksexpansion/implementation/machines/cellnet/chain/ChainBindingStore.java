package com.ytdd9527.networksexpansion.implementation.machines.cellnet.chain;

import com.balugaq.netex.api.enums.TransportMode;
import com.balugaq.netex.utils.Debug;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.rule.CellAcceptRules;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage.util.SerializeUtils;
import io.github.sefiraat.networks.Networks;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ChainBindingStore {
   public static final int MAX_LIST_SIZE = 9;
   static final String KEY_BIND_PREFIX = "chain_bind_";
   static final String KEY_DIR_MODE_PREFIX = "chain_dir_mode_";
   private static final String ENTRY_SEPARATOR = "|";
   private static final String ITEM_SEPARATOR = ",";
   private static final String DISTANCE_SEPARATOR = ":";

   private ChainBindingStore() {
   }

   @NotNull
   public static String bindKey(@NotNull BlockFace face) {
      return "chain_bind_" + face.name();
   }

   @NotNull
   public static String dirModeKey(@NotNull BlockFace face) {
      return "chain_dir_mode_" + face.name();
   }

   public static int maxDistance() {
      return Networks.getConfigManager().getChainMaxDistance();
   }

   @NotNull
   public static Map<Integer, List<ItemStack>> load(@NotNull Location location, @NotNull BlockFace face) {
      String raw = StorageCacheUtils.getData(location, bindKey(face));
      return decodeBindings(raw);
   }

   @NotNull
   public static Map<Integer, List<ItemStack>> decodeBindings(@Nullable String raw) {
      Map<Integer, List<ItemStack>> result = new LinkedHashMap<>();
      if (raw != null && !raw.isEmpty()) {
         for (String entry : raw.split("\\|")) {
            if (!entry.isEmpty()) {
               try {
                  int sep = entry.indexOf(":");
                  if (sep > 0) {
                     int distance;
                     try {
                        distance = Integer.parseInt(entry.substring(0, sep));
                     } catch (NumberFormatException var15) {
                        continue;
                     }

                     if (distance >= 1) {
                        List<ItemStack> list = new ArrayList<>();
                        String itemsPart = entry.substring(sep + 1);
                        if (!itemsPart.isEmpty()) {
                           for (String base64 : itemsPart.split(",")) {
                              ItemStack item = SerializeUtils.string2Object(base64);
                              if (item != null) {
                                 item.setAmount(1);
                                 list.add(item);
                              }
                           }
                        }

                        result.put(distance, list);
                     }
                  }
               } catch (RuntimeException var16) {
                  Debug.trace(var16, "Failed to parse chain binding table entry, skipped: " + entry);
               }
            }
         }

         return result;
      } else {
         return result;
      }
   }

   public static boolean save(@NotNull Location location, @NotNull BlockFace face, @NotNull Map<Integer, List<ItemStack>> bindings) {
      return saveEncoded(location, bindKey(face), bindings);
   }

   static boolean saveEncoded(@NotNull Location location, @NotNull String key, @NotNull Map<Integer, List<ItemStack>> bindings) {
      String encoded = encodeBindings(bindings);
      if (encoded == null) {
         return false;
      } else {
         if (encoded.isEmpty()) {
            StorageCacheUtils.removeData(location, key);
         } else {
            StorageCacheUtils.setData(location, key, encoded);
         }

         return true;
      }
   }

   @Nullable
   public static String encodeBindings(@NotNull Map<Integer, List<ItemStack>> bindings) {
      if (bindings.isEmpty()) {
         return "";
      } else {
         int cap = maxDistance();
         StringBuilder builder = new StringBuilder();
         boolean firstEntry = true;

         try {
            for (Entry<Integer, List<ItemStack>> entry : bindings.entrySet()) {
               int distance = entry.getKey();
               if (distance >= 1 && distance <= cap) {
                  StringBuilder itemsBuilder = new StringBuilder();
                  boolean firstItem = true;

                  for (ItemStack sample : entry.getValue()) {
                     if (sample != null && !sample.getType().isAir()) {
                        if (CellAcceptRules.isNbtOversized(sample)) {
                           Debug.debug("Chain binding item NBT too large, item skipped: " + sample.getType());
                        } else {
                           ItemStack normalized = sample.clone();
                           normalized.setAmount(1);
                           if (!firstItem) {
                              itemsBuilder.append(",");
                           }

                           firstItem = false;
                           itemsBuilder.append(SerializeUtils.object2String(normalized));
                        }
                     }
                  }

                  if (!firstEntry) {
                     builder.append("|");
                  }

                  firstEntry = false;
                  builder.append(distance).append(":").append((CharSequence)itemsBuilder);
               }
            }
         } catch (RuntimeException var12) {
            Debug.trace(var12, "Failed to serialize chain binding table, save refused");
            return null;
         }

         return firstEntry ? "" : builder.toString();
      }
   }

   public static boolean containsSimilar(@NotNull List<ItemStack> list, @NotNull ItemStack sample) {
      for (ItemStack existing : list) {
         if (existing.isSimilar(sample)) {
            return true;
         }
      }

      return false;
   }

   @Nullable
   public static TransportMode loadDirMode(@NotNull Location location, @NotNull BlockFace face) {
      String raw = StorageCacheUtils.getData(location, dirModeKey(face));
      if (raw != null && !raw.isEmpty()) {
         try {
            return TransportMode.valueOf(raw);
         } catch (IllegalArgumentException var4) {
            Debug.debug("Invalid direction mode override, treated as follow-machine: " + raw);
            return null;
         }
      } else {
         return null;
      }
   }

   public static void saveDirMode(@NotNull Location location, @NotNull BlockFace face, @Nullable TransportMode mode) {
      if (mode == null) {
         StorageCacheUtils.removeData(location, dirModeKey(face));
      } else {
         StorageCacheUtils.setData(location, dirModeKey(face), mode.name());
      }
   }
}
