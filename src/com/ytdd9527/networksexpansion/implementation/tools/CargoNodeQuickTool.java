package com.ytdd9527.networksexpansion.implementation.tools;

import com.balugaq.netex.utils.Debug;
import com.balugaq.netex.utils.Lang;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.core.items.SpecialSlimefunItem;
import com.ytdd9527.networksexpansion.utils.TextUtil;
import io.github.sefiraat.networks.utils.Keys;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemHandler;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import net.guizhanss.minecraft.guizhanlib.gugu.minecraft.helpers.inventory.ItemStackHelper;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CargoNodeQuickTool extends SpecialSlimefunItem {
   @NotNull
   private final NamespacedKey listKey;
   @NotNull
   private final NamespacedKey configKey;
   @NotNull
   private final NamespacedKey cargoKey;
   private final int[] listSlots = new int[]{19, 20, 21, 28, 29, 30, 37, 38, 39};
   private final Gson gson = new Gson();

   public CargoNodeQuickTool(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe);
      this.listKey = Keys.newKey("item_list");
      this.configKey = Keys.newKey("config");
      this.cargoKey = Keys.newKey("cargo_type");
   }

   public void preRegister() {
      this.addItemHandler(
         new ItemHandler[]{
            (ItemUseHandler)e -> {
               e.cancel();
               Block target = (Block)e.getClickedBlock().orElse(null);
               if (target != null) {
                  ItemStack tool = e.getItem();
                  Player p = e.getPlayer();
                  if (!this.isTool(tool)) {
                     p.sendMessage(Lang.getString("messages.unsupported-operation.cargo_node_quick_tool.invalid_tool"));
                  } else {
                     Location bLoc = target.getLocation();
                     SlimefunBlockData blockData = StorageCacheUtils.getBlock(bLoc);
                     if (blockData != null && blockData.getSfId().startsWith("CARGO_NODE_")) {
                        ItemMeta meta = tool.getItemMeta();
                        PersistentDataContainer container = meta.getPersistentDataContainer();
                        if (p.isSneaking()) {
                           String var26 = blockData.getSfId();
                           switch (var26) {
                              case "CARGO_NODE_INPUT":
                              case "CARGO_NODE_OUTPUT_ADVANCED":
                                 YamlConfiguration itemConfig = new YamlConfiguration();
                                 BlockMenu inv = blockData.getBlockMenu();
                                 if (inv == null) {
                                    return;
                                 } else {
                                    for (int slot : this.listSlots) {
                                       ItemStack itemInSlot = inv.getItemInSlot(slot);
                                       if (itemInSlot != null) {
                                          itemConfig.set(String.valueOf(slot), itemInSlot);
                                       }
                                    }

                                    container.set(this.listKey, PersistentDataType.STRING, itemConfig.saveToString());
                                 }
                              case "CARGO_NODE_OUTPUT":
                                 container.set(this.cargoKey, PersistentDataType.STRING, blockData.getSfId());
                                 container.set(this.configKey, PersistentDataType.STRING, this.gson.toJson(blockData.getAllData()));
                                 List<String> lore = meta.getLore();
                                 if (lore == null) {
                                    lore = new ArrayList<>(1);
                                 }

                                 SlimefunItem sf = SlimefunItem.getById(blockData.getSfId());
                                 if (sf != null) {
                                    lore.set(
                                       lore.size() - 1,
                                       String.format(Lang.getString("messages.completed-operation.cargo_node_quick_tool.node_set"), sf.getItemName())
                                    );
                                 }

                                 meta.setLore(lore);
                                 tool.setItemMeta(meta);
                                 p.sendMessage(Lang.getString("messages.completed-operation.cargo_node_quick_tool.config_saved"));
                                 return;
                              default:
                                 p.sendMessage(Lang.getString("messages.unsupported-operation.cargo_node_quick_tool.invalid_node"));
                           }
                        } else {
                           String storedId = (String)container.get(this.cargoKey, PersistentDataType.STRING);
                           if (storedId == null) {
                              p.sendMessage(Lang.getString("messages.unsupported-operation.cargo_node_quick_tool.no-config"));
                           } else if (!storedId.equalsIgnoreCase(blockData.getSfId())) {
                              p.sendMessage(Lang.getString("messages.unsupported-operation.cargo_node_quick_tool.node-type-not-same"));
                           } else {
                              BlockMenu inv = blockData.getBlockMenu();
                              if (inv != null) {
                                 String itemConfig = blockData.getSfId();
                                 switch (itemConfig) {
                                    case "CARGO_NODE_INPUT":
                                    case "CARGO_NODE_OUTPUT_ADVANCED":
                                       YamlConfiguration itemConfigx = new YamlConfiguration();

                                       try {
                                          String cfg = (String)container.get(this.listKey, PersistentDataType.STRING);
                                          if (cfg == null) {
                                             return;
                                          }

                                          itemConfigx.loadFromString(cfg);
                                       } catch (Exception var25) {
                                          Debug.trace(var25);
                                          return;
                                       }

                                       if (!itemConfigx.getKeys(false).isEmpty()) {
                                          Map<ItemStack, Boolean> itemList = new HashMap<>();
                                          Map<ItemStack, Integer> consumeSlots = new HashMap<>();

                                          for (String each : itemConfigx.getKeys(false)) {
                                             itemList.put(itemConfigx.getItemStack(each), false);
                                          }

                                          PlayerInventory pInv = p.getInventory();

                                          for (int i = 0; i <= 35; i++) {
                                             ItemStack includeItem = this.isInclude(pInv.getItem(i), itemList);
                                             if (includeItem != null) {
                                                consumeSlots.put(includeItem, i);
                                             }

                                             if (this.isAllTrue(itemList.values())) {
                                                break;
                                             }
                                          }

                                          if (this.isAllTrue(itemList.values())) {
                                             for (int slotx : this.listSlots) {
                                                ItemStack item = itemConfigx.getItemStack(String.valueOf(slotx));
                                                if (item != null) {
                                                   int consumeSlot = consumeSlots.get(item);
                                                   ItemStack itemInInv = pInv.getItem(consumeSlot);
                                                   if (itemInInv != null) {
                                                      ItemStack itemInTarget = inv.getItemInSlot(slotx);
                                                      if (SlimefunUtils.isItemSimilar(itemInTarget, item, true, false)) {
                                                         if (itemInTarget.getAmount() < item.getAmount()) {
                                                            itemInInv.setAmount(
                                                               Math.max(itemInInv.getAmount() - (item.getAmount() - itemInTarget.getAmount()), 0)
                                                            );
                                                         }
                                                      } else {
                                                         itemInInv.setAmount(Math.max(itemInInv.getAmount() - item.getAmount(), 0));
                                                      }

                                                      pInv.setItem(consumeSlot, itemInInv);
                                                      inv.replaceExistingItem(slotx, item);
                                                   }
                                                } else {
                                                   inv.replaceExistingItem(slotx, null);
                                                }
                                             }
                                          } else {
                                             p.sendMessage(Lang.getString("messages.unsupported-operation.cargo_node_quick_tool.not-enough-items"));
                                             Iterator var41 = itemList.keySet().iterator();
                                             if (var41.hasNext()) {
                                                ItemStack item = (ItemStack)var41.next();
                                                if (!itemList.get(item)) {
                                                   p.sendMessage(
                                                      TextUtil.color(
                                                         String.format(
                                                            Lang.getString("messages.unsupported-operation.cargo_node_quick_tool.missing-item-entry"),
                                                            ItemStackHelper.getDisplayName(item),
                                                            item.getAmount()
                                                         )
                                                      )
                                                   );
                                                } else {
                                                   for (int slotxx : this.listSlots) {
                                                      inv.replaceExistingItem(slotxx, null);
                                                   }
                                                }

                                                return;
                                             }
                                          }
                                       }
                                    case "CARGO_NODE_OUTPUT":
                                       break;
                                    default:
                                       p.sendMessage(Lang.getString("messages.unsupported-operation.cargo_node_quick_tool.invalid_node"));
                                       return;
                                 }

                                 Map<String, String> config = (Map<String, String>)this.gson
                                    .fromJson((String)container.get(this.configKey, PersistentDataType.STRING), (new TypeToken<Map<String, String>>() {
                                    }).getType());
                                 if (config != null) {
                                    config.forEach(blockData::setData);
                                 }

                                 inv.getPreset().newInstance(inv, bLoc);
                                 p.sendMessage(Lang.getString("messages.completed-operation.cargo_node_quick_tool.pasted_config"));
                              }
                           }
                        }
                     } else {
                        p.sendMessage(Lang.getString("messages.unsupported-operation.cargo_node_quick_tool.invalid_node"));
                     }
                  }
               }
            }
         }
      );
   }

   @Nullable
   private ItemStack isInclude(@Nullable ItemStack item, @NotNull Map<ItemStack, Boolean> checkList) {
      for (ItemStack each : checkList.keySet()) {
         if (item != null && StackUtils.itemsMatch(each, item, true, false) && item.getAmount() >= each.getAmount()) {
            checkList.put(each, true);
            return each;
         }
      }

      return null;
   }

   private boolean isAllTrue(@NotNull Collection<Boolean> set) {
      for (boolean each : set) {
         if (!each) {
            return false;
         }
      }

      return true;
   }

   private boolean isTool(@Nullable ItemStack tool) {
      if (tool != null && tool.getItemMeta() != null) {
         Slimefun sf = Slimefun.instance();
         if (sf != null) {
            NamespacedKey idKey = Keys.customNewKey(sf, "slimefun_item");
            PersistentDataContainer container = tool.getItemMeta().getPersistentDataContainer();
            if (container.has(idKey, PersistentDataType.STRING)) {
               return this.getId().equalsIgnoreCase((String)container.get(idKey, PersistentDataType.STRING)) && tool.getAmount() == 1;
            }
         }
      }

      return false;
   }
}
