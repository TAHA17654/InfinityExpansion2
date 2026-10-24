package com.ytdd9527.networksexpansion.utils.databases;

public interface QueuedTask {
   boolean execute();

   default boolean callback() {
      return false;
   }
}
