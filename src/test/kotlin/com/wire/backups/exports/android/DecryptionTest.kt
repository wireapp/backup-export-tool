package com.wire.backups.exports.android

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import com.wire.backups.exports.android.steps.decryptDatabase
import com.wire.backups.exports.android.steps.extractBackup
import java.io.File
import java.util.UUID
import kotlin.test.assertNotNull

@Disabled
class DecryptionTest {

    @Test
    fun `test decryption maciek`() {
        val db = File("ignored-assets/Wire-maciek102-Backup_20200708.android_wbu")
        val userId = UUID.fromString("2f9e89c9-78a7-477d-8def-fbd7ca3846b5")
        val password = "Qwerty1!".toByteArray()
        val decrypted = decryptDatabase(db, password, userId)

        val (metadata, file) = extractBackup(decrypted, userId, ".")
        assertNotNull(file)
        print(metadata)
    }

    @Test
    fun `test decryption antje`() {
        val db = File("ignored-assets/Wire-antje5-Backup_20200723.android_wbu")
        val userId = UUID.fromString("16dd24f3-664d-4a9f-abea-008e25a9f1a1")
        val password = "Wire12345!".toByteArray()
        val decrypted = decryptDatabase(db, password, userId)

        val (metadata, file) = extractBackup(decrypted, userId, ".")
        assertNotNull(file)
        print(metadata)
    }
}
