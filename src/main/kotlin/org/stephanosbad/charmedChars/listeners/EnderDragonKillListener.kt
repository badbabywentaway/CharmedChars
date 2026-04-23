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
 * Drops a randomly-colored logo block two blocks above the exit portal bedrock post
 * (X=0, Z=0 in The End) when the Ender Dragon dies.
 *
 * The drop Y is determined at runtime from the highest solid block at (0, 0) so the
 * position remains correct regardless of world height changes between versions.
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

        // Find the top of the bedrock post at the exit portal centre (0, 0) and drop
        // two blocks above it so the item lands visibly on top of the structure.
        val postTopY = world.getHighestBlockYAt(0, 0)
        val dropLocation = Location(world, 0.5, (postTopY + 2).toDouble(), 0.5)
        world.dropItemNaturally(dropLocation, logoItem)
    }
}
