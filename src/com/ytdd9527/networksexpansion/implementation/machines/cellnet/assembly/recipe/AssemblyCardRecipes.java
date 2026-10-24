package com.ytdd9527.networksexpansion.implementation.machines.cellnet.assembly.recipe;

import com.balugaq.netex.api.enums.CraftType;
import com.balugaq.netex.utils.Debug;
import com.balugaq.netex.utils.Lang;
import com.ytdd9527.networksexpansion.utils.itemstacks.ItemStackUtil;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class AssemblyCardRecipes {
   private static final int GRID_SLOTS = 9;

   private AssemblyCardRecipes() {
   }

   @Nullable
   public static AssemblyCardRecipes.MatchedRecipe resolveRecipe(@NotNull Player player, @NotNull ItemStack[] grid) {
      AssemblyCardRecipes.MatchedRecipe matched = matchCraftTypeRecipe(grid);
      if (matched != null) {
         SlimefunItem result = SlimefunItem.getByItem(matched.output());
         if (result != null && result.isDisabled()) {
            player.sendMessage(Lang.getString("messages.cellnet.assemblycard.save_failed_disabled"));
            return null;
         } else {
            return matched.output().getType() == Material.AIR ? null : matched;
         }
      } else {
         return matchVanillaRecipe(player, grid);
      }
   }

   @Nullable
   private static AssemblyCardRecipes.MatchedRecipe matchCraftTypeRecipe(@NotNull ItemStack[] grid) {
      try {
         for (Entry<CraftType, Set<Entry<ItemStack[], ItemStack>>> craftEntry : CraftType.map().entrySet()) {
            for (Entry<ItemStack[], ItemStack> recipe : craftEntry.getValue()) {
               if (craftEntry.getKey().testRecipe(grid, recipe.getKey())) {
                  return new AssemblyCardRecipes.MatchedRecipe(recipe.getValue(), canonicalize(recipe.getKey()));
               }
            }
         }
      } catch (Exception var5) {
         Debug.trace(var5, "Assembly card recipe detection (CraftType) error, falling back to vanilla detection");
      }

      return null;
   }

   @Nullable
   private static AssemblyCardRecipes.MatchedRecipe matchVanillaRecipe(@NotNull Player player, @NotNull ItemStack[] grid) {
      ItemStack vanilla = Bukkit.craftItem((ItemStack[])grid.clone(), player.getWorld(), player);
      if (vanilla != null && vanilla.getType() != Material.AIR) {
         ItemStack[] canonicalInputs = new ItemStack[9];

         for (int k = 0; k < 9 && k < grid.length; k++) {
            if (grid[k] != null) {
               canonicalInputs[k] = StackUtils.getAsQuantity(grid[k], 1);
            }
         }

         return new AssemblyCardRecipes.MatchedRecipe(vanilla, canonicalInputs);
      } else {
         return null;
      }
   }

   @NotNull
   private static ItemStack[] canonicalize(@NotNull ItemStack[] recipeInputs) {
      ItemStack[] canonicalInputs = (ItemStack[])recipeInputs.clone();

      for (int k = 0; k < canonicalInputs.length; k++) {
         if (canonicalInputs[k] != null) {
            canonicalInputs[k] = ItemStackUtil.getCleanItem(canonicalInputs[k]);
         }
      }

      return canonicalInputs;
   }

   public static boolean passesSaveLimits(@NotNull Player player, @NotNull List<ItemStack> merged, @NotNull ItemStack crafted) {
      ItemStack outOne = crafted.clone();
      outOne.setAmount(1);

      for (ItemStack input : merged) {
         ItemStack inOne = input.clone();
         inOne.setAmount(1);
         if (StackUtils.itemsMatch(outOne, inOne)) {
            player.sendMessage(Lang.getString("messages.cellnet.assemblycard.save_failed_same"));
            return false;
         }
      }

      int total = 0;

      for (ItemStack item : merged) {
         total += item.getAmount();
      }

      if (total <= 1152 && merged.size() <= 18) {
         return true;
      } else {
         player.sendMessage(Lang.getString("messages.cellnet.assemblycard.save_failed_overflow", 18, 1152));
         return false;
      }
   }

   @NotNull
   public static ItemStack[] padToSlots(@Nullable ItemStack[] canonicalInputs) {
      ItemStack[] padded = new ItemStack[9];
      if (canonicalInputs == null) {
         return padded;
      } else {
         int target = 0;

         for (ItemStack input : canonicalInputs) {
            if (input != null && !input.getType().isAir() && target < 9) {
               padded[target++] = input.clone();
            }
         }

         return padded;
      }
   }

   public static record MatchedRecipe(@NotNull ItemStack output, @NotNull ItemStack[] canonicalInputs) {
   }
}
