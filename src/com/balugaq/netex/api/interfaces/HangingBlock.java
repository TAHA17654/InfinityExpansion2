package com.balugaq.netex.api.interfaces;

import com.balugaq.netex.utils.Debug;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.sefiraat.networks.slimefun.network.NetworkDirectional;
import io.github.sefiraat.networks.slimefun.network.NetworkObject;
import io.github.sefiraat.networks.utils.Keys;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.papermc.paper.event.player.PlayerItemFrameChangeEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.ParametersAreNonnullByDefault;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface HangingBlock {
   Map<BlockFace, Function<Location, Location>> locationFixer = new HashMap<BlockFace, Function<Location, Location>>() {
      {
         this.put(BlockFace.NORTH, loc -> loc.clone().setDirection(BlockFace.NORTH.getDirection()));
         this.put(BlockFace.SOUTH, loc -> loc.clone().setDirection(BlockFace.SOUTH.getDirection()));
         this.put(BlockFace.EAST, loc -> loc.clone().setDirection(BlockFace.EAST.getDirection()));
         this.put(BlockFace.WEST, loc -> loc.clone().setDirection(BlockFace.WEST.getDirection()));
         this.put(BlockFace.UP, loc -> loc.clone().setDirection(BlockFace.UP.getDirection()));
         this.put(BlockFace.DOWN, loc -> loc.clone().setDirection(BlockFace.DOWN.getDirection()));
      }
   };
   Map<String, HangingBlock> REGISTRY = new HashMap<>();
   Map<Location, Map<BlockFace, HangingBlock>> hangingBlocks = new HashMap<>();
   String BS_NORTH = "netex-hanging-north";
   String BS_SOUTH = "netex-hanging-south";
   String BS_EAST = "netex-hanging-east";
   String BS_WEST = "netex-hanging-west";
   String BS_UP = "netex-hanging-up";
   String BS_DOWN = "netex-hanging-down";
   NamespacedKey NETEX_HANGING_KEY = Keys.newKey("netex_hanging");
   double CENTER_OFFSET = 0.5;
   double ITEM_FRAME_OFFSET = 0.675;

   static <T extends HangingBlock> void registerHangingBlock(@NotNull T block) {
      REGISTRY.put(block.getId(), block);
   }

   @Contract("null -> null")
   static HangingBlock getHangingBlock(@Nullable String id) {
      return id == null ? null : REGISTRY.get(id);
   }

   static void tagItemFrame(@NotNull ItemFrame itemFrame, @NotNull String id) {
      itemFrame.getPersistentDataContainer().set(NETEX_HANGING_KEY, PersistentDataType.STRING, id);
   }

   @Nullable
   static HangingBlock getByItemFrame(@NotNull ItemFrame itemFrame) {
      return getHangingBlock((String)itemFrame.getPersistentDataContainer().get(NETEX_HANGING_KEY, PersistentDataType.STRING));
   }

   static void tickHangingBlocks(@NotNull Block attachon) {
      Location loc = attachon.getLocation();
      Map<BlockFace, HangingBlock> hs = getHangingBlocks(loc);
      getHangingBlocks(loc, hs.keySet()).forEach((attachSide, itemFrame) -> {
         try {
            hs.get(attachSide).onTick(loc, attachSide, itemFrame);
         } catch (Exception var5) {
            Debug.trace(var5);
         }
      });
   }

   static void loadHangingBlocks(@NotNull SlimefunBlockData attachon) {
      Map<BlockFace, ItemFrame> map = getHangingBlocks(attachon.getLocation(), NetworkDirectional.VALID_FACES);

      for (Entry<BlockFace, ItemFrame> entry : map.entrySet()) {
         HangingBlock hangingBlock = getByItemFrame(entry.getValue());
         if (hangingBlock != null) {
            placeHangingBlock(attachon, entry.getValue(), entry.getKey(), hangingBlock);
         }
      }
   }

   @NotNull
   static Map<BlockFace, ItemFrame> getHangingBlocks(@NotNull Location attachon, @NotNull Set<BlockFace> attachSides) {
      Map<BlockFace, ItemFrame> hangingBlocks = new HashMap<>();

      for (BlockFace attachSide : attachSides) {
         ItemFrame v = getItemFrame(attachon, attachSide);
         if (v != null) {
            hangingBlocks.put(attachSide, v);
         }
      }

      return hangingBlocks;
   }

   @Nullable
   static String getHangingBlockId(@NotNull SlimefunBlockData attachon, @NotNull BlockFace attachSide) {
      return attachon.getData(getHangingBlockKey(attachSide));
   }

   @Nullable
   static String getHangingBlockKey(@NotNull BlockFace attachSide) {
      return switch (attachSide) {
         case NORTH -> "netex-hanging-north";
         case SOUTH -> "netex-hanging-south";
         case EAST -> "netex-hanging-east";
         case WEST -> "netex-hanging-west";
         case UP -> "netex-hanging-up";
         case DOWN -> "netex-hanging-down";
         default -> null;
      };
   }

   static <T extends HangingBlock> void placeHangingBlock(
      @NotNull SlimefunBlockData attachon, @NotNull ItemFrame entityBlock, @NotNull BlockFace attachSide, @NotNull T hangingBlock
   ) {
      switch (attachSide) {
         case NORTH:
            attachon.setData("netex-hanging-north", hangingBlock.getId());
            break;
         case SOUTH:
            attachon.setData("netex-hanging-south", hangingBlock.getId());
            break;
         case EAST:
            attachon.setData("netex-hanging-east", hangingBlock.getId());
            break;
         case WEST:
            attachon.setData("netex-hanging-west", hangingBlock.getId());
            break;
         case UP:
            attachon.setData("netex-hanging-up", hangingBlock.getId());
            break;
         case DOWN:
            attachon.setData("netex-hanging-down", hangingBlock.getId());
      }

      Map<BlockFace, HangingBlock> e = hangingBlocks.get(attachon.getLocation());
      if (e == null) {
         e = new HashMap<>();
      }

      e.put(attachSide, hangingBlock);
      hangingBlocks.put(attachon.getLocation(), e);
      tagItemFrame(entityBlock, hangingBlock.getId());
   }

   @NotNull
   static Map<BlockFace, HangingBlock> getHangingBlocks(Location attachon) {
      return Optional.ofNullable((Map)hangingBlocks.get(attachon)).orElse(new HashMap<>());
   }

   @Nullable
   static ItemFrame getItemFrame(@NotNull Location attachon, BlockFace attachSide) {
      Location center = attachon.toBlockLocation().add(0.5, 0.5, 0.5);

      for (Entity e : center.getWorld().getNearbyEntities(center, 0.675, 0.675, 0.675)) {
         if (e instanceof ItemFrame itemFrame && itemFrame.getAttachedFace() == attachSide) {
            return itemFrame;
         }
      }

      return null;
   }

   static void doFirstTick(@NotNull SlimefunBlockData attachon) {
      Location loc = attachon.getLocation();
      Map<BlockFace, HangingBlock> hs = getHangingBlocks(loc);
      getHangingBlocks(loc, hs.keySet()).forEach((attachSide, itemFrame) -> {
         try {
            hs.get(attachSide).onFirstTick(loc, attachon, attachSide, itemFrame);
         } catch (Exception var6) {
            Debug.trace(var6);
         }
      });
   }

   default Location getFixedLocation(@NotNull Location location, @NotNull BlockFace attachSide) {
      return locationFixer.get(attachSide).apply(location);
   }

   @OverridingMethodsMustInvokeSuper
   default void onPlace(@NotNull HangingPlaceEvent event, @NotNull Location attachon, @NotNull ItemFrame entityBlock, @NotNull BlockFace attachSide) {
      SlimefunBlockData data = StorageCacheUtils.getBlock(attachon);
      if (data == null) {
         event.setCancelled(true);
      } else {
         SlimefunItem sf = SlimefunItem.getById(data.getSfId());
         if (!(sf instanceof NetworkObject)) {
            event.setCancelled(true);
         } else {
            placeHangingBlock(data, entityBlock, attachSide, this);
         }
      }
   }

   default void breakBlock(@NotNull Location attachon, @NotNull ItemFrame entityBlock) {
      HangingBlock hangingBlock = getByItemFrame(entityBlock);
      if (hangingBlock != null) {
         hangingBlock.onBreak(attachon, entityBlock, hangingBlock);
      }
   }

   @OverridingMethodsMustInvokeSuper
   @ParametersAreNonnullByDefault
   default <T extends HangingBlock> void onBreak(Location attachon, ItemFrame entityBlock, T hangingBlock) {
      Map<BlockFace, HangingBlock> e = hangingBlocks.get(attachon);
      if (e != null) {
         e.remove(entityBlock.getAttachedFace());
         if (e.isEmpty()) {
            hangingBlocks.remove(attachon);
         }
      }

      ItemStack item = entityBlock.getItem();
      Location location = entityBlock.getLocation();
      entityBlock.remove();
      SlimefunBlockData data = StorageCacheUtils.getBlock(attachon);
      if (data != null) {
         String key = getHangingBlockKey(entityBlock.getAttachedFace());
         if (key != null) {
            data.removeData(key);
         }
      }
   }

   @ParametersAreNonnullByDefault
   void onInteract(Location var1, PlayerItemFrameChangeEvent var2);

   @ParametersAreNonnullByDefault
   void onTick(Location var1, BlockFace var2, ItemFrame var3);

   @ParametersAreNonnullByDefault
   default void onFirstTick(Location attachon, SlimefunBlockData attachonData, BlockFace attachSide, ItemFrame entityBlock) {
   }

   @NotNull
   String getId();
}
