package com.wire.backups.exports.ios.export

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test


class DatabaseExportKtTest {
    @Test
    @Disabled
    fun `test database export`() {
        val db = "ignored-assets/dejan.ios_wbu"
        val userId = "a106fcd5-3146-4551-a870-9b13b125f376"
        val password = "Qwerty123!"
        val decrypted = exportIosDatabase(
            db, password, userId,
            outputPath = "ignored-assets/test"
        )
        print(decrypted)
    }
}
