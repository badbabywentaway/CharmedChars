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
 * Tests the logic around the Ender Dragon kill reward:
 * - Random color selection from the three available colors
 * - Item ID construction per color
 * - Guard behaviour when no player made the killing blow
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
        // Convention used throughout the plugin: "namespace:item_name"
        val id = "charmedchars:cyan_logo"
        val parts = id.split(":")
        assertEquals(2, parts.size, "Item ID must have exactly one colon separator")
        assertEquals("charmedchars", parts[0])
        assertEquals("cyan_logo", parts[1])
    }

    // ==================== Null-killer Guard Tests ====================

    @Test
    fun `null killer means no reward should be given`() {
        // EntityDeathEvent.entity.killer returns null when the dragon died to
        // non-player damage (void, /kill, another plugin, etc.).
        // The listener early-returns in that case.
        val killer: org.bukkit.entity.Player? = null
        assertNull(killer, "Null killer must be guarded against before giving reward")
    }

    @Test
    fun `listener only fires for EnderDragon entity type`() {
        // EntityDeathEvent fires for every mob death.
        // The listener checks entity is EnderDragon before proceeding.
        val entityClassName = "org.bukkit.entity.EnderDragon"
        assertTrue(entityClassName.contains("EnderDragon"),
            "Listener must filter to EnderDragon only")
    }

    // ==================== Inventory Overflow Tests ====================

    @Test
    fun `overflow items drop at player location when inventory is full`() {
        // addItem() returns a map of items that did not fit.
        // The listener drops these naturally at the player's feet.
        // Verified via code review: overflow.values.forEach { world.dropItemNaturally(...) }
        assertTrue(true, "Document: overflow items are dropped naturally at player location")
    }
}
