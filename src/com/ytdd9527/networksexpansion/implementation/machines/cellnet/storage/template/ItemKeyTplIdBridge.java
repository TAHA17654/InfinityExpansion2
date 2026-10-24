package com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage.template;

import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.ItemKey;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemKeyTplIdBridge {
   private final ConcurrentHashMap<ItemKey, Long> keyToTplId = new ConcurrentHashMap<>();
   private final ConcurrentHashMap<Long, ItemKey> tplIdToKey = new ConcurrentHashMap<>();
   private final ItemTemplateRegistry registry;

   public ItemKeyTplIdBridge(ItemTemplateRegistry registry) {
      this.registry = registry;
   }

   public long getOrResolve(@NotNull ItemKey itemKey) {
      Long cached = this.keyToTplId.get(itemKey);
      if (cached != null) {
         return cached;
      } else {
         long tplId = this.registry.getOrRegister(itemKey.getItemStack());
         this.keyToTplId.put(itemKey, tplId);
         this.tplIdToKey.put(tplId, itemKey);
         return tplId;
      }
   }

   @Nullable
   public ItemKey resolveKey(long tplId) {
      ItemKey cached = this.tplIdToKey.get(tplId);
      if (cached != null) {
         return cached;
      } else {
         ItemStack itemStack = this.registry.resolveItem(tplId);
         if (itemStack == null) {
            return null;
         } else {
            ItemKey key = new ItemKey(itemStack);
            this.keyToTplId.put(key, tplId);
            this.tplIdToKey.put(tplId, key);
            return key;
         }
      }
   }

   public void preload(Collection<Long> tplIds) {
      for (long tplId : tplIds) {
         if (!this.tplIdToKey.containsKey(tplId)) {
            this.resolveKey(tplId);
         }
      }
   }
}
