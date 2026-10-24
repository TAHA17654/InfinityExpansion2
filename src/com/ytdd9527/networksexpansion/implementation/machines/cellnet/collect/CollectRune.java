package com.ytdd9527.networksexpansion.implementation.machines.cellnet.collect;

import com.balugaq.netex.utils.Lang;
import com.ytdd9527.networksexpansion.core.items.SpecialSlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemHandler;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemDropHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CollectRune extends SpecialSlimefunItem {
   private static final double RANGE = 1.5;

   public CollectRune(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe);
   }

   public void preRegister() {
      this.addItemHandler(new ItemHandler[]{(ItemDropHandler)(e, p, item) -> {
         if (!this.isItem(item.getItemStack())) {
            return false;
         } else if (!this.canUse(p, true)) {
            return true;
         } else {
            Slimefun.runSync(() -> this.activate(p, item), 20L);
            return true;
         }
      }});
   }

   private void activate(@NotNull Player player, @NotNull Item rune) {
      if (rune.isValid()) {
         Location l = rune.getLocation();
         Item target = l.getWorld().getNearbyEntities(l, 1.5, 1.5, 1.5, this::findMarkableTool).stream().map(Item.class::cast).findFirst().orElse(null);
         if (target != null) {
            ItemStack tool = target.getItemStack();
            l.getWorld().strikeLightningEffect(l);
            Slimefun.runSync(() -> {
               if (rune.isValid() && target.isValid() && tool.getAmount() == 1) {
                  target.remove();
                  rune.remove();
                  CollectService.markTool(tool);
                  l.getWorld().dropItemNaturally(l, tool);
                  player.sendMessage(Lang.getString("messages.cellnet.collect.apply-success"));
               }
            }, 10L);
         }
      }
   }

   private boolean findMarkableTool(@NotNull Entity entity) {
      if (!(entity instanceof Item item)) {
         return false;
      } else {
         ItemStack stack = item.getItemStack();
         return item.getPickupDelay() <= 0 && stack.getAmount() == 1 && isMarkableTool(stack) && !CollectService.hasMark(stack) && !this.isItem(stack);
      }
   }

   public static boolean isMarkableTool(@NotNull ItemStack stack) {
      Material type = stack.getType();
      return Tag.ITEMS_SWORDS.isTagged(type)
         || Tag.ITEMS_PICKAXES.isTagged(type)
         || Tag.ITEMS_AXES.isTagged(type)
         || Tag.ITEMS_HOES.isTagged(type)
         || Tag.ITEMS_SHOVELS.isTagged(type);
   }
}
