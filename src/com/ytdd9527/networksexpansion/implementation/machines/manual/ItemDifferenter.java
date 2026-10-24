package com.ytdd9527.networksexpansion.implementation.machines.manual;

import com.balugaq.netex.api.helpers.Icon;
import com.ytdd9527.networksexpansion.implementation.ExpansionItems;
import io.github.sefiraat.networks.network.NodeType;
import io.github.sefiraat.networks.slimefun.network.NetworkObject;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.ParametersAreNonnullByDefault;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
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
import org.jetbrains.annotations.NotNull;

public class ItemDifferenter extends NetworkObject {
   private static final int[] BACKGROUND_SLOTS = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 12, 14, 15, 17, 18, 19, 20, 21, 23, 24, 25, 26};
   private static final int ITEM_1_SLOT = 10;
   private static final int DIFF_SLOT = 13;
   private static final int ITEM_2_SLOT = 16;
   private static final int RESULT_SLOT = 22;

   @ParametersAreNonnullByDefault
   public ItemDifferenter(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe, NodeType.ITEM_DIFFERENTER);
      this.getSlotsToDrop().add(10);
      this.getSlotsToDrop().add(16);
   }

   public void postRegister() {
      new BlockMenuPreset(this.getId(), this.getItemName()) {
         public void init() {
            this.setSize(27);

            for (int slot : ItemDifferenter.BACKGROUND_SLOTS) {
               this.addItem(slot, Icon.GRAY_BACKGROUND);
            }

            this.addItem(13, Icon.DIFF_BUTTON);
            this.addItem(22, Icon.DIFF_RESULT_ICON);
         }

         public boolean canOpen(@NotNull Block block, @NotNull Player player) {
            return player.hasPermission("slimefun.inventory.bypass")
               || ExpansionItems.ITEM_DIFFERENTER.canUse(player, false)
                  && Slimefun.getProtectionManager().hasPermission(player, block.getLocation(), Interaction.INTERACT_BLOCK);
         }

         public void newInstance(@NotNull BlockMenu menu, @NotNull Block b) {
            for (int slot : ItemDifferenter.BACKGROUND_SLOTS) {
               menu.addMenuClickHandler(slot, ChestMenuUtils.getEmptyClickHandler());
            }

            menu.addMenuClickHandler(
               13,
               (p, s, i, a) -> {
                  ItemStack i1 = menu.getItemInSlot(10);
                  ItemStack i2 = menu.getItemInSlot(16);
                  return i1 != null && !i1.getType().isAir() && i2 != null && !i2.getType().isAir()
                     ? ItemDifferenter.setResult(menu, ItemDifferenter.calc(i1, i2), i1.isSimilar(i2))
                     : ItemDifferenter.setResult(menu, "no-item", false);
               }
            );
            menu.addMenuClickHandler(22, ChestMenuUtils.getEmptyClickHandler());
         }

         public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
            return new int[0];
         }
      };
   }

   public static boolean setResult(BlockMenu menu, String result, boolean isSimilar) {
      menu.replaceExistingItem(22, getResultIcon(result, isSimilar));
      return false;
   }

   public static ItemStack getResultIcon(String result, boolean isSimilar) {
      ItemStack i = Icon.DIFF_RESULT_ICON.clone();
      ItemMeta meta = i.getItemMeta();
      meta.setDisplayName((result.equals("no-item") ? "" : "isSimilar: " + isSimilar + " | ") + meta.getDisplayName() + ": " + result);
      i.setItemMeta(meta);
      return i;
   }

   public static String calc(ItemStack i1, ItemStack i2) {
      if (i1.getType() != i2.getType()) {
         return "neq.type";
      } else if (i1.getAmount() > i2.getAmount()) {
         return "neq.amount";
      } else if (StackUtils.isBlacklisted(i1) || StackUtils.isBlacklisted(i2)) {
         return "banned.blacklist";
      } else if (i1.hasItemMeta() && i2.hasItemMeta()) {
         ItemMeta itemMeta = i1.getItemMeta();
         ItemMeta cachedMeta = i2.getItemMeta();
         if (itemMeta != null && cachedMeta != null) {
            if (!itemMeta.getClass().equals(cachedMeta.getClass())) {
               return "neq.metaclass";
            } else {
               String r = canQuickEscapeMetaVariant(itemMeta, cachedMeta);
               if (r != null) {
                  return r;
               } else if (itemMeta.hasDisplayName() != cachedMeta.hasDisplayName()) {
                  return "neq.hasdisplayname";
               } else if (!itemMeta.getPersistentDataContainer().equals(cachedMeta.getPersistentDataContainer())) {
                  return "neq.pdc";
               } else if (!itemMeta.getEnchants().equals(cachedMeta.getEnchants())) {
                  return "neq.enchant";
               } else if (!itemMeta.getItemFlags().equals(cachedMeta.getItemFlags())) {
                  return "neq.itemflag";
               } else {
                  boolean hasAttributeOne = itemMeta.hasAttributeModifiers();
                  boolean hasAttributeTwo = cachedMeta.hasAttributeModifiers();
                  if (hasAttributeOne) {
                     if (!hasAttributeTwo || !Objects.equals(itemMeta.getAttributeModifiers(), cachedMeta.getAttributeModifiers())) {
                        return "neq.attribute";
                     }
                  } else if (hasAttributeTwo) {
                     return "neq.attribute";
                  }

                  if (StackUtils.IS_1_20_5) {
                     if (itemMeta.isFireResistant() != cachedMeta.isFireResistant()) {
                        return "neq.fireresistant";
                     }

                     if (itemMeta.isUnbreakable() != cachedMeta.isUnbreakable()) {
                        return "neq.unbreakable";
                     }

                     if (itemMeta.isHideTooltip() != cachedMeta.isHideTooltip()) {
                        return "neq.hidetooltip";
                     }

                     boolean hasRarityOne = itemMeta.hasRarity();
                     boolean hasRarityTwo = cachedMeta.hasRarity();
                     if (hasRarityOne) {
                        if (!hasRarityTwo || itemMeta.getRarity() != cachedMeta.getRarity()) {
                           return "neq.rarity";
                        }
                     } else if (hasRarityTwo) {
                        return "neq.rarity";
                     }

                     if (itemMeta.hasFood() && cachedMeta.hasFood()) {
                        if (!Objects.equals(itemMeta.getFood(), cachedMeta.getFood())) {
                           return "neq.food";
                        }
                     } else if (itemMeta.hasFood() != cachedMeta.hasFood()) {
                        return "neq.food";
                     }

                     if (itemMeta.hasTool() && cachedMeta.hasTool()) {
                        if (!Objects.equals(itemMeta.getTool(), cachedMeta.getTool())) {
                           return "neq.tool";
                        }
                     } else if (itemMeta.hasTool() != cachedMeta.hasTool()) {
                        return "neq.tool";
                     }

                     if (StackUtils.IS_1_21) {
                        if (itemMeta.hasJukeboxPlayable() && cachedMeta.hasJukeboxPlayable()) {
                           if (!Objects.equals(itemMeta.getJukeboxPlayable(), cachedMeta.getJukeboxPlayable())) {
                              return "neq.jukeboxplayable";
                           }
                        } else if (itemMeta.hasJukeboxPlayable() != cachedMeta.hasJukeboxPlayable()) {
                           return "neq.jukeboxplayable";
                        }
                     }
                  }

                  if (itemMeta.hasLore() && cachedMeta.hasLore()) {
                     if (!Objects.equals(itemMeta.getLore(), cachedMeta.getLore())) {
                        return "neq.lore";
                     }
                  } else if (itemMeta.hasLore() != cachedMeta.hasLore()) {
                     return "neq.lore";
                  }

                  Optional<String> optionalStackId1 = Slimefun.getItemDataService().getItemData(itemMeta);
                  Optional<String> optionalStackId2 = Slimefun.getItemDataService().getItemData(cachedMeta);
                  if (optionalStackId1.isPresent() != optionalStackId2.isPresent()) {
                     return "neq.sfid";
                  } else if (optionalStackId1.isPresent()) {
                     if (optionalStackId1.get().equals(optionalStackId2.get())) {
                        boolean hasCustomOne = itemMeta.hasCustomModelData();
                        boolean hasCustomTwo = cachedMeta.hasCustomModelData();
                        if (hasCustomOne) {
                           return hasCustomTwo && itemMeta.getCustomModelData() == cachedMeta.getCustomModelData() ? "eq" : "neq.custommodeldata";
                        } else {
                           return !hasCustomTwo ? "eq" : "neq.custommodeldata";
                        }
                     } else {
                        return "neq.sfid";
                     }
                  } else {
                     return itemMeta.hasDisplayName() && !Objects.equals(itemMeta.getDisplayName(), cachedMeta.getDisplayName()) ? "neq.displayname" : "eq";
                  }
               }
            }
         } else {
            return itemMeta == cachedMeta ? "eq" : "neq.hasmeta2";
         }
      } else {
         return i1.hasItemMeta() == i2.hasItemMeta() ? "eq" : "neq.hasmeta";
      }
   }

   public static String canQuickEscapeMetaVariant(@NotNull ItemMeta metaOne, @NotNull ItemMeta metaTwo) {
      if (metaOne instanceof Damageable instanceOne && metaTwo instanceof Damageable instanceTwo) {
         if (instanceOne.hasDamage() != instanceTwo.hasDamage()) {
            return "neq.damageable.damage.has";
         }

         if (instanceOne.getDamage() != instanceTwo.getDamage()) {
            return "neq.damageable.damage.get";
         }
      }

      if (metaOne instanceof Repairable instanceOne && metaTwo instanceof Repairable instanceTwo) {
         if (instanceOne.hasRepairCost() != instanceTwo.hasRepairCost()) {
            return "neq.repairable.repaircost.has";
         }

         if (instanceOne.getRepairCost() != instanceTwo.getRepairCost()) {
            return "neq.repairable.repaircost.get";
         }
      }

      if (metaOne instanceof AxolotlBucketMeta instanceOne && metaTwo instanceof AxolotlBucketMeta instanceTwo) {
         if (instanceOne.hasVariant() != instanceTwo.hasVariant()) {
            return "neq.axolotl.variant.has";
         }

         if (!instanceOne.hasVariant() || !instanceTwo.hasVariant()) {
            return "neq.axolotl.variant.has";
         }

         if (instanceOne.getVariant() != instanceTwo.getVariant()) {
            return "neq.axolotl.variant.get";
         }
      }

      if (metaOne instanceof BannerMeta instanceOne && metaTwo instanceof BannerMeta instanceTwo) {
         if (instanceOne.numberOfPatterns() != instanceTwo.numberOfPatterns()) {
            return "neq.banner.pattern.number";
         }

         if (!instanceOne.getPatterns().equals(instanceTwo.getPatterns())) {
            return "neq.banner.pattern.get";
         }
      }

      if (metaOne instanceof BlockDataMeta instanceOne
         && metaTwo instanceof BlockDataMeta instanceTwo
         && instanceOne.hasBlockData() != instanceTwo.hasBlockData()) {
         return "neq.blockdata.has";
      }

      if (metaOne instanceof BlockStateMeta instanceOne && metaTwo instanceof BlockStateMeta instanceTwo) {
         if (instanceOne.hasBlockState() != instanceTwo.hasBlockState()) {
            return "neq.blockstate.has";
         }

         if (!instanceOne.getBlockState().equals(instanceTwo.getBlockState())) {
            return "neq.blockstate.get";
         }
      }

      if (metaOne instanceof BookMeta instanceOne && metaTwo instanceof BookMeta instanceTwo) {
         if (instanceOne.getPageCount() != instanceTwo.getPageCount()) {
            return "neq.book.pagecount.get";
         }

         if (!Objects.equals(instanceOne.getAuthor(), instanceTwo.getAuthor())) {
            return "neq.book.author.get";
         }

         if (!Objects.equals(instanceOne.getTitle(), instanceTwo.getTitle())) {
            return "neq.book.title.get";
         }

         if (!Objects.equals(instanceOne.getGeneration(), instanceTwo.getGeneration())) {
            return "neq.book.generation.get";
         }
      }

      if (metaOne instanceof BundleMeta instanceOne && metaTwo instanceof BundleMeta instanceTwo) {
         return "neq.bundle.banned";
      }

      if (metaOne instanceof CompassMeta instanceOne && metaTwo instanceof CompassMeta instanceTwo) {
         if (instanceOne.isLodestoneTracked() != instanceTwo.isLodestoneTracked()) {
            return "neq.compass.lodestone.tracked";
         }

         if (!Objects.equals(instanceOne.getLodestone(), instanceTwo.getLodestone())) {
            return "neq.compass.lodestone.get";
         }
      }

      if (metaOne instanceof CrossbowMeta instanceOne && metaTwo instanceof CrossbowMeta instanceTwo) {
         if (instanceOne.hasChargedProjectiles() != instanceTwo.hasChargedProjectiles()) {
            return "neq.crossbow.charged.has";
         }

         if (!instanceOne.getChargedProjectiles().equals(instanceTwo.getChargedProjectiles())) {
            return "neq.crossbow.charged.get";
         }
      }

      if (metaOne instanceof EnchantmentStorageMeta instanceOne && metaTwo instanceof EnchantmentStorageMeta instanceTwo) {
         if (instanceOne.hasStoredEnchants() != instanceTwo.hasStoredEnchants()) {
            return "neq.enchantment.has";
         }

         if (!instanceOne.getStoredEnchants().equals(instanceTwo.getStoredEnchants())) {
            return "neq.enchantment.get";
         }
      }

      if (metaOne instanceof FireworkEffectMeta instanceOne
         && metaTwo instanceof FireworkEffectMeta instanceTwo
         && !Objects.equals(instanceOne.getEffect(), instanceTwo.getEffect())) {
         return "neq.fireworkeffect.get";
      }

      if (metaOne instanceof FireworkMeta instanceOne && metaTwo instanceof FireworkMeta instanceTwo) {
         if (instanceOne.getPower() != instanceTwo.getPower()) {
            return "neq.firework.power.get";
         }

         if (!instanceOne.getEffects().equals(instanceTwo.getEffects())) {
            return "neq.firework.effect.get";
         }
      }

      if (metaOne instanceof LeatherArmorMeta instanceOne
         && metaTwo instanceof LeatherArmorMeta instanceTwo
         && !instanceOne.getColor().equals(instanceTwo.getColor())) {
         return "neq.leatherarmor.color.get";
      }

      if (metaOne instanceof MapMeta instanceOne && metaTwo instanceof MapMeta instanceTwo) {
         if (instanceOne.hasMapView() != instanceTwo.hasMapView()) {
            return "neq.map.view.has";
         }

         if (instanceOne.hasLocationName() != instanceTwo.hasLocationName()) {
            return "neq.map.loactionname.has";
         }

         if (instanceOne.hasColor() != instanceTwo.hasColor()) {
            return "neq.map.color.has";
         }

         if (!Objects.equals(instanceOne.getMapView(), instanceTwo.getMapView())) {
            return "neq.map.view.get";
         }

         if (!Objects.equals(instanceOne.getLocationName(), instanceTwo.getLocationName())) {
            return "neq.map.locationname.get";
         }

         if (!Objects.equals(instanceOne.getColor(), instanceTwo.getColor())) {
            return "neq.map.color.get";
         }
      }

      if (metaOne instanceof PotionMeta instanceOne && metaTwo instanceof PotionMeta instanceTwo) {
         if (StackUtils.IS_1_20_5) {
            if (instanceOne.getBasePotionType() != instanceTwo.getBasePotionType()) {
               return "neq.potion.type.get";
            }
         } else if (!Objects.equals(instanceOne.getBasePotionData(), instanceTwo.getBasePotionData())) {
            return "neq.potion.data.get";
         }

         if (instanceOne.hasCustomEffects() != instanceTwo.hasCustomEffects()) {
            return "neq.potion.customeeffect.has";
         }

         if (instanceOne.hasColor() != instanceTwo.hasColor()) {
            return "neq.potion.color.has";
         }

         if (!Objects.equals(instanceOne.getColor(), instanceTwo.getColor())) {
            return "neq.potion.color.get";
         }

         if (!instanceOne.getCustomEffects().equals(instanceTwo.getCustomEffects())) {
            return "neq.potion.customeffect.get";
         }
      }

      if (metaOne instanceof SkullMeta instanceOne && metaTwo instanceof SkullMeta instanceTwo) {
         if (instanceOne.hasOwner() != instanceTwo.hasOwner()) {
            return "neq.skull.owner.has";
         }

         if (!Objects.equals(instanceOne.getOwningPlayer(), instanceTwo.getOwningPlayer())) {
            return "neq.skull.owning.get";
         }
      }

      if (metaOne instanceof SuspiciousStewMeta instanceOne && metaTwo instanceof SuspiciousStewMeta instanceTwo) {
         if (instanceOne.hasCustomEffects() != instanceTwo.hasCustomEffects()) {
            return "neq.suspiciousstew.customeffect.has";
         }

         if (!Objects.equals(instanceOne.getCustomEffects(), instanceTwo.getCustomEffects())) {
            return "neq.suspiciousstew.customeffect.get";
         }
      }

      if (metaOne instanceof TropicalFishBucketMeta instanceOne && metaTwo instanceof TropicalFishBucketMeta instanceTwo) {
         if (instanceOne.hasVariant() != instanceTwo.hasVariant()) {
            return "neq.tropical.variant.has";
         }

         if (!instanceOne.getPattern().equals(instanceTwo.getPattern())) {
            return "neq.tropical.pattern.get";
         }

         if (!instanceOne.getBodyColor().equals(instanceTwo.getBodyColor())) {
            return "neq.tropical.bodycolor.get";
         }

         if (!instanceOne.getPatternColor().equals(instanceTwo.getPatternColor())) {
            return "neq.tropical.patterncolor.get";
         }
      }

      if (metaOne instanceof KnowledgeBookMeta instanceOne && metaTwo instanceof KnowledgeBookMeta instanceTwo) {
         if (instanceOne.hasRecipes() != instanceTwo.hasRecipes()) {
            return "neq.knowledge.recipe.has";
         }

         if (!Objects.equals(instanceOne.getRecipes(), instanceTwo.getRecipes())) {
            return "neq.knowledge.recipe.get";
         }
      }

      if (metaOne instanceof MusicInstrumentMeta instanceOne
         && metaTwo instanceof MusicInstrumentMeta instanceTwo
         && !Objects.equals(instanceOne.getInstrument(), instanceTwo.getInstrument())) {
         return "neq.music.instrument.get";
      }

      if (metaOne instanceof ArmorMeta instanceOne && metaTwo instanceof ArmorMeta instanceTwo && !Objects.equals(instanceOne.getTrim(), instanceTwo.getTrim())
         )
       {
         return "neq.armor.trim";
      }

      if (StackUtils.IS_1_20_5) {
         if (metaOne instanceof WritableBookMeta instanceOne && metaTwo instanceof WritableBookMeta instanceTwo) {
            if (instanceOne.getPageCount() != instanceTwo.getPageCount()) {
               return "neq.writablebook.page.count";
            }

            if (!Objects.equals(instanceOne.getPages(), instanceTwo.getPages())) {
               return "neq.writablebook.page.get";
            }
         }

         if (StackUtils.IS_1_21) {
            if (metaOne instanceof OminousBottleMeta instanceOne && metaTwo instanceof OminousBottleMeta instanceTwo) {
               if (instanceOne.hasAmplifier() != instanceTwo.hasAmplifier()) {
                  return "neq.ominous.amplifier.has";
               }

               if (instanceOne.getAmplifier() != instanceTwo.getAmplifier()) {
                  return "neq.ominous.amplifier.get";
               }
            }

            if (metaOne instanceof ShieldMeta instanceOne
               && metaTwo instanceof ShieldMeta instanceTwo
               && !Objects.equals(instanceOne.getBaseColor(), instanceTwo.getBaseColor())) {
               return "neq.shield.basecolor";
            }
         }
      }

      return null;
   }
}
