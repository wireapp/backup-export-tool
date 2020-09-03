package com.wire.backups.exports.android.database.model.v2

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class User(
    val id: String,
    val name: String,
    val email: String?,
    val handle: String?
)
