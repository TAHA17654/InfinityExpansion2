package com.balugaq.netex.api.gui;

import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import org.jetbrains.annotations.NotNull;

public class GUI {
   @NotNull
   public final List<String> gui;

   public GUI(String... gui) {
      this.gui = List.of(gui);
   }

   public int select1(@NotNull String c) {
      return this.select1(c.charAt(0));
   }

   public int select1(char c) {
      int[] r = this.select(c);
      return r.length > 0 ? r[0] : -1;
   }

   public int[] select(@NotNull String c) {
      return this.select(c.charAt(0));
   }

   public int[] select(char c) {
      List<Integer> list = new ArrayList<>();

      for (int i = 0; i < this.gui.size(); i++) {
         String row = this.gui.get(i);

         for (int j = 0; j < row.length(); j++) {
            if (row.charAt(j) == c) {
               list.add(i * 9 + j);
            }
         }
      }

      return list.stream().mapToInt(ix -> ix).toArray();
   }

   @NotNull
   @Generated
   public List<String> getGui() {
      return this.gui;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof GUI other)) {
         return false;
      } else if (!other.canEqual(this)) {
         return false;
      } else {
         Object this$gui = this.getGui();
         Object other$gui = other.getGui();
         return this$gui == null ? other$gui == null : this$gui.equals(other$gui);
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof GUI;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $gui = this.getGui();
      return result * 59 + ($gui == null ? 43 : $gui.hashCode());
   }

   @Generated
   @Override
   public String toString() {
      return "GUI(gui=" + this.getGui() + ")";
   }
}
