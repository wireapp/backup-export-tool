package com.wire.bots.recording;

import com.wire.bots.sdk.Configuration;
import com.wire.bots.sdk.exceptions.HttpException;
import com.wire.bots.sdk.server.model.Asset;
import com.wire.bots.sdk.tools.Logger;
import com.wire.bots.sdk.user.API;
import com.wire.bots.sdk.user.LoginClient;
import com.wire.bots.sdk.user.model.User;

import javax.annotation.Nullable;
import javax.naming.AuthenticationException;
import javax.ws.rs.client.Client;
import java.util.UUID;

public class Helper {

    public static Client client;

    @Nullable
    public static byte[] getProfile(UUID userId) throws HttpException, AuthenticationException {
        String email = Configuration.propOrEnv("email", true);
        String password = Configuration.propOrEnv("password", true);

        LoginClient loginClient = new LoginClient(client);
        User robin = loginClient.login(email, password);
        String token = robin.getToken();
        API api = new API(client, null, token);
        com.wire.bots.sdk.server.model.User user = api.getUser(userId);
        for (Asset asset : user.assets) {
            Logger.debug("Downloading asset:%s %s:%s", asset.size, asset.type, asset.key);
            if (asset.size.equals("preview"))
                return api.downloadAsset(asset.key, null);
        }
        return null;
    }
}
