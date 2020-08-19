package com.wire.backups.exports.ios.decryption

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.ByteBuffer
import java.util.Base64
import kotlin.test.assertTrue


class HeaderKtTest {
    @Test
    @Disabled
    fun `test read metadata`() {
        val databaseFile = File("ignored-assets/ios_backup.ios_wbu")
        val header = readHeader(ByteBuffer.wrap(databaseFile.readBytes()))
        assertNotNull(header)
        print(header)
    }

    @Test
    fun `test reading header`() {
        // given
        val bytes = Base64.getDecoder().decode("V0JVSQAAAQ8CgQ/ikb7pIkWDhhDkY7uMxemLjGnPNJ2ohITEekzYAzAxygPF36PpKw9HXrGZWg==")
        val buffer = ByteBuffer.wrap(bytes)
        // when
        val header = readHeader(buffer)
        val expectedSalt = intArrayOf(15, 2, 129, 15, 226, 145, 190, 233, 34, 69, 131, 134, 16, 228, 99, 187)
            .map { it.toByte() }
            .toByteArray()
        val expectedUuidHash = intArrayOf(
            140, 197, 233, 139, 140, 105, 207, 52, 157, 168, 132, 132, 196, 122,
            76, 216, 3, 48, 49, 202, 3, 197, 223, 163, 233, 43, 15, 71, 94, 177,
            153, 90
        )
            .map { it.toByte() }
            .toByteArray()
        assertTrue { header.salt.contentEquals(expectedSalt) }
        assertTrue { header.uuidHash.contentEquals(expectedUuidHash) }
    }
}
