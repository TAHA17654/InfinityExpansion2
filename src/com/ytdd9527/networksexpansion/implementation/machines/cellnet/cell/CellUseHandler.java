package com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell;

import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class CellUseHandler {
   private CellUseHandler() {
   }

   @NotNull
   public static ItemUseHandler create(@NotNull CellUseHandler.CellUse use) {
      return e -> {
         if (!e.getPlayer().isSneaking()) {
            e.cancel();
            ItemStack cell = e.getItem();
            if (use.prepare(e.getPlayer(), cell)) {
               e.getPlayer().getInventory().setItemInMainHand(cell);
               use.open(e.getPlayer(), cell);
            }
         }
      };
   }

   public interface CellUse {
      boolean prepare(@NotNull Player var1, @NotNull ItemStack var2);

      void open(@NotNull Player var1, @NotNull ItemStack var2);
   }
}
