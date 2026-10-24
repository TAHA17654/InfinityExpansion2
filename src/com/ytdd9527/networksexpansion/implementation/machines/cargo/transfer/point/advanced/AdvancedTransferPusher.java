package com.ytdd9527.networksexpansion.implementation.machines.cargo.transfer.point.advanced;

import com.balugaq.netex.api.enums.TransferType;
import com.balugaq.netex.api.interfaces.PushTickOnly;
import com.balugaq.netex.api.interfaces.SoftCellBannable;
import com.ytdd9527.networksexpansion.core.items.machines.AbstractTransfer;
import io.github.sefiraat.networks.network.NodeType;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public class AdvancedTransferPusher extends AbstractTransfer implements SoftCellBannable, PushTickOnly {
   public AdvancedTransferPusher(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe) {
      this(itemGroup, item, recipeType, recipe, 1);
   }

   public AdvancedTransferPusher(
      @NotNull ItemGroup itemGroup,
      @NotNull SlimefunItemStack item,
      @NotNull RecipeType recipeType,
      ItemStack[] recipe,
      @Range(from = 1L,to = 64L) int outputAmount
   ) {
      super(itemGroup, item, recipeType, recipe, NodeType.TRANSFER_PUSHER);
   }

   @NotNull
   @Override
   public TransferType getTransferType() {
      return TransferType.ADVANCED_TRANSFER_PUSHER;
   }
}
