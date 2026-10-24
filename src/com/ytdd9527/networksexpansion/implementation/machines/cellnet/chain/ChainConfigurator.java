package com.ytdd9527.networksexpansion.implementation.machines.cellnet.chain;

import com.balugaq.netex.api.enums.TransportMode;
import com.balugaq.netex.utils.Lang;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.core.items.SpecialSlimefunItem;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.GhostItems;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.ui.BrowseUI;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.ui.Icons;
import io.github.sefiraat.networks.Networks;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemHandler;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.data.persistent.PersistentDataAPI;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChainConfigurator extends SpecialSlimefunItem implements Listener {
   private static final NamespacedKey CONFIG_KEY = new NamespacedKey(Networks.getInstance(), "chain_cfg");
   private static final int MAX_CONFIG_LENGTH = 100000;
   private static final int INFO_SLOT = 0;
   private static final int CLOSE_SLOT = 8;

   public ChainConfigurator(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe);
   }

   public void preRegister() {
      this.addItemHandler(new ItemHandler[]{(ItemUseHandler)e -> {
         e.cancel();
         Player player = e.getPlayer();
         ItemStack held = player.getInventory().getItemInMainHand();
         Optional<Block> optional = e.getClickedBlock();
         if (optional.isEmpty()) {
            if (player.isSneaking() && getStored(held) != null) {
               setStored(held, null, null, null);
               refreshLore(held);
            }
         } else {
            Block block = optional.get();
            Location location = block.getLocation();
            AbstractChainMachine machine = AbstractChainMachine.machineAt(location);
            if (machine != null) {
               if (Slimefun.getProtectionManager().hasPermission(player, block, Interaction.INTERACT_BLOCK)) {
                  ChainConfigurator.StoredConfig stored = getStored(held);
                  if (stored != null) {
                     apply(player, machine, location, stored);
                  }
               }
            }
         }
      }});
      Networks.getPluginManager().registerEvents(this, Networks.getInstance());
   }

   @EventHandler
   public void onLeftClickBlock(@NotNull PlayerInteractEvent e) {
      if (e.getAction() == Action.LEFT_CLICK_BLOCK && e.getHand() == EquipmentSlot.HAND) {
         Player player = e.getPlayer();
         ItemStack held = player.getInventory().getItemInMainHand();
         SlimefunItem sfItem = SlimefunItem.getByItem(held);
         if (sfItem == this) {
            Block block = e.getClickedBlock();
            if (block != null) {
               Location location = block.getLocation();
               AbstractChainMachine machine = AbstractChainMachine.machineAt(location);
               if (machine != null) {
                  if (Slimefun.getProtectionManager().hasPermission(player, block, Interaction.INTERACT_BLOCK)) {
                     e.setCancelled(true);
                     openSelect(player, location, held);
                  }
               }
            }
         }
      }
   }

   private static void openSelect(@NotNull Player player, @NotNull Location location, @NotNull ItemStack held) {
      AbstractChainMachine.LineState state = AbstractChainMachine.loadedState(location);
      int totalBindings = 0;

      for (BlockFace face : ChainBindingMenu.SLOT_FACES) {
         totalBindings += ChainBindingStore.load(location, face).size();
      }

      if (totalBindings != 0) {
         ChestMenu menu = new ChestMenu(Lang.getString("messages.cellnet.line.configurator-title-select"));
         menu.setPlayerInventoryClickable(false);
         menu.setEmptySlotsClickable(false);

         for (int slot = 0; slot < 27; slot++) {
            if (slot != 0 && slot != 8 && !ChainBindingMenu.in(ChainBindingMenu.DIRECTION_SLOTS, slot)) {
               menu.addItem(slot, ChestMenuUtils.getBackground(), (p, s, i, a) -> false);
            }
         }

         ItemStack info = Icons.CHAIN_BINDING_INFO.clone();
         info.editMeta(meta -> {
            meta.setDisplayName(Lang.getString("messages.cellnet.line.binding-info-name"));
            meta.setLore(List.of(Lang.getString("messages.cellnet.line.configurator-info-lore")));
         });
         GhostItems.mark(info);
         menu.addItem(0, info, (p, s, i, a) -> false);
         menu.addItem(8, BrowseUI.backButton(), (p, s, i, a) -> false);

         for (int i = 0; i < ChainBindingMenu.SLOT_FACES.length; i++) {
            BlockFace face = ChainBindingMenu.SLOT_FACES[i];
            boolean enabled = state.effectiveDirections().contains(face);
            int boundCount = ChainBindingStore.load(location, face).size();
            TransportMode override = ChainBindingStore.loadDirMode(location, face);
            ItemStack icon = ChainBindingMenu.directionIcon(location, face, enabled);
            icon.editMeta(
               meta -> {
                  meta.setDisplayName(Lang.getString("messages.cellnet.line.binding-direction-name", ChainBindingMenu.directionName(face)));
                  List<String> lore = new ArrayList<>();
                  lore.add(Lang.getString("messages.cellnet.line.configurator-dir-count", boundCount));
                  lore.add(
                     Lang.getString(
                        "messages.cellnet.line.configurator-dir-mode",
                        override == null
                           ? Lang.getString("messages.cellnet.line.binding-mode-follow")
                           : Lang.getString("messages.cellnet.line.modes." + override.name())
                     )
                  );
                  lore.add("");
                  lore.add(Lang.getString("messages.cellnet.line.configurator-dir-hint"));
                  meta.setLore(lore);
               }
            );
            GhostItems.mark(icon);
            menu.addItem(ChainBindingMenu.DIRECTION_SLOTS[i], icon, (p, s, it, a) -> {
               if (enabled) {
                  copyDirection(p, location, face, held);
               }

               return false;
            });
         }

         menu.open(new Player[]{player});
      }
   }

   private static void copyDirection(@NotNull Player player, @NotNull Location location, @NotNull BlockFace face, @NotNull ItemStack held) {
      Map<Integer, List<ItemStack>> bindings = ChainBindingStore.load(location, face);
      TransportMode mode = ChainBindingStore.loadDirMode(location, face);
      if (!bindings.isEmpty() || mode != null) {
         String encoded = ChainBindingStore.encodeBindings(bindings);
         if (encoded != null && encodeStored(face, mode, encoded).length() <= 100000) {
            setStored(held, face, mode, encoded);
            refreshLore(held);
            player.closeInventory();
         }
      }
   }

   private static void apply(
      @NotNull Player player, @NotNull AbstractChainMachine machine, @NotNull Location location, @NotNull ChainConfigurator.StoredConfig stored
   ) {
      if (machine.getWorkMode() != AbstractChainMachine.WorkMode.GRAB) {
         AbstractChainMachine.LineState state = AbstractChainMachine.loadedState(location);
         BlockMenu menu = StorageCacheUtils.getMenu(location);
         if (menu != null) {
            machine.recountModules(menu, state);
         }

         if (state.effectiveDirections().contains(stored.face())) {
            List<Location> targets = ChainTargetCache.targets(
               location, stored.face(), machine.effectiveDistance(state), state.vanilla, machine.workTtlTicks(), machine.idleTtlTicks()
            );
            Map<Integer, List<ItemStack>> newTable = new LinkedHashMap<>();
            int applied = 0;
            int missed = 0;

            for (Entry<Integer, List<ItemStack>> entry : stored.bindings().entrySet()) {
               if (entry.getKey() <= targets.size()) {
                  newTable.put(entry.getKey(), entry.getValue());
                  applied++;
               } else {
                  missed++;
               }
            }

            if (applied <= 0 || ChainBindingStore.save(location, stored.face(), newTable)) {
               if (!player.isSneaking() && stored.mode() != null) {
                  ChainBindingStore.saveDirMode(location, stored.face(), stored.mode());
                  Map<BlockFace, TransportMode> overrides = state.dirModes;
                  if (overrides != null) {
                     overrides.put(stored.face(), stored.mode());
                  }
               }

               AbstractChainMachine.invalidateBindings(state, stored.face());
            }
         }
      }
   }

   private static void setStored(@NotNull ItemStack held, @Nullable BlockFace face, @Nullable TransportMode mode, @Nullable String encoded) {
      if (!held.getType().isAir()) {
         ItemMeta meta = held.getItemMeta();
         if (meta != null) {
            if (face == null) {
               PersistentDataAPI.remove(meta, CONFIG_KEY);
            } else {
               PersistentDataAPI.setString(meta, CONFIG_KEY, encodeStored(face, mode, encoded == null ? "" : encoded));
            }

            held.setItemMeta(meta);
         }
      }
   }

   @NotNull
   private static String encodeStored(@NotNull BlockFace face, @Nullable TransportMode mode, @NotNull String encoded) {
      return face.name() + ";" + (mode == null ? "" : mode.name()) + ";" + encoded;
   }

   @Nullable
   private static ChainConfigurator.StoredConfig getStored(@NotNull ItemStack held) {
      if (!held.hasItemMeta()) {
         return null;
      } else {
         ItemMeta meta = held.getItemMeta();
         String raw = meta == null ? null : PersistentDataAPI.getString(meta, CONFIG_KEY);
         if (raw != null && !raw.isEmpty()) {
            int first = raw.indexOf(59);
            int second = raw.indexOf(59, first + 1);
            if (first > 0 && second >= 0) {
               BlockFace face;
               try {
                  face = BlockFace.valueOf(raw.substring(0, first));
               } catch (IllegalArgumentException var10) {
                  return null;
               }

               String modePart = raw.substring(first + 1, second);
               TransportMode mode = null;
               if (!modePart.isEmpty()) {
                  try {
                     mode = TransportMode.valueOf(modePart);
                  } catch (IllegalArgumentException var9) {
                  }
               }

               Map<Integer, List<ItemStack>> bindings = ChainBindingStore.decodeBindings(raw.substring(second + 1));
               return new ChainConfigurator.StoredConfig(face, bindings, mode);
            } else {
               return null;
            }
         } else {
            return null;
         }
      }
   }

   private static void refreshLore(@NotNull ItemStack held) {
      if (!held.getType().isAir()) {
         ItemMeta meta = held.getItemMeta();
         if (meta != null) {
            ChainConfigurator.StoredConfig stored = getStored(held);
            List<String> lore = new ArrayList<>();
            if (stored == null) {
               lore.add(Lang.getString("messages.cellnet.line.configurator-lore-hint-copy"));
               lore.add(Lang.getString("messages.cellnet.line.configurator-lore-hint-apply"));
               lore.add(Lang.getString("messages.cellnet.line.configurator-lore-hint-clear"));
            } else {
               lore.add(Lang.getString("messages.cellnet.line.configurator-lore-face", ChainBindingMenu.directionName(stored.face())));
               lore.add(Lang.getString("messages.cellnet.line.configurator-lore-count", stored.bindings().size()));
               if (stored.mode() == null) {
                  lore.add(Lang.getString("messages.cellnet.line.configurator-lore-follow"));
               } else {
                  lore.add(
                     Lang.getString("messages.cellnet.line.configurator-lore-mode", Lang.getString("messages.cellnet.line.modes." + stored.mode().name()))
                  );
               }

               lore.add("");
               lore.add(Lang.getString("messages.cellnet.line.configurator-lore-hint-apply"));
               lore.add(Lang.getString("messages.cellnet.line.configurator-lore-hint-clear"));
            }

            meta.setLore(lore);
            held.setItemMeta(meta);
         }
      }
   }

   private static record StoredConfig(@NotNull BlockFace face, @NotNull Map<Integer, List<ItemStack>> bindings, @Nullable TransportMode mode) {
   }
}
