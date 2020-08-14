package pw.forst.wire.backups.android.steps

import com.fasterxml.jackson.annotation.JsonProperty
import net.lingala.zip4j.ZipFile
import pw.forst.tools.katlib.parseJson
import pw.forst.tools.katlib.whenFalse
import java.io.File
import java.util.UUID

/**
 * Extracts SQLite database file.
 */
fun extractBackup(decryptedBackupZip: File, userId: UUID, pathToFolder: String): Pair<ExportMetadata, File> {
    val backupName = userId.toString()
    // extract files
    ZipFile(decryptedBackupZip).extractFile(backupName, pathToFolder)
    ZipFile(decryptedBackupZip).extractFile("export.json", pathToFolder)
    // read metadata
    val metaData =
        requireNotNull(
            parseJson<ExportMetadata>(
                File("$pathToFolder${File.separator}export.json").readText()
            )
        ) { "It was not possible to read required export metadata!" }
    // rename database file
    File("$pathToFolder${File.separator}$backupName")
        .renameTo(File("$pathToFolder${File.separator}$backupName.sqlite"))
        .whenFalse { throw IllegalStateException("It was not possible to rename the file!") }
    return metaData to File("$pathToFolder${File.separator}$backupName.sqlite")
}

/**
 * {"user_id":"2f9e89c9-78a7-477d-8def-fbd7ca3846b5","version":129,"creation_time":"2020-07-08T13:42:44.203Z","platform":"android"}
 */

data class ExportMetadata(
    @JsonProperty("user_id")
    val userId: String,
    @JsonProperty("version")
    val version: Int,
    @JsonProperty("creation_time")
    val creationTime: String,
    @JsonProperty("platform")
    val platform: String
)
