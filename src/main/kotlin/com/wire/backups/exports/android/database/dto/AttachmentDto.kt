package com.wire.backups.exports.android.database.dto

import com.wire.backups.exports.utils.ExportDate
import java.util.UUID

data class AttachmentDto(
    val id: UUID,
    val conversationId: UUID,
    val name: String,
    val sender: UUID,
    val timestamp: ExportDate,
    val contentLength: Int,
    val mimeType: String,
    val assetToken: String,
    val assetKey: String,
    val sha: ByteArray,
    val protobuf: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AttachmentDto

        if (id != other.id) return false
        if (conversationId != other.conversationId) return false
        if (name != other.name) return false
        if (sender != other.sender) return false
        if (timestamp != other.timestamp) return false
        if (contentLength != other.contentLength) return false
        if (mimeType != other.mimeType) return false
        if (assetToken != other.assetToken) return false
        if (assetKey != other.assetKey) return false
        if (!sha.contentEquals(other.sha)) return false
        if (!protobuf.contentEquals(other.protobuf)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + conversationId.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + sender.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + contentLength
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + assetToken.hashCode()
        result = 31 * result + assetKey.hashCode()
        result = 31 * result + sha.contentHashCode()
        result = 31 * result + protobuf.contentHashCode()
        return result
    }
}
