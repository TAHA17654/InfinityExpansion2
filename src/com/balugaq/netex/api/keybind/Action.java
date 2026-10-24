package com.balugaq.netex.api.keybind;

import com.balugaq.netex.api.interfaces.functions.Function5;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface Action extends Keyed, Comparable<Action> {
   static Action of(final NamespacedKey key, final Function5<Player, Integer, ItemStack, ClickAction, BlockMenu, ActionResult> function) {
      return (new Action() {
         @Override
         public ActionResult apply(Player player, int slot, ItemStack item, ClickAction action, BlockMenu menu) {
            return function.apply(player, slot, item, action, menu);
         }

         @NotNull
         public NamespacedKey getKey() {
            return key;
         }
      }).register();
   }

   @Nullable
   static Action get(NamespacedKey key) {
      return Keybinds.getAction(key);
   }

   default int compareTo(Action o) {
      return this.getKey().compareTo(o.getKey());
   }

   default Action register() {
      Keybinds.register(this);
      return this;
   }

   ActionResult apply(Player var1, int var2, ItemStack var3, ClickAction var4, BlockMenu var5);
}
