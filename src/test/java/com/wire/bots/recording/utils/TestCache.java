package com.wire.bots.recording.utils;

import com.wire.bots.recording.ConversationTemplateTest;
import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.models.MessageAssetBase;
import com.wire.bots.sdk.server.model.Asset;
import com.wire.bots.sdk.server.model.User;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class TestCache extends Cache {
    public TestCache() {
        super(null);
    }

    @Override
    public User getUserProfiles(UUID userId) {
        User ret = new User();
        ret.id = userId;

        if (userId.equals(ConversationTemplateTest.dejan)) {
            ret.name = "Dejan";
            ret.accent = 7;
        } else {
            ret.name = "Lipis";
            ret.accent = 1;
        }
        return ret;
    }

    @Override
    public User getUser(WireClient client, UUID userId) {
        return getUserProfiles(userId);
    }

    @Override
    File getProfileImage(WireClient client, UUID userId, List<Asset> assets) {
        return new File(String.format("src/test/resources/recording/avatars/%s.png", userId));
    }

    @Override
    File getAssetFile(WireClient client, MessageAssetBase message) {
        String extension = Helper.getExtension(message.getMimeType());
        return new File(String.format("src/test/resources/recording/images/%s.%s",
                message.getAssetKey(),
                extension));
    }
}
