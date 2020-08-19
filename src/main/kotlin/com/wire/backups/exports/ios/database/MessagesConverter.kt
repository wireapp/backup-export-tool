package com.wire.backups.exports.ios.database

import org.jetbrains.exposed.sql.ColumnSet
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import com.wire.backups.exports.android.database.converters.toExportDateFromIos
import com.wire.backups.exports.ios.database.config.IosDatabase
import com.wire.backups.exports.ios.database.model.GenericMessageData
import com.wire.backups.exports.ios.database.model.Messages
import com.wire.backups.exports.ios.model.IosMessageDto

internal fun IosDatabase.getGenericMessages(): List<IosMessageDto> =
    getGenericMessages(buildMappingCache())

internal fun IosDatabase.getGenericMessages(cache: EntityMappingCache) =
    (getMessages(cache) + getAssets(cache)).sortedBy { it.time }

private fun IosDatabase.getMessages(cache: EntityMappingCache) =
    genericMessageData.join(messages, JoinType.INNER,
        onColumn = genericMessageData.messageId,
        otherColumn = messages.id,
        additionalConstraint = { genericMessageData.messageId.isNotNull() }
    ).messagesSlice()
        .select { messages.conversationId.isNotNull() }
        .map { mapGenericMessage(it, cache) }

private fun IosDatabase.getAssets(cache: EntityMappingCache) =
    genericMessageData.join(messages, JoinType.INNER,
        onColumn = genericMessageData.assetId,
        otherColumn = messages.id,
        additionalConstraint = { genericMessageData.assetId.isNotNull() }
    ).messagesSlice()
        .select { messages.conversationId.isNotNull() }
        .map { mapGenericMessage(it, cache) }

private fun IosDatabase.mapGenericMessage(it: ResultRow, cache: EntityMappingCache) =
    IosMessageDto(
        id = it[messages.id],
        // these requires should be always ok, if this throws exception, the database is inconsistent
        senderUUID = cache.getUsersUuid(requireNotNull(it[messages.senderId]) { "Sender was null!" }),
        conversationUUID = cache.getConversationUuid(requireNotNull(it[messages.conversationId]) { "Conversation was null!" }),
        time = it[messages.timestamp].toExportDateFromIos(),
        protobuf = it[genericMessageData.proto].bytes,
        wasEdited = it[messages.updatedTimestamp] != null,
        reactions = cache.getReactionsForMessagePk(it[messages.id])
    )

private fun ColumnSet.messagesSlice() =
    slice(
        Messages.id, Messages.senderId, Messages.conversationId, Messages.timestamp,
        GenericMessageData.proto, Messages.updatedTimestamp
    )
