package com.kanastra.miniredis.domain.command

import com.kanastra.miniredis.application.Storage

class ListCommand(
    override val command: String,
    private val storage: Storage
) : Command {

    fun execute(key: String, values: List<String>): List<String>? {
        return when (command.uppercase()) {
            "LPUSH" -> executeLeftPush(key, values)
            "RPUSH" -> executeRightPush(key, values)
            "LPOP" -> executeLeftPop(key, values)
            "RPOP" -> executeRightPop(key, values)
            "LRANGE" -> executeLeftRange(key, values)
            else -> throw IllegalArgumentException("Unknown list command: $command")
        }
    }

    private fun executeLeftPush(key: String, values: List<String>): List<String> {
        require(values.isNotEmpty()) { "wrong number of arguments for LPUSH" }
        return listOf(storage.leftPush(key, values).toString())
    }

    private fun executeRightPush(key: String, values: List<String>): List<String> {
        require(values.isNotEmpty()) { "wrong number of arguments for RPUSH" }
        return listOf(storage.rightPush(key, values).toString())
    }

    private fun executeLeftPop(key: String, values: List<String>): List<String>? {
        require(values.size <= 1) { "wrong number of arguments for LPOP" }
        val count = values.firstOrNull()?.toIntOrNull() ?: 1
        return storage.leftPop(key, count)
    }

    private fun executeRightPop(key: String, values: List<String>): List<String>? {
        require(values.size <= 1) { "wrong number of arguments for RPOP" }
        val count = values.firstOrNull()?.toIntOrNull() ?: 1
        return storage.rightPop(key, count)
    }

    private fun executeLeftRange(key: String, values: List<String>): List<String>? {
        require(values.size == 2) { "wrong number of arguments for LRANGE" }
        val start = values[0].toIntOrNull() ?: throw IllegalArgumentException("start is not an integer or out of range")
        val end = values[1].toIntOrNull() ?: throw IllegalArgumentException("end is not an integer or out of range")
        return storage.leftRange(key, start, end)
    }
}