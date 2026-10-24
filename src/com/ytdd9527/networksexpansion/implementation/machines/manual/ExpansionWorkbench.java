package com.ytdd9527.networksexpansion.implementation.machines.manual;

import com.balugaq.netex.api.data.SuperRecipe;
import com.balugaq.netex.api.helpers.Icon;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.core.items.machines.AbstractManualCrafter;
import com.ytdd9527.networksexpansion.implementation.ExpansionRecipes;
import io.github.sefiraat.networks.utils.Keys;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.ParametersAreNonnullByDefault;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu.MenuClickHandler;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExpansionWorkbench extends AbstractManualCrafter {
   public static final List<SuperRecipe> RECIPES = new ArrayList<>();
   public static final RecipeType TYPE = new RecipeType(
      Keys.EXPANSION_WORKBENCH, Icon.RECIPE_TYPE_ITEMSTACK_EXPANSION_WORKBENCH_3x3, ExpansionWorkbench::addRecipe, new String[0]
   );
   public static final Map<Integer, ItemStack> BACKGROUNDS = new HashMap<>();
   private static final int[] BACKGROUND_SLOTS = new int[]{
      0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 13, 14, 15, 16, 17, 18, 22, 24, 26, 27, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44
   };
   private static final int[] RECIPE_SLOTS = new int[]{10, 11, 12, 19, 20, 21, 28, 29, 30};
   private static final int CRAFT_SLOT = 23;
   private static final int OUTPUT_SLOT = 25;

   @ParametersAreNonnullByDefault
   public ExpansionWorkbench(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe);
   }

   public static void addRecipe(ItemStack[] input, ItemStack output) {
      if (!Arrays.equals((Object[])input, (Object[])ExpansionRecipes.NULL)) {
         RECIPES.add(new SuperRecipe(true, input, output));
      }
   }

   @NotNull
   @Override
   public List<SuperRecipe> getRecipes() {
      return RECIPES;
   }

   @Override
   public int[] getInputSlots() {
      return RECIPE_SLOTS;
   }

   @Override
   public int[] getOutputSlots() {
      return new int[]{25};
   }

   @Override
   public int getHandleSlot() {
      return 23;
   }

   @Override
   public boolean isTransportable() {
      return true;
   }

   @NotNull
   @Override
   public Map<Integer, ItemStack> getBackgrounds() {
      return BACKGROUNDS;
   }

   @Nullable
   @Override
   public Map<Integer, MenuClickHandler> getMenuClickHandlers() {
      return null;
   }

   @NotNull
   @Override
   public BlockPlaceHandler getMachineBlockPlaceHandler() {
      return new BlockPlaceHandler(false) {
         public void onPlayerPlace(@NotNull BlockPlaceEvent e) {
         }
      };
   }

   @NotNull
   @Override
   public BlockBreakHandler getMachineBlockBreakHandler() {
      return new BlockBreakHandler(false, false) {
         @ParametersAreNonnullByDefault
         public void onPlayerBreak(BlockBreakEvent event, ItemStack itemStack, List<ItemStack> drops) {
            BlockMenu menu = StorageCacheUtils.getMenu(event.getBlock().getLocation());
            if (menu != null) {
               menu.dropItems(menu.getLocation(), ExpansionWorkbench.RECIPE_SLOTS);
               menu.dropItems(menu.getLocation(), new int[]{25});
            }
         }
      };
   }

   @Nullable
   @Override
   public BlockTicker getMachineBlockTicker() {
      return null;
   }

   public int getCapacity() {
      return 0;
   }

   static {
      for (int slot : BACKGROUND_SLOTS) {
         BACKGROUNDS.put(slot, Icon.BACKGROUND_STACK_3x3);
      }

      BACKGROUNDS.put(23, Icon.CRAFT_BUTTON_STACK_3x3);
   }
}
