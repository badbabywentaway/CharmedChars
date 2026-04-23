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
import org.bukkit.util.Vector
import org.stephanosbad.charmedChars.CharmedChars
import org.stephanosbad.charmedChars.items.BlockColor

/**
 * Drops a randomly-colored logo block onto the central bedrock pillar of the exit
 * portal fountain (X=0, Z=0 in The End) when the Ender Dragon dies.
 *
 * The item is spawned with dropItem (not dropItemNaturally) so it has zero horizontal
 * velocity — it cannot drift sideways onto the surrounding exit portal blocks, which
 * teleport any item entity that touches them to world spawn. A small upward velocity
 * gives the item a visible bounce before it settles on the solid central pillar.
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

        // Spawn directly above the central bedrock pillar at (0, 0) with a pure
        // upward velocity. dropItem (not dropItemNaturally) gives zero horizontal
        // velocity, so the item cannot drift onto the surrounding exit portal blocks.
        // The central pillar is solid bedrock — not a portal block — and the item
        // settles on top of it (or on the dragon egg after the first kill).
        val postTopY = world.getHighestBlockYAt(0, 0)
        val dropLocation = Location(world, 0.5, (postTopY + 1).toDouble(), 0.5)
        val droppedItem = world.dropItem(dropLocation, logoItem)
        droppedItem.velocity = Vector(0.0, 0.3, 0.0)
    }
}
