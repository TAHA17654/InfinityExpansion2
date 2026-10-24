package com.balugaq.netex.api.enums;

import com.balugaq.netex.api.gui.GUI;
import com.balugaq.netex.utils.GUIs;
import io.github.sefiraat.networks.Networks;
import java.util.HashMap;
import java.util.Map;
import lombok.Generated;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum TransferType {
   ADVANCED_LINE_TRANSFER(GUIs.ALT),
   ADVANCED_LINE_TRANSFER_BEST_PUSHER(GUIs.ALTBP),
   ADVANCED_LINE_TRANSFER_GRABBER(GUIs.ALTG),
   ADVANCED_LINE_TRANSFER_MORE_PUSHER(GUIs.ALTMP),
   ADVANCED_LINE_TRANSFER_PUSHER(GUIs.ALTP),
   LINE_TRANSFER(GUIs.LT),
   LINE_TRANSFER_BEST_PUSHER(GUIs.LTBP),
   LINE_TRANSFER_GRABBER(GUIs.LTG),
   LINE_TRANSFER_MORE_PUSHER(GUIs.LTMP),
   LINE_TRANSFER_PUSHER(GUIs.LTP),
   ADVANCED_TRANSFER(GUIs.AT),
   ADVANCED_TRANSFER_BEST_PUSHER(GUIs.ATBP),
   ADVANCED_TRANSFER_GRABBER(GUIs.ATG),
   ADVANCED_TRANSFER_MORE_PUSHER(GUIs.ATMP),
   ADVANCED_TRANSFER_PUSHER(GUIs.ATP),
   TRANSFER(GUIs.T),
   TRANSFER_BEST_PUSHER(GUIs.TBP),
   TRANSFER_GRABBER(GUIs.TG),
   TRANSFER_MORE_PUSHER(GUIs.TMP),
   TRANSFER_PUSHER(GUIs.TP),
   LINE_TRANSFER_VANILLA_GRABBER(GUIs.LTVG),
   LINE_TRANSFER_VANILLA_PUSHER(GUIs.LTVP),
   WHITELISTED_TRANSFER_GRABBER(GUIs.WTG),
   WHITELISTED_LINE_TRANSFER_GRABBER(GUIs.WLTG),
   WHITELISTED_TRANSFER_VANILLA_GRABBER(GUIs.WTVG),
   WHITELISTED_LINE_TRANSFER_VANILLA_GRABBER(GUIs.WLTVG),
   ADVANCED_LINE_TRANSFER_VANILLA_GRABBER(GUIs.ALTVG),
   ADVANCED_LINE_TRANSFER_VANILLA_PUSHER(GUIs.ALTVP),
   CHAIN_GRABBER(GUIs.CG),
   CHAIN_PUSHER(GUIs.CP),
   CHAIN_TRANSCEIVER(GUIs.CGP);

   @NotNull
   private final GUI gui;
   private final Map<String, Integer> config = new HashMap<>();

   private TransferType(@NotNull GUI gui) {
      this.gui = gui;
   }

   public int config(@NotNull String key) {
      return this.config(key, 0);
   }

   public int config(@NotNull String key, int defaultValue) {
      if (this.config.containsKey(key)) {
         return this.config.get(key);
      } else {
         int v = Networks.getConfigManager().getInt("items.NTW_EXPANSION_" + this.name() + "." + key, defaultValue);
         this.config.put(key, v);
         return v;
      }
   }

   public int config(@Nullable String id, @NotNull String key, int defaultValue) {
      return id == null ? this.config(key, defaultValue) : this.fixedConfig(id, key, defaultValue);
   }

   public int fixedConfig(@NotNull String id, @NotNull String key, int defaultValue) {
      String fkey = id + "." + key;
      if (this.config.containsKey(fkey)) {
         return this.config.get(fkey);
      } else {
         int v = Networks.getConfigManager().getInt("items." + fkey, defaultValue);
         this.config.put(fkey, v);
         return v;
      }
   }

   public boolean isTransfer() {
      return this.name().endsWith("TRANSFER");
   }

   public boolean isGrabber() {
      return this.isTransfer() || this.name().endsWith("GRABBER");
   }

   public boolean isPusher() {
      return this.isTransfer() || this.name().endsWith("PUSHER");
   }

   public boolean isVanilla() {
      return this.name().contains("VANILLA");
   }

   public boolean isAdvanced() {
      return this.name().contains("ADVANCED");
   }

   public boolean isWhitelisted() {
      return this.name().startsWith("WHITELISTED");
   }

   @NotNull
   @Generated
   public GUI getGui() {
      return this.gui;
   }

   @Generated
   public Map<String, Integer> getConfig() {
      return this.config;
   }
}
