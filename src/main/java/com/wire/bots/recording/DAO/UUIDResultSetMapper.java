package com.wire.bots.recording.DAO;

import com.wire.bots.sdk.tools.Logger;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.util.UUID;

public class UUIDResultSetMapper implements ResultSetMapper<UUID> {
    @Override
    public UUID map(int i, ResultSet rs, StatementContext statementContext) {
        try {
            Object uuid = rs.getObject("uuid");
            if (uuid != null)
                return (UUID) uuid;
            return null;
        } catch (Exception e) {
            Logger.error("UUIDResultSetMapper: %d %e", i, e);
            return null;
        }
    }
}
