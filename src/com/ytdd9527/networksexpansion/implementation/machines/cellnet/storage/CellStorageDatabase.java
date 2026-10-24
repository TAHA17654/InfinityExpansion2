package com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage;

import com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage.connection.ConnectionManager;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage.dao.CellDao;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage.journal.CheckpointTask;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage.journal.DirtyTracker;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage.journal.JournalWriter;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage.schema.DDLProvider;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage.schema.SchemaManager;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage.template.ItemKeyTplIdBridge;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage.template.ItemTemplateRegistry;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage.write.WriteStrategy;
import io.github.sefiraat.networks.Networks;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

public class CellStorageDatabase {
   private static final Logger LOGGER = Networks.getInstance().getLogger();
   private static final AtomicLong FLUSH_COUNTER = new AtomicLong(0L);
   private final StorageConfig storageConfig = new StorageConfig();
   private final ConnectionManager connMgr = new ConnectionManager(this.storageConfig);
   private final SchemaManager schemaManager = new SchemaManager(this.connMgr);
   private final ItemTemplateRegistry templateRegistry;
   private final ItemKeyTplIdBridge bridge;
   private final DirtyTracker dirtyTracker;
   private final WriteStrategy writeStrategy;
   private final JournalWriter journalWriter;
   private final CheckpointTask checkpointTask;
   private final CellDao storageController;
   private final BackupTask backupTask;
   private ScheduledExecutorService checkpointExecutor;
   private ScheduledExecutorService backupExecutor;

   public CellStorageDatabase() {
      DDLProvider ddl = this.schemaManager.getDDL();
      this.templateRegistry = new ItemTemplateRegistry(this.connMgr, ddl);
      this.bridge = new ItemKeyTplIdBridge(this.templateRegistry);
      this.dirtyTracker = new DirtyTracker();
      this.writeStrategy = new WriteStrategy();
      this.journalWriter = new JournalWriter(this.writeStrategy, this.dirtyTracker, this.connMgr);
      this.checkpointTask = new CheckpointTask(this.connMgr, this.writeStrategy, ddl, this.storageConfig);
      this.storageController = new CellDao(this.connMgr, this.bridge, this.dirtyTracker);
      this.backupTask = new BackupTask(this.storageConfig, this.connMgr);
   }

   public void init() {
      this.schemaManager.initSchema();
      this.schemaManager.markSchemaVersion();
      this.templateRegistry.preloadAll();
      this.checkpointTask.replayPendingJournal();
      this.checkpointExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
         Thread t = new Thread(r, "NetworksExpansion-Cellnet-Checkpoint");
         t.setDaemon(true);
         return t;
      });
      long checkpointInterval = (long)this.storageConfig.getCheckpointInterval();
      if (checkpointInterval > 0L) {
         this.checkpointExecutor.scheduleWithFixedDelay(this.checkpointTask, checkpointInterval, checkpointInterval, TimeUnit.SECONDS);
      }

      if (this.storageConfig.isBackupEnabled()) {
         this.backupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "NetworksExpansion-Cellnet-Backup");
            t.setDaemon(true);
            return t;
         });
         long backupIntervalSeconds = (long)this.storageConfig.getBackupIntervalHours() * 3600L;
         if (backupIntervalSeconds > 0L) {
            this.backupExecutor.scheduleWithFixedDelay(this.backupTask, backupIntervalSeconds, backupIntervalSeconds, TimeUnit.SECONDS);
         }
      }

      LOGGER.info(Networks.getLocalizationService().getString("messages.cellnet.persistence.db_initialized"));
   }

   public void saveAllAsync() {
      this.journalWriter.flush();
      long count = FLUSH_COUNTER.incrementAndGet();
      if (this.storageConfig.getCheckpointThreshold() > 0 && count % (long)this.storageConfig.getCheckpointThreshold() == 0L && this.checkpointExecutor != null
         )
       {
         this.checkpointExecutor.submit(this.checkpointTask);
      }
   }

   public void shutdown() {
      stopExecutor(this.backupExecutor, 5L);
      stopExecutor(this.checkpointExecutor, 15L);
      this.journalWriter.flush();
      this.checkpointTask.doCheckpoint();
      this.writeStrategy.shutdown();
      this.connMgr.shutdown();
      LOGGER.info(Networks.getLocalizationService().getString("messages.cellnet.persistence.db_closed"));
   }

   private static void stopExecutor(ScheduledExecutorService executor, long timeoutSeconds) {
      if (executor != null) {
         executor.shutdown();

         try {
            if (!executor.awaitTermination(timeoutSeconds, TimeUnit.SECONDS)) {
               executor.shutdownNow();
            }
         } catch (InterruptedException var4) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
         }
      }
   }

   public CellDao getStorageController() {
      return this.storageController;
   }
}
