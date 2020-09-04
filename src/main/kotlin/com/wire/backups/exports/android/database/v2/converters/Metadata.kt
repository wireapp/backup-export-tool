package com.wire.backups.exports.android.database.v2.converters

import com.wire.backups.exports.android.database.converters.toUuid
import com.wire.backups.exports.android.database.dto.DatabaseMetadata
import com.wire.backups.exports.android.database.v2.loaders.BackupExport
import java.util.UUID

internal fun BackupExport.getDatabaseMetadata(myId: UUID) =
    users.getValue(myId.toString())
        .let {
            DatabaseMetadata(
                userId = it.id.toUuid(),
                name = it.name,
                handle = requireNotNull(it.handle) { "Handle was null!" },
                email = it.email ?: "no-email"
            )
        }
