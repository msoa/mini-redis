package com.kanastra.miniredis.infrastructure.handler

import java.io.IOException
import java.net.ServerSocket
import java.util.logging.Logger

class ConnectionAcceptor(
    private val serverSocket: ServerSocket,
    private val clientManager: ClientManager
) : Runnable {
    private val logger = Logger.getLogger(ConnectionAcceptor::class.java.name)
    private var running = true

    override fun run() {
        logger.info("Connection Acceptor started")
        while (running) {
            try {
                val socket = serverSocket.accept()
                logger.info("Accepted connection from ${socket.inetAddress}")
                clientManager.handleClient(socket)
            } catch (e: IOException) {
                if (running) {
                    logger.severe("IOException occurred while accepting connections: ${e.message}")
                    stop()
                }
            }
        }
        logger.info("Connection Acceptor stopped")
    }

    fun stop() {
        running = false
    }
}