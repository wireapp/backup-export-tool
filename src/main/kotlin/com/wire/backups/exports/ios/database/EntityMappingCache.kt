package com.wire.backups.exports.ios.database

import org.jetbrains.exposed.sql.selectAll
import com.wire.backups.exports.ios.database.config.IosDatabase
import com.wire.backups.exports.ios.model.ReactionDto
import com.wire.backups.exports.ios.toUuid
import java.util.UUID

internal class EntityMappingCache(
    private val userMap: Map<Int, UUID>,
    private val conversationMap: Map<Int, UUID>,
    private val reactionsMap: Map<Int, List<Pair<Int, String>>>
) {
    fun getUsersUuid(primaryKey: Int) = userMap.getValue(primaryKey)

    fun getConversationUuid(primaryKey: Int) = conversationMap.getValue(primaryKey)

    fun getReactionsForMessagePk(primaryKey: Int) =
        reactionsMap.getOrDefault(primaryKey, emptyList())
            .let {
                it.map { (userPk, reaction) ->
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
        .selectAll()
        .associate { it[conversations.id] to it[conversations.remoteUuid].bytes.toUuid() }


private fun IosDatabase.usersMap(): Map<Int, UUID> =
    users
        .slice(users.id, users.remoteUuid)
        .selectAll()
        .associate { it[users.id] to it[users.remoteUuid].bytes.toUuid() }

internal fun IosDatabase.getReactionsMap(): Map<Int, List<Pair<Int, String>>> =
    usersReactions
        .innerJoin(reactions)
        .slice(reactions.unicodeValue, reactions.messageId, usersReactions.userId)
        .selectAll()
        .map { Triple(it[reactions.messageId], it[usersReactions.userId], it[reactions.unicodeValue]) }
        .groupBy { (messageId, _, _) -> messageId }
        .mapValues { (_, value) -> value.map { (_, userId, unicodeValue) -> userId to unicodeValue } }
