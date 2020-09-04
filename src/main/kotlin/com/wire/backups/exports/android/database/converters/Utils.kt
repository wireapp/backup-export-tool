package com.wire.backups.exports.android.database.converters

import com.wire.backups.exports.utils.ExportDate
import com.wire.backups.exports.utils.dateFormatter
import java.time.Instant


internal fun Long.toExportDateFromAndroid(): ExportDate = Instant.ofEpochMilli(this)
    .let { dateFormatter.format(it) }
