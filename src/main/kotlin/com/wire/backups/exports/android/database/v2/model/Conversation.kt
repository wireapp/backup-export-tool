package com.wire.backups.exports.android.database.v2.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Conversation(
    val id: ConversationId,
    val name: String?,
    val conversationType: Int?
)
