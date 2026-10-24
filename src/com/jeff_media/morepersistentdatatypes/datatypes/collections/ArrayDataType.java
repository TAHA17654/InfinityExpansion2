package com.jeff_media.morepersistentdatatypes.datatypes.collections;

import com.jeff_media.morepersistentdatatypes.DataType;
import java.lang.reflect.Array;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class ArrayDataType<T> implements PersistentDataType<PersistentDataContainer, T[]> {
   private static final String E_NOT_AN_ARRAY = "Not an array.";
   private static final NamespacedKey KEY_SIZE = DataType.Utils.getValueKey("s");
   private final Class<T[]> arrayClazz;
   private final Class<T> componentClazz;
   private final PersistentDataType<?, T> dataType;

   public ArrayDataType(@NotNull T[] array, @NotNull PersistentDataType<?, T> dataType) {
      this.arrayClazz = (Class<T[]>)array.getClass();
      this.componentClazz = (Class<T>)array.getClass().getComponentType();
      this.dataType = dataType;
   }

   @NotNull
   public Class<PersistentDataContainer> getPrimitiveType() {
      return PersistentDataContainer.class;
   }

   @NotNull
   public Class<T[]> getComplexType() {
      return this.arrayClazz;
   }

   @NotNull
   public PersistentDataContainer toPrimitive(T[] array, @NotNull PersistentDataAdapterContext context) {
      PersistentDataContainer pdc = context.newPersistentDataContainer();
      pdc.set(KEY_SIZE, DataType.INTEGER, array.length);

      for (int i = 0; i < array.length; i++) {
         T data = array[i];
         if (data != null) {
            pdc.set(DataType.Utils.getValueKey(i), this.dataType, data);
         }
      }

      return pdc;
   }

   @NotNull
   public T[] fromPrimitive(@NotNull PersistentDataContainer pdc, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
      Integer size = (Integer)pdc.get(KEY_SIZE, DataType.INTEGER);
      if (size == null) {
         throw new IllegalArgumentException("Not an array.");
      } else {
         T[] array = (T[])((Object[])Array.newInstance(this.componentClazz, size));

         for (int i = 0; i < size; i++) {
            array[i] = (T)pdc.get(DataType.Utils.getValueKey(i), this.dataType);
         }

         return array;
      }
   }
}
