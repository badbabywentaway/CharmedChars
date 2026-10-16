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
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.bukkit.Material
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginDescriptionFile
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.stephanosbad.charmedChars.CharmedChars
import org.stephanosbad.charmedChars.utility.ConfigManager
import org.stephanosbad.integrare.provider.NativeItemProvider
import java.io.File
import java.security.MessageDigest
import java.util.logging.Logger
import java.util.zip.ZipFile

/**
 * Golden-file snapshots of everything the four provider setups write to disk.
 *
 * Locks the current output (file layout, config/model content, texture bytes,
 * custom_variation / CMD / note block numbering, and result messages) so that
 * moving the setup code into Integrare can be checked for byte-for-byte parity.
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

    // Tests run against MockBukkit's 1.20 API, which has no POTENT_SULFUR. On MC 26.3 it
    // exists, so the setups emit the sulfur ingot recipe; they only null-check the lookup.
    @BeforeEach
    fun stubMaterialLookup() {
        mockkStatic(Material::class)
        every { Material.matchMaterial("POTENT_SULFUR") } returns Material.STONE
    }

    @AfterEach
    fun unstubMaterialLookup() {
        unmockkStatic(Material::class)
    }

    // ── Scenarios ─────────────────────────────────────────────────────────────

    @Test
    fun `ItemsAdder fresh install`() {
        val result = ItemsAdderSetup(mockPlugin("ItemsAdder")).autoSetup()
        verifySnapshot("itemsadder-fresh", File(pluginsDir, "ItemsAdder"), result.summary())
    }

    @Test
    fun `ItemsAdder install into existing items_packs yml`() {
        File(pluginsDir, "ItemsAdder/data/items_packs.yml").seed(
            "packs:\n  otherpack:\n    enabled: true\n"
        )
        val result = ItemsAdderSetup(mockPlugin("ItemsAdder")).autoSetup()
        verifySnapshot("itemsadder-existing-items-packs", File(pluginsDir, "ItemsAdder"), result.summary())
    }

    @Test
    fun `Oraxen fresh install`() {
        val result = OraxenSetup(mockPlugin("Oraxen")).autoSetup()
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
        val result = OraxenSetup(mockPlugin("Oraxen")).autoSetup()
        verifySnapshot("oraxen-existing-recipes", File(pluginsDir, "Oraxen"), result.summary())
    }

    @Test
    fun `Nexo fresh install`() {
        val result = NexoSetup(mockPlugin("Nexo")).setup()
        val summary = when (result) {
            is NexoSetup.SetupResult.Error -> "Error: ${result.message}"
            else -> result::class.simpleName.toString()
        }
        verifySnapshot("nexo-fresh", File(pluginsDir, "Nexo"), listOf(summary))
    }

    @Test
    fun `Native pack generation`() {
        val result = NativeItemManagerSetup(mockPlugin(null), NativeItemProvider()).autoSetup()
        verifySnapshot("native-fresh", charmedCharsDataFolder, result.messages + "success=${result.success}")
    }

    // ── Plugin mock ───────────────────────────────────────────────────────────

    /**
     * Mocks only what the setup classes touch: the provider plugin's data folder,
     * jar resources (served from the test classpath), logger, version and CMD base.
     */
    private fun mockPlugin(providerName: String?): CharmedChars {
        val plugin = mockk<CharmedChars>(relaxed = true)
        val providerPlugin = providerName?.let { name ->
            mockk<Plugin> { every { dataFolder } returns File(pluginsDir, name) }
        }
        every { plugin.server.pluginManager.getPlugin(any()) } answers {
            if (firstArg<String>() == providerName) providerPlugin else null
        }
        every { plugin.getResource(any()) } answers {
            javaClass.classLoader.getResourceAsStream(firstArg<String>())
        }
        every { plugin.logger } returns Logger.getLogger("SetupGoldenTest")
        every { plugin.dataFolder } returns charmedCharsDataFolder
        every { plugin.description } returns PluginDescriptionFile("CharmedChars", "GOLDEN", "test.Main")
        val configManager = mockk<ConfigManager> { every { nativeCmdBase } returns 1000 }
        every { plugin.configManager } returns configManager
        charmedCharsDataFolder.mkdirs()
        return plugin
    }

    private fun File.seed(content: String) {
        parentFile.mkdirs()
        writeText(content)
    }

    private fun ItemsAdderSetup.SetupResult.summary() = messages + "success=$success alreadySetup=$alreadySetup"
    private fun OraxenSetup.SetupResult.summary() = messages + "success=$success alreadySetup=$alreadySetup"

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
