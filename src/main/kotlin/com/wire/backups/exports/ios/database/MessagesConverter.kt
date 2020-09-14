package com.wire.backups.exports.ios.database

import com.wire.backups.exports.ios.database.config.IosDatabase
import com.wire.backups.exports.ios.database.model.GenericMessageData
import com.wire.backups.exports.ios.database.model.Messages
import com.wire.backups.exports.ios.model.IosMessageDto
import com.wire.backups.exports.ios.toExportDateFromIos
import com.wire.backups.exports.utils.mapCatching
import com.wire.backups.exports.utils.rowExportFailed
import com.wire.backups.exports.utils.transactionsLogger
import org.jetbrains.exposed.sql.ColumnSet
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import pw.forst.tools.katlib.whenFalse

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
        .filter { filterRow(it) }
        .mapCatching({ mapGenericMessage(it, cache) }, rowExportFailed)

private fun IosDatabase.getAssets(cache: EntityMappingCache) =
    genericMessageData.join(messages, JoinType.INNER,
        onColumn = genericMessageData.assetId,
        otherColumn = messages.id,
        additionalConstraint = { genericMessageData.assetId.isNotNull() }
    ).messagesSlice()
        .select { messages.conversationId.isNotNull() }
        .filter { filterRow(it) }
        .mapCatching({ mapGenericMessage(it, cache) }, rowExportFailed)

private fun IosDatabase.filterRow(it: ResultRow) =
    (it[messages.senderId] != null && it[messages.conversationId] != null)
        .whenFalse {
            transactionsLogger.warn {
                "Filtering result set because either senderId or conversationId was null!\n$it"
            }
        }

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
