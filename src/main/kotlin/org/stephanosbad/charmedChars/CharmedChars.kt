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
import org.stephanosbad.charmedChars.commands.ProviderSetupCommand
import org.stephanosbad.charmedChars.commands.StructureCodeCommand
import org.stephanosbad.charmedChars.commands.StructureDatabaseCommand
import org.stephanosbad.charmedChars.commands.VersionCommand
import org.stephanosbad.charmedChars.commands.GlassingBedsCommand
import org.stephanosbad.charmedChars.integration.CharmedCharsCatalog
import org.stephanosbad.integrare.CustomItemProviderManager
import org.stephanosbad.integrare.ProviderKind
import org.stephanosbad.integrare.catalog.ItemCatalog
import org.stephanosbad.integrare.setup.NativeSetup
import org.stephanosbad.integrare.setup.ProviderSetup
import org.stephanosbad.integrare.nativeitems.NativePackListener
import org.stephanosbad.integrare.nativeitems.NativePackServer
import org.stephanosbad.integrare.nativeitems.NativePlacementListener
import org.stephanosbad.integrare.provider.NativeItemProvider
import org.stephanosbad.charmedChars.utility.ConfigManager
import org.stephanosbad.charmedChars.database.StructureDatabase
import org.stephanosbad.charmedChars.listeners.StructureListener
import org.stephanosbad.charmedChars.listeners.FortressNumberGameListener
import org.stephanosbad.charmedChars.listeners.BastionNumberGameListener
import org.stephanosbad.charmedChars.listeners.GlassingBedsListener
import org.stephanosbad.charmedChars.listeners.OperatorActivationListener
import org.stephanosbad.charmedChars.listeners.EnderDragonKillListener
import org.stephanosbad.charmedChars.listeners.LogoBlockListener
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
     * Every CharmedChars custom item and recipe, as installed into the active provider
     */
    lateinit var itemCatalog: ItemCatalog
        private set

    private var nativePackServer: NativePackServer? = null

    /**
     * Native provider setup for [itemCatalog]
     */
    fun nativeSetup(provider: NativeItemProvider) = NativeSetup(
        itemCatalog, provider, dataFolder, logger, configManager.nativeCmdBase,
        zipFileName = "CharmedChars-native-pack.zip",
        packDescription = "CharmedChars Native Item Pack"
    )

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
        itemCatalog = CharmedCharsCatalog.create(this)
        customItemProviderManager = CustomItemProviderManager(this, CharmedCharsCatalog.NATIVE_NBT_KEY)
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

        // Initialize config manager first - nativeSetup() below needs it
        configManager = ConfigManager(this)
        configManager.loadConfig()

        // Native provider registers items synchronously so lazy item maps resolve correctly.
        // Pack hosting is deferred until after this (it needs the port/hostname, set below).
        val nativeProvider = customItemProviderManager.getProvider() as? NativeItemProvider
        if (nativeProvider != null) {
            nativeSetup(nativeProvider).registerItems()
        }

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

        // Start native pack HTTP server now that configManager is ready
        if (nativeProvider != null) {
            val nativeSetup = nativeSetup(nativeProvider)
            val existingZip = nativeSetup.zipFile
            if (existingZip.exists()) {
                nativeProvider.packHash = nativeSetup.computePackHash(existingZip)
                startNativePackHosting(nativeProvider, existingZip)
            }
        }

        // Initialize structure database
        structureDatabase = StructureDatabase(this)
        structureDatabase.initialize()

        // Register commands
        getCommand("reload")?.setExecutor(ReloadCommand(this))
        getCommand("iastatus")?.setExecutor(ItemsAdderStatusCommand())
        for (kind in ProviderKind.entries) {
            getCommand(ProviderSetupCommand.commandFor(kind))?.setExecutor(ProviderSetupCommand(this, kind))
        }
        getCommand("version")?.setExecutor(VersionCommand(this))
        getCommand("structurecode")?.setExecutor(StructureCodeCommand(this))
        val structureDbCommand = StructureDatabaseCommand(this)
        getCommand("structuredb")?.setExecutor(structureDbCommand)
        getCommand("structuredb")?.tabCompleter = structureDbCommand
        val glassingBedsCommand = GlassingBedsCommand(this)
        getCommand("glassingbeds")?.setExecutor(glassingBedsCommand)
        getCommand("glassingbeds")?.tabCompleter = glassingBedsCommand

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
        if (nativeProvider != null) {
            Bukkit.getPluginManager().registerEvents(
                NativePlacementListener(this, nativeProvider) { itemCatalog.isBlock(it.namespacedId) }, this
            )
            Bukkit.getPluginManager().registerEvents(
                NativePackListener(nativeProvider) { configManager.nativeItemsPackRequired }, this
            )
        }
        Bukkit.getPluginManager().registerEvents(ItemManager(this), this)
        Bukkit.getPluginManager().registerEvents(StructureListener(this, structureDatabase), this)
        Bukkit.getPluginManager().registerEvents(FortressNumberGameListener(this, structureDatabase), this)
        Bukkit.getPluginManager().registerEvents(BastionNumberGameListener(this, structureDatabase), this)
        Bukkit.getPluginManager().registerEvents(GlassingBedsListener(this), this)
        Bukkit.getPluginManager().registerEvents(OperatorActivationListener(this), this)
        Bukkit.getPluginManager().registerEvents(EnderDragonKillListener(this), this)
        Bukkit.getPluginManager().registerEvents(LogoBlockListener(this), this)


    }

    /**
     * Called when the plugin is disabled
     *
     * Cancels all running coroutines and performs cleanup operations.
     */
    override fun onDisable() {
        // Stop native pack HTTP server if running
        nativePackServer?.stop()
        nativePackServer = null

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
    fun startNativePackHosting(provider: NativeItemProvider, zipFile: java.io.File) {
        val hostname = resolveNativeHostname()
        val port = configManager.nativeItemsHttpPort
        val url = "http://$hostname:$port/${zipFile.name}"

        provider.packUrl = url
        provider.packHash = provider.packHash  // hash already set by caller

        if (nativePackServer == null) {
            nativePackServer = NativePackServer(logger)
        }
        nativePackServer!!.start(zipFile, port)

        logger.info("Native pack URL: $url")
        logger.info("Players will receive the pack on join.")
    }

    private fun resolveNativeHostname(): String {
        val configured = configManager.nativeItemsHostname
        if (configured.isNotEmpty()) return configured
        val serverIp = server.ip
        if (serverIp.isNotEmpty()) return serverIp
        return try {
            java.net.InetAddress.getLocalHost().hostAddress
        } catch (e: Exception) {
            "localhost"
        }
    }

    /**
     * Setup for the active custom item provider, installing [itemCatalog]
     */
    fun providerSetup(): ProviderSetup? =
        customItemProviderManager.getProvider()?.let { ProviderSetup.forProvider(it, this, itemCatalog, ::nativeSetup) }

    private fun checkProviderSetup() {
        val setup = providerSetup()
        if (setup == null) {
            logger.warning("Unknown provider: ${customItemProviderManager.getProviderName()}")
            return
        }
        if (setup.kind == ProviderKind.NATIVE) {
            checkNativeNoteBlockUpdates()
            return
        }

        val providerName = customItemProviderManager.getProviderName()
        val command = ProviderSetupCommand.commandFor(setup.kind)
        if (setup.isAlreadySetup()) {
            logger.info("$providerName configuration found. Ready to use!")
            return
        }

        logger.warning("========================================")
        logger.warning("$providerName is installed but not configured!")
        logger.warning("")
        logger.warning("Run /$command to generate configs & copy textures, then:")
        setup.nextSteps.forEach { logger.warning("  - $it") }
        if (setup.serverRequirements.isNotEmpty()) {
            logger.warning("")
            logger.warning("IMPORTANT: required server settings")
            setup.serverRequirements.forEach { logger.warning("  $it") }
        }
        logger.warning("========================================")

        // Notify online ops
        server.scheduler.runTask(this, Runnable {
            server.onlinePlayers.filter { it.isOp }.forEach { player ->
                player.sendMessage(
                    Component.text("⚠ CharmedChars needs setup! Run /$command to auto-configure $providerName")
                        .color(NamedTextColor.GOLD)
                )
            }
        })
    }

    private fun checkNativeNoteBlockUpdates() {
        logger.info("NativeItemManager is active — no external plugin required.")

        // NativeItems uses note blocks as in-world block carriers. Minecraft normally
        // resets the instrument state (which drives the texture) whenever the block
        // beneath a note block changes. disable-noteblock-updates suppresses that and
        // is REQUIRED for stable letter-block textures in this mode.
        val paperConfig = java.io.File("config/paper-global.yml")
        val noteBlockUpdatesDisabled = if (paperConfig.exists()) {
            org.bukkit.configuration.file.YamlConfiguration
                .loadConfiguration(paperConfig)
                .getBoolean("block-updates.disable-noteblock-updates", false)
        } else false

        if (!noteBlockUpdatesDisabled) {
            logger.warning("========================================")
            logger.warning("ACTION REQUIRED: paper-global.yml setting missing!")
            logger.warning("")
            logger.warning("CharmedChars NativeItems mode uses note blocks as")
            logger.warning("in-world block carriers. Minecraft resets a note")
            logger.warning("block's instrument state (which selects the texture)")
            logger.warning("whenever the block beneath it changes. Without the")
            logger.warning("setting below, letter-block textures will change")
            logger.warning("intermittently based on whatever is placed below them.")
            logger.warning("")
            logger.warning("Edit config/paper-global.yml and set:")
            logger.warning("  block-updates:")
            logger.warning("    disable-noteblock-updates: true")
            logger.warning("")
            logger.warning("Then restart the server.")
            logger.warning("========================================")

            server.scheduler.runTask(this, Runnable {
                server.onlinePlayers.filter { it.isOp }.forEach { player ->
                    player.sendMessage(
                        Component.text(
                            "⚠ CharmedChars NativeItems: set disable-noteblock-updates: true " +
                            "in config/paper-global.yml and restart"
                        ).color(NamedTextColor.GOLD)
                    )
                }
            })
        } else {
            logger.info("disable-noteblock-updates: true confirmed — NativeItems textures will be stable.")
        }
    }

}

