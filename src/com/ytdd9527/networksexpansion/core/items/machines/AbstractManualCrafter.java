package com.ytdd9527.networksexpansion.core.items.machines;

import com.balugaq.netex.api.data.SuperRecipe;
import com.balugaq.netex.api.enums.FeedbackType;
import com.balugaq.netex.utils.BlockMenuUtil;
import com.balugaq.netex.utils.Lang;
import com.ytdd9527.networksexpansion.core.items.SpecialSlimefunItem;
import com.ytdd9527.networksexpansion.utils.itemstacks.ItemStackUtil;
import io.github.sefiraat.networks.events.NetworkCraftEvent;
import io.github.sefiraat.networks.slimefun.network.AdminDebuggable;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemHandler;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNetComponentType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu.MenuClickHandler;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractManualCrafter extends SpecialSlimefunItem implements AdminDebuggable, EnergyNetComponent {
   public AbstractManualCrafter(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe);
   }

   public void preRegister() {
      BlockPlaceHandler blockPlaceHandler = this.getMachineBlockPlaceHandler();
      if (blockPlaceHandler != null) {
         this.addItemHandler(new ItemHandler[]{blockPlaceHandler});
      }

      BlockBreakHandler blockBreakHandler = this.getMachineBlockBreakHandler();
      if (blockBreakHandler != null) {
         this.addItemHandler(new ItemHandler[]{blockBreakHandler});
      }

      BlockTicker blockTicker = this.getMachineBlockTicker();
      if (blockTicker != null) {
         this.addItemHandler(new ItemHandler[]{blockTicker});
      }
   }

   public void postRegister() {
      new BlockMenuPreset(this.getId(), this.getItemName()) {
         public void init() {
            for (Entry<Integer, ItemStack> entry : AbstractManualCrafter.this.getBackgrounds().entrySet()) {
               this.addItem(entry.getKey(), entry.getValue(), ChestMenuUtils.getEmptyClickHandler());
            }
         }

         public boolean canOpen(@NotNull Block block, @NotNull Player player) {
            return player.hasPermission("slimefun.inventory.bypass")
               || this.getSlimefunItem().canUse(player, false)
                  && Slimefun.getProtectionManager().hasPermission(player, block.getLocation(), Interaction.INTERACT_BLOCK);
         }

         public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
            if (AbstractManualCrafter.this.isTransportable()) {
               return flow == ItemTransportFlow.WITHDRAW ? AbstractManualCrafter.this.getOutputSlots() : AbstractManualCrafter.this.getInputSlots();
            } else {
               return new int[0];
            }
         }

         public void newInstance(@NotNull BlockMenu menu, @NotNull Block b) {
            menu.addMenuClickHandler(AbstractManualCrafter.this.getHandleSlot(), (p, slot, item, action) -> {
               AbstractManualCrafter.this.craft(p, menu);
               return false;
            });
            Map<Integer, MenuClickHandler> menuClickHandlers = AbstractManualCrafter.this.getMenuClickHandlers();
            if (menuClickHandlers != null) {
               for (Entry<Integer, MenuClickHandler> entry : menuClickHandlers.entrySet()) {
                  menu.addMenuClickHandler(entry.getKey(), entry.getValue());
               }
            }
         }
      };
   }

   public void craft(@NotNull Player player, @NotNull BlockMenu blockMenu) {
      boolean success = false;

      for (SuperRecipe recipe : this.getRecipes()) {
         if (this.getCapacity() >= recipe.getConsumeEnergy()) {
            if (this.getCharge(blockMenu.getLocation()) >= recipe.getConsumeEnergy()) {
               if (recipe.getHandler() != null) {
                  success = recipe.getHandler().handle(player, blockMenu);
               } else if (recipe.isShaped()) {
                  success = this.shapedCraft(recipe, player, blockMenu);
               } else {
                  success = this.shapelessCraft(recipe, player, blockMenu);
               }
            }

            if (success) {
               break;
            }
         }
      }
   }

   public boolean shapedCraft(@NotNull SuperRecipe recipe, @NotNull Player player, @NotNull BlockMenu blockMenu) {
      World world = blockMenu.getLocation().getWorld();
      if (world == null) {
         return false;
      } else {
         for (int i = 0; i < Math.min(recipe.getInput().length, this.getInputSlots().length); i++) {
            ItemStack wanted = recipe.getInput()[i];
            if (wanted != null && wanted.getType() != Material.AIR) {
               ItemStack itemInSlot = blockMenu.getItemInSlot(this.getInputSlots()[i]);
               if (itemInSlot == null || itemInSlot.getType() == Material.AIR) {
                  return false;
               }

               if (!StackUtils.itemsMatch(itemInSlot, wanted, false, true)) {
                  return false;
               }
            }
         }

         if (!BlockMenuUtil.fits(blockMenu, recipe.getOutput(), this.getOutputSlots())) {
            this.sendFeedback(blockMenu.getLocation(), FeedbackType.NO_ENOUGH_SPACE);
            return false;
         } else {
            for (int ix = 0; ix < Math.min(recipe.getInput().length, this.getInputSlots().length); ix++) {
               ItemStack wanted = recipe.getInput()[ix];
               if (wanted != null && wanted.getType() != Material.AIR) {
                  BlockMenuUtil.consumeItem(blockMenu, this.getInputSlots()[ix], recipe.getInput()[ix].getAmount());
               }
            }

            for (ItemStack item : recipe.getOutput()) {
               if (item != null && item.getType() != Material.AIR) {
                  SlimefunItem sfi = SlimefunItem.getByItem(item);
                  if (sfi == null || !sfi.isDisabled() && !sfi.isDisabledIn(world)) {
                     NetworkCraftEvent event = new NetworkCraftEvent(player, this, recipe.getInput(), item);
                     Bukkit.getPluginManager().callEvent(event);
                     if (event.isCancelled()) {
                        return false;
                     }

                     item = event.getOutput();
                     ItemStack left = BlockMenuUtil.pushItem(blockMenu, ItemStackUtil.getCleanItem(item), this.getOutputSlots());
                     if (left != null && left.getType() != Material.AIR) {
                        player.sendMessage(Lang.getString("messages.unsupported-operation.manual_crafter.full-output"));
                        this.sendFeedback(blockMenu.getLocation(), FeedbackType.NO_ENOUGH_SPACE);
                        world.dropItem(blockMenu.getLocation(), left);
                     }
                  } else {
                     player.sendMessage(Lang.getString("messages.unsupported-operation.manual_crafter.disabled-output"));
                     this.sendFeedback(blockMenu.getLocation(), FeedbackType.DISABLED_OUTPUT);
                  }
               }
            }

            if (recipe.getConsumeEnergy() > 0) {
               this.removeCharge(blockMenu.getLocation(), recipe.getConsumeEnergy());
            }

            player.sendMessage(Lang.getString("messages.completed-operation.manual_crafter.success"));
            this.sendFeedback(blockMenu.getLocation(), FeedbackType.SUCCESS);
            return true;
         }
      }
   }

   public boolean shapelessCraft(@NotNull SuperRecipe recipe, @NotNull Player player, @NotNull BlockMenu blockMenu) {
      World world = blockMenu.getLocation().getWorld();
      if (world == null) {
         return false;
      } else {
         Map<ItemStack, Integer> wanted = new HashMap<>();

         for (int i = 0; i < Math.min(recipe.getInput().length, this.getInputSlots().length); i++) {
            ItemStack wantedItem = recipe.getInput()[i];
            if (wantedItem != null && wantedItem.getType() != Material.AIR) {
               if (wanted.containsKey(wantedItem)) {
                  wanted.put(wantedItem, wanted.get(wantedItem) + wantedItem.getAmount());
               } else {
                  wanted.put(wantedItem, wantedItem.getAmount());
               }
            }
         }

         Map<ItemStack, Integer> have = new HashMap<>();

         for (int slot : this.getInputSlots()) {
            ItemStack itemInSlot = blockMenu.getItemInSlot(slot);
            if (itemInSlot != null && itemInSlot.getType() != Material.AIR) {
               if (have.containsKey(itemInSlot)) {
                  have.put(itemInSlot, have.get(itemInSlot) + itemInSlot.getAmount());
               } else {
                  have.put(itemInSlot, itemInSlot.getAmount());
               }
            }
         }

         for (Entry<ItemStack, Integer> entry : wanted.entrySet()) {
            if (!have.containsKey(entry.getKey()) || have.get(entry.getKey()) < entry.getValue()) {
               break;
            }
         }

         if (!BlockMenuUtil.fits(blockMenu, recipe.getOutput(), this.getOutputSlots())) {
            this.sendFeedback(blockMenu.getLocation(), FeedbackType.NO_ENOUGH_SPACE);
            return false;
         } else {
            for (Entry<ItemStack, Integer> entryx : wanted.entrySet()) {
               for (int slotx : this.getInputSlots()) {
                  ItemStack itemInSlot = blockMenu.getItemInSlot(slotx);
                  if (itemInSlot != null && itemInSlot.getType() != Material.AIR) {
                     if (StackUtils.itemsMatch(itemInSlot, entryx.getKey())) {
                        int amount = Math.min(entryx.getValue(), itemInSlot.getAmount());
                        BlockMenuUtil.consumeItem(blockMenu, slotx, amount);
                        wanted.put(entryx.getKey(), wanted.get(entryx.getKey()) - amount);
                     }

                     if (wanted.get(entryx.getKey()) <= 0) {
                        wanted.remove(entryx.getKey());
                        break;
                     }
                  }
               }
            }

            assert wanted.isEmpty();

            for (ItemStack item : recipe.getOutput()) {
               if (item != null && item.getType() != Material.AIR) {
                  SlimefunItem sfi = SlimefunItem.getByItem(item);
                  if (sfi == null || !sfi.isDisabled() && !sfi.isDisabledIn(world)) {
                     ItemStack left = BlockMenuUtil.pushItem(blockMenu, item, this.getOutputSlots());
                     if (left != null && left.getType() != Material.AIR) {
                        player.sendMessage(Lang.getString("messages.unsupported-operation.manual_crafter.full-output"));
                        this.sendFeedback(blockMenu.getLocation(), FeedbackType.NO_ENOUGH_SPACE);
                        world.dropItem(blockMenu.getLocation(), left);
                     }
                  } else {
                     player.sendMessage(Lang.getString("messages.unsupported-operation.manual_crafter.disabled-output"));
                     this.sendFeedback(blockMenu.getLocation(), FeedbackType.DISABLED_OUTPUT);
                  }
               }
            }

            if (recipe.getConsumeEnergy() > 0) {
               this.removeCharge(blockMenu.getLocation(), recipe.getConsumeEnergy());
            }

            player.sendMessage(Lang.getString("messages.completed-operation.manual_crafter.success"));
            this.sendFeedback(blockMenu.getLocation(), FeedbackType.SUCCESS);
            return true;
         }
      }
   }

   @NotNull
   public EnergyNetComponentType getEnergyComponentType() {
      return EnergyNetComponentType.CONSUMER;
   }

   public abstract List<SuperRecipe> getRecipes();

   public abstract int[] getInputSlots();

   public abstract int[] getOutputSlots();

   public abstract int getHandleSlot();

   public abstract boolean isTransportable();

   public abstract Map<Integer, ItemStack> getBackgrounds();

   @Nullable
   public abstract Map<Integer, MenuClickHandler> getMenuClickHandlers();

   public abstract BlockPlaceHandler getMachineBlockPlaceHandler();

   public abstract BlockBreakHandler getMachineBlockBreakHandler();

   @Nullable
   public abstract BlockTicker getMachineBlockTicker();
}
