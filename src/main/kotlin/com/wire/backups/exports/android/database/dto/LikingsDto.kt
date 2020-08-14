package com.wire.backups.exports.android.database.dto

import com.wire.backups.exports.android.database.converters.ExportDate
import java.util.UUID

data class LikingsDto(
    val messageId: UUID,
    val userId: UUID,
    val conversationId: UUID,
    val time: ExportDate
)
