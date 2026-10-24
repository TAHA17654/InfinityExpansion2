package com.balugaq.netex.api.data;

import com.ytdd9527.networksexpansion.utils.itemstacks.CompareUtil;
import lombok.Generated;
import org.jetbrains.annotations.NotNull;

public class AdvancedMachineRecipe {
   @NotNull
   private final ItemAmountWrapper[] inputs;
   @NotNull
   private final AdvancedMachineRecipe.AdvancedRandomOutput[] randomOutputs;
   @NotNull
   private final int[] weightBeginValues;
   private int weightSum = 0;

   public AdvancedMachineRecipe(@NotNull ItemAmountWrapper[] inputs, @NotNull AdvancedMachineRecipe.AdvancedRandomOutput[] randomOutputs) {
      this.inputs = inputs;
      this.randomOutputs = randomOutputs;
      this.weightBeginValues = new int[randomOutputs.length];

      for (int i = 0; i < this.randomOutputs.length; i++) {
         this.weightBeginValues[i] = this.weightSum;
         this.weightSum = this.weightSum + this.randomOutputs[i].weight();
      }
   }

   @NotNull
   public ItemAmountWrapper[] getInput() {
      return this.inputs;
   }

   @NotNull
   public ItemAmountWrapper[] getOutput() {
      int r = (int)(Math.random() * (double)this.weightSum);
      return this.randomOutputs[CompareUtil.getIntSmallFuzzyIndex(this.weightBeginValues, r)].outputItem;
   }

   @NotNull
   public AdvancedMachineRecipe.AdvancedRandomOutput[] getOutputs() {
      return this.randomOutputs;
   }

   public boolean isRandomOutput() {
      return this.randomOutputs.length > 1;
   }

   @Generated
   public int getWeightSum() {
      return this.weightSum;
   }

   public static record AdvancedRandomOutput(@NotNull ItemAmountWrapper[] outputItem, int weight) {
      @NotNull
      public ItemAmountWrapper[] getOutputItem() {
         return this.outputItem;
      }
   }
}
