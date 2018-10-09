package com.wire.bots.recording;

import com.wire.bots.sdk.MessageHandlerBase;
import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.assets.FileAsset;
import com.wire.bots.sdk.assets.FileAssetPreview;
import com.wire.bots.sdk.assets.Picture;
import com.wire.bots.sdk.models.AttachmentMessage;
import com.wire.bots.sdk.models.ImageMessage;
import com.wire.bots.sdk.models.TextMessage;
import com.wire.bots.sdk.server.model.NewBot;
import com.wire.bots.sdk.server.model.User;
import com.wire.bots.sdk.tools.Logger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

public class MessageHandler extends MessageHandlerBase {
    private final Database db;

    MessageHandler() {
        db = new Database(Service.config.getStorage());
    }

    @Override
    public boolean onNewBot(NewBot newBot) {
        Logger.debug("onNewBot: bot: %s, user: %s", newBot.id, newBot.origin.id);
        return true;
    }

    @Override
    public void onNewConversation(WireClient client) {
        try {
            client.sendText("Recording enabled.\nIn order to show the history of all messages posted here type: /history");
        } catch (Exception e) {
            Logger.error("onNewConversation: %s %s", client.getId(), e);
        }
    }

    @Override
    public void onMemberJoin(WireClient client, ArrayList<String> userIds) {
        try {
            Logger.debug("onMemberJoin: %s users: %s", client.getId(), userIds);

            for (String userId : userIds) {
                client.sendDirectText("Recording enabled.\n" +
                        "In order to show the history of all messages posted here type: /history", userId);
            }
        } catch (Exception e) {
            Logger.error("onMemberJoin: %s %s", client.getId(), e);
        }
    }

    @Override
    public void onBotRemoved(String botId) {
        try {
            Logger.debug("onBotRemoved: %s", botId);

            if (!db.unsubscribe(botId))
                Logger.warning("Failed to unsubscribe. bot: %s", botId);
        } catch (SQLException e) {
            Logger.error("onBotRemoved: %s %s", botId, e);
        }
    }

    @Override
    public void onText(WireClient client, TextMessage msg) {
        try {
            String userId = msg.getUserId();
            Logger.debug("Text. bot: %s, from: %s", client.getId(), userId);

            String botId = client.getId();
            String messageId = msg.getMessageId();
            String cmd = msg.getText().toLowerCase().trim();
            if (cmd.equals("/history")) {
                ArrayList<Database.Record> records = db.getRecords(botId);

                Logger.info("Sending %d records", records.size());
                for (Database.Record record : records) {
                    if (record.type.equals("txt")) {
                        sendText(client, userId, record);
                    } else if (record.type.startsWith("image")) {
                        sendPicture(client, userId, record);
                    } else {
                        sendAttachment(client, userId, record);
                    }
                }
                return;
            }

            Logger.debug("Inserting text, bot: %s %s", botId, messageId);

            User user = client.getUser(userId);
            if (!db.insertTextRecord(botId, messageId, user.name, msg.getText()))
                Logger.warning("Failed to insert a text record. %s, %s", botId, messageId);
        } catch (Exception e) {
            Logger.error("OnText: %s ex: %s", client.getId(), e);
        }
    }

    @Override
    public void onEditText(WireClient client, TextMessage msg) {
        String botId = client.getId();
        String messageId = msg.getMessageId();
        try {
            if (!db.updateTextRecord(botId, messageId, msg.getText()))
                Logger.warning("Failed to update a text record. %s, %s", botId, messageId);

        } catch (SQLException e) {
            Logger.error("onEditText: bot: %s message: %s, %s", botId, messageId, e);
        }
    }

    @Override
    public void onDelete(WireClient client, TextMessage msg) {
        String botId = client.getId();
        String messageId = msg.getMessageId();
        try {
            if (!db.deleteRecord(botId, messageId))
                Logger.warning("Failed to delete a record: %s, %s", botId, messageId);
        } catch (SQLException e) {
            Logger.error("onDelete: %s, %s, %s", botId, messageId, e);
        }
    }

    private void sendAttachment(WireClient client, String userId, Database.Record record) throws Exception {
//        byte[] img = client.downloadAsset(record.assetKey,
//                record.assetToken,
//                record.sha256,
//                record.otrKey);
//
//        // save it locally
//        File file = new File(record.filename);
//        try (FileOutputStream fos = new FileOutputStream(file)) {
//            fos.write(img);
//        }
//
//        client.sendDirectText(String.format("**%s** sent:", record.sender), userId);
//        client.sendDirectFile(file, record.type, userId);
//
//        if (!file.delete())
//            Logger.warning("Failed to delete file: %s", file.getName());


        String messageId = UUID.randomUUID().toString();
        FileAssetPreview preview = new FileAssetPreview(record.filename, record.type, record.size, messageId);
        FileAsset asset = new FileAsset(record.assetKey, record.assetToken, record.sha256, messageId);

        client.sendDirectText(String.format("**%s** sent:", record.sender), userId);
        client.sendDirectFile(preview, asset, userId);
    }

    private void sendText(WireClient client, String userId, Database.Record record) throws Exception {
        //is this an url
        if (record.text.startsWith("http") && sendLinkPreview(client, userId, record)) {
            return;
        }

        // This is plain text.. send it
        String format = String.format("**%s**: _%s_", record.sender, record.text);
        client.sendDirectText(format, userId);
    }

    private boolean sendLinkPreview(WireClient client, String userId, Database.Record record) throws Exception {
        final String url = record.text;
        final String title = UrlUtil.extractPageTitle(url);
        String previewUrl = UrlUtil.extractPagePreview(url);
        if (previewUrl == null || previewUrl.isEmpty())
            return false;

        final Picture preview = Cache.getPictureUrl(client, previewUrl);
        if (preview != null) {
            String text = String.format("**%s** sent:", record.sender);
            client.sendDirectText(text, userId);
            client.sendDirectLinkPreview(url, title, preview, userId);
            return true;
        }
        return false;
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

        client.sendDirectText(String.format("**%s** sent:", record.sender), userId);
        client.sendPicture(picture);
    }

    public void onImage(WireClient client, ImageMessage msg) {
        try {
            Logger.debug("Image: type: %s, size: %,d KB, h: %d, w: %d, tag: %s",
                    msg.getMimeType(),
                    msg.getSize() / 1024,
                    msg.getHeight(),
                    msg.getWidth(),
                    msg.getTag()
            );
            User user = client.getUser(msg.getUserId());

            String messageId = msg.getMessageId();
            String botId = client.getId();
            Logger.debug("Inserting image, bot: %s %s", botId, messageId);

            boolean insertRecord = db.insertAssetRecord(botId,
                    messageId,
                    user.name,
                    msg.getMimeType(),
                    msg.getAssetKey(),
                    msg.getAssetToken(),
                    msg.getSha256(),
                    msg.getOtrKey(),
                    msg.getName(),
                    (int) msg.getSize(),
                    msg.getHeight(),
                    msg.getWidth());

            if (!insertRecord)
                Logger.warning("Failed to insert attachment record. %s, %s", botId, messageId);

        } catch (Exception e) {
            Logger.error("onImage: %s", e);
        }
    }

    @Override
    public void onAttachment(WireClient client, AttachmentMessage msg) {
        try {
            Logger.debug("Attachment: name: %s, type: %s, size: %,d KB",
                    msg.getName(),
                    msg.getMimeType(),
                    msg.getSize() / 1024
            );

            User user = client.getUser(msg.getUserId());

            String botId = client.getId();
            String messageId = msg.getMessageId();
            Logger.debug("Inserting attachment, bot: %s %s", botId, messageId);

            boolean insertRecord = db.insertAssetRecord(botId,
                    messageId,
                    user.name,
                    msg.getMimeType(),
                    msg.getAssetKey(),
                    msg.getAssetToken(),
                    msg.getSha256(),
                    msg.getOtrKey(),
                    msg.getName(),
                    (int) msg.getSize(),
                    0,
                    0);

            if (!insertRecord)
                Logger.warning("Failed to insert attachment record. %s, %s", botId, messageId);

        } catch (Exception e) {
            Logger.error("onAttachment: %s", e);
        }
    }
}
