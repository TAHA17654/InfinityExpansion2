package com.jeff_media.morepersistentdatatypes.datatypes;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class FileConfigurationDataType implements PersistentDataType<String, FileConfiguration> {
   @NotNull
   public Class<String> getPrimitiveType() {
      return String.class;
   }

   @NotNull
   public Class<FileConfiguration> getComplexType() {
      return FileConfiguration.class;
   }

   @NotNull
   public String toPrimitive(@NotNull FileConfiguration fileConfiguration, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
      return fileConfiguration.saveToString();
   }

   @NotNull
   public FileConfiguration fromPrimitive(@NotNull String s, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
      try {
         YamlConfiguration yaml = new YamlConfiguration();
         yaml.loadFromString(s);
         return yaml;
      } catch (InvalidConfigurationException var4) {
         var4.printStackTrace();
         return new YamlConfiguration();
      }
   }
}
