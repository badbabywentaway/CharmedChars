package org.stephanosbad.charmedChars.utility

import com.google.gson.Gson
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
        }
    }
}