package com.wire.bots.recording.DAO;

import com.wire.bots.recording.model.DBRecord;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;
import java.util.UUID;

public interface HistoryDAO {
    @SqlUpdate("INSERT INTO History (botId, messageId, sender, mimeType, text, timestamp, accent, senderId)" +
            " VALUES (:botId, :messageId, :sender, 'txt', :text, :timestamp, :accent, :senderId)")
    int insertTextRecord(@Bind("botId") UUID botId,
                         @Bind("messageId") String messageId,
                         @Bind("sender") String sender,
                         @Bind("text") String text,
                         @Bind("accent") int accent,
                         @Bind("senderId") UUID senderId,
                         @Bind("timestamp") int timestamp);

    @SqlUpdate("UPDATE History SET text = :text WHERE botId = :botId AND messageId = :msgId")
    int updateTextRecord(@Bind("botId") UUID botId,
                         @Bind("msgId") String msgId,
                         @Bind("text") String text);

    @SqlUpdate("INSERT INTO History (botId, messageId, sender, mimeType, assetKey, assetToken, sha256, otrKey, " +
            "timestamp, filename, size, height, width, accent, senderId)" +
            " VALUES (:botId, :messageId, :sender, :mimeType, :assetKey, :assetToken, :sha256, :otrKey, :timestamp," +
            " :filename, :size, :height, :width, :accent, :senderId)")
    int insertAssetRecord(@Bind("botId") UUID botId,
                          @Bind("messageId") String messageId,
                          @Bind("sender") String sender,
                          @Bind("mimeType") String mimeType,
                          @Bind("assetKey") String assetKey,
                          @Bind("assetToken") String assetToken,
                          @Bind("sha256") byte[] sha256,
                          @Bind("otrKey") byte[] otrKey,
                          @Bind("filename") String filename,
                          @Bind("size") int size,
                          @Bind("height") int height,
                          @Bind("width") int width,
                          @Bind("accent") int accent,
                          @Bind("senderId") UUID senderId,
                          @Bind("timestamp") int timestamp);

    @SqlUpdate("DELETE FROM History WHERE botId = :botId AND messageId = :msgId")
    int remove(@Bind("botId") UUID botId,
               @Bind("msgId") String msgId);

    @SqlUpdate("DELETE FROM History WHERE botId = :botId")
    int unsubscribe(@Bind("botId") UUID botId);

    @SqlQuery("SELECT * FROM History WHERE botId = :botId ORDER BY timestamp ASC")
    @RegisterMapper(HistoryResultSetMapper.class)
    List<DBRecord> getRecords(@Bind("botId") UUID botId);
}
