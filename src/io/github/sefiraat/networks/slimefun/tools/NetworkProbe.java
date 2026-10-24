package io.github.sefiraat.networks.slimefun.tools;

import com.balugaq.netex.utils.Lang;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.core.items.SpecialSlimefunItem;
import com.ytdd9527.networksexpansion.implementation.ExpansionItemStacks;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.NodeType;
import io.github.sefiraat.networks.slimefun.NetworksSlimefunItemStacks;
import io.github.sefiraat.networks.slimefun.network.NetworkController;
import io.github.sefiraat.networks.utils.Theme;
import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemHandler;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NetworkProbe extends SpecialSlimefunItem implements CanCooldown {
   public static final String SPACES = " ".repeat(32);
   private static final MessageFormat MESSAGE_FORMAT = new MessageFormat("{0}{1}: {2}{3}", Locale.ROOT);

   public NetworkProbe(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe);
   }

   private static void displayToPlayer(@NotNull Block block, @NotNull Player player) {
      NetworkRoot root = NetworkController.getNetworks().get(block.getLocation());
      if (root != null) {
         int bridges = root.getNodeCount(NodeType.BRIDGE);
         int monitors = root.getNodeCount(NodeType.STORAGE_MONITOR);
         int importers = root.getNodeCount(NodeType.IMPORT);
         int exporters = root.getNodeCount(NodeType.EXPORT);
         int grids = root.getNodeCount(NodeType.GRID);
         int cells = root.getNodeCount(NodeType.CELL);
         int grabbers = root.getNodeCount(NodeType.GRABBER);
         int pushers = root.getNodeCount(NodeType.PUSHER);
         int cutters = root.getNodeCount(NodeType.CUTTER);
         int pasters = root.getNodeCount(NodeType.PASTER);
         int vacuums = root.getNodeCount(NodeType.VACUUM);
         int purgers = root.getNodeCount(NodeType.PURGER);
         int crafters = root.getNodeCount(NodeType.CRAFTER);
         int powerNodes = root.getNodeCount(NodeType.POWER_NODE);
         int powerDisplays = root.getNodeCount(NodeType.POWER_DISPLAY);
         int encoders = root.getNodeCount(NodeType.ENCODER);
         int wirelessTransmitters = root.getNodeCount(NodeType.WIRELESS_TRANSMITTER);
         int wirelessReceivers = root.getNodeCount(NodeType.WIRELESS_RECEIVER);
         int powerOutlets = root.getNodeCount(NodeType.POWER_OUTLET);
         int greedyBlocks = root.getNodeCount(NodeType.GREEDY_BLOCK);
         int advancedImporters = root.getNodeCount(NodeType.ADVANCED_IMPORT);
         int advancedExporters = root.getNodeCount(NodeType.ADVANCED_EXPORT);
         int advancedGreedyBlocks = root.getNodeCount(NodeType.ADVANCED_GREEDY_BLOCK);
         int advancedPurgers = root.getNodeCount(NodeType.ADVANCED_PURGER);
         int advancedVacuums = root.getNodeCount(NodeType.ADVANCED_VACUUM);
         int transferPushers = root.getNodeCount(NodeType.TRANSFER_PUSHER);
         int transferGrabbers = root.getNodeCount(NodeType.TRANSFER_GRABBER);
         int transfers = root.getNodeCount(NodeType.TRANSFER);
         int lineTransferVanillaPushers = root.getNodeCount(NodeType.LINE_TRANSFER_VANILLA_PUSHER);
         int lineTransferVanillaGrabbers = root.getNodeCount(NodeType.LINE_TRANSFER_VANILLA_GRABBER);
         int inputOnlyMonitor = root.getNodeCount(NodeType.INPUT_ONLY_MONITOR);
         int outputOnlyMonitor = root.getNodeCount(NodeType.OUTPUT_ONLY_MONITOR);
         int linePowerOutlets = root.getNodeCount(NodeType.LINE_POWER_OUTLET);
         int decoders = root.getNodeCount(NodeType.DECODER);
         int quantumManagers = root.getNodeCount(NodeType.QUANTUM_MANAGER);
         int drawerManagers = root.getNodeCount(NodeType.DRAWER_MANAGER);
         int crafterManagers = root.getNodeCount(NodeType.CRAFTER_MANAGER);
         int itemFlowViewers = root.getNodeCount(NodeType.FLOW_VIEWER);
         int advancedWirelessTransmitters = root.getNodeCount(NodeType.ADVANCED_WIRELESS_TRANSMITTER);
         int itemDifferenters = root.getNodeCount(NodeType.ITEM_DIFFERENTER);
         int storageCardConverters = root.getNodeCount(NodeType.STORAGE_CARD_CONVERTER);
         int facingPresetters = root.getNodeCount(NodeType.FACING_PRESETTER);
         int visualGrids = root.getNodeCount(NodeType.VISUAL_GRID);
         Map<ItemStack, Long> allNetworkItems = root.getAllNetworkItemsLongTypeView();
         int distinctItems = allNetworkItems.size();
         long totalItems = allNetworkItems.values().stream().mapToLong(integer -> integer).sum();
         String nodeCount = root.getNodeCount() >= root.getMaxNodes() ? "" + Theme.ERROR + root.getNodeCount() + "+" : String.valueOf(root.getNodeCount());
         ChatColor c = Theme.CLICK_INFO.getColor();
         ChatColor p = Theme.SUCCESS.getColor();
         player.sendMessage(Lang.getString("messages.completed-operation.probe.split"));
         player.sendMessage(Lang.getString("messages.completed-operation.probe.expansion_title"));
         player.sendMessage(Lang.getString("messages.completed-operation.probe.split"));
         player.sendMessage(formatter(ExpansionItemStacks.NETWORK_BLUEPRINT_DECODER.getDisplayName(), (long)decoders));
         player.sendMessage(formatter(ExpansionItemStacks.QUANTUM_MANAGER.getDisplayName(), (long)quantumManagers));
         player.sendMessage(formatter(ExpansionItemStacks.DRAWER_MANAGER.getDisplayName(), (long)drawerManagers));
         player.sendMessage(formatter(ExpansionItemStacks.CRAFTER_MANAGER.getDisplayName(), (long)crafterManagers));
         player.sendMessage(formatter(ExpansionItemStacks.ITEM_FLOW_VIEWER.getDisplayName(), (long)itemFlowViewers));
         player.sendMessage(formatter(ExpansionItemStacks.ADVANCED_WIRELESS_TRANSMITTER.getDisplayName(), (long)advancedWirelessTransmitters));
         player.sendMessage(formatter(ExpansionItemStacks.ITEM_DIFFERENTER.getDisplayName(), (long)itemDifferenters));
         player.sendMessage(formatter(ExpansionItemStacks.STORAGE_CARD_CONVERTER.getDisplayName(), (long)storageCardConverters));
         player.sendMessage(formatter(ExpansionItemStacks.FACING_PRESETTER.getDisplayName(), (long)facingPresetters));
         player.sendMessage(formatter(ExpansionItemStacks.ADVANCED_IMPORT.getDisplayName(), (long)advancedImporters));
         player.sendMessage(formatter(ExpansionItemStacks.ADVANCED_EXPORT.getDisplayName(), (long)advancedExporters));
         player.sendMessage(formatter(ExpansionItemStacks.ADVANCED_GREEDY_BLOCK.getDisplayName(), (long)advancedGreedyBlocks));
         player.sendMessage(formatter(ExpansionItemStacks.ADVANCED_PURGER.getDisplayName(), (long)advancedPurgers));
         player.sendMessage(formatter(ExpansionItemStacks.ADVANCED_VACUUM.getDisplayName(), (long)advancedVacuums));
         player.sendMessage(formatter(ExpansionItemStacks.TRANSFER.getDisplayName(), (long)transfers));
         player.sendMessage(formatter(ExpansionItemStacks.TRANSFER_GRABBER.getDisplayName(), (long)transferGrabbers));
         player.sendMessage(formatter(ExpansionItemStacks.TRANSFER_PUSHER.getDisplayName(), (long)transferPushers));
         player.sendMessage(formatter(ExpansionItemStacks.LINE_TRANSFER_VANILLA_PUSHER.getDisplayName(), (long)lineTransferVanillaPushers));
         player.sendMessage(formatter(ExpansionItemStacks.LINE_TRANSFER_VANILLA_GRABBER.getDisplayName(), (long)lineTransferVanillaGrabbers));
         player.sendMessage(formatter(ExpansionItemStacks.NETWORK_INPUT_ONLY_MONITOR.getDisplayName(), (long)inputOnlyMonitor));
         player.sendMessage(formatter(ExpansionItemStacks.NETWORK_OUTPUT_ONLY_MONITOR.getDisplayName(), (long)outputOnlyMonitor));
         player.sendMessage(formatter(stringOrSpaces(ExpansionItemStacks.LINE_POWER_OUTLET_1.getDisplayName()).substring(0, 6), (long)linePowerOutlets));
         player.sendMessage(formatter(ExpansionItemStacks.VISUAL_GRID.getDisplayName(), (long)visualGrids));
         player.sendMessage(Lang.getString("messages.completed-operation.probe.split"));
         player.sendMessage(Lang.getString("messages.completed-operation.probe.networks_title"));
         player.sendMessage(Lang.getString("messages.completed-operation.probe.split"));
         player.sendMessage(formatter(NetworksSlimefunItemStacks.NETWORK_BRIDGE.getDisplayName(), (long)bridges));
         player.sendMessage(formatter(NetworksSlimefunItemStacks.NETWORK_MONITOR.getDisplayName(), (long)monitors));
         player.sendMessage(formatter(NetworksSlimefunItemStacks.NETWORK_IMPORT.getDisplayName(), (long)importers));
         player.sendMessage(formatter(NetworksSlimefunItemStacks.NETWORK_EXPORT.getDisplayName(), (long)exporters));
         player.sendMessage(formatter(NetworksSlimefunItemStacks.NETWORK_GRID.getDisplayName(), (long)grids));
         player.sendMessage(formatter(NetworksSlimefunItemStacks.NETWORK_CELL.getDisplayName(), (long)cells));
         player.sendMessage(formatter(NetworksSlimefunItemStacks.NETWORK_GRABBER.getDisplayName(), (long)grabbers));
         player.sendMessage(formatter(NetworksSlimefunItemStacks.NETWORK_PUSHER.getDisplayName(), (long)pushers));
         player.sendMessage(formatter(NetworksSlimefunItemStacks.NETWORK_PURGER.getDisplayName(), (long)purgers));
         player.sendMessage(formatter(NetworksSlimefunItemStacks.NETWORK_AUTO_CRAFTER.getDisplayName(), (long)crafters));
         player.sendMessage(formatter(stringOrSpaces(NetworksSlimefunItemStacks.NETWORK_CAPACITOR_1.getDisplayName()).substring(0, 4), (long)powerNodes));
         player.sendMessage(formatter(NetworksSlimefunItemStacks.NETWORK_POWER_DISPLAY.getDisplayName(), (long)powerDisplays));
         player.sendMessage(formatter(NetworksSlimefunItemStacks.NETWORK_RECIPE_ENCODER.getDisplayName(), (long)encoders));
         player.sendMessage(formatter(NetworksSlimefunItemStacks.NETWORK_CONTROL_X.getDisplayName(), (long)cutters));
         player.sendMessage(formatter(NetworksSlimefunItemStacks.NETWORK_CONTROL_V.getDisplayName(), (long)pasters));
         player.sendMessage(formatter(NetworksSlimefunItemStacks.NETWORK_VACUUM.getDisplayName(), (long)vacuums));
         player.sendMessage(formatter(NetworksSlimefunItemStacks.NETWORK_WIRELESS_TRANSMITTER.getDisplayName(), (long)wirelessTransmitters));
         player.sendMessage(formatter(NetworksSlimefunItemStacks.NETWORK_WIRELESS_RECEIVER.getDisplayName(), (long)wirelessReceivers));
         player.sendMessage(formatter(stringOrSpaces(NetworksSlimefunItemStacks.NETWORK_POWER_OUTLET_1.getDisplayName()).substring(0, 4), (long)powerOutlets));
         player.sendMessage(formatter(NetworksSlimefunItemStacks.NETWORK_GREEDY_BLOCK.getDisplayName(), (long)greedyBlocks));
         player.sendMessage(Lang.getString("messages.completed-operation.probe.split"));
         player.sendMessage(formatter(Lang.getString("messages.completed-operation.probe.distinct_items"), (long)distinctItems));
         player.sendMessage(formatter(Lang.getString("messages.completed-operation.probe.total_items"), totalItems));
         player.sendMessage(Lang.getString("messages.completed-operation.probe.split"));
         player.sendMessage(String.format(Lang.getString("messages.completed-operation.probe.total_nodes"), nodeCount, root.getMaxNodes()));
         if (root.isOverburdened()) {
            player.sendMessage(Lang.getString("messages.completed-operation.probe.overburdened"));
         }
      }
   }

   @NotNull
   public static String formatter(String name, long count) {
      return MESSAGE_FORMAT.format(new Object[]{Theme.CLICK_INFO.getColor(), name, Theme.SUCCESS.getColor(), count}, new StringBuffer(), null).toString();
   }

   @NotNull
   public static String formatter(String name, String s) {
      return MESSAGE_FORMAT.format(new Object[]{Theme.CLICK_INFO.getColor(), name, Theme.SUCCESS.getColor(), s}, new StringBuffer(), null).toString();
   }

   @NotNull
   public static String stringOrSpaces(@Nullable String s) {
      return s == null ? SPACES : s;
   }

   public void preRegister() {
      this.addItemHandler(new ItemHandler[]{this::onUse});
   }

   protected void onUse(@NotNull PlayerRightClickEvent e) {
      Optional<Block> optional = e.getClickedBlock();
      if (optional.isPresent()) {
         Block block = optional.get();
         Player player = e.getPlayer();
         if (this.canBeUsed(player, e.getItem())) {
            SlimefunBlockData blockData = StorageCacheUtils.getBlock(block.getLocation());
            if (blockData == null) {
               return;
            }

            SlimefunItem slimefunItem = SlimefunItem.getById(blockData.getSfId());
            if (slimefunItem instanceof NetworkController) {
               e.cancel();
               displayToPlayer(block, player);
               this.putOnCooldown(e.getItem());
            }
         }
      }
   }

   @Override
   public int cooldownDuration() {
      return 10;
   }
}
