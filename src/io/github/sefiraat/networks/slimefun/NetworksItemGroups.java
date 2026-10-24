package io.github.sefiraat.networks.slimefun;

import com.balugaq.netex.utils.Lang;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.slimefun.groups.DummyItemGroup;
import io.github.sefiraat.networks.slimefun.groups.MainFlexGroup;
import io.github.sefiraat.networks.utils.Keys;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import lombok.Generated;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class NetworksItemGroups {
   public static final MainFlexGroup MAIN = new MainFlexGroup(
      Keys.newKey("main"), new CustomItemStack(NetworksSlimefunItemStacks.NETWORK_CONTROLLER, Lang.getString("groups.main"), new String[0]), 0
   );
   public static final DummyItemGroup MATERIALS = new DummyItemGroup(
      Keys.newKey("materials"), new CustomItemStack(new ItemStack(Material.WHITE_STAINED_GLASS), Lang.getString("groups.materials"), new String[0]), 0
   );
   public static final DummyItemGroup TOOLS = new DummyItemGroup(
      Keys.newKey("tools"), new CustomItemStack(new ItemStack(Material.PAINTING), Lang.getString("groups.tools"), new String[0]), 0
   );
   public static final DummyItemGroup NETWORK_ITEMS = new DummyItemGroup(
      Keys.newKey("network_items"), new CustomItemStack(new ItemStack(Material.BLACK_STAINED_GLASS), Lang.getString("groups.network_items"), new String[0]), 0
   );
   public static final DummyItemGroup NETWORK_QUANTUMS = new DummyItemGroup(
      Keys.newKey("network_quantums"),
      new CustomItemStack(new ItemStack(Material.WHITE_TERRACOTTA), Lang.getString("groups.network_quantums"), new String[0]),
      0
   );
   public static final ItemGroup DISABLED_ITEMS = new NetworksItemGroups.HiddenItemGroup(
      Keys.newKey("disabled_items"), new CustomItemStack(new ItemStack(Material.BARRIER), Lang.getString("groups.disabled_items"), new String[0])
   );

   @Generated
   private NetworksItemGroups() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   static {
      Networks plugin = Networks.getInstance();
      MAIN.register(plugin);
      MATERIALS.register(plugin);
      TOOLS.register(plugin);
      NETWORK_ITEMS.register(plugin);
      NETWORK_QUANTUMS.register(plugin);
      DISABLED_ITEMS.register(plugin);
   }

   public static class HiddenItemGroup extends ItemGroup {
      public HiddenItemGroup(@NotNull NamespacedKey key, @NotNull ItemStack item) {
         super(key, item);
      }

      public boolean isHidden(@NotNull Player p) {
         return true;
      }
   }
}
