package com.wire.backups.exports.android.steps

import com.wire.backups.exports.android.decryption.decryptAndroidBackup
import com.wire.backups.exports.android.model.ExportMetadata
import java.io.File
import java.util.UUID


/**
 * Decrypts the backup and export the database.
 * Returns database file or null when it was not possible to extract the database.
 */
internal fun decryptAndExtract(databaseFile: File, password: String, userId: UUID, pathToNewFolder: String = "tmp"): DecryptionResult {
    val file = decryptAndroidBackup(databaseFile, userId.toString(), password)
    val (metadata, db) = extractBackup(file, pathToNewFolder)
    return DecryptionResult(metadata, db)
}

internal data class DecryptionResult(
    val metadata: ExportMetadata,
    val databaseFile: File
)
