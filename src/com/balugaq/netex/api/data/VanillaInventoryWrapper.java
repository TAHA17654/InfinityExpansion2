package com.balugaq.netex.api.data;

import com.balugaq.netex.api.enums.MinecraftVersion;
import com.bgsoftware.wildchests.api.WildChestsAPI;
import io.github.sefiraat.networks.Networks;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import lombok.Generated;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.inventory.DirtyChestMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.BrewingStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class VanillaInventoryWrapper extends BlockMenu {
   private static final int[] EMPTY = new int[0];
   private final BlockState blockState;

   public VanillaInventoryWrapper(Inventory inv, BlockState blockState) {
      super(createPreset(inv, blockState), blockState.getLocation() == null ? inv.getLocation() : blockState.getLocation(), inv);
      this.blockState = blockState;

      for (int slot = 0; slot < inv.getSize(); slot++) {
         this.addItem(slot, inv.getItem(slot));
      }
   }

   private static BlockMenuPreset createPreset(final Inventory inv, final BlockState blockState) {
      return new BlockMenuPreset("VANILLA_INVENTORY_WRAPPER", "VANILLA_INVENTORY_WRAPPER") {
         public void init() {
         }

         public boolean canOpen(Block block, Player player) {
            return false;
         }

         public int[] getSlotsAccessedByItemTransport(ItemTransportFlow itemTransportFlow) {
            return new int[0];
         }

         public int[] getSlotsAccessedByItemTransport(DirtyChestMenu menu, ItemTransportFlow flow, ItemStack item) {
            return VanillaInventoryWrapper.getSlotsAccessedByItemTransport(inv, blockState, flow, item);
         }
      };
   }

   private static int[] getSlotsAccessedByItemTransport(Inventory inv, BlockState blockState, ItemTransportFlow flow, ItemStack itemStack) {
      if (inv instanceof BrewerInventory) {
         if (flow == ItemTransportFlow.WITHDRAW) {
            if (blockState instanceof BrewingStand bs) {
               if (bs.getBrewingTime() > 0) {
                  return EMPTY;
               } else {
                  IntList list = new IntArrayList();

                  for (int i = 0; i < 3; i++) {
                     ItemStack stack = inv.getContents()[i];
                     if (stack != null && stack.getType() != Material.AIR) {
                        ItemMeta bpd = stack.getItemMeta();
                        if (bpd instanceof PotionMeta) {
                           PotionMeta potionMeta = (PotionMeta)bpd;
                           if (Networks.getInstance().getMCVersion().isAtLeast(MinecraftVersion.V1_20_5)) {
                              if (potionMeta.getBasePotionType() != PotionType.WATER) {
                                 list.add(i);
                              }
                           } else {
                              PotionData bpdx = potionMeta.getBasePotionData();
                              if (bpdx != null && bpdx.getType() != PotionType.WATER) {
                                 list.add(i);
                              }
                           }
                        } else {
                           list.add(i);
                        }
                     }
                  }

                  return list.toIntArray();
               }
            } else {
               return EMPTY;
            }
         } else if (itemStack.getType() == Material.BLAZE_POWDER) {
            return new int[]{4, 3};
         } else {
            return itemStack.getType() != Material.POTION && itemStack.getType() != Material.SPLASH_POTION && itemStack.getType() != Material.LINGERING_POTION
               ? new int[]{4}
               : generateArray(3);
         }
      } else if (inv instanceof FurnaceInventory) {
         if (flow == ItemTransportFlow.WITHDRAW) {
            return new int[]{2};
         } else {
            return itemStack.getType().isFuel() ? new int[]{1, 0} : new int[]{0, 1};
         }
      } else if (inv instanceof PlayerInventory) {
         return generateArray(36);
      } else {
         boolean wildChests = Networks.getSupportedPluginManager().isWildChests();
         boolean isChest = false;
         if (inv.getLocation() != null) {
            isChest = wildChests && WildChestsAPI.getChest(inv.getLocation()) != null;
         }

         return wildChests && isChest ? EMPTY : generateArray(inv.getSize());
      }
   }

   private static int[] generateArray(int size) {
      int[] array = new int[size];
      int i = 0;

      while (i < size) {
         array[i] = i++;
      }

      return array;
   }

   public void reset(boolean update) {
   }

   @Generated
   public BlockState getBlockState() {
      return this.blockState;
   }
}
