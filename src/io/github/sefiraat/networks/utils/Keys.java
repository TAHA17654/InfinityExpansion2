package io.github.sefiraat.networks.utils;

import com.jeff_media.morepersistentdatatypes.DataType;
import io.github.mooy1.infinityexpansion.InfinityExpansion;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.managers.SupportedPluginManager;
import io.github.sefiraat.networks.network.stackcaches.BlueprintInstance;
import io.github.sefiraat.networks.network.stackcaches.QuantumCache;
import io.github.sefiraat.networks.utils.datatypes.DataTypeMethods;
import io.github.sefiraat.networks.utils.datatypes.PersistentCraftingBlueprintType;
import io.github.sefiraat.networks.utils.datatypes.PersistentQuantumStorageType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.data.persistent.PersistentDataAPI;
import lombok.Generated;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class Keys {
   public static final String NETWORKS_ID = "networks";
   public static final String NETWORKS_CHANGED_ID = "networks-changed";
   public static final NamespacedKey ON_COOLDOWN = newKey("cooldown");
   public static final NamespacedKey ON_COOLDOWN2 = customNewKey("networks", "cooldown");
   public static final NamespacedKey ON_COOLDOWN3 = customNewKey("networks-changed", "cooldown");
   public static final NamespacedKey QUANTUM_STORAGE_INSTANCE = newKey("quantum_storage");
   public static final NamespacedKey QUANTUM_STORAGE_INSTANCE2 = customNewKey("networks", "quantum_storage");
   public static final NamespacedKey QUANTUM_STORAGE_INSTANCE3 = customNewKey("networks-changed", "quantum_storage");
   public static final NamespacedKey BLUEPRINT_INSTANCE = newKey("ntw_blueprint");
   public static final NamespacedKey BLUEPRINT_INSTANCE2 = customNewKey("networks", "ntw_blueprint");
   public static final NamespacedKey BLUEPRINT_INSTANCE3 = customNewKey("networks-changed", "ntw_blueprint");
   public static final NamespacedKey FACE = newKey("face");
   public static final NamespacedKey FACE2 = customNewKey("networks", "face");
   public static final NamespacedKey FACE3 = customNewKey("networks-changed", "face");
   public static final NamespacedKey ITEM = newKey("item");
   public static final NamespacedKey ITEM2 = customNewKey("networks", "item");
   public static final NamespacedKey ITEM3 = customNewKey("networks-changed", "item");
   public static final NamespacedKey TARGET_LOCATION = newKey("target-location");
   public static final NamespacedKey TARGET_LOCATION2 = customNewKey("networks", "target-location");
   public static final NamespacedKey TARGET_LOCATION3 = customNewKey("networks-changed", "target-location");
   public static final NamespacedKey AMOUNT = newKey("amount");
   public static final NamespacedKey AMOUNT2 = customNewKey("networks", "amount");
   public static final NamespacedKey AMOUNT3 = customNewKey("networks-changed", "amount");
   public static final NamespacedKey RECIPE = newKey("recipe");
   public static final NamespacedKey RECIPE2 = customNewKey("networks", "recipe");
   public static final NamespacedKey RECIPE3 = customNewKey("networks-changed", "recipe");
   public static final NamespacedKey OUTPUT = newKey("output");
   public static final NamespacedKey OUTPUT2 = customNewKey("networks", "output");
   public static final NamespacedKey OUTPUT3 = customNewKey("networks-changed", "output");
   public static final NamespacedKey MAX_AMOUNT = newKey("max_amount");
   public static final NamespacedKey MAX_AMOUNT2 = customNewKey("networks", "max_amount");
   public static final NamespacedKey MAX_AMOUNT3 = customNewKey("networks-changed", "max_amount");
   public static final NamespacedKey VOID = newKey("void");
   public static final NamespacedKey VOID2 = customNewKey("networks", "void");
   public static final NamespacedKey VOID3 = customNewKey("networks-changed", "void");
   public static final NamespacedKey SUPPORTS_CUSTOM_MAX_AMOUNT = newKey("supports_custom_max_amount");
   public static final NamespacedKey TRANSFER_MODE = newKey("transfer_mode");
   public static final NamespacedKey STORAGE_UNIT_UPGRADE_TABLE = newKey("storage_upgrade_table");
   public static final NamespacedKey STORAGE_UNIT_UPGRADE_TABLE_MODEL = newKey("storage_upgrade_table_model");
   public static final NamespacedKey ITEM_MOVER_ITEM = newKey("item_mover_item");
   public static final NamespacedKey ITEM_MOVER_AMOUNT = newKey("item_mover_amount");
   public static final NamespacedKey ITEM_MOVER_AMOUNT_LONG = newKey("item_mover_amount_long");
   public static final NamespacedKey EXPANSION_WORKBENCH = newKey("expansion_workbench");
   public static final NamespacedKey FACING_PRESET = newKey("facing_preset");
   public static final NamespacedKey CELL_UUID = newKey("cell_uuid");
   public static final NamespacedKey CELL_CAPACITY = newKey("cell_capacity");
   public static final NamespacedKey CELL_CURRENT_CAPACITY = newKey("cell_current_capacity");
   public static final NamespacedKey CELL_CUSTOM_NAME = newKey("cell_custom_name");
   public static final NamespacedKey CELL_SERVER = newKey("cell_server");
   public static final NamespacedKey CELL_CLEANER_MARKER = newKey("cell_cleaner_marker");
   public static final NamespacedKey CELL_CONVERTER_MARKER = newKey("cell_converter_marker");
   public static final NamespacedKey CRAFT_TARGET = newKey("craft_target");
   public static final NamespacedKey CRAFT_GEAR = newKey("craft_gear");
   public static final NamespacedKey CRAFT_MODE = newKey("craft_mode");
   public static final NamespacedKey CRAFT_REMAINING = newKey("craft_remaining");
   public static final NamespacedKey CRAFT_KEEP = newKey("craft_keep");
   public static final NamespacedKey CRAFT_GEAR_BACKUP = newKey("craft_gear_backup");
   public static final NamespacedKey SMART_CHANNEL = newKey("smart_channel");
   public static final NamespacedKey ASSEMBLY_SLOT_MARKER = newKey("assembly_slot_marker");
   public static final NamespacedKey ASSEMBLY_CARD_INGREDIENTS = newKey("card_ingredients");
   public static final NamespacedKey ASSEMBLY_CARD_OUTPUT = newKey("card_output");
   public static final NamespacedKey ASSEMBLY_CARD_HASH = newKey("card_hash");
   public static final NamespacedKey VOID_FILTERS = newKey("void_filters");
   public static final NamespacedKey CELL_RESERVED_ITEMS = newKey("cell_reserved_items");
   public static final NamespacedKey CELL_VOID_EXCESS = newKey("cell_void_excess");
   public static final NamespacedKey CELL_VOID_EXCESS_ITEMS = newKey("cell_void_excess_items");
   public static final NamespacedKey INFINITY_DISPLAY;
   public static final NamespacedKey TSINGSHAN_DISPLAY;

   public static long getCooldown(ItemMeta itemMeta) {
      long cooldownUntil = PersistentDataAPI.getLong(itemMeta, ON_COOLDOWN, -1L);
      if (cooldownUntil == -1L) {
         cooldownUntil = PersistentDataAPI.getLong(itemMeta, ON_COOLDOWN2, -1L);
      }

      if (cooldownUntil == -1L) {
         cooldownUntil = PersistentDataAPI.getLong(itemMeta, ON_COOLDOWN3, 0L);
      }

      return cooldownUntil;
   }

   @Nullable
   public static QuantumCache getQuantumCache(ItemMeta meta) {
      QuantumCache quantumCache = DataTypeMethods.getCustom(meta, QUANTUM_STORAGE_INSTANCE, PersistentQuantumStorageType.TYPE);
      if (quantumCache == null) {
         quantumCache = DataTypeMethods.getCustom(meta, QUANTUM_STORAGE_INSTANCE2, PersistentQuantumStorageType.TYPE);
      }

      if (quantumCache == null) {
         quantumCache = DataTypeMethods.getCustom(meta, QUANTUM_STORAGE_INSTANCE3, PersistentQuantumStorageType.TYPE);
      }

      return quantumCache;
   }

   @Nullable
   public static BlueprintInstance getBlueprintInstance(ItemMeta meta) {
      BlueprintInstance instance = DataTypeMethods.getCustom(meta, BLUEPRINT_INSTANCE, PersistentCraftingBlueprintType.TYPE);
      if (instance == null) {
         instance = DataTypeMethods.getCustom(meta, BLUEPRINT_INSTANCE2, PersistentCraftingBlueprintType.TYPE);
      }

      if (instance == null) {
         instance = DataTypeMethods.getCustom(meta, BLUEPRINT_INSTANCE3, PersistentCraftingBlueprintType.TYPE);
      }

      return instance;
   }

   @Nullable
   public static String getFace(ItemMeta itemMeta) {
      String string = DataTypeMethods.getCustom(itemMeta, FACE, DataType.STRING);
      if (string == null) {
         string = DataTypeMethods.getCustom(itemMeta, FACE2, DataType.STRING);
      }

      if (string == null) {
         string = DataTypeMethods.getCustom(itemMeta, FACE3, DataType.STRING);
      }

      return string;
   }

   @Nullable
   public static ItemStack[] getItems(ItemMeta itemMeta) {
      ItemStack[] templateStacks = DataTypeMethods.getCustom(itemMeta, ITEM, DataType.ITEM_STACK_ARRAY);
      if (templateStacks == null) {
         templateStacks = DataTypeMethods.getCustom(itemMeta, ITEM2, DataType.ITEM_STACK_ARRAY);
      }

      if (templateStacks == null) {
         templateStacks = DataTypeMethods.getCustom(itemMeta, ITEM3, DataType.ITEM_STACK_ARRAY);
      }

      return templateStacks;
   }

   @Nullable
   public static Location getTargetLocation(ItemMeta itemMeta) {
      Location location = (Location)PersistentDataAPI.get(itemMeta, TARGET_LOCATION, DataType.LOCATION);
      if (location == null) {
         location = (Location)PersistentDataAPI.get(itemMeta, TARGET_LOCATION2, DataType.LOCATION);
      }

      if (location == null) {
         location = (Location)PersistentDataAPI.get(itemMeta, TARGET_LOCATION3, DataType.LOCATION);
      }

      return location;
   }

   public static long getAmountLong(PersistentDataContainer primitive) {
      Long amount;
      try {
         amount = (Long)primitive.get(AMOUNT, DataType.LONG);
         if (amount == null) {
            amount = (Long)primitive.get(AMOUNT2, DataType.LONG);
         }

         if (amount == null) {
            amount = (Long)primitive.getOrDefault(AMOUNT3, DataType.LONG, 0L);
         }
      } catch (Throwable var4) {
         Integer amountI = (Integer)primitive.get(AMOUNT, DataType.INTEGER);
         if (amountI == null) {
            amountI = (Integer)primitive.get(AMOUNT2, DataType.INTEGER);
         }

         if (amountI == null) {
            amountI = (Integer)primitive.getOrDefault(AMOUNT3, DataType.INTEGER, 0);
         }

         amount = amountI.longValue();
      }

      return amount;
   }

   @Nullable
   public static ItemStack[] getRecipe(PersistentDataContainer primitive) {
      ItemStack[] recipe = (ItemStack[])primitive.get(RECIPE, DataType.ITEM_STACK_ARRAY);
      if (recipe == null) {
         recipe = (ItemStack[])primitive.get(RECIPE2, DataType.ITEM_STACK_ARRAY);
      }

      if (recipe == null) {
         recipe = (ItemStack[])primitive.get(RECIPE3, DataType.ITEM_STACK_ARRAY);
      }

      return recipe;
   }

   @Nullable
   public static ItemStack getOutput(PersistentDataContainer primitive) {
      ItemStack output = (ItemStack)primitive.get(OUTPUT, DataType.ITEM_STACK);
      if (output == null) {
         output = (ItemStack)primitive.get(OUTPUT2, DataType.ITEM_STACK);
      }

      if (output == null) {
         output = (ItemStack)primitive.get(OUTPUT3, DataType.ITEM_STACK);
      }

      return output;
   }

   public static long getMaxAmount(PersistentDataContainer primitive) {
      Long limit;
      try {
         limit = (Long)primitive.get(MAX_AMOUNT, DataType.LONG);
         if (limit == null) {
            limit = (Long)primitive.get(MAX_AMOUNT2, DataType.LONG);
         }

         if (limit == null) {
            limit = (Long)primitive.getOrDefault(MAX_AMOUNT3, DataType.LONG, 64L);
         }
      } catch (Throwable var4) {
         Integer limitI = (Integer)primitive.get(MAX_AMOUNT, DataType.INTEGER);
         if (limitI == null) {
            limitI = (Integer)primitive.get(MAX_AMOUNT2, DataType.INTEGER);
         }

         if (limitI == null) {
            limitI = (Integer)primitive.getOrDefault(MAX_AMOUNT3, DataType.INTEGER, 64);
         }

         limit = limitI.longValue();
      }

      return limit;
   }

   public static boolean getVoidExcess(PersistentDataContainer primitive) {
      Boolean voidExcess = (Boolean)primitive.get(VOID, DataType.BOOLEAN);
      if (voidExcess == null) {
         voidExcess = (Boolean)primitive.get(VOID2, DataType.BOOLEAN);
      }

      if (voidExcess == null) {
         voidExcess = (Boolean)primitive.getOrDefault(VOID3, DataType.BOOLEAN, false);
      }

      return voidExcess;
   }

   public static NamespacedKey newKey(String key) {
      return new NamespacedKey(Networks.getInstance(), key);
   }

   public static NamespacedKey customNewKey(String namespace, String key) {
      return new NamespacedKey(namespace, key);
   }

   public static NamespacedKey customNewKey(Plugin plugin, String key) {
      return new NamespacedKey(plugin, key);
   }

   @Generated
   private Keys() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      return o == this ? true : o instanceof Keys;
   }

   @Generated
   @Override
   public int hashCode() {
      int result = 1;
      return 1;
   }

   @Generated
   @Override
   public String toString() {
      return "Keys()";
   }

   static {
      if (SupportedPluginManager.getInstance().isInfinityExpansion()) {
         INFINITY_DISPLAY = InfinityExpansion.createKey("display");
      } else {
         INFINITY_DISPLAY = customNewKey("infinityexpansion", "display");
      }

      TSINGSHAN_DISPLAY = customNewKey("tsingshantechnology-fixed", "display");
   }
}
