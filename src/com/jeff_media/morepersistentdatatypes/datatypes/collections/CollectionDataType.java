package com.jeff_media.morepersistentdatatypes.datatypes.collections;

import com.jeff_media.morepersistentdatatypes.DataType;
import java.util.Collection;
import java.util.function.Supplier;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class CollectionDataType<C extends Collection<T>, T> implements PersistentDataType<PersistentDataContainer, C> {
   private static final String E_NOT_A_COLLECTION = "Not a collection.";
   private static final NamespacedKey KEY_SIZE = DataType.Utils.getValueKey("s");
   private final Supplier<? extends C> collectionSupplier;
   private final Class<C> collectionClazz;
   private final PersistentDataType<?, T> dataType;

   public CollectionDataType(@NotNull Supplier<C> supplier, @NotNull PersistentDataType<?, T> dataType) {
      this.collectionClazz = (Class<C>)supplier.get().getClass();
      this.collectionSupplier = supplier;
      this.dataType = dataType;
   }

   @NotNull
   public Class<PersistentDataContainer> getPrimitiveType() {
      return PersistentDataContainer.class;
   }

   @NotNull
   public Class<C> getComplexType() {
      return this.collectionClazz;
   }

   @NotNull
   public PersistentDataContainer toPrimitive(@NotNull C collection, @NotNull PersistentDataAdapterContext context) {
      PersistentDataContainer pdc = context.newPersistentDataContainer();
      pdc.set(KEY_SIZE, DataType.INTEGER, collection.size());
      int index = 0;

      for (T data : collection) {
         if (data != null) {
            pdc.set(DataType.Utils.getValueKey(index), this.dataType, data);
         }

         index++;
      }

      return pdc;
   }

   @NotNull
   public C fromPrimitive(@NotNull PersistentDataContainer pdc, @NotNull PersistentDataAdapterContext context) {
      C collection = (C)this.collectionSupplier.get();
      Integer size = (Integer)pdc.get(KEY_SIZE, DataType.INTEGER);
      if (size == null) {
         throw new IllegalArgumentException("Not a collection.");
      } else {
         for (int i = 0; i < size; i++) {
            collection.add((T)pdc.get(DataType.Utils.getValueKey(i), this.dataType));
         }

         return collection;
      }
   }
}
