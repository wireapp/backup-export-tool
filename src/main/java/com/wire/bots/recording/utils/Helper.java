package com.wire.bots.recording.utils;

import com.wire.bots.recording.Service;
import com.wire.bots.recording.model.DBRecord;
import com.wire.bots.sdk.Configuration;
import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.assets.Picture;
import com.wire.bots.sdk.exceptions.HttpException;
import com.wire.bots.sdk.models.AssetKey;
import com.wire.bots.sdk.server.model.Asset;
import com.wire.bots.sdk.tools.Logger;
import com.wire.bots.sdk.user.API;
import com.wire.bots.sdk.user.LoginClient;
import com.wire.bots.sdk.user.model.User;
import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import javax.naming.AuthenticationException;
import javax.ws.rs.client.Client;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Helper {
    private static API api = null;
    public static String baseDir = "";

    static File getProfile(UUID userId) {
        File file = avatarFile(userId);
        try {
            com.wire.bots.sdk.server.model.User user = getApi().getUser(userId);
            if (user == null) {
                Logger.warning("getProfile: missing user: %s", userId);
                return file;
            }

            if (user.assets == null) {
                Logger.warning("getProfile: missing assets. User: %s", userId);
                return file;
            }

            for (Asset asset : user.assets) {
                if (asset.size.equals("preview")) {
                    byte[] image = getApi().downloadAsset(asset.key, null);
                    saveImage(image, file);
                }
            }
        } catch (AuthenticationException e) {
            Logger.warning("getProfile: %s %s", userId, e);
            api = null;
        } catch (Exception e) {
            Logger.warning("getProfile: %s %s", userId, e);
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

    static String markdown2Html(String text, Boolean escape) {
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

    static File avatarFile(UUID userId) {
        return new File(String.format("%savatars/%s.png", baseDir, userId));
    }

    private static File imageFile(String assetKey, String mimeType) {
        String[] split = mimeType.split("/");
        String extension = split.length == 1 ? split[0] : split[1];
        String filename = String.format("%simages/%s.%s", baseDir, assetKey, extension);
        return new File(filename);
    }

    static Picture upload(WireClient client, String imgUrl) throws Exception {
        Picture preview = new Picture(imgUrl);
        preview.setPublic(true);

        AssetKey assetKey = client.uploadAsset(preview);
        preview.setAssetKey(assetKey.key);
        return preview;
    }

    public static API getApi() throws HttpException, AuthenticationException {
        if (api != null)
            return api;

        String email = Configuration.propOrEnv("email", true);
        String password = Configuration.propOrEnv("password", true);

        Client client = Service.instance.getClient();
        LoginClient loginClient = new LoginClient(client);
        User robin = loginClient.login(email, password);
        String token = robin.getToken();
        api = new API(client, null, token);
        return api;
    }
}
