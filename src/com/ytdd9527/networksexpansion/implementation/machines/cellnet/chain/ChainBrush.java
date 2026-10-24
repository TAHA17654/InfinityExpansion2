package com.ytdd9527.networksexpansion.implementation.machines.cellnet.chain;

import com.balugaq.netex.utils.Lang;
import com.ytdd9527.networksexpansion.core.items.SpecialSlimefunItem;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.slimefun.network.NetworkDirectional;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemHandler;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import java.util.List;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent.Builder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.TextDisplay.TextAlignment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

public class ChainBrush extends SpecialSlimefunItem {
   private static final int SUMMARY_TICKS = 100;
   private static final float SUMMARY_SCALE = 0.6F;

   public ChainBrush(@NotNull ItemGroup itemGroup, @NotNull SlimefunItemStack item, @NotNull RecipeType recipeType, @NotNull ItemStack[] recipe) {
      super(itemGroup, item, recipeType, recipe);
   }

   public void preRegister() {
      this.addItemHandler(new ItemHandler[]{(ItemUseHandler)e -> {
         e.cancel();
         Player player = e.getPlayer();
         Optional<Block> optional = e.getClickedBlock();
         if (!optional.isEmpty()) {
            Block block = optional.get();
            Location location = block.getLocation();
            if (Slimefun.getProtectionManager().hasPermission(player, block, Interaction.INTERACT_BLOCK)) {
               AbstractChainMachine machine = AbstractChainMachine.machineAt(location);
               if (machine != null) {
                  ChainBindingMenu.openDirections(player, location);
               } else {
                  ChainBrush.LineHit hit = resolveLineHit(location);
                  if (hit == null) {
                     player.sendMessage(Lang.getString("messages.cellnet.line.brush-no-line"));
                  } else {
                     if (player.isSneaking()) {
                        ChainBindingMenu.clearBinding(player, hit.origin(), hit.face(), hit.distance());
                        showSummary(location, hit, List.of());
                     } else {
                        showSummary(location, hit, ChainBindingStore.load(hit.origin(), hit.face()).get(hit.distance()));
                        ChainBindingMenu.openEdit(player, hit.origin(), hit.face(), hit.distance());
                     }
                  }
               }
            }
         }
      }});
   }

   @Nullable
   private static ChainBrush.LineHit resolveLineHit(@NotNull Location target) {
      int max = ChainBindingStore.maxDistance();

      for (BlockFace face : NetworkDirectional.VALID_FACES) {
         for (int distance = 1; distance <= max; distance++) {
            Location origin = target.getBlock().getRelative(face.getOppositeFace(), distance).getLocation();
            AbstractChainMachine machine = AbstractChainMachine.machineAt(origin);
            if (machine != null) {
               AbstractChainMachine.LineState state = AbstractChainMachine.loadedState(origin);
               if (state.effectiveDirections().contains(face)
                  && distance <= machine.effectiveDistance(state)
                  && AbstractChainMachine.hasModule(origin, ChainModule.BINDING)) {
                  return new ChainBrush.LineHit(origin, face, distance);
               }
            }
         }
      }

      return null;
   }

   private static void showSummary(@NotNull Location target, @NotNull ChainBrush.LineHit hit, @Nullable List<ItemStack> list) {
      Component text = ((Builder)((Builder)((Builder)((Builder)Component.text()
                     .append(
                        ChainBindingMenu.legacy(
                           Lang.getString("messages.cellnet.line.binding-target-position", hit.distance(), ChainBindingMenu.directionName(hit.face()))
                        )
                     ))
                  .append(Component.newline()))
               .append(ChainBindingMenu.legacy(Lang.getString("messages.cellnet.line.brush-summary-label"))))
            .append(
               list != null && !list.isEmpty()
                  ? summaryItems(list)
                  : Component.newline().append(ChainBindingMenu.legacy(Lang.getString("messages.cellnet.line.brush-summary-empty")))
            ))
         .build();
      TextDisplay display = (TextDisplay)target.getWorld().spawn(target.clone().add(0.5, 1.5, 0.5), TextDisplay.class);
      display.text(text);
      display.setBillboard(Billboard.VERTICAL);
      display.setAlignment(TextAlignment.CENTER);
      display.setTransformation(new Transformation(new Vector3f(), new AxisAngle4f(), new Vector3f(0.6F, 0.6F, 0.6F), new AxisAngle4f()));
      display.setPersistent(false);
      Bukkit.getScheduler().runTaskLater(Networks.getInstance(), display::remove, 100L);
   }

   @NotNull
   private static Component summaryItems(@NotNull List<ItemStack> list) {
      Component result = Component.empty();

      for (ItemStack item : list) {
         result = result.append(Component.newline());
         result = result.append(ChainBindingMenu.legacy(Lang.getString("messages.cellnet.line.brush-summary-bullet")));
         result = result.append(ChainBindingMenu.displayComponent(item));
      }

      return result;
   }

   private static record LineHit(@NotNull Location origin, @NotNull BlockFace face, int distance) {
   }
}
