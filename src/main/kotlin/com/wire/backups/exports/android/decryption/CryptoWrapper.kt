package com.wire.backups.exports.android.decryption

import com.goterl.lazycode.lazysodium.SodiumJava
import com.sun.jna.NativeLong
import com.wire.backups.exports.utils.LIBSODIUM_BINARIES_LOADING
import com.wire.backups.exports.utils.crypto_aead_chacha20poly1305_keybytes
import com.wire.backups.exports.utils.crypto_aead_xchacha20poly1305_ietf_abytes
import com.wire.backups.exports.utils.crypto_pwhash_MEMLIMIT_INTERACTIVE
import com.wire.backups.exports.utils.crypto_pwhash_OPSLIMIT_INTERACTIVE
import com.wire.backups.exports.utils.crypto_pwhash_alg_default

/**
 * Based on the original Android repo.
 *
 * https://github.com/wireapp/wire-android/blob/develop/app/src/main/kotlin/com/waz/zclient/feature/backup/crypto/CryptoWrapper.kt
 */
internal class CryptoWrapper {

    private val sodium by lazy { SodiumJava(LIBSODIUM_BINARIES_LOADING) }

    internal fun polyABytes() = crypto_aead_xchacha20poly1305_ietf_abytes

    internal fun decrypt(decrypted: ByteArray, cipherText: ByteArray, key: ByteArray, nonce: ByteArray): Int =
        sodium.crypto_aead_xchacha20poly1305_ietf_decrypt(
            decrypted,
            longArrayOf(1),
            byteArrayOf(),
            cipherText,
            cipherText.size.toLong(),
            byteArrayOf(),
            0,
            nonce,
            key
        )

    internal fun generatePwhashMessagePart(output: ByteArray, passBytes: ByteArray, salt: ByteArray) =
        sodium.crypto_pwhash(
            output,
            output.size.toLong(),
            passBytes,
            passBytes.size.toLong(),
            salt,
            crypto_pwhash_OPSLIMIT_INTERACTIVE.toLong(),
            NativeLong(crypto_pwhash_MEMLIMIT_INTERACTIVE.toLong()),
            crypto_pwhash_alg_default
        )

    internal fun aedPolyKeyBytes() = crypto_aead_chacha20poly1305_keybytes
}
