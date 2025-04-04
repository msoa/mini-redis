package com.kanastra.miniredis.domain.command

import com.kanastra.miniredis.application.Storage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class ListCommandTest {

    private lateinit var storage: Storage
    private lateinit var lPushCommand: ListCommand
    private lateinit var rPushCommand: ListCommand
    private lateinit var lPopCommand: ListCommand
    private lateinit var rPopCommand: ListCommand
    private lateinit var lRangeCommand: ListCommand

    @BeforeEach
    fun setUp() {
        storage = mock<Storage>()
        lPushCommand = ListCommand("LPUSH", storage)
        rPushCommand = ListCommand("RPUSH", storage)
        lPopCommand = ListCommand("LPOP", storage)
        rPopCommand = ListCommand("RPOP", storage)
        lRangeCommand = ListCommand("LRANGE", storage)
    }

    @Test
    fun `execute LPUSH with valid arguments`() {
        whenever(storage.leftPush(any(), any())).thenReturn(2L)

        val result = lPushCommand.execute("key", listOf("value1", "value2"))

        assertEquals(listOf("2"), result)
        verify(storage).leftPush("key", listOf("value1", "value2"))
    }

    @Test
    fun `execute LPUSH with invalid number of arguments`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            lPushCommand.execute("key", emptyList())
        }

        assertEquals("wrong number of arguments for LPUSH", exception.message)
        verify(storage, never()).leftPush(any(), any())
    }

    @Test
    fun `execute RPUSH with valid arguments`() {
        whenever(storage.rightPush(any(), any())).thenReturn(2L)

        val result = rPushCommand.execute("key", listOf("value1", "value2"))

        assertEquals(listOf("2"), result)
        verify(storage).rightPush("key", listOf("value1", "value2"))
    }

    @Test
    fun `execute RPUSH with invalid number of arguments`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            rPushCommand.execute("key", emptyList())
        }

        assertEquals("wrong number of arguments for RPUSH", exception.message)
        verify(storage, never()).rightPush(any(), any())
    }

    @Test
    fun `execute LPOP with valid arguments`() {
        whenever(storage.leftPop(any(), any())).thenReturn(listOf("value1", "value2"))

        val result = lPopCommand.execute("key", listOf("2"))

        assertEquals(listOf("value1", "value2"), result)
        verify(storage).leftPop("key", 2)
    }

    @Test
    fun `execute LPOP with default count`() {
        whenever(storage.leftPop(any(), any())).thenReturn(listOf("value1"))

        val result = lPopCommand.execute("key", emptyList())

        assertEquals(listOf("value1"), result)
        verify(storage).leftPop("key", 1)
    }

    @Test
    fun `execute LPOP with invalid number of arguments`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            lPopCommand.execute("key", listOf("1", "2"))
        }

        assertEquals("wrong number of arguments for LPOP", exception.message)
        verify(storage, never()).leftPop(any(), any())
    }

    @Test
    fun `execute RPOP with valid arguments`() {
        whenever(storage.rightPop(any(), any())).thenReturn(listOf("value1", "value2"))

        val result = rPopCommand.execute("key", listOf("2"))

        assertEquals(listOf("value1", "value2"), result)
        verify(storage).rightPop("key", 2)
    }

    @Test
    fun `execute RPOP with default count`() {
        whenever(storage.rightPop(any(), any())).thenReturn(listOf("value1"))

        val result = rPopCommand.execute("key", emptyList())

        assertEquals(listOf("value1"), result)
        verify(storage).rightPop("key", 1)
    }

    @Test
    fun `execute RPOP with invalid number of arguments`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            rPopCommand.execute("key", listOf("1", "2"))
        }

        assertEquals("wrong number of arguments for RPOP", exception.message)
        verify(storage, never()).rightPop(any(), any())
    }

    @Test
    fun `execute LRANGE with valid arguments`() {
        whenever(storage.leftRange(any(), any(), any())).thenReturn(listOf("value1", "value2"))

        val result = lRangeCommand.execute("key", listOf("0", "1"))

        assertEquals(listOf("value1", "value2"), result)
        verify(storage).leftRange("key", 0, 1)
    }

    @Test
    fun `execute LRANGE with invalid number of arguments`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            lRangeCommand.execute("key", listOf("0"))
        }

        assertEquals("wrong number of arguments for LRANGE", exception.message)
        verify(storage, never()).leftRange(any(), any(), any())
    }

    @Test
    fun `execute LRANGE with invalid start`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            lRangeCommand.execute("key", listOf("invalid", "1"))
        }

        assertEquals("start is not an integer or out of range", exception.message)
        verify(storage, never()).leftRange(any(), any(), any())
    }

    @Test
    fun `execute LRANGE with invalid end`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            lRangeCommand.execute("key", listOf("0", "invalid"))
        }

        assertEquals("end is not an integer or out of range", exception.message)
        verify(storage, never()).leftRange(any(), any(), any())
    }

    @Test
    fun `execute unknown command`() {
        val listCommand = ListCommand("UNKNOWN", storage)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            listCommand.execute("key", emptyList())
        }

        assertEquals("Unknown list command: UNKNOWN", exception.message)
        verify(storage, never()).leftPush(any(), any())
        verify(storage, never()).rightPush(any(), any())
        verify(storage, never()).leftPop(any(), any())
        verify(storage, never()).rightPop(any(), any())
        verify(storage, never()).leftRange(any(), any(), any())
    }
}