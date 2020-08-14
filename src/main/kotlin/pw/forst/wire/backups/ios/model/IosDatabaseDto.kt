package pw.forst.wire.backups.ios.model

import pw.forst.wire.backups.android.database.converters.ExportDate
import java.io.File
import java.util.UUID

/**
 * Metadata about database.
 */
data class IosDatabaseDto(
    /**
     * User who owns this database.
     */
    val userId: UUID,
    /**
     * ID of the device from which was export created.
     */
    val clientIdentifier: String,
    /**
     * Version of the model export.
     */
    val modelVersion: String,
    /**
     * Version of the application.
     */
    val appVersion: String,
    /**
     * When the export was created.
     */
    val creationTime: ExportDate,
    /**
     * Platform - iOS.
     */
    val platform: String,
    /**
     * File where the SQLite database is stored.
     */
    val databaseFile: File
)
