package com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell;

import com.balugaq.netex.utils.Lang;
import com.ytdd9527.networksexpansion.core.items.SpecialSlimefunItem;
import com.ytdd9527.networksexpansion.implementation.machines.cellnet.cell.menu.VoidCellMenu;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemHandler;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.NotPlaceable;
import java.util.ArrayList;
import java.util.List;
import net.guizhanss.minecraft.guizhanlib.gugu.minecraft.helpers.inventory.ItemStackHelper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class VoidCell extends SpecialSlimefunItem implements NotPlaceable {
   public VoidCell(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe);
   }

   public void preRegister() {
      this.addItemHandler(new ItemHandler[]{CellUseHandler.create(new CellUseHandler.CellUse() {
         @Override
         public boolean prepare(@NotNull Player player, @NotNull ItemStack cell) {
            VoidCellSupport.ensureInitialized(cell);
            VoidCell.this.renderLore(cell);
            return true;
         }

         @Override
         public void open(@NotNull Player player, @NotNull ItemStack cell) {
            VoidCell.this.openMenu(player, cell);
         }
      })});
   }

   protected void openMenu(@NotNull Player player, @NotNull ItemStack cellItem) {
      VoidCellMenu.openFromHand(player, cellItem);
   }

   public void renderLore(@NotNull ItemStack cellItem) {
      ItemMeta meta = cellItem.getItemMeta();
      if (meta != null) {
         List<String> lore = new ArrayList<>(Lang.getStringList("messages.cellnet.void.cell_lore"));
         lore.add(Lang.getString("messages.cellnet.void.extra_whitelist"));
         appendFilterSummary(lore, cellItem);
         meta.setLore(lore);
         cellItem.setItemMeta(meta);
      }
   }

   private static void appendFilterSummary(@NotNull List<String> lore, @NotNull ItemStack cellItem) {
      List<ItemStack> filters = VoidCellSupport.filtersView(cellItem);
      if (filters.isEmpty()) {
         lore.add(Lang.getString("messages.cellnet.void.lore_filter_empty"));
      } else {
         lore.add(Lang.getString("messages.cellnet.void.lore_filter_header", filters.size()));

         for (ItemStack filter : filters) {
            lore.add(Lang.getString("messages.cellnet.void.lore_filter_entry", ItemStackHelper.getDisplayName(filter)));
         }
      }
   }
}
