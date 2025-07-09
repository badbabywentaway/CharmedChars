package org.stephanosbad.charmedChars.Rewards

import org.bukkit.entity.Player

/**
 * Super class of currency plugins.
 */
abstract class CurrencyReward
/**
 * Constructor
 * @param minimumRewardCount - Minimum number of rewards to drop.
 * @param multiplier - Multiply factor (by score)
 * @param minimumThreshold - Minimum score to apply reward
 * @param maximumRewardCap - Maximum number of rewards of this type.
 */
internal constructor(
    minimumRewardCount: Double,
    multiplier: Double,
    minimumThreshold: Double,
    maximumRewardCap: Double
) : Reward(minimumRewardCount, multiplier, minimumThreshold, maximumRewardCap) {
    /**
     * Apply the reward.
     * @param player Player to whom to reward.
     * @param score - score to determine reward.
     */
    abstract fun applyReward(player: Player, score: Double)
}