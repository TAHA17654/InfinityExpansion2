package dev.sefiraat.sefilib.dough.data.persistent;

import java.util.UUID;
import javax.annotation.Nonnull;
import org.apache.commons.lang.Validate;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;

public final class PersistentUUIDDataType implements PersistentDataType<int[], UUID> {
   public static final PersistentDataType<int[], UUID> TYPE = new PersistentUUIDDataType();

   private PersistentUUIDDataType() {
   }

   @Nonnull
   public Class<int[]> getPrimitiveType() {
      return int[].class;
   }

   @Nonnull
   public Class<UUID> getComplexType() {
      return UUID.class;
   }

   @Nonnull
   public int[] toPrimitive(@Nonnull UUID complex, @Nonnull PersistentDataAdapterContext context) {
      return toIntArray(complex);
   }

   @Nonnull
   public UUID fromPrimitive(@Nonnull int[] primitive, @Nonnull PersistentDataAdapterContext context) {
      return fromIntArray(primitive);
   }

   @Nonnull
   public static UUID fromIntArray(@Nonnull int[] ints) {
      Validate.notNull(ints, "The provided integer array cannot be null!");
      Validate.isTrue(ints.length == 4, "The integer array must have a length of 4.");
      return new UUID((long)ints[0] | (long)ints[1] & 4294967295L, (long)ints[2] | (long)ints[3] & 4294967295L);
   }

   @Nonnull
   public static int[] toIntArray(@Nonnull UUID uuid) {
      Validate.notNull(uuid, "The provided uuid cannot be null!");
      long mostSig = uuid.getMostSignificantBits();
      long leastSig = uuid.getLeastSignificantBits();
      return new int[]{(int)(mostSig >> 32), (int)mostSig, (int)(leastSig >> 32), (int)leastSig};
   }
}
