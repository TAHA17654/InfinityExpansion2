package com.jeff_media.morepersistentdatatypes.datatypes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class FloatArrayDataType implements PersistentDataType<byte[], float[]> {
   @NotNull
   public Class<byte[]> getPrimitiveType() {
      return byte[].class;
   }

   @NotNull
   public Class<float[]> getComplexType() {
      return float[].class;
   }

   @NotNull
   public byte[] toPrimitive(float[] floats, @NotNull PersistentDataAdapterContext itemTagAdapterContext) {
      try {
         ByteArrayOutputStream bos = new ByteArrayOutputStream();

         byte[] var14;
         try {
            DataOutputStream dos = new DataOutputStream(bos);

            try {
               dos.writeInt(floats.length);

               for (float number : floats) {
                  dos.writeFloat(number);
               }

               dos.flush();
               var14 = bos.toByteArray();
            } catch (Throwable var11) {
               try {
                  dos.close();
               } catch (Throwable var10) {
                  var11.addSuppressed(var10);
               }

               throw var11;
            }

            dos.close();
         } catch (Throwable var12) {
            try {
               bos.close();
            } catch (Throwable var9) {
               var12.addSuppressed(var9);
            }

            throw var12;
         }

         bos.close();
         return var14;
      } catch (IOException var13) {
         throw new UncheckedIOException(var13);
      }
   }

   @NotNull
   public float[] fromPrimitive(@NotNull byte[] bytes, @NotNull PersistentDataAdapterContext itemTagAdapterContext) {
      try {
         ByteArrayInputStream bis = new ByteArrayInputStream(bytes);

         float[] var12;
         try {
            DataInputStream dis = new DataInputStream(bis);

            try {
               float[] floats = new float[dis.readInt()];

               for (int i = 0; i < floats.length; i++) {
                  floats[i] = dis.readFloat();
               }

               var12 = floats;
            } catch (Throwable var9) {
               try {
                  dis.close();
               } catch (Throwable var8) {
                  var9.addSuppressed(var8);
               }

               throw var9;
            }

            dis.close();
         } catch (Throwable var10) {
            try {
               bis.close();
            } catch (Throwable var7) {
               var10.addSuppressed(var7);
            }

            throw var10;
         }

         bis.close();
         return var12;
      } catch (IOException var11) {
         throw new UncheckedIOException(var11);
      }
   }
}
