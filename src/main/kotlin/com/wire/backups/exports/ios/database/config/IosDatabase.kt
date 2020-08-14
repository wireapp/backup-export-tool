package com.wire.backups.exports.ios.database.config

import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.select
import com.wire.backups.exports.ios.database.model.ConversationMembers
import com.wire.backups.exports.ios.database.model.Conversations
import com.wire.backups.exports.ios.database.model.GenericMessageData
import com.wire.backups.exports.ios.database.model.Messages
import com.wire.backups.exports.ios.database.model.PrimaryKeys
import com.wire.backups.exports.ios.database.model.Reactions
import com.wire.backups.exports.ios.database.model.SystemMessagesRelatedUsers
import com.wire.backups.exports.ios.database.model.Users
import com.wire.backups.exports.ios.database.model.UsersReactions
import com.wire.backups.exports.utils.primaryConstructorParameters
import kotlin.reflect.full.primaryConstructor

internal class IosDatabase(
    internal val entityTypePks: DatabaseTablesConfiguration
) {
    internal val conversationMembers by lazy { ConversationMembers }
    internal val conversations by lazy { Conversations }
    internal val genericMessageData by lazy { GenericMessageData }
    internal val messages by lazy { Messages }
    internal val primaryKeys by lazy { PrimaryKeys }
    internal val reactions by lazy { Reactions }
    internal val users by lazy { Users }

    internal val systemMessagesRelatedUsers by lazy {
        SystemMessagesRelatedUsers(
            userEntityTypePk = entityTypePks.userEntityTypePk,
            systemMessageEntityTypePk = entityTypePks.systemMessageEntityTypePk
        )
    }

    internal val usersReactions by lazy {
        UsersReactions(
            userEntityTypePk = entityTypePks.userEntityTypePk,
            reactionEntityTypePk = entityTypePks.reactionEntityTypePk
        )
    }
}

@Suppress("unused") // we need to force it to run inside transaction
internal fun Transaction.obtainDatabaseTablesConfiguration(): DatabaseTablesConfiguration {
    val entityNameToPk = PrimaryKeys.select {
        PrimaryKeys.name.inList(parameterNameToEntityName.values)
    }.associate { it[PrimaryKeys.name] to it[PrimaryKeys.entityKey] }

    // we can assert here as this is dataclass
    return primaryConstructorParameters<DatabaseTablesConfiguration>()!!
        .map { (parameterName, kParameter) ->
            val entityName = parameterNameToEntityName.getValue(parameterName!!)
            val entityTypePrimaryKey = entityNameToPk.getValue(entityName)
            kParameter to entityTypePrimaryKey
        }.toMap()
        .let { DatabaseTablesConfiguration::class.primaryConstructor!!.callBy(it) }
}

private val parameterNameToEntityName = mapOf(
    DatabaseTablesConfiguration::systemMessageEntityTypePk.name to "SystemMessage",
    DatabaseTablesConfiguration::reactionEntityTypePk.name to "Reaction",
    DatabaseTablesConfiguration::userEntityTypePk.name to "User"
)
