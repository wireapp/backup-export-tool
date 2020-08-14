package com.wire.backups.exports.ios.decryption

import com.goterl.lazycode.lazysodium.SodiumJava
import com.sun.jna.NativeLong
import com.wire.backups.exports.utils.crypto_pwhash_argon2i_ALG_ARGON2I13
import com.wire.backups.exports.utils.crypto_pwhash_argon2i_MEMLIMIT_MODERATE
import com.wire.backups.exports.utils.crypto_pwhash_argon2i_OPSLIMIT_MODERATE

/*
    Taken from original repo
    https://github.com/wireapp/wire-ios-cryptobox/blob/00ec8c7262d49814744c733c6eaa92e99bcd6b42/WireCryptobox/ChaCha20Encryption.swift#L187
 */

/**
 * Derives key for given password and salt.
 */
internal fun SodiumJava.deriveKey(password: String, salt: ByteArray): ByteArray {
    val buffer = ByteArray(crypto_secretstream_xchacha20poly1305_keybytes())
    val passwordBytes = password.toByteArray()
    val result = crypto_pwhash(
        buffer, buffer.size.toLong(), passwordBytes, passwordBytes.size.toLong(), salt,
        crypto_pwhash_argon2i_OPSLIMIT_MODERATE.toLong(),
        NativeLong(crypto_pwhash_argon2i_MEMLIMIT_MODERATE.toLong()),
        crypto_pwhash_argon2i_ALG_ARGON2I13
    )

    require(result == 0) { "It was not possible to derive key!" }
    return buffer
}
