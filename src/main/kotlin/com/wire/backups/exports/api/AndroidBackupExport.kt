package com.wire.backups.exports.api

import com.wire.backups.exports.android.database.converters.convertDatabase
import com.wire.backups.exports.android.database.loaders.createBackupExport
import com.wire.backups.exports.android.decryption.DecryptionResult
import com.wire.backups.exports.android.decryption.decryptAndExtractAndroidBackup
import com.wire.backups.exports.android.decryption.extractBackup
import com.wire.backups.exports.android.model.AndroidDatabaseExportDto
import mu.KLogging
import java.io.File
import java.util.UUID

/**
 * Exporter which can be used Android backups.
 */
class AndroidBackupExport internal constructor(
    private val userId: UUID,
    private val inputFile: File,
    private val outputDirectory: File,
    private val databasePassword: String
) : BackupExport<AndroidDatabaseExportDto> {

    private companion object : KLogging()

    internal constructor(topBuilder: DatabaseExport.Builder) : this(
        topBuilder.userId,
        topBuilder.inputFile,
        topBuilder.outputDirectory,
        topBuilder.databasePassword
    )

    override fun exportDatabase(): AndroidDatabaseExportDto =
        tryToExtractDependingOnExtension() ?: extractClassicExtraction()

    private fun extractClassicExtraction() = internallyDecrypt().let { decrypted ->
        AndroidDatabaseExportDto(
            exportMetadata = decrypted.metadata,
            database = convertDatabase(userId, createBackupExport(decrypted.databaseFile))
        )
    }

    private fun tryToExtractDependingOnExtension(): AndroidDatabaseExportDto? = runCatching {
        val decrypted = if (inputFile.extension == "zip") {
            val (metadata, db) = extractBackup(inputFile, outputDirectory.absolutePath)
            DecryptionResult(metadata, db)
        } else {
            internallyDecrypt()
        }
        AndroidDatabaseExportDto(
            exportMetadata = decrypted.metadata,
            database = convertDatabase(userId, createBackupExport(decrypted.databaseFile))
        )
    }.onFailure {
        logger.warn { "It was not possible to decrypt the database based on the file extension. Trying classic decryption." }
    }.getOrNull()

    private fun internallyDecrypt(): DecryptionResult =
        decryptAndExtractAndroidBackup(
            databaseFile = inputFile,
            password = databasePassword,
            userId = userId,
            pathToNewFolder = outputDirectory.absolutePath
        )

}

