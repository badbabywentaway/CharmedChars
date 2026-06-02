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
package org.stephanosbad.charmedChars.listeners

import org.bukkit.GameMode
import org.bukkit.Instrument
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Note
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.type.NoteBlock as NoteBlockData
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPhysicsEvent
import org.bukkit.event.block.NotePlayEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.stephanosbad.charmedChars.CharmedChars
import org.stephanosbad.charmedChars.integration.NativeItemProvider

/**
 * Handles placement and removal of native custom blocks.
 *
 * Letter/number/operator block items are PAPER (not directly placeable), so
 * right-click is intercepted to place a NOTE_BLOCK as the in-world carrier.
 * The note block's instrument+note state is set to the value registered in
 * NativeItemProvider so the resource pack can map each state to the correct
 * per-letter cube_all texture.
 *
 * Right-clicking an existing registered note block is cancelled to prevent
 * players changing the note (which would break the texture mapping).
 *
 * NOTE: For stable instrument state, add to paper-global.yml:
 *   block-updates:
 *     disable-noteblock-updates: true
 *
 * Only registered when the NativeItems provider is active.
 */
class NativePlacementListener(
    private val plugin: CharmedChars,
    private val provider: NativeItemProvider
) : Listener {

    // Locations of registered blocks that already have a pending physics-check task scheduled.
    // Prevents scheduling duplicate tasks when physics events fire in rapid succession.
    private val pendingPhysicsChecks = mutableSetOf<Location>()

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return

        val clickedBlock = event.clickedBlock ?: return

        // Prevent activating a registered letter block on EITHER hand (note would play otherwise)
        if (clickedBlock.type == Material.NOTE_BLOCK && provider.getCustomBlock(clickedBlock) != null) {
            event.isCancelled = true
            return
        }

        if (event.hand != EquipmentSlot.HAND) return  // placement is main-hand only

        val item = event.item?.takeIf { !it.type.isAir } ?: return
        val itemInfo = provider.getCustomItem(item) ?: return

        // Pyrite items are tools, not placeable blocks
        if (itemInfo.namespacedId.contains("pyrite")) return

        val targetBlock = clickedBlock.getRelative(event.blockFace)
        if (!targetBlock.type.isAir) return

        event.isCancelled = true

        targetBlock.type = Material.NOTE_BLOCK

        val state = provider.getPlacementState(itemInfo.namespacedId)
        if (state != null) {
            applyNoteBlockState(targetBlock, state)
            // Minecraft resets the note block instrument via block-physics updates based on the
            // block below. Re-apply one tick later to survive that update.
            val loc = targetBlock.location
            plugin.server.scheduler.runTaskLater(plugin, Runnable {
                val block = loc.block
                if (block.type == Material.NOTE_BLOCK) {
                    // [DIAG] Log if state drifted in the 1 tick since placement
                    val nd = block.blockData as? NoteBlockData
                    if (nd != null) {
                        @Suppress("DEPRECATION")
                        val actualNote = nd.note.id.toInt()
                        if (nd.instrument != state.first || actualNote != state.second) {
                            plugin.logger.info("[NativeDiag] 1t drift on ${itemInfo.namespacedId} at ${block.x},${block.y},${block.z}: " +
                                "actual=${nd.instrument}/$actualNote expected=${state.first}/${state.second} " +
                                "blockBelow=${block.getRelative(BlockFace.DOWN).type} powered=${nd.isPowered}")
                        }
                    }
                    applyNoteBlockState(block, state)
                }
            }, 1L)
        }

        provider.registerPlacedBlock(targetBlock.location, itemInfo.namespacedId)

        if (event.player.gameMode != GameMode.CREATIVE) {
            item.amount -= 1
        }
    }

    private fun applyNoteBlockState(block: Block, state: Pair<Instrument, Int>) {
        val noteData = block.blockData as? NoteBlockData ?: return
        noteData.instrument = state.first
        @Suppress("DEPRECATION")
        noteData.note = Note(state.second)
        noteData.isPowered = false
        block.blockData = noteData
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onNotePlay(event: NotePlayEvent) {
        val info = provider.getCustomBlock(event.block) ?: return
        // [DIAG] Log when a note fires on a registered block (right-click cancellation missed it)
        val nd = event.block.blockData as? NoteBlockData
        @Suppress("DEPRECATION")
        val noteId = nd?.note?.id?.toInt() ?: -1
        plugin.logger.info("[NativeDiag] Note play suppressed on ${info.namespacedId} at " +
            "${event.block.x},${event.block.y},${event.block.z}: " +
            "instrument=${nd?.instrument} note=$noteId powered=${nd?.isPowered}")
        event.isCancelled = true
    }

    // [DIAG] Detect note block state drift caused by Minecraft block-physics updates.
    // After any physics event on a registered block, check 1 tick later whether the
    // instrument+note state was silently changed by the engine.
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    fun onBlockPhysics(event: BlockPhysicsEvent) {
        val block = event.block
        if (block.type != Material.NOTE_BLOCK) return
        val info = provider.getCustomBlock(block) ?: return
        val loc = block.location
        if (!pendingPhysicsChecks.add(loc)) return  // task already queued for this location
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            pendingPhysicsChecks.remove(loc)
            val b = loc.block
            if (b.type != Material.NOTE_BLOCK) return@Runnable
            val blockInfo = provider.getCustomBlock(b) ?: return@Runnable
            val expected = provider.getPlacementState(blockInfo.namespacedId) ?: return@Runnable
            val nd = b.blockData as? NoteBlockData ?: return@Runnable
            @Suppress("DEPRECATION")
            val noteId = nd.note.id.toInt()
            if (nd.instrument != expected.first || noteId != expected.second) {
                plugin.logger.info("[NativeDiag] Physics drift on ${blockInfo.namespacedId} at ${b.x},${b.y},${b.z}: " +
                    "actual=${nd.instrument}/$noteId expected=${expected.first}/${expected.second} " +
                    "blockBelow=${b.getRelative(BlockFace.DOWN).type} powered=${nd.isPowered}")
            }
        }, 1L)
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent) {
        val customBlock = provider.getCustomBlock(event.block) ?: return

        // Prevent the note_block item from dropping; give back the custom item instead
        event.isDropItems = false

        if (event.player.gameMode != GameMode.CREATIVE) {
            val customItem = provider.getItemStack(customBlock.namespacedId)
            if (customItem != null) {
                event.block.world.dropItemNaturally(event.block.location, customItem)
            }
        }

        provider.unregisterPlacedBlock(event.block.location)
    }
}
