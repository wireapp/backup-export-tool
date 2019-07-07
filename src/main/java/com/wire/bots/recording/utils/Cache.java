package com.wire.bots.recording.utils;

import com.wire.bots.recording.model.DBRecord;
import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.assets.Picture;
import com.wire.bots.sdk.models.AssetKey;
import com.wire.bots.sdk.tools.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

class Cache {
    private static final ConcurrentHashMap<String, Picture> pictures = new ConcurrentHashMap<>();//<Url, Picture>
    private static final ConcurrentHashMap<String, File> assets = new ConcurrentHashMap<>();//<assetKey, File>
    private static final ConcurrentHashMap<UUID, File> profiles = new ConcurrentHashMap<>();//<userId, File>

    @Nullable
    static Picture getPictureUrl(WireClient client, String url) {
        return pictures.computeIfAbsent(url, k -> {
            try {
                return upload(client, url);
            } catch (Exception e) {
                Logger.warning("Cache.getPicture: url: %s, ex: %s", url, e);
                return null;
            }
        });
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
    static File getProfile(WireClient client, UUID userId) {
        return profiles.computeIfAbsent(userId, k -> {
            try {
                return Helper.getProfile(client, userId);
            } catch (Exception e) {
                Logger.warning("Cache.getProfile: userId: %s, ex: %s", k, e);
                return null;
            }
        });
    }

    private static Picture upload(WireClient client, String imgUrl) throws Exception {
        Picture preview = new Picture(imgUrl);
        preview.setPublic(true);

        AssetKey assetKey = client.uploadAsset(preview);
        preview.setAssetKey(assetKey.key);
        return preview;
    }
}
