package com.balugaq.netex.api.menu.common;

import com.balugaq.netex.api.groups.CraftItemGroup;
import com.balugaq.netex.api.groups.RecipeItemGroup;
import com.balugaq.netex.api.groups.TypeItemGroup;
import com.balugaq.netex.api.helpers.Icon;
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
import org.jetbrains.annotations.Nullable;

public class SlimefunItem6x6RecipeMenu extends ChestMenu {
   private final int BACK_SLOT = 1;
   private final int RECIPE_TYPE = 10;
   private final int RECIPE_RESULT = 37;
   private final int[] RECIPE_CONTENT = new int[]{
      3, 4, 5, 6, 7, 8, 12, 13, 14, 15, 16, 17, 21, 22, 23, 24, 25, 26, 30, 31, 32, 33, 34, 35, 39, 40, 41, 42, 43, 44, 48, 49, 50, 51, 52, 53
   };
   private final int[] BORDER = new int[]{0, 2, 11, 18, 19, 20, 27, 28, 29, 36, 38, 45, 47};
   private final int INFO_SLOT = 9;
   private final int WORK_BUTTON = 46;
   private final int WORK_BACK_SLOT = 0;
   private final int WORK_PREVIOUS_SLOT = 1;
   private final int WORK_NEXT_SLOT = 7;
   private final int[] WORK_BORDER = new int[]{2, 3, 4, 5, 6, 8};
   private final int[] WORK_CONTENT = new int[]{9, 18, 10, 19, 11, 20, 12, 21, 13, 22, 14, 23, 15, 24, 16, 25, 17, 26};
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

   public SlimefunItem6x6RecipeMenu(
      @NotNull Player player,
      @NotNull PlayerProfile playerProfile,
      @NotNull SlimefunGuideMode slimefunGuideMode,
      @NotNull SlimefunItem slimefunItem,
      @NotNull ItemGroup itemGroup
   ) {
      this(player, playerProfile, slimefunGuideMode, slimefunItem, itemGroup, 1);
   }

   public SlimefunItem6x6RecipeMenu(
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
      GuideHistory guideHistory = playerProfile.getGuideHistory();
      this.addItem(1, ItemStackUtil.getCleanItem(ChestMenuUtils.getBackButton(player, new String[0])));
      this.addMenuClickHandler(1, (pl, s, is, action) -> {
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
      this.addItem(37, ItemStackUtil.getCleanItem(ItemStackUtil.cloneWithoutNBT(slimefunItem.getRecipeOutput())));
      this.addMenuClickHandler(37, (p, slotx, item, action) -> {
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

      for (int slot : this.BORDER) {
         this.addItem(slot, ItemStackUtil.getCleanItem(ChestMenuUtils.getBackground()));
         this.addMenuClickHandler(slot, ChestMenuUtils.getEmptyClickHandler());
      }

      if (slimefunItem instanceof RecipeDisplayItem recipeDisplayItem && !recipeDisplayItem.getDisplayRecipes().isEmpty()) {
         this.addItem(46, ItemStackUtil.getCleanItem(Icon.RECIPE_ICON));
         this.addMenuClickHandler(46, (p, slotx, item, action) -> {
            ChestMenu chestMenu = this.setupWorkContent(page);
            if (chestMenu != null) {
               chestMenu.open(new Player[]{player});
            }

            return false;
         });
         return;
      }

      this.addItem(46, ItemStackUtil.getCleanItem(ChestMenuUtils.getBackground()));
      this.addMenuClickHandler(46, ChestMenuUtils.getEmptyClickHandler());
   }

   @Nullable
   private ChestMenu setupWorkContent(int page) {
      if (!(this.slimefunItem instanceof RecipeDisplayItem recipeDisplayItem)) {
         return null;
      } else {
         ChestMenu chestMenu = new ChestMenu(this.slimefunItem.getItemName());

         for (int slot : this.WORK_BORDER) {
            chestMenu.addItem(slot, ItemStackUtil.getCleanItem(ChestMenuUtils.getBackground()));
            chestMenu.addMenuClickHandler(slot, ChestMenuUtils.getEmptyClickHandler());
         }

         List<ItemStack> displayRecipes = recipeDisplayItem.getDisplayRecipes();
         chestMenu.addItem(0, ItemStackUtil.getCleanItem(ChestMenuUtils.getBackButton(this.player, new String[0])));
         chestMenu.addMenuClickHandler(0, (p, slot, item, action) -> {
            GuideHistory guideHistory = this.playerProfile.getGuideHistory();
            GuideUtil.removeLastEntry(guideHistory);
            if (this.itemGroup instanceof RecipeItemGroup recipeItemGroup) {
               recipeItemGroup.open(this.player, this.playerProfile, this.slimefunGuideMode);
            }

            return false;
         });
         chestMenu.addItem(
            1, ItemStackUtil.getCleanItem(ChestMenuUtils.getPreviousButton(this.player, page, (displayRecipes.size() - 1) / this.WORK_CONTENT.length + 1))
         );
         chestMenu.addMenuClickHandler(1, (p, slot, item, action) -> {
            ChestMenu menu = this.setupWorkContent(Math.max(page - 1, 1));
            if (menu != null) {
               menu.open(new Player[]{this.player});
            }

            return false;
         });
         chestMenu.addItem(
            7, ItemStackUtil.getCleanItem(ChestMenuUtils.getNextButton(this.player, page, (displayRecipes.size() - 1) / this.WORK_CONTENT.length + 1))
         );
         chestMenu.addMenuClickHandler(7, (p, slot, item, action) -> {
            ChestMenu menu = this.setupWorkContent(Math.min(page + 1, (displayRecipes.size() - 1) / this.WORK_CONTENT.length + 1));
            if (menu != null) {
               menu.open(new Player[]{this.player});
            }

            return false;
         });

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

               chestMenu.addItem(this.WORK_CONTENT[i], ItemStackUtil.getCleanItem(ItemStackUtil.cloneWithoutNBT(icon)));
               chestMenu.addMenuClickHandler(this.WORK_CONTENT[i], (p, slot, item, action) -> {
                  RecipeItemGroup recipeItemGroup = RecipeItemGroup.getByItemStack(this.player, this.playerProfile, this.slimefunGuideMode, itemStack);
                  if (recipeItemGroup != null) {
                     recipeItemGroup.open(this.player, this.playerProfile, this.slimefunGuideMode);
                  }

                  return false;
               });
            } else {
               chestMenu.addItem(this.WORK_CONTENT[i], ItemStackUtil.getCleanItem(null));
               chestMenu.addMenuClickHandler(this.WORK_CONTENT[i], ChestMenuUtils.getEmptyClickHandler());
            }
         }

         return chestMenu;
      }
   }
}
