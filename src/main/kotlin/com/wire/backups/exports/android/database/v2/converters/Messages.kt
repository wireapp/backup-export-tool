package com.wire.backups.exports.android.database.v2.converters

import com.fasterxml.jackson.databind.JsonNode
import com.wire.backups.exports.android.database.dto.LikingsDto
import com.wire.backups.exports.android.database.dto.MessageDto
import com.wire.backups.exports.android.database.v2.loaders.BackupExport
import com.wire.backups.exports.utils.mapCatching
import com.wire.backups.exports.utils.rowExportFailed
import pw.forst.tools.katlib.parseJson
import pw.forst.tools.katlib.whenNull

fun BackupExport.getLikings() =
    likes
        .mapNotNull { like -> messages[like.messageId]?.let { it to like } }
        .mapCatching({ (message, like) ->
            LikingsDto(
                messageId = like.messageId.toUuid(),
                userId = like.userId.toUuid(),
                conversationId = message.conversationId.toUuid(),
                time = like.timestamp.toExportDateFromAndroid()
            )

        }, rowExportFailed)

fun BackupExport.getTextMessages() =
    messages.values
        .filter { it.content != null }
        .mapCatching({
            MessageDto(
                id = it.id.toUuid(),
                conversationId = it.conversationId.toUuid(),
                userId = it.userId.toUuid(),
                time = it.time.toExportDateFromAndroid(),
                content = parseContent(it.content),
                quote = it.quote?.toUuid(),
                edited = it.editTime != 0L
            )

        }, rowExportFailed)

private fun parseContent(content: String?): String =
    requireNotNull(content) { "Content was null!" }.let {
        parseJson<JsonNode>(it)
            ?.get(0)
            ?.get("content")
            ?.asText()
            .whenNull { print("No content.") }
            ?: ""
    }
