package com.balugaq.netex.api.enums;

import io.github.thebusybiscuit.slimefun4.libraries.dough.skins.PlayerHead;
import io.github.thebusybiscuit.slimefun4.libraries.dough.skins.PlayerSkin;
import javax.annotation.ParametersAreNonnullByDefault;
import lombok.Generated;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public enum Skins {
   @Deprecated
   STORAGE_UNIT_UPGRADE_TABLE_MODEL("abb84e348ab58be0435f61e027fa6dfe557bee8893f1c10c3c2bebd2025da"),
   CARGO_STORAGE_UNIT_1_MODEL("f2cda05ea5d9cca70207a8b29fde1c9696c7527c62e599e3da2d275f20e0ba9b"),
   CARGO_STORAGE_UNIT_2_MODEL("4a627255431665fe1dbbb49c82165d699f3066208495ea3569bdfe0d0b57007d"),
   CARGO_STORAGE_UNIT_3_MODEL("debf6be82e823e582ff8f45e01cf51751734d9a776cff514e88ea26d4973bbf0"),
   CARGO_STORAGE_UNIT_4_MODEL("8e4ebefefa8cb3c5860ac8412659e123ba154d4b7096c3b123c0d1fca63c9799"),
   CARGO_STORAGE_UNIT_5_MODEL("d548046dd6fa07730c3fbb4ecbe5c1a340629744806f8814653d6aa75a9032c4"),
   CARGO_STORAGE_UNIT_6_MODEL("57b8324d1b29efd667bfbd69134e7d8917110791b81baf80fdb6e2c207afe181"),
   CARGO_STORAGE_UNIT_7_MODEL("5f561b8112b64fb01d9b65ebdd3bc985572d02306b42afe89cc82b9a5c65c37d"),
   CARGO_STORAGE_UNIT_8_MODEL("696b6c77f2c1133f6df736ef2d8c52d47f58487c23e8480ef79e0d60a289f58"),
   CARGO_STORAGE_UNIT_9_MODEL("6f568dd80820657554f9b45db031d22cd9b7d161c1982773b1b35854c9939824"),
   CARGO_STORAGE_UNIT_10_MODEL("45e15d15d608a86372bb5c3d10b06e628c87ed31009ad3891c5922b3fbd86771"),
   CARGO_STORAGE_UNIT_11_MODEL("eca4130f68c71172be032b2fcb2d0ace7ffb143f375709a7f5a2bc84f6f9fbd3"),
   CARGO_STORAGE_UNIT_12_MODEL("2544b09d559cebb406b722700f3bd37cb16d28d2f6814b583c64270084bc68b3"),
   CARGO_STORAGE_UNIT_13_MODEL("e7bc251a6cb0d6d9f05c5711911a6ec24b209dbe64267901a4b03761debcf738"),
   AUTHOR_SEFIRAAT("bb2725924e09d6b0bdf5ab864e63f80eb880bfa6fe1fa17f9fdb61bc1ae110db"),
   AUTHOR_YBW0014("82a5c8de37d1a48f41053b8cc2abaec79ffbf0fc11464b290b057dd1d1d3837e"),
   AUTHOR_TINALNESS("73112785f64d814103931505ace00048c087337785550c99a67449c392b39772"),
   @Deprecated
   NE_MODEL_CELL("bdca3f370bfc0cadee7fd014579b64271649b6e7c0290d8e1acdd6f27b476bf2"),
   @Deprecated
   NE_MODEL_CONTROLLER("64aeb99a68c671a03bb7415f5801960b739a98e43b1ae4e1bacd958a8b94227d"),
   @Deprecated
   BRIDGE1("7b7c9b6a23f21cca2b362b85b36dece3d8389e363014defe5b92ff6ee64f1ae"),
   @Deprecated
   BRIDGE2("7b7c9b6a23f21cca2b362b85b36dece3d8389e363014defe5b92ff6ee64f1ae"),
   @Deprecated
   BRIDGE_CORNER_RIM("59f37f1cbd47c3504511bf33c58c3a252b60713ec5fc5433d887d4a0d996210"),
   @Deprecated
   NE_MODEL_CAPACITOR_5("ecb316f7a227a8c59d58ae0dd6768fe4fa546d55b9cfdd56cfe40b6586d81c24");

   public static final Skins[] cachedValues = values();
   @NotNull
   private final String hash;

   @ParametersAreNonnullByDefault
   private Skins(String hash) {
      this.hash = hash;
   }

   @NotNull
   public ItemStack getPlayerHead() {
      return PlayerHead.getItemStack(PlayerSkin.fromHashCode(this.hash));
   }

   @Generated
   public static Skins[] getCachedValues() {
      return cachedValues;
   }

   @NotNull
   @Generated
   public String getHash() {
      return this.hash;
   }
}
