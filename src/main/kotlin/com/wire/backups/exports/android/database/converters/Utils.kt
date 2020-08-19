package com.wire.backups.exports.android.database.converters

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * String date format in yyyy-MM-dd'T'HH:mm:ss.SSS'Z' format.
 */
typealias ExportDate = String

internal fun Long.toExportDateFromAndroid(): ExportDate = Instant.ofEpochMilli(this).let { dateFormatter.format(it) }

internal fun Double.toExportDateFromIos(): ExportDate =
    // iOS has custom time format - see https://stackoverflow.com/a/54914712/7169288
    Instant.ofEpochSecond(this.toLong() + 978307200L).let { dateFormatter.format(it) }


internal fun String.toUuid(): UUID = UUID.fromString(this)

private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    .withZone(ZoneOffset.UTC)
