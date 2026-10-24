package com.jeff_media.morepersistentdatatypes.datatypes;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class GenericDataType<T, Z> implements PersistentDataType<T, Z> {
   private static final Class<?>[] ALLOWED_TYPES = new Class[]{
      Byte.class,
      byte[].class,
      Double.class,
      Float.class,
      Integer.class,
      int[].class,
      Long.class,
      long[].class,
      Short.class,
      String.class,
      PersistentDataContainer.class,
      PersistentDataContainer[].class
   };
   private final Class<T> primitiveType;
   private final Class<Z> complexType;
   private final Function<T, Z> toComplex;
   private final Function<Z, T> toPrimitive;

   public GenericDataType(Class<T> primitiveType, Class<Z> complexType, Function<T, Z> toComplex, Function<Z, T> toPrimitive) {
      if (Arrays.stream(ALLOWED_TYPES).noneMatch(clazz -> clazz.equals(primitiveType))) {
         throw new IllegalArgumentException(
            String.format(
               "Not a valid primitive type: %s. Valid primitive types are: %s",
               primitiveType.getName(),
               Arrays.stream(ALLOWED_TYPES).map(Class::getSimpleName).collect(Collectors.joining(", "))
            )
         );
      } else {
         this.primitiveType = primitiveType;
         this.complexType = complexType;
         this.toComplex = toComplex;
         this.toPrimitive = toPrimitive;
      }
   }

   @NotNull
   public Class<T> getPrimitiveType() {
      return this.primitiveType;
   }

   @NotNull
   public Class<Z> getComplexType() {
      return this.complexType;
   }

   @NotNull
   public T toPrimitive(@NotNull Z z, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
      return this.toPrimitive.apply(z);
   }

   @NotNull
   public Z fromPrimitive(@NotNull T t, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
      return this.toComplex.apply(t);
   }
}
