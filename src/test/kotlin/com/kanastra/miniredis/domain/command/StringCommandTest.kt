package com.kanastra.miniredis.domain.command

import com.kanastra.miniredis.application.Storage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class StringCommandTest {

    private lateinit var storage: Storage
    private lateinit var getCommand: StringCommand
    private lateinit var setCommand: StringCommand

    @BeforeEach
    fun setUp() {
        storage = mock<Storage>()
        getCommand = StringCommand("GET", storage)
        setCommand = StringCommand("SET", storage)
    }

    @Test
    fun `execute GET with valid arguments`() {
        whenever(storage.get(any())).thenReturn("value")

        val result = getCommand.execute("key", emptyList())

        assertEquals("value", result)
        verify(storage).get("key")
    }

    @Test
    fun `execute GET with invalid number of arguments`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            getCommand.execute("key", listOf("value"))
        }

        assertEquals("wrong number of arguments for GET", exception.message)
        verify(storage, never()).get(any())
    }

    @Test
    fun `execute SET with valid arguments`() {
        whenever(storage.save(any(), any())).thenReturn("OK")

        val result = setCommand.execute("key", listOf("value"))

        assertEquals("OK", result)
        verify(storage).save("key", "value")
    }

    @Test
    fun `execute SET with NX flag and key does not exist`() {
        whenever(storage.exists(any())).thenReturn(false)
        whenever(storage.save(any(), any())).thenReturn("OK")

        val result = setCommand.execute("key", listOf("value", "NX"))

        assertEquals("OK", result)
        verify(storage).save("key", "value")
    }

    @Test
    fun `execute SET with NX flag and key exists`() {
        whenever(storage.exists(any())).thenReturn(true)

        val result = setCommand.execute("key", listOf("value", "NX"))

        assertEquals(null, result)
        verify(storage, never()).save(any(), any())
    }

    @Test
    fun `execute SET with XX flag and key exists`() {
        whenever(storage.exists(any())).thenReturn(true)
        whenever(storage.save(any(), any())).thenReturn("OK")

        val result = setCommand.execute("key", listOf("value", "XX"))

        assertEquals("OK", result)
        verify(storage).save("key", "value")
    }

    @Test
    fun `execute SET with XX flag and key does not exist`() {
        whenever(storage.exists(any())).thenReturn(false)

        val result = setCommand.execute("key", listOf("value", "XX"))

        assertEquals(null, result)
        verify(storage, never()).save(any(), any())
    }

    @Test
    fun `execute SET with GET flag`() {
        whenever(storage.get(any())).thenReturn("oldValue")
        whenever(storage.save(any(), any())).thenReturn("OK")

        val result = setCommand.execute("key", listOf("value", "GET"))

        assertEquals("oldValue", result)
        verify(storage).save("key", "value")
    }

    @Test
    fun `execute SET with invalid number of arguments`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            setCommand.execute("key", emptyList())
        }

        assertEquals("wrong number of arguments for SET", exception.message)
        verify(storage, never()).save(any(), any())
    }

    @Test
    fun `execute unknown command`() {
        getCommand = StringCommand("UNKNOWN", storage)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            getCommand.execute("key", emptyList())
        }

        assertEquals("Unknown string command: UNKNOWN", exception.message)
        verify(storage, never()).get(any())
        verify(storage, never()).save(any(), any())
    }
}