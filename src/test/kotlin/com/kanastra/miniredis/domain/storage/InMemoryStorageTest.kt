package com.kanastra.miniredis.domain.storage

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class InMemoryStorageTest {

    private lateinit var storage: InMemoryStorage
    private lateinit var expireManager: ExpireManager

    @BeforeEach
    fun setUp() {
        expireManager = mock()
        storage = InMemoryStorage(expireManager)
    }

    @Test
    fun `save and get string`() {
        storage.save("key", "value")
        assertEquals("value", storage.get("key"))
    }

    @Test
    fun `leftPush and leftPop list`() {
        storage.leftPush("list", listOf("value1", "value2"))
        val popped = storage.leftPop("list", 1)
        assertEquals(listOf("value2"), popped)
    }

    @Test
    fun `rightPush and rightPop list`() {
        storage.rightPush("list", listOf("value1", "value2"))
        val popped = storage.rightPop("list", 1)
        assertEquals(listOf("value2"), popped)
    }

    @Test
    fun `leftRange list`() {
        storage.leftPush("list", listOf("value1", "value2", "value3"))
        val range = storage.leftRange("list", 0, 1)
        assertEquals(listOf("value3", "value2"), range)
    }

    @Test
    fun `cleanupExpiredKeys removes expired keys`() {
        storage.save("key1", "value1")
        storage.save("key2", "value2")
        storage.expire("key1", 1)
        Thread.sleep(1500)
        storage.cleanupExpiredKeys()
        assertNull(storage.get("key1"))
        assertNotNull(storage.get("key2"))
    }

    @Test
    fun `exists return true when key exists and false when not`() {
        storage.save("key", "value")
        assertTrue(storage.exists("key"))
        assertFalse(storage.exists("not_key"))
    }
}