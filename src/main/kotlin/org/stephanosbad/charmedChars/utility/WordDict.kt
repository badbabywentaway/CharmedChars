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
 *
 */
class WordDict {
    /**
     *
     */
    @SerializedName("Words")
    var words: HashSet<String?> = HashSet<String?>()

    companion object {
        var singleton: WordDict? = null

        /**
         * @throws FileNotFoundException
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