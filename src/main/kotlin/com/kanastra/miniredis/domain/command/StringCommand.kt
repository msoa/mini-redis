package com.kanastra.miniredis.domain.command

import com.kanastra.miniredis.application.Storage

class StringCommand(
    override val command: String,
    private val storage: Storage
) : Command {

    fun execute(key: String, values: List<String>): String? {
        return when (command.uppercase()) {
            "GET" -> executeGet(key, values)
            "SET" -> executeSet(key, values)
            else -> throw IllegalArgumentException("Unknown string command: $command")
        }
    }

    private fun executeGet(key: String, values: List<String>): String? {
        require(values.isEmpty()) { "wrong number of arguments for GET" }
        return storage.get(key)
    }

    private fun executeSet(key: String, values: List<String>): String? {
        require(values.isNotEmpty()) { "wrong number of arguments for SET" }
        var create = true
        var override = true
        var get = false
        values.subList(1, values.size).forEach {
            when (it.uppercase()) {
                "NX" -> override = false
                "XX" -> create = false
                "GET" -> get = true
            }
        }

        val oldValue = if (get) storage.get(key) else null
        val resp: String? = if (shouldSave(override, create, storage.exists(key))) {
            storage.save(key, values[0])
        } else {
            null
        }

        return if (get)
            oldValue
        else
            resp
    }

    private fun shouldSave(override: Boolean, create: Boolean, keyExists: Boolean): Boolean =
        (override || !keyExists) && (create || keyExists)
}