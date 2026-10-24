package com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage.connection;

import com.balugaq.netex.utils.Debug;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage.StorageConfig;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnectionManager {
   private final String jdbcUrl;
   private final StorageConfig config;

   public ConnectionManager(StorageConfig config) {
      this.config = config;
      File file = config.getSqliteFile();
      this.jdbcUrl = "jdbc:sqlite:" + file.getAbsolutePath();
      if (config.isWalMode()) {
         try (
            Connection conn = DriverManager.getConnection(this.jdbcUrl);
            Statement stat = conn.createStatement();
         ) {
            stat.execute("PRAGMA journal_mode=WAL");
         } catch (SQLException var11) {
            Debug.trace(var11, "Failed to set WAL mode");
         }
      }
   }

   public Connection getConnection() throws SQLException {
      Connection conn = DriverManager.getConnection(this.jdbcUrl);

      try (Statement stat = conn.createStatement()) {
         stat.execute("PRAGMA synchronous=NORMAL");
         stat.execute("PRAGMA busy_timeout=" + this.config.getBusyTimeout());
         stat.execute("PRAGMA foreign_keys=ON");
         if (this.config.isWalMode()) {
            stat.execute("PRAGMA journal_mode=WAL");
         }
      } catch (SQLException var7) {
         Debug.trace(var7, "Failed to set connection-level PRAGMA");
      }

      return conn;
   }

   public void shutdown() {
   }
}
