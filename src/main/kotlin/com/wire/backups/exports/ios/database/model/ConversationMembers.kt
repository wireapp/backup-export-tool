package com.wire.backups.exports.ios.database.model

import org.jetbrains.exposed.sql.Table

internal object ConversationMembers : Table("ZPARTICIPANTROLE") {
    val id = integer("Z_PK")
    val userId = integer("ZUSER") references Users.id
    val conversationId = (integer("ZCONVERSATION") references Conversations.id).nullable()
    override val primaryKey = PrimaryKey(id)
}
