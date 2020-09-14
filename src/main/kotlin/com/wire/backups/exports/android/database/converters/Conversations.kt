package com.wire.backups.exports.android.database.converters

import com.wire.backups.exports.android.database.dto.ConversationAddMemberDto
import com.wire.backups.exports.android.database.dto.ConversationLeaveMembersDto
import com.wire.backups.exports.android.database.dto.ConversationMembersDto
import com.wire.backups.exports.android.database.dto.ConversationsDataDto
import com.wire.backups.exports.android.database.dto.DirectConversationDto
import com.wire.backups.exports.android.database.dto.NamedConversationDto
import com.wire.backups.exports.android.database.loaders.BackupExport
import com.wire.backups.exports.utils.mapCatching
import com.wire.backups.exports.utils.rowExportFailed
import pw.forst.tools.katlib.filterNotNullBy
import pw.forst.tools.katlib.toUuid
import pw.forst.tools.katlib.whenNull
import java.util.UUID


internal fun BackupExport.getNamedConversations() =
    conversations.values
        .filterNotNullBy { it.name }
        .mapCatching({
            NamedConversationDto(
                it.id.toUuid(),
                // safe as we're filtering in previous step
                requireNotNull(it.name) { "Name was null" }
            )
        }, rowExportFailed)

internal fun BackupExport.getDirectMessages(myId: UUID) =
    conversations.values
        .filter { it.name == null }
        .mapNotNull { conv ->
            conversationMembers[conv.id]
                ?.let { conv.id to it }
                .whenNull { parsingLogger.warn { "Database is missing referenced conversation members: $conv" } }
        }
        .mapCatching({ (convId, members) ->
            DirectConversationDto(
                convId.toUuid(),
                otherUser = (members
                    .firstOrNull { it != myId.toString() }
                    ?: members.first())
                    .toUuid()
            )
        }, rowExportFailed)

internal fun BackupExport.getConversationsData(): ConversationsDataDto {
    val addMembers = messages.values
        .filter { it.messageType == "MemberJoin" }
        .mapCatching({
            ConversationAddMemberDto(
                conversationId = it.conversationId.toUuid(),
                timeStamp = it.time.toExportDateFromAndroid(),
                addingUser = it.userId.toUuid(),
                addedUsers = it.members
                    ?.split(",")
                    ?.map(String::toUuid)
                    ?: emptyList()
            )

        }, rowExportFailed)

    val leavingMembers = messages.values
        .filter { it.messageType == "MemberLeave" }
        .mapCatching({
            ConversationLeaveMembersDto(
                conversationId = it.conversationId.toUuid(),
                timeStamp = it.time.toExportDateFromAndroid(),
                leavingMembers = it.members
                    ?.split(",")
                    ?.map(String::toUuid)
                    ?: emptyList()
            )
        }, rowExportFailed)

    val members = conversationMembers
        .mapCatching({ (convId, users) ->
            ConversationMembersDto(convId.toUuid(), users.map { it.toUuid() })
        }, rowExportFailed)

    return ConversationsDataDto(members, addMembers, leavingMembers)
}
