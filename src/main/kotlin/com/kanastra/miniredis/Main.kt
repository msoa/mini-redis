package com.kanastra.miniredis

import com.kanastra.miniredis.domain.redis.RedisProtocolHandler
import com.kanastra.miniredis.domain.storage.InMemoryStorage
import com.kanastra.miniredis.domain.storage.SchedulerExpireManager
import com.kanastra.miniredis.infrastructure.handler.TcpServer
import java.util.logging.Level
import java.util.logging.Logger

fun main() {
    val logger = Logger.getLogger("MiniRedis")
    logger.level = Level.INFO

    val portEnv = System.getenv("MINIREDIS_PORT")
    val threadPoolSizeEnv = System.getenv("MINIREDIS_THREAD_POOL_SIZE")

    val defaultPort = 6379
    val defaultThreadPoolSize = Runtime.getRuntime().availableProcessors()

    val port = portEnv?.toIntOrNull() ?: defaultPort
    val threadPoolSize = threadPoolSizeEnv?.toIntOrNull() ?: defaultThreadPoolSize

    logger.info("Starting MiniRedis server on port: $port with thread pool size: $threadPoolSize")

    val expireManager = SchedulerExpireManager()
    val storage = InMemoryStorage(expireManager)
    val protocolHandler = RedisProtocolHandler(storage)
    val server = TcpServer(port, protocolHandler, threadPoolSize)

    try {
        server.start()
        while (true) {
            Thread.sleep(1000)
        }
    } catch (e: Exception) {
        logger.log(Level.SEVERE, "Error during server execution: ${e.message}", e)
        server.stop()
    }
}