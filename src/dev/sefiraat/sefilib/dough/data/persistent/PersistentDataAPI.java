package dev.sefiraat.sefilib.dough.data.persistent;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

@ParametersAreNonnullByDefault
public final class PersistentDataAPI {
   private PersistentDataAPI() {
   }

   public static void setByte(PersistentDataHolder holder, NamespacedKey key, byte value) {
      holder.getPersistentDataContainer().set(key, PersistentDataType.BYTE, value);
   }

   public static void setShort(PersistentDataHolder holder, NamespacedKey key, short value) {
      holder.getPersistentDataContainer().set(key, PersistentDataType.SHORT, value);
   }

   public static void setInt(PersistentDataHolder holder, NamespacedKey key, int value) {
      holder.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, value);
   }

   public static void setLong(PersistentDataHolder holder, NamespacedKey key, long value) {
      holder.getPersistentDataContainer().set(key, PersistentDataType.LONG, value);
   }

   public static void setFloat(PersistentDataHolder holder, NamespacedKey key, float value) {
      holder.getPersistentDataContainer().set(key, PersistentDataType.FLOAT, value);
   }

   public static void setDouble(PersistentDataHolder holder, NamespacedKey key, double value) {
      holder.getPersistentDataContainer().set(key, PersistentDataType.DOUBLE, value);
   }

   public static void setString(PersistentDataHolder holder, NamespacedKey key, String value) {
      holder.getPersistentDataContainer().set(key, PersistentDataType.STRING, value);
   }

   public static void setByteArray(PersistentDataHolder holder, NamespacedKey key, byte... value) {
      holder.getPersistentDataContainer().set(key, PersistentDataType.BYTE_ARRAY, value);
   }

   public static void setIntArray(PersistentDataHolder holder, NamespacedKey key, int... value) {
      holder.getPersistentDataContainer().set(key, PersistentDataType.INTEGER_ARRAY, value);
   }

   public static void setLongArray(PersistentDataHolder holder, NamespacedKey key, long... value) {
      holder.getPersistentDataContainer().set(key, PersistentDataType.LONG_ARRAY, value);
   }

   public static void setContainer(PersistentDataHolder holder, NamespacedKey key, PersistentDataContainer value) {
      holder.getPersistentDataContainer().set(key, PersistentDataType.TAG_CONTAINER, value);
   }

   public static void setBoolean(PersistentDataHolder holder, NamespacedKey key, boolean value) {
      setByte(holder, key, (byte)(value ? 1 : 0));
   }

   public static void setJsonObject(PersistentDataHolder holder, NamespacedKey key, JsonObject value) {
      holder.getPersistentDataContainer().set(key, PersistentJsonDataType.JSON_OBJECT, value);
   }

   public static void setJsonArray(PersistentDataHolder holder, NamespacedKey key, JsonArray value) {
      holder.getPersistentDataContainer().set(key, PersistentJsonDataType.JSON_ARRAY, value);
   }

   public static void setUUID(@Nonnull PersistentDataHolder holder, @Nonnull NamespacedKey key, @Nonnull UUID uuid) {
      holder.getPersistentDataContainer().set(key, PersistentUUIDDataType.TYPE, uuid);
   }

   @ParametersAreNonnullByDefault
   public static <T, Z> void set(PersistentDataHolder holder, NamespacedKey key, PersistentDataType<T, Z> type, Z obj) {
      holder.getPersistentDataContainer().set(key, type, obj);
   }

   public static boolean hasByte(PersistentDataHolder holder, NamespacedKey key) {
      return holder.getPersistentDataContainer().has(key, PersistentDataType.BYTE);
   }

   public static boolean hasShort(PersistentDataHolder holder, NamespacedKey key) {
      return holder.getPersistentDataContainer().has(key, PersistentDataType.SHORT);
   }

   public static boolean hasInt(PersistentDataHolder holder, NamespacedKey key) {
      return holder.getPersistentDataContainer().has(key, PersistentDataType.INTEGER);
   }

   public static boolean hasLong(PersistentDataHolder holder, NamespacedKey key) {
      return holder.getPersistentDataContainer().has(key, PersistentDataType.LONG);
   }

   public static boolean hasFloat(PersistentDataHolder holder, NamespacedKey key) {
      return holder.getPersistentDataContainer().has(key, PersistentDataType.FLOAT);
   }

   public static boolean hasDouble(PersistentDataHolder holder, NamespacedKey key) {
      return holder.getPersistentDataContainer().has(key, PersistentDataType.DOUBLE);
   }

   public static boolean hasString(PersistentDataHolder holder, NamespacedKey key) {
      return holder.getPersistentDataContainer().has(key, PersistentDataType.STRING);
   }

   public static boolean hasByteArray(PersistentDataHolder holder, NamespacedKey key) {
      return holder.getPersistentDataContainer().has(key, PersistentDataType.BYTE_ARRAY);
   }

   public static boolean hasIntArray(PersistentDataHolder holder, NamespacedKey key) {
      return holder.getPersistentDataContainer().has(key, PersistentDataType.INTEGER_ARRAY);
   }

   public static boolean hasLongArray(PersistentDataHolder holder, NamespacedKey key) {
      return holder.getPersistentDataContainer().has(key, PersistentDataType.LONG_ARRAY);
   }

   public static boolean hasContainer(PersistentDataHolder holder, NamespacedKey key) {
      return holder.getPersistentDataContainer().has(key, PersistentDataType.TAG_CONTAINER);
   }

   public static boolean hasBoolean(PersistentDataHolder holder, NamespacedKey key) {
      if (!holder.getPersistentDataContainer().has(key, PersistentDataType.BYTE)) {
         return false;
      } else {
         byte value = getByte(holder, key);
         return value == 0 || value == 1;
      }
   }

   public static boolean hasJsonObject(PersistentDataHolder holder, NamespacedKey key) {
      return holder.getPersistentDataContainer().has(key, PersistentJsonDataType.JSON_OBJECT);
   }

   public static boolean hasJsonArray(PersistentDataHolder holder, NamespacedKey key) {
      return holder.getPersistentDataContainer().has(key, PersistentJsonDataType.JSON_ARRAY);
   }

   public static boolean hasUUID(@Nonnull PersistentDataHolder holder, @Nonnull NamespacedKey key) {
      return holder.getPersistentDataContainer().has(key, PersistentUUIDDataType.TYPE);
   }

   @ParametersAreNonnullByDefault
   public static <T, Z> boolean has(PersistentDataHolder holder, NamespacedKey key, PersistentDataType<T, Z> type) {
      return holder.getPersistentDataContainer().has(key, type);
   }

   public static byte getByte(PersistentDataHolder holder, NamespacedKey key) {
      return (Byte)holder.getPersistentDataContainer().getOrDefault(key, PersistentDataType.BYTE, (byte)-1);
   }

   public static Optional<Byte> getOptionalByte(PersistentDataHolder holder, NamespacedKey key) {
      return !hasByte(holder, key) ? Optional.empty() : Optional.of(getByte(holder, key));
   }

   public static byte getByte(PersistentDataHolder holder, NamespacedKey key, byte defaultVal) {
      return (Byte)holder.getPersistentDataContainer().getOrDefault(key, PersistentDataType.BYTE, defaultVal);
   }

   public static Optional<Boolean> getOptionalBoolean(PersistentDataHolder holder, NamespacedKey key) {
      Byte b = (Byte)holder.getPersistentDataContainer().get(key, PersistentDataType.BYTE);
      return b == null ? Optional.empty() : Optional.of(b == 1);
   }

   public static boolean getBoolean(PersistentDataHolder holder, NamespacedKey key) {
      return getOptionalBoolean(holder, key).orElse(false);
   }

   public static short getShort(PersistentDataHolder holder, NamespacedKey key) {
      return (Short)holder.getPersistentDataContainer().getOrDefault(key, PersistentDataType.SHORT, Short.valueOf((short)-1));
   }

   public static Optional<Short> getOptionalShort(PersistentDataHolder holder, NamespacedKey key) {
      return !hasShort(holder, key) ? Optional.empty() : Optional.of(getShort(holder, key));
   }

   public static short getShort(PersistentDataHolder holder, NamespacedKey key, short defaultVal) {
      return (Short)holder.getPersistentDataContainer().getOrDefault(key, PersistentDataType.SHORT, defaultVal);
   }

   public static int getInt(PersistentDataHolder holder, NamespacedKey key) {
      return (Integer)holder.getPersistentDataContainer().getOrDefault(key, PersistentDataType.INTEGER, -1);
   }

   public static OptionalInt getOptionalInt(PersistentDataHolder holder, NamespacedKey key) {
      return !hasInt(holder, key) ? OptionalInt.empty() : OptionalInt.of(getInt(holder, key));
   }

   public static int getInt(PersistentDataHolder holder, NamespacedKey key, int defaultVal) {
      return (Integer)holder.getPersistentDataContainer().getOrDefault(key, PersistentDataType.INTEGER, defaultVal);
   }

   public static long getLong(PersistentDataHolder holder, NamespacedKey key) {
      return (Long)holder.getPersistentDataContainer().getOrDefault(key, PersistentDataType.LONG, -1L);
   }

   public static OptionalLong getOptionalLong(PersistentDataHolder holder, NamespacedKey key) {
      return !hasLong(holder, key) ? OptionalLong.empty() : OptionalLong.of(getLong(holder, key));
   }

   public static long getLong(PersistentDataHolder holder, NamespacedKey key, long defaultVal) {
      return (Long)holder.getPersistentDataContainer().getOrDefault(key, PersistentDataType.LONG, defaultVal);
   }

   public static float getFloat(PersistentDataHolder holder, NamespacedKey key) {
      return (Float)holder.getPersistentDataContainer().getOrDefault(key, PersistentDataType.FLOAT, -1.0F);
   }

   public static Optional<Float> getOptionalFloat(PersistentDataHolder holder, NamespacedKey key) {
      return !hasFloat(holder, key) ? Optional.empty() : Optional.of(getFloat(holder, key));
   }

   public static float getFloat(PersistentDataHolder holder, NamespacedKey key, float defaultVal) {
      return (Float)holder.getPersistentDataContainer().getOrDefault(key, PersistentDataType.FLOAT, defaultVal);
   }

   public static double getDouble(PersistentDataHolder holder, NamespacedKey key) {
      return (Double)holder.getPersistentDataContainer().getOrDefault(key, PersistentDataType.DOUBLE, -1.0);
   }

   public static OptionalDouble getOptionalDouble(PersistentDataHolder holder, NamespacedKey key) {
      return !hasDouble(holder, key) ? OptionalDouble.empty() : OptionalDouble.of(getDouble(holder, key));
   }

   public static double getDouble(PersistentDataHolder holder, NamespacedKey key, double defaultVal) {
      return (Double)holder.getPersistentDataContainer().getOrDefault(key, PersistentDataType.DOUBLE, defaultVal);
   }

   public static String getString(PersistentDataHolder holder, NamespacedKey key) {
      return (String)holder.getPersistentDataContainer().get(key, PersistentDataType.STRING);
   }

   public static Optional<String> getOptionalString(PersistentDataHolder holder, NamespacedKey key) {
      return Optional.ofNullable(getString(holder, key));
   }

   public static String getString(PersistentDataHolder holder, NamespacedKey key, String defaultVal) {
      return (String)holder.getPersistentDataContainer().getOrDefault(key, PersistentDataType.STRING, defaultVal);
   }

   public static byte[] getByteArray(PersistentDataHolder holder, NamespacedKey key) {
      return (byte[])holder.getPersistentDataContainer().get(key, PersistentDataType.BYTE_ARRAY);
   }

   public static Optional<byte[]> getOptionalByteArray(PersistentDataHolder holder, NamespacedKey key) {
      return Optional.ofNullable(getByteArray(holder, key));
   }

   public static byte[] getByteArray(PersistentDataHolder holder, NamespacedKey key, byte... defaultVal) {
      return (byte[])holder.getPersistentDataContainer().getOrDefault(key, PersistentDataType.BYTE_ARRAY, defaultVal);
   }

   public static int[] getIntArray(PersistentDataHolder holder, NamespacedKey key) {
      return (int[])holder.getPersistentDataContainer().get(key, PersistentDataType.INTEGER_ARRAY);
   }

   public static Optional<int[]> getOptionalIntArray(PersistentDataHolder holder, NamespacedKey key) {
      return Optional.ofNullable(getIntArray(holder, key));
   }

   public static int[] getIntArray(PersistentDataHolder holder, NamespacedKey key, int... defaultVal) {
      return (int[])holder.getPersistentDataContainer().getOrDefault(key, PersistentDataType.INTEGER_ARRAY, defaultVal);
   }

   public static long[] getLongArray(PersistentDataHolder holder, NamespacedKey key) {
      return (long[])holder.getPersistentDataContainer().get(key, PersistentDataType.LONG_ARRAY);
   }

   public static Optional<long[]> getOptionalLongArray(PersistentDataHolder holder, NamespacedKey key) {
      return Optional.ofNullable(getLongArray(holder, key));
   }

   public static long[] getLongArray(PersistentDataHolder holder, NamespacedKey key, long... defaultVal) {
      return (long[])holder.getPersistentDataContainer().getOrDefault(key, PersistentDataType.LONG_ARRAY, defaultVal);
   }

   public static PersistentDataContainer getContainer(PersistentDataHolder holder, NamespacedKey key) {
      return (PersistentDataContainer)holder.getPersistentDataContainer().get(key, PersistentDataType.TAG_CONTAINER);
   }

   public static Optional<PersistentDataContainer> getOptionalContainer(PersistentDataHolder holder, NamespacedKey key) {
      return Optional.ofNullable(getContainer(holder, key));
   }

   public static PersistentDataContainer getContainer(PersistentDataHolder holder, NamespacedKey key, PersistentDataContainer defaultVal) {
      return (PersistentDataContainer)holder.getPersistentDataContainer().getOrDefault(key, PersistentDataType.TAG_CONTAINER, defaultVal);
   }

   public static JsonObject getJsonObject(PersistentDataHolder holder, NamespacedKey key) {
      return (JsonObject)holder.getPersistentDataContainer().get(key, PersistentJsonDataType.JSON_OBJECT);
   }

   public static Optional<JsonObject> getOptionalJsonObject(PersistentDataHolder holder, NamespacedKey key) {
      return Optional.ofNullable(getJsonObject(holder, key));
   }

   public static JsonObject getJsonObject(PersistentDataHolder holder, NamespacedKey key, JsonObject defaultVal) {
      return (JsonObject)holder.getPersistentDataContainer().getOrDefault(key, PersistentJsonDataType.JSON_OBJECT, defaultVal);
   }

   public static JsonArray getJsonArray(PersistentDataHolder holder, NamespacedKey key) {
      return (JsonArray)holder.getPersistentDataContainer().get(key, PersistentJsonDataType.JSON_ARRAY);
   }

   public static Optional<JsonArray> getOptionalJsonArray(PersistentDataHolder holder, NamespacedKey key) {
      return Optional.ofNullable(getJsonArray(holder, key));
   }

   public static JsonArray getJsonArray(PersistentDataHolder holder, NamespacedKey key, JsonArray defaultVal) {
      return (JsonArray)holder.getPersistentDataContainer().getOrDefault(key, PersistentJsonDataType.JSON_ARRAY, defaultVal);
   }

   @Nullable
   public static UUID getUUID(@Nonnull PersistentDataHolder holder, @Nonnull NamespacedKey key) {
      return (UUID)holder.getPersistentDataContainer().get(key, PersistentUUIDDataType.TYPE);
   }

   @Nonnull
   public static Optional<UUID> getOptionalUUID(@Nonnull PersistentDataHolder holder, @Nonnull NamespacedKey key) {
      return Optional.ofNullable(getUUID(holder, key));
   }

   @Nullable
   @ParametersAreNonnullByDefault
   public static <T, Z> Z get(PersistentDataHolder holder, NamespacedKey key, PersistentDataType<T, Z> type) {
      return (Z)holder.getPersistentDataContainer().get(key, type);
   }

   @Nonnull
   @ParametersAreNonnullByDefault
   public static <T, Z> Z get(PersistentDataHolder holder, NamespacedKey key, PersistentDataType<T, Z> type, Z defaultVal) {
      return (Z)holder.getPersistentDataContainer().getOrDefault(key, type, defaultVal);
   }

   public static void remove(PersistentDataHolder holder, NamespacedKey key) {
      holder.getPersistentDataContainer().remove(key);
   }

   @Nonnull
   @ParametersAreNonnullByDefault
   public static <T, Z> Optional<Z> getOptional(PersistentDataHolder holder, NamespacedKey key, PersistentDataType<T, Z> type) {
      return (Optional<Z>)Optional.ofNullable(get(holder, key, type));
   }
}
