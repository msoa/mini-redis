package com.kanastra.miniredis.infrastructure.handler

import com.kanastra.miniredis.application.ProtocolHandler
import java.io.IOException
import java.net.ServerSocket
import java.util.logging.Logger

class TcpServer(
    private val port: Int,
    private val protocolHandler: ProtocolHandler,
    private val threadPoolSize: Int
) {
    private val logger = Logger.getLogger(TcpServer::class.java.name)
    private var serverSocket: ServerSocket? = null
    private var connectionAcceptor: ConnectionAcceptor? = null
    private var clientManager: ClientManager? = null
    private var isRunning = false

    fun start() {
        if (isRunning) {
            logger.warning("Server is already running on port $port")
            return
        }

        logger.info("Starting server on port $port")
        try {
            serverSocket = ServerSocket(port)
            clientManager = ClientManager(protocolHandler, threadPoolSize)
            connectionAcceptor = ConnectionAcceptor(serverSocket!!, clientManager!!)
            Thread(connectionAcceptor).start()
            clientManager!!.start()
            isRunning = true
            logger.info("Server started successfully on port $port")
        } catch (e: IOException) {
            logger.severe("Failed to start server on port $port: ${e.message}")
            stopInternal()
            throw e
        } catch (e: Exception) {
            logger.severe("An unexpected error occurred while starting server on port $port: ${e.message}")
            stopInternal()
            throw e
        }
    }

    fun stop() {
        if (isRunning) {
            logger.info("Stopping server on port $port")
            stopInternal()
            isRunning = false
            logger.info("Server stopped successfully on port $port")
        } else {
            logger.info("Server is not currently running.")
        }
    }

    private fun stopInternal() {
        connectionAcceptor?.stop()
        clientManager?.stop()
        serverSocket?.let {
            if (!it.isClosed) {
                try {
                    it.close()
                } catch (e: IOException) {
                    logger.severe("Error closing server socket: ${e.message}")
                }
            }
        }
    }
}