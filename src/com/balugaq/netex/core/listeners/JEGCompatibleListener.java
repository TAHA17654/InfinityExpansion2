package com.balugaq.netex.core.listeners;

import com.balugaq.jeg.api.objects.events.GuideEvents.ItemButtonClickEvent;
import com.balugaq.jeg.utils.ReflectionUtil;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.core.guide.GuideHistory;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JEGCompatibleListener implements Listener {
   public static final Map<UUID, ItemButtonClickEvent> LAST_EVENTS = new ConcurrentHashMap<>();
   public static final Map<UUID, GuideHistory> GUIDE_HISTORY = new ConcurrentHashMap<>();
   public static final Map<UUID, BiConsumer<ItemButtonClickEvent, PlayerProfile>> PROFILE_CALLBACKS = new ConcurrentHashMap<>();

   public static void addCallback(@NotNull UUID uuid, @NotNull BiConsumer<ItemButtonClickEvent, PlayerProfile> callback) {
      PROFILE_CALLBACKS.put(uuid, callback);
   }

   public static void removeCallback(@NotNull UUID uuid) {
      PROFILE_CALLBACKS.remove(uuid);
   }

   @NotNull
   public static PlayerProfile getPlayerProfile(@NotNull OfflinePlayer player) {
      try {
         return (PlayerProfile)PlayerProfile.find(player).orElseThrow(() -> new RuntimeException("PlayerProfile not found"));
      } catch (Throwable var2) {
         throw var2;
      }
   }

   public static void tagGuideOpen(@NotNull Player player) {
      if (PROFILE_CALLBACKS.containsKey(player.getUniqueId())) {
         PlayerProfile profile = getPlayerProfile(player);
         saveOriginGuideHistory(profile);
         clearGuideHistory(profile);
      }
   }

   private static void saveOriginGuideHistory(@NotNull PlayerProfile profile) {
      GuideHistory oldHistory = profile.getGuideHistory();
      GuideHistory newHistory = new GuideHistory(profile);
      ReflectionUtil.setValue(newHistory, "mainMenuPage", oldHistory.getMainMenuPage());
      LinkedList<?> queue = (LinkedList<?>)ReflectionUtil.getValue(oldHistory, "queue", LinkedList.class);
      ReflectionUtil.setValue(newHistory, "queue", queue != null ? queue.clone() : new LinkedList());
      GUIDE_HISTORY.put(profile.getUUID(), newHistory);
   }

   private static void clearGuideHistory(@NotNull PlayerProfile profile) {
      ReflectionUtil.setValue(profile, "guideHistory", new GuideHistory(profile));
   }

   @Nullable
   public static ItemButtonClickEvent getLastEvent(@NotNull UUID playerUUID) {
      return LAST_EVENTS.get(playerUUID);
   }

   public static void clearLastEvent(@NotNull UUID playerUUID) {
      LAST_EVENTS.remove(playerUUID);
   }

   @EventHandler
   public void onJEGItemClick(@NotNull ItemButtonClickEvent event) {
      Player player = event.getPlayer();
      if (PROFILE_CALLBACKS.containsKey(player.getUniqueId())) {
         PlayerProfile profile = getPlayerProfile(player);
         this.rollbackGuideHistory(profile);
         PROFILE_CALLBACKS.get(player.getUniqueId()).accept(event, profile);
         PROFILE_CALLBACKS.remove(player.getUniqueId());
         LAST_EVENTS.put(player.getUniqueId(), event);
      }
   }

   private void rollbackGuideHistory(@NotNull PlayerProfile profile) {
      GuideHistory originHistory = GUIDE_HISTORY.get(profile.getUUID());
      if (originHistory != null) {
         ReflectionUtil.setValue(profile, "guideHistory", originHistory);
      }
   }
}
