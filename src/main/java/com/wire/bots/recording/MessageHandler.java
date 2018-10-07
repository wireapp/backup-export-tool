package com.wire.bots.recording;

import com.wire.bots.sdk.MessageHandlerBase;
import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.assets.Picture;
import com.wire.bots.sdk.models.AttachmentMessage;
import com.wire.bots.sdk.models.ImageMessage;
import com.wire.bots.sdk.models.TextMessage;
import com.wire.bots.sdk.server.model.NewBot;
import com.wire.bots.sdk.server.model.User;
import com.wire.bots.sdk.tools.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.SQLException;
import java.util.ArrayList;

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

            db.unsubscribe(botId);
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
                    } else if (record.type.startsWith("file")) {
                        sendAttachment(client, userId, record);
                    } else {
                        Logger.warning("What the hell is: %s", record.type);
                    }
                }
                return;
            }

            Logger.debug("Inserting text, bot: %s %s", botId, messageId);

            User user = client.getUser(userId);
            db.insertTextRecord(botId, messageId, user.name, msg.getText());
        } catch (Exception e) {
            Logger.error("OnText: %s ex: %s", client.getId(), e);
        }
    }

    private void sendAttachment(WireClient client, String userId, Database.Record record) throws Exception {
        byte[] img = client.downloadAsset(record.assetKey,
                record.assetToken,
                record.sha256,
                record.otrKey);

        // save it locally
        File file = new File(record.filename);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(img);
        }

        client.sendDirectText(String.format("**%s** sent:", record.sender), userId);
        client.sendDirectFile(file, record.type, userId);

        file.delete();
    }

    private void sendText(WireClient client, String userId, Database.Record record) throws Exception {
        if (record.text.startsWith("http")) {
            final String url = record.text;
            final String title = UrlUtil.extractPageTitle(url);
            final Picture preview = Cache.getPictureUrl(client, UrlUtil.extractPagePreview(url));
            String text = String.format("**%s** sent:", record.sender);

            client.sendDirectText(text, userId);
            client.sendDirectLinkPreview(url, title, preview, userId);
        } else {
            String format = String.format("**%s**: _%s_", record.sender, record.text);
            client.sendDirectText(format, userId);
        }
    }

    private void sendPicture(WireClient client, String userId, Database.Record record) throws Exception {
        byte[] img = client.downloadAsset(record.assetKey,
                record.assetToken,
                record.sha256,
                record.otrKey);

        client.sendDirectText(String.format("**%s** sent:", record.sender), userId);
        client.sendDirectPicture(img, record.type, userId);
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

            Logger.debug("Inserting image, bot: %s %s", client.getId(), msg.getMessageId());

            db.insertAssetRecord(client.getId(),
                    msg.getMessageId(),
                    user.name,
                    msg.getMimeType(),
                    msg.getAssetKey(),
                    msg.getAssetToken(),
                    msg.getSha256(),
                    msg.getOtrKey(),
                    msg.getName());
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

            Logger.debug("Inserting attachment, bot: %s %s", client.getId(), msg.getMessageId());

            db.insertAssetRecord(client.getId(),
                    msg.getMessageId(),
                    user.name,
                    msg.getMimeType(),
                    msg.getAssetKey(),
                    msg.getAssetToken(),
                    msg.getSha256(),
                    msg.getOtrKey(),
                    msg.getName());
        } catch (Exception e) {
            Logger.error("onAttachment: %s", e);
        }
    }
}
