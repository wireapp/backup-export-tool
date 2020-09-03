package com.wire.backups.exports.android.database.v2.converters

import com.wire.backups.exports.android.database.dto.DatabaseMetadata
import com.wire.backups.exports.android.database.v2.loaders.BackupExport
import java.util.UUID

internal fun BackupExport.getDatabaseMetadata(myId: UUID) =
    users.getValue(myId.toString())
        .let {
            DatabaseMetadata(
                userId = UUID.fromString(it.id),
                name = it.name,
                handle = requireNotNull(it.handle) { "Handle was null!" },
                email = requireNotNull(it.email) { "Email was null!" }
            )
        }
