package com.wire.backups.exports.android.database.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class User(
    override val id: UserId,
    val name: String,
    val email: String?,
    val handle: String?
) : Model
