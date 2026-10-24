package dev.sefiraat.sefilib.misc;

public enum RotationFace {
   FRONT(0.0F, 0.0F, 1.0F),
   SIDE(1.0F, 0.0F, 0.0F),
   TOP(0.0F, 1.0F, 0.0F);

   private final float x;
   private final float y;
   private final float z;

   private RotationFace(float x, float y, float z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public float getX() {
      return this.x;
   }

   public float getY() {
      return this.y;
   }

   public float getZ() {
      return this.z;
   }
}
