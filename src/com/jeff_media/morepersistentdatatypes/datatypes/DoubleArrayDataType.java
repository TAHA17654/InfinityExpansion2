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

public class DoubleArrayDataType implements PersistentDataType<byte[], double[]> {
   @NotNull
   public Class<byte[]> getPrimitiveType() {
      return byte[].class;
   }

   @NotNull
   public Class<double[]> getComplexType() {
      return double[].class;
   }

   @NotNull
   public byte[] toPrimitive(double[] doubles, @NotNull PersistentDataAdapterContext itemTagAdapterContext) {
      try {
         ByteArrayOutputStream bos = new ByteArrayOutputStream();

         byte[] var15;
         try {
            DataOutputStream dos = new DataOutputStream(bos);

            try {
               dos.writeInt(doubles.length);

               for (double number : doubles) {
                  dos.writeDouble(number);
               }

               dos.flush();
               var15 = bos.toByteArray();
            } catch (Throwable var12) {
               try {
                  dos.close();
               } catch (Throwable var11) {
                  var12.addSuppressed(var11);
               }

               throw var12;
            }

            dos.close();
         } catch (Throwable var13) {
            try {
               bos.close();
            } catch (Throwable var10) {
               var13.addSuppressed(var10);
            }

            throw var13;
         }

         bos.close();
         return var15;
      } catch (IOException var14) {
         throw new UncheckedIOException(var14);
      }
   }

   @NotNull
   public double[] fromPrimitive(@NotNull byte[] bytes, @NotNull PersistentDataAdapterContext itemTagAdapterContext) {
      try {
         ByteArrayInputStream bis = new ByteArrayInputStream(bytes);

         double[] var12;
         try {
            DataInputStream dis = new DataInputStream(bis);

            try {
               double[] doubles = new double[dis.readInt()];

               for (int i = 0; i < doubles.length; i++) {
                  doubles[i] = dis.readDouble();
               }

               var12 = doubles;
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
