package com.wire.backups.exports.android.decryption

import java.io.File
import java.nio.ByteBuffer

private const val SALT_LENGTH = 16
private const val NONCE_LENGTH = 24
private val ANDROID_MAGIC_NUMBER = "WBUA".toCharArray().map { it.toByte() }.toByteArray()
private const val ANDROID_MAGIC_NUMBER_LENGTH = 4
private const val CURRENT_VERSION: Short = 1

private const val UUID_HASH_LENGTH = 32

// TODO: Discuss with iOS one common format for the header
internal const val TOTAL_HEADER_LENGTH = ANDROID_MAGIC_NUMBER_LENGTH + 1 + 2 + SALT_LENGTH + UUID_HASH_LENGTH + 4 + 4 + NONCE_LENGTH

/**
 * Based on the original Android repo.
 *
 * https://github.com/wireapp/wire-android/blob/develop/app/src/main/kotlin/com/waz/zclient/feature/backup/crypto/header/EncryptionHeader.kt
 */
internal class EncryptionHeaderMapper {

    internal fun readMetadata(encryptedBackup: File): EncryptedBackupHeader {
        require(encryptedBackup.length() > TOTAL_HEADER_LENGTH) { "UnableToReadMetaData" }
        val encryptedMetadataBytes = ByteArray(TOTAL_HEADER_LENGTH)

        encryptedBackup.inputStream().buffered().read(encryptedMetadataBytes)
        return fromByteArray(encryptedMetadataBytes)
    }

    private fun fromByteArray(bytes: ByteArray): EncryptedBackupHeader {
        require(bytes.size == TOTAL_HEADER_LENGTH) { "Invalid header length: ${bytes.size} (should be: $TOTAL_HEADER_LENGTH)" }

        val buffer = ByteBuffer.wrap(bytes)
        val magicNumber = ByteArray(ANDROID_MAGIC_NUMBER_LENGTH)
        buffer.get(magicNumber)
        require(magicNumber.contentEquals(ANDROID_MAGIC_NUMBER)) { "archive has incorrect magic number: $magicNumber (should be: ${ANDROID_MAGIC_NUMBER})" }

        buffer.get() //skip null byte
        val version = buffer.short
        require(version == CURRENT_VERSION) { "Unsupported backup version: $version (should be $CURRENT_VERSION)" }

        val salt = ByteArray(SALT_LENGTH)
        buffer.get(salt)
        val uuidHash = ByteArray(UUID_HASH_LENGTH)
        buffer.get(uuidHash)
        val opslimit = buffer.int
        val memlimit = buffer.int
        val nonce = ByteArray(NONCE_LENGTH)
        buffer.get(nonce)
        return EncryptedBackupHeader(CURRENT_VERSION, salt, uuidHash, opslimit, memlimit, nonce)
    }
}

internal data class EncryptedBackupHeader(
    val version: Short = CURRENT_VERSION,
    val salt: ByteArray,
    val uuidHash: ByteArray,
    val opsLimit: Int = 0,
    val memLimit: Int = 0,
    val nonce: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EncryptedBackupHeader

        if (version != other.version) return false
        if (!salt.contentEquals(other.salt)) return false
        if (!uuidHash.contentEquals(other.uuidHash)) return false
        if (opsLimit != other.opsLimit) return false
        if (memLimit != other.memLimit) return false
        if (!nonce.contentEquals(other.nonce)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = version.toInt()
        result = 31 * result + salt.contentHashCode()
        result = 31 * result + uuidHash.contentHashCode()
        result = 31 * result + opsLimit
        result = 31 * result + memLimit
        result = 31 * result + nonce.contentHashCode()
        return result
    }
}
