package com.kanastra.miniredis.infrastructure.handler

import com.kanastra.miniredis.application.ProtocolHandler
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.IOException
import java.net.ServerSocket
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TcpServerTest {

    private val port = 0
    private val threadPoolSize = 2
    private lateinit var protocolHandler: ProtocolHandler
    private lateinit var mockServerSocket: ServerSocket
    private lateinit var mockClientManager: ClientManager
    private lateinit var mockConnectionAcceptor: ConnectionAcceptor
    private lateinit var tcpServer: TcpServer

    @BeforeEach
    fun setUp() {
        protocolHandler = mockk()
        mockServerSocket = mockk()
        mockClientManager = mockk(relaxUnitFun = true)
        mockConnectionAcceptor = mockk(relaxUnitFun = true)
        tcpServer = TcpServer(port, protocolHandler, threadPoolSize)
    }

    @Test
    fun `start should create ServerSocket, ClientManager, and ConnectionAcceptor and start them`() {
        mockkStatic(ServerSocket::class)
        mockkConstructor(ClientManager::class)
        mockkConstructor(ConnectionAcceptor::class)

        every { anyConstructed<ClientManager>().start() } just Runs
        every { anyConstructed<ConnectionAcceptor>().run() } just Runs
        every { mockServerSocket.isClosed } returns false
        every { mockServerSocket.close() } just Runs

        tcpServer.start()

        verify { anyConstructed<ClientManager>().start() }
        verify { anyConstructed<ConnectionAcceptor>().run() }
        val isRunningField = TcpServer::class.java.getDeclaredField("isRunning")
        isRunningField.isAccessible = true
        assertTrue(isRunningField.getBoolean(tcpServer))

        unmockkStatic(ServerSocket::class)
    }

    @Test
    fun `start should handle IoException during startup and stop`() {
        mockkStatic(ServerSocket::class)
        mockkConstructor(ClientManager::class)
        every { anyConstructed<ClientManager>().start() } throws IOException("Startup error")
        every { mockServerSocket.isClosed } returns false
        every { mockServerSocket.close() } just Runs

        assertThrows<IOException> { tcpServer.start() }

        val isRunningField = TcpServer::class.java.getDeclaredField("isRunning")
        isRunningField.isAccessible = true
        assertFalse(isRunningField.getBoolean(tcpServer))

        unmockkStatic(ServerSocket::class)
    }

    @Test
    fun `start should handle Exception during startup and stop`() {
        mockkStatic(ServerSocket::class)
        mockkConstructor(ClientManager::class)
        every { anyConstructed<ClientManager>().start() } throws RuntimeException("Startup error")
        every { mockServerSocket.isClosed } returns false
        every { mockServerSocket.close() } just Runs

        assertThrows<RuntimeException> { tcpServer.start() }

        val isRunningField = TcpServer::class.java.getDeclaredField("isRunning")
        isRunningField.isAccessible = true
        assertFalse(isRunningField.getBoolean(tcpServer))

        unmockkStatic(ServerSocket::class)
    }

    @Test
    fun `stop should stop ConnectionAcceptor, ClientManager, close ServerSocket and set isRunning to false`() {
        val isRunningField = TcpServer::class.java.getDeclaredField("isRunning")
        isRunningField.isAccessible = true
        isRunningField.setBoolean(tcpServer, true)

        val mockAcceptor = mockk<ConnectionAcceptor>(relaxUnitFun = true)
        val mockManager = mockk<ClientManager>(relaxUnitFun = true)
        val mockSocket = mockk<ServerSocket>()

        val server = TcpServer(port, protocolHandler, threadPoolSize)
        val serverSocketField = TcpServer::class.java.getDeclaredField("serverSocket")
        serverSocketField.isAccessible = true
        serverSocketField.set(server, mockSocket)
        val connectionAcceptorField = TcpServer::class.java.getDeclaredField("connectionAcceptor")
        connectionAcceptorField.isAccessible = true
        connectionAcceptorField.set(server, mockAcceptor)
        val clientManagerField = TcpServer::class.java.getDeclaredField("clientManager")
        clientManagerField.isAccessible = true
        clientManagerField.set(server, mockManager)

        every { mockSocket.isClosed } returns false
        every { mockSocket.close() } just Runs

        server.stop()

        assertFalse(isRunningField.getBoolean(server))
    }

    @Test
    fun `stopInternal should handle IOException when closing ServerSocket`() {
        val mockAcceptor = mockk<ConnectionAcceptor>(relaxUnitFun = true)
        val mockManager = mockk<ClientManager>(relaxUnitFun = true)
        val mockSocket = mockk<ServerSocket>()

        val server = TcpServer(port, protocolHandler, threadPoolSize)
        val serverSocketField = TcpServer::class.java.getDeclaredField("serverSocket")
        serverSocketField.isAccessible = true
        serverSocketField.set(server, mockSocket)
        val connectionAcceptorField = TcpServer::class.java.getDeclaredField("connectionAcceptor")
        connectionAcceptorField.isAccessible = true
        connectionAcceptorField.set(server, mockAcceptor)
        val clientManagerField = TcpServer::class.java.getDeclaredField("clientManager")
        clientManagerField.isAccessible = true
        clientManagerField.set(server, mockManager)

        every { mockSocket.isClosed } returns false
        every { mockSocket.close() } throws IOException("Error closing socket")

        val stopInternalMethod = TcpServer::class.java.getDeclaredMethod("stopInternal")
        stopInternalMethod.isAccessible = true
        stopInternalMethod.invoke(server)

        verify { mockAcceptor.stop() }
        verify { mockManager.stop() }
        verify { mockSocket.close() }
    }
}