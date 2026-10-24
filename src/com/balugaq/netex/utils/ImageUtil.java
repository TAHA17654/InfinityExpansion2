package com.balugaq.netex.utils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.imageio.ImageIO;
import lombok.Generated;
import org.jetbrains.annotations.NotNull;

public final class ImageUtil {
   public static final List<String> allowedImageExtensions = List.of("bmp", "gif", "jpeg", "jpg", "png", "tiff", "wbmp");

   public static BufferedImage getImage(@NotNull String resourcePath) {
      try {
         if (!allowedImageExtensions.contains(resourcePath.substring(resourcePath.lastIndexOf(46) + 1))) {
            throw new IOException("Invalid image extension: " + resourcePath);
         } else {
            InputStream is = ImageUtil.class.getResourceAsStream(resourcePath);
            if (is == null) {
               throw new IOException("Image not found: " + resourcePath);
            } else {
               return ImageIO.read(is);
            }
         }
      } catch (IOException var2) {
         throw var2;
      }
   }

   @Generated
   private ImageUtil() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
