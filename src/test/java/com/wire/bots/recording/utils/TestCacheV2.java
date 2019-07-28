package com.wire.bots.recording.utils;

import com.wire.bots.recording.ConversationTemplateTest;
import com.wire.bots.sdk.models.MessageAssetBase;
import com.wire.bots.sdk.server.model.User;

import java.io.File;
import java.util.UUID;

public class TestCacheV2 extends CacheV2 {
    public TestCacheV2() {
        super(null);
    }

    @Override
    public User getUser(UUID userId) {
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
    File getProfileImage(User user) {
        return new File(String.format("src/test/resources/recording/avatars/%s.png", user.id));
    }

    @Override
    File getAssetFile(MessageAssetBase message) {
        String extension = HelperV2.getExtension(message.getMimeType());
        return new File(String.format("src/test/resources/recording/images/%s.%s",
                message.getAssetKey(),
                extension));
    }
}
