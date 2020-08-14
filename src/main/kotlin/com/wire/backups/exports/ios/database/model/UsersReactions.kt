package com.wire.backups.exports.ios.database.model

import org.jetbrains.exposed.sql.Table

internal class UsersReactions(
    userEntityTypePk: Int,
    reactionEntityTypePk: Int
) : Table("Z_${reactionEntityTypePk}USERS") {
    val reactionId = integer("Z_${reactionEntityTypePk}REACTIONS") references Reactions.id
    val userId = integer("Z_${userEntityTypePk}USERS") references Users.id
    override val primaryKey = PrimaryKey(reactionId, userId)
}
