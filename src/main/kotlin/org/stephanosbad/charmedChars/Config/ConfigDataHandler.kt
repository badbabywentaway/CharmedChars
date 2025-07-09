package org.stephanosbad.charmedChars.Config

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.stephanosbad.charmedChars.CharmedChars
import org.stephanosbad.charmedChars.Commands.CharBlock
import org.stephanosbad.charmedChars.Utility.LocationPair
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.List

class ConfigDataHandler(
    /**
     * parent plugin reference
     */
    private val plugin: CharmedChars
) {
    /**
     * configuration loaded from yaml file
     */
    var configuration: YamlConfiguration? = null

    /**
     * file from which to load configuration
     */
    private var file: File? = null

    /**
     * load config from file
     * @throws IOException - file or folder issues
     */

    @Throws(IOException::class)
    fun loadConfig() {
        if (file == null) {
            file = File(plugin.dataFolder, CONFIG_FILE_NAME)
            Bukkit.getLogger().info("File: " + file!!.getCanonicalPath())
        }

        if (!file!!.exists()) {
            createBlank()
        }
        configuration = YamlConfiguration.loadConfiguration(file!!)
        val defaultStream: InputStream? = plugin.getResource(CONFIG_FILE_NAME)
    }

    /**
     * Recreate default example file
     * @throws IOException - file or folder issue
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
     * Write default to yaml
     * @throws IOException - file or folder issue
     */
    @Throws(IOException::class)
    fun writeToYaml() {
        val loc: LocationPair = SampleLocationPair()
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
        configuration!!.set("Drop", dropsConfiguration)
        configuration!!.save(file!!)
    }

    fun SampleLocationPair(): LocationPair {
        return LocationPair(
            Location(plugin.server.getWorld("world"), -10.0, 0.0, -10.0),
            Location(plugin.server.getWorld("world"), 10.0, 0.0, 10.0)
        )
    }

    companion object {
        /**
         * hard coded config file name
         */
        var CONFIG_FILE_NAME: String = "config.yml"
    }
}
