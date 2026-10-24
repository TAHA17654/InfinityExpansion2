package com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage.dao;

import com.balugaq.netex.utils.Debug;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage.connection.ConnectionManager;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage.journal.DirtyTracker;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage.journal.JournalOp;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage.template.ItemKeyTplIdBridge;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.ItemKey;
import io.github.sefiraat.networks.Networks;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CellDao {
   private static final Logger LOGGER = Networks.getInstance().getLogger();
   private final ConnectionManager connMgr;
   private final ItemKeyTplIdBridge bridge;
   private final DirtyTracker dirtyTracker;

   public CellDao(ConnectionManager connMgr, ItemKeyTplIdBridge bridge, DirtyTracker dirtyTracker) {
      this.connMgr = connMgr;
      this.bridge = bridge;
      this.dirtyTracker = dirtyTracker;
   }

   @NotNull
   public CellDao.CellData loadData(@NotNull UUID cellUuid) {
      CellDao.CellData data = new CellDao.CellData(cellUuid);
      List<CellDao.CellItemRow> rows = this.queryCellItems(cellUuid.toString());
      if (!rows.isEmpty()) {
         List<Long> tplIds = new ArrayList<>(rows.size());

         for (CellDao.CellItemRow row : rows) {
            tplIds.add(row.tplId);
         }

         this.bridge.preload(tplIds);
      }

      for (CellDao.CellItemRow row : rows) {
         ItemKey key = this.bridge.resolveKey(row.tplId);
         if (key == null) {
            LOGGER.warning(Networks.getLocalizationService().getString("messages.cellnet.persistence.cell_resolve_tpl_failed", row.tplId, cellUuid));
         } else {
            data.storage.put(key.getItemStack(), Long.valueOf(row.amount));
            data.stored = data.stored + row.amount;
         }
      }

      this.loadMeta(cellUuid, data);
      return data;
   }

   private void loadMeta(@NotNull UUID cellUuid, @NotNull CellDao.CellData data) {
      String sql = "SELECT whitelist_enabled, whitelist FROM cell_meta WHERE cell_uuid = ?";

      try (
         Connection conn = this.connMgr.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
      ) {
         ps.setString(1, cellUuid.toString());

         try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
               data.whitelistEnabled = rs.getInt("whitelist_enabled") != 0;
               data.whitelist = deserializeWhitelist(rs.getBytes("whitelist"));
            }
         }
      } catch (SQLException var15) {
         LOGGER.warning(Networks.getLocalizationService().getString("messages.cellnet.persistence.cell_load_whitelist_failed", var15.getMessage()));
         Debug.trace(var15);
      }
   }

   public void markDirty(@NotNull UUID cellUuid, @NotNull ItemKey key, long finalAmount) {
      long tplId = this.bridge.getOrResolve(key);
      this.dirtyTracker.record(cellUuid, tplId, finalAmount, finalAmount > 0L ? JournalOp.PUT : JournalOp.REMOVE);
   }

   public void saveWhitelist(@NotNull UUID cellUuid, boolean whitelistEnabled, @NotNull List<ItemStack> whitelist) {
      byte[] whitelistBytes = serializeWhitelist(whitelist);
      long now = System.currentTimeMillis();
      String cellUuidStr = cellUuid.toString();
      String sql = "INSERT INTO cell_meta (cell_uuid, whitelist_enabled, whitelist, stored, snapshot_hash, updated_at, created_at) VALUES (?, ?, ?, 0, null, ?, ?) ON CONFLICT(cell_uuid) DO UPDATE SET whitelist_enabled = excluded.whitelist_enabled, whitelist = excluded.whitelist, updated_at = excluded.updated_at";

      try (
         Connection conn = this.connMgr.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
      ) {
         ps.setString(1, cellUuidStr);
         ps.setInt(2, whitelistEnabled ? 1 : 0);
         ps.setBytes(3, whitelistBytes);
         ps.setLong(4, now);
         ps.setLong(5, now);
         ps.executeUpdate();
      } catch (SQLException var17) {
         LOGGER.warning(Networks.getLocalizationService().getString("messages.cellnet.persistence.cell_save_whitelist_failed", var17.getMessage()));
         Debug.trace(var17);
      }
   }

   private List<CellDao.CellItemRow> queryCellItems(String cellUuid) {
      List<CellDao.CellItemRow> rows = new ArrayList<>();
      String sql = "SELECT tpl_id, amount FROM cell_items WHERE cell_uuid = ?";

      try (
         Connection conn = this.connMgr.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
      ) {
         ps.setString(1, cellUuid);

         try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
               rows.add(new CellDao.CellItemRow(rs.getLong("tpl_id"), rs.getLong("amount")));
            }
         }
      } catch (SQLException var15) {
         LOGGER.warning(Networks.getLocalizationService().getString("messages.cellnet.persistence.cell_load_items_failed", var15.getMessage()));
         Debug.trace(var15);
      }

      return rows;
   }

   @NotNull
   private static byte[] serializeWhitelist(@NotNull List<ItemStack> whitelist) {
      try {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();

         try (DataOutputStream out = new DataOutputStream(baos)) {
            out.writeInt(whitelist.size());

            for (ItemStack item : whitelist) {
               byte[] bytes = item.serializeAsBytes();
               out.writeInt(bytes.length);
               out.write(bytes);
            }
         }

         return baos.toByteArray();
      } catch (IOException var8) {
         LOGGER.warning(Networks.getLocalizationService().getString("messages.cellnet.persistence.cell_serialize_whitelist_failed", var8.getMessage()));
         Debug.trace(var8);
         return new byte[0];
      }
   }

   @NotNull
   private static List<ItemStack> deserializeWhitelist(@Nullable byte[] data) {
      List<ItemStack> whitelist = new ArrayList<>();
      if (data != null && data.length != 0) {
         try {
            Object i;
            try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(data))) {
               int count = in.readInt();
               if (count >= 0 && count <= data.length / 4) {
                  for (int ix = 0; ix < count; ix++) {
                     int length = in.readInt();
                     if (length < 0 || length > in.available()) {
                        LOGGER.warning(
                           Networks.getLocalizationService()
                              .getString("messages.cellnet.persistence.cell_deserialize_whitelist_failed", "invalid entry length " + length)
                        );
                        return whitelist;
                     }

                     byte[] bytes = new byte[length];
                     in.readFully(bytes);

                     try {
                        ItemStack item = ItemStack.deserializeBytes(bytes);
                        if (item != null && !item.getType().isAir()) {
                           whitelist.add(item);
                        }
                     } catch (RuntimeException var9) {
                        Debug.trace(var9, "Failed to deserialize whitelist entry");
                     }
                  }

                  return whitelist;
               }

               LOGGER.warning(
                  Networks.getLocalizationService().getString("messages.cellnet.persistence.cell_deserialize_whitelist_failed", "invalid entry count " + count)
               );
               i = whitelist;
            }

            return (List<ItemStack>)i;
         } catch (IOException var11) {
            LOGGER.warning(Networks.getLocalizationService().getString("messages.cellnet.persistence.cell_deserialize_whitelist_failed", var11.getMessage()));
            Debug.trace(var11);
            return whitelist;
         }
      } else {
         return whitelist;
      }
   }

   public static class CellData {
      public final UUID uuid;
      public final LinkedHashMap<ItemStack, Long> storage = new LinkedHashMap<>();
      public long stored;
      public boolean whitelistEnabled;
      public List<ItemStack> whitelist = new ArrayList<>();

      public CellData(UUID uuid) {
         this.uuid = uuid;
      }
   }

   private static record CellItemRow(long tplId, long amount) {
   }
}
