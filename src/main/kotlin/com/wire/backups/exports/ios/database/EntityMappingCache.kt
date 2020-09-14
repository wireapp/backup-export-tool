package com.wire.backups.exports.ios.database

import com.wire.backups.exports.ios.database.config.IosDatabase
import com.wire.backups.exports.ios.model.ReactionDto
import com.wire.backups.exports.utils.mapCatching
import com.wire.backups.exports.utils.rowExportFailed
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import pw.forst.tools.katlib.toUuid
import java.util.UUID

internal class EntityMappingCache(
    private val userMap: Map<Int, UUID>,
    private val conversationMap: Map<Int, UUID>,
    private val reactionsMap: Map<Int, List<Pair<Int, String>>>
) {
    fun getUsersUuid(primaryKey: Int) = userMap.getValue(primaryKey)

    fun getConversationUuid(primaryKey: Int) = conversationMap.getValue(primaryKey)

    fun getReactionsForMessagePk(primaryKey: Int) =
        reactionsMap.getOrDefault(primaryKey, emptyList()).let { reactions ->
            reactions.map { (userPk, reaction) ->
                ReactionDto(
                    userId = getUsersUuid(userPk),
                    unicodeValue = reaction
                )
            }
        }
}

internal fun IosDatabase.buildMappingCache() =
    EntityMappingCache(
        userMap = usersMap(),
        conversationMap = conversationsMap(),
        reactionsMap = getReactionsMap()
    )

private fun IosDatabase.conversationsMap(): Map<Int, UUID> =
    conversations
        .slice(conversations.id, conversations.remoteUuid)
        .select { conversations.id.isNotNull() and conversations.remoteUuid.isNotNull() }
        .associate { it[conversations.id] to it[conversations.remoteUuid].bytes.toUuid() }


private fun IosDatabase.usersMap(): Map<Int, UUID> =
    users
        .slice(users.id, users.remoteUuid)
        .select { users.id.isNotNull() and users.remoteUuid.isNotNull() }
        .associate { it[users.id] to it[users.remoteUuid].bytes.toUuid() }

internal fun IosDatabase.getReactionsMap(): Map<Int, List<Pair<Int, String>>> =
    usersReactions
        .innerJoin(reactions)
        .slice(reactions.unicodeValue, reactions.messageId, usersReactions.userId)
        .select { reactions.unicodeValue.isNotNull() and reactions.messageId.isNotNull() and usersReactions.userId.isNotNull() }
        .mapCatching({ Triple(it[reactions.messageId], it[usersReactions.userId], it[reactions.unicodeValue]) }, rowExportFailed)
        .groupBy { (messageId, _, _) -> messageId }
        .mapValues { (_, value) -> value.map { (_, userId, unicodeValue) -> userId to unicodeValue } }
