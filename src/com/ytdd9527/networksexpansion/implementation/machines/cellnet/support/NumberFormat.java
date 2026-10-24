package com.ytdd9527.networksexpansion.implementation.machines.cellnet.support;

import org.jetbrains.annotations.NotNull;

public final class NumberFormat {
   private static final long K = 1000L;
   private static final long M = 1000000L;
   private static final long B = 1000000000L;
   private static final long T = 1000000000000L;

   private NumberFormat() {
   }

   @NotNull
   public static String formatNumber(long value) {
      if (value >= 1000000000000L) {
         return fixed(value, 1000000000000L, 2) + "T";
      } else if (value >= 1000000000L) {
         return fixed(value, 1000000000L, 2) + "B";
      } else if (value >= 1000000L) {
         return fixed(value, 1000000L, 2) + "M";
      } else {
         return value >= 1000L ? fixed(value, 1000L, 2) + "K" : Long.toString(value);
      }
   }

   @NotNull
   public static String formatCellShort(long value) {
      if (value >= 1000000000000L) {
         return fixed(value, 1000000000000L, 1) + "T";
      } else if (value >= 1000000000L) {
         return fixed(value, 1000000000L, 1) + "B";
      } else if (value >= 1000000L) {
         return fixed(value, 1000000L, 0) + "M";
      } else if (value >= 10000L) {
         return fixed(value, 1000L, 0) + "K";
      } else {
         return value >= 1000L ? fixed(value, 1000L, 1) + "K" : Long.toString(value);
      }
   }

   @NotNull
   public static String formatCellNumber(long value) {
      if (value >= 1000000000000L) {
         return fixed(value, 1000000000000L, 1) + "T";
      } else if (value >= 1000000000L) {
         return fixed(value, 1000000000L, 1) + "B";
      } else if (value >= 1000000L) {
         return fixed(value, 1000000L, 0) + "M";
      } else {
         return value >= 1000L ? grouped(value) : Long.toString(value);
      }
   }

   @NotNull
   private static String fixed(long value, long divisor, int decimals) {
      long whole = value / divisor;
      long rem = value % divisor;
      if (decimals == 0) {
         return Long.toString(whole + (rem * 2L >= divisor ? 1L : 0L));
      } else {
         long scale = decimals == 1 ? 10L : 100L;
         long scaled = (rem * scale * 2L + divisor) / (divisor * 2L);
         if (scaled == scale) {
            whole++;
            scaled = 0L;
         }

         String frac = Long.toString(scaled);
         return decimals == 2 && frac.length() == 1 ? whole + ".0" + frac : whole + "." + frac;
      }
   }

   @NotNull
   private static String grouped(long value) {
      String s = Long.toString(value);
      int first = s.length() % 3;
      StringBuilder sb = new StringBuilder(s.length() + s.length() / 3);

      for (int i = 0; i < s.length(); i++) {
         if (i > 0 && (i - first) % 3 == 0) {
            sb.append(',');
         }

         sb.append(s.charAt(i));
      }

      return sb.toString();
   }
}
