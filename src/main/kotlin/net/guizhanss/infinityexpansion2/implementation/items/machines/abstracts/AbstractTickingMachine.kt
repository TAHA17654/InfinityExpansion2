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
     * Runs [process] up to [multiplier] times, as if [multiplier] machines were running on the same tick.
     * The output is moved into the [OutputBuffer] after each run, so the machine is only blocked when
     * one of the output items reaches [OutputBuffer.LIMIT].
     *
     * Returns the amount of successful runs.
     */
    private fun processCompressed(b: Block, menu: BlockMenu): Int {
        val buffer = OutputBuffer.of(menu)
        buffer.absorb(menu, outputSlots)

        var runs = 0
        while (runs < multiplier) {
            if (buffer.isFull()) {
                menu.setStatus { GuiItems.NO_ROOM }
                break
            }
            if (!process(b, menu)) break
            runs++
            buffer.absorb(menu, outputSlots)
        }

        buffer.refill(menu, outputSlots)
        buffer.save()
        if (!buffer.isEmpty() && menu.hasViewer() && outputSlots.isNotEmpty()) {
            // show the buffered amount in the status slot
            menu.replaceExistingItem(layout.statusSlot, buffer.createStatusItem())
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
