package com.ytdd9527.networksexpansion.utils;

import io.github.sefiraat.networks.Networks;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lombok.Generated;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.jetbrains.annotations.NotNull;

public final class TextUtil {
   public static final String PLACEHOLDER = "\u2020";
   public static final String COLOR_NORMAL = "\u00a7x\u00a78\u00a78\u00a7f\u00a7f\u00a7f\u00a7f";
   public static final String COLOR_STRESS = "\u00a7x\u00a7f\u00a7f\u00a7f\u00a7f\u00a78\u00a78";
   public static final String COLOR_ACTION = "\u00a7x\u00a7f\u00a7f\u00a78\u00a78\u00a70\u00a70";
   public static final String COLOR_INITIATIVE = "\u00a7x\u00a70\u00a70\u00a78\u00a78\u00a7f\u00a7f";
   public static final String COLOR_PASSIVE = "\u00a7x\u00a70\u00a70\u00a7f\u00a7f\u00a78\u00a78";
   public static final String COLOR_NUMBER = "\u00a7x\u00a7f\u00a7f\u00a78\u00a78\u00a7f\u00a7f";
   public static final String COLOR_POSITIVE = "\u00a7x\u00a78\u00a78\u00a7f\u00a7f\u00a78\u00a78";
   public static final String COLOR_NEGATIVE = "\u00a7x\u00a7f\u00a7f\u00a78\u00a78\u00a78\u00a78";
   public static final String COLOR_CONCEAL = "\u00a7x\u00a78\u00a78\u00a78\u00a78\u00a78\u00a78";
   public static final String COLOR_INPUT = "\u00a79";
   public static final String COLOR_OUTPUT = "\u00a76";
   public static final String BLACK = color("&0");
   public static final String DARK_BLUE = color("&1");
   public static final String DARK_GREEN = color("&2");
   public static final String DARK_AQUA = color("&3");
   public static final String DARK_RED = color("&4");
   public static final String DARK_PURPLE = color("&5");
   public static final String GOLD = color("&6");
   public static final String GRAY = color("&7");
   public static final String DARK_GRAY = color("&8");
   public static final String BLUE = color("&9");
   public static final String GREEN = color("&a");
   public static final String AQUA = color("&b");
   public static final String RED = color("&c");
   public static final String LIGHT_PURPLE = color("&d");
   public static final String YELLOW = color("&e");
   public static final String WHITE = color("&f");
   public static final String OBFUSCATED = color("&k");
   public static final String BOLD = color("&l");
   public static final String STRIKETHROUGH = color("&m");
   public static final String UNDERLINE = color("&n");
   public static final String ITALIC = color("&o");
   public static final String RESET = color("&r");
   public static final String MAGIC = OBFUSCATED;
   public static final Color WHITE_COLOR = Color.fromRGB(255, 255, 255);
   private static long COUNT = 0L;

   @NotNull
   public static String colorString(@NotNull String string0, @NotNull List<Color> colorList) {
      StringBuilder stringBuilder = new StringBuilder();
      if (string0.isEmpty()) {
         string0 = string0 + " ";
      }

      if (string0.length() == 1) {
         string0 = string0 + " ";
      }

      String string = string0.replaceAll("%s", "\u2020");
      int i = 0;

      for (int length = string.length() - 1; i <= length; i++) {
         double p = (double)i / (double)length * (double)(colorList.size() - 1);
         Color color1 = colorList.get((int)Math.floor(p));
         Color color2 = colorList.get((int)Math.ceil(p));
         int blue = (int)((double)color1.getBlue() * (1.0 - p + Math.floor(p)) + (double)color2.getBlue() * (p - Math.floor(p)));
         int green = (int)((double)color1.getGreen() * (1.0 - p + Math.floor(p)) + (double)color2.getGreen() * (p - Math.floor(p)));
         int red = (int)((double)color1.getRed() * (1.0 - p + Math.floor(p)) + (double)color2.getRed() * (p - Math.floor(p)));
         stringBuilder.append("\u00a7x")
            .append("\u00a7")
            .append(codeColor(red / 16))
            .append("\u00a7")
            .append(codeColor(red % 16))
            .append("\u00a7")
            .append(codeColor(green / 16))
            .append("\u00a7")
            .append(codeColor(green % 16))
            .append("\u00a7")
            .append(codeColor(blue / 16))
            .append("\u00a7")
            .append(codeColor(blue % 16));
         stringBuilder.append(string.charAt(i));
      }

      String re = stringBuilder.toString();
      return re.replaceAll("\u2020", "%s");
   }

   @NotNull
   public static String colorRandomString(@NotNull String string) {
      List<Color> colorList = new ArrayList<>();

      for (double r = 1.0; 1.0 / r >= Math.random() && r * r <= (double)string.length(); r++) {
         int red = (int)((Math.random() * 8.0 + 8.0) * 15.0 + Math.random() * 12.0 + 4.0);
         int green = (int)((Math.random() * 8.0 + 8.0) * 15.0 + Math.random() * 12.0 + 4.0);
         int blue = (int)((Math.random() * 8.0 + 8.0) * 15.0 + Math.random() * 12.0 + 4.0);
         colorList.add(Color.fromRGB(red, green, blue));
      }

      return colorString(string, colorList);
   }

   @NotNull
   public static String colorPseudorandomString(@NotNull String string) {
      List<Color> colorList = new ArrayList<>();
      double r = 1.0;

      for (Random random = new Random((long)(string.hashCode() / 2 + Networks.getInstance().getServer().getName().hashCode() / 2));
         1.0 / r >= random.nextDouble() && r * r <= (double)string.length();
         r++
      ) {
         int red = (int)((random.nextDouble() * 8.0 + 8.0) * 15.0 + random.nextDouble() * 12.0 + 4.0);
         int green = (int)((random.nextDouble() * 8.0 + 8.0) * 15.0 + random.nextDouble() * 12.0 + 4.0);
         int blue = (int)((random.nextDouble() * 8.0 + 8.0) * 15.0 + random.nextDouble() * 12.0 + 4.0);
         colorList.add(Color.fromRGB(red, green, blue));
      }

      return colorString(string, colorList);
   }

   @NotNull
   public static String colorPseudorandomString(@NotNull String string0, long seed) {
      List<Color> colorList = new ArrayList<>();
      double r = 1.0;
      String string = string0.replaceAll("%s", "\u2020");

      for (Random random = new Random((long)(string.hashCode() / 2) + seed / 2L); 1.0 / r >= random.nextDouble() && r * r <= (double)string.length(); r++) {
         int red = (int)((random.nextDouble() * 8.0 + 8.0) * 15.0 + random.nextDouble() * 12.0 + 4.0);
         int green = (int)((random.nextDouble() * 8.0 + 8.0) * 15.0 + random.nextDouble() * 12.0 + 4.0);
         int blue = (int)((random.nextDouble() * 8.0 + 8.0) * 15.0 + random.nextDouble() * 12.0 + 4.0);
         colorList.add(Color.fromRGB(red, green, blue));
      }

      return colorString(string, colorList);
   }

   @NotNull
   public static String getRandomColor() {
      return "\u00a7x\u00a7"
         + codeColor((int)(Math.random() * 8.0) + 8)
         + "\u00a7"
         + codeColor((int)(Math.random() * 8.0) + 8)
         + "\u00a7"
         + codeColor((int)(Math.random() * 8.0) + 8)
         + "\u00a7"
         + codeColor((int)(Math.random() * 8.0) + 8)
         + "\u00a7"
         + codeColor((int)(Math.random() * 8.0) + 8)
         + "\u00a7"
         + codeColor((int)(Math.random() * 8.0) + 8);
   }

   @NotNull
   public static String getPseudorandomColor(long seed) {
      COUNT += seed;
      Random random = new Random(COUNT);
      return "\u00a7x\u00a7"
         + codeColor(random.nextInt(8) + 8)
         + "\u00a7"
         + codeColor(random.nextInt(8) + 8)
         + "\u00a7"
         + codeColor(random.nextInt(8) + 8)
         + "\u00a7"
         + codeColor(random.nextInt(8) + 8)
         + "\u00a7"
         + codeColor(random.nextInt(8) + 8)
         + "\u00a7"
         + codeColor(random.nextInt(8) + 8);
   }

   @NotNull
   public static String toTextCode(@NotNull Color color) {
      return "\u00a7x\u00a7"
         + codeColor(color.getRed() / 16)
         + "\u00a7"
         + codeColor(color.getRed() % 16)
         + "\u00a7"
         + codeColor(color.getGreen() / 16)
         + "\u00a7"
         + codeColor(color.getGreen() % 16)
         + "\u00a7"
         + codeColor(color.getBlue() / 16)
         + "\u00a7"
         + codeColor(color.getBlue() % 16);
   }

   @NotNull
   public static String codeColor(int c) {
      if (c < 10 && c >= 0) {
         return String.valueOf(c);
      } else {
         return switch (c) {
            case 10 -> "a";
            case 11 -> "b";
            case 12 -> "c";
            case 13 -> "d";
            case 14 -> "e";
            case 15 -> "f";
            default -> "0";
         };
      }
   }

   @NotNull
   public static Color cloneColor(@NotNull Color color) {
      return Color.fromRGB(color.getRed(), color.getGreen(), color.getBlue());
   }

   @NotNull
   public static Color[] disperse(int size, @NotNull Color... colors) {
      if (size == 1 && colors.length > 0) {
         return new Color[]{cloneColor(colors[0])};
      } else if (size != 0 && colors.length != 0) {
         Color[] result = new Color[size--];

         for (int i = 0; i <= size; i++) {
            double p = (double)i / (double)size * (double)(colors.length - 1);
            int r = (int)(
               (double)colors[(int)Math.floor(p)].getRed() * (1.0 - p + Math.floor(p)) + (double)colors[(int)Math.ceil(p)].getRed() * (p - Math.floor(p))
            );
            int g = (int)(
               (double)colors[(int)Math.floor(p)].getGreen() * (1.0 - p + Math.floor(p)) + (double)colors[(int)Math.ceil(p)].getGreen() * (p - Math.floor(p))
            );
            int b = (int)(
               (double)colors[(int)Math.floor(p)].getBlue() * (1.0 - p + Math.floor(p)) + (double)colors[(int)Math.ceil(p)].getBlue() * (p - Math.floor(p))
            );
            result[i] = Color.fromRGB(r, g, b);
         }

         return result;
      } else {
         return new Color[0];
      }
   }

   @NotNull
   public static Color[] disperse(int size, @NotNull List<Color> colorList) {
      if (size == 1 && !colorList.isEmpty()) {
         return new Color[]{cloneColor(colorList.get(0))};
      } else if (size != 0 && !colorList.isEmpty()) {
         Color[] result = new Color[size--];

         for (int i = 0; i <= size; i++) {
            double p = (double)i / (double)size * (double)(colorList.size() - 1);
            int r = (int)(
               (double)colorList.get((int)Math.floor(p)).getRed() * (1.0 - p + Math.floor(p))
                  + (double)colorList.get((int)Math.ceil(p)).getRed() * (p - Math.floor(p))
            );
            int g = (int)(
               (double)colorList.get((int)Math.floor(p)).getGreen() * (1.0 - p + Math.floor(p))
                  + (double)colorList.get((int)Math.ceil(p)).getGreen() * (p - Math.floor(p))
            );
            int b = (int)(
               (double)colorList.get((int)Math.floor(p)).getBlue() * (1.0 - p + Math.floor(p))
                  + (double)colorList.get((int)Math.ceil(p)).getBlue() * (p - Math.floor(p))
            );
            result[i] = Color.fromRGB(r, g, b);
         }

         return result;
      } else {
         return new Color[0];
      }
   }

   @NotNull
   public static String color(@NotNull String s) {
      return ChatColor.translateAlternateColorCodes('&', s);
   }

   @NotNull
   public static String stripColor(String s) {
      return ChatColor.stripColor(s);
   }

   @Generated
   private TextUtil() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
