package com.balugaq.netex.utils;

import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.Pair;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.map.MapView.Scale;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class MapUtil {
   public static final Map<String, MapView> MAP_VIEWS = new HashMap<>();

   public static Pair<ItemStack, MapView> getImageItem(String imagePath) {
      ItemStack map = new ItemStack(Material.FILLED_MAP);
      MapView view = apply(map, imagePath);
      return new Pair(map, view);
   }

   @Nullable
   public static MapView apply(ItemStack map, String imagePath) {
      if (map.getItemMeta() instanceof MapMeta meta) {
         if (MAP_VIEWS.containsKey(imagePath)) {
            MapView view = MAP_VIEWS.get(imagePath);
            meta.setMapView(view);
            map.setItemMeta(meta);
            return view;
         } else {
            MapView view;
            if (!meta.hasMapView()) {
               view = Bukkit.createMap((World)Bukkit.getWorlds().getFirst());
            } else {
               view = meta.getMapView();
               if (view == null) {
                  view = Bukkit.createMap((World)Bukkit.getWorlds().getFirst());
               }
            }

            final BufferedImage finalImage = resizeImage(ImageUtil.getImage(imagePath), 128, 128);
            view.addRenderer(new MapRenderer() {
               public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
                  mapCanvas.drawImage(0, 0, finalImage);
               }
            });
            view.setScale(Scale.FARTHEST);
            view.setLocked(true);
            meta.setMapView(view);
            MAP_VIEWS.put(imagePath, view);
            meta.setScaling(true);
            meta.setColor(Color.GREEN);
            map.setItemMeta(meta);
            return view;
         }
      } else {
         return null;
      }
   }

   public static BufferedImage resizeImage(BufferedImage original, int targetWidth, int targetHeight) {
      BufferedImage resized = new BufferedImage(targetWidth, targetHeight, 1);
      Graphics2D g2d = resized.createGraphics();
      g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
      g2d.drawImage(original, 0, 0, targetWidth, targetHeight, null);
      g2d.dispose();
      return resized;
   }

   @Generated
   private MapUtil() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
