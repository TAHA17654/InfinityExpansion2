package com.balugaq.netex.api.groups;

import com.balugaq.netex.utils.GuideUtil;
import com.balugaq.netex.utils.Lang;
import com.ytdd9527.networksexpansion.utils.itemstacks.ItemStackUtil;
import com.ytdd9527.networksexpansion.utils.registry.SlimefunCraftRegistry;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.utils.Keys;
import io.github.thebusybiscuit.slimefun4.api.events.PlayerPreResearchEvent;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.groups.FlexItemGroup;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.api.researches.Research;
import io.github.thebusybiscuit.slimefun4.core.guide.GuideHistory;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuide;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuideMode;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class CraftItemGroup extends FlexItemGroup {
   private static final int BACK_SLOT = 1;
   private static final int PREVIOUS_SLOT = 3;
   private static final int NEXT_SLOT = 5;
   private static final int ICON_SLOT = 7;
   private static final int[] BORDER = new int[]{0, 2, 4, 6, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17};
   private static final int[] MAIN_CONTENT = new int[]{
      18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53
   };
   private static final JavaPlugin JAVA_PLUGIN = Networks.getInstance();
   private static final Map<SlimefunItem, CraftItemGroup> ITEM_MAP = new LinkedHashMap<>();
   private final int page;
   @NotNull
   private final SlimefunItem slimefunItem;
   @NotNull
   private final List<SlimefunItem> slimefunItemList;
   private Map<Integer, CraftItemGroup> pageMap = new LinkedHashMap<>();

   protected CraftItemGroup(@NotNull NamespacedKey key, @NotNull SlimefunItem slimefunItem) {
      super(key, ItemStackUtil.getCleanItem(ItemStackUtil.cloneWithoutNBT(slimefunItem.getItem())));
      this.page = 1;
      this.slimefunItem = slimefunItem;
      this.slimefunItemList = new ArrayList<>();

      for (SlimefunItem sfItem : SlimefunCraftRegistry.getInstance().getCraftSlimefunItemList(slimefunItem)) {
         if (!sfItem.isDisabled() && !sfItem.isHidden()) {
            this.slimefunItemList.add(sfItem);
         }
      }

      this.pageMap.put(1, this);
      ITEM_MAP.put(slimefunItem, this);
   }

   protected CraftItemGroup(@NotNull NamespacedKey key, @NotNull SlimefunItem slimefunItem, int page) {
      super(key, ItemStackUtil.getCleanItem(ItemStackUtil.cloneWithoutNBT(slimefunItem.getItem())));
      this.page = page;
      this.slimefunItem = slimefunItem;
      this.slimefunItemList = new ArrayList<>();

      for (SlimefunItem sfItem : SlimefunCraftRegistry.getInstance().getCraftSlimefunItemList(slimefunItem)) {
         if (!sfItem.isDisabled() && !sfItem.isHidden()) {
            this.slimefunItemList.add(sfItem);
         }
      }
   }

   @NotNull
   public static CraftItemGroup getBySlimefunItem(@NotNull SlimefunItem slimefunItem) {
      return new CraftItemGroup(Keys.newKey(slimefunItem.getId().hashCode() + ""), slimefunItem);
   }

   public boolean isVisible(@NotNull Player player, @NotNull PlayerProfile playerProfile, @NotNull SlimefunGuideMode slimefunGuideMode) {
      return false;
   }

   public void open(@NotNull Player player, @NotNull PlayerProfile playerProfile, @NotNull SlimefunGuideMode slimefunGuideMode) {
      playerProfile.getGuideHistory().add(this, this.page);
      this.generateMenu(player, playerProfile, slimefunGuideMode).open(new Player[]{player});
   }

   public void refresh(@NotNull Player player, @NotNull PlayerProfile playerProfile, @NotNull SlimefunGuideMode slimefunGuideMode) {
      GuideUtil.removeLastEntry(playerProfile.getGuideHistory());
      this.open(player, playerProfile, slimefunGuideMode);
   }

   @NotNull
   private ChestMenu generateMenu(@NotNull Player player, @NotNull PlayerProfile playerProfile, @NotNull SlimefunGuideMode slimefunGuideMode) {
      ChestMenu chestMenu = new ChestMenu(this.slimefunItem.getItemName());
      chestMenu.setEmptySlotsClickable(false);
      chestMenu.addMenuOpeningHandler(pl -> pl.playSound(pl.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0F, 1.0F));
      chestMenu.addItem(1, ItemStackUtil.getCleanItem(ChestMenuUtils.getBackButton(player, new String[0])));
      chestMenu.addMenuClickHandler(1, (pl, s, is, action) -> {
         GuideHistory guideHistory = playerProfile.getGuideHistory();
         if (action.isShiftClicked()) {
            SlimefunGuide.openMainMenu(playerProfile, slimefunGuideMode, guideHistory.getMainMenuPage());
         } else {
            guideHistory.goBack(Slimefun.getRegistry().getSlimefunGuide(SlimefunGuideMode.SURVIVAL_MODE));
         }

         return false;
      });
      chestMenu.addItem(
         3, ItemStackUtil.getCleanItem(ChestMenuUtils.getPreviousButton(player, this.page, (this.slimefunItemList.size() - 1) / MAIN_CONTENT.length + 1))
      );
      chestMenu.addMenuClickHandler(3, (p, slot, item, action) -> {
         GuideUtil.removeLastEntry(playerProfile.getGuideHistory());
         CraftItemGroup craftItemGroup = this.getByPage(Math.max(this.page - 1, 1));
         craftItemGroup.open(player, playerProfile, slimefunGuideMode);
         return false;
      });
      chestMenu.addItem(
         5, ItemStackUtil.getCleanItem(ChestMenuUtils.getNextButton(player, this.page, (this.slimefunItemList.size() - 1) / MAIN_CONTENT.length + 1))
      );
      chestMenu.addMenuClickHandler(5, (p, slot, item, action) -> {
         GuideUtil.removeLastEntry(playerProfile.getGuideHistory());
         CraftItemGroup craftItemGroup = this.getByPage(Math.min(this.page + 1, (this.slimefunItemList.size() - 1) / MAIN_CONTENT.length + 1));
         craftItemGroup.open(player, playerProfile, slimefunGuideMode);
         return false;
      });
      chestMenu.addItem(7, ItemStackUtil.getCleanItem(ItemStackUtil.cloneWithoutNBT(super.item)));
      chestMenu.addMenuClickHandler(7, ChestMenuUtils.getEmptyClickHandler());

      for (int slot : BORDER) {
         chestMenu.addItem(slot, ItemStackUtil.getCleanItem(ChestMenuUtils.getBackground()));
         chestMenu.addMenuClickHandler(slot, ChestMenuUtils.getEmptyClickHandler());
      }

      for (int i = 0; i < MAIN_CONTENT.length; i++) {
         int index = i + this.page * MAIN_CONTENT.length - MAIN_CONTENT.length;
         if (index < this.slimefunItemList.size()) {
            SlimefunItem slimefunItem = this.slimefunItemList.get(index);
            Research research = slimefunItem.getResearch();
            if (playerProfile.hasUnlocked(research)) {
               ItemStack itemStack = ItemStackUtil.cloneWithoutNBT(slimefunItem.getItem());
               ItemStackUtil.addLoreToFirst(itemStack, "\u00a77" + slimefunItem.getId());
               chestMenu.addItem(MAIN_CONTENT[i], ItemStackUtil.getCleanItem(itemStack));
               chestMenu.addMenuClickHandler(MAIN_CONTENT[i], (p, slot, item, action) -> {
                  RecipeItemGroup recipeItemGroup = RecipeItemGroup.getByItemStack(player, playerProfile, slimefunGuideMode, slimefunItem.getItem());
                  if (recipeItemGroup != null) {
                     Bukkit.getScheduler().runTask(JAVA_PLUGIN, () -> recipeItemGroup.open(player, playerProfile, slimefunGuideMode));
                  }

                  return false;
               });
            } else {
               ItemStack icon = ItemStackUtil.cloneItem(ChestMenuUtils.getNotResearchedItem());
               ItemStackUtil.setLore(
                  icon,
                  "\u00a77" + research.getName(player),
                  "\u00a74\u00a7l" + Slimefun.getLocalization().getMessage(player, "guide.locked"),
                  "",
                  Lang.getString("messages.guide.click-to-research"),
                  "",
                  Lang.getString("messages.guide.cost") + research.getCost() + Lang.getString("messages.guide.cost-level")
               );
               chestMenu.addItem(MAIN_CONTENT[i], ItemStackUtil.getCleanItem(icon));
               chestMenu.addMenuClickHandler(
                  MAIN_CONTENT[i],
                  (p, slot, item, action) -> {
                     PlayerPreResearchEvent event = new PlayerPreResearchEvent(player, research, slimefunItem);
                     Bukkit.getPluginManager().callEvent(event);
                     if (event.isCancelled() || playerProfile.hasUnlocked(research)) {
                        GuideUtil.removeLastEntry(playerProfile.getGuideHistory());
                        this.open(player, playerProfile, slimefunGuideMode);
                     } else if (research.canUnlock(player)) {
                        Slimefun.getRegistry()
                           .getSlimefunGuide(SlimefunGuideMode.SURVIVAL_MODE)
                           .unlockItem(player, slimefunItem, player1 -> this.refresh(player, playerProfile, slimefunGuideMode));
                     } else {
                        this.refresh(player, playerProfile, slimefunGuideMode);
                        Slimefun.getLocalization().sendMessage(player, "messages.not-enough-xp", true);
                     }

                     return false;
                  }
               );
            }
         }
      }

      return chestMenu;
   }

   @NotNull
   private CraftItemGroup getByPage(int page) {
      if (this.pageMap.containsKey(page)) {
         return this.pageMap.get(page);
      } else {
         synchronized ((CraftItemGroup)this.pageMap.get(1)) {
            if (this.pageMap.containsKey(page)) {
               return this.pageMap.get(page);
            } else {
               CraftItemGroup craftItemGroup = new CraftItemGroup(Keys.customNewKey(JAVA_PLUGIN, this.getKey().getKey() + "_" + page), this.slimefunItem, page);
               craftItemGroup.pageMap = this.pageMap;
               this.pageMap.put(page, craftItemGroup);
               return craftItemGroup;
            }
         }
      }
   }
}
