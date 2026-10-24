package com.ytdd9527.networksexpansion.core.items.machines;

import com.balugaq.netex.api.algorithm.Calculator;
import com.balugaq.netex.api.enums.TransportMode;
import com.balugaq.netex.api.helpers.Icon;
import com.balugaq.netex.utils.Lang;
import com.balugaq.netex.utils.NetworksVersionedEnchantment;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.GhostItems;
import com.ytdd9527.networksexpansion.utils.TextUtil;
import com.ytdd9527.networksexpansion.utils.itemstacks.ItemStackUtil;
import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.network.NodeType;
import io.github.sefiraat.networks.slimefun.network.NetworkDirectional;
import io.github.sefiraat.networks.utils.NetworkUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemHandler;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.common.ChatColors;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.utils.ChatUtils;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import net.guizhanss.guizhanlib.minecraft.helper.MaterialHelper;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public abstract class AdvancedDirectional extends NetworkDirectional {
   @NotNull
   public static final TransportMode DEFAULT_TRANSPORT_MODE = TransportMode.NONE;
   protected static final String DIRECTION = "direction";
   protected static final String OWNER_KEY = "uuid";
   protected static final String LIMIT_KEY = "transport_limit";
   protected static final String TRANSPORT_MODE_KEY = "transport_mode";
   private static final int NORTH_SLOT = 12;
   private static final int SOUTH_SLOT = 30;
   private static final int EAST_SLOT = 22;
   private static final int WEST_SLOT = 20;
   private static final int UP_SLOT = 15;
   private static final int DOWN_SLOT = 33;
   private static final Set<BlockFace> VALID_FACES = EnumSet.of(BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST);
   private static final Map<Location, BlockFace> SELECTED_DIRECTION_MAP = new HashMap<>();
   protected static final Map<Location, Integer> NETWORK_LIMIT_QUANTITY_MAP = new HashMap<>();
   private static final Map<Location, TransportMode> NETWORK_TRANSPORT_MODE_MAP = new HashMap<>();
   final NetworkDirectional instance = this;

   protected AdvancedDirectional(
      @NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe, NodeType type
   ) {
      this(itemGroup, item, recipeType, recipe, 1, type);
   }

   protected AdvancedDirectional(
      @NotNull ItemGroup itemGroup,
      @NotNull SlimefunItemStack item,
      @NotNull RecipeType recipeType,
      @NotNull ItemStack[] recipe,
      @Range(from = 1L,to = 64L) int outputAmount,
      NodeType type
   ) {
      super(itemGroup, item, recipeType, recipe, type);
      this.addItemHandler(new ItemHandler[]{new BlockTicker() {
         public boolean isSynchronized() {
            return AdvancedDirectional.this.runSync();
         }

         public void tick(@NotNull Block block, SlimefunItem slimefunItem, @NotNull SlimefunBlockData data) {
            AdvancedDirectional.this.onTick(data.getBlockMenu(), block);
         }

         public void uniqueTick() {
            AdvancedDirectional.this.onUniqueTick();
         }
      }});
   }

   @NotNull
   public static ItemStack getDirectionalSlotPane(@NotNull BlockFace blockFace, @NotNull SlimefunItem slimefunItem, boolean active) {
      ItemStack displayStack = ItemStackUtil.getCleanItem(
         new CustomItemStack(
            slimefunItem.getItem(),
            String.format(
               Lang.getString("messages.normal-operation.directional.display_name"), blockFace.name(), TextUtil.stripColor(slimefunItem.getItemName())
            ),
            new String[0]
         )
      );
      ItemMeta itemMeta = displayStack.getItemMeta();
      itemMeta.setLore(Lang.getStringList("messages.normal-operation.directional.display_lore"));
      if (active) {
         List<String> lore = itemMeta.getLore();
         if (lore == null) {
            lore = new ArrayList<>();
         }

         lore.add(Lang.getString("messages.normal-operation.directional.set_facing"));
         itemMeta.setLore(lore);
         itemMeta.addEnchant(NetworksVersionedEnchantment.LUCK_OF_THE_SEA, 1, true);
         itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
      }

      displayStack.setItemMeta(itemMeta);
      GhostItems.mark(displayStack);
      return displayStack;
   }

   @NotNull
   public static ItemStack getDirectionalSlotPane(@NotNull BlockFace blockFace, @NotNull Material blockMaterial, boolean active) {
      if (blockMaterial.isItem() && blockMaterial != Material.AIR) {
         ItemStack displayStack = new CustomItemStack(
            blockMaterial,
            String.format(Lang.getString("messages.normal-operation.directional.display_name"), blockFace.name(), MaterialHelper.getName(blockMaterial)),
            new String[0]
         );
         ItemMeta itemMeta = displayStack.getItemMeta();
         itemMeta.setLore(Lang.getStringList("messages.normal-operation.directional.display_lore"));
         if (active) {
            List<String> lore = itemMeta.getLore();
            if (lore == null) {
               lore = new ArrayList<>();
            }

            lore.add(Lang.getString("messages.normal-operation.directional.set_facing"));
            itemMeta.setLore(lore);
            itemMeta.addEnchant(NetworksVersionedEnchantment.LUCK_OF_THE_SEA, 1, true);
            itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
         }

         displayStack.setItemMeta(itemMeta);
         GhostItems.mark(displayStack);
         return displayStack;
      } else {
         Material material = active ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
         ItemStack emptyPane = ItemStackUtil.getCleanItem(
            new CustomItemStack(material, String.format(Lang.getString("messages.normal-operation.directional.display_empty"), blockFace), new String[0])
         );
         GhostItems.mark(emptyPane);
         return emptyPane;
      }
   }

   @Nullable
   public static BlockFace getSelectedFace(@NotNull Location location) {
      return SELECTED_DIRECTION_MAP.get(location);
   }

   @Override
   public void updateGui(@Nullable BlockMenu blockMenu) {
      if (blockMenu != null && blockMenu.hasViewer()) {
         BlockFace direction = this.getCurrentDirection(blockMenu);

         for (BlockFace blockFace : VALID_FACES) {
            Block block = blockMenu.getBlock().getRelative(blockFace);
            SlimefunItem slimefunItem = StorageCacheUtils.getSfItem(block.getLocation());
            if (slimefunItem != null) {
               switch (blockFace) {
                  case NORTH:
                     blockMenu.replaceExistingItem(this.getNorthSlot(), getDirectionalSlotPane(blockFace, slimefunItem, blockFace == direction));
                     break;
                  case SOUTH:
                     blockMenu.replaceExistingItem(this.getSouthSlot(), getDirectionalSlotPane(blockFace, slimefunItem, blockFace == direction));
                     break;
                  case EAST:
                     blockMenu.replaceExistingItem(this.getEastSlot(), getDirectionalSlotPane(blockFace, slimefunItem, blockFace == direction));
                     break;
                  case WEST:
                     blockMenu.replaceExistingItem(this.getWestSlot(), getDirectionalSlotPane(blockFace, slimefunItem, blockFace == direction));
                     break;
                  case UP:
                     blockMenu.replaceExistingItem(this.getUpSlot(), getDirectionalSlotPane(blockFace, slimefunItem, blockFace == direction));
                     break;
                  case DOWN:
                     blockMenu.replaceExistingItem(this.getDownSlot(), getDirectionalSlotPane(blockFace, slimefunItem, blockFace == direction));
                     break;
                  default:
                     throw new IllegalStateException(String.format(Lang.getString("messages.unsupported-operation.directional.unexcepted_value"), blockFace));
               }
            } else {
               Material material = block.getType();
               switch (blockFace) {
                  case NORTH:
                     blockMenu.replaceExistingItem(this.getNorthSlot(), getDirectionalSlotPane(blockFace, material, blockFace == direction));
                     break;
                  case SOUTH:
                     blockMenu.replaceExistingItem(this.getSouthSlot(), getDirectionalSlotPane(blockFace, material, blockFace == direction));
                     break;
                  case EAST:
                     blockMenu.replaceExistingItem(this.getEastSlot(), getDirectionalSlotPane(blockFace, material, blockFace == direction));
                     break;
                  case WEST:
                     blockMenu.replaceExistingItem(this.getWestSlot(), getDirectionalSlotPane(blockFace, material, blockFace == direction));
                     break;
                  case UP:
                     blockMenu.replaceExistingItem(this.getUpSlot(), getDirectionalSlotPane(blockFace, material, blockFace == direction));
                     break;
                  case DOWN:
                     blockMenu.replaceExistingItem(this.getDownSlot(), getDirectionalSlotPane(blockFace, material, blockFace == direction));
                     break;
                  default:
                     throw new IllegalStateException(String.format(Lang.getString("messages.unsupported-operation.directional.unexcepted_value"), blockFace));
               }
            }
         }
      }
   }

   @NotNull
   @Override
   protected BlockFace getCurrentDirection(@NotNull BlockMenu blockMenu) {
      BlockFace direction = SELECTED_DIRECTION_MAP.get(blockMenu.getLocation().clone());
      if (direction == null) {
         direction = BlockFace.valueOf(StorageCacheUtils.getData(blockMenu.getLocation(), "direction"));
         SELECTED_DIRECTION_MAP.put(blockMenu.getLocation().clone(), direction);
      }

      return direction;
   }

   @Override
   protected void onUniqueTick() {
      super.onUniqueTick();
   }

   @Override
   public void onPlace(@NotNull BlockPlaceEvent event) {
      NetworkStorage.removeNode(event.getBlock().getLocation());
      SlimefunBlockData blockData = StorageCacheUtils.getBlock(event.getBlock().getLocation());
      if (blockData != null) {
         blockData.setData("uuid", event.getPlayer().getUniqueId().toString());
         blockData.setData("direction", BlockFace.SELF.name());
         blockData.setData("transport_limit", String.valueOf(this.getMaxLimit()));
         blockData.setData("transport_mode", String.valueOf(DEFAULT_TRANSPORT_MODE));
         BlockMenu menu = blockData.getBlockMenu();
         if (menu != null) {
            NetworkUtils.applyConfig(this.instance, menu, event.getPlayer());
         }
      }
   }

   @Override
   public void postRegister() {
      new BlockMenuPreset(this.getId(), this.getItemName()) {
         public void init() {
            this.drawBackground(AdvancedDirectional.this.getBackgroundSlots());
            if (AdvancedDirectional.this.getOtherBackgroundSlots() != null && AdvancedDirectional.this.getOtherBackgroundStack() != null) {
               this.drawBackground(AdvancedDirectional.this.getOtherBackgroundStack(), AdvancedDirectional.this.getOtherBackgroundSlots());
            }

            this.addItem(
               AdvancedDirectional.this.getNorthSlot(),
               AdvancedDirectional.getDirectionalSlotPane(BlockFace.NORTH, Material.AIR, false),
               (player, i, itemStack, clickAction) -> false
            );
            this.addItem(
               AdvancedDirectional.this.getSouthSlot(),
               AdvancedDirectional.getDirectionalSlotPane(BlockFace.SOUTH, Material.AIR, false),
               (player, i, itemStack, clickAction) -> false
            );
            this.addItem(
               AdvancedDirectional.this.getEastSlot(),
               AdvancedDirectional.getDirectionalSlotPane(BlockFace.EAST, Material.AIR, false),
               (player, i, itemStack, clickAction) -> false
            );
            this.addItem(
               AdvancedDirectional.this.getWestSlot(),
               AdvancedDirectional.getDirectionalSlotPane(BlockFace.WEST, Material.AIR, false),
               (player, i, itemStack, clickAction) -> false
            );
            this.addItem(
               AdvancedDirectional.this.getUpSlot(),
               AdvancedDirectional.getDirectionalSlotPane(BlockFace.UP, Material.AIR, false),
               (player, i, itemStack, clickAction) -> false
            );
            this.addItem(
               AdvancedDirectional.this.getDownSlot(),
               AdvancedDirectional.getDirectionalSlotPane(BlockFace.DOWN, Material.AIR, false),
               (player, i, itemStack, clickAction) -> false
            );
            if (AdvancedDirectional.this.getAddSlot() != -1) {
               this.addItem(AdvancedDirectional.this.getAddSlot(), AdvancedDirectional.this.getAddIcon(), (p, i, itemStack, clickAction) -> false);
            }

            if (AdvancedDirectional.this.getMinusSlot() != -1) {
               this.addItem(AdvancedDirectional.this.getMinusSlot(), AdvancedDirectional.this.getMinusIcon(), (p, i, itemStack, clickAction) -> false);
            }

            if (AdvancedDirectional.this.getCargoNumberSlot() != -1) {
               this.addItem(
                  AdvancedDirectional.this.getCargoNumberSlot(), AdvancedDirectional.this.getCargoNumberIcon(), (p, i, itemStack, clickAction) -> false
               );
            }

            if (AdvancedDirectional.this.getTransportModeSlot() != -1) {
               this.addItem(
                  AdvancedDirectional.this.getTransportModeSlot(), AdvancedDirectional.this.getTransportModeIcon(), (p, i, itemStack, clickAction) -> false
               );
            }
         }

         public void newInstance(@NotNull BlockMenu blockMenu, @NotNull Block b) {
            Location location = blockMenu.getLocation();
            String string = StorageCacheUtils.getData(location, "direction");
            String rawLimit = StorageCacheUtils.getData(location, "transport_limit");
            String rawMode = StorageCacheUtils.getData(location, "transport_mode");
            BlockFace direction;
            if (string == null) {
               direction = BlockFace.SELF;
               StorageCacheUtils.setData(location, "direction", BlockFace.SELF.name());
            } else {
               direction = BlockFace.valueOf(string);
            }

            AdvancedDirectional.SELECTED_DIRECTION_MAP.put(location.clone(), direction);
            int limit;
            if (rawLimit == null) {
               limit = AdvancedDirectional.this.getMaxLimit();
            } else {
               limit = Integer.parseInt(rawLimit);
            }

            AdvancedDirectional.NETWORK_LIMIT_QUANTITY_MAP.put(location.clone(), limit);
            TransportMode mode;
            if (rawMode == null) {
               mode = AdvancedDirectional.DEFAULT_TRANSPORT_MODE;
               StorageCacheUtils.setData(location, "transport_mode", String.valueOf(mode));
            } else {
               mode = TransportMode.valueOf(rawMode);
            }

            AdvancedDirectional.NETWORK_TRANSPORT_MODE_MAP.put(location.clone(), mode);
            blockMenu.addMenuClickHandler(
               AdvancedDirectional.this.getNorthSlot(),
               (player, i, itemStack, clickAction) -> AdvancedDirectional.this.directionClick(player, clickAction, blockMenu, BlockFace.NORTH)
            );
            blockMenu.addMenuClickHandler(
               AdvancedDirectional.this.getSouthSlot(),
               (player, i, itemStack, clickAction) -> AdvancedDirectional.this.directionClick(player, clickAction, blockMenu, BlockFace.SOUTH)
            );
            blockMenu.addMenuClickHandler(
               AdvancedDirectional.this.getEastSlot(),
               (player, i, itemStack, clickAction) -> AdvancedDirectional.this.directionClick(player, clickAction, blockMenu, BlockFace.EAST)
            );
            blockMenu.addMenuClickHandler(
               AdvancedDirectional.this.getWestSlot(),
               (player, i, itemStack, clickAction) -> AdvancedDirectional.this.directionClick(player, clickAction, blockMenu, BlockFace.WEST)
            );
            blockMenu.addMenuClickHandler(
               AdvancedDirectional.this.getUpSlot(),
               (player, i, itemStack, clickAction) -> AdvancedDirectional.this.directionClick(player, clickAction, blockMenu, BlockFace.UP)
            );
            blockMenu.addMenuClickHandler(
               AdvancedDirectional.this.getDownSlot(),
               (player, i, itemStack, clickAction) -> AdvancedDirectional.this.directionClick(player, clickAction, blockMenu, BlockFace.DOWN)
            );
            if (AdvancedDirectional.this.getCargoNumberSlot() != -1) {
               blockMenu.addMenuClickHandler(AdvancedDirectional.this.getCargoNumberSlot(), (player, i, itemStack, clickAction) -> {
                  player.sendMessage(Lang.getString("messages.guide.quantity-input-prompt"));
                  ChatUtils.awaitInput(player, input -> {
                     try {
                        int value = Calculator.calculate(input).intValue();
                        if (value <= 0 || value > AdvancedDirectional.this.getMaxLimit()) {
                           player.sendMessage(String.format(Lang.getString("messages.guide.invalid-amount-input"), AdvancedDirectional.this.getMaxLimit()));
                           BlockMenu menu = StorageCacheUtils.getMenu(location);
                           if (menu != null) {
                              menu.open(new Player[]{player});
                           }

                           return;
                        }

                        AdvancedDirectional.this.setLimitQuantity(location, value);
                        AdvancedDirectional.this.updateShowIcon(location);
                        BlockMenu menu = StorageCacheUtils.getMenu(location);
                        if (menu != null) {
                           menu.open(new Player[]{player});
                        }
                     } catch (NumberFormatException var6x) {
                        player.sendMessage(var6x.getMessage());
                     }
                  });
                  return false;
               });
            }

            if (AdvancedDirectional.this.getAddSlot() != -1) {
               blockMenu.addMenuClickHandler(
                  AdvancedDirectional.this.getAddSlot(), (p, slot, item, action) -> AdvancedDirectional.this.addClick(location, action)
               );
            }

            if (AdvancedDirectional.this.getMinusSlot() != -1) {
               blockMenu.addMenuClickHandler(
                  AdvancedDirectional.this.getMinusSlot(), (p, slot, item, action) -> AdvancedDirectional.this.minusClick(location, action)
               );
            }

            if (AdvancedDirectional.this.getTransportModeSlot() != -1) {
               blockMenu.addMenuClickHandler(
                  AdvancedDirectional.this.getTransportModeSlot(), (p, slot, item, action) -> AdvancedDirectional.this.toggleTransportMode(location, action)
               );
            }

            AdvancedDirectional.this.updateShowIcon(location);
            AdvancedDirectional.this.updateTransportModeIcon(location);
         }

         public boolean canOpen(@NotNull Block block, @NotNull Player player) {
            return player.hasPermission("slimefun.inventory.bypass")
               || this.getSlimefunItem().canUse(player, false)
                  && Slimefun.getProtectionManager().hasPermission(player, block.getLocation(), Interaction.INTERACT_BLOCK);
         }

         public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
            return flow == ItemTransportFlow.INSERT ? AdvancedDirectional.this.getInputSlots() : AdvancedDirectional.this.getOutputSlots();
         }
      };
   }

   @ParametersAreNonnullByDefault
   @Override
   public boolean directionClick(Player player, ClickAction action, BlockMenu blockMenu, BlockFace blockFace) {
      if (action.isShiftClicked()) {
         this.openDirection(player, blockMenu, blockFace);
      } else {
         this.setDirection(blockMenu, blockFace);
      }

      return false;
   }

   @ParametersAreNonnullByDefault
   @Override
   public void openDirection(Player player, BlockMenu blockMenu, BlockFace blockFace) {
      super.openDirection(player, blockMenu, blockFace);
   }

   @ParametersAreNonnullByDefault
   @Override
   public void setDirection(BlockMenu blockMenu, BlockFace blockFace) {
      SELECTED_DIRECTION_MAP.put(blockMenu.getLocation().clone(), blockFace);
      StorageCacheUtils.setData(blockMenu.getBlock().getLocation(), "direction", blockFace.name());
   }

   @ParametersAreNonnullByDefault
   public boolean minusClick(Location location, ClickAction action) {
      int n = 1;
      if (action.isRightClicked()) {
         n = 8;
      }

      if (action.isShiftClicked()) {
         n = 64;
      }

      this.minusLimitQuantity(location, n);
      return false;
   }

   @ParametersAreNonnullByDefault
   public boolean addClick(Location location, ClickAction action) {
      int n = 1;
      if (action.isRightClicked()) {
         n = 8;
      }

      if (action.isShiftClicked()) {
         n = 64;
      }

      this.addLimitQuantity(location, n);
      return false;
   }

   @NotNull
   @Override
   protected abstract int[] getBackgroundSlots();

   @Nullable
   @Override
   protected int[] getOtherBackgroundSlots() {
      return super.getOtherBackgroundSlots();
   }

   @Nullable
   @Override
   protected ItemStack getOtherBackgroundStack() {
      return super.getOtherBackgroundStack();
   }

   @Range(
      from = 0L,
      to = 53L
   )
   @Override
   public int getNorthSlot() {
      return 12;
   }

   @Range(
      from = 0L,
      to = 53L
   )
   @Override
   public int getSouthSlot() {
      return 30;
   }

   @Range(
      from = 0L,
      to = 53L
   )
   @Override
   public int getEastSlot() {
      return 22;
   }

   @Range(
      from = 0L,
      to = 53L
   )
   @Override
   public int getWestSlot() {
      return 20;
   }

   @Range(
      from = 0L,
      to = 53L
   )
   @Override
   public int getUpSlot() {
      return 15;
   }

   @Range(
      from = 0L,
      to = 53L
   )
   @Override
   public int getDownSlot() {
      return 33;
   }

   @NotNull
   public ItemStack getCargoNumberIcon() {
      return Icon.SHOW_ICON;
   }

   @NotNull
   public ItemStack getMinusIcon() {
      return Icon.MINUS_ICON;
   }

   @NotNull
   public ItemStack getAddIcon() {
      return Icon.ADD_ICON;
   }

   @NotNull
   public ItemStack getTransportModeIcon() {
      return Icon.TRANSPORT_MODE_ICON;
   }

   public int getLimitQuantity(@NotNull Location location) {
      Integer quantity = NETWORK_LIMIT_QUANTITY_MAP.get(location.clone());
      if (quantity == null) {
         String squantity = StorageCacheUtils.getData(location, "transport_limit");
         if (squantity == null) {
            quantity = 64;
         } else {
            quantity = Integer.parseInt(squantity);
         }

         NETWORK_LIMIT_QUANTITY_MAP.put(location.clone(), quantity);
      }

      return quantity;
   }

   public void setLimitQuantity(@NotNull Location location, int quantity) {
      NETWORK_LIMIT_QUANTITY_MAP.put(location.clone(), quantity);
      StorageCacheUtils.setData(location, "transport_limit", Integer.toString(quantity));
   }

   @NotNull
   public TransportMode getCurrentTransportMode(@NotNull Location location) {
      TransportMode mode = NETWORK_TRANSPORT_MODE_MAP.get(location.clone());
      if (mode == null) {
         mode = TransportMode.valueOf(StorageCacheUtils.getData(location, "transport_mode"));
         NETWORK_TRANSPORT_MODE_MAP.put(location.clone(), mode);
      }

      return mode;
   }

   public void setTransportMode(@NotNull Location location, TransportMode mode) {
      NETWORK_TRANSPORT_MODE_MAP.put(location.clone(), mode);
      StorageCacheUtils.setData(location, "transport_mode", String.valueOf(mode));
   }

   public boolean toggleTransportMode(@NotNull Location location, @NotNull ClickAction action) {
      TransportMode mode = this.getCurrentTransportMode(location);
      if (action.isRightClicked()) {
         this.setTransportMode(location, mode.previous());
      } else {
         this.setTransportMode(location, mode.next());
      }

      this.updateTransportModeIcon(location);
      return false;
   }

   public void minusLimitQuantity(@NotNull Location location, int quantity) {
      int limitQuantity = this.getLimitQuantity(location);
      if (limitQuantity - quantity >= 1) {
         this.setLimitQuantity(location, limitQuantity - quantity);
      } else {
         this.setLimitQuantity(location, this.getMaxLimit() - (quantity - limitQuantity));
      }

      this.updateShowIcon(location);
   }

   public void addLimitQuantity(@NotNull Location location, int quantity) {
      int limitQuantity = this.getLimitQuantity(location);
      int newQuantity = limitQuantity + quantity;
      if (this.isExceedLimit(newQuantity)) {
         newQuantity -= this.getMaxLimit();
      }

      this.setLimitQuantity(location, newQuantity);
      this.updateShowIcon(location);
   }

   public abstract boolean isExceedLimit(int var1);

   public abstract int getMaxLimit();

   public void updateShowIcon(@NotNull Location location) {
      ItemStack clone = Icon.SHOW_ICON.clone();
      ItemMeta itemMeta = clone.getItemMeta();
      List<String> lore = new ArrayList<>();
      List<String> old = itemMeta.getLore();
      if (old != null) {
         lore.addAll(old);
      }

      lore.set(0, String.format(Lang.getString("messages.normal-operation.directional.limit_quantity"), this.getLimitQuantity(location)));
      itemMeta.setLore(lore);
      clone.setItemMeta(itemMeta);
      BlockMenu blockMenu = StorageCacheUtils.getMenu(location);
      if (blockMenu != null) {
         int slot = this.getCargoNumberSlot();
         if (slot != -1) {
            blockMenu.replaceExistingItem(slot, clone);
         }
      }
   }

   public void updateTransportModeIcon(@NotNull Location location) {
      ItemStack clone = Icon.TRANSPORT_MODE_ICON.clone();
      ItemMeta itemMeta = clone.getItemMeta();
      List<String> lore = new ArrayList<>();
      List<String> old = itemMeta.getLore();
      if (old != null) {
         lore.addAll(old);
      }

      lore.set(0, String.format(Lang.getString("messages.normal-operation.directional.transport_mode"), ""));
      TransportMode current = this.getCurrentTransportMode(location);

      for (TransportMode mode : TransportMode.values()) {
         lore.add(ChatColors.color((mode == current ? "&a" : "&c") + "- " + mode.getRawName()));
      }

      itemMeta.setLore(lore);
      clone.setItemMeta(itemMeta);
      BlockMenu blockMenu = StorageCacheUtils.getMenu(location);
      if (blockMenu != null) {
         int slot = this.getTransportModeSlot();
         if (slot != -1) {
            blockMenu.replaceExistingItem(slot, clone);
         }
      }
   }

   @Range(
      from = -1L,
      to = 53L
   )
   protected abstract int getMinusSlot();

   @Range(
      from = -1L,
      to = 53L
   )
   protected abstract int getCargoNumberSlot();

   @Range(
      from = -1L,
      to = 53L
   )
   protected abstract int getAddSlot();

   @Range(
      from = -1L,
      to = 53L
   )
   protected abstract int getTransportModeSlot();
}
