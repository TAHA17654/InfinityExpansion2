package com.balugaq.netex.api.menu.common;

import com.balugaq.netex.api.groups.CraftItemGroup;
import com.balugaq.netex.api.groups.RecipeItemGroup;
import com.balugaq.netex.api.groups.TypeItemGroup;
import com.balugaq.netex.utils.GuideUtil;
import com.balugaq.netex.utils.Lang;
import com.ytdd9527.networksexpansion.utils.itemstacks.ItemStackUtil;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.api.researches.Research;
import io.github.thebusybiscuit.slimefun4.core.attributes.RecipeDisplayItem;
import io.github.thebusybiscuit.slimefun4.core.guide.GuideHistory;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuide;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuideMode;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.List;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SlimefunItem3x3RecipeMenu extends ChestMenu {
   private final int BACK_SLOT = 1;
   private final int RECIPE_TYPE = 10;
   private final int RECIPE_RESULT = 16;
   private final int[] RECIPE_CONTENT = new int[]{3, 4, 5, 12, 13, 14, 21, 22, 23};
   private final int INFO_SLOT = 9;
   private final int[] BORDER = new int[]{27, 29, 30, 31, 32, 33, 35};
   private final int PREVIOUS_SLOT = 28;
   private final int NEXT_SLOT = 34;
   private final int[] WORK_CONTENT = new int[]{36, 45, 37, 46, 38, 47, 39, 48, 40, 49, 41, 50, 42, 51, 43, 52, 44, 53};
   @NotNull
   private final Player player;
   @NotNull
   private final PlayerProfile playerProfile;
   @NotNull
   private final SlimefunGuideMode slimefunGuideMode;
   @NotNull
   private final SlimefunItem slimefunItem;
   @NotNull
   private final ItemGroup itemGroup;

   public SlimefunItem3x3RecipeMenu(
      @NotNull Player player,
      @NotNull PlayerProfile playerProfile,
      @NotNull SlimefunGuideMode slimefunGuideMode,
      @NotNull SlimefunItem slimefunItem,
      @NotNull ItemGroup itemGroup
   ) {
      this(player, playerProfile, slimefunGuideMode, slimefunItem, itemGroup, 0);
   }

   public SlimefunItem3x3RecipeMenu(
      @NotNull Player player,
      @NotNull PlayerProfile playerProfile,
      @NotNull SlimefunGuideMode slimefunGuideMode,
      @NotNull SlimefunItem slimefunItem,
      @NotNull ItemGroup itemGroup,
      int page
   ) {
      super(slimefunItem.getItemName());
      this.player = player;
      this.playerProfile = playerProfile;
      this.slimefunGuideMode = slimefunGuideMode;
      this.slimefunItem = slimefunItem;
      this.itemGroup = itemGroup;
      this.setEmptySlotsClickable(false);
      this.addMenuOpeningHandler(pl -> pl.playSound(pl.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0F, 1.0F));
      this.addItem(1, ItemStackUtil.getCleanItem(ChestMenuUtils.getBackButton(player, new String[0])));
      this.addMenuClickHandler(1, (pl, s, is, action) -> {
         GuideHistory guideHistory = playerProfile.getGuideHistory();
         if (action.isShiftClicked()) {
            SlimefunGuide.openMainMenu(playerProfile, slimefunGuideMode, guideHistory.getMainMenuPage());
         } else {
            guideHistory.goBack(Slimefun.getRegistry().getSlimefunGuide(SlimefunGuideMode.SURVIVAL_MODE));
         }

         return false;
      });
      this.addItem(10, ItemStackUtil.getCleanItem(ItemStackUtil.cloneWithoutNBT(slimefunItem.getRecipeType().toItem())));
      this.addMenuClickHandler(10, (p, slotx, item, action) -> {
         TypeItemGroup typeItemGroup = TypeItemGroup.getByRecipeType(slimefunItem.getRecipeType());
         typeItemGroup.open(player, playerProfile, slimefunGuideMode);
         return false;
      });
      this.addItem(16, ItemStackUtil.getCleanItem(ItemStackUtil.cloneWithoutNBT(slimefunItem.getRecipeOutput())));
      this.addMenuClickHandler(16, (p, slotx, item, action) -> {
         CraftItemGroup craftItemGroup = CraftItemGroup.getBySlimefunItem(slimefunItem);
         craftItemGroup.open(player, playerProfile, slimefunGuideMode);
         return false;
      });

      for (int i = 0; i < slimefunItem.getRecipe().length; i++) {
         ItemStack itemStack = slimefunItem.getRecipe()[i];
         ItemStack icon = itemStack;
         SlimefunItem sfItem = SlimefunItem.getByItem(itemStack);
         if (sfItem != null) {
            Research research = sfItem.getResearch();
            if (research != null && !this.playerProfile.hasUnlocked(research)) {
               icon = ChestMenuUtils.getNotResearchedItem();
               ItemStackUtil.setLore(
                  icon,
                  "\u00a77" + research.getName(player),
                  "\u00a74\u00a7l" + Slimefun.getLocalization().getMessage(player, "guide.locked"),
                  "",
                  Lang.getString("messages.guide.click-to-research"),
                  "",
                  Lang.getString("messages.guide.cost") + research.getCost() + Lang.getString("messages.guide.cost-level")
               );
            }
         }

         this.addItem(this.RECIPE_CONTENT[i], ItemStackUtil.getCleanItem(ItemStackUtil.cloneWithoutNBT(icon)));
         this.addMenuClickHandler(this.RECIPE_CONTENT[i], (p, slotx, item, action) -> {
            RecipeItemGroup recipeItemGroup = RecipeItemGroup.getByItemStack(player, playerProfile, slimefunGuideMode, itemStack);
            if (recipeItemGroup != null) {
               recipeItemGroup.open(player, playerProfile, slimefunGuideMode);
            }

            return false;
         });
      }

      this.addMenuClickHandler(9, ChestMenuUtils.getEmptyClickHandler());
      if (slimefunItem instanceof RecipeDisplayItem) {
         List<ItemStack> displayRecipeItemList = ((RecipeDisplayItem)slimefunItem).getDisplayRecipes();
         if (!displayRecipeItemList.isEmpty()) {
            for (int slot : this.BORDER) {
               this.addItem(slot, ItemStackUtil.getCleanItem(ChestMenuUtils.getBackground()));
               this.addMenuClickHandler(slot, ChestMenuUtils.getEmptyClickHandler());
            }

            this.setupWorkContent(page);
         }
      }
   }

   private void setupWorkContent(int page) {
      if (this.slimefunItem instanceof RecipeDisplayItem recipeDisplayItem) {
         List<ItemStack> displayRecipes = recipeDisplayItem.getDisplayRecipes();
         this.addItem(
            28, ItemStackUtil.getCleanItem(ChestMenuUtils.getPreviousButton(this.player, page, (displayRecipes.size() - 1) / this.WORK_CONTENT.length + 1))
         );
         this.addMenuClickHandler(
            28,
            (p, slot, item, action) -> {
               RecipeItemGroup recipeItemGroup = RecipeItemGroup.getByItemStack(
                  this.player, this.playerProfile, this.slimefunGuideMode, this.slimefunItem.getItem(), Math.max(page - 1, 1)
               );
               if (recipeItemGroup != null) {
                  GuideUtil.removeLastEntry(this.playerProfile.getGuideHistory());
                  recipeItemGroup.open(this.player, this.playerProfile, this.slimefunGuideMode);
               }

               return false;
            }
         );
         this.addItem(
            34, ItemStackUtil.getCleanItem(ChestMenuUtils.getNextButton(this.player, page, (displayRecipes.size() - 1) / this.WORK_CONTENT.length + 1))
         );
         this.addMenuClickHandler(
            34,
            (p, slot, item, action) -> {
               RecipeItemGroup recipeItemGroup = RecipeItemGroup.getByItemStack(
                  this.player,
                  this.playerProfile,
                  this.slimefunGuideMode,
                  this.slimefunItem.getItem(),
                  Math.min(page + 1, (displayRecipes.size() - 1) / this.WORK_CONTENT.length + 1)
               );
               if (recipeItemGroup != null) {
                  GuideUtil.removeLastEntry(this.playerProfile.getGuideHistory());
                  recipeItemGroup.open(this.player, this.playerProfile, this.slimefunGuideMode);
               }

               return false;
            }
         );

         for (int i = 0; i < this.WORK_CONTENT.length; i++) {
            int index = i + page * this.WORK_CONTENT.length - this.WORK_CONTENT.length;
            if (index < displayRecipes.size()) {
               ItemStack itemStack = displayRecipes.get(index);
               ItemStack icon = itemStack;
               SlimefunItem slimefunItem = SlimefunItem.getByItem(itemStack);
               if (slimefunItem != null) {
                  Research research = slimefunItem.getResearch();
                  if (research != null && !this.playerProfile.hasUnlocked(research)) {
                     icon = ChestMenuUtils.getNotResearchedItem();
                     ItemStackUtil.setLore(
                        icon,
                        "\u00a77" + research.getName(this.player),
                        "\u00a74\u00a7l" + Slimefun.getLocalization().getMessage(this.player, "guide.locked"),
                        "",
                        Lang.getString("messages.guide.click-to-research"),
                        "",
                        Lang.getString("messages.guide.cost") + research.getCost() + Lang.getString("messages.guide.cost-level")
                     );
                  }
               }

               this.addItem(this.WORK_CONTENT[i], ItemStackUtil.getCleanItem(ItemStackUtil.cloneWithoutNBT(icon)));
               this.addMenuClickHandler(this.WORK_CONTENT[i], (p, slot, item, action) -> {
                  RecipeItemGroup recipeItemGroup = RecipeItemGroup.getByItemStack(this.player, this.playerProfile, this.slimefunGuideMode, itemStack);
                  if (recipeItemGroup != null) {
                     recipeItemGroup.open(this.player, this.playerProfile, this.slimefunGuideMode);
                  }

                  return false;
               });
            } else {
               this.addItem(this.WORK_CONTENT[i], ItemStackUtil.getCleanItem(null));
               this.addMenuClickHandler(this.WORK_CONTENT[i], ChestMenuUtils.getEmptyClickHandler());
            }
         }
      }
   }
}
