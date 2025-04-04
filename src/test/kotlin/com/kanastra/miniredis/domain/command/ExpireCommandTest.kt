package com.kanastra.miniredis.domain.command

import com.kanastra.miniredis.application.Storage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class ExpireCommandTest {

    private lateinit var storage: Storage
    private lateinit var expireCommand: ExpireCommand
    private lateinit var ttlCommand: ExpireCommand

    @BeforeEach
    fun setUp() {
        storage = mock<Storage>()
        expireCommand = ExpireCommand("EXPIRE", storage)
        ttlCommand = ExpireCommand("TTL", storage)
    }

    @Test
    fun `execute EXPIRE with valid arguments`() {
        whenever(storage.expire(any(), any())).thenReturn(1L)

        val result = expireCommand.execute("key", listOf("10"))

        assertEquals(1L, result)
        verify(storage).expire("key", 10L)
    }

    @Test
    fun `execute EXPIRE with invalid number of arguments`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            expireCommand.execute("key", listOf("10", "20"))
        }

        assertEquals("wrong number of arguments for EXPIRE", exception.message)
        verify(storage, never()).expire(any(), any())
    }

    @Test
    fun `execute EXPIRE with invalid ttl`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            expireCommand.execute("key", listOf("invalid"))
        }

        assertEquals("value is not an integer or out of range", exception.message)
        verify(storage, never()).expire(any(), any())
    }

    @Test
    fun `execute TTL with valid arguments`() {
        whenever(storage.ttl(any())).thenReturn(10L)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            ttlCommand.execute("key", listOf("value"))
        }

        assertEquals("wrong number of arguments for TTL", exception.message)
        verify(storage, never()).ttl(any())
    }

    @Test
    fun `execute TTL with invalid number of arguments`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            ttlCommand.execute("key", listOf("10"))
        }

        assertEquals("wrong number of arguments for TTL", exception.message)
        verify(storage, never()).ttl(any())
    }

    @Test
    fun `execute unknown command`() {
        expireCommand = ExpireCommand("UNKNOWN", storage)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            expireCommand.execute("key", emptyList())
        }

        assertEquals("Unknown expire command: UNKNOWN", exception.message)
        verify(storage, never()).ttl(any())
    }
}