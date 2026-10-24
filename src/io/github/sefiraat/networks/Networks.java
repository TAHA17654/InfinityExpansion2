package io.github.sefiraat.networks;

import com.balugaq.bim.BIMLoader;
import com.balugaq.netex.api.algorithm.ID;
import com.balugaq.netex.api.data.ItemFlowRecord;
import com.balugaq.netex.api.enums.MinecraftVersion;
import com.balugaq.netex.api.keybind.Keybinds;
import com.balugaq.netex.core.guide.GridNewStyleCustomAmountGuideOption;
import com.balugaq.netex.libraries.libby.BukkitLibraryManager;
import com.balugaq.netex.libraries.libby.Library;
import com.balugaq.netex.libraries.libby.LibraryManager;
import com.balugaq.netex.utils.Debug;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.core.managers.ConfigManager;
import com.ytdd9527.networksexpansion.core.services.LocalizationService;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.drive.CellDrive;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage.CellStorageDatabase;
import com.ytdd9527.networksexpansion.setup.SetupUtil;
import com.ytdd9527.networksexpansion.utils.databases.DataSource;
import com.ytdd9527.networksexpansion.utils.databases.DataStorage;
import com.ytdd9527.networksexpansion.utils.databases.QueryQueue;
import io.github.sefiraat.networks.bstats.bukkit.Metrics;
import io.github.sefiraat.networks.bstats.charts.AdvancedPie;
import io.github.sefiraat.networks.commands.NetworksMain;
import io.github.sefiraat.networks.integrations.HudCallbacks;
import io.github.sefiraat.networks.integrations.NetheoPlants;
import io.github.sefiraat.networks.managers.ListenerManager;
import io.github.sefiraat.networks.managers.SupportedPluginManager;
import io.github.sefiraat.networks.slimefun.NetworksSlimefunItemStacks;
import io.github.sefiraat.networks.slimefun.network.AdminDebuggable;
import io.github.sefiraat.networks.slimefun.network.NetworkController;
import io.github.sefiraat.networks.utils.NetworkUtils;
import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.core.guide.options.SlimefunGuideSettings;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.paperlib.PaperLib;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import lombok.Generated;
import net.guizhanss.guizhanlibplugin.updater.GuizhanUpdater;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.error.YAMLException;

public class Networks extends JavaPlugin implements SlimefunAddon {
   private static final String DEFAULT_LANGUAGE = "zh-CN";
   private static Networks instance;
   private static DataSource dataSource;
   private static QueryQueue queryQueue;
   private static BukkitRunnable autoSaveThread;
   private static BukkitRunnable aeAutoSaveThread;
   private static CellStorageDatabase cellStorageDatabase;
   private static MinecraftVersion minecraftVersion = MinecraftVersion.UNKNOWN;
   @NotNull
   private final String username = "ytdd9527";
   @NotNull
   private final String repo = "NetworksExpansion";
   @NotNull
   private final String branch = "master";
   private ConfigManager configManager;
   private ListenerManager listenerManager;
   private SupportedPluginManager supportedPluginManager;
   private LocalizationService localizationService;
   private BIMLoader bimLoader;
   private long slimefunTickCount;

   public static ConfigManager getConfigManager() {
      return getInstance().configManager;
   }

   public static Networks getInstance() {
      return instance;
   }

   @NotNull
   public static PluginManager getPluginManager() {
      return getInstance().getServer().getPluginManager();
   }

   public static SupportedPluginManager getSupportedPluginManager() {
      return getInstance().supportedPluginManager;
   }

   public static LocalizationService getLocalizationService() {
      return getInstance().localizationService;
   }

   public static ListenerManager getListenerManager() {
      return getInstance().listenerManager;
   }

   public static BIMLoader getBIMLoader() {
      return getInstance().bimLoader;
   }

   public static long getSlimefunTickCount() {
      return getInstance().slimefunTickCount;
   }

   public void onEnable() {
      instance = this;
      this.loadLibraries();
      this.getLogger().info("Loading language");
      this.configManager = new ConfigManager();
      this.localizationService = new LocalizationService(this);
      String language = this.configManager.getLanguage();

      try {
         this.localizationService.addLanguage(language);
         this.getLogger().info("Language " + language + " loaded successfully.");
      } catch (IllegalArgumentException | YAMLException | ClassCastException var8) {
         this.getLogger().log(Level.WARNING, "Failed to load language " + language, (Throwable)var8);
      }

      this.localizationService.addDefaultLanguage("zh-CN");
      this.getLogger().info("Default language zh-CN loaded successfully.");
      this.superHead();
      this.environmentCheck();
      this.getLogger().info(getLocalizationService().getString("messages.startup.loaded-language"));
      this.getLogger().info(getLocalizationService().getString("messages.startup.getting-config"));
      this.saveDefaultConfig();
      this.getLogger().info(getLocalizationService().getString("messages.startup.trying-auto-update"));
      this.tryUpdate();
      this.bimLoader = new BIMLoader(this);
      this.supportedPluginManager = new SupportedPluginManager();
      this.getLogger().info(getLocalizationService().getString("messages.startup.connecting-database"));

      try {
         dataSource = new DataSource();
      } catch (SQLException | ClassNotFoundException var7) {
         this.getLogger().warning(getLocalizationService().getString("messages.startup.failed-to-connect-database"));
         Debug.trace(var7);
         this.onDisable();
      }

      this.getLogger().info(getLocalizationService().getString("messages.startup.creating-query-queue"));
      queryQueue = new QueryQueue();
      queryQueue.startThread();
      this.getLogger().info(getLocalizationService().getString("messages.startup.creating-auto-save-thread"));
      autoSaveThread = new BukkitRunnable() {
         public void run() {
            DataStorage.saveAmountChange();
         }
      };
      int seconds = this.getConfig().getInt("drawer-auto-save-period");
      seconds = seconds <= 0 ? 300 : seconds;
      long period = 20L * (long)seconds;
      autoSaveThread.runTaskTimerAsynchronously(this, 2L * period, period);
      aeAutoSaveThread = new BukkitRunnable() {
         public void run() {
            CellDrive.saveAllDriveCells();
         }
      };
      aeAutoSaveThread.runTaskTimerAsynchronously(this, 2L * period, period);
      this.getLogger().info(getLocalizationService().getString("messages.startup.initializing-cell-database"));

      try {
         cellStorageDatabase = new CellStorageDatabase();
         cellStorageDatabase.init();
      } catch (Exception var6) {
         cellStorageDatabase = null;
         this.getLogger().warning(getLocalizationService().getString("messages.startup.failed-to-init-cell-database"));
         Debug.trace(var6);
      }

      this.getLogger().info(getLocalizationService().getString("messages.startup.registering-items"));
      SetupUtil.setupAll();
      this.getLogger().info(getLocalizationService().getString("messages.startup.registering-listeners"));
      this.listenerManager = new ListenerManager();
      this.getLogger().info(getLocalizationService().getString("messages.startup.registering-commands"));
      PluginCommand c = this.getCommand("networks");
      if (c != null) {
         c.setExecutor(new NetworksMain());
      }

      this.setupMetrics();
      Bukkit.getScheduler().runTaskTimer(this, () -> this.slimefunTickCount++, 1L, (long)Slimefun.getTickerTask().getTickRate());
      Bukkit.getScheduler().runTaskTimer(this, () -> {
         Set<Location> wrongs = new HashSet<>();

         for (Location controller : new HashSet<>(NetworkController.getNetworks().keySet())) {
            SlimefunBlockData data = StorageCacheUtils.getBlock(controller);
            if (data == null || !NetworksSlimefunItemStacks.NETWORK_CONTROLLER.getItemId().equals(data.getSfId())) {
               wrongs.add(controller);
            }
         }

         for (Location wrong : wrongs) {
            NetworkUtils.clearNetwork(wrong);
         }
      }, 1L, (long)Slimefun.getTickerTask().getTickRate());
      Bukkit.getScheduler()
         .runTaskTimer(this, () -> NetworkController.getRecords().values().forEach(ItemFlowRecord::gc), 1L, (long)Slimefun.getTickerTask().getTickRate());
      AdminDebuggable.load();
      SlimefunGuideSettings.addOption(GridNewStyleCustomAmountGuideOption.instance());
      Bukkit.getScheduler().runTaskLaterAsynchronously(this, Keybinds::distinctAll, 1L);
      ID.fetchId();
      Keybinds.fetchScripts();
      this.getLogger().info(getLocalizationService().getString("messages.startup.enabled-successfully"));
   }

   public void onDisable() {
      try {
         this.getLogger().info(getLocalizationService().getString("messages.shutdown.saving-config"));
         ID.saveId();
         this.configManager.saveAll();
         this.getLogger().info(getLocalizationService().getString("messages.shutdown.disconnecting-database"));
      } catch (Exception var3) {
         Debug.trace(var3);
      }

      if (autoSaveThread != null) {
         autoSaveThread.cancel();
      }

      if (aeAutoSaveThread != null) {
         aeAutoSaveThread.cancel();
      }

      DataStorage.saveAmountChange();
      CellDrive.saveAllDriveCells();
      if (cellStorageDatabase != null) {
         cellStorageDatabase.shutdown();
      }

      if (queryQueue != null) {
         while (!queryQueue.isAllDone()) {
            this.getLogger().info(String.format(getLocalizationService().getString("messages.shutdown.saving-data"), queryQueue.getTaskAmount()));

            try {
               Thread.sleep(1000L);
            } catch (InterruptedException var2) {
               Debug.trace(var2);
            }
         }

         queryQueue.scheduleAbort();
      }

      this.getLogger().info(getLocalizationService().getString("messages.shutdown.saved-all-data"));
      this.getLogger().info(getLocalizationService().getString("messages.shutdown.disabled-successfully"));
   }

   public void tryUpdate() {
      if (this.configManager.isAutoUpdate() && this.getDescription().getVersion().startsWith("Build")) {
         GuizhanUpdater.start(this, this.getFile(), this.username, this.repo, this.branch);
      }
   }

   public void superHead() {
      for (String line : getLocalizationService().getStringList("messages.super-head")) {
         this.getLogger().info(line);
      }
   }

   public void environmentCheck() {
      if (!this.getServer().getPluginManager().isPluginEnabled("GuizhanLibPlugin")) {
         this.getLogger().log(Level.SEVERE, getLocalizationService().getString("messages.depend.not-found-guizhanlib"));
         this.getLogger().log(Level.SEVERE, getLocalizationService().getString("messages.depend.suggest-download-guizhanlib"));
      } else {
         try {
            minecraftVersion = MinecraftVersion.current();
         } catch (NoSuchFieldError | NoClassDefFoundError var3) {
            for (int i = 0; i < 20; i++) {
               this.getLogger().severe(getLocalizationService().getString("messages.depend.suggest-download-newer-slimefun"));
            }
         }

         if (minecraftVersion == MinecraftVersion.UNKNOWN) {
            int major = PaperLib.getMinecraftVersion();
            int minor = PaperLib.getMinecraftPatchVersion();
            minecraftVersion = MinecraftVersion.of(major, minor);
         }
      }
   }

   public void setupIntegrations() {
      if (this.supportedPluginManager.isSlimeHud()) {
         this.getLogger().info(getLocalizationService().getString("messages.integrations.found-slimehud"));

         try {
            HudCallbacks.setup();
         } catch (NoClassDefFoundError var3) {
            this.getLogger().warning(getLocalizationService().getString("messages.integrations.not-found-slimehud"));
         }
      }

      if (this.supportedPluginManager.isNetheopoiesis()) {
         this.getLogger().info(getLocalizationService().getString("messages.integrations.found-netheopoiesis"));

         try {
            NetheoPlants.setup();
         } catch (NoClassDefFoundError var2) {
            this.getLogger().warning(getLocalizationService().getString("messages.integrations.not-found-netheopoiesis"));
         }
      }
   }

   private void loadLibraries() {
      LibraryManager libraryManager = new BukkitLibraryManager(this);
      libraryManager.addMavenCentral();
      this.getLogger().info("Loading Pinyin");
      Library pinyin = Library.builder().groupId("com{}github{}houbb").artifactId("pinyin").version("0.4.0").build();
      libraryManager.loadLibrary(pinyin);
      this.getLogger().info("Loading opencc4j");
      Library opencc4j = Library.builder().groupId("com{}github{}houbb").artifactId("opencc4j").version("1.14.0").build();
      libraryManager.loadLibrary(opencc4j);
      this.getLogger().info("Loading heaven");
      Library heaven = Library.builder().groupId("com{}github{}houbb").artifactId("heaven").version("0.13.0").build();
      libraryManager.loadLibrary(heaven);
      this.getLogger().info("Loading nlp-common");
      Library nlp = Library.builder().groupId("com{}github{}houbb").artifactId("nlp-common").version("0.0.5").build();
      libraryManager.loadLibrary(nlp);
   }

   public MinecraftVersion getMCVersion() {
      return minecraftVersion;
   }

   public void setupMetrics() {
      Metrics metrics = new Metrics(this, 34492);
      AdvancedPie networksChart = new AdvancedPie("networks", () -> {
         Map<String, Integer> networksMap = new HashMap<>();
         networksMap.put("Number of networks", NetworkController.getNetworks().size());
         return networksMap;
      });
      metrics.addCustomChart(networksChart);
   }

   @NotNull
   public JavaPlugin getJavaPlugin() {
      return this;
   }

   @Nullable
   public String getBugTrackerURL() {
      return MessageFormat.format("https://github.com/{0}/{1}/issues/", this.username, this.repo);
   }

   @NotNull
   public String getWikiURL() {
      return MessageFormat.format("https://slimefun-addons-wiki.guizhanss.cn/networks/{0}/{1}", this.username, this.repo);
   }

   public void debug(String message) {
      if (getConfigManager().isDebug()) {
         this.getLogger().warning("[DEBUG] " + message);
      }
   }

   @Generated
   public static DataSource getDataSource() {
      return dataSource;
   }

   @Generated
   public static QueryQueue getQueryQueue() {
      return queryQueue;
   }

   @Generated
   public static BukkitRunnable getAutoSaveThread() {
      return autoSaveThread;
   }

   @Generated
   public static BukkitRunnable getAeAutoSaveThread() {
      return aeAutoSaveThread;
   }

   @Generated
   public static CellStorageDatabase getCellStorageDatabase() {
      return cellStorageDatabase;
   }
}
