package com.kanastra.miniredis.domain.storage

import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ExpireManagerTest {

    private lateinit var expireManager: ExpireManager
    private lateinit var storage: InMemoryStorage

    @BeforeEach
    fun setUp() {
        expireManager = SchedulerExpireManager()
        storage = mockk(relaxed = true)
    }

    @AfterEach
    fun tearDown() {
        expireManager.stop()
    }

    @Test
    fun `start should schedule cleanupExpiredKeys`() {
        expireManager.start(storage)
        Thread.sleep(2000)
        verify(atLeast = 1) { storage.cleanupExpiredKeys() }
    }

    @Test
    fun `start should schedule with custom delays`() {
        expireManager.start(storage, 10, 5)
        Thread.sleep(11000)
        verify(atLeast = 1) { storage.cleanupExpiredKeys() }
    }
}