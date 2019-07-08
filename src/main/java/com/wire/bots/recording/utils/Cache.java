package com.wire.bots.recording.utils;

import com.wire.bots.recording.Service;
import com.wire.bots.recording.model.DBRecord;
import com.wire.bots.sdk.Configuration;
import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.assets.Picture;
import com.wire.bots.sdk.tools.Logger;
import com.wire.bots.sdk.user.API;
import com.wire.bots.sdk.user.LoginClient;
import com.wire.bots.sdk.user.model.User;

import javax.annotation.Nullable;
import javax.ws.rs.client.Client;
import java.io.File;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

class Cache {
    private static final ConcurrentHashMap<String, Picture> pictures = new ConcurrentHashMap<>();//<Url, Picture>
    private static final ConcurrentHashMap<String, File> assets = new ConcurrentHashMap<>();//<assetKey, File>
    private static final ConcurrentHashMap<UUID, File> profiles = new ConcurrentHashMap<>();//<userId, File>
    private static API api;

    static {
        api = getApi();
    }

    static File downloadImage(WireClient client, DBRecord record) {
        return assets.computeIfAbsent(record.assetKey, k -> {
            try {
                return Helper.downloadImage(client, record);
            } catch (Exception e) {
                Logger.warning("Cache.downloadImage: assetId: %s, ex: %s", k, e);
                return null;
            }
        });
    }

    @Nullable
    static File getProfile(UUID userId) {
        return profiles.computeIfAbsent(userId, k -> {
            try {
                return Helper.getProfile(api, userId);
            } catch (Exception e) {
                Logger.warning("Cache.getProfile: userId: %s, ex: %s", k, e);
                api = getApi();
                return null;
            }
        });
    }

    @Nullable
    static Picture getPictureUrl(WireClient client, String url) {
        return pictures.computeIfAbsent(url, k -> {
            try {
                return Helper.upload(client, url);
            } catch (Exception e) {
                Logger.warning("Cache.getPicture: url: %s, ex: %s", url, e);
                return null;
            }
        });
    }

    private static API getApi() {
        Client client = Service.instance.getClient();

        try {
            String email = Configuration.propOrEnv("email", true);
            String password = Configuration.propOrEnv("password", true);

            LoginClient loginClient = new LoginClient(client);
            User robin = loginClient.login(email, password);
            String token = robin.getToken();
            return new API(client, null, token);
        } catch (Exception e) {
            Logger.warning("getAPI: %s", e);
            return new API(client, null, null);
        }
    }
}
