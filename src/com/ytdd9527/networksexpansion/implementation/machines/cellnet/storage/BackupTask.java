package com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage;

import com.balugaq.netex.utils.Debug;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage.connection.ConnectionManager;
import io.github.sefiraat.networks.Networks;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;

public class BackupTask implements Runnable {
   private static final Logger LOGGER = Networks.getInstance().getLogger();
   private final StorageConfig config;
   private final ConnectionManager connMgr;

   public BackupTask(StorageConfig config, ConnectionManager connMgr) {
      this.config = config;
      this.connMgr = connMgr;
   }

   @Override
   public void run() {
      if (this.config.isBackupEnabled()) {
         try {
            this.doBackup();
         } catch (Exception var2) {
            LOGGER.warning(Networks.getLocalizationService().getString("messages.cellnet.persistence.backup_failed", var2.getMessage()));
            Debug.trace(var2);
         }
      }
   }

   public void doBackup() {
      File dbFile = this.config.getSqliteFile();
      if (!dbFile.exists()) {
         LOGGER.warning(Networks.getLocalizationService().getString("messages.cellnet.persistence.backup_file_missing", dbFile.getAbsolutePath()));
      } else {
         File backupDir = new File(dbFile.getParentFile(), "backups");
         backupDir.mkdirs();
         String timestamp = String.valueOf(System.currentTimeMillis());
         File backupFile = new File(backupDir, "cellnet_storage_" + timestamp + ".db");

         try {
            Files.deleteIfExists(backupFile.toPath());
         } catch (IOException var11) {
            LOGGER.warning(Networks.getLocalizationService().getString("messages.cellnet.persistence.backup_copy_failed", var11.getMessage()));
            Debug.trace(var11);
            return;
         }

         try (
            Connection conn = this.connMgr.getConnection();
            Statement st = conn.createStatement();
         ) {
            st.execute("VACUUM INTO '" + backupFile.getAbsolutePath().replace("'", "''") + "'");
         } catch (SQLException var14) {
            LOGGER.warning(Networks.getLocalizationService().getString("messages.cellnet.persistence.backup_copy_failed", var14.getMessage()));
            Debug.trace(var14);
            return;
         }

         this.deleteOldBackups(backupDir, this.config.getBackupKeep());
      }
   }

   private void deleteOldBackups(@NotNull File backupDir, int keep) {
      if (keep > 0) {
         File[] backups = backupDir.listFiles((dir, name) -> name.startsWith("cellnet_storage_") && name.endsWith(".db"));
         if (backups != null && backups.length > keep) {
            Arrays.sort(backups, Comparator.comparing(File::getName));

            for (int i = 0; i < backups.length - keep; i++) {
               try {
                  Files.deleteIfExists(backups[i].toPath());
               } catch (IOException var6) {
                  LOGGER.warning(
                     Networks.getLocalizationService().getString("messages.cellnet.persistence.backup_cleanup_failed", backups[i].getName(), var6.getMessage())
                  );
                  Debug.trace(var6);
               }
            }
         }
      }
   }
}
