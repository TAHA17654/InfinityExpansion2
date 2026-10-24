package com.balugaq.netex.api.gui;

import com.balugaq.netex.api.helpers.Icon;
import com.balugaq.netex.utils.Lang;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.utils.TextUtil;
import io.github.sefiraat.networks.utils.MatchOption;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class MatchOptionMenu {
   private static final String KEY_MATCH_OPTION = "match_option";
   public static final Map<Location, MatchOption> MATCH_OPTION_MAP = new ConcurrentHashMap<>();
   private static final int CLOSE_SLOT = 53;
   @NotNull
   private static final int[] TOGGLE_SLOTS = new int[]{13, 22, 31, 40, 49};
   @NotNull
   private static final long[] IGNORE_FLAGS = new long[]{1L, 2L, 4L, 8L, 16L};
   @NotNull
   private static final String[] ICON_KEYS = new String[]{
      "match-option.ignore-durability",
      "match-option.ignore-enchantment",
      "match-option.ignore-attribute",
      "match-option.ignore-lore",
      "match-option.ignore-custom-model-data"
   };
   @NotNull
   private static final Material[] MATERIALS = new Material[]{
      Material.IRON_SWORD, Material.ENCHANTED_BOOK, Material.DIAMOND_CHESTPLATE, Material.WRITTEN_BOOK, Material.FILLED_MAP
   };

   private MatchOptionMenu() {
   }

   @NotNull
   public static MatchOption getMatchOption(@NotNull Location location) {
      return MATCH_OPTION_MAP.computeIfAbsent(location.clone(), MatchOptionMenu::load);
   }

   public static void setMatchOption(@NotNull Location location, @NotNull MatchOption option) {
      MATCH_OPTION_MAP.put(location.clone(), option);
      StorageCacheUtils.setData(location, "match_option", Long.toString(option.getIgnoreFlags()));
   }

   public static void openMenu(@NotNull Location location, @NotNull Player player) {
      MatchOption option = getMatchOption(location);
      ChestMenu menu = new ChestMenu(Lang.getString("messages.match-option.title"));
      menu.setSize(54);

      for (int slot = 0; slot < 54; slot++) {
         menu.addItem(slot, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
      }

      for (int i = 0; i < TOGGLE_SLOTS.length; i++) {
         int slot = TOGGLE_SLOTS[i];
         long flag = IGNORE_FLAGS[i];
         boolean enabled = (option.getIgnoreFlags() & flag) != 0L;
         menu.addItem(slot, getToggleStack(ICON_KEYS[i], MATERIALS[i], enabled));
         menu.addMenuClickHandler(slot, (p, s, item, action) -> {
            MatchOption current = getMatchOption(location);
            setMatchOption(location, current.withIgnore(flag, !enabled));
            openMenu(location, p);
            return false;
         });
      }

      menu.addItem(53, Icon.MATCH_OPTION_CLOSE);
      menu.addMenuClickHandler(53, (p, s, item, action) -> {
         p.closeInventory();
         return false;
      });
      menu.open(new Player[]{player});
   }

   @NotNull
   private static ItemStack getToggleStack(@NotNull String iconKey, @NotNull Material material, boolean enabled) {
      ItemStack stack = Lang.getIcon(iconKey, material);
      ItemMeta meta = stack.getItemMeta();
      if (meta == null) {
         return stack;
      } else {
         List<String> lore = new ArrayList<>(meta.getLore() == null ? List.of() : meta.getLore());
         lore.add(TextUtil.GRAY + Lang.getString("messages.match-option.click_tip"));
         lore.add(enabled ? TextUtil.GREEN + Lang.getString("messages.match-option.enabled") : TextUtil.RED + Lang.getString("messages.match-option.disabled"));
         meta.setLore(lore);
         stack.setItemMeta(meta);
         return stack;
      }
   }

   @NotNull
   private static MatchOption load(@NotNull Location location) {
      String raw = StorageCacheUtils.getData(location, "match_option");
      if (raw == null) {
         return MatchOption.DEFAULT;
      } else {
         try {
            return MatchOption.of(Long.parseLong(raw.trim()));
         } catch (NumberFormatException var3) {
            return MatchOption.DEFAULT;
         }
      }
   }
}
