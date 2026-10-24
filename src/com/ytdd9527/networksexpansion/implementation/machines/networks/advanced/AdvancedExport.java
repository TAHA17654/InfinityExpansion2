package com.ytdd9527.networksexpansion.implementation.machines.networks.advanced;

import com.balugaq.netex.api.enums.FeedbackType;
import com.balugaq.netex.api.helpers.Icon;
import com.balugaq.netex.utils.BlockMenuUtil;
import com.balugaq.netex.utils.Lang;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.implementation.ExpansionItems;
import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.NodeDefinition;
import io.github.sefiraat.networks.network.NodeType;
import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import io.github.sefiraat.networks.slimefun.network.NetworkObject;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemHandler;
import io.github.thebusybiscuit.slimefun4.api.items.ItemSetting;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.items.settings.IntRangeSetting;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.RecipeDisplayItem;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.ArrayList;
import java.util.List;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class AdvancedExport extends NetworkObject implements RecipeDisplayItem {
   private static final int[] BACKGROUND_SLOTS = new int[]{18, 19, 20, 21, 23, 24, 25, 45, 46, 47, 48, 50, 51, 52, 53};
   private static final int[] TEST_ITEM_SLOTS = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17};
   private static final int[] TEST_ITEM_BACKDROP = new int[]{22};
   private static final int[] OUTPUT_ITEM_SLOTS = new int[]{27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44};
   private static final int[] OUTPUT_ITEM_BACKDROP = new int[]{49};
   private final int lockModeSlot = 26;
   @NotNull
   private final ItemSetting<Integer> tickRate = new IntRangeSetting(this, "tick_rate", 1, 1, 10);

   public AdvancedExport(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe, NodeType.ADVANCED_EXPORT);
      this.addItemSetting(new ItemSetting[]{this.tickRate});

      for (int testItemSlot : TEST_ITEM_SLOTS) {
         this.getSlotsToDrop().add(testItemSlot);
      }

      for (int outputItemSlot : OUTPUT_ITEM_SLOTS) {
         this.getSlotsToDrop().add(outputItemSlot);
      }

      this.addItemHandler(new ItemHandler[]{new BlockTicker() {
         private int tick = 1;

         public boolean isSynchronized() {
            return false;
         }

         public void tick(@NotNull Block block, SlimefunItem item, @NotNull SlimefunBlockData data) {
            if (this.tick <= 1) {
               BlockMenu blockMenu = data.getBlockMenu();
               if (blockMenu == null) {
                  return;
               }

               AdvancedExport.this.addToRegistry(block);
               AdvancedExport.this.tryFetchItem(blockMenu);
            }
         }

         public void uniqueTick() {
            this.tick = this.tick <= 1 ? (Integer)AdvancedExport.this.tickRate.getValue() : this.tick - 1;
         }
      }, new BlockBreakHandler(true, true) {
         public void onPlayerBreak(@NotNull BlockBreakEvent e, @NotNull ItemStack item, @NotNull List<ItemStack> drops) {
            BlockMenu blockMenu = StorageCacheUtils.getMenu(e.getBlock().getLocation());
            if (blockMenu != null) {
               for (int testitemslot : AdvancedExport.this.getTestSlots()) {
                  blockMenu.dropItems(blockMenu.getLocation(), new int[]{testitemslot});
               }

               for (int outputitemslot : AdvancedExport.this.getOutputSlots()) {
                  blockMenu.dropItems(blockMenu.getLocation(), new int[]{outputitemslot});
               }
            }
         }
      }});
   }

   private void tryFetchItem(@NotNull BlockMenu blockMenu) {
      NodeDefinition definition = NetworkStorage.getNode(blockMenu.getLocation());
      if (definition != null && definition.getNode() != null) {
         NetworkRoot networkRoot = definition.getNode().getRoot();
         List<ItemRequest> itemRequests = new ArrayList<>();
         int totalFreeStackSpaces = 0;

         for (int outputSlot : this.getOutputSlots()) {
            ItemStack currentStack = blockMenu.getItemInSlot(outputSlot);
            if (currentStack != null && currentStack.getType() != Material.AIR) {
               totalFreeStackSpaces += currentStack.getMaxStackSize() - currentStack.getAmount();
            } else {
               totalFreeStackSpaces += 64;
            }
         }

         if (totalFreeStackSpaces == 0) {
            this.sendFeedback(blockMenu.getLocation(), FeedbackType.NO_ENOUGH_SPACE);
         } else {
            for (int testItemSlot : this.getTestSlots()) {
               ItemStack currentStack = blockMenu.getItemInSlot(testItemSlot);
               if (currentStack != null && currentStack.getType() != Material.AIR) {
                  itemRequests.add(new ItemRequest(StackUtils.getAsQuantity(currentStack, 1), currentStack.getAmount()));
               }
            }

            if (itemRequests.isEmpty()) {
               this.sendFeedback(blockMenu.getLocation(), FeedbackType.NO_ITEM_REQUEST);
            } else {
               for (ItemRequest itemRequest : itemRequests) {
                  ItemStack fetched = networkRoot.getItemStack(blockMenu.getLocation(), itemRequest);
                  if (fetched != null) {
                     this.placeItems(networkRoot, blockMenu, fetched.clone(), fetched.getAmount(), this.getOutputSlots());
                  }
               }

               this.sendFeedback(blockMenu.getLocation(), FeedbackType.WORKING);
            }
         }
      } else {
         this.sendFeedback(blockMenu.getLocation(), FeedbackType.NO_NETWORK_FOUND);
      }
   }

   private void placeItems(@NotNull NetworkRoot root, @NotNull BlockMenu blockMenu, @NotNull ItemStack itemStack, int itemAmount, int[] outputSlots) {
      BlockMenuUtil.pushItem(blockMenu, itemStack, outputSlots);
      if (itemStack.getAmount() > 0) {
         this.returnItems(root, itemStack.clone(), blockMenu);
      }
   }

   private void returnItems(@NotNull NetworkRoot root, @NotNull ItemStack itemStack, @NotNull BlockMenu blockMenu) {
      root.addItemStack(blockMenu.getLocation(), itemStack);
   }

   public void postRegister() {
      new BlockMenuPreset(this.getId(), this.getItemName()) {
         public void init() {
            for (int slot : AdvancedExport.BACKGROUND_SLOTS) {
               this.addItem(slot, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
            }

            this.drawBackground(AdvancedExport.BACKGROUND_SLOTS);
            this.drawBackground(Icon.EXPORT_TEMPLATE_BACKGROUND_STACK, AdvancedExport.TEST_ITEM_BACKDROP);
            this.drawBackground(Icon.EXPORT_OUTPUT_BACKGROUND_STACK, AdvancedExport.OUTPUT_ITEM_BACKDROP);
            this.addItem(26, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
         }

         public boolean canOpen(@NotNull Block block, @NotNull Player player) {
            return player.hasPermission("slimefun.inventory.bypass")
               || ExpansionItems.ADVANCED_EXPORT.canUse(player, false)
                  && Slimefun.getProtectionManager().hasPermission(player, block.getLocation(), Interaction.INTERACT_BLOCK);
         }

         public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
            return flow == ItemTransportFlow.WITHDRAW ? AdvancedExport.this.getOutputSlots() : new int[0];
         }
      };
   }

   private int[] getTestSlots() {
      return TEST_ITEM_SLOTS;
   }

   private int[] getOutputSlots() {
      return OUTPUT_ITEM_SLOTS;
   }

   @NotNull
   public List<ItemStack> getDisplayRecipes() {
      List<ItemStack> displayRecipes = new ArrayList<>();
      displayRecipes.add(Lang.getMechanism("export"));
      return displayRecipes;
   }
}
