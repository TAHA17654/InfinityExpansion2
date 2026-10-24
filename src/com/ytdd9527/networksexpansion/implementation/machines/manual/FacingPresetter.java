package com.ytdd9527.networksexpansion.implementation.machines.manual;

import com.balugaq.netex.api.enums.FacingPreset;
import com.balugaq.netex.api.helpers.Icon;
import com.balugaq.netex.utils.Lang;
import com.balugaq.netex.utils.NetworksVersionedEnchantment;
import io.github.sefiraat.networks.network.NodeType;
import io.github.sefiraat.networks.slimefun.network.NetworkDirectional;
import io.github.sefiraat.networks.utils.Keys;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import java.util.ArrayList;
import java.util.List;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class FacingPresetter extends NetworkDirectional {
   private static final int[] ITEM_SLOTS = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26};
   private static final int[] BACKGROUND_SLOTS = new int[]{27, 29, 30, 32, 34, 35, 37, 39, 40, 41, 43, 44, 45, 47, 48, 50, 51, 52, 53};
   private static final int HELP_ICON = 33;
   private static final int AUTO_SLOT = 42;
   private static final int NORTH_SLOT = 28;
   private static final int SOUTH_SLOT = 46;
   private static final int EAST_SLOT = 38;
   private static final int WEST_SLOT = 36;
   private static final int UP_SLOT = 31;
   private static final int DOWN_SLOT = 49;

   public FacingPresetter(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe, NodeType.FACING_PRESETTER);

      for (int slot : ITEM_SLOTS) {
         this.getSlotsToDrop().add(slot);
      }
   }

   @Override
   protected int[] getBackgroundSlots() {
      return BACKGROUND_SLOTS;
   }

   @Override
   public int getNorthSlot() {
      return 28;
   }

   @Override
   public int getSouthSlot() {
      return 46;
   }

   @Override
   public int getEastSlot() {
      return 38;
   }

   @Override
   public int getWestSlot() {
      return 36;
   }

   @Override
   public int getUpSlot() {
      return 31;
   }

   @Override
   public int getDownSlot() {
      return 49;
   }

   @Override
   public void postRegister() {
      new BlockMenuPreset(this.getId(), this.getItemName()) {
         public void init() {
            this.drawBackground(FacingPresetter.this.getBackgroundSlots());
            this.addItem(33, Icon.FACING_PRESETTER_HELP_ICON, (player, i, itemStack, clickAction) -> false);
            this.addItem(
               FacingPresetter.this.getNorthSlot(), FacingPresetter.getDirectionalSlotPane(FacingPreset.NORTH), (player, i, itemStack, clickAction) -> false
            );
            this.addItem(
               FacingPresetter.this.getSouthSlot(), FacingPresetter.getDirectionalSlotPane(FacingPreset.SOUTH), (player, i, itemStack, clickAction) -> false
            );
            this.addItem(
               FacingPresetter.this.getEastSlot(), FacingPresetter.getDirectionalSlotPane(FacingPreset.EAST), (player, i, itemStack, clickAction) -> false
            );
            this.addItem(
               FacingPresetter.this.getWestSlot(), FacingPresetter.getDirectionalSlotPane(FacingPreset.WEST), (player, i, itemStack, clickAction) -> false
            );
            this.addItem(
               FacingPresetter.this.getUpSlot(), FacingPresetter.getDirectionalSlotPane(FacingPreset.UP), (player, i, itemStack, clickAction) -> false
            );
            this.addItem(
               FacingPresetter.this.getDownSlot(), FacingPresetter.getDirectionalSlotPane(FacingPreset.DOWN), (player, i, itemStack, clickAction) -> false
            );
            this.addItem(42, FacingPresetter.getDirectionalSlotPane(FacingPreset.AUTO), (player, i, itemStack, clickAction) -> false);
         }

         public void newInstance(@NotNull BlockMenu blockMenu, @NotNull Block b) {
            blockMenu.addMenuClickHandler(
               FacingPresetter.this.getNorthSlot(),
               (player, i, itemStack, clickAction) -> FacingPresetter.setFacingPreset(player, blockMenu, FacingPreset.NORTH)
            );
            blockMenu.addMenuClickHandler(
               FacingPresetter.this.getSouthSlot(),
               (player, i, itemStack, clickAction) -> FacingPresetter.setFacingPreset(player, blockMenu, FacingPreset.SOUTH)
            );
            blockMenu.addMenuClickHandler(
               FacingPresetter.this.getEastSlot(), (player, i, itemStack, clickAction) -> FacingPresetter.setFacingPreset(player, blockMenu, FacingPreset.EAST)
            );
            blockMenu.addMenuClickHandler(
               FacingPresetter.this.getWestSlot(), (player, i, itemStack, clickAction) -> FacingPresetter.setFacingPreset(player, blockMenu, FacingPreset.WEST)
            );
            blockMenu.addMenuClickHandler(
               FacingPresetter.this.getUpSlot(), (player, i, itemStack, clickAction) -> FacingPresetter.setFacingPreset(player, blockMenu, FacingPreset.UP)
            );
            blockMenu.addMenuClickHandler(
               FacingPresetter.this.getDownSlot(), (player, i, itemStack, clickAction) -> FacingPresetter.setFacingPreset(player, blockMenu, FacingPreset.DOWN)
            );
            blockMenu.addMenuClickHandler(42, (player, i, itemStack, clickAction) -> FacingPresetter.setFacingPreset(player, blockMenu, FacingPreset.AUTO));
         }

         public boolean canOpen(@NotNull Block block, @NotNull Player player) {
            return player.hasPermission("slimefun.inventory.bypass")
               || this.getSlimefunItem().canUse(player, false)
                  && Slimefun.getProtectionManager().hasPermission(player, block.getLocation(), Interaction.INTERACT_BLOCK);
         }

         public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
            return new int[0];
         }
      };
   }

   public static boolean setFacingPreset(Player player, BlockMenu blockMenu, FacingPreset facingPreset) {
      if (!(SlimefunItem.getById(blockMenu.getPreset().getID()) instanceof FacingPresetter)) {
         return false;
      } else {
         boolean success = false;

         for (int slot : ITEM_SLOTS) {
            ItemStack item = blockMenu.getItemInSlot(slot);
            if (SlimefunItem.getByItem(item) instanceof NetworkDirectional) {
               ItemMeta meta = item.getItemMeta();
               PersistentDataContainer pdc = meta.getPersistentDataContainer();
               List<String> lore = meta.getLore();
               if (lore == null) {
                  lore = new ArrayList<>();
               }

               if (!pdc.has(Keys.FACING_PRESET)) {
                  pdc.set(Keys.FACING_PRESET, PersistentDataType.STRING, facingPreset.name());
                  lore.add(String.format(Lang.getString("messages.completed-operation.directional.presetted_facing"), facingPreset.name()));
                  meta.setLore(lore);
                  item.setItemMeta(meta);
                  blockMenu.replaceExistingItem(slot, item);
                  success = true;
               } else if (!((String)pdc.getOrDefault(Keys.FACING_PRESET, PersistentDataType.STRING, "")).equals(facingPreset.name())) {
                  if (!lore.isEmpty()) {
                     lore.remove(lore.size() - 1);
                  }

                  lore.add(String.format(Lang.getString("messages.completed-operation.directional.presetted_facing"), facingPreset.name()));
                  meta.setLore(lore);
                  pdc.set(Keys.FACING_PRESET, PersistentDataType.STRING, facingPreset.name());
                  item.setItemMeta(meta);
                  blockMenu.replaceExistingItem(slot, item);
                  success = true;
               }
            }
         }

         if (success) {
            player.sendMessage(String.format(Lang.getString("messages.completed-operation.directional.presetted"), facingPreset.name()));
         } else {
            player.sendMessage(Lang.getString("messages.completed-operation.directional.presetted_failed"));
         }

         return false;
      }
   }

   @NotNull
   public static ItemStack getDirectionalSlotPane(FacingPreset preset) {
      ItemStack displayStack = new CustomItemStack(
         Material.GREEN_STAINED_GLASS_PANE,
         String.format(Lang.getString("messages.normal-operation.directional.preset_display_name"), preset.name()),
         new String[0]
      );
      ItemMeta itemMeta = displayStack.getItemMeta();
      itemMeta.addEnchant(NetworksVersionedEnchantment.LUCK_OF_THE_SEA, 1, true);
      itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
      displayStack.setItemMeta(itemMeta);
      return displayStack;
   }

   @Override
   public void updateGui(@Nullable BlockMenu blockMenu) {
   }
}
