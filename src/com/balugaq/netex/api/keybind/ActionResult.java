package com.balugaq.netex.api.keybind;

public record ActionResult(MultiActionHandle handle, boolean allowClick) {
   public static ActionResult of(MultiActionHandle handle, boolean allowClick) {
      return new ActionResult(handle, allowClick);
   }
}
