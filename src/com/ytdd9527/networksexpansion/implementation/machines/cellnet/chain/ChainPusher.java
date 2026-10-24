package com.ytdd9527.networksexpansion.implementation.machines.cellnet.chain;

import com.balugaq.netex.api.enums.TransferType;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ChainPusher extends ChainGuiBase {
   public ChainPusher(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe);
   }

   @NotNull
   @Override
   public TransferType getTransferType() {
      return TransferType.CHAIN_PUSHER;
   }

   @NotNull
   @Override
   public AbstractChainMachine.WorkMode getWorkMode() {
      return AbstractChainMachine.WorkMode.PUSH;
   }
}
