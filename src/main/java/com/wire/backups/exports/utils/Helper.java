package com.wire.backups.exports.utils;

import com.wire.xenon.models.MessageAssetBase;
import com.wire.xenon.tools.Logger;
import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jetbrains.annotations.Nullable;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class Helper {
    private static final List<Extension> extensions = Collections.singletonList(AutolinkExtension.create());

    private static final Parser parser = Parser
            .builder()
            .extensions(extensions)
            .build();
    private String root;

    public Helper(String root) {
        this.root = root;
    }


    public Helper() {
        this("recording");
    }

    static String getExtension(String mimeType) {
        String[] split = mimeType.split("/");
        return split.length == 1 ? split[0] : split[1];
    }

    private static File save(byte[] image, File file) throws IOException {
        try (DataOutputStream os = new DataOutputStream(new FileOutputStream(file))) {
            os.write(image);
        }
        return file;
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

    public void setRoot(String root) {
        this.root = root;
    }

    File getProfile(byte[] profile, String key) throws Exception {
        String filename = avatarFile(key);
        File file = new File(filename);

        Logger.info("downloaded profile: %s, size: %d, file: %s", key, profile.length, file.getAbsolutePath());
        return save(profile, file);
    }

    File saveAsset(byte[] image, MessageAssetBase message) throws Exception {
        File file = assetFile(message.getAssetKey(), message.getMimeType());
        return save(image, file);
    }

    File assetFile(String assetKey, String mimeType) {
        String extension = mimeType != null ? getExtension(mimeType) : "";
        if (extension.isEmpty())
            extension = "error";
        String filename = String.format("%s/assets/%s.%s", root, assetKey, extension);
        return new File(filename);
    }

    String avatarFile(String key) {
        return String.format("%s/avatars/%s.png", root, key);
    }
}
