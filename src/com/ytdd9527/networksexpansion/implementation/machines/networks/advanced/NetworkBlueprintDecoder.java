package com.ytdd9527.networksexpansion.implementation.machines.networks.advanced;

import com.balugaq.netex.api.enums.FeedbackType;
import com.balugaq.netex.api.helpers.Icon;
import com.balugaq.netex.utils.BlockMenuUtil;
import com.balugaq.netex.utils.Lang;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.core.items.unusable.Blueprint;
import com.ytdd9527.networksexpansion.implementation.ExpansionItems;
import io.github.sefiraat.networks.network.NodeType;
import io.github.sefiraat.networks.network.stackcaches.BlueprintInstance;
import io.github.sefiraat.networks.slimefun.network.NetworkObject;
import io.github.sefiraat.networks.utils.Keys;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemHandler;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class NetworkBlueprintDecoder extends NetworkObject {
   private static final int[] BACKGROUND_SLOTS = new int[]{0, 1, 2, 3, 4, 5, 9, 11, 12, 14, 18, 19, 20, 21, 22, 23};
   private static final int[] OUTPUT_SLOTS = new int[]{6, 7, 8, 15, 16, 17, 24, 25, 26};
   private static final int INPUT_SLOT = 10;
   private static final int DECODE_SLOT = 13;

   public NetworkBlueprintDecoder(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe, NodeType.DECODER);
   }

   public static int[] getBackgroundSlots() {
      return BACKGROUND_SLOTS;
   }

   public static int[] getOutputSlots() {
      return OUTPUT_SLOTS;
   }

   public static int getInputSlot() {
      return 10;
   }

   public static int getDecodeSlot() {
      return 13;
   }

   public void preRegister() {
      this.addItemHandler(new ItemHandler[]{new BlockBreakHandler(false, false) {
         public void onPlayerBreak(@NotNull BlockBreakEvent blockBreakEvent, @NotNull ItemStack itemStack, @NotNull List<ItemStack> list) {
            Location location = blockBreakEvent.getBlock().getLocation();
            BlockMenu blockMenu = StorageCacheUtils.getMenu(location);
            if (blockMenu != null) {
               blockMenu.dropItems(location, NetworkBlueprintDecoder.getOutputSlots());
               blockMenu.dropItems(location, new int[]{NetworkBlueprintDecoder.getInputSlot()});
            }
         }
      }});
   }

   public void postRegister() {
      new BlockMenuPreset(this.getId(), this.getItemName()) {
         public void init() {
            this.setSize(27);

            for (int slot : NetworkBlueprintDecoder.BACKGROUND_SLOTS) {
               this.addItem(slot, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
            }

            this.addItem(13, Icon.DECODE_ITEM, ChestMenuUtils.getEmptyClickHandler());
         }

         public void newInstance(@NotNull BlockMenu menu, @NotNull Block b) {
            menu.addMenuClickHandler(13, (player, slot, clickedItem, clickAction) -> {
               NetworkBlueprintDecoder.this.decode(player, menu);
               return false;
            });
         }

         public boolean canOpen(@NotNull Block block, @NotNull Player player) {
            return player.hasPermission("slimefun.inventory.bypass")
               || ExpansionItems.ADVANCED_EXPORT.canUse(player, false)
                  && Slimefun.getProtectionManager().hasPermission(player, block.getLocation(), Interaction.INTERACT_BLOCK);
         }

         public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
            return flow == ItemTransportFlow.INSERT ? new int[]{NetworkBlueprintDecoder.getInputSlot()} : NetworkBlueprintDecoder.getOutputSlots();
         }
      };
   }

   private void decode(@NotNull Player player, @NotNull BlockMenu menu) {
      ItemStack input = menu.getItemInSlot(getInputSlot());
      if (input != null && input.getType() != Material.AIR) {
         SlimefunItem item = SlimefunItem.getByItem(input);
         if (!(item instanceof Blueprint)) {
            player.sendMessage(Lang.getString("messages.unsupported-operation.decoder.not_blueprint"));
            this.sendFeedback(menu.getLocation(), FeedbackType.NOT_BLUEPRINT);
         } else {
            ItemMeta meta = input.getItemMeta();
            if (meta == null) {
               player.sendMessage(Lang.getString("messages.unsupported-operation.decoder.invalid_blueprint"));
               this.sendFeedback(menu.getLocation(), FeedbackType.INVALID_BLUEPRINT);
            } else {
               BlueprintInstance blueprintInstance = Keys.getBlueprintInstance(meta);
               if (blueprintInstance == null) {
                  player.sendMessage(Lang.getString("messages.unsupported-operation.decoder.invalid_blueprint"));
                  this.sendFeedback(menu.getLocation(), FeedbackType.INVALID_BLUEPRINT);
               } else {
                  ItemStack[] inputs = blueprintInstance.getRecipeItems();
                  if (!BlockMenuUtil.fits(menu, inputs, getOutputSlots())) {
                     player.sendMessage(Lang.getString("messages.unsupported-operation.decoder.output_full"));
                     this.sendFeedback(menu.getLocation(), FeedbackType.OUTPUT_FULL);
                  } else {
                     input.setAmount(input.getAmount() - 1);
                     Map<ItemStack, Integer> left = BlockMenuUtil.pushItem(menu, inputs, getOutputSlots());
                     if (left != null && !left.isEmpty()) {
                        for (Entry<ItemStack, Integer> entry : left.entrySet()) {
                           player.sendMessage(Lang.getString("messages.unsupported-operation.decoder.output_full"));
                           this.sendFeedback(menu.getLocation(), FeedbackType.OUTPUT_FULL);
                           menu.getLocation().getWorld().dropItem(menu.getLocation(), entry.getKey());
                        }
                     }

                     player.sendMessage(Lang.getString("messages.completed-operation.decoder.decode_blueprint_success"));
                     this.sendFeedback(menu.getLocation(), FeedbackType.SUCCESS);
                  }
               }
            }
         }
      } else {
         player.sendMessage(Lang.getString("messages.unsupported-operation.decoder.no_input"));
         this.sendFeedback(menu.getLocation(), FeedbackType.NO_INPUT);
      }
   }
}
