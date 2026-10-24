package com.ytdd9527.networksexpansion.implementation.machines.manual;

import com.balugaq.netex.api.data.StorageUnitData;
import com.balugaq.netex.api.enums.FeedbackType;
import com.balugaq.netex.api.enums.StorageUnitType;
import com.balugaq.netex.api.helpers.Icon;
import com.balugaq.netex.utils.BlockMenuUtil;
import com.balugaq.netex.utils.Lang;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.core.items.SpecialSlimefunItem;
import com.ytdd9527.networksexpansion.implementation.ExpansionItemStacks;
import com.ytdd9527.networksexpansion.implementation.machines.unit.NetworksDrawer;
import com.ytdd9527.networksexpansion.utils.databases.DataStorage;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.events.NetworkCraftEvent;
import io.github.sefiraat.networks.slimefun.network.AdminDebuggable;
import io.github.sefiraat.networks.utils.Keys;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemHandler;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu.AdvancedMenuClickHandler;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StorageUnitUpgradeTable extends SpecialSlimefunItem implements AdminDebuggable {
   private static final Map<ItemStack[], ItemStack> recipes = new HashMap<>();
   public static final RecipeType TYPE = new RecipeType(
      Keys.STORAGE_UNIT_UPGRADE_TABLE, ExpansionItemStacks.STORAGE_UNIT_UPGRADE_TABLE, StorageUnitUpgradeTable::addRecipe, new String[0]
   );
   private final int[] border = new int[]{0, 8, 9, 17, 18, 26};
   private final int[] innerBorder = new int[]{1, 5, 6, 7, 10, 14, 16, 19, 23, 24, 25};
   private final int[] inputSlots = new int[]{2, 3, 4, 11, 12, 13, 20, 21, 22};
   private final int outputSlot = 15;
   private final int actionBtnSlot = 17;

   public StorageUnitUpgradeTable(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe);
      new BlockMenuPreset(this.getId(), this.getItemName()) {
         public void init() {
            for (int slot : StorageUnitUpgradeTable.this.border) {
               this.addItem(slot, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
            }

            ItemStack innerBorderItem = new CustomItemStack(Material.LIME_STAINED_GLASS_PANE, " ", new String[]{" "});

            for (int slot : StorageUnitUpgradeTable.this.innerBorder) {
               this.addItem(slot, innerBorderItem, ChestMenuUtils.getEmptyClickHandler());
            }

            this.addItem(15, null, new AdvancedMenuClickHandler() {
               public boolean onClick(@NotNull InventoryClickEvent e, Player p, int slot, @Nullable ItemStack cursor, ClickAction action) {
                  ItemStack itemInSlot = e.getInventory().getItem(slot);
                  return (cursor == null || cursor.getType() == Material.AIR) && (itemInSlot == null || itemInSlot.getType() != Material.BARRIER);
               }

               public boolean onClick(Player p, int slot, ItemStack cursor, ClickAction action) {
                  return false;
               }
            });
         }

         public void newInstance(@NotNull BlockMenu menu, @NotNull Block b) {
            menu.addMenuClickHandler(17, (p, slot, item, action) -> {
               StorageUnitUpgradeTable.this.craft(p, menu);
               return false;
            });
            menu.replaceExistingItem(17, Icon.ACTION_BUTTON);
         }

         public boolean canOpen(@NotNull Block b, @NotNull Player p) {
            return p.hasPermission("slimefun.inventory.bypass")
               || StorageUnitUpgradeTable.this.canUse(p, false) && Slimefun.getProtectionManager().hasPermission(p, b, Interaction.INTERACT_BLOCK);
         }

         public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
            return new int[0];
         }
      };
   }

   public static void addRecipe(ItemStack[] recipe, ItemStack out) {
      recipes.put(recipe, out);
   }

   private void craft(@NotNull Player p, @NotNull BlockMenu menu) {
      for (Entry<ItemStack[], ItemStack> each : recipes.entrySet()) {
         if (this.match(menu, each.getKey())) {
            ItemStack itemInSlot = menu.getItemInSlot(15);
            int id = NetworksDrawer.getBoundId(menu.getItemInSlot(this.inputSlots[4]));
            ItemStack out = each.getValue().clone();
            if (id != -1) {
               if (itemInSlot != null && itemInSlot.getType() != Material.AIR) {
                  return;
               }

               if (DataStorage.isContainerLoaded(id)) {
                  if (DataStorage.getCachedStorageData(id).isPresent()) {
                     StorageUnitData data = DataStorage.getCachedStorageData(id).get();
                     this.upgrade(data);
                  }
               } else {
                  DataStorage.requestStorageData(id);
                  Networks.getQueryQueue().scheduleQuery(() -> {
                     if (DataStorage.getCachedStorageData(id).isPresent()) {
                        StorageUnitData data = DataStorage.getCachedStorageData(id).get();
                        this.upgrade(data);
                     }

                     return true;
                  });
               }

               out = NetworksDrawer.bindIdNew(out, id, NetworksDrawer.isLocked(id), NetworksDrawer.isVoidExcess(id));
            }

            SlimefunItemStack sfis = (SlimefunItemStack)out;
            SlimefunItem sfi = SlimefunItem.getById(sfis.getItemId());
            if (sfi != null && sfi.isDisabled()) {
               return;
            }

            ItemStack[] inputs = new ItemStack[this.inputSlots.length];

            for (int i = 0; i < this.inputSlots.length; i++) {
               ItemStack itemStack = menu.getItemInSlot(this.inputSlots[i]);
               if (itemStack == null) {
                  inputs[i] = null;
               } else {
                  inputs[i] = itemStack.clone();
               }
            }

            NetworkCraftEvent event = new NetworkCraftEvent(p, this, inputs, out);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
               return;
            }

            out = event.getOutput();
            if (itemInSlot != null && itemInSlot.getType() != Material.AIR) {
               if (!StackUtils.itemsMatch(itemInSlot, out)) {
                  return;
               }

               if (itemInSlot.getAmount() + (out == null ? 0 : out.getAmount()) > itemInSlot.getMaxStackSize()) {
                  return;
               }

               itemInSlot.setAmount(itemInSlot.getAmount() + (out == null ? 0 : out.getAmount()));
            } else {
               menu.replaceExistingItem(15, out);
            }

            for (int slot : this.inputSlots) {
               BlockMenuUtil.consumeItem(menu, slot);
            }

            this.sendFeedback(menu.getLocation(), FeedbackType.SUCCESS);
            return;
         }
      }

      p.sendMessage(Lang.getString("messages.unsupported-operation.storage_unit_upgrade_table.no_recipe_match"));
   }

   private boolean match(@NotNull BlockMenu menu, ItemStack[] recipe) {
      for (int i = 0; i < 9; i++) {
         if (i == 4) {
            SlimefunItem sfItem = SlimefunItem.getByItem(menu.getItemInSlot(this.inputSlots[i]));
            if (sfItem != null && SlimefunUtils.isItemSimilar(recipe[i], sfItem.getItem(), true, false)) {
               continue;
            }

            if (!StackUtils.itemsMatch(recipe[i], menu.getItemInSlot(this.inputSlots[i]))) {
               return false;
            }
         }

         if (!StackUtils.itemsMatch(recipe[i], menu.getItemInSlot(this.inputSlots[i]))) {
            return false;
         }
      }

      return true;
   }

   private void upgrade(@NotNull StorageUnitData d) {
      StorageUnitType next = d.getSizeType().next();
      if (next != null) {
         d.setSizeType(next);
      }
   }

   public void preRegister() {
      this.addItemHandler(new ItemHandler[]{this.getBlockBreakHandler()});
   }

   @NotNull
   private BlockBreakHandler getBlockBreakHandler() {
      return new BlockBreakHandler(false, false) {
         public void onPlayerBreak(@NotNull BlockBreakEvent event, @NotNull ItemStack itemStack, @NotNull List<ItemStack> drops) {
            Location l = event.getBlock().getLocation();
            BlockMenu menu = StorageCacheUtils.getMenu(l);
            if (menu != null) {
               menu.dropItems(menu.getLocation(), StorageUnitUpgradeTable.this.inputSlots);
               menu.dropItems(menu.getLocation(), new int[]{15});
            }

            Slimefun.getDatabaseManager().getBlockDataController().removeBlock(l);
         }
      };
   }
}
