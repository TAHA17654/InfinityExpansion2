package com.balugaq.bim.grid;

import lombok.Generated;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.jspecify.annotations.NullMarked;

@NullMarked
public enum GridOrientation {
   XY(new Vector(1, 0, 0), new Vector(0, 1, 0), new Vector(0, 0, 1), 0.0F, 0.0F, new Quaternionf().rotationY((float) Math.PI)),
   XY_REVERSED(new Vector(-1, 0, 0), new Vector(0, 1, 0), new Vector(0, 0, -1), 180.0F, 0.0F, new Quaternionf()),
   XZ(
      new Vector(1, 0, 0),
      new Vector(0, 0, -1),
      new Vector(0, 1, 0),
      0.0F,
      -90.0F,
      new Quaternionf().rotationX((float) (-Math.PI / 2)).rotateY((float) Math.PI)
   ),
   XZ_REVERSED(
      new Vector(1, 0, 0), new Vector(0, 0, 1), new Vector(0, -1, 0), 0.0F, 90.0F, new Quaternionf().rotationX((float) (Math.PI / 2)).rotateY((float) Math.PI)
   ),
   YZ(new Vector(0, 0, -1), new Vector(0, 1, 0), new Vector(1, 0, 0), 270.0F, 0.0F, new Quaternionf().rotationY((float) (Math.PI / 2)).rotateY((float) Math.PI)),
   YZ_REVERSED(
      new Vector(0, 0, 1), new Vector(0, 1, 0), new Vector(-1, 0, 0), 90.0F, 0.0F, new Quaternionf().rotationY((float) (-Math.PI / 2)).rotateY((float) Math.PI)
   );

   final Vector widthDir;
   final Vector upDir;
   final Vector normal;
   final float yaw;
   final float pitch;
   final Quaternionf itemRotation;

   private GridOrientation(Vector widthDir, Vector upDir, Vector normal, float yaw, float pitch, Quaternionf itemRotation) {
      this.widthDir = widthDir;
      this.upDir = upDir;
      this.normal = normal;
      this.yaw = yaw;
      this.pitch = pitch;
      this.itemRotation = itemRotation;
   }

   public Location apply(Location base, double a, double b, double c) {
      return base.clone().add(this.widthDir.clone().multiply(a)).add(this.upDir.clone().multiply(b)).add(this.normal.clone().multiply(c));
   }

   public boolean isHorizontal() {
      return this == XZ || this == XZ_REVERSED;
   }

   public GridOrientation reverse() {
      return switch (this) {
         case XY -> XY_REVERSED;
         case XY_REVERSED -> XY;
         case XZ -> XZ_REVERSED;
         case XZ_REVERSED -> XZ;
         case YZ -> YZ_REVERSED;
         case YZ_REVERSED -> YZ;
      };
   }

   public static GridOrientation fromYawPitch(float yaw, float pitch) {
      float y = normalizeYaw(yaw);
      float p = normalizePitch(pitch);
      GridOrientation best = XY;
      float bestScore = Float.MAX_VALUE;

      for (GridOrientation o : values()) {
         float dPitch = Math.abs(p - o.pitch);
         float dYaw = yawDistance(y, o.yaw);
         float score = dPitch * 4.0F + dYaw;
         if (score < bestScore) {
            bestScore = score;
            best = o;
         }
      }

      return best;
   }

   private static float normalizeYaw(float yaw) {
      float y = yaw % 360.0F;
      if (y < 0.0F) {
         y += 360.0F;
      }

      return y;
   }

   private static float normalizePitch(float pitch) {
      float p = pitch % 360.0F;
      if (p < -180.0F) {
         p += 360.0F;
      }

      if (p > 180.0F) {
         p -= 360.0F;
      }

      if (p > 90.0F) {
         p = 180.0F - p;
      }

      if (p < -90.0F) {
         p = -180.0F - p;
      }

      return p;
   }

   private static float yawDistance(float a, float b) {
      float d = Math.abs(a - b) % 360.0F;
      return d > 180.0F ? 360.0F - d : d;
   }

   @Generated
   public Vector getWidthDir() {
      return this.widthDir;
   }

   @Generated
   public Vector getUpDir() {
      return this.upDir;
   }

   @Generated
   public Vector getNormal() {
      return this.normal;
   }

   @Generated
   public float getYaw() {
      return this.yaw;
   }

   @Generated
   public float getPitch() {
      return this.pitch;
   }

   @Generated
   public Quaternionf getItemRotation() {
      return this.itemRotation;
   }
}
