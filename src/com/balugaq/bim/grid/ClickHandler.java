package com.balugaq.bim.grid;

import org.jspecify.annotations.NullMarked;

@NullMarked
@FunctionalInterface
public interface ClickHandler {
   ClickHandler cancel = b -> b.event().setCancelled(true);

   void onClick(ClickDTO var1);
}
