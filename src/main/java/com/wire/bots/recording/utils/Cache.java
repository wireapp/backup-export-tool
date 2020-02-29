package com.wire.bots.recording.utils;

import com.wire.bots.recording.Service;
import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.exceptions.HttpException;
import com.wire.bots.sdk.models.MessageAssetBase;
import com.wire.bots.sdk.server.model.User;
import com.wire.bots.sdk.tools.Logger;
import com.wire.bots.sdk.user.API;
import com.wire.bots.sdk.user.LoginClient;
import com.wire.bots.sdk.user.model.Access;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Cache {
    private static final ConcurrentHashMap<String, File> assetsMap = new ConcurrentHashMap<>();//<assetKey, File>
    private static final ConcurrentHashMap<UUID, User> users = new ConcurrentHashMap<>();//<userId, User>
    private static final ConcurrentHashMap<UUID, User> profiles = new ConcurrentHashMap<>();//<userId, User>

    File getAssetFile(WireClient client, MessageAssetBase message) {
        return assetsMap.computeIfAbsent(message.getAssetKey(), k -> {
            try {
                return Helper.downloadAsset(client, message);
            } catch (Exception e) {
                Logger.error("Cache.getAssetFile: %s", e);
                return Helper.assetFile(message.getAssetKey(), message.getMimeType());
            }
        });
    }

    File getProfileImage(WireClient client, String key) {
        return assetsMap.computeIfAbsent(key, k -> {
            try {
                return Helper.getProfile(client, key);
            } catch (Exception e) {
                Logger.error("Cache.getProfileImage: key: %s, ex: %s", key, e);
                return new File(Helper.avatarFile(key));
            }
        });
    }

    public User getProfile(UUID userId) {
        return profiles.computeIfAbsent(userId, k -> {
            String email = Service.instance.getConfig().email;
            String password = Service.instance.getConfig().password;

            LoginClient loginClient = new LoginClient(Service.instance.getClient());
            Access access = null;
            try {
                access = getAccess(email, password, loginClient);
                API api = new API(Service.instance.getClient(), null, access.getToken());
                return api.getUser(userId);
            } catch (Exception e) {
                Logger.error("Cache.getProfile: userId: %s, ex: %s", userId, e);
                User ret = new User();
                ret.id = userId;
                ret.name = userId.toString();
                return ret;
            } finally {
                if (access != null) {
                    try {
                        loginClient.logout(access.getToken(), access.getCookie());
                    } catch (HttpException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private Access getAccess(String email, String password, LoginClient loginClient) throws HttpException, InterruptedException {
        int retries = 1;
        HttpException exception = null;
        while (retries < 5) {
            try {
                return loginClient.login(email, password);
            } catch (HttpException e) {
                exception = e;

                if (e.getStatusCode() != 420)
                    break;

                Logger.warning("getAccess: %s, %d, retrying...", e.getMessage(), e.getStatusCode());
                retries++;
                Thread.sleep(5 * 1000);
            }
        }
        throw exception;
    }

    public User getUser(WireClient client, UUID userId) {
        return users.computeIfAbsent(userId, k -> {
            try {
                return client.getUser(userId);
            } catch (Exception e) {
                Logger.error("Cache.getUser: userId: %s, ex: %s", userId, e);
                User ret = new User();
                ret.id = userId;
                ret.name = userId.toString();
                return ret;
            }
        });
    }

    public void clear(UUID userId) {
        users.remove(userId);
        profiles.remove(userId);
    }
}
