package com.wire.bots.recording.utils;

import com.wire.bots.recording.model.DBRecord;
import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.server.model.Asset;
import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Helper {

    static File getProfile(WireClient client, UUID userId) throws Exception {
        File file = avatarFile(userId);
        com.wire.bots.sdk.server.model.User user = client.getUser(userId.toString());
        for (Asset asset : user.assets) {
            if (asset.size.equals("preview")) {
                byte[] image = client.downloadProfilePicture(asset.key);
                saveImage(image, file);
            }
        }
        return file;
    }

    static File downloadImage(WireClient client, DBRecord record) throws Exception {
        byte[] image = client.downloadAsset(record.assetKey, record.assetToken, record.sha256, record.otrKey);
        File file = imageFile(record.assetKey, record.mimeType);
        return saveImage(image, file);
    }

    private static File saveImage(byte[] image, File file) throws IOException {
        try (DataOutputStream os = new DataOutputStream(new FileOutputStream(file))) {
            os.write(image);
        }
        return file;
    }

    public static String markdown2Html(String text, Boolean escape) {
        List<Extension> extensions = Collections.singletonList(AutolinkExtension.create());

        Parser parser = Parser
                .builder()
                .extensions(extensions)
                .build();

        Node document = parser.parse(text);
        HtmlRenderer renderer = HtmlRenderer
                .builder()
                .escapeHtml(escape)
                .extensions(extensions)
                .build();
        return renderer.render(document);
    }

    private static File avatarFile(UUID userId) {
        return new File(String.format("avatars/%s.png", userId));
    }

    private static File imageFile(String assetKey, String mimeType) {
        String[] split = mimeType.split("/");
        String extension = split.length == 1 ? split[0] : split[1];
        String filename = String.format("images/%s.%s", assetKey, extension);
        return new File(filename);
    }
}
