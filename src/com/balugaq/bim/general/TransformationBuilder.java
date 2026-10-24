package com.balugaq.bim.general;

import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class TransformationBuilder {
   private Vector3f translation = new Vector3f(0.0F, 0.0F, 0.0F);
   private Quaternionf leftRotation = new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F);
   private Vector3f scale = new Vector3f(1.0F, 1.0F, 1.0F);
   private Quaternionf rightRotation = new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F);

   public static TransformationBuilder create() {
      return new TransformationBuilder();
   }

   public TransformationBuilder translation(@NotNull Vector3f translation) {
      this.translation = new Vector3f(translation);
      return this;
   }

   public TransformationBuilder translation(float x, float y, float z) {
      this.translation = new Vector3f(x, y, z);
      return this;
   }

   public TransformationBuilder leftRotation(@NotNull Quaternionf leftRotation) {
      this.leftRotation = new Quaternionf(leftRotation);
      return this;
   }

   public TransformationBuilder leftRotation(@NotNull AxisAngle4f leftRotation) {
      this.leftRotation = new Quaternionf(leftRotation);
      return this;
   }

   public TransformationBuilder leftRotation(float x, float y, float z, float w) {
      this.leftRotation = new Quaternionf(x, y, z, w);
      return this;
   }

   public TransformationBuilder scale(@NotNull Vector3f scale) {
      this.scale = new Vector3f(scale);
      return this;
   }

   public TransformationBuilder scale(float x, float y, float z) {
      this.scale = new Vector3f(x, y, z);
      return this;
   }

   public TransformationBuilder scale(float uniform) {
      this.scale = new Vector3f(uniform, uniform, uniform);
      return this;
   }

   public TransformationBuilder rightRotation(@NotNull Quaternionf rightRotation) {
      this.rightRotation = new Quaternionf(rightRotation);
      return this;
   }

   public TransformationBuilder rightRotation(@NotNull AxisAngle4f rightRotation) {
      this.rightRotation = new Quaternionf(rightRotation);
      return this;
   }

   public TransformationBuilder rightRotation(float x, float y, float z, float w) {
      this.rightRotation = new Quaternionf(x, y, z, w);
      return this;
   }

   public Transformation build() {
      return new Transformation(this.translation, this.leftRotation, this.scale, this.rightRotation);
   }

   @Override
   public String toString() {
      return "TransformationBuilder{translation="
         + this.translation
         + ", leftRotation="
         + this.leftRotation
         + ", scale="
         + this.scale
         + ", rightRotation="
         + this.rightRotation
         + "}";
   }
}
