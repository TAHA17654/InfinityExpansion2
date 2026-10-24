package com.ytdd9527.networksexpansion.core.items.machines;

import com.balugaq.netex.api.enums.CraftType;
import com.balugaq.netex.api.enums.FeedbackType;
import com.balugaq.netex.api.helpers.Icon;
import com.balugaq.netex.api.interfaces.CraftTyped;
import com.balugaq.netex.utils.BlockMenuUtil;
import com.balugaq.netex.utils.Lang;
import com.ytdd9527.networksexpansion.core.items.unusable.Blueprint;
import com.ytdd9527.networksexpansion.utils.itemstacks.ItemStackUtil;
import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.NodeDefinition;
import io.github.sefiraat.networks.network.NodeType;
import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import io.github.sefiraat.networks.slimefun.NetworkSlimefunItems;
import io.github.sefiraat.networks.slimefun.network.NetworkObject;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.inventory.DirtyChestMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BlueprintEncoder extends NetworkObject implements CraftTyped {
   private static final int[] BACKGROUND = new int[]{
      0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 15, 17, 18, 20, 24, 25, 27, 28, 29, 33, 36, 37, 38, 39, 40, 41, 42, 43, 44
   };
   private static final int[] RECIPE_SLOTS = new int[]{12, 13, 14, 21, 22, 23, 30, 31, 32};
   private static final int[] BLUEPRINT_BACK = new int[]{10, 28};
   private static final int BLANK_BLUEPRINT_SLOT = 19;
   private static final int ENCODE_SLOT = 16;
   private static final int OUTPUT_SLOT = 34;
   private static final int ITEM_TARGET_DESC_SLOT = 26;
   private static final int ITEM_TARGET_SLOT = 35;
   private static final int CHARGE_COST = 2000;
   private CraftType craftType = CraftType.CRAFTING;
   private Blueprint blankBlueprint;

   public BlueprintEncoder(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe, NodeType.ENCODER);

      for (int recipeSlot : RECIPE_SLOTS) {
         this.getSlotsToDrop().add(recipeSlot);
      }

      this.getSlotsToDrop().add(19);
      this.getSlotsToDrop().add(34);
   }

   public void postRegister() {
      new BlockMenuPreset(this.getId(), this.getItemName()) {
         public void init() {
            this.drawBackground(BlueprintEncoder.BACKGROUND);
            this.drawBackground(Icon.BLUEPRINT_BACK_STACK, BlueprintEncoder.BLUEPRINT_BACK);
            this.addItem(16, Icon.ENCODE_STACK, (player, i, itemStack, clickAction) -> false);
            this.addItem(26, Icon.ITEM_TARGET_DESC_STACK, (player, i, itemStack, clickAction) -> false);
         }

         public void newInstance(@NotNull BlockMenu menu, @NotNull Block b) {
            menu.addMenuClickHandler(16, (player, s, itemStack, clickAction) -> {
               int times = clickAction.isShiftClicked() ? 64 : 1;
               int i = 0;

               while (i < times && BlueprintEncoder.this.tryEncode(player, menu)) {
                  i++;
               }

               return false;
            });
            ItemStack fix = menu.getItemInSlot(35);
            if (StackUtils.itemsMatch(fix, ChestMenuUtils.getBackground())) {
               menu.replaceExistingItem(35, null);
            }
         }

         public boolean canOpen(@NotNull Block block, @NotNull Player player) {
            return player.hasPermission("slimefun.inventory.bypass")
               || this.getSlimefunItem().canUse(player, false)
                  && Slimefun.getProtectionManager().hasPermission(player, block.getLocation(), Interaction.INTERACT_BLOCK);
         }

         public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
            return flow == ItemTransportFlow.WITHDRAW ? new int[]{34} : new int[0];
         }

         public int[] getSlotsAccessedByItemTransport(DirtyChestMenu menu, ItemTransportFlow flow, ItemStack itemStack) {
            if (flow == ItemTransportFlow.WITHDRAW) {
               return new int[]{34};
            } else {
               List<Integer> slots = new ArrayList<>();
               if (StackUtils.itemsMatch(itemStack, menu.getItemInSlot(19))) {
                  slots.add(19);
               }

               for (int slot : BlueprintEncoder.RECIPE_SLOTS) {
                  if (StackUtils.itemsMatch(itemStack, menu.getItemInSlot(slot))) {
                     slots.add(slot);
                  }
               }

               return slots.stream().mapToInt(Integer::intValue).toArray();
            }
         }
      };
   }

   public boolean tryEncode(@NotNull Player player, @NotNull BlockMenu blockMenu) {
      NodeDefinition definition = NetworkStorage.getNode(blockMenu.getLocation());
      if (definition != null && definition.getNode() != null) {
         NetworkRoot root = definition.getNode().getRoot();
         long networkCharge = root.getRootPower();
         if (networkCharge < 2000L) {
            player.sendMessage(Lang.getString("messages.unsupported-operation.encoder.not_enough_power"));
            this.sendFeedback(blockMenu.getLocation(), FeedbackType.NOT_ENOUGH_POWER);
            return false;
         } else {
            ItemStack blueprint = blockMenu.getItemInSlot(19);
            if (blueprint == null || blueprint.getType() == Material.AIR) {
               ItemStack bp = this.resolveBlank().getItem();
               blueprint = root.getItemStack(blockMenu.getLocation(), new ItemRequest(bp, bp.getMaxStackSize()));
               if (blueprint == null) {
                  player.sendMessage(Lang.getString("messages.feedback.no_blueprint_found"));
                  this.sendFeedback(blockMenu.getLocation(), FeedbackType.NO_BLUEPRINT_FOUND);
                  return false;
               }

               blockMenu.replaceExistingItem(19, blueprint);
            }

            SlimefunItem sfi = SlimefunItem.getByItem(blueprint);
            if (sfi != null && sfi.isDisabled()) {
               player.sendMessage(Lang.getString("messages.unsupported-operation.encoder.disabled_blueprint"));
               this.sendFeedback(blockMenu.getLocation(), FeedbackType.DISABLED_BLUEPRINT);
               return false;
            } else if (!this.isValidBlueprint(sfi)) {
               player.sendMessage(Lang.getString("messages.unsupported-operation.encoder.invalid_blueprint"));
               this.sendFeedback(blockMenu.getLocation(), FeedbackType.INVALID_BLUEPRINT);
               return false;
            } else {
               ItemStack[] inputs = new ItemStack[RECIPE_SLOTS.length];
               int i = 0;

               for (int recipeSlot : RECIPE_SLOTS) {
                  ItemStack stackInSlot = blockMenu.getItemInSlot(recipeSlot);
                  if (stackInSlot != null) {
                     inputs[i] = ItemStackUtil.getCleanItem(stackInSlot.clone());
                  }

                  i++;
               }

               ItemStack crafted = null;
               ItemStack[] inp = null;
               ItemStack target = blockMenu.getItemInSlot(35);
               if (target == null || target.getType() == Material.AIR) {
                  target = null;
               }

               label134:
               for (Entry<CraftType, Set<Entry<ItemStack[], ItemStack>>> e : this.encodeMap().entrySet()) {
                  boolean found = false;
                  Iterator var17 = e.getValue().iterator();

                  while (true) {
                     if (var17.hasNext()) {
                        Entry<ItemStack[], ItemStack> entry = (Entry<ItemStack[], ItemStack>)var17.next();
                        if (!this.testRecipe(e.getKey(), inputs, entry.getKey())) {
                           continue;
                        }

                        crafted = entry.getValue();
                        if (target != null && !StackUtils.itemsMatch(crafted, target)) {
                           continue;
                        }

                        inp = (ItemStack[])entry.getKey().clone();

                        for (int k = 0; k < inp.length; k++) {
                           if (inp[k] != null) {
                              inp[k] = ItemStackUtil.getCleanItem(inp[k]);
                           }
                        }

                        found = true;
                     }

                     if (found) {
                        break label134;
                     }
                     break;
                  }
               }

               if (crafted != null) {
                  SlimefunItem sfi2 = SlimefunItem.getByItem(crafted);
                  if (sfi2 != null && sfi2.isDisabled()) {
                     player.sendMessage(Lang.getString("messages.unsupported-operation.encoder.disabled_output"));
                     this.sendFeedback(blockMenu.getLocation(), FeedbackType.DISABLED_OUTPUT);
                     return false;
                  }
               }

               if (crafted == null && this.canTestVanillaRecipe(inputs)) {
                  crafted = Bukkit.craftItem((ItemStack[])inputs.clone(), player.getWorld(), player);
                  inp = new ItemStack[RECIPE_SLOTS.length];

                  for (int kx = 0; kx < RECIPE_SLOTS.length; kx++) {
                     if (inputs[kx] != null) {
                        inp[kx] = StackUtils.getAsQuantity(inputs[kx], 1);
                     }
                  }
               }

               if (inp != null && crafted != null && crafted.getType() != Material.AIR) {
                  ItemStack blueprintClone = StackUtils.getAsQuantity(blueprint, 1);
                  this.blueprintSetter(blueprintClone, inp, crafted.clone());
                  if (BlockMenuUtil.fits(blockMenu, blueprintClone, 34)) {
                     ItemStack recover = null;
                     if (blueprint.getAmount() == 1) {
                        recover = root.getItemStack(blockMenu.getLocation(), new ItemRequest(blueprint, blueprint.getMaxStackSize()));
                     }

                     blueprint.setAmount(blueprint.getAmount() - 1);
                     if (recover != null) {
                        BlockMenuUtil.pushItem(blockMenu, recover, 19);
                     }

                     int j = 0;

                     for (int recipeSlot : RECIPE_SLOTS) {
                        ItemStack slotItem = blockMenu.getItemInSlot(recipeSlot);
                        if (slotItem != null) {
                           slotItem.setAmount(slotItem.getAmount() - inp[j].getAmount());
                        }

                        j++;
                     }

                     BlockMenuUtil.pushItem(blockMenu, blueprintClone, 34);
                     this.sendFeedback(blockMenu.getLocation(), FeedbackType.SUCCESS);
                     root.removeRootPower(2000L);
                     return true;
                  } else {
                     player.sendMessage(Lang.getString("messages.unsupported-operation.encoder.output_full"));
                     this.sendFeedback(blockMenu.getLocation(), FeedbackType.OUTPUT_FULL);
                     return false;
                  }
               } else {
                  player.sendMessage(Lang.getString("messages.unsupported-operation.encoder.invalid_recipe"));
                  this.sendFeedback(blockMenu.getLocation(), FeedbackType.INVALID_RECIPE);
                  return false;
               }
            }
         }
      } else {
         this.sendFeedback(blockMenu.getLocation(), FeedbackType.NO_NETWORK_FOUND);
         player.sendMessage(Lang.getString("messages.feedback.no_network_found"));
         return false;
      }
   }

   public void blueprintSetter(ItemStack itemStack, @NotNull ItemStack[] inputs, ItemStack crafted) {
      this.craftType().blueprintSetter(itemStack, inputs, crafted);
   }

   public boolean isValidBlueprint(SlimefunItem item) {
      return this.craftType().isValidBlueprint(item);
   }

   public Set<Entry<ItemStack[], ItemStack>> getRecipeEntries() {
      return this.craftType().getRecipeEntries();
   }

   public boolean testRecipe(CraftType craftType, ItemStack[] inputs, ItemStack[] recipe) {
      return craftType.testRecipe(inputs, recipe);
   }

   public boolean canTestVanillaRecipe(ItemStack[] var1) {
      return this.craftType() == CraftType.CRAFTING;
   }

   public BlueprintEncoder(ItemGroup var1, SlimefunItemStack var2, RecipeType var3, ItemStack[] var4, CraftType var5, Blueprint var6) {
      this(var1, var2, var3, var4);
      this.craftType = var5;
      this.blankBlueprint = var6;
   }

   @Override
   public CraftType craftType() {
      return this.craftType;
   }

   private Blueprint resolveBlank() {
      Blueprint var10000 = this.blankBlueprint;
      if (this.blankBlueprint == null) {
         var10000 = NetworkSlimefunItems.CRAFTING_BLUEPRINT;
      }

      return var10000;
   }

   private Map encodeMap() {
      return this.craftType() == CraftType.CRAFTING ? CraftType.map() : Collections.singletonMap(this.craftType(), this.craftType().getRecipeEntries());
   }
}
