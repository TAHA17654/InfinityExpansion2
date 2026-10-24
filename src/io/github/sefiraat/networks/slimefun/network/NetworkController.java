package io.github.sefiraat.networks.slimefun.network;

import com.balugaq.netex.api.data.ItemFlowRecord;
import com.balugaq.netex.api.events.NetworkRootReadyEvent;
import com.balugaq.netex.utils.Lang;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.network.NetworkNode;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.NodeDefinition;
import io.github.sefiraat.networks.network.NodeType;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemHandler;
import io.github.thebusybiscuit.slimefun4.api.items.ItemSetting;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.items.settings.IntRangeSetting;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Generated;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class NetworkController extends NetworkObject {
   private static final Map<Location, ItemFlowRecord> records = new ConcurrentHashMap<>();
   private static final Map<Location, Boolean> recordFlow = new ConcurrentHashMap<>();
   private static final String CRAYON = "crayon";
   private static final Map<Location, NetworkRoot> NETWORKS = new ConcurrentHashMap<>();
   private static final Set<Location> CRAYONS = ConcurrentHashMap.newKeySet();
   protected final Map<Location, Boolean> firstTickMap = new ConcurrentHashMap<>();
   @NotNull
   private final ItemSetting<Integer> maxNodes = new IntRangeSetting(this, "max_nodes", 10, 8000, 50000);

   public NetworkController(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe, NodeType.CONTROLLER);
      this.addItemSetting(new ItemSetting[]{this.maxNodes});
      this.addItemHandler(
         new ItemHandler[]{
            new BlockTicker() {
               public boolean isSynchronized() {
                  return false;
               }

               public void tick(@NotNull Block block, SlimefunItem item, @NotNull SlimefunBlockData data) {
                  if (!NetworkController.this.firstTickMap.containsKey(block.getLocation())) {
                     NetworkController.this.onFirstTick(block, data);
                     NetworkController.this.firstTickMap.put(block.getLocation(), true);
                  }

                  NetworkController.this.addToRegistry(block);
                  NetworkRoot networkRoot = new NetworkRoot(
                     block.getLocation(),
                     (Integer)NetworkController.this.maxNodes.getValue(),
                     NetworkController.recordFlow.getOrDefault(block.getLocation(), false),
                     NetworkController.records.get(block.getLocation())
                  );
                  networkRoot.addAllChildren();
                  boolean crayon = NetworkController.CRAYONS.contains(block.getLocation());
                  if (crayon) {
                     networkRoot.setDisplayParticles(true);
                  }

                  NetworkController.NETWORKS.put(block.getLocation(), networkRoot);
                  NodeDefinition definition = NetworkStorage.getNode(block.getLocation());
                  if (definition != null) {
                     definition.setNode(networkRoot);
                  }

                  NetworkRootReadyEvent event = new NetworkRootReadyEvent(networkRoot);
                  Bukkit.getPluginManager().callEvent(event);
               }
            }
         }
      );
   }

   public static void enableRecord(Location root) {
      recordFlow.put(root, true);
      records.putIfAbsent(root, new ItemFlowRecord());
   }

   public static void disableRecord(Location root) {
      recordFlow.put(root, false);
      ItemFlowRecord record = records.get(root);
      if (record != null) {
         record.forceGC();
      }
   }

   @NotNull
   public static Map<Location, NetworkRoot> getNetworks() {
      return NETWORKS;
   }

   @NotNull
   public static Set<Location> getCrayons() {
      return CRAYONS;
   }

   public static void addCrayon(@NotNull Location location) {
      StorageCacheUtils.setData(location, "crayon", String.valueOf(true));
      CRAYONS.add(location);
   }

   public static void removeCrayon(@NotNull Location location) {
      StorageCacheUtils.removeData(location, "crayon");
      CRAYONS.remove(location);
   }

   public static boolean hasCrayon(@NotNull Location location) {
      return CRAYONS.contains(location);
   }

   public static void wipeNetwork(@NotNull Location location) {
      NetworkRoot networkRoot = NETWORKS.remove(location);
      if (networkRoot != null) {
         for (NetworkNode node : networkRoot.getChildrenNodes()) {
            NetworkStorage.removeNode(node.getNodePosition());
         }
      }
   }

   @Override
   protected void cancelPlace(@NotNull BlockPlaceEvent event) {
      event.getPlayer().sendMessage(Lang.getString("messages.unsupported-operation.controller.cancel_place"));
      event.setCancelled(true);
   }

   private void onFirstTick(@NotNull Block block, @NotNull SlimefunBlockData data) {
      String crayon = data.getData("crayon");
      if (Boolean.parseBoolean(crayon)) {
         CRAYONS.add(block.getLocation());
      }
   }

   @Generated
   public Map<Location, Boolean> getFirstTickMap() {
      return this.firstTickMap;
   }

   @Generated
   public static Map<Location, ItemFlowRecord> getRecords() {
      return records;
   }

   @Generated
   public static Map<Location, Boolean> getRecordFlow() {
      return recordFlow;
   }

   @NotNull
   @Generated
   public ItemSetting<Integer> getMaxNodes() {
      return this.maxNodes;
   }
}
