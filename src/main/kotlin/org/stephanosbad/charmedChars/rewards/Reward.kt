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

/**
 * Abstract base class for word score rewards
 *
 * Rewards are given to players when they form valid words. The reward amount
 * is calculated based on the word score using a configurable formula:
 *
 * If score >= minimumThreshold:
 *   amount = (score - threshold) * multiplier + minimumRewardCount
 * Else:
 *   amount = minimumRewardCount
 *
 * The final amount is capped at maximumRewardCap.
 *
 * @property minimumRewardCount Base reward amount (always given if any reward is given)
 * @property multiplier Multiplier applied to score above threshold
 * @property minimumThreshold Minimum score required to apply the multiplier
 * @property maximumRewardCap Maximum reward amount (prevents excessive rewards)
 */
abstract class Reward internal constructor(
    var minimumRewardCount: Double,
    var multiplier: Double,
    var minimumThreshold: Double,
    var maximumRewardCap: Double
)