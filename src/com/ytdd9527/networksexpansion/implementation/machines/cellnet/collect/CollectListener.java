package com.ytdd9527.networksexpansion.implementation.machines.cellnet.collect;

import com.balugaq.netex.utils.Lang;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.sefiraat.networks.slimefun.network.NetworkController;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CollectListener implements Listener {
   private static final long INTERACT_THROTTLE_MS = 250L;
   private static final Map<UUID, Long> LAST_INTERACT = new ConcurrentHashMap<>();

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public void onEntityDeath(@NotNull EntityDeathEvent event) {
      if (CollectService.isEnabled()) {
         Player killer = event.getEntity().getKiller();
         if (killer != null) {
            CollectService.collectFromKill(killer, event);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH,
      ignoreCancelled = true
   )
   public void onBlockDropItem(@NotNull BlockDropItemEvent event) {
      if (CollectService.isEnabled()) {
         CollectService.collectFromBlock(event.getPlayer(), event.getItems());
      }
   }

   @EventHandler
   public void onInteract(@NotNull PlayerInteractEvent event) {
      if (CollectService.isEnabled()) {
         if (event.getHand() == EquipmentSlot.HAND && event.getPlayer().isSneaking()) {
            boolean rightAir = event.getAction() == Action.RIGHT_CLICK_AIR;
            boolean rightBlock = event.getAction() == Action.RIGHT_CLICK_BLOCK;
            if (rightAir || rightBlock) {
               ItemStack weapon = event.getPlayer().getInventory().getItemInMainHand();
               if (CollectService.hasMark(weapon)) {
                  if (!throttled(event.getPlayer())) {
                     if (rightAir) {
                        CollectService.toggleMode(weapon, event.getPlayer());
                     } else {
                        Block block = event.getClickedBlock();
                        if (block != null) {
                           if (StorageCacheUtils.getSfItem(block.getLocation()) instanceof NetworkController) {
                              Player player = event.getPlayer();
                              if (!Slimefun.getProtectionManager().hasPermission(player, block.getLocation(), Interaction.INTERACT_BLOCK)) {
                                 player.sendMessage(Lang.getString("messages.cellnet.collect.no-permission"));
                              } else {
                                 event.setCancelled(true);
                                 CollectService.bind(weapon, block.getLocation());
                                 player.sendMessage(Lang.getString("messages.cellnet.collect.bind", block.getX() + ", " + block.getY() + ", " + block.getZ()));
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static boolean throttled(@NotNull Player player) {
      long now = System.currentTimeMillis();
      Long last = LAST_INTERACT.get(player.getUniqueId());
      if (last != null && now - last < 250L) {
         return true;
      } else {
         LAST_INTERACT.put(player.getUniqueId(), now);
         return false;
      }
   }
}
