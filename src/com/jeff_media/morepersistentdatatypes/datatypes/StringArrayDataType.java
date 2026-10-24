package com.jeff_media.morepersistentdatatypes.datatypes;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class StringArrayDataType implements PersistentDataType<byte[], String[]> {
   private final Charset charset;

   public StringArrayDataType(Charset charset) {
      this.charset = charset;
   }

   @NotNull
   public Class<byte[]> getPrimitiveType() {
      return byte[].class;
   }

   @NotNull
   public Class<String[]> getComplexType() {
      return String[].class;
   }

   @NotNull
   public byte[] toPrimitive(String[] strings, @NotNull PersistentDataAdapterContext context) {
      byte[][] allStringBytes = new byte[strings.length][];
      int total = 0;

      for (int i = 0; i < allStringBytes.length; i++) {
         byte[] bytes = strings[i].getBytes(this.charset);
         allStringBytes[i] = bytes;
         total += bytes.length;
      }

      ByteBuffer buffer = ByteBuffer.allocate(total + allStringBytes.length * 4);

      for (byte[] bytes : allStringBytes) {
         buffer.putInt(bytes.length);
         buffer.put(bytes);
      }

      return buffer.array();
   }

   @NotNull
   public String[] fromPrimitive(@NotNull byte[] bytes, @NotNull PersistentDataAdapterContext itemTagAdapterContext) {
      ByteBuffer buffer = ByteBuffer.wrap(bytes);
      List<String> list = new ArrayList<>();

      while (buffer.remaining() > 0 && buffer.remaining() >= 4) {
         int stringLength = buffer.getInt();
         if (buffer.remaining() < stringLength) {
            break;
         }

         byte[] stringBytes = new byte[stringLength];
         buffer.get(stringBytes);
         list.add(new String(stringBytes, this.charset));
      }

      return list.toArray(new String[0]);
   }
}
