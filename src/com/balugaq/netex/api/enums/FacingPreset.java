package com.balugaq.netex.api.enums;

import com.balugaq.netex.utils.Lang;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.utils.ParticleUtil;
import io.github.sefiraat.networks.slimefun.network.NetworkDirectional;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import java.util.HashSet;
import java.util.Set;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jspecify.annotations.NullMarked;

@NullMarked
public enum FacingPreset {
   NORTH,
   SOUTH,
   WEST,
   EAST,
   DOWN,
   UP,
   AUTO;

   private static final BlockFace[] FACES = new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST, BlockFace.DOWN, BlockFace.UP};

   public boolean tryApply(BlockPlaceEvent event, SlimefunBlockData blockData) {
      if (!(SlimefunItem.getById(blockData.getSfId()) instanceof NetworkDirectional)) {
         return false;
      } else if (this != AUTO) {
         this.setFace(blockData, this.name());
         return true;
      } else {
         Player player = event.getPlayer();
         if (this.isPreferBlock(blockData, event.getBlockAgainst())) {
            this.setFace(blockData, this.getFace(event.getBlock(), event.getBlockAgainst()).name());
            return true;
         } else {
            Set<BlockFace> preferFaces = new HashSet<>();

            for (BlockFace face : FACES) {
               if (this.isPreferBlock(blockData, event.getBlock().getRelative(face))) {
                  preferFaces.add(face);
               }
            }

            Location loc = blockData.getLocation();
            boolean isGrabber = blockData.getSfId().contains("TRANSFER") || blockData.getSfId().contains("GRABBER");
            boolean isPusher = blockData.getSfId().contains("TRANSFER") || blockData.getSfId().contains("PUSHER");
            if (!preferFaces.isEmpty()) {
               if (preferFaces.size() == 1) {
                  this.setFace(blockData, preferFaces.stream().findFirst().get().name());
                  return true;
               } else {
                  this.setFace(blockData, BlockFace.SELF.name());
                  player.sendMessage(
                     String.format(
                        Lang.getString("messages.unsupported-operation.comprehensive.unable_to_auto_set_face"),
                        loc.getBlockX(),
                        loc.getBlockY(),
                        loc.getBlockZ()
                     )
                  );
                  ParticleUtil.highlightBlock(player, loc, 4);
                  return false;
               }
            } else {
               for (BlockFace facex : FACES) {
                  Block b = event.getBlock().getRelative(facex);
                  if (this.isSlimefunBlock(b)) {
                     BlockMenu menu = StorageCacheUtils.getMenu(b.getLocation());
                     if (menu != null
                        && (!isGrabber || menu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW).length != 0)
                        && (!isPusher || menu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.INSERT).length != 0)) {
                        this.setFace(blockData, facex.name());
                        return true;
                     }
                  }
               }

               player.sendMessage(
                  String.format(
                     Lang.getString("messages.unsupported-operation.comprehensive.unable_to_auto_set_face"), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()
                  )
               );
               return false;
            }
         }
      }
   }

   public void setFace(SlimefunBlockData blockData, String face) {
      blockData.setData("direction", face);
   }

   public boolean isPreferBlock(SlimefunBlockData from, Block to) {
      if (from.getSfId().startsWith("NTW_") && from.getSfId().contains("VANILLA") && !this.isSlimefunBlock(to) && to.getState() instanceof Container) {
         return true;
      } else {
         SlimefunItem to_sf = StorageCacheUtils.getSfItem(to.getLocation());
         if (to_sf == null) {
            return false;
         } else {
            String fid = to_sf.getId();
            if (!from.getSfId().contains("NTW_")) {
               return true;
            } else {
               String tid = to_sf.getId();
               if (from.getSfId().contains("MONITOR")) {
                  return tid.contains("DRAWER") || tid.contains("BARREL") || tid.contains("STORAGE");
               } else {
                  return tid.contains("NTW_") && tid.contains("VANILLA") && !tid.contains("LINE")
                     ? tid.contains("GRABBER") && (fid.contains("GRABBER") || fid.contains("TRANSFER"))
                        || tid.contains("PUSHER") && (fid.contains("PUSHER") || fid.contains("TRANSFER"))
                     : !tid.contains("NTW_");
               }
            }
         }
      }
   }

   public boolean isSlimefunBlock(Block block) {
      return StorageCacheUtils.hasSlimefunBlock(block.getLocation());
   }

   public BlockFace getFace(Block from, Block to) {
      BlockFace face = from.getFace(to);
      return face != null ? face : BlockFace.SELF;
   }
}
