package com.ytdd9527.networksexpansion.utils.itemstacks;

import lombok.Generated;
import org.jetbrains.annotations.NotNull;

public final class CompareUtil {
   @NotNull
   public static CompareUtil.SimpleComparator<Integer> INTEGER_SIMPLE_COMPARATOR = (comparison, compared) -> comparison - compared;

   public static <T> int getIndex(@NotNull T[] sources, T targetValue, @NotNull CompareUtil.SimpleComparator<T> simpleComparator) {
      int left = 0;
      int right = sources.length;

      for (int mid = left + right >> 1; mid != left && mid != right; mid = left + right >> 1) {
         int r = simpleComparator.compare(targetValue, sources[mid]);
         if (r > 0) {
            left = mid;
         } else {
            if (r >= 0) {
               return mid;
            }

            right = mid;
         }
      }

      return -1;
   }

   public static <T> int getSmallFuzzyIndex(@NotNull T[] sources, T targetValue, @NotNull CompareUtil.SimpleComparator<T> simpleComparator) {
      int left = 0;
      int right = sources.length;

      for (int mid = left + right >> 1; mid != left && mid != right; mid = left + right >> 1) {
         int r = simpleComparator.compare(targetValue, sources[mid]);
         if (r > 0) {
            left = mid;
         } else {
            if (r >= 0) {
               return mid;
            }

            right = mid;
         }
      }

      return left;
   }

   public static <T> int getBigFuzzyIndex(@NotNull T[] sources, T targetValue, @NotNull CompareUtil.SimpleComparator<T> simpleComparator) {
      int left = 0;
      int right = sources.length;

      for (int mid = left + right >> 1; mid != left && mid != right; mid = left + right >> 1) {
         int r = simpleComparator.compare(targetValue, sources[mid]);
         if (r > 0) {
            left = mid;
         } else {
            if (r >= 0) {
               return mid;
            }

            right = mid;
         }
      }

      return right;
   }

   public static int getIntIndex(@NotNull int[] sources, int targetValue) {
      int left = 0;
      int right = sources.length;

      for (int mid = left + right >> 1; mid != left && mid != right; mid = left + right >> 1) {
         int r = targetValue - sources[mid];
         if (r > 0) {
            left = mid;
         } else {
            if (r >= 0) {
               return mid;
            }

            right = mid;
         }
      }

      return -1;
   }

   public static int getIntSmallFuzzyIndex(@NotNull int[] sources, int targetValue) {
      int left = 0;
      int right = sources.length;

      for (int mid = left + right >> 1; mid != left && mid != right; mid = left + right >> 1) {
         int r = targetValue - sources[mid];
         if (r > 0) {
            left = mid;
         } else {
            if (r >= 0) {
               return mid;
            }

            right = mid;
         }
      }

      return left;
   }

   public static int getIntBigFuzzyIndex(@NotNull int[] sources, int targetValue) {
      int left = 0;
      int right = sources.length;

      for (int mid = left + right >> 1; mid != left && mid != right; mid = left + right >> 1) {
         int r = targetValue - sources[mid];
         if (r > 0) {
            left = mid;
         } else {
            if (r >= 0) {
               return mid;
            }

            right = mid;
         }
      }

      return left;
   }

   @Generated
   private CompareUtil() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   public interface SimpleComparator<T> {
      int compare(T var1, T var2);
   }
}
