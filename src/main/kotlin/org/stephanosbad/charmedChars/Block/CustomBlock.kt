package org.stephanosbad.charmedChars.Block

import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Item
import org.bukkit.inventory.ItemStack

public enum class TileColor {
    CYAN,
    MAGENTA,
    YELLOW
}

class CustomBlock(var id : String, var tileColor: TileColor ) {

    var itemStack = ItemStack(Material.PAPER)
    init{
        itemStack.itemMeta
    }

    companion object {
        fun byAlreadyPlaced(block: Block?): CustomBlock? {
            return CustomBlock(block?.blockData?.material?.name?:"")

        }

        fun getInstance(string: String): CustomBlock? {
            return CustomBlock(string)
        }

    }

}


