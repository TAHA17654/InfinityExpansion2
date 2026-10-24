package com.balugaq.netex.api.transfer;

import com.balugaq.netex.api.enums.TransportMode;
import com.balugaq.netex.api.interfaces.GrabTickOnly;
import com.balugaq.netex.api.interfaces.PushTickOnly;
import com.balugaq.netex.utils.Lang;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Generated;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public class TransferConfiguration {
   public final int defaultDistance;
   public final int defaultPushTick;
   public final int defaultGrabTick;
   @NotNull
   public final TransportMode defaultTransportMode;
   public final int defaultTransportLimit;
   public final int defaultRequiredPower;
   public final int maxDistance;
   public final int maxTransportLimit;
   @NotNull
   public final int[] backgroundSlots;
   @NotNull
   public final int[] templateBackgroundSlots;
   @NotNull
   public final int[] templateSlots;
   public final int northSlot;
   public final int southSlot;
   public final int eastSlot;
   public final int westSlot;
   public final int upSlot;
   public final int downSlot;
   public final int minusSlot;
   public final int cargoNumberSlot;
   public final int addSlot;
   public final int transportModeSlot;

   @Range(
      from = 0L,
      to = 2147483647L
   )
   public int dd() {
      return this.defaultDistance;
   }

   @Range(
      from = 0L,
      to = 2147483647L
   )
   public int dpt() {
      return this.defaultPushTick;
   }

   @Range(
      from = 0L,
      to = 2147483647L
   )
   public int dgt() {
      return this.defaultGrabTick;
   }

   @NotNull
   public TransportMode dtm() {
      return this.defaultTransportMode;
   }

   @Range(
      from = 0L,
      to = 2147483647L
   )
   public int dtl() {
      return this.defaultTransportLimit;
   }

   @Range(
      from = 0L,
      to = 2147483647L
   )
   public int drp() {
      return this.defaultRequiredPower;
   }

   @Range(
      from = 0L,
      to = 2147483647L
   )
   public int md() {
      return this.maxDistance;
   }

   @Range(
      from = 0L,
      to = 2147483647L
   )
   public int mtl() {
      return this.maxTransportLimit;
   }

   @NotNull
   public int[] bs() {
      return this.backgroundSlots;
   }

   @NotNull
   public int[] tbs() {
      return this.templateBackgroundSlots;
   }

   @NotNull
   public int[] ts() {
      return this.templateSlots;
   }

   @Range(
      from = 0L,
      to = 53L
   )
   public int ns() {
      return this.northSlot;
   }

   @Range(
      from = 0L,
      to = 53L
   )
   public int ss() {
      return this.southSlot;
   }

   @Range(
      from = 0L,
      to = 53L
   )
   public int es() {
      return this.eastSlot;
   }

   @Range(
      from = 0L,
      to = 53L
   )
   public int ws() {
      return this.westSlot;
   }

   @Range(
      from = 0L,
      to = 53L
   )
   public int us() {
      return this.upSlot;
   }

   @Range(
      from = 0L,
      to = 53L
   )
   public int ds() {
      return this.downSlot;
   }

   @Range(
      from = 0L,
      to = 53L
   )
   public int ms() {
      return this.minusSlot;
   }

   @Range(
      from = 0L,
      to = 53L
   )
   public int cns() {
      return this.cargoNumberSlot;
   }

   @Range(
      from = 0L,
      to = 53L
   )
   public int as() {
      return this.addSlot;
   }

   @Range(
      from = 0L,
      to = 53L
   )
   public int tms() {
      return this.transportModeSlot;
   }

   @NotNull
   public final ItemStack getInformationIcon() {
      List<String> lore = new ArrayList<>();
      lore.add("");
      if (this.maxDistance != 1) {
         lore.add(String.format(Lang.getString("icons.mechanism.transfers.max_distance"), this.maxDistance));
      }

      if (!(this instanceof PushTickOnly)) {
         lore.add(String.format(Lang.getString("icons.mechanism.transfers.push_item_tick"), this.defaultPushTick));
      }

      if (!(this instanceof GrabTickOnly)) {
         lore.add(String.format(Lang.getString("icons.mechanism.transfers.grab_item_tick"), this.defaultGrabTick));
      }

      if (this.defaultRequiredPower != 0) {
         lore.add(String.format(Lang.getString("icons.mechanism.transfers.required_power"), this.defaultRequiredPower));
      }

      return new CustomItemStack(Material.BOOK, Lang.getString("icons.mechanism.transfers.data_title"), lore);
   }

   @Generated
   public TransferConfiguration(
      int defaultDistance,
      int defaultPushTick,
      int defaultGrabTick,
      @NotNull TransportMode defaultTransportMode,
      int defaultTransportLimit,
      int defaultRequiredPower,
      int maxDistance,
      int maxTransportLimit,
      @NotNull int[] backgroundSlots,
      @NotNull int[] templateBackgroundSlots,
      @NotNull int[] templateSlots,
      int northSlot,
      int southSlot,
      int eastSlot,
      int westSlot,
      int upSlot,
      int downSlot,
      int minusSlot,
      int cargoNumberSlot,
      int addSlot,
      int transportModeSlot
   ) {
      if (defaultTransportMode == null) {
         throw new NullPointerException("defaultTransportMode is marked non-null but is null");
      } else {
         this.defaultDistance = defaultDistance;
         this.defaultPushTick = defaultPushTick;
         this.defaultGrabTick = defaultGrabTick;
         this.defaultTransportMode = defaultTransportMode;
         this.defaultTransportLimit = defaultTransportLimit;
         this.defaultRequiredPower = defaultRequiredPower;
         this.maxDistance = maxDistance;
         this.maxTransportLimit = maxTransportLimit;
         this.backgroundSlots = backgroundSlots;
         this.templateBackgroundSlots = templateBackgroundSlots;
         this.templateSlots = templateSlots;
         this.northSlot = northSlot;
         this.southSlot = southSlot;
         this.eastSlot = eastSlot;
         this.westSlot = westSlot;
         this.upSlot = upSlot;
         this.downSlot = downSlot;
         this.minusSlot = minusSlot;
         this.cargoNumberSlot = cargoNumberSlot;
         this.addSlot = addSlot;
         this.transportModeSlot = transportModeSlot;
      }
   }

   @Generated
   public int getDefaultDistance() {
      return this.defaultDistance;
   }

   @Generated
   public int getDefaultPushTick() {
      return this.defaultPushTick;
   }

   @Generated
   public int getDefaultGrabTick() {
      return this.defaultGrabTick;
   }

   @NotNull
   @Generated
   public TransportMode getDefaultTransportMode() {
      return this.defaultTransportMode;
   }

   @Generated
   public int getDefaultTransportLimit() {
      return this.defaultTransportLimit;
   }

   @Generated
   public int getDefaultRequiredPower() {
      return this.defaultRequiredPower;
   }

   @Generated
   public int getMaxDistance() {
      return this.maxDistance;
   }

   @Generated
   public int getMaxTransportLimit() {
      return this.maxTransportLimit;
   }

   @Generated
   @NotNull
   public int[] getBackgroundSlots() {
      return this.backgroundSlots;
   }

   @Generated
   @NotNull
   public int[] getTemplateBackgroundSlots() {
      return this.templateBackgroundSlots;
   }

   @Generated
   @NotNull
   public int[] getTemplateSlots() {
      return this.templateSlots;
   }

   @Generated
   public int getNorthSlot() {
      return this.northSlot;
   }

   @Generated
   public int getSouthSlot() {
      return this.southSlot;
   }

   @Generated
   public int getEastSlot() {
      return this.eastSlot;
   }

   @Generated
   public int getWestSlot() {
      return this.westSlot;
   }

   @Generated
   public int getUpSlot() {
      return this.upSlot;
   }

   @Generated
   public int getDownSlot() {
      return this.downSlot;
   }

   @Generated
   public int getMinusSlot() {
      return this.minusSlot;
   }

   @Generated
   public int getCargoNumberSlot() {
      return this.cargoNumberSlot;
   }

   @Generated
   public int getAddSlot() {
      return this.addSlot;
   }

   @Generated
   public int getTransportModeSlot() {
      return this.transportModeSlot;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof TransferConfiguration other)) {
         return false;
      } else if (!other.canEqual(this)) {
         return false;
      } else if (this.getDefaultDistance() != other.getDefaultDistance()) {
         return false;
      } else if (this.getDefaultPushTick() != other.getDefaultPushTick()) {
         return false;
      } else if (this.getDefaultGrabTick() != other.getDefaultGrabTick()) {
         return false;
      } else if (this.getDefaultTransportLimit() != other.getDefaultTransportLimit()) {
         return false;
      } else if (this.getDefaultRequiredPower() != other.getDefaultRequiredPower()) {
         return false;
      } else if (this.getMaxDistance() != other.getMaxDistance()) {
         return false;
      } else if (this.getMaxTransportLimit() != other.getMaxTransportLimit()) {
         return false;
      } else if (this.getNorthSlot() != other.getNorthSlot()) {
         return false;
      } else if (this.getSouthSlot() != other.getSouthSlot()) {
         return false;
      } else if (this.getEastSlot() != other.getEastSlot()) {
         return false;
      } else if (this.getWestSlot() != other.getWestSlot()) {
         return false;
      } else if (this.getUpSlot() != other.getUpSlot()) {
         return false;
      } else if (this.getDownSlot() != other.getDownSlot()) {
         return false;
      } else if (this.getMinusSlot() != other.getMinusSlot()) {
         return false;
      } else if (this.getCargoNumberSlot() != other.getCargoNumberSlot()) {
         return false;
      } else if (this.getAddSlot() != other.getAddSlot()) {
         return false;
      } else if (this.getTransportModeSlot() != other.getTransportModeSlot()) {
         return false;
      } else {
         Object this$defaultTransportMode = this.getDefaultTransportMode();
         Object other$defaultTransportMode = other.getDefaultTransportMode();
         if (this$defaultTransportMode == null ? other$defaultTransportMode == null : this$defaultTransportMode.equals(other$defaultTransportMode)) {
            if (!Arrays.equals(this.getBackgroundSlots(), other.getBackgroundSlots())) {
               return false;
            } else {
               return !Arrays.equals(this.getTemplateBackgroundSlots(), other.getTemplateBackgroundSlots())
                  ? false
                  : Arrays.equals(this.getTemplateSlots(), other.getTemplateSlots());
            }
         } else {
            return false;
         }
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof TransferConfiguration;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      result = result * 59 + this.getDefaultDistance();
      result = result * 59 + this.getDefaultPushTick();
      result = result * 59 + this.getDefaultGrabTick();
      result = result * 59 + this.getDefaultTransportLimit();
      result = result * 59 + this.getDefaultRequiredPower();
      result = result * 59 + this.getMaxDistance();
      result = result * 59 + this.getMaxTransportLimit();
      result = result * 59 + this.getNorthSlot();
      result = result * 59 + this.getSouthSlot();
      result = result * 59 + this.getEastSlot();
      result = result * 59 + this.getWestSlot();
      result = result * 59 + this.getUpSlot();
      result = result * 59 + this.getDownSlot();
      result = result * 59 + this.getMinusSlot();
      result = result * 59 + this.getCargoNumberSlot();
      result = result * 59 + this.getAddSlot();
      result = result * 59 + this.getTransportModeSlot();
      Object $defaultTransportMode = this.getDefaultTransportMode();
      result = result * 59 + ($defaultTransportMode == null ? 43 : $defaultTransportMode.hashCode());
      result = result * 59 + Arrays.hashCode(this.getBackgroundSlots());
      result = result * 59 + Arrays.hashCode(this.getTemplateBackgroundSlots());
      return result * 59 + Arrays.hashCode(this.getTemplateSlots());
   }

   @Generated
   @Override
   public String toString() {
      return "TransferConfiguration(defaultDistance="
         + this.getDefaultDistance()
         + ", defaultPushTick="
         + this.getDefaultPushTick()
         + ", defaultGrabTick="
         + this.getDefaultGrabTick()
         + ", defaultTransportMode="
         + this.getDefaultTransportMode()
         + ", defaultTransportLimit="
         + this.getDefaultTransportLimit()
         + ", defaultRequiredPower="
         + this.getDefaultRequiredPower()
         + ", maxDistance="
         + this.getMaxDistance()
         + ", maxTransportLimit="
         + this.getMaxTransportLimit()
         + ", backgroundSlots="
         + Arrays.toString(this.getBackgroundSlots())
         + ", templateBackgroundSlots="
         + Arrays.toString(this.getTemplateBackgroundSlots())
         + ", templateSlots="
         + Arrays.toString(this.getTemplateSlots())
         + ", northSlot="
         + this.getNorthSlot()
         + ", southSlot="
         + this.getSouthSlot()
         + ", eastSlot="
         + this.getEastSlot()
         + ", westSlot="
         + this.getWestSlot()
         + ", upSlot="
         + this.getUpSlot()
         + ", downSlot="
         + this.getDownSlot()
         + ", minusSlot="
         + this.getMinusSlot()
         + ", cargoNumberSlot="
         + this.getCargoNumberSlot()
         + ", addSlot="
         + this.getAddSlot()
         + ", transportModeSlot="
         + this.getTransportModeSlot()
         + ")";
   }
}
