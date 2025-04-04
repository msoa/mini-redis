package com.kanastra.miniredis.application

interface ProtocolHandler {
    fun handleMessage(message: String): String
    fun handleException(e: Exception): String
}