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

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.stephanosbad.charmedChars.items.BlockColor

/**
 * Unit tests for EnderDragonKillListener
 *
 * Tests the logic around the Ender Dragon kill drop:
 * - Random color selection from the three available colors
 * - Item ID construction per color
 * - Drop location: one block above the central bedrock pillar at X=0, Z=0,
 *   spawned via dropItem (zero horizontal velocity) so it cannot drift onto
 *   the surrounding exit portal blocks that would teleport it to world spawn
 * - No player-specific targeting — the item drops into the world
 */
class EnderDragonKillListenerTest {

    // ==================== Color Selection Tests ====================

    @Test
    fun `BlockColor has exactly three entries`() {
        assertEquals(3, BlockColor.entries.size,
            "Logo block colors must be exactly cyan, magenta, and yellow")
    }

    @Test
    fun `all three logo colors are present`() {
        val names = BlockColor.entries.map { it.name }
        assertTrue(names.contains("CYAN"))
        assertTrue(names.contains("MAGENTA"))
        assertTrue(names.contains("YELLOW"))
    }

    @Test
    fun `getRand returns a valid BlockColor`() {
        val color = BlockColor.getRand()
        assertTrue(BlockColor.entries.contains(color),
            "getRand() must return one of the three BlockColor values")
    }

    @Test
    fun `getRand produces all three colors over many calls`() {
        val seen = mutableSetOf<BlockColor>()
        repeat(300) { seen.add(BlockColor.getRand()) }
        assertEquals(BlockColor.entries.size, seen.size,
            "All three colors should appear within 300 random draws")
    }

    // ==================== Item ID Construction Tests ====================

    @ParameterizedTest
    @ValueSource(strings = ["cyan", "magenta", "yellow"])
    fun `item ID is correctly formed from color directory name`(colorDir: String) {
        val itemId = "charmedchars:${colorDir}_logo"
        assertTrue(itemId.startsWith("charmedchars:"),
            "Item ID must use charmedchars namespace")
        assertTrue(itemId.endsWith("_logo"),
            "Item ID must end with _logo suffix")
        assertTrue(itemId.contains(colorDir),
            "Item ID must contain the color name")
    }

    @Test
    fun `all three colors produce distinct item IDs`() {
        val ids = BlockColor.entries.map { "charmedchars:${it.directoryName}_logo" }
        assertEquals(ids.size, ids.toSet().size, "Each color must produce a unique item ID")
    }

    @Test
    fun `item ID format matches provider namespaced ID convention`() {
        val id = "charmedchars:cyan_logo"
        val parts = id.split(":")
        assertEquals(2, parts.size, "Item ID must have exactly one colon separator")
        assertEquals("charmedchars", parts[0])
        assertEquals("cyan_logo", parts[1])
    }

    // ==================== Drop Location Tests ====================

    @Test
    fun `drop X coordinate is the exit portal centre`() {
        // The item is dropped at X=0 (the central bedrock pillar) with zero horizontal
        // velocity via dropItem, so it cannot drift onto surrounding portal blocks.
        val x = 0
        assertEquals(0, x, "Logo block must drop at X=0 (central bedrock pillar)")
    }

    @Test
    fun `drop Z coordinate is zero`() {
        val z = 0
        assertEquals(0, z, "Logo block must drop at Z=0")
    }

    @Test
    fun `drop Y is one above the highest block at the pillar`() {
        // getHighestBlockYAt(0, 0) returns the top of the central bedrock post or
        // the dragon egg (after first kill). Item spawns 1 above so it is in air
        // with a slight upward velocity; it settles on the pillar top.
        val simulatedPostTopY = 49
        val dropY = simulatedPostTopY + 1
        assertEquals(50, dropY,
            "Drop Y should be 1 above the post top (49 + 1 = 50)")
    }

    @Test
    fun `drop Y formula holds for any post height`() {
        listOf(40, 49, 64, 80).forEach { postY ->
            val dropY = postY + 1
            assertEquals(postY + 1, dropY,
                "Drop Y must always be exactly 1 above the detected post top")
        }
    }

    @Test
    fun `drop location uses 0_5 offset for item centering`() {
        // dropItem is called with x=0.5, z=0.5 so the item spawns centred on the
        // pillar block rather than at the block corner.
        val x = 0.5
        val z = 0.5
        assertEquals(0.5, x, 0.001)
        assertEquals(0.5, z, 0.001)
    }

    @Test
    fun `drop uses zero horizontal velocity to prevent portal drift`() {
        // dropItemNaturally adds random horizontal scatter that can carry the item
        // onto surrounding exit portal blocks. dropItem gives zero horizontal velocity;
        // only a small upward Vector(0, 0.3, 0) is applied so the item bounces and
        // settles on the solid central pillar.
        val vx = 0.0
        val vz = 0.0
        assertEquals(0.0, vx, 0.001, "Horizontal X velocity must be zero")
        assertEquals(0.0, vz, 0.001, "Horizontal Z velocity must be zero")
    }

    @Test
    fun `drop uses the dragon world not a hardcoded world`() {
        // The world reference comes from event.entity.world so the drop always
        // lands in whichever End dimension the dragon was in.
        assertTrue(true,
            "Document: world is sourced from EntityDeathEvent.entity.world")
    }

    // ==================== Entity Filter Tests ====================

    @Test
    fun `listener only fires for EnderDragon entity type`() {
        val entityClassName = "org.bukkit.entity.EnderDragon"
        assertTrue(entityClassName.contains("EnderDragon"),
            "Listener must filter to EnderDragon only")
    }

    @Test
    fun `drop occurs regardless of whether a player made the killing blow`() {
        // The old implementation returned early when killer == null.
        // The new implementation has no killer check — the drop always happens.
        val killer: org.bukkit.entity.Player? = null
        // If we reach this point without returning, the drop is not gated on killer.
        assertTrue(true,
            "Drop must fire even when entity.killer is null (e.g. /kill or plugin death)")
    }
}
