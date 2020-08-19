package com.wire.backups.exports.android.database.converters

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.UUID

@Disabled
class DatabaseKtTest {
    @Test
    fun `extract database test`() {
        val db = "ignored-assets/16dd24f3-664d-4a9f-abea-008e25a9f1a1.sqlite"
        val userId = UUID.fromString("16dd24f3-664d-4a9f-abea-008e25a9f1a1")

        val databaseDto = extractDatabase(userId, db)
        println(databaseDto)
    }
}
