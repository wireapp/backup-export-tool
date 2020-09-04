package com.wire.backups.exports.android.steps

import com.goterl.lazycode.lazysodium.SodiumJava
import com.goterl.lazycode.lazysodium.interfaces.SecretStream
import com.sun.jna.NativeLong
import com.wire.backups.exports.utils.crypto_pwhash_MEMLIMIT_INTERACTIVE
import com.wire.backups.exports.utils.crypto_pwhash_OPSLIMIT_INTERACTIVE
import com.wire.backups.exports.utils.crypto_pwhash_alg_default


/**
 * Decrypts given [input].
 */
internal fun SodiumJava.decrypt(input: ByteArray, password: ByteArray, metadata: EncryptedBackupHeader): ByteArray {
    val key = hash(password, metadata.salt, metadata.opslimit, metadata.memlimit)
    val streamHeaderLength = crypto_secretstream_xchacha20poly1305_headerbytes()
    val header = input.take(streamHeaderLength).toByteArray()
    val cipherText = input.drop(streamHeaderLength).toByteArray()

    val state = initPull(key, header)
    // output if decryption was success
    val decrypted = ByteArray(cipherText.size + crypto_secretstream_xchacha20poly1305_abytes())
    // decrypt data
    val returnCode = crypto_secretstream_xchacha20poly1305_pull(
        state, decrypted, LongArray(1), ByteArray(1), cipherText, cipherText.size.toLong(),
        ByteArray(0), 0
    )
    require(returnCode == 0) { "Failed to decrypt backup, got code $returnCode" }
    return decrypted
}

/**
 * Creates hash from the given input.
 */
internal fun SodiumJava.hash(
    passBytes: ByteArray,
    salt: ByteArray,
    opslimit: Int = crypto_pwhash_OPSLIMIT_INTERACTIVE,
    memlimit: Int = crypto_pwhash_MEMLIMIT_INTERACTIVE
): ByteArray {
    val outputLength = crypto_secretstream_xchacha20poly1305_keybytes()
    val output = ByteArray(outputLength)
    val ret = crypto_pwhash(
        output, output.size.toLong(), passBytes, passBytes.size.toLong(), salt,
        opslimit.toLong(),
        NativeLong(memlimit.toLong()),
        crypto_pwhash_alg_default
    )
    require(ret == 0) { "It was not possible to create hash!" }
    return output
}


private fun SodiumJava.initPull(key: ByteArray, header: ByteArray): SecretStream.State =
    initializeState(
        key,
        header
    ) { s, k, h -> crypto_secretstream_xchacha20poly1305_init_pull(s, k, h) }

private fun SodiumJava.initializeState(key: ByteArray, header: ByteArray, init: (SecretStream.State, ByteArray, ByteArray) -> Int):
        SecretStream.State {
    //Got this magic number from https://github.com/joshjdevl/libsodium-jni/blob/master/src/test/java/org/libsodium/jni/crypto/SecretStreamTest.java#L48
    val state = SecretStream.State()
    require(header.size == crypto_secretstream_xchacha20poly1305_headerbytes()) { "Invalid header length" }
    require(key.size == crypto_secretstream_xchacha20poly1305_keybytes()) { "Invalid key length" }
    val ret = init(state, header, key)
    require(ret == 0) { "It was not possible to initialize state." }
    return state

}
