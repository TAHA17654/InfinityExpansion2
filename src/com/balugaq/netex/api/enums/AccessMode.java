package com.balugaq.netex.api.enums;

import lombok.Generated;

public enum AccessMode {
   INPUT(true, false),
   OUTPUT(false, true),
   ALL(true, true);

   final boolean inputAccess;
   final boolean outputAccess;

   private AccessMode(boolean inputAccess, boolean outputAccess) {
      this.inputAccess = inputAccess;
      this.outputAccess = outputAccess;
   }

   @Generated
   public boolean isInputAccess() {
      return this.inputAccess;
   }

   @Generated
   public boolean isOutputAccess() {
      return this.outputAccess;
   }
}
