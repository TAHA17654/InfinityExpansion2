package com.ytdd9527.networksexpansion.implementation.machines.networks.advanced;

import com.balugaq.netex.api.helpers.Icon;
import com.balugaq.netex.utils.BlockMenuUtil;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.core.items.SpecialSlimefunItem;
import io.github.sefiraat.networks.slimefun.network.AdminDebuggable;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemHandler;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.inventory.DirtyChestMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DueMachine extends SpecialSlimefunItem implements AdminDebuggable {
   private static final int[] DUE_ITEM_SLOTS = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8};
   private static final int[] DUE_BORDERS = new int[0];
   private static final int[] DUE_INPUT_SPLIT = new int[]{9, 10, 11, 12, 13, 14, 15, 16};
   private static final int[] INPUT_OUTPUT_SPLIT = new int[]{27, 28, 29, 30, 31, 32, 33, 34};
   private static final int[] BACKGROUND_BORDERS = new int[]{17, 35};
   private static final int[] INPUT_SLOTS = new int[]{18, 19, 20, 21, 22, 23, 24, 25, 26};
   private static final int[] OUTPUT_SLOTS = new int[]{36, 37, 38, 39, 40, 41, 42, 43, 44};
   private static final int[] INPUT_BORDERS = new int[0];
   private static final int[] OUTPUT_BORDERS = new int[0];

   public DueMachine(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe);
      this.addItemHandler(new ItemHandler[]{new BlockBreakHandler(false, false) {
         @ParametersAreNonnullByDefault
         public void onPlayerBreak(BlockBreakEvent event, ItemStack item, List<ItemStack> drops) {
            Location location = event.getBlock().getLocation();
            BlockMenu blockMenu = StorageCacheUtils.getMenu(location);
            if (blockMenu != null) {
               blockMenu.dropItems(location, DueMachine.DUE_ITEM_SLOTS);
               blockMenu.dropItems(location, DueMachine.INPUT_SLOTS);
               blockMenu.dropItems(location, DueMachine.OUTPUT_SLOTS);
            }

            Slimefun.getDatabaseManager().getBlockDataController().removeBlock(location);
         }
      }});
   }

   private static void onTick(@NotNull Block block) {
      Location location = block.getLocation();
      BlockMenu blockMenu = StorageCacheUtils.getMenu(location);
      if (blockMenu != null) {
         for (int i = 0; i < DUE_ITEM_SLOTS.length; i++) {
            int dueSlot = DUE_ITEM_SLOTS[i];
            int inputSlot = INPUT_SLOTS[i];
            int outputSlot = OUTPUT_SLOTS[i];
            ItemStack dueItem = blockMenu.getItemInSlot(dueSlot);
            if (dueItem != null && dueItem.getType() != Material.AIR) {
               ItemStack inputItem = blockMenu.getItemInSlot(inputSlot);
               if (inputItem != null
                  && inputItem.getType() != Material.AIR
                  && StackUtils.itemsMatch(inputItem, dueItem, false, true)
                  && BlockMenuUtil.fits(blockMenu, dueItem, outputSlot)) {
                  BlockMenuUtil.consumeItem(blockMenu, inputSlot, dueItem.getAmount());
                  BlockMenuUtil.pushItem(blockMenu, dueItem.clone(), outputSlot);
               }
            }
         }
      }
   }

   public void preRegister() {
      this.addItemHandler(new ItemHandler[]{new BlockPlaceHandler(false) {
         public void onPlayerPlace(@NotNull BlockPlaceEvent blockPlaceEvent) {
         }
      }});
      this.addItemHandler(new ItemHandler[]{new BlockBreakHandler(false, false) {
         public void onPlayerBreak(@NotNull BlockBreakEvent blockBreakEvent, @NotNull ItemStack itemStack, @NotNull List<ItemStack> list) {
         }
      }});
      this.addItemHandler(new ItemHandler[]{new BlockTicker() {
         public boolean isSynchronized() {
            return false;
         }

         public void tick(@NotNull Block block, SlimefunItem slimefunItem, SlimefunBlockData data) {
            DueMachine.onTick(block);
         }
      }});
   }

   public void postRegister() {
      new BlockMenuPreset(this.getId(), this.getItemName()) {
         public void init() {
            this.setSize(45);

            for (int slot : DueMachine.DUE_BORDERS) {
               this.addItem(slot, Icon.DUE_BORDER_ICON, ChestMenuUtils.getEmptyClickHandler());
            }

            for (int slot : DueMachine.DUE_INPUT_SPLIT) {
               this.addItem(slot, Icon.DUE_INPUT_SPLIT_ICON, ChestMenuUtils.getEmptyClickHandler());
            }

            for (int slot : DueMachine.INPUT_OUTPUT_SPLIT) {
               this.addItem(slot, Icon.INPUT_OUTPUT_SPLIT_ICON, ChestMenuUtils.getEmptyClickHandler());
            }

            for (int slot : DueMachine.BACKGROUND_BORDERS) {
               this.addItem(slot, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
            }

            for (int slot : DueMachine.INPUT_BORDERS) {
               this.addItem(slot, ChestMenuUtils.getInputSlotTexture(), ChestMenuUtils.getEmptyClickHandler());
            }

            for (int slot : DueMachine.OUTPUT_BORDERS) {
               this.addItem(slot, ChestMenuUtils.getOutputSlotTexture(), ChestMenuUtils.getEmptyClickHandler());
            }
         }

         public boolean canOpen(@NotNull Block block, @NotNull Player player) {
            return player.hasPermission("slimefun.inventory.bypass")
               || Slimefun.getPermissionsService().hasPermission(player, this.getSlimefunItem())
                  && Slimefun.getProtectionManager().hasPermission(player, block, Interaction.INTERACT_BLOCK);
         }

         public int[] getSlotsAccessedByItemTransport(ItemTransportFlow itemTransportFlow) {
            return itemTransportFlow == ItemTransportFlow.WITHDRAW ? DueMachine.OUTPUT_SLOTS : new int[0];
         }

         public int[] getSlotsAccessedByItemTransport(@NotNull DirtyChestMenu menu, ItemTransportFlow itemTransportFlow, @Nullable ItemStack incoming) {
            if (itemTransportFlow != ItemTransportFlow.INSERT) {
               return DueMachine.OUTPUT_SLOTS;
            } else if (incoming != null && incoming.getType() != Material.AIR) {
               List<Integer> matched = new ArrayList<>();

               for (int i = 0; i < DueMachine.DUE_ITEM_SLOTS.length; i++) {
                  int dueSlot = DueMachine.DUE_ITEM_SLOTS[i];
                  int inputSlot = DueMachine.INPUT_SLOTS[i];
                  if (StackUtils.itemsMatch(menu.getItemInSlot(dueSlot), incoming)) {
                     matched.add(inputSlot);
                  }
               }

               if (matched.isEmpty()) {
                  return new int[0];
               } else {
                  int[] slots = new int[matched.size()];

                  for (int ix = 0; ix < matched.size(); ix++) {
                     slots[ix] = matched.get(ix);
                  }

                  return slots;
               }
            } else {
               return new int[0];
            }
         }
      };
   }

   public int[] getItemSlots() {
      return DUE_ITEM_SLOTS;
   }
}
