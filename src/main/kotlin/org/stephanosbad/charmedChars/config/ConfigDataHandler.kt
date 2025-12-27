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
package org.stephanosbad.charmedChars.config

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.stephanosbad.charmedChars.CharmedChars
import org.stephanosbad.charmedChars.database.StructureType
import org.stephanosbad.charmedChars.rewards.DropReward
import org.stephanosbad.charmedChars.utility.LocationPair
import java.io.File
import java.io.IOException

/**
 * Handler for reward and zone configuration data
 *
 * Manages config.yml which contains:
 * - Drop reward configurations (materials, multipliers, thresholds)
 * - Include/exclude zones for letter block mechanics
 * - Legacy VaultCurrency configuration (deprecated)
 *
 * This is separate from ConfigManager which handles plugin settings.
 *
 * @property plugin The CharmedChars plugin instance
 */
class ConfigDataHandler(private val plugin: CharmedChars) {
    /**
     * The loaded YAML configuration instance
     *
     * Contains reward configurations and zone definitions.
     */
    var configuration: YamlConfiguration? = null

    /**
     * The config.yml file handle
     */
    private var file: File? = null

    /**
     * Loads configuration from config.yml
     *
     * Ensures the default configuration file exists by calling saveDefaultConfig(),
     * then loads it. This uses the full default config from resources instead of
     * creating a minimal config programmatically.
     *
     * @throws IOException if file operations fail
     */
    @Throws(IOException::class)
    fun loadConfig() {
        if (file == null) {
            file = File(plugin.dataFolder, CONFIG_FILE_NAME)
        }

        // Use Bukkit's saveDefaultConfig to copy the full default config from resources
        // This only creates the file if it doesn't exist
        if (!file!!.exists()) {
            plugin.saveDefaultConfig()
        }

        configuration = YamlConfiguration.loadConfiguration(file!!)
    }

    /**
     * Creates a new default configuration file
     *
     * Deletes any existing file and creates a fresh config.yml with default
     * reward and zone configurations.
     *
     * @throws IOException if file operations fail
     */
    @Throws(IOException::class)
    private fun createBlank() {
        if (file!!.exists()) {
            file!!.delete()
        }
        if (!file!!.getParentFile().exists()) {
            file!!.getParentFile().mkdirs()
        }
        file!!.createNewFile()
        configuration = YamlConfiguration()

        writeToYaml()
    }

    /**
     * Writes default configuration values to config.yml
     *
     * Creates default configuration including:
     * - Example exclusion zone (spawn area)
     * - VaultCurrency configuration (legacy/deprecated)
     * - Drop reward configurations (iron ingots and gold nuggets)
     *
     * @throws IOException if file write operations fail
     */
    @Throws(IOException::class)
    fun writeToYaml() {
        val loc: LocationPair = sampleLocationPair()
        configuration!!.set("exclude.from", loc.first)
        configuration!!.set("exclude.to", loc.second)

        val vaultConfiguration = configuration!!.createSection("VaultCurrency")
        vaultConfiguration.set("minimumRewardCount", 0.0)
        vaultConfiguration.set("multiplier", 0.5)
        vaultConfiguration.set("minimumThreshold", 0.0)
        vaultConfiguration.set("maximumRewardCap", 2000.0)
        configuration!!.save(file!!)
        val dropReward = HashMap<String?, Any?>()
        dropReward.put("materialName", Material.IRON_INGOT.toString())
        dropReward.put("minimumRewardCount", 1.0)
        dropReward.put("multiplier", 0.01)
        dropReward.put("minimumThreshold", 100.0)
        dropReward.put("maximumRewardCap", 20.0)
        val dropReward1 = HashMap<String?, Any?>()
        dropReward1.put("materialName", Material.GOLD_NUGGET.toString())
        dropReward1.put("minimumRewardCount", 0.0)
        dropReward1.put("multiplier", 0.01)
        dropReward1.put("minimumThreshold", 200.0)
        dropReward1.put("maximumRewardCap", 50.0)
        val dropsConfiguration = listOf<MutableMap<String?, Any?>?>(dropReward, dropReward1)
        configuration!!.set("letter-blocks.Drop", dropsConfiguration)
        configuration!!.save(file!!)
    }

    /**
     * Creates a sample LocationPair for the default exclusion zone
     *
     * Returns a 20x20 block area centered at spawn (0,0) in the default world.
     * This serves as an example exclusion zone where letter blocks won't work.
     *
     * @return A LocationPair defining a small spawn protection zone
     */
    fun sampleLocationPair(): LocationPair {
        return LocationPair(
            Location(plugin.server.getWorld("world"), -10.0, 0.0, -10.0),
            Location(plugin.server.getWorld("world"), 10.0, 0.0, 10.0)
        )
    }

    /**
     * Loads number score rewards for a specific structure type
     *
     * Parses the number-score-rewards configuration section for the given structure type
     * and creates DropReward instances with the configured parameters.
     *
     * @param structureType The structure type (FORTRESS or BASTION_REMNANT)
     * @return List of DropReward instances for the structure
     */
    fun loadNumberScoreRewards(structureType: StructureType): List<DropReward> {
        val rewards = mutableListOf<DropReward>()

        if (configuration == null) {
            return rewards
        }

        val structureKey = when (structureType) {
            StructureType.FORTRESS -> "fortress"
            StructureType.BASTION_REMNANT -> "bastion"
        }

        val path = "number-score-rewards.$structureKey.Drop"
        val dropList = configuration!!.getList(path) ?: return rewards

        for (drop in dropList) {
            try {
                if (drop !is MutableMap<*, *>) {
                    continue
                }
                val dropParams = drop as MutableMap<*, *>
                val materialName = dropParams["materialName"] as? String ?: continue
                val minimumRewardCount = dropParams["minimumRewardCount"] as? Double ?: 0.0
                val multiplier = dropParams["multiplier"] as? Double ?: 0.0
                val minimumThreshold = dropParams["minimumThreshold"] as? Double ?: 0.0
                val maximumRewardCap = dropParams["maximumRewardCap"] as? Double ?: 0.0

                rewards.add(
                    DropReward(
                        materialName,
                        minimumRewardCount,
                        multiplier,
                        minimumThreshold,
                        maximumRewardCap
                    )
                )
            } catch (e: Exception) {
                plugin.logger.warning("Error loading number score reward for $structureType: ${e.message}")
            }
        }

        return rewards
    }

    companion object {
        /**
         * The name of the configuration file
         */
        var CONFIG_FILE_NAME: String = "config.yml"
    }
}
