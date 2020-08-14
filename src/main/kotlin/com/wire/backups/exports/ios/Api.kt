package com.wire.backups.exports.ios

import com.wire.backups.exports.ios.database.buildMappingCache
import com.wire.backups.exports.ios.database.getConversations
import com.wire.backups.exports.ios.database.getGenericMessages
import com.wire.backups.exports.ios.database.getUserAddedToConversation
import com.wire.backups.exports.ios.database.getUserLeftConversation
import com.wire.backups.exports.ios.database.verifyDatabaseMetadata
import com.wire.backups.exports.ios.database.withDatabase
import com.wire.backups.exports.ios.export.exportIosDatabase
import com.wire.backups.exports.ios.model.IosDatabaseExportDto
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
