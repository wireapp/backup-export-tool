package com.wire.backups.exports.android.database.converters

import com.wire.backups.exports.android.database.dto.DatabaseMetadata
import com.wire.backups.exports.android.database.loaders.BackupExport
import pw.forst.tools.katlib.toUuid
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
