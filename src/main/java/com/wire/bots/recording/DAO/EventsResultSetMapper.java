package com.wire.bots.recording.DAO;

import com.wire.bots.recording.model.Event;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.util.UUID;

public class EventsResultSetMapper implements ResultSetMapper<Event> {
    @Override
    public Event map(int i, ResultSet rs, StatementContext statementContext) {
        Event event = new Event();
        try {
            Object conversationId = rs.getObject("conversationId");
            if (conversationId != null)
                event.conversationId = (UUID) conversationId;
            event.time = rs.getString("time");
            event.type = rs.getString("type");
            event.payload = rs.getString("payload");
            Object messageId = rs.getObject("messageId");
            if (messageId != null)
                event.messageId = (UUID) messageId;
            return event;
        } catch (Exception e) {
            // Logger.error("EventsResultSetMapper: %s", e);
            return event;
        }
    }
}
