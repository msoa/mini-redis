package com.kanastra.miniredis.domain.storage

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class SchedulerExpireManager : ExpireManager {
    private val scheduler = Executors.newScheduledThreadPool(1)

    override fun start(storage: InMemoryStorage, initialDelayInSec: Long, periodInSec: Long) {
        scheduler.scheduleAtFixedRate({
            storage.cleanupExpiredKeys()
        }, initialDelayInSec, periodInSec, TimeUnit.SECONDS)
    }

    override fun stop() {
        if (!scheduler.isShutdown)
            scheduler.shutdown()
    }
}