package com.wire.bots.recording.commands;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.waz.model.Messages;
import com.wire.bots.recording.model.ExportConfig;
import com.wire.bots.recording.utils.Collector;
import com.wire.bots.recording.utils.Helper;
import com.wire.bots.recording.utils.InstantCache;
import com.wire.bots.sdk.models.*;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import pw.forst.wire.backups.android.database.dto.*;
import pw.forst.wire.backups.android.steps.DecryptionResult;
import pw.forst.wire.backups.android.steps.ExportMetadata;

import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static pw.forst.wire.backups.android.database.converters.DatabaseKt.extractDatabase;
import static pw.forst.wire.backups.android.steps.OrchestrateKt.decryptAndExtract;

public class BackupAndroidCommand extends BackupCommandBase {

    private static final String VERSION = "0.2.0";
    protected final HashMap<UUID, Conversation> conversationHashMap = new HashMap<>();

    private final TimedMessagesExecutor timedMessagesExecutor = new TimedMessagesExecutor();
    // as this is commandline tool, this is OK
    private ExportMetadata exportMetadata;
    private DatabaseMetadata databaseMetadata;
    private UUID backupUserId;

    public BackupAndroidCommand() {
        super("android-pdf", "Convert Wire Android backup file into PDF");
    }

    @Override
    public void configure(Subparser subparser) {
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

        subparser.addArgument("-in", "--input")
                .dest("in")
                .type(String.class)
                .required(true)
                .help("Backup file");

        subparser.addArgument("-out", "--output")
                .dest("out")
                .type(String.class)
                .required(false)
                .help("Output directory");

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
    public void run(Bootstrap<ExportConfig> bootstrap, Namespace namespace, ExportConfig config) throws Exception {
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

        // init cache
        InstantCache cache = new InstantCache(email, password, getClient(bootstrap, config));
        backupUserId = cache.getUserId(userName);
        if (backupUserId == null) {
            throw new IllegalStateException("It was not possible to obtain user id! Check for other errors.");
        }
        // set root and create directories
        final String logicalRoot = backupUserId.toString();
        final String fileSystemRoot = (out != null ? out : ".") + String.format("/%s", logicalRoot);

        makeDirs(fileSystemRoot); // create necessary directories
        Helper.root = fileSystemRoot; // set root for physical files
        Collector.root = logicalRoot; // set root (last folder in path) for pdf links to asssets
        // decrypt database
        final DecryptionResult decryptionResult = decryptAndExtract(in, databasePassword, backupUserId.toString(), fileSystemRoot + "/tmp");
        if (decryptionResult == null) {
            throw new IllegalArgumentException("It was not possible to decrypt the database!");
        }
        // extract database data
        final DatabaseDto databaseDto = extractDatabase(backupUserId, decryptionResult.getDatabaseFile());
        exportMetadata = decryptionResult.getMetadata();
        databaseMetadata = databaseDto.getMetaData();
        // process data and prepare collectors
        processConversations(databaseDto, cache);
        appendMembers(databaseDto.getConversationsData(), cache);
        processConversationsData(databaseDto.getConversationsData(), cache);
        processMessages(databaseDto, cache);
        processAttachments(databaseDto.getAttachments(), cache);
        processLikings(databaseDto.getLikings(), cache);
        // execute delayed operations
        fillCollector();
        // build pdfs
        createPDFs(fileSystemRoot, fileSystemRoot.replace("/" + logicalRoot, ""));
    }

    private void processLikings(Collection<LikingsDto> likings, InstantCache cache) {
        likings.forEach(liking -> {
            final Collector collector = getCollector(liking.getConversationId(), cache);
            final ReactionMessage like = new ReactionMessage(UUID.randomUUID(), liking.getConversationId(), null, liking.getUserId());
            like.setReactionMessageId(liking.getMessageId());
            like.setEmoji("❤️"); // no other emojis allowed so far
            like.setTime(liking.getTime());
            delayedCollector(liking.getTime(), () -> collector.add(like));
        });
    }

    private void processAttachments(Collection<AttachmentDto> attachments, InstantCache cache) {
        attachments.forEach(attachment -> attachmentAdd(getCollector(attachment.getConversationId(), cache), attachment));
    }

    private byte[] obtainOtr(AttachmentDto attachmentDto) {
        try {
            Messages.GenericMessage proto = Messages.GenericMessage.parseFrom(attachmentDto.getProtobuf());
            return proto.getAsset().getUploaded().getOtrKey().toByteArray();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("It was not possible to obtain otr key!");
            return null;
        }

    }

    private void attachmentAdd(Collector collector, AttachmentDto attachmentDto) {
        byte[] otr = obtainOtr(attachmentDto);
        if (otr == null) {
            // not possible to obtain otr
            return;
        }
        final MessageAssetBase msg = attachmentDto.getMimeType().startsWith("image")
                ? new ImageMessage(attachmentDto.getId(), attachmentDto.getConversationId(), null, attachmentDto.getSender())
                : new AttachmentMessage(attachmentDto.getId(), attachmentDto.getConversationId(), null, attachmentDto.getSender());

        msg.setTime(attachmentDto.getTimestamp());
        msg.setSize(attachmentDto.getContentLength());
        msg.setMimeType(attachmentDto.getMimeType());
        msg.setAssetToken(attachmentDto.getAssetToken());
        msg.setAssetKey(attachmentDto.getAssetKey());
        msg.setOtrKey(otr);
        msg.setSha256(attachmentDto.getSha());
        msg.setName(attachmentDto.getName());

        delayedCollector(attachmentDto.getTimestamp(), () -> {
            try {
                System.out.printf("Processing attachment %s\n", msg.getName());
                // visitor pattern would be better...
                if (msg instanceof ImageMessage) {
                    collector.add((ImageMessage) msg);
                } else {
                    collector.add((AttachmentMessage) msg);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });

    }

    private void processConversations(DatabaseDto db, InstantCache cache) {
        db.getNamedConversations().forEach(conversation ->
                conversationHashMap.put(conversation.getId(), new Conversation(conversation.getId(), conversation.getName()))
        );

        db.getDirectConversations().forEach(conversation -> {
            final String name = cache.getUser(conversation.getOtherUser()).name;
            final Conversation conv = new Conversation(conversation.getId(), name);
            conversationHashMap.put(conv.id, conv);

            System.out.printf("%s, id: %s\n",
                    conv.name,
                    conv.id);
        });
    }

    private void appendMembers(ConversationsDataDto data, InstantCache cache) {
        data.getMembers().forEach(convMembers -> {
            final Collector collector = getCollector(convMembers.getConversationId(), cache);
            final List<String> members = convMembers.getCurrentMembers()
                    .stream()
                    .map(collector::getUserName)
                    .collect(Collectors.toList());

            final String message = String.format("Members at the time of export: %s", String.join(", ", members));

            delayedCollector(exportMetadata.getCreationTime(), () -> {
                try {
                    collector.addSystem(message, exportMetadata.getCreationTime(), "", UUID.randomUUID());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    private void processConversationsData(ConversationsDataDto data, InstantCache cache) {
        joinedEvent(data.getJoined(), cache);
        leftEvent(data.getLeft(), cache);
    }

    private void joinedEvent(List<ConversationAddMemberDto> joined, InstantCache cache) {
        joined.forEach(event -> {
            final Collector collector = getCollector(event.getConversationId(), cache);
            final String addingUserUsername = collector.getUserName(event.getAddingUser());
            event.getAddedUsers().forEach(addedUser -> {
                final String addedUserUsername = collector.getUserName(addedUser);
                delayedCollector(event.getTimeStamp(), () -> {
                    try {
                        collector.addSystem(
                                String.format("%s added %s", addingUserUsername, addedUserUsername),
                                event.getTimeStamp(),
                                "conversation.member-join",
                                UUID.randomUUID()
                        );
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                });
            });
        });
    }

    private void leftEvent(List<ConversationLeaveMembersDto> left, InstantCache cache) {
        left.forEach(event -> {
            final Collector collector = getCollector(event.getConversationId(), cache);
            event.getLeavingMembers().forEach(leaving -> {
                final String userName = collector.getUserName(leaving);
                delayedCollector(event.getTimeStamp(), () -> {
                    try {
                        collector.addSystem(
                                String.format("%s left the conversation", userName),
                                event.getTimeStamp(),
                                "conversation.member-leave",
                                UUID.randomUUID()
                        );
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                });
            });
        });
    }

    private Conversation getConversation(UUID convId) {
        return conversationHashMap.get(convId);
    }

    private void processMessages(DatabaseDto db, InstantCache cache) {
        db.getMessages().forEach(message -> {
            final TextMessage txt;

            if (!message.getEdited()) {
                txt = new TextMessage(message.getId(), message.getConversationId(), null, message.getUserId());
            } else {
                final EditedTextMessage edited
                        = new EditedTextMessage(message.getId(), message.getConversationId(), null, message.getUserId());
                edited.setReplacingMessageId(message.getId());
                txt = edited;
            }

            txt.setTime(message.getTime());
            txt.setText(message.getContent());
            txt.setQuotedMessageId(message.getQuote());
            final Collector collector = getCollector(message.getConversationId(), cache);

            delayedCollector(message.getTime(), () -> {
                try {
                    // again, double dispatch would be better..
                    if (txt instanceof EditedTextMessage) {
                        collector.addEdit((EditedTextMessage) txt);
                    } else {
                        collector.add(txt);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    private Collector getCollector(UUID convId, InstantCache cache) {
        return collectorHashMap.computeIfAbsent(convId, x -> {
            Collector collector = new Collector(cache);
            Conversation conversation = getConversation(convId);
            collector.setConvName(conversation.name);
            collector.setConversationId(convId);
            collector.details = new Collector.Details();

            collector.details.name = databaseMetadata.getName();
            collector.details.handle = databaseMetadata.getHandle();
            collector.details.id = backupUserId.toString();
            collector.details.platform = exportMetadata.getPlatform();
            collector.details.date = exportMetadata.getCreationTime();
            collector.details.version = String.valueOf(exportMetadata.getVersion());
            return collector;
        });
    }

    private void delayedCollector(String timestamp, Runnable r) {
        timedMessagesExecutor.add(timestamp, r);
    }

    private void fillCollector() {
        timedMessagesExecutor.execute();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Conversation {
        @JsonProperty
        UUID id;
        @JsonProperty
        String name;

        public Conversation(UUID id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
