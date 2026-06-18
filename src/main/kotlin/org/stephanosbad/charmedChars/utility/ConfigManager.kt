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
import org.stephanosbad.charmedChars.database.StructureType
import org.stephanosbad.charmedChars.rewards.DropReward

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
     * Default: 0.125 (12.5% chance)
     */
    val letterBlockDropChance: Double
        get() = config.getDouble("letter-blocks.drop-chance", 0.125)

    /**
     * Drop chance multiplier for Fortune I enchantment
     *
     * Default: 2.0 (25% total with base 12.5%)
     */
    val lootingMultiplier1: Double
        get() = config.getDouble("letter-blocks.fortune-multipliers.1", 2.0)

    /**
     * Drop chance multiplier for Fortune II enchantment
     *
     * Default: 3.0 (37.5% total with base 12.5%)
     */
    val lootingMultiplier2: Double
        get() = config.getDouble("letter-blocks.fortune-multipliers.2", 3.0)

    /**
     * Drop chance multiplier for Fortune III enchantment
     *
     * Default: 4.0 (50% total with base 12.5%)
     */
    val lootingMultiplier3: Double
        get() = config.getDouble("letter-blocks.fortune-multipliers.3", 4.0)

    /**
     * Minimum word length for same-color words
     *
     * Default: 3 letters
     */
    val minWordLengthSameColor: Int
        get() = config.getInt("letter-blocks.min-word-length.same-color", 3)

    /**
     * Minimum word length for multi-color words
     *
     * Default: 4 letters
     */
    val minWordLengthMultiColor: Int
        get() = config.getInt("letter-blocks.min-word-length.multi-color", 4)

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
     * Returns the list of items to prefill into the logo block shulker box.
     *
     * Reads from `logo-block.shulker-contents` in config.yml.
     * Each entry must have `materialName` (String) and `quantity` (Int).
     * Defaults to 4 ghast tears if the config section is absent or invalid.
     *
     * @return List of (Material, quantity) pairs
     */
    fun logoBlockShulkerContents(): List<Pair<org.bukkit.Material, Int>> {
        val list = config.getList("logo-block.shulker-contents")
        if (list.isNullOrEmpty()) {
            return listOf(org.bukkit.Material.GHAST_TEAR to 4)
        }
        return list.mapNotNull { entry ->
            val map = entry as? Map<*, *> ?: return@mapNotNull null
            val materialName = map["materialName"] as? String ?: return@mapNotNull null
            val quantity = (map["quantity"] as? Int) ?: 1
            val material = runCatching { org.bukkit.Material.valueOf(materialName) }.getOrNull()
                ?: return@mapNotNull null
            material to quantity
        }.ifEmpty { listOf(org.bukkit.Material.GHAST_TEAR to 4) }
    }

    /**
     * Whether the glassing beds feature is enabled
     *
     * When enabled, bed explosions in the Nether or End will convert
     * lava blocks within a 5-block radius to glass blocks.
     * Default: false
     */
    val nativeItemsHttpPort: Int
        get() = config.getInt("native-items.http-port", 8080)

    val nativeCmdBase: Int
        get() = config.getInt("native-items.cmd-base", 1000)

    val nativeItemsHostname: String
        get() = config.getString("native-items.hostname", "") ?: ""

    val nativeItemsPackRequired: Boolean
        get() = config.getBoolean("native-items.pack-required", false)

    val glassingBedsEnabled: Boolean
        get() = config.getBoolean("glassing-beds.enabled", false)

    /**
     * Maximum Y-level for glassing beds lava conversion
     *
     * Only lava blocks at or below this Y-level will be converted to glass.
     * This prevents using beds to glass lava ocean surfaces (Y=31) for easy travel.
     * Default: 28
     */
    val glassingBedsMaxY: Int
        get() = config.getInt("glassing-beds.max-y", 28)

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
     * Gets the number score rewards for fortress structures
     *
     * Returns a list of DropReward instances configured for fortress number games.
     * These rewards use a score-based formula where the 3-digit number is the score.
     *
     * @return List of DropReward instances for fortresses
     */
    fun getFortressNumberScoreRewards(): List<DropReward> {
        return plugin.configDataHandler?.loadNumberScoreRewards(StructureType.FORTRESS) ?: emptyList()
    }

    /**
     * Gets the number score rewards for bastion remnant structures
     *
     * Returns a list of DropReward instances configured for bastion number games.
     * These rewards use a score-based formula where the 3-digit number is the score.
     *
     * @return List of DropReward instances for bastions
     */
    fun getBastionNumberScoreRewards(): List<DropReward> {
        return plugin.configDataHandler?.loadNumberScoreRewards(StructureType.BASTION_REMNANT) ?: emptyList()
    }

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