package com.wire.backups.exports.ios.model

import com.wire.backups.exports.utils.ExportDate
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
