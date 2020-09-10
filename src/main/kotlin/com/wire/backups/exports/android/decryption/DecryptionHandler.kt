package com.wire.backups.exports.android.decryption

import java.io.File

internal fun decryptAndroidBackup(backupFile: File, userId: String, password: String): File =
    DecryptionHandler(
        Crypto(CryptoWrapper()),
        EncryptionHeaderMapper()
    ).decryptBackup(backupFile, userId, password)

/**
 * Based on the code from Android repo.
 *
 * https://github.com/wireapp/wire-android/blob/develop/app/src/main/kotlin/com/waz/zclient/feature/backup/crypto/decryption/DecryptionHandler.kt
 */
internal class DecryptionHandler(
    private val crypto: Crypto,
    private val cryptoHeaderMetaData: EncryptionHeaderMapper
) {

    internal fun decryptBackup(backupFile: File, userId: String, password: String): File {
        val header = cryptoHeaderMetaData.readMetadata(backupFile)
        val hash = crypto.hashWithMessagePart(userId, header.salt)
        require(hash.contentEquals(header.uuidHash)) { "HashesDoNotMatch" }
        return decryptBackupFile(password, backupFile, header.salt, header.nonce)
    }

    private fun decryptBackupFile(password: String, backupFile: File, salt: ByteArray, nonce: ByteArray): File {
        val cipherText = readCipherText(backupFile)
        val decryptedBackupBytes = decryptWithHash(cipherText, password, salt, nonce)
        return File.createTempFile(TMP_FILE_NAME, TMP_FILE_EXTENSION).apply {
            writeBytes(decryptedBackupBytes)
        }
    }

    private fun readCipherText(backupFile: File): ByteArray {
        require(TOTAL_HEADER_LENGTH < backupFile.length()) { "DecryptionFailed" }
        return ByteArray(backupFile.length().toInt() - TOTAL_HEADER_LENGTH).also {
            backupFile.inputStream().buffered().apply {
                skip(TOTAL_HEADER_LENGTH.toLong())
                read(it)
            }
        }
    }

    private fun decryptWithHash(cipherText: ByteArray, password: String, salt: ByteArray, nonce: ByteArray): ByteArray {
        val key = crypto.hashWithMessagePart(password, salt)
        require(key.size == crypto.decryptExpectedKeyBytes()) {
            "Key length invalid: ${key.size} did not match ${crypto.decryptExpectedKeyBytes()}"
        }
        return decrypt(cipherText, key, nonce)
    }

    private fun decrypt(cipherText: ByteArray, key: ByteArray, nonce: ByteArray): ByteArray {
        require(crypto.aBytesLength() <= cipherText.size) { "DecryptionFailed" }
        val decrypted = ByteArray(cipherText.size - crypto.aBytesLength())
        val res = crypto.decrypt(decrypted, cipherText, key, nonce)
        require(res == 0) { "DecryptionFailed" }
        return decrypted
    }

    companion object {
        private const val TMP_FILE_NAME = "wire_backup"
        private const val TMP_FILE_EXTENSION = ".zip"
    }
}
