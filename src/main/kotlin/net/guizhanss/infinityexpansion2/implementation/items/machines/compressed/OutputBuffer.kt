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
 * An [ItemStack] can never hold more than 99 items, so the large storage of compressed machines is
 * implemented by keeping the items in this buffer (one entry per item type, each up to [LIMIT]) and only showing
 * normal stacks in the slots. The output slots are refilled from the buffer every tick, so hoppers, cargo
 * and players still interact with ordinary item stacks.
 */
class OutputBuffer private constructor(private val location: Location, private val key: String) {

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
     * Moves the items in the given [slots] into the buffer. Only the items accepted by [accept] are moved.
     */
    @Synchronized
    fun absorb(menu: BlockMenu, slots: IntArray, accept: (ItemStack) -> Boolean = { true }) {
        for (slot in slots) {
            val item = menu.getItemInSlot(slot)
            if (item.isAir() || !accept(item!!)) continue

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
     * Adds [count] items of [template] directly into the buffer, without going through any slot.
     * At most [maxTypes] different item types are kept.
     *
     * Returns the amount of items that were actually added.
     */
    @Synchronized
    fun add(template: ItemStack, count: Int, maxTypes: Int): Int {
        if (count <= 0 || template.isAir()) return 0

        var entry = entries.firstOrNull { matches(it.template, template) }
        if (entry == null) {
            if (entries.size >= maxTypes) return 0
            val single = template.clone().apply { amount = 1 }
            entry = Entry(single, serialize(single), 0)
            entries.add(entry)
        }

        val moved = minOf(count, LIMIT - entry.amount)
        if (moved <= 0) {
            if (entry.amount <= 0) entries.remove(entry)
            return 0
        }

        entry.amount += moved
        dirty = true
        return moved
    }

    /**
     * Fills the given [slots] with the items from the buffer. The slots only get normal stacks
     * (never more than the max stack size of the item), because bigger stacks cannot be saved and break
     * the menu on the client.
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
        location.setString(key, entries.joinToString(ENTRY_SEPARATOR) { "${it.amount}$FIELD_SEPARATOR${it.serialized}" })
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
     * The first item type in the buffer (as a single item) and its amount, or `null` if the buffer is empty.
     */
    @Synchronized
    fun first(): Pair<ItemStack, Int>? = entries.firstOrNull()?.let { it.template.clone() to it.amount }

    /**
     * The lore lines that describe the content of this buffer.
     */
    @Synchronized
    fun lines(): List<String> = entries.map {
        ChatUtil.color(
            "&7${ItemUtils.getItemName(it.template)}&8: &e${NUMBER_FORMAT.format(it.amount)} &7/ &e${NUMBER_FORMAT.format(LIMIT)}"
        )
    }

    private fun load() {
        val raw = location.getString(key)
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
         * The maximum amount of each item type that is stored in the buffers of a compressed machine.
         */
        const val LIMIT = 32768

        private const val BS_OUTPUT = "output_buffer"
        private const val BS_INPUT = "input_buffer"
        private const val ENTRY_SEPARATOR = ";"
        private const val FIELD_SEPARATOR = ":"
        private val NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.US)

        private val outputBuffers = ConcurrentHashMap<BlockPosition, OutputBuffer>()
        private val inputBuffers = ConcurrentHashMap<BlockPosition, OutputBuffer>()

        /**
         * Gets (or loads) the output buffer of the machine at the given [menu].
         */
        fun of(menu: BlockMenu): OutputBuffer =
            outputBuffers.computeIfAbsent(menu.position) { OutputBuffer(menu.location, BS_OUTPUT) }

        /**
         * Gets (or loads) the input buffer of the machine at the given [menu].
         *
         * Network pushers can only put one normal stack into each input slot, so the input slots are emptied
         * into this buffer every tick, which leaves room for the next push.
         */
        fun inputOf(menu: BlockMenu): OutputBuffer =
            inputBuffers.computeIfAbsent(menu.position) { OutputBuffer(menu.location, BS_INPUT) }

        /**
         * Forgets the buffers of the machine at the given [menu] and returns the items inside them.
         */
        fun remove(menu: BlockMenu): List<ItemStack> {
            val output = outputBuffers.remove(menu.position) ?: OutputBuffer(menu.location, BS_OUTPUT)
            val input = inputBuffers.remove(menu.position) ?: OutputBuffer(menu.location, BS_INPUT)
            return output.drain() + input.drain()
        }

        /**
         * Creates the item that shows what is currently stored in the buffers of the machine at the given [menu].
         */
        fun createStatusItem(menu: BlockMenu): ItemStack {
            val item = ItemStack(Material.BARREL)
            val meta = item.itemMeta ?: return item
            meta.setDisplayName(ChatUtil.color("&bBuffered items"))
            val lore = mutableListOf<String>()
            val input = inputOf(menu).lines()
            val output = of(menu).lines()
            if (input.isNotEmpty()) {
                lore.add(ChatUtil.color("&aInput:"))
                lore.addAll(input)
            }
            if (output.isNotEmpty()) {
                lore.add(ChatUtil.color("&6Output:"))
                lore.addAll(output)
            }
            meta.lore = lore
            item.itemMeta = meta
            return item
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
