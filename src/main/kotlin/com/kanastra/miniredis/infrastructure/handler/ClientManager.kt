package com.kanastra.miniredis.infrastructure.handler

import com.kanastra.miniredis.application.ProtocolHandler
import java.net.Socket
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.logging.Logger

class ClientManager(
    private val protocolHandler: ProtocolHandler,
    threadPoolSize: Int = Runtime.getRuntime().availableProcessors()
) {
    private val logger = Logger.getLogger(ClientManager::class.java.name)
    private val pool: ExecutorService = Executors.newFixedThreadPool(threadPoolSize)
    private var running = false

    fun start() {
        running = true
        logger.info("Client Manager started")
    }

    fun handleClient(clientSocket: Socket) {
        if (running) {
            pool.execute(ClientProcessor(clientSocket, protocolHandler))
        } else {
            logger.info("Client Manager is not running, cannot handle client.")
            clientSocket.close()
        }
    }

    fun stop() {
        running = false
        if (!pool.isShutdown) {
            pool.shutdown()
            logger.info("Client Manager stopped")
        }
    }
}