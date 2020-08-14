package com.wire.backups.exports.ios.export

import com.wire.backups.exports.ios.decryption.decryptIosBackup
import com.wire.backups.exports.ios.model.IosDatabaseDto
import java.io.File
import java.util.UUID

/**
 * Decrypts database and returns database metadata and database file.
 */
@Suppress("unused") // used by the Java APi
fun exportIosDatabase(inputFile: String, password: String, userId: String) =
// we could use default parameters value, but Java wouldn't be able to use it,
// thus overloading
    exportIosDatabase(inputFile, password, userId, "./")

/**
 * Decrypts database and returns database metadata and database file.
 */
fun exportIosDatabase(inputFile: String, password: String, userId: String, outputPath: String) =
    exportIosDatabase(inputFile, password, UUID.fromString(userId), outputPath)

/**
 * Decrypts database and returns database metadata and database file.
 */
fun exportIosDatabase(inputFile: String, password: String, userId: UUID, outputPath: String): IosDatabaseDto {
    val decryptedBytes = decryptIosBackup(
        File(inputFile),
        password = password,
        userId = userId
    )

    val (exportJson, databaseFile) = extractDatabaseFiles(decryptedBytes, outputPath)
    val metadata = parseDatabaseMetadata(exportJson)
    return IosDatabaseDto(
        userId = UUID.fromString(metadata.userIdentifier),
        clientIdentifier = metadata.clientIdentifier,
        modelVersion = metadata.modelVersion,
        appVersion = metadata.appVersion,
        creationTime = metadata.creationTime,
        platform = metadata.platform,
        databaseFile = databaseFile
    )
}
