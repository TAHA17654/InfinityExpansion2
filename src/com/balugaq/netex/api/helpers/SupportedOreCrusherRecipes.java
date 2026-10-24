package com.balugaq.netex.api.helpers;

import io.github.sefiraat.networks.utils.StackUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.multiblocks.MultiBlockMachine;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import io.github.thebusybiscuit.slimefun4.implementation.items.backpacks.SlimefunBackpack;
import java.util.HashMap;
import java.util.Map;
import lombok.Generated;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class SupportedOreCrusherRecipes {
   private static final Map<ItemStack[], ItemStack> RECIPES = new HashMap<>();

   @NotNull
   public static Map<ItemStack[], ItemStack> getRecipes() {
      return RECIPES;
   }

   public static void addRecipe(@NotNull ItemStack[] input, @NotNull ItemStack output) {
      RECIPES.put(input, output);
   }

   public static boolean testRecipe(@NotNull ItemStack[] input, @NotNull ItemStack[] recipe) {
      for (int test = 0; test < recipe.length; test++) {
         if (!StackUtils.itemsMatch(input[test], recipe[test], false, true)) {
            return false;
         }
      }

      return true;
   }

   public static boolean allowedRecipe(@NotNull SlimefunItem item) {
      return !(item instanceof SlimefunBackpack);
   }

   @Generated
   private SupportedOreCrusherRecipes() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   static {
      String id = SlimefunItems.ORE_CRUSHER.getItemId();
      if (SlimefunItem.getById(id) instanceof MultiBlockMachine mb) {
         boolean isInput = true;
         ItemStack[] input = null;

         for (ItemStack[] recipe : mb.getRecipes()) {
            if (isInput) {
               input = recipe;
            } else {
               if (input.length != 9) {
                  ItemStack[] newInput = new ItemStack[9];

                  for (int i = 0; i < 9; i++) {
                     if (i < input.length) {
                        newInput[i] = input[i];
                     } else {
                        newInput[i] = null;
                     }
                  }

                  input = newInput;
               }

               RECIPES.put(input, recipe[0]);
            }

            isInput = !isInput;
         }
      }

      for (SlimefunItem item : Slimefun.getRegistry().getEnabledSlimefunItems()) {
         RecipeType recipeType = item.getRecipeType();
         if (recipeType == RecipeType.ORE_CRUSHER && allowedRecipe(item)) {
            ItemStack[] itemStacks = new ItemStack[9];
            int ix = 0;

            for (ItemStack itemStack : item.getRecipe()) {
               if (itemStack == null) {
                  itemStacks[ix] = null;
               } else {
                  itemStacks[ix] = new ItemStack(itemStack.clone());
               }

               if (++ix >= 9) {
                  break;
               }
            }

            addRecipe(itemStacks, item.getRecipeOutput());
         }
      }
   }
}
