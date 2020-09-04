package com.wire.backups.exports.android.model

import com.wire.backups.exports.android.database.dto.DatabaseDto

/**
 * Data extracted from the database including metadata and messages.
 */
data class AndroidDatabaseExportDto(
    /**
     * Information about export and the database.
     */
    val exportMetadata: ExportMetadata,
    /**
     * Database export.
     */
    val database: DatabaseDto
)
