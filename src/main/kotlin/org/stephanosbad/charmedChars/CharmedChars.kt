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
import org.stephanosbad.charmedChars.commands.VersionCommand
import org.stephanosbad.charmedChars.integration.ItemsAdderSetup
import org.stephanosbad.charmedChars.utility.ConfigManager
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
        getCommand("reload")?.setExecutor(ReloadCommand(this))
        getCommand("iastatus")?.setExecutor(ItemsAdderStatusCommand())
        getCommand("iasetup")?.setExecutor(SetupItemsAdderCommand(this))
        getCommand("version")?.setExecutor(VersionCommand(this))

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

        // Check ItemsAdder setup status
        launch {
            delay(2000) // Wait for ItemsAdder to fully load
            checkItemsAdderSetup()
        }

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

    /**
     * Check if ItemsAdder is set up and notify admins if not
     */
    private fun checkItemsAdderSetup() {
        val setup = ItemsAdderSetup(this)

        if (!setup.isItemsAdderAvailable()) {
            logger.warning("========================================")
            logger.warning("ItemsAdder plugin not found!")
            logger.warning("CharmedChars requires ItemsAdder to function.")
            logger.warning("Please install ItemsAdder from SpigotMC.")
            logger.warning("========================================")
            return
        }

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

}

