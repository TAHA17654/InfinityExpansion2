package com.ytdd9527.networksexpansion.implementation.machines.cellnet.assembly;

import com.balugaq.netex.api.enums.CraftType;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.inventory.CampfireRecipe;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class RecipeLegitimacy {
   private RecipeLegitimacy() {
   }

   public static boolean isValid(@NotNull List<ItemStack> inputs, @NotNull ItemStack output) {
      if (!inputs.isEmpty() && !output.getType().isAir() && output.getAmount() > 0) {
         List<ItemStack> merged = AssemblyCard.mergeInputs(inputs);
         ItemStack outOne = output.clone();
         outOne.setAmount(1);
         return matchesVanillaRecipe(merged, output, outOne) || matchesCraftTypeRecipe(merged, output, outOne) || matchesSlimefunRecipe(merged, output, outOne);
      } else {
         return false;
      }
   }

   private static boolean matchesCraftTypeRecipe(@NotNull List<ItemStack> merged, @NotNull ItemStack output, @NotNull ItemStack outOne) {
      for (Entry<CraftType, Set<Entry<ItemStack[], ItemStack>>> craftEntry : CraftType.map().entrySet()) {
         for (Entry<ItemStack[], ItemStack> recipe : craftEntry.getValue()) {
            ItemStack result = recipe.getValue();
            if (result != null && !result.getType().isAir() && result.getAmount() == output.getAmount()) {
               ItemStack resultOne = result.clone();
               resultOne.setAmount(1);
               if (StackUtils.itemsMatch(outOne, resultOne)) {
                  List<ItemStack> need = new ArrayList<>();

                  for (ItemStack cell : recipe.getKey()) {
                     if (cell != null && !cell.getType().isAir()) {
                        need.add(cell);
                     }
                  }

                  if (!need.isEmpty() && sameConsumption(merged, need)) {
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   private static boolean matchesVanillaRecipe(@NotNull List<ItemStack> merged, @NotNull ItemStack output, @NotNull ItemStack outOne) {
      for (Recipe recipe : Bukkit.getRecipesFor(output)) {
         if (recipe instanceof ShapedRecipe) {
            ShapedRecipe shaped = (ShapedRecipe)recipe;
            List<ItemStack> need = new ArrayList<>();

            for (ItemStack grid : shaped.getIngredientMap().values()) {
               if (grid != null && !grid.getType().isAir()) {
                  need.add(grid);
               }
            }

            if (matchesCraftingOutput(shaped.getResult(), output) && sameConsumption(merged, need)) {
               return true;
            }
         } else if (recipe instanceof ShapelessRecipe shapeless) {
            List<ItemStack> need = new ArrayList<>(shapeless.getIngredientList());
            if (matchesCraftingOutput(shapeless.getResult(), output) && sameConsumption(merged, need)) {
               return true;
            }
         } else if (recipe instanceof FurnaceRecipe furnace) {
            if (matchesSmelting(furnace.getInputChoice(), furnace.getResult(), merged, output)) {
               return true;
            }
         } else if (recipe instanceof CampfireRecipe campfire && matchesSmelting(campfire.getInputChoice(), campfire.getResult(), merged, output)) {
            return true;
         }
      }

      return false;
   }

   private static boolean matchesSlimefunRecipe(@NotNull List<ItemStack> merged, @NotNull ItemStack output, @NotNull ItemStack outOne) {
      for (SlimefunItem item : Slimefun.getRegistry().getAllSlimefunItems()) {
         ItemStack[] grid = item.getRecipe();
         if (grid != null && item.getRecipeType() != RecipeType.NULL) {
            ItemStack result = item.getRecipeOutput();
            if (result != null && !result.getType().isAir()) {
               List<ItemStack> need = new ArrayList<>();

               for (ItemStack cell : grid) {
                  if (cell != null && !cell.getType().isAir()) {
                     need.add(cell);
                  }
               }

               if (!need.isEmpty()) {
                  ItemStack resultOne = result.clone();
                  resultOne.setAmount(1);
                  if (StackUtils.itemsMatch(outOne, resultOne) && result.getAmount() == output.getAmount() && sameConsumption(merged, need)) {
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   private static boolean matchesCraftingOutput(@Nullable ItemStack result, @NotNull ItemStack output) {
      if (result != null && !result.getType().isAir() && result.getAmount() == output.getAmount()) {
         ItemStack resultOne = result.clone();
         resultOne.setAmount(1);
         ItemStack outOne = output.clone();
         outOne.setAmount(1);
         return StackUtils.itemsMatch(outOne, resultOne);
      } else {
         return false;
      }
   }

   private static boolean matchesSmelting(@NotNull RecipeChoice choice, @Nullable ItemStack result, @NotNull List<ItemStack> merged, @NotNull ItemStack output) {
      if (merged.size() == 1 && result != null && !result.getType().isAir()) {
         ItemStack inputOne = merged.get(0).clone();
         inputOne.setAmount(1);
         if (!choice.test(inputOne)) {
            return false;
         } else {
            ItemStack resultOne = result.clone();
            resultOne.setAmount(1);
            ItemStack outOne = output.clone();
            outOne.setAmount(1);
            return StackUtils.itemsMatch(outOne, resultOne) && result.getAmount() == output.getAmount();
         }
      } else {
         return false;
      }
   }

   private static boolean sameConsumption(@NotNull List<ItemStack> card, @NotNull List<ItemStack> recipe) {
      List<ItemStack> need = AssemblyCard.mergeInputs(recipe);
      if (card.size() != need.size()) {
         return false;
      } else {
         boolean[] used = new boolean[need.size()];

         for (ItemStack cardItem : card) {
            ItemStack cardOne = cardItem.clone();
            cardOne.setAmount(1);
            boolean hit = false;

            for (int i = 0; i < need.size(); i++) {
               if (!used[i]) {
                  ItemStack recipeItem = need.get(i);
                  ItemStack recipeOne = recipeItem.clone();
                  recipeOne.setAmount(1);
                  if (StackUtils.itemsMatch(cardOne, recipeOne) && recipeItem.getAmount() == cardItem.getAmount()) {
                     used[i] = true;
                     hit = true;
                     break;
                  }
               }
            }

            if (!hit) {
               return false;
            }
         }

         return true;
      }
   }
}
