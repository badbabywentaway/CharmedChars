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


import org.apache.commons.lang3.StringUtils
import java.io.*
import java.net.JarURLConnection
import java.net.URL

/**
 * Utility object for file and directory operations
 *
 * Provides methods for copying files, directories, and JAR resources recursively.
 * Used primarily for the ItemsAdder auto-setup functionality to copy configuration
 * files and textures from plugin resources to the ItemsAdder directory.
 */
object FileUtils {
    /**
     * Copies a single file to a destination
     *
     * @param toCopy The source file to copy
     * @param destFile The destination file path
     * @return true if the copy succeeded, false otherwise
     */
    fun copyFile(toCopy: File, destFile: File): Boolean {
        try {
            return copyStream(
                FileInputStream(toCopy),
                FileOutputStream(destFile)
            )
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * Recursively copies files and directories
     *
     * If the source is a file, copies it to the destination directory.
     * If the source is a directory, creates it in the destination and recursively copies all children.
     *
     * @param toCopy The source file or directory to copy
     * @param destDir The destination directory
     * @return true if all copies succeeded, false if any failed
     */
    private fun copyFilesRecusively(
        toCopy: File,
        destDir: File
    ): Boolean {
        assert(destDir.isDirectory)

        if (!toCopy.isDirectory) {
            return copyFile(toCopy, File(destDir, toCopy.getName()))
        } else {
            val newDestDir = File(destDir, toCopy.getName())
            if (!newDestDir.exists() && !newDestDir.mkdir()) {
                return false
            }
            for (child in toCopy.listFiles()) {
                if (!copyFilesRecusively(child, newDestDir)) {
                    return false
                }
            }
        }
        return true
    }

    /**
     * Recursively copies resources from a JAR file
     *
     * Extracts all entries from a JAR that match the specified entry path,
     * preserving the directory structure in the destination.
     *
     * @param destDir The destination directory for extracted files
     * @param jarConnection The JAR URL connection providing access to the JAR contents
     * @return true if all extractions succeeded, false if any failed
     * @throws IOException if file operations fail
     */
    @Throws(IOException::class)
    fun copyJarResourcesRecursively(
        destDir: File?,
        jarConnection: JarURLConnection
    ): Boolean {
        val jarFile = jarConnection.jarFile

        val e = jarFile.entries()
        while (e.hasMoreElements()) {
            val entry = e.nextElement()
            if (entry.getName().startsWith(jarConnection.entryName)) {
                val filename = StringUtils.removeStart(
                    entry.getName(),  //
                    jarConnection.entryName
                )

                val f = File(destDir, filename)
                if (!entry.isDirectory) {
                    val entryInputStream = jarFile.getInputStream(entry)
                    if (!copyStream(entryInputStream, f)) {
                        return false
                    }
                    entryInputStream.close()
                } else {
                    if (!ensureDirectoryExists(f)) {
                        throw IOException(
                            "Could not create directory: "
                                    + f.absolutePath
                        )
                    }
                }
            }
        }
        return true
    }

    /**
     * Copies resources recursively from a URL (supports both JAR and file URLs)
     *
     * Automatically detects whether the source is a JAR resource or filesystem path
     * and uses the appropriate copy method.
     *
     * @param originUrl The URL of the resource to copy (can be jar: or file: URL)
     * @param destination The destination directory
     * @return true if the copy succeeded, false otherwise
     */
    fun copyResourcesRecursively(
        originUrl: URL, destination: File
    ): Boolean {
        try {
            val urlConnection = originUrl.openConnection()
            return if (urlConnection is JarURLConnection) {
                copyJarResourcesRecursively(
                    destination,
                    urlConnection
                )
            } else {
                copyFilesRecusively(
                    File(originUrl.getPath()),
                    destination
                )
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * Copies data from an input stream to a file
     *
     * @param `is` The input stream to read from
     * @param f The destination file
     * @return true if the copy succeeded, false otherwise
     */
    private fun copyStream(`is`: InputStream, f: File): Boolean {
        try {
            return copyStream(`is`, FileOutputStream(f))
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * Copies data from an input stream to an output stream
     *
     * Reads from the input stream in 1024-byte chunks and writes to the output stream.
     * Closes both streams when complete.
     *
     * @param `is` The input stream to read from
     * @param os The output stream to write to
     * @return true if the copy succeeded, false otherwise
     */
    private fun copyStream(`is`: InputStream, os: OutputStream): Boolean {
        try {
            val buf = ByteArray(1024)

            var len = 0
            while ((`is`.read(buf).also { len = it }) > 0) {
                os.write(buf, 0, len)
            }
            `is`.close()
            os.close()
            return true
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * Ensures a directory exists, creating it if necessary
     *
     * @param f The directory to check/create
     * @return true if the directory exists or was created successfully
     */
    private fun ensureDirectoryExists(f: File): Boolean {
        return f.exists() || f.mkdir()
    }
}
