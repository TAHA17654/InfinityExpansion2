package io.github.sefiraat.networks.network.stackcaches;

import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlueprintInstance extends ItemStackCache {
   public static final BlueprintInstance INVALID = new BlueprintInstance(new ItemStack[0], new ItemStack(Material.AIR));
   private final ItemStack[] recipeItems;
   @Nullable
   private Recipe recipe = null;

   public BlueprintInstance(@Nullable ItemStack[] recipeItems, @NotNull ItemStack expectedOutput) {
      super(expectedOutput);
      this.recipeItems = recipeItems;
   }

   @Nullable
   public Recipe getRecipe() {
      return this.recipe;
   }

   public void setRecipe(@Nullable Recipe recipe) {
      this.recipe = recipe;
   }

   public void generateVanillaRecipe(@NotNull World world) {
      if (this.recipe == null) {
         this.recipe = Bukkit.getCraftingRecipe(this.recipeItems, world);
      }
   }

   @Generated
   public ItemStack[] getRecipeItems() {
      return this.recipeItems;
   }
}
