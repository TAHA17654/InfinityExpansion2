package com.balugaq.netex.api.interfaces;

import com.balugaq.netex.api.enums.FeedbackType;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.NodeDefinition;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface SoftCellBannable extends FeedbackSendable {
   boolean SOFT_CELL_BAN = Networks.getConfigManager().isSoftCellBan();
   int SOFT_CELL_BAN_THRESHOLD = Networks.getConfigManager().getSoftCellBanThreshold();

   default boolean checkSoftCellBan(@NotNull BlockMenu blockMenu, NodeDefinition definition) {
      return this.checkSoftCellBan(blockMenu.getLocation(), definition);
   }

   default boolean checkSoftCellBan(@NotNull BlockMenu blockMenu, @NotNull NetworkRoot root) {
      return this.checkSoftCellBan(blockMenu.getLocation(), root);
   }

   default boolean checkSoftCellBan(@NotNull Location location, @Nullable NodeDefinition definition) {
      return definition != null && definition.getNode() != null ? this.checkSoftCellBan(location, definition.getNode().getRoot()) : true;
   }

   default boolean checkSoftCellBan(@NotNull Location location, @NotNull NetworkRoot root) {
      if (SOFT_CELL_BAN && root.getCellsSize() > SOFT_CELL_BAN_THRESHOLD) {
         this.sendFeedback(location, FeedbackType.SOFT_CELL_BANNED);
         return true;
      } else {
         return false;
      }
   }
}
