package com.wire.backups.exports.ios.database

import com.wire.backups.exports.ios.model.IosDatabaseDto

internal fun verifyDatabaseMetadata(db: IosDatabaseDto) {
    require(supportedModelVersions.contains(db.modelVersion)) {
        "Unsupported version of export! This tool supports $supportedModelVersions, but export is ${db.modelVersion}"
    }
}

private val supportedModelVersions = setOf(
    "2.81.0",
    "2.82.0"
)
