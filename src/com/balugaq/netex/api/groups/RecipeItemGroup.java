package com.balugaq.netex.api.groups;

import com.balugaq.netex.api.menu.common.SlimefunItem3x3RecipeMenu;
import com.balugaq.netex.api.menu.common.SlimefunItem6x6RecipeMenu;
import com.ytdd9527.networksexpansion.utils.itemstacks.ItemStackUtil;
import io.github.sefiraat.networks.utils.Keys;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.groups.FlexItemGroup;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.core.guide.GuideHistory;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuide;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuideMode;
import java.util.HashMap;
import java.util.Map;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RecipeItemGroup extends FlexItemGroup {
   private static final Map<String, RecipeItemGroup> ID_MAP = new HashMap<>();
   private static final int SMALL_LIMIT = 9;
   private static final int BIG_LIMIT = 36;
   @NotNull
   private final String id;
   private final int page;

   public RecipeItemGroup(@NotNull NamespacedKey key, @NotNull SlimefunItem slimefunItem) {
      super(key, ItemStackUtil.getCleanItem(ItemStackUtil.cloneWithoutNBT(slimefunItem.getItem())));
      this.id = slimefunItem.getId();
      this.page = 1;
   }

   public RecipeItemGroup(@NotNull NamespacedKey key, @NotNull SlimefunItem slimefunItem, int page) {
      super(key, ItemStackUtil.getCleanItem(ItemStackUtil.cloneWithoutNBT(slimefunItem.getItem())));
      this.id = slimefunItem.getId();
      this.page = page;
   }

   @Nullable
   public static RecipeItemGroup getByItemStack(
      @NotNull Player player, @NotNull PlayerProfile playerProfile, @NotNull SlimefunGuideMode slimefunGuideMode, @Nullable ItemStack itemStack, int page
   ) {
      SlimefunItem slimefunItem = SlimefunItem.getByItem(itemStack);
      if (slimefunItem != null) {
         if (!playerProfile.hasUnlocked(slimefunItem.getResearch())) {
            return null;
         } else if (page == 1) {
            if (ID_MAP.containsKey(slimefunItem.getId())) {
               return ID_MAP.get(slimefunItem.getId());
            } else {
               synchronized (ID_MAP) {
                  if (ID_MAP.containsKey(slimefunItem.getId())) {
                     return ID_MAP.get(slimefunItem.getId());
                  } else {
                     RecipeItemGroup recipeItemGroup = new RecipeItemGroup(
                        Keys.newKey("SLIMEFUN_ITEM" + slimefunItem.getId().hashCode() + "_" + page), slimefunItem
                     );
                     ID_MAP.put(slimefunItem.getId(), recipeItemGroup);
                     return recipeItemGroup;
                  }
               }
            }
         } else {
            return new RecipeItemGroup(Keys.newKey("SLIMEFUN_ITEM" + slimefunItem.getId().hashCode()), slimefunItem, page);
         }
      } else {
         return !ItemStackUtil.isItemNull(itemStack) ? null : null;
      }
   }

   @Nullable
   public static RecipeItemGroup getByItemStack(
      @NotNull Player player, @NotNull PlayerProfile playerProfile, @NotNull SlimefunGuideMode slimefunGuideMode, @Nullable ItemStack itemStack
   ) {
      return getByItemStack(player, playerProfile, slimefunGuideMode, itemStack, 1);
   }

   @Nullable
   public static RecipeItemGroup getBySlimefunItem(
      @NotNull Player player, @NotNull PlayerProfile playerProfile, @NotNull SlimefunGuideMode slimefunGuideMode, @Nullable SlimefunItem slimefunItem, int page
   ) {
      if (slimefunItem != null) {
         if (page == 1) {
            if (ID_MAP.containsKey(slimefunItem.getId())) {
               return ID_MAP.get(slimefunItem.getId());
            } else {
               synchronized (ID_MAP) {
                  if (ID_MAP.containsKey(slimefunItem.getId())) {
                     return ID_MAP.get(slimefunItem.getId());
                  } else {
                     RecipeItemGroup recipeItemGroup = new RecipeItemGroup(
                        Keys.newKey("SLIMEFUN_ITEM" + slimefunItem.getId().hashCode() + "_" + page), slimefunItem
                     );
                     ID_MAP.put(slimefunItem.getId(), recipeItemGroup);
                     return recipeItemGroup;
                  }
               }
            }
         } else {
            return new RecipeItemGroup(Keys.newKey("SLIMEFUN_ITEM" + slimefunItem.getId().hashCode()), slimefunItem, page);
         }
      } else {
         return null;
      }
   }

   @Nullable
   public static RecipeItemGroup getBySlimefunItem(
      @NotNull Player player, @NotNull PlayerProfile playerProfile, @NotNull SlimefunGuideMode slimefunGuideMode, @Nullable SlimefunItem slimefunItem
   ) {
      return getBySlimefunItem(player, playerProfile, slimefunGuideMode, slimefunItem, 1);
   }

   public boolean isVisible(@NotNull Player player, @NotNull PlayerProfile playerProfile, @NotNull SlimefunGuideMode slimefunGuideMode) {
      return false;
   }

   public void open(@NotNull Player player, @NotNull PlayerProfile playerProfile, @NotNull SlimefunGuideMode slimefunGuideMode) {
      playerProfile.getGuideHistory().add(this, this.page);
      ChestMenu chestMenu = this.generateMenu(player, playerProfile, slimefunGuideMode);
      if (chestMenu != null) {
         chestMenu.open(new Player[]{player});
      } else {
         GuideHistory guideHistory = playerProfile.getGuideHistory();
         SlimefunGuide.openMainMenu(playerProfile, slimefunGuideMode, guideHistory.getMainMenuPage());
      }
   }

   @Nullable
   private ChestMenu generateMenu(@NotNull Player player, @NotNull PlayerProfile playerProfile, @NotNull SlimefunGuideMode slimefunGuideMode) {
      SlimefunItem slimefunItem = SlimefunItem.getById(this.id);
      if (slimefunItem != null) {
         if (slimefunItem.getRecipe().length <= 9) {
            return new SlimefunItem3x3RecipeMenu(player, playerProfile, slimefunGuideMode, slimefunItem, this, this.page);
         } else {
            return slimefunItem.getRecipe().length <= 36
               ? new SlimefunItem6x6RecipeMenu(player, playerProfile, slimefunGuideMode, slimefunItem, this, this.page)
               : null;
         }
      } else {
         return null;
      }
   }
}
