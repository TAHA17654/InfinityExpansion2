package com.balugaq.netex.api.enums;

import com.balugaq.netex.api.helpers.SupportedAncientAltarRecipes;
import com.balugaq.netex.api.helpers.SupportedArmorForgeRecipes;
import com.balugaq.netex.api.helpers.SupportedCompressorRecipes;
import com.balugaq.netex.api.helpers.SupportedCraftingTableRecipes;
import com.balugaq.netex.api.helpers.SupportedExpansionWorkbenchRecipes;
import com.balugaq.netex.api.helpers.SupportedGrindStoneRecipes;
import com.balugaq.netex.api.helpers.SupportedJuicerRecipes;
import com.balugaq.netex.api.helpers.SupportedMagicWorkbenchRecipes;
import com.balugaq.netex.api.helpers.SupportedOreCrusherRecipes;
import com.balugaq.netex.api.helpers.SupportedPressureChamberRecipes;
import com.balugaq.netex.api.helpers.SupportedQuantumWorkbenchRecipes;
import com.balugaq.netex.api.helpers.SupportedSmelteryRecipes;
import com.balugaq.netex.utils.Lang;
import com.ytdd9527.networksexpansion.core.items.unusable.Blueprint;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiPredicate;
import lombok.Generated;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

@NullMarked
public enum CraftType {
   ANCIENT_ALTAR(SupportedAncientAltarRecipes.getRecipes(), SupportedAncientAltarRecipes::testRecipe),
   ARMOR_FORGE(SupportedArmorForgeRecipes.getRecipes(), SupportedArmorForgeRecipes::testRecipe),
   COMPRESSOR(SupportedCompressorRecipes.getRecipes(), SupportedCompressorRecipes::testRecipe),
   CRAFTING(SupportedCraftingTableRecipes.getRecipes(), SupportedCraftingTableRecipes::testRecipe),
   EXPANSION_WORKBENCH(SupportedExpansionWorkbenchRecipes.getRecipes(), SupportedExpansionWorkbenchRecipes::testRecipe),
   GRIND_STONE(SupportedGrindStoneRecipes.getRecipes(), SupportedGrindStoneRecipes::testRecipe),
   JUICER(SupportedJuicerRecipes.getRecipes(), SupportedJuicerRecipes::testRecipe),
   MAGIC_WORKBENCH(SupportedMagicWorkbenchRecipes.getRecipes(), SupportedMagicWorkbenchRecipes::testRecipe),
   ORE_CRUSHER(SupportedOreCrusherRecipes.getRecipes(), SupportedOreCrusherRecipes::testRecipe),
   PRESSURE_CHAMBER(SupportedPressureChamberRecipes.getRecipes(), SupportedPressureChamberRecipes::testRecipe),
   QUANTUM_WORKBENCH(SupportedQuantumWorkbenchRecipes.getRecipes(), SupportedQuantumWorkbenchRecipes::testRecipe),
   SMELTERY(SupportedSmelteryRecipes.getRecipes(), SupportedSmelteryRecipes::testRecipe);

   private static final Map<CraftType, Set<Entry<ItemStack[], ItemStack>>> map = buildMap();
   private final Set<Entry<ItemStack[], ItemStack>> recipeEntries;
   private final BiPredicate<ItemStack[], ItemStack[]> testRecipe;

   private CraftType(Map<ItemStack[], ItemStack> recipes, BiPredicate<ItemStack[], ItemStack[]> testRecipe) {
      this.recipeEntries = recipes.entrySet();
      this.testRecipe = testRecipe;
   }

   private static Map<CraftType, Set<Entry<ItemStack[], ItemStack>>> buildMap() {
      Map<CraftType, Set<Entry<ItemStack[], ItemStack>>> result = new HashMap<>();

      for (CraftType type : values()) {
         result.put(type, type.recipeEntries);
      }

      return Collections.unmodifiableMap(result);
   }

   public boolean testRecipe(ItemStack[] inputs, ItemStack[] recipe) {
      return this.testRecipe.test(inputs, recipe);
   }

   public boolean isValidBlueprint(SlimefunItem item) {
      return item instanceof Blueprint blueprint ? blueprint.craftType() == this : false;
   }

   public void blueprintSetter(ItemStack itemStack, ItemStack[] inputs, ItemStack crafted) {
      Blueprint.setBlueprint(itemStack, inputs, crafted);
   }

   public String translate() {
      return Lang.getString("messages.normal-operation.common.crafter_types." + this.name().toLowerCase());
   }

   public static Collection<Set<Entry<ItemStack[], ItemStack>>> entries() {
      return Arrays.stream(values()).map(CraftType::getRecipeEntries).toList();
   }

   public static Map<CraftType, Set<Entry<ItemStack[], ItemStack>>> map() {
      return map;
   }

   @Generated
   public Set<Entry<ItemStack[], ItemStack>> getRecipeEntries() {
      return this.recipeEntries;
   }

   @FunctionalInterface
   public interface BlueprintSetter {
      void apply(ItemStack var1, ItemStack[] var2, ItemStack var3);
   }
}
