package com.example.machinestacker;

import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class StackListener implements Listener {

    private final MachineStacker plugin;

    public StackListener(MachineStacker plugin) {
        this.plugin = plugin;
    }

    /** Shift + right click a machine while holding the same machine = add it to the stack. */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (!e.getPlayer().isSneaking() || !e.getPlayer().hasPermission("machinestacker.use")) {
            return;
        }
        Block block = e.getClickedBlock();
        ItemStack hand = e.getItem();
        if (block == null || hand == null || hand.getAmount() < 1) {
            return;
        }

        SlimefunItem placed = BlockStorage.check(block);
        SlimefunItem held = SlimefunItem.getByItem(hand);
        if (placed == null || held == null || !placed.getId().equals(held.getId())) {
            return;
        }
        if (placed.getBlockTicker() == null || plugin.isBlacklisted(placed.getId())) {
            return;
        }

        // respect protection plugins (they deny the block interaction before us)
        if (e.useInteractedBlock() == org.bukkit.event.Event.Result.DENY) {
            return;
        }
        e.setCancelled(true);

        Location loc = block.getLocation();
        int current = plugin.getStack(loc);
        if (current >= plugin.getMaxStack()) {
            e.getPlayer().sendMessage("§cThis machine is already at max stack (" + plugin.getMaxStack() + ").");
            return;
        }

        if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
            hand.setAmount(hand.getAmount() - 1);
        }
        plugin.setStack(loc, current + 1);
        e.getPlayer().sendMessage("§aStacked! §7(" + (current + 1) + "/" + plugin.getMaxStack() + ")");
    }

    /** Give back the extra machines when the block is broken. */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        Location loc = e.getBlock().getLocation();
        int count = plugin.getStack(loc);
        if (count <= 1) {
            return;
        }
        SlimefunItem item = BlockStorage.check(e.getBlock());
        plugin.setStack(loc, 1);
        if (item == null) {
            return;
        }
        int extra = count - 1;
        while (extra > 0) {
            ItemStack drop = item.getItem().clone();
            int amt = Math.min(extra, drop.getMaxStackSize());
            drop.setAmount(amt);
            loc.getWorld().dropItemNaturally(loc.clone().add(0.5, 0.5, 0.5), drop);
            extra -= amt;
        }
    }
}
