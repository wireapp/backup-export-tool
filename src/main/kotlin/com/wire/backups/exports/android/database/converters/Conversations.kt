@file:Suppress("unused") // we need to force it to run inside transaction

package com.wire.backups.exports.android.database.converters

import com.wire.backups.exports.android.database.dto.ConversationAddMemberDto
import com.wire.backups.exports.android.database.dto.ConversationLeaveMembersDto
import com.wire.backups.exports.android.database.dto.ConversationMembersDto
import com.wire.backups.exports.android.database.dto.ConversationsDataDto
import com.wire.backups.exports.android.database.dto.DirectConversationDto
import com.wire.backups.exports.android.database.dto.NamedConversationDto
import com.wire.backups.exports.android.database.model.ConversationMembers
import com.wire.backups.exports.android.database.model.Conversations
import com.wire.backups.exports.android.database.model.Messages
import com.wire.backups.exports.utils.mapCatching
import com.wire.backups.exports.utils.rowExportFailed
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import java.util.UUID


fun Transaction.getNamedConversations() = Conversations
    .slice(Conversations.id, Conversations.name)
    .select { Conversations.name.isNotNull() }
    .mapCatching({
        NamedConversationDto(
            it[Conversations.id].toUuid(),
            it[Conversations.name] ?: "no name"
        )
    }, rowExportFailed)

fun Transaction.getDirectMessages(myId: UUID) = (Conversations leftJoin ConversationMembers)
    .slice(Conversations.id, Conversations.name, ConversationMembers.userId, ConversationMembers.conversationId)
    .select { Conversations.name.isNull() }
    .groupBy({ it[Conversations.id].toUuid() }, { it[ConversationMembers.userId].toUuid() })
    .mapValues { (_, values) -> values.firstOrNull { it != myId } ?: myId }
    .mapCatching(
        { (conversationId, userId) -> DirectConversationDto(conversationId, otherUser = userId) },
        { "Data mapping failed:\n$it" }
    )

fun Transaction.getConversationsData(): ConversationsDataDto {
    val addMembers = Messages
        .slice(Messages.messageType, Messages.conversationId, Messages.time, Messages.userId, Messages.members)
        .select { Messages.messageType eq "MemberJoin" }
        .mapCatching({
            ConversationAddMemberDto(
                conversationId = it[Messages.conversationId].toUuid(),
                timeStamp = it[Messages.time].toExportDateFromAndroid(),
                addingUser = it[Messages.userId].toUuid(),
                addedUsers = it[Messages.members]
                    ?.split(",")
                    ?.map(String::toUuid)
                    ?: emptyList()
            )
        }, rowExportFailed)

    val leavingMembers = Messages
        .slice(Messages.messageType, Messages.conversationId, Messages.time, Messages.members)
        .select { Messages.messageType eq "MemberLeave" }
        .mapCatching({
            ConversationLeaveMembersDto(
                conversationId = it[Messages.conversationId].toUuid(),
                timeStamp = it[Messages.time].toExportDateFromAndroid(),
                leavingMembers = it[Messages.members]
                    ?.split(",")
                    ?.map(String::toUuid)
                    ?: emptyList()
            )
        }, rowExportFailed)

    val members = ConversationMembers
        .slice(ConversationMembers.conversationId, ConversationMembers.userId)
        .selectAll()
        .map { it[ConversationMembers.conversationId].toUuid() to it[ConversationMembers.userId].toUuid() }
        .groupBy({ it.first }, { it.second })
        .mapCatching(
            { (conversationId, members) -> ConversationMembersDto(conversationId, members) },
            { "Data mapping failed:\n$it" }
        )

    return ConversationsDataDto(members, addMembers, leavingMembers)
}
