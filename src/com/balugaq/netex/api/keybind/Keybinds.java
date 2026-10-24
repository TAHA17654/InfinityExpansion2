package com.balugaq.netex.api.keybind;

import com.balugaq.netex.api.algorithm.ID;
import com.balugaq.netex.api.helpers.Icon;
import com.balugaq.netex.utils.Debug;
import com.balugaq.netex.utils.Lang;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.utils.ReflectionUtil;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.utils.Keys;
import io.github.thebusybiscuit.slimefun4.libraries.dough.config.Config;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.ChatUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Consumer;
import lombok.Generated;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu.MenuClickHandler;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class Keybinds implements MenuClickHandler, Keyed {
   private static final Map<NamespacedKey, LinkedHashMap<Keybind, Action>> defaultKeybinds = new HashMap<>();
   private static final Map<NamespacedKey, Set<Keybind>> usableKeybind = new HashMap<>();
   private static final Map<NamespacedKey, Set<Action>> usableAction = new HashMap<>();
   private static final Map<NamespacedKey, Keybinds> keybindsRegistry = new HashMap<>();
   private static final Map<NamespacedKey, Keybind> keybindRegistry = new HashMap<>();
   private static final Map<NamespacedKey, Action> actionRegistry = new HashMap<>();
   private static final Map<NamespacedKey, List<KeybindsScript>> keybindsScripts = new HashMap<>();
   private final NamespacedKey key;
   private ActionResult defaultActionResult = ActionResult.of(MultiActionHandle.CONTINUE, false);
   public static int[] backgroundSlots = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 13, 17, 18, 22, 26, 27, 31, 35, 36, 40, 44, 45, 46, 47, 48, 50, 51, 52, 53};
   public static int[] keybindsSlots = new int[]{10, 19, 28, 37, 14, 23, 32, 41};
   public static int[] bordersSlots = new int[]{11, 20, 29, 38, 15, 24, 33, 42};
   public static int[] actionsSlots = new int[]{12, 21, 30, 39, 16, 25, 34, 43};
   public static int previousSlot = 47;
   public static int nextSlot = 52;

   public Keybinds(NamespacedKey key) {
      this.key = key;
      this.register();
   }

   @Nullable
   public static Keybinds get(NamespacedKey key) {
      return getKeybinds(key);
   }

   @Nullable
   public static Keybind getKeybind(NamespacedKey key) {
      return keybindRegistry.get(key);
   }

   @Nullable
   public static Action getAction(NamespacedKey key) {
      return actionRegistry.get(key);
   }

   @Nullable
   public static Keybinds getKeybinds(NamespacedKey key) {
      return keybindsRegistry.get(key);
   }

   public static Keybinds register(Keybinds keybinds) {
      keybindsRegistry.put(keybinds.getKey(), keybinds);
      return keybinds;
   }

   public static Keybind register(Keybind keybind) {
      keybindRegistry.put(keybind.getKey(), keybind);
      return keybind;
   }

   public static Action register(Action action) {
      actionRegistry.put(action.getKey(), action);
      return action;
   }

   public static Keybinds create(NamespacedKey key) {
      return new Keybinds(key);
   }

   public static Keybinds create(NamespacedKey key, Consumer<Keybinds> consumer) {
      return new Keybinds(key).set(consumer);
   }

   public static void distinctAll() {
      for (Entry<NamespacedKey, Set<Keybind>> entrySet : new HashSet<>(usableKeybind.entrySet())) {
         Set<Keybind> set = entrySet.getValue();
         Set<NamespacedKey> keys = new HashSet<>();
         Set<Keybind> cleaned = new HashSet<>();

         for (Keybind keybind : set) {
            if (!keys.contains(keybind.getKey())) {
               keys.add(keybind.getKey());
               cleaned.add(keybind);
            }
         }

         usableKeybind.put(entrySet.getKey(), cleaned);
      }

      for (Entry<NamespacedKey, Set<Action>> entrySet : new HashSet<>(usableAction.entrySet())) {
         Set<Action> set = entrySet.getValue();
         Set<NamespacedKey> keys = new HashSet<>();
         Set<Action> cleaned = new HashSet<>();

         for (Action action : set) {
            if (!keys.contains(action.getKey())) {
               keys.add(action.getKey());
               cleaned.add(action);
            }
         }

         usableAction.put(entrySet.getKey(), cleaned);
      }
   }

   public static void fetchScripts() {
      try {
         File keybindsFolder = new File(Networks.getInstance().getDataFolder(), "keybinds");
         File[] files = keybindsFolder.listFiles();
         if (files == null) {
            return;
         }

         for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".nkb")) {
               Config config = new Config("plugins/" + Networks.getInstance().getName() + "/keybinds/" + file.getName());
               KeybindsScript script = KeybindsScript.fromConfig(config);
               if (script != null) {
                  Keybinds keybinds = script.getKeybinds();
                  if (keybinds != null) {
                     if (!keybindsScripts.containsKey(keybinds.getKey())) {
                        keybindsScripts.put(keybinds.getKey(), new ArrayList<>());
                     }

                     keybindsScripts.get(keybinds.getKey()).add(script);
                  }
               }
            }
         }
      } catch (Exception var9) {
         Debug.trace(var9);
      }
   }

   public Keybinds register() {
      return register(this);
   }

   public Keybinds usableKeybinds(Keybind... keybinds) {
      return this.usableKeybinds(List.of(keybinds));
   }

   public Keybinds usableKeybinds(Collection<Keybind> keybinds) {
      usableKeybind.merge(this.key, new HashSet<>(keybinds), (a, b) -> new HashSet<Keybind>() {
            {
               this.addAll(a);
               this.addAll(b);
            }
         });
      return this;
   }

   public Set<Keybind> usableKeybinds() {
      return usableKeybind.getOrDefault(this.key, new HashSet<>());
   }

   public Keybinds usableActions(Action... actions) {
      return this.usableActions(List.of(actions));
   }

   public Keybinds usableActions(Collection<Action> actions) {
      usableAction.merge(this.key, new HashSet<>(actions), (a, b) -> new HashSet<Action>() {
            {
               this.addAll(a);
               this.addAll(b);
            }
         });
      return this;
   }

   public Set<Action> usableActions() {
      return usableAction.getOrDefault(this.key, new HashSet<>());
   }

   public Keybinds defaultKeybinds(LinkedHashMap<Keybind, Action> keybinds) {
      defaultKeybinds.put(this.key, keybinds);
      return this;
   }

   public Keybinds defaultKeybinds(Object... keybinds) {
      if (!defaultKeybinds.containsKey(this.key)) {
         defaultKeybinds.put(this.key, new LinkedHashMap<>());
      }

      for (int i = 0; i < keybinds.length; i += 2) {
         Object action = keybinds[i];
         if (action instanceof Keybind) {
            Keybind keybind = (Keybind)action;
            if (keybinds[i + 1] instanceof Action actionx) {
               defaultKeybinds.get(this.key).put(keybind, actionx);
            }
         }
      }

      return this;
   }

   public Keybinds defaultKeybinds(Map<Keybind, Action> keybinds) {
      if (!defaultKeybinds.containsKey(this.key)) {
         defaultKeybinds.put(this.key, new LinkedHashMap<>());
      }

      defaultKeybinds.get(this.key).putAll(keybinds);
      return this;
   }

   public String keybindKey(NamespacedKey key) {
      return "keybinds." + this.key + "." + key;
   }

   public LinkedHashMap<Keybind, Action> getKeybinds(Location location) {
      LinkedHashMap<Keybind, Action> keybinds = new LinkedHashMap<>(defaultKeybinds.get(this.key));

      for (Keybind keybind : keybinds.keySet()) {
         String type = StorageCacheUtils.getData(location, this.keybindKey(keybind.getKey()));
         if (type != null) {
            NamespacedKey key = NamespacedKey.fromString(type);
            if (key != null) {
               Action action = actionRegistry.get(key);
               if (action != null) {
                  keybinds.put(keybind, action);
               }
            }
         }
      }

      return keybinds;
   }

   @Nullable
   public BlockMenu getMenu(Player player) {
      InventoryHolder var3 = ((Inventory)ReflectionUtil.invokeMethod(ReflectionUtil.invokeMethod(player, "getOpenInventory"), "getTopInventory")).getHolder();
      return var3 instanceof BlockMenu ? (BlockMenu)var3 : null;
   }

   public boolean onClick(Player player, int i, ItemStack itemStack, ClickAction clickAction) {
      BlockMenu menu = this.getMenu(player);
      if (menu == null) {
         return false;
      } else {
         Location location = menu.getLocation();

         for (Entry<Keybind, Action> entry : this.getKeybinds(location).entrySet()) {
            if (entry.getKey().test(player, i, itemStack, clickAction, menu)) {
               ActionResult result = entry.getValue().apply(player, i, itemStack, clickAction, menu);
               if (result.handle() == MultiActionHandle.BREAK) {
                  return result.allowClick();
               }

               if (result.allowClick() != this.defaultActionResult.allowClick()) {
                  return !this.defaultActionResult.allowClick();
               }
            }
         }

         return this.defaultActionResult.allowClick();
      }
   }

   public ActionResult defaultActionResult() {
      return this.defaultActionResult;
   }

   public Keybinds defaultActionResult(ActionResult actionResult) {
      this.defaultActionResult = actionResult;
      return this;
   }

   public Keybinds set(Consumer<Keybinds> consumer) {
      consumer.accept(this);
      return this;
   }

   public Keybinds generate() {
      return this.usableActions(Action.of(Keys.newKey("do-nothing"), (p, s, i, a, m) -> this.defaultActionResult));
   }

   public ItemStack icon() {
      return Lang.getIcon("keybinds." + this.key.getKey(), Material.DIAMOND_ORE);
   }

   public void openMenu(Location location, Player player) {
      this.openMenu(location, player, 1);
   }

   public void openMenu(Location location, Player player, int page) {
      ChestMenu menu = new ChestMenu(Lang.getString("messages.keybind.sub-title"));

      for (int slot : backgroundSlots) {
         menu.addItem(slot, Icon.BLUE_BACKGROUND, (p, s, i, a) -> false);
      }

      List<Entry<Keybind, Action>> keybinds = this.getKeybinds(location).entrySet().stream().toList();

      for (int slot = 0; slot < keybindsSlots.length; slot++) {
         if (slot >= keybinds.size()) {
            menu.addItem(keybindsSlots[slot], Icon.LIGHT_GRAY_BACKGROUND, (p, s, i, a) -> false);
            menu.addItem(bordersSlots[slot], Icon.LIGHT_GRAY_BACKGROUND, (p, s, i, a) -> false);
            menu.addItem(actionsSlots[slot], Icon.LIGHT_GRAY_BACKGROUND, (p, s, i, a) -> false);
         } else {
            Entry<Keybind, Action> entry = keybinds.get(slot + (page - 1) * keybindsSlots.length);
            Keybind keybind = entry.getKey();
            Action action = entry.getValue();
            menu.addItem(keybindsSlots[slot], Lang.getIcon("keybinds." + keybind.getKey().getKey(), Material.OAK_WOOD), (p, s, i, a) -> false);
            menu.addItem(bordersSlots[slot], Icon.YELLOW_BORDER, (p, s, i, a) -> false);
            menu.addItem(actionsSlots[slot], Lang.getIcon("keybinds." + action.getKey().getKey(), Material.REDSTONE_TORCH), (p, s, i, a) -> {
               this.openActionSelectMenu(location, player, this, keybind, action, 1, p2 -> this.openMenu(location, p2, page));
               return false;
            });
         }
      }

      int maxPage = (keybinds.size() - 1) / keybindsSlots.length + 1;
      if (page > 1) {
         menu.addItem(previousSlot, Icon.getPageStack(Icon.PAGE_PREVIOUS_STACK, page, maxPage), (p, s, i, a) -> {
            if (page <= 1) {
               return false;
            } else {
               this.openMenu(location, p, page - 1);
               return false;
            }
         });
      }

      if (page < maxPage) {
         menu.addItem(nextSlot, Icon.getPageStack(Icon.PAGE_NEXT_STACK, page, maxPage), (p, s, i, a) -> {
            if (page >= maxPage) {
               return false;
            } else {
               this.openMenu(location, p, page + 1);
               return false;
            }
         });
      }

      menu.addItem(49, Icon.SCRIPT_CENTER, (p, s, i, a) -> {
         this.openScriptsMenu(location, player);
         return false;
      });
      menu.open(new Player[]{player});
   }

   public void openActionSelectMenu(Location location, Player player, Keybinds keybinds, Keybind keybind, Action action, int page, Consumer<Player> back) {
      ChestMenu menu = new ChestMenu(Lang.getString("messages.keybind.action-select-title"));
      List<Action> actions = keybinds.usableActions().stream().toList();

      int i;
      for (i = 0; i < Math.min(45, actions.size()); i++) {
         Action a = actions.get(i);
         menu.addItem(i, Lang.getIcon("keybinds." + a.getKey().getKey(), Material.REDSTONE_TORCH), (p, s, i1, a2) -> {
            keybind.set(location, keybinds, a);
            back.accept(player);
            return false;
         });
      }

      if (i < 45) {
         for (int j = i; j < (i + 8) / 9 * 9; j++) {
            menu.addItem(j, Icon.LIGHT_GRAY_BACKGROUND, (p, s, i1, a) -> false);
         }
      }

      i = (i + 8) / 9 * 9;
      int maxPage = (actions.size() - 1) / 45 + 1;

      for (int j = 0; j < 9; j++) {
         if (j == 1) {
            menu.addItem(i + j, Icon.getPageStack(Icon.PAGE_PREVIOUS_STACK, page, maxPage), (p, s, i1, a) -> {
               if (page <= 1) {
                  return false;
               } else {
                  this.openActionSelectMenu(location, p, keybinds, keybind, action, page - 1, back);
                  return false;
               }
            });
         } else if (j == 7) {
            menu.addItem(i + j, Icon.getPageStack(Icon.PAGE_NEXT_STACK, page, maxPage), (p, s, i1, a) -> {
               if (page >= maxPage) {
                  return false;
               } else {
                  this.openActionSelectMenu(location, p, keybinds, keybind, action, page + 1, back);
                  return false;
               }
            });
         } else {
            menu.addItem(i + j, Icon.BLUE_BACKGROUND, (p, s, i1, a) -> false);
         }
      }

      menu.open(new Player[]{player});
   }

   @NotNull
   public NamespacedKey getKey() {
      return this.key;
   }

   public void upload(Location location, Player p, String keybindsName) {
      KeybindsScript script = KeybindsScript.warp(p, this, keybindsName, location, ID.nextId());
      script.save();
      if (!keybindsScripts.containsKey(this.key)) {
         keybindsScripts.put(this.key, new ArrayList<>());
      }

      keybindsScripts.get(this.key).add(script);
   }

   public List<KeybindsScript> getScripts() {
      return keybindsScripts.getOrDefault(this.key, new ArrayList<>());
   }

   public void openScriptsMenu(Location location, Player player) {
      this.openScriptsMenu(location, player, 1);
   }

   public void openScriptsMenu(Location location, Player player, int page) {
      ChestMenu menu = new ChestMenu(Lang.getString("messages.keybind.scripts-title"));
      List<KeybindsScript> scripts = this.getScripts();

      for (int i = 0; i < 45; i++) {
         if (i >= scripts.size()) {
            menu.addItem(i, Icon.LIGHT_GRAY_BACKGROUND, (p, s, i1, a) -> false);
         } else {
            KeybindsScript script = scripts.get(i);
            menu.addItem(
               i,
               new CustomItemStack(
                  Material.PAPER,
                  String.format(Lang.getString("icons.keybinds.scripts.display.name"), script.getAuthorName(), script.getKeybindsName(), script.getId()),
                  new String[0]
               ),
               (p, s, i1, a) -> {
                  this.copyScript(location, script);
                  p.sendMessage(Lang.getString("messages.keybind.scripts.copied"));
                  return false;
               }
            );
         }
      }

      for (int ix = 45; ix < 54; ix++) {
         menu.addItem(ix, Icon.GRAY_BACKGROUND, (p, s, i1, a) -> false);
      }

      int maxPage = (scripts.size() - 1) / 45 + 1;
      menu.addItem(46, Icon.getPageStack(Icon.PAGE_PREVIOUS_STACK, page, maxPage), (p, s, i1, a) -> {
         if (page <= 1) {
            return false;
         } else {
            this.openScriptsMenu(location, p, page - 1);
            return false;
         }
      });
      menu.addItem(52, Icon.getPageStack(Icon.PAGE_NEXT_STACK, page, maxPage), (p, s, i1, a) -> {
         if (page >= maxPage) {
            return false;
         } else {
            this.openScriptsMenu(location, p, page + 1);
            return false;
         }
      });
      menu.addItem(49, Icon.UPLOAD_KEYBIND_SCRIPT, (p, s, i1, a) -> {
         p.closeInventory();
         p.sendMessage(Lang.getString("messages.keybind.scripts.upload"));
         ChatUtils.awaitInput(p, name -> {
            this.upload(location, player, name);
            this.openScriptsMenu(location, p, page);
         });
         return false;
      });
      menu.open(new Player[]{player});
   }

   public void copyScript(Location location, KeybindsScript script) {
      script.getCode().forEach((key, value) -> key.set(location, script.getKeybinds(), value));
   }

   @Generated
   public ActionResult getDefaultActionResult() {
      return this.defaultActionResult;
   }

   @Generated
   public void setDefaultActionResult(ActionResult defaultActionResult) {
      this.defaultActionResult = defaultActionResult;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof Keybinds other)) {
         return false;
      } else if (!other.canEqual(this)) {
         return false;
      } else {
         Object this$key = this.getKey();
         Object other$key = other.getKey();
         if (this$key == null ? other$key == null : this$key.equals(other$key)) {
            Object this$defaultActionResult = this.getDefaultActionResult();
            Object other$defaultActionResult = other.getDefaultActionResult();
            return this$defaultActionResult == null ? other$defaultActionResult == null : this$defaultActionResult.equals(other$defaultActionResult);
         } else {
            return false;
         }
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof Keybinds;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $key = this.getKey();
      result = result * 59 + ($key == null ? 43 : $key.hashCode());
      Object $defaultActionResult = this.getDefaultActionResult();
      return result * 59 + ($defaultActionResult == null ? 43 : $defaultActionResult.hashCode());
   }

   @Generated
   @Override
   public String toString() {
      return "Keybinds(key=" + this.getKey() + ", defaultActionResult=" + this.getDefaultActionResult() + ")";
   }
}
