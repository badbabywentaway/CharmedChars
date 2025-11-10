package org.stephanosbad.charmedChars.Block

import kotlin.random.Random

enum class BlockColor(val directoryName: String)
{
    CYAN("cyan"),
    MAGENTA("magenta"),
    YELLOW("yellow");

    companion object {
        fun getRand(): BlockColor {
            return BlockColor.entries[(Math.random() * BlockColor.entries.size).toInt()]
        }
    }
}
