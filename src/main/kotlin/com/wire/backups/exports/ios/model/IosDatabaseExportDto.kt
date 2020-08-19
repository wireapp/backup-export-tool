package com.wire.backups.exports.ios.model

/**
 * Data extracted from the database including metadata and messages.
 */
data class IosDatabaseExportDto(
    /**
     * Information about export and the database.
     */
    val metadata: IosDatabaseDto,
    /**
     * Parsed messages from the database.
     */
    val messages: List<IosMessageDto>,
    /**
     * All conversations in the database.
     */
    val conversations: List<ConversationDto>,
    /**
     * Events when user was added to conversation.
     */
    val addedParticipants: List<IosUserAddedToConversation>,
    /**
     * Events where user left conversation.
     */
    val leftParticipants: List<IosUserLeftConversation>
)
