package net.guizhanss.infinityexpansion2.implementation.items.machines.compressed

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import net.guizhanss.infinityexpansion2.core.IERegistry
import net.guizhanss.infinityexpansion2.implementation.items.machines.abstracts.HopperMachine
import org.bukkit.inventory.ItemStack

/**
 * Compresses [CompressedMachines.FACTOR] machines into one compressed machine.
 *
 * The recipes are added from [IERegistry.compressedMachines], so this must be created after
 * [CompressedMachines.registerAll] has been called.
 *
 * To get the machines back, put the compressed machine into a Grind Stone.
 */
class MachineCompressor(
    itemGroup: ItemGroup,
    itemStack: SlimefunItemStack,
    recipeType: RecipeType,
    recipe: Array<out ItemStack?>,
    energyPerTick: Int,
) : HopperMachine(itemGroup, itemStack, recipeType, recipe, energyPerTick) {

    init {
        for ((base, compressed) in IERegistry.compressedMachines) {
            val input = ItemStack(base).apply { amount = CompressedMachines.FACTOR }
            addRecipe(input, ItemStack(compressed))
        }
    }
}
