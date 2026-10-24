package com.balugaq.netex.api.interfaces;

import java.util.function.BiPredicate;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

@NullMarked
@FunctionalInterface
public interface SuperRecipeHandler extends BiPredicate<Player, BlockMenu> {
   default boolean test(Player player, BlockMenu blockMenu) {
      return this.handle(player, blockMenu);
   }

   boolean handle(Player var1, BlockMenu var2);
}
