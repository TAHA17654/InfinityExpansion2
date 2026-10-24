package com.ytdd9527.networksexpansion.implementation.machines.networks.advanced;

import com.balugaq.netex.api.enums.FeedbackType;
import com.balugaq.netex.api.enums.FilterMode;
import com.balugaq.netex.api.enums.MatchMode;
import com.balugaq.netex.api.helpers.Icon;
import com.balugaq.netex.utils.Lang;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import dev.sefiraat.sefilib.misc.ParticleUtils;
import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.managers.SupportedPluginManager;
import io.github.sefiraat.networks.network.NodeDefinition;
import io.github.sefiraat.networks.network.NodeType;
import io.github.sefiraat.networks.slimefun.NetworkSlimefunItems;
import io.github.sefiraat.networks.slimefun.network.NetworkObject;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemHandler;
import io.github.thebusybiscuit.slimefun4.api.items.ItemSetting;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.items.settings.IntRangeSetting;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.ParametersAreNonnullByDefault;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class AdvancedVacuum extends NetworkObject {
   public static final Map<Location, FilterMode> CACHE_FILTER_MODE = new HashMap<>();
   public static final Map<Location, MatchMode> CACHE_MATCH_MODE = new HashMap<>();
   public static final String BS_FILTER_MODE = "filter-mode";
   public static final String BS_MATCH_MODE = "match-mode";
   public static final Map<Location, List<ItemStack>> CACHE_FILTER_ITEMS = new ConcurrentHashMap<>();
   private static final int[] BACKGROUND_SLOTS = new int[]{38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53};
   private static final int[] INPUT_SLOTS = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17};
   private static final int[] SPLIT_SLOTS = new int[]{18, 19, 20, 21, 22, 23, 24, 25, 26};
   private static final int[] FILTER_SLOTS = new int[]{27, 28, 29, 30, 31, 32, 33, 34, 35};
   private static final int FILTER_MODE_SLOT = 36;
   private static final int MATCH_MODE_SLOT = 37;
   @NotNull
   private final ItemSetting<Integer> tickRate = new IntRangeSetting(this, "tick_rate", 1, 1, 10);
   @NotNull
   private final ItemSetting<Integer> vacuumRange = new IntRangeSetting(this, "vacuum_range", 1, 2, 5);

   public AdvancedVacuum(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe, NodeType.ADVANCED_VACUUM);
      this.addItemSetting(new ItemSetting[]{this.tickRate, this.vacuumRange});

      for (int inputSlot : INPUT_SLOTS) {
         this.getSlotsToDrop().add(inputSlot);
      }

      for (int inputSlot : FILTER_SLOTS) {
         this.getSlotsToDrop().add(inputSlot);
      }

      this.addItemHandler(new ItemHandler[]{new BlockTicker() {
         private int tick = 1;

         public boolean isSynchronized() {
            return false;
         }

         public void tick(@NotNull Block block, SlimefunItem item, @NotNull SlimefunBlockData data) {
            if (this.tick <= 1) {
               BlockMenu blockMenu = data.getBlockMenu();
               if (blockMenu == null) {
                  return;
               }

               AdvancedVacuum.this.addToRegistry(block);
               AdvancedVacuum.this.tryAddItem(blockMenu);
               Bukkit.getScheduler().runTask(Networks.getInstance(), bukkitTask -> AdvancedVacuum.this.findItem(blockMenu));
            }
         }

         public void uniqueTick() {
            this.tick = this.tick <= 1 ? (Integer)AdvancedVacuum.this.tickRate.getValue() : this.tick - 1;
         }
      }});
      this.addItemHandler(new ItemHandler[]{new BlockPlaceHandler(false) {
         public void onPlayerPlace(@NotNull BlockPlaceEvent event) {
            NetworkStorage.removeNode(event.getBlock().getLocation());
            SlimefunBlockData blockData = StorageCacheUtils.getBlock(event.getBlock().getLocation());
            if (blockData != null) {
               blockData.setData("uuid", event.getPlayer().getUniqueId().toString());
            }
         }
      }});
      this.addItemHandler(new ItemHandler[]{new BlockBreakHandler(false, false) {
         @ParametersAreNonnullByDefault
         public void onPlayerBreak(BlockBreakEvent blockBreakEvent, ItemStack itemStack, List<ItemStack> list) {
            Location location = blockBreakEvent.getBlock().getLocation();
            AdvancedVacuum.CACHE_FILTER_ITEMS.remove(location);
            AdvancedVacuum.CACHE_FILTER_MODE.remove(location);
            AdvancedVacuum.CACHE_MATCH_MODE.remove(location);
         }
      }});
   }

   public static void clearFilterItemsCache(@NotNull BlockMenu menu) {
      CACHE_FILTER_ITEMS.remove(menu.getLocation());
   }

   @NotNull
   public static List<ItemStack> getFilterItemStacksFromMenu(@NotNull BlockMenu menu) {
      List<ItemStack> filterItemStacks = new ArrayList<>();

      for (int slot : FILTER_SLOTS) {
         ItemStack itemStack = menu.getItemInSlot(slot);
         if (itemStack != null && itemStack.getType() != Material.AIR) {
            filterItemStacks.add(itemStack);
         }
      }

      return filterItemStacks;
   }

   @NotNull
   public static List<ItemStack> getFilterItemStacks(@NotNull BlockMenu menu) {
      List<ItemStack> cache = CACHE_FILTER_ITEMS.get(menu.getLocation());
      if (cache != null) {
         return cache;
      } else {
         List<ItemStack> filterItemStacks = getFilterItemStacksFromMenu(menu);
         CACHE_FILTER_ITEMS.put(menu.getLocation(), filterItemStacks);
         return filterItemStacks;
      }
   }

   public static boolean isAllowedItemStack(@NotNull BlockMenu menu, @NotNull ItemStack item) {
      List<ItemStack> itemStacks = getFilterItemStacks(menu);
      FilterMode filterMode = getFilterMode(menu);
      MatchMode matchMode = getMatchMode(menu);
      if (filterMode == FilterMode.WHITE_LIST) {
         for (ItemStack itemStack : itemStacks) {
            if (matchMode == MatchMode.MATERIAL_MATCH) {
               if (itemStack.getType() == item.getType()) {
                  return true;
               }
            } else {
               if (matchMode != MatchMode.ALL_MATCH) {
                  return true;
               }

               if (StackUtils.itemsMatch(itemStack, item, true, false)) {
                  return true;
               }
            }
         }

         return false;
      } else if (filterMode == FilterMode.BLACK_LIST) {
         for (ItemStack itemStackx : itemStacks) {
            if (matchMode == MatchMode.MATERIAL_MATCH) {
               if (itemStackx.getType() == item.getType()) {
                  return false;
               }
            } else {
               if (matchMode != MatchMode.ALL_MATCH) {
                  return true;
               }

               if (StackUtils.itemsMatch(itemStackx, item, true, false)) {
                  return false;
               }
            }
         }

         return true;
      } else {
         return true;
      }
   }

   public static void setFilterMode(@NotNull BlockMenu blockMenu, @NotNull FilterMode filterMode) {
      Location location = blockMenu.getLocation();
      CACHE_FILTER_MODE.put(location, filterMode);
      StorageCacheUtils.setData(location, "filter-mode", filterMode.name());
      updateBlockMenu(blockMenu);
   }

   public static void setMatchMode(@NotNull BlockMenu blockMenu, @NotNull MatchMode matchMode) {
      Location location = blockMenu.getLocation();
      CACHE_MATCH_MODE.put(location, matchMode);
      StorageCacheUtils.setData(location, "match-mode", matchMode.name());
      updateBlockMenu(blockMenu);
   }

   @NotNull
   public static FilterMode getFilterMode(@NotNull BlockMenu blockMenu) {
      Location location = blockMenu.getLocation();
      FilterMode cache = CACHE_FILTER_MODE.get(location);
      if (cache != null) {
         return cache;
      } else {
         String mode = StorageCacheUtils.getData(location, "filter-mode");
         if (mode == null) {
            setFilterMode(blockMenu, FilterMode.BLACK_LIST);
            return FilterMode.BLACK_LIST;
         } else {
            try {
               return FilterMode.valueOf(mode);
            } catch (IllegalArgumentException var5) {
               setFilterMode(blockMenu, FilterMode.BLACK_LIST);
               return FilterMode.BLACK_LIST;
            }
         }
      }
   }

   @NotNull
   public static MatchMode getMatchMode(@NotNull BlockMenu blockMenu) {
      Location location = blockMenu.getLocation();
      MatchMode cache = CACHE_MATCH_MODE.get(location);
      if (cache != null) {
         return cache;
      } else {
         String mode = StorageCacheUtils.getData(location, "match-mode");
         if (mode == null) {
            setMatchMode(blockMenu, MatchMode.ALL_MATCH);
            return MatchMode.ALL_MATCH;
         } else {
            try {
               return MatchMode.valueOf(mode);
            } catch (IllegalArgumentException var5) {
               setMatchMode(blockMenu, MatchMode.ALL_MATCH);
               return MatchMode.ALL_MATCH;
            }
         }
      }
   }

   @NotNull
   public static ItemStack getFilterModeIcon(@NotNull FilterMode filterMode) {
      return switch (filterMode) {
         case BLACK_LIST -> Icon.FILTER_MODE_BLACK_LIST;
         case WHITE_LIST -> Icon.FILTER_MODE_WHITE_LIST;
      };
   }

   @NotNull
   public static ItemStack getMatchModeIcon(@NotNull MatchMode matchMode) {
      return switch (matchMode) {
         case ALL_MATCH -> Icon.MATCH_MODE_ALL_MATCH;
         case MATERIAL_MATCH -> Icon.MATCH_MODE_MATERIAL_MATCH;
      };
   }

   public static void toggleFilterMode(@NotNull Player player, @NotNull BlockMenu menu) {
      FilterMode currentMode = FilterMode.BLACK_LIST;
      switch (getFilterMode(menu)) {
         case BLACK_LIST:
            setFilterMode(menu, FilterMode.WHITE_LIST);
            currentMode = FilterMode.WHITE_LIST;
            break;
         case WHITE_LIST:
            setFilterMode(menu, FilterMode.BLACK_LIST);
      }

      player.sendMessage(
         String.format(
            Lang.getString("messages.completed-operation.comprehensive.toggled_filter_mode"),
            currentMode == FilterMode.BLACK_LIST
               ? Lang.getString("messages.completed-operation.comprehensive.filter_mode_black_list")
               : Lang.getString("messages.completed-operation.comprehensive.filter_mode_white_list")
         )
      );
   }

   public static void toggleMatchMode(@NotNull Player player, @NotNull BlockMenu menu) {
      MatchMode currentMode = MatchMode.ALL_MATCH;
      switch (getMatchMode(menu)) {
         case ALL_MATCH:
            setMatchMode(menu, MatchMode.MATERIAL_MATCH);
            currentMode = MatchMode.MATERIAL_MATCH;
            break;
         case MATERIAL_MATCH:
            setMatchMode(menu, MatchMode.ALL_MATCH);
      }

      player.sendMessage(
         String.format(
            Lang.getString("messages.completed-operation.comprehensive.toggled_match_mode"),
            currentMode == MatchMode.ALL_MATCH
               ? Lang.getString("messages.completed-operation.comprehensive.match_mode_all_match")
               : Lang.getString("messages.completed-operation.comprehensive.match_mode_material_match")
         )
      );
   }

   public static void updateBlockMenu(@NotNull BlockMenu menu) {
      menu.addItem(36, getFilterModeIcon(getFilterMode(menu)));
      menu.addItem(37, getMatchModeIcon(getMatchMode(menu)));
   }

   private void findItem(@NotNull BlockMenu blockMenu) {
      for (int inputSlot : INPUT_SLOTS) {
         ItemStack inSlot = blockMenu.getItemInSlot(inputSlot);
         if (inSlot == null || inSlot.getType() == Material.AIR) {
            Location location = blockMenu.getLocation().clone().add(0.5, 0.5, 0.5);
            int range = (Integer)this.vacuumRange.getValue();
            Collection<Entity> items = location.getWorld().getNearbyEntities(location, (double)range, (double)range, (double)range, Item.class::isInstance);

            for (Entity optionalEntity : items.stream().toList()) {
               if (optionalEntity instanceof Item item) {
                  String ownerUUID = StorageCacheUtils.getData(blockMenu.getLocation(), "uuid");
                  if (ownerUUID == null) {
                     this.sendFeedback(blockMenu.getLocation(), FeedbackType.NO_OWNER_FOUND);
                     return;
                  }

                  UUID uuid = UUID.fromString(ownerUUID);
                  OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                  if (!Slimefun.getProtectionManager().hasPermission(offlinePlayer, item.getLocation(), Interaction.INTERACT_ENTITY)) {
                     this.sendFeedback(blockMenu.getLocation(), FeedbackType.NO_PERMISSION);
                     return;
                  }

                  if (item.getPickupDelay() <= 0 && !SlimefunUtils.hasNoPickupFlag(item)) {
                     ItemStack itemStack = item.getItemStack().clone();
                     if (isAllowedItemStack(blockMenu, itemStack)) {
                        ItemStack finalPush = item.getItemStack().clone();
                        int amount = SupportedPluginManager.getStackAmount(item);
                        int maxAmount = item.getItemStack().getMaxStackSize();
                        if (amount <= 0) {
                           return;
                        }

                        if (amount > maxAmount) {
                           SupportedPluginManager.setStackAmount(item, amount - maxAmount);
                           finalPush.setAmount(maxAmount);
                        } else {
                           finalPush.setAmount(amount);
                           item.remove();
                        }

                        blockMenu.replaceExistingItem(inputSlot, finalPush);
                        ParticleUtils.displayParticleRandomly(item, 1.0, 5, new DustOptions(Color.BLUE, 1.0F));
                        return;
                     }

                     this.sendFeedback(blockMenu.getLocation(), FeedbackType.NOT_ALLOWED_ITEM);
                  }
               } else {
                  this.sendFeedback(blockMenu.getLocation(), FeedbackType.NO_ITEM_FOUND);
               }
            }
         }
      }

      this.sendFeedback(blockMenu.getLocation(), FeedbackType.WORKING);
   }

   private void tryAddItem(@NotNull BlockMenu blockMenu) {
      NodeDefinition definition = NetworkStorage.getNode(blockMenu.getLocation());
      if (definition.getNode() == null) {
         this.sendFeedback(blockMenu.getLocation(), FeedbackType.NO_NETWORK_FOUND);
      } else {
         for (int inputSlot : INPUT_SLOTS) {
            ItemStack itemStack = blockMenu.getItemInSlot(inputSlot);
            if (itemStack != null && itemStack.getType() != Material.AIR) {
               definition.getNode().getRoot().addItemStack(blockMenu.getLocation(), itemStack);
            }
         }

         this.sendFeedback(blockMenu.getLocation(), FeedbackType.WORKING);
      }
   }

   public void postRegister() {
      new BlockMenuPreset(this.getId(), this.getItemName()) {
         public void init() {
            this.setSize(54);
            this.drawBackground(AdvancedVacuum.BACKGROUND_SLOTS);
            this.drawBackground(Icon.ADVANCED_VACUUM_SPLIT_BLOCK, AdvancedVacuum.SPLIT_SLOTS);
         }

         public void newInstance(@NotNull BlockMenu menu, @NotNull Block b) {
            AdvancedVacuum.updateBlockMenu(menu);
            menu.addMenuClickHandler(36, (p, s, i, a) -> {
               AdvancedVacuum.toggleFilterMode(p, menu);
               return false;
            });
            menu.addMenuClickHandler(37, (p, s, i, a) -> {
               AdvancedVacuum.toggleMatchMode(p, menu);
               return false;
            });
            menu.addMenuOpeningHandler(p -> AdvancedVacuum.clearFilterItemsCache(menu));
            menu.addMenuCloseHandler(p -> AdvancedVacuum.clearFilterItemsCache(menu));
         }

         public boolean canOpen(@NotNull Block block, @NotNull Player player) {
            return player.hasPermission("slimefun.inventory.bypass")
               || NetworkSlimefunItems.NETWORK_VACUUM.canUse(player, false)
                  && Slimefun.getProtectionManager().hasPermission(player, block.getLocation(), Interaction.INTERACT_BLOCK);
         }

         public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
            return flow == ItemTransportFlow.INSERT ? AdvancedVacuum.INPUT_SLOTS : new int[0];
         }
      };
   }
}
