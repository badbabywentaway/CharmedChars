package org.stephanosbad.charmedChars.block

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack
import org.stephanosbad.charmedChars.CharmedChars
import org.stephanosbad.charmedChars.items.BlockColor
import org.stephanosbad.charmedChars.items.LetterBlock
import org.stephanosbad.charmedChars.items.NonAlphaNumBlocks
import org.stephanosbad.charmedChars.items.NumericBlock


class CustomBlockEngine(private val plugin: CharmedChars, var initialBlockCode: Int) {

    init{
        globalPlugin = plugin
    }

    val letterBlockKeys = buildMap<Pair<BlockColor, LetterBlock>, Pair<NamespacedKey, Int>> {
        BlockColor.entries.forEach { color ->
            LetterBlock.entries.forEach { letter ->
                val customModelData = getColorOffset(color) + letter.customVariation
                put(
                    Pair(color, letter),
                    Pair(NamespacedKey(plugin, "${color.directoryName}_${letter.name.lowercase()}"), customModelData)
                )
            }
        }
    }

    val numberBlockKeys = buildMap<Pair<BlockColor, NumericBlock>, Pair<NamespacedKey, Int>> {
        var numberIndex = 0
        BlockColor.entries.forEach { color ->
            NumericBlock.entries.forEach { number ->
                val customModelData = getColorOffset(color) + NUMBER_OFFSET + numberIndex
                put(
                    Pair(color, number),
                    Pair(NamespacedKey(plugin, "${color.directoryName}_${number.c}"), customModelData)
                )
                numberIndex++
            }
            numberIndex = 0  // Reset for next color
        }
    }

    val characterBlockKeys = buildMap<Pair<BlockColor, NonAlphaNumBlocks>, Pair<NamespacedKey, Int>> {
        var charIndex = 0
        BlockColor.entries.forEach { color ->
            NonAlphaNumBlocks.entries.forEach { char ->
                val customModelData = getColorOffset(color) + OPERATOR_OFFSET + charIndex
                put(
                    Pair(color, char),
                    Pair(NamespacedKey(plugin, "${color.directoryName}_${char.nonAlphaNumBlockName}"), customModelData)
                )
                charIndex++
            }
            charIndex = 0  // Reset for next color
        }
    }

    companion object {
        // Color offsets for custom model data
        const val CYAN_OFFSET = 1100
        const val MAGENTA_OFFSET = 1200
        const val YELLOW_OFFSET = 1300
        const val NUMBER_OFFSET = 300  // Numbers: offset + 300 + index (0-9)
        const val OPERATOR_OFFSET = 400  // Operators: offset + 400 + index

        internal var globalPlugin : CharmedChars? = null

        fun getColorOffset(color: BlockColor): Int {
            return when(color) {
                BlockColor.CYAN -> CYAN_OFFSET
                BlockColor.MAGENTA -> MAGENTA_OFFSET
                BlockColor.YELLOW -> YELLOW_OFFSET
            }
        }

        fun byAlreadyPlaced(block: Block?): CustomBlock? {

            var meta = block?.drops?.firstOrNull()?.itemMeta
            var localBlockEngine = globalPlugin?.customBlockEngine

            if(meta != null && localBlockEngine != null && meta.hasCustomModelData())
            {
                val customModelData = meta.customModelData

                // Determine color by custom model data range
                val color: BlockColor = when {
                    customModelData >= YELLOW_OFFSET -> BlockColor.YELLOW
                    customModelData >= MAGENTA_OFFSET -> BlockColor.MAGENTA
                    customModelData >= CYAN_OFFSET -> BlockColor.CYAN
                    else -> return null
                }

                val baseVariation = customModelData - getColorOffset(color)

                // Try to find letter block
                val letterBlock = LetterBlock.entries.firstOrNull { it.customVariation == baseVariation }
                if (letterBlock != null) {
                    return getInstance(color, letterBlock)
                }

                return null
            }
            return null
        }

        fun getInstance(color: BlockColor, letterBlock: LetterBlock): CustomBlock? {
            var letterBlockKey = globalPlugin?.customBlockEngine?.letterBlockKeys[Pair(color, letterBlock )]

            letterBlockKey?.let{
                val item = ItemStack(Material.NOTE_BLOCK)
                val meta = item.itemMeta

                meta.displayName(
                    Component.text("${color.name} ${letterBlock.character} Block")
                        .color(when(color){
                            BlockColor.CYAN -> NamedTextColor.AQUA
                            BlockColor.YELLOW -> NamedTextColor.YELLOW
                            BlockColor.MAGENTA -> NamedTextColor.LIGHT_PURPLE
                        })
                )
                meta.setCustomModelData(letterBlockKey.second)
                item.itemMeta = meta

                return CustomBlock(letterBlock, item)
            }

            return null
        }

        fun getInstance(color: BlockColor, nonAlphaNumeric: NonAlphaNumBlocks): CustomBlock?{
            var letterBlockKey = globalPlugin?.customBlockEngine?.characterBlockKeys[Pair(color, nonAlphaNumeric )]
            letterBlockKey?.let {
                val item = ItemStack(Material.NOTE_BLOCK)
                val meta = item.itemMeta

                meta.displayName(
                    Component.text("${color.name} ${nonAlphaNumeric.nonAlphaNumBlockName} Block")
                        .color(
                            when (color) {
                                BlockColor.CYAN -> NamedTextColor.AQUA
                                BlockColor.YELLOW -> NamedTextColor.YELLOW
                                BlockColor.MAGENTA -> NamedTextColor.LIGHT_PURPLE
                            }
                        )
                )
                meta.setCustomModelData(letterBlockKey.second)
                item.itemMeta = meta

                return CustomBlock(nonAlphaNumeric, item)
            }
            return null
        }

        fun getInstance(color: BlockColor, numericBlock: NumericBlock): CustomBlock?{
            var numberBlockKey = globalPlugin?.customBlockEngine?.numberBlockKeys[Pair(color, numericBlock )]
            numberBlockKey?.let {
                val item = ItemStack(Material.NOTE_BLOCK)
                val meta = item.itemMeta

                meta.displayName(
                    Component.text("${color.name} ${numericBlock.c} Block")
                        .color(
                            when (color) {
                                BlockColor.CYAN -> NamedTextColor.AQUA
                                BlockColor.YELLOW -> NamedTextColor.YELLOW
                                BlockColor.MAGENTA -> NamedTextColor.LIGHT_PURPLE
                            }
                        )
                )
                meta.setCustomModelData(numberBlockKey.second)
                item.itemMeta = meta

                return CustomBlock(numericBlock, item)
            }
            return null
        }


    }

}

