package com.wire.backups.exports.exporters;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.wire.backups.exports.utils.Collector;
import com.wire.backups.exports.utils.Helper;
import com.wire.backups.exports.utils.InstantCache;
import com.wire.xenon.models.*;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import javax.ws.rs.client.Client;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class DesktopExporter extends ExporterBase {
    private final HashMap<UUID, _Conversation> conversationHashMap = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private _Export export;

    private static void unzip(String source, String destination) throws ZipException {
        ZipFile zipFile = new ZipFile(source);
        zipFile.extractAll(destination);
    }

    public DesktopExporter(ExportConfiguration config, Client client) {
        super(client, config);
    }

    @Override
    public void run() throws Exception {
        printVersion();

        String temporaryExtractionDirectory = (config.getOut() != null ? config.getOut() : ".") + "/tmp";

        final File inputDir = new File(temporaryExtractionDirectory);
        //noinspection ResultOfMethodCallIgnored
        inputDir.mkdirs();

        unzip(config.getIn(), inputDir.getAbsolutePath());

        objectMapper.addHandler(new _DeserializationProblemHandler());

        final File exportFile = new File(temporaryExtractionDirectory + "/export.json");

        export = objectMapper.readValue(exportFile, _Export.class);

        System.out.printf("Processing backup:\nDevice: %s\nUser: %s (@%s)\nid: %s\ncreated: %s\nplatform: %s\nversion: %d\n\n",
                export.client_id,
                export.user_name,
                export.user_handle,
                export.user_id,
                export.creation_time,
                export.platform,
                export.version);

        final String logicalRoot = String.format("%s/%s", export.user_handle, export.creation_time.replace(":", "-"));
        final String fileSystemRoot = (config.getOut() != null ? config.getOut() : ".") + String.format("/%s", logicalRoot);
        makeDirs(fileSystemRoot);

        final File eventsFile = new File(String.format("%s/in/%s", fileSystemRoot, "events.json"));
        final File conversationsFile = new File(String.format("%s/in/%s", fileSystemRoot, "conversations.json"));

        Files.copy(new File(temporaryExtractionDirectory + "/events.json").toPath(), eventsFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(new File(temporaryExtractionDirectory + "/conversations.json").toPath(), conversationsFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(exportFile.toPath(), new File(String.format("%s/in/%s", fileSystemRoot, "export.json")).toPath(), StandardCopyOption.REPLACE_EXISTING);

        final Event[] events = objectMapper.readValue(eventsFile, Event[].class);
        final _Conversation[] conversations = objectMapper.readValue(conversationsFile, _Conversation[].class);

        System.out.printf("Loaded: %d conversations and %d events\n\n",
                conversations.length,
                events.length);

        final Helper helper = new Helper(fileSystemRoot);
        this.logicalRoot = logicalRoot;

        InstantCache cache = new InstantCache(config, client, helper);

        processConversations(conversations, cache);

        processEvents(events, cache);

        createPDFs(fileSystemRoot, fileSystemRoot.replace("/" + logicalRoot, ""));
    }

    private void processConversations(_Conversation[] conversations, InstantCache cache) {
        for (_Conversation conversation : conversations) {
            try {
                if (conversation.name == null || conversation.name.isEmpty()) {
                    if (conversation.others != null && !conversation.others.isEmpty())
                        conversation.name = cache.getUserName(conversation.others.get(0));
                }
                conversationHashMap.put(conversation.id, conversation);

                System.out.printf("%s, id: %s, type: %d\n",
                        conversation.name,
                        conversation.id,
                        conversation.type);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void processEvents(Event[] events, InstantCache cache) {
        System.out.println("\nEvents:");

        for (Event event : events) {
            if (event.type == null)
                continue;

            System.out.printf("Id: %s, time: %s, conv: %s, type: %s\n",
                    event.id,
                    event.time,
                    event.conversation,
                    event.type
            );

            try {
                switch (event.type) {
                    case "conversation.group-creation": {
                        onGroupCreation(getCollector(event.conversation, cache), event);
                    }
                    break;
                    case "conversation.message-add": {
                        onMessageAdd(getCollector(event.conversation, cache), event);
                    }
                    break;
                    case "conversation.asset-add": {
                        onAssetAdd(getCollector(event.conversation, cache), event);
                    }
                    break;
                    case "conversation.member-join": {
                        onMemberJoin(getCollector(event.conversation, cache), event);
                    }
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Collector getCollector(UUID convId, InstantCache cache) {
        return collectorHashMap.computeIfAbsent(convId, x -> {
            Collector collector = new Collector(cache, logicalRoot);
            _Conversation conversation = getConversation(convId);
            collector.setConvName(conversation.name);
            collector.setConversationId(convId);
            collector.details = new Collector.Details();
            collector.details.name = export.user_name;
            collector.details.handle = export.user_handle;
            collector.details.id = export.user_id.toString();
            collector.details.device = export.client_id;
            collector.details.platform = export.platform;
            collector.details.date = export.creation_time;
            collector.details.version = String.valueOf(export.version);

            return collector;
        });
    }

    private _Conversation getConversation(UUID convId) {
        return conversationHashMap.get(convId);
    }

    private void onMemberJoin(Collector collector, Event event) throws ParseException {
        for (UUID userId : event.data.user_ids) {
            String txt = String.format("**%s** %s **%s**",
                    collector.getUserName(event.from),
                    "added",
                    collector.getUserName(userId));
            collector.addSystem(txt, event.time, "conversation.member-join", event.id);
        }
    }

    private void onAssetAdd(Collector collector, Event event) throws ParseException {
        if (event.data.otrKey == null || event.data.sha256 == null)
            return;
        if (event.data.contentType.startsWith("image")) {
            final ImageMessage img = new ImageMessage(event.id, event.conversation, null, event.from);
            img.setTime(event.time);
            img.setSize(event.data.contentLength);
            img.setMimeType(event.data.contentType);
            img.setAssetToken(event.data.token);
            img.setAssetKey(event.data.key);
            img.setOtrKey(toArray(event.data.otrKey));
            img.setSha256(toArray(event.data.sha256));
            collector.add(img);
        } else {
            final AttachmentMessage att = new AttachmentMessage(event.id, event.conversation, null, event.from);
            att.setTime(event.time);
            att.setSize(event.data.contentLength);
            att.setMimeType(event.data.contentType);
            att.setAssetToken(event.data.token);
            att.setAssetKey(event.data.key);
            att.setOtrKey(toArray(event.data.otrKey));
            att.setSha256(toArray(event.data.sha256));
            att.setName(event.data.info.name);
            collector.add(att);
        }
    }

    private void onMessageAdd(Collector collector, Event event) throws ParseException {
        if (event.data.replacingMessageId != null) {
            EditedTextMessage edit = new EditedTextMessage(event.id, event.conversation, null, event.from);
            edit.setText(event.data.content);
            edit.setTime(event.editedTime != null ? event.editedTime : event.time);
            edit.setReplacingMessageId(event.data.replacingMessageId);
            collector.addEdit(edit);
        } else {
            final TextMessage txt = new TextMessage(event.id, event.conversation, null, event.from);
            txt.setTime(event.time);
            txt.setText(event.data.content);
            if (event.data.quote != null) {
                txt.setQuotedMessageId(event.data.quote.messageId);
            }
            collector.add(txt);
        }

        if (event.reactions != null) {
            for (UUID userId : event.reactions.keySet()) {
                ReactionMessage like = new ReactionMessage(UUID.randomUUID(), event.conversation, null, userId);
                like.setReactionMessageId(event.id);
                like.setEmoji(event.reactions.get(userId));
                like.setTime(event.time);
                collector.add(like);
            }
        }
    }

    private byte[] toArray(HashMap<String, Byte> otrKey) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(32);
        for (String key : otrKey.keySet()) {
            byteBuffer.put(Integer.parseInt(key), otrKey.get(key));
        }
        return byteBuffer.array();
    }

    private void onGroupCreation(Collector collector, Event event) throws ParseException {
        String txt = String.format("**%s** created conversation **%s** with:",
                collector.getUserName(event.from),
                event.data.name);
        collector.addSystem(txt, event.time, "conversation.create", event.id);
        for (UUID userId : event.data.userIds) {
            final String userName = collector.getUserName(userId);
            collector.addSystem(userName, event.time, "conversation.member-join", UUID.randomUUID());
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Event {
        @JsonProperty
        UUID conversation;
        @JsonProperty
        UUID from;
        @JsonProperty
        UUID id;
        @JsonProperty
        String type;
        @JsonProperty
        String time;
        @JsonProperty
        Data data;
        @JsonProperty("edited_time")
        String editedTime;
        @JsonProperty("reactions")
        HashMap<UUID, String> reactions;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Data {
        @JsonProperty
        String content;
        @JsonProperty
        ArrayList<UUID> user_ids;
        @JsonProperty
        ArrayList<UUID> userIds;
        @JsonProperty
        String name;
        @JsonProperty("content_length")
        int contentLength;
        @JsonProperty("content_type")
        String contentType;
        @JsonProperty("key")
        String key;
        @JsonProperty("token")
        String token;
        @JsonProperty("otr_key")
        HashMap<String, Byte> otrKey;
        @JsonProperty("sha256")
        HashMap<String, Byte> sha256;
        @JsonProperty("replacing_message_id")
        UUID replacingMessageId;
        @JsonProperty("quote")
        Quote quote;
        @JsonProperty
        Info info;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Info {
        @JsonProperty
        String name;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Quote {
        @JsonProperty("message_id")
        UUID messageId;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class _Conversation {
        @JsonProperty
        UUID id;
        @JsonProperty
        String name;
        @JsonProperty
        ArrayList<UUID> others;
        @JsonProperty
        int type;
    }

    static class _Export {
        @JsonProperty
        String client_id;
        @JsonProperty
        String creation_time;
        @JsonProperty
        String platform;
        @JsonProperty
        String user_handle;
        @JsonProperty
        UUID user_id;
        @JsonProperty
        String user_name;
        @JsonProperty
        int version;
    }

    static class _DeserializationProblemHandler extends DeserializationProblemHandler {
        @Override
        public Object handleWeirdStringValue(DeserializationContext c, Class<?> t, String v, String f) {
            return null;
        }

        @Override
        public Object handleWeirdNumberValue(DeserializationContext c, Class<?> t, Number v, String f) {
            return null;
        }

        @Override
        @SuppressWarnings("deprecation")
        public Object handleUnexpectedToken(DeserializationContext c, Class<?> t, JsonToken j, JsonParser p, String f) {
            return null;
        }
    }
}
