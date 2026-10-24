package com.ytdd9527.networksexpansion.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JavaUtil {
   @SafeVarargs
   @NotNull
   public static <T> Set<T> toSet(@NotNull T... objects) {
      Set<T> result = new HashSet<>(objects.length);
      result.addAll(Arrays.asList(objects));
      return result;
   }

   @SafeVarargs
   @NotNull
   public static <T> List<T> toList(@NotNull T... objects) {
      List<T> result = new ArrayList<>(objects.length);
      result.addAll(Arrays.asList(objects));
      return result;
   }

   public static int[] toArray(@NotNull List<Integer> list) {
      int[] result = new int[list.size()];

      for (int i = 0; i < list.size(); i++) {
         result[i] = list.get(i);
      }

      return result;
   }

   public static int[] reserve(@NotNull int[] objects) {
      int[] result = (int[])objects.clone();

      for (int i = 0; i < objects.length; i++) {
         result[i] = objects[objects.length - 1 - i];
      }

      return result;
   }

   @NotNull
   public static <T> List<T> reserve(@NotNull List<T> objectList) {
      List<T> result = new ArrayList<>(objectList);

      for (int i = 0; i < objectList.size(); i++) {
         result.set(i, objectList.get(objectList.size() - 1 - i));
      }

      return result;
   }

   public static int[] shuffle(@NotNull int[] objects) {
      List<Integer> collect = Arrays.stream(objects).boxed().collect(Collectors.toList());
      Collections.shuffle(collect);
      int[] result = (int[])objects.clone();

      for (int i = 0; i < collect.size(); i++) {
         result[i] = collect.get(i);
      }

      return result;
   }

   public static <T> T[] shuffle(@NotNull T[] objects) {
      List<T> collect = Arrays.<T>stream(objects).collect(Collectors.toList());
      Collections.shuffle(collect);
      T[] result = (T[])objects.clone();

      for (int i = 0; i < collect.size(); i++) {
         result[i] = collect.get(i);
      }

      return result;
   }

   @NotNull
   public static <T> List<T> shuffle(@NotNull List<T> objectList) {
      List<T> list = new ArrayList<>(objectList);
      Collections.shuffle(list);
      return list;
   }

   public static double[] disperse(int size, @NotNull Number... value) {
      if (size == 1 && value.length > 0) {
         return new double[]{value[0].doubleValue()};
      } else if (size != 0 && value.length != 0) {
         double[] result = new double[size--];

         for (int i = 0; i <= size; i++) {
            double p = (double)i / (double)size * (double)(value.length - 1);
            double value1 = value[(int)Math.floor(p)].doubleValue() * (1.0 - p + Math.floor(p));
            double value2 = value[(int)Math.ceil(p)].doubleValue() * (p - Math.floor(p));
            result[i] = value1 + value2;
         }

         return result;
      } else {
         return new double[0];
      }
   }

   @NotNull
   public static String[] split(@NotNull String string) {
      String[] result = new String[string.length()];

      for (int i = 0; i < string.length(); i++) {
         result[i] = String.valueOf(string.charAt(i));
      }

      return result;
   }

   public static int[] generateRandomInts(int length) {
      int[] result = new int[length];
      int i = 0;

      while (i < result.length) {
         result[i] = i++;
      }

      return shuffle(result);
   }

   public static int[] generateInts(int length) {
      int[] result = new int[length];
      int i = 0;

      while (i < result.length) {
         result[i] = i++;
      }

      return result;
   }

   @NotNull
   public static <T> List<T> shuffleByInts(@NotNull List<T> list, @NotNull int[] ints) {
      List<T> result = new ArrayList<>(list.size());

      for (int anInt : ints) {
         result.add(list.get(anInt));
      }

      return result;
   }

   @NotNull
   public static String[] addToFirst(String value, @NotNull String... values) {
      String[] result = new String[values.length + 1];
      result[0] = value;
      System.arraycopy(values, 0, result, 1, values.length);
      return result;
   }

   @SafeVarargs
   public static <T> boolean matchOnce(T source, @NotNull T... targets) {
      for (T object : targets) {
         if (object.equals(source)) {
            return true;
         }
      }

      return false;
   }

   public static long testTime(@NotNull Runnable runnable) {
      long beginTime = System.nanoTime();
      runnable.run();
      return System.nanoTime() - beginTime;
   }

   @SafeVarargs
   @Nullable
   public static <T> T getFirstNonnull(@NotNull T... objects) {
      for (T object : objects) {
         if (object != null) {
            return object;
         }
      }

      return null;
   }
}
