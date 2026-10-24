package com.balugaq.netex.libraries.libby;

import com.balugaq.netex.libraries.libby.classloader.URLClassLoaderHelper;
import com.balugaq.netex.libraries.libby.logging.adapters.JDKLogAdapter;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Objects;
import org.bukkit.plugin.Plugin;

public class BukkitLibraryManager extends LibraryManager {
   private final URLClassLoaderHelper classLoader;

   public BukkitLibraryManager(Plugin plugin) {
      this(plugin, "lib");
   }

   public BukkitLibraryManager(Plugin plugin, String directoryName) {
      super(new JDKLogAdapter(Objects.requireNonNull(plugin, "plugin").getLogger()), plugin.getDataFolder().toPath(), directoryName);
      this.classLoader = new URLClassLoaderHelper((URLClassLoader)plugin.getClass().getClassLoader(), this);
   }

   @Override
   protected void addToClasspath(Path file) {
      this.classLoader.addToClasspath(file);
   }
}
