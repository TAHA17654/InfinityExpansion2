package com.balugaq.netex.api.factories;

import com.balugaq.netex.api.enums.TransferType;
import com.balugaq.netex.api.transfer.TransferConfiguration;
import com.ytdd9527.networksexpansion.core.items.machines.AdvancedDirectional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TransferConfigFactory {
   public static final int ADVANCED_DEFAULT_TRANSPORT_LIMIT = 3456;
   public static final int DEFAULT_MAX_DISTANCE = 64;
   public static final String MAX_DISTANCE = "max-distance";
   public static final String PUSHITEM_TICK = "pushitem-tick";
   public static final String GRABITEM_TICK = "grabitem-tick";
   public static final String REQUIRED_POWER = "required-power";

   @NotNull
   public static TransferConfiguration getTransferConfiguration(@NotNull TransferType transferType) {
      return getTransferConfiguration(transferType, null);
   }

   @NotNull
   public static TransferConfiguration getTransferConfiguration(@NotNull TransferType transferType, @Nullable String id) {
      return new TransferConfiguration(
         transferType.config(id, "max-distance", 1),
         transferType.config(id, "pushitem-tick", 1),
         transferType.config(id, "grabitem-tick", 1),
         AdvancedDirectional.DEFAULT_TRANSPORT_MODE,
         transferType.isAdvanced() ? 3456 : 64,
         transferType.config(id, "required-power", transferType.isTransfer() ? 5000 : 0),
         transferType.config(id, "max-distance", 1),
         transferType.isAdvanced() ? 3456 : 64,
         transferType.getGui().select("B"),
         transferType.getGui().select("T"),
         transferType.getGui().select("t"),
         transferType.getGui().select1("n"),
         transferType.getGui().select1("s"),
         transferType.getGui().select1("e"),
         transferType.getGui().select1("w"),
         transferType.getGui().select1("u"),
         transferType.getGui().select1("d"),
         transferType.getGui().select1("p"),
         transferType.getGui().select1("q"),
         transferType.getGui().select1("r"),
         transferType.getGui().select1("o")
      );
   }
}
