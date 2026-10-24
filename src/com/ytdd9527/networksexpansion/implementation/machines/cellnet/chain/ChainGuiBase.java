package com.ytdd9527.networksexpansion.implementation.machines.cellnet.chain;

import com.balugaq.netex.api.enums.TransportMode;
import com.balugaq.netex.utils.Debug;
import com.balugaq.netex.utils.Lang;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.GhostItems;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.ui.Icons;
import io.github.sefiraat.networks.slimefun.network.NetworkDirectional;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public abstract class ChainGuiBase extends AbstractChainMachine {
   public static final int UP_SLOT = 11;
   public static final int NORTH_SLOT = 12;
   public static final int DOWN_SLOT = 13;
   public static final int WEST_SLOT = 20;
   public static final int EAST_SLOT = 22;
   public static final int SOUTH_SLOT = 30;
   public static final int BINDING_ENTRY_SLOT = 4;

   protected ChainGuiBase(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe);
   }

   @Override
   protected void wireMenu(@NotNull BlockMenu menu, @NotNull AbstractChainMachine.LineState state) {
      loadWorkState(menu.getLocation(), state);
      this.recountModules(menu, state);
      this.wireTemplateSlots(menu, state);
      this.wireModuleSlots(menu, state);
      this.wireModeSlot(menu, state);
      this.wireDistanceSlot(menu, state);
      this.wireDirectionSlots(menu, state);
      this.wireBindingEntry(menu, state);
      menu.addMenuOpeningHandler(p -> {
         this.recountModules(menu, state);
         this.refreshAll(menu, state);
         ChainRangeParticles.show(menu.getLocation(), state.effectiveDirections(), this.effectiveDistance(state));
      });
      menu.addMenuCloseHandler(p -> ChainRangeParticles.fadeLater(menu.getLocation()));
   }

   private void wireBindingEntry(@NotNull BlockMenu menu, @NotNull AbstractChainMachine.LineState state) {
      menu.addMenuClickHandler(4, (p, s, i, a) -> {
         if (this.getWorkMode() == AbstractChainMachine.WorkMode.GRAB) {
            p.sendMessage(Lang.getString("messages.cellnet.line.binding-grabber-unsupported"));
            return false;
         } else if (!state.bindingModule) {
            p.sendMessage(Lang.getString("messages.cellnet.line.binding-module-required"));
            return false;
         } else {
            ChainBindingMenu.openDirections(p, menu.getLocation());
            return false;
         }
      });
   }

   private void refreshBindingEntry(@NotNull BlockMenu menu, @NotNull AbstractChainMachine.LineState state) {
      boolean usable = this.getWorkMode() != AbstractChainMachine.WorkMode.GRAB && state.bindingModule;
      ItemStack icon = (usable ? Icons.CHAIN_BINDING_ENTRY : Icons.CHAIN_BINDING_ENTRY_LOCKED).clone();
      icon.editMeta(meta -> {
         meta.setDisplayName(Lang.getString(usable ? "messages.cellnet.line.binding-entry-name" : "messages.cellnet.line.binding-locked-name"));
         meta.setLore(List.of(Lang.getString(usable ? "messages.cellnet.line.binding-entry-lore" : "messages.cellnet.line.binding-locked-lore")));
      });
      GhostItems.mark(icon);
      menu.replaceExistingItem(4, icon);
   }

   private void wireTemplateSlots(@NotNull BlockMenu menu, @NotNull AbstractChainMachine.LineState state) {
      for (int i = 0; i < TEMPLATE_SLOTS.length; i++) {
         int index = i;
         int slot = TEMPLATE_SLOTS[i];
         menu.addMenuClickHandler(slot, (p, s, item, action) -> {
            if (this.getWorkMode() == AbstractChainMachine.WorkMode.GRAB) {
               return false;
            } else {
               ItemStack current = menu.getItemInSlot(slot);
               boolean placeholder = isTemplatePlaceholder(current);
               if (index < this.templateSlotCount(state)) {
                  ItemStack cursor = p.getItemOnCursor();
                  if (cursor == null || cursor.getType().isAir()) {
                     return !placeholder;
                  } else if (placeholder) {
                     menu.replaceExistingItem(slot, cursor.clone());
                     p.setItemOnCursor(null);
                     return false;
                  } else {
                     return true;
                  }
               } else {
                  ItemStack cursor0 = p.getItemOnCursor();
                  if ((cursor0 == null || cursor0.getType().isAir()) && current != null && !current.getType().isAir() && !placeholder) {
                     p.setItemOnCursor(current);
                     menu.replaceExistingItem(slot, this.lockedTemplatePlaceholder());
                  }

                  return false;
               }
            }
         });
      }
   }

   private void wireModuleSlots(@NotNull BlockMenu menu, @NotNull AbstractChainMachine.LineState state) {
      for (int i = 0; i < 8; i++) {
         int moduleSlot = 36 + i;
         menu.addMenuClickHandler(moduleSlot, (p, slot, item, action) -> {
            this.handleModuleClick(menu, state, p, moduleSlot);
            return false;
         });
      }
   }

   private void handleModuleClick(@NotNull BlockMenu menu, @NotNull AbstractChainMachine.LineState state, @NotNull Player player, int slot) {
      ItemStack current = menu.getItemInSlot(slot);
      boolean currentIsModule = current != null && ChainModule.of(current) != null;
      ItemStack cursor = player.getItemOnCursor();
      boolean cursorAir = cursor == null || cursor.getType().isAir();
      ChainModule cursorModule = ChainModule.of(cursor);
      boolean changed = false;
      if (cursorAir) {
         if (currentIsModule) {
            player.setItemOnCursor(current);
            menu.replaceExistingItem(slot, this.modulePlaceholder());
            changed = true;
         }
      } else if (cursorModule != null) {
         if (this.getWorkMode() == AbstractChainMachine.WorkMode.GRAB && cursorModule == ChainModule.CAPACITY) {
            player.sendMessage(Lang.getString("messages.cellnet.line.module-capacity-unsupported"));
            return;
         }

         if (this.getWorkMode() == AbstractChainMachine.WorkMode.GRAB && cursorModule == ChainModule.BINDING) {
            player.sendMessage(Lang.getString("messages.cellnet.line.module-binding-unsupported"));
            return;
         }

         if (currentIsModule) {
            player.getWorld().dropItemNaturally(player.getLocation(), current);
         }

         menu.replaceExistingItem(slot, cursor.asOne());
         cursor.setAmount(cursor.getAmount() - 1);
         changed = true;
      } else {
         player.sendMessage(Lang.getString("messages.cellnet.line.module-not-module"));
      }

      if (changed) {
         this.recountModules(menu, state);
         this.refreshAll(menu, state);
         saveWorkState(menu.getLocation(), state);
      }
   }

   private void wireModeSlot(@NotNull BlockMenu menu, @NotNull AbstractChainMachine.LineState state) {
      menu.addMenuClickHandler(29, (p, slot, item, action) -> {
         if (!state.modeModule) {
            p.sendMessage(Lang.getString("messages.cellnet.line.mode-locked"));
            return false;
         } else {
            state.transportMode = action.isRightClicked() ? state.transportMode.previous() : state.transportMode.next();
            StorageCacheUtils.setData(menu.getLocation(), "chain_mode", state.transportMode.name());
            this.refreshModeIcon(menu, state);
            return false;
         }
      });
   }

   private void wireDistanceSlot(@NotNull BlockMenu menu, @NotNull AbstractChainMachine.LineState state) {
      menu.addMenuClickHandler(21, (p, slot, item, action) -> {
         ItemStack cursor = p.getItemOnCursor();
         if (cursor != null && !cursor.getType().isAir()) {
            int cap = this.distanceCap(state);
            int old = state.distance;
            state.distance = Math.max(1, Math.min(cursor.getAmount(), cap));
            if (old != state.distance) {
               StorageCacheUtils.setData(menu.getLocation(), "chain_distance", String.valueOf(state.distance));
               p.sendMessage(Lang.getString("messages.cellnet.line.distance-set", state.distance, cap));
            } else {
               p.sendMessage(Lang.getString("messages.cellnet.line.distance-locked", state.distance, cap));
            }

            this.refreshDistanceIcon(menu, state);
            return false;
         } else {
            return false;
         }
      });
   }

   private void wireDirectionSlots(@NotNull BlockMenu menu, @NotNull AbstractChainMachine.LineState state) {
      for (BlockFace face : NetworkDirectional.VALID_FACES) {
         int slot = directionSlot(face);
         if (slot >= 0) {
            menu.addMenuClickHandler(slot, (p, s, item, action) -> {
               if (action.isShiftClicked() && !action.isRightClicked()) {
                  ChainBindingMenu.openTarget(p, menu.getBlock().getRelative(face).getLocation());
                  return false;
               } else {
                  if (state.multiDirection) {
                     this.toggleDirection(state, face);
                  } else {
                     state.directions.clear();
                     state.directions.add(face);
                     StorageCacheUtils.setData(menu.getLocation(), "direction", face.name());
                  }

                  saveWorkState(menu.getLocation(), state);
                  this.refreshDirectionIcons(menu, state);
                  return false;
               }
            });
         }
      }
   }

   private void toggleDirection(@NotNull AbstractChainMachine.LineState state, @NotNull BlockFace face) {
      if (!state.directions.remove(face)) {
         state.directions.add(face);
      }

      if (state.directions.isEmpty()) {
         state.directions.add(face);
      }
   }

   private void refreshAll(@NotNull BlockMenu menu, @NotNull AbstractChainMachine.LineState state) {
      this.refreshDistanceIcon(menu, state);
      this.refreshModeIcon(menu, state);
      this.refreshDirectionIcons(menu, state);
      this.refreshModulePlaceholders(menu);
      this.refreshTemplatePlaceholders(menu, state);
      this.refreshBindingEntry(menu, state);
   }

   private void refreshDistanceIcon(@NotNull BlockMenu menu, @NotNull AbstractChainMachine.LineState state) {
      ItemStack icon = Icons.LINE_DISTANCE.clone();
      icon.setAmount(Math.max(1, Math.min(state.distance, 64)));
      icon.editMeta(meta -> {
         meta.setDisplayName(Lang.getString("messages.cellnet.line.distance-name", state.distance, this.distanceCap(state)));
         meta.setLore(Lang.getStringList("messages.cellnet.line.distance-lore"));
      });
      GhostItems.mark(icon);
      menu.replaceExistingItem(21, icon);
   }

   private void refreshModeIcon(@NotNull BlockMenu menu, @NotNull AbstractChainMachine.LineState state) {
      TransportMode current = state.modeModule ? state.transportMode : TransportMode.NONE;
      ItemStack icon = (state.modeModule ? Icons.LINE_MODE_ON : Icons.LINE_MODE_OFF).clone();
      icon.editMeta(meta -> {
         meta.setDisplayName(Lang.getString("messages.cellnet.line.mode-name"));
         List<String> lore = new ArrayList<>();

         for (TransportMode mode : TransportMode.values()) {
            String name = Lang.getString("messages.cellnet.line.modes." + mode.name());
            if (mode == current) {
               lore.add(Lang.getString("messages.cellnet.line.mode-row-current", name));
            } else {
               lore.add(Lang.getString("messages.cellnet.line.mode-row-other", name));
            }
         }

         lore.add(Lang.getString(state.modeModule ? "messages.cellnet.line.mode-hint" : "messages.cellnet.line.mode-locked"));
         meta.setLore(lore);
      });
      GhostItems.mark(icon);
      menu.replaceExistingItem(29, icon);
   }

   private void refreshDirectionIcons(@NotNull BlockMenu menu, @NotNull AbstractChainMachine.LineState state) {
      Set<BlockFace> effective = state.effectiveDirections();

      for (BlockFace face : NetworkDirectional.VALID_FACES) {
         int slot = directionSlot(face);
         if (slot >= 0) {
            try {
               boolean selected = effective.contains(face);
               menu.replaceExistingItem(slot, ChainBindingMenu.directionPane(menu.getLocation(), face, selected));
            } catch (Exception var8) {
               Debug.trace(var8, "Failed to refresh direction panel: " + face);
               menu.replaceExistingItem(slot, this.fallbackDirectionPane(face));
            }
         }
      }
   }

   @NotNull
   private ItemStack fallbackDirectionPane(@NotNull BlockFace face) {
      ItemStack icon = Icons.LINE_DIRECTION_FALLBACK.clone();
      icon.editMeta(meta -> meta.setDisplayName(Lang.getString("messages.cellnet.line.direction-" + face.name().toLowerCase(Locale.ROOT))));
      GhostItems.mark(icon);
      return icon;
   }

   private void refreshModulePlaceholders(@NotNull BlockMenu menu) {
      for (int slot = 36; slot < 44; slot++) {
         ItemStack current = menu.getItemInSlot(slot);
         if (current == null || current.getType().isAir()) {
            menu.replaceExistingItem(slot, this.modulePlaceholder());
         }
      }
   }

   @NotNull
   private ItemStack modulePlaceholder() {
      ItemStack icon = Icons.LINE_MODULE_SLOT.clone();
      icon.editMeta(meta -> {
         meta.setDisplayName(Lang.getString("messages.cellnet.line.module-slot-name"));
         meta.setLore(List.of(Lang.getString("messages.cellnet.line.module-slot-lore")));
      });
      GhostItems.mark(icon);
      return icon;
   }

   @NotNull
   private ItemStack lockedTemplatePlaceholder() {
      ItemStack icon = Icons.LINE_TEMPLATE_LOCKED.clone();
      icon.editMeta(meta -> {
         meta.setDisplayName(Lang.getString("messages.cellnet.line.template-locked-name"));
         meta.setLore(List.of(Lang.getString("messages.cellnet.line.template-locked-lore")));
         meta.getPersistentDataContainer().set(PLACEHOLDER_KEY, PersistentDataType.BYTE, (byte)1);
      });
      GhostItems.mark(icon);
      return icon;
   }

   private void refreshTemplatePlaceholders(@NotNull BlockMenu menu, @NotNull AbstractChainMachine.LineState state) {
      if (this.getWorkMode() != AbstractChainMachine.WorkMode.GRAB) {
         int unlocked = this.templateSlotCount(state);

         for (int i = 0; i < TEMPLATE_SLOTS.length; i++) {
            int slot = TEMPLATE_SLOTS[i];
            ItemStack current = menu.getItemInSlot(slot);
            boolean placeholder = isTemplatePlaceholder(current);
            if (i < unlocked) {
               if (placeholder) {
                  menu.replaceExistingItem(slot, null);
               }
            } else if (current == null || current.getType().isAir()) {
               menu.replaceExistingItem(slot, this.lockedTemplatePlaceholder());
            }
         }
      }
   }

   @Override
   protected void clearDisplayIcons(@NotNull BlockMenu menu) {
      menu.replaceExistingItem(29, null);
      menu.replaceExistingItem(21, null);
      menu.replaceExistingItem(4, null);

      for (BlockFace face : NetworkDirectional.VALID_FACES) {
         int slot = directionSlot(face);
         if (slot >= 0) {
            menu.replaceExistingItem(slot, null);
         }
      }

      for (int slot = 36; slot < 44; slot++) {
         ItemStack current = menu.getItemInSlot(slot);
         if (current != null && ChainModule.of(current) == null) {
            menu.replaceExistingItem(slot, null);
         }
      }

      for (int slotx : TEMPLATE_SLOTS) {
         if (isTemplatePlaceholder(menu.getItemInSlot(slotx))) {
            menu.replaceExistingItem(slotx, null);
         }
      }
   }

   private static int directionSlot(@NotNull BlockFace face) {
      return switch (face) {
         case NORTH -> 12;
         case UP -> 11;
         case WEST -> 20;
         case EAST -> 22;
         case SOUTH -> 30;
         case DOWN -> 13;
         default -> -1;
      };
   }

   public void postRegister() {
      new BlockMenuPreset(this.getId(), this.getItemName()) {
         public void init() {
            this.setSize(54);
            boolean grab = ChainGuiBase.this.getWorkMode() == AbstractChainMachine.WorkMode.GRAB;

            for (int slot = 0; slot < 54; slot++) {
               if (slot != 4 && (grab || !ChainBindingMenu.in(AbstractChainMachine.TEMPLATE_SLOTS, slot)) && (slot < 36 || slot >= 44)) {
                  this.addItem(slot, ChestMenuUtils.getBackground(), (p, s, i, a) -> false);
               }
            }
         }

         public boolean canOpen(@NotNull Block block, @NotNull Player player) {
            return player.hasPermission("slimefun.inventory.bypass")
               || ChainGuiBase.this.canUse(player, false)
                  && Slimefun.getProtectionManager().hasPermission(player, block.getLocation(), Interaction.INTERACT_BLOCK);
         }

         public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
            return new int[0];
         }

         public void newInstance(@NotNull BlockMenu menu, @NotNull Block block) {
            ChainGuiBase.this.setupMenu(menu);
         }
      };
   }

   private void setupMenu(@NotNull BlockMenu menu) {
      AbstractChainMachine.LineState state = state(menu.getLocation());

      for (int slot = 0; slot < 54; slot++) {
         menu.addMenuClickHandler(slot, (p, s, i, a) -> false);
      }

      this.wireMenu(menu, state);
      state.wired = true;
   }
}
