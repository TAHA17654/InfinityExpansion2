package com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage.journal;

import com.balugaq.netex.utils.Debug;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage.connection.ConnectionManager;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage.write.WriteStrategy;
import io.github.sefiraat.networks.Networks;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class JournalWriter {
   private static final Logger LOGGER = Networks.getInstance().getLogger();
   private static final int FLUSH_BATCH_SIZE = 2000;
   private final WriteStrategy writeStrategy;
   private final DirtyTracker dirtyTracker;
   private final ConnectionManager connMgr;

   public JournalWriter(WriteStrategy writeStrategy, DirtyTracker dirtyTracker, ConnectionManager connMgr) {
      this.writeStrategy = writeStrategy;
      this.dirtyTracker = dirtyTracker;
      this.connMgr = connMgr;
   }

   public synchronized void flush() {
      List<JournalRow> allRows = this.dirtyTracker.drainPhase1();
      if (!allRows.isEmpty()) {
         List<List<JournalRow>> batches = partition(allRows, 2000);
         List<JournalRow> failedRows = new ArrayList<>();

         for (List<JournalRow> batch : batches) {
            try {
               this.writeStrategy.runExclusive(() -> {
                  try {
                     this.batchInsertJournal(batch);
                     return null;
                  } catch (SQLException var3x) {
                     throw new RuntimeException(var3x);
                  }
               });
            } catch (Exception var7) {
               failedRows.addAll(batch);
               LOGGER.warning(Networks.getLocalizationService().getString("messages.cellnet.persistence.journal_flush_failed", var7.getMessage()));
               Debug.trace(var7);
            }
         }

         if (failedRows.isEmpty()) {
            this.dirtyTracker.commitFlush();
         } else {
            this.dirtyTracker.rollbackFlush(failedRows);
            LOGGER.warning(Networks.getLocalizationService().getString("messages.cellnet.persistence.journal_flush_partial", failedRows.size()));
         }
      }
   }

   private void batchInsertJournal(List<JournalRow> rows) throws SQLException {
      String sql = "INSERT INTO journal (cell_uuid, op, tpl_id, new_amount, crc32, timestamp, applied) VALUES (?, ?, ?, ?, ?, ?, 0)";

      try (Connection conn = this.connMgr.getConnection()) {
         conn.setAutoCommit(false);

         try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (JournalRow row : rows) {
               ps.setString(1, row.cellUuid());
               ps.setString(2, String.valueOf(row.op()));
               if (row.tplId() != null) {
                  ps.setLong(3, row.tplId());
               } else {
                  ps.setNull(3, -5);
               }

               if (row.newAmount() != null) {
                  ps.setLong(4, row.newAmount());
               } else {
                  ps.setNull(4, -5);
               }

               ps.setInt(5, row.crc32());
               ps.setLong(6, row.timestamp());
               ps.addBatch();
            }

            ps.executeBatch();
            conn.commit();
         } catch (SQLException var10) {
            conn.rollback();
            throw var10;
         }
      }
   }

   private static <T> List<List<T>> partition(List<T> list, int batchSize) {
      List<List<T>> batches = new ArrayList<>();
      int i = 0;

      while (i < list.size()) {
         batches.add(list.subList(i, Math.min(i + batchSize, list.size())));
         i += batchSize;
      }

      return batches;
   }
}
