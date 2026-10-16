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

import io.mockk.every
import io.mockk.mockk
import org.bukkit.Material
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.stephanosbad.integrare.catalog.ItemCatalog
import org.stephanosbad.integrare.provider.NativeItemProvider
import org.stephanosbad.integrare.setup.ItemsAdderSetup
import org.stephanosbad.integrare.setup.NativeSetup
import org.stephanosbad.integrare.setup.NexoSetup
import org.stephanosbad.integrare.setup.OraxenSetup
import org.stephanosbad.integrare.setup.SetupResult
import java.io.File
import java.security.MessageDigest
import java.util.logging.Logger
import java.util.zip.ZipFile

/**
 * Golden-file snapshots of everything the four provider setups write to disk
 * for the CharmedChars catalog.
 *
 * Locks the output (file layout, config/model content, texture bytes,
 * custom_variation / CMD / note block numbering, and result messages) so any
 * change to what servers receive shows up as a reviewable snapshot diff.
 *
 * Snapshots live in src/test/golden/. To regenerate after an intended change:
 *   ./gradlew test --tests '*SetupGoldenTest' -PupdateGolden
 * then review the diff before committing.
 */
class SetupGoldenTest {

    @TempDir
    lateinit var tmp: File

    private val pluginsDir get() = File(tmp, "plugins")
    private val charmedCharsDataFolder get() = File(pluginsDir, "CharmedChars")
    private val logger = Logger.getLogger("SetupGoldenTest")

    // Tests run against MockBukkit's 1.20 API, which has no POTENT_SULFUR. On MC 26.3
    // it exists, so resolve it like a 26.3 server would; setups only null-check it.
    private val resolveMaterial: (String) -> Material? = { name ->
        if (name == "POTENT_SULFUR") Material.STONE else Material.matchMaterial(name)
    }

    private val catalog: ItemCatalog by lazy { CharmedCharsCatalog.create(jarResources()) }

    // ── Scenarios ─────────────────────────────────────────────────────────────

    @Test
    fun `ItemsAdder fresh install`() {
        val result = ItemsAdderSetup(catalog, File(pluginsDir, "ItemsAdder"), logger, "CharmedChars", resolveMaterial).install()
        verifySnapshot("itemsadder-fresh", File(pluginsDir, "ItemsAdder"), result.summary())
    }

    @Test
    fun `ItemsAdder install into existing items_packs yml`() {
        File(pluginsDir, "ItemsAdder/data/items_packs.yml").seed(
            "packs:\n  otherpack:\n    enabled: true\n"
        )
        val result = ItemsAdderSetup(catalog, File(pluginsDir, "ItemsAdder"), logger, "CharmedChars", resolveMaterial).install()
        verifySnapshot("itemsadder-existing-items-packs", File(pluginsDir, "ItemsAdder"), result.summary())
    }

    @Test
    fun `Oraxen fresh install`() {
        val result = OraxenSetup(catalog, File(pluginsDir, "Oraxen"), logger, "CharmedChars", resolveMaterial).install()
        verifySnapshot("oraxen-fresh", File(pluginsDir, "Oraxen"), result.summary())
    }

    @Test
    fun `Oraxen install appends to existing recipe files`() {
        File(pluginsDir, "Oraxen/recipes/shaped.yml").seed(
            "user_shaped:\n  shape:\n    - AAA\n  result:\n    minecraft_type: STONE\n"
        )
        File(pluginsDir, "Oraxen/recipes/shapeless.yml").seed(
            "user_shapeless:\n  result:\n    minecraft_type: STONE\n"
        )
        val result = OraxenSetup(catalog, File(pluginsDir, "Oraxen"), logger, "CharmedChars", resolveMaterial).install()
        verifySnapshot("oraxen-existing-recipes", File(pluginsDir, "Oraxen"), result.summary())
    }

    @Test
    fun `Nexo fresh install`() {
        val result = NexoSetup(catalog, File(pluginsDir, "Nexo"), logger, "CharmedChars", resolveMaterial).install()
        verifySnapshot("nexo-fresh", File(pluginsDir, "Nexo"), result.summary())
    }

    @Test
    fun `Native pack generation`() {
        val setup = NativeSetup(
            catalog, NativeItemProvider(), charmedCharsDataFolder, logger,
            cmdBase = 1000, "CharmedChars-native-pack.zip", "CharmedChars Native Item Pack", resolveMaterial
        )
        val result = setup.generatePack()
        verifySnapshot("native-fresh", charmedCharsDataFolder, result.messages + "success=${result.success}")
    }

    // ── Plugin mock ───────────────────────────────────────────────────────────

    /** A plugin whose jar resources are served from the test classpath, for the catalog's textures. */
    private fun jarResources(): Plugin = mockk<Plugin> {
        every { getResource(any()) } answers { javaClass.classLoader.getResourceAsStream(firstArg<String>()) }
    }

    private fun File.seed(content: String) {
        parentFile.mkdirs()
        writeText(content)
    }

    private fun SetupResult.summary() = messages + "success=$success alreadySetup=$alreadySetup"

    // ── Snapshot rendering ────────────────────────────────────────────────────

    private fun render(root: File, messages: List<String>): String {
        val sb = StringBuilder()
        sb.append("##### RESULT MESSAGES #####\n")
        messages.forEach { sb.append(normalizePaths(it)).append('\n') }

        val files = root.walkTopDown().filter { it.isFile }
            .sortedBy { it.relativeTo(root).invariantSeparatorsPath }
            .toList()
        sb.append("\n##### FILES (${files.size}) #####\n")
        files.forEach { sb.append(it.relativeTo(root).invariantSeparatorsPath).append('\n') }

        for (file in files) {
            val path = file.relativeTo(root).invariantSeparatorsPath
            when (file.extension.lowercase()) {
                "png" -> sb.append("\n===== $path [sha256 ${sha256(file.readBytes())}] =====\n")
                // Zip entry timestamps vary per run, so record entry names and content hashes only
                "zip" -> {
                    sb.append("\n===== $path [zip] =====\n")
                    ZipFile(file).use { zip ->
                        zip.entries().toList().sortedBy { it.name }.forEach { entry ->
                            val bytes = zip.getInputStream(entry).use { it.readBytes() }
                            sb.append("${entry.name} sha256 ${sha256(bytes)}\n")
                        }
                    }
                }
                else -> {
                    val text = file.readText(Charsets.UTF_8)
                    sb.append("\n===== $path =====\n").append(text)
                    if (!text.endsWith("\n")) sb.append("\n<no newline at end of file>\n")
                }
            }
        }
        return sb.toString()
    }

    private fun normalizePaths(message: String): String =
        message.replace(tmp.absolutePath, "<TMP>").replace('\\', '/')

    private fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }

    // ── Compare / update ──────────────────────────────────────────────────────

    private fun verifySnapshot(name: String, root: File, messages: List<String>) {
        val actual = render(root, messages)
        val golden = File(GOLDEN_DIR, "$name.snapshot")

        if (System.getProperty("golden.update") == "true") {
            golden.parentFile.mkdirs()
            golden.writeText(actual, Charsets.UTF_8)
            return
        }

        if (!golden.exists()) {
            fail<Unit>("Missing golden file ${golden.path}. Generate it with -PupdateGolden and review it.")
        }
        val expected = golden.readText(Charsets.UTF_8)
        if (expected == actual) return

        val actualFile = File(ACTUAL_DIR, "$name.snapshot")
        actualFile.parentFile.mkdirs()
        actualFile.writeText(actual, Charsets.UTF_8)

        val expectedLines = expected.lines()
        val actualLines = actual.lines()
        val firstDiff = (0 until maxOf(expectedLines.size, actualLines.size))
            .first { expectedLines.getOrNull(it) != actualLines.getOrNull(it) }
        fail<Unit>(
            "Setup output differs from ${golden.path} at line ${firstDiff + 1}:\n" +
                "  expected: ${expectedLines.getOrNull(firstDiff)}\n" +
                "  actual:   ${actualLines.getOrNull(firstDiff)}\n" +
                "Full output written to ${actualFile.path}. " +
                "If the change is intended, rerun with -PupdateGolden and review the diff."
        )
    }

    companion object {
        private val GOLDEN_DIR = File("src/test/golden")
        private val ACTUAL_DIR = File("build/golden-actual")
    }
}
