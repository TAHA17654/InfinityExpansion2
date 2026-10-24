package com.balugaq.netex.api.events;

import com.balugaq.netex.api.enums.StorageType;
import io.github.sefiraat.networks.network.NetworkRoot;
import lombok.Generated;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class NetworkRootLocateStorageEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private final NetworkRoot root;
   private final StorageType storageType;
   private final boolean inputAble;
   private final boolean outputAble;
   private final NetworkRootLocateStorageEvent.Strategy strategy;

   public NetworkRootLocateStorageEvent(NetworkRoot root, StorageType storageType, boolean inputAble, boolean outputAble, boolean isSync) {
      super(!isSync);
      this.root = root;
      this.storageType = storageType;
      this.inputAble = inputAble;
      this.outputAble = outputAble;
      this.strategy = NetworkRootLocateStorageEvent.Strategy.DEFAULT;
   }

   public NetworkRootLocateStorageEvent(NetworkRoot root, StorageType storageType, NetworkRootLocateStorageEvent.Strategy strategy, boolean isSync) {
      super(!isSync);
      this.root = root;
      this.storageType = storageType;
      this.inputAble = false;
      this.outputAble = false;
      this.strategy = strategy;
   }

   @NotNull
   public static HandlerList getHandlerList() {
      return handlers;
   }

   @NotNull
   public HandlerList getHandlers() {
      return handlers;
   }

   @Generated
   public NetworkRoot getRoot() {
      return this.root;
   }

   @Generated
   public StorageType getStorageType() {
      return this.storageType;
   }

   @Generated
   public boolean isInputAble() {
      return this.inputAble;
   }

   @Generated
   public boolean isOutputAble() {
      return this.outputAble;
   }

   @Generated
   public NetworkRootLocateStorageEvent.Strategy getStrategy() {
      return this.strategy;
   }

   public interface Strategy extends Cloneable {
      NetworkRootLocateStorageEvent.Strategy DEFAULT = new NetworkRootLocateStorageEvent.Strategy.StrategyImpl();
      String DEFAULT_TAG = "default";

      @NotNull
      static NetworkRootLocateStorageEvent.Strategy custom(String tag) {
         return NetworkRootLocateStorageEvent.Strategy.StrategyImpl.custom(tag);
      }

      @NotNull
      String getTag();

      @NotNull
      NetworkRootLocateStorageEvent.Strategy setTag(String var1);

      public static class StrategyImpl implements NetworkRootLocateStorageEvent.Strategy {
         private String tag = "default";

         @NotNull
         public static NetworkRootLocateStorageEvent.Strategy custom(String tag) {
            try {
               return new NetworkRootLocateStorageEvent.Strategy.StrategyImpl().setTag(tag);
            } catch (Throwable var2) {
               throw var2;
            }
         }

         @NotNull
         @Override
         public String getTag() {
            return this.tag;
         }

         @NotNull
         @Override
         public NetworkRootLocateStorageEvent.Strategy setTag(String tag) {
            this.tag = tag;
            return this;
         }

         @NotNull
         public NetworkRootLocateStorageEvent.Strategy clone() {
            return new NetworkRootLocateStorageEvent.Strategy.StrategyImpl().setTag(this.tag);
         }
      }
   }
}
