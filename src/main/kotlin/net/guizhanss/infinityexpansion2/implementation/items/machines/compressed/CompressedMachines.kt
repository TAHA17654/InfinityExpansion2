package net.guizhanss.infinityexpansion2.implementation.items.machines.compressed

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import net.guizhanss.guizhanlib.kt.slimefun.items.builder.ItemRegistry
import net.guizhanss.guizhanlib.kt.slimefun.items.edit
import net.guizhanss.guizhanlib.kt.slimefun.items.toItem
import net.guizhanss.infinityexpansion2.InfinityExpansion2
import net.guizhanss.infinityexpansion2.core.IERegistry
import net.guizhanss.infinityexpansion2.implementation.guide.IEItemGroups
import net.guizhanss.infinityexpansion2.implementation.items.machines.GrowingMachine
import net.guizhanss.infinityexpansion2.implementation.items.machines.Quarry
import net.guizhanss.infinityexpansion2.implementation.items.machines.VoidHarvester
import net.guizhanss.infinityexpansion2.implementation.items.machines.abstracts.AbstractTickingMachine
import net.guizhanss.infinityexpansion2.implementation.items.machines.abstracts.HopperMachine
import net.guizhanss.infinityexpansion2.implementation.items.machines.abstracts.MaterialGenerator
import net.guizhanss.infinityexpansion2.implementation.recipes.IERecipeTypes
import net.guizhanss.infinityexpansion2.utils.items.builder.createInstance
import org.bukkit.inventory.ItemStack
import java.util.logging.Level

/**
 * Creates the compressed version of every compressible machine.
 *
 * A compressed machine stands for [FACTOR] normal machines:
 * - it is crafted in the [MachineCompressor] with [FACTOR] machines
 * - it can be decompressed in the Grind Stone, which gives the [FACTOR] machines back
 * - it consumes [FACTOR] times the energy, and produces [FACTOR] times the output
 * - it stacks up to [OutputBuffer.LIMIT] items of each output
 */
object CompressedMachines {

    /**
     * The amount of machines that are compressed into one.
     */
    const val FACTOR = 64

    /**
     * Whether the machine should have a compressed version.
     * Only machines that run a recipe or produce an output on their own can be compressed.
     */
    fun isCompressible(item: SlimefunItem) = item is AbstractTickingMachine &&
        item !is MachineCompressor &&
        (item is MaterialGenerator || item is Quarry || item is VoidHarvester ||
            item is GrowingMachine || item is HopperMachine)

    /**
     * Creates and registers all the compressed machines. This must be called after all the machines are registered.
     */
    internal fun registerAll(registry: ItemRegistry) {
        val localization = InfinityExpansion2.localization

        for (spec in IERegistry.compressibleMachines.toList()) {
            val baseName = localization.getItemName(spec.id)
            val lore = localization.getItemLore(spec.id) + listOf(
                "",
                localization.getLore("compressed.info", FACTOR),
                localization.getLore("compressed.energy", FACTOR),
                localization.getLore("compressed.buffer", OutputBuffer.LIMIT),
            )

            val stack = SlimefunItemStack(
                "${registry.prefix}COMPRESSED_${spec.id}",
                spec.material.convert(),
                localization.getLore("compressed.name", baseName),
                *lore.toTypedArray()
            )

            // the recipe is only used for displaying, the actual crafting is done in the machine compressor
            val input = spec.baseStack.edit { amount(FACTOR) }
            val recipe = arrayOf<ItemStack?>(input, null, null, null, null, null, null, null, null)

            val args = arrayOf(IEItemGroups.COMPRESSED, stack, IERecipeTypes.MACHINE_COMPRESSOR, recipe, *spec.args.toTypedArray())
            val item = createInstance(spec.clazz, args)
            if (item !is AbstractTickingMachine) {
                InfinityExpansion2.log(Level.WARNING, "${spec.id} cannot be compressed")
                continue
            }

            // must be set before registering, because the energy capacity is determined when registering
            item.multiplier = FACTOR
            item.register(InfinityExpansion2.instance)

            IERegistry.compressedMachines.add(spec.baseStack to stack)
            registerDecompressRecipe(stack, spec.baseStack)
        }
    }

    /**
     * Compressed machines are decompressed with the Grind Stone.
     */
    private fun registerDecompressRecipe(compressed: SlimefunItemStack, base: SlimefunItemStack) {
        val input = compressed.toItem()
        val output = base.edit { amount(FACTOR) }
        RecipeType.GRIND_STONE.register(arrayOf(input), output)
    }
}
