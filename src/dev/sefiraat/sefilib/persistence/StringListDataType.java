package dev.sefiraat.sefilib.persistence;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class StringListDataType implements PersistentDataType<PersistentDataContainer, List<String>> {
   public static final PersistentDataType<PersistentDataContainer, List<String>> TYPE = new StringListDataType();

   @Nonnull
   public Class<PersistentDataContainer> getPrimitiveType() {
      return PersistentDataContainer.class;
   }

   @Nonnull
   public Class<List<String>> getComplexType() {
      return List.class;
   }

   @Nonnull
   public PersistentDataContainer toPrimitive(@Nonnull List<String> complex, @Nonnull PersistentDataAdapterContext context) {
      PersistentDataContainer container = context.newPersistentDataContainer();

      for (int i = 0; i < complex.size(); i++) {
         NamespacedKey key = new NamespacedKey("sefilib", String.valueOf(i));
         container.set(key, STRING, complex.get(i));
      }

      return container;
   }

   @Nonnull
   public List<String> fromPrimitive(@Nonnull PersistentDataContainer primitive, @Nonnull PersistentDataAdapterContext context) {
      List<String> strings = new ArrayList<>();

      for (NamespacedKey key : primitive.getKeys()) {
         strings.add((String)primitive.get(key, STRING));
      }

      return strings;
   }
}
