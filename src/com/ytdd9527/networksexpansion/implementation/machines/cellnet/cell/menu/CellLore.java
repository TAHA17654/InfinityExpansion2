package com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.menu;

import com.balugaq.netex.utils.Lang;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.CellTier;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.ledger.CellLedger;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.ledger.CellPersistence;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.NumberFormat;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.ui.CellUI;
import io.github.sefiraat.networks.slimefun.network.NetworkQuantumStorage;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.guizhanss.minecraft.guizhanlib.gugu.minecraft.helpers.inventory.ItemStackHelper;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CellLore {
   private static final String LORE_NOT_CONFIGURED = Lang.getString("messages.cellnet.cell.lore.not_configured");
   private static final String LORE_HINT_OPEN = Lang.getString("messages.cellnet.cell.lore.hint_open");
   private static final String LORE_HINT_DRIVE = Lang.getString("messages.cellnet.cell.lore.hint_drive");

   private CellLore() {
   }

   public static void applySpecLore(@NotNull ItemStack itemStack, long perTypeLimit, long currentPerTypeLimit) {
      UUID cellUuid = CellPersistence.getCellUUID(itemStack);
      if (cellUuid != null) {
         ItemMeta meta = itemStack.getItemMeta();
         if (meta != null) {
            List<String> newLore = new ArrayList<>();
            appendBaseLore(meta, newLore);
            CellLedger cache = CellLedger.getActiveCaches().get(cellUuid);
            int storedTypes = cache == null ? 0 : cache.getStoredItems().size();
            appendNameLine(newLore, cache);
            appendCapacitySection(newLore, perTypeLimit, currentPerTypeLimit, storedTypes);
            appendStoredItems(newLore, cache);
            appendUpgradeLine(newLore, perTypeLimit, currentPerTypeLimit);
            appendHints(newLore);
            meta.setLore(newLore);
            itemStack.setItemMeta(meta);
         }
      }
   }

   private static void appendBaseLore(@NotNull ItemMeta meta, @NotNull List<String> newLore) {
      List<String> baseLore = meta.getLore();
      if (baseLore != null && !baseLore.isEmpty()) {
         newLore.add(baseLore.get(0));
      }
   }

   private static void appendNameLine(@NotNull List<String> newLore, @Nullable CellLedger cache) {
      String customName = cache != null ? cache.getCustomName() : null;
      if (customName != null && !customName.isEmpty()) {
         newLore.add(Lang.getString("messages.cellnet.cell.lore.name_line", customName));
      }
   }

   private static void appendCapacitySection(@NotNull List<String> newLore, long perTypeLimit, long currentPerTypeLimit, int storedTypes) {
      long maxUnits = CellPersistence.getMaxUnitsFor(perTypeLimit);
      boolean unlimited = maxUnits == Long.MAX_VALUE;
      long slots = unlimited ? Long.MAX_VALUE : Math.max(1L, Math.min(currentPerTypeLimit, maxUnits));
      long filled = Math.min(slots, (long)storedTypes);
      newLore.add(Lang.getString("messages.cellnet.cell.lore.capacity_line", unlimited ? "\u221e" : NumberFormat.formatCellShort(perTypeLimit)));
      ChatColor fillColor = CellUI.barColor(filled, slots);
      String bar = CellUI.barBody(CellUI.barFill(filled, slots), fillColor);
      newLore.add(
         Lang.getString(
            "messages.cellnet.cell.lore.units_line",
            fillColor.toString() + NumberFormat.formatCellNumber(filled),
            unlimited ? "\u221e" : NumberFormat.formatCellNumber(slots),
            bar
         )
      );
      long remainingUnits = unlimited ? 0L : Math.max(0L, slots - filled);
      newLore.add(Lang.getString("messages.cellnet.cell.lore.remaining_units", unlimited ? "\u221e" : NumberFormat.formatCellNumber(remainingUnits)));
   }

   private static void appendStoredItems(@NotNull List<String> newLore, @Nullable CellLedger cache) {
      List<CellLedger.CellEntry> storedItems = cache == null ? List.of() : cache.getStoredItems();
      if (!storedItems.isEmpty()) {
         int shown = Math.min(9, storedItems.size());

         for (int i = 0; i < shown; i++) {
            CellLedger.CellEntry entry = storedItems.get(i);
            newLore.add(
               Lang.getString("messages.cellnet.cell.lore.item_entry", ItemStackHelper.getDisplayName(entry.sample), NumberFormat.formatNumber(entry.amount))
            );
         }

         if (storedItems.size() > shown) {
            newLore.add(Lang.getString("messages.cellnet.cell.lore.and_more", storedItems.size() - shown));
         }
      }
   }

   private static void appendUpgradeLine(@NotNull List<String> newLore, long perTypeLimit, long currentPerTypeLimit) {
      long maxUnits = CellPersistence.getMaxUnitsFor(perTypeLimit);
      if (maxUnits != Long.MAX_VALUE) {
         long remaining = Math.max(0L, maxUnits - Math.max(1L, Math.min(currentPerTypeLimit, maxUnits)));
         if (remaining > 0L) {
            NetworkQuantumStorage mat = CellTier.upgradeMaterialOf(perTypeLimit);
            String matName = mat == null ? LORE_NOT_CONFIGURED : ItemStackHelper.getDisplayName(mat.getItem());
            newLore.add(Lang.getString("messages.cellnet.cell.lore.upgrade_line", matName));
         }
      }
   }

   private static void appendHints(@NotNull List<String> newLore) {
      newLore.add(LORE_HINT_OPEN);
      newLore.add(LORE_HINT_DRIVE);
   }
}
