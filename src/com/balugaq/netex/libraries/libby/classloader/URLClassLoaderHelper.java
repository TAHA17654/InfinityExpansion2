package com.balugaq.netex.libraries.libby.classloader;

import com.balugaq.netex.libraries.libby.Library;
import com.balugaq.netex.libraries.libby.LibraryManager;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import sun.misc.Unsafe;

public class URLClassLoaderHelper {
   private static final Unsafe theUnsafe;
   private final URLClassLoader classLoader;
   private MethodHandle addURLMethodHandle = null;

   public URLClassLoaderHelper(URLClassLoader classLoader, LibraryManager libraryManager) {
      Objects.requireNonNull(libraryManager, "libraryManager");
      this.classLoader = Objects.requireNonNull(classLoader, "classLoader");

      try {
         Method addURLMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);

         try {
            openUrlClassLoaderModule();
         } catch (Exception var7) {
         }

         try {
            addURLMethod.setAccessible(true);
         } catch (Exception var9) {
            if (!var9.getClass().getName().equals("java.lang.reflect.InaccessibleObjectException")) {
               throw new RuntimeException("Cannot set accessible URLClassLoader#addURL(URL)", var9);
            }

            if (theUnsafe != null) {
               try {
                  this.addURLMethodHandle = this.getPrivilegedMethodHandle(addURLMethod).bindTo(classLoader);
                  return;
               } catch (Exception var8) {
                  this.addURLMethodHandle = null;
               }
            }

            try {
               this.addOpensWithAgent(libraryManager);
               addURLMethod.setAccessible(true);
            } catch (Exception var6) {
               System.err
                  .println(
                     "Cannot access URLClassLoader#addURL(URL), if you are using Java 9+ try to add the following option to your java command: --add-opens java.base/java.net=ALL-UNNAMED"
                  );
               throw new RuntimeException("Cannot access URLClassLoader#addURL(URL)", var6);
            }
         }

         this.addURLMethodHandle = MethodHandles.lookup().unreflect(addURLMethod).bindTo(classLoader);
      } catch (IllegalAccessException | NoSuchMethodException var10) {
         throw new RuntimeException(var10);
      }
   }

   public void addToClasspath(URL url) {
      try {
         this.addURLMethodHandle.invokeWithArguments(Objects.requireNonNull(url, "url"));
      } catch (Throwable var3) {
         throw new RuntimeException(var3);
      }
   }

   public void addToClasspath(Path path) {
      try {
         this.addToClasspath(Objects.requireNonNull(path, "path").toUri().toURL());
      } catch (MalformedURLException var3) {
         throw new IllegalArgumentException(var3);
      }
   }

   private static void openUrlClassLoaderModule() throws Exception {
      Class<?> moduleClass = Class.forName("java.lang.Module");
      Method getModuleMethod = Class.class.getMethod("getModule");
      Method addOpensMethod = moduleClass.getMethod("addOpens", String.class, moduleClass);
      Object urlClassLoaderModule = getModuleMethod.invoke(URLClassLoader.class);
      Object thisModule = getModuleMethod.invoke(URLClassLoaderHelper.class);
      addOpensMethod.invoke(urlClassLoaderModule, URLClassLoader.class.getPackage().getName(), thisModule);
   }

   private MethodHandle getPrivilegedMethodHandle(Method method) throws Exception {
      for (Field trustedLookup : Lookup.class.getDeclaredFields()) {
         if (trustedLookup.getType() == Lookup.class && Modifier.isStatic(trustedLookup.getModifiers()) && !trustedLookup.isSynthetic()) {
            try {
               Lookup lookup = (Lookup)theUnsafe.getObject(theUnsafe.staticFieldBase(trustedLookup), theUnsafe.staticFieldOffset(trustedLookup));
               return lookup.unreflect(method);
            } catch (Exception var7) {
            }
         }
      }

      throw new RuntimeException("Cannot get privileged method handle.");
   }

   private void addOpensWithAgent(LibraryManager libraryManager) throws Exception {
      IsolatedClassLoader isolatedClassLoader = new IsolatedClassLoader();

      try {
         isolatedClassLoader.addPath(
            libraryManager.downloadLibrary(
               Library.builder()
                  .groupId("net.bytebuddy")
                  .artifactId("byte-buddy-agent")
                  .version("1.12.1")
                  .checksum("mcCtBT9cljUEniB5ESpPDYZMfVxEs1JRPllOiWTP+bM=")
                  .repository("https://repo1.maven.org/maven2/")
                  .build()
            )
         );
         Class<?> byteBuddyAgent = isolatedClassLoader.loadClass("net.bytebuddy.agent.ByteBuddyAgent");
         Object instrumentation = byteBuddyAgent.getDeclaredMethod("install").invoke(null);
         Class<?> instrumentationClass = Class.forName("java.lang.instrument.Instrumentation");
         Method redefineModule = instrumentationClass.getDeclaredMethod(
            "redefineModule", Class.forName("java.lang.Module"), Set.class, Map.class, Map.class, Set.class, Map.class
         );
         Method getModule = Class.class.getDeclaredMethod("getModule");
         Map<String, Set<?>> toOpen = Collections.singletonMap("java.net", Collections.singleton(getModule.invoke(this.getClass())));
         redefineModule.invoke(
            instrumentation,
            getModule.invoke(URLClassLoader.class),
            Collections.emptySet(),
            Collections.emptyMap(),
            toOpen,
            Collections.emptySet(),
            Collections.emptyMap()
         );
      } finally {
         try {
            isolatedClassLoader.close();
         } catch (Exception var14) {
         }
      }
   }

   static {
      Unsafe unsafe = null;

      for (Field f : Unsafe.class.getDeclaredFields()) {
         try {
            if (f.getType() == Unsafe.class && Modifier.isStatic(f.getModifiers())) {
               f.setAccessible(true);
               unsafe = (Unsafe)f.get(null);
            }
         } catch (Exception var6) {
         }
      }

      theUnsafe = unsafe;
   }
}
