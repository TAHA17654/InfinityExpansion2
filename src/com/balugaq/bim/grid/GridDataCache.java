package com.balugaq.bim.grid;

import com.balugaq.bim.general.BlockPos;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Generated;
import org.bukkit.entity.Entity;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class GridDataCache {
   private final Map<BlockPos, ActiveGrid> activeGrids = new HashMap<>();
   private final Map<Entity, InteractUnit> index = new HashMap<>();
   private final Map<UUID, InteractUnit> watching = new HashMap<>();

   public Map<BlockPos, ActiveGrid> activeGrids() {
      return this.activeGrids;
   }

   public Map<Entity, InteractUnit> index() {
      return this.index;
   }

   public Map<UUID, InteractUnit> watching() {
      return this.watching;
   }

   @Generated
   public Map<BlockPos, ActiveGrid> getActiveGrids() {
      return this.activeGrids;
   }

   @Generated
   public Map<Entity, InteractUnit> getIndex() {
      return this.index;
   }

   @Generated
   public Map<UUID, InteractUnit> getWatching() {
      return this.watching;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof GridDataCache other)) {
         return false;
      } else if (!other.canEqual(this)) {
         return false;
      } else {
         Object this$activeGrids = this.getActiveGrids();
         Object other$activeGrids = other.getActiveGrids();
         if (this$activeGrids == null ? other$activeGrids == null : this$activeGrids.equals(other$activeGrids)) {
            Object this$index = this.getIndex();
            Object other$index = other.getIndex();
            if (this$index == null ? other$index == null : this$index.equals(other$index)) {
               Object this$watching = this.getWatching();
               Object other$watching = other.getWatching();
               return this$watching == null ? other$watching == null : this$watching.equals(other$watching);
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
      return other instanceof GridDataCache;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $activeGrids = this.getActiveGrids();
      result = result * 59 + ($activeGrids == null ? 43 : $activeGrids.hashCode());
      Object $index = this.getIndex();
      result = result * 59 + ($index == null ? 43 : $index.hashCode());
      Object $watching = this.getWatching();
      return result * 59 + ($watching == null ? 43 : $watching.hashCode());
   }

   @Generated
   @Override
   public String toString() {
      return "GridDataCache(activeGrids=" + this.getActiveGrids() + ", index=" + this.getIndex() + ", watching=" + this.getWatching() + ")";
   }
}
