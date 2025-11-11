package org.stephanosbad.charmedChars.items

enum class BlockColor(val directoryName: String)
{
    CYAN("cyan"),
    MAGENTA("magenta"),
    YELLOW("yellow");

    companion object {
        fun getRand(): BlockColor {
            return entries[(Math.random() * entries.size).toInt()]
        }
    }
}