package io.github.sefiraat.networks.network;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.slimefun.network.NetworkController;
import io.github.sefiraat.networks.slimefun.network.NetworkPowerNode;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import lombok.Generated;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NetworkNode {
   public static final Set<BlockFace> VALID_FACES = EnumSet.of(BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST);
   protected final Set<NetworkNode> childrenNodes = new HashSet<>();
   protected final Location nodePosition;
   protected final NodeType nodeType;
   protected final long power;
   @Nullable
   protected NetworkNode parent = null;
   protected NetworkRoot root = null;

   public NetworkNode(Location location, NodeType type) {
      this.nodePosition = location;
      this.nodeType = type;
      this.power = this.retrieveBlockCharge();
   }

   public void addChild(@NotNull NetworkNode child) {
      child.setParent(this);
      child.setRoot(this.getRoot());
      if (this.root != null) {
         this.root.addRootPower(child.getPower());
         this.root.registerNode(child.nodePosition, child.nodeType);
      }

      this.childrenNodes.add(child);
   }

   @NotNull
   public Location getNodePosition() {
      return this.nodePosition;
   }

   @NotNull
   public NodeType getNodeType() {
      return this.nodeType;
   }

   public boolean networkContains(@NotNull NetworkNode networkNode) {
      return this.networkContains(networkNode.nodePosition);
   }

   public boolean networkContains(@NotNull Location location) {
      return this.root == null ? false : this.root.getNodeLocations().contains(location);
   }

   private void setRoot(@Nullable NetworkRoot root) {
      this.root = root;
   }

   private void setParent(@Nullable NetworkNode parent) {
      this.parent = parent;
   }

   public void addAllChildren() {
      if (this.root != null) {
         Deque<NetworkNode> nodeStack = new ArrayDeque<>(200);
         nodeStack.push(this);

         while (!nodeStack.isEmpty()) {
            NetworkNode currentNode = nodeStack.pop();

            for (BlockFace face : VALID_FACES) {
               Location testLocation = currentNode.nodePosition.clone().add(face.getDirection());
               NodeDefinition testDefinition = NetworkStorage.getNode(testLocation);
               if (testDefinition != null) {
                  NodeType testType = testDefinition.getType();
                  if (testType == NodeType.CONTROLLER && !testLocation.equals(this.root.nodePosition)) {
                     this.killAdditionalController(testLocation);
                  } else if (testType != NodeType.CONTROLLER && !currentNode.networkContains(testLocation)) {
                     if (this.root.getNodeCount() >= this.root.getMaxNodes()) {
                        this.root.setOverburdened(true);
                        return;
                     }

                     NetworkNode networkNode = new NetworkNode(testLocation, testType);
                     currentNode.addChild(networkNode);
                     nodeStack.push(networkNode);
                     testDefinition.setNode(networkNode);
                     NetworkStorage.registerNode(testLocation, testDefinition);
                  }
               }
            }
         }
      }
   }

   private void killAdditionalController(@NotNull final Location location) {
      final SlimefunItem sfItem = StorageCacheUtils.getSfItem(location);
      if (sfItem != null) {
         Slimefun.getDatabaseManager().getBlockDataController().removeBlock(location);
         BukkitRunnable runnable = new BukkitRunnable() {
            public void run() {
               NetworkController.wipeNetwork(location);
               location.getWorld().dropItemNaturally(location, sfItem.getItem());
               location.getBlock().setType(Material.AIR);
            }
         };
         runnable.runTask(Networks.getInstance());
         NetworkController.wipeNetwork(location);
      }
   }

   protected long retrieveBlockCharge() {
      if (this.nodeType == NodeType.POWER_NODE) {
         int blockCharge = 0;
         if (StorageCacheUtils.getSfItem(this.nodePosition) instanceof NetworkPowerNode powerNode) {
            blockCharge = powerNode.getCharge(this.nodePosition);
         }

         return (long)blockCharge;
      } else {
         return 0L;
      }
   }

   @Generated
   public Set<NetworkNode> getChildrenNodes() {
      return this.childrenNodes;
   }

   @Generated
   public long getPower() {
      return this.power;
   }

   @Nullable
   @Generated
   public NetworkNode getParent() {
      return this.parent;
   }

   @Generated
   public NetworkRoot getRoot() {
      return this.root;
   }
}
