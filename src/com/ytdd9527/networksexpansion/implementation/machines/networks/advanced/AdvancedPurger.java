package com.ytdd9527.networksexpansion.implementation.machines.networks.advanced;

import com.balugaq.netex.api.enums.FeedbackType;
import com.balugaq.netex.api.gui.MatchOptionMenu;
import com.balugaq.netex.api.helpers.Icon;
import com.balugaq.netex.utils.Lang;
import com.balugaq.netex.utils.NetworksVersionedParticle;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.implementation.ExpansionItems;
import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.network.NodeDefinition;
import io.github.sefiraat.networks.network.NodeType;
import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import io.github.sefiraat.networks.slimefun.network.NetworkObject;
import io.github.sefiraat.networks.utils.MatchOption;
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
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class AdvancedPurger extends NetworkObject implements RecipeDisplayItem {
   private static final String KEY_UUID = "display-uuid";
   private static final int MATCH_OPTION_SLOT = 53;
   private static final int[] TEST_ITEM_SLOT = new int[]{
      0,
      1,
      2,
      3,
      4,
      5,
      6,
      7,
      9,
      10,
      11,
      12,
      13,
      14,
      15,
      16,
      18,
      19,
      20,
      21,
      22,
      23,
      24,
      25,
      27,
      28,
      29,
      30,
      31,
      32,
      33,
      34,
      36,
      37,
      38,
      39,
      40,
      41,
      42,
      43,
      45,
      46,
      47,
      48,
      49,
      50,
      51,
      52
   };
   private static final int[] TEST_ITEM_BACKDROP = new int[]{8, 17, 26, 35, 44};
   @NotNull
   private final ItemSetting<Integer> tickRate;
   private boolean useSpecialModel = false;

   public AdvancedPurger(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe, NodeType.ADVANCED_PURGER);
      this.tickRate = new IntRangeSetting(this, "tick_rate", 1, 1, 10);
      this.addItemSetting(new ItemSetting[]{this.tickRate});

      for (int testItemSlot : TEST_ITEM_SLOT) {
         this.getSlotsToDrop().add(testItemSlot);
      }

      this.addItemHandler(new ItemHandler[]{new BlockTicker() {
         private int tick = 1;

         public boolean isSynchronized() {
            return false;
         }

         public void tick(@NotNull Block block, SlimefunItem item, SlimefunBlockData data) {
            if (this.tick <= 1) {
               AdvancedPurger.this.addToRegistry(block);
               BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
               if (blockMenu != null) {
                  AdvancedPurger.this.tryKillItem(blockMenu);
               }
            }
         }

         public void uniqueTick() {
            this.tick = this.tick <= 1 ? (Integer)AdvancedPurger.this.tickRate.getValue() : this.tick - 1;
         }
      }, new BlockBreakHandler(true, true) {
         public void onPlayerBreak(@NotNull BlockBreakEvent e, @NotNull ItemStack item, @NotNull List<ItemStack> drops) {
            BlockMenu blockMenu = StorageCacheUtils.getMenu(e.getBlock().getLocation());
            if (blockMenu != null) {
               blockMenu.dropItems(blockMenu.getLocation(), AdvancedPurger.TEST_ITEM_SLOT);
            }
         }
      }});
   }

   private void tryKillItem(@NotNull BlockMenu blockMenu) {
      NodeDefinition definition = NetworkStorage.getNode(blockMenu.getLocation());
      if (definition != null && definition.getNode() != null) {
         for (int testitemslot : TEST_ITEM_SLOT) {
            ItemStack testItem = blockMenu.getItemInSlot(testitemslot);
            if (testItem == null) {
               this.sendFeedback(blockMenu.getLocation(), FeedbackType.NO_TEMPLATE_FOUND);
               return;
            }

            ItemStack clone = testItem.clone();
            clone.setAmount(1);
            MatchOption matchOption = MatchOptionMenu.getMatchOption(blockMenu.getLocation());
            ItemRequest itemRequest = new ItemRequest(clone, clone.getMaxStackSize());
            ItemStack retrieved = definition.getNode().getRoot().getItemStack(blockMenu.getLocation(), itemRequest, matchOption);
            if (retrieved != null) {
               retrieved.setAmount(0);
               Location location = blockMenu.getLocation().clone().add(0.5, 1.2, 0.5);
               if (definition.getNode().getRoot().isDisplayParticles()) {
                  location.getWorld().spawnParticle(NetworksVersionedParticle.SMOKE, location, 0, 0.0, 0.05, 0.0);
               }
            }
         }

         this.sendFeedback(blockMenu.getLocation(), FeedbackType.WORKING);
      } else {
         this.sendFeedback(blockMenu.getLocation(), FeedbackType.NO_NETWORK_FOUND);
      }
   }

   public void postRegister() {
      new BlockMenuPreset(this.getId(), this.getItemName()) {
         public void init() {
            this.drawBackground(Icon.PURGER_TEMPLATE_BACKGROUND_STACK, AdvancedPurger.TEST_ITEM_BACKDROP);
            this.addItem(53, Icon.MATCH_OPTION_SETTINGS, (p, i, itemStack, clickAction) -> false);
         }

         public void newInstance(@NotNull BlockMenu menu, @NotNull Block b) {
            menu.addMenuClickHandler(53, (player, slot, itemStack, clickAction) -> {
               MatchOptionMenu.openMenu(menu.getLocation(), player);
               return false;
            });
         }

         public boolean canOpen(@NotNull Block block, @NotNull Player player) {
            return player.hasPermission("slimefun.inventory.bypass")
               || ExpansionItems.ADVANCED_PURGER.canUse(player, false)
                  && Slimefun.getProtectionManager().hasPermission(player, block.getLocation(), Interaction.INTERACT_BLOCK);
         }

         public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
            return new int[0];
         }
      };
   }

   @NotNull
   public List<ItemStack> getDisplayRecipes() {
      List<ItemStack> displayRecipes = new ArrayList<>();
      displayRecipes.add(Lang.getMechanism("advanced_purger"));
      return displayRecipes;
   }

   @Generated
   public void setUseSpecialModel(boolean useSpecialModel) {
      this.useSpecialModel = useSpecialModel;
   }
}
