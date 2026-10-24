package com.balugaq.netex.utils;

import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.core.guide.GuideHistory;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuideMode;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.annotation.ParametersAreNonnullByDefault;
import lombok.Generated;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class GuideUtil {
   @ParametersAreNonnullByDefault
   public static void openMainMenuAsync(Player player, SlimefunGuideMode mode, int selectedPage) {
      if (!PlayerProfile.get(player, profile -> Slimefun.runSync(() -> openMainMenu(player, profile, mode, selectedPage)))) {
         Slimefun.getLocalization().sendMessage(player, "messages.opening-guide");
      }
   }

   @ParametersAreNonnullByDefault
   public static void openMainMenu(Player player, PlayerProfile profile, SlimefunGuideMode mode, int selectedPage) {
      Slimefun.getRegistry().getSlimefunGuide(mode).openMainMenu(profile, selectedPage);
   }

   public static void removeLastEntry(@NotNull GuideHistory guideHistory) {
      try {
         Method getLastEntry = guideHistory.getClass().getDeclaredMethod("getLastEntry", boolean.class);
         getLastEntry.setAccessible(true);
         getLastEntry.invoke(guideHistory, true);
      } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException var2) {
         Debug.trace(var2);
      }
   }

   @Generated
   private GuideUtil() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
