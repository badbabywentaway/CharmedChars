package org.stephanosbad.charmedChars

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.stephanosbad.charmedChars.Commands.CharBlock
import org.stephanosbad.charmedChars.Config.ConfigDataHandler
import org.stephanosbad.charmedChars.Items.ItemManager
import org.stephanosbad.charmedChars.Utility.FileUtils
import org.stephanosbad.charmedChars.Utility.WordDict
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.Bukkit
import org.stephanosbad.charmedChars.Block.CustomBlockListener
import org.stephanosbad.charmedChars.Block.CustomBlocks
import org.stephanosbad.charmedChars.Commands.BlocksCommand
import org.stephanosbad.charmedChars.Commands.ExampleCommand
import org.stephanosbad.charmedChars.Commands.ReloadCommand
import org.stephanosbad.charmedChars.Commands.TextureCommand
import org.stephanosbad.charmedChars.Utility.ConfigManager
import org.stephanosbad.charmedChars.graphics.TextureManager
import org.stephanosbad.charmedChars.listeners.ExampleListener
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.coroutines.CoroutineContext

class CharmedChars : JavaPlugin(), CoroutineScope {

    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    lateinit var configManager: ConfigManager
        private set

    lateinit var customBlocks: CustomBlocks
        private set

    lateinit var textureManager: TextureManager
        private set

    // In-memory storage for custom block data
    // In production, you should use a database or persistent file storage
    val customBlockData = mutableMapOf<String, String>()
    companion object {
        var oraxenPlugin: Plugin? = null

        fun getRecursive(directory: File): List<File?> {

            val retValue = mutableListOf<File?>()
            val faFiles: Array<File?>? = directory.listFiles()
            for (file in faFiles!!) {
                if (file!!.getName().matches("^(.*?)".toRegex())) {
                    retValue.add(file)
                }
                if (file.isDirectory()) {
                    retValue.addAll(
                        getRecursive(
                            file
                        )
                    )
                }
            }
            return retValue
        }
    }

    /**
     * Location of configuration data handler
     */
    var configDataHandler: ConfigDataHandler? = null

    override fun onEnable() {

        // Plugin startup logic
        println("Minecraft Letter/Number Block Plugin Starting")

        configDataHandler = ConfigDataHandler(this)
        try {
            configDataHandler!!.loadConfig()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        try {
            WordDict.init(this)
        } catch (e: IOException) {
            e.printStackTrace()
        }


//        var itemsAdderFolder = getPackUrl(true)
//            try {
//                FileUtils.copyResourcesRecursively(
//                    Objects.requireNonNull(this.javaClass.getResource("/Oraxen")),
//                    File(itemsAdderFolder!!)
//                )
//            } catch (e: Exception) {
//                e.printStackTrace()
//            } catch (e: Error) {
//                e.printStackTrace()
//            }


       /* if (setupEconomy()) {
            vaultEconomyEnabled = setupEconomy()
        }*/
        //println("Vault " + (if (vaultEconomyEnabled) "confirmed." else "not available."))

        // Initialize config manager
        configManager = ConfigManager(this)
        configManager.loadConfig()

        // Initialize texture manager
        textureManager = TextureManager(this)

        // Initialize custom blocks system
        customBlocks = CustomBlocks(this)
        customBlocks.registerCustomBlocks()

        // Initialize textures system
        if (configManager.customTexturesEnabled) {
            launch {
                delay(2000) // Wait for other systems to load
                textureManager.initialize()
            }
        }

        // Register commands
        getCommand("example")?.setExecutor(ExampleCommand(this))
        getCommand("reload")?.setExecutor(ReloadCommand(this))
        getCommand("blocks")?.setExecutor(BlocksCommand(this))
        getCommand("textures")?.setExecutor(TextureCommand(this))

        // Register event listeners
        server.pluginManager.registerEvents(ExampleListener(this), this)
        server.pluginManager.registerEvents(CustomBlockListener(this), this)

        // Async startup operations
        launch {
            delay(1000) // Simulate some startup work
            logger.info("Async startup completed!")
        }

        // Plugin startup logic using Paper's Adventure API
        logger.info("MyMinecraftPlugin v${description.version} has been enabled!")

        server.consoleSender.sendMessage(
            Component.text("Plugin loaded successfully! Built with Gradle + Kotlin")
                .color(NamedTextColor.GREEN)
        )

        if (configManager.customBlocksEnabled) {
            server.consoleSender.sendMessage(
                Component.text("Custom blocks system enabled!")
                    .color(NamedTextColor.AQUA)
            )
        }

        if (configManager.customTexturesEnabled) {
            server.consoleSender.sendMessage(
                Component.text("Custom textures system enabled!")
                    .color(NamedTextColor.LIGHT_PURPLE)
            )
        }

        if (getCommand(CharBlock.CommandName) != null) {
            getCommand(CharBlock.CommandName)!!.setExecutor(CharBlock())
            getCommand(CharBlock.CommandName)!!.tabCompleter = CharBlock()
        }
        Bukkit.getPluginManager().registerEvents(ItemManager(this), this)


    }

    override fun onDisable() {
        // Plugin shutdown logic
        // Plugin shutdown logic
        saveCustomBlockData()

        // Cancel all coroutines
        job.cancel()

        // Plugin shutdown logic
        logger.info("MyMinecraftPlugin has been disabled!")

        server.consoleSender.sendMessage(
            Component.text("Plugin unloaded successfully!")
                .color(NamedTextColor.YELLOW)
        )
        println("Minecraft Letter/Number Block Plugin Stopping")
    }

    /**
     * Set up economy plugin (Vault alone as of now)
     * @return Successfulness
     */
    /*private fun setupEconomy(): Boolean {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false
        }
        val rsp: RegisteredServiceProvider<Economy?>? =
            getServer().getServicesManager().getRegistration<Economy?>(Economy::class.java)
        if (rsp == null) {
            return false
        }
        econ = rsp.getProvider()
        return econ != null
    }*/

    fun reload() {
        launch {
            configManager.reloadConfig()

            // Reload custom blocks if needed
            if (configManager.customBlocksEnabled) {
                customBlocks.registerCustomBlocks()
            }

            // Reload textures if needed
            if (configManager.customTexturesEnabled) {
                textureManager.regenerateResourcePack()
            }

            logger.info("Plugin configuration reloaded!")

            server.consoleSender.sendMessage(
                Component.text("Configuration reloaded!")
                    .color(NamedTextColor.GREEN)
            )
        }
    }

    // Utility function for async operations
    fun runAsync(block: suspend CoroutineScope.() -> Unit) {
        launch(Dispatchers.IO) {
            block()
        }
    }

    // Custom block data persistence methods
    // In production, implement proper database/file storage
    private fun saveCustomBlockData() {
        if (customBlockData.isEmpty()) return

        logger.info("Saving ${customBlockData.size} custom block locations...")
        // TODO: Implement persistent storage (JSON file, database, etc.)
        // For now, data is lost on server restart
    }

    private fun loadCustomBlockData() {
        // TODO: Load custom block data from persistent storage
        logger.info("Loading custom block data...")
    }

}

