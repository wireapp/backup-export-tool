package com.wire.backups.exports.android.database.converters

import com.wire.backups.exports.utils.ExportDate
import com.wire.backups.exports.utils.dateFormatter
import mu.KLogger
import mu.KLogging
import java.time.Instant

val parsingLogger: KLogger
    get() = KLogging().logger("com.wire.backups.exports.android")


internal fun Long.toExportDateFromAndroid(): ExportDate = Instant.ofEpochMilli(this)
    .let { dateFormatter.format(it) }
