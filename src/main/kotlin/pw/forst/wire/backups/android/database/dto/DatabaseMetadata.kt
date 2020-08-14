package pw.forst.wire.backups.android.database.dto

import java.util.UUID

data class DatabaseMetadata(
    val userId: UUID,
    val name: String,
    val handle: String,
    val email: String
)
