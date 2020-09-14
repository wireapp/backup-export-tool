package com.wire.backups.exports.android.database.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class Asset(
    override val id: AssetId,
    val token: String?,
    val name: String,
    val encryption: String,
    val mime: String,
    val sha: ByteArray?,
    val size: Int,
    val conversationId: ConversationId?
) : Model {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Asset

        if (id != other.id) return false
        if (token != other.token) return false
        if (name != other.name) return false
        if (encryption != other.encryption) return false
        if (mime != other.mime) return false
        if (!sha.contentEquals(other.sha)) return false
        if (size != other.size) return false
        if (conversationId != other.conversationId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + token.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + encryption.hashCode()
        result = 31 * result + mime.hashCode()
        result = 31 * result + sha.contentHashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + (conversationId?.hashCode() ?: 0)
        return result
    }
}
