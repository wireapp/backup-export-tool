package pw.forst.wire.backups.ios.database

import org.jetbrains.exposed.sql.selectAll
import pw.forst.wire.backups.ios.database.config.IosDatabase
import pw.forst.wire.backups.ios.model.ConversationDto
import pw.forst.wire.backups.ios.toUuid
import java.util.UUID


internal fun IosDatabase.getConversations(userId: UUID) =
    conversationMembers
        .innerJoin(conversations)
        .innerJoin(users)
        .slice(conversations.remoteUuid, conversations.name, users.remoteUuid, users.name)
        .selectAll()
        .groupBy(
            { it[conversations.remoteUuid].bytes.toUuid() to it[conversations.name] },
            { it[users.remoteUuid].bytes.toUuid() to it[users.name] }
        )
        .map { (idName, users) ->
            val (conversationId, conversationName) = idName
            ConversationDto(
                id = conversationId,
                name = conversationName
                // direct message to some other person
                    ?: users.firstOrNull { (id, _) -> id != userId }?.second
                    // direct message to myself
                    ?: users.firstOrNull()?.second
                    // some weird state, but we don't really care about name
                    ?: "No name",
                members = users.map { (id, _) -> id }
            )
        }
