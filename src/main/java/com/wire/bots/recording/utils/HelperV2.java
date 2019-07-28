package com.wire.bots.recording.utils;

import com.wire.bots.recording.Service;
import com.wire.bots.sdk.Configuration;
import com.wire.bots.sdk.exceptions.HttpException;
import com.wire.bots.sdk.models.MessageAssetBase;
import com.wire.bots.sdk.server.model.Asset;
import com.wire.bots.sdk.server.model.User;
import com.wire.bots.sdk.tools.Util;
import com.wire.bots.sdk.user.API;
import com.wire.bots.sdk.user.LoginClient;
import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import javax.annotation.Nullable;
import javax.naming.AuthenticationException;
import javax.ws.rs.client.Client;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

class HelperV2 {
    private static final List<Extension> extensions = Collections.singletonList(AutolinkExtension.create());
    private static final Parser parser = Parser
            .builder()
            .extensions(extensions)
            .build();

    static File getProfile(API api, User user) throws IOException, HttpException {
        String filename = avatarFile(user.id);
        File file = new File(filename);
        if (user.assets == null)
            return file;

        for (Asset asset : user.assets) {
            if (asset.size.equals("preview")) {
                byte[] profile = api.downloadAsset(asset.key, null);
                save(profile, file);
                break;
            }
        }
        return file;
    }

    static File downloadAsset(API api, MessageAssetBase message) throws Exception {
        File file = assetFile(message.getAssetKey(), message.getMimeType());
        byte[] cipher = api.downloadAsset(message.getAssetKey(), message.getAssetToken());

        byte[] sha256 = MessageDigest.getInstance("SHA-256").digest(cipher);
        if (!Arrays.equals(sha256, message.getSha256()))
            throw new Exception("Failed sha256 check");

        byte[] image = Util.decrypt(message.getOtrKey(), cipher);
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
        String filename = String.format("images/%s.%s", assetKey, extension);
        return new File(filename);
    }

    private static String getExtension(String mimeType) {
        String[] split = mimeType.split("/");
        return split.length == 1 ? split[0] : split[1];
    }

    static String avatarFile(UUID senderId) {
        return String.format("avatars/%s.png", senderId);
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

    static API getApi() throws HttpException, AuthenticationException {
        String email = Configuration.propOrEnv("email", true);
        String password = Configuration.propOrEnv("password", true);

        Client client = Service.instance.getClient();
        LoginClient loginClient = new LoginClient(client);
        String token = loginClient.login(email, password).getToken();
        return new API(client, null, token);
    }
}
