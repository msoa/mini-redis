package com.kanastra.miniredis.domain.command

import com.kanastra.miniredis.application.Storage

class ExpireCommand(
    override val command: String,
    private val storage: Storage
) : Command {

    fun execute(key: String, values: List<String>): Long? {
        return when (command.uppercase()) {
            "EXPIRE" -> executeExpire(key, values)
            "TTL" -> executeTtl(key, values)
            else -> throw IllegalArgumentException("Unknown expire command: $command")
        }
    }

    private fun executeExpire(key: String, values: List<String>): Long {
        require(values.size == 1) { "wrong number of arguments for EXPIRE" }
        val ttl = values[0].toLongOrNull() ?: throw IllegalArgumentException("value is not an integer or out of range")
        return storage.expire(key, ttl) ?: -1
    }

    private fun executeTtl(key: String, values: List<String>): Long? {
        require(values.isEmpty()) { "wrong number of arguments for TTL" }
        return storage.ttl(key)
    }
}