package com.ytdd9527.networksexpansion.implementation.machines.manual;

import com.balugaq.netex.api.helpers.Icon;
import com.balugaq.netex.utils.Lang;
import com.ytdd9527.networksexpansion.implementation.ExpansionItems;
import com.ytdd9527.networksexpansion.utils.ReflectionUtil;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.network.NodeType;
import io.github.sefiraat.networks.network.stackcaches.QuantumCache;
import io.github.sefiraat.networks.slimefun.network.NetworkObject;
import io.github.sefiraat.networks.utils.Keys;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.api.researches.Research;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class StorageCardConverter extends NetworkObject {
   private static final int[] BACKGROUND_SLOTS = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 12, 14, 15, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26};
   private static final int STORAGE_SLOT = 11;
   private static final int CONVERT_SLOT = 13;
   private static final int CARD_SLOT = 16;
   private static SlimefunItem card = null;

   @ParametersAreNonnullByDefault
   public StorageCardConverter(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe, NodeType.STORAGE_CARD_CONVERTER);
      this.getSlotsToDrop().add(11);
      this.getSlotsToDrop().add(16);
   }

   public void postRegister() {
      new BlockMenuPreset(this.getId(), this.getItemName()) {
         public void init() {
            this.setSize(27);

            for (int slot : StorageCardConverter.BACKGROUND_SLOTS) {
               this.addItem(slot, Icon.GRAY_BACKGROUND, ChestMenuUtils.getEmptyClickHandler());
            }

            this.addItem(13, Icon.STORAGE_CONVERT_BUTTON);
         }

         public boolean canOpen(@NotNull Block block, @NotNull Player player) {
            return player.hasPermission("slimefun.inventory.bypass")
               || ExpansionItems.STORAGE_CARD_CONVERTER.canUse(player, false)
                  && Slimefun.getProtectionManager().hasPermission(player, block.getLocation(), Interaction.INTERACT_BLOCK);
         }

         public void newInstance(@NotNull BlockMenu menu, @NotNull Block b) {
            menu.addMenuClickHandler(
               13,
               (p, s, i, a) -> {
                  if (!Networks.getSupportedPluginManager().isFinalTECH()) {
                     p.sendMessage(Lang.getString("messages.unsupported-operation.storage_card_converter.final_tech_required"));
                     return false;
                  } else if (StorageCardConverter.card == null) {
                     p.sendMessage(Lang.getString("messages.unsupported-operation.storage_card_converter.final_tech_required"));
                     return false;
                  } else {
                     PlayerProfile profile = (PlayerProfile)PlayerProfile.find(p).orElse(null);
                     if (profile == null) {
                        return false;
                     } else {
                        Research research = StorageCardConverter.card.getResearch();
                        if (research != null && research.isEnabled() && !profile.hasUnlocked(research)) {
                           p.sendMessage(Lang.getString("messages.unsupported-operation.storage_card_converter.research_required"));
                           return false;
                        } else if (StorageCardConverter.card.isDisabledIn(b.getWorld())) {
                           p.sendMessage(Lang.getString("messages.unsupported-operation.storage_card_converter.card_disabled"));
                           return false;
                        } else {
                           ItemStack quantumStorageItem = menu.getItemInSlot(11);
                           ItemStack storageCardItem = menu.getItemInSlot(16);
                           if (storageCardItem != null && !storageCardItem.getType().isAir()) {
                              p.sendMessage(Lang.getString("messages.unsupported-operation.storage_card_converter.output_full"));
                              return false;
                           } else if (quantumStorageItem != null && !quantumStorageItem.getType().isAir()) {
                              if (quantumStorageItem.getAmount() != 1) {
                                 p.sendMessage(Lang.getString("messages.unsupported-operation.storage_card_converter.quantum_storage_not_single"));
                                 return false;
                              } else {
                                 ItemMeta meta = quantumStorageItem.getItemMeta();
                                 QuantumCache cache = Keys.getQuantumCache(meta);
                                 if (cache == null) {
                                    p.sendMessage(Lang.getString("messages.unsupported-operation.storage_card_converter.not_quantum_storage"));
                                    return false;
                                 } else if (cache.getAmountLong() < 4096L) {
                                    p.sendMessage(Lang.getString("messages.unsupported-operation.storage_card_converter.too_few_items"));
                                    return false;
                                 } else if (Boolean.FALSE.equals(ReflectionUtil.invokeMethod(StorageCardConverter.card, "isTargetItem", cache.getItemStack()))) {
                                    p.sendMessage(Lang.getString("messages.unsupported-operation.storage_card_converter.not_target_item"));
                                    return false;
                                 } else {
                                    ItemStack crd = (ItemStack)ReflectionUtil.invokeMethod(
                                       StorageCardConverter.card, "getValidItem", cache.getItemStack(), cache.getAmountLong()
                                    );
                                    if (crd != null) {
                                       menu.replaceExistingItem(16, crd);
                                       menu.replaceExistingItem(11, null);
                                       p.sendMessage(Lang.getString("messages.completed-operation.storage_card_converter.success"));
                                    }

                                    return false;
                                 }
                              }
                           } else {
                              p.sendMessage(Lang.getString("messages.unsupported-operation.storage_card_converter.no_quantum_storage"));
                              return false;
                           }
                        }
                     }
                  }
               }
            );
         }

         public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
            return new int[0];
         }
      };
   }

   static {
      try {
         if (Networks.getSupportedPluginManager().isFinalTECH()) {
            card = ReflectionUtil.getStaticValue(Class.forName("io.taraxacum.finaltech.setup.FinalTechItems"), "STORAGE_CARD", SlimefunItem.class);
         }
      } catch (ClassCastException | ClassNotFoundException var1) {
      }
   }
}
