package com.wire.backups.exports.android.database.loaders

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File

internal class GenericLoaderKtTest {

    @Test
    @Disabled("integration test")
    fun `try to load macieks backup`() {
        val export = createBackupExport(
            File("/Users/lukas/work/wire/backups-export/data/new-backups/maciek1010")
        )
        println(export)
    }
}



