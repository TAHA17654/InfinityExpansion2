package com.jeff_media.morepersistentdatatypes.datatypes.serializable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Array;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.NotNull;

public class ConfigurationSerializableArrayDataType<T extends ConfigurationSerializable> implements PersistentDataType<byte[], T[]> {
   private final Class<T> type;
   private final Class<T[]> types;

   public ConfigurationSerializableArrayDataType(Class<T[]> types) {
      this.type = (Class<T>)types.getComponentType();
      this.types = types;
   }

   @NotNull
   public Class<byte[]> getPrimitiveType() {
      return byte[].class;
   }

   @NotNull
   public Class<T[]> getComplexType() {
      return this.types;
   }

   @NotNull
   public byte[] toPrimitive(@NotNull T[] serializable, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
      try {
         ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

         byte[] var14;
         try {
            BukkitObjectOutputStream bukkitObjectOutputStream = new BukkitObjectOutputStream(outputStream);

            try {
               bukkitObjectOutputStream.writeInt(serializable.length);

               for (T t : serializable) {
                  bukkitObjectOutputStream.writeObject(t);
               }

               var14 = outputStream.toByteArray();
            } catch (Throwable var11) {
               try {
                  bukkitObjectOutputStream.close();
               } catch (Throwable var10) {
                  var11.addSuppressed(var10);
               }

               throw var11;
            }

            bukkitObjectOutputStream.close();
         } catch (Throwable var12) {
            try {
               outputStream.close();
            } catch (Throwable var9) {
               var12.addSuppressed(var9);
            }

            throw var12;
         }

         outputStream.close();
         return var14;
      } catch (IOException var13) {
         throw new UncheckedIOException(
            ConfigurationSerializableDataType.getExceptionMessage(this.type, ConfigurationSerializableDataType.SerializationType.SERIALIZATION), var13
         );
      }
   }

   @NotNull
   public T[] fromPrimitive(@NotNull byte[] bytes, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
      try {
         ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);

         ConfigurationSerializable[] var14;
         try {
            BukkitObjectInputStream bukkitObjectInputStream = new BukkitObjectInputStream(inputStream);

            try {
               T[] ts = (T[])((ConfigurationSerializable[])Array.newInstance(this.type, bukkitObjectInputStream.readInt()));

               for (int i = 0; i < ts.length; i++) {
                  ts[i] = (T)bukkitObjectInputStream.readObject();
               }

               var14 = ts;
            } catch (Throwable var9) {
               try {
                  bukkitObjectInputStream.close();
               } catch (Throwable var8) {
                  var9.addSuppressed(var8);
               }

               throw var9;
            }

            bukkitObjectInputStream.close();
         } catch (Throwable var10) {
            try {
               inputStream.close();
            } catch (Throwable var7) {
               var10.addSuppressed(var7);
            }

            throw var10;
         }

         inputStream.close();
         return (T[])var14;
      } catch (EOFException var11) {
         return (T[])((ConfigurationSerializable[])Array.newInstance(this.getComplexType().getComponentType(), 0));
      } catch (IOException var12) {
         throw new UncheckedIOException(
            ConfigurationSerializableDataType.getExceptionMessage(this.type, ConfigurationSerializableDataType.SerializationType.DESERIALIZATION), var12
         );
      } catch (ClassNotFoundException var13) {
         throw new RuntimeException(
            ConfigurationSerializableDataType.getExceptionMessage(this.type, ConfigurationSerializableDataType.SerializationType.DESERIALIZATION), var13
         );
      }
   }
}
