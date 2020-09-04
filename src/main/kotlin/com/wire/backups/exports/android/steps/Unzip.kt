package com.wire.backups.exports.android.steps

import com.wire.backups.exports.android.model.ExportMetadata
import net.lingala.zip4j.ZipFile
import pw.forst.tools.katlib.parseJson
import java.io.File

/**
 * Extracts SQLite database file.
 */
internal fun extractBackup(decryptedBackupZip: File, pathToFolder: String): Pair<ExportMetadata, File> {
    // extract files
    ZipFile(decryptedBackupZip).extractAll(pathToFolder)
    // read metadata
    val metaData =
        requireNotNull(
            parseJson<ExportMetadata>(
                File("$pathToFolder${File.separator}export.json").readText()
            )
        ) { "It was not possible to read required export metadata!" }
    // rename database file
    return metaData to File(pathToFolder)
}
