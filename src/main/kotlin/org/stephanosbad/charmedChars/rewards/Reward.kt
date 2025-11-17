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