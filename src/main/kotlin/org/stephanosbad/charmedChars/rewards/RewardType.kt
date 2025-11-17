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
 * Enum of available reward types
 *
 * Defines the types of rewards that can be configured for valid words.
 * Used by ConfigDataHandler when loading reward configurations from config.yml.
 *
 * Currently supported reward types:
 * - Drop: Spawns Minecraft items at the player's location
 *
 * Future reward types could include:
 * - Economy: Give money (requires Vault)
 * - Experience: Give XP points
 * - Custom: Execute custom commands
 */
enum class RewardType {
    /**
     * Item drop reward type
     *
     * Configured in config.yml under the "Drop" section.
     * See DropReward class for implementation.
     */
    Drop
}
