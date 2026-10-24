package com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.rule;

import com.balugaq.netex.utils.Debug;
import io.github.sefiraat.networks.utils.Keys;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public final class CellAcceptRules {
   private static final int MAX_ITEM_BYTES = 131072;

   private CellAcceptRules() {
   }

   public static boolean isNbtOversized(@NotNull ItemStack itemStack) {
      if (!itemStack.hasItemMeta()) {
         return false;
      } else {
         try {
            ItemStack unit = itemStack.clone();
            unit.setAmount(1);
            return unit.serializeAsBytes().length > 131072;
         } catch (Exception var2) {
            Debug.debug("Item NBT size check serialization failed, rejected as oversized: " + var2.getMessage());
            return true;
         }
      }
   }

   public static boolean isUsedCell(@NotNull ItemStack itemStack) {
      try {
         ItemMeta meta = itemStack.getItemMeta();
         return meta == null
            ? false
            : meta.getPersistentDataContainer().has(Keys.CELL_UUID, PersistentDataType.STRING)
               || meta.getPersistentDataContainer().has(Keys.VOID_FILTERS, PersistentDataType.BYTE_ARRAY);
      } catch (Exception var2) {
         Debug.debug("Cell usage trace check failed, rejected as used: " + var2.getMessage());
         return true;
      }
   }

   public static boolean isFilledContainer(@NotNull ItemStack itemStack) {
      ItemMeta meta = itemStack.getItemMeta();
      if (meta instanceof BlockStateMeta blockStateMeta && blockStateMeta.getBlockState() instanceof ShulkerBox shulkerBox) {
         return !shulkerBox.getInventory().isEmpty();
      }

      return meta instanceof BundleMeta bundleMeta ? bundleMeta.hasItems() : false;
   }
}
