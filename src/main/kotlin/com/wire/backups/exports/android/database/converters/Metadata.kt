package com.wire.backups.exports.android.database.converters

import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.select
import com.wire.backups.exports.android.database.dto.DatabaseMetadata
import com.wire.backups.exports.android.database.model.Users
import java.util.UUID

@Suppress("unused") // we need to force it to run inside transaction
fun Transaction.getDatabaseMetadata(myId: UUID) =
    Users.select {
        Users.id eq myId.toString()
    }.first().let {
        DatabaseMetadata(
            userId = UUID.fromString(it[Users.id]),
            name = it[Users.name],
            handle = it[Users.handle],
            email = it[Users.email]
        )
    }
