package com.ytdd9527.networksexpansion.utils;

import com.balugaq.netex.api.groups.MainItemGroup;
import com.balugaq.netex.api.groups.SubFlexItemGroup;
import io.github.sefiraat.networks.utils.Keys;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import lombok.Generated;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

public final class GroupConfigUtil {
   @NotNull
   public static MainItemGroup getMainItemGroup(@NotNull String key, @NotNull Material defaultMaterial, @NotNull String defaultName) {
      CustomItemStack customItemStack = new CustomItemStack(defaultMaterial, defaultName, new String[0]);
      NamespacedKey namespacedKey = Keys.newKey(key);
      return new MainItemGroup(namespacedKey, customItemStack, 0);
   }

   @NotNull
   public static SubFlexItemGroup getSubFlexItemGroup(@NotNull String key, @NotNull Material defaultMaterial, @NotNull String defaultName) {
      CustomItemStack customItemStack = new CustomItemStack(defaultMaterial, defaultName, new String[0]);
      NamespacedKey namespacedKey = Keys.newKey(key);
      return new SubFlexItemGroup(namespacedKey, customItemStack, 0);
   }

   @Generated
   private GroupConfigUtil() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
