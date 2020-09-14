package com.wire.backups.exports.android.database.dto

import com.wire.backups.exports.utils.ExportDate
import java.util.UUID

data class MessageDto(
    val id: UUID,
    val conversationId: UUID,
    val userId: UUID,
    val time: ExportDate,
    val content: String,
    val edited: Boolean,
    val quote: UUID? = null
)
