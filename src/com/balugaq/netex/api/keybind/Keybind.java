package com.balugaq.netex.api.keybind;

import com.balugaq.netex.api.enums.AmountHandleStrategy;
import com.balugaq.netex.api.interfaces.BaseGrid;
import com.balugaq.netex.api.visual.Screen;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.network.GridItemRequest;
import io.github.sefiraat.networks.network.NodeDefinition;
import io.github.sefiraat.networks.slimefun.network.grid.GridCache;
import io.github.sefiraat.networks.utils.Keys;
import lombok.Generated;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface Keybind extends Keyed, Comparable<Keybind> {
   Keybind leftClick = of(Keys.newKey("left-click"), (player, slot, item, action, menu) -> !action.isShiftClicked() && !action.isRightClicked());
   Keybind rightClick = of(Keys.newKey("right-click"), (player, slot, item, action, menu) -> !action.isShiftClicked() && action.isRightClicked());
   Keybind shiftClick = of(Keys.newKey("shift-click"), (player, slot, item, action, menu) -> action.isShiftClicked());
   Keybind shiftLeftClick = of(Keys.newKey("shift-left-click"), (player, slot, item, action, menu) -> action.isShiftClicked() && !action.isRightClicked());
   Keybind shiftRightClick = of(Keys.newKey("shift-right-click"), (player, slot, item, action, menu) -> action.isShiftClicked() && action.isRightClicked());

   static Keybind of(NamespacedKey key, KeybindPredicate predicate) {
      return Keybind.KeybindImpl.of(key, predicate).register();
   }

   @Nullable
   static Keybind get(NamespacedKey key) {
      return Keybinds.getKeybind(key);
   }

   static Action gridActionGenerate(BaseGrid grid, AmountHandleStrategy strategy, boolean toInventory) {
      return Action.of(
         Keys.newKey(grid.getClass().getSimpleName().toLowerCase() + "-" + strategy.name().toLowerCase() + "-" + (toInventory ? "inv" : "cursor")),
         (player, s, i, a, menu) -> {
            NodeDefinition definition = NetworkStorage.getNode(menu.getLocation());
            if (definition != null && definition.getNode() != null) {
               ItemStack clone = grid.precheck(definition, menu, player, i);
               if (clone == null) {
                  return ActionResult.of(MultiActionHandle.CONTINUE, false);
               } else {
                  strategy.handle(menu.getLocation(), player, clone, amount -> {
                     GridItemRequest request = new GridItemRequest(clone, amount, player);
                     if (toInventory) {
                        grid.addToInventory(player, definition, request, menu);
                     } else {
                        grid.addToCursor(player, definition, request, a.isRightClicked(), menu);
                     }

                     GridCache gridCache = grid.getCacheMap().get(menu.getLocation());
                     if (gridCache != null) {
                        if (gridCache.getDisplayMode() == GridCache.DisplayMode.DISPLAY) {
                           gridCache.addPullItemHistory(clone);
                        }

                        grid.updateDisplay(Screen.of(menu));
                     }
                  });
                  return ActionResult.of(MultiActionHandle.BREAK, false);
               }
            } else {
               return ActionResult.of(MultiActionHandle.CONTINUE, false);
            }
         }
      );
   }

   default void set(Location location, Keybinds keybind, Action action) {
      StorageCacheUtils.setData(location, keybind.keybindKey(this.getKey()), action.getKey().toString());
   }

   default Keybind register() {
      Keybinds.register(this);
      return this;
   }

   default int compareTo(@NotNull Keybind o) {
      return this.getKey().compareTo(o.getKey());
   }

   boolean test(Player var1, int var2, ItemStack var3, ClickAction var4, BlockMenu var5);

   public static class KeybindImpl implements Keybind {
      private final NamespacedKey key;
      private final KeybindPredicate predicate;

      public KeybindImpl(NamespacedKey key, KeybindPredicate predicate) {
         this.key = key;
         this.predicate = predicate;
      }

      public static Keybind.KeybindImpl of(NamespacedKey key, KeybindPredicate predicate) {
         return new Keybind.KeybindImpl(key, predicate);
      }

      @Override
      public boolean test(Player player, int slot, ItemStack item, ClickAction action, BlockMenu menu) {
         return this.predicate.test(player, slot, item, action, menu);
      }

      @NotNull
      public NamespacedKey getKey() {
         return this.key;
      }

      @Generated
      @Override
      public boolean equals(Object o) {
         if (o == this) {
            return true;
         } else if (!(o instanceof Keybind.KeybindImpl other)) {
            return false;
         } else if (!other.canEqual(this)) {
            return false;
         } else {
            Object this$key = this.getKey();
            Object other$key = other.getKey();
            return this$key == null ? other$key == null : this$key.equals(other$key);
         }
      }

      @Generated
      protected boolean canEqual(Object other) {
         return other instanceof Keybind.KeybindImpl;
      }

      @Generated
      @Override
      public int hashCode() {
         int PRIME = 59;
         int result = 1;
         Object $key = this.getKey();
         return result * 59 + ($key == null ? 43 : $key.hashCode());
      }

      @Generated
      public KeybindPredicate getPredicate() {
         return this.predicate;
      }

      @Generated
      @Override
      public String toString() {
         return "Keybind.KeybindImpl(key=" + this.getKey() + ", predicate=" + this.getPredicate() + ")";
      }
   }
}
