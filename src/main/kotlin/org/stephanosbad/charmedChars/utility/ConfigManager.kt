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
package org.stephanosbad.charmedChars.utility

import org.bukkit.configuration.file.FileConfiguration
import org.stephanosbad.charmedChars.CharmedChars

/**
 * Configuration manager for accessing plugin settings
 *
 * Provides type-safe accessors for all plugin configuration values from config.yml.
 * Handles loading, reloading, and saving the configuration file.
 *
 * @property plugin The CharmedChars plugin instance
 */
class ConfigManager(private val plugin: CharmedChars) {

    /**
     * The loaded Bukkit configuration instance
     */
    private lateinit var config: FileConfiguration

    /**
     * Loads the configuration from disk
     *
     * Saves the default config.yml if it doesn't exist, then reloads the configuration.
     * Should be called during plugin initialization.
     */
    fun loadConfig() {
        plugin.saveDefaultConfig()
        plugin.reloadConfig()
        this.config = plugin.config
    }

    /**
     * Base chance (0.0 to 1.0) for letter blocks to drop when mining logs with gold tools
     *
     * Default: 0.06 (6% chance)
     */
    val letterBlockDropChance: Double
        get() = config.getDouble("letter-blocks.drop-chance", 0.06)

    /**
     * Drop chance multiplier for Looting I enchantment
     *
     * Default: 1.67 (approximately 10% total with base 6%)
     */
    val lootingMultiplier1: Double
        get() = config.getDouble("letter-blocks.looting-multipliers.1", 1.67)

    /**
     * Drop chance multiplier for Looting II enchantment
     *
     * Default: 2.67 (approximately 16% total with base 6%)
     */
    val lootingMultiplier2: Double
        get() = config.getDouble("letter-blocks.looting-multipliers.2", 2.67)

    /**
     * Drop chance multiplier for Looting III enchantment
     *
     * Default: 3.33 (approximately 20% total with base 6%)
     */
    val lootingMultiplier3: Double
        get() = config.getDouble("letter-blocks.looting-multipliers.3", 3.33)

    /**
     * Whether to integrate with WorldGuard for protection checks
     *
     * If true and WorldGuard is installed, letter blocks will respect WorldGuard regions.
     * Default: true
     */
    val worldGuardIntegration: Boolean
        get() = config.getBoolean("protection.worldguard-integration", true)

    /**
     * Whether to integrate with GriefPrevention for claim checks
     *
     * If true and GriefPrevention is installed, letter blocks will respect GriefPrevention claims.
     * Default: true
     */
    val griefPreventionIntegration: Boolean
        get() = config.getBoolean("protection.griefprevention-integration", true)

    /**
     * Material name for fortress number game reward
     *
     * Default: BLAZE_ROD
     */
    val fortressRewardMaterial: String
        get() = config.getString("structure-rewards.fortress.material", "BLAZE_ROD") ?: "BLAZE_ROD"

    /**
     * Amount of items to give for fortress number game reward
     *
     * Default: 12
     */
    val fortressRewardAmount: Int
        get() = config.getInt("structure-rewards.fortress.amount", 12)

    /**
     * Material name for bastion remnant number game reward
     *
     * Default: ENDER_PEARL
     */
    val bastionRewardMaterial: String
        get() = config.getString("structure-rewards.bastion.material", "ENDER_PEARL") ?: "ENDER_PEARL"

    /**
     * Amount of items to give for bastion remnant number game reward
     *
     * Default: 16
     */
    val bastionRewardAmount: Int
        get() = config.getInt("structure-rewards.bastion.amount", 16)

    /**
     * Reloads the configuration from disk
     *
     * Discards any in-memory changes and reloads from the config.yml file.
     * Used by the /ccreload command.
     */
    fun reloadConfig() {
        plugin.reloadConfig()
        this.config = plugin.config
    }

    /**
     * Saves the current configuration to disk
     *
     * Writes any programmatic changes to the config.yml file.
     */
    fun saveConfig() {
        plugin.saveConfig()
    }
}