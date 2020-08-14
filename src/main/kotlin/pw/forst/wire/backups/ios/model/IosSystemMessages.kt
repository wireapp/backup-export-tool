package pw.forst.wire.backups.ios.model

import pw.forst.wire.backups.android.database.converters.ExportDate
import java.util.UUID

data class IosUserAddedToConversation(
    val whoAddedUser: UUID,
    val addedUser: UUID,
    val conversation: UUID,
    val timestamp: ExportDate
)

data class IosUserLeftConversation(
    val userSendingLeftMessage: UUID?,
    val leavingUser: UUID,
    val conversation: UUID,
    val timestamp: ExportDate
)
