package com.wire.backups.exports.android.database.converters

import com.fasterxml.jackson.databind.JsonNode
import com.wire.backups.exports.android.database.dto.LikingsDto
import com.wire.backups.exports.android.database.dto.MessageDto
import com.wire.backups.exports.android.database.model.Likings
import com.wire.backups.exports.android.database.model.Messages
import com.wire.backups.exports.utils.mapCatching
import com.wire.backups.exports.utils.rowExportFailed
import com.wire.backups.exports.utils.transactionsLogger
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import pw.forst.tools.katlib.parseJson
import pw.forst.tools.katlib.whenFalse
import pw.forst.tools.katlib.whenNull

@Suppress("unused") // we need to force it to run inside transaction
fun Transaction.getLikings() =
    (Likings innerJoin Messages)
        .slice(Likings.messageId, Likings.userId, Messages.conversationId, Likings.timestamp)
        .selectAll()
        .mapCatching({
            LikingsDto(
                messageId = it[Likings.messageId].toUuid(),
                userId = it[Likings.userId].toUuid(),
                conversationId = it[Messages.conversationId].toUuid(),
                time = it[Likings.timestamp].toExportDateFromAndroid()
            )
        }, rowExportFailed)

@Suppress("unused") // we need to force it to run inside transaction
fun Transaction.getTextMessages() =
    Messages
        .slice(
            Messages.messageType, Messages.id, Messages.conversationId, Messages.userId, Messages.time,
            Messages.quote, Messages.editTime, Messages.content
        ).select { Messages.messageType eq "Text" }
        .filter {
            (it[Messages.content] != null)
                .whenFalse { transactionsLogger.warn { "Filtering result set because content was null!\n$it" } }
        }
        .mapCatching({ row ->
            MessageDto(
                id = row[Messages.id].toUuid(),
                conversationId = row[Messages.conversationId].toUuid(),
                userId = row[Messages.userId].toUuid(),
                time = row[Messages.time].toExportDateFromAndroid(),
                content = parseContent(row[Messages.content]),
                quote = row[Messages.quote]?.toUuid(),
                edited = row[Messages.editTime] != 0L
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
