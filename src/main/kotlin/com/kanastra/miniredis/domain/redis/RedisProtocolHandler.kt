package com.kanastra.miniredis.domain.redis

import com.kanastra.miniredis.application.ProtocolHandler
import com.kanastra.miniredis.application.Storage
import com.kanastra.miniredis.domain.command.ExpireCommand
import com.kanastra.miniredis.domain.command.ListCommand
import com.kanastra.miniredis.domain.command.StringCommand
import java.util.logging.Logger

const val ERROR = "$-1\r\n"

class RedisProtocolHandler(private val storage: Storage) : ProtocolHandler {
    private val logger = Logger.getLogger(this.javaClass.name)
    private val crlf = "\r\n"

    private val commands = mapOf(
        "SET" to StringCommand("SET", storage),
        "GET" to StringCommand("GET", storage),
        "LPUSH" to ListCommand("LPUSH", storage),
        "RPUSH" to ListCommand("RPUSH", storage),
        "LPOP" to ListCommand("LPOP", storage),
        "RPOP" to ListCommand("RPOP", storage),
        "LRANGE" to ListCommand("LRANGE", storage),
        "EXPIRE" to ExpireCommand("EXPIRE", storage),
        "TTL" to ExpireCommand("TTL", storage)
    )

    override fun handleMessage(message: String): String {
        val list = message.trim().split(crlf).map(::handleCommand)
        return list.joinToString(separator = "")
    }

    private fun handleCommand(message: String): String {
        val (commandName, key, values) = parseCommand(message) ?: return ERROR

        val command = commands[commandName] ?: return "-ERR unknown command '${commandName.uppercase()}'\r\n"

        return try {
            when (command) {
                is StringCommand -> formatResponse(command.execute(key, values))
                is ListCommand -> formatResponse(command.execute(key, values))
                is ExpireCommand -> formatResponse(command.execute(key, values))
                else -> "-ERR unknown command '${command.command.uppercase()}'\r\n"
            }
        } catch (e: IllegalArgumentException) {
            "-ERR ${e.message}\r\n"
        } catch (e: Exception) {
            "-ERR internal server error\r\n"
        }
    }

    private fun parseCommand(message: String): Triple<String, String, List<String>>? {
        val parts = message.replace(crlf, "").trim().split(" ")
        if (parts.size < 2) return null

        val command = parts[0].uppercase()
        val key = parts[1]
        val values = parts.subList(2, parts.size)

        return Triple(command, key, values)
    }

    private fun formatResponse(response: String?): String {
        return response?.let { "+$it\r\n" } ?: "-ERR no result\r\n"
    }

    private fun formatResponse(response: List<String>?): String {
        return response?.let {
            "*${it.size}${it.map { item -> "$crlf\$${item.length}$crlf$item" }.joinToString(separator = "")}\r\n"
        } ?: "*-1\r\n"
    }

    private fun formatResponse(response: Long?): String {
        return response?.let { ":$it\r\n" } ?: ":-1\r\n"
    }

    override fun handleException(e: Exception): String =
        "-ERR internal server error '${e.message}'\r\n"
}
