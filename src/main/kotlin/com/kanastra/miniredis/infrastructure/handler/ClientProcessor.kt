package com.kanastra.miniredis.infrastructure.handler

import com.kanastra.miniredis.application.ProtocolHandler
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.util.logging.Logger

const val MAX_DATA_LENGTH = 16368

class ClientProcessor(
    private val socket: Socket,
    private val protocolHandler: ProtocolHandler,
    private val maxDataLength: Int = MAX_DATA_LENGTH
) : Runnable {
    private val logger = Logger.getLogger(this.javaClass.name)

    override fun run() {
        try {
            BufferedReader(InputStreamReader(socket.getInputStream())).use { reader ->
                PrintWriter(socket.getOutputStream(), false).use { writer ->
                    handleClient(reader, writer)
                }
            }
        } catch (e: IOException) {
            logger.severe("IOException occurred while handling client ${socket.inetAddress}: ${e.message}")
            socket.getOutputStream().write(protocolHandler.handleException(e).toByteArray())
        } catch (e: Exception) {
            logger.severe("Exception occurred while handling client ${socket.inetAddress}: ${e.message}")
            socket.getOutputStream().write(protocolHandler.handleException(e).toByteArray())
        }
    }

    private fun handleClient(reader: BufferedReader, writer: PrintWriter) {
        while (!socket.isInputShutdown) {
            val message = readMessage(reader) ?: break

            logger.info("Received message from ${socket.inetAddress}: ${message.trim()}")

            try {
                val response = protocolHandler.handleMessage(message)
                writer.print(response)
                logger.info("Sent response to ${socket.inetAddress}: ${response.trim()}")
            } catch (e: Exception) {
                logger.severe("Error processing command from ${socket.inetAddress}: ${e.message}")
                writer.print(protocolHandler.handleException(e))
            } finally {
                writer.flush()
            }
        }
    }

    private fun readMessage(reader: BufferedReader): String? {
        val buffer = CharArray(maxDataLength)
        val bytesRead = reader.read(buffer)
        return if (bytesRead > 0) String(buffer, 0, bytesRead) else null
    }
}