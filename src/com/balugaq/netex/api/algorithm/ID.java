package com.balugaq.netex.api.algorithm;

import io.github.sefiraat.networks.Networks;
import java.util.concurrent.atomic.AtomicLong;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ID {
   private static AtomicLong id = new AtomicLong(0L);

   public static long nextId() {
      return id.incrementAndGet();
   }

   public static void fetchId() {
      id = new AtomicLong(Networks.getConfigManager().getLong("id", 0L));
   }

   public static void saveId() {
      Networks.getConfigManager().set("id", id.get());
   }
}
