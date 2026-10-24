package com.balugaq.bim.grid;

import com.balugaq.bim.BIMLoader;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.HashSet;
import java.util.Set;
import lombok.Generated;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ActiveGrid {
   final Location location;
   final GridPreset option;
   final GridOrientation orientation;
   Int2ObjectOpenHashMap<InteractUnit> units = new Int2ObjectOpenHashMap();
   Set<TextDisplay> backgrounds = new HashSet<>();
   int scrollOffset;
   int waitTicks;
   boolean isDisplaying;
   final Int2ObjectOpenHashMap<ItemStack> items = new Int2ObjectOpenHashMap();
   final Int2ObjectOpenHashMap<ClickHandler> clickHandlers = new Int2ObjectOpenHashMap();

   private void checkBound(int slot) {
      if (slot < 0 || slot >= this.option.width * this.option.height) {
         throw new IndexOutOfBoundsException(slot);
      }
   }

   public void setItem(@Range(from = 0L,to = 2147483647L) int slot, ItemStack item) {
      this.checkBound(slot);
      this.items.put(slot, item);
      this.option.updateDisplayItem(this, slot, this.getUnit(slot));
   }

   public void setItemUnsafe(@Range(from = 0L,to = 2147483647L) int slot, ItemStack item) {
      this.checkBound(slot);
      this.items.put(slot, item);
   }

   public void setClickHandler(@Range(from = 0L,to = 2147483647L) int slot, ClickHandler clickHandler) {
      this.checkBound(slot);
      this.clickHandlers.put(slot, clickHandler);
   }

   @Nullable
   public InteractUnit getUnit(@Range(from = 0L,to = 2147483647L) int slot) {
      return (InteractUnit)this.units.get(slot);
   }

   @Nullable
   public ItemStack getItemInSlot(@Range(from = 0L,to = 2147483647L) int slot) {
      return (ItemStack)this.items.get(slot);
   }

   public BoundingBox getOccupiedBoundingBox() {
      return this.option.getOccupiedBoundingBox(this.location, this.orientation);
   }

   public BoundingBox getShowingBoundingBox() {
      return this.getOccupiedBoundingBox().expand(10.0);
   }

   public BoundingBox getHidingBoundingBox() {
      return this.getOccupiedBoundingBox().expand(50.0);
   }

   public String getTag() {
      return this.location.getBlockX() + ":" + this.location.getBlockY() + ":" + this.location.getBlockZ();
   }

   public void tag(Entity e) {
      e.getPersistentDataContainer().set(BIMLoader.get(this.option.plugin).getGridUtil().TAG, PersistentDataType.STRING, this.getTag());
   }

   @Generated
   public Location getLocation() {
      return this.location;
   }

   @Generated
   public GridPreset getOption() {
      return this.option;
   }

   @Generated
   public GridOrientation getOrientation() {
      return this.orientation;
   }

   @Generated
   public Int2ObjectOpenHashMap<InteractUnit> getUnits() {
      return this.units;
   }

   @Generated
   public Set<TextDisplay> getBackgrounds() {
      return this.backgrounds;
   }

   @Generated
   public int getScrollOffset() {
      return this.scrollOffset;
   }

   @Generated
   public int getWaitTicks() {
      return this.waitTicks;
   }

   @Generated
   public boolean isDisplaying() {
      return this.isDisplaying;
   }

   @Generated
   public Int2ObjectOpenHashMap<ItemStack> getItems() {
      return this.items;
   }

   @Generated
   public Int2ObjectOpenHashMap<ClickHandler> getClickHandlers() {
      return this.clickHandlers;
   }

   @Generated
   public void setUnits(Int2ObjectOpenHashMap<InteractUnit> units) {
      this.units = units;
   }

   @Generated
   public void setBackgrounds(Set<TextDisplay> backgrounds) {
      this.backgrounds = backgrounds;
   }

   @Generated
   public void setScrollOffset(int scrollOffset) {
      this.scrollOffset = scrollOffset;
   }

   @Generated
   public void setWaitTicks(int waitTicks) {
      this.waitTicks = waitTicks;
   }

   @Generated
   public void setDisplaying(boolean isDisplaying) {
      this.isDisplaying = isDisplaying;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof ActiveGrid other)) {
         return false;
      } else if (!other.canEqual(this)) {
         return false;
      } else if (this.getScrollOffset() != other.getScrollOffset()) {
         return false;
      } else if (this.getWaitTicks() != other.getWaitTicks()) {
         return false;
      } else if (this.isDisplaying() != other.isDisplaying()) {
         return false;
      } else {
         Object this$location = this.getLocation();
         Object other$location = other.getLocation();
         if (this$location == null ? other$location == null : this$location.equals(other$location)) {
            Object this$option = this.getOption();
            Object other$option = other.getOption();
            if (this$option == null ? other$option == null : this$option.equals(other$option)) {
               Object this$orientation = this.getOrientation();
               Object other$orientation = other.getOrientation();
               if (this$orientation == null ? other$orientation == null : this$orientation.equals(other$orientation)) {
                  Object this$units = this.getUnits();
                  Object other$units = other.getUnits();
                  if (this$units == null ? other$units == null : this$units.equals(other$units)) {
                     Object this$backgrounds = this.getBackgrounds();
                     Object other$backgrounds = other.getBackgrounds();
                     if (this$backgrounds == null ? other$backgrounds == null : this$backgrounds.equals(other$backgrounds)) {
                        Object this$items = this.getItems();
                        Object other$items = other.getItems();
                        if (this$items == null ? other$items == null : this$items.equals(other$items)) {
                           Object this$clickHandlers = this.getClickHandlers();
                           Object other$clickHandlers = other.getClickHandlers();
                           return this$clickHandlers == null ? other$clickHandlers == null : this$clickHandlers.equals(other$clickHandlers);
                        } else {
                           return false;
                        }
                     } else {
                        return false;
                     }
                  } else {
                     return false;
                  }
               } else {
                  return false;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof ActiveGrid;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      result = result * 59 + this.getScrollOffset();
      result = result * 59 + this.getWaitTicks();
      result = result * 59 + (this.isDisplaying() ? 79 : 97);
      Object $location = this.getLocation();
      result = result * 59 + ($location == null ? 43 : $location.hashCode());
      Object $option = this.getOption();
      result = result * 59 + ($option == null ? 43 : $option.hashCode());
      Object $orientation = this.getOrientation();
      result = result * 59 + ($orientation == null ? 43 : $orientation.hashCode());
      Object $units = this.getUnits();
      result = result * 59 + ($units == null ? 43 : $units.hashCode());
      Object $backgrounds = this.getBackgrounds();
      result = result * 59 + ($backgrounds == null ? 43 : $backgrounds.hashCode());
      Object $items = this.getItems();
      result = result * 59 + ($items == null ? 43 : $items.hashCode());
      Object $clickHandlers = this.getClickHandlers();
      return result * 59 + ($clickHandlers == null ? 43 : $clickHandlers.hashCode());
   }

   @Generated
   @Override
   public String toString() {
      return "ActiveGrid(location="
         + this.getLocation()
         + ", option="
         + this.getOption()
         + ", orientation="
         + this.getOrientation()
         + ", units="
         + this.getUnits()
         + ", backgrounds="
         + this.getBackgrounds()
         + ", scrollOffset="
         + this.getScrollOffset()
         + ", waitTicks="
         + this.getWaitTicks()
         + ", isDisplaying="
         + this.isDisplaying()
         + ", items="
         + this.getItems()
         + ", clickHandlers="
         + this.getClickHandlers()
         + ")";
   }

   @Generated
   ActiveGrid(Location location, GridPreset option, GridOrientation orientation) {
      this.location = location;
      this.option = option;
      this.orientation = orientation;
   }
}
