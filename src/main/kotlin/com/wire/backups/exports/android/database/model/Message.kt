package com.wire.backups.exports.android.database.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class Message(
    override val id: MessageId,
    val conversationId: ConversationId,
    val messageType: String,
    val userId: UserId,
    val content: String?,
    val protos: ByteArray?,
    val time: Long,
    val members: String?,
    val name: String?,
    val editTime: Long,
    val quote: String?,
    val assetId: AssetId?
) : Model {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Message

        if (id != other.id) return false
        if (conversationId != other.conversationId) return false
        if (messageType != other.messageType) return false
        if (userId != other.userId) return false
        if (content != other.content) return false
        if (protos != null) {
            if (other.protos == null) return false
            if (!protos.contentEquals(other.protos)) return false
        } else if (other.protos != null) return false
        if (time != other.time) return false
        if (members != other.members) return false
        if (name != other.name) return false
        if (editTime != other.editTime) return false
        if (quote != other.quote) return false
        if (assetId != other.assetId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + conversationId.hashCode()
        result = 31 * result + messageType.hashCode()
        result = 31 * result + userId.hashCode()
        result = 31 * result + (content?.hashCode() ?: 0)
        result = 31 * result + (protos?.contentHashCode() ?: 0)
        result = 31 * result + time.hashCode()
        result = 31 * result + (members?.hashCode() ?: 0)
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + editTime.hashCode()
        result = 31 * result + (quote?.hashCode() ?: 0)
        result = 31 * result + (assetId?.hashCode() ?: 0)
        return result
    }
}
