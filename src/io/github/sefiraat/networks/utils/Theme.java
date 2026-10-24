package io.github.sefiraat.networks.utils;

import com.balugaq.netex.utils.Lang;
import com.ytdd9527.networksexpansion.utils.TextUtil;
import com.ytdd9527.networksexpansion.utils.itemstacks.ItemStackUtil;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.ParametersAreNonnullByDefault;
import lombok.Generated;
import net.guizhanss.minecraft.guizhanlib.gugu.minecraft.helpers.inventory.ItemStackHelper;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle.DustOptions;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public enum Theme {
   GOLD(ChatColor.GOLD, Lang.getString("theme.gold")),
   WHITE(ChatColor.WHITE, Lang.getString("theme.white")),
   AQUA(ChatColor.AQUA, Lang.getString("theme.aqua")),
   WARNING(ChatColor.YELLOW, Lang.getString("theme.warning")),
   ERROR(ChatColor.RED, Lang.getString("theme.error")),
   NOTICE(ChatColor.WHITE, Lang.getString("theme.notice")),
   PASSIVE(ChatColor.GRAY, Lang.getString("theme.passive")),
   SUCCESS(ChatColor.GREEN, Lang.getString("theme.success")),
   MAIN(ChatColor.of("#21588f"), Lang.getString("theme.main")),
   CLICK_INFO(ChatColor.of("#e4ed32"), Lang.getString("theme.click_info")),
   RESEARCH(ChatColor.of("#a60e03"), Lang.getString("theme.research")),
   CRAFTING(ChatColor.of("#dbcea9"), Lang.getString("theme.crafting")),
   MACHINE(ChatColor.of("#3295a8"), Lang.getString("theme.machine")),
   TOOL(ChatColor.of("#6b32a8"), Lang.getString("theme.tool")),
   MECHANISM(ChatColor.of("#3295a8"), Lang.getString("theme.mechanism")),
   FUEL(ChatColor.of("#112211"), Lang.getString("theme.fuel")),
   MATERIAL_CLASS(ChatColor.of("#a4c2ba"), Lang.getString("theme.material_class")),
   RECIPE_TYPE(ChatColor.of("#ffe89c"), Lang.getString("theme.recipe_type")),
   GUIDE(ChatColor.of("#444444"), Lang.getString("theme.guide"));

   private static final Theme[] cachedValues = values();
   @NotNull
   private final ChatColor color;
   @NotNull
   private final String loreLine;

   @ParametersAreNonnullByDefault
   private Theme(ChatColor color, String loreLine) {
      this.color = color;
      this.loreLine = loreLine;
   }

   @ParametersAreNonnullByDefault
   @NotNull
   public static SlimefunItemStack themedSlimefunItemStack(String id, ItemStack itemStack, Theme themeType, String name, String... lore) {
      ChatColor passiveColor = PASSIVE.getColor();
      List<String> finalLore = new ArrayList<>();
      finalLore.add("");

      for (String s : lore) {
         finalLore.add(passiveColor + s);
      }

      finalLore.add("");
      finalLore.add(applyThemeToString(CLICK_INFO, themeType.getLoreLine()));
      return new SlimefunItemStack(id, itemStack, applyThemeToString(themeType, name), finalLore.toArray(new String[finalLore.size() - 1]));
   }

   @ParametersAreNonnullByDefault
   @NotNull
   public static SlimefunItemStack themedSlimefunItemStack(String id, String texture, Theme themeType, String name, String... lore) {
      ChatColor passiveColor = PASSIVE.getColor();
      List<String> finalLore = new ArrayList<>();
      finalLore.add("");

      for (String s : lore) {
         finalLore.add(passiveColor + s);
      }

      finalLore.add("");
      finalLore.add(applyThemeToString(CLICK_INFO, themeType.getLoreLine()));
      return new SlimefunItemStack(id, texture, applyThemeToString(themeType, name), finalLore.toArray(new String[finalLore.size() - 1]));
   }

   @ParametersAreNonnullByDefault
   @NotNull
   public static SlimefunItemStack themedSlimefunItemStack(SlimefunItemStack sfis, Theme themeType) {
      String id = sfis.getItemId();
      ItemStack itemStack = ItemStackUtil.getCleanItem(sfis);
      String name = sfis.getDisplayName();
      ItemMeta meta = sfis.getItemMeta();
      List<String> lore = (List<String>)(meta == null ? new ArrayList<>() : meta.getLore());
      return themedSlimefunItemStack(
         id,
         itemStack,
         themeType,
         name == null ? Lang.getString("theme.name_not_found") : name,
         lore == null ? new String[]{Lang.getString("theme.lore_not_found")} : lore.toArray(new String[0])
      );
   }

   @ParametersAreNonnullByDefault
   @NotNull
   public static SlimefunItemStack tsItem(String id, ItemStack itemStack, Theme themeType, String name, String... lore) {
      List<String> finalLore = new ArrayList<>(Arrays.stream(lore).toList());
      finalLore.add("");
      finalLore.add(applyThemeToString(SUCCESS, themeType.getLoreLine()));
      return new SlimefunItemStack(id, itemStack, applyThemeToString(themeType, name), finalLore.toArray(new String[finalLore.size() - 1]));
   }

   @ParametersAreNonnullByDefault
   @NotNull
   public static SlimefunItemStack random(String id, ItemStack itemStack, Theme themeType, String name, String... lore) {
      String coloredName = TextUtil.colorPseudorandomString(name);
      ChatColor passiveColor = PASSIVE.getColor();
      List<String> finalLore = new ArrayList<>();
      finalLore.add("");

      for (String s : lore) {
         finalLore.add(passiveColor + s);
      }

      finalLore.add(applyThemeToString(SUCCESS, themeType.getLoreLine()));
      return new SlimefunItemStack(id, itemStack, coloredName, finalLore.toArray(new String[0]));
   }

   @ParametersAreNonnullByDefault
   @NotNull
   public static SlimefunItemStack random(SlimefunItemStack sfis, Theme themeType) {
      String id = sfis.getItemId();
      ItemStack itemStack = ItemStackUtil.getCleanItem(sfis);
      String name = sfis.getDisplayName();
      ItemMeta meta = sfis.getItemMeta();
      List<String> lore = (List<String>)(meta == null ? new ArrayList<>() : meta.getLore());
      return random(
         id,
         itemStack,
         themeType,
         name == null ? Lang.getString("theme.name_not_found") : name,
         lore == null ? new String[]{Lang.getString("theme.lore_not_found")} : lore.toArray(new String[0])
      );
   }

   @ParametersAreNonnullByDefault
   @NotNull
   public static SlimefunItemStack random(String id, String texture, Theme themeType, String name, String... lore) {
      String coloredName = TextUtil.colorPseudorandomString(name);
      ChatColor passiveColor = PASSIVE.getColor();
      List<String> finalLore = new ArrayList<>();
      finalLore.add("");

      for (String s : lore) {
         finalLore.add(passiveColor + s);
      }

      finalLore.add(applyThemeToString(SUCCESS, themeType.getLoreLine()));
      return new SlimefunItemStack(id, texture, coloredName, finalLore.toArray(new String[0]));
   }

   @ParametersAreNonnullByDefault
   @NotNull
   public static SlimefunItemStack model(String id, ItemStack itemStack, Theme themeType, String name, String... lore) {
      String coloredName = TextUtil.colorPseudorandomString(name);
      ChatColor passiveColor = PASSIVE.getColor();
      List<String> finalLore = new ArrayList<>();
      finalLore.add("");

      for (String s : lore) {
         finalLore.add(passiveColor + s);
      }

      finalLore.add(applyThemeToString(SUCCESS, themeType.getLoreLine()));
      return new SlimefunItemStack(id, itemStack, coloredName + Lang.getString("theme.model"), finalLore.toArray(new String[0]));
   }

   @ParametersAreNonnullByDefault
   @NotNull
   public static SlimefunItemStack model(String id, String texture, Theme themeType, String name, String... lore) {
      String coloredName = TextUtil.colorPseudorandomString(name);
      ChatColor passiveColor = PASSIVE.getColor();
      List<String> finalLore = new ArrayList<>();
      finalLore.add("");

      for (String s : lore) {
         finalLore.add(passiveColor + s);
      }

      finalLore.add(applyThemeToString(SUCCESS, themeType.getLoreLine()));
      return new SlimefunItemStack(id, texture, coloredName + Lang.getString("theme.model"), finalLore.toArray(new String[finalLore.size() - 1]));
   }

   @ParametersAreNonnullByDefault
   @NotNull
   public static SlimefunItemStack model(SlimefunItemStack sfis, Theme themeType) {
      String id = sfis.getItemId();
      AtomicReference<String> texture = new AtomicReference<>("");
      sfis.getSkullTexture().ifPresentOrElse(texture::set, () -> {
      });
      String name = sfis.getDisplayName();
      ItemMeta meta = sfis.getItemMeta();
      List<String> lore = (List<String>)(meta == null ? new ArrayList<>() : meta.getLore());
      return model(
         id,
         texture.get(),
         themeType,
         name == null ? Lang.getString("theme.name_not_found") : name,
         lore == null ? new String[]{Lang.getString("theme.lore_not_found")} : lore.toArray(new String[0])
      );
   }

   @ParametersAreNonnullByDefault
   @NotNull
   public static String applyThemeToString(Theme themeType, String string) {
      return themeType.getColor() + string;
   }

   @ParametersAreNonnullByDefault
   @NotNull
   public static ItemStack themedItemStack(Material material, Theme themeType, String name, String... lore) {
      ChatColor passiveColor = PASSIVE.getColor();
      List<String> finalLore = new ArrayList<>();
      finalLore.add("");

      for (String s : lore) {
         finalLore.add(passiveColor + s);
      }

      finalLore.add("");
      finalLore.add(applyThemeToString(CLICK_INFO, themeType.getLoreLine()));
      return ItemStackUtil.getCleanItem(new CustomItemStack(material, applyThemeToString(themeType, name), finalLore.toArray(new String[finalLore.size() - 1])));
   }

   @ParametersAreNonnullByDefault
   @NotNull
   public static ItemStack themedItemStack(ItemStack itemStack, Theme themeType) {
      String name = ItemStackHelper.getDisplayName(itemStack);
      ItemMeta meta = itemStack.getItemMeta();
      if (meta == null) {
         return itemStack;
      } else {
         List<String> lore = meta.getLore();
         if (lore == null) {
            lore = new ArrayList<>();
         }

         ChatColor passiveColor = PASSIVE.getColor();
         List<String> finalLore = new ArrayList<>();
         finalLore.add("");

         for (String s : lore) {
            finalLore.add(passiveColor + s);
         }

         finalLore.add("");
         finalLore.add(applyThemeToString(CLICK_INFO, themeType.getLoreLine()));
         return ItemStackUtil.getCleanItem(new CustomItemStack(itemStack.getType(), applyThemeToString(themeType, name), finalLore));
      }
   }

   @NotNull
   public DustOptions getDustOptions(float size) {
      return new DustOptions(Color.fromRGB(this.color.getColor().getRed(), this.color.getColor().getGreen(), this.color.getColor().getBlue()), size);
   }

   @Override
   public String toString() {
      return this.color.toString();
   }

   @NotNull
   @Generated
   public ChatColor getColor() {
      return this.color;
   }

   @NotNull
   @Generated
   public String getLoreLine() {
      return this.loreLine;
   }
}
