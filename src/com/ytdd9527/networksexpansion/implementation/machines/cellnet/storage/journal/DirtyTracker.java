package com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage.journal;

import com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage.util.CRC32Utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.jetbrains.annotations.NotNull;

public class DirtyTracker {
   private final AtomicReference<ConcurrentHashMap<UUID, ConcurrentHashMap<Long, DirtyTracker.DirtyEntry>>> dirtyMapRef = new AtomicReference<>(
      new ConcurrentHashMap<>()
   );
   private final AtomicLong fallbackSequence = new AtomicLong();
   private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
   private Map<UUID, Map<Long, DirtyTracker.DirtyEntry>> pendingFlush = null;

   public void record(@NotNull UUID cellUuid, long tplId, long newAmount, @NotNull JournalOp op) {
      this.record(cellUuid, tplId, newAmount, op, this.fallbackSequence.incrementAndGet());
   }

   public void record(@NotNull UUID cellUuid, long tplId, long newAmount, @NotNull JournalOp op, long sequence) {
      DirtyTracker.DirtyEntry entry = new DirtyTracker.DirtyEntry(op, newAmount, System.currentTimeMillis(), sequence);
      this.lock.readLock().lock();

      try {
         this.dirtyMapRef.get().computeIfAbsent(cellUuid, k -> new ConcurrentHashMap<>()).merge(tplId, entry, DirtyTracker::newerEntry);
      } finally {
         this.lock.readLock().unlock();
      }
   }

   public List<JournalRow> drainPhase1() {
      this.lock.writeLock().lock();

      List snapshot;
      try {
         ConcurrentHashMap<UUID, ConcurrentHashMap<Long, DirtyTracker.DirtyEntry>> dirtyMap = this.dirtyMapRef.getAndSet(new ConcurrentHashMap<>());
         if (!dirtyMap.isEmpty()) {
            Map<UUID, Map<Long, DirtyTracker.DirtyEntry>> snapshotx = new HashMap<>();

            for (Entry<UUID, ConcurrentHashMap<Long, DirtyTracker.DirtyEntry>> entry : dirtyMap.entrySet()) {
               snapshotx.put(entry.getKey(), new HashMap<>(entry.getValue()));
            }

            this.pendingFlush = snapshotx;
            return this.toJournalRows(snapshotx);
         }

         snapshot = Collections.emptyList();
      } finally {
         this.lock.writeLock().unlock();
      }

      return snapshot;
   }

   public void commitFlush() {
      this.lock.writeLock().lock();

      try {
         this.pendingFlush = null;
      } finally {
         this.lock.writeLock().unlock();
      }
   }

   public void rollbackFlush(@NotNull List<JournalRow> failedRows) {
      this.lock.writeLock().lock();

      try {
         if (this.pendingFlush != null) {
            Map<UUID, Map<Long, DirtyTracker.DirtyEntry>> snapshot = this.pendingFlush;

            for (JournalRow row : failedRows) {
               Long tplId = row.tplId();
               if (tplId != null) {
                  UUID cellUuid = UUID.fromString(row.cellUuid());
                  Map<Long, DirtyTracker.DirtyEntry> cellMap = snapshot.get(cellUuid);
                  if (cellMap != null) {
                     DirtyTracker.DirtyEntry original = cellMap.get(tplId);
                     if (original != null) {
                        this.dirtyMapRef.get().computeIfAbsent(cellUuid, k -> new ConcurrentHashMap<>()).merge(tplId, original, DirtyTracker::newerEntry);
                     }
                  }
               }
            }

            this.pendingFlush = null;
            return;
         }
      } finally {
         this.lock.writeLock().unlock();
      }
   }

   private static DirtyTracker.DirtyEntry newerEntry(DirtyTracker.DirtyEntry a, DirtyTracker.DirtyEntry b) {
      if (a.sequence() != b.sequence()) {
         return a.sequence() > b.sequence() ? a : b;
      } else {
         return a.timestamp() >= b.timestamp() ? a : b;
      }
   }

   private List<JournalRow> toJournalRows(Map<UUID, Map<Long, DirtyTracker.DirtyEntry>> data) {
      List<JournalRow> rows = new ArrayList<>();

      for (Entry<UUID, Map<Long, DirtyTracker.DirtyEntry>> cellEntry : data.entrySet()) {
         String cellUuid = cellEntry.getKey().toString();

         for (Entry<Long, DirtyTracker.DirtyEntry> itemEntry : cellEntry.getValue().entrySet()) {
            DirtyTracker.DirtyEntry de = itemEntry.getValue();
            JournalOp op = de.op();
            Long tplId = itemEntry.getKey();
            Long newAmount = op == JournalOp.REMOVE ? null : de.newAmount();
            int crc = CRC32Utils.computeJournal(cellUuid, op.code(), tplId, newAmount);
            rows.add(new JournalRow(cellUuid, op.code(), tplId, newAmount, crc, de.timestamp()));
         }
      }

      return rows;
   }

   public static record DirtyEntry(JournalOp op, long newAmount, long timestamp, long sequence) {
   }
}
