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
import org.bukkit.Material
import org.bukkit.Note
import org.bukkit.block.data.type.NoteBlock as NoteBlockData
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.NotePlayEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
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
class NativePlacementListener(private val provider: NativeItemProvider) : Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        if (event.hand != EquipmentSlot.HAND) return  // prevent double-fire from off-hand

        val clickedBlock = event.clickedBlock ?: return

        // Prevent players tuning a registered letter block (changes note → breaks texture)
        if (clickedBlock.type == Material.NOTE_BLOCK && provider.getCustomBlock(clickedBlock) != null) {
            event.isCancelled = true
            return
        }

        val item = event.item?.takeIf { !it.type.isAir } ?: return
        val itemInfo = provider.getCustomItem(item) ?: return

        // Pyrite items are tools, not placeable blocks
        if (itemInfo.namespacedId.contains("pyrite")) return

        val targetBlock = clickedBlock.getRelative(event.blockFace)
        if (!targetBlock.type.isAir) return

        event.isCancelled = true

        targetBlock.type = Material.NOTE_BLOCK

        // Set the instrument+note state that the resource pack maps to this letter's texture.
        // The state must be applied after setting the type, and re-applied because Minecraft
        // may override the instrument on placement based on the block below.
        val state = provider.getPlacementState(itemInfo.namespacedId)
        if (state != null) {
            val noteData = targetBlock.blockData as NoteBlockData
            noteData.instrument = state.first
            @Suppress("DEPRECATION")
            noteData.note = Note(state.second)
            noteData.isPowered = false
            targetBlock.blockData = noteData
        }

        provider.registerPlacedBlock(targetBlock.location, itemInfo.namespacedId)

        if (event.player.gameMode != GameMode.CREATIVE) {
            item.amount -= 1
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onNotePlay(event: NotePlayEvent) {
        if (provider.getCustomBlock(event.block) != null) {
            event.isCancelled = true
        }
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
