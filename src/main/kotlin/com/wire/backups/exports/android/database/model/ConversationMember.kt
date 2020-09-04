package com.wire.backups.exports.android.database.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class ConversationMember(
    val userId: UserId,
    val conversationId: ConversationId
)
