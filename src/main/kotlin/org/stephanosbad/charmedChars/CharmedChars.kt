package org.stephanosbad.charmedChars

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.stephanosbad.charmedChars.commands.CharBlock
import org.stephanosbad.charmedChars.config.ConfigDataHandler
import org.stephanosbad.charmedChars.items.ItemManager
import org.stephanosbad.charmedChars.utility.WordDict
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.Bukkit
import org.stephanosbad.charmedChars.block.CustomBlockEngine
import org.stephanosbad.charmedChars.commands.ExampleCommand
import org.stephanosbad.charmedChars.commands.ReloadCommand
import org.stephanosbad.charmedChars.commands.TextureCommand
import org.stephanosbad.charmedChars.utility.ConfigManager
import org.stephanosbad.charmedChars.graphics.TextureManager
import org.stephanosbad.charmedChars.listeners.ExampleListener
import java.io.IOException
import kotlin.coroutines.CoroutineContext

class CharmedChars : JavaPlugin(), CoroutineScope {

    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    lateinit var configManager: ConfigManager
        private set

    lateinit var textureManager: TextureManager
        private set

    lateinit var customBlockEngine: CustomBlockEngine
        private set

    val isCustomBlockEngineInitialized: Boolean
        get() = ::customBlockEngine.isInitialized

    lateinit var resourcePackServer: org.stephanosbad.charmedChars.graphics.ResourcePackServer
        private set

    companion object {
//        var oraxenPlugin: Plugin? = null
//
//        fun getRecursive(directory: File): List<File?> {
//
//            val retValue = mutableListOf<File?>()
//            val faFiles: Array<File?>? = directory.listFiles()
//            for (file in faFiles!!) {
//                if (file!!.getName().matches("^(.*?)".toRegex())) {
//                    retValue.add(file)
//                }
//                if (file.isDirectory()) {
//                    retValue.addAll(
//                        getRecursive(
//                            file
//                        )
//                    )
//                }
//            }
//            return retValue
//        }
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

        // Initialize texture manager (constructor only, not initialized yet)
        textureManager = TextureManager(this)

        // IMPORTANT: CustomBlockEngine must be initialized BEFORE textureManager.initialize()
        // TextureManager depends on CustomBlockEngine for generating note_block.json
        customBlockEngine = CustomBlockEngine(this, 1100)

        // Initialize resource pack server
        resourcePackServer = org.stephanosbad.charmedChars.graphics.ResourcePackServer(this)

        // Initialize textures system
        // Note: Runs asynchronously to avoid blocking server startup, but CustomBlockEngine
        // is already initialized synchronously above, so no race condition exists
        if (configManager.customTexturesEnabled) {
            launch {
                textureManager.initialize()
                // Start HTTP server after resource pack is generated
                resourcePackServer.start()
            }
        }

        // Register commands
        getCommand("example")?.setExecutor(ExampleCommand(this))
        getCommand("reload")?.setExecutor(ReloadCommand(this))
        getCommand("textures")?.setExecutor(TextureCommand(this))

        // Register event listeners
        server.pluginManager.registerEvents(ExampleListener(this), this)
        server.pluginManager.registerEvents(org.stephanosbad.charmedChars.listeners.ResourcePackListener(this), this)

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
        // Stop HTTP server
        if (::resourcePackServer.isInitialized) {
            resourcePackServer.stop()
        }

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

    fun reload() {
        launch {
            configManager.reloadConfig()

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

}

