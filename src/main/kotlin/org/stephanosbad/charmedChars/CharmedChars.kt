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
import org.stephanosbad.charmedChars.commands.ExampleCommand
import org.stephanosbad.charmedChars.commands.ReloadCommand
import org.stephanosbad.charmedChars.utility.ConfigManager
import org.stephanosbad.charmedChars.listeners.ExampleListener
import java.io.IOException
import kotlin.coroutines.CoroutineContext

class CharmedChars : JavaPlugin(), CoroutineScope {

    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    lateinit var configManager: ConfigManager
        private set

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

        // Initialize config manager
        configManager = ConfigManager(this)
        configManager.loadConfig()

        // Register commands
        getCommand("example")?.setExecutor(ExampleCommand(this))
        getCommand("reload")?.setExecutor(ReloadCommand(this))

        // Register event listeners
        server.pluginManager.registerEvents(ExampleListener(this), this)

        // Async startup operations
        launch {
            delay(1000) // Simulate some startup work
            logger.info("Async startup completed!")
        }

        // Plugin startup logic using Paper's Adventure API
        logger.info("CharmedChars v${description.version} has been enabled!")

        server.consoleSender.sendMessage(
            Component.text("CharmedChars loaded successfully with ItemsAdder integration!")
                .color(NamedTextColor.GREEN)
        )

        if (getCommand(CharBlock.CommandName) != null) {
            getCommand(CharBlock.CommandName)!!.setExecutor(CharBlock())
            getCommand(CharBlock.CommandName)!!.tabCompleter = CharBlock()
        }
        Bukkit.getPluginManager().registerEvents(ItemManager(this), this)


    }

    override fun onDisable() {
        // Cancel all coroutines
        job.cancel()

        // Plugin shutdown logic
        logger.info("CharmedChars has been disabled!")

        server.consoleSender.sendMessage(
            Component.text("CharmedChars unloaded successfully!")
                .color(NamedTextColor.YELLOW)
        )
        println("CharmedChars Plugin Stopping")
    }

    fun reload() {
        launch {
            configManager.reloadConfig()

            logger.info("CharmedChars configuration reloaded!")

            server.consoleSender.sendMessage(
                Component.text("CharmedChars configuration reloaded!")
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

