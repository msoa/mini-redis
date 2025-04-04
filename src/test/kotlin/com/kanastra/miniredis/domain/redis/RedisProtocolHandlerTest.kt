package com.kanastra.miniredis.domain.redis

import com.kanastra.miniredis.application.Storage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class RedisProtocolHandlerTest {

    private lateinit var storage: Storage
    private lateinit var protocolHandler: RedisProtocolHandler

    @BeforeEach
    fun setUp() {
        storage = mock<Storage>()
        protocolHandler = RedisProtocolHandler(storage)
    }

    @Test
    fun `handleMessage with SET command`() {
        whenever(storage.save(any(), any())).thenReturn("OK")

        val result = protocolHandler.handleMessage("SET key value\r\n")

        assertEquals("+OK\r\n", result)
        verify(storage).save("key", "value")
    }

    @Test
    fun `handleMessage with GET command`() {
        whenever(storage.get(any())).thenReturn("value")

        val result = protocolHandler.handleMessage("GET key\r\n")

        assertEquals("+value\r\n", result)
        verify(storage).get("key")
    }

    @Test
    fun `handleMessage with LPUSH command`() {
        whenever(storage.leftPush(any(), any())).thenReturn(2L)

        val result = protocolHandler.handleMessage("LPUSH key value1 value2\r\n")

        assertEquals("*1\r\n$1\r\n2\r\n", result)
        verify(storage).leftPush("key", listOf("value1", "value2"))
    }

    @Test
    fun `handleMessage with LRANGE command`() {
        whenever(storage.leftRange(any(), any(), any())).thenReturn(listOf("value1", "value2"))

        val result = protocolHandler.handleMessage("LRANGE key 0 1\r\n")

        assertEquals("*2\r\n$6\r\nvalue1\r\n$6\r\nvalue2\r\n", result)
        verify(storage).leftRange("key", 0, 1)
    }

    @Test
    fun `handleMessage with EXPIRE command`() {
        whenever(storage.expire(any(), any())).thenReturn(1L)

        val result = protocolHandler.handleMessage("EXPIRE key 10\r\n")

        assertEquals(":1\r\n", result)
        verify(storage).expire("key", 10L)
    }

    @Test
    fun `handleMessage with TTL command`() {
        whenever(storage.ttl(any())).thenReturn(10L)

        val result = protocolHandler.handleMessage("TTL key\r\n")

        assertEquals(":10\r\n", result)
        verify(storage).ttl("key")
    }

    @Test
    fun `handleMessage with unknown command`() {
        val result = protocolHandler.handleMessage("UNKNOWN key\r\n")

        assertEquals("-ERR unknown command 'UNKNOWN'\r\n", result)
        verify(storage, never()).get(any())
        verify(storage, never()).save(any(), any())
    }

    @Test
    fun `handleMessage with invalid number of arguments`() {
        val result = protocolHandler.handleMessage("GET\r\n")

        assertEquals(ERROR, result)
        verify(storage, never()).get(any())
    }

    @Test
    fun `handleMessage with IllegalArgumentException`() {
        whenever(storage.get(any())).thenThrow(IllegalArgumentException("invalid argument"))

        val result = protocolHandler.handleMessage("GET key\r\n")

        assertEquals("-ERR invalid argument\r\n", result)
        verify(storage).get("key")
    }

    @Test
    fun `handleMessage with generic exception`() {
        whenever(storage.get(any())).thenThrow(RuntimeException("internal error"))

        val result = protocolHandler.handleMessage("GET key\r\n")

        assertEquals("-ERR internal server error\r\n", result)
        verify(storage).get("key")
    }

    @Test
    fun `handleException`() {
        val exception = RuntimeException("test exception")
        val result = protocolHandler.handleException(exception)

        assertEquals("-ERR internal server error 'test exception'\r\n", result)
    }
}