package com.ytdd9527.networksexpansion.implementation.machines.cellnet.chain;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum ChainModule {
   RANGE,
   CAPACITY,
   MODE,
   VANILLA,
   MULTI_DIRECTION,
   BINDING;

   private static final Map<String, ChainModule> BY_ID = Stream.of(values()).collect(Collectors.toMap(ChainModule::itemId, Function.identity()));

   @Nullable
   public static ChainModule of(@Nullable ItemStack itemStack) {
      if (itemStack != null && !itemStack.getType().isAir()) {
         SlimefunItem slimefunItem = SlimefunItem.getByItem(itemStack);
         return slimefunItem == null ? null : BY_ID.get(slimefunItem.getId());
      } else {
         return null;
      }
   }

   @NotNull
   public String itemId() {
      return "NTW_EXPANSION_CHAIN_MODULE_" + this.name();
   }
}
