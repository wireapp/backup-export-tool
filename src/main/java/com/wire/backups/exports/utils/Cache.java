package com.wire.backups.exports.utils;

import com.wire.backups.exports.exporters.ExportConfiguration;
import com.wire.helium.API;
import com.wire.helium.LoginClient;
import com.wire.helium.models.Access;
import com.wire.xenon.WireClient;
import com.wire.xenon.backend.models.User;
import com.wire.xenon.exceptions.HttpException;
import com.wire.xenon.models.MessageAssetBase;
import com.wire.xenon.tools.Logger;

import javax.ws.rs.client.Client;
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

    private final Client httpClient;
    private final WireClient wireClient;
    private final Helper helper;
    private final ExportConfiguration configuration;

    public Cache(
            Client httpClient,
            WireClient wireClient,
            Helper helper,
            ExportConfiguration configuration
    ) {
        this.httpClient = httpClient;
        this.wireClient = wireClient;
        this.helper = helper;
        this.configuration = configuration;
    }

    public Cache(Client httpClient, WireClient wireClient, ExportConfiguration configuration) {
        this(httpClient, wireClient, new Helper(), configuration);
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
        return wireClient.downloadAsset(message.getAssetKey(),
                message.getAssetToken(),
                message.getSha256(),
                message.getOtrKey());
    }

    protected User getUserInternal(UUID userId) throws HttpException {
        return wireClient.getUser(userId);
    }

    protected byte[] downloadProfilePicture(String key) throws Exception {
        return wireClient.downloadProfilePicture(key);
    }

    public User getProfile(UUID userId) {
        return profiles.computeIfAbsent(userId, k -> getUserObject(userId));
    }

    protected User getUserObject(UUID userId) {
        String email = configuration.getEmail();
        String password = configuration.getPassword();

        LoginClient loginClient = new LoginClient(this.httpClient);
        try {
            Access access = getAccess(email, password, loginClient);
            API api = new API(this.httpClient, null, access.getAccessToken());
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

    public String getUserName(UUID userId) {
        return getUserName(getUser(userId));
    }

    public String getUserName(User user) {
        return !user.name.trim().equals("default") ? user.name : user.id.toString();
    }
}
