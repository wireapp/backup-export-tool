package pw.forst.wire.backups.ios.database

import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import pw.forst.wire.backups.android.database.converters.toExportDateFromIos
import pw.forst.wire.backups.ios.database.config.IosDatabase
import pw.forst.wire.backups.ios.database.model.Messages
import pw.forst.wire.backups.ios.database.model.SystemMessageType
import pw.forst.wire.backups.ios.model.IosUserAddedToConversation
import pw.forst.wire.backups.ios.model.IosUserLeftConversation

internal fun IosDatabase.getUserAddedToConversation(cache: EntityMappingCache): List<IosUserAddedToConversation> =
    userAddedEvents(cache)

internal fun IosDatabase.getUserLeftConversation(cache: EntityMappingCache): List<IosUserLeftConversation> =
    userLeftEvent(cache)

private fun IosDatabase.userAddedEvents(cache: EntityMappingCache): List<IosUserAddedToConversation> =
    (systemMessagesRelatedUsers innerJoin messages)
        .slice(Messages.senderId, systemMessagesRelatedUsers.userId, Messages.conversationId, Messages.timestamp)
        .select {
            messages.entityType.eq(entityTypePks.systemMessageEntityTypePk) and
                    messages.systemMessageType.eq(SystemMessageType.ZMSystemMessageTypeParticipantsAdded.ordinal)
        }.map {
            IosUserAddedToConversation(
                whoAddedUser = cache.getUsersUuid(
                    requireNotNull(it[messages.senderId]) { "Sender was null!" }
                ),
                addedUser = cache.getUsersUuid(it[systemMessagesRelatedUsers.userId]),
                conversation = cache.getConversationUuid(
                    requireNotNull(it[messages.conversationId]) { "Conversation id was null!" }
                ),
                timestamp = it[messages.timestamp].toExportDateFromIos()
            )
        }

private fun IosDatabase.userLeftEvent(cache: EntityMappingCache): List<IosUserLeftConversation> =
    (systemMessagesRelatedUsers innerJoin messages)
        .slice(messages.senderId, systemMessagesRelatedUsers.userId, messages.conversationId, messages.timestamp)
        .select {
            messages.entityType.eq(entityTypePks.systemMessageEntityTypePk) and
                    messages.systemMessageType.eq(SystemMessageType.ZMSystemMessageTypeParticipantsRemoved.ordinal)
        }.map {
            IosUserLeftConversation(
                userSendingLeftMessage = it[messages.senderId]?.let { userId -> cache.getUsersUuid(userId) },
                leavingUser = cache.getUsersUuid(it[systemMessagesRelatedUsers.userId]),
                conversation = cache.getConversationUuid(
                    requireNotNull(it[messages.conversationId]) { "Conversation id was null!" }
                ),
                timestamp = it[messages.timestamp].toExportDateFromIos()
            )
        }
