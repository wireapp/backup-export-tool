package com.wire.bots.recording;

import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.assets.Picture;
import com.wire.bots.sdk.models.AssetKey;
import com.wire.bots.sdk.tools.Logger;

import javax.annotation.Nullable;
import java.util.concurrent.ConcurrentHashMap;

class Cache {
    private static final ConcurrentHashMap<String, Picture> pictures = new ConcurrentHashMap<>();//<Url, Picture>

    @Nullable
    static Picture getPictureUrl(WireClient client, String url) {
        return pictures.computeIfAbsent(url, k -> {
            try {
                return upload(client, url);
            } catch (Exception e) {
                e.printStackTrace();
                Logger.warning("Cache.getPicture: url: %s, ex: %s", url, e);
                return null;
            }
        });
    }

    private static Picture upload(WireClient client, String imgUrl) throws Exception {
        Picture preview = new Picture(imgUrl);
        preview.setPublic(true);
        preview.setRetention("eternal");

        AssetKey assetKey = client.uploadAsset(preview);
        preview.setAssetKey(assetKey.key);
        return preview;
    }
}
