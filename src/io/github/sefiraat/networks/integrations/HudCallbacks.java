package io.github.sefiraat.networks.integrations;

import com.balugaq.netex.api.data.StorageUnitData;
import com.balugaq.netex.core.guide.QuantumSlimeHUDDisplayOption;
import com.balugaq.netex.utils.Lang;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.implementation.machines.networks.advanced.AdvancedGreedyBlock;
import com.ytdd9527.networksexpansion.implementation.machines.unit.NetworksDrawer;
import com.ytdd9527.networksexpansion.utils.TextUtil;
import io.github.schntgaispock.slimehud.SlimeHUD;
import io.github.schntgaispock.slimehud.util.HudBuilder;
import io.github.schntgaispock.slimehud.waila.HudController;
import io.github.sefiraat.networks.network.stackcaches.QuantumCache;
import io.github.sefiraat.networks.slimefun.network.NetworkGreedyBlock;
import io.github.sefiraat.networks.slimefun.network.NetworkQuantumStorage;
import io.github.thebusybiscuit.slimefun4.core.guide.options.SlimefunGuideSettings;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import net.guizhanss.minecraft.guizhanlib.gugu.minecraft.helpers.inventory.ItemStackHelper;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class HudCallbacks {
   private static final String EMPTY = Lang.getString("messages.integrations.slimehud.empty_quantum_storage");

   public static void setup() {
      HudController controller = SlimeHUD.getHudController();
      controller.registerCustomHandler(
         NetworkQuantumStorage.class,
         request -> {
            Location location = request.getLocation();
            QuantumCache cache = NetworkQuantumStorage.getCaches().get(location);
            return cache != null && cache.getItemStack() != null
               ? format(request.getPlayer(), cache.getItemStack(), cache.getAmountLong(), cache.getLimitLong())
               : EMPTY;
         }
      );
      controller.registerCustomHandler(NetworkGreedyBlock.class, request -> {
         Location location = request.getLocation();
         BlockMenu menu = StorageCacheUtils.getMenu(location);
         if (menu == null) {
            return EMPTY;
         } else {
            ItemStack templateStack = menu.getItemInSlot(10);
            if (templateStack != null && templateStack.getType() != Material.AIR) {
               ItemStack itemStack = menu.getItemInSlot(16);
               int amount = itemStack != null && itemStack.getType() == templateStack.getType() ? itemStack.getAmount() : 0;
               return format(request.getPlayer(), templateStack, (long)amount, (long)templateStack.getMaxStackSize());
            } else {
               return EMPTY;
            }
         }
      });
      controller.registerCustomHandler(AdvancedGreedyBlock.class, request -> {
         Player player = request.getPlayer();
         Location location = request.getLocation();
         BlockMenu menu = StorageCacheUtils.getMenu(location);
         if (menu == null) {
            return EMPTY;
         } else {
            ItemStack templateStack = menu.getItemInSlot(10);
            if (templateStack != null && templateStack.getType() != Material.AIR) {
               int amount = 0;

               for (int i : AdvancedGreedyBlock.INPUT_SLOTS) {
                  ItemStack itemStack = menu.getItemInSlot(i);
                  if (itemStack.getType() == templateStack.getType()) {
                     amount += itemStack.getAmount();
                  }
               }

               return format(player, templateStack, (long)amount, (long)templateStack.getMaxStackSize());
            } else {
               return EMPTY;
            }
         }
      });
      controller.registerCustomHandler(
         NetworksDrawer.class,
         request -> {
            Player player = request.getPlayer();
            Location location = request.getLocation();
            StorageUnitData data = NetworksDrawer.getStorageData(location);
            if (data == null) {
               return EMPTY;
            } else if (data.getStoredItemsDirectly().isEmpty()) {
               return EMPTY;
            } else {
               double usedAmountPercent = (double)data.getTotalAmountLong()
                  / (double)(data.getSizeType().getMaxItemCount() * data.getSizeType().getEachMaxSize());
               return TextUtil.GRAY
                  + "| "
                  + TextUtil.WHITE
                  + data.getStoredTypeCount()
                  + "/"
                  + data.getSizeType().getMaxItemCount()
                  + " "
                  + TextUtil.GRAY
                  + "| "
                  + (int)(usedAmountPercent * 1000.0) / 10
                  + "%";
            }
         }
      );
      SlimefunGuideSettings.addOption(QuantumSlimeHUDDisplayOption.instance());
   }

   @NotNull
   private static String format(@NotNull Player player, @NotNull ItemStack itemStack, long amount, long limit) {
      String amountStr = HudBuilder.getAbbreviatedNumber(amount);
      String limitStr = HudBuilder.getAbbreviatedNumber(limit);
      String itemName = ItemStackHelper.getDisplayName(itemStack);
      String raw = TextUtil.GRAY + "| " + TextUtil.WHITE + itemName + " " + TextUtil.GRAY + "| ";
      return QuantumSlimeHUDDisplayOption.isEnabled(player) ? raw + amountStr + "/" + limitStr : raw + (int)(amount / limit * 1000L) / 10 + "%";
   }
}
