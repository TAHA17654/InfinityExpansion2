package com.balugaq.bim.grid;

import java.util.HashMap;
import java.util.Map;
import lombok.Generated;
import org.bukkit.NamespacedKey;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class GridPresetRegistry {
   private static final GridPresetRegistry optionRegistry = new GridPresetRegistry();
   private final Map<NamespacedKey, GridPreset> presetRegistry = new HashMap<>();

   public static void registerOption(GridPreset option) {
      optionRegistry.presetRegistry.put(option.getIdentifier(), option);
   }

   public static GridPreset getOption(NamespacedKey id) {
      return optionRegistry.presetRegistry.get(id);
   }

   @Generated
   public Map<NamespacedKey, GridPreset> getPresetRegistry() {
      return this.presetRegistry;
   }

   @Generated
   public static GridPresetRegistry getOptionRegistry() {
      return optionRegistry;
   }
}
