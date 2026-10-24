package com.ytdd9527.networksexpansion.implementation.machines.cellnet.ui;

import com.ytdd9527.networksexpansion.core.items.SpecialSlimefunItem;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MenuShells {
   private MenuShells() {
   }

   public static void create(
      @NotNull SpecialSlimefunItem owner,
      final int size,
      @NotNull final int[] backgroundSlots,
      @Nullable final ItemStack backgroundItem,
      @NotNull final MenuShells.CanOpen canOpen,
      @NotNull final MenuShells.TransportSlots transport,
      @NotNull final MenuShells.OnReady onReady
   ) {
      new BlockMenuPreset(owner.getId(), owner.getItemName()) {
         public void init() {
            this.setSize(size);
            if (backgroundItem != null) {
               for (int slot : backgroundSlots) {
                  this.addItem(slot, backgroundItem.clone());
               }
            } else {
               this.drawBackground(backgroundSlots);
            }
         }

         public boolean canOpen(@NotNull Block block, @NotNull Player player) {
            return canOpen.test(block, player);
         }

         public int[] getSlotsAccessedByItemTransport(@NotNull ItemTransportFlow flow) {
            return transport.get(flow);
         }

         public void newInstance(@NotNull BlockMenu menu, @NotNull Block block) {
            for (int slot : backgroundSlots) {
               menu.addMenuClickHandler(slot, (p, s, i, a) -> false);
            }

            onReady.ready(menu, block);
         }
      };
   }

   public static void create(
      @NotNull SpecialSlimefunItem owner,
      int size,
      @NotNull int[] backgroundSlots,
      @NotNull MenuShells.CanOpen canOpen,
      @NotNull MenuShells.TransportSlots transport,
      @NotNull MenuShells.OnReady onReady
   ) {
      create(owner, size, backgroundSlots, null, canOpen, transport, onReady);
   }

   @NotNull
   public static int[] emptyTransport(@NotNull ItemTransportFlow flow) {
      return new int[0];
   }

   @FunctionalInterface
   public interface CanOpen {
      boolean test(@NotNull Block var1, @NotNull Player var2);
   }

   @FunctionalInterface
   public interface OnReady {
      void ready(@NotNull BlockMenu var1, @NotNull Block var2);
   }

   @FunctionalInterface
   public interface TransportSlots {
      int[] get(@NotNull ItemTransportFlow var1);
   }
}
