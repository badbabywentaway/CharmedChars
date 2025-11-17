package org.stephanosbad.charmedChars.items


import dev.lone.itemsadder.api.CustomStack
import org.bukkit.inventory.ItemStack

enum class NonAlphaNumBlocks(val charVal: Char, blockName: String) {
    PLUS('+', "plus"),
    MINUS('-', "minus"),
    MULTIPLY('*', "multiply"),
    DIVISION('/', "division");

    private val _itemStacks: MutableMap<BlockColor, ItemStack?> by lazy {
        mutableMapOf<BlockColor, ItemStack?>().apply {
            for (color in BlockColor.entries) {
                val itemId = "charmedchars:${color.directoryName}_${this@NonAlphaNumBlocks.nonAlphaNumBlockName}"
                val customStack = CustomStack.getInstance(itemId)
                if (customStack == null) {
                    System.err.println("WARNING: ItemsAdder CustomStack.getInstance returned null for $itemId")
                }
                this[color] = customStack?.itemStack
            }
        }
    }

    val itemStacks: MutableMap<BlockColor, ItemStack?>
        get() = _itemStacks

    val nonAlphaNumBlockName = blockName
}