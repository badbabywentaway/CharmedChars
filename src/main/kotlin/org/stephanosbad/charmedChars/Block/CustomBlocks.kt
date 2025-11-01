package org.stephanosbad.charmedChars.Block
// ============================================================================
// FILE: src/main/kotlin/com/yourname/myplugin/blocks/CustomBlocks.kt
// Block registry and factory for custom blocks with texture support
// ============================================================================

//package com.yourname.myplugin.blocks


//import net.kyori.adventure.text.Component
//import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.stephanosbad.charmedChars.CharmedChars
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.persistence.PersistentDataType

/**
 * Registry and factory for custom blocks with texture support
 */
class CustomBlocks(private val plugin: CharmedChars) {

    // Custom block identifiers
    companion object {
        const val MAGIC_STONE = "magic_stone"
        const val ENCHANTED_LOG = "enchanted_log"
        const val CRYSTAL_BLOCK = "crystal_block"
    }

    // Custom block keys for persistent data
    private val magicStoneKey = NamespacedKey(plugin, MAGIC_STONE)
    private val enchantedLogKey = NamespacedKey(plugin, ENCHANTED_LOG)
    private val crystalBlockKey = NamespacedKey(plugin, CRYSTAL_BLOCK)

    fun registerCustomBlocks() {
        if (!plugin.configManager.customBlocksEnabled) {
            plugin.logger.info("Custom blocks are disabled in config")
            return
        }

        registerMagicStone()
        registerEnchantedLog()
        registerCrystalBlock()

        plugin.logger.info("Custom blocks registered successfully!")
    }

    /**
     * Magic Stone - Glows and provides special effects
     */
    private fun registerMagicStone() {
        val recipe = ShapedRecipe(magicStoneKey, createMagicStone())
        recipe.shape(
            "SGS",
            "GDG",
            "SGS"
        )
        recipe.setIngredient('S', Material.STONE)
        recipe.setIngredient('G', Material.GLOWSTONE_DUST)
        recipe.setIngredient('D', Material.DIAMOND)

        plugin.server.addRecipe(recipe)
    }

    /**
     * Enchanted Log - Special wood with magical properties
     */
    private fun registerEnchantedLog() {
        val recipe = ShapedRecipe(enchantedLogKey, createEnchantedLog())
        recipe.shape(
            "LLL",
            "LBL",
            "LLL"
        )
        recipe.setIngredient('L', Material.OAK_LOG)
        recipe.setIngredient('B', Material.ENCHANTED_BOOK)

        plugin.server.addRecipe(recipe)
    }

    /**
     * Crystal Block - Rare decorative block
     */
    private fun registerCrystalBlock() {
        val recipe = ShapedRecipe(crystalBlockKey, createCrystalBlock())
        recipe.shape(
            "AQA",
            "QEQ",
            "AQA"
        )
        recipe.setIngredient('A', Material.AMETHYST_SHARD)
        recipe.setIngredient('Q', Material.QUARTZ)
        recipe.setIngredient('E', Material.EMERALD)

        plugin.server.addRecipe(recipe)
    }

    // Factory methods for creating custom block items with textures

    fun createMagicStone(): ItemStack {
        val item = ItemStack(Material.SMOOTH_STONE)
        val meta = item.itemMeta

        meta.displayName(
            Component.text("Magic Stone")
                .color(NamedTextColor.LIGHT_PURPLE)
        )

        meta.lore(listOf(
            Component.text("A mystical stone that glows with inner light")
                .color(NamedTextColor.GRAY),
            Component.text("Right-click to activate magical effects!")
                .color(NamedTextColor.YELLOW),
            Component.text("Custom texture available with resource pack")
                .color(NamedTextColor.DARK_PURPLE)
        ))

        // Mark as custom block
        meta.persistentDataContainer.set(magicStoneKey, PersistentDataType.STRING, MAGIC_STONE)

        // Add enchanted glow
        meta.addEnchant(Enchantment.UNBREAKING, 1, true)
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS)

        item.itemMeta = meta

        // Apply custom texture if enabled
        return if (plugin.configManager.customTexturesEnabled) {
            plugin.textureManager.applyCustomTexture(item, MAGIC_STONE)
        } else {
            item
        }
    }

    fun createEnchantedLog(): ItemStack {
        val item = ItemStack(Material.DARK_OAK_LOG)
        val meta = item.itemMeta

        meta.displayName(
            Component.text("Enchanted Log")
                .color(NamedTextColor.GREEN)
        )

        meta.lore(listOf(
            Component.text("Ancient wood infused with magical energy")
                .color(NamedTextColor.GRAY),
            Component.text("Grows faster when planted")
                .color(NamedTextColor.GREEN),
            Component.text("Provides bonus XP when harvested")
                .color(NamedTextColor.AQUA),
            Component.text("Custom texture available with resource pack")
                .color(NamedTextColor.DARK_GREEN)
        ))

        meta.persistentDataContainer.set(enchantedLogKey, PersistentDataType.STRING, ENCHANTED_LOG)
        meta.addEnchant(Enchantment.EFFICIENCY, 1, true)
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS)

        item.itemMeta = meta

        // Apply custom texture if enabled
        return if (plugin.configManager.customTexturesEnabled) {
            plugin.textureManager.applyCustomTexture(item, ENCHANTED_LOG)
        } else {
            item
        }
    }

    fun createCrystalBlock(): ItemStack {
        val item = ItemStack(Material.AMETHYST_BLOCK)
        val meta = item.itemMeta

        meta.displayName(
            Component.text("Crystal Block")
                .color(NamedTextColor.AQUA)
        )

        meta.lore(listOf(
            Component.text("A rare crystalline formation")
                .color(NamedTextColor.GRAY),
            Component.text("Amplifies nearby enchantments")
                .color(NamedTextColor.LIGHT_PURPLE),
            Component.text("Emits a soft, calming light")
                .color(NamedTextColor.WHITE),
            Component.text("Custom texture available with resource pack")
                .color(NamedTextColor.DARK_AQUA)
        ))

        meta.persistentDataContainer.set(crystalBlockKey, PersistentDataType.STRING, CRYSTAL_BLOCK)
        meta.addEnchant(Enchantment.FORTUNE, 1, true)
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS)

        item.itemMeta = meta

        // Apply custom texture if enabled
        return if (plugin.configManager.customTexturesEnabled) {
            plugin.textureManager.applyCustomTexture(item, CRYSTAL_BLOCK)
        } else {
            item
        }
    }

    // Utility methods

    fun isCustomBlock(item: ItemStack): Boolean {
        val meta = item.itemMeta ?: return false
        val container = meta.persistentDataContainer

        return container.has(magicStoneKey, PersistentDataType.STRING) ||
                container.has(enchantedLogKey, PersistentDataType.STRING) ||
                container.has(crystalBlockKey, PersistentDataType.STRING)
    }

    fun getCustomBlockType(item: ItemStack): String? {
        val meta = item.itemMeta ?: return null
        val container = meta.persistentDataContainer

        return when {
            container.has(magicStoneKey, PersistentDataType.STRING) -> MAGIC_STONE
            container.has(enchantedLogKey, PersistentDataType.STRING) -> ENCHANTED_LOG
            container.has(crystalBlockKey, PersistentDataType.STRING) -> CRYSTAL_BLOCK
            else -> null
        }
    }

    fun getCustomBlockItem(type: String): ItemStack? {
        return when (type.lowercase()) {
            MAGIC_STONE -> createMagicStone()
            ENCHANTED_LOG -> createEnchantedLog()
            CRYSTAL_BLOCK -> createCrystalBlock()
            else -> null
        }
    }
}


