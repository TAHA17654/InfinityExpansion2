package com.balugaq.netex.api.keybind;

import com.balugaq.netex.api.algorithm.ID;
import com.balugaq.netex.utils.Debug;
import io.github.sefiraat.networks.Networks;
import io.github.thebusybiscuit.slimefun4.libraries.dough.config.Config;
import io.github.thebusybiscuit.slimefun4.utils.ChatUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class KeybindsScript {
   private final OfflinePlayer player;
   private final Keybinds keybinds;
   private final String keybindsName;
   private final Map<Keybind, Action> code;
   private final long id;

   public KeybindsScript(OfflinePlayer player, Keybinds keybinds, String keybindsName, Map<Keybind, Action> code, long id) {
      this.player = player;
      this.keybinds = keybinds;
      this.keybindsName = keybindsName;
      this.code = code;
      this.id = id;
   }

   public KeybindsScript(OfflinePlayer player, Keybinds keybinds, String keybindsName, Location location, long id) {
      this(player, keybinds, keybindsName, keybinds.getKeybinds(location), id);
   }

   public static KeybindsScript warp(Player player, Keybinds keybinds, String keybindsName, Location location, long id) {
      return new KeybindsScript(player, keybinds, keybindsName, location, id);
   }

   @Nullable
   public static KeybindsScript fromConfig(Config config) {
      try {
         Map<Keybind, Action> code = new HashMap<>();

         for (String key : config.getKeys("keybinds")) {
            code.put(Keybind.get(NamespacedKey.fromString(key)), Action.get(NamespacedKey.fromString(config.getString("keybinds." + key))));
         }

         return new KeybindsScript(
            Bukkit.getOfflinePlayer(config.getString("author-name")),
            Keybinds.get(NamespacedKey.fromString(config.getString("keybinds-type"))),
            config.getString("keybinds-name"),
            code,
            config.getLong("id")
         );
      } catch (Exception var4) {
         Debug.trace(var4);
         return null;
      }
   }

   public String getAuthorName() {
      return this.player.getName();
   }

   public String getKeybindsName() {
      return ChatUtils.removeColorCodes(this.keybindsName);
   }

   public String getKeybindsType() {
      return this.keybinds.getKey().toString();
   }

   public void save() {
      Config config = new Config("plugins/" + Networks.getInstance().getName() + "/keybinds/" + ID.nextId() + ".nkb");
      config.setValue("keybinds-type", this.getKeybindsType());
      config.setValue("author-name", this.getAuthorName());
      config.setValue("keybinds-name", this.getKeybindsName());

      for (Entry<Keybind, Action> entry : this.code.entrySet()) {
         config.setValue("keybinds." + entry.getKey().getKey(), entry.getValue().getKey().toString());
      }

      config.setValue("id", this.id);
      config.save();
   }

   @Generated
   public OfflinePlayer getPlayer() {
      return this.player;
   }

   @Generated
   public Keybinds getKeybinds() {
      return this.keybinds;
   }

   @Generated
   public Map<Keybind, Action> getCode() {
      return this.code;
   }

   @Generated
   public long getId() {
      return this.id;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof KeybindsScript other)) {
         return false;
      } else if (!other.canEqual(this)) {
         return false;
      } else if (this.getId() != other.getId()) {
         return false;
      } else {
         Object this$player = this.getPlayer();
         Object other$player = other.getPlayer();
         if (this$player == null ? other$player == null : this$player.equals(other$player)) {
            Object this$keybinds = this.getKeybinds();
            Object other$keybinds = other.getKeybinds();
            if (this$keybinds == null ? other$keybinds == null : this$keybinds.equals(other$keybinds)) {
               Object this$keybindsName = this.getKeybindsName();
               Object other$keybindsName = other.getKeybindsName();
               if (this$keybindsName == null ? other$keybindsName == null : this$keybindsName.equals(other$keybindsName)) {
                  Object this$code = this.getCode();
                  Object other$code = other.getCode();
                  return this$code == null ? other$code == null : this$code.equals(other$code);
               } else {
                  return false;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof KeybindsScript;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      long $id = this.getId();
      result = result * 59 + (int)($id >>> 32 ^ $id);
      Object $player = this.getPlayer();
      result = result * 59 + ($player == null ? 43 : $player.hashCode());
      Object $keybinds = this.getKeybinds();
      result = result * 59 + ($keybinds == null ? 43 : $keybinds.hashCode());
      Object $keybindsName = this.getKeybindsName();
      result = result * 59 + ($keybindsName == null ? 43 : $keybindsName.hashCode());
      Object $code = this.getCode();
      return result * 59 + ($code == null ? 43 : $code.hashCode());
   }

   @Generated
   @Override
   public String toString() {
      return "KeybindsScript(player="
         + this.getPlayer()
         + ", keybinds="
         + this.getKeybinds()
         + ", keybindsName="
         + this.getKeybindsName()
         + ", code="
         + this.getCode()
         + ", id="
         + this.getId()
         + ")";
   }
}
