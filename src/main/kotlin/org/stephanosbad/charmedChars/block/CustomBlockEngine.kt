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
            if (block == null) {
                globalPlugin?.logger?.info("[CustomBlockEngine Debug] Block is null")
                return null
            }

            if (block.type != Material.NOTE_BLOCK) {
                globalPlugin?.logger?.info("[CustomBlockEngine Debug] Block is not a NOTE_BLOCK")
                return null
            }

            // Get the note block data to reverse-engineer the custom model data
            val noteBlockData = block.blockData as? org.bukkit.block.data.type.NoteBlock
            if (noteBlockData == null) {
                globalPlugin?.logger?.info("[CustomBlockEngine Debug] Could not get NoteBlock data")
                return null
            }

            val note = noteBlockData.note.id
            val instrument = noteBlockData.instrument
            val instrumentIndex = org.bukkit.Instrument.values().indexOf(instrument)

            globalPlugin?.logger?.info("[CustomBlockEngine Debug] Note: $note, Instrument: $instrument (index: $instrumentIndex)")

            // Reverse the mapping from BlockPlaceListener
            // Original: relativeValue = customModelData - 1100
            //          note = relativeValue % 25
            //          instrumentIndex = (relativeValue / 25) % Instrument.values().size
            // Reverse: relativeValue = instrumentIndex * 25 + note
            //          customModelData = relativeValue + 1100
            val relativeValue = instrumentIndex * 25 + note
            val customModelData = relativeValue + CYAN_OFFSET

            globalPlugin?.logger?.info("[CustomBlockEngine Debug] Calculated customModelData: $customModelData (relativeValue: $relativeValue)")

            val localBlockEngine = globalPlugin?.customBlockEngine
            if (localBlockEngine == null) {
                globalPlugin?.logger?.info("[CustomBlockEngine Debug] CustomBlockEngine is null")
                return null
            }

            // Determine color by custom model data range
            val color: BlockColor = when {
                customModelData >= YELLOW_OFFSET -> BlockColor.YELLOW
                customModelData >= MAGENTA_OFFSET -> BlockColor.MAGENTA
                customModelData >= CYAN_OFFSET -> BlockColor.CYAN
                else -> {
                    globalPlugin?.logger?.info("[CustomBlockEngine Debug] CustomModelData $customModelData is below CYAN_OFFSET ($CYAN_OFFSET)")
                    return null
                }
            }

            val baseVariation = customModelData - getColorOffset(color)
            globalPlugin?.logger?.info("[CustomBlockEngine Debug] Color: $color, baseVariation: $baseVariation")

            // Try to find letter block
            val letterBlock = LetterBlock.entries.firstOrNull { it.customVariation == baseVariation }
            if (letterBlock != null) {
                globalPlugin?.logger?.info("[CustomBlockEngine Debug] Found letter block: ${letterBlock.character}")
                return getInstance(color, letterBlock)
            }

            // Try to find number block
            val numberIndex = baseVariation - NUMBER_OFFSET
            if (numberIndex >= 0 && numberIndex < NumericBlock.entries.size) {
                val numberBlock = NumericBlock.entries.getOrNull(numberIndex)
                if (numberBlock != null) {
                    globalPlugin?.logger?.info("[CustomBlockEngine Debug] Found number block: ${numberBlock.c}")
                    return getInstance(color, numberBlock)
                }
            }

            // Try to find character block
            val charIndex = baseVariation - OPERATOR_OFFSET
            if (charIndex >= 0 && charIndex < NonAlphaNumBlocks.entries.size) {
                val charBlock = NonAlphaNumBlocks.entries.getOrNull(charIndex)
                if (charBlock != null) {
                    globalPlugin?.logger?.info("[CustomBlockEngine Debug] Found character block: ${charBlock.nonAlphaNumBlockName}")
                    return getInstance(color, charBlock)
                }
            }

            globalPlugin?.logger?.info("[CustomBlockEngine Debug] No matching block type found for baseVariation: $baseVariation")
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

