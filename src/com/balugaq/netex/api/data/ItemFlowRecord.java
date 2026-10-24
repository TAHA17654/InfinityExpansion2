package com.balugaq.netex.api.data;

import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import io.github.sefiraat.networks.utils.StackUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Generated;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemFlowRecord {
   public static final long THRESHOLD = Networks.getConfigManager().getRecordGCThreshold();
   public static final long DEADLINE = Networks.getConfigManager().getRecordGCDeadline();
   public final Map<ItemStack, List<ItemFlowRecord.TransportAction>> actions = new ConcurrentHashMap<>();
   public long actionsCount = 0L;
   public long lastChangeTime = System.currentTimeMillis();

   public void gc() {
      if (this.actionsCount > THRESHOLD) {
         this.actions.clear();
      }
   }

   public void forceGC() {
      this.actions.clear();
   }

   public void addAction(Location accessor, @NotNull ItemStack before, int after) {
      int change = before.getAmount() - after;
      if (change != 0) {
         this.lastChangeTime = System.currentTimeMillis();
         this.actionsCount++;
         ItemStack key = StackUtils.getAsQuantity(before, 1);
         List<ItemFlowRecord.TransportAction> list = this.actions.computeIfAbsent(key, k -> new ArrayList<>());
         ItemFlowRecord.TransportAction action = new ItemFlowRecord.TransportAction(accessor, (long)change, System.currentTimeMillis());
         list.add(action);
      }
   }

   public void addAction(Location accessor, @NotNull ItemRequest request) {
      if (request.getReceivedAmount() != 0) {
         this.lastChangeTime = System.currentTimeMillis();
         this.actionsCount++;
         ItemStack key = StackUtils.getAsQuantity(request.getItemStack(), 1);
         List<ItemFlowRecord.TransportAction> list = this.actions.computeIfAbsent(key, k -> new ArrayList<>());
         ItemFlowRecord.TransportAction action = new ItemFlowRecord.TransportAction(accessor, (long)(-request.getReceivedAmount()), System.currentTimeMillis());
         list.add(action);
      }
   }

   @Generated
   public Map<ItemStack, List<ItemFlowRecord.TransportAction>> getActions() {
      return this.actions;
   }

   @Generated
   public long getActionsCount() {
      return this.actionsCount;
   }

   @Generated
   public long getLastChangeTime() {
      return this.lastChangeTime;
   }

   @Generated
   public void setActionsCount(long actionsCount) {
      this.actionsCount = actionsCount;
   }

   @Generated
   public void setLastChangeTime(long lastChangeTime) {
      this.lastChangeTime = lastChangeTime;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof ItemFlowRecord other)) {
         return false;
      } else if (!other.canEqual(this)) {
         return false;
      } else if (this.getActionsCount() != other.getActionsCount()) {
         return false;
      } else if (this.getLastChangeTime() != other.getLastChangeTime()) {
         return false;
      } else {
         Object this$actions = this.getActions();
         Object other$actions = other.getActions();
         return this$actions == null ? other$actions == null : this$actions.equals(other$actions);
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof ItemFlowRecord;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      long $actionsCount = this.getActionsCount();
      result = result * 59 + (int)($actionsCount >>> 32 ^ $actionsCount);
      long $lastChangeTime = this.getLastChangeTime();
      result = result * 59 + (int)($lastChangeTime >>> 32 ^ $lastChangeTime);
      Object $actions = this.getActions();
      return result * 59 + ($actions == null ? 43 : $actions.hashCode());
   }

   @Generated
   @Override
   public String toString() {
      return "ItemFlowRecord(actions=" + this.getActions() + ", actionsCount=" + this.getActionsCount() + ", lastChangeTime=" + this.getLastChangeTime() + ")";
   }

   public static record TransportAction(Location accessor, long amount, long milliSecond) {
   }
}
