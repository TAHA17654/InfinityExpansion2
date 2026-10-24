package com.ytdd9527.networksexpansion.implementation.machines.cellnet.filler;

import com.balugaq.netex.utils.Debug;
import com.balugaq.netex.utils.Lang;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage.util.SerializeUtils;
import io.github.sefiraat.networks.Networks;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FillerPdc {
   private static final String BUFFER_KEY = "container_filler_buffer";
   private static final String PAUSED_KEY = "container_filler_paused";
   private static final String BOX_MODE_KEY = "container_filler_box_mode";
   private static final Set<String> CORRUPT_WARNED = ConcurrentHashMap.newKeySet();

   private FillerPdc() {
   }

   @Nullable
   public static ItemStack loadBuffer(@NotNull Location location) {
      String data = StorageCacheUtils.getData(location, "container_filler_buffer");
      if (data != null && !data.isEmpty()) {
         ItemStack buffer = SerializeUtils.string2Object(data);
         if (buffer == null) {
            warnCorrupted(location, "buffer");
            removeBuffer(location);
            return null;
         } else {
            return buffer;
         }
      } else {
         return null;
      }
   }

   public static void saveBuffer(@NotNull Location location, @NotNull ItemStack buffer) {
      StorageCacheUtils.setData(location, "container_filler_buffer", SerializeUtils.object2String(buffer));
   }

   public static void removeBuffer(@NotNull Location location) {
      StorageCacheUtils.removeData(location, "container_filler_buffer");
   }

   public static boolean isPausedPersisted(@NotNull Location location) {
      String data = StorageCacheUtils.getData(location, "container_filler_paused");
      if (data != null && !data.isEmpty()) {
         try {
            return Integer.parseInt(data) != 0;
         } catch (NumberFormatException var3) {
            Debug.trace(var3, "storage assembler paused flag parse");
            return false;
         }
      } else {
         return false;
      }
   }

   public static void writePaused(@NotNull Location location, boolean paused) {
      StorageCacheUtils.setData(location, "container_filler_paused", paused ? "1" : "0");
   }

   public static boolean isBoxModePersisted(@NotNull Location location) {
      String data = StorageCacheUtils.getData(location, "container_filler_box_mode");
      return data != null && !data.isEmpty() && data.charAt(0) == '1';
   }

   public static void writeBoxMode(@NotNull Location location, boolean boxMode) {
      StorageCacheUtils.setData(location, "container_filler_box_mode", boxMode ? "1" : "0");
   }

   private static void warnCorrupted(@NotNull Location location, @NotNull String category) {
      if (CORRUPT_WARNED.add(coordsKey(location) + "|" + category)) {
         Networks.getInstance().getLogger().warning(Lang.getString("messages.cellnet.storage_assembler." + category + "_corrupted", coordsKey(location)));
      }
   }

   @NotNull
   private static String coordsKey(@NotNull Location location) {
      return "world="
         + (location.getWorld() == null ? "null" : location.getWorld().getName())
         + " x="
         + location.getBlockX()
         + " y="
         + location.getBlockY()
         + " z="
         + location.getBlockZ();
   }
}
