/*
 * CharmedChars - A word-forming puzzle game for Minecraft
 * Copyright (C) 2025 StephanosBad
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. If not, see <https://www.gnu.org/licenses/>.
 */
package org.stephanosbad.charmedChars.integration

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.stephanosbad.charmedChars.CharmedChars
import org.stephanosbad.charmedChars.items.BlockColor
import org.stephanosbad.charmedChars.items.LetterBlock
import org.stephanosbad.charmedChars.items.NonAlphaNumBlocks
import org.stephanosbad.charmedChars.items.NumericBlock
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Registers all CharmedChars items into NativeItemProvider on startup and
 * generates the resource pack on /nativesetup.
 *
 * CMD values start at 1000, assigned sequentially by buildItemList():
 *   CYAN letters (1000-1025), digits (1026-1035), operators (1036-1039),
 *   MAGENTA (1040-1079), YELLOW (1080-1119).
 *
 * Both registerAllItems() and the resource pack JSON share buildItemList() so
 * the predicate values in paper.json always match the in-memory registry.
 */
class NativeItemManagerSetup(
    private val plugin: CharmedChars,
    private val provider: NativeItemProvider
) {

    private val packRoot = File(plugin.dataFolder, "native-pack")
    private val packMeta = File(packRoot, "pack.mcmeta")

    fun isAlreadySetup(): Boolean = provider.registeredItemCount() > 0

    fun isPackGenerated(): Boolean = packMeta.exists()

    fun setup() {
        if (isAlreadySetup()) return
        registerAllItems()
        plugin.logger.info("NativeItemManager: registered ${provider.registeredItemCount()} items")
    }

    /** Runs the full resource-pack generation. Called by /nativesetup. */
    fun autoSetup(force: Boolean = false): SetupResult {
        val messages = mutableListOf<String>()

        if (isPackGenerated() && !force) {
            return SetupResult(
                success = true,
                alreadySetup = true,
                messages = listOf(
                    "Native resource pack already exists at: ${packRoot.absolutePath}",
                    "To regenerate, run /nativesetup force"
                )
            )
        }

        if (force && packRoot.exists()) {
            messages.add("Force mode — deleting existing pack...")
            packRoot.deleteRecursively()
            messages.add("  Done")
        }

        return try {
            createDirectories()
            messages.add("Generating pack.mcmeta...")
            writeMcMeta()
            messages.add("  Done")

            messages.add("Generating paper.json overrides...")
            val overrideCount = writePaperOverrides()
            messages.add("  Done ($overrideCount overrides)")

            messages.add("Generating item model JSON files...")
            val itemModelCount = writeItemModels()
            messages.add("  Done ($itemModelCount files)")

            messages.add("Generating block model JSON files...")
            val blockModelCount = writeBlockModels()
            messages.add("  Done ($blockModelCount files)")

            messages.add("Copying textures...")
            val textureCount = copyTextures()
            messages.add("  Done ($textureCount files)")

            messages.add("Zipping pack...")
            val zipFile = zipPack()
            messages.add("  Done (${zipFile.name})")

            messages.add("Computing SHA-1 hash...")
            val hash = computePackHash(zipFile)
            messages.add("  Done")

            messages.add("")
            messages.add("=== Pack Ready! ===")
            messages.add("Zip: ${zipFile.absolutePath}")
            messages.add("Run /nativesetup again if you need to regenerate.")
            messages.add("")

            SetupResult(success = true, alreadySetup = false, messages = messages, zipFile = zipFile, packHash = hash)
        } catch (e: Exception) {
            messages.add("ERROR: ${e.message}")
            plugin.logger.severe("Failed to generate native resource pack: ${e.message}")
            e.printStackTrace()
            SetupResult(success = false, alreadySetup = false, messages = messages)
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Single source of truth for namespacedId → CMD mappings.
     * Order must never change without regenerating existing packs.
     */
    private fun buildItemList(): List<Pair<String, Int>> {
        val list = mutableListOf<Pair<String, Int>>()
        var cmd = 1000
        for (color in BlockColor.entries) {
            for (letter in LetterBlock.entries)
                list += "charmedchars:${color.directoryName}_${letter.character.lowercaseChar()}" to cmd++
            for (number in NumericBlock.entries)
                list += "charmedchars:${color.directoryName}_${number.c}" to cmd++
            for (op in NonAlphaNumBlocks.entries)
                list += "charmedchars:${color.directoryName}_${op.nonAlphaNumBlockName}" to cmd++
        }
        return list
    }

    private fun registerAllItems() {
        for ((namespacedId, cmd) in buildItemList()) {
            val (colorName, itemSuffix) = namespacedId.removePrefix("charmedchars:").split("_", limit = 2)
            val color = BlockColor.entries.first { it.directoryName == colorName }
            val textColor = when (color) {
                BlockColor.CYAN -> NamedTextColor.AQUA
                BlockColor.MAGENTA -> NamedTextColor.LIGHT_PURPLE
                BlockColor.YELLOW -> NamedTextColor.YELLOW
            }
            val label = "${colorName.replaceFirstChar { it.uppercase() }} $itemSuffix"
            provider.registerItem(namespacedId, cmd, Component.text(label).color(textColor))
        }
    }

    private fun createDirectories() {
        val colors = listOf("cyan", "magenta", "yellow")
        File(packRoot, "assets/minecraft/models/item").mkdirs()
        for (color in colors) {
            File(packRoot, "assets/charmedchars/models/item/$color").mkdirs()
            File(packRoot, "assets/charmedchars/models/block/$color").mkdirs()
            File(packRoot, "assets/charmedchars/textures/block/$color").mkdirs()
        }
    }

    private fun writeMcMeta() {
        packMeta.writeText("""
            {
              "pack": {
                "pack_format": 46,
                "description": "CharmedChars Native Item Pack"
              }
            }
        """.trimIndent())
    }

    private fun writePaperOverrides(): Int {
        val items = buildItemList()
        val overrides = items.joinToString(",\n    ") { (id, cmd) ->
            val modelPath = id.replace("charmedchars:", "charmedchars:item/").replace("_", "/", ignoreCase = false).let {
                // id format: charmedchars:cyan_a  → model path: charmedchars:item/cyan/a
                val bare = id.removePrefix("charmedchars:")         // "cyan_a"
                val underscore = bare.indexOf('_')
                val color = bare.substring(0, underscore)           // "cyan"
                val suffix = bare.substring(underscore + 1)         // "a"
                "charmedchars:item/$color/$suffix"
            }
            """{"predicate": {"custom_model_data": $cmd}, "model": "$modelPath"}"""
        }
        File(packRoot, "assets/minecraft/models/item/paper.json").writeText(
            """
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "minecraft:item/paper"
  },
  "overrides": [
    $overrides
  ]
}
            """.trimIndent()
        )
        return items.size
    }

    private fun writeItemModels(): Int {
        var count = 0
        for ((namespacedId, _) in buildItemList()) {
            val bare = namespacedId.removePrefix("charmedchars:")
            val color = bare.substringBefore('_')
            val suffix = bare.substringAfter('_')
            val file = File(packRoot, "assets/charmedchars/models/item/$color/$suffix.json")
            file.writeText("""{"parent": "charmedchars:block/$color/$suffix"}""")
            count++
        }
        return count
    }

    private fun writeBlockModels(): Int {
        var count = 0
        for ((namespacedId, _) in buildItemList()) {
            val bare = namespacedId.removePrefix("charmedchars:")
            val color = bare.substringBefore('_')
            val suffix = bare.substringAfter('_')
            val file = File(packRoot, "assets/charmedchars/models/block/$color/$suffix.json")
            file.writeText(
                """
{
  "parent": "minecraft:block/cube_all",
  "textures": {
    "all": "charmedchars:block/$color/$suffix"
  }
}
                """.trimIndent()
            )
            count++
        }
        return count
    }

    private fun copyTextures(): Int {
        var count = 0
        for ((namespacedId, _) in buildItemList()) {
            val bare = namespacedId.removePrefix("charmedchars:")
            val color = bare.substringBefore('_')
            val suffix = bare.substringAfter('_')
            val src = "pack-oraxen/assets/minecraft/textures/block/$color/$suffix.png"
            val dest = File(packRoot, "assets/charmedchars/textures/block/$color/$suffix.png")
            if (copyResource(src, dest)) count++
        }
        return count
    }

    private fun copyResource(resourcePath: String, dest: File): Boolean {
        val stream = plugin.getResource(resourcePath) ?: run {
            plugin.logger.warning("Texture not found in JAR: $resourcePath")
            return false
        }
        return try {
            stream.use { input -> FileOutputStream(dest).use { input.copyTo(it) } }
            true
        } catch (e: Exception) {
            plugin.logger.warning("Failed to copy $resourcePath: ${e.message}")
            false
        }
    }

    fun getZipFile(): File = File(plugin.dataFolder, "CharmedChars-native-pack.zip")

    private fun zipPack(): File {
        val zipFile = getZipFile()
        if (zipFile.exists()) zipFile.delete()
        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            packRoot.walkTopDown().filter { it.isFile }.forEach { file ->
                zos.putNextEntry(ZipEntry(file.relativeTo(packRoot).invariantSeparatorsPath))
                file.inputStream().use { it.copyTo(zos) }
                zos.closeEntry()
            }
        }
        return zipFile
    }

    fun computePackHash(zipFile: File): ByteArray {
        val digest = MessageDigest.getInstance("SHA-1")
        zipFile.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest()
    }

    data class SetupResult(
        val success: Boolean,
        val alreadySetup: Boolean,
        val messages: List<String>,
        val zipFile: File? = null,
        val packHash: ByteArray? = null
    )
}
