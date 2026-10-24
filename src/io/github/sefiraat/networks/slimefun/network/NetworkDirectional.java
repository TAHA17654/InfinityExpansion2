package io.github.sefiraat.networks.slimefun.network;

import com.balugaq.netex.api.enums.FacingPreset;
import com.balugaq.netex.api.enums.FeedbackType;
import com.balugaq.netex.utils.Lang;
import com.balugaq.netex.utils.NetworksVersionedEnchantment;
import com.balugaq.netex.utils.NetworksVersionedParticle;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.support.GhostItems;
import com.ytdd9527.networksexpansion.utils.TextUtil;
import com.ytdd9527.networksexpansion.utils.itemstacks.ItemStackUtil;
import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.network.NodeType;
import io.github.sefiraat.networks.utils.Keys;
import io.github.sefiraat.networks.utils.NetworkUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemHandler;
import io.github.thebusybiscuit.slimefun4.api.items.ItemSetting;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.items.settings.IntRangeSetting;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.ParametersAreNonnullByDefault;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import net.guizhanss.guizhanlib.minecraft.helper.MaterialHelper;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public abstract class NetworkDirectional extends NetworkObject {
   public static final String DIRECTION = "direction";
   public static final String OWNER_KEY = "uuid";
   public static final Set<BlockFace> VALID_FACES = EnumSet.of(BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST);
   private static final int NORTH_SLOT = 12;
   private static final int SOUTH_SLOT = 30;
   private static final int EAST_SLOT = 22;
   private static final int WEST_SLOT = 20;
   private static final int UP_SLOT = 15;
   private static final int DOWN_SLOT = 33;
   private static final Set<Location> locked = new HashSet<>();
   protected static final Map<Location, BlockFace> SELECTED_DIRECTION_MAP = new HashMap<>();
   @NotNull
   private final ItemSetting<Integer> tickRate = new IntRangeSetting(this, "tick_rate", 1, 1, 10);

   protected NetworkDirectional(
      @NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe, NodeType type
   ) {
      this(itemGroup, item, recipeType, recipe, 1, type);
   }

   protected NetworkDirectional(
      @NotNull ItemGroup itemGroup,
      @NotNull SlimefunItemStack item,
      @NotNull RecipeType recipeType,
      @NotNull ItemStack[] recipe,
      @Range(from = 1L,to = 64L) int outputAmount,
      NodeType type
   ) {
      super(itemGroup, item, recipeType, recipe, outputAmount, type);
      this.addItemSetting(new ItemSetting[]{this.tickRate});
      this.addItemHandler(new ItemHandler[]{new BlockTicker() {
         private int tick = 1;

         public boolean isSynchronized() {
            return NetworkDirectional.this.runSync();
         }

         public void tick(@NotNull Block block, SlimefunItem slimefunItem, @NotNull SlimefunBlockData data) {
            if (this.tick <= 1) {
               NetworkDirectional.this.onTick(data.getBlockMenu(), block);
            }
         }

         public void uniqueTick() {
            this.tick = this.tick <= 1 ? (Integer)NetworkDirectional.this.tickRate.getValue() : this.tick - 1;
            if (this.tick <= 1) {
               NetworkDirectional.this.onUniqueTick();
            }
         }
      }});
   }

   @NotNull
   public static ItemStack getDirectionalSlotPane(@NotNull BlockFace blockFace, @NotNull SlimefunItem slimefunItem, boolean active) {
      ItemStack displayStack = ItemStackUtil.getCleanItem(
         new CustomItemStack(
            new CustomItemStack(slimefunItem.getItem(), meta -> {
               PersistentDataContainer container = meta.getPersistentDataContainer();

               for (NamespacedKey key : container.getKeys()) {
                  container.remove(key);
               }
            }),
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
   protected BlockFace getCurrentDirection(@NotNull BlockMenu blockMenu) {
      BlockFace direction = SELECTED_DIRECTION_MAP.get(blockMenu.getLocation().clone());
      if (direction == null) {
         direction = BlockFace.valueOf(StorageCacheUtils.getData(blockMenu.getLocation(), "direction"));
         SELECTED_DIRECTION_MAP.put(blockMenu.getLocation().clone(), direction);
      }

      return direction;
   }

   @Override
   public void onPlace(@NotNull BlockPlaceEvent event) {
      NetworkStorage.removeNode(event.getBlock().getLocation());
      SlimefunBlockData blockData = StorageCacheUtils.getBlock(event.getBlock().getLocation());
      if (blockData != null) {
         blockData.setData("uuid", event.getPlayer().getUniqueId().toString());
         blockData.setData("direction", BlockFace.SELF.name());
         BlockMenu blockMenu = blockData.getBlockMenu();
         if (blockMenu != null) {
            NetworkUtils.applyConfig(this, blockMenu, event.getPlayer());
         }

         PersistentDataContainer pdc = event.getItemInHand().getItemMeta().getPersistentDataContainer();
         if (pdc.has(Keys.FACING_PRESET)) {
            String p = (String)pdc.get(Keys.FACING_PRESET, PersistentDataType.STRING);
            if (p != null) {
               FacingPreset facingPreset = FacingPreset.valueOf(p);
               if (facingPreset != null && facingPreset.tryApply(event, blockData)) {
                  event.getPlayer().sendMessage(Lang.getString("messages.completed-operation.comprehensive.facing_preset_applied"));
               }
            }
         }
      }
   }

   @OverridingMethodsMustInvokeSuper
   protected void onTick(@Nullable BlockMenu blockMenu, @NotNull Block block) {
      this.sendFeedback(block.getLocation(), FeedbackType.TICKING);
      this.addToRegistry(block);
      this.updateGui(blockMenu);
   }

   protected void onUniqueTick() {
   }

   public void postRegister() {
      new BlockMenuPreset(this.getId(), this.getItemName()) {
         public void init() {
            this.drawBackground(NetworkDirectional.this.getBackgroundSlots());
            if (NetworkDirectional.this.getOtherBackgroundSlots() != null && NetworkDirectional.this.getOtherBackgroundStack() != null) {
               this.drawBackground(NetworkDirectional.this.getOtherBackgroundStack(), NetworkDirectional.this.getOtherBackgroundSlots());
            }

            this.addItem(
               NetworkDirectional.this.getNorthSlot(),
               NetworkDirectional.getDirectionalSlotPane(BlockFace.NORTH, Material.AIR, false),
               (player, i, itemStack, clickAction) -> false
            );
            this.addItem(
               NetworkDirectional.this.getSouthSlot(),
               NetworkDirectional.getDirectionalSlotPane(BlockFace.SOUTH, Material.AIR, false),
               (player, i, itemStack, clickAction) -> false
            );
            this.addItem(
               NetworkDirectional.this.getEastSlot(),
               NetworkDirectional.getDirectionalSlotPane(BlockFace.EAST, Material.AIR, false),
               (player, i, itemStack, clickAction) -> false
            );
            this.addItem(
               NetworkDirectional.this.getWestSlot(),
               NetworkDirectional.getDirectionalSlotPane(BlockFace.WEST, Material.AIR, false),
               (player, i, itemStack, clickAction) -> false
            );
            this.addItem(
               NetworkDirectional.this.getUpSlot(),
               NetworkDirectional.getDirectionalSlotPane(BlockFace.UP, Material.AIR, false),
               (player, i, itemStack, clickAction) -> false
            );
            this.addItem(
               NetworkDirectional.this.getDownSlot(),
               NetworkDirectional.getDirectionalSlotPane(BlockFace.DOWN, Material.AIR, false),
               (player, i, itemStack, clickAction) -> false
            );
         }

         public void newInstance(@NotNull BlockMenu blockMenu, @NotNull Block b) {
            String string = StorageCacheUtils.getData(blockMenu.getLocation(), "direction");
            BlockFace direction;
            if (string == null) {
               direction = BlockFace.SELF;
               StorageCacheUtils.setData(blockMenu.getLocation(), "direction", BlockFace.SELF.name());
            } else {
               direction = BlockFace.valueOf(string);
            }

            NetworkDirectional.SELECTED_DIRECTION_MAP.put(blockMenu.getLocation().clone(), direction);
            blockMenu.addMenuClickHandler(
               NetworkDirectional.this.getNorthSlot(),
               (player, i, itemStack, clickAction) -> NetworkDirectional.this.directionClick(player, clickAction, blockMenu, BlockFace.NORTH)
            );
            blockMenu.addMenuClickHandler(
               NetworkDirectional.this.getSouthSlot(),
               (player, i, itemStack, clickAction) -> NetworkDirectional.this.directionClick(player, clickAction, blockMenu, BlockFace.SOUTH)
            );
            blockMenu.addMenuClickHandler(
               NetworkDirectional.this.getEastSlot(),
               (player, i, itemStack, clickAction) -> NetworkDirectional.this.directionClick(player, clickAction, blockMenu, BlockFace.EAST)
            );
            blockMenu.addMenuClickHandler(
               NetworkDirectional.this.getWestSlot(),
               (player, i, itemStack, clickAction) -> NetworkDirectional.this.directionClick(player, clickAction, blockMenu, BlockFace.WEST)
            );
            blockMenu.addMenuClickHandler(
               NetworkDirectional.this.getUpSlot(),
               (player, i, itemStack, clickAction) -> NetworkDirectional.this.directionClick(player, clickAction, blockMenu, BlockFace.UP)
            );
            blockMenu.addMenuClickHandler(
               NetworkDirectional.this.getDownSlot(),
               (player, i, itemStack, clickAction) -> NetworkDirectional.this.directionClick(player, clickAction, blockMenu, BlockFace.DOWN)
            );
         }

         public boolean canOpen(@NotNull Block block, @NotNull Player player) {
            return player.hasPermission("slimefun.inventory.bypass")
               || this.getSlimefunItem().canUse(player, false)
                  && Slimefun.getProtectionManager().hasPermission(player, block.getLocation(), Interaction.INTERACT_BLOCK);
         }

         public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
            return flow == ItemTransportFlow.INSERT ? NetworkDirectional.this.getInputSlots() : NetworkDirectional.this.getOutputSlots();
         }
      };
   }

   @ParametersAreNonnullByDefault
   public boolean directionClick(Player player, ClickAction action, BlockMenu blockMenu, BlockFace blockFace) {
      if (action.isShiftClicked()) {
         this.openDirection(player, blockMenu, blockFace);
      } else {
         this.setDirection(blockMenu, blockFace);
      }

      return false;
   }

   @ParametersAreNonnullByDefault
   public void openDirection(Player player, BlockMenu blockMenu, BlockFace blockFace) {
      BlockMenu targetMenu = StorageCacheUtils.getMenu(blockMenu.getBlock().getRelative(blockFace).getLocation());
      if (targetMenu != null) {
         Location location = targetMenu.getLocation();
         SlimefunItem item = StorageCacheUtils.getSfItem(location);
         if (item != null
            && item.canUse(player, true)
            && Slimefun.getProtectionManager().hasPermission(player, blockMenu.getLocation(), Interaction.INTERACT_BLOCK)) {
            targetMenu.open(new Player[]{player});
         }
      }
   }

   @ParametersAreNonnullByDefault
   public void setDirection(BlockMenu blockMenu, BlockFace blockFace) {
      SELECTED_DIRECTION_MAP.put(blockMenu.getLocation().clone(), blockFace);
      StorageCacheUtils.setData(blockMenu.getBlock().getLocation(), "direction", blockFace.name());
   }

   @NotNull
   protected int[] getBackgroundSlots() {
      return new int[]{
         0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 13, 14, 16, 17, 18, 19, 21, 23, 24, 25, 26, 27, 28, 29, 21, 31, 32, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44
      };
   }

   @Nullable
   protected int[] getOtherBackgroundSlots() {
      return null;
   }

   @Nullable
   protected ItemStack getOtherBackgroundStack() {
      return null;
   }

   public int getNorthSlot() {
      return 12;
   }

   public int getSouthSlot() {
      return 30;
   }

   public int getEastSlot() {
      return 22;
   }

   public int getWestSlot() {
      return 20;
   }

   public int getUpSlot() {
      return 15;
   }

   public int getDownSlot() {
      return 33;
   }

   public int[] getItemSlots() {
      return new int[0];
   }

   public int[] getInputSlots() {
      return new int[0];
   }

   public int[] getOutputSlots() {
      return new int[0];
   }

   protected DustOptions getDustOptions() {
      return new DustOptions(Color.RED, 1.0F);
   }

   protected void showParticle(@NotNull Location location, @NotNull BlockFace blockFace) {
      Vector faceVector = blockFace.getDirection().clone().multiply(-1);
      Vector pushVector = faceVector.clone().multiply(2);
      Location displayLocation = location.clone().add(0.5, 0.5, 0.5).add(faceVector);
      location.getWorld()
         .spawnParticle(NetworksVersionedParticle.DUST, displayLocation, 0, pushVector.getX(), pushVector.getY(), pushVector.getZ(), this.getDustOptions());
   }
}
