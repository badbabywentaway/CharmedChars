package org.stephanosbad.charmedChars.graphics

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import org.stephanosbad.charmedChars.CharmedChars
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.net.InetSocketAddress

/**
 * Embedded HTTP server for hosting the resource pack
 * Eliminates the need for external web hosting
 */
class ResourcePackServer(private val plugin: CharmedChars) {

    private var httpServer: HttpServer? = null
    private val resourcePackFile = File(plugin.dataFolder, "CharmedChars-ResourcePack.zip")

    /**
     * Start the HTTP server
     */
    fun start() {
        if (!plugin.configManager.selfHostEnabled) {
            plugin.logger.info("Self-hosting is disabled. Resource pack will not be served by plugin.")
            return
        }

        if (!resourcePackFile.exists()) {
            plugin.logger.warning("Cannot start HTTP server: Resource pack file not found at ${resourcePackFile.absolutePath}")
            return
        }

        try {
            val port = plugin.configManager.selfHostPort
            val address = InetSocketAddress(port)

            httpServer = HttpServer.create(address, 0).apply {
                // Create handler for resource pack requests
                createContext("/CharmedChars-ResourcePack.zip", ResourcePackHandler())

                // Optional: Add a status endpoint
                createContext("/status", StatusHandler())

                executor = null // Use default executor
                start()
            }

            plugin.logger.info("Resource pack HTTP server started on port $port")
            plugin.logger.info("Resource pack URL: ${getResourcePackUrl()}")
        } catch (e: IOException) {
            plugin.logger.severe("Failed to start HTTP server: ${e.message}")
            plugin.logger.severe("Port ${plugin.configManager.selfHostPort} may already be in use. Try a different port in config.yml")
        } catch (e: Exception) {
            plugin.logger.severe("Unexpected error starting HTTP server: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Stop the HTTP server
     */
    fun stop() {
        httpServer?.let {
            it.stop(0)
            plugin.logger.info("Resource pack HTTP server stopped")
        }
        httpServer = null
    }

    /**
     * Check if server is running
     */
    fun isRunning(): Boolean = httpServer != null

    /**
     * Get the resource pack URL
     */
    fun getResourcePackUrl(): String {
        if (!isRunning()) return ""

        val configuredHost = plugin.configManager.selfHostAddress
        val port = plugin.configManager.selfHostPort

        // If configured address is 0.0.0.0 (bind to all interfaces),
        // we need to use the actual server IP for the client URL
        val host = if (configuredHost == "0.0.0.0") {
            // Use the server's IP address or localhost
            plugin.server.ip.ifBlank { "localhost" }
        } else {
            configuredHost
        }

        return "http://$host:$port/CharmedChars-ResourcePack.zip"
    }

    /**
     * Handler for resource pack file requests
     */
    private inner class ResourcePackHandler : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            try {
                if (exchange.requestMethod != "GET") {
                    sendResponse(exchange, 405, "Method Not Allowed")
                    return
                }

                if (!resourcePackFile.exists()) {
                    sendResponse(exchange, 404, "Resource pack not found")
                    return
                }

                // Set headers
                exchange.responseHeaders.add("Content-Type", "application/zip")
                exchange.responseHeaders.add("Content-Disposition", "attachment; filename=\"CharmedChars-ResourcePack.zip\"")
                exchange.responseHeaders.add("Cache-Control", "no-cache, no-store, must-revalidate")
                exchange.responseHeaders.add("Pragma", "no-cache")
                exchange.responseHeaders.add("Expires", "0")

                // Send file
                exchange.sendResponseHeaders(200, resourcePackFile.length())
                FileInputStream(resourcePackFile).use { fis ->
                    exchange.responseBody.use { os ->
                        fis.copyTo(os)
                    }
                }

                plugin.logger.info("Served resource pack to ${exchange.remoteAddress.address.hostAddress}")
            } catch (e: Exception) {
                plugin.logger.warning("Error serving resource pack: ${e.message}")
                try {
                    sendResponse(exchange, 500, "Internal Server Error")
                } catch (ignored: Exception) {
                }
            }
        }
    }

    /**
     * Handler for status endpoint
     */
    private inner class StatusHandler : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            try {
                if (exchange.requestMethod != "GET") {
                    sendResponse(exchange, 405, "Method Not Allowed")
                    return
                }

                val status = """
                    {
                        "plugin": "CharmedChars",
                        "status": "running",
                        "resourcePack": "${if (resourcePackFile.exists()) "available" else "missing"}",
                        "hash": "${plugin.textureManager.getResourcePackHash() ?: "unknown"}"
                    }
                """.trimIndent()

                exchange.responseHeaders.add("Content-Type", "application/json")
                sendResponse(exchange, 200, status)
            } catch (e: Exception) {
                sendResponse(exchange, 500, "Internal Server Error")
            }
        }
    }

    /**
     * Helper method to send text responses
     */
    private fun sendResponse(exchange: HttpExchange, statusCode: Int, message: String) {
        val response = message.toByteArray()
        exchange.sendResponseHeaders(statusCode, response.size.toLong())
        exchange.responseBody.use { os ->
            os.write(response)
        }
    }
}
