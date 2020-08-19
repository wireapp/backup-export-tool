package com.wire.backups.exports.ios.database.model

import org.jetbrains.exposed.sql.Table

internal class SystemMessagesRelatedUsers(
    userEntityTypePk: Int,
    systemMessageEntityTypePk: Int
) : Table("Z_${systemMessageEntityTypePk}USERS") {
    val userId = integer("Z_${userEntityTypePk}USERS1") references Users.id
    val messageId = integer("Z_${systemMessageEntityTypePk}SYSTEMMESSAGES") references Messages.id

    override val primaryKey = PrimaryKey(userId, messageId)
}
