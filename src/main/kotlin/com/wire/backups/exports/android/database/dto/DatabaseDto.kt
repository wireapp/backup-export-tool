package com.wire.backups.exports.android.database.dto

/**
 * Represents all data stored in the database.
 */
data class DatabaseDto(
    /**
     * Metadata about database.
     */
    val metaData: DatabaseMetadata,
    /**
     * Conversations with names - group conversations.
     */
    val namedConversations: List<NamedConversationDto>,
    /**
     * Conversations 1:1
     */
    val directConversations: List<DirectConversationDto>,
    /**
     * All messages that were sent.
     */
    val messages: List<MessageDto>,
    /**
     * Information about conversations.
     */
    val conversationsData: ConversationsDataDto,
    /**
     * Sent attachments.
     */
    val attachments: List<AttachmentDto>,
    /**
     * Sent likes.
     */
    val likings: List<LikingsDto>
)
