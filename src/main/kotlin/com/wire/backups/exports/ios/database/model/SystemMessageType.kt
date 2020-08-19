@file:Suppress("unused") // because we can't manually set ordinals

package com.wire.backups.exports.ios.database.model

/**
 * Copy paste from original iOS repo:
 *
 * https://github.com/wireapp/wire-ios-data-model/blob/ceb2c1b06f01c9a4642d5a127bb82a519226d7c4/Source/Public/ZMMessage.h#L63
 */
enum class SystemMessageType {
    ZMSystemMessageTypeInvalid,
    ZMSystemMessageTypeParticipantsAdded,
    ZMSystemMessageTypeParticipantsRemoved,
    ZMSystemMessageTypeConversationNameChanged,
    ZMSystemMessageTypeConnectionRequest,
    ZMSystemMessageTypeConnectionUpdate,
    ZMSystemMessageTypeMissedCall,
    ZMSystemMessageTypeNewClient,
    ZMSystemMessageTypeIgnoredClient,
    ZMSystemMessageTypeConversationIsSecure,
    ZMSystemMessageTypePotentialGap,
    ZMSystemMessageTypeDecryptionFailed,
    ZMSystemMessageTypeDecryptionFailed_RemoteIdentityChanged,
    ZMSystemMessageTypeNewConversation,
    ZMSystemMessageTypeReactivatedDevice,
    ZMSystemMessageTypeUsingNewDevice,
    ZMSystemMessageTypeMessageDeletedForEveryone,
    ZMSystemMessageTypePerformedCall,
    ZMSystemMessageTypeTeamMemberLeave,
    ZMSystemMessageTypeMessageTimerUpdate,
    ZMSystemMessageTypeReadReceiptsEnabled,
    ZMSystemMessageTypeReadReceiptsDisabled,
    ZMSystemMessageTypeReadReceiptsOn,
    ZMSystemMessageTypeLegalHoldEnabled,
    ZMSystemMessageTypeLegalHoldDisabled
}


//typedef NS_ENUM(int16_t, ZMSystemMessageType) {
//    ZMSystemMessageTypeInvalid = 0,
//    ZMSystemMessageTypeParticipantsAdded = 1,
//    ZMSystemMessageTypeParticipantsRemoved = 2,
//    ZMSystemMessageTypeConversationNameChanged = 3,
//    ZMSystemMessageTypeConnectionRequest = 4,
//    ZMSystemMessageTypeConnectionUpdate = 5,
//    ZMSystemMessageTypeMissedCall =  6,
//    ZMSystemMessageTypeNewClient = 7,
//    ZMSystemMessageTypeIgnoredClient = 8,
//    ZMSystemMessageTypeConversationIsSecure = 9,
//    ZMSystemMessageTypePotentialGap = 10,
//    ZMSystemMessageTypeDecryptionFailed = 11,
//    ZMSystemMessageTypeDecryptionFailed_RemoteIdentityChanged = 12,
//    ZMSystemMessageTypeNewConversation = 13,
//    ZMSystemMessageTypeReactivatedDevice = 14,
//    ZMSystemMessageTypeUsingNewDevice = 15,
//    ZMSystemMessageTypeMessageDeletedForEveryone = 16,
//    ZMSystemMessageTypePerformedCall = 17,
//    ZMSystemMessageTypeTeamMemberLeave = 18,
//    ZMSystemMessageTypeMessageTimerUpdate = 19,
//    ZMSystemMessageTypeReadReceiptsEnabled = 20,
//    ZMSystemMessageTypeReadReceiptsDisabled = 21,
//    ZMSystemMessageTypeReadReceiptsOn = 22,
//    ZMSystemMessageTypeLegalHoldEnabled = 23,
//    ZMSystemMessageTypeLegalHoldDisabled = 24
//};
