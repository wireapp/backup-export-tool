package com.wire.backups.exports.android.database.loaders

import com.wire.backups.exports.android.database.model.Asset
import com.wire.backups.exports.android.database.model.AssetId
import com.wire.backups.exports.android.database.model.Conversation
import com.wire.backups.exports.android.database.model.ConversationId
import com.wire.backups.exports.android.database.model.Like
import com.wire.backups.exports.android.database.model.Message
import com.wire.backups.exports.android.database.model.MessageId
import com.wire.backups.exports.android.database.model.User
import com.wire.backups.exports.android.database.model.UserId


internal data class BackupExport(
    val assets: Map<AssetId, Asset>,
    val conversations: Map<ConversationId, Conversation>,
    val conversationMembers: Map<ConversationId, List<UserId>>,
    val likes: List<Like>,
    val messages: Map<MessageId, Message>,
    val users: Map<UserId, User>
)
