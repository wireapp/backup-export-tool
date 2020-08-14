package pw.forst.wire.backups.android.database.converters

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import pw.forst.wire.backups.android.database.dto.DatabaseDto
import java.io.File
import java.util.UUID

/**
 * Exports data from the provided database.
 */
@Suppress("unused") // library API used in recording bot
fun extractDatabase(userId: UUID, databaseFile: File) = extractDatabase(userId, databaseFile.absolutePath)

/**
 * Exports data from the provided database.
 */
fun extractDatabase(userId: UUID, databasePath: String): DatabaseDto {
    Database.connect("jdbc:sqlite:$databasePath")
    return extractDatabase(userId)
}

/**
 * Exports data from the provided database.
 */
fun extractDatabase(userId: UUID) = transaction {
    DatabaseDto(
        getDatabaseMetadata(userId),
        getNamedConversations(),
        getDirectMessages(userId),
        getTextMessages(),
        getConversationsData(),
        getAttachments(),
        getLikings()
    )
}
