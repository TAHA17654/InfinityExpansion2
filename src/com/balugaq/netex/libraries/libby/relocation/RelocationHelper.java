package com.balugaq.netex.libraries.libby.relocation;

import com.balugaq.netex.libraries.libby.Library;
import com.balugaq.netex.libraries.libby.LibraryManager;
import com.balugaq.netex.libraries.libby.classloader.IsolatedClassLoader;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class RelocationHelper {
   private final Constructor<?> jarRelocatorConstructor;
   private final Method jarRelocatorRunMethod;
   private final Constructor<?> relocationConstructor;

   public RelocationHelper(LibraryManager libraryManager) {
      Objects.requireNonNull(libraryManager, "libraryManager");
      IsolatedClassLoader classLoader = new IsolatedClassLoader();
      classLoader.addPath(
         libraryManager.downloadLibrary(
            Library.builder()
               .groupId("org.ow2.asm")
               .artifactId("asm-commons")
               .version("9.10.1")
               .checksum("bQq++3y/ly6hbts37BSDU3JQUGOkX5dqt+qIntlJeJU=")
               .repository("https://repo1.maven.org/maven2/")
               .build()
         )
      );
      classLoader.addPath(
         libraryManager.downloadLibrary(
            Library.builder()
               .groupId("org.ow2.asm")
               .artifactId("asm")
               .version("9.10.1")
               .checksum("7YJdEKsTmcjAy2aeaIzwyMgmKbTIOZtYNSto6SyhD8s=")
               .repository("https://repo1.maven.org/maven2/")
               .build()
         )
      );
      classLoader.addPath(
         libraryManager.downloadLibrary(
            Library.builder()
               .groupId("me.lucko")
               .artifactId("jar-relocator")
               .version("1.7")
               .checksum("b30RhOF6kHiHl+O5suNLh/+eAr1iOFEFLXhwkHHDu4I=")
               .repository("https://repo1.maven.org/maven2/")
               .build()
         )
      );

      try {
         Class<?> jarRelocatorClass = classLoader.loadClass("me.lucko.jarrelocator.JarRelocator");
         Class<?> relocationClass = classLoader.loadClass("me.lucko.jarrelocator.Relocation");
         this.jarRelocatorConstructor = jarRelocatorClass.getConstructor(File.class, File.class, Collection.class);
         this.jarRelocatorRunMethod = jarRelocatorClass.getMethod("run");
         this.relocationConstructor = relocationClass.getConstructor(String.class, String.class, Collection.class, Collection.class);
      } catch (ReflectiveOperationException var5) {
         throw new RuntimeException(var5);
      }
   }

   public void relocate(Path in, Path out, Collection<Relocation> relocations) {
      Objects.requireNonNull(in, "in");
      Objects.requireNonNull(out, "out");
      Objects.requireNonNull(relocations, "relocations");

      try {
         List<Object> rules = new LinkedList<>();

         for (Relocation relocation : relocations) {
            rules.add(
               this.relocationConstructor
                  .newInstance(relocation.getPattern(), relocation.getRelocatedPattern(), relocation.getIncludes(), relocation.getExcludes())
            );
         }

         this.jarRelocatorRunMethod.invoke(this.jarRelocatorConstructor.newInstance(in.toFile(), out.toFile(), rules));
      } catch (ReflectiveOperationException var7) {
         throw new RuntimeException(var7);
      }
   }
}
