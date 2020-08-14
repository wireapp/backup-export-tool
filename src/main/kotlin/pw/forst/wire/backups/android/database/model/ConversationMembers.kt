package pw.forst.wire.backups.android.database.model

import org.jetbrains.exposed.sql.Table

object ConversationMembers : Table("ConversationMembers") {
    val userId = text("user_id")
    val conversationId = text("conv_id") references Conversations.id

    override val primaryKey = PrimaryKey(userId, conversationId)
}
