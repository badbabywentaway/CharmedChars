package org.stephanosbad.charmedChars.items

import kotlin.random.Random

enum class BlockColor(val directoryName: String)
{
    CYAN("cyan"),
    MAGENTA("magenta"),
    YELLOW("yellow");

    companion object {
        fun getRand(): BlockColor {
            return entries.random()
        }
    }
}