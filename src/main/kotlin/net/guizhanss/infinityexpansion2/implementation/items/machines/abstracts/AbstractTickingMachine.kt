package net.guizhanss.infinityexpansion2.implementation.items.machines.abstracts

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.items.settings.IntRangeSetting
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent
import io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNetComponentType
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset
import net.guizhanss.guizhanlib.slimefun.machines.TickingMenuBlock
import net.guizhanss.infinityexpansion2.core.items.attributes.CustomTickRateMachine
import net.guizhanss.infinityexpansion2.core.items.attributes.EnergyTickingConsumer
import net.guizhanss.infinityexpansion2.core.menu.MenuLayout
import net.guizhanss.infinityexpansion2.implementation.items.machines.compressed.OutputBuffer
import net.guizhanss.infinityexpansion2.utils.items.GuiItems
import org.bukkit.block.Block
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack

/**
 * A ticking machine consumes energy every tick, and runs with a custom tick rate.
 */
abstract class AbstractTickingMachine(
    itemGroup: ItemGroup,
    itemStack: SlimefunItemStack,
    recipeType: RecipeType,
    recipe: Array<out ItemStack?>,
    protected val layout: MenuLayout,
    energyPerTick: Int,
    tickRate: Int = 1,
) : TickingMenuBlock(itemGroup, itemStack, recipeType, recipe), EnergyNetComponent, EnergyTickingConsumer,
    CustomTickRateMachine {

    // should avoid accessing this directly, use getCustomTickRate() instead
    protected val tickRateSetting = IntRangeSetting(this, "tick-rate", 1, tickRate, 3600)

    // should avoid accessing this directly, use getEnergyConsumptionPerTick() instead
    protected val energyPerTickSetting = IntRangeSetting(this, "energy-per-tick", 1, energyPerTick, 1_000_000_000)

    init {
        addItemSetting(tickRateSetting, energyPerTickSetting)
    }

    /**
     * How many machines this machine stands for.
     *
     * Normal machines use 1. A compressed machine (made of 64 machines) uses 64: it consumes [multiplier] times the
     * energy, runs [process] up to [multiplier] times per tick, and buffers up to [OutputBuffer.LIMIT] items of each
     * output type. It must be set before the machine is registered.
     */
    var multiplier: Int = 1
        internal set(value) {
            require(value >= 1) { "multiplier must be positive" }
            field = value
        }

    val isCompressed: Boolean get() = multiplier > 1

    override fun getCustomTickRate() = tickRateSetting.value

    override fun getEnergyConsumptionPerTick() =
        (energyPerTickSetting.value.toLong() * multiplier).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()

    override fun setup(preset: BlockMenuPreset) {
        layout.setupPreset(preset)
    }

    override fun getInputSlots() = layout.inputSlots

    override fun getOutputSlots() = layout.outputSlots

    override fun getCapacity() =
        (getEnergyConsumptionPerTick().toLong() * 2).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()

    override fun getEnergyComponentType() = EnergyNetComponentType.CONSUMER

    override fun tick(b: Block, menu: BlockMenu) {
        if (getCharge(menu.location) < getEnergyConsumptionPerTick()) {
            menu.setStatus {
                GuiItems.noPower(getEnergyConsumptionPerTick(), getCharge(menu.location), capacity)
            }
        } else if (tickCount % getCustomTickRate() == 0) {
            if (!isCompressed) {
                if (process(b, menu)) {
                    removeCharge(menu.location, getEnergyConsumptionPerTick())
                }
            } else {
                val runs = processCompressed(b, menu)
                if (runs > 0) {
                    val energy = (energyPerTickSetting.value.toLong() * runs).coerceAtMost(Int.MAX_VALUE.toLong())
                    removeCharge(menu.location, energy.toInt())
                }
            }
        }
    }

    /**
     * Whether the item in an input slot should be moved into the input buffer of a compressed machine.
     * Only the machines that consume their input should return `true` for the items they use.
     */
    protected open fun isBufferedInput(item: ItemStack) = false

    /**
     * Runs [process] up to [multiplier] times, as if [multiplier] machines were running on the same tick.
     *
     * - The output is moved into the output buffer after each run, so the machine is only blocked when
     *   one of the output items reaches [OutputBuffer.LIMIT]. At the end the output slots are filled with
     *   up to [OutputBuffer.LIMIT] items each.
     * - The input slots are emptied into the input buffer after the runs, so network pushers (which only put
     *   one normal stack into a slot) can keep pushing. Before the runs, the buffer is put back to the slots.
     *
     * Returns the amount of successful runs.
     */
    private fun processCompressed(b: Block, menu: BlockMenu): Int {
        val output = OutputBuffer.of(menu)
        val input = OutputBuffer.inputOf(menu)
        val bufferInput = inputSlots.isNotEmpty()

        output.absorb(menu, outputSlots)
        if (bufferInput) {
            input.absorb(menu, inputSlots, { isBufferedInput(it) })
            input.refill(menu, inputSlots, oversize = true)
        }

        var runs = 0
        while (runs < multiplier) {
            if (output.isFull()) {
                menu.setStatus { GuiItems.NO_ROOM }
                break
            }
            if (!process(b, menu)) break
            runs++
            output.absorb(menu, outputSlots)
        }

        if (bufferInput) {
            input.absorb(menu, inputSlots, { isBufferedInput(it) })
            input.save()
        }
        output.refill(menu, outputSlots)
        output.save()

        if ((!output.isEmpty() || !input.isEmpty()) && menu.hasViewer()) {
            // show the buffered amounts in the status slot
            menu.replaceExistingItem(layout.statusSlot, OutputBuffer.createStatusItem(menu))
        }
        return runs
    }

    override fun onBreak(e: BlockBreakEvent, menu: BlockMenu) {
        super.onBreak(e, menu)
        if (isCompressed) {
            // the items in the buffer are not in the menu, drop them
            val dropLocation = menu.location.clone().add(0.5, 0.5, 0.5)
            OutputBuffer.remove(menu).forEach { menu.location.world.dropItem(dropLocation, it) }
        }
    }

    protected abstract fun process(b: Block, menu: BlockMenu): Boolean

    protected fun BlockMenu.setStatus(itemStack: () -> ItemStack) {
        if (hasViewer()) {
            replaceExistingItem(layout.statusSlot, itemStack())
        }
    }
}
