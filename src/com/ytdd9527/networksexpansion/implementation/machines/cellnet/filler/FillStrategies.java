package com.ytdd9527.networksexpansion.implementation.machines.cellnet.filler;

import com.balugaq.netex.api.data.ItemContainer;
import com.balugaq.netex.api.data.StorageUnitData;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.StorageCell;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.VoidCellSupport;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.ledger.CellLedger;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.menu.CellLore;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.rule.CellAcceptRules;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.ItemKey;
import com.ytdd9527.networksexpansion.implementation.machines.unit.NetworksDrawer;
import com.ytdd9527.networksexpansion.utils.databases.DataStorage;
import io.github.sefiraat.networks.network.stackcaches.QuantumCache;
import io.github.sefiraat.networks.slimefun.network.NetworkQuantumStorage;
import io.github.sefiraat.networks.utils.Keys;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.sefiraat.networks.utils.datatypes.DataTypeMethods;
import io.github.sefiraat.networks.utils.datatypes.PersistentQuantumStorageType;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FillStrategies {
   private static final int MAX_ITEM_BYTES = 131072;
   private static final Set<Integer> PENDING_LOADS = ConcurrentHashMap.newKeySet();

   private FillStrategies() {
   }

   @Nullable
   public static FillStrategies.Strategy create(@NotNull ItemStack container, @NotNull Location accessor, boolean boxMode) {
      if (container.getAmount() != 1 || oversizeNbt(container)) {
         return null;
      } else if (isShulker(container.getType())) {
         if (container.getItemMeta() instanceof BlockStateMeta meta && meta.getBlockState() instanceof ShulkerBox box) {
            return new FillStrategies.ShulkerStrategy(container, meta, box, boxMode);
         }

         return null;
      } else if (VoidCellSupport.isVoidCell(container)) {
         return null;
      } else if (StorageCell.isStorageCell(container)) {
         return new FillStrategies.CellStrategy(StorageCell.loadCellCache(container, StorageCell.getPerTypeLimit(container)));
      } else {
         SlimefunItem sfItem = SlimefunItem.getByItem(container);
         if (sfItem instanceof NetworkQuantumStorage storage) {
            return new FillStrategies.QuantumStrategy(container, storage, Keys.getQuantumCache(container.getItemMeta()));
         } else if (sfItem instanceof NetworksDrawer) {
            int id = NetworksDrawer.getBoundId(container);
            if (id == -1) {
               return null;
            } else {
               StorageUnitData data = DataStorage.getCachedStorageData(id).orElse(null);
               return data != null && !data.isPlaced() ? new FillStrategies.DrawerStrategy(data, NetworksDrawer.getLock(container), accessor) : null;
            }
         } else {
            return null;
         }
      }
   }

   @Nullable
   public static String containerError(@NotNull ItemStack item) {
      if (oversizeNbt(item)) {
         return "messages.cellnet.storage_assembler.container_oversize";
      } else if (isShulker(item.getType())) {
         return null;
      } else if (VoidCellSupport.isVoidCell(item)) {
         return "messages.cellnet.storage_assembler.void_cell_rejected";
      } else if (StorageCell.isStorageCell(item)) {
         return null;
      } else {
         SlimefunItem sfItem = SlimefunItem.getByItem(item);
         if (sfItem instanceof NetworkQuantumStorage) {
            return null;
         } else if (sfItem instanceof NetworksDrawer) {
            int id = NetworksDrawer.getBoundId(item);
            if (id == -1) {
               return "messages.cellnet.storage_assembler.drawer_unbound";
            } else {
               if (DataStorage.isContainerLoaded(id)) {
                  PENDING_LOADS.remove(id);
                  StorageUnitData data = DataStorage.getCachedStorageData(id).orElse(null);
                  if (data != null && data.isPlaced()) {
                     return "messages.cellnet.storage_assembler.drawer_placed";
                  }
               }

               return null;
            }
         } else {
            return "messages.cellnet.storage_assembler.container_rejected";
         }
      }
   }

   public static int unloadedDrawerId(@NotNull ItemStack container) {
      if (!(SlimefunItem.getByItem(container) instanceof NetworksDrawer)) {
         return -1;
      } else {
         int id = NetworksDrawer.getBoundId(container);
         if (id == -1) {
            return -1;
         } else if (DataStorage.isContainerLoaded(id)) {
            PENDING_LOADS.remove(id);
            return -1;
         } else {
            return id;
         }
      }
   }

   public static void requestDrawerLoad(int id) {
      if (PENDING_LOADS.add(id)) {
         DataStorage.requestStorageData(id);
      }
   }

   private static boolean isShulker(@NotNull Material material) {
      return material.name().endsWith("SHULKER_BOX");
   }

   private static boolean oversizeNbt(@NotNull ItemStack item) {
      try {
         return item.serializeAsBytes().length > 131072;
      } catch (Exception var2) {
         return true;
      }
   }

   private static final class CellStrategy implements FillStrategies.Strategy {
      private final CellLedger cache;

      private CellStrategy(@NotNull CellLedger cache) {
         this.cache = cache;
      }

      @Override
      public boolean singleType() {
         return false;
      }

      @NotNull
      @Override
      public List<FillStrategies.TemplateFill> plan(@NotNull List<ItemStack> templates) {
         long maxTypes = Math.min(this.cache.getCurrentPerTypeLimit(), this.cache.getMaxUnits());
         int usedTypes = this.cache.getAllItems().keySet().size();
         long freeTypes = Math.max(0L, maxTypes - (long)usedTypes);
         long perType = this.cache.getPerTypeLimit();
         List<ItemStack> whitelist = this.cache.isWhitelistEnabled() ? this.cache.getWhitelist() : null;
         List<FillStrategies.TemplateFill> plan = new ArrayList<>();

         for (ItemStack template : templates) {
            if (!FillStrategies.oversizeNbt(template) && !CellAcceptRules.isUsedCell(template) && !CellAcceptRules.isFilledContainer(template)) {
               ItemKey key = new ItemKey(template);
               long existing = this.cache.getAmount(key);
               long capacity;
               if (existing > 0L) {
                  capacity = Math.max(0L, perType - existing);
               } else if (freeTypes <= 0L || whitelist != null && !this.inWhitelist(template, whitelist)) {
                  capacity = 0L;
               } else {
                  capacity = perType;
                  freeTypes--;
               }

               if (capacity > 0L) {
                  plan.add(new FillStrategies.TemplateFill(template, Math.min(capacity, 2147483647L)));
               }
            }
         }

         return plan;
      }

      private boolean inWhitelist(@NotNull ItemStack template, @NotNull List<ItemStack> whitelist) {
         for (ItemStack item : whitelist) {
            if (StackUtils.itemsMatch(item, template)) {
               return true;
            }
         }

         return false;
      }

      @Override
      public long apply(@NotNull ItemStack template, long obtained) {
         return this.cache.pushItemLong(new ItemKey(template), obtained);
      }

      @Override
      public boolean hasContent() {
         return this.cache.getStored() > 0L;
      }

      @Override
      public void refreshDisplay(@NotNull ItemStack container) {
         CellLore.applySpecLore(container, this.cache.getPerTypeLimit(), this.cache.getCurrentPerTypeLimit());
      }
   }

   private static final class DrawerStrategy implements FillStrategies.Strategy {
      private final StorageUnitData data;
      private final boolean locked;
      private final Location accessor;

      private DrawerStrategy(@NotNull StorageUnitData data, boolean locked, @NotNull Location accessor) {
         this.data = data;
         this.locked = locked;
         this.accessor = accessor;
      }

      @Override
      public boolean singleType() {
         return false;
      }

      @NotNull
      @Override
      public List<FillStrategies.TemplateFill> plan(@NotNull List<ItemStack> templates) {
         int eachMax = this.data.getSizeType().getEachMaxSize();
         int maxTypes = this.data.getSizeType().getMaxItemCount();
         List<ItemContainer> stored = this.data.getStoredItems();
         int freeTypes = Math.max(0, maxTypes - stored.size());
         List<FillStrategies.TemplateFill> plan = new ArrayList<>();

         for (ItemStack template : templates) {
            if (!FillStrategies.oversizeNbt(template)) {
               long capacity = 0L;
               boolean matched = false;

               for (ItemContainer each : stored) {
                  if (each.isSimilar(template)) {
                     capacity = Math.max(0L, (long)(eachMax - each.getAmount()));
                     matched = true;
                     break;
                  }
               }

               if (!matched && !this.locked && freeTypes > 0) {
                  capacity = (long)eachMax;
                  freeTypes--;
               }

               if (capacity > 0L) {
                  plan.add(new FillStrategies.TemplateFill(template, Math.min(capacity, 2147483647L)));
               }
            }
         }

         return plan;
      }

      @Override
      public long apply(@NotNull ItemStack template, long obtained) {
         ItemStack unit = template.clone();
         unit.setAmount(1);
         return (long)this.data.addStoredItem0(this.accessor, unit, (int)Math.min(obtained, 2147483647L), this.locked, !this.locked);
      }

      @Override
      public boolean hasContent() {
         return !this.data.getStoredItems().isEmpty();
      }
   }

   private static final class QuantumStrategy implements FillStrategies.Strategy {
      private final ItemStack container;
      private final NetworkQuantumStorage storage;
      private QuantumCache cache;
      private boolean loreInitialized;

      private QuantumStrategy(@NotNull ItemStack container, @NotNull NetworkQuantumStorage storage, @Nullable QuantumCache cache) {
         this.container = container;
         this.storage = storage;
         this.cache = cache;
         this.loreInitialized = cache != null;
      }

      @Override
      public boolean singleType() {
         return true;
      }

      @NotNull
      @Override
      public List<FillStrategies.TemplateFill> plan(@NotNull List<ItemStack> templates) {
         if (this.cache != null && this.cache.getItemStack() != null) {
            for (ItemStack template : templates) {
               if (!FillStrategies.oversizeNbt(template) && StackUtils.itemsMatch(this.cache.getItemStack(), template)) {
                  long capacity = Math.max(0L, this.cache.getLimitLong() - this.cache.getAmountLong());
                  if (capacity > 0L) {
                     return List.of(new FillStrategies.TemplateFill(template, Math.min(capacity, 2147483647L)));
                  }

                  return List.of();
               }
            }

            return List.of();
         } else {
            long capacity = this.cache != null ? Math.max(0L, this.cache.getLimitLong() - this.cache.getAmountLong()) : this.storage.getMaxAmount();

            for (ItemStack templatex : templates) {
               if (!FillStrategies.oversizeNbt(templatex)) {
                  return List.of(new FillStrategies.TemplateFill(templatex, Math.min(capacity, 2147483647L)));
               }
            }

            return List.of();
         }
      }

      @Override
      public long apply(@NotNull ItemStack template, long obtained) {
         if (this.cache == null || this.cache.getItemStack() == null) {
            ItemStack stored = template.clone();
            stored.setAmount(1);
            this.cache = new QuantumCache(stored, 0L, this.storage.getMaxAmount(), false, this.storage.supportsCustomMaxAmount());
         }

         int amountIn = (int)Math.min(obtained, 2147483647L);
         int leftover = this.cache.increaseAmount(amountIn);
         long accepted = (long)(amountIn - leftover);
         if (accepted > 0L) {
            this.writeBack();
         }

         return accepted;
      }

      @Override
      public boolean hasContent() {
         return this.cache != null && this.cache.getAmountLong() > 0L;
      }

      private void writeBack() {
         ItemMeta meta = this.container.getItemMeta();
         DataTypeMethods.setCustom(meta, Keys.QUANTUM_STORAGE_INSTANCE, PersistentQuantumStorageType.TYPE, this.cache);
         if (this.loreInitialized) {
            this.cache.updateMetaLore(meta);
         } else {
            this.cache.addMetaLore(meta);
            this.loreInitialized = true;
         }

         this.container.setItemMeta(meta);
      }
   }

   private static final class ShulkerStrategy implements FillStrategies.Strategy {
      private final ItemStack container;
      private final BlockStateMeta meta;
      private final ShulkerBox box;
      private final ItemStack[] contents;
      private final boolean boxMode;
      private List<ItemStack> planTemplates;
      private final List<List<Integer>> planSlots = new ArrayList<>();

      private ShulkerStrategy(@NotNull ItemStack container, @NotNull BlockStateMeta meta, @NotNull ShulkerBox box, boolean boxMode) {
         this.container = container;
         this.meta = meta;
         this.box = box;
         this.boxMode = boxMode;
         this.contents = box.getInventory().getStorageContents();
      }

      @Override
      public boolean singleType() {
         return !this.boxMode;
      }

      @NotNull
      @Override
      public List<FillStrategies.TemplateFill> plan(@NotNull List<ItemStack> templates) {
         this.planTemplates = List.copyOf(templates);
         this.planSlots.clear();
         return this.boxMode ? this.planBanded(templates) : this.planSingleType(templates);
      }

      @NotNull
      private List<FillStrategies.TemplateFill> planSingleType(@NotNull List<ItemStack> templates) {
         ItemStack dedicated = null;

         for (ItemStack slot : this.contents) {
            if (slot != null && !slot.getType().isAir()) {
               dedicated = slot;
               break;
            }
         }

         ItemStack chosen = null;
         if (dedicated != null) {
            for (ItemStack t : templates) {
               if (!FillStrategies.oversizeNbt(t) && t.isSimilar(dedicated)) {
                  chosen = t;
                  break;
               }
            }
         } else {
            for (ItemStack tx : templates) {
               if (!FillStrategies.oversizeNbt(tx)) {
                  chosen = tx;
                  break;
               }
            }
         }

         if (chosen == null) {
            return List.of();
         } else {
            int max = Math.max(1, chosen.getMaxStackSize());
            long capacity = 0L;
            List<Integer> slots = new ArrayList<>();

            for (int i = 0; i < this.contents.length; i++) {
               ItemStack slotx = this.contents[i];
               if (slotx == null || slotx.getType().isAir()) {
                  capacity += (long)max;
                  slots.add(i);
               } else if (slotx.isSimilar(chosen) && slotx.getAmount() < max) {
                  capacity += (long)(max - slotx.getAmount());
                  slots.add(i);
               }
            }

            if (capacity <= 0L) {
               return List.of();
            } else {
               this.planSlots.add(slots);
               return List.of(new FillStrategies.TemplateFill(chosen, Math.min(capacity, 2147483647L)));
            }
         }
      }

      @NotNull
      private List<FillStrategies.TemplateFill> planBanded(@NotNull List<ItemStack> templates) {
         List<FillStrategies.TemplateFill> plan = new ArrayList<>();

         for (int ti = 0; ti < templates.size(); ti++) {
            ItemStack template = templates.get(ti);
            if (FillStrategies.oversizeNbt(template)) {
               this.planSlots.add(new ArrayList<>());
            } else {
               int max = Math.max(1, template.getMaxStackSize());
               long capacity = 0L;
               List<Integer> slots = new ArrayList<>();

               for (int i = 0; i < this.contents.length; i++) {
                  if (i % templates.size() == ti) {
                     ItemStack slot = this.contents[i];
                     if (slot == null || slot.getType().isAir()) {
                        capacity += (long)max;
                        slots.add(i);
                     } else if (slot.isSimilar(template) && slot.getAmount() < max) {
                        capacity += (long)(max - slot.getAmount());
                        slots.add(i);
                     }
                  }
               }

               this.planSlots.add(slots);
               if (capacity > 0L) {
                  plan.add(new FillStrategies.TemplateFill(template, Math.min(capacity, 2147483647L)));
               }
            }
         }

         return plan;
      }

      @Override
      public long apply(@NotNull ItemStack template, long obtained) {
         int max = Math.max(1, template.getMaxStackSize());
         long left = obtained;
         int ti = this.boxMode ? this.planTemplates.indexOf(template) : 0;
         if (ti >= 0 && ti < this.planSlots.size()) {
            for (int slotIndex : this.planSlots.get(ti)) {
               if (left <= 0L) {
                  break;
               }

               ItemStack slot = this.contents[slotIndex];
               if (slot == null || slot.getType().isAir()) {
                  int put = (int)Math.min(left, (long)max);
                  this.contents[slotIndex] = template.asQuantity(put);
                  left -= (long)put;
               } else if (slot.isSimilar(template) && slot.getAmount() < max) {
                  int put = (int)Math.min(left, (long)(max - slot.getAmount()));
                  this.contents[slotIndex] = slot.asQuantity(slot.getAmount() + put);
                  left -= (long)put;
               }
            }

            long accepted = obtained - left;
            if (accepted > 0L) {
               this.box.getInventory().setStorageContents(this.contents);
               this.meta.setBlockState(this.box);
               this.container.setItemMeta(this.meta);
            }

            return accepted;
         } else {
            return 0L;
         }
      }

      @Override
      public boolean hasContent() {
         for (ItemStack slot : this.contents) {
            if (slot != null && !slot.getType().isAir()) {
               return true;
            }
         }

         return false;
      }
   }

   public interface Strategy {
      boolean singleType();

      @NotNull
      List<FillStrategies.TemplateFill> plan(@NotNull List<ItemStack> var1);

      long apply(@NotNull ItemStack var1, long var2);

      boolean hasContent();

      default void refreshDisplay(@NotNull ItemStack container) {
      }
   }

   public static record TemplateFill(@NotNull ItemStack template, long request) {
   }
}
