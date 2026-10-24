package com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell;

import com.balugaq.netex.utils.Lang;
import com.ytdd9527.networksexpansion.core.items.SpecialSlimefunItem;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.ledger.CellLedger;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.ledger.CellPersistence;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.menu.CellLore;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.menu.CellMenu;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemHandler;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.NotPlaceable;
import java.util.UUID;
import lombok.Generated;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StorageCell extends SpecialSlimefunItem implements NotPlaceable {
   private final long perTypeLimit;

   public StorageCell(
      @NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe, long perTypeLimit
   ) {
      super(itemGroup, item, recipeType, recipe);
      this.perTypeLimit = perTypeLimit;
   }

   public void preRegister() {
      this.addItemHandler(new ItemHandler[]{CellUseHandler.create(new CellUseHandler.CellUse() {
         @Override
         public boolean prepare(@NotNull Player player, @NotNull ItemStack cell) {
            if (CellPersistence.isWrongServer(player, cell)) {
               player.sendMessage(Lang.getString("messages.cellnet.cell.wrong_server"));
               return false;
            } else {
               long per = StorageCell.getPerTypeLimit(cell);
               StorageCell.loadCellCache(cell, per);
               StorageCell.applyLore(cell, per, StorageCell.getCurrentPerTypeLimit(cell));
               return true;
            }
         }

         @Override
         public void open(@NotNull Player player, @NotNull ItemStack cell) {
            CellMenu.open(player, cell);
         }
      })});
   }

   public static boolean isStorageCell(@Nullable ItemStack itemStack) {
      return CellPersistence.isStorageCell(itemStack);
   }

   @Nullable
   public static UUID getCellUUID(@Nullable ItemStack itemStack) {
      return CellPersistence.getCellUUID(itemStack);
   }

   @NotNull
   public static UUID getOrCreateCellUUID(@NotNull ItemStack itemStack) {
      return CellPersistence.getOrCreateCellUUID(itemStack);
   }

   public static long getPerTypeLimit(@NotNull ItemStack itemStack) {
      return CellPersistence.getPerTypeLimit(itemStack);
   }

   public static long getCurrentPerTypeLimit(@NotNull ItemStack itemStack) {
      return CellPersistence.getCurrentPerTypeLimit(itemStack);
   }

   public static void applyLore(@NotNull ItemStack itemStack, long perTypeLimit, long currentPerTypeLimit) {
      CellLore.applySpecLore(itemStack, perTypeLimit, currentPerTypeLimit);
   }

   public static void initializeCell(@NotNull ItemStack itemStack, long perTypeLimit) {
      CellPersistence.initializeCell(itemStack, perTypeLimit);
   }

   @NotNull
   public static CellLedger loadCellCache(@NotNull ItemStack itemStack, long perTypeLimit) {
      return CellPersistence.loadCellCache(itemStack, perTypeLimit);
   }

   @Generated
   public long getPerTypeLimit() {
      return this.perTypeLimit;
   }
}
