package com.wire.backups.exports.exporters;

import com.waz.model.Messages;
import com.wire.backups.exports.api.DatabaseExport;
import com.wire.backups.exports.ios.model.*;
import com.wire.backups.exports.utils.Collector;
import com.wire.backups.exports.utils.Helper;
import com.wire.backups.exports.utils.InstantCache;
import com.wire.xenon.backend.models.User;
import com.wire.xenon.models.*;

import javax.ws.rs.client.Client;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class IosExporter extends ExporterBase {
    private final Map<UUID, ConversationDto> conversations = new HashMap<>();
    private final TimedMessagesExecutor timedMessagesExecutor = new TimedMessagesExecutor();
    private User user;
    private IosDatabaseDto databaseMetadata;

    public IosExporter(ExportConfiguration config, Client client) {
        super(client, config);
    }

    @Override
    public void run() throws Exception {
        printVersion();

        System.out.println("Logging into Wire services.");
        final Helper helper = new Helper();
        InstantCache cache = new InstantCache(config, client, helper);
        final UUID userId = cache.getUserId(config.getUserName());
        user = cache.getUser(userId);

        final String logicalRoot = user.id.toString();
        final String fileSystemRoot = (config.getOut() != null ? config.getOut() : ".") + String.format("/%s", logicalRoot);
        makeDirs(fileSystemRoot); // create necessary directories

        helper.setRoot(fileSystemRoot);
        this.logicalRoot = logicalRoot;

        System.out.println("Reading database.");

        final IosDatabaseExportDto databaseExport = DatabaseExport.builder()
                .forUserId(user.id)
                .fromEncryptedExport(config.getIn())
                .withPassword(config.getDatabasePassword())
                .toOutputDirectory(fileSystemRoot)
                .buildForIosBackup()
                .exportDatabase();

        databaseMetadata = databaseExport.getMetadata();
        final List<IosMessageDto> messages = databaseExport.getMessages();
        System.out.println("Database exported.");
        // fill conversation map
        databaseExport.getConversations().forEach(c -> conversations.put(c.getId(), c));
        for (int i = 0; i < messages.size(); i++) {
            IosMessageDto msg = messages.get(i);
            MessageBase messageBase;
            try {
                final Messages.GenericMessage genericMessage = Messages.GenericMessage.parseFrom(msg.getProtobuf());
                messageBase = GenericMessageConverter.convert(msg.getSenderUUID(), "", msg.getConversationUUID(),
                        msg.getTime(), genericMessage);
                if (messageBase == null) {
                    // TODO maybe log this - assets without information about key and token
                    // we can't do anything about that..
                    continue;
                }
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            final Collector collector = getCollector(messageBase.getConversationId(), cache);
            final String logMessage = String.format("Processed messages: %d/%d", i + 1, messages.size());
            // insert into collector
            insertToCollector(collector, messageBase, msg, logMessage);
            // insert potential reactions
            fillReaction(collector, msg, messageBase);
        }

        processSystemMessages(databaseExport, cache);

        System.out.println("Execution flow prepared.");
        // write all messages
        timedMessagesExecutor.execute();
        // append members list
        writeConversationMembers();

        System.out.println("Creating pdfs");
        createPDFs(fileSystemRoot, fileSystemRoot.replace("/" + logicalRoot, ""));
    }

    private void processSystemMessages(IosDatabaseExportDto databaseExport, InstantCache cache) {
        processAddedUsers(databaseExport.getAddedParticipants(), cache);
        processLeftUsers(databaseExport.getLeftParticipants(), cache);
    }

    private void processAddedUsers(List<IosUserAddedToConversation> added, InstantCache cache) {
        added.forEach(event -> {
            final Collector collector = getCollector(event.getConversation(), cache);
            final String addingUserUsername = collector.getUserName(event.getWhoAddedUser());
            final String addedUserUsername = collector.getUserName(event.getAddedUser());
            timedMessagesExecutor.add(event.getTimestamp(), () -> {
                try {
                    collector.addSystem(
                            String.format("%s added %s", addingUserUsername, addedUserUsername),
                            event.getTimestamp(),
                            "conversation.member-join",
                            UUID.randomUUID()
                    );
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    private void processLeftUsers(List<IosUserLeftConversation> left, InstantCache cache) {
        left.forEach(event -> {
            final Collector collector = getCollector(event.getConversation(), cache);
            final String userName = collector.getUserName(event.getLeavingUser());
            timedMessagesExecutor.add(event.getTimestamp(), () -> {
                try {
                    collector.addSystem(
                            String.format("%s left the conversation", userName),
                            event.getTimestamp(),
                            "conversation.member-leave",
                            UUID.randomUUID()
                    );
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            });
        });
    }


    private void insertToCollector(Collector collector, MessageBase messageBase, IosMessageDto msg, String logMessage) {
        timedMessagesExecutor.add(messageBase.getTime(), () -> {
            try {
                // I know this is really ugly...
                if (messageBase instanceof TextMessage) {
                    if (!msg.getWasEdited()) {
                        collector.add((TextMessage) messageBase);
                    } else {
                        final EditedTextMessage edited
                                = new EditedTextMessage(messageBase.getMessageId(), messageBase.getConversationId(),
                                messageBase.getClientId(), messageBase.getUserId());
                        edited.setTime(messageBase.getTime());
                        edited.setText(((TextMessage) messageBase).getText());
                        collector.addEdit(edited);
                    }
                }
                if (messageBase instanceof ImageMessage) {
                    collector.add((ImageMessage) messageBase);
                }
                if (messageBase instanceof VideoMessage) {
                    collector.add((VideoMessage) messageBase);
                }
                if (messageBase instanceof AttachmentMessage) {
                    collector.add((AttachmentMessage) messageBase);
                }
                if (messageBase instanceof ReactionMessage) {
                    collector.add((ReactionMessage) messageBase);
                }
                if (messageBase instanceof LinkPreviewMessage) {
                    collector.addLink((LinkPreviewMessage) messageBase);
                }
                if (messageBase instanceof EditedTextMessage) {
                    collector.addEdit((EditedTextMessage) messageBase);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            System.out.println(logMessage);
        });
    }

    private void fillReaction(Collector collector, IosMessageDto msg, MessageBase messageBase) {
        msg.getReactions().forEach(r -> {
            ReactionMessage like = new ReactionMessage(UUID.randomUUID(), messageBase.getConversationId(), null, r.getUserId());
            like.setReactionMessageId(messageBase.getMessageId());
            like.setEmoji(r.getUnicodeValue());
            like.setTime(messageBase.getTime());
            timedMessagesExecutor.add(messageBase.getTime(), () -> collector.add(like));
        });
    }

    private void writeConversationMembers() {
        collectorHashMap.forEach((conversationId, conversationCollector) -> {
            final ConversationDto conversation = conversations.get(conversationId);
            final List<String> members = conversation.getMembers()
                    .stream()
                    .map(conversationCollector::getUserName)
                    .collect(Collectors.toList());

            final String message = String.format("Members at the time of export: %s", String.join(", ", members));
            try {
                conversationCollector.addSystem(message, databaseMetadata.getCreationTime(), "", UUID.randomUUID());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            System.out.printf("Conversation \"%s\" processed.%n", conversation.getName());
        });
    }

    protected Collector getCollector(UUID convId, InstantCache cache) {
        return collectorHashMap.computeIfAbsent(convId, x -> {
            final ConversationDto conversation = conversations.get(convId);

            Collector collector = new Collector(cache, logicalRoot);
            collector.setConvName(conversation.getName());
            collector.setConversationId(conversation.getId());

            collector.details = new Collector.Details();

            collector.details.name = cache.getUserName(user);
            collector.details.handle = user.handle;
            collector.details.id = user.id.toString();
            collector.details.platform = databaseMetadata.getPlatform();
            collector.details.date = databaseMetadata.getCreationTime();
            collector.details.device = databaseMetadata.getClientIdentifier();
            collector.details.version = databaseMetadata.getModelVersion();

            return collector;
        });
    }
}
