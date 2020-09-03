package com.wire.backups.exports.android.database.model.v2

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Conversation(
    val id: String,
    val name: String?,
    val conversationType: Int?
)
