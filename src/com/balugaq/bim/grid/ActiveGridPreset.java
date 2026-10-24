package com.balugaq.bim.grid;

import com.balugaq.bim.BIMLoader;
import com.balugaq.bim.general.TransformationBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent.Builder;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform;
import org.bukkit.entity.TextDisplay.TextAlignment;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract class ActiveGridPreset extends GridPreset {
   public ActiveGridPreset(
      Plugin plugin, NamespacedKey identifier, @Range(from = 1L,to = 2147483647L) int height, @Range(from = 1L,to = 2147483647L) int width, float gap
   ) {
      super(plugin, identifier, height, width, gap);
   }

   public ActiveGridPreset(Plugin plugin, NamespacedKey identifier, @Range(from = 1L,to = 2147483647L) int height, @Range(from = 1L,to = 2147483647L) int width) {
      super(plugin, identifier, height, width);
   }

   @Override
   public void init(ActiveGrid active, int idx, InteractUnit unit) {
      Location location = unit.location;
      GridOrientation o = GridOrientation.fromYawPitch(location.getYaw(), location.getPitch());
      float gap = this.getGap();
      unit.itemDisplay = (ItemDisplay)location.getWorld().spawn(o.apply(location, (double)(gap * 0.5F), (double)(-gap * 0.5F), 0.0), ItemDisplay.class);
      unit.titleDisplay = (TextDisplay)location.getWorld()
         .spawn(o.apply(location, (double)(gap * 0.5F), (double)(-gap * 0.5F) + 0.03, 0.005), TextDisplay.class);
      unit.amountDisplay = (TextDisplay)location.getWorld().spawn(o.apply(location, (double)gap - 0.02, 0.01 - (double)gap, 0.004), TextDisplay.class);
      ItemStack stack = active.getItemInSlot(idx);
      unit.itemDisplay.setItemStack(stack);
      unit.itemDisplay.setItemDisplayTransform(ItemDisplayTransform.GUI);
      unit.itemDisplay.setTransformation(TransformationBuilder.create().scale(0.1F).leftRotation(o.getItemRotation()).build());
      unit.itemDisplay.setBrightness(GridUtil.KDB);
      active.tag(unit.itemDisplay);
      if (stack != null) {
         unit.titleDisplay.text(stack.effectiveName());
      }

      unit.titleDisplay.setDefaultBackground(false);
      unit.titleDisplay.setTransformation(TransformationBuilder.create().scale(0.1F).build());
      unit.titleDisplay.setBackgroundColor(Color.fromARGB(0));
      unit.titleDisplay.setBillboard(o.isHorizontal() ? Billboard.CENTER : Billboard.FIXED);
      unit.titleDisplay.setBrightness(GridUtil.MDB);
      unit.titleDisplay.setTextOpacity((byte)0);
      unit.titleDisplay.setAlignment(TextAlignment.CENTER);
      active.tag(unit.titleDisplay);
      if (stack != null) {
         unit.amountDisplay
            .text(
               ((Builder)((Builder)Component.text().color(NamedTextColor.WHITE)).append(Component.text(GridUtil.formatAmount((long)stack.getAmount()))))
                  .build()
            );
      }

      unit.amountDisplay.setTransformation(TransformationBuilder.create().scale(0.1F).build());
      unit.amountDisplay.setDefaultBackground(false);
      unit.amountDisplay.setBackgroundColor(Color.fromARGB(0));
      unit.amountDisplay.setBillboard(o.isHorizontal() ? Billboard.CENTER : Billboard.FIXED);
      unit.amountDisplay.setBrightness(GridUtil.MDB);
      unit.amountDisplay.setTextOpacity((byte)0);
      active.tag(unit.amountDisplay);
   }

   @Override
   public void onShow(ActiveGrid grid) {
      super.onShow(grid);
   }

   @Override
   public void onHide(ActiveGrid grid) {
      super.onHide(grid);
   }

   public BIMLoader getLoader() {
      return BIMLoader.get(this.plugin);
   }

   @Override
   public void onHover(InteractUnit unit, Player player) {
      unit.titleDisplay.setTextOpacity((byte)-1);
      unit.itemDisplay.setBrightness(GridUtil.MDB);
      unit.amountDisplay.setTextOpacity((byte)-1);
      InteractUnit old = this.getLoader().getCache().watching().put(player.getUniqueId(), unit);
      if (old != null && unit != old) {
         old.titleDisplay.setTextOpacity((byte)0);
         old.itemDisplay.setBrightness(GridUtil.KDB);
         old.amountDisplay.setTextOpacity((byte)0);
      }
   }

   @Override
   public void onScroll(ActiveGrid active, ScrollResult result, int delta, PlayerItemHeldEvent event) {
      if (!event.getPlayer().isSneaking()) {
         super.onScroll(active, result, delta, event);
         active.units.forEach((i, u) -> this.updateDisplayItem(active, i, u));
      }
   }

   @Override
   public void updateDisplayItem(ActiveGrid active, int idx, InteractUnit u) {
      ItemStack stack = active.getItemInSlot(idx + u.grid.scrollOffset * this.getWidth());
      u.itemDisplay.setItemStack(stack);
      if (stack == null) {
         u.amountDisplay.text(Component.empty());
      } else {
         u.amountDisplay
            .text(
               ((Builder)((Builder)Component.text().color(NamedTextColor.WHITE)).append(Component.text(GridUtil.formatAmount((long)stack.getAmount()))))
                  .build()
            );
      }
   }
}
