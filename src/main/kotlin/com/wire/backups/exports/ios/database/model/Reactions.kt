package com.wire.backups.exports.ios.database.model

import org.jetbrains.exposed.sql.Table

internal object Reactions : Table("ZREACTION") {
    val id = integer("Z_PK")
    val messageId = integer("ZMESSAGE") references Messages.id
    val unicodeValue = varchar("ZUNICODEVALUE", VARCHAR_DEFAULT_SIZE)
    override val primaryKey = PrimaryKey(id)
}
