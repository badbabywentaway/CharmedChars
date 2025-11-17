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

class DropReward(
    /**
     * Name of MC material to drop for rewards.
     */
    var materialName: String?,
    minimumRewardCount: Double,
    multiplier: Double,
    minimumThreshold: Double,
    maximumRewardCap: Double
) : Reward(minimumRewardCount, multiplier, minimumThreshold, maximumRewardCap) {
    /**
     * Set material based on name
     */
    private fun setMaterial() {
        material = Material.valueOf(materialName!!)
    }

    /**
     * MC material to drop for rewards.
     */
    private var material: Material? = null

    /**
     * Constructor
     *
     * @param materialName       - Name of MC material to drop for rewards.
     * @param minimumRewardCount - Minimum number of rewards to drop.
     * @param multiplier         - Multiply factor (by score)
     * @param minimumThreshold   - Minimum score to apply reward
     * @param maximumRewardCap   - Maximum number of rewards of this type.
     */
    init {
        setMaterial()
    }

    /**
     * Apply the reward. Drops are location specific.
     *
     * @param player - player.
     * @param location - location to drop the reward.
     * @param score    - score in which to apply reward.
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
