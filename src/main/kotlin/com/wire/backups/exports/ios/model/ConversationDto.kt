package com.wire.backups.exports.ios.model

import java.util.UUID

data class ConversationDto(
    val id: UUID,
    val name: String,
    val members: List<UUID>
)
