package com.wire.backups.exports.api

import java.io.File
import java.util.UUID

/**
 * Builder for exporting databases from Wire client backups.
 */
object DatabaseExport {

    /**
     * Entry point for decryption and database export.
     */
    @Suppress("unused") // not true, used as API
    @JvmStatic
    fun builder() = Builder()

    @Suppress("MemberVisibilityCanBePrivate", "unused") // because it is an API
    class Builder internal constructor() {

        internal lateinit var userId: UUID
        internal lateinit var inputFile: File
        internal lateinit var databasePassword: String
        internal var outputDirectory: File = File("./backup-export")

        /**
         * Sets ID of user who created the backup.
         *
         * Used to verify that decryption is correct and that the backup
         * belongs to correct user.
         */
        fun forUserId(id: String) = forUserId(UUID.fromString(id))

        /**
         * Sets ID of user who created the backup.
         *
         * Used to verify that decryption is correct and that the backup
         * belongs to correct user.
         */
        fun forUserId(id: UUID) = this.also { userId = id }

        /**
         * Set path to encrypted export from Wire client application.
         */
        fun fromEncryptedExport(file: String) = fromEncryptedExport(File(file))

        /**
         * Set path to encrypted export from Wire client application.
         */
        fun fromEncryptedExport(file: File) = this.also {
            require(file.exists()) { "The provided encrypted database file does not exist!" }
            inputFile = file
        }

        /**
         * Set password to the encrypted export.
         */
        fun withPassword(password: String) = this.also { databasePassword = password }

        /**
         * Optionally set output directory for the output.
         */
        fun toOutputDirectory(directory: String) = toOutputDirectory(File(directory))

        /**
         * Optionally set output directory for the output.
         */
        fun toOutputDirectory(directory: File) = this.also { outputDirectory = directory }

        /**
         * This backup is from iOS.
         */
        fun buildForIosBackup() = IosBackupExport(this)

        /**
         * This backup is from Android.
         */
        fun buildForAndroidBackup() = AndroidBackupExport(this)
    }
}
