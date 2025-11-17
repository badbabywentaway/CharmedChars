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

class ConfigManager(private val plugin: CharmedChars) {

    private lateinit var config: FileConfiguration

    fun loadConfig() {
        plugin.saveDefaultConfig()
        plugin.reloadConfig()
        this.config = plugin.config
    }

    // Letter Block Drop Configuration
    val letterBlockDropChance: Double
        get() = config.getDouble("letter-blocks.drop-chance", 0.06)

    val lootingMultiplier1: Double
        get() = config.getDouble("letter-blocks.looting-multipliers.1", 1.67)

    val lootingMultiplier2: Double
        get() = config.getDouble("letter-blocks.looting-multipliers.2", 2.67)

    val lootingMultiplier3: Double
        get() = config.getDouble("letter-blocks.looting-multipliers.3", 3.33)

    // Protection Integration
    val worldGuardIntegration: Boolean
        get() = config.getBoolean("protection.worldguard-integration", true)

    val griefPreventionIntegration: Boolean
        get() = config.getBoolean("protection.griefprevention-integration", true)

    fun reloadConfig() {
        plugin.reloadConfig()
        this.config = plugin.config
    }

    fun saveConfig() {
        plugin.saveConfig()
    }
}