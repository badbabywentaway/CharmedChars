package org.stephanosbad.charmedChars.Block

import org.bukkit.block.Block
import org.bukkit.entity.Item
import org.bukkit.inventory.ItemStack

class CustomBlock(var id : String) {


    companion object {
        fun byAlreadyPlaced(block: Block?): CustomBlock? {
            return CustomBlock(block?.blockData?.material?.name?:"")

        }

        fun getInstance(string: String): Item? {}

    }

}


