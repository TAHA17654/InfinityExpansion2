package com.example.machinestacker;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockTicker;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Map;

/**
 * Every few ticks, runs the machine's own ticker (count - 1) extra times.
 * Speed, energy use and output scale exactly like having `count` separate machines.
 */
public class StackTask implements Runnable {

    private final MachineStacker plugin;

    public StackTask(MachineStacker plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Map.Entry<Location, Integer> entry : new ArrayList<>(plugin.getStacks().entrySet())) {
            Location loc = entry.getKey();
            int count = Math.min(entry.getValue(), plugin.getMaxStack());

            if (loc.getWorld() == null || !loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) {
                continue;
            }

            Block block = loc.getBlock();
            SlimefunItem item = BlockStorage.check(block);
            if (item == null) {
                plugin.setStack(loc, 1); // machine is gone
                continue;
            }

            BlockTicker ticker = item.getBlockTicker();
            if (ticker == null) {
                continue;
            }

            for (int i = 1; i < count; i++) {
                try {
                    ticker.tick(block, item, BlockStorage.getLocationInfo(loc));
                } catch (Exception ex) {
                    plugin.getLogger().warning("Stacked tick failed for " + item.getId() + ": " + ex.getMessage());
                    break;
                }
            }
        }
    }
}
