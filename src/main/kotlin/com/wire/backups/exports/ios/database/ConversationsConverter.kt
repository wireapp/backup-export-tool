package com.wire.backups.exports.ios.database

import com.wire.backups.exports.ios.database.config.IosDatabase
import com.wire.backups.exports.ios.model.ConversationDto
import com.wire.backups.exports.utils.mapCatching
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import pw.forst.tools.katlib.toUuid
import java.util.UUID


internal fun IosDatabase.getConversations(userId: UUID) =
    conversationMembers
        .innerJoin(conversations)
        .innerJoin(users)
        .slice(conversations.remoteUuid, conversations.name, users.remoteUuid, users.name)
        .select {
            conversations.remoteUuid.isNotNull() and users.remoteUuid.isNotNull()
        }
        .groupBy(
            { it[conversations.remoteUuid].bytes.toUuid() to it[conversations.name] },
            { it[users.remoteUuid].bytes.toUuid() to it[users.name] }
        )
        .mapCatching({ (idName, users) ->
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
        }, { "It was not possible to transform data\n$it" })
