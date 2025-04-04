package com.kanastra.miniredis.infrastructure.handler

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.IOException
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConnectionAcceptorTest {

    private lateinit var serverSocket: ServerSocket
    private lateinit var clientManager: ClientManager
    private lateinit var connectionAcceptor: ConnectionAcceptor

    @BeforeEach
    fun setUp() {
        serverSocket = mockk()
        clientManager = mockk(relaxUnitFun = true)
        connectionAcceptor = ConnectionAcceptor(serverSocket, clientManager)
    }

    @Test
    fun `run should accept connections and delegate to clientManager`() {
        val mockClientSocket = mockk<Socket>()
        val mockInetAddress = mockk<InetAddress>()
        every { mockClientSocket.inetAddress } returns mockInetAddress
        every { mockInetAddress.toString() } returns "mock.address"

        every { serverSocket.accept() } returns mockClientSocket andThenThrows IOException("Simulated stop")

        connectionAcceptor.run()

        verify(exactly = 2) { serverSocket.accept() }
        verify(exactly = 1) { clientManager.handleClient(mockClientSocket) }
    }

    @Test
    fun `run should stop when IOException occurs during accept`() {
        every { serverSocket.accept() } throws IOException("Test IOException")

        connectionAcceptor.run()

        val runningField = ConnectionAcceptor::class.java.getDeclaredField("running")
        runningField.isAccessible = true
        assertFalse(runningField.getBoolean(connectionAcceptor))
    }

    @Test
    fun `stop should set running to false`() {
        val runningField = ConnectionAcceptor::class.java.getDeclaredField("running")
        runningField.isAccessible = true
        assertTrue(runningField.getBoolean(connectionAcceptor))

        connectionAcceptor.stop()

        assertFalse(runningField.getBoolean(connectionAcceptor))
    }
}