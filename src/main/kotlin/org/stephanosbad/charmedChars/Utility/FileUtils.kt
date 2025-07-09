package org.stephanosbad.charmedChars.Utility

import org.apache.commons.lang.StringUtils
import java.io.*
import java.net.JarURLConnection
import java.net.URL

object FileUtils {
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

    private fun copyFilesRecusively(
        toCopy: File,
        destDir: File
    ): Boolean {
        assert(destDir.isDirectory())

        if (!toCopy.isDirectory()) {
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

    @Throws(IOException::class)
    fun copyJarResourcesRecursively(
        destDir: File?,
        jarConnection: JarURLConnection
    ): Boolean {
        val jarFile = jarConnection.getJarFile()

        val e = jarFile.entries()
        while (e.hasMoreElements()) {
            val entry = e.nextElement()
            if (entry.getName().startsWith(jarConnection.getEntryName())) {
                val filename = StringUtils.removeStart(
                    entry.getName(),  //
                    jarConnection.getEntryName()
                )

                val f = File(destDir, filename)
                if (!entry.isDirectory()) {
                    val entryInputStream = jarFile.getInputStream(entry)
                    if (!copyStream(entryInputStream, f)) {
                        return false
                    }
                    entryInputStream.close()
                } else {
                    if (!ensureDirectoryExists(f)) {
                        throw IOException(
                            "Could not create directory: "
                                    + f.getAbsolutePath()
                        )
                    }
                }
            }
        }
        return true
    }

    fun copyResourcesRecursively( //
        originUrl: URL, destination: File
    ): Boolean {
        try {
            val urlConnection = originUrl.openConnection()
            if (urlConnection is JarURLConnection) {
                return copyJarResourcesRecursively(
                    destination,
                    urlConnection
                )
            } else {
                return copyFilesRecusively(
                    File(originUrl.getPath()),
                    destination
                )
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }

    private fun copyStream(`is`: InputStream, f: File): Boolean {
        try {
            return copyStream(`is`, FileOutputStream(f))
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return false
    }

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

    private fun ensureDirectoryExists(f: File): Boolean {
        return f.exists() || f.mkdir()
    }
}
