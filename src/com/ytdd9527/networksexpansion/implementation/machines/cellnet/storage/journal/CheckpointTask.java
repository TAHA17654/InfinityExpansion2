package com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage.journal;

import com.balugaq.netex.utils.Debug;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage.StorageConfig;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage.connection.ConnectionManager;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage.schema.DDLProvider;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage.util.CRC32Utils;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage.write.WriteStrategy;
import io.github.sefiraat.networks.Networks;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

public class CheckpointTask implements Runnable {
   private static final Logger LOGGER = Networks.getInstance().getLogger();
   private final int checkpointBatchSize = 500;
   private final long journalRetentionMs;
   private final long archiveRetentionMs;
   private final boolean archiveEnabled;
   private final int archiveMaxRows;
   private final ConnectionManager connMgr;
   private final WriteStrategy writeStrategy;
   private final DDLProvider ddl;

   public CheckpointTask(ConnectionManager connMgr, WriteStrategy writeStrategy, DDLProvider ddl, StorageConfig config) {
      this.connMgr = connMgr;
      this.writeStrategy = writeStrategy;
      this.ddl = ddl;
      this.journalRetentionMs = config.getJournalRetentionMinutes() * 60L * 1000L;
      this.archiveRetentionMs = (long)config.getArchiveRetentionDays() * 24L * 60L * 60L * 1000L;
      this.archiveEnabled = config.isArchiveEnabled();
      this.archiveMaxRows = config.getArchiveMaxRows();
   }

   @Override
   public void run() {
      try {
         this.doCheckpoint();
      } catch (Exception var2) {
         LOGGER.warning(Networks.getLocalizationService().getString("messages.cellnet.persistence.checkpoint_failed", var2.getMessage()));
         Debug.trace(var2);
      }
   }

   public void doCheckpoint() {
      long maxJournalId = this.queryMaxPendingJournalId();
      if (maxJournalId >= 0L) {
         boolean more = true;

         while (more) {
            try {
               more = this.writeStrategy.runExclusive(() -> this.doCheckpointBatch(maxJournalId)) >= 500;
            } catch (Exception var5) {
               LOGGER.warning(Networks.getLocalizationService().getString("messages.cellnet.persistence.checkpoint_lock_timeout"));
               Debug.trace(var5);
               return;
            }
         }

         if (this.archiveEnabled) {
            this.archiveAndCleanup();
         }
      }
   }

   private int doCheckpointBatch(long maxJournalId) {
      try {
         int var16;
         try (Connection conn = this.connMgr.getConnection()) {
            conn.setAutoCommit(false);

            try {
               CheckpointTask.LoadedJournal loaded = this.loadPendingJournal(conn, maxJournalId);
               if (loaded.entries.isEmpty()) {
                  conn.commit();
                  return loaded.readCount;
               }

               Map<String, Map<Long, CheckpointTask.JournalEntry>> grouped = this.groupByCell(loaded.entries);

               for (Entry<String, Map<Long, CheckpointTask.JournalEntry>> cellEntry : grouped.entrySet()) {
                  String cellUuid = cellEntry.getKey();

                  for (Entry<Long, CheckpointTask.JournalEntry> itemEntry : cellEntry.getValue().entrySet()) {
                     this.applyJournalEntry(conn, cellUuid, itemEntry.getValue());
                  }

                  this.updateCellMetaStored(conn, cellUuid);
               }

               this.markApplied(conn, loaded.entries);
               conn.commit();
               var16 = loaded.readCount;
            } catch (SQLException var12) {
               conn.rollback();
               throw var12;
            }
         }

         return var16;
      } catch (SQLException var14) {
         LOGGER.warning(Networks.getLocalizationService().getString("messages.cellnet.persistence.checkpoint_batch_failed", var14.getMessage()));
         Debug.trace(var14);
         return 0;
      }
   }

   private void applyJournalEntry(Connection conn, String cellUuid, CheckpointTask.JournalEntry je) throws SQLException {
      JournalOp op = JournalOp.fromCode(je.op);
      if (op != null) {
         switch (op) {
            case PUT:
               if (je.tplId != null && je.newAmount != null) {
                  if (je.newAmount > 0L) {
                     String sql = this.ddl.upsertCellItem();

                     try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, cellUuid);
                        ps.setLong(2, je.tplId);
                        ps.setLong(3, je.newAmount);
                        ps.setInt(4, CRC32Utils.computeCellItem(cellUuid, je.tplId, je.newAmount));
                        ps.setLong(5, je.timestamp);
                        ps.executeUpdate();
                     }
                  } else {
                     try (PreparedStatement ps = conn.prepareStatement("DELETE FROM cell_items WHERE cell_uuid = ? AND tpl_id = ?")) {
                        ps.setString(1, cellUuid);
                        ps.setLong(2, je.tplId);
                        ps.executeUpdate();
                     }
                  }
               }
               break;
            case REMOVE:
               if (je.tplId != null) {
                  try (PreparedStatement ps = conn.prepareStatement("DELETE FROM cell_items WHERE cell_uuid = ? AND tpl_id = ?")) {
                     ps.setString(1, cellUuid);
                     ps.setLong(2, je.tplId);
                     ps.executeUpdate();
                  }
               }
         }
      }
   }

   private void updateCellMetaStored(Connection conn, String cellUuid) throws SQLException {
      long stored = 0L;

      try (PreparedStatement ps = conn.prepareStatement("SELECT COALESCE(SUM(amount), 0) FROM cell_items WHERE cell_uuid = ?")) {
         ps.setString(1, cellUuid);

         try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
               stored = rs.getLong(1);
            }
         }
      }

      String snapshotHash = this.computeSnapshotHash(conn, cellUuid);

      try (PreparedStatement upd = conn.prepareStatement("UPDATE cell_meta SET stored = ?, snapshot_hash = ?, updated_at = ? WHERE cell_uuid = ?")) {
         upd.setLong(1, stored);
         upd.setString(2, snapshotHash);
         upd.setLong(3, System.currentTimeMillis());
         upd.setString(4, cellUuid);
         upd.executeUpdate();
      }
   }

   private String computeSnapshotHash(Connection conn, String cellUuid) {
      try {
         MessageDigest sha256 = MessageDigest.getInstance("SHA-256");

         String var9;
         try (PreparedStatement ps = conn.prepareStatement("SELECT cell_uuid, tpl_id, amount FROM cell_items WHERE cell_uuid = ? ORDER BY tpl_id")) {
            ps.setString(1, cellUuid);

            try (ResultSet rs = ps.executeQuery()) {
               StringBuilder digest = new StringBuilder();
               boolean first = true;

               while (rs.next()) {
                  if (!first) {
                     digest.append('\n');
                  }

                  first = false;
                  digest.append(rs.getString("cell_uuid")).append('|').append(rs.getLong("tpl_id")).append('|').append(rs.getLong("amount"));
               }

               byte[] hash = sha256.digest(digest.toString().getBytes(StandardCharsets.UTF_8));
               var9 = bytesToHex(hash);
            }
         }

         return var9;
      } catch (SQLException | NoSuchAlgorithmException var14) {
         LOGGER.warning(Networks.getLocalizationService().getString("messages.cellnet.persistence.checkpoint_snapshot_hash_failed", var14.getMessage()));
         return null;
      }
   }

   private static String bytesToHex(byte[] bytes) {
      StringBuilder sb = new StringBuilder(bytes.length * 2);

      for (byte b : bytes) {
         sb.append(String.format("%02x", b & 255));
      }

      return sb.toString();
   }

   private CheckpointTask.LoadedJournal loadPendingJournal(Connection conn, long maxJournalId) throws SQLException {
      String sql = "SELECT journal_id, cell_uuid, op, tpl_id, new_amount, crc32, timestamp FROM journal WHERE applied = 0 AND journal_id <= ? ORDER BY journal_id LIMIT ?";
      List<CheckpointTask.JournalEntry> entries = new ArrayList<>();
      List<Long> corruptedIds = new ArrayList<>();
      int readCount = 0;

      try (PreparedStatement ps = conn.prepareStatement(sql)) {
         ps.setLong(1, maxJournalId);
         ps.setInt(2, 500);

         try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
               readCount++;
               CheckpointTask.JournalEntry je = new CheckpointTask.JournalEntry();
               je.journalId = rs.getLong("journal_id");
               je.cellUuid = rs.getString("cell_uuid");
               je.op = rs.getString("op").charAt(0);
               long rawTplId = rs.getLong("tpl_id");
               je.tplId = rs.wasNull() ? null : rawTplId;
               long rawNewAmount = rs.getLong("new_amount");
               je.newAmount = rs.wasNull() ? null : rawNewAmount;
               je.crc32 = rs.getInt("crc32");
               je.timestamp = rs.getLong("timestamp");
               int expectedCrc = CRC32Utils.computeJournal(je.cellUuid, je.op, je.tplId, je.newAmount);
               if (je.crc32 != expectedCrc) {
                  corruptedIds.add(je.journalId);
               } else {
                  entries.add(je);
               }
            }
         }
      }

      if (!corruptedIds.isEmpty()) {
         Debug.debug("Journal checkpoint skipped " + corruptedIds.size() + " corrupted entries (CRC mismatch), ids=" + corruptedIds);
         this.markCorruptedApplied(conn, corruptedIds);
      }

      return new CheckpointTask.LoadedJournal(entries, readCount);
   }

   private void markCorruptedApplied(Connection conn, List<Long> journalIds) throws SQLException {
      try (PreparedStatement ps = conn.prepareStatement("UPDATE journal SET applied = 1 WHERE journal_id = ?")) {
         for (long id : journalIds) {
            ps.setLong(1, id);
            ps.addBatch();
         }

         ps.executeBatch();
      }
   }

   private Map<String, Map<Long, CheckpointTask.JournalEntry>> groupByCell(List<CheckpointTask.JournalEntry> entries) {
      Map<String, Map<Long, CheckpointTask.JournalEntry>> grouped = new LinkedHashMap<>();

      for (CheckpointTask.JournalEntry je : entries) {
         grouped.computeIfAbsent(je.cellUuid, k -> new LinkedHashMap<>());
         long key = je.tplId != null ? je.tplId : -1L;
         CheckpointTask.JournalEntry existing = grouped.get(je.cellUuid).get(key);
         if (existing == null || je.journalId > existing.journalId) {
            grouped.get(je.cellUuid).put(key, je);
         }
      }

      return grouped;
   }

   private void markApplied(Connection conn, List<CheckpointTask.JournalEntry> entries) throws SQLException {
      try (PreparedStatement ps = conn.prepareStatement("UPDATE journal SET applied = 1 WHERE journal_id = ?")) {
         for (CheckpointTask.JournalEntry je : entries) {
            ps.setLong(1, je.journalId);
            ps.addBatch();
         }

         ps.executeBatch();
      }
   }

   private long queryMaxPendingJournalId() {
      try (
         Connection conn = this.connMgr.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery("SELECT MAX(journal_id) FROM journal WHERE applied = 0");
      ) {
         if (rs.next()) {
            long val = rs.getLong(1);
            return rs.wasNull() ? -1L : val;
         } else {
            return -1L;
         }
      } catch (SQLException var14) {
         LOGGER.warning(Networks.getLocalizationService().getString("messages.cellnet.persistence.checkpoint_query_max_id_failed", var14.getMessage()));
         Debug.trace(var14);
         return -1L;
      }
   }

   private void archiveAndCleanup() {
      long now = System.currentTimeMillis();
      long retentionCutoff = now - this.journalRetentionMs;
      long archiveCutoff = now - this.archiveRetentionMs;

      try (Connection conn = this.connMgr.getConnection()) {
         conn.setAutoCommit(false);

         try {
            try (PreparedStatement ps = conn.prepareStatement(
                  "INSERT INTO journal_archive (journal_id, cell_uuid, op, tpl_id, new_amount, crc32, timestamp) SELECT journal_id, cell_uuid, op, tpl_id, new_amount, crc32, timestamp FROM journal WHERE applied = 1 AND timestamp < ?"
               )) {
               ps.setLong(1, retentionCutoff);
               ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM journal WHERE applied = 1 AND timestamp < ?")) {
               ps.setLong(1, retentionCutoff);
               ps.executeUpdate();
            }

            conn.commit();
         } catch (SQLException var18) {
            conn.rollback();
            throw var18;
         }

         try (PreparedStatement ps = conn.prepareStatement("DELETE FROM journal_archive WHERE timestamp < ?")) {
            ps.setLong(1, archiveCutoff);
            ps.executeUpdate();
         }

         if (this.archiveMaxRows > 0) {
            this.trimArchiveToMaxRows(conn);
         }

         conn.commit();
      } catch (SQLException var20) {
         LOGGER.warning(Networks.getLocalizationService().getString("messages.cellnet.persistence.checkpoint_archive_failed", var20.getMessage()));
         Debug.trace(var20);
      }
   }

   private void trimArchiveToMaxRows(Connection conn) throws SQLException {
      long count = 0L;

      try (
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM journal_archive");
      ) {
         if (rs.next()) {
            count = rs.getLong(1);
         }
      }

      if (count > (long)this.archiveMaxRows) {
         long toDelete = count - (long)this.archiveMaxRows;

         try (PreparedStatement ps = conn.prepareStatement(
               "DELETE FROM journal_archive WHERE rowid IN (SELECT rowid FROM journal_archive ORDER BY timestamp ASC LIMIT ?)"
            )) {
            ps.setLong(1, toDelete);
            ps.executeUpdate();
         }
      }
   }

   public void replayPendingJournal() {
      long maxId = this.queryMaxPendingJournalId();
      if (maxId >= 0L) {
         boolean more = true;

         while (more) {
            try {
               more = this.writeStrategy.runExclusive(() -> this.doCheckpointBatch(maxId)) >= 500;
            } catch (Exception var5) {
               LOGGER.warning(Networks.getLocalizationService().getString("messages.cellnet.persistence.replay_lock_timeout"));
               Debug.trace(var5);
               return;
            }
         }
      }
   }

   private static class JournalEntry {
      long journalId;
      String cellUuid;
      char op;
      Long tplId;
      Long newAmount;
      int crc32;
      long timestamp;
   }

   private static class LoadedJournal {
      final List<CheckpointTask.JournalEntry> entries;
      final int readCount;

      LoadedJournal(List<CheckpointTask.JournalEntry> entries, int readCount) {
         this.entries = entries;
         this.readCount = readCount;
      }
   }
}
