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
 * Drops a randomly-colored logo block onto the outer bedrock arm of the exit portal
 * fountain (X=3, Z=0 in The End) when the Ender Dragon dies.
 *
 * The drop is deliberately placed on the outer arm rather than the centre because
 * any item entity that touches an exit portal block is teleported to world spawn.
 * The portal hole spans ±2 blocks from centre (X/Z); the outer bedrock arms at ±3
 * are solid and clear of all portal blocks.
 *
 * Drop Y is determined at runtime from the highest block at (3, 0) so it works
 * regardless of world height differences between server configurations.
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

        // Drop onto the outer bedrock arm of the fountain (X=3, Z=0) rather than
        // the centre (X=0, Z=0). The exit portal blocks span ±2 from centre; any
        // item entity that touches them is teleported to world spawn. The outer arm
        // at X=3 is solid bedrock and sits just beyond the portal hole, so the item
        // is guaranteed never to land on a portal block regardless of dropItemNaturally's
        // random scatter velocity.
        val outerArmY = world.getHighestBlockYAt(3, 0)
        val dropLocation = Location(world, 3.5, (outerArmY + 2).toDouble(), 0.5)
        world.dropItemNaturally(dropLocation, logoItem)
    }
}
