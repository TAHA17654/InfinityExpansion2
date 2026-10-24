package com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage.write;

import io.github.sefiraat.networks.Networks;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

public class WriteStrategy {
   private static final long TIMEOUT_SECONDS_DEFAULT = 10L;
   private final long timeoutSeconds;
   private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
      Thread t = new Thread(r, "NetworksExpansion-Cellnet-Write");
      t.setDaemon(true);
      return t;
   });

   public WriteStrategy() {
      long configured = Networks.getConfigManager().getCellnetStorageWriteTimeout();
      this.timeoutSeconds = configured > 0L ? configured : 10L;
   }

   public <T> T runExclusive(Supplier<T> task) {
      CompletableFuture<T> future = CompletableFuture.supplyAsync(task, this.executor);

      try {
         return future.get(this.timeoutSeconds, TimeUnit.SECONDS);
      } catch (InterruptedException var10) {
         Thread.currentThread().interrupt();
         throw new IllegalStateException("Networks Cell Network write operation was interrupted", var10);
      } catch (ExecutionException var11) {
         Throwable cause = var11.getCause();
         if (cause instanceof RuntimeException re) {
            throw re;
         } else {
            throw new IllegalStateException("Networks Cell Network write operation failed", cause);
         }
      } catch (TimeoutException var12) {
         try {
            return future.get(this.timeoutSeconds, TimeUnit.SECONDS);
         } catch (InterruptedException var7) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Networks Cell Network write operation was interrupted", var7);
         } catch (TimeoutException var8) {
            throw new IllegalStateException("Networks Cell Network write operation timed out, task is still running in the background", var8);
         } catch (ExecutionException var9) {
            Throwable causex = var9.getCause();
            if (causex instanceof RuntimeException re) {
               throw re;
            } else {
               throw new IllegalStateException("Networks Cell Network write operation failed", causex);
            }
         }
      }
   }

   public void shutdown() {
      this.executor.shutdown();

      try {
         if (!this.executor.awaitTermination(this.timeoutSeconds, TimeUnit.SECONDS)) {
            this.executor.shutdownNow();
         }
      } catch (InterruptedException var2) {
         this.executor.shutdownNow();
         Thread.currentThread().interrupt();
      }
   }
}
