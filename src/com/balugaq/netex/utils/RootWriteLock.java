package com.balugaq.netex.utils;

public class RootWriteLock {
   private static final Object lock = new Object();

   public static Object get() {
      return lock;
   }
}
