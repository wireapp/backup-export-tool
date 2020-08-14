package pw.forst.wire.backups.android.database.dto

import pw.forst.wire.backups.android.database.converters.ExportDate
import java.util.UUID

data class LikingsDto(
    val messageId: UUID,
    val userId: UUID,
    val conversationId: UUID,
    val time: ExportDate
)
