@file:Suppress("deprecation")

package net.guizhanss.infinityexpansion2.implementation.items.machines.compressed

import io.github.thebusybiscuit.slimefun4.libraries.dough.blocks.BlockPosition
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.ItemUtils
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu
import net.guizhanss.guizhanlib.kt.minecraft.extensions.isAir
import net.guizhanss.guizhanlib.kt.slimefun.extensions.position
import net.guizhanss.guizhanlib.kt.slimefun.utils.getString
import net.guizhanss.guizhanlib.kt.slimefun.utils.setString
import net.guizhanss.guizhanlib.minecraft.utils.ChatUtil
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.text.NumberFormat
import java.util.Base64
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/**
 * A virtual output buffer used by compressed machines.
 *
 * An [ItemStack] can never hold more than 99 items, so the "4,096 items in one stack" of compressed machines is
 * implemented by keeping the overflow in this buffer (one entry per item type, each up to [LIMIT]) and only showing
 * normal stacks in the output slots. The output slots are refilled from the buffer every tick, so hoppers, cargo
 * and players still interact with ordinary item stacks.
 */
class OutputBuffer private constructor(private val location: Location) {

    private class Entry(val template: ItemStack, val serialized: String, var amount: Int)

    private val entries = mutableListOf<Entry>()
    private var dirty = false

    init {
        load()
    }

    @Synchronized
    fun isEmpty() = entries.isEmpty()

    /**
     * Whether at least one item type in the buffer has reached [LIMIT].
     */
    @Synchronized
    fun isFull() = entries.any { it.amount >= LIMIT }

    /**
     * Moves the items in the given output [slots] into the buffer.
     */
    @Synchronized
    fun absorb(menu: BlockMenu, slots: IntArray) {
        for (slot in slots) {
            val item = menu.getItemInSlot(slot)
            if (item.isAir()) continue

            var entry = entries.firstOrNull { matches(it.template, item!!) }
            if (entry == null) {
                // one item type per output slot at most
                if (entries.size >= slots.size) continue
                val template = item!!.clone().apply { amount = 1 }
                entry = Entry(template, serialize(template), 0)
                entries.add(entry)
            }

            val moved = minOf(item!!.amount, LIMIT - entry.amount)
            if (moved <= 0) {
                if (entry.amount <= 0) entries.remove(entry)
                continue
            }

            entry.amount += moved
            menu.consumeItem(slot, moved)
            dirty = true
        }
    }

    /**
     * Fills the given output [slots] with (at most) normal stacks from the buffer.
     */
    @Synchronized
    fun refill(menu: BlockMenu, slots: IntArray) {
        val iterator = entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val maxStack = entry.template.maxStackSize

            for (slot in slots) {
                if (entry.amount <= 0) break

                val current = menu.getItemInSlot(slot)
                if (current.isAir()) {
                    val give = minOf(maxStack, entry.amount)
                    menu.replaceExistingItem(slot, entry.template.clone().apply { amount = give })
                    entry.amount -= give
                    dirty = true
                } else if (matches(entry.template, current!!) && current.amount < maxStack) {
                    val give = minOf(maxStack - current.amount, entry.amount)
                    menu.replaceExistingItem(slot, current.clone().apply { amount = current.amount + give })
                    entry.amount -= give
                    dirty = true
                }
            }

            if (entry.amount <= 0) {
                iterator.remove()
                dirty = true
            }
        }
    }

    /**
     * Writes the buffer into the block data, if it has changed.
     */
    @Synchronized
    fun save() {
        if (!dirty) return
        dirty = false
        location.setString(BS_BUFFER, entries.joinToString(ENTRY_SEPARATOR) { "${it.amount}$FIELD_SEPARATOR${it.serialized}" })
    }

    /**
     * Removes everything from the buffer and returns it as a list of item stacks (split into normal stacks).
     */
    @Synchronized
    fun drain(): List<ItemStack> {
        val result = mutableListOf<ItemStack>()
        for (entry in entries) {
            var left = entry.amount
            val maxStack = entry.template.maxStackSize
            while (left > 0) {
                val give = minOf(maxStack, left)
                result.add(entry.template.clone().apply { amount = give })
                left -= give
            }
        }
        entries.clear()
        dirty = true
        save()
        return result
    }

    /**
     * Creates the item that shows what is currently stored in the buffer.
     */
    @Synchronized
    fun createStatusItem(): ItemStack {
        val item = ItemStack(Material.BARREL)
        val meta = item.itemMeta ?: return item
        meta.setDisplayName(ChatUtil.color("&bBuffered output"))
        meta.lore = entries.map {
            ChatUtil.color(
                "&7${ItemUtils.getItemName(it.template)}&8: &e${NUMBER_FORMAT.format(it.amount)} &7/ &e${NUMBER_FORMAT.format(LIMIT)}"
            )
        }
        item.itemMeta = meta
        return item
    }

    private fun load() {
        val raw = location.getString(BS_BUFFER)
        if (raw.isNullOrEmpty()) return

        for (part in raw.split(ENTRY_SEPARATOR)) {
            val fields = part.split(FIELD_SEPARATOR, limit = 2)
            if (fields.size != 2) continue
            val amount = fields[0].toIntOrNull() ?: continue
            val template = deserialize(fields[1]) ?: continue
            if (amount > 0) {
                entries.add(Entry(template, fields[1], amount))
            }
        }
    }

    companion object {

        /**
         * The maximum amount of each item type that is stacked inside a compressed machine.
         */
        const val LIMIT = 4096

        private const val BS_BUFFER = "output_buffer"
        private const val ENTRY_SEPARATOR = ";"
        private const val FIELD_SEPARATOR = ":"
        private val NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.US)

        private val buffers = ConcurrentHashMap<BlockPosition, OutputBuffer>()

        /**
         * Gets (or loads) the buffer of the machine at the given [menu].
         */
        fun of(menu: BlockMenu): OutputBuffer =
            buffers.computeIfAbsent(menu.position) { OutputBuffer(menu.location) }

        /**
         * Forgets the buffer of the machine at the given [menu] and returns the items inside it.
         */
        fun remove(menu: BlockMenu): List<ItemStack> {
            val buffer = buffers.remove(menu.position) ?: OutputBuffer(menu.location)
            return buffer.drain()
        }

        private fun matches(template: ItemStack, item: ItemStack) =
            SlimefunUtils.isItemSimilar(item, template, true, false)

        private fun serialize(item: ItemStack): String =
            Base64.getEncoder().encodeToString(item.serializeAsBytes())

        private fun deserialize(data: String): ItemStack? = try {
            ItemStack.deserializeBytes(Base64.getDecoder().decode(data))
        } catch (e: Exception) {
            null
        }
    }
}
