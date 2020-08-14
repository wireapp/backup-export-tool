package pw.forst.wire.backups.api

import java.io.File

/**
 * Common interface for all clients exports.
 */
interface BackupExport<T> {

    /**
     * Decrypts the database and provides access to the decrypted database.
     */
    fun decryptDatabase(): File

    /**
     * Decrypts backup and extracts database information.
     */
    fun exportDatabase(): T
}
