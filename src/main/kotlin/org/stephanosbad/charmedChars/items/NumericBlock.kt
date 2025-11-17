package org.stephanosbad.charmedChars.items

import dev.lone.itemsadder.api.CustomStack
import org.bukkit.inventory.ItemStack

enum class NumericBlock(val c: Char) {
    BLOCK_0('0'),
    BLOCK_1('1'),
    BLOCK_2('2'),
    BLOCK_3('3'),
    BLOCK_4('4'),
    BLOCK_5('5'),
    BLOCK_6('6'),
    BLOCK_7('7'),
    BLOCK_8('8'),
    BLOCK_9('9'),
    ;

    private val _itemStacks: MutableMap<BlockColor, ItemStack?> by lazy {
        mutableMapOf<BlockColor, ItemStack?>().apply {
            for (color in BlockColor.entries) {
                val itemId = "charmedchars:${color.directoryName}_${this@NumericBlock.c}"
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
}