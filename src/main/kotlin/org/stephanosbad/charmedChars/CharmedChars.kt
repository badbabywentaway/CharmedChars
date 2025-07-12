package org.stephanosbad.charmedChars

import io.th0rgal.oraxen.OraxenPlugin
import io.th0rgal.oraxen.compatibilities.CompatibilitiesManager
import org.stephanosbad.charmedChars.Commands.CharBlock
import org.stephanosbad.charmedChars.Config.ConfigDataHandler
import org.stephanosbad.charmedChars.Items.ItemManager
import org.stephanosbad.charmedChars.Utility.FileUtils
import org.stephanosbad.charmedChars.Utility.WordDict
//import net.milkbowl.vault.economy.Economy
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.RegisteredServiceProvider
import org.bukkit.plugin.java.JavaPlugin

import org.bukkit.Bukkit
import java.io.File
import java.io.IOException
import java.util.*

class CharmedChars : JavaPlugin() {

    /**
     * Economy plugin
     */
    //var econ: Economy? = null

    /**
     * Is Vault economy available
     */
    var vaultEconomyEnabled: Boolean = false

    /**
     * Is Oraxen loaded (mandatory)
     */
    var oraxenLoaded: Boolean = false

    /**
     * Oraxen plugin
     */
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

        if ((OraxenPlugin.get()
                .also { oraxenPlugin = it }) != null
        ) {
            oraxenLoaded = true
            CompatibilitiesManager.addCompatibility("McLetterNumberBlocks", ItemManager::class.java)
            val oraxenFolder: File =
                OraxenPlugin.get().dataFolder
            try {
                FileUtils.copyResourcesRecursively(
                    Objects.requireNonNull(this.javaClass.getResource("/Oraxen")),
                    oraxenFolder
                )
            } catch (e: Exception) {
                e.printStackTrace()
            } catch (e: Error) {
                e.printStackTrace()
            }
        }

       /* if (setupEconomy()) {
            vaultEconomyEnabled = setupEconomy()
        }*/
        println("Vault " + (if (vaultEconomyEnabled) "confirmed." else "not available."))

        if (getCommand(CharBlock.CommandName) != null) {
            getCommand(CharBlock.CommandName)!!.setExecutor(CharBlock())
            getCommand(CharBlock.CommandName)!!.setTabCompleter(CharBlock())
        }
        Bukkit.getPluginManager().registerEvents(ItemManager(this), this)


    }

    override fun onDisable() {
        // Plugin shutdown logic
        // Plugin shutdown logic
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
}

