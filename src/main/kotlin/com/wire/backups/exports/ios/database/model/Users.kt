package com.wire.backups.exports.ios.database.model

import org.jetbrains.exposed.sql.Table

internal object Users : Table("ZUSER") {
    val id = integer("Z_PK")
    val name = varchar("ZNAME", VARCHAR_DEFAULT_SIZE).nullable()
    val handle = varchar("ZHANDLE", VARCHAR_DEFAULT_SIZE).nullable()
    val remoteUuid = blob("ZREMOTEIDENTIFIER_DATA")
    override val primaryKey = PrimaryKey(GenericMessageData.id)
}
