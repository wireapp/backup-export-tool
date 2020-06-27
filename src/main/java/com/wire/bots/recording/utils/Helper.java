package com.wire.bots.recording.utils;

import com.wire.bots.sdk.models.MessageAssetBase;
import com.wire.bots.sdk.tools.Logger;
import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import javax.annotation.Nullable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

class Helper {
    private static final List<Extension> extensions = Collections.singletonList(AutolinkExtension.create());
    private static final Parser parser = Parser
            .builder()
            .extensions(extensions)
            .build();

    static File getProfile(byte[] profile, String key) throws Exception {
        String filename = avatarFile(key);
        File file = new File(filename);

        Logger.info("downloaded profile: %s, size: %d, file: %s", key, profile.length, file.getAbsolutePath());
        return save(profile, file);
    }

    static File saveAsset(byte[] image, MessageAssetBase message) throws Exception {
        File file = assetFile(message.getAssetKey(), message.getMimeType());
        return save(image, file);
    }

    private static File save(byte[] image, File file) throws IOException {
        try (DataOutputStream os = new DataOutputStream(new FileOutputStream(file))) {
            os.write(image);
        }
        return file;
    }

    static File assetFile(String assetKey, String mimeType) {
        String extension = getExtension(mimeType);
        if (extension.isEmpty())
            extension = "error";
        String filename = String.format("recording/images/%s.%s", assetKey, extension);
        return new File(filename);
    }

    static String getExtension(String mimeType) {
        String[] split = mimeType.split("/");
        return split.length == 1 ? split[0] : split[1];
    }

    static String avatarFile(String key) {
        return String.format("recording/avatars/%s.png", key);
    }

    @Nullable
    static String markdown2Html(@Nullable String text) {
        if (text == null)
            return null;
        Node document = parser.parse(text);
        return HtmlRenderer
                .builder()
                .escapeHtml(true)
                .extensions(extensions)
                .build()
                .render(document);
    }
}
