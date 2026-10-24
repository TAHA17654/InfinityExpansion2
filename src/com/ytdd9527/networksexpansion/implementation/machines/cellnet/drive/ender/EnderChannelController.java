package com.ytdd9527.networksexpansion.implementation.machines.cellnet.drive.ender;

import com.balugaq.netex.utils.Debug;
import com.ytdd9527.networksexpansion.core.services.LocalizationService;
import io.github.sefiraat.networks.Networks;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class EnderChannelController {
   private static final String TABLE_CREATE_SQL = "CREATE TABLE IF NOT EXISTS ender_channels (channel TEXT NOT NULL, cell_uuid CHAR(36) NOT NULL, PRIMARY KEY (channel, cell_uuid))";
   private static final String INDEX_CELL_UNIQUE_SQL = "CREATE UNIQUE INDEX IF NOT EXISTS idx_enders_cell_unique ON ender_channels(cell_uuid)";
   private static volatile EnderChannelController instance;
   private final AtomicBoolean tableReady = new AtomicBoolean();
   @NotNull
   private final File dbFile;

   public EnderChannelController(@NotNull File dbFile) {
      this.dbFile = dbFile;
      File parent = dbFile.getParentFile();
      if (parent != null && !parent.exists()) {
         parent.mkdirs();
      }
   }

   @NotNull
   public static EnderChannelController getInstance() {
      if (instance == null) {
         synchronized (EnderChannelController.class) {
            if (instance == null) {
               File file = new File(new File(Networks.getInstance().getDataFolder(), "data"), "ender_channels.db");
               instance = new EnderChannelController(file);
            }
         }
      }

      return instance;
   }

   public boolean bind(@NotNull String channelName, @NotNull String cellUuid) {
      String existing = this.getChannelOfCell(cellUuid);
      if (channelName.equals(existing)) {
         return true;
      } else {
         return !this.execute("DELETE FROM ender_channels WHERE cell_uuid = ?", cellUuid)
            ? false
            : this.execute("INSERT INTO ender_channels (channel, cell_uuid) VALUES (?, ?)", channelName, cellUuid);
      }
   }

   public boolean unbindCell(@NotNull String cellUuid) {
      return this.execute("DELETE FROM ender_channels WHERE cell_uuid = ?", cellUuid);
   }

   @Nullable
   public String getChannelOfCell(@NotNull String cellUuid) {
      return this.querySingle("SELECT channel FROM ender_channels WHERE cell_uuid = ? LIMIT 1", rs -> rs.getString(1), cellUuid);
   }

   @NotNull
   public List<String> getCellUuidsOfChannel(@NotNull String channelName) {
      List<String> result = new ArrayList<>();
      this.queryEach("SELECT cell_uuid FROM ender_channels WHERE channel = ? ORDER BY rowid", rs -> result.add(rs.getString(1)), channelName);
      return result;
   }

   public void flushAndClose() {
      if (this.tableReady.get()) {
         try (
            Connection conn = this.getConnection();
            PreparedStatement ps = conn.prepareStatement("PRAGMA wal_checkpoint(TRUNCATE)");
         ) {
            ps.execute();
         } catch (SQLException var9) {
            Debug.trace(var9, "EnderChannelController failed to flush data on shutdown");
         }
      }
   }

   @NotNull
   private Connection getConnection() throws SQLException {
      this.ensureTable();
      Connection conn = DriverManager.getConnection("jdbc:sqlite:" + this.dbFile.getAbsolutePath());

      try (Statement stat = conn.createStatement()) {
         stat.execute("PRAGMA busy_timeout=5000");
      } catch (SQLException var7) {
         Networks.getInstance().getLogger().warning("Failed to set ender_channels connection PRAGMA");
         Debug.trace(var7, "Failed to set ender_channels connection PRAGMA");
      }

      return conn;
   }

   @NotNull
   private static String message(@NotNull String key, @NotNull String fallback) {
      try {
         LocalizationService service = Networks.getLocalizationService();
         if (service != null) {
            return service.getString(key);
         }
      } catch (Throwable var3) {
         Debug.trace(var3, "EnderChannelController failed to read text: " + key);
      }

      return fallback;
   }

   private void ensureTable() throws SQLException {
      if (!this.tableReady.get()) {
         try (
            Connection conn = DriverManager.getConnection("jdbc:sqlite:" + this.dbFile.getAbsolutePath());
            Statement stat = conn.createStatement();
         ) {
            stat.execute("CREATE TABLE IF NOT EXISTS ender_channels (channel TEXT NOT NULL, cell_uuid CHAR(36) NOT NULL, PRIMARY KEY (channel, cell_uuid))");
            stat.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_enders_cell_unique ON ender_channels(cell_uuid)");
         } catch (SQLException var9) {
            Networks.getInstance().getLogger().warning(message("messages.cellnet.ender.table_create_failed", "Failed to create ender channel database tables"));
            throw var9;
         }

         this.tableReady.set(true);
      }
   }

   private boolean execute(@NotNull String sql, @NotNull Object... args) {
      try {
         boolean var12;
         try (
            Connection conn = this.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
         ) {
            for (int i = 0; i < args.length; i++) {
               ps.setObject(i + 1, args[i]);
            }

            ps.executeUpdate();
            var12 = true;
         }

         return var12;
      } catch (SQLException var11) {
         Networks.getInstance()
            .getLogger()
            .warning(message("messages.cellnet.ender.write_failed", "Failed to write ender channel data: {0}").replace("{0}", var11.getMessage()));
         Debug.trace(var11, "EnderChannelController write failed: " + sql);
         return false;
      }
   }

   @Nullable
   private <T> T querySingle(@NotNull String sql, @NotNull EnderChannelController.RowMapper<T> mapper, @NotNull Object... args) {
      try {
         Object var7;
         try (
            Connection conn = this.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
         ) {
            for (int i = 0; i < args.length; i++) {
               ps.setObject(i + 1, args[i]);
            }

            try (ResultSet rs = ps.executeQuery()) {
               if (rs.next()) {
                  return mapper.map(rs);
               }

               var7 = null;
            }
         }

         return (T)var7;
      } catch (SQLException var15) {
         Networks.getInstance()
            .getLogger()
            .warning(message("messages.cellnet.ender.read_failed", "Failed to read ender channel data: {0}").replace("{0}", var15.getMessage()));
         Debug.trace(var15, "EnderChannelController query failed: " + sql);
         return null;
      }
   }

   private void queryEach(@NotNull String sql, @NotNull EnderChannelController.RowConsumer consumer, @NotNull Object... args) {
      try (
         Connection conn = this.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
      ) {
         for (int i = 0; i < args.length; i++) {
            ps.setObject(i + 1, args[i]);
         }

         try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
               consumer.accept(rs);
            }
         }
      } catch (SQLException var15) {
         Networks.getInstance()
            .getLogger()
            .warning(message("messages.cellnet.ender.read_failed", "Failed to read ender channel data: {0}").replace("{0}", var15.getMessage()));
         Debug.trace(var15, "EnderChannelController iteration query failed: " + sql);
      }
   }

   private interface RowConsumer {
      void accept(ResultSet var1) throws SQLException;
   }

   private interface RowMapper<T> {
      T map(ResultSet var1) throws SQLException;
   }
}
