package dev.sefiraat.sefilib.entity.display;

import dev.sefiraat.sefilib.dough.data.persistent.PersistentDataAPI;
import dev.sefiraat.sefilib.persistence.StringListDataType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Interaction;

public class DisplayGroup {
   private static final NamespacedKey KEY_LIST = new NamespacedKey("sefilib", "child_display_list");
   private static final NamespacedKey KEY_NAMES = new NamespacedKey("sefilib", "child_display_names");
   @Nonnull
   private final Interaction parentDisplay;
   private final Map<String, Display> displays = new HashMap<>();

   public DisplayGroup(@Nonnull Location location) {
      this(location, 1.0F, 1.0F);
   }

   public DisplayGroup(@Nonnull Location location, float height, float width) {
      this.parentDisplay = (Interaction)location.getWorld().spawnEntity(location, EntityType.INTERACTION);
      this.parentDisplay.setInteractionHeight(height);
      this.parentDisplay.setInteractionWidth(width);
      this.applyLists(new ArrayList<>(), new ArrayList<>());
   }

   public DisplayGroup(@Nonnull Interaction textDisplay) {
      this.parentDisplay = textDisplay;
      List<String> childList = this.getChildList();
      List<String> childNames = this.getChildNames();
      if (childList != null && childNames != null) {
         if (childList.size() != childNames.size()) {
            throw new IllegalStateException("This display's memory has been borked");
         } else {
            for (int i = 0; i < childList.size(); i++) {
               String s = childList.get(i);
               UUID uuid = UUID.fromString(s);
               Entity entity = Bukkit.getEntity(uuid);
               if (entity != null && !entity.isDead() && entity instanceof Display display) {
                  this.displays.put(childNames.get(i), display);
               }
            }
         }
      } else {
         throw new IllegalArgumentException("This display was never part of a group");
      }
   }

   @Nonnull
   public Interaction getParentDisplay() {
      return this.parentDisplay;
   }

   public UUID getParentUUID() {
      return this.parentDisplay.getUniqueId();
   }

   public Location getLocation() {
      return this.parentDisplay.getLocation();
   }

   public Map<String, Display> getDisplays() {
      return Collections.unmodifiableMap(this.displays);
   }

   public void addDisplay(@Nonnull String name, @Nonnull Display display) {
      List<String> childList = this.getChildList();
      List<String> childNames = this.getChildNames();
      if (childList != null && childNames != null) {
         childList.add(display.getUniqueId().toString());
         childNames.add(name);
         this.applyLists(childList, childNames);
         this.displays.put(name, display);
      } else {
         throw new IllegalArgumentException("This display doesn't appear to have a group");
      }
   }

   @Nullable
   public Display removeDisplay(@Nonnull String name) {
      Display display = this.displays.remove(name);
      if (display == null) {
         return display;
      } else {
         List<String> childList = this.getChildList();
         List<String> childNames = this.getChildNames();
         if (childList != null && childNames != null) {
            childList.add(display.getUniqueId().toString());
            childNames.add(name);
            this.applyLists(childList, childNames);
            return display;
         } else {
            throw new IllegalArgumentException("This display doesn't appear to have a group");
         }
      }
   }

   public void killDisplay(@Nonnull String name) {
      Display display = this.removeDisplay(name);
      if (display != null) {
         display.remove();
      }
   }

   public void remove() {
      this.displays.forEach((s, display) -> display.remove());
      this.parentDisplay.remove();
   }

   public void teleport(@Nonnull Location location) {
      this.displays.forEach((s, display) -> {
         Location offset = this.getParentDisplay().getLocation().subtract(display.getLocation());
         display.teleport(location.clone().add(offset));
      });
      this.getParentDisplay().teleport(location);
   }

   @Nullable
   private List<String> getChildList() {
      return PersistentDataAPI.get(this.parentDisplay, KEY_LIST, StringListDataType.TYPE);
   }

   @Nullable
   private List<String> getChildNames() {
      return PersistentDataAPI.get(this.parentDisplay, KEY_NAMES, StringListDataType.TYPE);
   }

   private void applyLists(@Nonnull List<String> childList, @Nonnull List<String> childNames) {
      PersistentDataAPI.set(this.parentDisplay, KEY_LIST, StringListDataType.TYPE, childList);
      PersistentDataAPI.set(this.parentDisplay, KEY_NAMES, StringListDataType.TYPE, childNames);
   }

   @Nullable
   public static DisplayGroup fromUUID(@Nonnull UUID uuid) {
      Entity entity = Bukkit.getEntity(uuid);
      return entity != null && !entity.isDead() && entity instanceof Interaction display ? fromInteraction(display) : null;
   }

   @Nullable
   public static DisplayGroup fromInteraction(@Nonnull Interaction interaction) {
      return PersistentDataAPI.has(interaction, KEY_LIST, StringListDataType.TYPE) ? new DisplayGroup(interaction) : null;
   }
}
