package com.wire.backups.exports.android.database.converters

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import kotlin.test.Ignore
import kotlin.test.Test

@Ignore
class ConversationsTest {
    @Test
    fun `test get conversations`() {
        Database.connect(
            "jdbc:sqlite:ignored-assets/2f9e89c9-78a7-477d-8def-fbd7ca3846b5.sqlite"
        )

        val myId = UUID.fromString("2f9e89c9-78a7-477d-8def-fbd7ca3846b5")

        transaction {
            println(getNamedConversations())
            println(getDirectMessages(myId))
        }
    }
}
