package com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage.template;

import com.balugaq.netex.utils.Debug;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage.connection.ConnectionManager;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage.schema.DDLProvider;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage.util.CRC32Utils;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage.util.SerializeUtils;
import io.github.sefiraat.networks.Networks;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemTemplateRegistry {
   private static final Logger LOGGER = Networks.getInstance().getLogger();
   private final ConcurrentHashMap<Long, Long> dataHashToTplId = new ConcurrentHashMap<>();
   private final ConcurrentHashMap<Long, ItemStack> tplIdToItem = new ConcurrentHashMap<>();
   private final ConnectionManager connMgr;
   private final DDLProvider ddl;

   public ItemTemplateRegistry(ConnectionManager connMgr, DDLProvider ddl) {
      this.connMgr = connMgr;
      this.ddl = ddl;
   }

   private static long dataHash(@NotNull String itemData) {
      try {
         MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
         byte[] digest = sha256.digest(itemData.getBytes(StandardCharsets.UTF_8));
         long hash = 0L;

         for (int i = 0; i < 8; i++) {
            hash = hash << 8 | (long)(digest[i] & 255);
         }

         return hash;
      } catch (NoSuchAlgorithmException var6) {
         return (long)CRC32Utils.compute(itemData) & 4294967295L;
      }
   }

   public long getOrRegister(@NotNull ItemStack itemStack) {
      String itemData = SerializeUtils.object2String(itemStack);
      long hash = dataHash(itemData);
      Long cached = this.dataHashToTplId.get(hash);
      if (cached != null) {
         return cached;
      } else {
         Long dbTplId = this.queryTplId(hash, itemData);
         if (dbTplId != null) {
            this.dataHashToTplId.put(hash, dbTplId);
            return dbTplId;
         } else {
            long newTplId = this.insertTemplate(itemData, hash);
            if (newTplId > 0L) {
               this.dataHashToTplId.put(hash, newTplId);
               this.tplIdToItem.put(newTplId, itemStack.clone());
               return newTplId;
            } else {
               dbTplId = this.queryTplId(hash, itemData);
               if (dbTplId != null) {
                  this.dataHashToTplId.put(hash, dbTplId);
                  return dbTplId;
               } else {
                  throw new IllegalStateException(Networks.getLocalizationService().getString("messages.cellnet.persistence.template_register_failed"));
               }
            }
         }
      }
   }

   private long insertTemplate(String itemData, long hash) {
      String sql = this.ddl.insertIgnore() + " INTO item_templates (item_data, item_data_hash, crc32, created_at) VALUES (?, ?, ?, ?)";

      try {
         long rs;
         try (
            Connection conn = this.connMgr.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, 1);
         ) {
            ps.setString(1, itemData);
            ps.setLong(2, hash);
            ps.setInt(3, CRC32Utils.compute(itemData));
            ps.setLong(4, System.currentTimeMillis());
            int affected = ps.executeUpdate();
            if (affected != 0) {
               try (ResultSet rsx = ps.getGeneratedKeys()) {
                  if (rsx.next()) {
                     return rsx.getLong(1);
                  }

                  return -1L;
               }
            }

            rs = -1L;
         }

         return rs;
      } catch (SQLException var17) {
         LOGGER.warning(Networks.getLocalizationService().getString("messages.cellnet.persistence.template_insert_failed", var17.getMessage()));
         Debug.trace(var17);
         return -1L;
      }
   }

   @Nullable
   private Long queryTplId(long hash, String expectedItemData) {
      String sql = "SELECT tpl_id, item_data FROM item_templates WHERE item_data_hash = ?";

      try (
         Connection conn = this.connMgr.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
      ) {
         ps.setLong(1, hash);

         try (ResultSet rs = ps.executeQuery()) {
            return rs.next() && Objects.equals(rs.getString("item_data"), expectedItemData) ? rs.getLong("tpl_id") : null;
         }
      } catch (SQLException var16) {
         LOGGER.warning(Networks.getLocalizationService().getString("messages.cellnet.persistence.template_query_failed", var16.getMessage()));
         Debug.trace(var16);
         return null;
      }
   }

   @Nullable
   public ItemStack resolveItem(long tplId) {
      ItemStack cached = this.tplIdToItem.get(tplId);
      if (cached != null) {
         return cached.clone();
      } else {
         String itemData = this.loadTemplateData(tplId);
         if (itemData == null) {
            return null;
         } else {
            ItemStack item = SerializeUtils.string2Object(itemData);
            if (item != null) {
               this.tplIdToItem.put(tplId, item.clone());
            }

            return item;
         }
      }
   }

   @Nullable
   private String loadTemplateData(long tplId) {
      String sql = "SELECT item_data FROM item_templates WHERE tpl_id = ?";

      try (
         Connection conn = this.connMgr.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
      ) {
         ps.setLong(1, tplId);

         try (ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getString("item_data") : null;
         }
      } catch (SQLException var15) {
         LOGGER.warning(Networks.getLocalizationService().getString("messages.cellnet.persistence.template_load_failed", var15.getMessage()));
         Debug.trace(var15);
         return null;
      }
   }

   public void preloadAll() {
      String sql = "SELECT tpl_id, item_data FROM item_templates";

      try (
         Connection conn = this.connMgr.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql);
      ) {
         while (rs.next()) {
            long tplId = rs.getLong("tpl_id");
            String itemData = rs.getString("item_data");
            ItemStack item = SerializeUtils.string2Object(itemData);
            if (item != null) {
               this.tplIdToItem.put(tplId, item);
               this.dataHashToTplId.put(dataHash(itemData), tplId);
            }
         }
      } catch (SQLException var15) {
         LOGGER.warning(Networks.getLocalizationService().getString("messages.cellnet.persistence.template_preload_failed", var15.getMessage()));
         Debug.trace(var15);
      }
   }
}
