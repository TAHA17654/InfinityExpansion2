package io.github.sefiraat.networks.network;

import lombok.Generated;

public class NodeDefinition {
   private final NodeType type;
   private final long timeRegistered;
   private final int charge;
   private NetworkNode node;

   public NodeDefinition(NodeType type) {
      this(type, 0);
   }

   public NodeDefinition(NodeType type, int charge) {
      this.type = type;
      this.timeRegistered = System.currentTimeMillis();
      this.charge = charge;
   }

   public boolean isExpired() {
      return System.currentTimeMillis() > this.timeRegistered + 3000L;
   }

   @Generated
   public NodeType getType() {
      return this.type;
   }

   @Generated
   public int getCharge() {
      return this.charge;
   }

   @Generated
   public void setNode(NetworkNode node) {
      this.node = node;
   }

   @Generated
   public NetworkNode getNode() {
      return this.node;
   }
}
