package com.wire.backups.exports.android.database.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class Like(
    val messageId: MessageId,
    val userId: UserId,
    val timestamp: Long
)
