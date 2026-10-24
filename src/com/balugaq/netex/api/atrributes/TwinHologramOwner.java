package com.balugaq.netex.api.atrributes;

import com.ytdd9527.networksexpansion.utils.TextUtil;
import io.github.thebusybiscuit.slimefun4.core.attributes.ItemAttribute;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TwinHologramOwner extends ItemAttribute {
   default void updateHolograms(@NotNull Block block, @Nullable String topText, @NotNull String bottomText) {
      this.updateTopHologram(block, topText);
      this.updateBottomHologram(block, bottomText);
   }

   default void updateTopHologram(@NotNull Block block, @Nullable String topText) {
      if (topText == null) {
         this.removeTopHologram(block);
      } else {
         Location locTop = block.getLocation().add(this.getTopHologramOffset(block));
         Slimefun.getHologramsService().setHologramLabel(locTop, TextUtil.color(topText));
      }
   }

   default void updateBottomHologram(@NotNull Block block, @Nullable String bottomText) {
      if (bottomText == null) {
         this.removeBottomHologram(block);
      } else {
         Location locBot = block.getLocation().add(this.getBottomHologramOffset(block));
         Slimefun.getHologramsService().setHologramLabel(locBot, TextUtil.color(bottomText));
      }
   }

   default void removeHolograms(@NotNull Block block) {
      this.removeBottomHologram(block);
      this.removeTopHologram(block);
   }

   default void removeBottomHologram(@NotNull Block block) {
      Location locBot = block.getLocation().add(this.getBottomHologramOffset(block));
      Slimefun.getHologramsService().removeHologram(locBot);
   }

   default void removeTopHologram(@NotNull Block block) {
      Location locTop = block.getLocation().add(this.getTopHologramOffset(block));
      Slimefun.getHologramsService().removeHologram(locTop);
   }

   default double getSpacingBetweenBottomAndTop() {
      return 0.2;
   }

   @NotNull
   default Vector getTopHologramOffset(@NotNull Block block) {
      return this.getBottomHologramOffset(block).add(new Vector(0.0, this.getSpacingBetweenBottomAndTop(), 0.0));
   }

   @NotNull
   default Vector getBottomHologramOffset(@NotNull Block block) {
      return Slimefun.getHologramsService().getDefaultOffset();
   }
}
