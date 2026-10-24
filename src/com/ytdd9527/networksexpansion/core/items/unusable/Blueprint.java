package com.ytdd9527.networksexpansion.core.items.unusable;

import com.balugaq.netex.api.enums.CraftType;
import com.balugaq.netex.api.interfaces.CraftTyped;
import com.balugaq.netex.utils.Lang;
import io.github.sefiraat.networks.network.stackcaches.BlueprintInstance;
import io.github.sefiraat.networks.utils.Keys;
import io.github.sefiraat.networks.utils.Theme;
import io.github.sefiraat.networks.utils.datatypes.DataTypeMethods;
import io.github.sefiraat.networks.utils.datatypes.PersistentCraftingBlueprintType;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.DistinctiveItem;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;
import net.guizhanss.minecraft.guizhanlib.gugu.minecraft.helpers.inventory.ItemStackHelper;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class Blueprint extends UnusableSlimefunItem implements DistinctiveItem, CraftTyped {
   private CraftType craftType = CraftType.CRAFTING;

   public Blueprint(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe);
   }

   @ParametersAreNonnullByDefault
   public static void setBlueprint(ItemStack blueprint, ItemStack[] recipe, ItemStack output) {
      ItemMeta itemMeta = blueprint.getItemMeta();
      DataTypeMethods.setCustom(itemMeta, Keys.BLUEPRINT_INSTANCE, PersistentCraftingBlueprintType.TYPE, new BlueprintInstance(recipe, output));
      List<String> lore = new ArrayList<>();
      lore.add(Lang.getString("messages.blueprint.title"));

      for (ItemStack item : recipe) {
         if (item == null) {
            lore.add(Theme.PASSIVE + "- " + Lang.getString("messages.blueprint.empty"));
         } else {
            lore.add(Theme.PASSIVE + "- " + ItemStackHelper.getDisplayName(item));
         }
      }

      lore.add("");
      lore.add(Lang.getString("messages.blueprint.output"));
      lore.add(Theme.PASSIVE + "- " + ItemStackHelper.getDisplayName(output));
      itemMeta.setLore(lore);
      blueprint.setItemMeta(itemMeta);
   }

   public boolean canStack(@NotNull ItemMeta meta1, @NotNull ItemMeta meta2) {
      return meta1.getPersistentDataContainer().equals(meta2.getPersistentDataContainer());
   }

   public Blueprint(ItemGroup var1, SlimefunItemStack var2, RecipeType var3, ItemStack[] var4, CraftType var5) {
      this(var1, var2, var3, var4);
      this.craftType = var5;
   }

   @Override
   public CraftType craftType() {
      return this.craftType;
   }
}
