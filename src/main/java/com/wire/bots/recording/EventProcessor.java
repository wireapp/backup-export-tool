package com.wire.bots.recording;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wire.bots.recording.model.Event;
import com.wire.bots.recording.utils.CacheV2;
import com.wire.bots.recording.utils.CollectorV2;
import com.wire.bots.sdk.models.*;
import com.wire.bots.sdk.server.model.Member;
import com.wire.bots.sdk.server.model.SystemMessage;
import com.wire.bots.sdk.tools.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

class EventProcessor {
    private final CacheV2 cache;
    private final ObjectMapper mapper = new ObjectMapper();

    EventProcessor() {
        this.cache = new CacheV2();
    }

    File saveHtml(List<Event> events, String filename) throws IOException {
        CollectorV2 collector = new CollectorV2(cache);
        for (Event event : events) {
            add(collector, event);
        }
        return collector.executeFile(filename);
    }

    private void add(CollectorV2 collector, Event event) {
        try {
            switch (event.type) {
                case "conversation.create": {
                    SystemMessage msg = mapper.readValue(event.payload, SystemMessage.class);
                    collector.setConvName(msg.conversation.name);

                    String text = formatConversation(msg, collector.getCache());
                    collector.addSystem(text, msg.time, event.type, msg.id);
                }
                break;
                case "conversation.rename": {
                    SystemMessage msg = mapper.readValue(event.payload, SystemMessage.class);
                    String convName = msg.conversation.name;
                    collector.setConvName(convName);

                    String text = String.format("**%s** %s **%s**",
                            collector.getUserName(msg.from),
                            "renamed conversation",
                            convName);
                    collector.addSystem(text, msg.time, event.type, msg.id);
                }
                break;
                case "conversation.otr-message-add.new-text": {
                    TextMessage message = mapper.readValue(event.payload, TextMessage.class);
                    collector.add(message);
                }
                break;
                case "conversation.otr-message-add.new-attachment": {
                    AttachmentMessage message = mapper.readValue(event.payload, AttachmentMessage.class);
                    collector.add(message);
                }
                break;
                case "conversation.otr-message-add.new-image": {
                    ImageMessage message = mapper.readValue(event.payload, ImageMessage.class);
                    collector.add(message);
                }
                break;
                case "conversation.otr-message-add.new-link": {
                    LinkPreviewMessage message = mapper.readValue(event.payload, LinkPreviewMessage.class);
                    collector.addLink(message);
                }
                break;
                case "conversation.member-join": {
                    SystemMessage msg = mapper.readValue(event.payload, SystemMessage.class);
                    for (UUID userId : msg.users) {
                        String format = String.format("**%s** %s **%s**",
                                collector.getUserName(msg.from),
                                "added",
                                collector.getUserName(userId));
                        collector.addSystem(format, msg.time, event.type, msg.id);
                    }
                }
                break;
                case "conversation.member-leave": {
                    SystemMessage msg = mapper.readValue(event.payload, SystemMessage.class);
                    for (UUID userId : msg.users) {
                        String format = String.format("**%s** %s **%s**",
                                collector.getUserName(msg.from),
                                "removed",
                                collector.getUserName(userId));
                        collector.addSystem(format, msg.time, event.type, msg.id);
                    }
                }
                break;
                case "conversation.member-leave.bot-removed": {
                    SystemMessage msg = mapper.readValue(event.payload, SystemMessage.class);
                    String format = String.format("**%s** %s",
                            collector.getUserName(msg.from),
                            "stopped recording");
                    collector.addSystem(format, msg.time, event.type, msg.id);
                }
                break;
                case "conversation.otr-message-add.edit-text": {
                    EditedTextMessage message = mapper.readValue(event.payload, EditedTextMessage.class);
                    message.setText(message.getText());
                    collector.addEdit(message);
                }
                break;
                case "conversation.otr-message-add.delete-text": {
                    DeletedTextMessage msg = mapper.readValue(event.payload, DeletedTextMessage.class);
                    String userName = collector.getUserName(msg.getUserId());
                    String text = String.format("**%s** deleted something", userName);
                    collector.addSystem(text, msg.getTime(), event.type, msg.getMessageId());
                }
                break;
                case "conversation.otr-message-add.new-reaction": {
                    ReactionMessage message = mapper.readValue(event.payload, ReactionMessage.class);
                    collector.add(message);
                }
                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("MessageHandler.add: %s %s %s", event.conversationId, event.type, e);
        }
    }

    private String formatConversation(SystemMessage msg, CacheV2 cache) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("**%s** started recording in **%s** with: \n",
                cache.getUser(msg.from).name,
                msg.conversation.name));
        for (Member member : msg.conversation.members) {
            sb.append(String.format("- **%s** \n", cache.getUser(member.id).name));
        }
        return sb.toString();
    }
}
