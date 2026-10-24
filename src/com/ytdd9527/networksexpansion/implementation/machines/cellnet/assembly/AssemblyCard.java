package com.ytdd9527.networksexpansion.implementation.machines.cellnet.assembly;

import com.balugaq.netex.utils.Lang;
import com.ytdd9527.networksexpansion.core.items.SpecialSlimefunItem;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage.util.CRC32Utils;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.storage.util.SerializeUtils;
import com.ytdd9527.networksexpansion.utils.itemstacks.ItemStackUtil;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.utils.Keys;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.NotPlaceable;
import io.github.thebusybiscuit.slimefun4.libraries.dough.data.persistent.PersistentDataAPI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.guizhanss.minecraft.guizhanlib.gugu.minecraft.helpers.inventory.ItemStackHelper;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AssemblyCard extends SpecialSlimefunItem implements NotPlaceable {
   public static final NamespacedKey ASSEMBLY_CARD_GRID = new NamespacedKey(Networks.getInstance(), "ASSEMBLY_CARD_GRID");
   public static final int MAX_ENTRIES = 18;
   public static final int MAX_TOTAL_AMOUNT = 1152;
   private static final int GRID_SLOTS = 9;
   private static final String GRID_FIELD_PREFIX = "g";
   private static final String ENTRY_SEPARATOR = "\u0001";
   private static final String FIELD_SEPARATOR = "\u0002";

   public AssemblyCard(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe);
   }

   public void preRegister() {
   }

   public static boolean isCard(@Nullable ItemStack itemStack) {
      return itemStack != null && itemStack.getType() == Material.PAPER && SlimefunItem.getByItem(itemStack) instanceof AssemblyCard;
   }

   @Nullable
   public static AssemblyCard.RecipeEntry readCard(@NotNull ItemStack card) {
      if (!card.hasItemMeta()) {
         return null;
      } else {
         ItemMeta meta = card.getItemMeta();
         if (meta == null) {
            return null;
         } else {
            ItemStack output = null;
            String outputB64 = PersistentDataAPI.getString(meta, Keys.ASSEMBLY_CARD_OUTPUT);
            if (outputB64 != null && !outputB64.isEmpty()) {
               output = SerializeUtils.string2Object(outputB64);
            }

            if (output == null) {
               return null;
            } else {
               byte[] raw = PersistentDataAPI.getByteArray(meta, Keys.ASSEMBLY_CARD_INGREDIENTS);
               List<ItemStack> ingredients = decodeIngredients(raw);
               if (ingredients.isEmpty()) {
                  return null;
               } else {
                  byte[] gridRaw = PersistentDataAPI.getByteArray(meta, ASSEMBLY_CARD_GRID);
                  if (gridRaw != null && gridRaw.length != 0) {
                     ItemStack[] grid = decodeGrid(gridRaw);
                     if (grid == null) {
                        return null;
                     } else {
                        String recordedHash = PersistentDataAPI.getString(meta, Keys.ASSEMBLY_CARD_HASH);
                        return recordedHash != null && recordedHash.equals(fingerprint(ingredients, output, grid))
                           ? new AssemblyCard.RecipeEntry(ingredients, output, grid)
                           : null;
                     }
                  } else {
                     return null;
                  }
               }
            }
         }
      }
   }

   public static boolean writeCard(@NotNull ItemStack card, @NotNull List<ItemStack> mergedInputs, @NotNull ItemStack output, @NotNull ItemStack[] grid) {
      if (!RecipeLegitimacy.isValid(mergedInputs, output)) {
         return false;
      } else {
         ItemMeta meta = card.getItemMeta();
         if (meta == null) {
            return false;
         } else {
            ItemStack[] cleanGrid = sanitizeGrid(grid);
            if (cleanGrid == null) {
               return false;
            } else {
               PersistentDataAPI.setString(meta, Keys.ASSEMBLY_CARD_OUTPUT, SerializeUtils.object2String(output));
               PersistentDataAPI.setByteArray(meta, Keys.ASSEMBLY_CARD_INGREDIENTS, encodeIngredients(mergedInputs));
               PersistentDataAPI.setByteArray(meta, ASSEMBLY_CARD_GRID, encodeGrid(cleanGrid));
               PersistentDataAPI.setString(meta, Keys.ASSEMBLY_CARD_HASH, fingerprint(mergedInputs, output, cleanGrid));
               List<String> lore = buildLore(mergedInputs, output);
               meta.setLore(lore);
               card.setItemMeta(meta);
               return true;
            }
         }
      }
   }

   @NotNull
   private static List<String> buildLore(@NotNull List<ItemStack> ingredients, @NotNull ItemStack output) {
      List<String> lore = new ArrayList<>();
      lore.add(Lang.getString("messages.cellnet.assemblycard.lore_output"));
      lore.add(Lang.getString("messages.cellnet.assemblycard.lore_entry", output.getAmount(), ItemStackHelper.getDisplayName(output)));
      lore.add(Lang.getString("messages.cellnet.assemblycard.lore_inputs"));

      for (ItemStack ingredient : ingredients) {
         lore.add(Lang.getString("messages.cellnet.assemblycard.lore_entry", ingredient.getAmount(), ItemStackHelper.getDisplayName(ingredient)));
      }

      return lore;
   }

   @Nullable
   private static ItemStack[] sanitizeGrid(@Nullable ItemStack[] grid) {
      if (grid != null && grid.length == 9) {
         ItemStack[] clean = new ItemStack[9];
         boolean any = false;

         for (int i = 0; i < 9; i++) {
            ItemStack slot = grid[i];
            if (slot != null && !slot.getType().isAir()) {
               clean[i] = ItemStackUtil.getCleanItem(slot);
               any = true;
            }
         }

         return any ? clean : null;
      } else {
         return null;
      }
   }

   @NotNull
   public static List<ItemStack> mergeInputs(@NotNull List<ItemStack> inputs) {
      Map<ItemStack, ItemStack> merged = new LinkedHashMap<>();

      for (ItemStack input : inputs) {
         if (input != null && !input.getType().isAir()) {
            ItemStack probe = input.clone();
            probe.setAmount(1);
            ItemStack existing = merged.get(probe);
            if (existing == null) {
               merged.put(probe, input.clone());
            } else {
               existing.setAmount(existing.getAmount() + input.getAmount());
            }
         }
      }

      return new ArrayList<>(merged.values());
   }

   @NotNull
   private static byte[] encodeIngredients(@NotNull List<ItemStack> ingredients) {
      StringBuilder builder = new StringBuilder();

      for (int i = 0; i < ingredients.size(); i++) {
         ItemStack ingredient = ingredients.get(i);
         if (i > 0) {
            builder.append("\u0001");
         }

         builder.append(SerializeUtils.object2String(ingredient)).append("\u0002").append(ingredient.getAmount());
      }

      return builder.toString().getBytes(StandardCharsets.UTF_8);
   }

   @NotNull
   private static List<ItemStack> decodeIngredients(@Nullable byte[] raw) {
      if (raw != null && raw.length != 0) {
         String encoded = new String(raw, StandardCharsets.UTF_8);
         List<ItemStack> result = new ArrayList<>();

         for (String part : encoded.split("\u0001", -1)) {
            if (result.size() >= 18) {
               return List.of();
            }

            if (!part.isEmpty()) {
               String[] fields = part.split("\u0002", -1);
               if (fields.length != 2) {
                  return List.of();
               }

               ItemStack decoded = SerializeUtils.string2Object(fields[0]);
               if (decoded == null) {
                  return List.of();
               }

               try {
                  long amount = Long.parseLong(fields[1]);
                  if (amount <= 0L || amount > 1152L) {
                     return List.of();
                  }

                  decoded.setAmount((int)Math.min(amount, 2147483647L));
                  result.add(decoded);
               } catch (NumberFormatException var11) {
                  return List.of();
               }
            }
         }

         return result;
      } else {
         return List.of();
      }
   }

   @NotNull
   private static String fingerprint(@NotNull List<ItemStack> mergedInputs, @NotNull ItemStack output, @NotNull ItemStack[] grid) {
      List<String> parts = new ArrayList<>(mergedInputs.size());

      for (ItemStack input : mergedInputs) {
         parts.add(SerializeUtils.object2String(input) + input.getAmount());
      }

      Collections.sort(parts);
      StringBuilder builder = new StringBuilder(SerializeUtils.object2String(output));

      for (String part : parts) {
         builder.append("\u0001").append(part);
      }

      for (int i = 0; i < grid.length && i < 9; i++) {
         ItemStack slot = grid[i];
         builder.append("\u0001")
            .append("g")
            .append(i)
            .append("\u0002")
            .append(slot != null && !slot.getType().isAir() ? SerializeUtils.object2String(slot) : "");
      }

      return Integer.toHexString(CRC32Utils.compute(builder.toString()));
   }

   @NotNull
   private static byte[] encodeGrid(@NotNull ItemStack[] grid) {
      StringBuilder builder = new StringBuilder();

      for (int i = 0; i < 9; i++) {
         if (i > 0) {
            builder.append("\u0001");
         }

         ItemStack slot = grid[i];
         if (slot != null && !slot.getType().isAir()) {
            builder.append(SerializeUtils.object2String(slot));
         }
      }

      return builder.toString().getBytes(StandardCharsets.UTF_8);
   }

   @Nullable
   private static ItemStack[] decodeGrid(@Nullable byte[] raw) {
      if (raw != null && raw.length != 0) {
         String[] parts = new String(raw, StandardCharsets.UTF_8).split("\u0001", -1);
         if (parts.length != 9) {
            return null;
         } else {
            ItemStack[] grid = new ItemStack[9];

            for (int i = 0; i < 9; i++) {
               if (!parts[i].isEmpty()) {
                  ItemStack decoded = SerializeUtils.string2Object(parts[i]);
                  if (decoded == null) {
                     return null;
                  }

                  grid[i] = decoded;
               }
            }

            return grid;
         }
      } else {
         return null;
      }
   }

   public static record RecipeEntry(@NotNull List<ItemStack> ingredients, @NotNull ItemStack output, @NotNull ItemStack[] grid) {
   }
}
