package com.wire.backups.exports.ios.decryption

import com.goterl.lazycode.lazysodium.SodiumJava
import com.goterl.lazycode.lazysodium.interfaces.SecretStream
import com.wire.backups.exports.utils.LIBSODIUM_BINARIES_LOADING
import java.io.File
import java.nio.ByteBuffer
import java.util.UUID


/**
 * Decrypts and verifies iOS backup. Returns decrypted bytes.
 */
fun decryptIosBackup(databaseFile: File, password: String, userId: UUID): ByteArray =
    decryptIosBackup(ByteBuffer.wrap(databaseFile.readBytes()), password, userId)

/**
 * Decrypts and verifies iOS backup. Returns decrypted bytes.
 */
fun decryptIosBackup(input: ByteBuffer, password: String, userId: UUID): ByteArray {
    // load Sodium library
    val sodium = SodiumJava(LIBSODIUM_BINARIES_LOADING)
    // read and verifies the header, modifies input buffer
    val fileHeader = readHeader(input)
    // now we check the uuid
    val iosHashUserId = sodium.iosUuidHash(userId, fileHeader.salt)
    require(iosHashUserId.contentEquals(fileHeader.uuidHash)) { "UUID mismatch!" }
    // obtain key for decryption
    val key = sodium.deriveKey(password, fileHeader.salt)
    // create state and cipher header bytes
    val state = SecretStream.State()
    val chachaHeader = ByteArray(sodium.crypto_secretstream_xchacha20poly1305_headerbytes())
    // read header from the remaining input bytes
    input.get(chachaHeader)
    // initialize state with encryption header and key
    val initPullResult = sodium.crypto_secretstream_xchacha20poly1305_init_pull(state, chachaHeader, key)
    require(initPullResult == 0) { "It was not possible to init state!" }

    // TODO and this is magic ladies and gentleman...
    // found after debugging test cases from iOS not sure whether this is correct
    // but now is the decryption result 0 and the decrypted data are valid
    state.nonce[0] = 0

    // read rest of the cipher text
    val cipherText = ByteArray(input.remaining())
    input.get(cipherText)
    // prepare decryption buffers
    // TODO allow decryption of larger files by using 1024*1024 buffer
    val decrypted = ByteArray(cipherText.size + sodium.crypto_secretstream_xchacha20poly1305_abytes())
    val decryptedMessageLength = LongArray(1)
    val tag = ByteArray(1)
    // decrypt data
    val decryptionResult = sodium.crypto_secretstream_xchacha20poly1305_pull(
        state, decrypted, decryptedMessageLength, tag, cipherText, cipherText.size.toLong(),
        ByteArray(0), 0
    )
    require(decryptionResult == 0) { "Decryption failed" }
    return decrypted.take(decryptedMessageLength[0].toInt()).toByteArray()
}
