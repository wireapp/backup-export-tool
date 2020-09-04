package com.wire.backups.exports.android.database.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class Conversation(
    override val id: ConversationId,
    val name: String?,
    val conversationType: Int?
) : Model
