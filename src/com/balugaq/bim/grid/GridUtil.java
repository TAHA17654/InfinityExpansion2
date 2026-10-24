package com.balugaq.bim.grid;

import com.balugaq.bim.BIMLoader;
import com.balugaq.bim.events.PlayerOffGridEvent;
import com.balugaq.bim.events.PlayerOffHoverUnitEvent;
import com.balugaq.bim.general.BlockPos;
import com.balugaq.bim.general.TransformationBuilder;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Collection;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.entity.Display.Brightness;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class GridUtil {
   private final Plugin instance;
   public final NamespacedKey TAG;
   public static final Brightness MDB = new Brightness(15, 15);
   public static final Brightness KDB = new Brightness(12, 12);
   public static final int SHOW_DISTANCE = 10;
   public static final int HIDE_DISTANCE = 50;
   public static final byte TEXT_OPACITY_HIDDEN = 0;
   public static final byte TEXT_OPACITY_SHOWN = -1;

   public GridUtil(Plugin instance) {
      this.instance = instance;
      this.TAG = new NamespacedKey(instance, "tag");
   }

   @Nullable
   public static <T extends Entity> T rayTraceEntity(Vector origin, Vector direction, Collection<T> candidates) {
      Vector dir = direction.clone().normalize();
      T closest = null;
      double minDistance = Double.MAX_VALUE;

      for (T candidate : candidates) {
         Vector toCandidate = candidate.getLocation().toVector().subtract(origin);
         double t = toCandidate.dot(dir);
         if (!(t < 0.0)) {
            double distance = toCandidate.crossProduct(dir).length();
            if (distance < 0.12 && distance < minDistance) {
               minDistance = distance;
               closest = candidate;
            }
         }
      }

      return closest;
   }

   public static String formatAmount(long amount) {
      if (amount < 0L) {
         return "-" + formatAmount(-amount);
      } else if (amount < 1000L) {
         return String.valueOf(amount);
      } else {
         String[] units = new String[]{"", "K", "M", "B", "T", "P", "E"};
         int unitIndex = 0;

         double value;
         for (value = (double)amount; value >= 1000.0 && unitIndex < units.length - 1; unitIndex++) {
            value /= 1000.0;
         }

         String formatted = String.format("%.1f", value);
         if (formatted.endsWith(".0")) {
            formatted = formatted.substring(0, formatted.length() - 2);
         }

         return formatted + units[unitIndex];
      }
   }

   public BIMLoader getLoader() {
      return BIMLoader.get(this.instance);
   }

   public ActiveGrid placeGrid(BlockPos pos, GridOrientation o, GridPreset option) {
      this.removeGrid(pos, option);
      GridDataCache cache = this.getLoader().getCache();
      Location loc = pos.toLocation();
      ActiveGrid active = new ActiveGrid(loc, option, o);
      Int2ObjectOpenHashMap<InteractUnit> units = new Int2ObjectOpenHashMap();
      active.units = units;
      float gap = option.getGap();

      for (int h = 0; h < option.getHeight(); h++) {
         for (int w = 0; w < option.getWidth(); w++) {
            Location uloc = o.apply(loc, (double)gap * ((double)w + 0.5), (double)gap * ((double)(option.getHeight() - h) - 0.5), 0.0);
            int i = h * option.getWidth() + w;
            InteractUnit u = new InteractUnit(i, uloc, active);
            units.put(i, u);
            cache.index().put(u.itemDisplay, u);
            cache.index().put(u.titleDisplay, u);
            cache.index().put(u.amountDisplay, u);
            option.init(active, i, u);
         }
      }

      cache.activeGrids().put(pos, active);
      if (option.defaultBackground()) {
         this.addDefaultBackground(active, loc);
      }

      option.postInit(active);
      return active;
   }

   private void addDefaultBackground(ActiveGrid active, Location location) {
      GridPreset option = active.option;
      float scale = 0.1F;
      float precision = 2.0F;
      float heightPerText = 0.25F * scale;
      float widthPerText = 0.016041666F * scale;
      float gap = option.gap;
      GridOrientation o = GridOrientation.fromYawPitch(location.getYaw(), location.getPitch());
      float blockWidth = (gap + 0.003125F) * (float)option.getWidth();
      float blockHeight = gap * (float)option.getHeight();
      float textWidth = blockWidth / widthPerText * 0.625F * precision;
      int t = Math.round(blockHeight / heightPerText);
      TextComponent ht = Component.text(".".repeat(Math.round(textWidth / 4.0F)));

      for (int h = 1; h <= t; h++) {
         TextDisplay bg = (TextDisplay)location.getWorld()
            .spawn(
               o.apply(
                  location,
                  (double)gap * ((double)((float)option.getWidth() / 2.0F) + 0.5),
                  (double)gap * ((double)option.getHeight() - 0.5) - (double)(heightPerText * (float)h) - 0.003,
                  0.001
               ),
               TextDisplay.class
            );
         bg.setRotation(o.getYaw(), o.getPitch());
         bg.text(ht);
         bg.setTextOpacity((byte)0);
         bg.setTransformation(TransformationBuilder.create().scale(scale * 2.0F / precision).build());
         bg.setBackgroundColor(Color.fromRGB(9145227));
         bg.setBrightness(MDB);
         bg.setLineWidth(999999);
         active.tag(bg);
         active.backgrounds.add(bg);
      }

      String s = ".\n".repeat(Math.round(blockHeight / heightPerText * precision));
      s = s.substring(0, s.length() - 1);
      TextComponent wt = Component.text(s);

      for (int w = 0; w <= option.getWidth(); w++) {
         TextDisplay divider = (TextDisplay)location.getWorld()
            .spawn(o.apply(location, (double)gap * ((double)w + 0.5), (double)(-gap) * 0.5, 0.002), TextDisplay.class);
         divider.setRotation(o.getYaw(), o.getPitch());
         divider.text(wt);
         divider.setTextOpacity((byte)0);
         divider.setTransformation(TransformationBuilder.create().scale(scale / precision).build());
         divider.setBackgroundColor(Color.fromRGB(12829635));
         divider.setBrightness(MDB);
         divider.setLineWidth(1);
         active.tag(divider);
         active.backgrounds.add(divider);
      }

      for (int h = 0; h <= option.getHeight(); h++) {
         TextDisplay divider = (TextDisplay)location.getWorld()
            .spawn(
               o.apply(
                  location,
                  (double)gap * ((double)((float)option.getWidth() / 2.0F) + 0.5),
                  (double)gap * ((double)(option.getHeight() - h) - 0.5) - 0.003,
                  0.003
               ),
               TextDisplay.class
            );
         divider.setRotation(o.getYaw(), o.getPitch());
         divider.text(Component.text(".".repeat(Math.round(textWidth * 2.0F))));
         divider.setTextOpacity((byte)0);
         divider.setTransformation(TransformationBuilder.create().scale(scale / 4.0F / precision).build());
         divider.setBackgroundColor(Color.fromRGB(12829635));
         divider.setBrightness(MDB);
         divider.setLineWidth(999999);
         active.tag(divider);
         active.backgrounds.add(divider);
      }
   }

   public void removeGrid(BlockPos pos, GridPreset option) {
      GridDataCache cache = this.getLoader().getCache();
      ActiveGrid act = cache.activeGrids().get(pos);
      if (act != null) {
         this.removeGrid(pos, act.getOccupiedBoundingBox());
      } else {
         Location loc = pos.toLocation();

         for (GridOrientation ori : GridOrientation.values()) {
            this.removeGrid(pos, option.getOccupiedBoundingBox(loc, ori));
         }
      }
   }

   public void removeGrid(BlockPos pos, BoundingBox boundingBox) {
      GridDataCache cache = this.getLoader().getCache();
      ActiveGrid active = cache.activeGrids().remove(pos);
      if (active == null) {
         for (Entity e : pos.toLocation().getWorld().getNearbyEntities(boundingBox)) {
            String c = (String)e.getPersistentDataContainer().get(this.TAG, PersistentDataType.STRING);
            if (pos.getTag().equals(c)) {
               e.remove();
            }
         }
      } else {
         active.option.onDestroy(active);

         for (TextDisplay display : active.backgrounds) {
            display.remove();
         }

         cache.watching()
            .values()
            .removeIf(
               u -> u.itemDisplay.isDead()
                     || !u.itemDisplay.isValid()
                     || u.titleDisplay.isDead()
                     || !u.titleDisplay.isValid()
                     || u.amountDisplay.isDead()
                     || !u.amountDisplay.isValid()
            );
      }
   }

   public void offGrid(Player player) {
      InteractUnit u = this.getLoader().getCache().watching().remove(player.getUniqueId());
      if (u != null) {
         new PlayerOffGridEvent(player, u.grid).callEvent();
         offHover(u, player);
      }
   }

   public static void offHover(InteractUnit old, Player p) {
      new PlayerOffHoverUnitEvent(p, old).callEvent();
      old.grid.option.offHover(old, p);
      old.titleDisplay.setTextOpacity((byte)0);
      old.itemDisplay.setBrightness(KDB);
   }

   public static Vector getDirection(float yaw, float pitch) {
      double yawRad = Math.toRadians((double)yaw);
      double pitchRad = Math.toRadians((double)pitch);
      double x = -Math.sin(yawRad) * Math.cos(pitchRad);
      double y = -Math.sin(pitchRad);
      double z = Math.cos(yawRad) * Math.cos(pitchRad);
      return new Vector(x, y, z);
   }

   @Nullable
   public InteractUnit rayTraceUnit(Player p) {
      ItemDisplay result = rayTraceEntity(
         p.getEyeLocation().toVector(),
         getDirection(p.getYaw(), p.getPitch()),
         p.getLocation().getWorld().getNearbyEntitiesByType(ItemDisplay.class, p.getLocation(), 5.0)
      );
      return result == null ? null : this.getLoader().getCache().index().get(result);
   }
}
