package com.balugaq.netex.api.enums;

import org.bukkit.Bukkit;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record MinecraftVersion(int major, int minor, int patch) implements Comparable<MinecraftVersion> {
   public static final MinecraftVersion UNKNOWN = new MinecraftVersion();
   public static final MinecraftVersion V1_20 = of(1, 20);
   public static final MinecraftVersion V1_20_5 = of(1, 20, 5);
   public static final MinecraftVersion V1_21 = of(1, 21);
   public static final MinecraftVersion V1_21_3 = of(1, 21, 3);
   public static final MinecraftVersion V1_21_4 = of(1, 21, 4);

   public MinecraftVersion() {
      this(999, 999, 999);
   }

   public static MinecraftVersion current() {
      return of(Bukkit.getMinecraftVersion());
   }

   public static MinecraftVersion of(String version) {
      return deserialize(version);
   }

   public boolean isAtLeast(String version) {
      return this.isAtLeast(of(version));
   }

   public boolean isAtLeast(MinecraftVersion version) {
      return this.major > version.major
         || this.major == version.major && this.minor > version.minor
         || this.major == version.major && this.minor == version.minor && this.patch >= version.patch;
   }

   public boolean isBefore(String version) {
      return this.isBefore(of(version));
   }

   public boolean isBefore(MinecraftVersion version) {
      return !this.isAtLeast(version) && this != version;
   }

   public static MinecraftVersion deserialize(String s) {
      String[] split = s.split("\\.");
      if (split.length < 2) {
         throw new IllegalArgumentException("Invalid version string: " + s);
      } else {
         int major = Integer.parseUnsignedInt(split[0]);
         int minor = Integer.parseUnsignedInt(split[1]);
         int patch = Integer.parseUnsignedInt(split.length == 2 ? "0" : split[2]);
         return of(major, minor, patch);
      }
   }

   public static MinecraftVersion of(int major, int minor, int patch) {
      return new MinecraftVersion(major, minor, patch);
   }

   public static MinecraftVersion of(int major, int minor) {
      return of(major, minor, 0);
   }

   public int compareTo(MinecraftVersion o) {
      return Integer.compare(this.major * 1000000 + this.minor * 10000 + this.patch, o.major * 1000000 + o.minor * 10000 + o.patch);
   }

   public String humanize() {
      return this.major + "." + this.minor + "." + this.patch;
   }
}
