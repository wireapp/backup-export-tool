package com.wire.backups.exports.android.database.model

import org.jetbrains.exposed.sql.Table

object Assets2 : Table("Assets2") {
    val id = text("_id")
    val token = text("token").nullable()
    val name = text("name")
    val encryption = text("encryption")
    val mime = text("mime")
    val sha = blob("sha")
    val size = integer("size")
    val conversationId = text("conversation_id").nullable()

    override val primaryKey = PrimaryKey(id)
}
