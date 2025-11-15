package org.stephanosbad.charmedChars.block

import org.stephanosbad.charmedChars.CharmedChars

/**
 * Custom blocks registry (currently unused - demo blocks removed)
 * Kept for potential future custom block implementations
 */
class CustomBlocks(private val plugin: CharmedChars) {

    fun registerCustomBlocks() {
        // Demo blocks (Magic Stone, Enchanted Log, Crystal Block) have been removed
        // This method is kept for potential future custom block implementations
        plugin.logger.info("Custom blocks system initialized (no blocks registered)")
    }
}
