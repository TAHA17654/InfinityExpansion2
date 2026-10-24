package com.ytdd9527.networksexpansion.implementation.machines.networks.advanced;

import com.balugaq.netex.api.enums.TransportFacing;
import com.balugaq.netex.api.helpers.Icon;
import com.balugaq.netex.utils.BlockMenuUtil;
import com.balugaq.netex.utils.Lang;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.core.items.SpecialSlimefunItem;
import io.github.sefiraat.networks.slimefun.network.AdminDebuggable;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemHandler;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.FaceAttachable;
import org.bukkit.block.data.FaceAttachable.AttachedFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class Offsetter extends SpecialSlimefunItem implements AdminDebuggable {
   private static final Map<Location, TransportFacing> facingMap = new HashMap<>();
   private static final Map<Location, Integer> offsetMap = new HashMap<>();
   private static final String BS_OFFSET = "offset";
   private static final int OFFSET_DECREASE_SLOT = 3;
   private static final int OFFSET_SHOW_SLOT = 4;
   private static final int OFFSET_INCREASE_SLOT = 5;

   public Offsetter(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe);
   }

   private static void onTick(@NotNull Block block) {
      Location location = block.getLocation();
      BlockMenu blockMenu = StorageCacheUtils.getMenu(location);
      if (blockMenu != null) {
         TransportFacing facing = facingMap.get(location);
         if (facing != null) {
            BlockFace from;
            BlockFace to;
            switch (facing) {
               case UP_TO_DOWN:
                  from = BlockFace.UP;
                  to = BlockFace.DOWN;
                  break;
               case DOWN_TO_UP:
                  from = BlockFace.DOWN;
                  to = BlockFace.UP;
                  break;
               case EAST_TO_WEST:
                  from = BlockFace.EAST;
                  to = BlockFace.WEST;
                  break;
               case WEST_TO_EAST:
                  from = BlockFace.WEST;
                  to = BlockFace.EAST;
                  break;
               case SOUTH_TO_NORTH:
                  from = BlockFace.SOUTH;
                  to = BlockFace.NORTH;
                  break;
               case NORTH_TO_SOUTH:
                  from = BlockFace.NORTH;
                  to = BlockFace.SOUTH;
                  break;
               default:
                  from = null;
                  to = null;
            }

            if (from != null && to != null) {
               Block fromBlock = block.getRelative(from);
               Block toBlock = block.getRelative(to);
               Location fromLocation = fromBlock.getLocation();
               Location toLocation = toBlock.getLocation();
               BlockMenu fromMenu = StorageCacheUtils.getMenu(fromLocation);
               BlockMenu toMenu = StorageCacheUtils.getMenu(toLocation);
               if (fromMenu != null && toMenu != null) {
                  int[] fromSlot = fromMenu.getPreset().getSlotsAccessedByItemTransport(fromMenu, ItemTransportFlow.WITHDRAW, null);

                  for (int i = 0; i < fromSlot.length; i++) {
                     ItemStack fromItem = fromMenu.getItemInSlot(fromSlot[i]);
                     if (fromItem != null && fromItem.getType() != Material.AIR) {
                        int[] toSlot = toMenu.getPreset().getSlotsAccessedByItemTransport(toMenu, ItemTransportFlow.INSERT, fromItem);
                        int offset = getOffset(location);
                        int offseti = i + offset;
                        if (offseti < toSlot.length && offseti >= 0 && BlockMenuUtil.fits(toMenu, fromItem, toSlot[offseti])) {
                           BlockMenuUtil.pushItem(toMenu, fromItem, toSlot[offseti]);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static void increaseOffset(@NotNull Location location, int amount) {
      int offset = getOffset(location);
      if (offset + amount <= 53 && offset + amount >= -53) {
         setOffset(location, offset + amount);
      }
   }

   private static void decreaseOffset(@NotNull Location location, int amount) {
      int offset = getOffset(location);
      if (offset - amount <= 53 && offset - amount >= -53) {
         setOffset(location, offset - amount);
      }
   }

   private static void setOffset(@NotNull Location location, int offset) {
      offsetMap.put(location, offset);
      StorageCacheUtils.setData(location, "offset", String.valueOf(offset));
      updateOffsetShowIcon(location, offset);
   }

   private static int getOffset(@NotNull Location location) {
      Integer offset = offsetMap.get(location);
      if (offset == null) {
         String data = StorageCacheUtils.getData(location, "offset");
         if (data == null) {
            setOffset(location, 0);
            offset = 0;
         } else {
            try {
               offset = Integer.parseInt(data);
            } catch (NumberFormatException var4) {
               setOffset(location, 0);
               offset = 0;
            }
         }
      }

      return offset;
   }

   private static void updateOffsetShowIcon(@NotNull Location location, int offset) {
      ItemStack newIcon = Icon.OFFSET_SHOW_ICON.clone();
      ItemMeta meta = newIcon.getItemMeta();
      if (meta != null) {
         List<String> newLore = new ArrayList<>();
         newLore.add(String.format(Lang.getString("icons.offset-show-icon.lore"), offset));
         meta.setLore(newLore);
         newIcon.setItemMeta(meta);
         BlockMenu blockMenu = StorageCacheUtils.getMenu(location);
         if (blockMenu != null) {
            blockMenu.replaceExistingItem(4, newIcon);
         }
      }
   }

   public void preRegister() {
      this.addItemHandler(new ItemHandler[]{new BlockBreakHandler(false, false) {
         public void onPlayerBreak(@NotNull BlockBreakEvent blockBreakEvent, @NotNull ItemStack itemStack, @NotNull List<ItemStack> list) {
            Location location = blockBreakEvent.getBlock().getLocation();
            Offsetter.facingMap.remove(location);
            Offsetter.offsetMap.remove(location);
         }
      }});
      this.addItemHandler(new ItemHandler[]{new BlockTicker() {
         public boolean isSynchronized() {
            return false;
         }

         public void tick(@NotNull Block block, SlimefunItem slimefunItem, SlimefunBlockData data) {
            Offsetter.onTick(block);
         }
      }});
   }

   public void postRegister() {
      new BlockMenuPreset(this.getId(), this.getItemName()) {
         public void init() {
            for (int i = 0; i < 9; i++) {
               this.addItem(i, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
            }

            this.addItem(5, Icon.OFFSET_INCREASE_ICON, ChestMenuUtils.getEmptyClickHandler());
            this.addItem(4, Icon.OFFSET_SHOW_ICON, ChestMenuUtils.getEmptyClickHandler());
            this.addItem(3, Icon.OFFSET_DECREASE_ICON, ChestMenuUtils.getEmptyClickHandler());
         }

         public boolean canOpen(@NotNull Block block, @NotNull Player player) {
            return player.hasPermission("slimefun.inventory.bypass")
               || Slimefun.getPermissionsService().hasPermission(player, this.getSlimefunItem())
                  && Slimefun.getProtectionManager().hasPermission(player, block, Interaction.INTERACT_BLOCK);
         }

         public int[] getSlotsAccessedByItemTransport(ItemTransportFlow itemTransportFlow) {
            return new int[0];
         }

         public void newInstance(@NotNull BlockMenu blockMenu, @NotNull Block block) {
            BlockData blockData = block.getBlockData();
            BlockFace blockFace = null;
            AttachedFace attachedFace = null;
            if (blockData instanceof Directional directional) {
               blockFace = directional.getFacing();
            }

            if (blockFace != null) {
               if (blockData instanceof FaceAttachable faceAttachable) {
                  attachedFace = faceAttachable.getAttachedFace();
               }

               if (attachedFace != null) {
                  Location location = block.getLocation();
                  switch (attachedFace) {
                     case FLOOR:
                        Offsetter.facingMap.put(location, TransportFacing.DOWN_TO_UP);
                        break;
                     case CEILING:
                        Offsetter.facingMap.put(location, TransportFacing.UP_TO_DOWN);
                        break;
                     case WALL:
                        switch (blockFace) {
                           case WEST:
                              Offsetter.facingMap.put(location, TransportFacing.EAST_TO_WEST);
                              break;
                           case EAST:
                              Offsetter.facingMap.put(location, TransportFacing.WEST_TO_EAST);
                              break;
                           case NORTH:
                              Offsetter.facingMap.put(location, TransportFacing.SOUTH_TO_NORTH);
                              break;
                           case SOUTH:
                              Offsetter.facingMap.put(location, TransportFacing.NORTH_TO_SOUTH);
                        }
                  }

                  Offsetter.getOffset(location);
                  Offsetter.updateOffsetShowIcon(location, Offsetter.getOffset(location));
                  blockMenu.addMenuClickHandler(5, (player, slot, item, clickAction) -> {
                     Offsetter.increaseOffset(location, 1);
                     return false;
                  });
                  blockMenu.addMenuClickHandler(3, (player, slot, item, clickAction) -> {
                     Offsetter.decreaseOffset(location, 1);
                     return false;
                  });
               }
            }
         }
      };
   }
}
