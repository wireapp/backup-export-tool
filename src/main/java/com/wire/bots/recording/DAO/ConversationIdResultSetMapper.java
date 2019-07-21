package com.wire.bots.recording.DAO;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.util.UUID;

public class ConversationIdResultSetMapper implements ResultSetMapper<UUID> {
    @Override
    public UUID map(int i, ResultSet rs, StatementContext statementContext) {
        try {
            Object conversationId = rs.getObject("conversationId");
            if (conversationId != null)
                return (UUID) conversationId;

            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
