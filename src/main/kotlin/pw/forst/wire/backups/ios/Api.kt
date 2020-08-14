package pw.forst.wire.backups.ios

import pw.forst.wire.backups.ios.database.buildMappingCache
import pw.forst.wire.backups.ios.database.getConversations
import pw.forst.wire.backups.ios.database.getGenericMessages
import pw.forst.wire.backups.ios.database.getUserAddedToConversation
import pw.forst.wire.backups.ios.database.getUserLeftConversation
import pw.forst.wire.backups.ios.database.verifyDatabaseMetadata
import pw.forst.wire.backups.ios.database.withDatabase
import pw.forst.wire.backups.ios.export.exportIosDatabase
import pw.forst.wire.backups.ios.model.IosDatabaseExportDto
import java.util.UUID

/**
 * Decrypts and extracts iOS database and loads all information that are necessary for the exports.
 */
@Suppress("unused") // used in Java
fun processIosBackup(
    encryptedBackupPath: String,
    password: String,
    userIdForBackup: String
): IosDatabaseExportDto =
    processIosBackup(
        encryptedBackupPath = encryptedBackupPath,
        password = password,
        userIdForBackup = userIdForBackup,
        outputDirectory = "./ios-backup-export"
    )

/**
 * Decrypts and extracts iOS database and loads all information that are necessary for the exports.
 */
fun processIosBackup(
    encryptedBackupPath: String,
    password: String,
    userIdForBackup: String,
    outputDirectory: String
): IosDatabaseExportDto =
    processIosBackup(
        encryptedBackupFile = encryptedBackupPath,
        password = password,
        userIdForBackup = UUID.fromString(userIdForBackup),
        outputDirectory = outputDirectory
    )

/**
 * Decrypts and extracts iOS database and loads all information that are necessary for the exports.
 */
fun processIosBackup(
    encryptedBackupFile: String,
    password: String,
    userIdForBackup: UUID,
    outputDirectory: String
): IosDatabaseExportDto =
    exportIosDatabase(
        inputFile = encryptedBackupFile,
        password = password,
        userId = userIdForBackup,
        outputPath = outputDirectory
    ).let { database ->
        // verifies that this export tool binary supports given export
        verifyDatabaseMetadata(database)

        withDatabase(database.databaseFile) {
            val cache = buildMappingCache()
            IosDatabaseExportDto(
                metadata = database,
                messages = getGenericMessages(cache),
                conversations = getConversations(userIdForBackup),
                addedParticipants = getUserAddedToConversation(cache),
                leftParticipants = getUserLeftConversation(cache)
            )
        }
    }
