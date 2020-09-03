package com.wire.backups.exports.android.database.model.v2

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ConversationMember(
    val userId: String,
    val conversationId: String
)
