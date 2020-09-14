package com.wire.backups.exports.ios

import com.wire.backups.exports.utils.ExportDate
import com.wire.backups.exports.utils.dateFormatter
import java.time.Instant

internal fun Double.toExportDateFromIos(): ExportDate =
    // iOS has custom time format - see https://stackoverflow.com/a/54914712/7169288
    Instant.ofEpochSecond(this.toLong() + 978307200L)
        .let { dateFormatter.format(it) }
