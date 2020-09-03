package com.wire.backups.exports.android.database.v2.converters

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * String date format in yyyy-MM-dd'T'HH:mm:ss.SSS'Z' format.
 */
internal typealias ExportDate = String

internal fun Long.toExportDateFromAndroid(): ExportDate = Instant.ofEpochMilli(this).let { dateFormatter.format(it) }

internal fun String.toUuid(): UUID = UUID.fromString(this)

private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    .withZone(ZoneOffset.UTC)
