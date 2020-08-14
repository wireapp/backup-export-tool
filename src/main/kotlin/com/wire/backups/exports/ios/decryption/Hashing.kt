package com.wire.backups.exports.ios.decryption

import com.goterl.lazycode.lazysodium.SodiumJava
import com.sun.jna.NativeLong
import com.wire.backups.exports.utils.crypto_pwhash_argon2i_ALG_ARGON2I13
import com.wire.backups.exports.utils.crypto_pwhash_argon2i_MEMLIMIT_INTERACTIVE
import com.wire.backups.exports.utils.crypto_pwhash_argon2i_OPSLIMIT_INTERACTIVE
import java.nio.ByteBuffer
import java.util.UUID

/*
    Taken from original repo
    https://github.com/wireapp/wire-ios-cryptobox/blob/00ec8c7262d49814744c733c6eaa92e99bcd6b42/WireCryptobox/ChaCha20Encryption.swift#L145
 */

/**
 * Creates hash of UUID.
 */
internal fun SodiumJava.iosUuidHash(
    uuid: UUID,
    salt: ByteArray
): ByteArray {
    crypto_secretstream_xchacha20poly1305_keybytes()
    val uuidBytes = asIosBytes(uuid)
    val hash = ByteArray(crypto_secretstream_xchacha20poly1305_keybytes())
    val ret = crypto_pwhash(
        hash, hash.size.toLong(), uuidBytes, uuidBytes.size.toLong(), salt,
        crypto_pwhash_argon2i_OPSLIMIT_INTERACTIVE.toLong(),
        NativeLong(crypto_pwhash_argon2i_MEMLIMIT_INTERACTIVE.toLong()),
        crypto_pwhash_argon2i_ALG_ARGON2I13
    )
    require(ret == 0) { "It was not possible to create hash!" }
    return hash
}

private fun asIosBytes(uuid: UUID): ByteArray =
// because of the bug in the iOS, we need to use 128 bytes
// https://github.com/wireapp/wire-ios-cryptobox/blob/00ec8c7262d49814744c733c6eaa92e99bcd6b42/WireCryptobox/ChaCha20Encryption.swift#L146
    ByteBuffer.wrap(ByteArray(128)).apply {
        putLong(uuid.mostSignificantBits)
        putLong(uuid.leastSignificantBits)
    }.array()
