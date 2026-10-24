package io.github.sefiraat.networks.utils.datatypes;

import com.jeff_media.morepersistentdatatypes.DataType;
import com.ytdd9527.networksexpansion.implementation.ExpansionItems;
import io.github.sefiraat.networks.network.stackcaches.BlueprintInstance;
import io.github.sefiraat.networks.utils.Keys;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class PersistentCraftingBlueprintType implements PersistentDataType<PersistentDataContainer, BlueprintInstance> {
   public static final PersistentDataType<PersistentDataContainer, BlueprintInstance> TYPE = new PersistentCraftingBlueprintType();

   public Class<PersistentDataContainer> getPrimitiveType() {
      return PersistentDataContainer.class;
   }

   public Class<BlueprintInstance> getComplexType() {
      return BlueprintInstance.class;
   }

   public static void setItemStack(PersistentDataContainer container, String key, ItemStack itemStack) {
      setItemStack(container, Keys.newKey(key), itemStack);
   }

   public static void setItemStack(PersistentDataContainer container, NamespacedKey key, ItemStack itemStack) {
      if (itemStack != null && itemStack.getType() != Material.AIR) {
         if (itemStack.isSimilar(new ItemStack(itemStack.getType()))) {
            container.set(key, DataType.STRING, "mc;" + itemStack.getType().name() + ";" + itemStack.getAmount());
         } else {
            SlimefunItem sf = SlimefunItem.getByItem(itemStack);
            if (sf != null && itemStack.isSimilar(sf.getItem())) {
               container.set(key, DataType.STRING, "sf;" + sf.getId() + ";" + itemStack.getAmount());
            } else {
               container.set(key, DataType.ITEM_STACK, itemStack);
            }
         }
      }
   }

   @Nullable
   public static ItemStack getItemStack(PersistentDataContainer primitive, NamespacedKey key) {
      try {
         String s = (String)primitive.get(key, DataType.STRING);
         String[] s2 = s.split(";");
         if (s2[0].equals("mc")) {
            return new ItemStack(Material.valueOf(s2[1]), Integer.parseInt(s2[2]));
         } else if (s2[0].equals("sf")) {
            SlimefunItem sf = SlimefunItem.getById(s2[1]);
            if (sf == null) {
               sf = ExpansionItems.PLACEHOLDER_ITEM;
            }

            ItemStack item = sf.getItem().clone();
            item.setAmount(Integer.parseInt(s2[2]));
            return item;
         } else {
            return null;
         }
      } catch (Exception var7) {
         try {
            return (ItemStack)primitive.get(key, DataType.ITEM_STACK);
         } catch (Exception var6) {
            return null;
         }
      }
   }

   public PersistentDataContainer toPrimitive(BlueprintInstance complex, PersistentDataAdapterContext context) {
      PersistentDataContainer container = context.newPersistentDataContainer();

      for (int i = 0; i < complex.getRecipeItems().length; i++) {
         setItemStack(container, "recipe_" + i, complex.getRecipeItems()[i]);
      }

      if (complex.getItemStack() != null) {
         setItemStack(container, Keys.OUTPUT, complex.getItemStack());
      }

      return container;
   }

   public BlueprintInstance fromPrimitive(PersistentDataContainer primitive, PersistentDataAdapterContext context) {
      ItemStack[] recipe = Keys.getRecipe(primitive);
      if (recipe == null) {
         List<NamespacedKey> recipeKeys = primitive.getKeys().stream().filter(k -> k.getKey().startsWith("recipe_")).toList();
         recipe = new ItemStack[Math.max(9, recipeKeys.size())];

         for (NamespacedKey key : recipeKeys) {
            int slot = Integer.parseInt(key.getKey().split("_")[1]);
            recipe[slot] = getItemStack(primitive, key);
         }
      }

      ItemStack output;
      try {
         output = Keys.getOutput(primitive);
      } catch (Exception var8) {
         output = getItemStack(primitive, Keys.OUTPUT);
      }

      return recipe != null && output != null ? new BlueprintInstance(recipe, output) : BlueprintInstance.INVALID;
   }
}
