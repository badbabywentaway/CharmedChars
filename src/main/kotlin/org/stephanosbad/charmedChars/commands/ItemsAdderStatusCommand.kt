package org.stephanosbad.charmedChars.commands

import dev.lone.itemsadder.api.CustomStack
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.stephanosbad.charmedChars.items.BlockColor
import org.stephanosbad.charmedChars.items.LetterBlock

class ItemsAdderStatusCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        sender.sendMessage(
            Component.text("=== ItemsAdder Integration Status ===")
                .color(NamedTextColor.GOLD)
        )

        // Check if ItemsAdder is available
        var itemsAdderAvailable = false
        try {
            CustomStack.getInstance("test")
            itemsAdderAvailable = true
            sender.sendMessage(
                Component.text("✓ ItemsAdder API: Available")
                    .color(NamedTextColor.GREEN)
            )
        } catch (e: Exception) {
            sender.sendMessage(
                Component.text("✗ ItemsAdder API: Not available")
                    .color(NamedTextColor.RED)
            )
            sender.sendMessage(
                Component.text("  Error: ${e.message}")
                    .color(NamedTextColor.GRAY)
            )
        }

        if (!itemsAdderAvailable) {
            sender.sendMessage(
                Component.text("ItemsAdder is not loaded. Please install ItemsAdder!")
                    .color(NamedTextColor.RED)
            )
            return true
        }

        // Test a few sample blocks
        sender.sendMessage(
            Component.text("\nTesting sample blocks:")
                .color(NamedTextColor.YELLOW)
        )

        val testBlocks = listOf(
            "charmedchars:cyan_a",
            "charmedchars:magenta_e",
            "charmedchars:yellow_z",
            "charmedchars:cyan_0",
            "charmedchars:magenta_5",
            "charmedchars:yellow_plus"
        )

        var foundCount = 0
        var missingCount = 0

        for (blockId in testBlocks) {
            val customStack = CustomStack.getInstance(blockId)
            if (customStack != null) {
                foundCount++
                sender.sendMessage(
                    Component.text("  ✓ $blockId")
                        .color(NamedTextColor.GREEN)
                )
            } else {
                missingCount++
                sender.sendMessage(
                    Component.text("  ✗ $blockId - NOT FOUND")
                        .color(NamedTextColor.RED)
                )
            }
        }

        sender.sendMessage(
            Component.text("\nResults: $foundCount found, $missingCount missing")
                .color(if (missingCount == 0) NamedTextColor.GREEN else NamedTextColor.YELLOW)
        )

        // Check enum initialization
        sender.sendMessage(
            Component.text("\nChecking LetterBlock enum:")
                .color(NamedTextColor.YELLOW)
        )

        try {
            val testLetter = LetterBlock.A
            val testStack = testLetter.itemStacks[BlockColor.CYAN]

            if (testStack != null) {
                sender.sendMessage(
                    Component.text("  ✓ LetterBlock.A (CYAN) loaded successfully")
                        .color(NamedTextColor.GREEN)
                )
                sender.sendMessage(
                    Component.text("  Material: ${testStack.type}")
                        .color(NamedTextColor.GRAY)
                )
            } else {
                sender.sendMessage(
                    Component.text("  ✗ LetterBlock.A (CYAN) returned null ItemStack")
                        .color(NamedTextColor.RED)
                )
            }
        } catch (e: Exception) {
            sender.sendMessage(
                Component.text("  ✗ Error checking LetterBlock: ${e.message}")
                    .color(NamedTextColor.RED)
            )
        }

        if (missingCount > 0) {
            sender.sendMessage(
                Component.text("\n⚠ Setup Required:")
                    .color(NamedTextColor.YELLOW)
            )
            sender.sendMessage(
                Component.text("  1. Copy itemsadder-config/data/charmedchars/ to plugins/ItemsAdder/data/")
                    .color(NamedTextColor.GRAY)
            )
            sender.sendMessage(
                Component.text("  2. Copy textures to ItemsAdder resourcepack folder")
                    .color(NamedTextColor.GRAY)
            )
            sender.sendMessage(
                Component.text("  3. Run /iazip to generate and load items")
                    .color(NamedTextColor.GRAY)
            )
            sender.sendMessage(
                Component.text("  4. Restart the server")
                    .color(NamedTextColor.GRAY)
            )
            sender.sendMessage(
                Component.text("\nSee: itemsadder-config/README.md for detailed setup instructions")
                    .color(NamedTextColor.AQUA)
            )
        } else {
            sender.sendMessage(
                Component.text("\n✓ All test blocks loaded successfully!")
                    .color(NamedTextColor.GREEN)
            )
        }

        return true
    }
}
