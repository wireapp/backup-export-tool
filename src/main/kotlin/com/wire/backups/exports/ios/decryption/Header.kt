package com.wire.backups.exports.ios.decryption

import java.nio.ByteBuffer
import java.nio.charset.Charset

/**
 * Note that this function modifies the passed [buffer].
 *
 * The file header is following:
 * 4 bytes  - string    - PLATFORM - indicates whether this is iOS or Android, expected value is WBUI
 * 1 byte   - byte      - empty space
 * 2 bytes  - integer   - VERSION - indicates version of backup
 * 16 bytes - bytes     - SALT - used for correct decryption initialization
 * 32 bytes - bytes     - UUID hash - used to verify whether the backup belongs to user with given UUID
 */
internal fun readHeader(buffer: ByteBuffer): EncryptedBackupHeader {
    // read platform header
    ByteArray(HeaderParameters.PLATFORM.sizeInHeader)
        .also { buffer.get(it) }
        .also { verifyPlatformBytes(it) }
    // skip null byte - HeaderParameters.EMPTY_SPACE
    buffer.get()
    // check version
    verifyVersionShort(buffer.short)
    // parse header
    return EncryptedBackupHeader(
        salt = ByteArray(HeaderParameters.SALT.sizeInHeader).also { buffer.get(it) },
        uuidHash = ByteArray(HeaderParameters.UUID_HASH.sizeInHeader).also { buffer.get(it) }
    )
}

private fun verifyPlatformBytes(platformBytes: ByteArray) {
    platformBytes.toString(Charset.forName("UTF-8")).also {
        require(HeaderParameters.PLATFORM.valueCheck(it)) {
            "Platform incorrect! Got $it"
        }
    }
}

private fun verifyVersionShort(version: Short) {
    require(HeaderParameters.VERSION.valueCheck(version)) {
        "Version of backup is not correct! Got $version"
    }
}


/*
    Taken from original iOS repo:
    https://github.com/wireapp/wire-ios-cryptobox/blob/00ec8c7262d49814744c733c6eaa92e99bcd6b42/WireCryptobox/ChaCha20Encryption.swift#L88
 */
private enum class HeaderParameters(
    val sizeInHeader: Int,
    val valueCheck: (Any) -> Boolean
) {
    PLATFORM(4, { it == "WBUI" }),
    EMPTY_SPACE(1, { true }),
    VERSION(2, { it == 1.toShort() }),
    SALT(16, { true }),
    UUID_HASH(32, { true })
}

internal data class EncryptedBackupHeader(
    val salt: ByteArray,
    val uuidHash: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EncryptedBackupHeader

        if (!salt.contentEquals(other.salt)) return false
        if (!uuidHash.contentEquals(other.uuidHash)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = salt.contentHashCode()
        result = 31 * result + uuidHash.contentHashCode()
        return result
    }
}
