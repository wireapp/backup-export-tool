package com.wire.bots.recording;

import com.wire.bots.sdk.Configuration;
import com.wire.bots.sdk.tools.Logger;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

class Database {
    private final Configuration.DB conf;

    Database(Configuration.DB conf) {
        this.conf = conf;
    }

    boolean insertTextRecord(String botId, String msgId, String sender, String text) throws SQLException {
        try (Connection c = newConnection()) {
            PreparedStatement stmt = c.prepareStatement("INSERT INTO History (botId, messageId, sender, mimeType, text, timestamp)" +
                    " VALUES (?, ?, ?, ?, ?, ?)");
            stmt.setObject(1, UUID.fromString(botId));
            stmt.setString(2, msgId);
            stmt.setString(3, sender);
            stmt.setString(4, "txt");
            stmt.setString(5, text);
            stmt.setInt(6, (int) (new Date().getTime() / 1000));
            return stmt.executeUpdate() == 1;
        }
    }

    boolean updateTextRecord(String botId, String msgId, String text) throws SQLException {
        try (Connection c = newConnection()) {
            PreparedStatement stmt = c.prepareStatement("UPDATE History SET text = ? WHERE botId = ? AND messageId = ?");
            stmt.setString(1, text);
            stmt.setObject(2, UUID.fromString(botId));
            stmt.setString(3, msgId);
            return stmt.executeUpdate() == 1;
        }
    }

    boolean deleteRecord(String botId, String msgId) throws SQLException {
        try (Connection c = newConnection()) {
            PreparedStatement stmt = c.prepareStatement("DELETE FROM History WHERE botId = ? AND messageId = ?");
            stmt.setObject(1, UUID.fromString(botId));
            stmt.setString(2, msgId);
            return stmt.executeUpdate() == 1;
        }
    }

    boolean insertAssetRecord(String botId, String msgId, String sender, String mimeType, String assetKey, String token,
                              byte[] sha256, byte[] otrKey, String filename) throws SQLException {
        try (Connection c = newConnection()) {
            PreparedStatement stmt = c.prepareStatement("INSERT INTO History (botId, messageId, sender, mimeType," +
                    " assetKey, assetToken, sha256, otrKey, timestamp, filename)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            stmt.setObject(1, UUID.fromString(botId));
            stmt.setString(2, msgId);
            stmt.setString(3, sender);
            stmt.setString(4, mimeType);
            stmt.setString(5, assetKey);
            stmt.setString(6, token);
            stmt.setBinaryStream(7, new ByteArrayInputStream(sha256));
            stmt.setBinaryStream(8, new ByteArrayInputStream(otrKey));
            stmt.setInt(9, (int) (new Date().getTime() / 1000));
            stmt.setString(10, filename);
            return stmt.executeUpdate() == 1;
        }
    }

    ArrayList<Record> getRecords(String botId) throws Exception {
        ArrayList<Record> ret = new ArrayList<>();
        try (Connection c = newConnection()) {
            PreparedStatement stmt = c.prepareStatement("SELECT * FROM History WHERE botId = ? ORDER BY timestamp ASC");
            stmt.setObject(1, UUID.fromString(botId));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Record record = new Record();
                record.sender = rs.getString("sender");
                record.text = rs.getString("text");
                record.type = rs.getString("mimeType");
                if (!record.type.equals("txt")) {
                    record.assetKey = rs.getString("assetKey");
                    record.assetToken = rs.getString("assetToken");
                    InputStream sha256 = rs.getBinaryStream("sha256");
                    record.sha256 = new byte[sha256.available()];
                    if (sha256.available() != sha256.read(record.sha256))
                        Logger.warning("Sha256 incomplete read");
                    InputStream otrKey = rs.getBinaryStream("otrKey");
                    record.otrKey = new byte[otrKey.available()];
                    if (otrKey.available() != otrKey.read(record.otrKey))
                        Logger.warning("otrKey incomplete read");

                    record.filename = rs.getString("filename");
                }
                ret.add(record);
            }
        }
        return ret;
    }

    boolean unsubscribe(String botId) throws SQLException {
        try (Connection c = newConnection()) {
            PreparedStatement stmt = c.prepareStatement("DELETE FROM History WHERE botId = ?");
            stmt.setObject(1, UUID.fromString(botId));
            return stmt.executeUpdate() > 0;
        }
    }

    private Connection newConnection() throws SQLException {
        String url = String.format("jdbc:%s://%s:%d/%s", conf.driver, conf.host, conf.port, conf.database);
        return DriverManager.getConnection(url, conf.user, conf.password);
    }

    static class Record {
        String sender;
        String text;
        String type;
        String assetKey;
        String assetToken;
        byte[] sha256;
        byte[] otrKey;
        String filename;
    }
}
