/*
 * CharmedChars - A word-forming puzzle game for Minecraft
 * Copyright (C) 2025 StephanosBad
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. If not, see <https://www.gnu.org/licenses/>.
 */
package org.stephanosbad.charmedchars.listeners

import org.bukkit.Bukkit
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDamageAbortEvent
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.stephanosbad.charmedchars.CharmedChars

/**
 * Diagnostic logging of the order in which hit/break events reach CharmedChars
 * blocks, enabled by `debug.event-order` in config.yml.
 *
 * Item providers fire click, damage and break events in different orders (and
 * some fire several for one hit), which word scoring and logo blocks depend on.
 * Each event is logged twice: at LOWEST, before CharmedChars' own handlers, with
 * the custom block the provider reports at that moment; and at MONITOR, with the
 * final cancelled state after every plugin has handled it. Block drops are logged
 * too, to show whether and when a CharmedChars block dropped as an item.
 *
 * Lines are prefixed "[EventOrder]" with the server tick and a running sequence
 * number, so one hit's events can be read in order.
 */
class EventOrderDebugListener(private val plugin: CharmedChars) : Listener {

    private var sequence = 0L

    /**
     * Recently seen CharmedChars block positions → (item id, tick), so events that
     * arrive after the block was removed (e.g. the MONITOR pass, or the drop) are
     * still logged
     */
    private val recent = mutableMapOf<String, Pair<String, Int>>()

    private val enabled get() = plugin.configManager.debugEventOrder

    @EventHandler(priority = EventPriority.LOWEST)
    fun beforeInteract(e: PlayerInteractEvent) {
        if (e.action == Action.LEFT_CLICK_BLOCK) e.clickedBlock?.let { log("before", "Interact", e, e.player, it) }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun afterInteract(e: PlayerInteractEvent) {
        if (e.action == Action.LEFT_CLICK_BLOCK) e.clickedBlock?.let { log("after ", "Interact", e, e.player, it) }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun beforeDamage(e: BlockDamageEvent) = log("before", "Damage", e, e.player, e.block, "instaBreak=${e.instaBreak}")

    @EventHandler(priority = EventPriority.MONITOR)
    fun afterDamage(e: BlockDamageEvent) = log("after ", "Damage", e, e.player, e.block, "instaBreak=${e.instaBreak}")

    @EventHandler(priority = EventPriority.MONITOR)
    fun onDamageAbort(e: BlockDamageAbortEvent) = log("      ", "DamageAbort", null, e.player, e.block)

    @EventHandler(priority = EventPriority.LOWEST)
    fun beforeBreak(e: BlockBreakEvent) = log("before", "Break", e, e.player, e.block, "dropItems=${e.isDropItems}")

    @EventHandler(priority = EventPriority.MONITOR)
    fun afterBreak(e: BlockBreakEvent) = log("after ", "Break", e, e.player, e.block, "dropItems=${e.isDropItems}")

    @EventHandler(priority = EventPriority.MONITOR)
    fun onDrop(e: BlockDropItemEvent) {
        if (!enabled) return
        val drops = e.items.joinToString { item ->
            val custom = plugin.customItemProviderManager.getProvider()?.getCustomItem(item.itemStack)
            custom?.namespacedId ?: item.itemStack.type.name
        }
        // The block is already gone by now; report what it was and what dropped
        log("      ", "DropItem", e, e.player, e.block, "was=${e.blockState.type} drops=[$drops]")
    }

    private fun log(stage: String, name: String, event: Event?, player: Player, block: Block, extra: String = "") {
        if (!enabled) return
        val tick = Bukkit.getCurrentTick()
        val key = "${block.world.name}:${block.x}:${block.y}:${block.z}"
        recent.entries.removeIf { tick - it.value.second > RECENT_TICKS }

        val provider = plugin.customItemProviderManager.getProvider()
        val custom = provider?.getCustomBlock(block)?.namespacedId
        // Oraxen/Nexo have no real namespaces, so their providers prefix with their own
        // name (e.g. "oraxen:cyan_a") instead of "charmedchars:" - accept both, same as
        // ItemManager.getCustomVariation() already does when parsing this same ID.
        val ownPrefix = provider?.getProviderName()?.lowercase() + ":"
        val shown = when {
            custom?.startsWith("charmedchars:") == true -> custom.also { recent[key] = it to tick }
            custom?.startsWith(ownPrefix) == true -> custom.also { recent[key] = it to tick }
            key in recent -> "gone(was ${recent.getValue(key).first})"
            else -> return // not a CharmedChars block
        }
        val cancelled = (event as? Cancellable)?.isCancelled?.let { " cancelled=$it" } ?: ""
        plugin.logger.info(
            "[EventOrder] tick=$tick #${++sequence} $stage $name " +
                "player=${player.name} block=${block.x},${block.y},${block.z} ${block.type} " +
                "custom=$shown$cancelled $extra".trimEnd()
        )
    }

    private companion object {
        /** How long a removed block's position keeps being logged */
        const val RECENT_TICKS = 100
    }
}
