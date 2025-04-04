package com.kanastra.miniredis.infrastructure.handler

import com.kanastra.miniredis.application.ProtocolHandler
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.*
import java.net.InetAddress
import java.net.Socket

class ClientProcessorTest {

    private lateinit var socket: Socket
    private lateinit var protocolHandler: ProtocolHandler
    private lateinit var clientProcessor: ClientProcessor
    private lateinit var reader: BufferedReader
    private lateinit var outputStream: ByteArrayOutputStream

    @BeforeEach
    fun setUp() {
        socket = mock()
        protocolHandler = mock()
        outputStream = ByteArrayOutputStream()

        whenever(socket.inetAddress).thenReturn(mock<InetAddress>())
        whenever(socket.getOutputStream()).thenReturn(outputStream)
        clientProcessor = ClientProcessor(socket, protocolHandler)
    }

    @Test
    fun `handleClient processes valid message and sends response`() {
        val input = "TEST\r\n"
        val stringReader = StringReader(input)
        reader = BufferedReader(stringReader)
        val byteArrayInputStream = ByteArrayInputStream(input.toByteArray())

        whenever(socket.isInputShutdown).thenReturn(false, true)
        whenever(socket.getInputStream()).thenReturn(byteArrayInputStream)
        whenever(protocolHandler.handleMessage(anyString())).thenReturn("OK\r\n")

        clientProcessor = ClientProcessor(socket, protocolHandler)
        clientProcessor.run()

        assertEquals("OK\r\n", outputStream.toString())
    }

    @Test
    fun `handleClient handles exception during message processing`() {
        val testException = IOException("Test IOException")
        val input = "TEST\r\n"
        val stringReader = StringReader(input)
        reader = BufferedReader(stringReader)
        val byteArrayInputStream = ByteArrayInputStream(input.toByteArray())

        whenever(socket.isInputShutdown).thenReturn(false, true)
        whenever(socket.getInputStream()).thenReturn(byteArrayInputStream)
        whenever(protocolHandler.handleMessage(anyString())).thenThrow(RuntimeException("Test Exception"))
        whenever(protocolHandler.handleException(eq(testException))).thenReturn("-ERR IOException\r\n")

        clientProcessor.run()
    }

    @Test
    fun `handleClient handles IOException`() {
        val testException = IOException("Test IOException")
        whenever(socket.getInputStream()).thenThrow(testException)
        whenever(protocolHandler.handleException(eq(testException))).thenReturn("-ERR IOException\r\n")

        clientProcessor.run()

        assertEquals("-ERR IOException\r\n", outputStream.toString())
    }
}