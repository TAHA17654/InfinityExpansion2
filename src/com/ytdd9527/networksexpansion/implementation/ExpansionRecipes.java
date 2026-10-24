package com.ytdd9527.networksexpansion.implementation;

import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.CellTier;
import io.github.sefiraat.networks.slimefun.NetworkSlimefunItems;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ExpansionRecipes {
   public static final ItemStack HOPPER = new ItemStack(Material.HOPPER);
   public static final ItemStack CRAFTING_TABLE = new ItemStack(Material.CRAFTING_TABLE);
   public static final ItemStack[] NULL = new ItemStack[]{null, null, null, null, null, null, null, null, null};
   public static final ItemStack[] RUNE_COLLECT = NULL;
   public static final ItemStack[] NETWORKS_EXPANSION_WORKBENCH = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      CRAFTING_TABLE,
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem()
   };
   public static final ItemStack[] LINE_TRANSFER_PUSHER = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_PUSHER.getItem(),
      NetworkSlimefunItems.NETWORK_EXPORT.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.NETWORK_EXPORT.getItem(),
      NetworkSlimefunItems.NETWORK_MONITOR.getItem(),
      NetworkSlimefunItems.NETWORK_EXPORT.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.NETWORK_EXPORT.getItem(),
      NetworkSlimefunItems.NETWORK_PUSHER.getItem()
   };
   public static final ItemStack[] LINE_TRANSFER_MORE_PUSHER = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_MORE_PUSHER.getItem(),
      NetworkSlimefunItems.NETWORK_EXPORT.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.NETWORK_EXPORT.getItem(),
      NetworkSlimefunItems.NETWORK_MONITOR.getItem(),
      NetworkSlimefunItems.NETWORK_EXPORT.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.NETWORK_EXPORT.getItem(),
      NetworkSlimefunItems.NETWORK_MORE_PUSHER.getItem()
   };
   public static final ItemStack[] LINE_TRANSFER_BEST_PUSHER = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_BEST_PUSHER.getItem(),
      NetworkSlimefunItems.NETWORK_EXPORT.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.NETWORK_EXPORT.getItem(),
      NetworkSlimefunItems.NETWORK_MONITOR.getItem(),
      NetworkSlimefunItems.NETWORK_EXPORT.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.NETWORK_EXPORT.getItem(),
      NetworkSlimefunItems.NETWORK_BEST_PUSHER.getItem()
   };
   public static final ItemStack[] LINE_TRANSFER_PLUS_PUSHER = new ItemStack[]{
      NetworkSlimefunItems.SHRINKING_BASE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.SHRINKING_BASE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      ExpansionItemStacks.LINE_TRANSFER_PUSHER,
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.SHRINKING_BASE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.SHRINKING_BASE.getItem()
   };
   public static final ItemStack[] LINE_TRANSFER_PLUS_MORE_PUSHER = new ItemStack[]{
      NetworkSlimefunItems.SHRINKING_BASE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.SHRINKING_BASE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      ExpansionItemStacks.LINE_TRANSFER_MORE_PUSHER,
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.SHRINKING_BASE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.SHRINKING_BASE.getItem()
   };
   public static final ItemStack[] LINE_TRANSFER_PLUS_BEST_PUSHER = new ItemStack[]{
      NetworkSlimefunItems.SHRINKING_BASE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.SHRINKING_BASE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      ExpansionItemStacks.LINE_TRANSFER_BEST_PUSHER,
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.SHRINKING_BASE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.SHRINKING_BASE.getItem()
   };
   public static final ItemStack[] ADVANCED_LINE_TRANSFER_PUSHER = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      ExpansionItemStacks.LINE_TRANSFER_PUSHER,
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      ExpansionItemStacks.LINE_TRANSFER_PUSHER,
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem()
   };
   public static final ItemStack[] ADVANCED_LINE_TRANSFER_MORE_PUSHER = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      ExpansionItemStacks.LINE_TRANSFER_MORE_PUSHER,
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      ExpansionItemStacks.LINE_TRANSFER_MORE_PUSHER,
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem()
   };
   public static final ItemStack[] ADVANCED_LINE_TRANSFER_BEST_PUSHER = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      ExpansionItemStacks.LINE_TRANSFER_BEST_PUSHER,
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      ExpansionItemStacks.LINE_TRANSFER_BEST_PUSHER,
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem()
   };
   public static final ItemStack[] ADVANCED_LINE_TRANSFER_PLUS_PUSHER = new ItemStack[]{
      NetworkSlimefunItems.SHRINKING_BASE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.SHRINKING_BASE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      ExpansionItemStacks.ADVANCED_LINE_TRANSFER_PUSHER,
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.SHRINKING_BASE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.SHRINKING_BASE.getItem()
   };
   public static final ItemStack[] ADVANCED_LINE_TRANSFER_PLUS_MORE_PUSHER = new ItemStack[]{
      NetworkSlimefunItems.SHRINKING_BASE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.SHRINKING_BASE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      ExpansionItemStacks.ADVANCED_LINE_TRANSFER_MORE_PUSHER,
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.SHRINKING_BASE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.SHRINKING_BASE.getItem()
   };
   public static final ItemStack[] ADVANCED_LINE_TRANSFER_PLUS_BEST_PUSHER = new ItemStack[]{
      NetworkSlimefunItems.SHRINKING_BASE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.SHRINKING_BASE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      ExpansionItemStacks.ADVANCED_LINE_TRANSFER_BEST_PUSHER,
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.SHRINKING_BASE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.SHRINKING_BASE.getItem()
   };
   public static final ItemStack[] LINE_TRANSFER_GRABBER = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_GRABBER.getItem(),
      NetworkSlimefunItems.NETWORK_IMPORT.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.NETWORK_IMPORT.getItem(),
      NetworkSlimefunItems.NETWORK_MONITOR.getItem(),
      NetworkSlimefunItems.NETWORK_IMPORT.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.NETWORK_IMPORT.getItem(),
      NetworkSlimefunItems.NETWORK_GRABBER.getItem()
   };
   public static final ItemStack[] LINE_TRANSFER_PLUS_GRABBER = new ItemStack[]{
      NetworkSlimefunItems.SHRINKING_BASE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.SHRINKING_BASE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      ExpansionItemStacks.LINE_TRANSFER_GRABBER,
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.SHRINKING_BASE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.SHRINKING_BASE.getItem()
   };
   public static final ItemStack[] ADVANCED_LINE_TRANSFER_GRABBER = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      ExpansionItemStacks.LINE_TRANSFER_GRABBER,
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      ExpansionItemStacks.LINE_TRANSFER_GRABBER,
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem()
   };
   public static final ItemStack[] ADVANCED_LINE_TRANSFER_PLUS_GRABBER = new ItemStack[]{
      NetworkSlimefunItems.SHRINKING_BASE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.SHRINKING_BASE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      ExpansionItemStacks.ADVANCED_LINE_TRANSFER_GRABBER,
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.SHRINKING_BASE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.SHRINKING_BASE.getItem()
   };
   public static final ItemStack[] LINE_TRANSFER = new ItemStack[]{
      ExpansionItemStacks.LINE_TRANSFER_PUSHER,
      NetworkSlimefunItems.NETWORK_IMPORT.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_EXPORT.getItem(),
      NetworkSlimefunItems.NETWORK_MONITOR.getItem(),
      NetworkSlimefunItems.NETWORK_EXPORT.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_IMPORT.getItem(),
      ExpansionItemStacks.LINE_TRANSFER_GRABBER
   };
   public static final ItemStack[] LINE_TRANSFER_PLUS = new ItemStack[]{
      ExpansionItemStacks.LINE_TRANSFER_PLUS_PUSHER,
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      ExpansionItemStacks.LINE_TRANSFER_PLUS_GRABBER
   };
   public static final ItemStack[] ADVANCED_LINE_TRANSFER = new ItemStack[]{
      ExpansionItemStacks.ADVANCED_LINE_TRANSFER_PUSHER,
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      ExpansionItemStacks.ADVANCED_LINE_TRANSFER_GRABBER
   };
   public static final ItemStack[] ADVANCED_LINE_TRANSFER_PLUS = new ItemStack[]{
      ExpansionItemStacks.ADVANCED_LINE_TRANSFER_PLUS_PUSHER,
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      ExpansionItemStacks.ADVANCED_LINE_TRANSFER_PLUS_GRABBER
   };
   public static final ItemStack[] TRANSFER_PUSHER = new ItemStack[]{null, null, null, null, ExpansionItemStacks.LINE_TRANSFER_PUSHER, null, null, null, null};
   public static final ItemStack[] TRANSFER_MORE_PUSHER = new ItemStack[]{
      null, null, null, null, ExpansionItemStacks.LINE_TRANSFER_MORE_PUSHER, null, null, null, null
   };
   public static final ItemStack[] TRANSFER_BEST_PUSHER = new ItemStack[]{
      null, null, null, null, ExpansionItemStacks.LINE_TRANSFER_BEST_PUSHER, null, null, null, null
   };
   public static final ItemStack[] TRANSFER_GRABBER = new ItemStack[]{
      null, null, null, null, ExpansionItemStacks.LINE_TRANSFER_GRABBER, null, null, null, null
   };
   public static final ItemStack[] TRANSFER = new ItemStack[]{null, null, null, null, ExpansionItemStacks.LINE_TRANSFER, null, null, null, null};
   public static final ItemStack[] ADVANCED_TRANSFER_PUSHER = new ItemStack[]{
      null, null, null, null, ExpansionItemStacks.ADVANCED_LINE_TRANSFER_PUSHER, null, null, null, null
   };
   public static final ItemStack[] ADVANCED_TRANSFER_MORE_PUSHER = new ItemStack[]{
      null, null, null, null, ExpansionItemStacks.ADVANCED_LINE_TRANSFER_MORE_PUSHER, null, null, null, null
   };
   public static final ItemStack[] ADVANCED_TRANSFER_BEST_PUSHER = new ItemStack[]{
      null, null, null, null, ExpansionItemStacks.ADVANCED_LINE_TRANSFER_BEST_PUSHER, null, null, null, null
   };
   public static final ItemStack[] ADVANCED_TRANSFER_GRABBER = new ItemStack[]{
      null, null, null, null, ExpansionItemStacks.ADVANCED_LINE_TRANSFER_GRABBER, null, null, null, null
   };
   public static final ItemStack[] ADVANCED_TRANSFER = new ItemStack[]{
      null, null, null, null, ExpansionItemStacks.ADVANCED_LINE_TRANSFER, null, null, null, null
   };
   public static final ItemStack[] LINE_TRANSFER_VANILLA_GRABBER = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_VANILLA_GRABBER.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.NETWORK_VANILLA_GRABBER.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.NETWORK_VANILLA_GRABBER.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.NETWORK_VANILLA_GRABBER.getItem()
   };
   public static final ItemStack[] LINE_TRANSFER_VANILLA_PUSHER = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_VANILLA_PUSHER.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.NETWORK_VANILLA_PUSHER.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.NETWORK_VANILLA_PUSHER.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.NETWORK_VANILLA_PUSHER.getItem()
   };
   public static final ItemStack[] ADVANCED_IMPORT = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_IMPORT.getItem(),
      NetworkSlimefunItems.NETWORK_IMPORT.getItem(),
      NetworkSlimefunItems.NETWORK_IMPORT.getItem(),
      NetworkSlimefunItems.OPTIC_STAR.getItem(),
      NetworkSlimefunItems.OPTIC_STAR.getItem(),
      NetworkSlimefunItems.OPTIC_STAR.getItem(),
      NetworkSlimefunItems.NETWORK_IMPORT.getItem(),
      NetworkSlimefunItems.NETWORK_IMPORT.getItem(),
      NetworkSlimefunItems.NETWORK_IMPORT.getItem()
   };
   public static final ItemStack[] ADVANCED_EXPORT = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_EXPORT.getItem(),
      NetworkSlimefunItems.RADIOACTIVE_OPTIC_STAR.getItem(),
      NetworkSlimefunItems.NETWORK_EXPORT.getItem(),
      NetworkSlimefunItems.NETWORK_EXPORT.getItem(),
      NetworkSlimefunItems.RADIOACTIVE_OPTIC_STAR.getItem(),
      NetworkSlimefunItems.NETWORK_EXPORT.getItem(),
      NetworkSlimefunItems.NETWORK_EXPORT.getItem(),
      NetworkSlimefunItems.RADIOACTIVE_OPTIC_STAR.getItem(),
      NetworkSlimefunItems.NETWORK_EXPORT.getItem()
   };
   public static final ItemStack[] ADVANCED_PURGER = new ItemStack[]{
      NetworkSlimefunItems.RADIOACTIVE_OPTIC_STAR.getItem(),
      NetworkSlimefunItems.RADIOACTIVE_OPTIC_STAR.getItem(),
      NetworkSlimefunItems.RADIOACTIVE_OPTIC_STAR.getItem(),
      NetworkSlimefunItems.RADIOACTIVE_OPTIC_STAR.getItem(),
      NetworkSlimefunItems.NETWORK_PURGER.getItem(),
      NetworkSlimefunItems.RADIOACTIVE_OPTIC_STAR.getItem(),
      NetworkSlimefunItems.RADIOACTIVE_OPTIC_STAR.getItem(),
      NetworkSlimefunItems.RADIOACTIVE_OPTIC_STAR.getItem(),
      NetworkSlimefunItems.RADIOACTIVE_OPTIC_STAR.getItem()
   };
   public static final ItemStack[] ADVANCED_GREEDY_BLOCK = new ItemStack[]{
      NetworkSlimefunItems.SYNTHETIC_EMERALD_SHARD.getItem(),
      NetworkSlimefunItems.NETWORK_GREEDY_BLOCK.getItem(),
      NetworkSlimefunItems.SYNTHETIC_EMERALD_SHARD.getItem(),
      NetworkSlimefunItems.NETWORK_GREEDY_BLOCK.getItem(),
      NetworkSlimefunItems.NETWORK_GREEDY_BLOCK.getItem(),
      NetworkSlimefunItems.NETWORK_GREEDY_BLOCK.getItem(),
      NetworkSlimefunItems.SHRINKING_BASE.getItem(),
      NetworkSlimefunItems.NETWORK_GREEDY_BLOCK.getItem(),
      NetworkSlimefunItems.SHRINKING_BASE.getItem()
   };
   public static final ItemStack[] NETWORK_INPUT_ONLY_MONITOR = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.NETWORK_MONITOR.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      SlimefunItems.CARGO_INPUT_NODE,
      NetworkSlimefunItems.OPTIC_GLASS.getItem()
   };
   public static final ItemStack[] NETWORK_OUTPUT_ONLY_MONITOR = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      SlimefunItems.CARGO_OUTPUT_NODE_2,
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.NETWORK_MONITOR.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem()
   };
   public static final ItemStack[] NETWORK_CAPACITOR_5 = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_CAPACITOR_4.getItem(),
      NetworkSlimefunItems.NETWORK_CAPACITOR_4.getItem(),
      NetworkSlimefunItems.NETWORK_CAPACITOR_4.getItem(),
      NetworkSlimefunItems.NETWORK_CAPACITOR_4.getItem(),
      NetworkSlimefunItems.NETWORK_CAPACITOR_4.getItem(),
      NetworkSlimefunItems.NETWORK_CAPACITOR_4.getItem(),
      NetworkSlimefunItems.NETWORK_CAPACITOR_4.getItem(),
      NetworkSlimefunItems.NETWORK_CAPACITOR_4.getItem(),
      NetworkSlimefunItems.NETWORK_CAPACITOR_4.getItem()
   };
   public static final ItemStack[] NETWORK_CAPACITOR_6 = new ItemStack[]{
      ExpansionItemStacks.NETWORK_CAPACITOR_5,
      ExpansionItemStacks.NETWORK_CAPACITOR_5,
      ExpansionItemStacks.NETWORK_CAPACITOR_5,
      ExpansionItemStacks.NETWORK_CAPACITOR_5,
      ExpansionItemStacks.NETWORK_CAPACITOR_5,
      ExpansionItemStacks.NETWORK_CAPACITOR_5,
      ExpansionItemStacks.NETWORK_CAPACITOR_5,
      ExpansionItemStacks.NETWORK_CAPACITOR_5,
      ExpansionItemStacks.NETWORK_CAPACITOR_5
   };
   public static final ItemStack[] ADVANCED_AUTO_CRAFTING_TABLE = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_AUTO_CRAFTER.getItem(),
      NetworkSlimefunItems.ADVANCED_NANOBOTS.getItem(),
      NetworkSlimefunItems.NETWORK_AUTO_CRAFTER.getItem(),
      NetworkSlimefunItems.ADVANCED_NANOBOTS.getItem(),
      NetworkSlimefunItems.INTERDIMENSIONAL_PRESENCE.getItem(),
      NetworkSlimefunItems.ADVANCED_NANOBOTS.getItem(),
      NetworkSlimefunItems.NETWORK_AUTO_CRAFTER.getItem(),
      NetworkSlimefunItems.NETWORK_RECIPE_ENCODER.getItem(),
      NetworkSlimefunItems.NETWORK_AUTO_CRAFTER.getItem()
   };
   public static final ItemStack[] ADVANCED_AUTO_CRAFTING_TABLE_WITHHOLDING = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_AUTO_CRAFTER_WITHHOLDING.getItem(),
      NetworkSlimefunItems.ADVANCED_NANOBOTS.getItem(),
      NetworkSlimefunItems.NETWORK_AUTO_CRAFTER_WITHHOLDING.getItem(),
      NetworkSlimefunItems.ADVANCED_NANOBOTS.getItem(),
      NetworkSlimefunItems.INTERDIMENSIONAL_PRESENCE.getItem(),
      NetworkSlimefunItems.ADVANCED_NANOBOTS.getItem(),
      NetworkSlimefunItems.NETWORK_AUTO_CRAFTER_WITHHOLDING.getItem(),
      NetworkSlimefunItems.NETWORK_RECIPE_ENCODER.getItem(),
      NetworkSlimefunItems.NETWORK_AUTO_CRAFTER_WITHHOLDING.getItem()
   };
   public static final ItemStack[] SMART_GRABBER = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.SIMPLE_NANOBOTS.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.NETWORK_GRABBER.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.SIMPLE_NANOBOTS.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem()
   };
   public static final ItemStack[] SMART_PUSHER = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.ADVANCED_NANOBOTS.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.NETWORK_PUSHER.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.ADVANCED_NANOBOTS.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem()
   };
   public static final ItemStack[] NETWORK_GRID_NEW_STYLE = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.NETWORK_GRID.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem()
   };
   public static final ItemStack[] ADVANCED_QUANTUM_STORAGE = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_14.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem()
   };
   public static final ItemStack[] NETWORK_BRIDGE_ORDINAL = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      new ItemStack(Material.GLASS),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem()
   };
   public static final ItemStack[] NETWORK_BRIDGE_WHITE = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      new ItemStack(Material.WHITE_DYE),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem()
   };
   public static final ItemStack[] NETWORK_BRIDGE_LIGHT_GRAY = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      new ItemStack(Material.LIGHT_GRAY_DYE),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem()
   };
   public static final ItemStack[] NETWORK_BRIDGE_GRAY = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      new ItemStack(Material.GRAY_DYE),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem()
   };
   public static final ItemStack[] NETWORK_BRIDGE_BLACK = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      new ItemStack(Material.BLACK_DYE),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem()
   };
   public static final ItemStack[] NETWORK_BRIDGE_BROWN = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      new ItemStack(Material.BROWN_DYE),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem()
   };
   public static final ItemStack[] NETWORK_BRIDGE_RED = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      new ItemStack(Material.RED_DYE),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem()
   };
   public static final ItemStack[] NETWORK_BRIDGE_ORANGE = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      new ItemStack(Material.ORANGE_DYE),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem()
   };
   public static final ItemStack[] NETWORK_BRIDGE_YELLOW = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      new ItemStack(Material.YELLOW_DYE),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem()
   };
   public static final ItemStack[] NETWORK_BRIDGE_LIME = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      new ItemStack(Material.LIME_DYE),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem()
   };
   public static final ItemStack[] NETWORK_BRIDGE_GREEN = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      new ItemStack(Material.GREEN_DYE),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem()
   };
   public static final ItemStack[] NETWORK_BRIDGE_CYAN = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      new ItemStack(Material.CYAN_DYE),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem()
   };
   public static final ItemStack[] NETWORK_BRIDGE_LIGHT_BLUE = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      new ItemStack(Material.LIGHT_BLUE_DYE),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem()
   };
   public static final ItemStack[] NETWORK_BRIDGE_BLUE = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      new ItemStack(Material.BLUE_DYE),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem()
   };
   public static final ItemStack[] NETWORK_BRIDGE_PURPLE = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      new ItemStack(Material.PURPLE_DYE),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem()
   };
   public static final ItemStack[] NETWORK_BRIDGE_MAGENTA = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      new ItemStack(Material.MAGENTA_DYE),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem()
   };
   public static final ItemStack[] NETWORK_BRIDGE_PINK = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      new ItemStack(Material.PINK_DYE),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem()
   };
   public static final ItemStack[] CARGO_NODE_QUICK_TOOL = new ItemStack[]{
      new ItemStack(Material.LEATHER),
      SlimefunItems.SOLAR_PANEL,
      new ItemStack(Material.LEATHER),
      new ItemStack(Material.LEATHER),
      SlimefunItems.ANDROID_MEMORY_CORE,
      new ItemStack(Material.LEATHER),
      SlimefunItems.ADVANCED_CIRCUIT_BOARD,
      SlimefunItems.SMALL_CAPACITOR,
      SlimefunItems.ADVANCED_CIRCUIT_BOARD
   };
   public static final ItemStack[] STORAGE_UNIT_UPGRADE_TABLE = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.SIMPLE_NANOBOTS.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.SIMPLE_NANOBOTS.getItem(),
      NetworkSlimefunItems.NETWORK_QUANTUM_WORKBENCH.getItem(),
      NetworkSlimefunItems.SIMPLE_NANOBOTS.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.SIMPLE_NANOBOTS.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem()
   };
   public static final ItemStack[] CARGO_STORAGE_UNIT_1 = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_1.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_1.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem()
   };
   public static final ItemStack[] CARGO_STORAGE_UNIT_2 = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_9.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_9.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      ExpansionItemStacks.CARGO_STORAGE_UNIT_1,
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_9.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_9.getItem()
   };
   public static final ItemStack[] CARGO_STORAGE_UNIT_3 = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_10.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_10.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      ExpansionItemStacks.CARGO_STORAGE_UNIT_2,
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_10.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_10.getItem()
   };
   public static final ItemStack[] CARGO_STORAGE_UNIT_4 = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_1.getItem(),
      SlimefunItems.BOOSTED_URANIUM,
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_1.getItem(),
      SlimefunItems.BOOSTED_URANIUM,
      ExpansionItemStacks.CARGO_STORAGE_UNIT_3,
      SlimefunItems.BOOSTED_URANIUM,
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_1.getItem(),
      SlimefunItems.BOOSTED_URANIUM,
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_1.getItem()
   };
   public static final ItemStack[] CARGO_STORAGE_UNIT_5 = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_2.getItem(),
      SlimefunItems.NETHER_ICE,
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_2.getItem(),
      SlimefunItems.NETHER_ICE,
      ExpansionItemStacks.CARGO_STORAGE_UNIT_4,
      SlimefunItems.NETHER_ICE,
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_2.getItem(),
      SlimefunItems.NETHER_ICE,
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_2.getItem()
   };
   public static final ItemStack[] CARGO_STORAGE_UNIT_6 = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_3.getItem(),
      SlimefunItems.FUEL_BUCKET,
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_3.getItem(),
      SlimefunItems.FUEL_BUCKET,
      ExpansionItemStacks.CARGO_STORAGE_UNIT_5,
      SlimefunItems.FUEL_BUCKET,
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_3.getItem(),
      SlimefunItems.FUEL_BUCKET,
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_3.getItem()
   };
   public static final ItemStack[] CARGO_STORAGE_UNIT_7 = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_3.getItem(),
      NetworkSlimefunItems.OPTIC_STAR.getItem(),
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_3.getItem(),
      NetworkSlimefunItems.OPTIC_STAR.getItem(),
      ExpansionItemStacks.CARGO_STORAGE_UNIT_6,
      NetworkSlimefunItems.OPTIC_STAR.getItem(),
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_3.getItem(),
      NetworkSlimefunItems.OPTIC_STAR.getItem(),
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_3.getItem()
   };
   public static final ItemStack[] CARGO_STORAGE_UNIT_8 = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_3.getItem(),
      NetworkSlimefunItems.RADIOACTIVE_OPTIC_STAR.getItem(),
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_3.getItem(),
      NetworkSlimefunItems.RADIOACTIVE_OPTIC_STAR.getItem(),
      ExpansionItemStacks.CARGO_STORAGE_UNIT_7,
      NetworkSlimefunItems.RADIOACTIVE_OPTIC_STAR.getItem(),
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_3.getItem(),
      NetworkSlimefunItems.RADIOACTIVE_OPTIC_STAR.getItem(),
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_3.getItem()
   };
   public static final ItemStack[] CARGO_STORAGE_UNIT_9 = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_4.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_4.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      ExpansionItemStacks.CARGO_STORAGE_UNIT_8,
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_4.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_4.getItem()
   };
   public static final ItemStack[] CARGO_STORAGE_UNIT_10 = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_4.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_4.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      ExpansionItemStacks.CARGO_STORAGE_UNIT_9,
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_4.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_4.getItem()
   };
   public static final ItemStack[] CARGO_STORAGE_UNIT_11 = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_5.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_5.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      ExpansionItemStacks.CARGO_STORAGE_UNIT_10,
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_5.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_5.getItem()
   };
   public static final ItemStack[] CARGO_STORAGE_UNIT_12 = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_6.getItem(),
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_8.getItem(),
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_6.getItem(),
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_8.getItem(),
      ExpansionItemStacks.CARGO_STORAGE_UNIT_11,
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_8.getItem(),
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_6.getItem(),
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_8.getItem(),
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_6.getItem()
   };
   public static final ItemStack[] CARGO_STORAGE_UNIT_13 = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_7.getItem(),
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_8.getItem(),
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_7.getItem(),
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_8.getItem(),
      ExpansionItemStacks.CARGO_STORAGE_UNIT_12,
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_8.getItem(),
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_7.getItem(),
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_8.getItem(),
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_7.getItem()
   };
   public static final ItemStack[] CARGO_STORAGE_UNIT_1_MODEL = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      ExpansionItemStacks.CARGO_STORAGE_UNIT_1,
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem()
   };
   public static final ItemStack[] CARGO_STORAGE_UNIT_2_MODEL = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      ExpansionItemStacks.CARGO_STORAGE_UNIT_2,
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem()
   };
   public static final ItemStack[] CARGO_STORAGE_UNIT_3_MODEL = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      ExpansionItemStacks.CARGO_STORAGE_UNIT_3,
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem()
   };
   public static final ItemStack[] CARGO_STORAGE_UNIT_4_MODEL = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      ExpansionItemStacks.CARGO_STORAGE_UNIT_4,
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem()
   };
   public static final ItemStack[] CARGO_STORAGE_UNIT_5_MODEL = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      ExpansionItemStacks.CARGO_STORAGE_UNIT_5,
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem()
   };
   public static final ItemStack[] CARGO_STORAGE_UNIT_6_MODEL = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      ExpansionItemStacks.CARGO_STORAGE_UNIT_6,
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem()
   };
   public static final ItemStack[] CARGO_STORAGE_UNIT_7_MODEL = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      ExpansionItemStacks.CARGO_STORAGE_UNIT_7,
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem()
   };
   public static final ItemStack[] CARGO_STORAGE_UNIT_8_MODEL = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      ExpansionItemStacks.CARGO_STORAGE_UNIT_8,
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem()
   };
   public static final ItemStack[] CARGO_STORAGE_UNIT_9_MODEL = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      ExpansionItemStacks.CARGO_STORAGE_UNIT_9,
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem()
   };
   public static final ItemStack[] CARGO_STORAGE_UNIT_10_MODEL = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      ExpansionItemStacks.CARGO_STORAGE_UNIT_10,
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem()
   };
   public static final ItemStack[] CARGO_STORAGE_UNIT_11_MODEL = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      ExpansionItemStacks.CARGO_STORAGE_UNIT_11,
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem()
   };
   public static final ItemStack[] CARGO_STORAGE_UNIT_12_MODEL = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      ExpansionItemStacks.CARGO_STORAGE_UNIT_12,
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem()
   };
   public static final ItemStack[] CARGO_STORAGE_UNIT_13_MODEL = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      ExpansionItemStacks.CARGO_STORAGE_UNIT_13,
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem()
   };
   public static final ItemStack[] DUE_MACHINE_CONFIGURATOR = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_0.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_0.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_CONFIGURATOR.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_0.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_0.getItem()
   };
   public static final ItemStack[] ITEM_MOVER = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_8.getItem(),
      ExpansionItemStacks.ADVANCED_IMPORT,
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_8.getItem(),
      ExpansionItemStacks.ADVANCED_EXPORT,
      NetworkSlimefunItems.NETWORK_WIRELESS_CONFIGURATOR.getItem(),
      ExpansionItemStacks.ADVANCED_EXPORT,
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_8.getItem(),
      ExpansionItemStacks.ADVANCED_IMPORT,
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_8.getItem()
   };
   public static final ItemStack[] NETWORK_BLUEPRINT_DECODER = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_RECIPE_ENCODER.getItem(),
      NetworkSlimefunItems.NETWORK_RECIPE_ENCODER.getItem(),
      NetworkSlimefunItems.NETWORK_RECIPE_ENCODER.getItem(),
      NetworkSlimefunItems.NETWORK_RECIPE_ENCODER.getItem(),
      new ItemStack(Material.DIAMOND),
      NetworkSlimefunItems.NETWORK_RECIPE_ENCODER.getItem(),
      NetworkSlimefunItems.NETWORK_RECIPE_ENCODER.getItem(),
      NetworkSlimefunItems.NETWORK_RECIPE_ENCODER.getItem(),
      NetworkSlimefunItems.NETWORK_RECIPE_ENCODER.getItem()
   };
   public static final ItemStack[] LINE_POWER_OUTLET_1 = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_STAR.getItem(),
      NetworkSlimefunItems.NETWORK_POWER_OUTLET_1.getItem(),
      NetworkSlimefunItems.OPTIC_STAR.getItem(),
      NetworkSlimefunItems.NETWORK_POWER_OUTLET_1.getItem(),
      NetworkSlimefunItems.RADIOACTIVE_OPTIC_STAR.getItem(),
      NetworkSlimefunItems.NETWORK_POWER_OUTLET_1.getItem(),
      NetworkSlimefunItems.OPTIC_STAR.getItem(),
      NetworkSlimefunItems.NETWORK_POWER_OUTLET_1.getItem(),
      NetworkSlimefunItems.OPTIC_STAR.getItem()
   };
   public static final ItemStack[] LINE_POWER_OUTLET_2 = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      ExpansionItemStacks.LINE_POWER_OUTLET_1,
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.NETWORK_CAPACITOR_1.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem()
   };
   public static final ItemStack[] LINE_POWER_OUTLET_3 = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      ExpansionItemStacks.LINE_POWER_OUTLET_2,
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.NETWORK_CAPACITOR_1.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem()
   };
   public static final ItemStack[] LINE_POWER_OUTLET_4 = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      ExpansionItemStacks.LINE_POWER_OUTLET_3,
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.NETWORK_CAPACITOR_1.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem()
   };
   public static final ItemStack[] LINE_POWER_OUTLET_5 = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      SlimefunItems.ALUMINUM_BRONZE_INGOT,
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      SlimefunItems.SYNTHETIC_SAPPHIRE,
      ExpansionItemStacks.LINE_POWER_OUTLET_4,
      SlimefunItems.SYNTHETIC_SAPPHIRE,
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      SlimefunItems.ALUMINUM_BRONZE_INGOT,
      NetworkSlimefunItems.OPTIC_GLASS.getItem()
   };
   public static final ItemStack[] LINE_POWER_OUTLET_6 = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      SlimefunItems.ALUMINUM_BRASS_INGOT,
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      SlimefunItems.SYNTHETIC_DIAMOND,
      ExpansionItemStacks.LINE_POWER_OUTLET_5,
      SlimefunItems.SYNTHETIC_DIAMOND,
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      SlimefunItems.ALUMINUM_BRASS_INGOT,
      NetworkSlimefunItems.OPTIC_GLASS.getItem()
   };
   public static final ItemStack[] LINE_POWER_OUTLET_7 = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      SlimefunItems.HARDENED_METAL_INGOT,
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      SlimefunItems.SYNTHETIC_EMERALD,
      ExpansionItemStacks.LINE_POWER_OUTLET_6,
      SlimefunItems.SYNTHETIC_EMERALD,
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      SlimefunItems.HARDENED_METAL_INGOT,
      NetworkSlimefunItems.OPTIC_GLASS.getItem()
   };
   public static final ItemStack[] LINE_POWER_OUTLET_8 = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      SlimefunItems.REINFORCED_ALLOY_INGOT,
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      SlimefunItems.POWER_CRYSTAL,
      ExpansionItemStacks.LINE_POWER_OUTLET_7,
      SlimefunItems.POWER_CRYSTAL,
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      SlimefunItems.REINFORCED_ALLOY_INGOT,
      NetworkSlimefunItems.OPTIC_GLASS.getItem()
   };
   public static final ItemStack[] LINE_POWER_OUTLET_9 = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      SlimefunItems.CARGO_MOTOR,
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      SlimefunItems.BLISTERING_INGOT_3,
      ExpansionItemStacks.LINE_POWER_OUTLET_8,
      SlimefunItems.BLISTERING_INGOT_3,
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      SlimefunItems.CARGO_MOTOR,
      NetworkSlimefunItems.OPTIC_GLASS.getItem()
   };
   public static final ItemStack[] LINE_POWER_OUTLET_10 = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      SlimefunItems.CARGO_CONNECTOR_NODE,
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      SlimefunItems.BLISTERING_INGOT_3,
      ExpansionItemStacks.LINE_POWER_OUTLET_9,
      SlimefunItems.BLISTERING_INGOT_3,
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      SlimefunItems.CARGO_CONNECTOR_NODE,
      NetworkSlimefunItems.OPTIC_GLASS.getItem()
   };
   public static final ItemStack[] LINE_POWER_OUTLET_11 = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      SlimefunItems.CARGO_MANAGER,
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      SlimefunItems.BLISTERING_INGOT_3,
      ExpansionItemStacks.LINE_POWER_OUTLET_10,
      SlimefunItems.BLISTERING_INGOT_3,
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      SlimefunItems.CARGO_MANAGER,
      NetworkSlimefunItems.OPTIC_GLASS.getItem()
   };
   public static final ItemStack[] DUE_MACHINE = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_PUSHER.getItem(),
      NetworkSlimefunItems.NETWORK_GRABBER.getItem(),
      NetworkSlimefunItems.NETWORK_PUSHER.getItem(),
      NetworkSlimefunItems.NETWORK_GRABBER.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_GRABBER.getItem(),
      NetworkSlimefunItems.NETWORK_PUSHER.getItem(),
      NetworkSlimefunItems.NETWORK_GRABBER.getItem(),
      NetworkSlimefunItems.NETWORK_PUSHER.getItem()
   };
   public static final ItemStack[] OFFSETTER = new ItemStack[]{
      null,
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      null,
      HOPPER,
      NetworkSlimefunItems.NETWORK_MONITOR.getItem(),
      HOPPER,
      null,
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      null
   };
   @Deprecated
   public static final ItemStack[] BETTER_GRABBER = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_STAR.getItem(),
      NetworkSlimefunItems.NETWORK_PUSHER.getItem(),
      NetworkSlimefunItems.OPTIC_STAR.getItem(),
      NetworkSlimefunItems.NETWORK_PUSHER.getItem(),
      NetworkSlimefunItems.NETWORK_GRABBER.getItem(),
      NetworkSlimefunItems.NETWORK_PUSHER.getItem(),
      NetworkSlimefunItems.OPTIC_STAR.getItem(),
      NetworkSlimefunItems.NETWORK_PUSHER.getItem(),
      NetworkSlimefunItems.OPTIC_STAR.getItem()
   };
   public static final ItemStack[] NETWORK_CRAFTING_GRID_NEW_STYLE = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_CRAFTING_GRID.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      ExpansionItemStacks.NETWORK_GRID_NEW_STYLE,
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_CRAFTING_GRID.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem()
   };
   public static final ItemStack[] STATUS_VIEWER = new ItemStack[]{
      NetworkSlimefunItems.SYNTHETIC_EMERALD_SHARD.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.SYNTHETIC_EMERALD_SHARD.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.SYNTHETIC_EMERALD_SHARD.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.SYNTHETIC_EMERALD_SHARD.getItem()
   };
   public static final ItemStack[] QUANTUM_MANAGER = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_0.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_0.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_0.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem()
   };
   public static final ItemStack[] DRAWER_MANAGER = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      ExpansionItemStacks.CARGO_STORAGE_UNIT_1,
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      ExpansionItemStacks.CARGO_STORAGE_UNIT_1,
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      ExpansionItemStacks.CARGO_STORAGE_UNIT_1,
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem()
   };
   public static final ItemStack[] ITEM_FLOW_VIEWER = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_GRID.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_GRID.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_GRID.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem()
   };
   public static final ItemStack[] ADVANCED_VACUUM = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.RADIOACTIVE_OPTIC_STAR.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_VACUUM.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.RADIOACTIVE_OPTIC_STAR.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem()
   };
   public static final ItemStack[] CRAFTER_MANAGER = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_AUTO_CRAFTER.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_AUTO_CRAFTER.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_AUTO_CRAFTER.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem()
   };
   public static final ItemStack[] SWITCHING_MONITOR = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_MONITOR.getItem(),
      NetworkSlimefunItems.NETWORK_MONITOR.getItem(),
      NetworkSlimefunItems.NETWORK_MONITOR.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_GRID.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_MONITOR.getItem(),
      NetworkSlimefunItems.NETWORK_MONITOR.getItem(),
      NetworkSlimefunItems.NETWORK_MONITOR.getItem()
   };
   public static final ItemStack[] HANGING_GRID_NEW_STYLE = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      ExpansionItemStacks.NETWORK_GRID_NEW_STYLE,
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      ExpansionItemStacks.NETWORK_GRID_NEW_STYLE,
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      ExpansionItemStacks.NETWORK_GRID_NEW_STYLE,
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem()
   };
   public static final ItemStack[] WHITELISTED_TRANSFER_GRABBER = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_STAR.getItem(),
      NetworkSlimefunItems.NETWORK_PUSHER.getItem(),
      NetworkSlimefunItems.OPTIC_STAR.getItem(),
      NetworkSlimefunItems.NETWORK_PUSHER.getItem(),
      NetworkSlimefunItems.NETWORK_GRABBER.getItem(),
      NetworkSlimefunItems.NETWORK_PUSHER.getItem(),
      NetworkSlimefunItems.OPTIC_STAR.getItem(),
      NetworkSlimefunItems.NETWORK_PUSHER.getItem(),
      NetworkSlimefunItems.OPTIC_STAR.getItem()
   };
   public static final ItemStack[] WHITELISTED_LINE_TRANSFER_GRABBER = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_STAR.getItem(),
      ExpansionItemStacks.WHITELISTED_TRANSFER_GRABBER,
      NetworkSlimefunItems.OPTIC_STAR.getItem(),
      ExpansionItemStacks.WHITELISTED_TRANSFER_GRABBER,
      NetworkSlimefunItems.NETWORK_GRABBER.getItem(),
      ExpansionItemStacks.WHITELISTED_TRANSFER_GRABBER,
      NetworkSlimefunItems.OPTIC_STAR.getItem(),
      ExpansionItemStacks.WHITELISTED_TRANSFER_GRABBER,
      NetworkSlimefunItems.OPTIC_STAR.getItem()
   };
   public static final ItemStack[] WHITELISTED_TRANSFER_VANILLA_GRABBER = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      new ItemStack(Material.HOPPER),
      ExpansionItemStacks.WHITELISTED_TRANSFER_GRABBER,
      new ItemStack(Material.HOPPER),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem()
   };
   public static final ItemStack[] WHITELISTED_LINE_TRANSFER_VANILLA_GRABBER = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_STAR.getItem(),
      ExpansionItemStacks.WHITELISTED_TRANSFER_VANILLA_GRABBER,
      NetworkSlimefunItems.OPTIC_STAR.getItem(),
      ExpansionItemStacks.WHITELISTED_TRANSFER_VANILLA_GRABBER,
      NetworkSlimefunItems.NETWORK_GRABBER.getItem(),
      ExpansionItemStacks.WHITELISTED_TRANSFER_VANILLA_GRABBER,
      NetworkSlimefunItems.OPTIC_STAR.getItem(),
      ExpansionItemStacks.WHITELISTED_TRANSFER_VANILLA_GRABBER,
      NetworkSlimefunItems.OPTIC_STAR.getItem()
   };
   public static final ItemStack[] SMART_NETWORK_CRAFTING_GRID_NEW_STYLE = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      ExpansionItemStacks.NETWORK_CRAFTING_GRID_NEW_STYLE,
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem()
   };
   public static final ItemStack[] ADVANCED_WIRELESS_TRANSMITTER = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_WIRELESS_TRANSMITTER.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_WIRELESS_RECEIVER.getItem(),
      NetworkSlimefunItems.NETWORK_WIRELESS_TRANSMITTER.getItem(),
      NetworkSlimefunItems.NETWORK_WIRELESS_RECEIVER.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_WIRELESS_TRANSMITTER.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem()
   };
   public static final ItemStack[] ADVANCED_LINE_TRANSFER_VANILLA_GRABBER = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_STAR.getItem(),
      ExpansionItemStacks.LINE_TRANSFER_VANILLA_GRABBER,
      NetworkSlimefunItems.OPTIC_STAR.getItem(),
      ExpansionItemStacks.LINE_TRANSFER_VANILLA_GRABBER,
      NetworkSlimefunItems.NETWORK_GRABBER.getItem(),
      ExpansionItemStacks.LINE_TRANSFER_VANILLA_GRABBER,
      NetworkSlimefunItems.OPTIC_STAR.getItem(),
      ExpansionItemStacks.LINE_TRANSFER_VANILLA_GRABBER,
      NetworkSlimefunItems.OPTIC_STAR.getItem()
   };
   public static final ItemStack[] ADVANCED_LINE_TRANSFER_VANILLA_PUSHER = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_STAR.getItem(),
      ExpansionItemStacks.LINE_TRANSFER_VANILLA_PUSHER,
      NetworkSlimefunItems.OPTIC_STAR.getItem(),
      ExpansionItemStacks.LINE_TRANSFER_VANILLA_PUSHER,
      NetworkSlimefunItems.NETWORK_PUSHER.getItem(),
      ExpansionItemStacks.LINE_TRANSFER_VANILLA_PUSHER,
      NetworkSlimefunItems.OPTIC_STAR.getItem(),
      ExpansionItemStacks.LINE_TRANSFER_VANILLA_PUSHER,
      NetworkSlimefunItems.OPTIC_STAR.getItem()
   };
   public static final ItemStack[] ADVANCED_LINE_TRANSFER_VANILLA_PLUS_GRABBER = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_STAR.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_STAR.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      ExpansionItemStacks.ADVANCED_LINE_TRANSFER_VANILLA_GRABBER,
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_STAR.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_STAR.getItem()
   };
   public static final ItemStack[] ADVANCED_LINE_TRANSFER_VANILLA_PLUS_PUSHER = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_STAR.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_STAR.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      ExpansionItemStacks.ADVANCED_LINE_TRANSFER_VANILLA_PUSHER,
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_STAR.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_STAR.getItem()
   };
   public static final ItemStack[] ITEM_DIFFERENTER = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem()
   };
   public static final ItemStack[] STORAGE_CARD_CONVERTER = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      ExpansionItemStacks.NETWORKS_EXPANSION_WORKBENCH,
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      ExpansionItemStacks.NETWORKS_EXPANSION_WORKBENCH,
      ExpansionItemStacks.NETWORKS_EXPANSION_WORKBENCH,
      ExpansionItemStacks.NETWORKS_EXPANSION_WORKBENCH,
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      ExpansionItemStacks.NETWORKS_EXPANSION_WORKBENCH,
      NetworkSlimefunItems.OPTIC_GLASS.getItem()
   };
   public static final ItemStack[] FACING_PRESETTER = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_CONFIGURATOR.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_CONFIGURATOR.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem()
   };
   public static final ItemStack[] SUPER_TRASH = new ItemStack[]{
      SlimefunItems.PORTABLE_DUSTBIN,
      SlimefunItems.PORTABLE_DUSTBIN,
      SlimefunItems.PORTABLE_DUSTBIN,
      SlimefunItems.PORTABLE_DUSTBIN,
      SlimefunItems.PORTABLE_DUSTBIN,
      SlimefunItems.PORTABLE_DUSTBIN,
      SlimefunItems.PORTABLE_DUSTBIN,
      SlimefunItems.PORTABLE_DUSTBIN,
      SlimefunItems.PORTABLE_DUSTBIN
   };
   public static final ItemStack[] AUTHOR_TINALNESS = new ItemStack[]{
      NetworkSlimefunItems.SYNTHETIC_EMERALD_SHARD.getItem(),
      NetworkSlimefunItems.NETWORK_PROBE.getItem(),
      NetworkSlimefunItems.SYNTHETIC_EMERALD_SHARD.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.SIMPLE_NANOBOTS.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.SYNTHETIC_EMERALD_SHARD.getItem(),
      ExpansionItemStacks.STATUS_VIEWER,
      NetworkSlimefunItems.SYNTHETIC_EMERALD_SHARD.getItem()
   };
   public static final ItemStack[] CELL_DRIVE = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_CAPACITOR_2.getItem(),
      NetworkSlimefunItems.NETWORK_CAPACITOR_2.getItem(),
      NetworkSlimefunItems.NETWORK_CAPACITOR_2.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      CellTier.T3.stack(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_CAPACITOR_2.getItem(),
      NetworkSlimefunItems.NETWORK_CAPACITOR_2.getItem(),
      NetworkSlimefunItems.NETWORK_CAPACITOR_2.getItem()
   };
   public static final ItemStack[] CELL_CLEANER = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      ExpansionItemStacks.CELL_DRIVE,
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      CellTier.T2.stack(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem()
   };
   public static final ItemStack[] CELL_CONVERTER = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      ExpansionItemStacks.CELL_CLEANER,
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_0.getItem(),
      NetworkSlimefunItems.NETWORK_BRIDGE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem()
   };
   public static final ItemStack ENDER_PEARL = new ItemStack(Material.ENDER_PEARL);
   public static final ItemStack WITHER_ROSE = new ItemStack(Material.WITHER_ROSE);
   public static final ItemStack[] ASSEMBLY_DRIVE = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      ExpansionItemStacks.NETWORK_BRIDGE_ORDINAL,
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      ExpansionItemStacks.CELL_DRIVE,
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      ExpansionItemStacks.NETWORK_BRIDGE_ORDINAL,
      NetworkSlimefunItems.OPTIC_CABLE.getItem()
   };
   public static final ItemStack[] VOID_CELL = new ItemStack[]{null, WITHER_ROSE, null, WITHER_ROSE, CellTier.T1.stack(), WITHER_ROSE, null, WITHER_ROSE, null};
   public static final ItemStack[] ENDER_DRIVE = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      ENDER_PEARL,
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      ENDER_PEARL,
      ExpansionItemStacks.CELL_DRIVE,
      ENDER_PEARL,
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      ENDER_PEARL,
      NetworkSlimefunItems.OPTIC_GLASS.getItem()
   };
   public static final ItemStack PAPER_STACK = new ItemStack(Material.PAPER);
   public static final ItemStack INK_SAC = new ItemStack(Material.INK_SAC);
   public static final ItemStack[] ASSEMBLY_CARD = new ItemStack[]{
      PAPER_STACK, PAPER_STACK, PAPER_STACK, INK_SAC, ENDER_PEARL, INK_SAC, PAPER_STACK, PAPER_STACK, PAPER_STACK
   };
   public static final ItemStack[] ASSEMBLY_WORKSHOP = new ItemStack[]{
      null,
      ExpansionItemStacks.ASSEMBLY_CARD,
      null,
      ExpansionItemStacks.ASSEMBLY_CARD,
      NetworkSlimefunItems.NETWORK_RECIPE_ENCODER.getItem(),
      ExpansionItemStacks.ASSEMBLY_CARD,
      null,
      ExpansionItemStacks.ASSEMBLY_CARD,
      null
   };
   public static final ItemStack[] CONTAINER_FILLER = new ItemStack[]{
      null,
      ExpansionItemStacks.ASSEMBLY_CARD,
      null,
      ExpansionItemStacks.ASSEMBLY_CARD,
      new ItemStack(Material.RESPAWN_ANCHOR),
      ExpansionItemStacks.ASSEMBLY_CARD,
      null,
      ExpansionItemStacks.ASSEMBLY_CARD,
      null
   };
   public static final ItemStack[] CHANNEL_CONFIGURATOR = new ItemStack[]{null, null, null, null, PAPER_STACK, null, null, ENDER_PEARL, null};
   public static final ItemStack[] OVERCLOCK_CORE = new ItemStack[]{
      null,
      new ItemStack(Material.REDSTONE),
      null,
      new ItemStack(Material.REDSTONE),
      new ItemStack(Material.GOLD_INGOT),
      new ItemStack(Material.REDSTONE),
      null,
      ENDER_PEARL,
      null
   };
   public static final ItemStack[] SMART_CORE = new ItemStack[]{
      null,
      new ItemStack(Material.ECHO_SHARD),
      null,
      new ItemStack(Material.ECHO_SHARD),
      new ItemStack(Material.GOLD_INGOT),
      new ItemStack(Material.ECHO_SHARD),
      null,
      ENDER_PEARL,
      null
   };
   public static final ItemStack[] ASSEMBLY_MONITOR = new ItemStack[]{
      new ItemStack(Material.IRON_INGOT),
      new ItemStack(Material.SPYGLASS),
      new ItemStack(Material.IRON_INGOT),
      new ItemStack(Material.IRON_INGOT),
      new ItemStack(Material.REDSTONE),
      new ItemStack(Material.IRON_INGOT),
      new ItemStack(Material.IRON_INGOT),
      new ItemStack(Material.IRON_INGOT),
      new ItemStack(Material.IRON_INGOT)
   };
   public static final ItemStack[] DRIVE_MONITOR = new ItemStack[]{
      new ItemStack(Material.IRON_INGOT),
      new ItemStack(Material.ENDER_EYE),
      new ItemStack(Material.IRON_INGOT),
      new ItemStack(Material.IRON_INGOT),
      new ItemStack(Material.REDSTONE),
      new ItemStack(Material.IRON_INGOT),
      new ItemStack(Material.IRON_INGOT),
      new ItemStack(Material.IRON_INGOT),
      new ItemStack(Material.IRON_INGOT)
   };
   public static final ItemStack[] CHAIN_GRABBER = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_GRABBER.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.NETWORK_GRABBER.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.NETWORK_GRABBER.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.NETWORK_GRABBER.getItem()
   };
   public static final ItemStack[] CHAIN_PUSHER = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_PUSHER.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.NETWORK_PUSHER.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.NETWORK_PUSHER.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.NETWORK_PUSHER.getItem()
   };
   public static final ItemStack[] CHAIN_TRANSCEIVER = new ItemStack[]{
      NetworkSlimefunItems.NETWORK_GRABBER.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.NETWORK_PUSHER.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.NETWORK_PUSHER.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.NETWORK_GRABBER.getItem()
   };
   public static final ItemStack[] CHAIN_MODULE_RANGE = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.NETWORK_MONITOR.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem()
   };
   public static final ItemStack[] CHAIN_MODULE_CAPACITY = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.NETWORK_MORE_PUSHER.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem()
   };
   public static final ItemStack[] CHAIN_MODULE_MODE = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.NETWORK_CONFIGURATOR.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem()
   };
   public static final ItemStack[] CHAIN_MODULE_VANILLA = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.NETWORK_VANILLA_GRABBER.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem()
   };
   public static final ItemStack[] CHAIN_MODULE_MULTI_DIRECTION = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.NETWORK_WIRELESS_TRANSMITTER.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem()
   };
   public static final ItemStack[] CHAIN_MODULE_BINDING = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.NETWORK_IMPORT.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem()
   };
   public static final ItemStack[] CHAIN_CONFIGURATOR = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.NETWORK_WIRELESS_CONFIGURATOR.getItem(),
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      ExpansionItemStacks.CHAIN_MODULE_BINDING,
      NetworkSlimefunItems.OPTIC_GLASS.getItem()
   };
   public static final ItemStack[] CHAIN_BRUSH = new ItemStack[]{
      null,
      NetworkSlimefunItems.OPTIC_CABLE.getItem(),
      null,
      null,
      NetworkSlimefunItems.NETWORK_WIRELESS_CONFIGURATOR.getItem(),
      null,
      null,
      ExpansionItemStacks.CHAIN_MODULE_BINDING,
      null
   };
   public static final ItemStack[] VISUAL_GRID = new ItemStack[]{
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.NETWORK_GRID.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem(),
      NetworkSlimefunItems.OPTIC_GLASS.getItem()
   };
}
