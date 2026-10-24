package com.balugaq.netex.core.guide;

import com.balugaq.jeg.api.patches.JEGGuideSettings;
import com.balugaq.netex.utils.Lang;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.utils.Keys;
import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.core.guide.options.SlimefunGuideOption;
import io.github.thebusybiscuit.slimefun4.core.guide.options.SlimefunGuideSettings;
import io.github.thebusybiscuit.slimefun4.libraries.dough.data.persistent.PersistentDataAPI;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import java.util.Optional;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class QuantumSlimeHUDDisplayOption implements SlimefunGuideOption<Boolean> {
   public static final QuantumSlimeHUDDisplayOption instance = new QuantumSlimeHUDDisplayOption();

   public static QuantumSlimeHUDDisplayOption instance() {
      return instance;
   }

   public SlimefunAddon getAddon() {
      return Networks.getInstance();
   }

   public Optional<ItemStack> getDisplayItem(Player p, ItemStack guide) {
      boolean enabled = this.getSelectedOption(p, guide).orElse(false);
      ItemStack item = new CustomItemStack(
         isEnabled(p) ? Material.KNOWLEDGE_BOOK : Material.BOOK,
         Lang.getString(enabled ? "messages.guide.quantum-hud-display-mode.display-name-percent" : "messages.guide.quantum-hud-display-mode.display-name-value"),
         new String[]{
            "",
            Lang.getString(
               !enabled ? "messages.guide.quantum-hud-display-mode.click-tip-to-percent" : "messages.guide.quantum-hud-display-mode.click-tip-to-value"
            )
         }
      );
      return Optional.of(item);
   }

   public static boolean isEnabled(Player p) {
      return getSelectedOption(p);
   }

   public NamespacedKey getKey() {
      return key0();
   }

   public static boolean getSelectedOption(Player p) {
      return !PersistentDataAPI.hasByte(p, key0()) || PersistentDataAPI.getByte(p, key0()) == 1;
   }

   public static NamespacedKey key0() {
      return Keys.newKey("beginners_guide");
   }

   public void onClick(Player p, ItemStack guide) {
      this.setSelectedOption(p, guide, !this.getSelectedOption(p, guide).orElse(false));
      if (Networks.getSupportedPluginManager().isJustEnoughGuide()) {
         JEGGuideSettings.openSettings(p, guide);
      } else {
         SlimefunGuideSettings.openSettings(p, guide);
      }
   }

   public Optional<Boolean> getSelectedOption(Player p, ItemStack guide) {
      NamespacedKey key = this.getKey();
      boolean value = !PersistentDataAPI.hasByte(p, key) || PersistentDataAPI.getByte(p, key) == 1;
      return Optional.of(value);
   }

   public void setSelectedOption(Player p, ItemStack guide, Boolean value) {
      PersistentDataAPI.setByte(p, this.getKey(), (byte)(value ? 1 : 0));
   }
}
