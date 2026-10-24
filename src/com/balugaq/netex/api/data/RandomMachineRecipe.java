package com.balugaq.netex.api.data;

import com.ytdd9527.networksexpansion.utils.itemstacks.CompareUtil;
import com.ytdd9527.networksexpansion.utils.itemstacks.ItemStackUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Generated;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RandomMachineRecipe extends MachineRecipe {
   @NotNull
   private RandomMachineRecipe.RandomOutput[] randomOutputs;
   private int[] weightBeginValues;
   private int weightSum = 0;

   public RandomMachineRecipe(@NotNull MachineRecipe machineRecipe, @NotNull RandomMachineRecipe.RandomOutput[] randomOutputs) {
      super(machineRecipe.getTicks(), machineRecipe.getInput(), new ItemStack[0]);
      this.randomOutputs = randomOutputs;
      this.weightBeginValues = new int[randomOutputs.length];

      for (int i = 0; i < this.randomOutputs.length; i++) {
         this.weightBeginValues[i] = this.weightSum;
         this.weightSum = this.weightSum + this.randomOutputs[i].getWeight();
      }
   }

   public RandomMachineRecipe(@NotNull ItemStack[] input, @NotNull RandomMachineRecipe.RandomOutput[] randomOutputs) {
      super(0, input, new ItemStack[0]);
      this.randomOutputs = randomOutputs;
      this.weightBeginValues = new int[randomOutputs.length];

      for (int i = 0; i < this.randomOutputs.length; i++) {
         this.weightBeginValues[i] = this.weightSum;
         this.weightSum = this.weightSum + this.randomOutputs[i].getWeight();
      }
   }

   public RandomMachineRecipe(@NotNull ItemStack[] input, @NotNull List<RandomMachineRecipe.RandomOutput> randomOutputs) {
      super(0, input, new ItemStack[0]);
      this.randomOutputs = randomOutputs.toArray(new RandomMachineRecipe.RandomOutput[0]);
      this.weightBeginValues = new int[randomOutputs.size()];

      for (int i = 0; i < this.randomOutputs.length; i++) {
         this.weightBeginValues[i] = this.weightSum;
         this.weightSum = this.weightSum + this.randomOutputs[i].getWeight();
      }
   }

   public RandomMachineRecipe(@NotNull ItemStack input, @NotNull List<RandomMachineRecipe.RandomOutput> randomOutputs) {
      super(0, new ItemStack[]{input}, new ItemStack[0]);
      this.randomOutputs = randomOutputs.toArray(new RandomMachineRecipe.RandomOutput[0]);
      this.weightBeginValues = new int[randomOutputs.size()];

      for (int i = 0; i < this.randomOutputs.length; i++) {
         this.weightBeginValues[i] = this.weightSum;
         this.weightSum = this.weightSum + this.randomOutputs[i].getWeight();
      }
   }

   @NotNull
   public ItemStack[] getOutput() {
      int r = (int)(Math.random() * (double)this.weightSum);
      return this.randomOutputs[CompareUtil.getIntSmallFuzzyIndex(this.weightBeginValues, r)].outputItem;
   }

   @NotNull
   public ItemStack[] getAllOutput() {
      List<ItemStack> itemList = new ArrayList<>();

      for (RandomMachineRecipe.RandomOutput randomOutput : this.randomOutputs) {
         itemList.addAll(Arrays.asList(randomOutput.outputItem));
      }

      return ItemStackUtil.getItemArray(itemList);
   }

   @NotNull
   public RandomMachineRecipe.RandomOutput[] getRandomOutputs() {
      return this.randomOutputs;
   }

   @NotNull
   public RandomMachineRecipe addRandomOutput(@NotNull RandomMachineRecipe.RandomOutput... randomOutputs) {
      RandomMachineRecipe.RandomOutput[] newRandomOutput = new RandomMachineRecipe.RandomOutput[this.randomOutputs.length + randomOutputs.length];
      int[] newWeightBeginValues = new int[this.weightBeginValues.length + randomOutputs.length];
      System.arraycopy(this.randomOutputs, 0, newRandomOutput, 0, this.randomOutputs.length);
      System.arraycopy(this.weightBeginValues, 0, newWeightBeginValues, 0, this.weightBeginValues.length);
      int newWeightSum = this.weightSum;

      for (int i = 0; i < randomOutputs.length; i++) {
         RandomMachineRecipe.RandomOutput randomOutput = randomOutputs[i];
         newRandomOutput[i + this.randomOutputs.length] = randomOutputs[i];
         newWeightBeginValues[i + this.weightBeginValues.length] = newWeightSum;
         newWeightSum += randomOutput.weight;
      }

      this.randomOutputs = newRandomOutput;
      this.weightBeginValues = newWeightBeginValues;
      this.weightSum = newWeightSum;
      return this;
   }

   public static class RandomOutput {
      @NotNull
      private final ItemStack[] outputItem;
      private final int weight;

      public RandomOutput(@NotNull List<ItemStack> outputItem, int weight) {
         this.outputItem = outputItem.toArray(new ItemStack[0]);
         this.weight = weight;
      }

      public RandomOutput(@NotNull ItemStack[] outputItem, int weight) {
         this.outputItem = outputItem;
         this.weight = weight;
      }

      public RandomOutput(@NotNull ItemStack outputItem, int weight) {
         this.outputItem = new ItemStack[]{outputItem};
         this.weight = weight;
      }

      public RandomOutput(@NotNull Material outputItem, int weight) {
         this.outputItem = new ItemStack[]{new ItemStack(outputItem)};
         this.weight = weight;
      }

      @NotNull
      public ItemStack[] getOutputItem() {
         return this.outputItem;
      }

      @Generated
      public int getWeight() {
         return this.weight;
      }
   }
}
