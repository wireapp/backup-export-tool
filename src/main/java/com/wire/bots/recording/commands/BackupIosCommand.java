package com.wire.bots.recording.commands;

import com.waz.model.Messages;
import com.wire.bots.recording.utils.Collector;
import com.wire.bots.recording.utils.Helper;
import com.wire.bots.recording.utils.InstantCache;
import com.wire.bots.sdk.exceptions.HttpException;
import com.wire.bots.sdk.models.*;
import com.wire.bots.sdk.server.model.User;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import pw.forst.wire.backups.ios.model.ConversationDto;
import pw.forst.wire.backups.ios.model.IosDatabaseDto;
import pw.forst.wire.backups.ios.model.IosDatabaseExportDto;
import pw.forst.wire.backups.ios.model.IosMessageDto;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static pw.forst.wire.backups.ios.ApiKt.processIosBackup;


public class BackupIosCommand extends BackupCommandBase {

    private static final String VERSION = "0.3.0";

    private User user;
    private IosDatabaseDto databaseMetadata;
    private final Map<UUID, ConversationDto> conversations = new HashMap<>();
    private final TimedMessagesExecutor timedMessagesExecutor = new TimedMessagesExecutor();

    public BackupIosCommand() {
        super("ios-pdf", "Convert Wire iOS backup file into PDF");
    }

    @Override
    public void configure(Subparser subparser) {
        subparser.addArgument("-in", "--input")
                .dest("in")
                .type(String.class)
                .required(true)
                .help("Encrypted iOS database");

        subparser.addArgument("-out", "--output")
                .dest("out")
                .type(String.class)
                .required(false)
                .help("Output directory");

        subparser.addArgument("-e", "--email")
                .dest("email")
                .type(String.class)
                .required(true)
                .help("Email address");

        subparser.addArgument("-p", "--password")
                .dest("password")
                .type(String.class)
                .required(true)
                .help("Password");

        subparser.addArgument("-u", "--username")
                .dest("username")
                .type(String.class)
                .required(true)
                .help("Username");

        subparser.addArgument("-bp", "--backup-password")
                .dest("dbPassword")
                .type(String.class)
                .required(true)
                .help("Backup password");
    }

    @Override
    public void run(Bootstrap<?> bootstrap, Namespace namespace) throws HttpException {
        System.out.printf("Backup to PDF converter version: %s\n\n", VERSION);

        final String email = namespace.getString("email");
        final String password = namespace.getString("password");
        final String in = namespace.getString("in");
        final String userName = namespace.getString("username");
        final String databasePassword = namespace.getString("dbPassword");

        String out = namespace.getString("out");
        if (out != null && out.endsWith("/")) {
            out = out.substring(0, out.length() - 1);
        }

        InstantCache cache = new InstantCache(email, password, getClient(bootstrap));
        final UUID userId = cache.getUserId(userName);
        user = cache.getUser(userId);

        final String logicalRoot = user.id.toString();
        final String fileSystemRoot = (out != null ? out : ".") + String.format("/%s", logicalRoot);
        makeDirs(fileSystemRoot); // create necessary directories

        Helper.root = fileSystemRoot;
        Collector.root = logicalRoot;

        final IosDatabaseExportDto databaseExport = processIosBackup(in, databasePassword, user.id, fileSystemRoot);
        databaseMetadata = databaseExport.getMetadata();
        final List<IosMessageDto> messages = databaseExport.getMessages();
        // fill conversation map
        databaseExport.getConversations().forEach(c -> conversations.put(c.getId(), c));
        for (int i = 0; i < messages.size(); i++) {
            final int idx = i; // because of the lambda closure
            IosMessageDto msg = messages.get(idx);
            timedMessagesExecutor.add(msg.getTime(), () -> {
                try {
                    final Messages.GenericMessage genericMessage = Messages.GenericMessage.parseFrom(msg.getProtobuf());
                    final MessageBase messageBase = GenericMessageConverter.convert(
                            msg.getSenderUUID(),
                            "",
                            msg.getConversationUUID(),
                            msg.getTime(),
                            genericMessage);

                    final Collector collector = getCollector(msg.getConversationUUID(), cache);

                    if (messageBase instanceof TextMessage) {
                        if (!msg.getWasEdited()) {
                            collector.add((TextMessage) messageBase);
                        } else {
                            final EditedTextMessage edited
                                    = new EditedTextMessage(messageBase.getMessageId(), messageBase.getConversationId(),
                                    messageBase.getClientId(), messageBase.getUserId());
                            edited.setTime(msg.getTime());
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
                } catch (Exception e) {
                    e.printStackTrace();
                }

                System.out.println(String.format("Processed messages: %d/%d", idx + 1, messages.size()));
            });
        }
        // write all messages
        timedMessagesExecutor.execute();
        // append members list
        writeConversationMembers();

        System.out.println("Creating pdfs");
        createPDFs(fileSystemRoot, fileSystemRoot.replace("/" + logicalRoot, ""));
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
            System.out.println(String.format("Conversation \"%s\" processed.", conversation.getName()));
        });
    }

    protected Collector getCollector(UUID convId, InstantCache cache) {
        return collectorHashMap.computeIfAbsent(convId, x -> {
            final ConversationDto conversation = conversations.get(convId);

            Collector collector = new Collector(cache);
            collector.setConvName(conversation.getName());
            collector.setConversationId(conversation.getId());

            collector.details = new Collector.Details();

            collector.details.name = user.name;
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
