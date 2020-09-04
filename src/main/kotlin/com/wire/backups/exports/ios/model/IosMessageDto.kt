package com.wire.backups.exports.ios.model

import com.wire.backups.exports.utils.ExportDate
import java.util.UUID


/**
 * Message from iOS backup with protobuf and envelope.
 */
data class IosMessageDto(
    /**
     * Id of the message.
     */
    val id: Int,
    /**
     * Who sent the message.
     */
    val senderUUID: UUID,
    /**
     * In which conversation was message posted.
     */
    val conversationUUID: UUID,
    /**
     * When was the conversation sent.
     */
    val time: ExportDate,
    /**
     * Raw protobuf from the server.
     */
    val protobuf: ByteArray,
    /**
     * Indicates whether this message was edited.
     */
    val wasEdited: Boolean,
    /**
     * Reactions to this message.
     */
    val reactions: List<ReactionDto>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IosMessageDto

        if (id != other.id) return false
        if (senderUUID != other.senderUUID) return false
        if (conversationUUID != other.conversationUUID) return false
        if (time != other.time) return false
        if (!protobuf.contentEquals(other.protobuf)) return false
        if (wasEdited != other.wasEdited) return false
        if (reactions != other.reactions) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + senderUUID.hashCode()
        result = 31 * result + conversationUUID.hashCode()
        result = 31 * result + time.hashCode()
        result = 31 * result + protobuf.contentHashCode()
        result = 31 * result + wasEdited.hashCode()
        result = 31 * result + reactions.hashCode()
        return result
    }
}
