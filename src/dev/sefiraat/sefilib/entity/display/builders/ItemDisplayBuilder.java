package dev.sefiraat.sefilib.entity.display.builders;

import dev.sefiraat.sefilib.entity.display.DisplayGroup;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform;
import org.bukkit.inventory.ItemStack;

public class ItemDisplayBuilder extends DisplayBuilder<ItemDisplayBuilder> {
   protected ItemStack itemStack;
   protected ItemDisplayTransform itemDisplayTransform;

   public ItemDisplayBuilder setItemStack(@Nullable ItemStack itemStack) {
      this.itemStack = itemStack;
      return this;
   }

   public ItemDisplayBuilder setItemDisplayTransform(@Nonnull ItemDisplayTransform itemDisplayTransform) {
      this.itemDisplayTransform = itemDisplayTransform;
      return this;
   }

   public ItemDisplay build() {
      if (this.location == null) {
         throw new IllegalStateException("You must provide a location for the Display Entity");
      } else {
         return this.generateDisplay(this.location);
      }
   }

   public ItemDisplay build(@Nonnull DisplayGroup group) {
      if (this.groupParentOffset == null) {
         throw new IllegalStateException("You must provide a Group Parent Offset vector");
      } else {
         return this.generateDisplay(group.getLocation().clone().add(this.groupParentOffset));
      }
   }

   private ItemDisplay generateDisplay(@Nonnull Location location) {
      ItemDisplay display = (ItemDisplay)location.getWorld().spawnEntity(location, EntityType.ITEM_DISPLAY);
      display.setItemStack(this.itemStack);
      if (this.itemDisplayTransform != null) {
         display.setItemDisplayTransform(this.itemDisplayTransform);
      }

      this.applyDisplay(display);
      return display;
   }
}
