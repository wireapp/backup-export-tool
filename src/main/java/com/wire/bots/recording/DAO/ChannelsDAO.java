package com.wire.bots.recording.DAO;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;
import java.util.UUID;

public interface ChannelsDAO {
    @SqlUpdate("INSERT INTO Recording_Channels (conversationId, botId) " +
            "VALUES (:conversationId, :botId) ON CONFLICT (conversationId) DO NOTHING")
    int insert(@Bind("conversationId") UUID conversationId,
               @Bind("botId") UUID botId);

    @SqlQuery("SELECT conversationId AS UUID FROM Recording_Channels WHERE conversationId = :conversationId")
    @RegisterMapper(UUIDResultSetMapper.class)
    UUID contains(@Bind("conversationId") UUID conversationId);

    @SqlQuery("SELECT botId AS UUID FROM Recording_Channels WHERE conversationId = :conversationId")
    @RegisterMapper(UUIDResultSetMapper.class)
    UUID getBotId(@Bind("conversationId") UUID conversationId);

    @SqlQuery("SELECT conversationId AS UUID FROM Recording_Channels")
    @RegisterMapper(UUIDResultSetMapper.class)
    List<UUID> listConversations();

    @SqlUpdate("DELETE FROM Recording_Channels WHERE conversationId = :conversationId")
    int delete(@Bind("conversationId") UUID conversationId);
}
