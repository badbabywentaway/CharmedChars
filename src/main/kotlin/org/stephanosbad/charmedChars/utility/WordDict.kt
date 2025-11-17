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
package org.stephanosbad.charmedChars.utility

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.stephanosbad.charmedChars.CharmedChars
import java.io.FileNotFoundException
import java.io.InputStreamReader

/**
 * Dictionary for word validation
 *
 * Loads and manages the dictionary of valid English words from Words.json.
 * Used to validate whether player-formed letter sequences are legitimate words.
 * The dictionary is loaded once at plugin initialization and stored as a singleton.
 */
class WordDict {
    /**
     * HashSet of valid lowercase English words
     *
     * Loaded from Words.json in the plugin resources.
     * Words are stored in lowercase for case-insensitive matching.
     */
    @SerializedName("Words")
    var words: HashSet<String?> = HashSet<String?>()

    companion object {
        /**
         * Singleton instance of the word dictionary
         *
         * Initialized by calling init() during plugin startup.
         */
        var singleton: WordDict? = null

        /**
         * Initializes the word dictionary from Words.json
         *
         * Loads the JSON dictionary file from plugin resources and deserializes it
         * into a WordDict instance. Logs the number of words loaded.
         *
         * @param sourceClass The CharmedChars plugin instance (used to access resources)
         * @throws FileNotFoundException if Words.json is not found in plugin resources
         */
        @Throws(FileNotFoundException::class)
        fun init(sourceClass: CharmedChars) {
            val gson = Gson()
            val loader =
                sourceClass.javaClass.getResourceAsStream("/Words.json")
            if (loader == null) {
                throw (FileNotFoundException("Words.json"))
            }
            singleton = gson.fromJson<WordDict?>(InputStreamReader(loader), WordDict::class.java)
            org.bukkit.Bukkit.getLogger().info("WordDict loaded with ${singleton?.words?.size ?: 0} words")
        }
    }
}