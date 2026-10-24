package com.example.machinestacker;

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MachineStacker extends JavaPlugin implements SlimefunAddon {

    private final Map<Location, Integer> stacks = new HashMap<>();
    private File dataFile;
    private int maxStack;
    private List<String> blacklist;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSettings();
        dataFile = new File(getDataFolder(), "stacks.yml");
        loadStacks();

        getServer().getPluginManager().registerEvents(new StackListener(this), this);

        int interval = Math.max(1, getConfig().getInt("tick-interval", 2));
        Bukkit.getScheduler().runTaskTimer(this, new StackTask(this), 40L, interval);
        Bukkit.getScheduler().runTaskTimer(this, this::saveStacks, 6000L, 6000L);
    }

    @Override
    public void onDisable() {
        saveStacks();
    }

    private void loadSettings() {
        maxStack = Math.max(1, Math.min(64, getConfig().getInt("max-stack", 64)));
        blacklist = getConfig().getStringList("blacklist");
    }

    public int getMaxStack() {
        return maxStack;
    }

    public boolean isBlacklisted(String id) {
        return blacklist.contains(id);
    }

    public Map<Location, Integer> getStacks() {
        return stacks;
    }

    public int getStack(Location loc) {
        return stacks.getOrDefault(loc, 1);
    }

    public void setStack(Location loc, int amount) {
        if (amount <= 1) {
            stacks.remove(loc);
        } else {
            stacks.put(loc, amount);
        }
    }

    private void loadStacks() {
        stacks.clear();
        if (!dataFile.exists()) {
            return;
        }
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(dataFile);
        for (String key : cfg.getKeys(false)) {
            String[] p = key.split(";");
            if (p.length != 4 || Bukkit.getWorld(p[0]) == null) {
                continue;
            }
            try {
                Location loc = new Location(Bukkit.getWorld(p[0]),
                        Integer.parseInt(p[1]), Integer.parseInt(p[2]), Integer.parseInt(p[3]));
                stacks.put(loc, cfg.getInt(key));
            } catch (NumberFormatException ignored) {
                // skip broken entry
            }
        }
    }

    public void saveStacks() {
        FileConfiguration cfg = new YamlConfiguration();
        for (Map.Entry<Location, Integer> e : stacks.entrySet()) {
            Location l = e.getKey();
            cfg.set(l.getWorld().getName() + ";" + l.getBlockX() + ";" + l.getBlockY() + ";" + l.getBlockZ(), e.getValue());
        }
        try {
            getDataFolder().mkdirs();
            cfg.save(dataFile);
        } catch (IOException ex) {
            getLogger().warning("Could not save stacks.yml: " + ex.getMessage());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("machinestacker.reload")) {
                sender.sendMessage("§cNo permission.");
                return true;
            }
            reloadConfig();
            loadSettings();
            sender.sendMessage("§aMachineStacker reloaded.");
            return true;
        }
        if (!(sender instanceof Player player)) {
            return true;
        }
        Block target = player.getTargetBlockExact(6);
        if (target == null || BlockStorage.check(target) == null) {
            player.sendMessage("§eLook at a Slimefun machine.");
            return true;
        }
        player.sendMessage("§aStack: §f" + getStack(target.getLocation()) + "§7/" + maxStack);
        return true;
    }

    @Override
    public JavaPlugin getJavaPlugin() {
        return this;
    }

    @Override
    public String getBugTrackerURL() {
        return null;
    }
}
