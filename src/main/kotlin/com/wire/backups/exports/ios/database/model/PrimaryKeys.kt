package com.wire.backups.exports.ios.database.model

import org.jetbrains.exposed.sql.Table

/**
 * Stores numbered entities.
 */
internal object PrimaryKeys : Table("Z_PRIMARYKEY") {
    val entityKey = integer("Z_ENT")
    val name = varchar("Z_NAME", VARCHAR_DEFAULT_SIZE)
    override val primaryKey = PrimaryKey(entityKey)
}
