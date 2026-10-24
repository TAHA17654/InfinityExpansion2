package com.balugaq.netex.api.keybind;

import com.balugaq.netex.api.interfaces.functions.Function5;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

@NullMarked
@FunctionalInterface
public interface KeybindPredicate extends Function5<Player, Integer, ItemStack, ClickAction, BlockMenu, Boolean> {
   default Boolean apply(Player player, Integer slot, ItemStack itemStack, ClickAction clickAction, BlockMenu menu) {
      return this.test(player, slot, itemStack, clickAction, menu);
   }

   boolean test(Player var1, int var2, ItemStack var3, ClickAction var4, BlockMenu var5);
}
