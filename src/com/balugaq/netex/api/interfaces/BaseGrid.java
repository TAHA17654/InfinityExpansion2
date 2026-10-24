package com.balugaq.netex.api.interfaces;

import com.balugaq.netex.api.enums.AmountHandleStrategy;
import com.balugaq.netex.api.keybind.Action;
import com.balugaq.netex.api.keybind.ActionResult;
import com.balugaq.netex.api.keybind.Keybind;
import com.balugaq.netex.api.keybind.Keybinds;
import com.balugaq.netex.api.keybind.MultiActionHandle;
import com.balugaq.netex.api.visual.Screen;
import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.network.GridItemRequest;
import io.github.sefiraat.networks.network.NodeDefinition;
import io.github.sefiraat.networks.slimefun.network.grid.GridCache;
import io.github.sefiraat.networks.utils.Keys;
import io.github.sefiraat.networks.utils.StackUtils;
import java.util.HashMap;
import java.util.Map;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface BaseGrid {
   Map<String, Keybinds> KEYBINDS = new HashMap<>();

   default Keybinds displayKeybinds() {
      String key = this.getClass().getSimpleName().toLowerCase() + "-display-keybinds";
      if (KEYBINDS.containsKey(key)) {
         return KEYBINDS.get(key);
      } else {
         Keybinds keybinds = Keybinds.create(
               Keys.newKey(key),
               it -> {
                  Keybind clickOnWithCursor = Keybind.of(Keys.newKey("clicked-item-not-equals-to-cursor"), (player, s, i, a, menu) -> {
                     NodeDefinition definition = NetworkStorage.getNode(menu.getLocation());
                     if (definition != null && definition.getNode() != null) {
                        ItemStack clone = this.precheck(definition, menu, player, i);
                        if (clone == null) {
                           return false;
                        } else {
                           ItemStack cursor = player.getItemOnCursor();
                           return cursor.getType() != Material.AIR && !StackUtils.itemsMatch(clone, player.getItemOnCursor(), true, false);
                        }
                     } else {
                        return false;
                     }
                  });
                  Action storeCursor = Action.of(Keys.newKey("store-cursor"), (player, s, i, a, menu) -> {
                     NodeDefinition definition = NetworkStorage.getNode(menu.getLocation());
                     if (definition != null && definition.getNode() != null) {
                        definition.getNode().getRoot().addItemStack(menu.getLocation(), player.getItemOnCursor());
                        return ActionResult.of(MultiActionHandle.BREAK, false);
                     } else {
                        return ActionResult.of(MultiActionHandle.CONTINUE, false);
                     }
                  });
                  Action strategy1 = Keybind.gridActionGenerate(this, AmountHandleStrategy.ONE, false);
                  Action strategy2 = Keybind.gridActionGenerate(this, AmountHandleStrategy.STACK, false);
                  Action strategy3 = Keybind.gridActionGenerate(this, AmountHandleStrategy.STACK, true);
                  Action strategy4 = Keybind.gridActionGenerate(this, AmountHandleStrategy.ONE, true);
                  Action strategy5 = Keybind.gridActionGenerate(this, AmountHandleStrategy.CUSTOM, false);
                  Action strategy6 = Keybind.gridActionGenerate(this, AmountHandleStrategy.CUSTOM, true);
                  Action strategy7 = Keybind.gridActionGenerate(this, AmountHandleStrategy.ASK, false);
                  Action strategy8 = Keybind.gridActionGenerate(this, AmountHandleStrategy.ASK, true);
                  it.usableKeybinds(
                     Keybind.leftClick, Keybind.rightClick, Keybind.shiftLeftClick, Keybind.shiftRightClick, Keybind.shiftClick, clickOnWithCursor
                  );
                  it.usableActions(storeCursor, strategy1, strategy2, strategy3, strategy4, strategy5, strategy6, strategy7, strategy8);
                  it.defaultKeybinds(
                     clickOnWithCursor,
                     storeCursor,
                     Keybind.leftClick,
                     strategy1,
                     Keybind.rightClick,
                     strategy2,
                     Keybind.shiftLeftClick,
                     strategy3,
                     Keybind.shiftRightClick,
                     strategy4
                  );
                  it.defaultActionResult(ActionResult.of(MultiActionHandle.CONTINUE, false));
               }
            )
            .generate();
         KEYBINDS.put(key, keybinds);
         return keybinds;
      }
   }

   default Keybinds outsideKeybinds() {
      String key = "outside-keybinds";
      if (KEYBINDS.containsKey(key)) {
         return KEYBINDS.get(key);
      } else {
         Keybinds keybinds = Keybinds.create(
               Keys.newKey(key),
               it -> {
                  it.usableKeybinds(Keybind.leftClick, Keybind.rightClick, Keybind.shiftLeftClick, Keybind.shiftRightClick, Keybind.shiftClick);
                  Action storeItem = Action.of(Keys.newKey("store-item"), (p, s, i, a, menu) -> {
                     this.receiveItem(p, i, a, Screen.of(menu));
                     return ActionResult.of(MultiActionHandle.BREAK, false);
                  });
                  it.usableActions(
                     storeItem,
                     Keybind.gridActionGenerate(this, AmountHandleStrategy.ONE, true),
                     Keybind.gridActionGenerate(this, AmountHandleStrategy.STACK, true),
                     Keybind.gridActionGenerate(this, AmountHandleStrategy.CUSTOM, true),
                     Keybind.gridActionGenerate(this, AmountHandleStrategy.ASK, true)
                  );
                  it.defaultKeybinds(Keybind.shiftLeftClick, storeItem);
                  it.defaultActionResult(ActionResult.of(MultiActionHandle.CONTINUE, true));
               }
            )
            .generate();
         KEYBINDS.put(key, keybinds);
         return keybinds;
      }
   }

   @Nullable
   ItemStack precheck(NodeDefinition var1, BlockMenu var2, Player var3, ItemStack var4);

   void addToInventory(Player var1, NodeDefinition var2, GridItemRequest var3, BlockMenu var4);

   void addToCursor(Player var1, NodeDefinition var2, GridItemRequest var3, boolean var4, BlockMenu var5);

   Map<Location, GridCache> getCacheMap();

   void updateDisplay(Screen var1);

   void receiveItem(Player var1, ItemStack var2, ClickAction var3, Screen var4);
}
