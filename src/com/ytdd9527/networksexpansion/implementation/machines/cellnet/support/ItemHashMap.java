package com.ytdd9527.networksexpansion.implementation.machines.cellnet.support;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ItemHashMap<V> implements Map<ItemStack, V> {
   private final Map<ItemKey, V> map = new ConcurrentHashMap<>();

   @Override
   public int size() {
      return this.map.size();
   }

   @Override
   public boolean isEmpty() {
      return this.map.isEmpty();
   }

   @Override
   public boolean containsKey(Object key) {
      if (key instanceof ItemStack) {
         return this.map.containsKey(new ItemKey((ItemStack)key));
      } else {
         return key instanceof ItemKey itemKey ? this.map.containsKey(itemKey) : false;
      }
   }

   @Override
   public boolean containsValue(Object value) {
      return this.map.containsValue(value);
   }

   @Override
   public V get(Object key) {
      if (key instanceof ItemStack) {
         return this.map.get(new ItemKey((ItemStack)key));
      } else {
         return key instanceof ItemKey itemKey ? this.map.get(itemKey) : null;
      }
   }

   @Nullable
   public V getKey(ItemKey key) {
      return this.map.get(key);
   }

   @Nullable
   public V put(ItemStack key, V value) {
      return this.map.put(new ItemKey(key), value);
   }

   @Nullable
   public V putKey(ItemKey key, V value) {
      return this.map.put(key, value);
   }

   @Nullable
   @Override
   public V remove(Object key) {
      if (key instanceof ItemStack) {
         return this.map.remove(new ItemKey((ItemStack)key));
      } else {
         return key instanceof ItemKey itemKey ? this.map.remove(itemKey) : null;
      }
   }

   @Nullable
   public V removeKey(ItemKey key) {
      return this.map.remove(key);
   }

   @Override
   public void putAll(Map<? extends ItemStack, ? extends V> m) {
      for (Entry<? extends ItemStack, ? extends V> entry : m.entrySet()) {
         this.put(entry.getKey(), (V)entry.getValue());
      }
   }

   @Override
   public void clear() {
      this.map.clear();
   }

   @Override
   public Set<ItemStack> keySet() {
      return new AbstractSet<ItemStack>() {
         @Override
         public Iterator<ItemStack> iterator() {
            return new Iterator<ItemStack>() {
               private final Iterator<ItemKey> wrapperIterator = ItemHashMap.this.map.keySet().iterator();

               @Override
               public boolean hasNext() {
                  return this.wrapperIterator.hasNext();
               }

               public ItemStack next() {
                  return this.wrapperIterator.next().getItemStack();
               }

               @Override
               public void remove() {
                  this.wrapperIterator.remove();
               }
            };
         }

         @Override
         public int size() {
            return ItemHashMap.this.map.size();
         }

         @Override
         public boolean contains(Object o) {
            return ItemHashMap.this.containsKey(o);
         }

         @Override
         public boolean remove(Object o) {
            return ItemHashMap.this.remove(o) != null;
         }
      };
   }

   @Override
   public Collection<V> values() {
      return this.map.values();
   }

   @Override
   public Set<Entry<ItemStack, V>> entrySet() {
      return new AbstractSet<Entry<ItemStack, V>>() {
         @Override
         public Iterator<Entry<ItemStack, V>> iterator() {
            return new Iterator<Entry<ItemStack, V>>() {
               private final Iterator<Entry<ItemKey, V>> entryIterator = ItemHashMap.this.map.entrySet().iterator();

               @Override
               public boolean hasNext() {
                  return this.entryIterator.hasNext();
               }

               public Entry<ItemStack, V> next() {
                  Entry<ItemKey, V> entry = this.entryIterator.next();
                  return new SimpleEntry<>(entry.getKey().getItemStack(), entry.getValue());
               }

               @Override
               public void remove() {
                  this.entryIterator.remove();
               }
            };
         }

         @Override
         public int size() {
            return ItemHashMap.this.map.size();
         }

         @Override
         public boolean contains(Object o) {
            if (o instanceof Entry<?, ?> entry && entry.getKey() instanceof ItemStack) {
               ItemKey wrapper = new ItemKey((ItemStack)entry.getKey());
               return ItemHashMap.this.map.containsKey(wrapper) && Objects.equals(ItemHashMap.this.map.get(wrapper), entry.getValue());
            }

            return false;
         }

         @Override
         public boolean remove(Object o) {
            if (o instanceof Entry<?, ?> entry && entry.getKey() instanceof ItemStack) {
               ItemKey wrapper = new ItemKey((ItemStack)entry.getKey());
               if (Objects.equals(ItemHashMap.this.map.get(wrapper), entry.getValue())) {
                  ItemHashMap.this.map.remove(wrapper);
                  return true;
               }
            }

            return false;
         }
      };
   }

   public Set<Entry<ItemKey, V>> keyEntrySet() {
      return this.map.entrySet();
   }

   public Map<ItemKey, V> keyedView() {
      return Collections.unmodifiableMap(this.map);
   }

   public V getOrDefault(ItemKey key, V defaultValue) {
      V v = this.getKey(key);
      return v == null && !this.containsKey(key) ? defaultValue : v;
   }

   @Override
   public String toString() {
      return this.map.entrySet().stream().map(entry -> entry.getKey().getItemStack() + "=" + entry.getValue()).collect(Collectors.joining(", ", "{", "}"));
   }
}
