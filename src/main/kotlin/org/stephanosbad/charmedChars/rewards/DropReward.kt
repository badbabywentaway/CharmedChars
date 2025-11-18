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
package org.stephanosbad.charmedChars.rewards

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.math.roundToInt

/**
 * Item drop reward implementation
 *
 * Spawns Minecraft items naturally at the player's location when they form valid words.
 * The number of items dropped is calculated based on the word score using the
 * parent Reward class formula, then rounded to an integer.
 *
 * Example configuration (from config.yml):
 * ```
 * - materialName: IRON_INGOT
 *   minimumRewardCount: 1.0
 *   multiplier: 0.01pull
 *   minimumThreshold: 100.0
 *   maximumRewardCap: 20.0
 * ```
 * This would drop 1-20 iron ingots based on word scores from 100-2000.
 *
 * @property materialName The Minecraft material name (e.g., "IRON_INGOT", "GOLD_NUGGET")
 */
class DropReward(
    var materialName: String?,
    minimumRewardCount: Double,
    multiplier: Double,
    minimumThreshold: Double,
    maximumRewardCap: Double
) : Reward(minimumRewardCount, multiplier, minimumThreshold, maximumRewardCap) {

    /**
     * The resolved Minecraft material to drop
     *
     * Initialized from materialName in the constructor.
     */
    private var material: Material? = null

    /**
     * Resolves the material name to a Minecraft Material enum value
     */
    private fun setMaterial() {
        material = Material.valueOf(materialName!!)
    }

    /**
     * Initializes the material from the material name
     */
    init {
        setMaterial()
    }

    /**
     * Applies the drop reward to the player
     *
     * Calculates the reward amount based on the score, spawns items at the specified
     * location, and sends a message to the player showing what they received.
     *
     * @param player The player who formed the word
     * @param location The location where items should be dropped
     * @param score The word score used to calculate reward amount
     */
    fun applyReward(player: Player, location: Location, score: Double) {
        var netAmount: Double = if (score >= minimumThreshold)
            (score - minimumThreshold) * multiplier + minimumRewardCount
        else
            minimumRewardCount
        if (netAmount > maximumRewardCap) {
            netAmount = maximumRewardCap
        }
        val count = netAmount.roundToInt().toInt()
        if (location.world == null) {
            return
        }
        if (count > 0) {
            player.sendMessage("§e+$count x $materialName")
            location.world.dropItemNaturally(location, ItemStack(material!!, count))
        }
    }
}
