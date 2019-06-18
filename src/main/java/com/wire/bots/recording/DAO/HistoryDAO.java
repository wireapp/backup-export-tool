package com.wire.bots.recording.DAO;

import com.wire.bots.recording.model.DBRecord;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;
import java.util.UUID;

public interface HistoryDAO {
    @SqlUpdate("INSERT INTO History (botId, messageId, sender, mimeType, text, timestamp, accent, senderId)" +
            " VALUES (:botId, :messageId, :sender, 'txt', :text, :timestamp, :accent, :senderId)")
    int insertTextRecord(UUID botId, String messageId, String sender, String text, int accent, UUID senderId, int timestamp);

    @SqlUpdate("UPDATE History SET text = :text WHERE botId = :botId AND messageId = :msgId")
    int updateTextRecord(UUID botId, String msgId, String text);

    @SqlUpdate("INSERT INTO History (botId, messageId, sender, mimeType, assetKey, assetToken, sha256, otrKey, " +
            "timestamp, filename, size, height, width, accent, senderId)" +
            " VALUES (:botId, :messageId, :sender, :mimeType, :assetKey, :assetToken, :sha256, :otrKey, :timestamp," +
            " :filename, :size, :height, :width, :accent, :senderId)")
    int insertAssetRecord(UUID botId, String messageId, String sender, String mimeType, String assetKey, String token,
                          byte[] sha256, byte[] otrKey, String filename, int size, int height, int width, int accent,
                          UUID senderId, int timestamp);

    @SqlUpdate("DELETE FROM History WHERE botId = :botId AND messageId = :msgId")
    int remove(UUID botId, String msgId);

    @SqlUpdate("DELETE FROM History WHERE botId = :botId")
    int unsubscribe(UUID botId);

    @SqlQuery("SELECT * FROM History WHERE botId = :botId ORDER BY timestamp ASC")
    @RegisterMapper(HistoryResultSetMapper.class)
    List<DBRecord> getRecords(UUID botId);
}
