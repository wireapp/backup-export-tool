package com.wire.backups.exports.utils;

import com.wire.backups.exports.Service;
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
    /**
     * Actually can be left static, as this is cache and keys/user ids and stuff like that don't change.
     */
    private static final ConcurrentHashMap<String, File> assetsMap = new ConcurrentHashMap<>();//<assetKey, File>
    private static final ConcurrentHashMap<UUID, User> users = new ConcurrentHashMap<>();//<userId, User>
    private static final ConcurrentHashMap<UUID, User> profiles = new ConcurrentHashMap<>();//<userId, User>

    private final WireClient client;
    private final Helper helper;

    public Cache(WireClient client, Helper helper) {
        this.client = client;
        this.helper = helper;
    }

    public Cache(WireClient client) {
        this(client, new Helper());
    }

    public static void clear(UUID userId) {
        users.remove(userId);
        profiles.remove(userId);
    }

    File getAssetFile(MessageAssetBase message) {
        return assetsMap.computeIfAbsent(message.getAssetKey(), k -> {
            try {
                byte[] image = downloadAsset(message);
                return helper.saveAsset(image, message);
            } catch (Exception e) {
                Logger.error("Cache.getAssetFile: %s", e);
                return helper.assetFile(message.getAssetKey(), message.getMimeType());
            }
        });
    }

    File getProfileImage(String key) {
        return assetsMap.computeIfAbsent(key, k -> {
            try {
                byte[] profile = downloadProfilePicture(key);
                return helper.getProfile(profile, key);
            } catch (Exception e) {
                Logger.error("Cache.getProfileImage: key: %s, ex: %s", key, e);
                return new File(helper.avatarFile(key));
            }
        });
    }

    protected byte[] downloadAsset(MessageAssetBase message) throws Exception {
        return client.downloadAsset(message.getAssetKey(),
                message.getAssetToken(),
                message.getSha256(),
                message.getOtrKey());
    }

    protected User getUserInternal(UUID userId) throws HttpException {
        return client.getUser(userId);
    }

    protected byte[] downloadProfilePicture(String key) throws Exception {
        return client.downloadProfilePicture(key);
    }

    public User getProfile(UUID userId) {
        return profiles.computeIfAbsent(userId, k -> getUserObject(userId));
    }

    protected User getUserObject(UUID userId) {
        String email = Service.instance.getConfig().email;
        String password = Service.instance.getConfig().password;

        LoginClient loginClient = new LoginClient(Service.instance.getClient());
        try {
            Access access = getAccess(email, password, loginClient);
            API api = new API(Service.instance.getClient(), null, access.getToken());
            return api.getUser(userId);
        } catch (Exception e) {
            Logger.error("Cache.getUserObject: userId: %s, ex: %s", userId, e);
            User ret = new User();
            ret.id = userId;
            ret.name = userId.toString();
            return ret;
        }
    }

    private Access getAccess(String email, String password, LoginClient loginClient) throws HttpException, InterruptedException {
        int retries = 1;
        HttpException exception = null;
        while (retries < 5) {
            try {
                return loginClient.login(email, password);
            } catch (HttpException e) {
                exception = e;

                if (e.getCode() != 420)
                    break;

                Logger.warning("getAccess: %s, %d, retrying...", e.getMessage(), e.getCode());
                retries++;
                Thread.sleep(5 * 1000);
            }
        }
        throw exception;
    }

    public User getUser(UUID userId) {
        return users.computeIfAbsent(userId, k -> {
            try {
                return getUserInternal(userId);
            } catch (Exception e) {
                Logger.error("Cache.getUser: userId: %s, ex: %s", userId, e);
                User ret = new User();
                ret.id = userId;
                ret.name = userId.toString();
                return ret;
            }
        });
    }
}
