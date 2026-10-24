package com.ytdd9527.networksexpansion.implementation.machines.cellnet.support;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ChatInput {
   public static final long TIMEOUT_MS = 30000L;
   private static final ConcurrentHashMap<UUID, ChatInput.Pending> PENDING = new ConcurrentHashMap<>();

   private ChatInput() {
   }

   public static boolean request(@NotNull Player player, @NotNull ChatInput.InputType type, @NotNull Object context) {
      ChatInput.Pending previous = PENDING.put(player.getUniqueId(), new ChatInput.Pending(type, context, System.currentTimeMillis()));
      return previous != null;
   }

   @Nullable
   public static ChatInput.Pending pollPending(@NotNull UUID playerUuid) {
      ChatInput.Pending pending = PENDING.remove(playerUuid);
      if (pending == null) {
         return null;
      } else {
         return System.currentTimeMillis() - pending.createdAt() > 30000L ? null : pending;
      }
   }

   public static void clear(@NotNull UUID playerUuid) {
      PENDING.remove(playerUuid);
   }

   public static record DriveNameContext(@NotNull Location location) {
   }

   public static enum InputType {
      TARGET,
      DRIVE_NAME,
      MANAGER_DRIVE_NAME,
      MONITOR_SEARCH;
   }

   public static record Pending(@NotNull ChatInput.InputType type, @NotNull Object context, long createdAt) {
   }

   public static record SearchContext(@NotNull ChatInput.SearchTarget target, @NotNull Location location, @Nullable UUID playerUuid, @Nullable UUID cellUuid) {
   }

   public static enum SearchTarget {
      MONITOR,
      DRIVE_BROWSER,
      CELL_MENU,
      CELL_CLEANER,
      CELL_CONVERTER,
      DRIVE_MANAGER;
   }

   public static record TargetContext(@NotNull Location location, int blueprintSlot, boolean keep) {
   }
}
