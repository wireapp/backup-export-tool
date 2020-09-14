package com.wire.backups.exports.android.decryption

import com.wire.backups.exports.android.model.ExportMetadata
import net.lingala.zip4j.ZipFile
import pw.forst.tools.katlib.parseJson
import java.io.File
import java.util.UUID


/**
 * Decrypts the backup and export the database.
 * Returns database file or null when it was not possible to extract the database.
 */
internal fun decryptAndExtractAndroidBackup(
    databaseFile: File,
    password: String,
    userId: UUID,
    pathToNewFolder: String = "tmp"
): DecryptionResult {
    val file = decryptAndroidBackup(databaseFile, userId.toString(), password)
    val (metadata, db) = extractBackup(file, pathToNewFolder)
    return DecryptionResult(metadata, db)
}


/**
 * Extracts SQLite database file.
 */
internal fun extractBackup(decryptedBackupZip: File, pathToFolder: String): Pair<ExportMetadata, File> {
    // extract files
    ZipFile(decryptedBackupZip).extractAll(pathToFolder)
    // read metadata
    val metaData =
        requireNotNull(
            parseJson<ExportMetadata>(
                File("$pathToFolder${File.separator}export.json").readText()
            )
        ) { "It was not possible to read required export metadata!" }
    // rename database file
    return metaData to File(pathToFolder)
}


internal data class DecryptionResult(
    val metadata: ExportMetadata,
    val databaseFile: File
)
