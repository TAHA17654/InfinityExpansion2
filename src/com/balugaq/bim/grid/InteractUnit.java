package com.balugaq.bim.grid;

import lombok.Generated;
import org.bukkit.Location;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class InteractUnit {
   final int idx;
   final Location location;
   final ActiveGrid grid;
   ItemDisplay itemDisplay;
   TextDisplay titleDisplay;
   TextDisplay amountDisplay;

   @Override
   public String toString() {
      return "InteractUnit{idx="
         + this.idx
         + ", location="
         + this.location
         + ", grid.option="
         + this.grid.option
         + ", itemDisplay="
         + this.itemDisplay
         + ", titleDisplay="
         + this.titleDisplay
         + ", amountDisplay="
         + this.amountDisplay
         + "}";
   }

   @Generated
   public int getIdx() {
      return this.idx;
   }

   @Generated
   public Location getLocation() {
      return this.location;
   }

   @Generated
   public ActiveGrid getGrid() {
      return this.grid;
   }

   @Generated
   public ItemDisplay getItemDisplay() {
      return this.itemDisplay;
   }

   @Generated
   public TextDisplay getTitleDisplay() {
      return this.titleDisplay;
   }

   @Generated
   public TextDisplay getAmountDisplay() {
      return this.amountDisplay;
   }

   @Generated
   public void setItemDisplay(ItemDisplay itemDisplay) {
      this.itemDisplay = itemDisplay;
   }

   @Generated
   public void setTitleDisplay(TextDisplay titleDisplay) {
      this.titleDisplay = titleDisplay;
   }

   @Generated
   public void setAmountDisplay(TextDisplay amountDisplay) {
      this.amountDisplay = amountDisplay;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof InteractUnit other)) {
         return false;
      } else if (!other.canEqual(this)) {
         return false;
      } else if (this.getIdx() != other.getIdx()) {
         return false;
      } else {
         Object this$location = this.getLocation();
         Object other$location = other.getLocation();
         if (this$location == null ? other$location == null : this$location.equals(other$location)) {
            Object this$grid = this.getGrid();
            Object other$grid = other.getGrid();
            if (this$grid == null ? other$grid == null : this$grid.equals(other$grid)) {
               Object this$itemDisplay = this.getItemDisplay();
               Object other$itemDisplay = other.getItemDisplay();
               if (this$itemDisplay == null ? other$itemDisplay == null : this$itemDisplay.equals(other$itemDisplay)) {
                  Object this$titleDisplay = this.getTitleDisplay();
                  Object other$titleDisplay = other.getTitleDisplay();
                  if (this$titleDisplay == null ? other$titleDisplay == null : this$titleDisplay.equals(other$titleDisplay)) {
                     Object this$amountDisplay = this.getAmountDisplay();
                     Object other$amountDisplay = other.getAmountDisplay();
                     return this$amountDisplay == null ? other$amountDisplay == null : this$amountDisplay.equals(other$amountDisplay);
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
      return other instanceof InteractUnit;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      result = result * 59 + this.getIdx();
      Object $location = this.getLocation();
      result = result * 59 + ($location == null ? 43 : $location.hashCode());
      Object $grid = this.getGrid();
      result = result * 59 + ($grid == null ? 43 : $grid.hashCode());
      Object $itemDisplay = this.getItemDisplay();
      result = result * 59 + ($itemDisplay == null ? 43 : $itemDisplay.hashCode());
      Object $titleDisplay = this.getTitleDisplay();
      result = result * 59 + ($titleDisplay == null ? 43 : $titleDisplay.hashCode());
      Object $amountDisplay = this.getAmountDisplay();
      return result * 59 + ($amountDisplay == null ? 43 : $amountDisplay.hashCode());
   }

   @Generated
   public InteractUnit(int idx, Location location, ActiveGrid grid) {
      this.idx = idx;
      this.location = location;
      this.grid = grid;
   }
}
