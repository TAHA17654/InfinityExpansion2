package dev.sefiraat.sefilib.dough.data.persistent;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javax.annotation.Nonnull;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;

public final class PersistentJsonDataType<T extends JsonElement> implements PersistentDataType<String, T> {
   public static final PersistentJsonDataType<JsonObject> JSON_OBJECT = new PersistentJsonDataType((Class<T>)JsonObject.class);
   public static final PersistentJsonDataType<JsonArray> JSON_ARRAY = new PersistentJsonDataType((Class<T>)JsonArray.class);
   private final Class<T> jsonClass;
   private final JsonParser parser = new JsonParser();

   public PersistentJsonDataType(@Nonnull Class<T> jsonClass) {
      this.jsonClass = jsonClass;
   }

   public Class<String> getPrimitiveType() {
      return String.class;
   }

   public Class<T> getComplexType() {
      return this.jsonClass;
   }

   public String toPrimitive(JsonElement complex, PersistentDataAdapterContext context) {
      return complex.toString();
   }

   public T fromPrimitive(String primitive, PersistentDataAdapterContext context) {
      JsonElement json = this.parser.parse(primitive);
      return this.jsonClass.isInstance(json) ? this.jsonClass.cast(json) : null;
   }
}
