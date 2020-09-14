package com.wire.backups.exports.android

import com.wire.backups.exports.android.decryption.decryptAndroidBackup
import com.wire.backups.exports.android.decryption.extractBackup
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertNotNull

@Disabled
class DecryptionTest {

    @Test
    fun `test decryption lukas`() {
        val db = File("ignored-assets/lukas_forst.android_wbu")
        val userId = "accf53a3-2c29-4150-8be2-0b2b8d832b8b"
        val password = "Qwerty1! "
        val decrypted = decryptAndroidBackup(db, userId = userId, password = password)

        val (metadata, file) = extractBackup(decrypted, "data")
        assertNotNull(file)
        print(metadata)
    }
}
