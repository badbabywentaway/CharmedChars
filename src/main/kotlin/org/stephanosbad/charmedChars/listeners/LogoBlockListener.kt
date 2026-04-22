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
package org.stephanosbad.charmedChars.listeners

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.ShulkerBox
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.stephanosbad.charmedChars.CharmedChars

/**
 * Transforms a placed logo block into a filled shulker box when hit with a gold/pyrite
 * tool in The End dimension.
 *
 * The shulker box color matches the logo block color (cyan/magenta/yellow).
 * Contents are loaded from config (default: 4 ghast tears).
 *
 * @property plugin Reference to the main plugin instance
 */
class LogoBlockListener(
    private val plugin: CharmedChars
) : Listener {

    // Guard against processing the same block twice when both events fire together
    private val processingBlocks = mutableSetOf<String>()

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    fun onInteractLogoBlock(event: PlayerInteractEvent) {
        if (event.action != Action.LEFT_CLICK_BLOCK) return
        val block = event.clickedBlock ?: return
        val color = getLogoBlockColor(block) ?: return

        if (block.world.environment != World.Environment.THE_END) return
        if (!isValidTool(event.player.inventory.itemInMainHand)) return

        val key = blockKey(block)
        if (!processingBlocks.add(key)) return

        try {
            event.isCancelled = true
            transformToShulkerBox(block, color)
            event.player.sendMessage(
                Component.text("The logo block transforms into a shulker box!")
                    .color(NamedTextColor.GOLD)
            )
        } finally {
            processingBlocks.remove(key)
        }
    }

    // BlockDamageEvent for Nexo compatibility (noteblock updates disabled)
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    fun onDamageLogoBlock(event: BlockDamageEvent) {
        val block = event.block
        val color = getLogoBlockColor(block) ?: return

        if (block.world.environment != World.Environment.THE_END) return
        if (!isValidTool(event.player.inventory.itemInMainHand)) return

        val key = blockKey(block)
        if (!processingBlocks.add(key)) return

        try {
            event.isCancelled = true
            transformToShulkerBox(block, color)
            event.player.sendMessage(
                Component.text("The logo block transforms into a shulker box!")
                    .color(NamedTextColor.GOLD)
            )
        } finally {
            processingBlocks.remove(key)
        }
    }

    private fun transformToShulkerBox(block: org.bukkit.block.Block, color: String) {
        val shulkerMaterial = shulkerMaterialFor(color)

        val provider = plugin.customItemProviderManager.getProvider()
        if (provider != null) {
            provider.removeCustomBlock(block)
        } else {
            block.type = Material.AIR
        }

        block.type = shulkerMaterial

        val shulkerState = block.state as? ShulkerBox ?: return
        plugin.configManager.logoBlockShulkerContents().forEach { (material, quantity) ->
            shulkerState.inventory.addItem(ItemStack(material, quantity))
        }
        shulkerState.update()
    }

    /**
     * Returns the color name (cyan/magenta/yellow) if the block is a logo block, else null.
     */
    private fun getLogoBlockColor(block: org.bukkit.block.Block): String? {
        val provider = plugin.customItemProviderManager.getProvider() ?: return null
        val customBlock = provider.getCustomBlock(block) ?: return null
        // Namespaced ID format: "charmedchars:cyan_logo"
        val itemName = customBlock.namespacedId.substringAfter(":")
        if (!itemName.endsWith("_logo")) return null
        return itemName.removeSuffix("_logo")
    }

    private fun shulkerMaterialFor(color: String): Material = when (color) {
        "cyan"    -> Material.CYAN_SHULKER_BOX
        "magenta" -> Material.MAGENTA_SHULKER_BOX
        "yellow"  -> Material.YELLOW_SHULKER_BOX
        else      -> Material.SHULKER_BOX
    }

    private fun isValidTool(item: ItemStack): Boolean {
        if (item.itemMeta == null) return false
        if (item.containsEnchantment(Enchantment.SILK_TOUCH)) return false

        if (item.type.name.lowercase().contains("gold")) return true

        val provider = plugin.customItemProviderManager.getProvider()
        if (provider != null) {
            val customItem = provider.getCustomItem(item)
            if (customItem != null && customItem.namespacedId.lowercase().contains("pyrite")) return true
        }

        val displayName = item.itemMeta?.displayName() ?: return false
        val plain = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
            .serialize(displayName).lowercase()
        return plain.contains("gold") || plain.contains("pyrite")
    }

    private fun blockKey(block: org.bukkit.block.Block) =
        "${block.world.name}:${block.x}:${block.y}:${block.z}"
}
