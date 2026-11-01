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
package org.stephanosbad.charmedchars.listeners

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.ShulkerBox
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.stephanosbad.charmedchars.CharmedChars
import org.stephanosbad.integrare.CustomBlockHitListener
import org.stephanosbad.integrare.matchesTool

/**
 * Transforms a placed logo block into a filled shulker box when hit with a gold/pyrite
 * tool in The End dimension.
 *
 * The shulker box color matches the logo block color (cyan/magenta/yellow).
 * Contents are loaded from config (default: 4 ghast tears).
 *
 * @property plugin Reference to the main plugin instance
 */
class LogoBlockTransform(private val plugin: CharmedChars) {

    companion object {
        /**
         * Registers the logo-block-to-shulker-box transform as a [Listener].
         *
         * Event-order handling (providers fire Interact/Damage/Break in different
         * orders, and even the same provider changes order between its own
         * versions) is delegated to [CustomBlockHitListener] rather than
         * reimplemented here.
         */
        fun listener(plugin: CharmedChars): Listener {
            val transform = LogoBlockTransform(plugin)
            val provider = plugin.customItemProviderManager.getProvider()
                ?: error("LogoBlockTransform.listener requires an initialized custom item provider")
            return CustomBlockHitListener(
                provider = provider,
                matches = { it.blockName.endsWith("_logo") },
                onHit = transform::tryTransform
            )
        }
    }

    /**
     * Transforms [block] if it is a logo block in The End hit with a valid tool, and
     * returns true so the caller cancels its event.
     */
    fun tryTransform(block: Block, player: Player): Boolean {
        val color = getLogoBlockColor(block) ?: return false

        if (block.world.environment != World.Environment.THE_END) return false
        if (!isValidTool(player.inventory.itemInMainHand)) return false

        transformToShulkerBox(block, color)
        player.sendMessage(
            Component.text("The logo block transforms into a shulker box!")
                .color(NamedTextColor.GOLD)
        )
        return true
    }

    private fun transformToShulkerBox(block: Block, color: String) {
        val shulkerMaterial = shulkerMaterialFor(color)

        val provider = plugin.customItemProviderManager.getProvider()
        if (provider != null) {
            provider.removeCustomBlock(block)
        } else {
            block.type = Material.AIR
        }

        // Tick+1: let the custom item provider finish its own block removal, then place the shulker.
        // Tick+2: populate the inventory once the tile entity is fully initialized.
        // Note: do NOT call shulkerState.update() after adding items — it writes the empty snapshot
        //       back over the live tile entity inventory, wiping the contents.
        plugin.server.scheduler.runTask(plugin) { _ ->
            block.type = shulkerMaterial
            plugin.server.scheduler.runTask(plugin) { _ ->
                val shulkerState = block.state as? ShulkerBox ?: return@runTask
                plugin.configManager.logoBlockShulkerContents().forEach { (material, quantity) ->
                    shulkerState.inventory.addItem(ItemStack(material, quantity))
                }
            }
        }
    }

    /**
     * Returns the color name (cyan/magenta/yellow) if the block is a logo block, else null.
     */
    private fun getLogoBlockColor(block: Block): String? {
        val provider = plugin.customItemProviderManager.getProvider() ?: return null
        val customBlock = provider.getCustomBlock(block) ?: return null
        val itemName = customBlock.blockName
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
        if (item.containsEnchantment(Enchantment.SILK_TOUCH)) return false
        return plugin.customItemProviderManager.getProvider().matchesTool(item, "gold", "pyrite")
    }
}
