package com.wire.backups.exports.ios.database.model

import org.jetbrains.exposed.sql.Table

internal object Conversations : Table("ZCONVERSATION") {
    val id = integer("Z_PK")
    val remoteUuid = blob("ZREMOTEIDENTIFIER_DATA")
    val name = varchar("ZUSERDEFINEDNAME", VARCHAR_DEFAULT_SIZE).nullable()
    override val primaryKey = PrimaryKey(GenericMessageData.id)
}
