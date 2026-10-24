package com.ytdd9527.networksexpansion.utils.databases;

import com.balugaq.netex.api.data.ItemContainer;
import com.balugaq.netex.api.data.StorageUnitData;
import com.balugaq.netex.api.enums.StorageUnitType;
import com.balugaq.netex.utils.Debug;
import com.balugaq.netex.utils.Lang;
import com.ytdd9527.networksexpansion.implementation.machines.unit.NetworksDrawer;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.thebusybiscuit.slimefun4.utils.itemstack.ItemStackWrapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DataSource {
   private final String ITEM_ID_KEY = "NEXT_ITEM_ID";
   private final String CONTAINER_ID_KEY = "NEXT_CONTAINER_ID";
   @NotNull
   private final Logger logger;
   @NotNull
   private final Map<Integer, ItemStack> itemMap;
   @NotNull
   private final Map<String, String> environment;
   private Connection conn;
   private int nextContainerId = 0;
   private int nextItemId = 0;

   public DataSource() throws ClassNotFoundException, SQLException {
      this.connect();
      this.createTable();
      this.logger = Networks.getInstance().getLogger();
      this.itemMap = new HashMap<>();
      this.loadItemMap();
      this.environment = new HashMap<>();
      this.loadEnvironment();
      this.init();
   }

   @NotNull
   public static String getBase64String(ItemStack item) throws IOException {
      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      BukkitObjectOutputStream bs = new BukkitObjectOutputStream(stream);
      bs.writeObject(item);
      bs.close();
      return Base64.getEncoder().encodeToString(stream.toByteArray());
   }

   public static ItemStack getItemStack(@NotNull String base64Str) throws IOException, ClassNotFoundException {
      ByteArrayInputStream stream = new ByteArrayInputStream(Base64.getMimeDecoder().decode(base64Str));
      BukkitObjectInputStream bs = new BukkitObjectInputStream(stream);
      ItemStack re = (ItemStack)bs.readObject();
      bs.close();
      return re;
   }

   void saveNewStorageData(@NotNull StorageUnitData storageData) {
      String sql = "INSERT INTO Container VALUES("
         + storageData.getId()
         + ",'"
         + storageData.getOwner().getUniqueId()
         + "',"
         + storageData.getSizeType().ordinal()
         + ","
         + (storageData.isPlaced() ? 1 : 0)
         + ",'"
         + DataStorage.formatLocation(storageData.getLastLocation())
         + "');";
      this.scheduleExecute(sql, Lang.getString("messages.data-saving.error-occurred-when-saving-new-data"));
   }

   int getNextContainerId() {
      int re = this.nextContainerId++;
      String sql = "UPDATE Environment SET VarValue = " + this.nextContainerId + " WHERE VarName = 'NEXT_CONTAINER_ID';";
      if (!this.environment.containsKey("NEXT_CONTAINER_ID")) {
         sql = "INSERT INTO Environment VALUES ('NEXT_CONTAINER_ID'," + this.nextContainerId + ");";
         this.environment.put("NEXT_CONTAINER_ID", this.nextContainerId + "");
      }

      this.scheduleExecute(sql, Lang.getString("messages.data-saving.error-occurred-when-updating-environment-var"));
      return re;
   }

   @Nullable
   public StorageUnitData getStorageData(int id) {
      StorageUnitData re = null;
      String sql = "SELECT * FROM Container WHERE ContainerID = " + id + ";";

      try (
         Statement stat = this.conn.createStatement();
         ResultSet result = stat.executeQuery(sql);
      ) {
         if (result.next()) {
            String[] locStr = result.getString("LastLocation").split(";");
            Location l = null;
            if (locStr.length == 4) {
               World w = Bukkit.getWorld(UUID.fromString(locStr[0]));
               if (w != null) {
                  l = new Location(w, (double)Integer.parseInt(locStr[1]), (double)Integer.parseInt(locStr[2]), (double)Integer.parseInt(locStr[3]));
               }
            }

            re = new StorageUnitData(
               result.getInt("ContainerID"),
               result.getString("PlayerUUID"),
               StorageUnitType.values()[result.getInt("SizeType") % 13],
               result.getBoolean("IsPlaced"),
               l,
               this.getStoredItem(id)
            );
         }
      } catch (SQLException var13) {
         this.logger.warning(Lang.getString("messages.data-saving.error-occurred-when-loading-data"));
         Debug.trace(var13);
      }

      return re;
   }

   int getItemId(@NotNull ItemStack item) {
      ItemStack clone = item.clone();
      ItemStackWrapper wrapper = ItemStackWrapper.wrap(item);

      for (Entry<Integer, ItemStack> each : this.itemMap.entrySet()) {
         if (StackUtils.itemsMatch(each.getValue(), wrapper)) {
            return each.getKey();
         }
      }

      int re = this.nextItemId++;
      Networks.getQueryQueue().scheduleUpdate(() -> {
         try {
            boolean var5x;
            try (Statement stat = this.conn.createStatement()) {
               String sql = "UPDATE Environment SET VarValue = " + this.nextItemId + " WHERE VarName = 'NEXT_ITEM_ID';";
               if (!this.environment.containsKey("NEXT_ITEM_ID")) {
                  sql = "INSERT INTO Environment VALUES ('NEXT_ITEM_ID'," + this.nextItemId + ");";
                  this.environment.put("NEXT_ITEM_ID", this.nextItemId + "");
               }

               stat.execute(sql);
               stat.execute("INSERT INTO ItemStack VALUES (" + re + ",'" + getBase64String(clone) + "');");
               var5x = true;
            }

            return var5x;
         } catch (IOException | SQLException var8) {
            this.logger.warning(Lang.getString("messages.data-saving.error-occurred-when-saving-itemstack"));
            Debug.trace(var8);
            return false;
         }
      });
      return re;
   }

   void updateContainer(int id, String key, String value) {
      String sql = "UPDATE Container SET " + key + " = '" + value + "' WHERE ContainerID = " + id + ";";
      this.scheduleExecute(sql, Lang.getString("messages.data-saving.error-occurred-when-updating-container-data"));
   }

   void addStoredItem(int containerId, int itemId, int amount) {
      if (amount > 0) {
         String sql = "INSERT INTO ItemStored VALUES(" + containerId + "," + itemId + "," + amount + ");";
         this.scheduleExecute(sql, Lang.getString("messages.data-saving.error-occurred-when-updating-storage"));
      }
   }

   void updateItemAmount(int containerId, int itemId, int amount) {
      if (NetworksDrawer.isLocked(containerId)) {
         if (amount < 0) {
            this.deleteStoredItem(containerId, itemId);
            return;
         }
      } else if (amount <= 0) {
         this.deleteStoredItem(containerId, itemId);
         return;
      }

      String sql = "UPDATE ItemStored SET Amount = " + amount + " WHERE ContainerID = " + containerId + " AND ItemID = " + itemId + ";";
      this.scheduleExecute(sql, Lang.getString("messages.data-saving.error-occurred-when-updating-storage"));
   }

   void deleteStoredItem(int containerId, int itemId) {
      String sql = "DELETE FROM ItemStored WHERE ContainerID = " + containerId + " AND ItemID = " + itemId + ";";
      this.scheduleExecute(sql, Lang.getString("messages.data-saving.error-occurred-when-updating-storage"));
   }

   int getIdFromLocation(@NotNull Location l) {
      String sql = "SELECT ContainerID FROM Container WHERE IsPlaced = 1 AND LastLocation = '" + DataStorage.formatLocation(l) + "';";

      try {
         int var5;
         try (
            Statement stat = this.conn.createStatement();
            ResultSet resultSet = stat.executeQuery(sql);
         ) {
            if (!resultSet.next()) {
               return -1;
            }

            var5 = resultSet.getInt(1);
         }

         return var5;
      } catch (SQLException var11) {
         this.logger.warning(Lang.getString("messages.data-saving.error-occurred-when-fixing-data"));
         Debug.trace(var11);
         return -1;
      }
   }

   private void connect() throws SQLException, ClassNotFoundException {
      File dataFolder = Networks.getInstance().getDataFolder();
      if ((!dataFolder.exists() || !dataFolder.isDirectory()) && !dataFolder.mkdir()) {
         throw new IllegalStateException(Lang.getString("messages.data-saving.error-occurred-when-creating-data-folder"));
      } else {
         Class.forName("org.sqlite.JDBC");
         this.conn = DriverManager.getConnection("jdbc:sqlite:" + dataFolder.getPath() + "/CargoStorageUnits.db");
      }
   }

   private void createTable() throws SQLException {
      try (Statement stat = this.conn.createStatement()) {
         stat.execute(
            "CREATE TABLE IF NOT EXISTS Container (ContainerID CHAR(8) PRIMARY KEY NOT NULL,PlayerUUID CHAR(36) NOT NULL,SizeType TINYINT NOT NULL,IsPlaced TINYINT NOT NULL,LastLocation VARCHAR(64) NOT NULL);"
         );
         stat.execute("CREATE TABLE IF NOT EXISTS ItemStack (ItemID CHAR(8) PRIMARY KEY NOT NULL,Item TEXT NOT NULL);");
         stat.execute("CREATE TABLE IF NOT EXISTS ItemStored (ContainerID CHAR(8) NOT NULL,ItemID CHAR(8) NOT NULL,Amount CHAR(8) NOT NULL);");
         stat.execute("CREATE TABLE IF NOT EXISTS Environment (VarName CHAR(8) PRIMARY KEY NOT NULL,VarValue CHAR(128) NOT NULL);");
      }
   }

   private void loadItemMap() {
      String sql = "SELECT Item, ItemID FROM ItemStack;";
      this.executeQuery(sql, result -> {
         try {
            while (result.next()) {
               this.itemMap.put(result.getInt("ItemID"), getItemStack(result.getString("Item")));
            }
         } catch (IOException | ClassNotFoundException | SQLException var3) {
            this.logger.warning(Lang.getString("messages.data-saving.error-occurred-when-loading-itemstack"));
            Debug.trace(var3);
         }
      });
   }

   private void loadEnvironment() {
      String sql = "SELECT VarName, VarValue FROM Environment;";
      this.executeQuery(sql, result -> {
         try {
            while (result.next()) {
               this.environment.put(result.getString(1), result.getString(2));
            }
         } catch (SQLException var3) {
            this.logger.warning(Lang.getString("messages.data-saving.error-occurred-when-loading-environment-var"));
            Debug.trace(var3);
         }
      });
   }

   private void init() {
      String temp = this.environment.get("NEXT_ITEM_ID");
      if (temp != null) {
         this.nextItemId = Integer.parseInt(temp);
      }

      temp = this.environment.get("NEXT_CONTAINER_ID");
      if (temp != null) {
         this.nextContainerId = Integer.parseInt(temp);
      }
   }

   @NotNull
   private ConcurrentHashMap<Integer, ItemContainer> getStoredItem(final int id) {
      final ConcurrentHashMap<Integer, ItemContainer> re = new ConcurrentHashMap<>();
      Networks.getQueryQueue().scheduleQuery(new QueuedTask() {
         private boolean success = true;

         @Override
         public boolean execute() {
            String sql = "SELECT ItemID, Amount FROM ItemStored WHERE ContainerID = " + id + ";";
            DataSource.this.executeQuery(sql, result -> {
               try {
                  while (result.next()) {
                     int itemId = result.getInt("ItemID");
                     ItemStack item = DataSource.this.itemMap.get(itemId);
                     if (item != null) {
                        re.put(itemId, new ItemContainer(itemId, item, result.getInt("Amount")));
                     }
                  }
               } catch (SQLException var5) {
                  this.success = false;
                  DataSource.this.logger.warning(Lang.getString("messages.data-saving.error-occurred-when-loading-storage"));
                  Debug.trace(var5);
               }
            });
            return this.success;
         }

         @Override
         public boolean callback() {
            DataStorage.setContainerLoaded(id);
            return false;
         }
      });
      return re;
   }

   private void executeQuery(String sql, @NotNull Consumer<ResultSet> usage) {
      try (
         Statement stat = this.conn.createStatement();
         ResultSet result = stat.executeQuery(sql);
      ) {
         usage.accept(result);
      } catch (SQLException var11) {
         this.logger.warning(Lang.getString("messages.data-saving.error-occurred-when-executing-query"));
         Debug.trace(var11);
      }
   }

   private void scheduleExecute(String sql, String errorMsg) {
      Networks.getQueryQueue().scheduleUpdate(() -> {
         try {
            boolean var4;
            try (Statement stat = this.conn.createStatement()) {
               stat.execute(sql);
               var4 = true;
            }

            return var4;
         } catch (SQLException var8) {
            this.logger.warning(errorMsg);
            Debug.trace(var8);
            return false;
         }
      });
   }
}
