package com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage.schema;

import com.balugaq.netex.utils.Debug;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage.connection.ConnectionManager;
import io.github.sefiraat.networks.Networks;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class SchemaManager {
   private static final Logger LOGGER = Networks.getInstance().getLogger();
   private final ConnectionManager connMgr;
   private final DDLProvider ddl = new DDLProvider();

   public SchemaManager(ConnectionManager connMgr) {
      this.connMgr = connMgr;
   }

   public DDLProvider getDDL() {
      return this.ddl;
   }

   public void initSchema() {
      try {
         try (Connection conn = this.connMgr.getConnection()) {
            this.exec(conn, this.ddl.createSchemaInfoTable());
            this.initItemTemplatesTable(conn);
            this.exec(conn, this.ddl.createCellMetaTable());
            this.exec(conn, this.ddl.createCellItemsTable());
            this.exec(conn, this.ddl.createJournalTable());
            this.exec(conn, this.ddl.createJournalIndex());
            this.exec(conn, this.ddl.createJournalArchiveTable());
            this.exec(conn, this.ddl.createArchiveIndexes());
            this.exec(conn, this.ddl.createArchiveTimestampIndex());

            try (
               Statement stmt = conn.createStatement();
               ResultSet rs = stmt.executeQuery("SELECT config_value FROM schema_info WHERE config_key = 'created_at'");
            ) {
               if (!rs.next()) {
                  long now = System.currentTimeMillis();
                  this.exec(conn, "INSERT INTO schema_info (config_key, config_value) VALUES ('created_at', '" + now + "')");
                  this.exec(conn, "INSERT INTO schema_info (config_key, config_value) VALUES ('last_migrated', '" + now + "')");
               }
            }
         }
      } catch (SQLException var12) {
         LOGGER.warning(Networks.getLocalizationService().getString("messages.cellnet.persistence.schema_init_failed", var12.getMessage()));
         Debug.trace(var12);
         throw new RuntimeException("Schema initialization failed", var12);
      }
   }

   private void initItemTemplatesTable(Connection conn) throws SQLException {
      this.exec(conn, this.ddl.createItemTemplatesTable());
      this.exec(conn, this.ddl.createItemTemplatesDedup());
   }

   public void markSchemaVersion() {
      try (Connection conn = this.connMgr.getConnection()) {
         this.exec(conn, "INSERT OR REPLACE INTO schema_info (config_key, config_value) VALUES ('schema_version', '1')");
      } catch (SQLException var6) {
         LOGGER.warning(Networks.getLocalizationService().getString("messages.cellnet.persistence.schema_mark_version_failed", var6.getMessage()));
         Debug.trace(var6);
      }
   }

   private void exec(Connection conn, String sql) throws SQLException {
      try (Statement stmt = conn.createStatement()) {
         stmt.execute(sql);
      }
   }
}
