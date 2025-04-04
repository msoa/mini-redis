package com.kanastra.miniredis.application

interface Storage {
    fun exists(key: String): Boolean
    fun save(key: String, value: String): String
    fun get(key: String): String?
    fun leftPush(key: String, list: List<String>): Long
    fun rightPush(key: String, list: List<String>): Long
    fun leftPop(key: String, count: Int): List<String>?
    fun rightPop(key: String, count: Int): List<String>?
    fun leftRange(key: String, start: Int, end: Int): List<String>?
    fun expire(key: String, ttl: Long): Long?
    fun ttl(key: String): Long?
}