package com.wire.backups.exports.api

import com.wire.backups.exports.android.database.converters.convertDatabase
import com.wire.backups.exports.android.database.loaders.createBackupExport
import com.wire.backups.exports.android.model.AndroidDatabaseExportDto
import com.wire.backups.exports.android.steps.DecryptionResult
import com.wire.backups.exports.android.steps.decryptAndExtract
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
                database = convertDatabase(userId, createBackupExport(decrypted.databaseFile))
            )
        }

    @Suppress("ComplexRedundantLet")
    private fun internallyDecrypt(): DecryptionResult =
        decryptAndExtract(
            databaseFile = inputFile,
            password = databasePassword,
            userId = userId,
            pathToNewFolder = outputDirectory.absolutePath
        )

}

