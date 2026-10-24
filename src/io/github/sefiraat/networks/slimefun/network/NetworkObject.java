package io.github.sefiraat.networks.slimefun.network;

import com.balugaq.netex.api.interfaces.HangingBlock;
import com.balugaq.netex.utils.Lang;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.core.items.SpecialSlimefunItem;
import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.NodeDefinition;
import io.github.sefiraat.networks.network.NodeType;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.thebusybiscuit.slimefun4.api.exceptions.IncompatibleItemHandlerException;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemHandler;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.ParametersAreNonnullByDefault;
import lombok.Generated;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public abstract class NetworkObject extends SpecialSlimefunItem implements AdminDebuggable {
   public static final Queue<Location> scheduledHangingTick = new ConcurrentLinkedQueue<>();
   protected static final Set<BlockFace> CHECK_FACES = Set.of(BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST);
   private final NodeType nodeType;
   private final List<Integer> slotsToDrop = new ArrayList<>();
   private final Set<Location> firstTickLocations = new HashSet<>();

   protected NetworkObject(
      @NotNull ItemGroup itemGroup,
      @NotNull SlimefunItemStack item,
      @NotNull RecipeType recipeType,
      @NotNull ItemStack[] recipe,
      @Range(from = 1L,to = 64L) int outputAmount,
      @NotNull NodeType type
   ) {
      this(itemGroup, item, recipeType, recipe, StackUtils.getAsQuantity(item, outputAmount), type);
   }

   protected NetworkObject(
      @NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe, NodeType type
   ) {
      this(itemGroup, item, recipeType, recipe, null, type);
   }

   protected NetworkObject(
      @NotNull ItemGroup itemGroup,
      @NotNull SlimefunItemStack item,
      @NotNull RecipeType recipeType,
      @NotNull ItemStack[] recipe,
      ItemStack recipeOutput,
      NodeType type
   ) {
      super(itemGroup, item, recipeType, recipe, recipeOutput);
      this.nodeType = type;
      this.addItemHandler(new ItemHandler[]{new BlockTicker() {
         public boolean isSynchronized() {
            return NetworkObject.this.runSync();
         }

         public void tick(@NotNull Block b, SlimefunItem item, @NotNull SlimefunBlockData data) {
            if (!NetworkObject.this.firstTickLocations.contains(b.getLocation())) {
               Bukkit.getScheduler().runTask(Networks.getInstance(), () -> {
                  HangingBlock.loadHangingBlocks(data);
                  HangingBlock.doFirstTick(data);
               });
               NetworkObject.this.firstTickLocations.add(b.getLocation());
            } else {
               NetworkObject.this.addToRegistry(b);
               NetworkObject.this.tickHangingBlocks(b);
            }
         }

         @NotNull
         public Optional<IncompatibleItemHandlerException> validate(@NotNull SlimefunItem slimefunItem) {
            return Optional.empty();
         }
      }, new BlockBreakHandler(false, false) {
         @ParametersAreNonnullByDefault
         public void onPlayerBreak(BlockBreakEvent event, ItemStack item, List<ItemStack> drops) {
            NetworkObject.this.preBreak(event);
            NetworkObject.this.onBreak(event);
            NetworkObject.this.postBreak(event);
         }
      }, new BlockPlaceHandler(false) {
         @ParametersAreNonnullByDefault
         public void onPlayerPlace(BlockPlaceEvent event) {
            NetworkObject.this.prePlace(event);
            NetworkObject.this.onPlace(event);
            NetworkObject.this.postPlace(event);
         }
      }});
   }

   protected void addToRegistry(@NotNull Block block) {
      if (!NetworkStorage.containsKey(block.getLocation())) {
         NodeDefinition nodeDefinition = new NodeDefinition(this.nodeType);
         NetworkStorage.registerNode(block.getLocation(), nodeDefinition);
      }
   }

   protected void tickHangingBlocks(@NotNull Block block) {
      scheduledHangingTick.add(block.getLocation());
   }

   @OverridingMethodsMustInvokeSuper
   protected void preBreak(@NotNull BlockBreakEvent event) {
      NetworkRoot.removePersistentAccessHistory(event.getBlock().getLocation());
      NetworkRoot.removeCountObservingAccessHistory(event.getBlock().getLocation());
   }

   @OverridingMethodsMustInvokeSuper
   protected void onBreak(@NotNull BlockBreakEvent event) {
      Location location = event.getBlock().getLocation();
      BlockMenu blockMenu = StorageCacheUtils.getMenu(location);
      if (blockMenu != null) {
         for (int i : this.getSlotsToDrop()) {
            blockMenu.dropItems(location, new int[]{i});
         }
      }

      Slimefun.getDatabaseManager().getBlockDataController().removeBlock(location);
   }

   @OverridingMethodsMustInvokeSuper
   protected void postBreak(@NotNull BlockBreakEvent event) {
   }

   @OverridingMethodsMustInvokeSuper
   protected void prePlace(@NotNull BlockPlaceEvent event) {
   }

   protected void cancelPlace(@NotNull BlockPlaceEvent event) {
      event.getPlayer().sendMessage(Lang.getString("messages.unsupported-operation.comprehensive.cancel_place"));
      event.setCancelled(true);
   }

   @OverridingMethodsMustInvokeSuper
   protected void onPlace(@NotNull BlockPlaceEvent event) {
   }

   @OverridingMethodsMustInvokeSuper
   protected void postPlace(@NotNull BlockPlaceEvent event) {
   }

   public boolean isAdminDebuggable() {
      return false;
   }

   public boolean runSync() {
      return false;
   }

   @Generated
   public NodeType getNodeType() {
      return this.nodeType;
   }

   @Generated
   public List<Integer> getSlotsToDrop() {
      return this.slotsToDrop;
   }

   @Generated
   public Set<Location> getFirstTickLocations() {
      return this.firstTickLocations;
   }

   static {
      Bukkit.getScheduler().runTaskTimer(Networks.getInstance(), () -> {
         while (!scheduledHangingTick.isEmpty()) {
            Location location = scheduledHangingTick.poll();
            Block block = location.getBlock();
            HangingBlock.tickHangingBlocks(block);
         }
      }, 1L, (long)Slimefun.getTickerTask().getTickRate());
   }
}
