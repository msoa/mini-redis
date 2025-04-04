package com.kanastra.miniredis.infrastructure.handler

import com.kanastra.miniredis.application.ProtocolHandler
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.Socket
import java.util.concurrent.ExecutorService
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ClientManagerTest {

    private lateinit var protocolHandler: ProtocolHandler
    private lateinit var clientManager: ClientManager
    private lateinit var mockSocket: Socket

    @BeforeEach
    fun setUp() {
        protocolHandler = mockk()
        clientManager = ClientManager(protocolHandler, 2)
        mockSocket = mockk(relaxed = true)
    }

    @Test
    fun `start sets running to true and logs info`() {
        clientManager.start()
        val field = ClientManager::class.java.getDeclaredField("running")
        field.isAccessible = true
        assertTrue(field.getBoolean(clientManager))
    }

    @Test
    fun `handleClient submits ClientProcessor to the thread pool when running`() {
        clientManager.start()
        clientManager.handleClient(mockSocket)

        Thread.sleep(100)
        verify(exactly = 0) { mockSocket.close() }
    }

    @Test
    fun `handleClient closes socket when not running`() {
        clientManager.handleClient(mockSocket)
        verify { mockSocket.close() }
    }

    @Test
    fun `stop sets running to false and shuts down the thread pool`() {
        clientManager.start()
        clientManager.stop()

        val field = ClientManager::class.java.getDeclaredField("running")
        field.isAccessible = true
        assertFalse(field.getBoolean(clientManager))

        val poolField = ClientManager::class.java.getDeclaredField("pool")
        poolField.isAccessible = true
        val pool = poolField.get(clientManager) as ExecutorService
        assertTrue(pool.isShutdown)
    }

    @Test
    fun `stop does not shutdown pool if already shutdown`() {
        clientManager.start()
        clientManager.stop()
        clientManager.stop()

        val poolField = ClientManager::class.java.getDeclaredField("pool")
        poolField.isAccessible = true
        val pool = poolField.get(clientManager) as ExecutorService
        assertTrue(pool.isShutdown)
    }
}