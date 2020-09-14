package com.wire.backups.exports.android.database.converters

import com.fasterxml.jackson.databind.JsonNode
import com.wire.backups.exports.android.database.dto.LikingsDto
import com.wire.backups.exports.android.database.dto.MessageDto
import com.wire.backups.exports.android.database.loaders.BackupExport
import com.wire.backups.exports.utils.mapCatching
import com.wire.backups.exports.utils.rowExportFailed
import pw.forst.tools.katlib.filterNotNullBy
import pw.forst.tools.katlib.parseJson
import pw.forst.tools.katlib.toUuid
import pw.forst.tools.katlib.whenNull

internal fun BackupExport.getLikings() =
    likes
        .mapNotNull { like ->
            messages[like.messageId]
                ?.let { it to like }
                .whenNull { parsingLogger.warn { "Database is missing referenced message: $like" } }
        }
        .mapCatching({ (message, like) ->
            LikingsDto(
                messageId = like.messageId.toUuid(),
                userId = like.userId.toUuid(),
                conversationId = message.conversationId.toUuid(),
                time = like.timestamp.toExportDateFromAndroid()
            )

        }, rowExportFailed)

internal fun BackupExport.getTextMessages() =
    messages.values
        .filterNotNullBy { it.content }
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
