package com.jeff_media.morepersistentdatatypes.datatypes;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class BooleanArrayDataType implements PersistentDataType<byte[], boolean[]> {
   @NotNull
   public Class<byte[]> getPrimitiveType() {
      return byte[].class;
   }

   @NotNull
   public Class<boolean[]> getComplexType() {
      return boolean[].class;
   }

   @NotNull
   public byte[] toPrimitive(boolean[] booleans, @NotNull PersistentDataAdapterContext context) {
      byte[] bytes = new byte[booleans.length];

      for (int i = 0; i < booleans.length; i++) {
         bytes[i] = (byte)(booleans[i] ? 1 : 0);
      }

      return bytes;
   }

   @NotNull
   public boolean[] fromPrimitive(byte[] bytes, @NotNull PersistentDataAdapterContext context) {
      boolean[] booleans = new boolean[bytes.length];

      for (int i = 0; i < bytes.length; i++) {
         booleans[i] = bytes[i] != 0;
      }

      return booleans;
   }
}
