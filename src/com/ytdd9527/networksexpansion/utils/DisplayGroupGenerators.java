package com.ytdd9527.networksexpansion.utils;

import com.balugaq.netex.api.enums.Skins;
import com.balugaq.netex.api.enums.StorageUnitType;
import com.balugaq.netex.api.enums.Transformations;
import com.ytdd9527.networksexpansion.utils.itemstacks.ItemStackUtil;
import dev.sefiraat.sefilib.entity.display.DisplayGroup;
import dev.sefiraat.sefilib.entity.display.builders.ItemDisplayBuilder;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import lombok.Generated;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public final class DisplayGroupGenerators {
   @Deprecated
   public static final CustomItemStack BRIDGE_STACK = new CustomItemStack(Skins.BRIDGE1.getPlayerHead(), "", new String[0]);
   @Deprecated
   public static final CustomItemStack BRIDGE_CORNER_RIM_STACK = new CustomItemStack(Skins.BRIDGE2.getPlayerHead(), "", new String[0]);

   @NotNull
   public static DisplayGroup generateStorageUnit(@NotNull Location location, ItemStack stack, StorageUnitType type) {
      DisplayGroup displayGroup = new DisplayGroup(location, 1.1F, 0.5F);
      displayGroup.addDisplay(
         "StorageUnit_" + (type.ordinal() + 1),
         ((ItemDisplayBuilder)((ItemDisplayBuilder)new ItemDisplayBuilder().setGroupParentOffset(new Vector(0, 1, 0)))
               .setItemStack(ItemStackUtil.getCleanItem(stack))
               .setTransformation(Transformations.TWO.getTransformation()))
            .build(displayGroup)
      );
      return displayGroup;
   }

   @NotNull
   public static DisplayGroup generateCloche(@NotNull Location location) {
      DisplayGroup displayGroup = new DisplayGroup(location, 1.1F, 0.5F);
      displayGroup.addDisplay(
         "purge",
         ((ItemDisplayBuilder)((ItemDisplayBuilder)new ItemDisplayBuilder().setGroupParentOffset(new Vector(0, 1, 0)))
               .setItemStack(ItemStackUtil.getCleanItem(BRIDGE_STACK))
               .setTransformation(Transformations.TWO.getTransformation()))
            .build(displayGroup)
      );
      return displayGroup;
   }

   @NotNull
   public static DisplayGroup generateCell(@NotNull Location location) {
      DisplayGroup displayGroup = new DisplayGroup(location, 1.1F, 0.5F);
      displayGroup.addDisplay(
         "cell",
         ((ItemDisplayBuilder)((ItemDisplayBuilder)new ItemDisplayBuilder().setGroupParentOffset(new Vector(0, 1, 0)))
               .setItemStack(ItemStackUtil.getCleanItem(BRIDGE_STACK))
               .setTransformation(Transformations.TWO.getTransformation()))
            .build(displayGroup)
      );
      return displayGroup;
   }

   @NotNull
   public static DisplayGroup generatePowerNode(@NotNull Location location) {
      DisplayGroup displayGroup = new DisplayGroup(location, 2.0F, 0.5F);
      displayGroup.addDisplay(
         "powernode",
         ((ItemDisplayBuilder)((ItemDisplayBuilder)new ItemDisplayBuilder().setGroupParentOffset(new Vector(0, 0, 0)))
               .setItemStack(ItemStackUtil.getCleanItem(BRIDGE_STACK))
               .setTransformation(Transformations.NE_MODEL_CAPACITOR_5.getTransformation()))
            .build(displayGroup)
      );
      return displayGroup;
   }

   @NotNull
   public static DisplayGroup generateBridge1(@NotNull Location location) {
      DisplayGroup displayGroup = new DisplayGroup(location, 1.0F, 0.5F);
      displayGroup.addDisplay(
         "bridge_1",
         ((ItemDisplayBuilder)((ItemDisplayBuilder)new ItemDisplayBuilder().setGroupParentOffset(new Vector(0.0, 0.65, 0.0)))
               .setItemStack(ItemStackUtil.getCleanItem(BRIDGE_STACK))
               .setTransformation(Transformations.BRIDGE_1.getTransformation()))
            .build(displayGroup)
      );
      displayGroup.addDisplay(
         "bridge_3",
         ((ItemDisplayBuilder)((ItemDisplayBuilder)new ItemDisplayBuilder().setGroupParentOffset(new Vector(0.0, 0.9, 0.3)))
               .setItemStack(ItemStackUtil.getCleanItem(BRIDGE_CORNER_RIM_STACK))
               .setTransformation(Transformations.BRIDGE_3.getTransformation()))
            .build(displayGroup)
      );
      return displayGroup;
   }

   @NotNull
   public static DisplayGroup generateBridge2(@NotNull Location location) {
      DisplayGroup displayGroup = new DisplayGroup(location, 1.0F, 0.5F);
      displayGroup.addDisplay(
         "bridge_2",
         ((ItemDisplayBuilder)((ItemDisplayBuilder)new ItemDisplayBuilder().setGroupParentOffset(new Vector(0, 1, 0)))
               .setItemStack(ItemStackUtil.getCleanItem(BRIDGE_STACK))
               .setTransformation(Transformations.BRIDGE_2.getTransformation()))
            .build(displayGroup)
      );
      return displayGroup;
   }

   @NotNull
   public static DisplayGroup generateBridge3(@NotNull Location location) {
      DisplayGroup displayGroup = new DisplayGroup(location, 1.0F, 0.5F);
      displayGroup.addDisplay(
         "bridge_3",
         ((ItemDisplayBuilder)((ItemDisplayBuilder)new ItemDisplayBuilder().setGroupParentOffset(new Vector(0.0, 1.0, 0.4)))
               .setItemStack(ItemStackUtil.getCleanItem(BRIDGE_STACK))
               .setTransformation(Transformations.BRIDGE_3.getTransformation()))
            .build(displayGroup)
      );
      return displayGroup;
   }

   @NotNull
   public static DisplayGroup generateBridge4(@NotNull Location location) {
      DisplayGroup displayGroup = new DisplayGroup(location, 1.0F, 0.5F);
      displayGroup.addDisplay(
         "bridge_4",
         ((ItemDisplayBuilder)((ItemDisplayBuilder)new ItemDisplayBuilder().setGroupParentOffset(new Vector(0.0, 1.0, 0.25)))
               .setItemStack(ItemStackUtil.getCleanItem(BRIDGE_CORNER_RIM_STACK))
               .setTransformation(Transformations.BRIDGE_3.getTransformation()))
            .build(displayGroup)
      );
      return displayGroup;
   }

   @Generated
   private DisplayGroupGenerators() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
