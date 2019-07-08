package com.wire.bots.recording.utils;

import com.wire.bots.recording.model.DBRecord;
import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.assets.Picture;
import com.wire.bots.sdk.tools.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

class Cache {
    private static final ConcurrentHashMap<String, Picture> pictures = new ConcurrentHashMap<>();//<Url, Picture>
    private static final ConcurrentHashMap<String, File> assets = new ConcurrentHashMap<>();//<assetKey, File>
    private static final ConcurrentHashMap<UUID, File> profiles = new ConcurrentHashMap<>();//<userId, File>

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
            File file = Helper.avatarFile(userId);
            if (file.exists())
                return file;
            return Helper.getProfile(userId);
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
}
