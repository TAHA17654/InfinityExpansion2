package com.ytdd9527.networksexpansion.implementation.machines.networks.advanced;

import com.balugaq.netex.api.algorithm.Calculator;
import com.balugaq.netex.api.enums.FeedbackType;
import com.balugaq.netex.api.interfaces.SoftCellBannable;
import com.balugaq.netex.utils.Lang;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.core.items.machines.AdvancedDirectional;
import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.NodeDefinition;
import io.github.sefiraat.networks.network.NodeType;
import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.utils.ChatUtils;
import java.util.ArrayList;
import java.util.List;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class AdvancedWirelessTransmitter extends AdvancedDirectional implements SoftCellBannable {
   private static final String BS_TARGET_LOCATION = "target";
   private static final int[] TEMPLATE_SLOTS = new int[]{0, 1, 2, 3};

   public AdvancedWirelessTransmitter(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe, NodeType.ADVANCED_WIRELESS_TRANSMITTER);

      for (int slot : TEMPLATE_SLOTS) {
         this.getSlotsToDrop().add(slot);
      }
   }

   @NotNull
   @Override
   protected int[] getBackgroundSlots() {
      return new int[]{4, 5};
   }

   @Override
   public boolean isExceedLimit(int quantity) {
      return quantity > 3456;
   }

   @Override
   public int getMaxLimit() {
      return 3456;
   }

   @Range(
      from = -1L,
      to = 53L
   )
   @Override
   protected int getMinusSlot() {
      return 6;
   }

   @Range(
      from = -1L,
      to = 53L
   )
   @Override
   protected int getCargoNumberSlot() {
      return 7;
   }

   @Range(
      from = -1L,
      to = 53L
   )
   @Override
   protected int getAddSlot() {
      return 8;
   }

   @Range(
      from = -1L,
      to = 53L
   )
   @Override
   protected int getTransportModeSlot() {
      return -1;
   }

   public void setTargetLocation(Location machine, Player player, Location target) {
      if (machine.getWorld() != target.getWorld()) {
         player.sendMessage(Lang.getString("messages.commands.worldedit.must-select-same-world"));
      } else if (!Slimefun.getProtectionManager().hasPermission(player, target, Interaction.INTERACT_BLOCK)) {
         player.sendMessage(Lang.getString("messages.feedback.no_permission"));
      } else {
         StorageCacheUtils.setData(machine, "target", string(target));
      }
   }

   public static String string(Location target) {
      return target.getBlockX() + ";" + target.getBlockY() + ";" + target.getBlockZ();
   }

   @Nullable
   public Location getTargetLocation(Location machine) {
      String s = StorageCacheUtils.getData(machine, "target");
      if (s == null) {
         return null;
      } else {
         String[] split = s.split(";");
         return new Location(machine.getWorld(), (double)Integer.parseInt(split[0]), (double)Integer.parseInt(split[1]), (double)Integer.parseInt(split[2]));
      }
   }

   @Override
   public void postRegister() {
      new BlockMenuPreset(this.getId(), this.getItemName()) {
         public void init() {
            this.drawBackground(AdvancedWirelessTransmitter.this.getBackgroundSlots());
            this.addItem(AdvancedWirelessTransmitter.this.getAddSlot(), AdvancedWirelessTransmitter.this.getAddIcon(), (p, i, itemStack, clickAction) -> false);
            this.addItem(
               AdvancedWirelessTransmitter.this.getMinusSlot(), AdvancedWirelessTransmitter.this.getMinusIcon(), (p, i, itemStack, clickAction) -> false
            );
            this.addItem(
               AdvancedWirelessTransmitter.this.getCargoNumberSlot(),
               AdvancedWirelessTransmitter.this.getCargoNumberIcon(),
               (p, i, itemStack, clickAction) -> false
            );
         }

         public void newInstance(@NotNull BlockMenu blockMenu, @NotNull Block b) {
            Location location = blockMenu.getLocation();
            String rawLimit = StorageCacheUtils.getData(location, "transport_limit");
            int limit = rawLimit == null ? AdvancedWirelessTransmitter.this.getMaxLimit() : Integer.parseInt(rawLimit);
            AdvancedWirelessTransmitter.NETWORK_LIMIT_QUANTITY_MAP.put(location.clone(), limit);
            if (AdvancedWirelessTransmitter.this.getCargoNumberSlot() != -1) {
               blockMenu.addMenuClickHandler(
                  AdvancedWirelessTransmitter.this.getCargoNumberSlot(),
                  (player, i, itemStack, clickAction) -> {
                     player.sendMessage(Lang.getString("messages.guide.quantity-input-prompt"));
                     ChatUtils.awaitInput(
                        player,
                        input -> {
                           try {
                              int value = Calculator.calculate(input).intValue();
                              if (value <= 0 || value > AdvancedWirelessTransmitter.this.getMaxLimit()) {
                                 player.sendMessage(
                                    String.format(Lang.getString("messages.guide.invalid-amount-input"), AdvancedWirelessTransmitter.this.getMaxLimit())
                                 );
                                 BlockMenu menu = StorageCacheUtils.getMenu(location);
                                 if (menu != null) {
                                    menu.open(new Player[]{player});
                                 }

                                 return;
                              }

                              AdvancedWirelessTransmitter.this.setLimitQuantity(location, value);
                              AdvancedWirelessTransmitter.this.updateShowIcon(location);
                              BlockMenu menu = StorageCacheUtils.getMenu(location);
                              if (menu != null) {
                                 menu.open(new Player[]{player});
                              }
                           } catch (NumberFormatException var6) {
                              player.sendMessage(var6.getMessage());
                           }
                        }
                     );
                     return false;
                  }
               );
            }

            blockMenu.addMenuClickHandler(
               AdvancedWirelessTransmitter.this.getAddSlot(), (p, slot, item, action) -> AdvancedWirelessTransmitter.this.addClick(location, action)
            );
            blockMenu.addMenuClickHandler(
               AdvancedWirelessTransmitter.this.getMinusSlot(), (p, slot, item, action) -> AdvancedWirelessTransmitter.this.minusClick(location, action)
            );
            AdvancedWirelessTransmitter.this.updateShowIcon(location);
            AdvancedWirelessTransmitter.this.updateTransportModeIcon(location);
         }

         public boolean canOpen(@NotNull Block block, @NotNull Player player) {
            return player.hasPermission("slimefun.inventory.bypass")
               || this.getSlimefunItem().canUse(player, false)
                  && Slimefun.getProtectionManager().hasPermission(player, block.getLocation(), Interaction.INTERACT_BLOCK);
         }

         public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
            return AdvancedWirelessTransmitter.TEMPLATE_SLOTS;
         }
      };
   }

   @Override
   protected void onTick(@Nullable BlockMenu menu, Block block) {
      this.sendFeedback(block.getLocation(), FeedbackType.TICKING);
      this.addToRegistry(block);
      Location location = block.getLocation();
      if (menu == null) {
         this.sendFeedback(location, FeedbackType.NO_MENU);
      } else {
         Location target = this.getTargetLocation(location);
         if (target == null) {
            this.sendFeedback(location, FeedbackType.NO_TARGET_LOCATION);
         } else {
            NodeDefinition srcn = NetworkStorage.getNode(location);
            if (srcn != null && srcn.getNode() != null) {
               NodeDefinition tgtn = NetworkStorage.getNode(target);
               if (tgtn != null && tgtn.getNode() != null) {
                  NetworkRoot src = srcn.getNode().getRoot();
                  NetworkRoot tgt = tgtn.getNode().getRoot();
                  if (src == tgt) {
                     this.sendFeedback(location, FeedbackType.SAME_NETWORK);
                  } else if (!this.checkSoftCellBan(location, src)) {
                     List<ItemStack> templates = new ArrayList<>();

                     for (int slot : TEMPLATE_SLOTS) {
                        ItemStack template = menu.getItemInSlot(slot);
                        if (template != null && template.getType() != Material.AIR) {
                           templates.add(template);
                        }
                     }

                     if (templates.isEmpty()) {
                        this.sendFeedback(location, FeedbackType.NO_TEMPLATE_FOUND);
                     } else {
                        for (ItemStack template : templates) {
                           ItemStack itemStack = src.getItemStack(location, new ItemRequest(template, this.getLimitQuantity(location)));
                           if (itemStack != null && itemStack.getAmount() > 0) {
                              tgt.addItemStack(target, itemStack);
                              src.addItemStack(location, itemStack);
                           }
                        }

                        this.sendFeedback(location, FeedbackType.WORKING);
                     }
                  }
               } else {
                  this.sendFeedback(location, FeedbackType.NO_TARGET_NETWORK_FOUND);
               }
            } else {
               this.sendFeedback(location, FeedbackType.NO_NETWORK_FOUND);
            }
         }
      }
   }
}
