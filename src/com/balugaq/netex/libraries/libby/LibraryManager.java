package com.balugaq.netex.libraries.libby;

import com.balugaq.netex.libraries.libby.classloader.IsolatedClassLoader;
import com.balugaq.netex.libraries.libby.logging.LogLevel;
import com.balugaq.netex.libraries.libby.logging.Logger;
import com.balugaq.netex.libraries.libby.logging.adapters.LogAdapter;
import com.balugaq.netex.libraries.libby.relocation.Relocation;
import com.balugaq.netex.libraries.libby.relocation.RelocationHelper;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public abstract class LibraryManager {
   protected final Logger logger;
   protected final Path saveDirectory;
   private final Set<String> repositories = new LinkedHashSet<>();
   private RelocationHelper relocator;
   private final Map<String, IsolatedClassLoader> isolatedLibraries = new HashMap<>();

   @Deprecated
   protected LibraryManager(LogAdapter logAdapter, Path dataDirectory) {
      this.logger = new Logger(Objects.requireNonNull(logAdapter, "logAdapter"));
      this.saveDirectory = Objects.requireNonNull(dataDirectory, "dataDirectory").toAbsolutePath().resolve("lib");
   }

   protected LibraryManager(LogAdapter logAdapter, Path dataDirectory, String directoryName) {
      this.logger = new Logger(Objects.requireNonNull(logAdapter, "logAdapter"));
      this.saveDirectory = Objects.requireNonNull(dataDirectory, "dataDirectory")
         .toAbsolutePath()
         .resolve(Objects.requireNonNull(directoryName, "directoryName"));
   }

   protected abstract void addToClasspath(Path file);

   protected void addToIsolatedClasspath(Library library, Path file) {
      String id = library.getId();
      IsolatedClassLoader classLoader;
      if (id != null) {
         classLoader = this.isolatedLibraries.computeIfAbsent(id, s -> new IsolatedClassLoader());
      } else {
         classLoader = new IsolatedClassLoader();
      }

      classLoader.addPath(file);
   }

   public IsolatedClassLoader getIsolatedClassLoaderOf(String libraryId) {
      return this.isolatedLibraries.get(libraryId);
   }

   public LogLevel getLogLevel() {
      return this.logger.getLevel();
   }

   public void setLogLevel(LogLevel level) {
      this.logger.setLevel(level);
   }

   public Collection<String> getRepositories() {
      List<String> urls;
      synchronized (this.repositories) {
         urls = new LinkedList<>(this.repositories);
      }

      return Collections.unmodifiableList(urls);
   }

   public void addRepository(String url) {
      String repo = Objects.requireNonNull(url, "url").endsWith("/") ? url : url + '/';
      synchronized (this.repositories) {
         this.repositories.add(repo);
      }
   }

   public void addMavenLocal() {
      this.addRepository(Paths.get(System.getProperty("user.home")).resolve(".m2/repository").toUri().toString());
   }

   public void addMavenCentral() {
      this.addRepository("https://repo1.maven.org/maven2/");
   }

   public void addSonatype() {
      this.addRepository("https://oss.sonatype.org/content/groups/public/");
   }

   public void addJCenter() {
      this.addRepository("https://jcenter.bintray.com/");
   }

   public void addJitPack() {
      this.addRepository("https://jitpack.io/");
   }

   public Collection<String> resolveLibrary(Library library) {
      Set<String> urls = new LinkedHashSet<>(Objects.requireNonNull(library, "library").getUrls());
      boolean snapshot = library.isSnapshot();

      for (String repository : library.getRepositories()) {
         if (snapshot) {
            String url = this.resolveSnapshot(repository, library);
            if (url != null) {
               urls.add(repository + url);
            }
         } else {
            urls.add(repository + library.getPath());
         }
      }

      for (String repositoryx : this.getRepositories()) {
         if (snapshot) {
            String url = this.resolveSnapshot(repositoryx, library);
            if (url != null) {
               urls.add(repositoryx + url);
            }
         } else {
            urls.add(repositoryx + library.getPath());
         }
      }

      return Collections.unmodifiableSet(urls);
   }

   private String resolveSnapshot(String repository, Library library) {
      String url = Objects.requireNonNull(repository, "repository") + Objects.requireNonNull(library, "library").getPartialPath() + "maven-metadata.xml";

      try {
         URLConnection connection = new URL(Objects.requireNonNull(url, "url")).openConnection();
         connection.setConnectTimeout(5000);
         connection.setReadTimeout(5000);
         connection.setRequestProperty("User-Agent", "libby/1.3.2");
         InputStream in = connection.getInputStream();

         String var6;
         try {
            var6 = this.getURLFromMetadata(in, library);
         } catch (Throwable var9) {
            if (in != null) {
               try {
                  in.close();
               } catch (Throwable var8) {
                  var9.addSuppressed(var8);
               }
            }

            throw var9;
         }

         if (in != null) {
            in.close();
         }

         return var6;
      } catch (MalformedURLException var10) {
         throw new IllegalArgumentException(var10);
      } catch (IOException var11) {
         if (var11 instanceof FileNotFoundException) {
            this.logger.debug("File not found: " + url);
         } else if (var11 instanceof SocketTimeoutException) {
            this.logger.debug("Connect timed out: " + url);
         } else if (var11 instanceof UnknownHostException) {
            this.logger.debug("Unknown host: " + url);
         } else {
            this.logger.debug("Unexpected IOException", var11);
         }

         return null;
      }
   }

   private String getURLFromMetadata(InputStream inputStream, Library library) throws IOException {
      Objects.requireNonNull(inputStream, "inputStream");
      Objects.requireNonNull(library, "library");

      String timestamp;
      String buildNumber;
      try {
         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
         Document doc = dBuilder.parse(inputStream);
         doc.getDocumentElement().normalize();
         NodeList nodes = doc.getElementsByTagName("snapshot");
         if (nodes.getLength() == 0) {
            return null;
         }

         Node snapshot = nodes.item(0);
         if (snapshot.getNodeType() != 1) {
            return null;
         }

         Node timestampNode = ((Element)snapshot).getElementsByTagName("timestamp").item(0);
         if (timestampNode == null || timestampNode.getNodeType() != 1) {
            return null;
         }

         Node buildNumberNode = ((Element)snapshot).getElementsByTagName("buildNumber").item(0);
         if (buildNumberNode == null || buildNumberNode.getNodeType() != 1) {
            return null;
         }

         Node timestampChild = timestampNode.getFirstChild();
         if (timestampChild == null || timestampChild.getNodeType() != 3) {
            return null;
         }

         Node buildNumberChild = buildNumberNode.getFirstChild();
         if (buildNumberChild == null || buildNumberChild.getNodeType() != 3) {
            return null;
         }

         timestamp = timestampChild.getNodeValue();
         buildNumber = buildNumberChild.getNodeValue();
      } catch (SAXException | ParserConfigurationException var14) {
         this.logger.debug("Invalid maven-metadata.xml", var14);
         return null;
      }

      String version = library.getVersion();
      if (version.endsWith("-SNAPSHOT")) {
         version = version.substring(0, version.length() - "-SNAPSHOT".length());
      }

      String url = library.getPartialPath() + library.getArtifactId() + '-' + version + '-' + timestamp + '-' + buildNumber;
      if (library.hasClassifier()) {
         url = url + '-' + library.getClassifier();
      }

      return url + ".jar";
   }

   private byte[] downloadLibrary(String url) {
      try {
         URLConnection connection = new URL(Objects.requireNonNull(url, "url")).openConnection();
         connection.setConnectTimeout(5000);
         connection.setReadTimeout(5000);
         connection.setRequestProperty("User-Agent", "libby/1.3.2");
         InputStream in = connection.getInputStream();

         byte[] e;
         label72: {
            Object var8;
            try {
               label73: {
                  byte[] buf = new byte[8192];
                  ByteArrayOutputStream out = new ByteArrayOutputStream();

                  int len;
                  try {
                     while ((len = in.read(buf)) != -1) {
                        out.write(buf, 0, len);
                     }
                  } catch (SocketTimeoutException var10) {
                     this.logger.warn("Download timed out: " + connection.getURL());
                     var8 = null;
                     break label73;
                  }

                  this.logger.info("Downloaded library " + connection.getURL());
                  e = out.toByteArray();
                  break label72;
               }
            } catch (Throwable var11) {
               if (in != null) {
                  try {
                     in.close();
                  } catch (Throwable var9) {
                     var11.addSuppressed(var9);
                  }
               }

               throw var11;
            }

            if (in != null) {
               in.close();
            }

            return (byte[])var8;
         }

         if (in != null) {
            in.close();
         }

         return e;
      } catch (MalformedURLException var12) {
         throw new IllegalArgumentException(var12);
      } catch (IOException var13) {
         if (var13 instanceof FileNotFoundException) {
            this.logger.debug("File not found: " + url);
         } else if (var13 instanceof SocketTimeoutException) {
            this.logger.debug("Connect timed out: " + url);
         } else if (var13 instanceof UnknownHostException) {
            this.logger.debug("Unknown host: " + url);
         } else {
            this.logger.debug("Unexpected IOException", var13);
         }

         return null;
      }
   }

   public Path downloadLibrary(Library library) {
      Path file = this.saveDirectory.resolve(Objects.requireNonNull(library, "library").getPath());
      label117:
      if (!Files.exists(file)) {
         Collection<String> urls = this.resolveLibrary(library);
         if (urls.isEmpty()) {
            throw new RuntimeException("Library '" + library + "' couldn't be resolved, add a repository");
         } else {
            MessageDigest md = null;
            if (library.hasChecksum()) {
               try {
                  md = MessageDigest.getInstance("SHA-256");
               } catch (NoSuchAlgorithmException var21) {
                  throw new RuntimeException(var21);
               }
            }

            Path out = file.resolveSibling(file.getFileName() + ".tmp");
            out.toFile().deleteOnExit();

            Path var25;
            try {
               Files.createDirectories(file.getParent());
               Iterator e = urls.iterator();

               byte[] bytes;
               while (true) {
                  if (!e.hasNext()) {
                     throw new RuntimeException("Failed to download library '" + library + "'");
                  }

                  String url = (String)e.next();
                  bytes = this.downloadLibrary(url);
                  if (bytes != null) {
                     if (md == null) {
                        break;
                     }

                     byte[] checksum = md.digest(bytes);
                     if (Arrays.equals(checksum, library.getChecksum())) {
                        break;
                     }

                     this.logger.warn("*** INVALID CHECKSUM ***");
                     this.logger.warn(" Library :  " + library);
                     this.logger.warn(" URL :  " + url);
                     this.logger.warn(" Expected :  " + Base64.getEncoder().encodeToString(library.getChecksum()));
                     this.logger.warn(" Actual :  " + Base64.getEncoder().encodeToString(checksum));
                  }
               }

               Files.write(out, bytes);
               Files.move(out, file);
               var25 = file;
            } catch (IOException var23) {
               throw new UncheckedIOException(var23);
            } finally {
               try {
                  Files.deleteIfExists(out);
               } catch (IOException var20) {
               }
            }

            return var25;
         }
      } else if (!library.isSnapshot()) {
         return file;
      } else {
         try {
            Files.delete(file);
            break label117;
         } catch (IOException var22) {
            throw new UncheckedIOException(var22);
         }
      }
   }

   private Path relocate(Path in, String out, Collection<Relocation> relocations) {
      Objects.requireNonNull(in, "in");
      Objects.requireNonNull(out, "out");
      Objects.requireNonNull(relocations, "relocations");
      Path file = this.saveDirectory.resolve(out);
      if (Files.exists(file)) {
         return file;
      } else {
         Path tmpOut = file.resolveSibling(file.getFileName() + ".tmp");
         tmpOut.toFile().deleteOnExit();
         synchronized (this) {
            if (this.relocator == null) {
               this.relocator = new RelocationHelper(this);
            }
         }

         Path e;
         try {
            this.relocator.relocate(in, tmpOut, relocations);
            Files.move(tmpOut, file);
            this.logger.info("Relocations applied to " + this.saveDirectory.getParent().relativize(in));
            e = file;
         } catch (IOException var16) {
            throw new UncheckedIOException(var16);
         } finally {
            try {
               Files.deleteIfExists(tmpOut);
            } catch (IOException var15) {
            }
         }

         return e;
      }
   }

   public void loadLibrary(Library library) {
      Path file = this.downloadLibrary(Objects.requireNonNull(library, "library"));
      if (library.hasRelocations()) {
         file = this.relocate(file, library.getRelocatedPath(), library.getRelocations());
      }

      if (library.isIsolatedLoad()) {
         this.addToIsolatedClasspath(library, file);
      } else {
         this.addToClasspath(file);
      }
   }
}
