package com.wire.backups.exports.android.decryption

import mu.KLogging

/**
 * Based on the original Android code.
 *
 * https://github.com/wireapp/wire-android/blob/develop/app/src/main/kotlin/com/waz/zclient/feature/backup/crypto/Crypto.kt
 */
internal class Crypto(private val cryptoWrapper: CryptoWrapper) {

    private companion object : KLogging()

    internal fun hashWithMessagePart(input: String, salt: ByteArray): ByteArray {
        val output = ByteArray(encryptExpectedKeyBytes())
        val passBytes = input.toByteArray()
        val pushMessage = cryptoWrapper.generatePwhashMessagePart(output, passBytes, salt)
        require(pushMessage == 0) { "Hashing failed." }
        return output
    }

    internal fun aBytesLength(): Int =
        cryptoWrapper.polyABytes()

    internal fun decrypt(decrypted: ByteArray, cipherText: ByteArray, key: ByteArray, nonce: ByteArray) =
        cryptoWrapper.decrypt(decrypted, cipherText, key, nonce)

    private fun encryptExpectedKeyBytes() = cryptoWrapper.aedPolyKeyBytes()

    internal fun decryptExpectedKeyBytes() = cryptoWrapper.aedPolyKeyBytes()
}
