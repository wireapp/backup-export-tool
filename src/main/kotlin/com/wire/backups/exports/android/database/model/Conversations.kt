package com.wire.backups.exports.android.database.model

import org.jetbrains.exposed.sql.Table

object Conversations : Table("Conversations") {
    val id = text("_id")
    val name = text("name").nullable()
    override val primaryKey = PrimaryKey(id)
}
