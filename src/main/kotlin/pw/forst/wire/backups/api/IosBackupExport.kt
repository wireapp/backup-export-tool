package pw.forst.wire.backups.api

import pw.forst.wire.backups.ios.export.exportIosDatabase
import pw.forst.wire.backups.ios.model.IosDatabaseExportDto
import pw.forst.wire.backups.ios.processIosBackup
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
