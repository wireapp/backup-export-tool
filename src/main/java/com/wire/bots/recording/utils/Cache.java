package com.wire.bots.recording.utils;

import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.models.MessageAssetBase;
import com.wire.bots.sdk.server.model.Asset;
import com.wire.bots.sdk.server.model.User;
import com.wire.bots.sdk.tools.Logger;
import com.wire.bots.sdk.user.API;

import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Cache {
    private static final ConcurrentHashMap<String, File> pictures = new ConcurrentHashMap<>();//<assetKey, Picture>
    private static final ConcurrentHashMap<UUID, User> users = new ConcurrentHashMap<>();//<userId, User>
    private static final ConcurrentHashMap<UUID, File> profiles = new ConcurrentHashMap<>();//<userId, Picture>
    private API api;

    public Cache(API api) {
        this.api = api;
    }

    public Cache() {
        try {
            api = Helper.getApi();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    File getAssetFile(WireClient client, MessageAssetBase message) {
        return pictures.computeIfAbsent(message.getAssetKey(), k -> {
            try {
                return Helper.downloadAsset(client, message);
            } catch (Exception e) {
                Logger.error("Cache.getAssetFile: %s", e);
                return Helper.assetFile(message.getAssetKey(), message.getMimeType());
            }
        });
    }

    File getProfileImage(WireClient client, UUID userId, List<Asset> assets) {
        return profiles.computeIfAbsent(userId, k -> {
            try {
                return Helper.getProfile(client, userId, assets);
            } catch (Exception e) {
                Logger.error("Cache.getProfileImage: userId: %s, ex: %s", userId, e);
                return new File(Helper.avatarFile(userId));
            }
        });
    }

    public User getUserProfiles(UUID userId) {
        return users.computeIfAbsent(userId, k -> {
            try {
                return api.getUser(userId);
            } catch (Exception e) {
                Logger.warning("Cache.getUser: userId: %s, ex: %s", userId, e);
                try {
                    api = Helper.getApi();
                    return api.getUser(userId);
                } catch (Exception e1) {
                    Logger.error("Cache.getUser: userId: %s, ex: %s", userId, e1);
                    User ret = new User();
                    ret.id = userId;
                    ret.name = userId.toString();
                    return ret;
                }
            }
        });
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
