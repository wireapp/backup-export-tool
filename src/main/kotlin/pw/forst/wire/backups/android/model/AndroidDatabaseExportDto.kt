package pw.forst.wire.backups.android.model

import pw.forst.wire.backups.android.database.dto.DatabaseDto
import pw.forst.wire.backups.android.steps.ExportMetadata

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
