package com.wire.bots.recording.DAO;

import com.wire.bots.recording.model.DBRecord;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class HistoryResultSetMapper implements ResultSetMapper<DBRecord> {
    @Override
    public DBRecord map(int i, ResultSet rs, StatementContext statementContext) throws SQLException {
        DBRecord record = new DBRecord();
        record.senderId = (UUID) rs.getObject("senderId");
        record.sender = rs.getString("sender");
        record.accent = rs.getInt("accent");
        record.text = rs.getString("text");
        record.mimeType = rs.getString("mimeType");

        record.height = rs.getInt("height");
        record.width = rs.getInt("width");
        record.size = rs.getInt("size");

        record.assetKey = rs.getString("assetKey");
        record.assetToken = rs.getString("assetToken");
        record.filename = rs.getString("filename");

        record.otrKey = rs.getBytes("otrKey");
        record.sha256 = rs.getBytes("otrKey");

        record.timestamp = rs.getInt("timestamp");
        return record;
    }
}
