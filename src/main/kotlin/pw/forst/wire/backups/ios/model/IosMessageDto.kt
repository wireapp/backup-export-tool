package pw.forst.wire.backups.ios.model

import pw.forst.wire.backups.android.database.converters.ExportDate
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
)
