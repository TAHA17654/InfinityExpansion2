package com.ytdd9527.networksexpansion.implementation.machines.networks.advanced;

import com.balugaq.netex.api.interfaces.HangingBlock;
import com.balugaq.netex.utils.Debug;
import com.balugaq.netex.utils.InventoryUtil;
import com.ytdd9527.networksexpansion.implementation.ExpansionItems;
import com.ytdd9527.networksexpansion.utils.TextUtil;
import com.ytdd9527.networksexpansion.utils.databases.DataSource;
import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.NodeDefinition;
import io.github.sefiraat.networks.network.NodeType;
import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import io.github.sefiraat.networks.slimefun.network.NetworkObject;
import io.github.sefiraat.networks.utils.Keys;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.Placeable;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.papermc.paper.event.player.PlayerItemFrameChangeEvent;
import io.papermc.paper.event.player.PlayerItemFrameChangeEvent.ItemFrameChangeAction;
import java.io.IOException;
import javax.annotation.ParametersAreNonnullByDefault;
import net.guizhanss.minecraft.guizhanlib.gugu.minecraft.helpers.inventory.ItemStackHelper;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SwitchingMonitor extends NetworkObject implements HangingBlock, Placeable {
   public static final NamespacedKey ORIGINAL = Keys.newKey("switching-monitor-original");
   public static final String SLASH = TextUtil.GRAY + " - " + TextUtil.GREEN + "x";
   public static final long K = 1000L;
   public static final long M = 1000000L;
   public static final long B = 1000000000L;
   public static final long T = 1000000000000L;
   public static final long Q = 1000000000000000L;
   public static final double FIX_OFFSET = 0.17500000000000004;

   public SwitchingMonitor(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe, NodeType.SWITCHING_MONITOR);
      HangingBlock.registerHangingBlock(this);
   }

   @Contract("null -> null")
   public static ItemStack uniconize(@Nullable ItemStack item) {
      try {
         if (item != null && item.getType() != Material.AIR) {
            ItemStack clone = item.clone();
            ItemMeta meta = clone.getItemMeta();
            if (meta == null) {
               return clone;
            } else {
               PersistentDataContainer pdc = meta.getPersistentDataContainer();
               String base64 = (String)pdc.get(ORIGINAL, PersistentDataType.STRING);
               if (base64 == null) {
                  pdc.remove(ORIGINAL);
                  meta.setDisplayName(removeSuffixAmount(meta.getDisplayName()));
                  clone.setItemMeta(meta);
                  return clone;
               } else {
                  try {
                     return DataSource.getItemStack(base64);
                  } catch (IOException var6) {
                     Debug.trace(var6);
                     pdc.remove(ORIGINAL);
                     clone.setItemMeta(meta);
                     return clone;
                  }
               }
            }
         } else {
            return item;
         }
      } catch (Throwable var7) {
         throw var7;
      }
   }

   @Contract("null, _ -> null")
   public static ItemStack iconize(@Nullable ItemStack template, long amount) {
      try {
         if (template != null && template.getType() != Material.AIR) {
            ItemStack clone = template.clone();
            ItemMeta meta = clone.getItemMeta();
            if (meta == null) {
               return clone;
            } else {
               PersistentDataContainer pdc = meta.getPersistentDataContainer();
               if (pdc.has(ORIGINAL, PersistentDataType.STRING)) {
                  return template;
               } else {
                  pdc.set(ORIGINAL, PersistentDataType.STRING, DataSource.getBase64String(template));
                  meta.setDisplayName(addSuffixAmount(ItemStackHelper.getDisplayName(template), amount));
                  clone.setItemMeta(meta);
                  return clone;
               }
            }
         } else {
            return template;
         }
      } catch (Throwable var6) {
         throw var6;
      }
   }

   @Contract("null, _ -> null; !null, _ -> !null")
   public static String addSuffixAmount(@Nullable String original, long amount) {
      if (original == null) {
         return null;
      } else if (amount < 1000L) {
         return original + SLASH + amount;
      } else if (amount < 1000000L) {
         return original + SLASH + amount / 1000L + "k";
      } else if (amount < 1000000000L) {
         return original + SLASH + amount / 1000000L + "m";
      } else if (amount < 1000000000000L) {
         return original + SLASH + amount / 1000000000L + "b";
      } else {
         return amount < 1000000000000000L ? original + SLASH + amount / 1000000000000L + "t" : original + SLASH + amount;
      }
   }

   @Contract("null -> null; !null -> !null")
   public static String removeSuffixAmount(@Nullable String original) {
      return original == null ? null : original.replaceAll(SLASH + "[0-9]+[kmbt]?", "");
   }

   @Override
   public void onInteract(@NotNull Location attachon, @NotNull PlayerItemFrameChangeEvent event) {
      Player player = event.getPlayer();
      if (!player.isOp() && !Slimefun.getProtectionManager().hasPermission(player, attachon, Interaction.INTERACT_ENTITY)) {
         event.setCancelled(true);
      } else {
         if (event.getAction() == ItemFrameChangeAction.ROTATE) {
            event.setCancelled(true);
            this.handleItemsAction(attachon, player, event);
         } else if (event.getAction() == ItemFrameChangeAction.REMOVE) {
            event.setCancelled(true);
            ItemFrame itemFrame = event.getItemFrame();
            ItemStack template = itemFrame.getItem();
            itemFrame.setItem(null, false);
            Location fixedLocation = itemFrame.getLocation().clone();
            BlockFace attachedFace = itemFrame.getAttachedFace();
            switch (attachedFace.getOppositeFace()) {
               case NORTH:
                  fixedLocation.add(0.0, 0.0, -0.17500000000000004);
                  break;
               case SOUTH:
                  fixedLocation.add(0.0, 0.0, 0.17500000000000004);
                  break;
               case EAST:
                  fixedLocation.add(0.17500000000000004, 0.0, 0.0);
                  break;
               case WEST:
                  fixedLocation.add(-0.17500000000000004, 0.0, 0.0);
                  break;
               case UP:
                  fixedLocation.add(0.0, 0.17500000000000004, 0.0);
                  break;
               case DOWN:
                  fixedLocation.add(0.0, -0.17500000000000004, 0.0);
            }

            fixedLocation.getWorld().dropItemNaturally(fixedLocation, uniconize(template));
         }
      }
   }

   private void handleItemsAction(@NotNull Location attachon, @NotNull Player player, @NotNull PlayerItemFrameChangeEvent event) {
      NodeDefinition definition = NetworkStorage.getNode(attachon);
      if (definition != null && definition.getNode() != null) {
         NetworkRoot root = definition.getNode().getRoot();
         ItemStack template = event.getItemFrame().getItem();
         if (template != null && template.getType() != Material.AIR) {
            template = uniconize(template);
            ItemStack hand = player.getInventory().getItemInMainHand();
            boolean takeItem = hand == null || hand.getType() == Material.AIR;
            boolean shift = player.isSneaking();
            if (takeItem || StackUtils.itemsMatch(hand, template, true, false)) {
               if (takeItem) {
                  int amount = this.calculateSpace(player, template);
                  if (amount == 0) {
                     return;
                  }

                  if (shift) {
                     ItemStack result = root.getItemStack(attachon, new ItemRequest(template, amount));
                     if (result != null) {
                        InventoryUtil.addItem(player, result);
                        if (result.getAmount() > 0) {
                           root.addItemStack(attachon, result);
                        }
                     }
                  } else {
                     ItemStack result = root.getItemStack(attachon, new ItemRequest(template, Math.min(amount, template.getMaxStackSize())));
                     if (result != null) {
                        InventoryUtil.addItem(player, result);
                        if (result.getAmount() > 0) {
                           root.addItemStack(attachon, result);
                        }
                     }
                  }
               } else if (shift) {
                  for (ItemStack item : player.getInventory().getStorageContents()) {
                     if (item != null && item.getType() != Material.AIR && StackUtils.itemsMatch(item, template, true, false)) {
                        int before = item.getAmount();
                        root.addItemStack(attachon, item);
                        int after = item.getAmount();
                        if (before == after) {
                           break;
                        }
                     }
                  }
               } else {
                  root.addItemStack(attachon, hand);
               }
            }
         }
      }
   }

   public int calculateSpace(@NotNull Player player, @NotNull ItemStack template) {
      int amount = 0;

      for (ItemStack item : player.getInventory().getStorageContents()) {
         if (item == null || item.getType() == Material.AIR) {
            amount += template.getMaxStackSize();
         } else if (StackUtils.itemsMatch(item, template, true, false)) {
            amount += Math.max(0, template.getMaxStackSize() - item.getAmount());
         }
      }

      return amount;
   }

   @ParametersAreNonnullByDefault
   @Override
   public void onTick(Location attachon, BlockFace attachSide, ItemFrame entityBlock) {
      NodeDefinition definition = NetworkStorage.getNode(attachon);
      if (definition != null && definition.getNode() != null) {
         NetworkRoot root = definition.getNode().getRoot();
         ItemStack template = entityBlock.getItem();
         if (template != null && template.getType() != Material.AIR) {
            template = uniconize(template);
            long amount = root.getAllNetworkItemsLongTypeView().getOrDefault(template, 0L);
            entityBlock.setItem(iconize(template, amount), false);
         }
      }
   }

   @ParametersAreNonnullByDefault
   @Override
   public <T extends HangingBlock> void onBreak(Location attachon, ItemFrame entityBlock, T hangingBlock) {
      HangingBlock.super.onBreak(attachon, entityBlock, hangingBlock);
      Location location = entityBlock.getLocation();
      location.getWorld().dropItemNaturally(location, uniconize(entityBlock.getItem()));
      location.getWorld().dropItemNaturally(location, ExpansionItems.SWITCHING_MONITOR.getItem());
   }
}
