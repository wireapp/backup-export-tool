package com.wire.backups.exports.android.database.model.v2

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Like(
    val messageId: String,
    val userId: String,
    val timestamp: Long
)
