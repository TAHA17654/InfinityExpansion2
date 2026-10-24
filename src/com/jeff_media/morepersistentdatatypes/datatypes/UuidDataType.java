package com.jeff_media.morepersistentdatatypes.datatypes;

import java.nio.ByteBuffer;
import java.util.UUID;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class UuidDataType implements PersistentDataType<byte[], UUID> {
   @NotNull
   public Class<byte[]> getPrimitiveType() {
      return byte[].class;
   }

   @NotNull
   public Class<UUID> getComplexType() {
      return UUID.class;
   }

   @NotNull
   public byte[] toPrimitive(UUID complex, @NotNull PersistentDataAdapterContext context) {
      ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
      bb.putLong(complex.getMostSignificantBits());
      bb.putLong(complex.getLeastSignificantBits());
      return bb.array();
   }

   @NotNull
   public UUID fromPrimitive(@NotNull byte[] primitive, @NotNull PersistentDataAdapterContext context) {
      ByteBuffer bb = ByteBuffer.wrap(primitive);
      long firstLong = bb.getLong();
      long secondLong = bb.getLong();
      return new UUID(firstLong, secondLong);
   }
}
