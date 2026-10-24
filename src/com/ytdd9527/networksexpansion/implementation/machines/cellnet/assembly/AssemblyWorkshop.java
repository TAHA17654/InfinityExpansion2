package com.ytdd9527.networksexpansion.implementation.machines.cellnet.assembly;

import com.balugaq.jeg.api.recipe_complete.RecipeCompletableRegistry;
import com.balugaq.netex.utils.BlockMenuUtil;
import com.balugaq.netex.utils.Debug;
import com.balugaq.netex.utils.Lang;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.core.items.SpecialSlimefunItem;
import com.ytdd9527.networksexpansion.implementation.ExpansionItems;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.assembly.recipe.AssemblyCardRecipes;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.drive.DriveOwnership;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.NetworkUtil;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.ui.Icons;
import com.ytdd9527.networksexpansion.utils.itemstacks.ItemStackUtil;
import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.NodeDefinition;
import io.github.sefiraat.networks.network.NodeType;
import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import io.github.sefiraat.networks.utils.Keys;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemHandler;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.libraries.dough.data.persistent.PersistentDataAPI;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import java.util.ArrayList;
import java.util.List;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.inventory.DirtyChestMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class AssemblyWorkshop extends SpecialSlimefunItem {
   private static final int[] RECIPE_SLOTS = new int[]{12, 13, 14, 21, 22, 23, 30, 31, 32};
   private static final int CARD_SLOT = 19;
   private static final int ENCODE_SLOT = 16;
   private static final int CLEAR_SLOT = 26;
   private static final int OUTPUT_SLOT = 34;
   private static final int[] BACKGROUND = new int[]{
      0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 15, 17, 18, 20, 24, 25, 27, 28, 29, 33, 35, 36, 37, 38, 39, 40, 41, 42, 44
   };
   private static final long ENCODE_COST = 2000L;

   public AssemblyWorkshop(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe);
      Bukkit.getScheduler().runTaskLater(Networks.getInstance(), this::registerRecipeCompletable, 1L);
   }

   private void registerRecipeCompletable() {
      if (Networks.getSupportedPluginManager().isJustEnoughGuide()) {
         try {
            RecipeCompletableRegistry.registerRecipeCompletable(this, this.getIngredientSlots(), false);
         } catch (Exception var2) {
            Debug.trace(var2);
         }
      }
   }

   public void preRegister() {
      this.addItemHandler(new ItemHandler[]{new BlockPlaceHandler(false) {
         public void onPlayerPlace(@NotNull BlockPlaceEvent event) {
            Location location = event.getBlock().getLocation();
            DriveOwnership.writeOwner(location, event.getPlayer().getUniqueId());
            AssemblyWorkshop.ensureNetworkNode(location);
         }
      }});
      this.addItemHandler(new ItemHandler[]{new BlockTicker() {
         public boolean isSynchronized() {
            return false;
         }

         public void tick(@NotNull Block b, SlimefunItem item, @NotNull SlimefunBlockData data) {
            AssemblyWorkshop.ensureNetworkNode(b.getLocation());
         }
      }});
      this.addItemHandler(
         new ItemHandler[]{
            new BlockBreakHandler(false, false) {
               public void onPlayerBreak(@NotNull BlockBreakEvent event, @NotNull ItemStack item, @NotNull List<ItemStack> drops) {
                  Player breaker = event.getPlayer();
                  Location location = event.getBlock().getLocation();
                  if (!DriveOwnership.bypasses(breaker)
                     && !DriveOwnership.passesGates(breaker, location, Interaction.BREAK_BLOCK, AssemblyWorkshop.this.canUse(breaker, false))) {
                     event.setCancelled(true);
                     breaker.sendMessage(Lang.getString("messages.cellnet.drive.break_not_allowed"));
                  } else {
                     NetworkStorage.removeNode(location);
                     NetworkUtil.invalidate(location);
                     DriveOwnership.clearOwnerCache(location);
                     BlockMenu blockMenu = StorageCacheUtils.getMenu(location);
                     if (blockMenu != null) {
                        blockMenu.dropItems(location, AssemblyWorkshop.RECIPE_SLOTS);
                        blockMenu.dropItems(location, new int[]{19, 34});
                     }
                  }
               }
            }
         }
      );
   }

   public void postRegister() {
      new BlockMenuPreset(this.getId(), this.getItemName()) {
         public void init() {
            this.setSize(45);
            this.drawBackground(AssemblyWorkshop.BACKGROUND);
            this.addItem(16, AssemblyWorkshop.buildEncodeButton(), (player, i, itemStack, clickAction) -> false);
            this.addItem(26, AssemblyWorkshop.buildClearButton(), (player, i, itemStack, clickAction) -> false);
         }

         public boolean canOpen(@NotNull Block block, @NotNull Player player) {
            return DriveOwnership.bypasses(player)
               || DriveOwnership.passesGates(player, block.getLocation(), Interaction.INTERACT_BLOCK, AssemblyWorkshop.this.canUse(player, false));
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

               for (int slot : AssemblyWorkshop.RECIPE_SLOTS) {
                  if (StackUtils.itemsMatch(itemStack, menu.getItemInSlot(slot))) {
                     slots.add(slot);
                  }
               }

               return slots.stream().mapToInt(Integer::intValue).toArray();
            }
         }

         public void newInstance(@NotNull BlockMenu menu, @NotNull Block block) {
            menu.addMenuClickHandler(16, (player, s, itemStack, clickAction) -> {
               int times = clickAction.isShiftClicked() ? 64 : 1;
               int i = 0;

               while (i < times && AssemblyWorkshop.this.tryEncode(player, menu)) {
                  i++;
               }

               return false;
            });
            menu.addMenuClickHandler(26, (player, s, itemStack, clickAction) -> {
               AssemblyWorkshop.this.tryClear(player, menu);
               return false;
            });
         }
      };
   }

   @NotNull
   private static ItemStack buildEncodeButton() {
      return Icons.ASSEMBLY_WORKSHOP_ENCODE;
   }

   @NotNull
   private static ItemStack buildClearButton() {
      return Icons.ASSEMBLY_WORKSHOP_CLEAR;
   }

   private boolean tryEncode(@NotNull Player player, @NotNull BlockMenu menu) {
      Location location = menu.getLocation();
      NetworkRoot root = NetworkUtil.findRoot(location);
      if (root == null) {
         player.sendMessage(Lang.getString("messages.cellnet.workshop.no_network"));
         return false;
      } else if (root.getRootPower() < 2000L) {
         player.sendMessage(Lang.getString("messages.cellnet.workshop.no_power"));
         return false;
      } else {
         ItemStack cardStack = menu.getItemInSlot(19);
         if (cardStack == null || cardStack.getType().isAir() || AssemblyCard.readCard(cardStack) != null) {
            ItemStack pulled = root.getItemStack(location, new ItemRequest(ExpansionItems.ASSEMBLY_CARD.getItem(), 64));
            if (pulled == null || !AssemblyCard.isCard(pulled) || AssemblyCard.readCard(pulled) != null) {
               if (pulled != null) {
                  root.addItemStack(location, pulled);
               }

               player.sendMessage(Lang.getString("messages.cellnet.workshop.no_card"));
               return false;
            }

            menu.replaceExistingItem(19, pulled);
            cardStack = pulled;
         }

         ItemStack[] grid = new ItemStack[RECIPE_SLOTS.length];
         boolean any = false;

         for (int i = 0; i < RECIPE_SLOTS.length; i++) {
            ItemStack slotItem = menu.getItemInSlot(RECIPE_SLOTS[i]);
            if (slotItem != null && !slotItem.getType().isAir()) {
               ItemStack clean = ItemStackUtil.getCleanItem(slotItem.clone());
               clean.setAmount(1);
               grid[i] = clean;
               any = true;
            }
         }

         if (!any) {
            player.sendMessage(Lang.getString("messages.cellnet.workshop.invalid_recipe"));
            return false;
         } else {
            AssemblyCardRecipes.MatchedRecipe matched = AssemblyCardRecipes.resolveRecipe(player, grid);
            if (matched == null) {
               player.sendMessage(Lang.getString("messages.cellnet.workshop.invalid_recipe"));
               return false;
            } else {
               List<ItemStack> ingredients = new ArrayList<>();

               for (ItemStack input : matched.canonicalInputs()) {
                  if (input != null && !input.getType().isAir()) {
                     ingredients.add(input);
                  }
               }

               List<ItemStack> merged = AssemblyCard.mergeInputs(ingredients);
               if (!AssemblyCardRecipes.passesSaveLimits(player, merged, matched.output())) {
                  return false;
               } else {
                  for (int ix = 0; ix < RECIPE_SLOTS.length; ix++) {
                     ItemStack required = matched.canonicalInputs()[ix];
                     if (required != null && !required.getType().isAir()) {
                        ItemStack slotItem = menu.getItemInSlot(RECIPE_SLOTS[ix]);
                        if (slotItem == null || slotItem.getAmount() < required.getAmount()) {
                           player.sendMessage(Lang.getString("messages.cellnet.workshop.material_short"));
                           return false;
                        }
                     }
                  }

                  ItemStack encodedCard = cardStack.asQuantity(1);
                  if (!AssemblyCard.writeCard(encodedCard, merged, matched.output().clone(), AssemblyCardRecipes.padToSlots(matched.canonicalInputs()))) {
                     player.sendMessage(Lang.getString("messages.cellnet.workshop.invalid_recipe"));
                     return false;
                  } else if (!BlockMenuUtil.fits(menu, encodedCard, 34)) {
                     player.sendMessage(Lang.getString("messages.cellnet.workshop.output_full"));
                     return false;
                  } else {
                     menu.replaceExistingItem(19, cardStack.getAmount() > 1 ? cardStack.asQuantity(cardStack.getAmount() - 1) : null);

                     for (int ixx = 0; ixx < RECIPE_SLOTS.length; ixx++) {
                        ItemStack required = matched.canonicalInputs()[ixx];
                        if (required != null && !required.getType().isAir()) {
                           ItemStack slotItem = menu.getItemInSlot(RECIPE_SLOTS[ixx]);
                           int left = slotItem.getAmount() - required.getAmount();
                           menu.replaceExistingItem(RECIPE_SLOTS[ixx], left > 0 ? slotItem.asQuantity(left) : null);
                        }
                     }

                     BlockMenuUtil.pushItem(menu, encodedCard, 34);
                     root.removeRootPower(2000L);
                     return true;
                  }
               }
            }
         }
      }
   }

   private void tryClear(@NotNull Player player, @NotNull BlockMenu menu) {
      ItemStack cardStack = menu.getItemInSlot(19);
      if (cardStack != null && AssemblyCard.isCard(cardStack)) {
         AssemblyCard.RecipeEntry entry = AssemblyCard.readCard(cardStack);
         if (entry == null) {
            player.sendMessage(Lang.getString("messages.cellnet.workshop.clear_no_recipe"));
         } else {
            int copies = Math.max(1, cardStack.getAmount());

            for (ItemStack ingredient : entry.ingredients()) {
               ItemStack refund = ingredient.clone();
               refund.setAmount((int)Math.min((long)refund.getAmount() * (long)copies, 2147483647L));
               ItemStack leftover = BlockMenuUtil.pushItem(menu, refund, RECIPE_SLOTS);
               if (leftover != null) {
                  ItemStackUtil.giveOrDropItem(player, leftover);
               }
            }

            cardStack.editMeta(meta -> {
               PersistentDataAPI.remove(meta, Keys.ASSEMBLY_CARD_OUTPUT);
               PersistentDataAPI.remove(meta, Keys.ASSEMBLY_CARD_INGREDIENTS);
               PersistentDataAPI.remove(meta, AssemblyCard.ASSEMBLY_CARD_GRID);
               PersistentDataAPI.remove(meta, Keys.ASSEMBLY_CARD_HASH);
               List<String> blankLore = ExpansionItems.ASSEMBLY_CARD.getItem().getItemMeta().getLore();
               if (blankLore != null) {
                  meta.setLore(blankLore);
               }
            });
            player.sendMessage(Lang.getString("messages.cellnet.workshop.clear_ok"));
         }
      } else {
         player.sendMessage(Lang.getString("messages.cellnet.workshop.clear_no_recipe"));
      }
   }

   private static void ensureNetworkNode(@NotNull Location location) {
      if (!NetworkStorage.containsKey(location)) {
         NetworkStorage.registerNode(location, new NodeDefinition(NodeType.BRIDGE));
      }
   }

   public int[] getIngredientSlots() {
      return RECIPE_SLOTS;
   }
}
