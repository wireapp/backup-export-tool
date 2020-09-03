package com.wire.backups.exports.android.database.v2.loaders

import com.wire.backups.exports.android.database.v2.model.Asset
import com.wire.backups.exports.android.database.v2.model.AssetId
import com.wire.backups.exports.android.database.v2.model.Conversation
import com.wire.backups.exports.android.database.v2.model.ConversationId
import com.wire.backups.exports.android.database.v2.model.Like
import com.wire.backups.exports.android.database.v2.model.Message
import com.wire.backups.exports.android.database.v2.model.MessageId
import com.wire.backups.exports.android.database.v2.model.User
import com.wire.backups.exports.android.database.v2.model.UserId


data class BackupExport(
    val assets: Map<AssetId, Asset>,
    val conversations: Map<ConversationId, Conversation>,
    val conversationMembers: Map<ConversationId, List<UserId>>,
    val likes: List<Like>,
    val messages: Map<MessageId, Message>,
    val users: Map<UserId, User>
)
