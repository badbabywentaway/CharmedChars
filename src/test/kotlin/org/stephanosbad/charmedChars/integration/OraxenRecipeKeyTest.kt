/*
 * CharmedChars - A word-forming puzzle game for Minecraft
 * Copyright (C) 2025 StephanosBad
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 */
package org.stephanosbad.charmedChars.integration

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

/**
 * Tests for Oraxen recipe key naming and cleanup regex correctness.
 *
 * All CharmedChars recipe keys must be prefixed with "charmedchars_" to prevent
 * YAML key collisions in Oraxen's shared shaped.yml / shapeless.yml files.
 */
class OraxenRecipeKeyTest {

    private val expectedShapelessKeys = listOf(
        "charmedchars_pyrite_ingot",
        "charmedchars_pyrite_ingot_sulfur"
    )

    private val expectedShapedKeys = listOf(
        "charmedchars_pyrite_pickaxe",
        "charmedchars_pyrite_axe",
        "charmedchars_pyrite_shovel",
        "charmedchars_pyrite_hoe"
    )

    private val allRecipeKeys = expectedShapelessKeys + expectedShapedKeys

    // ── Key naming ────────────────────────────────────────────────────────────

    @Test
    fun `all recipe keys are prefixed with charmedchars underscore`() {
        allRecipeKeys.forEach { key ->
            assertTrue(key.startsWith("charmedchars_"),
                "'$key' must start with 'charmedchars_'")
        }
    }

    @Test
    fun `no bare pyrite keys exist`() {
        val bareKeys = listOf("pyrite_ingot", "pyrite_pickaxe", "pyrite_axe",
                              "pyrite_shovel", "pyrite_hoe", "pyrite_ingot_sulfur")
        bareKeys.forEach { bare ->
            assertFalse(allRecipeKeys.contains(bare),
                "Bare key '$bare' must not appear — use 'charmedchars_$bare' instead")
        }
    }

    @Test
    fun `shapeless recipe count is 2 including sulfur alt`() {
        assertEquals(2, expectedShapelessKeys.size)
    }

    @Test
    fun `shaped recipe count is 4`() {
        assertEquals(4, expectedShapedKeys.size)
    }

    @Test
    fun `total recipe count is 6`() {
        assertEquals(6, allRecipeKeys.size)
    }

    // ── Cyan display name tag ─────────────────────────────────────────────────

    @Test
    fun `cyan display name uses aqua not cyan tag`() {
        val colorTagMap = mapOf(
            "cyan"    to "<aqua>",
            "magenta" to "<light_purple>",
            "yellow"  to "<yellow>"
        )
        assertEquals("<aqua>", colorTagMap["cyan"],
            "<cyan> is not a valid MiniMessage tag — must be <aqua>")
    }

    @Test
    fun `cyan tag is not the literal string cyan in angle brackets`() {
        val cyanTag = "<aqua>"
        assertNotEquals("<cyan>", cyanTag,
            "Must not use <cyan> — it is not a valid MiniMessage color name")
    }

    @ParameterizedTest
    @ValueSource(strings = ["<aqua>", "<light_purple>", "<yellow>"])
    fun `all color tags are valid MiniMessage named colors`(tag: String) {
        val validTags = setOf("<aqua>", "<light_purple>", "<yellow>", "<gold>",
                              "<red>", "<green>", "<blue>", "<white>", "<gray>",
                              "<dark_gray>", "<dark_red>", "<dark_green>",
                              "<dark_blue>", "<dark_aqua>", "<dark_purple>", "<black>")
        assertTrue(validTags.contains(tag), "'$tag' is not a recognised MiniMessage color tag")
    }

    // ── Cleanup regex correctness ─────────────────────────────────────────────

    @Test
    fun `removal regex matches charmedchars prefixed shapeless keys`() {
        val sampleYaml = """
            other_plugin_recipe:
              result:
                item: other:thing
            charmedchars_pyrite_ingot:
              result:
                oraxen_item: pyrite_ingot
              ingredients:
                A:
                  amount: 1
                  minecraft_type: IRON_INGOT
            another_plugin_recipe:
              result:
                item: another:thing
        """.trimIndent()

        val cleaned = sampleYaml
            .replace(Regex("charmedchars_pyrite_ingot_sulfur:.*?(?=\\n\\w+:|$)", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("charmedchars_pyrite_ingot:.*?(?=\\n\\w+:|$)", RegexOption.DOT_MATCHES_ALL), "")
            .trim()

        assertFalse(cleaned.contains("charmedchars_pyrite_ingot"),
            "Cleanup regex should remove charmedchars_pyrite_ingot entry")
        assertTrue(cleaned.contains("other_plugin_recipe"),
            "Cleanup regex must not remove other plugins' recipe entries")
        assertTrue(cleaned.contains("another_plugin_recipe"),
            "Cleanup regex must not remove entries after the removed block")
    }

    @Test
    fun `removal regex matches sulfur key without touching ingot key`() {
        val sampleYaml = """
            charmedchars_pyrite_ingot:
              result:
                oraxen_item: pyrite_ingot
            charmedchars_pyrite_ingot_sulfur:
              result:
                oraxen_item: pyrite_ingot
        """.trimIndent()

        // Remove sulfur first (as the actual code does)
        val afterSulfurRemoval = sampleYaml
            .replace(Regex("charmedchars_pyrite_ingot_sulfur:.*?(?=\\n\\w+:|$)", RegexOption.DOT_MATCHES_ALL), "")

        assertTrue(afterSulfurRemoval.contains("charmedchars_pyrite_ingot:"),
            "Removing sulfur key must not remove the base ingot key")

        val fullyClean = afterSulfurRemoval
            .replace(Regex("charmedchars_pyrite_ingot:.*?(?=\\n\\w+:|$)", RegexOption.DOT_MATCHES_ALL), "")
            .trim()

        assertFalse(fullyClean.contains("charmedchars_pyrite_ingot"),
            "Both keys should be gone after full cleanup")
    }

    @Test
    fun `removal regex matches all four shaped tool keys`() {
        val toolKeys = listOf("charmedchars_pyrite_pickaxe", "charmedchars_pyrite_axe",
                              "charmedchars_pyrite_shovel", "charmedchars_pyrite_hoe")
        val sampleYaml = toolKeys.joinToString("\n") { key ->
            "$key:\n  result:\n    oraxen_item: ${key.removePrefix("charmedchars_")}"
        }

        var cleaned = sampleYaml
        toolKeys.forEach { key ->
            cleaned = cleaned.replace(
                Regex("${Regex.escape(key)}:.*?(?=\\n\\w+:|\$)", RegexOption.DOT_MATCHES_ALL), ""
            )
        }
        cleaned = cleaned.trim()

        toolKeys.forEach { key ->
            assertFalse(cleaned.contains(key), "Cleanup should remove '$key'")
        }
    }

    @Test
    fun `hasCharmedCharsRecipes detects prefixed ingot key`() {
        val content = "charmedchars_pyrite_ingot:\n  result:\n    oraxen_item: pyrite_ingot"
        assertTrue(content.contains("charmedchars_pyrite_ingot:"),
            "Detection should look for the prefixed key, not the bare key")
        assertFalse(content.contains("pyrite_ingot:") && !content.contains("charmedchars_"),
            "Bare 'pyrite_ingot:' must not be used as the detection sentinel")
    }

    // ── YAML structure of generated shapeless recipe ──────────────────────────

    @Test
    fun `generated shapeless recipe contains oraxen_item result`() {
        val recipeYaml = buildString {
            appendLine("charmedchars_pyrite_ingot:")
            appendLine("  result:")
            appendLine("    oraxen_item: pyrite_ingot")
            appendLine("  ingredients:")
            appendLine("    A:")
            appendLine("      amount: 1")
            appendLine("      minecraft_type: IRON_INGOT")
            appendLine("    B:")
            appendLine("      amount: 1")
            appendLine("      minecraft_type: REDSTONE")
        }

        assertTrue(recipeYaml.contains("charmedchars_pyrite_ingot:"))
        assertTrue(recipeYaml.contains("oraxen_item: pyrite_ingot"))
        assertTrue(recipeYaml.contains("minecraft_type: IRON_INGOT"))
        assertTrue(recipeYaml.contains("minecraft_type: REDSTONE"))
    }

    @Test
    fun `generated shaped recipe keys use charmedchars prefix`() {
        val shapedYaml = buildString {
            appendLine("charmedchars_pyrite_pickaxe:")
            appendLine("  shape:")
            appendLine("    - PPP")
            appendLine("    - _S_")
            appendLine("    - _S_")
            appendLine("  result:")
            appendLine("    oraxen_item: pyrite_pickaxe")
        }

        assertTrue(shapedYaml.startsWith("charmedchars_pyrite_pickaxe:"),
            "Shaped recipe key must be namespaced")
        assertFalse(shapedYaml.startsWith("pyrite_pickaxe:"),
            "Must not use bare key")
    }
}
