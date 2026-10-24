package io.github.sefiraat.networks.utils.datatypes;

import java.util.Optional;
import lombok.Generated;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DataTypeMethods {
   @Nullable
   public static <T, Z> Z getCustom(@NotNull PersistentDataHolder holder, @NotNull NamespacedKey key, @NotNull PersistentDataType<T, Z> type) {
      return (Z)holder.getPersistentDataContainer().get(key, type);
   }

   @NotNull
   public static <T, Z> Optional<Z> getOptionalCustom(@NotNull PersistentDataHolder holder, @NotNull NamespacedKey key, @NotNull PersistentDataType<T, Z> type) {
      return (Optional<Z>)Optional.ofNullable(getCustom(holder, key, type));
   }

   @NotNull
   public static <T, Z> Z getCustom(
      @NotNull PersistentDataHolder holder, @NotNull NamespacedKey key, @NotNull PersistentDataType<T, Z> type, @NotNull Z defaultVal
   ) {
      return (Z)holder.getPersistentDataContainer().getOrDefault(key, type, defaultVal);
   }

   public static <T, Z> boolean hasCustom(@NotNull PersistentDataHolder holder, @NotNull NamespacedKey key, @NotNull PersistentDataType<T, Z> type) {
      return holder.getPersistentDataContainer().has(key, type);
   }

   public static <T, Z> void setCustom(@NotNull PersistentDataHolder holder, @NotNull NamespacedKey key, @NotNull PersistentDataType<T, Z> type, @NotNull Z obj) {
      holder.getPersistentDataContainer().set(key, type, obj);
   }

   public static <T, Z> void removeCustom(@NotNull PersistentDataHolder holder, @NotNull NamespacedKey key) {
      holder.getPersistentDataContainer().remove(key);
   }

   @Generated
   private DataTypeMethods() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
