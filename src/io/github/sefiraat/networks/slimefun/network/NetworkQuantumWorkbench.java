package io.github.sefiraat.networks.slimefun.network;

import com.balugaq.netex.api.enums.FeedbackType;
import com.balugaq.netex.api.helpers.Icon;
import com.balugaq.netex.utils.BlockMenuUtil;
import com.balugaq.netex.utils.Lang;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.core.items.SpecialSlimefunItem;
import io.github.sefiraat.networks.events.NetworkCraftEvent;
import io.github.sefiraat.networks.network.stackcaches.QuantumCache;
import io.github.sefiraat.networks.utils.Keys;
import io.github.sefiraat.networks.utils.datatypes.DataTypeMethods;
import io.github.sefiraat.networks.utils.datatypes.PersistentQuantumStorageType;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemHandler;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.ParametersAreNonnullByDefault;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class NetworkQuantumWorkbench extends SpecialSlimefunItem {
   private static final int[] BACKGROUND_SLOTS = new int[]{
      0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 13, 14, 15, 16, 17, 18, 22, 24, 26, 27, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44
   };
   private static final int[] RECIPE_SLOTS = new int[]{10, 11, 12, 19, 20, 21, 28, 29, 30};
   private static final int CRAFT_SLOT = 23;
   private static final int OUTPUT_SLOT = 25;
   private static final Map<ItemStack[], ItemStack> RECIPES = new HashMap<>();
   public static final RecipeType TYPE = new RecipeType(
      Keys.newKey("quantum-workbench"), Icon.RECIPE_TYPE_ITEMSTACK_QUANTUM_WORKBENCH, NetworkQuantumWorkbench::addRecipe, new String[0]
   );

   @ParametersAreNonnullByDefault
   public NetworkQuantumWorkbench(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe);
   }

   public static void addRecipe(ItemStack[] input, ItemStack output) {
      RECIPES.put(input, output);
   }

   public void preRegister() {
      this.addItemHandler(new ItemHandler[]{this.getBlockBreakHandler()});
   }

   public void postRegister() {
      new BlockMenuPreset(this.getId(), this.getItemName()) {
         public void init() {
            this.drawBackground(NetworkQuantumWorkbench.BACKGROUND_SLOTS);
            this.addItem(23, Icon.QUANTUM_WORKBENCH_CRAFT_BUTTON_STACK, (p, slot, item, action) -> false);
         }

         public boolean canOpen(@NotNull Block block, @NotNull Player player) {
            return player.hasPermission("slimefun.inventory.bypass")
               || this.getSlimefunItem().canUse(player, false)
                  && Slimefun.getProtectionManager().hasPermission(player, block.getLocation(), Interaction.INTERACT_BLOCK);
         }

         public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
            return flow == ItemTransportFlow.WITHDRAW ? new int[]{25} : new int[0];
         }

         public void newInstance(@NotNull BlockMenu menu, @NotNull Block b) {
            menu.addMenuClickHandler(23, (player, slot, item, action) -> {
               NetworkQuantumWorkbench.this.craft(menu, player);
               return false;
            });
         }
      };
   }

   public void craft(@NotNull BlockMenu menu, @NotNull Player player) {
      ItemStack[] inputs = new ItemStack[RECIPE_SLOTS.length];
      int i = 0;

      for (int recipeSlot : RECIPE_SLOTS) {
         ItemStack stack = menu.getItemInSlot(recipeSlot);
         inputs[i] = stack;
         i++;
      }

      ItemStack crafted = null;

      for (Entry<ItemStack[], ItemStack> entry : RECIPES.entrySet()) {
         if (this.testRecipe(inputs, entry.getKey())) {
            crafted = entry.getValue().clone();
            break;
         }
      }

      if (crafted != null) {
         ItemStack coreItem = inputs[4];
         SlimefunItem oldQuantum = SlimefunItem.getByItem(coreItem);
         if (oldQuantum instanceof NetworkQuantumStorage) {
            ItemMeta oldMeta = coreItem.getItemMeta();
            ItemMeta newMeta = crafted.getItemMeta();
            NetworkQuantumStorage newQuantum = (NetworkQuantumStorage)SlimefunItem.getByItem(crafted);
            if (newQuantum == null) {
               return;
            }

            QuantumCache oldCache = DataTypeMethods.getCustom(oldMeta, Keys.QUANTUM_STORAGE_INSTANCE, PersistentQuantumStorageType.TYPE);
            if (oldCache == null) {
               oldCache = DataTypeMethods.getCustom(oldMeta, Keys.QUANTUM_STORAGE_INSTANCE2, PersistentQuantumStorageType.TYPE);
            }

            if (oldCache == null) {
               oldCache = DataTypeMethods.getCustom(oldMeta, Keys.QUANTUM_STORAGE_INSTANCE3, PersistentQuantumStorageType.TYPE);
            }

            if (oldCache != null) {
               ItemStack itemStack = oldCache.getItemStack();
               QuantumCache newCache = new QuantumCache(
                  itemStack != null ? itemStack.clone() : null,
                  oldCache.getAmountLong(),
                  newQuantum.getMaxAmount(),
                  oldCache.isVoidExcess(),
                  newQuantum.supportsCustomMaxAmount()
               );
               DataTypeMethods.setCustom(newMeta, Keys.QUANTUM_STORAGE_INSTANCE, PersistentQuantumStorageType.TYPE, newCache);
               newCache.addMetaLore(newMeta);
               crafted.setItemMeta(newMeta);
            }
         }

         if (BlockMenuUtil.fits(menu, crafted, 25)) {
            for (int recipeSlot : RECIPE_SLOTS) {
               if (menu.getItemInSlot(recipeSlot) != null) {
                  BlockMenuUtil.consumeItem(menu, recipeSlot, 1, true);
               }
            }

            NetworkCraftEvent event = new NetworkCraftEvent(player, this, inputs, crafted);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
               return;
            }

            crafted = event.getOutput();
            BlockMenuUtil.pushItem(menu, crafted, 25);
            this.sendFeedback(menu.getLocation(), FeedbackType.SUCCESS);
         } else {
            player.sendMessage(Lang.getString("messages.unsupported-operation.quantum_workbench.output_slot_full"));
            this.sendFeedback(menu.getLocation(), FeedbackType.OUTPUT_FULL);
         }
      }
   }

   private boolean testRecipe(ItemStack[] input, @NotNull ItemStack[] recipe) {
      for (int test = 0; test < recipe.length; test++) {
         if (!SlimefunUtils.isItemSimilar(input[test], recipe[test], true, false, false)) {
            return false;
         }
      }

      return true;
   }

   @NotNull
   private BlockBreakHandler getBlockBreakHandler() {
      return new BlockBreakHandler(false, false) {
         public void onPlayerBreak(@NotNull BlockBreakEvent event, @NotNull ItemStack itemStack, @NotNull List<ItemStack> drops) {
            BlockMenu menu = StorageCacheUtils.getMenu(event.getBlock().getLocation());
            if (menu != null) {
               menu.dropItems(menu.getLocation(), NetworkQuantumWorkbench.RECIPE_SLOTS);
               menu.dropItems(menu.getLocation(), new int[]{25});
            }
         }
      };
   }
}
