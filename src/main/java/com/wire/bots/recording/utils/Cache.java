package com.wire.bots.recording.utils;

import com.wire.bots.sdk.models.MessageAssetBase;
import com.wire.bots.sdk.server.model.User;
import com.wire.bots.sdk.tools.Logger;
import com.wire.bots.sdk.user.API;

import java.io.File;
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

    File getAssetFile(MessageAssetBase message) {
        File file = pictures.computeIfAbsent(message.getAssetKey(), k -> {
            try {
                return Helper.downloadAsset(api, message);
            } catch (Exception e) {
                Logger.warning("Cache.getAssetFile: %s", e);
                try {
                    api = Helper.getApi();
                    return Helper.downloadAsset(api, message);
                } catch (Exception e1) {
                    Logger.error("Cache.getAssetFile: %s", e1);
                    return null;
                }
            }
        });

        if (file == null)
            file = Helper.assetFile(message.getAssetKey(), message.getMimeType());
        return file;
    }

    File getProfileImage(User user) {
        File file = profiles.computeIfAbsent(user.id, k -> {
            try {
                return Helper.getProfile(api, user);
            } catch (Exception e) {
                Logger.warning("Cache.getProfileImage: userId: %s, ex: %s", user.id, e);
                try {
                    api = Helper.getApi();
                    return Helper.getProfile(api, user);
                } catch (Exception e1) {
                    Logger.error("Cache.getProfileImage: userId: %s, ex: %s", user.id, e1);
                    return null;
                }
            }
        });

        if (file == null)
            file = new File(Helper.avatarFile(user.id));
        return file;
    }

    public User getUser(UUID userId) {
        User user = users.computeIfAbsent(userId, k -> {
            try {
                return api.getUser(userId);
            } catch (Exception e) {
                Logger.warning("Cache.getUser: userId: %s, ex: %s", userId, e);
                try {
                    api = Helper.getApi();
                    return api.getUser(userId);
                } catch (Exception e1) {
                    Logger.error("Cache.getUser: userId: %s, ex: %s", userId, e1);
                    return null;
                }
            }
        });

        if (user == null) {
            user = new User();
            user.id = userId;
            user.name = userId.toString();
        }
        return user;
    }
}
