package com.jeff_media.morepersistentdatatypes;

import com.jeff_media.morepersistentdatatypes.datatypes.BlockDataArrayDataType;
import com.jeff_media.morepersistentdatatypes.datatypes.BooleanArrayDataType;
import com.jeff_media.morepersistentdatatypes.datatypes.CharArrayDataType;
import com.jeff_media.morepersistentdatatypes.datatypes.DoubleArrayDataType;
import com.jeff_media.morepersistentdatatypes.datatypes.FileConfigurationDataType;
import com.jeff_media.morepersistentdatatypes.datatypes.FloatArrayDataType;
import com.jeff_media.morepersistentdatatypes.datatypes.GenericDataType;
import com.jeff_media.morepersistentdatatypes.datatypes.ShortArrayDataType;
import com.jeff_media.morepersistentdatatypes.datatypes.StringArrayDataType;
import com.jeff_media.morepersistentdatatypes.datatypes.UuidDataType;
import com.jeff_media.morepersistentdatatypes.datatypes.collections.ArrayDataType;
import com.jeff_media.morepersistentdatatypes.datatypes.collections.CollectionDataType;
import com.jeff_media.morepersistentdatatypes.datatypes.collections.MapDataType;
import com.jeff_media.morepersistentdatatypes.datatypes.serializable.ConfigurationSerializableArrayDataType;
import com.jeff_media.morepersistentdatatypes.datatypes.serializable.ConfigurationSerializableDataType;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.BlockVector;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public interface DataType {
   PersistentDataType<byte[], AttributeModifier> ATTRIBUTE_MODIFIER = new ConfigurationSerializableDataType(AttributeModifier.class);
   PersistentDataType<byte[], AttributeModifier[]> ATTRIBUTE_MODIFIER_ARRAY = new ConfigurationSerializableArrayDataType(AttributeModifier[].class);
   PersistentDataType<String, BlockData> BLOCK_DATA = new GenericDataType<>(String.class, BlockData.class, Bukkit::createBlockData, BlockData::getAsString);
   PersistentDataType<byte[], BlockData[]> BLOCK_DATA_ARRAY = new BlockDataArrayDataType();
   PersistentDataType<byte[], BlockVector> BLOCK_VECTOR = new ConfigurationSerializableDataType(BlockVector.class);
   PersistentDataType<byte[], BlockVector[]> BLOCK_VECTOR_ARRAY = new ConfigurationSerializableArrayDataType(BlockVector[].class);
   PersistentDataType<byte[], BoundingBox> BOUNDING_BOX = new ConfigurationSerializableDataType(BoundingBox.class);
   PersistentDataType<byte[], BoundingBox[]> BOUNDING_BOX_ARRAY = new ConfigurationSerializableArrayDataType(BoundingBox[].class);
   PersistentDataType<byte[], Color> COLOR = new ConfigurationSerializableDataType(Color.class);
   PersistentDataType<byte[], Color[]> COLOR_ARRAY = new ConfigurationSerializableArrayDataType(Color[].class);
   PersistentDataType<byte[], ConfigurationSerializable> CONFIGURATION_SERIALIZABLE = new ConfigurationSerializableDataType<>(ConfigurationSerializable.class);
   PersistentDataType<byte[], ConfigurationSerializable[]> CONFIGURATION_SERIALIZABLE_ARRAY = new ConfigurationSerializableArrayDataType<>(
      ConfigurationSerializable[].class
   );
   PersistentDataType<Long, Date> DATE = new GenericDataType<>(Long.class, Date.class, Date::new, Date::getTime);
   PersistentDataType<String, FileConfiguration> FILE_CONFIGURATION = new FileConfigurationDataType();
   PersistentDataType<byte[], FireworkEffect> FIREWORK_EFFECT = new ConfigurationSerializableDataType(FireworkEffect.class);
   PersistentDataType<byte[], FireworkEffect[]> FIREWORK_EFFECT_ARRAY = new ConfigurationSerializableArrayDataType(FireworkEffect[].class);
   PersistentDataType<byte[], ItemMeta> ITEM_META = new ConfigurationSerializableDataType(ItemMeta.class);
   PersistentDataType<byte[], ItemMeta[]> ITEM_META_ARRAY = new ConfigurationSerializableArrayDataType(ItemMeta[].class);
   PersistentDataType<byte[], ItemStack> ITEM_STACK = new ConfigurationSerializableDataType(ItemStack.class);
   PersistentDataType<byte[], ItemStack[]> ITEM_STACK_ARRAY = new ConfigurationSerializableArrayDataType(ItemStack[].class);
   PersistentDataType<byte[], Location> LOCATION = new ConfigurationSerializableDataType(Location.class);
   PersistentDataType<byte[], Location[]> LOCATION_ARRAY = new ConfigurationSerializableArrayDataType(Location[].class);
   PersistentDataType<byte[], OfflinePlayer> OFFLINE_PLAYER = new ConfigurationSerializableDataType(OfflinePlayer.class);
   PersistentDataType<byte[], OfflinePlayer[]> OFFLINE_PLAYER_ARRAY = new ConfigurationSerializableArrayDataType(OfflinePlayer[].class);
   PersistentDataType<byte[], Pattern> PATTERN = new ConfigurationSerializableDataType(Pattern.class);
   PersistentDataType<byte[], Pattern[]> PATTERN_ARRAY = new ConfigurationSerializableArrayDataType(Pattern[].class);
   PersistentDataType<byte[], Player> PLAYER = new ConfigurationSerializableDataType(Player.class);
   PersistentDataType<byte[], Player[]> PLAYER_ARRAY = new ConfigurationSerializableArrayDataType(Player[].class);
   PersistentDataType<byte[], PotionEffect> POTION_EFFECT = new ConfigurationSerializableDataType(PotionEffect.class);
   PersistentDataType<byte[], PotionEffect[]> POTION_EFFECT_ARRAY = new ConfigurationSerializableArrayDataType(PotionEffect[].class);
   PersistentDataType<byte[], UUID> UUID = new UuidDataType();
   PersistentDataType<byte[], Vector> VECTOR = new ConfigurationSerializableDataType(Vector.class);
   PersistentDataType<byte[], Vector[]> VECTOR_ARRAY = new ConfigurationSerializableArrayDataType(Vector[].class);
   PersistentDataType<Byte, Boolean> BOOLEAN = new GenericDataType<>(
      Byte.class, Boolean.class, aByte -> aByte == 1, aBoolean -> Byte.valueOf((byte)(aBoolean ? 1 : 0))
   );
   PersistentDataType<byte[], boolean[]> BOOLEAN_ARRAY = new BooleanArrayDataType();
   PersistentDataType<Integer, Character> CHARACTER = new GenericDataType<>(
      Integer.class, Character.class, integer -> (char)integer.intValue(), character -> Integer.valueOf(character)
   );
   PersistentDataType<int[], char[]> CHARACTER_ARRAY = new CharArrayDataType();
   PersistentDataType<byte[], double[]> DOUBLE_ARRAY = new DoubleArrayDataType();
   PersistentDataType<byte[], float[]> FLOAT_ARRAY = new FloatArrayDataType();
   PersistentDataType<byte[], short[]> SHORT_ARRAY = new ShortArrayDataType();
   PersistentDataType<byte[], String[]> STRING_ARRAY = new StringArrayDataType(StandardCharsets.UTF_8);
   PersistentDataType<Byte, Byte> BYTE = PersistentDataType.BYTE;
   PersistentDataType<byte[], byte[]> BYTE_ARRAY = PersistentDataType.BYTE_ARRAY;
   PersistentDataType<Double, Double> DOUBLE = PersistentDataType.DOUBLE;
   PersistentDataType<Float, Float> FLOAT = PersistentDataType.FLOAT;
   PersistentDataType<Integer, Integer> INTEGER = PersistentDataType.INTEGER;
   PersistentDataType<int[], int[]> INTEGER_ARRAY = PersistentDataType.INTEGER_ARRAY;
   PersistentDataType<Long, Long> LONG = PersistentDataType.LONG;
   PersistentDataType<long[], long[]> LONG_ARRAY = PersistentDataType.LONG_ARRAY;
   PersistentDataType<Short, Short> SHORT = PersistentDataType.SHORT;
   PersistentDataType<String, String> STRING = PersistentDataType.STRING;
   PersistentDataType<PersistentDataContainer, PersistentDataContainer> TAG_CONTAINER = PersistentDataType.TAG_CONTAINER;
   PersistentDataType<PersistentDataContainer[], PersistentDataContainer[]> TAG_CONTAINER_ARRAY = PersistentDataType.TAG_CONTAINER_ARRAY;

   static <E extends Enum<E>> PersistentDataType<String, E> asEnum(@NotNull Class<E> enumClazz) {
      return new GenericDataType<>(String.class, enumClazz, s -> Enum.valueOf(enumClazz, s), Enum::name);
   }

   static <T> ArrayDataType<T> asArray(@NotNull T[] array, @NotNull PersistentDataType<?, T> dataType) {
      return new ArrayDataType<>(array, dataType);
   }

   static <C extends Collection<T>, T> CollectionDataType<C, T> asGenericCollection(@NotNull Supplier<C> supplier, @NotNull PersistentDataType<?, T> type) {
      return new CollectionDataType<>(supplier, type);
   }

   static <T> CollectionDataType<List<T>, T> asList(@NotNull PersistentDataType<?, T> type) {
      return asGenericCollection(ArrayList::new, type);
   }

   static <T> CollectionDataType<ArrayList<T>, T> asArrayList(@NotNull PersistentDataType<?, T> type) {
      return asGenericCollection(ArrayList::new, type);
   }

   static <T> CollectionDataType<LinkedList<T>, T> asLinkedList(@NotNull PersistentDataType<?, T> type) {
      return asGenericCollection(LinkedList::new, type);
   }

   static <T> CollectionDataType<Set<T>, T> asSet(@NotNull PersistentDataType<?, T> type) {
      return asGenericCollection(HashSet::new, type);
   }

   static <T> CollectionDataType<HashSet<T>, T> asHashSet(@NotNull PersistentDataType<?, T> type) {
      return asGenericCollection(HashSet::new, type);
   }

   static <T> CollectionDataType<CopyOnWriteArrayList<T>, T> asCopyOnWriteArrayList(@NotNull PersistentDataType<?, T> type) {
      return asGenericCollection(CopyOnWriteArrayList::new, type);
   }

   static <T> CollectionDataType<CopyOnWriteArraySet<T>, T> asCopyOnWriteArraySet(@NotNull PersistentDataType<?, T> type) {
      return asGenericCollection(CopyOnWriteArraySet::new, type);
   }

   static <E extends Enum<E>> CollectionDataType<EnumSet<E>, E> asEnumSet(@NotNull Class<E> enumClazz) {
      return asGenericCollection(() -> EnumSet.noneOf(enumClazz), asEnum(enumClazz));
   }

   static <M extends Map<K, V>, K, V> MapDataType<M, K, V> asGenericMap(
      @NotNull Supplier<M> supplier, @NotNull PersistentDataType<?, K> keyType, @NotNull PersistentDataType<?, V> valueType
   ) {
      return new MapDataType<>(supplier, keyType, valueType);
   }

   static <K, V> MapDataType<Map<K, V>, K, V> asMap(@NotNull PersistentDataType<?, K> keyType, @NotNull PersistentDataType<?, V> valueType) {
      return asGenericMap(HashMap::new, keyType, valueType);
   }

   static <K, V> MapDataType<HashMap<K, V>, K, V> asHashMap(@NotNull PersistentDataType<?, K> keyType, @NotNull PersistentDataType<?, V> valueType) {
      return asGenericMap(HashMap::new, keyType, valueType);
   }

   static <K, V> MapDataType<ConcurrentHashMap<K, V>, K, V> asConcurrentHashMap(
      @NotNull PersistentDataType<?, K> keyType, @NotNull PersistentDataType<?, V> valueType
   ) {
      return asGenericMap(ConcurrentHashMap::new, keyType, valueType);
   }

   static <K, V> MapDataType<IdentityHashMap<K, V>, K, V> asIdentityHashMap(
      @NotNull PersistentDataType<?, K> keyType, @NotNull PersistentDataType<?, V> valueType
   ) {
      return asGenericMap(IdentityHashMap::new, keyType, valueType);
   }

   static <K, V> MapDataType<LinkedHashMap<K, V>, K, V> asLinkedHashMap(@NotNull PersistentDataType<?, K> keyType, @NotNull PersistentDataType<?, V> valueType) {
      return asGenericMap(LinkedHashMap::new, keyType, valueType);
   }

   static <K, V> MapDataType<TreeMap<K, V>, K, V> asTreeMap(@NotNull PersistentDataType<?, K> keyType, @NotNull PersistentDataType<?, V> valueType) {
      return asGenericMap(TreeMap::new, keyType, valueType);
   }

   static <K, V> MapDataType<Hashtable<K, V>, K, V> asHashtable(@NotNull PersistentDataType<?, K> keyType, @NotNull PersistentDataType<?, V> valueType) {
      return asGenericMap(Hashtable::new, keyType, valueType);
   }

   static <E extends Enum<E>, V> MapDataType<EnumMap<E, V>, E, V> asEnumMap(@NotNull Class<E> enumClazz, @NotNull PersistentDataType<?, V> valueType) {
      return asGenericMap(() -> new EnumMap<>(enumClazz), asEnum(enumClazz), valueType);
   }

   public static class Utils {
      private static final Map<String, NamespacedKey> KEY_KEYS = new HashMap<>();
      private static final Map<String, NamespacedKey> VALUE_KEYS = new HashMap<>();

      private Utils() {
      }

      public static NamespacedKey getKeyKey(int index) {
         return getKeyKey(String.valueOf(index));
      }

      public static NamespacedKey getKeyKey(String name) {
         return KEY_KEYS.computeIfAbsent(name, __ -> NamespacedKey.fromString("k:" + name));
      }

      public static NamespacedKey getValueKey(int index) {
         return getValueKey(String.valueOf(index));
      }

      public static NamespacedKey getValueKey(String name) {
         return VALUE_KEYS.computeIfAbsent(name, __ -> NamespacedKey.fromString("v:" + name));
      }

      static {
         IntStream.range(0, 100).forEach(number -> {
            getValueKey(number);
            getKeyKey(number);
         });
      }
   }
}
