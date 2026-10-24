package com.ytdd9527.networksexpansion.utils.databases;

import com.balugaq.netex.utils.Debug;
import com.balugaq.netex.utils.Lang;
import io.github.sefiraat.networks.Networks;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class QueryQueue {
   @NotNull
   private final BlockingQueue<QueuedTask> updateTasks = new LinkedBlockingDeque<>();
   @NotNull
   private final BlockingQueue<QueuedTask> queryTasks = new LinkedBlockingDeque<>();
   private boolean threadStarted = false;

   public synchronized void scheduleUpdate(@NotNull QueuedTask task) {
      if (!this.updateTasks.offer(task)) {
         throw new IllegalStateException(Lang.getString("messages.unsupported-operation.comprehensive.invalid_queue"));
      }
   }

   public synchronized void scheduleQuery(@NotNull QueuedTask task) {
      if (!this.queryTasks.offer(task)) {
         throw new IllegalStateException(Lang.getString("messages.unsupported-operation.comprehensive.invalid_queue"));
      }
   }

   public void startThread() {
      if (!this.threadStarted) {
         this.getProcessor(this.queryTasks).runTaskAsynchronously(Networks.getInstance());
         this.getProcessor(this.updateTasks).runTaskAsynchronously(Networks.getInstance());
         this.threadStarted = true;
      }
   }

   public int getTaskAmount() {
      return this.updateTasks.size() + this.queryTasks.size();
   }

   public boolean isAllDone() {
      return !this.threadStarted || this.getTaskAmount() == 0;
   }

   public void scheduleAbort() {
      QueuedTask abortTask = new QueuedTask() {
         @Override
         public boolean execute() {
            return true;
         }

         @Override
         public boolean callback() {
            return true;
         }
      };
      this.queryTasks.offer(abortTask);
      this.updateTasks.offer(abortTask);
   }

   @NotNull
   private BukkitRunnable getProcessor(@NotNull final BlockingQueue<QueuedTask> queue) {
      return new BukkitRunnable() {
         public void run() {
            while (true) {
               try {
                  QueuedTask task = queue.take();
                  if (task.execute() && task.callback()) {
                     return;
                  }
               } catch (Exception var2) {
                  Debug.trace(var2);
               }
            }
         }
      };
   }
}
