package com.wire.backups.exports.api

/**
 * Common interface for all clients exports.
 */
interface BackupExport<T> {

    /**
     * Decrypts backup and extracts database information.
     */
    fun exportDatabase(): T
}
