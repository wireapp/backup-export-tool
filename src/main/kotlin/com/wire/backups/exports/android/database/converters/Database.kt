package com.wire.backups.exports.android.database.converters

import com.wire.backups.exports.android.database.dto.DatabaseDto
import com.wire.backups.exports.android.database.loaders.BackupExport
import java.util.UUID

/**
 * Exports data from the provided database.
 */
internal fun convertDatabase(userId: UUID, export: BackupExport) = with(export) {
    DatabaseDto(
        getDatabaseMetadata(userId),
        getNamedConversations(),
        getDirectMessages(userId),
        getTextMessages(),
        getConversationsData(),
        getAttachments(),
        getLikings()
    )
}
