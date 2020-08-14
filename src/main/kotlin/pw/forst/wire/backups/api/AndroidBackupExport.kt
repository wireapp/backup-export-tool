package pw.forst.wire.backups.api

import pw.forst.wire.backups.android.database.converters.extractDatabase
import pw.forst.wire.backups.android.model.AndroidDatabaseExportDto
import pw.forst.wire.backups.android.steps.DecryptionResult
import pw.forst.wire.backups.android.steps.decryptAndExtract
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

    internal constructor(topBuilder: DatabaseExport.Builder) : this(
        topBuilder.userId,
        topBuilder.inputFile,
        topBuilder.outputDirectory,
        topBuilder.databasePassword
    )

    @Suppress("ComplexRedundantLet") // not true, we need to init sodium
    override fun decryptDatabase(): File =
        internallyDecrypt().databaseFile


    override fun exportDatabase(): AndroidDatabaseExportDto =
        internallyDecrypt().let { decrypted ->
            AndroidDatabaseExportDto(
                exportMetadata = decrypted.metadata,
                database = extractDatabase(userId, decrypted.databaseFile)
            )
        }

    @Suppress("ComplexRedundantLet")
    private fun internallyDecrypt(): DecryptionResult =
        decryptAndExtract(
            databaseFile = inputFile,
            password = databasePassword.toByteArray(),
            userId = userId,
            pathToNewFolder = outputDirectory.absolutePath
        )

}

