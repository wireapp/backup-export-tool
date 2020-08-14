package com.wire.backups.exports.api

import com.wire.backups.exports.ios.export.exportIosDatabase
import com.wire.backups.exports.ios.model.IosDatabaseExportDto
import com.wire.backups.exports.ios.processIosBackup
import java.io.File
import java.util.UUID

/**
 * Exporter which can be used iOS backups.
 */
class IosBackupExport internal constructor(
    private val userId: UUID,
    private val inputFile: File,
    private val outputDirectory: File,
    private val databasePassword: String
) : BackupExport<IosDatabaseExportDto> {

    internal constructor(topBuilder: DatabaseExport.Builder) : this(
        topBuilder.userId,
        topBuilder.inputFile,
        topBuilder.outputDirectory,
        topBuilder.databasePassword
    )

    @Suppress("ComplexRedundantLet") // not true, we need to init sodium
    override fun decryptDatabase(): File =
        exportIosDatabase(
            inputFile = inputFile.absolutePath,
            password = databasePassword,
            userId = userId,
            outputPath = outputDirectory.absolutePath
        ).databaseFile

    override fun exportDatabase(): IosDatabaseExportDto =
        processIosBackup(
            encryptedBackupFile = inputFile.absolutePath,
            password = databasePassword,
            userIdForBackup = userId,
            outputDirectory = outputDirectory.absolutePath
        )
}
