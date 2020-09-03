package com.wire.backups.exports.android.database.v2.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class User(
    val id: UserId,
    val name: String,
    val email: String?,
    val handle: String?
)
