package io.github.sefiraat.networks.utils;

import com.balugaq.netex.api.enums.MinecraftVersion;
import com.ytdd9527.networksexpansion.utils.itemstacks.ItemStackUtil;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.network.stackcaches.ItemStackCache;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.data.persistent.PersistentDataAPI;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.ParametersAreNonnullByDefault;
import lombok.Generated;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.AxolotlBucketMeta;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BlockDataMeta;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.KnowledgeBookMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.MusicInstrumentMeta;
import org.bukkit.inventory.meta.OminousBottleMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.inventory.meta.ShieldMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.SuspiciousStewMeta;
import org.bukkit.inventory.meta.TropicalFishBucketMeta;
import org.bukkit.inventory.meta.WritableBookMeta;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class StackUtils {
   private static final boolean FORCE_CHECK_LORE = Networks.getConfigManager().isForceCheckLore();
   private static final MinecraftVersion MC_VERSION = Networks.getInstance().getMCVersion();
   public static final boolean IS_1_20_5 = MC_VERSION.isAtLeast(MinecraftVersion.V1_20_5);
   public static final boolean IS_1_21 = MC_VERSION.isAtLeast(MinecraftVersion.V1_21);
   public static final boolean IS_1_21_3 = MC_VERSION.isAtLeast(MinecraftVersion.V1_21_3);
   public static final boolean IS_1_21_4 = MC_VERSION.isAtLeast(MinecraftVersion.V1_21_4);

   private static MatchOption toOption(boolean checkLore, boolean checkAmount, boolean checkCustomModelId) {
      long flags = 0L;
      if (!checkLore) {
         flags |= 8L;
      }

      if (!checkCustomModelId) {
         flags |= 16L;
      }

      if (!checkAmount) {
         flags |= 32L;
      }

      return MatchOption.of(flags);
   }

   public static ItemStack getAsQuantity(@Nullable ItemStack itemStack, int amount) {
      if (itemStack == null) {
         return ItemStackUtil.AIR;
      } else {
         ItemStack clone = itemStack.clone();
         clone.setAmount(amount);
         return clone;
      }
   }

   public static boolean itemsMatch(
      @Nullable ItemStack itemStack1, @Nullable ItemStack itemStack2, boolean checkLore, boolean checkAmount, boolean checkCustomModelId
   ) {
      return itemsMatch(new ItemStackCache(itemStack1), itemStack2, toOption(checkLore, checkAmount, checkCustomModelId));
   }

   public static boolean itemsMatch(@Nullable ItemStack itemStack1, @Nullable ItemStack itemStack2, boolean checkLore, boolean checkAmount) {
      return itemsMatch(new ItemStackCache(itemStack1), itemStack2, toOption(checkLore, checkAmount, true));
   }

   public static boolean itemsMatch(@Nullable ItemStack itemStack1, @Nullable ItemStack itemStack2, boolean checkLore) {
      return itemsMatch(new ItemStackCache(itemStack1), itemStack2, toOption(checkLore, false, true));
   }

   public static boolean itemsMatch(@Nullable ItemStack itemStack1, @Nullable ItemStack itemStack2) {
      return itemsMatch(new ItemStackCache(itemStack1), itemStack2, MatchOption.DEFAULT);
   }

   public static boolean itemsMatch(ItemStackCache cache, @Nullable ItemStack itemStack, boolean checkLore, boolean checkAmount) {
      return itemsMatch(cache, itemStack, toOption(checkLore, checkAmount, true));
   }

   public static boolean itemsMatch(ItemStackCache cache, @Nullable ItemStack itemStack, boolean checkLore) {
      return itemsMatch(cache, itemStack, toOption(checkLore, false, true));
   }

   public static boolean itemsMatch(ItemStackCache cache, @Nullable ItemStack itemStack) {
      return itemsMatch(cache, itemStack, MatchOption.DEFAULT);
   }

   public static boolean itemsMatch(@Nullable ItemStack itemStack, ItemStackCache cache, boolean checkLore, boolean checkAmount, boolean checkCustomModelId) {
      return itemsMatch(cache, itemStack, toOption(checkLore, checkAmount, checkCustomModelId));
   }

   public static boolean itemsMatch(@Nullable ItemStack itemStack, ItemStackCache cache, boolean checkLore, boolean checkAmount) {
      return itemsMatch(cache, itemStack, toOption(checkLore, checkAmount, true));
   }

   public static boolean itemsMatch(@Nullable ItemStack itemStack, ItemStackCache cache, boolean checkLore) {
      return itemsMatch(cache, itemStack, toOption(checkLore, false, true));
   }

   public static boolean itemsMatch(@Nullable ItemStack itemStack, ItemStackCache cache) {
      return itemsMatch(cache, itemStack, MatchOption.DEFAULT);
   }

   public static boolean itemsMatch(ItemStackCache cache, @Nullable ItemStack itemStack, @Nullable MatchOption option) {
      MatchOption matchOption = option == null ? MatchOption.DEFAULT : option;
      boolean ignoreAmount = matchOption.isIgnoreAmount();
      if (cache.getItemStack() != null && itemStack != null) {
         if (itemStack.getType() != cache.getItemType()) {
            return false;
         } else if (!matchOption.isIgnoreAmount() && itemStack.getAmount() > cache.getItemStack().getAmount()) {
            return false;
         } else if (isBlacklisted(itemStack) || isBlacklisted(cache.getItemStack())) {
            return false;
         } else if (Networks.getConfigManager().useBukkitItemComparison()) {
            return itemStack.isSimilar(cache.getItemStack());
         } else {
            return IS_1_21_4
               ? itemsMatchModern(ItemStackUtil.asCraftItemStack(cache.getItemStack()), ItemStackUtil.asCraftItemStack(itemStack), matchOption)
               : legacyItemsMatch(cache, itemStack, matchOption);
         }
      } else {
         return itemStack == null && cache.getItemStack() == null;
      }
   }

   private static boolean legacyItemsMatch(ItemStackCache cache, ItemStack itemStack, MatchOption matchOption) {
      boolean ignoreDurability = matchOption.isIgnoreDurability();
      boolean ignoreEnchantment = matchOption.isIgnoreEnchantment();
      boolean ignoreAttribute = matchOption.isIgnoreAttribute();
      boolean ignoreLore = matchOption.isIgnoreLore();
      boolean ignoreCustomModelData = matchOption.isIgnoreCustomModelData();
      boolean itemHasComponents = ItemStackUtil.hasCustomComponents(itemStack);
      boolean cachedHasComponents = ItemStackUtil.hasCustomComponents(cache.getItemStack());
      if (itemHasComponents && cachedHasComponents) {
         ItemMeta itemMeta = itemStack.getItemMeta();
         ItemMeta cachedMeta = cache.getItemMeta();
         if (itemMeta != null && cachedMeta != null) {
            if (!itemMeta.getClass().equals(cachedMeta.getClass())) {
               return false;
            } else if (canQuickEscapeMetaVariant(itemMeta, cachedMeta, ignoreDurability)) {
               return false;
            } else if (itemMeta.hasDisplayName() != cachedMeta.hasDisplayName()) {
               return false;
            } else if (!itemMeta.getPersistentDataContainer().equals(cachedMeta.getPersistentDataContainer())) {
               return false;
            } else if (!ignoreEnchantment && !itemMeta.getEnchants().equals(cachedMeta.getEnchants())) {
               return false;
            } else if (!itemMeta.getItemFlags().equals(cachedMeta.getItemFlags())) {
               return false;
            } else {
               if (!ignoreAttribute) {
                  boolean hasAttributeOne = itemMeta.hasAttributeModifiers();
                  boolean hasAttributeTwo = cachedMeta.hasAttributeModifiers();
                  if (hasAttributeOne) {
                     if (!hasAttributeTwo || !Objects.equals(itemMeta.getAttributeModifiers(), cachedMeta.getAttributeModifiers())) {
                        return false;
                     }
                  } else if (hasAttributeTwo) {
                     return false;
                  }
               }

               if (IS_1_20_5) {
                  if (itemMeta.isFireResistant() != cachedMeta.isFireResistant()) {
                     return false;
                  }

                  if (itemMeta.isUnbreakable() != cachedMeta.isUnbreakable()) {
                     return false;
                  }

                  if (itemMeta.isHideTooltip() != cachedMeta.isHideTooltip()) {
                     return false;
                  }

                  boolean hasRarityOne = itemMeta.hasRarity();
                  boolean hasRarityTwo = cachedMeta.hasRarity();
                  if (hasRarityOne) {
                     if (!hasRarityTwo || itemMeta.getRarity() != cachedMeta.getRarity()) {
                        return false;
                     }
                  } else if (hasRarityTwo) {
                     return false;
                  }

                  if (itemMeta.hasFood() && cachedMeta.hasFood()) {
                     if (!Objects.equals(itemMeta.getFood(), cachedMeta.getFood())) {
                        return false;
                     }
                  } else if (itemMeta.hasFood() != cachedMeta.hasFood()) {
                     return false;
                  }

                  if (itemMeta.hasTool() && cachedMeta.hasTool()) {
                     if (!Objects.equals(itemMeta.getTool(), cachedMeta.getTool())) {
                        return false;
                     }
                  } else if (itemMeta.hasTool() != cachedMeta.hasTool()) {
                     return false;
                  }

                  if (IS_1_21) {
                     if (itemMeta.hasJukeboxPlayable() && cachedMeta.hasJukeboxPlayable()) {
                        if (!Objects.equals(itemMeta.getJukeboxPlayable(), cachedMeta.getJukeboxPlayable())) {
                           return false;
                        }
                     } else if (itemMeta.hasJukeboxPlayable() != cachedMeta.hasJukeboxPlayable()) {
                        return false;
                     }
                  }
               }

               if (!ignoreLore || shouldCompareLore(itemStack) || shouldCompareLore(cache.getItemStack())) {
                  if (itemMeta.hasLore() && cachedMeta.hasLore()) {
                     if (!Objects.equals(itemMeta.getLore(), cachedMeta.getLore())) {
                        return false;
                     }
                  } else if (itemMeta.hasLore() != cachedMeta.hasLore()) {
                     return false;
                  }
               }

               Optional<String> optionalStackId1 = Slimefun.getItemDataService().getItemData(itemMeta);
               Optional<String> optionalStackId2 = Slimefun.getItemDataService().getItemData(cachedMeta);
               if (optionalStackId1.isPresent() != optionalStackId2.isPresent()) {
                  return false;
               } else if (!optionalStackId1.isPresent()) {
                  return !itemMeta.hasDisplayName() || Objects.equals(itemMeta.getDisplayName(), cachedMeta.getDisplayName());
               } else if (!optionalStackId1.get().equals(optionalStackId2.get())) {
                  return false;
               } else if (!ignoreCustomModelData) {
                  boolean hasCustomOne = itemMeta.hasCustomModelData();
                  boolean hasCustomTwo = cachedMeta.hasCustomModelData();
                  return !hasCustomOne ? !hasCustomTwo : hasCustomTwo && itemMeta.getCustomModelData() == cachedMeta.getCustomModelData();
               } else {
                  return true;
               }
            }
         } else {
            return itemMeta == cachedMeta;
         }
      } else {
         return itemHasComponents == cachedHasComponents;
      }
   }

   private static boolean itemsMatchModern(ItemStack cacheItem, ItemStack itemStack, MatchOption option) {
      if (!cacheItem.matchesWithoutData(itemStack, option.toDataComponentSet(), true)) {
         return false;
      } else {
         return option.isIgnoreLore() && !shouldCompareLore(itemStack) && !shouldCompareLore(cacheItem)
            ? true
            : loreMatchesLoose(
               ((ItemLore)cacheItem.getData(DataComponentTypes.LORE)).styledLines(), ((ItemLore)itemStack.getData(DataComponentTypes.LORE)).styledLines()
            );
      }
   }

   private static boolean loreMatchesLoose(List<Component> a1, List<Component> a2) {
      if (a1.size() != a2.size()) {
         return false;
      } else {
         PlainTextComponentSerializer serializer = PlainTextComponentSerializer.plainText();

         for (int i = 0; i < a1.size(); i++) {
            if (!serializer.serialize(a1.get(i)).equals(serializer.serialize(a2.get(i)))) {
               return false;
            }
         }

         return true;
      }
   }

   private static boolean shouldCompareLore(ItemStack itemStack) {
      return FORCE_CHECK_LORE
         || itemStack.getMaxStackSize() == 1
         || itemStack.getType() == Material.PLAYER_HEAD
         || itemStack.getType() == Material.SPAWNER
         || itemStack.getType() == Material.SUGAR
         || itemStack.getType() == Material.MINECART
         || itemStack.getType() == Material.CHEST_MINECART;
   }

   public static boolean canQuickEscapeMetaVariant(ItemMeta metaOne, ItemMeta metaTwo) {
      return canQuickEscapeMetaVariant(metaOne, metaTwo, false);
   }

   public static boolean canQuickEscapeMetaVariant(ItemMeta metaOne, ItemMeta metaTwo, boolean ignoreDurability) {
      if (!ignoreDurability) {
         if (metaOne instanceof Damageable instanceOne && metaTwo instanceof Damageable instanceTwo) {
            if (instanceOne.hasDamage() != instanceTwo.hasDamage()) {
               return true;
            }

            if (instanceOne.hasDamage() && instanceOne.getDamage() != instanceTwo.getDamage()) {
               return true;
            }
         }

         if (metaOne instanceof Repairable instanceOne && metaTwo instanceof Repairable instanceTwo) {
            if (instanceOne.hasRepairCost() != instanceTwo.hasRepairCost()) {
               return true;
            }

            if (instanceOne.hasRepairCost() && instanceOne.getRepairCost() != instanceTwo.getRepairCost()) {
               return true;
            }
         }
      }

      if (metaOne instanceof AxolotlBucketMeta instanceOne && metaTwo instanceof AxolotlBucketMeta instanceTwo) {
         if (instanceOne.hasVariant() != instanceTwo.hasVariant()) {
            return true;
         }

         if (instanceOne.hasVariant() && instanceOne.getVariant() != instanceTwo.getVariant()) {
            return true;
         }
      }

      if (metaOne instanceof BannerMeta instanceOne && metaTwo instanceof BannerMeta instanceTwo) {
         if (instanceOne.numberOfPatterns() != instanceTwo.numberOfPatterns()) {
            return true;
         }

         if (!instanceOne.getPatterns().equals(instanceTwo.getPatterns())) {
            return true;
         }
      }

      if (metaOne instanceof BlockDataMeta instanceOne
         && metaTwo instanceof BlockDataMeta instanceTwo
         && instanceOne.hasBlockData() != instanceTwo.hasBlockData()) {
         return true;
      }

      if (metaOne instanceof BlockStateMeta instanceOne && metaTwo instanceof BlockStateMeta instanceTwo) {
         if (instanceOne.hasBlockState() != instanceTwo.hasBlockState()) {
            return true;
         }

         if (instanceOne.hasBlockState() && !instanceOne.getBlockState().equals(instanceTwo.getBlockState())) {
            return true;
         }
      }

      if (metaOne instanceof BookMeta instanceOne && metaTwo instanceof BookMeta instanceTwo) {
         if (instanceOne.getPageCount() != instanceTwo.getPageCount()) {
            return true;
         }

         if (!Objects.equals(instanceOne.getAuthor(), instanceTwo.getAuthor())) {
            return true;
         }

         if (!Objects.equals(instanceOne.getTitle(), instanceTwo.getTitle())) {
            return true;
         }

         if (!Objects.equals(instanceOne.getGeneration(), instanceTwo.getGeneration())) {
            return true;
         }
      }

      if (metaOne instanceof BundleMeta instanceOne && metaTwo instanceof BundleMeta instanceTwo) {
         return false;
      }

      if (metaOne instanceof CompassMeta instanceOne && metaTwo instanceof CompassMeta instanceTwo) {
         if (instanceOne.isLodestoneTracked() != instanceTwo.isLodestoneTracked()) {
            return true;
         }

         if (instanceOne.isLodestoneTracked() && !Objects.equals(instanceOne.getLodestone(), instanceTwo.getLodestone())) {
            return true;
         }
      }

      if (metaOne instanceof CrossbowMeta instanceOne && metaTwo instanceof CrossbowMeta instanceTwo) {
         if (instanceOne.hasChargedProjectiles() != instanceTwo.hasChargedProjectiles()) {
            return true;
         }

         if (instanceOne.hasChargedProjectiles() && !instanceOne.getChargedProjectiles().equals(instanceTwo.getChargedProjectiles())) {
            return true;
         }
      }

      if (metaOne instanceof EnchantmentStorageMeta instanceOne && metaTwo instanceof EnchantmentStorageMeta instanceTwo) {
         if (instanceOne.hasStoredEnchants() != instanceTwo.hasStoredEnchants()) {
            return true;
         }

         if (instanceOne.hasStoredEnchants() && !instanceOne.getStoredEnchants().equals(instanceTwo.getStoredEnchants())) {
            return true;
         }
      }

      if (metaOne instanceof FireworkEffectMeta instanceOne
         && metaTwo instanceof FireworkEffectMeta instanceTwo
         && !Objects.equals(instanceOne.getEffect(), instanceTwo.getEffect())) {
         return true;
      }

      if (metaOne instanceof FireworkMeta instanceOne && metaTwo instanceof FireworkMeta instanceTwo) {
         if (instanceOne.getPower() != instanceTwo.getPower()) {
            return true;
         }

         if (!instanceOne.getEffects().equals(instanceTwo.getEffects())) {
            return true;
         }
      }

      if (metaOne instanceof LeatherArmorMeta instanceOne
         && metaTwo instanceof LeatherArmorMeta instanceTwo
         && !instanceOne.getColor().equals(instanceTwo.getColor())) {
         return true;
      }

      if (metaOne instanceof MapMeta instanceOne && metaTwo instanceof MapMeta instanceTwo) {
         if (instanceOne.hasMapView() != instanceTwo.hasMapView()) {
            return true;
         }

         if (instanceOne.hasLocationName() != instanceTwo.hasLocationName()) {
            return true;
         }

         if (instanceOne.hasColor() != instanceTwo.hasColor()) {
            return true;
         }

         if (instanceOne.hasMapView() && !Objects.equals(instanceOne.getMapView(), instanceTwo.getMapView())) {
            return true;
         }

         if (instanceOne.hasLocationName() && !Objects.equals(instanceOne.getLocationName(), instanceTwo.getLocationName())) {
            return true;
         }

         if (instanceOne.hasColor() && !Objects.equals(instanceOne.getColor(), instanceTwo.getColor())) {
            return true;
         }
      }

      if (metaOne instanceof PotionMeta instanceOne && metaTwo instanceof PotionMeta instanceTwo) {
         if (IS_1_20_5) {
            if (instanceOne.getBasePotionType() != instanceTwo.getBasePotionType()) {
               return true;
            }
         } else if (!Objects.equals(instanceOne.getBasePotionData(), instanceTwo.getBasePotionData())) {
            return true;
         }

         if (instanceOne.hasCustomEffects() != instanceTwo.hasCustomEffects()) {
            return true;
         }

         if (instanceOne.hasColor() != instanceTwo.hasColor()) {
            return true;
         }

         if (instanceOne.hasColor() && !Objects.equals(instanceOne.getColor(), instanceTwo.getColor())) {
            return true;
         }

         if (instanceOne.hasCustomEffects() && !instanceOne.getCustomEffects().equals(instanceTwo.getCustomEffects())) {
            return true;
         }
      }

      if (metaOne instanceof SkullMeta instanceOne && metaTwo instanceof SkullMeta instanceTwo) {
         if (instanceOne.hasOwner() != instanceTwo.hasOwner()) {
            return true;
         }

         if (!Networks.getConfigManager().isDisableProfileCheck() && !Objects.equals(instanceOne.getOwningPlayer(), instanceTwo.getOwningPlayer())) {
            return true;
         }
      }

      if (metaOne instanceof SuspiciousStewMeta instanceOne && metaTwo instanceof SuspiciousStewMeta instanceTwo) {
         if (instanceOne.hasCustomEffects() != instanceTwo.hasCustomEffects()) {
            return true;
         }

         if (instanceOne.hasCustomEffects() && !Objects.equals(instanceOne.getCustomEffects(), instanceTwo.getCustomEffects())) {
            return true;
         }
      }

      if (metaOne instanceof TropicalFishBucketMeta instanceOne && metaTwo instanceof TropicalFishBucketMeta instanceTwo) {
         if (instanceOne.hasVariant() != instanceTwo.hasVariant()) {
            return true;
         }

         if (instanceOne.hasVariant()) {
            if (!instanceOne.getPattern().equals(instanceTwo.getPattern())) {
               return true;
            }

            if (!instanceOne.getBodyColor().equals(instanceTwo.getBodyColor())) {
               return true;
            }

            if (!instanceOne.getPatternColor().equals(instanceTwo.getPatternColor())) {
               return true;
            }
         }
      }

      if (metaOne instanceof KnowledgeBookMeta instanceOne && metaTwo instanceof KnowledgeBookMeta instanceTwo) {
         if (instanceOne.hasRecipes() != instanceTwo.hasRecipes()) {
            return true;
         }

         if (instanceOne.hasRecipes() && !Objects.equals(instanceOne.getRecipes(), instanceTwo.getRecipes())) {
            return true;
         }
      }

      if (metaOne instanceof MusicInstrumentMeta instanceOne
         && metaTwo instanceof MusicInstrumentMeta instanceTwo
         && !Objects.equals(instanceOne.getInstrument(), instanceTwo.getInstrument())) {
         return true;
      }

      if (metaOne instanceof ArmorMeta instanceOne && metaTwo instanceof ArmorMeta instanceTwo && !Objects.equals(instanceOne.getTrim(), instanceTwo.getTrim())
         )
       {
         return true;
      }

      if (IS_1_20_5) {
         if (metaOne instanceof WritableBookMeta instanceOne && metaTwo instanceof WritableBookMeta instanceTwo) {
            if (instanceOne.getPageCount() != instanceTwo.getPageCount()) {
               return true;
            }

            if (!Objects.equals(instanceOne.getPages(), instanceTwo.getPages())) {
               return true;
            }
         }

         if (IS_1_21) {
            if (metaOne instanceof OminousBottleMeta instanceOne && metaTwo instanceof OminousBottleMeta instanceTwo) {
               if (instanceOne.hasAmplifier() != instanceTwo.hasAmplifier()) {
                  return true;
               }

               if (instanceOne.hasAmplifier() && instanceOne.getAmplifier() != instanceTwo.getAmplifier()) {
                  return true;
               }
            }

            if (metaOne instanceof ShieldMeta instanceOne && metaTwo instanceof ShieldMeta instanceTwo) {
               return Objects.equals(instanceOne.getBaseColor(), instanceTwo.getBaseColor());
            }
         }
      }

      return false;
   }

   @ParametersAreNonnullByDefault
   public static void putOnCooldown(ItemStack itemStack, int durationInSeconds) {
      ItemMeta itemMeta = itemStack.getItemMeta();
      if (itemMeta != null) {
         PersistentDataAPI.setLong(itemMeta, Keys.ON_COOLDOWN, System.currentTimeMillis() + (long)durationInSeconds * 1000L);
         itemStack.setItemMeta(itemMeta);
      }
   }

   @ParametersAreNonnullByDefault
   public static boolean isOnCooldown(ItemStack itemStack) {
      ItemMeta itemMeta = itemStack.getItemMeta();
      return itemMeta != null ? System.currentTimeMillis() < Keys.getCooldown(itemMeta) : false;
   }

   public static boolean isBlacklisted(ItemStack itemStack) {
      return itemStack.getType() == Material.AIR
         || itemStack.getType().getMaxDurability() < 0
         || Tag.SHULKER_BOXES.isTagged(itemStack.getType())
         || isBundle(itemStack.getType());
   }

   private static boolean isBundle(Material material) {
      return material == Material.BUNDLE
         || IS_1_21_3
            && (
               material == Material.BLACK_BUNDLE
                  || material == Material.BLUE_BUNDLE
                  || material == Material.BROWN_BUNDLE
                  || material == Material.CYAN_BUNDLE
                  || material == Material.GRAY_BUNDLE
                  || material == Material.GREEN_BUNDLE
                  || material == Material.LIGHT_BLUE_BUNDLE
                  || material == Material.LIGHT_GRAY_BUNDLE
                  || material == Material.LIME_BUNDLE
                  || material == Material.MAGENTA_BUNDLE
                  || material == Material.ORANGE_BUNDLE
                  || material == Material.PINK_BUNDLE
                  || material == Material.PURPLE_BUNDLE
                  || material == Material.RED_BUNDLE
                  || material == Material.WHITE_BUNDLE
                  || material == Material.YELLOW_BUNDLE
            );
   }

   @Generated
   private StackUtils() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
