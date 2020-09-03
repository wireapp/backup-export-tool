package com.wire.backups.exports.android.database.v2.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class ConversationMember(
    val userId: UserId,
    val conversationId: ConversationId
)
