package com.balugaq.netex.api.data;

import com.balugaq.netex.api.interfaces.SuperRecipeHandler;
import java.util.Arrays;
import lombok.Generated;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class SuperRecipe {
   private final ItemStack[] input;
   private final ItemStack[] output;
   private final int consumeEnergy;
   @Nullable
   private final SuperRecipeHandler handler;
   private final boolean isShaped;

   public SuperRecipe(boolean isShaped, ItemStack[] input, ItemStack[] output, int consumeEnergy) {
      this.isShaped = isShaped;
      this.input = input;
      this.output = output;
      this.consumeEnergy = consumeEnergy;
      this.handler = null;
   }

   public SuperRecipe(boolean isShaped, ItemStack[] input, ItemStack[] output) {
      this.isShaped = isShaped;
      this.input = input;
      this.output = output;
      this.consumeEnergy = 0;
      this.handler = null;
   }

   public SuperRecipe(boolean isShaped, ItemStack[] input, ItemStack output, int consumeEnergy) {
      this.isShaped = isShaped;
      this.input = input;
      this.output = new ItemStack[]{output};
      this.consumeEnergy = consumeEnergy;
      this.handler = null;
   }

   public SuperRecipe(boolean isShaped, ItemStack[] input, ItemStack output) {
      this.isShaped = isShaped;
      this.input = input;
      this.output = new ItemStack[]{output};
      this.consumeEnergy = 0;
      this.handler = null;
   }

   public SuperRecipe(boolean isShaped, ItemStack[] input, ItemStack[] output, int consumeEnergy, @Nullable SuperRecipeHandler handler) {
      this.isShaped = isShaped;
      this.input = input;
      this.output = output;
      this.consumeEnergy = consumeEnergy;
      this.handler = handler;
   }

   public SuperRecipe(boolean isShaped, ItemStack[] input, ItemStack[] output, @Nullable SuperRecipeHandler handler) {
      this.isShaped = isShaped;
      this.input = input;
      this.output = output;
      this.consumeEnergy = 0;
      this.handler = handler;
   }

   public SuperRecipe(boolean isShaped, ItemStack[] input, ItemStack output, int consumeEnergy, @Nullable SuperRecipeHandler handler) {
      this.isShaped = isShaped;
      this.input = input;
      this.output = new ItemStack[]{output};
      this.consumeEnergy = consumeEnergy;
      this.handler = handler;
   }

   public SuperRecipe(boolean isShaped, ItemStack[] input, ItemStack output, @Nullable SuperRecipeHandler handler) {
      this.isShaped = isShaped;
      this.input = input;
      this.output = new ItemStack[]{output};
      this.consumeEnergy = 0;
      this.handler = handler;
   }

   public SuperRecipe(boolean isShaped, ItemStack input, ItemStack[] output, int consumeEnergy, @Nullable SuperRecipeHandler handler) {
      this.isShaped = isShaped;
      this.input = new ItemStack[]{input};
      this.output = output;
      this.consumeEnergy = consumeEnergy;
      this.handler = handler;
   }

   public SuperRecipe(boolean isShaped, ItemStack input, ItemStack[] output, @Nullable SuperRecipeHandler handler) {
      this.isShaped = isShaped;
      this.input = new ItemStack[]{input};
      this.output = output;
      this.consumeEnergy = 0;
      this.handler = handler;
   }

   public SuperRecipe(boolean isShaped, ItemStack input, ItemStack output, int consumeEnergy, @Nullable SuperRecipeHandler handler) {
      this.isShaped = isShaped;
      this.input = new ItemStack[]{input};
      this.output = new ItemStack[]{output};
      this.consumeEnergy = consumeEnergy;
      this.handler = handler;
   }

   public SuperRecipe(boolean isShaped, ItemStack input, ItemStack output, @Nullable SuperRecipeHandler handler) {
      this.isShaped = isShaped;
      this.input = new ItemStack[]{input};
      this.output = new ItemStack[]{output};
      this.consumeEnergy = 0;
      this.handler = handler;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof SuperRecipe other)) {
         return false;
      } else if (!other.canEqual(this)) {
         return false;
      } else if (this.getConsumeEnergy() != other.getConsumeEnergy()) {
         return false;
      } else if (this.isShaped() != other.isShaped()) {
         return false;
      } else if (!Arrays.deepEquals(this.getInput(), other.getInput())) {
         return false;
      } else if (!Arrays.deepEquals(this.getOutput(), other.getOutput())) {
         return false;
      } else {
         Object this$handler = this.getHandler();
         Object other$handler = other.getHandler();
         return this$handler == null ? other$handler == null : this$handler.equals(other$handler);
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof SuperRecipe;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      result = result * 59 + this.getConsumeEnergy();
      result = result * 59 + (this.isShaped() ? 79 : 97);
      result = result * 59 + Arrays.deepHashCode(this.getInput());
      result = result * 59 + Arrays.deepHashCode(this.getOutput());
      Object $handler = this.getHandler();
      return result * 59 + ($handler == null ? 43 : $handler.hashCode());
   }

   @Generated
   public ItemStack[] getInput() {
      return this.input;
   }

   @Generated
   public ItemStack[] getOutput() {
      return this.output;
   }

   @Generated
   public int getConsumeEnergy() {
      return this.consumeEnergy;
   }

   @Nullable
   @Generated
   public SuperRecipeHandler getHandler() {
      return this.handler;
   }

   @Generated
   public boolean isShaped() {
      return this.isShaped;
   }

   @Generated
   @Override
   public String toString() {
      return "SuperRecipe(input="
         + Arrays.deepToString(this.getInput())
         + ", output="
         + Arrays.deepToString(this.getOutput())
         + ", consumeEnergy="
         + this.getConsumeEnergy()
         + ", handler="
         + this.getHandler()
         + ", isShaped="
         + this.isShaped()
         + ")";
   }
}
