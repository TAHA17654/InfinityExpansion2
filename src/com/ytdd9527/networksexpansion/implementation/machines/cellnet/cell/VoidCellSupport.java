package com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell;

import com.balugaq.netex.utils.Debug;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.ledger.CellPersistence;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage.util.SerializeUtils;
import io.github.sefiraat.networks.utils.Keys;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.libraries.dough.data.persistent.PersistentDataAPI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class VoidCellSupport {
   public static final int MAX_FILTER_SLOTS = 9;
   private static final String FILTER_SEPARATOR = "|";
   private static final Pattern FILTER_SEPARATOR_PATTERN = Pattern.compile(Pattern.quote("|"));
   private static final ThreadLocal<Integer> SUSPEND_DEPTH = ThreadLocal.withInitial(() -> 0);

   private VoidCellSupport() {
   }

   public static boolean isVoidCell(@Nullable ItemStack itemStack) {
      return itemStack != null && !itemStack.getType().isAir() ? SlimefunItem.getByItem(itemStack) instanceof VoidCell : false;
   }

   public static void ensureInitialized(@NotNull ItemStack itemStack) {
      if (CellPersistence.getPerTypeLimit(itemStack) <= 0L) {
         CellPersistence.initializeCell(itemStack, Long.MAX_VALUE);
      }

      CellPersistence.getOrCreateCellUUID(itemStack);
      initializeVoidCell(itemStack);
   }

   public static void initializeVoidCell(@NotNull ItemStack itemStack) {
      ItemMeta meta = itemStack.getItemMeta();
      if (meta != null) {
         if (!PersistentDataAPI.hasByteArray(meta, Keys.VOID_FILTERS)) {
            PersistentDataAPI.setByteArray(meta, Keys.VOID_FILTERS, encodeFilters(List.of()));
         }

         itemStack.setItemMeta(meta);
      }
   }

   @NotNull
   public static List<ItemStack> filtersView(@NotNull ItemStack itemStack) {
      ItemMeta meta = itemStack.getItemMeta();
      if (meta == null) {
         return List.of();
      } else {
         byte[] raw = PersistentDataAPI.getByteArray(meta, Keys.VOID_FILTERS);
         return decodeFilters(raw);
      }
   }

   public static void writeVoidMeta(@NotNull ItemStack itemStack, @NotNull List<ItemStack> filters) {
      ItemMeta meta = itemStack.getItemMeta();
      if (meta != null) {
         List<ItemStack> normalized = normalizeFilters(filters);
         PersistentDataAPI.setByteArray(meta, Keys.VOID_FILTERS, encodeFilters(normalized));
         itemStack.setItemMeta(meta);
      }
   }

   @NotNull
   public static List<ItemStack> normalizeFilters(@NotNull List<ItemStack> filters) {
      List<ItemStack> result = new ArrayList<>(9);

      for (ItemStack filter : filters) {
         if (result.size() >= 9) {
            break;
         }

         if (filter != null && !filter.getType().isAir()) {
            ItemStack copy = filter.clone();
            copy.setAmount(1);
            if (!isDuplicateFilter(result, copy)) {
               result.add(copy);
            }
         }
      }

      return result;
   }

   private static boolean isDuplicateFilter(@NotNull List<ItemStack> result, @NotNull ItemStack candidate) {
      for (ItemStack existing : result) {
         if (StackUtils.itemsMatch(candidate, existing)) {
            return true;
         }
      }

      return false;
   }

   @NotNull
   private static byte[] encodeFilters(@NotNull List<ItemStack> filters) {
      StringBuilder builder = new StringBuilder();

      for (int i = 0; i < filters.size(); i++) {
         if (i > 0) {
            builder.append("|");
         }

         builder.append(SerializeUtils.object2String(filters.get(i)));
      }

      return builder.toString().getBytes(StandardCharsets.UTF_8);
   }

   @NotNull
   private static List<ItemStack> decodeFilters(@Nullable byte[] raw) {
      if (raw != null && raw.length != 0) {
         String encoded = new String(raw, StandardCharsets.UTF_8);
         if (encoded.isEmpty()) {
            return List.of();
         } else {
            List<ItemStack> result = new ArrayList<>(9);

            for (String part : FILTER_SEPARATOR_PATTERN.split(encoded, -1)) {
               if (result.size() >= 9) {
                  break;
               }

               if (!part.isEmpty()) {
                  ItemStack decoded = SerializeUtils.string2Object(part);
                  if (decoded == null) {
                     Debug.debug("Failed to deserialize a Void Cell filter entry, entry skipped");
                  } else {
                     decoded.setAmount(1);
                     result.add(decoded);
                  }
               }
            }

            return result;
         }
      } else {
         return List.of();
      }
   }

   public static boolean matchesAnyFilter(@NotNull List<ItemStack> filters, @Nullable ItemStack template) {
      if (template != null && !template.getType().isAir()) {
         for (ItemStack filter : filters) {
            if (filter.getType() == template.getType() && StackUtils.itemsMatch(template, filter)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static void suspend() {
      SUSPEND_DEPTH.set(SUSPEND_DEPTH.get() + 1);
   }

   public static void resume() {
      int depth = SUSPEND_DEPTH.get();
      if (depth > 0) {
         SUSPEND_DEPTH.set(depth - 1);
      } else {
         Debug.debug("VoidCellSupport.resume has no matching suspend call");
      }
   }

   public static boolean isSuspended() {
      return SUSPEND_DEPTH.get() > 0;
   }
}
