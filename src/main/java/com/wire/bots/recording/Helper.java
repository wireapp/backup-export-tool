package com.wire.bots.recording;

import com.wire.bots.sdk.Configuration;
import com.wire.bots.sdk.exceptions.HttpException;
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

import javax.annotation.Nullable;
import javax.naming.AuthenticationException;
import javax.ws.rs.client.Client;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

class Helper {

    @Nullable
    static byte[] getProfile(UUID userId) throws HttpException, AuthenticationException {
        Client client = Service.instance.getClient();
        String email = Configuration.propOrEnv("email", true);
        String password = Configuration.propOrEnv("password", true);

        LoginClient loginClient = new LoginClient(client);
        User robin = loginClient.login(email, password);
        String token = robin.getToken();
        API api = new API(client, null, token);
        for (Asset asset : api.getUser(userId).assets) {
            Logger.debug("Downloading asset:%s %s:%s", asset.size, asset.type, asset.key);
            if (asset.size.equals("preview"))
                return api.downloadAsset(asset.key, null);
        }
        return null;
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
}
