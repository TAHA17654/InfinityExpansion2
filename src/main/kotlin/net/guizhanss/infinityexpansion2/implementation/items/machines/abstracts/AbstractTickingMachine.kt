@file:Suppress("deprecation")

package net.guizhanss.infinityexpansion2.implementation.items.machines.abstracts

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.items.settings.IntRangeSetting
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent
import io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNetComponentType
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockUseHandler
import me.mrCookieSlime.Slimefun.api.BlockStorage
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu
import net.guizhanss.guizhanlib.minecraft.utils.ChatUtil
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
    baseLayout: MenuLayout,
    energyPerTick: Int,
    tickRate: Int = 1,
) : TickingMenuBlock(itemGroup, itemStack, recipeType, recipe), EnergyNetComponent, EnergyTickingConsumer,
    CustomTickRateMachine {

    /**
     * The menu layout. Compressed machines get a bigger one, see [MenuLayout.compressed].
     */
    protected val layout: MenuLayout
        get() = if (isCompressed) MenuLayout.compressed(baseLayout) else baseLayout

    private val baseLayout: MenuLayout = baseLayout

    // should avoid accessing this directly, use getCustomTickRate() instead
    protected val tickRateSetting = IntRangeSetting(this, "tick-rate", 1, tickRate, 3600)

    // should avoid accessing this directly, use getEnergyConsumptionPerTick() instead
    protected val energyPerTickSetting = IntRangeSetting(this, "energy-per-tick", 1, energyPerTick, 1_000_000_000)

    init {
        addItemSetting(tickRateSetting, energyPerTickSetting)

        // compressed machines only work through their buffers, they have no gui
        addItemHandler(BlockUseHandler { e ->
            if (!isCompressed) return@BlockUseHandler
            val player = e.player
            if (player.isSneaking) return@BlockUseHandler
            e.cancel()
            val block = e.clickedBlock.orElse(null) ?: return@BlockUseHandler
            val menu = BlockStorage.getInventory(block) ?: return@BlockUseHandler
            val lines = OutputBuffer.of(menu).lines().map { "  $it" }
            val input = OutputBuffer.inputOf(menu, bufferedInputLimit).lines().map { "  $it" }
            player.sendMessage(ChatUtil.color("&bCompressed machine &7- works through its buffers, no gui"))
            player.sendMessage(ChatUtil.color("&7Output:"))
            if (lines.isEmpty()) player.sendMessage(ChatUtil.color("  &8empty")) else lines.forEach { player.sendMessage(it) }
            player.sendMessage(ChatUtil.color("&7Input:"))
            if (input.isEmpty()) player.sendMessage(ChatUtil.color("  &8empty")) else input.forEach { player.sendMessage(it) }
        })
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

    /**
     * The output slots that are used by the output buffer, for [net.guizhanss.infinityexpansion2.api.CompressedBufferApi].
     */
    fun bufferedOutputSlots(): IntArray = outputSlots

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
                // skip this tick if a network grabber is taking items out of the buffers right now
                val lock = OutputBuffer.lockOf(menu)
                if (!lock.tryLock()) return
                val runs = try {
                    processCompressed(b, menu)
                } finally {
                    lock.unlock()
                }
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
     * Whether the buffered inputs are put back into the input slots before each run.
     * Machines that read the input buffer directly (instead of the slots) should return `false`.
     */
    protected open val stagesBufferedInput: Boolean get() = true

    /**
     * The maximum amount of each item type in the input buffer of a compressed machine.
     */
    open val bufferedInputLimit: Int get() = OutputBuffer.LIMIT

    /**
     * Runs [process] up to [multiplier] times, as if [multiplier] machines were running on the same tick.
     *
     * - The output is moved into the output buffer after each run, so the machine is only blocked when
     *   one of the output items reaches [OutputBuffer.LIMIT]. At the end the output slots are filled with
     *   normal stacks from the buffer.
     * - The input slots are emptied into the input buffer after the runs, so network pushers (which only put
     *   one normal stack into a slot) can keep pushing. Before each run, the slots are topped up from the buffer.
     *
     * The slots never hold more than a normal stack.
     *
     * Returns the amount of successful runs.
     */
    private fun processCompressed(b: Block, menu: BlockMenu): Int {
        val output = OutputBuffer.of(menu)
        val input = OutputBuffer.inputOf(menu, bufferedInputLimit)
        val bufferInput = inputSlots.isNotEmpty()

        output.absorb(menu, outputSlots)
        if (bufferInput) {
            input.absorb(menu, inputSlots) { isBufferedInput(it) }
        }

        var runs = 0
        while (runs < multiplier) {
            if (output.isFull()) {
                menu.setStatus { GuiItems.NO_ROOM }
                break
            }
            if (bufferInput && stagesBufferedInput) {
                input.refill(menu, inputSlots)
            }
            if (!process(b, menu)) break
            runs++
            output.absorb(menu, outputSlots)
        }

        if (bufferInput) {
            input.absorb(menu, inputSlots) { isBufferedInput(it) }
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
