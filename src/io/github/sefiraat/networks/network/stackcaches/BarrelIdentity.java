package io.github.sefiraat.networks.network.stackcaches;

import io.github.sefiraat.networks.network.barrel.BarrelCore;
import io.github.sefiraat.networks.network.barrel.BarrelType;
import javax.annotation.ParametersAreNonnullByDefault;
import lombok.Generated;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public abstract class BarrelIdentity extends ItemStackCache implements BarrelCore {
   private Location location;
   private long amount;
   private long limit;
   private BarrelType type;

   @ParametersAreNonnullByDefault
   protected BarrelIdentity(Location location, @Nullable ItemStack itemStack, long amount, long limit, BarrelType type) {
      super(itemStack);
      this.location = location;
      this.amount = amount;
      this.limit = limit;
      this.type = type;
   }

   @Generated
   public Location getLocation() {
      return this.location;
   }

   @Generated
   public long getAmount() {
      return this.amount;
   }

   @Generated
   public long getLimit() {
      return this.limit;
   }

   @Generated
   public BarrelType getType() {
      return this.type;
   }

   @Generated
   public void setLocation(Location location) {
      this.location = location;
   }

   @Generated
   public void setAmount(long amount) {
      this.amount = amount;
   }

   @Generated
   public void setLimit(long limit) {
      this.limit = limit;
   }

   @Generated
   public void setType(BarrelType type) {
      this.type = type;
   }
}
