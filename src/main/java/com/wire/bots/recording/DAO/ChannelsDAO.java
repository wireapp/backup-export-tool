package com.wire.bots.recording.DAO;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;
import java.util.UUID;

public interface ChannelsDAO {
    @SqlUpdate("INSERT INTO Recording_Channels (conversationId) VALUES (:conversationId) ON CONFLICT (conversationId) DO NOTHING")
    int insert(@Bind("conversationId") UUID conversationId);

    @SqlQuery("SELECT conversationId FROM Recording_Channels WHERE conversationId = :conversationId")
    @RegisterMapper(ConversationIdResultSetMapper.class)
    UUID get(@Bind("conversationId") UUID conversationId);

    @SqlQuery("SELECT conversationId FROM Recording_Channels")
    @RegisterMapper(ConversationIdResultSetMapper.class)
    List<UUID> listConversations();

    @SqlUpdate("DELETE FROM Recording_Channels WHERE conversationId = :conversationId")
    int delete(@Bind("conversationId") UUID conversationId);
}
