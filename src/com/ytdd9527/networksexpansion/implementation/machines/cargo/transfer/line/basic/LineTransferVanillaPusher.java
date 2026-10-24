package com.ytdd9527.networksexpansion.implementation.machines.cargo.transfer.line.basic;

import com.balugaq.netex.api.enums.TransferType;
import com.balugaq.netex.api.interfaces.PushTickOnly;
import com.balugaq.netex.api.interfaces.SoftCellBannable;
import com.balugaq.netex.api.interfaces.VanillaTransfer;
import com.ytdd9527.networksexpansion.core.items.machines.AbstractTransfer;
import io.github.sefiraat.networks.network.NodeType;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import org.bukkit.Color;
import org.bukkit.Particle.DustOptions;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class LineTransferVanillaPusher extends AbstractTransfer implements SoftCellBannable, PushTickOnly, VanillaTransfer {
   public LineTransferVanillaPusher(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe, NodeType.LINE_TRANSFER_VANILLA_PUSHER);
   }

   @Override
   public TransferType getTransferType() {
      return TransferType.LINE_TRANSFER_VANILLA_PUSHER;
   }

   @NotNull
   @Override
   protected DustOptions getDustOptions() {
      return new DustOptions(Color.MAROON, 1.0F);
   }

   @Override
   public boolean runSync() {
      return true;
   }
}
