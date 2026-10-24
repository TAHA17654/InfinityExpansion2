package com.ytdd9527.networksexpansion.core.items.machines;

import com.balugaq.netex.api.enums.CraftType;
import com.balugaq.netex.api.enums.FeedbackType;
import com.balugaq.netex.api.helpers.Icon;
import com.balugaq.netex.api.interfaces.CraftTyped;
import com.balugaq.netex.api.interfaces.SoftCellBannable;
import com.balugaq.netex.utils.BlockMenuUtil;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.ytdd9527.networksexpansion.core.items.unusable.Blueprint;
import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.NodeDefinition;
import io.github.sefiraat.networks.network.NodeType;
import io.github.sefiraat.networks.network.stackcaches.BlueprintInstance;
import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import io.github.sefiraat.networks.slimefun.network.NetworkObject;
import io.github.sefiraat.networks.utils.Keys;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemHandler;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AutoCrafter extends NetworkObject implements SoftCellBannable, CraftTyped {
   public static final int BLUEPRINT_SLOT = 10;
   public static final int OUTPUT_SLOT = 16;
   public static final Map<Location, BlueprintInstance> INSTANCE_MAP = new HashMap<>();
   private static final int[] BACKGROUND_SLOTS = new int[]{3, 4, 5, 12, 13, 14, 21, 22, 23};
   private static final int[] BLUEPRINT_BACKGROUND = new int[]{0, 1, 2, 9, 11, 18, 19, 20};
   private static final int[] OUTPUT_BACKGROUND = new int[]{6, 7, 8, 15, 17, 24, 25, 26};
   protected final int chargePerCraft;
   protected final boolean withholding;
   private CraftType craftType = CraftType.CRAFTING;

   public AutoCrafter(
      @NotNull ItemGroup itemGroup,
      @NotNull SlimefunItemStack item,
      @NotNull RecipeType recipeType,
      @NotNull ItemStack[] recipe,
      int chargePerCraft,
      boolean withholding
   ) {
      super(itemGroup, item, recipeType, recipe, NodeType.CRAFTER);
      this.chargePerCraft = chargePerCraft;
      this.withholding = withholding;
      this.getSlotsToDrop().add(10);
      this.getSlotsToDrop().add(16);
      this.addItemHandler(new ItemHandler[]{new BlockTicker() {
         public boolean isSynchronized() {
            return false;
         }

         public void tick(@NotNull Block block, SlimefunItem slimefunItem, @NotNull SlimefunBlockData data) {
            BlockMenu blockMenu = data.getBlockMenu();
            if (blockMenu != null) {
               AutoCrafter.this.addToRegistry(block);
               AutoCrafter.this.craftPreFlight(blockMenu);
            }
         }
      }});
   }

   public static void updateCache(@NotNull BlockMenu blockMenu) {
      INSTANCE_MAP.remove(blockMenu.getLocation());
   }

   protected void craftPreFlight(@NotNull BlockMenu blockMenu) {
      this.releaseCache(blockMenu);
      NodeDefinition definition = NetworkStorage.getNode(blockMenu.getLocation());
      if (definition != null && definition.getNode() != null) {
         NetworkRoot root = definition.getNode().getRoot();
         if (!this.checkSoftCellBan(blockMenu.getLocation(), root)) {
            if (!this.withholding) {
               ItemStack stored = blockMenu.getItemInSlot(16);
               if (stored != null && stored.getType() != Material.AIR) {
                  root.addItemStack(blockMenu.getLocation(), stored);
               }
            }

            ItemStack blueprint = blockMenu.getItemInSlot(10);
            if (blueprint != null && blueprint.getType() != Material.AIR) {
               long networkCharge = root.getRootPower();
               if (networkCharge < (long)this.chargePerCraft) {
                  this.sendFeedback(blockMenu.getLocation(), FeedbackType.NOT_ENOUGH_POWER);
               } else {
                  SlimefunItem item = SlimefunItem.getByItem(blueprint);
                  if (!this.isValidBlueprint(item)) {
                     this.sendFeedback(blockMenu.getLocation(), FeedbackType.INVALID_BLUEPRINT);
                  } else {
                     BlueprintInstance instance = INSTANCE_MAP.get(blockMenu.getLocation());
                     if (instance == null) {
                        ItemMeta blueprintMeta = blueprint.getItemMeta();
                        BlueprintInstance instance2 = Keys.getBlueprintInstance(blueprintMeta);
                        if (instance2 == null) {
                           this.sendFeedback(blockMenu.getLocation(), FeedbackType.NO_BLUEPRINT_INSTANCE_FOUND);
                           return;
                        }

                        if (instance2 == BlueprintInstance.INVALID) {
                           this.sendFeedback(blockMenu.getLocation(), FeedbackType.BROKEN_BLUEPRINT);
                           return;
                        }

                        this.setCache(blockMenu, instance2);
                        instance = instance2;
                     }

                     ItemStack output = blockMenu.getItemInSlot(16);
                     int blueprintAmount = this.canBlueprintStack() ? blueprint.getAmount() : 1;
                     ItemStack targetOutput = instance.getItemStack();
                     if (output == null
                        || output.getType() == Material.AIR
                        || targetOutput == null
                        || output.getAmount() + targetOutput.getAmount() * blueprintAmount <= output.getMaxStackSize()
                           && StackUtils.itemsMatch(targetOutput, output)) {
                        if (this.tryCraft(blockMenu, instance, root, blueprintAmount, output)) {
                           root.removeRootPower((long)this.chargePerCraft);
                        }
                     } else {
                        this.sendFeedback(blockMenu.getLocation(), FeedbackType.OUTPUT_FULL);
                     }
                  }
               }
            } else {
               this.sendFeedback(blockMenu.getLocation(), FeedbackType.NO_BLUEPRINT_FOUND);
            }
         }
      } else {
         this.sendFeedback(blockMenu.getLocation(), FeedbackType.NO_NETWORK_FOUND);
      }
   }

   private boolean tryCraft(
      @NotNull BlockMenu blockMenu, @NotNull BlueprintInstance instance, @NotNull NetworkRoot root, int blueprintAmount, @Nullable ItemStack existing
   ) {
      HashMap<ItemStack, Integer> requiredItems = new HashMap<>();

      for (int i = 0; i < instance.getRecipeItems().length; i++) {
         ItemStack requested = instance.getRecipeItems()[i];
         if (requested != null) {
            requiredItems.merge(requested, requested.getAmount() * blueprintAmount, Integer::sum);
         }
      }

      for (Entry<ItemStack, Integer> entry : requiredItems.entrySet()) {
         if (!root.contains(new ItemRequest(entry.getKey(), entry.getValue()))) {
            this.sendFeedback(blockMenu.getLocation(), FeedbackType.NOT_ENOUGH_ITEMS_IN_NETWORK);
            return false;
         }
      }

      ItemStack[] fetcheds = new ItemStack[instance.getRecipeItems().length];

      for (int ix = 0; ix < instance.getRecipeItems().length; ix++) {
         ItemStack requested = instance.getRecipeItems()[ix];
         if (requested != null) {
            ItemStack fetched = root.getItemStack(blockMenu.getLocation(), new ItemRequest(requested, requested.getAmount() * blueprintAmount));
            fetcheds[ix] = fetched;
            if (fetched == null || fetched.getAmount() < requested.getAmount() * blueprintAmount) {
               this.returnItems(root, fetcheds, blockMenu);
               return false;
            }
         }
      }

      Location location = blockMenu.getLocation().clone().add(0.5, 1.1, 0.5);
      if (root.isDisplayParticles()) {
         location.getWorld().spawnParticle(Particle.WAX_OFF, location, 0, 0.0, 4.0, 0.0);
      }

      ItemStack crafted = instance.getItemStack().clone();
      crafted.setAmount(crafted.getAmount() * blueprintAmount);
      if (crafted.getAmount() > crafted.getMaxStackSize()) {
         this.returnItems(root, fetcheds, blockMenu);
         this.sendFeedback(blockMenu.getLocation(), FeedbackType.RESULT_IS_TOO_LARGE);
         return false;
      } else {
         if (!this.withholding && existing != null && existing.getType() != Material.AIR) {
            root.addItemStack(blockMenu.getLocation(), crafted);
         }

         if (crafted != null && crafted.getType() != Material.AIR) {
            BlockMenuUtil.pushItem(blockMenu, crafted, 16);
         }

         this.sendFeedback(blockMenu.getLocation(), FeedbackType.WORKING);
         return true;
      }
   }

   protected void returnItems(@NotNull NetworkRoot root, @Nullable @NotNull ItemStack[] inputs, @NotNull BlockMenu blockMenu) {
      for (ItemStack input : inputs) {
         if (input != null) {
            root.addItemStack(blockMenu.getLocation(), input);
         }
      }
   }

   public void releaseCache(@NotNull BlockMenu blockMenu) {
      INSTANCE_MAP.remove(blockMenu.getLocation());
   }

   public void setCache(@NotNull BlockMenu blockMenu, @NotNull BlueprintInstance blueprintInstance) {
      if (!blockMenu.hasViewer()) {
         INSTANCE_MAP.putIfAbsent(blockMenu.getLocation().clone(), blueprintInstance);
      }
   }

   public void postRegister() {
      new BlockMenuPreset(this.getId(), this.getItemName()) {
         public void init() {
            this.drawBackground(AutoCrafter.BACKGROUND_SLOTS);
            this.drawBackground(Icon.BLUEPRINT_BACKGROUND_STACK, AutoCrafter.BLUEPRINT_BACKGROUND);
            this.drawBackground(Icon.OUTPUT_BACKGROUND_STACK, AutoCrafter.OUTPUT_BACKGROUND);
         }

         public void newInstance(@NotNull BlockMenu menu, @NotNull Block b) {
            menu.addMenuOpeningHandler(p -> AutoCrafter.this.releaseCache(menu));
            menu.addMenuCloseHandler(p -> AutoCrafter.this.releaseCache(menu));
            menu.addMenuClickHandler(10, (player, slot, clickedItem, clickAction) -> {
               AutoCrafter.this.releaseCache(menu);
               return true;
            });
         }

         public boolean canOpen(@NotNull Block block, @NotNull Player player) {
            return player.hasPermission("slimefun.inventory.bypass")
               || this.getSlimefunItem().canUse(player, false)
                  && Slimefun.getProtectionManager().hasPermission(player, block.getLocation(), Interaction.INTERACT_BLOCK);
         }

         public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
            return AutoCrafter.this.withholding && flow == ItemTransportFlow.WITHDRAW ? new int[]{16} : new int[0];
         }
      };
   }

   public boolean isValidBlueprint(SlimefunItem var1) {
      return var1 instanceof Blueprint && ((Blueprint)var1).craftType() == this.craftType;
   }

   public boolean canBlueprintStack() {
      return false;
   }

   public AutoCrafter(ItemGroup var1, SlimefunItemStack var2, RecipeType var3, ItemStack[] var4, int var5, boolean var6, CraftType var7) {
      this(var1, var2, var3, var4, var5, var6);
      this.craftType = var7;
   }

   @Override
   public CraftType craftType() {
      return this.craftType;
   }
}
