package org.stephanosbad.charmedChars.rewards

abstract class Reward
/**
 * Constructor
 * @param minimumRewardCount - Minimum number of rewards to drop.
 * @param multiplier - Multiply factor (by score)
 * @param minimumThreshold - Minimum score to apply reward
 * @param maximumRewardCap - Maximum number of rewards of this type.
 */ internal constructor(
    var minimumRewardCount: Double,
    var multiplier: Double,
    var minimumThreshold: Double,
    var maximumRewardCap: Double
)