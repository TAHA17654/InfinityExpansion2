package com.balugaq.netex.api.enums;

import dev.sefiraat.sefilib.misc.RotationFace;
import dev.sefiraat.sefilib.misc.TransformationBuilder;
import io.github.sefiraat.networks.Networks;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

public enum Transformations {
   BRIDGE_1(new TransformationBuilder().scale(0.5F, 0.5F, 2.0F).build()),
   BRIDGE_3(new TransformationBuilder().scale(-1.5F, 1.5F, 0.8F).build()),
   BRIDGE_2(new TransformationBuilder().scale(2.0F, 1.0F, 1.0F).secondRotation(RotationFace.TOP, 90.0F).build()),
   TWO(new TransformationBuilder().scale(1.9F, 2.0F, 1.9F).build()),
   NE_MODEL_CAPACITOR_5(new TransformationBuilder().scale(1.5F, -4.0F, 1.5F).build()),
   CLOCHE_BASE(new TransformationBuilder().scale(0.5F, 1.0F, 0.5F).build()),
   CLOCHE_GLASS(new TransformationBuilder().scale(0.5F, 0.5F, 0.5F).build()),
   CLOCHE_DIRT(new TransformationBuilder().scale(0.4F, 0.4F, 0.4F).build());

   @NotNull
   private final Transformation transformation;

   private Transformations(@NotNull Transformation transformation) {
      this.transformation = transformation;
   }

   @NotNull
   public Transformation getTransformation() {
      return this.getTransformation(true);
   }

   @NotNull
   public Transformation getTransformation(boolean itemDisplay) {
      return itemDisplay && Networks.getInstance().getMCVersion().isAtLeast(MinecraftVersion.V1_20)
         ? new Transformation(
            this.transformation.getTranslation(),
            this.transformation.getLeftRotation(),
            this.transformation.getScale(),
            new Quaternionf(this.transformation.getRightRotation()).rotateY((float) Math.PI)
         )
         : this.transformation;
   }
}
