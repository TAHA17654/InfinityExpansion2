package io.github.sefiraat.networks.utils;

import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.HashSet;
import java.util.Set;
import lombok.Generated;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.Internal;

public class MatchOption {
   public static final long IGNORE_DURABILITY = 1L;
   public static final long IGNORE_ENCHANTMENT = 2L;
   public static final long IGNORE_ATTRIBUTE = 4L;
   public static final long IGNORE_LORE = 8L;
   public static final long IGNORE_CUSTOM_MODEL_DATA = 16L;
   @Internal
   public static final long IGNORE_AMOUNT = 32L;
   private static final long IGNORE_MASK = 63L;
   public static final long NO_IGNORE = 0L;
   public static final MatchOption DEFAULT = new MatchOption(48L);
   private final long ignoreFlags;
   public static final Long2ObjectOpenHashMap<Set<DataComponentType>> cacheSets = new Long2ObjectOpenHashMap();

   private MatchOption(long ignoreFlags) {
      this.ignoreFlags = ignoreFlags & 63L;
   }

   @NotNull
   public static MatchOption of(long ignoreFlags) {
      return new MatchOption(ignoreFlags);
   }

   @NotNull
   public MatchOption withIgnore(long flag, boolean ignored) {
      long flags = ignored ? this.ignoreFlags | flag : this.ignoreFlags & ~flag;
      return new MatchOption(flags);
   }

   public boolean isIgnoreAmount() {
      return (this.ignoreFlags & 32L) != 0L;
   }

   public boolean isIgnoreDurability() {
      return (this.ignoreFlags & 1L) != 0L;
   }

   public boolean isIgnoreEnchantment() {
      return (this.ignoreFlags & 2L) != 0L;
   }

   public boolean isIgnoreAttribute() {
      return (this.ignoreFlags & 4L) != 0L;
   }

   public boolean isIgnoreLore() {
      return (this.ignoreFlags & 8L) != 0L;
   }

   public boolean isIgnoreCustomModelData() {
      return (this.ignoreFlags & 16L) != 0L;
   }

   public boolean isEmpty() {
      return this.ignoreFlags == 0L;
   }

   public boolean isAnyIgnore() {
      return this.ignoreFlags != 0L;
   }

   public Set<DataComponentType> toDataComponentSet() {
      Set<DataComponentType> set = (Set<DataComponentType>)cacheSets.get(this.ignoreFlags);
      if (set != null) {
         return set;
      } else {
         Set<DataComponentType> var2 = new HashSet();
         var2.add(DataComponentTypes.LORE);
         if (this.isIgnoreAttribute()) {
            var2.add(DataComponentTypes.ATTRIBUTE_MODIFIERS);
         }

         if (this.isIgnoreDurability()) {
            var2.add(DataComponentTypes.DAMAGE);
         }

         if (this.isIgnoreEnchantment()) {
            var2.add(DataComponentTypes.ENCHANTMENTS);
         }

         if (this.isIgnoreCustomModelData()) {
            var2.add(DataComponentTypes.CUSTOM_MODEL_DATA);
         }

         cacheSets.put(this.ignoreFlags, var2);
         return var2;
      }
   }

   @Generated
   public long getIgnoreFlags() {
      return this.ignoreFlags;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof MatchOption other)) {
         return false;
      } else {
         return !other.canEqual(this) ? false : this.getIgnoreFlags() == other.getIgnoreFlags();
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof MatchOption;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      long $ignoreFlags = this.getIgnoreFlags();
      return result * 59 + (int)($ignoreFlags >>> 32 ^ $ignoreFlags);
   }

   @Generated
   @Override
   public String toString() {
      return "MatchOption(ignoreFlags=" + this.getIgnoreFlags() + ")";
   }

   static {
      cacheSets.defaultReturnValue(null);
   }
}
