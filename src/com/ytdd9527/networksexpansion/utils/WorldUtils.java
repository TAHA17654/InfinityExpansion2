package com.ytdd9527.networksexpansion.utils;

import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.Pair;
import io.github.thebusybiscuit.slimefun4.libraries.paperlib.PaperLib;
import java.lang.reflect.Field;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Deprecated
public class WorldUtils {
   @Nullable
   protected static Class<?> craftBlockStateClass;
   @Nullable
   protected static Field interfaceBlockDataField;
   @Nullable
   protected static Field blockPositionField;
   @Nullable
   protected static Field worldField;
   @Nullable
   protected static Field weakWorldField;
   protected static boolean success = false;

   public static boolean copyBlockState(@NotNull BlockState fromBlockState, @NotNull Block toBlock) {
      if (!success) {
         return false;
      } else {
         BlockState toState = PaperLib.getBlockState(toBlock, false).getState();
         if (craftBlockStateClass == null || craftBlockStateClass.isInstance(toState) && craftBlockStateClass.isInstance(fromBlockState)) {
            try {
               if (blockPositionField != null) {
                  blockPositionField.set(fromBlockState, blockPositionField.get(toState));
               }

               if (worldField != null) {
                  worldField.set(fromBlockState, worldField.get(toState));
               }

               if (weakWorldField != null) {
                  weakWorldField.set(fromBlockState, weakWorldField.get(toState));
               }

               fromBlockState.update(true, false);
               return true;
            } catch (Throwable var4) {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   static {
      try {
         World sampleWorld = (World)Bukkit.getWorlds().get(0);
         BlockState blockstate = sampleWorld.getBlockAt(0, 0, 0).getState();
         boolean fail = false;
         Pair<Field, Class<?>> result = ReflectionUtil.getDeclaredFieldsRecursively(blockstate.getClass(), "data");
         if (result != null) {
            interfaceBlockDataField = (Field)result.getFirstValue();
            if (interfaceBlockDataField != null) {
               interfaceBlockDataField.setAccessible(true);
            } else {
               fail = true;
            }

            craftBlockStateClass = (Class<?>)result.getSecondValue();
            if (craftBlockStateClass != null) {
               Pair<Field, Class<?>> r2 = ReflectionUtil.getDeclaredFieldsRecursively(craftBlockStateClass, "position");
               if (r2 != null) {
                  blockPositionField = (Field)r2.getFirstValue();
                  if (blockPositionField != null) {
                     blockPositionField.setAccessible(true);
                  } else {
                     fail = true;
                  }
               } else {
                  fail = true;
               }

               Pair<Field, Class<?>> r3 = ReflectionUtil.getDeclaredFieldsRecursively(craftBlockStateClass, "world");
               if (r3 != null) {
                  worldField = (Field)r3.getFirstValue();
                  if (worldField != null) {
                     worldField.setAccessible(true);
                  } else {
                     fail = true;
                  }
               } else {
                  fail = true;
               }

               Pair<Field, Class<?>> r4 = ReflectionUtil.getDeclaredFieldsRecursively(craftBlockStateClass, "weakWorld");
               if (r4 != null) {
                  weakWorldField = (Field)r4.getFirstValue();
                  if (weakWorldField != null) {
                     weakWorldField.setAccessible(true);
                  } else {
                     fail = true;
                  }
               } else {
                  fail = true;
               }
            }
         } else {
            fail = true;
         }

         success = !fail;
      } catch (Throwable var7) {
      }
   }
}
