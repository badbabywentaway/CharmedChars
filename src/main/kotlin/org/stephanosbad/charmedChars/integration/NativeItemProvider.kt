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
package org.stephanosbad.charmedChars.integration

import de.tr7zw.changeme.nbtapi.NBT
import de.tr7zw.changeme.nbtapi.iface.ReadWriteItemNBT
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.CustomModelData
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack
import java.util.concurrent.ConcurrentHashMap

/**
 * Built-in fallback provider for servers without ItemsAdder, Oraxen, or Nexo.
 *
 * Items are identified by a custom NBT tag written via Item-NBT-API (MIT).
 * Placed blocks are tracked in-memory; NativePlacementListener maintains the registry.
 *
 * Custom model data values start at 1000 and are assigned sequentially by
 * NativeItemManagerSetup during onEnable, before any lazy item maps are accessed.
 */
class NativeItemProvider : CustomItemProvider {

    companion object {
        internal const val NBT_KEY = "charmedchars_item_id"
    }

    private val itemRegistry = mutableMapOf<String, ItemStack>()
    private val placedBlocks = ConcurrentHashMap<Location, CustomBlockInfo>()

    fun registerItem(namespacedId: String, customModelData: Int, displayName: Component) {
        val base = ItemStack(Material.PAPER).apply {
            editMeta { it.displayName(displayName) }
            setData(
                DataComponentTypes.CUSTOM_MODEL_DATA,
                CustomModelData.customModelData().addFloat(customModelData.toFloat()).build()
            )
        }
        // modify(ItemStack, Consumer) mutates in-place and returns void
        NBT.modify(base) { nbt: ReadWriteItemNBT -> nbt.setString(NBT_KEY, namespacedId) }
        itemRegistry[namespacedId] = base
    }

    fun registeredItemCount(): Int = itemRegistry.size

    override fun getItemStack(namespacedId: String): ItemStack? =
        itemRegistry[namespacedId]?.clone()

    override fun getCustomItem(itemStack: ItemStack): CustomItemInfo? {
        // readNbt returns ReadableNBT directly, avoiding Function/Consumer overload ambiguity
        val id = NBT.readNbt(itemStack).getString(NBT_KEY)?.takeIf { it.isNotEmpty() } ?: return null
        val parts = id.split(":", limit = 2)
        if (parts.size != 2) return null
        return CustomItemInfo(id, parts[0], parts[1])
    }

    override fun getCustomBlock(block: Block): CustomBlockInfo? =
        placedBlocks[block.location]

    override fun removeCustomBlock(block: Block): Boolean {
        val existed = placedBlocks.remove(block.location) != null
        block.type = Material.AIR
        return existed
    }

    override fun getProviderName(): String = "NativeItems"

    override fun isAvailable(): Boolean = true

    fun registerPlacedBlock(location: Location, namespacedId: String) {
        val parts = namespacedId.split(":", limit = 2)
        if (parts.size == 2) {
            placedBlocks[location] = CustomBlockInfo(namespacedId, parts[0], parts[1])
        }
    }

    fun unregisterPlacedBlock(location: Location) {
        placedBlocks.remove(location)
    }
}
