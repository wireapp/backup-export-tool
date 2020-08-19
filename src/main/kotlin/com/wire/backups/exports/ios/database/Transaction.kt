package com.wire.backups.exports.ios.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import com.wire.backups.exports.ios.database.config.IosDatabase
import com.wire.backups.exports.ios.database.config.obtainDatabaseTablesConfiguration
import java.io.File

/**
 * Wrapper for [org.jetbrains.exposed.sql.transactions.transaction].
 */
internal fun <T> transaction(sqliteDatabase: File, statement: Transaction.() -> T) =
    transaction(sqliteDatabase.absolutePath, statement)


/**
 * Wrapper for [org.jetbrains.exposed.sql.transactions.transaction].
 */
internal fun <T> transaction(sqliteDatabasePath: String, statement: Transaction.() -> T) =
    org.jetbrains.exposed.sql.transactions.transaction(
        Database.connect("jdbc:sqlite:$sqliteDatabasePath"), statement
    )

/**
 * Wrapper which creates [IosDatabase] for you.
 */
internal fun <T> withDatabase(sqliteDatabase: File, statement: IosDatabase.() -> T) =
    withDatabase(sqliteDatabase.absolutePath, statement)

/**
 * Wrapper which creates [IosDatabase] for you.
 */
internal fun <T> withDatabase(sqliteDatabase: String, statement: IosDatabase.() -> T) =
    transaction(sqliteDatabase) {
        val config = obtainDatabaseTablesConfiguration()
        statement(IosDatabase(config))
    }
