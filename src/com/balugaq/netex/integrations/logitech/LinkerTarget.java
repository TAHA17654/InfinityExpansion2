package com.balugaq.netex.integrations.logitech;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum LinkerTarget {
   AUTO,
   QUANTUM_STORAGE,
   CELL,
   DRAWER;

   @NotNull
   public static LinkerTarget of(@Nullable String name) {
      if (name != null && !name.isEmpty()) {
         try {
            return valueOf(name);
         } catch (IllegalArgumentException var2) {
            return AUTO;
         }
      } else {
         return AUTO;
      }
   }

   @NotNull
   public LinkerTarget next() {
      LinkerTarget[] values = values();
      return values[(this.ordinal() + 1) % values.length];
   }
}
