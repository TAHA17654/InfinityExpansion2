package io.github.sefiraat.networks.managers;

import com.balugaq.netex.integrations.logitech.LogitechIntegration;
import com.bgsoftware.wildstacker.api.WildStackerAPI;
import com.google.common.base.Preconditions;
import dev.rosewood.rosestacker.api.RoseStackerAPI;
import dev.rosewood.rosestacker.stack.StackedItem;
import io.github.sefiraat.networks.Networks;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SupportedPluginManager {
   private static SupportedPluginManager instance;
   @Nullable
   private LogitechIntegration logitechIntegration = null;
   private final boolean infinityExpansion;
   private final boolean tsingshanTechnology;
   private final boolean fluffyMachines;
   private final boolean netheopoiesis;
   private final boolean slimeHud;
   private final boolean roseStacker;
   private final boolean wildStacker;
   private final boolean guguSlimefunLib;
   private RoseStackerAPI roseStackerAPI;
   private final boolean finalTECH;
   private boolean justEnoughGuide;
   private boolean mcMMO;
   private boolean wildChests;
   private boolean logitech;

   public SupportedPluginManager() {
      Preconditions.checkArgument(instance == null, "Cannot instantiate class");
      instance = this;
      this.infinityExpansion = Bukkit.getPluginManager().isPluginEnabled("InfinityExpansion");
      this.tsingshanTechnology = Bukkit.getPluginManager().isPluginEnabled("TsingshanTechnology-Fixed")
         || Bukkit.getPluginManager().isPluginEnabled("TsingshanTechnology");
      this.fluffyMachines = Bukkit.getPluginManager().isPluginEnabled("FluffyMachines");
      this.netheopoiesis = Bukkit.getPluginManager().isPluginEnabled("Netheopoiesis");
      this.slimeHud = Bukkit.getPluginManager().isPluginEnabled("SlimeHUD") || Bukkit.getPluginManager().isPluginEnabled("SlimeHUDPlus");
      this.roseStacker = Bukkit.getPluginManager().isPluginEnabled("RoseStacker");
      if (this.roseStacker) {
         this.roseStackerAPI = RoseStackerAPI.getInstance();
      }

      this.wildStacker = Bukkit.getPluginManager().isPluginEnabled("WildStacker");
      this.guguSlimefunLib = Bukkit.getPluginManager().isPluginEnabled("GuguSlimefunLib");
      this.finalTECH = Bukkit.getPluginManager().isPluginEnabled("FinalTECH") || Bukkit.getPluginManager().isPluginEnabled("FinalTECH-Changed");
      Networks.getInstance().getServer().getScheduler().runTaskLater(Networks.getInstance(), this::firstTickRegistrations, 1L);
   }

   public static int getStackAmount(@NotNull Item item) {
      if (getInstance().isWildStacker()) {
         return WildStackerAPI.getItemAmount(item);
      } else if (getInstance().isRoseStacker()) {
         StackedItem stackedItem = getInstance().getRoseStackerAPI().getStackedItem(item);
         return stackedItem == null ? item.getItemStack().getAmount() : stackedItem.getStackSize();
      } else {
         return item.getItemStack().getAmount();
      }
   }

   public static void setStackAmount(@NotNull Item item, int amount) {
      if (getInstance().isWildStacker()) {
         com.bgsoftware.wildstacker.api.objects.StackedItem stackedItem = WildStackerAPI.getStackedItem(item);
         if (stackedItem != null) {
            stackedItem.setStackAmount(amount, true);
         }
      }

      if (getInstance().isRoseStacker()) {
         StackedItem stackedItem = getInstance().getRoseStackerAPI().getStackedItem(item);
         if (stackedItem != null) {
            stackedItem.setStackSize(amount);
         }
      }

      if (!getInstance().isWildStacker() && !getInstance().isRoseStacker()) {
         item.getItemStack().setAmount(amount);
      }
   }

   private void firstTickRegistrations() {
      this.wildChests = Bukkit.getPluginManager().isPluginEnabled("WildChests");
      this.mcMMO = Bukkit.getPluginManager().isPluginEnabled("mcMMO");
      this.justEnoughGuide = Bukkit.getPluginManager().isPluginEnabled("JustEnoughGuide");
      if (this.justEnoughGuide) {
         try {
            Class.forName("com.balugaq.jeg.api.objects.events.GuideEvents");
         } catch (ClassNotFoundException var2) {
            this.justEnoughGuide = false;
         }
      }

      this.logitech = Bukkit.getPluginManager().isPluginEnabled("LogiTech");
      if (this.logitech) {
         this.logitechIntegration = new LogitechIntegration();
      }
   }

   @Generated
   public static SupportedPluginManager getInstance() {
      return instance;
   }

   @Nullable
   @Generated
   public LogitechIntegration getLogitechIntegration() {
      return this.logitechIntegration;
   }

   @Generated
   public boolean isInfinityExpansion() {
      return this.infinityExpansion;
   }

   @Generated
   public boolean isTsingshanTechnology() {
      return this.tsingshanTechnology;
   }

   @Generated
   public boolean isFluffyMachines() {
      return this.fluffyMachines;
   }

   @Generated
   public boolean isNetheopoiesis() {
      return this.netheopoiesis;
   }

   @Generated
   public boolean isSlimeHud() {
      return this.slimeHud;
   }

   @Generated
   public boolean isRoseStacker() {
      return this.roseStacker;
   }

   @Generated
   public boolean isWildStacker() {
      return this.wildStacker;
   }

   @Generated
   public boolean isGuguSlimefunLib() {
      return this.guguSlimefunLib;
   }

   @Generated
   public RoseStackerAPI getRoseStackerAPI() {
      return this.roseStackerAPI;
   }

   @Generated
   public boolean isFinalTECH() {
      return this.finalTECH;
   }

   @Generated
   public void setJustEnoughGuide(boolean justEnoughGuide) {
      this.justEnoughGuide = justEnoughGuide;
   }

   @Generated
   public boolean isJustEnoughGuide() {
      return this.justEnoughGuide;
   }

   @Generated
   public boolean isMcMMO() {
      return this.mcMMO;
   }

   @Generated
   public boolean isWildChests() {
      return this.wildChests;
   }

   @Generated
   public boolean isLogitech() {
      return this.logitech;
   }
}
