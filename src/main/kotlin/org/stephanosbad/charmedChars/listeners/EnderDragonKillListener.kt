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

import org.bukkit.Location
import org.bukkit.entity.EnderDragon
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.stephanosbad.charmedChars.CharmedChars
import org.stephanosbad.charmedChars.items.BlockColor

/**
 * Drops a randomly-colored logo block onto the end stone ground at X=5, Z=0 when
 * the Ender Dragon dies.
 *
 * The drop is placed on the flat end stone just outside the exit portal fountain
 * structure to avoid two hazards:
 *  1. Exit portal blocks (span ±2 from centre) teleport any touching item entity
 *     to world spawn.
 *  2. The dragon egg (present after the first kill) has a non-full collision box —
 *     items placed on or near it can slide off onto the surrounding portal blocks.
 *
 * At X=5 the item lands on solid end stone clear of both hazards. dropItemNaturally's
 * scatter (~1 block max) cannot reach the portal from this distance.
 *
 * @property plugin Reference to the main plugin instance
 */
class EnderDragonKillListener(
    private val plugin: CharmedChars
) : Listener {

    @EventHandler
    fun onEnderDragonDeath(event: EntityDeathEvent) {
        if (event.entity !is EnderDragon) return

        val world = event.entity.world

        val color = BlockColor.getRand()
        val itemId = "charmedchars:${color.directoryName}_logo"

        val provider = plugin.customItemProviderManager.getProvider()
        if (provider == null) {
            plugin.logger.warning("EnderDragonKillListener: no custom item provider available, cannot drop logo block")
            return
        }

        val logoItem = provider.getItemStack(itemId)
        if (logoItem == null) {
            plugin.logger.warning("EnderDragonKillListener: item '$itemId' not found in provider ${provider.getProviderName()}")
            return
        }

        // Drop on end stone at X=5, Z=0 — outside the fountain structure and
        // well beyond the exit portal blocks (±2 from centre) and the dragon egg
        // (non-full hitbox, items can slide off onto portal blocks).
        val groundY = world.getHighestBlockYAt(5, 0)
        val dropLocation = Location(world, 5.5, (groundY + 2).toDouble(), 0.5)
        world.dropItemNaturally(dropLocation, logoItem)
    }
}
