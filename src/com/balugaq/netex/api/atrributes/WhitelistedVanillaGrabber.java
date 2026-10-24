package com.balugaq.netex.api.atrributes;

import io.github.sefiraat.networks.network.NetworkRoot;
import java.util.List;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.BrewingStand;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface WhitelistedVanillaGrabber extends WhitelistedGrabber {
   default void grabInventory(
      @NotNull BlockMenu blockMenu, @NotNull BlockState blockState, @NotNull Inventory inventory, @NotNull NetworkRoot root, @NotNull List<ItemStack> templates
   ) {
      if (inventory instanceof FurnaceInventory furnaceInventory) {
         ItemStack furnaceInventoryResult = furnaceInventory.getResult();
         ItemStack furnaceInventoryFuel = furnaceInventory.getFuel();
         if (furnaceInventoryResult != null && furnaceInventoryResult.getType() != Material.AIR && this.inTemplates(templates, furnaceInventoryResult)) {
            this.grabItem(root, blockMenu, furnaceInventoryResult);
         } else if (this.inTemplates(templates, furnaceInventoryFuel)) {
            this.grabItem(root, blockMenu, furnaceInventoryFuel);
         }
      } else if (inventory instanceof BrewerInventory brewerInventory) {
         if (!(blockState instanceof BrewingStand brewingStand)) {
            return;
         }

         if (brewingStand.getBrewingTime() > 0) {
            return;
         }

         if (this.inTemplates(templates, brewerInventory.getFuel())) {
            this.grabItem(root, blockMenu, brewerInventory.getFuel());
            return;
         }

         for (int i = 0; i < 3; i++) {
            ItemStack stack = brewerInventory.getContents()[i];
            if (this.inTemplates(templates, stack)) {
               this.grabItem(root, blockMenu, stack);
               break;
            }
         }
      } else {
         for (ItemStack stack : inventory.getContents()) {
            if (this.inTemplates(templates, stack) && this.grabItem(root, blockMenu, stack)) {
               break;
            }
         }
      }
   }

   default boolean grabItem(@NotNull NetworkRoot root, @NotNull BlockMenu blockMenu, @Nullable ItemStack stack) {
      if (stack != null && stack.getType() != Material.AIR) {
         root.addItemStack(blockMenu.getLocation(), stack);
         return true;
      } else {
         return false;
      }
   }
}
