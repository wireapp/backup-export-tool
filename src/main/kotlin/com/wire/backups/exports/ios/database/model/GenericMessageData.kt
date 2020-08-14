package com.wire.backups.exports.ios.database.model

import org.jetbrains.exposed.sql.Table

internal object GenericMessageData : Table("ZGENERICMESSAGEDATA") {
    val id = integer("Z_PK")
    val proto = blob("ZDATA")
    val messageId = (integer("ZMESSAGE") references Messages.id).nullable()
    val assetId = (integer("ZASSET") references Messages.id).nullable()
    override val primaryKey = PrimaryKey(id)
}
