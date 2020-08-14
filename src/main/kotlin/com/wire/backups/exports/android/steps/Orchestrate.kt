package com.wire.backups.exports.android.steps

import java.io.File
import java.util.UUID

/**
 * Decrypts the backup and export the database.
 * Returns database file or null when it was not possible to extract the database.
 */
fun decryptAndExtract(databaseFilePath: String, password: String, userId: String, pathToNewFolder: String = "tmp") =
    decryptAndExtract(
        File(databaseFilePath),
        password.toByteArray(),
        UUID.fromString(userId),
        pathToNewFolder
    )

/**
 * Decrypts the backup and export the database.
 * Returns database file or null when it was not possible to extract the database.
 */
internal fun decryptAndExtract(databaseFile: File, password: ByteArray, userId: UUID, pathToNewFolder: String = "tmp"): DecryptionResult =
    decryptDatabase(databaseFile, password, userId).let {
        val (metadata, db) = extractBackup(it, userId, pathToNewFolder)
        DecryptionResult(metadata, db)
    }

data class DecryptionResult(
    val metadata: ExportMetadata,
    val databaseFile: File
)
