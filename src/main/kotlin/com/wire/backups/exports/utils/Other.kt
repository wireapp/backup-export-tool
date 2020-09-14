package com.wire.backups.exports.utils

import mu.KLogger
import mu.KLogging
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.reflect.full.primaryConstructor

/**
 * String date format in yyyy-MM-dd'T'HH:mm:ss.SSS'Z' format.
 */
internal typealias ExportDate = String

internal val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    .withZone(ZoneOffset.UTC)


inline fun <reified T : Any> primaryConstructorParameters() =
    T::class.primaryConstructor?.parameters?.associateBy { it.name }


val transactionsLogger: KLogger
    get() = KLogging().logger("com.wire.backups.exports.db")

val catchingLogger: KLogger
    get() = KLogging().logger("com.wire.backups.exports")


inline fun <T, R> Iterable<T>.mapCatching(transform: (T) -> R?): List<R> =
    mapNotNull { runCatching { transform(it) }.getOrNull() }

inline fun <T, R> Iterable<T>.mapCatching(transform: (T) -> R?, crossinline errorLog: (T) -> String): List<R> =
    mapNotNull { item ->
        runCatching { transform(item) }
            .onFailure {
                catchingLogger.error { errorLog(item) }
                catchingLogger.debug(it) { }
            }
            .getOrNull()
    }

inline fun <T, R> Iterable<T>.mapCatching(errorLog: String, transform: (T) -> R?): List<R> =
    mapCatching(transform, { errorLog })


val rowExportFailed: (Any) -> String = { "It was not possible to map record:\n$it" }


inline fun <K, V, R> Map<out K, V>.mapCatching(transform: (Map.Entry<K, V>) -> R?): List<R> =
    this.mapNotNull { runCatching { transform(it) }.getOrNull() }

inline fun <K, V, R> Map<out K, V>.mapCatching(
    transform: (Map.Entry<K, V>) -> R?,
    crossinline errorLog: (Map.Entry<K, V>) -> String
): List<R> =
    this.mapNotNull { item ->
        runCatching { transform(item) }
            .onFailure {
                catchingLogger.error { errorLog(item) }
                catchingLogger.debug(it) { }
            }
            .getOrNull()
    }

