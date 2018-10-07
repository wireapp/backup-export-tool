package com.wire.bots.alert;

import com.wire.bots.sdk.MessageHandlerBase;
import com.wire.bots.sdk.WireClient;
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
        Logger.info(String.format("onNewBot: bot: %s, user: %s",
                newBot.id,
                newBot.origin.id));
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
            db.unsubscribe(botId);
        } catch (SQLException e) {
            Logger.error("onBotRemoved: %s %s", botId, e);
        }
    }

    @Override
    public void onText(WireClient client, TextMessage msg) {
        try {
            String userId = msg.getUserId();
            Logger.info("Text. bot: %s, from: %s", client.getId(), userId);

            String botId = client.getId();
            String messageId = msg.getMessageId();
            String cmd = msg.getText().toLowerCase().trim();
            if (cmd.equals("/history")) {
                for (Database.Record record : db.getRecords(botId)) {
                    if (record.type.equals("txt")) {
                        String format = String.format("**%s**: _%s_", record.sender, record.text);
                        client.sendDirectText(format, userId);
                    } else if (record.type.startsWith("image")) {
                        byte[] img = client.downloadAsset(record.assetKey,
                                record.assetToken,
                                record.sha256,
                                record.otrKey);

                        client.sendDirectText(String.format("**%s** sent:", record.sender), userId);
                        client.sendDirectPicture(img, record.type, userId);
                    } else if (record.type.startsWith("file")) {
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
                    } else {
                        Logger.warning("What the hell is: %s", record.type);
                    }
                }
                return;
            }

            User user = client.getUser(userId);
            db.insertTextRecord(botId, messageId, user.name, msg.getText());
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("OnText: %s ex: %s", client.getId(), e);
        }
    }

    public void onImage(WireClient client, ImageMessage msg) {
        try {
            Logger.info("Image: type: %s, size: %,d KB, h: %d, w: %d, tag: %s",
                    msg.getMimeType(),
                    msg.getSize() / 1024,
                    msg.getHeight(),
                    msg.getWidth(),
                    msg.getTag()
            );
            User user = client.getUser(msg.getUserId());

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
            Logger.info("Attachment: name: %s, type: %s, size: %,d KB",
                    msg.getName(),
                    msg.getMimeType(),
                    msg.getSize() / 1024
            );

            User user = client.getUser(msg.getUserId());

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
