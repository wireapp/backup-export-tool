package com.wire.backups.exports.android.database.loaders

import com.wire.backups.exports.android.database.model.Asset
import com.wire.backups.exports.android.database.model.Conversation
import com.wire.backups.exports.android.database.model.ConversationMember
import com.wire.backups.exports.android.database.model.Like
import com.wire.backups.exports.android.database.model.Message
import com.wire.backups.exports.android.database.model.Model
import com.wire.backups.exports.android.database.model.User
import com.wire.backups.exports.utils.mapCatching
import pw.forst.tools.katlib.parseJson
import java.io.File

internal fun createBackupExport(root: File): BackupExport =
    BackupExport(
        loadAndParse<List<Asset>>("Assets", root).flatAndMap(),
        loadAndParse<List<Conversation>>("Conversations", root).flatAndMap(),
        loadAndParse<List<ConversationMember>>("ConversationMembers", root)
            .flatten()
            .groupBy({ it.conversationId }, { it.userId }),
        loadAndParse<List<Like>>("Likes", root).flatten(),
        loadAndParse<List<Message>>("Messages", root).flatAndMap(),
        loadAndParse<List<User>>("Users", root).flatAndMap()
    )

private fun <T : Model> List<List<T>>.flatAndMap() =
    flatten().associateBy { it.id }


// sadly can't parse List<T> thus we need this
// see https://gist.github.com/LukasForst/8c48e1c71fa944739e5a98aec6e5c43d
private inline fun <reified T> loadAndParse(fileName: String, root: File) =
    root.listFiles()
        ?.filter { it.name.startsWith(fileName) && it.name.endsWith(".json") }
        ?.mapCatching { parseJson<T>(it.readText()) }
        ?: emptyList()
