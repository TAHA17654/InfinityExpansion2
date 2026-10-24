package com.ytdd9527.networksexpansion.core.items;

import com.balugaq.netex.api.interfaces.FeedbackSendable;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.NotConfigurable;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public abstract class SpecialSlimefunItem extends SlimefunItem implements FeedbackSendable {
   public SpecialSlimefunItem(
      @NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @Nullable @NotNull ItemStack[] recipe
   ) {
      super(itemGroup, item, recipeType, recipe);
   }

   public SpecialSlimefunItem(
      @NotNull ItemGroup itemGroup,
      @NotNull SlimefunItemStack item,
      @NotNull RecipeType recipeType,
      @Nullable @NotNull ItemStack[] recipe,
      @Nullable ItemStack recipeOutput
   ) {
      super(itemGroup, item, recipeType, recipe, recipeOutput);
   }

   public SpecialSlimefunItem(
      @NotNull ItemGroup itemGroup, @NotNull ItemStack item, @NotNull String id, @NotNull RecipeType recipeType, @Nullable @NotNull ItemStack[] recipe
   ) {
      super(itemGroup, item, id, recipeType, recipe);
   }

   public SpecialSlimefunItem(
      @NotNull ItemGroup itemGroup,
      @NotNull SlimefunItemStack item,
      @NotNull RecipeType recipeType,
      @Nullable @NotNull ItemStack[] recipe,
      @Range(from = 1L,to = 64L) int outputAmount
   ) {
      this(itemGroup, item, recipeType, recipe, StackUtils.getAsQuantity(item, outputAmount));
   }

   public void register(@NotNull SlimefunAddon addon) {
      super.register(addon);
      this.enchantable = false;
      this.disenchantable = true;
      if (!(this instanceof NotConfigurable)) {
         Slimefun.getItemCfg().setDefaultValue(this.getId() + ".allow-enchanting", this.enchantable);
         Slimefun.getItemCfg().setDefaultValue(this.getId() + ".allow-disenchanting", this.disenchantable);
      }
   }

   @NotNull
   public SpecialSlimefunItem registerThis() {
      this.register(Networks.getInstance());
      return this;
   }
}
