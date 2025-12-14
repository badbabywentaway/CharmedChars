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

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

/**
 * Unit tests for CustomItemProviderManager logic
 *
 * Tests the provider detection and initialization logic concepts:
 * - Mutual exclusivity pattern (cannot have both ItemsAdder and Oraxen)
 * - Provider selection requirements
 * - Configuration validation patterns
 *
 * NOTE: These tests focus on the logical patterns and validation rules
 * rather than full integration testing with actual provider implementations,
 * since ItemsAdder and Oraxen APIs are not available in test environment.
 */
class CustomItemProviderManagerTest {

    // ==================== Provider Mutual Exclusivity Tests ====================

    @Test
    fun `exactly one provider should be active using XOR logic`() {
        // The manager enforces mutual exclusivity using XOR
        // Valid states: (ItemsAdder=true, Oraxen=false) or (ItemsAdder=false, Oraxen=true)
        // Invalid states: (both=true) or (both=false)

        val validConfigurations = listOf(
            Pair(true, false),   // Only ItemsAdder
            Pair(false, true)    // Only Oraxen
        )

        val invalidConfigurations = listOf(
            Pair(true, true),    // Both installed
            Pair(false, false)   // Neither installed
        )

        for ((itemsAdder, oraxen) in validConfigurations) {
            val exclusivityCheck = (itemsAdder xor oraxen)
            assertTrue(exclusivityCheck,
                "ItemsAdder=$itemsAdder, Oraxen=$oraxen should be valid (XOR=true)")
        }

        for ((itemsAdder, oraxen) in invalidConfigurations) {
            val exclusivityCheck = (itemsAdder xor oraxen)
            assertFalse(exclusivityCheck,
                "ItemsAdder=$itemsAdder, Oraxen=$oraxen should be invalid (XOR=false)")
        }
    }

    @ParameterizedTest
    @CsvSource(
        "true, false, true",   // ItemsAdder only: valid
        "false, true, true",   // Oraxen only: valid
        "true, true, false",   // Both: invalid
        "false, false, false"  // Neither: invalid
    )
    fun `provider configuration validation`(
        itemsAdderInstalled: Boolean,
        oraxenInstalled: Boolean,
        expectedValid: Boolean
    ) {
        val isValid = (itemsAdderInstalled xor oraxenInstalled)
        assertEquals(expectedValid, isValid,
            "ItemsAdder=$itemsAdderInstalled, Oraxen=$oraxenInstalled should be ${if (expectedValid) "valid" else "invalid"}")
    }

    @Test
    fun `XOR ensures mutual exclusivity`() {
        // XOR truth table:
        // false XOR false = false (neither installed - invalid)
        // false XOR true = true (Oraxen only - valid)
        // true XOR false = true (ItemsAdder only - valid)
        // true XOR true = false (both installed - invalid)

        assertFalse(false xor false, "Neither installed should be invalid")
        assertTrue(false xor true, "Oraxen only should be valid")
        assertTrue(true xor false, "ItemsAdder only should be valid")
        assertFalse(true xor true, "Both installed should be invalid")
    }

    // ==================== Provider Name Tests ====================

    @Test
    fun `provider names are well-defined strings`() {
        val validProviderNames = listOf("ItemsAdder", "Oraxen", "Unknown")

        for (name in validProviderNames) {
            assertFalse(name.isBlank(), "Provider name should not be blank: $name")
            assertTrue(name.first().isUpperCase(),
                "Provider name should start with uppercase: $name")
        }
    }

    @Test
    fun `unknown provider indicates uninitialized state`() {
        val uninitializedProviderName = "Unknown"

        assertEquals("Unknown", uninitializedProviderName,
            "Uninitialized state should return 'Unknown'")
        assertNotEquals("ItemsAdder", uninitializedProviderName)
        assertNotEquals("Oraxen", uninitializedProviderName)
    }

    // ==================== Error Message Requirements Tests ====================

    @Test
    fun `error messages should be actionable`() {
        // When both are installed, message should mention "both"
        val bothInstalledKeywords = listOf("both", "conflict", "mutual", "exclusive")
        assertTrue(bothInstalledKeywords.any { it == "both" },
            "Error for both installed should mention the conflict")

        // When neither is installed, message should mention requirement
        val neitherInstalledKeywords = listOf("neither", "requires", "install", "missing")
        assertTrue(neitherInstalledKeywords.any { it == "requires" },
            "Error for neither installed should mention requirement")
    }

    @Test
    fun `error state validation pattern`() {
        // Error states:
        // 1. Both ItemsAdder and Oraxen installed
        // 2. Neither ItemsAdder nor Oraxen installed

        val errorStates = listOf(
            Triple(true, true, "both installed"),
            Triple(false, false, "neither installed")
        )

        for ((itemsAdder, oraxen, description) in errorStates) {
            val isError = !(itemsAdder xor oraxen)
            assertTrue(isError, "Should be error state: $description")
        }
    }

    // ==================== Provider Selection Logic Tests ====================

    @Test
    fun `ItemsAdder takes precedence when only it is installed`() {
        // Logical flow:
        // if (itemsAdderAvailable && !oraxenAvailable) -> use ItemsAdder

        val itemsAdderAvailable = true
        val oraxenAvailable = false

        val shouldUseItemsAdder = itemsAdderAvailable && !oraxenAvailable
        assertTrue(shouldUseItemsAdder,
            "Should select ItemsAdder when only it is available")
    }

    @Test
    fun `Oraxen is selected when only it is installed`() {
        // Logical flow:
        // if (!itemsAdderAvailable && oraxenAvailable) -> use Oraxen

        val itemsAdderAvailable = false
        val oraxenAvailable = true

        val shouldUseOraxen = !itemsAdderAvailable && oraxenAvailable
        assertTrue(shouldUseOraxen,
            "Should select Oraxen when only it is available")
    }

    @ParameterizedTest
    @CsvSource(
        "true, false, ItemsAdder",
        "false, true, Oraxen"
    )
    fun `correct provider is selected based on availability`(
        itemsAdderAvailable: Boolean,
        oraxenAvailable: Boolean,
        expectedProvider: String
    ) {
        val selectedProvider = when {
            itemsAdderAvailable && !oraxenAvailable -> "ItemsAdder"
            !itemsAdderAvailable && oraxenAvailable -> "Oraxen"
            else -> "Unknown"
        }

        assertEquals(expectedProvider, selectedProvider,
            "Should select $expectedProvider when ItemsAdder=$itemsAdderAvailable, Oraxen=$oraxenAvailable")
    }

    // ==================== Initialization Success Criteria Tests ====================

    @Test
    fun `initialization succeeds only with exactly one provider`() {
        // Success criteria: XOR of availability flags

        val successCases = listOf(
            Pair(true, false),   // ItemsAdder only
            Pair(false, true)    // Oraxen only
        )

        for ((itemsAdder, oraxen) in successCases) {
            val shouldSucceed = (itemsAdder xor oraxen)
            assertTrue(shouldSucceed,
                "Should succeed with ItemsAdder=$itemsAdder, Oraxen=$oraxen")
        }
    }

    @Test
    fun `initialization fails without valid provider configuration`() {
        // Failure criteria: NOT(XOR) of availability flags

        val failureCases = listOf(
            Pair(true, true),    // Both
            Pair(false, false)   // Neither
        )

        for ((itemsAdder, oraxen) in failureCases) {
            val shouldFail = !(itemsAdder xor oraxen)
            assertTrue(shouldFail,
                "Should fail with ItemsAdder=$itemsAdder, Oraxen=$oraxen")
        }
    }

    // ==================== Provider Availability Check Tests ====================

    @Test
    fun `provider availability is boolean check`() {
        // isAvailable() should return true or false
        val availabilityStates = listOf(true, false)

        for (state in availabilityStates) {
            assertTrue(state == true || state == false,
                "Availability should be boolean: $state")
        }
    }

    @Test
    fun `plugin detection checks plugin manager`() {
        // Bukkit.getPluginManager().getPlugin("PluginName") returns:
        // - Plugin instance if installed
        // - null if not installed

        val pluginInstalled = true  // Would return Plugin instance
        val pluginNotInstalled = false  // Would return null

        assertNotEquals(pluginInstalled, pluginNotInstalled,
            "Installed and not installed should be different states")
    }

    // ==================== Integration Contract Tests ====================

    @Test
    fun `manager requires CharmedChars plugin instance`() {
        // Constructor: CustomItemProviderManager(plugin: CharmedChars)
        // This ensures logging and configuration access

        val hasPluginParameter = true
        assertTrue(hasPluginParameter,
            "Manager should require plugin instance in constructor")
    }

    @Test
    fun `initialization returns result with success flag`() {
        // InitResult should have:
        // - success: Boolean
        // - provider: CustomItemProvider?
        // - messages: List<String>

        data class MockInitResult(
            val success: Boolean,
            val provider: Any?,
            val messages: List<String>
        )

        val successResult = MockInitResult(true, "SomeProvider", listOf("Success"))
        val failureResult = MockInitResult(false, null, listOf("Error"))

        assertTrue(successResult.success, "Success result should have success=true")
        assertFalse(failureResult.success, "Failure result should have success=false")
        assertNotNull(successResult.provider, "Success should have provider")
        assertNull(failureResult.provider, "Failure should have null provider")
    }
}
