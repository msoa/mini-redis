package com.kanastra.miniredis.domain.storage

interface ExpireManager {
    fun start(storage: InMemoryStorage, initialDelayInSec: Long = 0, periodInSec: Long = 1)
    fun stop()
}