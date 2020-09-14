package com.wire.backups.exports.android.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty


/**
 * Metadata about export.
 *
 * According to the https://github.com/wearezeta/documentation/tree/0180afa7073909aef1dd199cd48fcddf4d915038/topics/backup#metadata-file
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ExportMetadata(
    @JsonProperty("platform")
    val platform: String,
    @JsonProperty("version")
    val version: String,
    @JsonProperty("user_id")
    val userId: String,
    @JsonProperty("creation_time")
    val createdTime: String,
    @JsonProperty("client_id")
    val clientId: String
)
