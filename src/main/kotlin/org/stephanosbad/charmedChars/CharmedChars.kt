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
import org.stephanosbad.charmedChars.commands.ItemsAdderStatusCommand
import org.stephanosbad.charmedChars.commands.ReloadCommand
import org.stephanosbad.charmedChars.commands.SetupItemsAdderCommand
import org.stephanosbad.charmedChars.commands.SetupOraxenCommand
import org.stephanosbad.charmedChars.commands.SetupNexoCommand
import org.stephanosbad.charmedChars.commands.StructureCodeCommand
import org.stephanosbad.charmedChars.commands.StructureDatabaseCommand
import org.stephanosbad.charmedChars.commands.VersionCommand
import org.stephanosbad.charmedChars.integration.ItemsAdderSetup
import org.stephanosbad.charmedChars.integration.CustomItemProviderManager
import org.stephanosbad.charmedChars.utility.ConfigManager
import org.stephanosbad.charmedChars.database.StructureDatabase
import org.stephanosbad.charmedChars.listeners.StructureListener
import org.stephanosbad.charmedChars.listeners.FortressNumberGameListener
import org.stephanosbad.charmedChars.listeners.BastionNumberGameListener
import java.io.IOException
import kotlin.coroutines.CoroutineContext

/**
 * CharmedChars - Main plugin class for the word-forming puzzle game
 *
 * This plugin allows players to collect letter blocks by mining logs with gold tools,
 * arrange them into words, and earn rewards based on word scores. It integrates with
 * ItemsAdder for custom blocks and supports optional protection plugins.
 *
 * @property configManager Configuration manager for accessing plugin settings
 * @property configDataHandler Handler for configuration data and rewards
 */
class CharmedChars : JavaPlugin(), CoroutineScope {

    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    /**
     * Configuration manager for accessing plugin settings
     */
    lateinit var configManager: ConfigManager
        private set

    /**
     * Configuration data handler for rewards and gameplay settings
     */
    var configDataHandler: ConfigDataHandler? = null

    /**
     * Database manager for structure tracking
     */
    lateinit var structureDatabase: StructureDatabase
        private set

    /**
     * Custom item provider manager (ItemsAdder or Oraxen)
     */
    lateinit var customItemProviderManager: CustomItemProviderManager
        private set

    /**
     * Called when the plugin is enabled
     *
     * Initializes configuration, registers commands and event listeners,
     * loads the word dictionary, and checks ItemsAdder setup status.
     */
    override fun onEnable() {

        // Plugin startup logic
        println("Minecraft Letter/Number Block Plugin Starting")

        // Initialize custom item provider (must be first!)
        customItemProviderManager = CustomItemProviderManager(this)
        val providerResult = customItemProviderManager.initialize()

        if (!providerResult.success) {
            logger.severe("========================================")
            logger.severe("FATAL: Failed to initialize custom item provider!")
            providerResult.messages.forEach { logger.severe(it) }
            logger.severe("Plugin will be disabled.")
            logger.severe("========================================")
            server.pluginManager.disablePlugin(this)
            return
        }

        logger.info("Custom item provider initialized: ${customItemProviderManager.getProviderName()}")

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

        // Initialize structure database
        structureDatabase = StructureDatabase(this)
        structureDatabase.initialize()

        // Register commands
        getCommand("reload")?.setExecutor(ReloadCommand(this))
        getCommand("iastatus")?.setExecutor(ItemsAdderStatusCommand())
        getCommand("iasetup")?.setExecutor(SetupItemsAdderCommand(this))
        getCommand("oraxensetup")?.setExecutor(SetupOraxenCommand(this))
        getCommand("nexosetup")?.setExecutor(SetupNexoCommand(this))
        getCommand("version")?.setExecutor(VersionCommand(this))
        getCommand("structurecode")?.setExecutor(StructureCodeCommand(this))
        val structureDbCommand = StructureDatabaseCommand(this)
        getCommand("structuredb")?.setExecutor(structureDbCommand)
        getCommand("structuredb")?.tabCompleter = structureDbCommand

        // Async startup operations
        launch {
            delay(1000) // Simulate some startup work
            logger.info("Async startup completed!")
        }

        // Plugin startup logic using Paper's Adventure API
        logger.info("CharmedChars v${description.version} has been enabled!")

        server.consoleSender.sendMessage(
            Component.text("CharmedChars loaded successfully with ${customItemProviderManager.getProviderName()} integration!")
                .color(NamedTextColor.GREEN)
        )

        // Check provider setup status
        launch {
            delay(2000) // Wait for provider to fully load
            checkProviderSetup()
        }

        if (getCommand(CharBlock.CommandName) != null) {
            getCommand(CharBlock.CommandName)!!.setExecutor(CharBlock())
            getCommand(CharBlock.CommandName)!!.tabCompleter = CharBlock()
        }

        // Register event listeners
        Bukkit.getPluginManager().registerEvents(ItemManager(this), this)
        Bukkit.getPluginManager().registerEvents(StructureListener(this, structureDatabase), this)
        Bukkit.getPluginManager().registerEvents(FortressNumberGameListener(this, structureDatabase), this)
        Bukkit.getPluginManager().registerEvents(BastionNumberGameListener(this, structureDatabase), this)


    }

    /**
     * Called when the plugin is disabled
     *
     * Cancels all running coroutines and performs cleanup operations.
     */
    override fun onDisable() {
        // Close database connection
        if (::structureDatabase.isInitialized) {
            structureDatabase.close()
        }

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

    /**
     * Reloads the plugin configuration
     *
     * Asynchronously reloads the configuration from config.yml and notifies
     * the command sender of the result.
     */
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

    /**
     * Checks if the custom item provider is properly set up and notifies administrators
     *
     * Verifies that the active provider (ItemsAdder or Oraxen) is configured with CharmedChars
     * custom blocks. If not configured, sends warnings to the console and notifies
     * online operators with setup instructions.
     */
    private fun checkProviderSetup() {
        val providerName = customItemProviderManager.getProviderName()

        when (providerName) {
            "ItemsAdder" -> {
                val setup = ItemsAdderSetup(this)

                if (!setup.isAlreadySetup()) {
                    logger.warning("========================================")
                    logger.warning("ItemsAdder is installed but not configured!")
                    logger.warning("")
                    logger.warning("Run one of these commands to setup:")
                    logger.warning("  /iasetup     - Auto-copy configs & textures")
                    logger.warning("  OR manually copy files (see docs)")
                    logger.warning("")
                    logger.warning("Then run /iazip and restart the server.")
                    logger.warning("========================================")

                    // Notify online ops
                    server.scheduler.runTask(this, Runnable {
                        server.onlinePlayers.filter { it.isOp }.forEach { player ->
                            player.sendMessage(
                                Component.text("⚠ CharmedChars needs setup! Run /iasetup to auto-configure ItemsAdder")
                                    .color(NamedTextColor.GOLD)
                            )
                        }
                    })
                } else {
                    logger.info("ItemsAdder configuration found. Ready to use!")
                }
            }

            "Oraxen" -> {
                val setup = org.stephanosbad.charmedChars.integration.OraxenSetup(this)

                if (!setup.isAlreadySetup()) {
                    logger.warning("========================================")
                    logger.warning("Oraxen is installed but not configured!")
                    logger.warning("")
                    logger.warning("Run this command to setup:")
                    logger.warning("  /oraxensetup - Auto-generate configs & copy textures")
                    logger.warning("")
                    logger.warning("Then run /oraxen reload all and restart the server.")
                    logger.warning("========================================")

                    // Notify online ops
                    server.scheduler.runTask(this, Runnable {
                        server.onlinePlayers.filter { it.isOp }.forEach { player ->
                            player.sendMessage(
                                Component.text("⚠ CharmedChars needs setup! Run /oraxensetup to auto-configure Oraxen")
                                    .color(NamedTextColor.GOLD)
                            )
                        }
                    })
                } else {
                    logger.info("Oraxen configuration found. Ready to use!")
                }
            }

            else -> {
                logger.warning("Unknown provider: $providerName")
            }
        }
    }

}

