package com.ytdd9527.networksexpansion.core.items.machines;

import com.balugaq.netex.api.enums.CraftType;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class AdvancedAutoCrafter extends AutoCrafter {
   public AdvancedAutoCrafter(
      @NotNull ItemGroup itemGroup,
      @NotNull SlimefunItemStack item,
      @NotNull RecipeType recipeType,
      @NotNull ItemStack[] recipe,
      int chargePerCraft,
      boolean withholding
   ) {
      super(itemGroup, item, recipeType, recipe, chargePerCraft, withholding);
   }

   @Override
   public boolean canBlueprintStack() {
      return true;
   }

   public AdvancedAutoCrafter(ItemGroup var1, SlimefunItemStack var2, RecipeType var3, ItemStack[] var4, int var5, boolean var6, CraftType var7) {
      super(var1, var2, var3, var4, var5, var6, var7);
   }
}
