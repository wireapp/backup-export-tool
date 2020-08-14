package pw.forst.wire.backups.android.database.model

import org.jetbrains.exposed.sql.Table

object Messages: Table("Messages") {
    val id = text("_id")
    val conversationId = text("conv_id") references Conversations.id
    val messageType = text("msg_type")
    val userId = text("user_id")
    val content = text("content").nullable()
    val protos = blob("protos").nullable()
    val time = long("time")
    val members = text("members").nullable()
    val name = text("name").nullable()
    val editTime = long("edit_time")
    val quote = text("quote").nullable()

    @Suppress("unused") // actually used as key to assets2 during join
    val assetId = (text("asset_id") references Assets2.id).nullable()

    override val primaryKey = PrimaryKey(id)
}
