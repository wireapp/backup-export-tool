package com.wire.backups.exports.android.database.model

import org.jetbrains.exposed.sql.Table

object Likings : Table("Likings") {
    val messageId = text("message_id") references Messages.id
    val userId = text("user_id")
    val timestamp = long("timestamp")
    override val primaryKey = PrimaryKey(messageId, userId)
}
