package com.balugaq.netex.api.algorithm;

import com.balugaq.netex.api.data.StorageUnitData;
import com.balugaq.netex.api.helpers.Icon;
import com.ytdd9527.networksexpansion.implementation.machines.managers.DrawerManager;
import com.ytdd9527.networksexpansion.utils.TextUtil;
import io.github.sefiraat.networks.network.stackcaches.BarrelIdentity;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map.Entry;
import net.guizhanss.minecraft.guizhanlib.gugu.minecraft.helpers.inventory.ItemStackHelper;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class Sorters {
   public static final String NO_ITEM = ItemStackHelper.getDisplayName(Icon.QUANTUM_STORAGE_NO_ITEM);
   public static final Comparator<Entry<ItemStack, Long>> ITEMSTACK_ALPHABETICAL_SORT = Comparator.comparing(entry -> {
      ItemStack itemStack = entry.getKey();
      SlimefunItem slimefunItem = SlimefunItem.getByItem(itemStack);
      return slimefunItem != null ? TextUtil.stripColor(slimefunItem.getItemName()) : TextUtil.stripColor(ItemStackHelper.getDisplayName(itemStack));
   }, Collator.getInstance(Locale.CHINA)::compare);
   public static final Comparator<Entry<ItemStack, Long>> ITEMSTACK_NUMERICAL_SORT = Entry.comparingByValue();
   public static final Comparator<Entry<ItemStack, Long>> ITEMSTACK_ADDON_SORT = Comparator.comparing(entry -> {
      ItemStack itemStack = entry.getKey();
      SlimefunItem slimefunItem = SlimefunItem.getByItem(itemStack);
      return slimefunItem != null ? TextUtil.stripColor(slimefunItem.getAddon().getName()) : "Minecraft";
   }, Collator.getInstance(Locale.CHINA)::compare);
   public static final Comparator<? super BarrelIdentity> BARREL_ALPHABETICAL_SORT = Comparator.comparing(barrel -> {
      ItemStack itemStack = barrel.getItemStack();
      if (itemStack == null) {
         return NO_ITEM;
      } else {
         SlimefunItem slimefunItem = SlimefunItem.getByItem(itemStack);
         return slimefunItem != null ? TextUtil.stripColor(slimefunItem.getItemName()) : TextUtil.stripColor(ItemStackHelper.getDisplayName(itemStack));
      }
   }, Collator.getInstance(Locale.CHINA)::compare);
   public static final Comparator<BarrelIdentity> BARREL_NUMERICAL_SORT = Comparator.comparingLong(BarrelIdentity::getAmount);
   public static final Comparator<? super StorageUnitData> DRAWER_ALPHABETICAL_SORT = Comparator.comparing(data -> {
      ItemStack itemStack = DrawerManager.getItemStack(data);
      if (itemStack == null) {
         return NO_ITEM;
      } else {
         SlimefunItem slimefunItem = SlimefunItem.getByItem(itemStack);
         return slimefunItem != null ? TextUtil.stripColor(slimefunItem.getItemName()) : TextUtil.stripColor(ItemStackHelper.getDisplayName(itemStack));
      }
   }, Collator.getInstance(Locale.CHINA)::compare);
   public static final Comparator<StorageUnitData> DRAWER_NUMERICAL_SORT = Comparator.comparingLong(StorageUnitData::getTotalAmountLong);
}
