package com.wire.bots.recording;

import com.wire.bots.recording.model.Config;
import com.wire.bots.sdk.MessageHandlerBase;
import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.models.AttachmentMessage;
import com.wire.bots.sdk.models.ImageMessage;
import com.wire.bots.sdk.models.TextMessage;
import com.wire.bots.sdk.server.model.NewBot;
import com.wire.bots.sdk.server.model.User;
import com.wire.bots.sdk.tools.Logger;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import static com.wire.bots.recording.UrlUtil.getFile;

public class MessageHandler extends MessageHandlerBase {
    private static final String WELCOME_LABEL = "Recording was enabled.\nAvailable commands:\n" +
            "`/history` - receive previous messages\n" +
            "`/pdf` - receive previous messages in PDF format";
    private final Database db;

    MessageHandler(Config config) {
        db = new Database(config.getStorage());
    }

    @Override
    public boolean onNewBot(NewBot newBot) {
        Logger.debug("onNewBot: bot: %s, user: %s", newBot.id, newBot.origin.id);
        return true;
    }

    @Override
    public void onNewConversation(WireClient client) {
        try {
            client.sendText(WELCOME_LABEL);
        } catch (Exception e) {
            Logger.error("onNewConversation: %s %s", client.getId(), e);
        }
    }

    @Override
    public void onMemberJoin(WireClient client, ArrayList<String> userIds) {
        String botId = client.getId();
        Logger.debug("onMemberJoin: %s users: %s", botId, userIds);

        try {
            ArrayList<Database.Record> records = db.getRecords(botId);
            Logger.info("Sending %d records", records.size());

            Collector collector = collect(client, botId);

            for (String userId : userIds) {
                collector.send(client, userId);
            }
        } catch (Exception e) {
            Logger.error("onMemberJoin: %s %s", botId, e);
        }
    }

    @Override
    public void onBotRemoved(String botId) {
        Logger.debug("onBotRemoved: %s", botId);
        try {
            if (!db.unsubscribe(botId))
                Logger.warning("Failed to unsubscribe. bot: %s", botId);
        } catch (SQLException e) {
            Logger.error("onBotRemoved: %s %s", botId, e);
        }
    }

    @Override
    public void onText(WireClient client, TextMessage msg) {
        String userId = msg.getUserId();
        String botId = client.getId();
        String messageId = msg.getMessageId();

        Logger.debug("onText. bot: %s, msgId: %s", botId, messageId);
        try {
            String cmd = msg.getText().toLowerCase().trim();
            if (cmd.equals("/history")) {
                Formatter formatter = new Formatter();
                for (Database.Record record : db.getRecords(botId)) {
                    if (!formatter.add(record)) {
                        formatter.print(client, userId);
                        formatter.add(record);
                    }
                }
                formatter.print(client, userId);
                return;
            }

            if (cmd.equals("/pdf")) {
                Collector collector = collect(client, botId);
                collector.send(client, userId);
                return;
            }

            Logger.debug("Inserting text, bot: %s %s", botId, messageId);

            User user = client.getUser(userId);
            if (!db.insertTextRecord(botId, messageId, user.name, msg.getText(), user.accent, userId))
                Logger.warning("Failed to insert a text record. %s, %s", botId, messageId);
        } catch (Exception e) {
            e.printStackTrace();
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

    @Override
    public void onImage(WireClient client, ImageMessage msg) {
        String messageId = msg.getMessageId();
        String botId = client.getId();
        String userId = msg.getUserId();

        Logger.debug("onImage: %s type: %s, size: %,d KB, h: %d, w: %d, tag: %s",
                botId,
                msg.getMimeType(),
                msg.getSize() / 1024,
                msg.getHeight(),
                msg.getWidth(),
                msg.getTag()
        );

        try {
            User user = client.getUser(msg.getUserId());
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
                    msg.getWidth(),
                    user.accent,
                    userId);

            if (!insertRecord)
                Logger.warning("Failed to insert attachment record. %s, %s", botId, messageId);
        } catch (Exception e) {
            Logger.error("onImage: %s %s %s", botId, messageId, e);
        }
    }

    @Override
    public void onAttachment(WireClient client, AttachmentMessage msg) {
        String botId = client.getId();
        String messageId = msg.getMessageId();
        String userId = msg.getUserId();

        Logger.debug("onAttachment: %s, name: %s, type: %s, size: %,d KB",
                botId,
                msg.getName(),
                msg.getMimeType(),
                msg.getSize() / 1024
        );

        try {
            User user = client.getUser(msg.getUserId());
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
                    0,
                    user.accent,
                    userId);

            if (!insertRecord)
                Logger.warning("Failed to insert attachment record. %s, %s", botId, messageId);
        } catch (Exception e) {
            Logger.error("onAttachment: %s %s %s", botId, messageId, e);
        }
    }

    private Collector collect(WireClient client, String botId) throws Exception {
        Collector collector = new Collector();
        for (Database.Record record : db.getRecords(botId)) {
            if (record.type.startsWith("image")) {
                downloadImage(client, record);
            }
            collector.add(record);
        }
        return collector;
    }

    private void downloadImage(WireClient client, Database.Record record) {
        try {
            byte[] image = client.downloadAsset(record.assetKey, record.assetToken, record.sha256, record.otrKey);
            File file = saveImage(image, record.assetKey, record.type);
            assert file.exists();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File saveImage(byte[] image, String assetKey, String mimeType) throws IOException {
        File file = getFile(assetKey, mimeType);
        if (!file.exists()) {
            try (DataOutputStream os = new DataOutputStream(new FileOutputStream(file))) {
                os.write(image);
            }
        }
        return file;
    }
}
