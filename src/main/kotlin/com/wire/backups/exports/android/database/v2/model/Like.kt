package com.wire.backups.exports.android.database.v2.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Like(
    val messageId: MessageId,
    val userId: UserId,
    val timestamp: Long
)
