package com.kanastra.miniredis.domain.storage

import com.kanastra.miniredis.application.Storage
import java.util.concurrent.ConcurrentHashMap

class InMemoryStorage(private val expireManager: ExpireManager) : Storage {
    private val data = ConcurrentHashMap<String, MutableList<String>>()
    private val expirations = ConcurrentHashMap<String, Long>()

    init {
        expireManager.start(this)
    }

    override fun exists(key: String): Boolean =
        data[key]?.isNotEmpty() ?: false

    override fun save(key: String, value: String): String {
        data[key] = mutableListOf(value)
        expirations.remove(key)
        return "OK"
    }

    override fun get(key: String): String? {
        return data[key]?.get(0)
    }

    override fun leftPush(key: String, list: List<String>): Long {
        data.computeIfAbsent(key) { mutableListOf() }.addAll(0, list.reversed())
        expirations.remove(key)
        return data[key]?.size?.toLong() ?: 0L
    }

    override fun rightPush(key: String, list: List<String>): Long {
        data.computeIfAbsent(key) { mutableListOf() }.addAll(list)
        expirations.remove(key)
        return data[key]?.size?.toLong() ?: 0L
    }

    override fun leftPop(key: String, count: Int): List<String>? {
        if (data[key].isNullOrEmpty()) return null
        val list = data[key] ?: return null
        val startAdjusted = 0
        val endAdjusted = count.coerceAtMost(list.size)
        return list.subList(startAdjusted, endAdjusted)
    }

    override fun rightPop(key: String, count: Int): List<String>? {
        if (data[key].isNullOrEmpty()) return null
        val list = data[key] ?: return null
        val startAdjusted = (list.size - count).coerceAtLeast(0)
        val endAdjusted = list.size
        return list.subList(startAdjusted, endAdjusted)
    }

    override fun leftRange(key: String, start: Int, end: Int): List<String>? {
        val list = data[key] ?: return null
        val startAdjusted = start.coerceAtLeast(0)
        val endAdjusted = (end + 1).coerceAtMost(list.size)
        return list.subList(startAdjusted, endAdjusted)
    }

    override fun expire(key: String, ttl: Long): Long {
        expirations[key] = System.currentTimeMillis() + ttl * 1000
        return 1L
    }

    override fun ttl(key: String): Long? {
        val expiration = expirations[key] ?: return null
        val remaining = (expiration - System.currentTimeMillis()) / 1000
        return if (remaining > 0) remaining else null
    }

    fun cleanupExpiredKeys() {
        val currentTime = System.currentTimeMillis()
        val expiredKeys = expirations.filter { (_, expirationTime) ->
            currentTime > expirationTime
        }.keys

        expiredKeys.forEach { key ->
            data.remove(key)
            expirations.remove(key)
        }
    }
}