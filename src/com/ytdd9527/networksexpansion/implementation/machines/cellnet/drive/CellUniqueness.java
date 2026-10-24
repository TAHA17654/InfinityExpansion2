package com.ytdd9527.networksexpansion.implementation.machines.cellnet.drive;

import com.balugaq.netex.utils.Lang;
import com.balugaq.netex.utils.NetworksVersionedParticle;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.ledger.CellPersistence;
import io.github.sefiraat.networks.Networks;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CellUniqueness {
   private static final Map<String, Set<Location>> UUID_TO_DRIVES = new ConcurrentHashMap<>();

   private CellUniqueness() {
   }

   @Nullable
   public static String getUuidString(@Nullable ItemStack itemStack) {
      return CellPersistence.getCellUUIDString(itemStack);
   }

   @NotNull
   public static Set<String> collectUuidStrings(@NotNull BlockMenu menu) {
      Set<String> uuids = new HashSet<>();

      for (int slot : CellDrive.CELL_SLOTS) {
         ItemStack item = menu.getItemInSlot(slot);
         if (item != null && !item.getType().isAir()) {
            String uuid = getUuidString(item);
            if (uuid != null) {
               uuids.add(uuid);
            }
         }
      }

      return uuids;
   }

   public static boolean isDuplicateUuid(@NotNull BlockMenu menu, @NotNull ItemStack candidate) {
      String uuid = getUuidString(candidate);
      return uuid == null ? false : collectUuidStrings(menu).contains(uuid);
   }

   public static synchronized void registerCell(@NotNull Location driveLocation, @NotNull ItemStack cell) {
      String uuid = getUuidString(cell);
      if (uuid != null) {
         UUID_TO_DRIVES.computeIfAbsent(uuid, k -> ConcurrentHashMap.newKeySet()).add(driveLocation);
      }
   }

   public static synchronized void registerDrive(@NotNull BlockMenu menu) {
      Location location = menu.getLocation();
      unregisterDrive(location);

      for (int slot : CellDrive.CELL_SLOTS) {
         ItemStack item = menu.getItemInSlot(slot);
         String uuid = getUuidString(item);
         if (uuid != null) {
            UUID_TO_DRIVES.computeIfAbsent(uuid, k -> ConcurrentHashMap.newKeySet()).add(location);
         }
      }
   }

   public static synchronized void unregisterDrive(@NotNull Location driveLocation) {
      if (!UUID_TO_DRIVES.isEmpty()) {
         List<String> empty = new ArrayList<>();

         for (Entry<String, Set<Location>> entry : UUID_TO_DRIVES.entrySet()) {
            Set<Location> drives = entry.getValue();
            drives.remove(driveLocation);
            if (drives.isEmpty()) {
               empty.add(entry.getKey());
            }
         }

         for (String uuid : empty) {
            UUID_TO_DRIVES.remove(uuid);
         }
      }
   }

   private static boolean isDuplicateAcrossDrives(@NotNull Location currentDrive, @NotNull ItemStack candidate) {
      String uuid = getUuidString(candidate);
      return uuid != null && isDuplicateAcrossDrives(currentDrive, uuid);
   }

   private static boolean isDuplicateAcrossDrives(@NotNull Location currentDrive, @NotNull String uuid) {
      Set<Location> drives = UUID_TO_DRIVES.get(uuid);
      if (drives != null && !drives.isEmpty()) {
         for (Location drive : drives) {
            if (!drive.equals(currentDrive)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static boolean isDuplicate(@NotNull BlockMenu menu, @NotNull ItemStack candidate) {
      return isDuplicateUuid(menu, candidate) || isDuplicateAcrossDrives(menu.getLocation(), candidate);
   }

   public static void scanAndEjectDuplicates(@NotNull BlockMenu menu, @NotNull Player player) {
      registerDrive(menu);
      Location location = menu.getLocation();
      Set<String> seen = new HashSet<>();

      for (int slot : CellDrive.CELL_SLOTS) {
         ItemStack item = menu.getItemInSlot(slot);
         String uuid = getUuidString(item);
         if (uuid != null && (!seen.add(uuid) || isDuplicateAcrossDrives(location, uuid))) {
            ejectDuplicate(menu, player, slot);
         }
      }
   }

   public static void ejectDuplicate(@NotNull BlockMenu menu, @NotNull Player player, int slot) {
      ItemStack item = menu.getItemInSlot(slot);
      if (item != null && !item.getType().isAir()) {
         menu.replaceExistingItem(slot, null);
         String uuid = getUuidString(item);
         if (uuid != null) {
            Set<Location> drives = UUID_TO_DRIVES.get(uuid);
            if (drives != null && !collectUuidStrings(menu).contains(uuid)) {
               drives.remove(menu.getLocation());
               if (drives.isEmpty()) {
                  UUID_TO_DRIVES.remove(uuid);
               }
            }
         }

         dropItem(menu.getLocation().clone().add(0.0, 1.0, 0.0), item);
      }
   }

   public static void notifyDuplicateRejected(@NotNull Player player) {
      String message = Lang.getString("messages.cellnet.drive.cell_uuid_duplicate");
      player.sendTitle("", message, 5, 55, 20);
   }

   private static void dropItem(@NotNull Location location, @NotNull ItemStack item) {
      World world = location.getWorld();
      if (world != null) {
         Location center = location.clone().add(0.5, 0.15, 0.5);
         Item entity = world.dropItem(center, item);
         if (entity != null) {
            entity.setVelocity(new Vector(0, 0, 0));
            flashEntity(entity);
         }
      }
   }

   private static void flashEntity(@NotNull final Item entity) {
      (new BukkitRunnable() {
         private int ticks = 0;

         public void run() {
            if (!entity.isValid()) {
               this.cancel();
            } else {
               entity.setGlowing(this.ticks % 2 == 0);
               World world = entity.getWorld();
               if (world != null) {
                  Location loc = entity.getLocation();
                  world.spawnParticle(NetworksVersionedParticle.DUST, loc, 8, 0.3, 0.3, 0.3, new DustOptions(Color.fromRGB(16734810), 1.2F));
               }

               if (++this.ticks >= 4) {
                  entity.setGlowing(false);
                  this.cancel();
               }
            }
         }
      }).runTaskTimer(Networks.getInstance(), 0L, 4L);
   }
}
