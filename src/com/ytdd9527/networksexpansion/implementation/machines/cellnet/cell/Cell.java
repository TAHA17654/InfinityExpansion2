package com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell;

import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.ItemHashMap;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.ItemKey;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Cell {
   @NotNull
   UUID getUuid();

   long getAmount(@NotNull ItemKey var1);

   boolean canReceiveItem(@NotNull ItemKey var1);

   long receiveCapacity(@NotNull ItemKey var1);

   boolean contains(@NotNull ItemKey var1);

   int pushItem(@NotNull ItemKey var1, int var2);

   @Nullable
   ItemStack takeItem(@NotNull ItemKey var1, long var2);

   long takeItemAmount(@NotNull ItemKey var1, long var2);

   @NotNull
   Map<ItemStack, Long> getAllItems();

   void accumulateInto(@NotNull ItemHashMap<Long> var1, @Nullable Map<ItemKey, List<UUID>> var2);

   long getStoredCount();
}
