package com.wire.backups.exports.android.database.converters

import com.wire.backups.exports.android.database.dto.AttachmentDto
import com.wire.backups.exports.android.database.model.Assets2
import com.wire.backups.exports.android.database.model.Messages
import com.wire.backups.exports.utils.mapCatching
import com.wire.backups.exports.utils.rowExportFailed
import com.wire.backups.exports.utils.transactionsLogger
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import pw.forst.tools.katlib.whenFalse

@Suppress("unused") // because we need it to run inside transaction
fun Transaction.getAttachments(): List<AttachmentDto> =
    (Messages innerJoin Assets2)
        .slice(
            Messages.id, Messages.conversationId, Assets2.name, Messages.userId, Messages.time,
            Assets2.size, Assets2.mime, Assets2.token, Assets2.id, Assets2.sha, Messages.protos
        )
        .select { Messages.assetId.isNotNull() and Assets2.id.isNotNull() }
        .filter {
            (it[Assets2.token] != null && it[Messages.protos]?.bytes != null).whenFalse {
                transactionsLogger.warn { "Filtering result set because either asset token or message bytes were null!\n$it" }
            }
        }
        .mapCatching({
            AttachmentDto(
                id = it[Messages.id].toUuid(),
                conversationId = it[Messages.conversationId].toUuid(),
                name = it[Assets2.name],
                sender = it[Messages.userId].toUuid(),
                timestamp = it[Messages.time].toExportDateFromAndroid(),
                contentLength = it[Assets2.size],
                mimeType = it[Assets2.mime],
                assetToken = requireNotNull(it[Assets2.token]) { "Asset token was null!" },
                assetKey = it[Assets2.id],
                sha = it[Assets2.sha].bytes,
                protobuf = requireNotNull(it[Messages.protos]?.bytes) { "Protobuf for asset was null!" }
            )
        }, rowExportFailed)

