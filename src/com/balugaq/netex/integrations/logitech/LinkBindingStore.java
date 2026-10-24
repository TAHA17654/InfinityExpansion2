package com.balugaq.netex.integrations.logitech;

import com.balugaq.netex.utils.Debug;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.api.DriveType;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.ItemKey;
import com.ytdd9527.networksexpansion.implementation.machines.unit.NetworksDrawer;
import io.github.sefiraat.networks.Networks;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class LinkBindingStore {
   private static final String KEY = "LinkBindings";
   private static final int MAX_ENTRIES = 64;
   private static final String INDEX_FILE = "linker-bindings.yml";
   private static final String INDEX_KEY = "targets";
   private static int replayRounds;
   private static final int MAX_REPLAY_ROUNDS = 600;

   private LinkBindingStore() {
   }

   static void record(@NotNull Location target, @NotNull ItemStack sample) {
      List<String> encoded = readEntries(target);
      String entry = Base64.getEncoder().encodeToString(sample.serializeAsBytes());
      if (encoded.contains(entry)) {
         indexAdd(target);
      } else {
         if (encoded.size() >= 64) {
            encoded.remove(0);
         }

         encoded.add(entry);
         StorageCacheUtils.setData(target, "LinkBindings", String.join(",", encoded));
         indexAdd(target);
      }
   }

   static int replay(@NotNull Location target, @NotNull Consumer<ItemStack> injector) {
      int injected = 0;

      for (String entry : readEntries(target)) {
         try {
            ItemStack sample = ItemStack.deserializeBytes(Base64.getDecoder().decode(entry));
            injector.accept(sample);
            injected++;
         } catch (Throwable var6) {
            Debug.debug("Corrupted entanglement binding record, skipped: " + var6.getMessage());
         }
      }

      return injected;
   }

   @NotNull
   private static List<String> readEntries(@NotNull Location target) {
      String data;
      try {
         data = StorageCacheUtils.getData(target, "LinkBindings");
      } catch (IllegalStateException var3) {
         return new ArrayList<>();
      }

      if (data != null && !data.isEmpty()) {
         List<String> entries = new ArrayList<>(List.of(data.split(",", -1)));
         entries.removeIf(String::isEmpty);
         return entries;
      } else {
         return new ArrayList<>();
      }
   }

   public static void replayDrive(@NotNull Location driveLocation) {
      if (LinkerGrid.initialized) {
         if (replay(driveLocation, sample -> CellVirtualCache.inject(driveLocation, new ItemKey(sample))) > 0) {
            indexAdd(driveLocation);
         }
      }
   }

   public static void replayDrawer(@NotNull Location drawerLocation) {
      if (LinkerGrid.initialized) {
         if (replay(drawerLocation, sample -> DrawerVirtualCache.inject(drawerLocation, sample)) > 0) {
            indexAdd(drawerLocation);
         }
      }
   }

   public static void forget(@NotNull Location target) {
      List<String> entries = new ArrayList<>(readIndex());
      if (entries.remove(encode(target))) {
         writeIndex(entries);
      }
   }

   public static void replayAll() {
      if (LinkerGrid.initialized) {
         List<String> entries = readIndex();
         if (!entries.isEmpty()) {
            List<String> alive = new ArrayList<>();
            List<String> deferred = new ArrayList<>();

            for (String entry : entries) {
               Location target = decode(entry);
               if (target == null) {
                  deferred.add(entry);
               } else {
                  SlimefunItem item;
                  try {
                     item = StorageCacheUtils.getSfItem(target);
                  } catch (IllegalStateException var8) {
                     deferred.add(entry);
                     continue;
                  }

                  if (item == null) {
                     deferred.add(entry);
                  } else if (DriveType.of(item) != null) {
                     replay(target, sample -> CellVirtualCache.inject(target, new ItemKey(sample)));
                     alive.add(entry);
                  } else if (item instanceof NetworksDrawer) {
                     replay(target, sample -> DrawerVirtualCache.inject(target, sample));
                     alive.add(entry);
                  }
               }
            }

            alive.addAll(deferred);
            if (alive.size() != entries.size()) {
               writeIndex(alive);
            }

            if (!deferred.isEmpty() && ++replayRounds < 600) {
               Bukkit.getScheduler().runTaskLater(Networks.getInstance(), LinkBindingStore::replayAll, 100L);
            }
         }
      }
   }

   private static void indexAdd(@NotNull Location target) {
      List<String> entries = new ArrayList<>(readIndex());
      String entry = encode(target);
      if (!entries.contains(entry)) {
         entries.add(entry);
         writeIndex(entries);
      }
   }

   @NotNull
   private static List<String> readIndex() {
      File file = indexFile();
      return !file.exists() ? new ArrayList<>() : new ArrayList<>(YamlConfiguration.loadConfiguration(file).getStringList("targets"));
   }

   private static void writeIndex(@NotNull List<String> entries) {
      YamlConfiguration yaml = new YamlConfiguration();
      yaml.set("targets", entries);

      try {
         yaml.save(indexFile());
      } catch (IOException var3) {
         Networks.getInstance().getLogger().log(Level.WARNING, "Failed to save quantum entanglement binding index", (Throwable)var3);
      }
   }

   @NotNull
   private static File indexFile() {
      return new File(Networks.getInstance().getDataFolder(), "linker-bindings.yml");
   }

   @NotNull
   private static String encode(@NotNull Location location) {
      World world = location.getWorld();
      return world.getName() + ";" + location.getBlockX() + ";" + location.getBlockY() + ";" + location.getBlockZ();
   }

   private static Location decode(@NotNull String entry) {
      String[] parts = entry.split(";");
      if (parts.length != 4) {
         return null;
      } else {
         World world = Bukkit.getWorld(parts[0]);
         if (world == null) {
            return null;
         } else {
            try {
               return new Location(world, (double)Integer.parseInt(parts[1]), (double)Integer.parseInt(parts[2]), (double)Integer.parseInt(parts[3]));
            } catch (NumberFormatException var4) {
               return null;
            }
         }
      }
   }
}
