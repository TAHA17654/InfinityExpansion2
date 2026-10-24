package net.guizhanss.infinityexpansion2.api

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu
import net.guizhanss.infinityexpansion2.implementation.items.machines.abstracts.AbstractTickingMachine
import net.guizhanss.infinityexpansion2.implementation.items.machines.compressed.OutputBuffer
import java.util.logging.Level

/**
 * API for other addons (like Networks) that want to take the items out of the output buffer of a compressed machine.
 *
 * Usage:
 * ```
 * val staged = CompressedBufferApi.stage(menu)
 * try {
 *     // take items from the output slots, as usual (a slot can hold more than a normal stack now)
 * } finally {
 *     if (staged) CompressedBufferApi.unstage(menu)
 * }
 * ```
 * Everything between [stage] and [unstage] must be quick and must happen on the same thread.
 */
object CompressedBufferApi {

    /**
     * If the machine of the [menu] is a compressed machine, moves its buffered output into the output slots
     * (each slot may hold up to 32768 items of the same type) and returns `true`.
     * In this case [unstage] must be called afterwards.
     *
     * Returns `false` (and changes nothing) if the machine is not compressed, nobody can use the buffer right now,
     * or a player is looking at the menu.
     */
    @JvmStatic
    fun stage(menu: BlockMenu): Boolean {
        val machine = StorageCacheUtils.getSfItem(menu.location) as? AbstractTickingMachine ?: return false
        if (!machine.isCompressed || menu.hasViewer()) return false

        val slots = machine.outputSlots
        if (slots.isEmpty()) return false

        val lock = OutputBuffer.lockOf(menu)
        if (!lock.tryLock()) return false

        try {
            val buffer = OutputBuffer.of(menu)
            buffer.absorb(menu, slots)
            buffer.refillOversize(menu, slots)
            return true
        } catch (e: Exception) {
            restore(menu, machine)
            lock.unlock()
            throw e
        }
    }

    /**
     * Moves everything in the output slots back into the buffer, and fills the slots with normal stacks.
     */
    @JvmStatic
    fun unstage(menu: BlockMenu) {
        val lock = OutputBuffer.lockOf(menu)
        if (!lock.isHeldByCurrentThread) return

        try {
            val machine = StorageCacheUtils.getSfItem(menu.location) as? AbstractTickingMachine ?: return
            restore(menu, machine)
        } finally {
            lock.unlock()
        }
    }

    private fun restore(menu: BlockMenu, machine: AbstractTickingMachine) {
        try {
            val buffer = OutputBuffer.of(menu)
            buffer.absorb(menu, machine.outputSlots)
            buffer.refill(menu, machine.outputSlots)
            buffer.save()
        } catch (e: Exception) {
            net.guizhanss.infinityexpansion2.InfinityExpansion2.log(Level.SEVERE, e, "Failed to restore the buffer")
        }
    }
}
