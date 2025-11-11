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

    val welcomeMessage: String
        get() = config.getString("welcome-message") ?: "Welcome to the server!"

    val debugMode: Boolean
        get() = config.getBoolean("debug-mode", false)

    val blockedWords: List<String>
        get() = config.getStringList("blocked-words")

    val maxPlayersPerCommand: Int
        get() = config.getInt("max-players-per-command", 5)

    val asyncProcessingEnabled: Boolean
        get() = config.getBoolean("async-processing", true)

    val customBlocksEnabled: Boolean
        get() = config.getBoolean("custom-blocks.enabled", true)

    val customBlockHardness: Double
        get() = config.getDouble("custom-blocks.hardness", 2.0)

    val customTexturesEnabled: Boolean
        get() = config.getBoolean("custom-textures.enabled", true)

    val autoGenerateResourcePack: Boolean
        get() = config.getBoolean("custom-textures.auto-generate", true)

    val resourcePackVersion: Int
        get() = config.getInt("custom-textures.pack-format", 18)

    val textureResolution: Int
        get() = config.getInt("custom-textures.resolution", 16)

    fun reloadConfig() {
        plugin.reloadConfig()
        this.config = plugin.config
    }

    fun saveConfig() {
        plugin.saveConfig()
    }

    // Extension function for easier config access with type safety
    internal inline fun <reified T> getConfigValue(path: String, default: T): T {
        return when (T::class) {
            String::class -> (config.getString(path) ?: default) as T
            Int::class -> config.getInt(path, default as Int) as T
            Boolean::class -> config.getBoolean(path, default as Boolean) as T
            Double::class -> config.getDouble(path, default as Double) as T
            List::class -> (config.getList(path) ?: default) as T
            else -> default
        }
    }
}