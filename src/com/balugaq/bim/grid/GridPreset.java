package com.balugaq.bim.grid;

import com.balugaq.bim.BIMLoader;
import com.balugaq.bim.general.BlockPos;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import lombok.Generated;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract class GridPreset {
   public static final float DEFAULT_GAP = 0.12F;
   final Plugin plugin;
   final NamespacedKey identifier;
   @Range(
      from = 1L,
      to = 2147483647L
   )
   final int height;
   @Range(
      from = 1L,
      to = 2147483647L
   )
   final int width;
   float gap;

   public GridPreset(Plugin plugin, NamespacedKey identifier, @Range(from = 1L,to = 2147483647L) int height, @Range(from = 1L,to = 2147483647L) int width) {
      this(plugin, identifier, height, width, 0.12F);
   }

   public GridPreset(
      Plugin plugin, NamespacedKey identifier, @Range(from = 1L,to = 2147483647L) int height, @Range(from = 1L,to = 2147483647L) int width, float gap
   ) {
      this.plugin = plugin;
      this.identifier = identifier;
      this.height = height;
      this.width = width;
      this.gap = gap;
      GridPresetRegistry.registerOption(this);
   }

   public ActiveGrid place(BlockPos pos, GridOrientation orientation) {
      return BIMLoader.get(this.plugin).getGridUtil().placeGrid(pos, orientation, this);
   }

   public abstract void init(ActiveGrid var1, int var2, InteractUnit var3);

   public abstract void postInit(ActiveGrid var1);

   @OverridingMethodsMustInvokeSuper
   public void onShow(ActiveGrid grid) {
      grid.isDisplaying = true;
   }

   @OverridingMethodsMustInvokeSuper
   public void onHide(ActiveGrid grid) {
      grid.isDisplaying = false;
   }

   public void onHover(InteractUnit unit, Player player) {
   }

   public void offHover(InteractUnit unit, Player player) {
   }

   @OverridingMethodsMustInvokeSuper
   public void onScroll(ActiveGrid active, ScrollResult result, int delta, PlayerItemHeldEvent event) {
      switch (result) {
         case UP:
            active.scrollOffset = Math.min(
               active.scrollOffset + delta,
               (Math.max(0, this.entriesSize(active) - this.getWidth() * this.getHeight()) + this.getWidth() - 1) / this.getWidth()
            );
            break;
         case DOWN:
            active.scrollOffset = Math.max(active.scrollOffset - delta, 0);
      }
   }

   public abstract void updateDisplayItem(ActiveGrid var1, int var2, InteractUnit var3);

   public abstract void tick(ActiveGrid var1);

   public abstract int tickInterval(ActiveGrid var1);

   public abstract int entriesSize(ActiveGrid var1);

   public void onDestroy(ActiveGrid g) {
      g.units.values().forEach(u -> {
         u.itemDisplay.remove();
         u.titleDisplay.remove();
         u.amountDisplay.remove();
      });
      g.backgrounds.forEach(Entity::remove);
   }

   public boolean defaultBackground() {
      return true;
   }

   public BoundingBox getOccupiedBoundingBox(Location location, GridOrientation orientation) {
      Location location1 = orientation.apply(location, (double)this.getGap() * ((double)this.getWidth() + 0.5), (double)(-this.getGap()) * 0.5, 0.0);
      Location location2 = orientation.apply(location, (double)this.getGap() * 0.5, (double)this.getGap() * ((double)this.getHeight() - 0.5), 0.0);
      return BoundingBox.of(location1, location2);
   }

   @Generated
   public Plugin getPlugin() {
      return this.plugin;
   }

   @Generated
   public NamespacedKey getIdentifier() {
      return this.identifier;
   }

   @Generated
   public int getHeight() {
      return this.height;
   }

   @Generated
   public int getWidth() {
      return this.width;
   }

   @Generated
   public float getGap() {
      return this.gap;
   }

   @Generated
   public void setGap(float gap) {
      this.gap = gap;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof GridPreset other)) {
         return false;
      } else if (!other.canEqual(this)) {
         return false;
      } else if (this.getHeight() != other.getHeight()) {
         return false;
      } else if (this.getWidth() != other.getWidth()) {
         return false;
      } else if (Float.compare(this.getGap(), other.getGap()) != 0) {
         return false;
      } else {
         Object this$plugin = this.getPlugin();
         Object other$plugin = other.getPlugin();
         if (this$plugin == null ? other$plugin == null : this$plugin.equals(other$plugin)) {
            Object this$identifier = this.getIdentifier();
            Object other$identifier = other.getIdentifier();
            return this$identifier == null ? other$identifier == null : this$identifier.equals(other$identifier);
         } else {
            return false;
         }
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof GridPreset;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      result = result * 59 + this.getHeight();
      result = result * 59 + this.getWidth();
      result = result * 59 + Float.floatToIntBits(this.getGap());
      Object $plugin = this.getPlugin();
      result = result * 59 + ($plugin == null ? 43 : $plugin.hashCode());
      Object $identifier = this.getIdentifier();
      return result * 59 + ($identifier == null ? 43 : $identifier.hashCode());
   }

   @Generated
   @Override
   public String toString() {
      return "GridPreset(plugin="
         + this.getPlugin()
         + ", identifier="
         + this.getIdentifier()
         + ", height="
         + this.getHeight()
         + ", width="
         + this.getWidth()
         + ", gap="
         + this.getGap()
         + ")";
   }
}
