package com.balugaq.netex.utils;

import com.balugaq.netex.api.data.VanillaInventoryWrapper;
import com.balugaq.netex.api.enums.TransportMode;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import io.github.sefiraat.networks.slimefun.network.NetworkObject;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import lombok.Generated;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class LineOperationUtil {
   public static final Location UNKNOWN_LOCATION = new Location(null, 0.0, 0.0, 0.0);

   public static void doOperation(@NotNull Location startLocation, @NotNull BlockFace direction, int limit, @NotNull Consumer<BlockMenu> consumer) {
      doOperation(startLocation, direction, limit, false, true, consumer);
   }

   public static void doOperation(
      @NotNull Location startLocation, @NotNull BlockFace direction, int limit, boolean skipNoMenu, @NotNull Consumer<BlockMenu> consumer
   ) {
      doOperation(startLocation, direction, limit, skipNoMenu, true, consumer);
   }

   public static void doOperation(
      @NotNull Location startLocation,
      @NotNull BlockFace direction,
      int limit,
      boolean skipNoMenu,
      boolean optimizeExperience,
      @NotNull Consumer<BlockMenu> consumer
   ) {
      Location location = startLocation.clone();
      int finalLimit = limit;
      if (optimizeExperience) {
         finalLimit = limit + 1;
      }

      for (int i = 0; i < finalLimit; i++) {
         switch (direction) {
            case NORTH:
               location.setZ(location.getZ() - 1.0);
               break;
            case SOUTH:
               location.setZ(location.getZ() + 1.0);
               break;
            case EAST:
               location.setX(location.getX() + 1.0);
               break;
            case WEST:
               location.setX(location.getX() - 1.0);
               break;
            case UP:
               location.setY(location.getY() + 1.0);
               break;
            case DOWN:
               location.setY(location.getY() - 1.0);
         }

         BlockMenu blockMenu = StorageCacheUtils.getMenu(location);
         if (blockMenu == null) {
            if (!skipNoMenu) {
               return;
            }
         } else {
            consumer.accept(blockMenu);
         }
      }
   }

   public static void doVanillaOperation(@NotNull Location startLocation, @NotNull BlockFace direction, int limit, @NotNull Consumer<BlockMenu> consumer) {
      doVanillaOperation(startLocation, direction, limit, false, false, consumer);
   }

   public static void doVanillaOperation(
      @NotNull Location startLocation, @NotNull BlockFace direction, int limit, boolean skipNoInventory, @NotNull Consumer<BlockMenu> consumer
   ) {
      doVanillaOperation(startLocation, direction, limit, skipNoInventory, false, consumer);
   }

   public static void doVanillaOperation(
      @NotNull Location startLocation,
      @NotNull BlockFace direction,
      int limit,
      boolean skipNoInventory,
      boolean optimizeExperience,
      @NotNull Consumer<BlockMenu> consumer
   ) {
      Location location = startLocation.clone();
      int finalLimit = limit;
      if (optimizeExperience) {
         finalLimit = limit + 1;
      }

      for (int i = 0; i < finalLimit; i++) {
         switch (direction) {
            case NORTH:
               location.setZ(location.getZ() - 1.0);
               break;
            case SOUTH:
               location.setZ(location.getZ() + 1.0);
               break;
            case EAST:
               location.setX(location.getX() + 1.0);
               break;
            case WEST:
               location.setX(location.getX() - 1.0);
               break;
            case UP:
               location.setY(location.getY() + 1.0);
               break;
            case DOWN:
               location.setY(location.getY() - 1.0);
         }

         BlockState state = location.getBlock().getState(false);
         if (state instanceof InventoryHolder holder) {
            Inventory inv = holder.getInventory();
            if (inv != null) {
               VanillaInventoryWrapper wrapper = new VanillaInventoryWrapper(inv, state);
               consumer.accept(wrapper);
            }
         } else if (!skipNoInventory) {
            return;
         }
      }
   }

   public static void doEnergyOperation(@NotNull Location startLocation, @NotNull BlockFace direction, int limit, @NotNull Consumer<Location> consumer) {
      doEnergyOperation(startLocation, direction, limit, true, true, consumer);
   }

   public static void doEnergyOperation(
      @NotNull Location startLocation, @NotNull BlockFace direction, int limit, boolean allowNoMenu, @NotNull Consumer<Location> consumer
   ) {
      doEnergyOperation(startLocation, direction, limit, allowNoMenu, true, consumer);
   }

   public static void doEnergyOperation(
      @NotNull Location startLocation,
      @NotNull BlockFace direction,
      int limit,
      boolean allowNoMenu,
      boolean optimizeExperience,
      @NotNull Consumer<Location> consumer
   ) {
      Location location = startLocation.clone();
      int finalLimit = limit;
      if (optimizeExperience) {
         finalLimit = limit + 1;
      }

      for (int i = 0; i < finalLimit; i++) {
         switch (direction) {
            case NORTH:
               location.setZ(location.getZ() - 1.0);
               break;
            case SOUTH:
               location.setZ(location.getZ() + 1.0);
               break;
            case EAST:
               location.setX(location.getX() + 1.0);
               break;
            case WEST:
               location.setX(location.getX() - 1.0);
               break;
            case UP:
               location.setY(location.getY() + 1.0);
               break;
            case DOWN:
               location.setY(location.getY() - 1.0);
         }

         BlockMenu blockMenu = StorageCacheUtils.getMenu(location);
         if (blockMenu == null && !allowNoMenu) {
            return;
         }

         consumer.accept(location);
      }
   }

   @Deprecated
   public static void grabItem(@NotNull NetworkRoot root, @NotNull BlockMenu blockMenu, @NotNull TransportMode transportMode, int limitQuantity) {
      grabItem(UNKNOWN_LOCATION, root, blockMenu, transportMode, limitQuantity);
   }

   public static void grabItem(
      @NotNull Location accessor, @NotNull NetworkRoot root, @NotNull BlockMenu blockMenu, @NotNull TransportMode transportMode, int limitQuantity
   ) {
      boolean staged = CompressedBridge.stage(blockMenu);
      try {
         grabItemInternal(accessor, root, blockMenu, transportMode, limitQuantity);
      } finally {
         if (staged) {
            CompressedBridge.unstage(blockMenu);
         }
      }
   }

   private static void grabItemInternal(
      @NotNull Location accessor, @NotNull NetworkRoot root, @NotNull BlockMenu blockMenu, @NotNull TransportMode transportMode, int limitQuantity
   ) {
      int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(blockMenu, ItemTransportFlow.WITHDRAW, null);
      int limit = limitQuantity;
      switch (transportMode) {
         case NONE:
         case NONNULL_ONLY:
            for (int slotxxxx : slots) {
               ItemStack item = blockMenu.getItemInSlot(slotxxxx);
               if (item != null && item.getType() != Material.AIR) {
                  int exceptedReceive = Math.min(item.getAmount(), limit);
                  ItemStack clone = StackUtils.getAsQuantity(item, exceptedReceive);
                  root.addItemStack(accessor, clone);
                  int taken = exceptedReceive - clone.getAmount();
                  grabFromSlot(blockMenu, slotxxxx, item, taken);
                  limit -= taken;
                  if (limit <= 0) {
                     break;
                  }
               }
            }
         case NULL_ONLY:
         case P2P:
         case P2P_SPECIFIED_QUANTITY:
         default:
            break;
         case FIRST_ONLY:
            if (slots.length > 0) {
               ItemStack item = blockMenu.getItemInSlot(slots[0]);
               if (item != null && item.getType() != Material.AIR) {
                  int exceptedReceive = Math.min(item.getAmount(), limitQuantity);
                  ItemStack clone = StackUtils.getAsQuantity(item, exceptedReceive);
                  root.addItemStack(accessor, clone);
                  grabFromSlot(blockMenu, slots[0], item, exceptedReceive - clone.getAmount());
               }
            }
            break;
         case LAST_ONLY:
            if (slots.length > 0) {
               int lastSlot = slots[slots.length - 1];
               ItemStack item = blockMenu.getItemInSlot(lastSlot);
               if (item != null && item.getType() != Material.AIR) {
                  int exceptedReceive = Math.min(item.getAmount(), limitQuantity);
                  ItemStack clone = StackUtils.getAsQuantity(item, exceptedReceive);
                  root.addItemStack(accessor, clone);
                  grabFromSlot(blockMenu, lastSlot, item, exceptedReceive - clone.getAmount());
               }
            }
            break;
         case FIRST_STOP:
            for (int slotxxx : slots) {
               ItemStack item = blockMenu.getItemInSlot(slotxxx);
               if (item != null && item.getType() != Material.AIR) {
                  int exceptedReceive = Math.min(item.getAmount(), limitQuantity);
                  ItemStack clone = StackUtils.getAsQuantity(item, exceptedReceive);
                  root.addItemStack(accessor, clone);
                  grabFromSlot(blockMenu, slotxxx, item, exceptedReceive - clone.getAmount());
                  break;
               }
            }
            break;
         case LAZY:
            if (slots.length > 0) {
               ItemStack delta = blockMenu.getItemInSlot(slots[0]);
               if (delta != null && delta.getType() != Material.AIR) {
                  for (int slotxx : slots) {
                     ItemStack item = blockMenu.getItemInSlot(slotxx);
                     if (item != null && item.getType() != Material.AIR) {
                        int exceptedReceive = Math.min(item.getAmount(), limit);
                        ItemStack clone = StackUtils.getAsQuantity(item, exceptedReceive);
                        root.addItemStack(accessor, clone);
                        int taken = exceptedReceive - clone.getAmount();
                        grabFromSlot(blockMenu, slotxx, item, taken);
                        limit -= taken;
                        if (limit <= 0) {
                           break;
                        }
                     }
                  }
               }
            }
            break;
         case VOID:
            for (int slotx : slots) {
               ItemStack item = blockMenu.getItemInSlot(slotx);
               if (item != null && item.getType() != Material.AIR) {
                  int exceptedReceive = Math.min(item.getAmount(), limit);
                  ItemStack clone = StackUtils.getAsQuantity(item, exceptedReceive);
                  root.addItemStack(accessor, clone);
                  limit -= exceptedReceive - clone.getAmount();
                  blockMenu.replaceExistingItem(slotx, null);
                  if (limit <= 0) {
                     break;
                  }
               }
            }
            break;
         case SPECIFIED_QUANTITY:
            Map<Integer, ItemStack> itemSamples = new LinkedHashMap<>();
            Map<Integer, Integer> itemTotals = new LinkedHashMap<>();
            int typeIndex = 0;

            for (int slot : slots) {
               ItemStack item = blockMenu.getItemInSlot(slot);
               if (item != null && item.getType() != Material.AIR) {
                  boolean found = false;

                  for (Entry<Integer, ItemStack> entry : itemSamples.entrySet()) {
                     if (StackUtils.itemsMatch(entry.getValue(), item)) {
                        itemTotals.merge(entry.getKey(), item.getAmount(), Integer::sum);
                        found = true;
                        break;
                     }
                  }

                  if (!found) {
                     itemSamples.put(typeIndex, StackUtils.getAsQuantity(item, 1));
                     itemTotals.put(typeIndex, item.getAmount());
                     typeIndex++;
                  }
               }
            }

            for (Entry<Integer, ItemStack> entryx : itemSamples.entrySet()) {
               int total = itemTotals.get(entryx.getKey());
               if (total > limitQuantity) {
                  int toRemove = total - limitQuantity;

                  for (int i = slots.length - 1; i >= 0 && toRemove > 0; i--) {
                     ItemStack item = blockMenu.getItemInSlot(slots[i]);
                     if (item != null && item.getType() != Material.AIR && StackUtils.itemsMatch(entryx.getValue(), item)) {
                        int grabFromSlot = Math.min(item.getAmount(), toRemove);
                        ItemStack clone = StackUtils.getAsQuantity(item, grabFromSlot);
                        root.addItemStack(accessor, clone);
                        int actualGrabbed = grabFromSlot - clone.getAmount();
                        grabFromSlot(blockMenu, slots[i], item, actualGrabbed);
                        toRemove -= actualGrabbed;
                     }
                  }
               }
            }
      }
   }

   private static void grabFromSlot(@NotNull BlockMenu blockMenu, int slot, @NotNull ItemStack item, int taken) {
      int remaining = item.getAmount() - taken;
      if (remaining <= 0) {
         blockMenu.replaceExistingItem(slot, null);
      } else {
         item.setAmount(remaining);
      }
   }

   @Deprecated
   public static void pushItem(
      @NotNull NetworkRoot root, @NotNull BlockMenu blockMenu, @NotNull List<ItemStack> clones, @NotNull TransportMode transportMode, int limitQuantity
   ) {
      pushItem(UNKNOWN_LOCATION, root, blockMenu, clones, transportMode, limitQuantity);
   }

   public static void pushItem(
      @NotNull Location accessor,
      @NotNull NetworkRoot root,
      @NotNull BlockMenu blockMenu,
      @NotNull List<ItemStack> templates,
      @NotNull TransportMode transportMode,
      int limitQuantity
   ) {
      for (int i = 0; i < templates.size(); i++) {
         ItemStack template = templates.get(i);
         if (template != null && template.getType() != Material.AIR) {
            pushItem(accessor, root, blockMenu, template, i, transportMode, limitQuantity);
         }
      }
   }

   @Deprecated
   public static void pushItem(
      @NotNull NetworkRoot root, @NotNull BlockMenu blockMenu, @NotNull ItemStack clone, int itemIndex, @NotNull TransportMode transportMode, int limitQuantity
   ) {
      pushItem(UNKNOWN_LOCATION, root, blockMenu, clone, itemIndex, transportMode, limitQuantity);
   }

   public static void pushRequests(
      @NotNull Location accessor,
      @NotNull NetworkRoot root,
      @NotNull BlockMenu blockMenu,
      @NotNull List<ItemRequest> requests,
      @NotNull TransportMode transportMode,
      int limitQuantity
   ) {
      for (int i = 0; i < requests.size(); i++) {
         ItemRequest request = requests.get(i);
         ItemStack template = request.getItemStack();
         if (template != null && template.getType() != Material.AIR) {
            request.setAmount(template.getMaxStackSize());
            pushItem(accessor, root, blockMenu, template, i, transportMode, limitQuantity, request);
         }
      }
   }

   public static void pushItem(
      @NotNull Location accessor,
      @NotNull NetworkRoot root,
      @NotNull BlockMenu blockMenu,
      @NotNull ItemStack template,
      int itemIndex,
      @NotNull TransportMode transportMode,
      int limitQuantity
   ) {
      pushItem(accessor, root, blockMenu, template, itemIndex, transportMode, limitQuantity, new ItemRequest(template, template.getMaxStackSize()));
   }

   public static void pushItem(
      @NotNull Location accessor,
      @NotNull NetworkRoot root,
      @NotNull BlockMenu blockMenu,
      @NotNull ItemStack template,
      int itemIndex,
      @NotNull TransportMode transportMode,
      int limitQuantity,
      @NotNull ItemRequest itemRequest
   ) {
      int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(blockMenu, ItemTransportFlow.INSERT, template);
      switch (transportMode) {
         case NONE:
            int freeSpace = 0;
            int[] openBuf = new int[slots.length];
            int openCount = 0;

            for (int slotx : slots) {
               ItemStack itemStackx = blockMenu.getItemInSlot(slotx);
               if (itemStackx == null || itemStackx.getType() == Material.AIR) {
                  freeSpace += template.getMaxStackSize();
                  openBuf[openCount++] = slotx;
               } else if (itemStackx.getAmount() < template.getMaxStackSize() && StackUtils.itemsMatch(itemRequest, itemStackx)) {
                  int availableSpace = itemStackx.getMaxStackSize() - itemStackx.getAmount();
                  if (availableSpace > 0) {
                     freeSpace += availableSpace;
                     openBuf[openCount++] = slotx;
                  }
               }
            }

            if (freeSpace <= 0) {
               return;
            }

            itemRequest.setAmount(Math.min(freeSpace, limitQuantity));
            ItemStack retrievedx = root.getItemStack(accessor, itemRequest);
            if (retrievedx != null && retrievedx.getType() != Material.AIR) {
               BlockMenuUtil.pushItem(blockMenu, retrievedx, openCount == slots.length ? slots : Arrays.copyOf(openBuf, openCount));
            }
            break;
         case NONNULL_ONLY:
            int free = limitQuantity;

            for (int slotxxxxx : slots) {
               ItemStack itemStackxx = blockMenu.getItemInSlot(slotxxxxx);
               if (itemStackxx != null
                  && itemStackxx.getType() != Material.AIR
                  && itemStackxx.getAmount() < template.getMaxStackSize()
                  && StackUtils.itemsMatch(itemRequest, itemStackxx)) {
                  int space = itemStackxx.getMaxStackSize() - itemStackxx.getAmount();
                  if (space > 0) {
                     itemRequest.setAmount(space);
                     itemRequest.setAmount(Math.min(itemRequest.getAmount(), free));
                     ItemStack retrievedx = root.getItemStack(accessor, itemRequest);
                     if (retrievedx != null && retrievedx.getType() != Material.AIR) {
                        free -= retrievedx.getAmount();
                        BlockMenuUtil.pushItem(blockMenu, retrievedx, slotxxxxx);
                        if (free <= 0) {
                           return;
                        }
                     }
                  }
               }
            }
            break;
         case NULL_ONLY:
            int free = limitQuantity;

            for (int slotxxxxxx : slots) {
               ItemStack itemStackxx = blockMenu.getItemInSlot(slotxxxxxx);
               if (itemStackxx == null || itemStackxx.getType() == Material.AIR) {
                  itemRequest.setAmount(template.getMaxStackSize());
                  itemRequest.setAmount(Math.min(itemRequest.getAmount(), free));
                  ItemStack retrievedx = root.getItemStack(accessor, itemRequest);
                  if (retrievedx != null && retrievedx.getType() != Material.AIR) {
                     free -= retrievedx.getAmount();
                     BlockMenuUtil.pushItem(blockMenu, retrievedx, slotxxxxxx);
                     if (free <= 0) {
                        return;
                     }
                  }
               }
            }
            break;
         case P2P:
            if (itemIndex >= slots.length) {
               return;
            }

            int slotx = slots[itemIndex];
            pushSlot(accessor, root, itemRequest, blockMenu, template, slotx, limitQuantity);
            break;
         case P2P_SPECIFIED_QUANTITY:
            if (itemIndex >= slots.length) {
               return;
            }

            int slot = slots[itemIndex];
            int existingCount = 0;
            ItemStack itemStack = blockMenu.getItemInSlot(slot);
            if (itemStack != null && itemStack.getType() != Material.AIR && StackUtils.itemsMatch(itemRequest, itemStack)) {
               existingCount += itemStack.getAmount();
            }

            int targetCount = template.getAmount();
            int scheduleToMove = Math.min(targetCount - existingCount, limitQuantity);
            if (scheduleToMove <= 0) {
               return;
            }

            itemRequest.setAmount(scheduleToMove);
            ItemStack retrieved = root.getItemStack(accessor, itemRequest);
            if (retrieved != null && retrieved.getType() != Material.AIR) {
               BlockMenuUtil.pushItem(blockMenu, retrieved, slot);
            }
            break;
         case FIRST_ONLY:
            if (slots.length != 0) {
               int slotx = slots[0];
               pushSlot(accessor, root, itemRequest, blockMenu, template, slotx, limitQuantity);
            }
            break;
         case LAST_ONLY:
            if (slots.length != 0) {
               int slotx = slots[slots.length - 1];
               pushSlot(accessor, root, itemRequest, blockMenu, template, slotx, limitQuantity);
            }
            break;
         case FIRST_STOP:
            int freeSpace = 0;

            for (int slotxx : slots) {
               ItemStack itemStackx = blockMenu.getItemInSlot(slotxx);
               if (itemStackx == null || itemStackx.getType() == Material.AIR) {
                  freeSpace += template.getMaxStackSize();
                  break;
               }

               if (itemStackx.getAmount() < template.getMaxStackSize()) {
                  if (StackUtils.itemsMatch(itemRequest, itemStackx)) {
                     int availableSpace = itemStackx.getMaxStackSize() - itemStackx.getAmount();
                     if (availableSpace > 0) {
                        freeSpace += availableSpace;
                     }
                  }
                  break;
               }
            }

            if (freeSpace <= 0) {
               return;
            }

            itemRequest.setAmount(Math.min(freeSpace, limitQuantity));
            ItemStack retrievedx = root.getItemStack(accessor, itemRequest);
            if (retrievedx != null && retrievedx.getType() != Material.AIR) {
               BlockMenuUtil.pushItem(blockMenu, retrievedx, slots);
            }
            break;
         case LAZY:
            if (slots.length > 0) {
               ItemStack delta = blockMenu.getItemInSlot(slots[0]);
               if (delta == null || delta.getType() == Material.AIR) {
                  int freeSpace = 0;
                  int[] openBuf = new int[slots.length];
                  int openCount = 0;

                  for (int slotxxxx : slots) {
                     ItemStack itemStackxx = blockMenu.getItemInSlot(slotxxxx);
                     if (itemStackxx == null || itemStackxx.getType() == Material.AIR) {
                        freeSpace += template.getMaxStackSize();
                        openBuf[openCount++] = slotxxxx;
                     } else if (itemStackxx.getAmount() < template.getMaxStackSize() && StackUtils.itemsMatch(itemRequest, itemStackxx)) {
                        int availableSpace = itemStackxx.getMaxStackSize() - itemStackxx.getAmount();
                        if (availableSpace > 0) {
                           freeSpace += availableSpace;
                           openBuf[openCount++] = slotxxxx;
                        }
                     }
                  }

                  if (freeSpace <= 0) {
                     return;
                  }

                  itemRequest.setAmount(Math.min(freeSpace, limitQuantity));
                  ItemStack retrievedx = root.getItemStack(accessor, itemRequest);
                  if (retrievedx != null && retrievedx.getType() != Material.AIR) {
                     BlockMenuUtil.pushItem(blockMenu, retrievedx, openCount == slots.length ? slots : Arrays.copyOf(openBuf, openCount));
                  }
               }
            }
            break;
         case VOID:
            itemRequest.setAmount(limitQuantity);
            ItemStack retrieved = root.getItemStack(accessor, itemRequest);
            if (retrieved != null && retrieved.getType() != Material.AIR) {
               BlockMenuUtil.pushItem(blockMenu, retrieved, slots);
            }
            break;
         case SPECIFIED_QUANTITY:
            int existingCountx = 0;

            for (int slotxx : slots) {
               ItemStack itemStackxx = blockMenu.getItemInSlot(slotxx);
               if (itemStackxx != null && itemStackxx.getType() != Material.AIR && StackUtils.itemsMatch(itemRequest, itemStackxx)) {
                  existingCountx += itemStackxx.getAmount();
               }
            }

            if (existingCountx < limitQuantity) {
               int deficit = limitQuantity - existingCountx;
               int availableSpace = 0;
               int[] openBuf = new int[slots.length];
               int openCount = 0;

               for (int slotxxx : slots) {
                  ItemStack itemStackxx = blockMenu.getItemInSlot(slotxxx);
                  if (itemStackxx == null || itemStackxx.getType() == Material.AIR) {
                     availableSpace += template.getMaxStackSize();
                     openBuf[openCount++] = slotxxx;
                  } else if (StackUtils.itemsMatch(itemRequest, itemStackxx)) {
                     int space = itemStackxx.getMaxStackSize() - itemStackxx.getAmount();
                     if (space > 0) {
                        availableSpace += space;
                        openBuf[openCount++] = slotxxx;
                     }
                  }
               }

               if (availableSpace <= 0) {
                  return;
               }

               int toRequest = Math.min(deficit, availableSpace);
               itemRequest.setAmount(toRequest);
               ItemStack retrievedx = root.getItemStack(accessor, itemRequest);
               if (retrievedx != null && retrievedx.getType() != Material.AIR) {
                  BlockMenuUtil.pushItem(blockMenu, retrievedx, openCount == slots.length ? slots : Arrays.copyOf(openBuf, openCount));
               }
            }
      }
   }

   public static void pushSlot(
      @NotNull Location accessor,
      @NotNull NetworkRoot root,
      @NotNull ItemRequest itemRequest,
      @NotNull BlockMenu blockMenu,
      @NotNull ItemStack template,
      int slot,
      int limitQuantity
   ) {
      ItemStack itemStack = blockMenu.getItemInSlot(slot);
      if (itemStack != null && itemStack.getType() != Material.AIR) {
         if (itemStack.getAmount() >= template.getMaxStackSize()) {
            return;
         }

         if (!StackUtils.itemsMatch(itemRequest, itemStack)) {
            return;
         }

         int space = itemStack.getMaxStackSize() - itemStack.getAmount();
         if (space <= 0) {
            return;
         }

         itemRequest.setAmount(space);
      } else {
         itemRequest.setAmount(template.getMaxStackSize());
      }

      itemRequest.setAmount(Math.min(itemRequest.getAmount(), limitQuantity));
      ItemStack retrieved = root.getItemStack(accessor, itemRequest);
      if (retrieved != null && retrieved.getType() != Material.AIR) {
         BlockMenuUtil.pushItem(blockMenu, retrieved, slot);
      }
   }

   public static void outPower(@NotNull Location location, @NotNull NetworkRoot root, int rate) {
      SlimefunBlockData blockData = StorageCacheUtils.getBlock(location);
      if (blockData != null) {
         if (!blockData.isDataLoaded()) {
            StorageCacheUtils.requestLoad(blockData);
         } else {
            SlimefunItem slimefunItem = SlimefunItem.getById(blockData.getSfId());
            if (slimefunItem instanceof EnergyNetComponent component && !(slimefunItem instanceof NetworkObject)) {
               int existingCharge = component.getCharge(location);
               int capacity = component.getCapacity();
               int space = capacity - existingCharge;
               if (space <= 0) {
                  return;
               }

               int possibleGeneration = Math.min(rate, space);
               long power = root.getRootPower();
               if (power <= 0L) {
                  return;
               }

               int gen = power < (long)possibleGeneration ? (int)power : possibleGeneration;
               component.addCharge(location, gen);
               root.removeRootPower((long)gen);
               return;
            }
         }
      }
   }

   @Generated
   private LineOperationUtil() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
