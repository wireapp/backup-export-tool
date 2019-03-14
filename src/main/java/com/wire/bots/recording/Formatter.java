package com.wire.bots.recording;

import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.assets.FileAsset;
import com.wire.bots.sdk.assets.FileAssetPreview;
import com.wire.bots.sdk.assets.Picture;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

class Formatter {
    private String sender = null;
    private ArrayList<Database.Record> records = new ArrayList<>();

    boolean add(Database.Record record) {
        if (sender == null) {
            sender = record.sender;
            records.add(record);
            return true;
        }
        if (sender.equals(record.sender)) {
            records.add(record);
            return true;
        }
        return false;
    }

    void print(WireClient client, String userId) throws Exception {
        if (sender == null)
            return;

        StringBuilder sb = new StringBuilder();
        sb.append("**").append(sender).append("**\n");

        for (Database.Record record : records) {
            String text = record.text;
            if (isTxt(record)) {
                sb.append("- ").append(text).append("\n");
                continue;
            }

            Picture preview = null;
            if (isHttp(record)) {
                preview = getPreview(client, text);
                if (preview == null) {
                    sb.append("- ").append(text).append("\n");
                    continue;
                }
            }

            client.sendDirectText(sb.toString(), userId);
            sb = new StringBuilder();

            if (isHttp(record)) {
                sendLinkPreview(client, userId, text, preview);
            } else if (isImage(record))
                sendPicture(client, userId, record);
            else {
                sendAttachment(client, userId, record);
            }
        }

        client.sendDirectText(sb.toString(), userId);
        sender = null;
        records.clear();
    }

    private boolean isTxt(Database.Record record) {
        return record.type.equals("txt") && !isHttp(record);
    }

    private boolean isImage(Database.Record record) {
        return record.type.startsWith("image");
    }

    private boolean isHttp(Database.Record record) {
        return record.type.equals("txt") && record.text.startsWith("http");
    }

    private void sendPicture(WireClient client, String userId, Database.Record record) throws Exception {
        Picture picture = new Picture();
        picture.setAssetKey(record.assetKey);
        picture.setAssetToken(record.assetToken);
        picture.setSha256(record.sha256);
        picture.setOtrKey(record.otrKey);
        picture.setMimeType(record.type);
        picture.setSize(record.size);
        picture.setHeight(record.height);
        picture.setWidth(record.width);
        client.sendDirectPicture(picture, userId);
    }

    private Picture getPreview(WireClient client, String url) throws IOException {
        String previewUrl = UrlUtil.extractPagePreview(url);
        if (previewUrl == null || previewUrl.isEmpty())
            return null;

        return Cache.getPictureUrl(client, previewUrl);
    }

    private void sendLinkPreview(WireClient client, String userId, String url, Picture preview) throws Exception {
        final String title = UrlUtil.extractPageTitle(url);
        client.sendDirectLinkPreview(url, title, preview, userId);
    }

    private void sendAttachment(WireClient client, String userId, Database.Record record) throws Exception {
        String messageId = UUID.randomUUID().toString();
        FileAssetPreview preview = new FileAssetPreview(record.filename, record.type, record.size, messageId);
        FileAsset asset = new FileAsset(record.assetKey, record.assetToken, record.sha256, messageId);
        client.sendDirectFile(preview, asset, userId);
    }
}
