package com.ytdd9527.networksexpansion.core.services;

import com.balugaq.netex.api.data.Language;
import com.google.common.base.Preconditions;
import com.ytdd9527.networksexpansion.utils.TextUtil;
import com.ytdd9527.networksexpansion.utils.itemstacks.ItemStackUtil;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.utils.Keys;
import io.github.sefiraat.networks.utils.Theme;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.ParametersAreNonnullByDefault;
import lombok.Generated;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LocalizationService {
   public static final Set<String> noticed = new HashSet<>();
   private static final Map<String, String> CACHE = new HashMap<>();
   private static final String KEY_NAME = ".name";
   private static final String KEY_LORE = ".lore";
   private static final String MSG_KEY_NULL = "key cannot be null";
   private static final String MSG_ID_NULL = "id cannot be null";
   private static final String MSG_MATERIAL_NULL = "Material cannot be null";
   private static final String MSG_ITEMSTACK_NULL = "ItemStack cannot be null";
   private static final String MSG_TEXTURE_NULL = "Texture cannot be null";
   @NotNull
   private final JavaPlugin plugin;
   @NotNull
   private final String langFolderName;
   @NotNull
   private final File langFolder;
   @NotNull
   private final List<String> languages;
   @NotNull
   private final Map<String, Language> langMap;
   private final String colorTagRegex = "<[a-zA-Z0-9_]+>";
   private final Pattern pattern = Pattern.compile("<[a-zA-Z0-9_]+>");
   @NotNull
   private String idPrefix = "";
   @NotNull
   private String itemGroupKey = "categories";
   @NotNull
   private String itemsKey = "items";
   @NotNull
   private String recipesKey = "recipes";

   public LocalizationService(@NotNull Networks plugin) {
      this(plugin.getJavaPlugin());
   }

   @ParametersAreNonnullByDefault
   public LocalizationService(JavaPlugin plugin) {
      this(plugin, "lang");
   }

   @ParametersAreNonnullByDefault
   public LocalizationService(JavaPlugin plugin, String folderName) {
      this.languages = new LinkedList<>();
      this.langMap = new LinkedHashMap<>();
      Preconditions.checkArgument(plugin != null, "The plugin instance should not be null");
      Preconditions.checkArgument(folderName != null, "The folder name should not be null");
      this.plugin = plugin;
      if (!plugin.getDataFolder().exists()) {
         plugin.getDataFolder().mkdir();
      }

      this.langFolderName = folderName;
      this.langFolder = new File(plugin.getDataFolder(), folderName);
      if (!this.langFolder.exists()) {
         this.langFolder.mkdir();
      }
   }

   @ParametersAreNonnullByDefault
   public LocalizationService(JavaPlugin plugin, String folderName, String langFile) {
      this(plugin, folderName);
      this.addLanguage(langFile);
   }

   @ParametersAreNonnullByDefault
   @NotNull
   public String getString(String key, Object... args) {
      return MessageFormat.format(this.getString(key), args);
   }

   @ParametersAreNonnullByDefault
   public void sendMessage(CommandSender sender, String messageKey, Object... args) {
      Preconditions.checkArgument(sender != null, "CommandSender cannot be null");
      Preconditions.checkArgument(messageKey != null, "Message key cannot be null");
      this.send(sender, MessageFormat.format(this.getString("messages." + messageKey), args));
   }

   @ParametersAreNonnullByDefault
   public void sendActionbarMessage(Player p, String messageKey, Object... args) {
      Preconditions.checkArgument(p != null, "Player cannot be null");
      Preconditions.checkArgument(messageKey != null, "Message key cannot be null");
      String message = MessageFormat.format(this.getString("messages." + messageKey), args);
      BaseComponent[] components = TextComponent.fromLegacyText(this.color(message));
      p.spigot().sendMessage(ChatMessageType.ACTION_BAR, components);
   }

   public final void addDefaultLanguage(@NotNull String langFilename) {
      this.addLanguage(langFilename);
   }

   public final void addLanguage(@NotNull String langFilename) {
      Preconditions.checkArgument(langFilename != null, "The language file name should not be null");
      File langFile = new File(this.langFolder, langFilename + ".yml");
      String resourcePath = this.langFolderName + "/" + langFilename + ".yml";
      if (!langFile.exists()) {
         try {
            this.plugin.saveResource(resourcePath, false);
         } catch (IllegalArgumentException var7) {
            this.plugin.getLogger().log(Level.SEVERE, "The language file {0} does not exist in jar file!", resourcePath);
            return;
         }
      }

      this.languages.add(langFilename);
      InputStream resource = this.plugin.getResource(resourcePath);
      if (resource == null) {
         this.plugin.getLogger().log(Level.SEVERE, "The language file " + resourcePath + " does not exist in jar file!");
      } else {
         InputStreamReader defaultReader = new InputStreamReader(resource, StandardCharsets.UTF_8);
         FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(defaultReader);
         this.langMap.put(langFilename, new Language(langFilename, langFile, defaultConfig));
      }
   }

   @NotNull
   public String getString0(@NotNull String path) {
      Preconditions.checkArgument(path != null, "path cannot be null");
      String cached = CACHE.get(path);
      if (cached != null) {
         return cached;
      } else {
         for (String lang : this.languages) {
            String localization = this.langMap.get(lang).getLang().getString(path);
            if (localization != null) {
               CACHE.put(path, localization);
               return localization;
            }
         }

         if (noticed.contains(path)) {
            return path;
         } else {
            noticed.add(path);
            Networks.getInstance().getLogger().severe("No localization found for path: " + path);
            return path;
         }
      }
   }

   @NotNull
   public List<String> getStringList(@NotNull String path) {
      Preconditions.checkArgument(path != null, "path cannot be null");

      for (String lang : this.languages) {
         List<String> localization = this.langMap.get(lang).getLang().getStringList(path);
         if (!localization.isEmpty()) {
            for (int i = 0; i < localization.size(); i++) {
               localization.set(i, this.color(localization.get(i)));
            }

            return localization;
         }
      }

      return new ArrayList<>();
   }

   @NotNull
   public String[] getStringArray(@NotNull String path) {
      return this.getStringList(path).stream().map(this::color).toList().toArray(new String[0]);
   }

   @NotNull
   protected JavaPlugin getPlugin() {
      return this.plugin;
   }

   @NotNull
   public String getString(@NotNull String path) {
      return this.color(this.getString0(path));
   }

   @ParametersAreNonnullByDefault
   @NotNull
   public SlimefunItemStack getItemBy(String key, String id, Material material, String... extraLore) {
      Preconditions.checkArgument(key != null, "key cannot be null");
      Preconditions.checkArgument(id != null, "id cannot be null");
      Preconditions.checkArgument(material != null, "Material cannot be null");
      SlimefunItemStack item = new SlimefunItemStack(
         (this.idPrefix + id).toUpperCase(Locale.ROOT), material, this.getString(key + "." + id + ".name"), this.getStringArray(key + "." + id + ".lore")
      );
      if (extraLore != null && extraLore.length != 0) {
         this.appendLore(item, extraLore);
      }

      return item;
   }

   @ParametersAreNonnullByDefault
   @NotNull
   public SlimefunItemStack getItemBy(String key, String id, String texture, String... extraLore) {
      Preconditions.checkArgument(key != null, "key cannot be null");
      Preconditions.checkArgument(id != null, "id cannot be null");
      Preconditions.checkArgument(texture != null, "Texture cannot be null");
      return this.appendLore(
         new SlimefunItemStack(
            (this.idPrefix + id).toUpperCase(Locale.ROOT), texture, this.getString(key + "." + id + ".name"), this.getStringArray(key + "." + id + ".lore")
         ),
         extraLore
      );
   }

   @ParametersAreNonnullByDefault
   @NotNull
   public SlimefunItemStack getItemBy(String key, String id, ItemStack itemStack, String... extraLore) {
      Preconditions.checkArgument(key != null, "key cannot be null");
      Preconditions.checkArgument(id != null, "id cannot be null");
      Preconditions.checkArgument(itemStack != null, "ItemStack cannot be null");
      return this.appendLore(
         new SlimefunItemStack(
            (this.idPrefix + id).toUpperCase(Locale.ROOT), itemStack, this.getString(key + "." + id + ".name"), this.getStringArray(key + "." + id + ".lore")
         ),
         extraLore
      );
   }

   @ParametersAreNonnullByDefault
   @NotNull
   public SlimefunItemStack getItemGroupItem(String id, Material material) {
      return this.getItemBy(this.itemGroupKey, id, material);
   }

   @ParametersAreNonnullByDefault
   @NotNull
   public SlimefunItemStack getItemGroupItem(String id, String texture) {
      return this.getItemBy(this.itemGroupKey, id, texture);
   }

   @ParametersAreNonnullByDefault
   @NotNull
   public SlimefunItemStack getItemGroupItem(String id, ItemStack itemStack) {
      return this.getItemBy(this.itemGroupKey, id, itemStack);
   }

   @NotNull
   public SlimefunItemStack getItem(@NotNull String id, @NotNull Material material, String... extraLore) {
      return this.getItemBy(this.itemsKey, id, material, extraLore);
   }

   @ParametersAreNonnullByDefault
   @NotNull
   public SlimefunItemStack getItem(String id, String texture, String... extraLore) {
      return this.getItemBy(this.itemsKey, id, texture, extraLore);
   }

   @ParametersAreNonnullByDefault
   @NotNull
   public SlimefunItemStack getItem(String id, ItemStack itemStack, String... extraLore) {
      return this.getItemBy(this.itemsKey, id, itemStack, extraLore);
   }

   @ParametersAreNonnullByDefault
   @NotNull
   public RecipeType getRecipeType(String id, Material material, String... extraLore) {
      return new RecipeType(Keys.customNewKey(this.getPlugin(), id), this.getItemBy(this.recipesKey, id, material, extraLore));
   }

   @ParametersAreNonnullByDefault
   @NotNull
   public RecipeType getRecipeType(String id, String texture, String... extraLore) {
      return new RecipeType(Keys.customNewKey(this.getPlugin(), id), this.getItemBy(this.recipesKey, id, texture, extraLore));
   }

   @ParametersAreNonnullByDefault
   @NotNull
   public RecipeType getRecipeType(String id, ItemStack itemStack, String... extraLore) {
      return new RecipeType(Keys.customNewKey(this.getPlugin(), id), this.getItemBy(this.recipesKey, id, itemStack, extraLore));
   }

   @NotNull
   private <T extends ItemStack> T appendLore(@NotNull T itemStack, @Nullable String... extraLore) {
      Preconditions.checkArgument(itemStack != null, "ItemStack cannot be null");
      if (extraLore != null && extraLore.length != 0) {
         ItemMeta meta = itemStack.getItemMeta();
         List<String> lore = (List<String>)(meta.hasLore() ? meta.getLore() : new ArrayList<>());
         if (lore == null) {
            lore = new ArrayList<>();
         }

         lore.addAll(this.color(Arrays.asList(extraLore)));
         meta.setLore(lore);
         itemStack.setItemMeta(meta);
      }

      return itemStack;
   }

   @NotNull
   public String color(@NotNull String str) {
      str = TextUtil.color(str);
      if (str.startsWith("<random_color>")) {
         str = str.replaceAll("<random_color>", "");
         str = TextUtil.colorPseudorandomString(str);
      }

      if (str.startsWith("<color_random_string>")) {
         str = str.replace("<color_random_string>", "");
         str = TextUtil.colorRandomString(str);
      }

      for (Matcher matcher = this.pattern.matcher(str); matcher.find(); matcher = this.pattern.matcher(str)) {
         boolean found = false;
         String colorCode = matcher.group();

         for (Theme testTheme : Theme.values()) {
            String name = testTheme.name();
            if (name.equalsIgnoreCase(colorCode.replace("<", "").replace(">", ""))) {
               str = str.replaceAll(colorCode, testTheme + "");
               found = true;
               break;
            }
         }

         if (!found) {
            break;
         }
      }

      return str;
   }

   @NotNull
   public List<String> color(@NotNull List<String> strList) {
      Preconditions.checkArgument(strList != null, "String list cannot be null");
      return strList.stream().map(this::color).collect(Collectors.toList());
   }

   @ParametersAreNonnullByDefault
   public void send(CommandSender sender, String message, Object... args) {
      sender.sendMessage(this.color(MessageFormat.format(message, args)));
   }

   @ParametersAreNonnullByDefault
   @NotNull
   public ItemStack getItemStack(String key, Material material) {
      return ItemStackUtil.getCleanItem(new CustomItemStack(material, this.getString(key + ".name"), this.getStringArray(key + ".lore")));
   }

   @ParametersAreNonnullByDefault
   @NotNull
   public ItemStack getIcon(String key, Material material) {
      return this.getItemStack("icons." + key, material);
   }

   @ParametersAreNonnullByDefault
   @NotNull
   public ItemStack getMechanism(String key) {
      return this.getIcon("mechanism." + key, Material.BOOK);
   }

   @Generated
   public void setIdPrefix(@NotNull String idPrefix) {
      if (idPrefix == null) {
         throw new NullPointerException("idPrefix is marked non-null but is null");
      } else {
         this.idPrefix = idPrefix;
      }
   }

   @NotNull
   @Generated
   public String getIdPrefix() {
      return this.idPrefix;
   }

   @Generated
   public void setItemGroupKey(@NotNull String itemGroupKey) {
      if (itemGroupKey == null) {
         throw new NullPointerException("itemGroupKey is marked non-null but is null");
      } else {
         this.itemGroupKey = itemGroupKey;
      }
   }

   @Generated
   public void setItemsKey(@NotNull String itemsKey) {
      if (itemsKey == null) {
         throw new NullPointerException("itemsKey is marked non-null but is null");
      } else {
         this.itemsKey = itemsKey;
      }
   }

   @Generated
   public void setRecipesKey(@NotNull String recipesKey) {
      if (recipesKey == null) {
         throw new NullPointerException("recipesKey is marked non-null but is null");
      } else {
         this.recipesKey = recipesKey;
      }
   }
}
