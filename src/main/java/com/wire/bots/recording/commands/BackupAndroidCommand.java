package com.wire.bots.recording.commands;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.wire.bots.recording.utils.Collector;
import com.wire.bots.recording.utils.Helper;
import com.wire.bots.recording.utils.InstantCache;
import com.wire.bots.recording.utils.PdfGenerator;
import com.wire.bots.sdk.models.TextMessage;
import io.dropwizard.cli.Command;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.client.ssl.TlsConfiguration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import pw.forst.wire.android.backups.database.dto.*;
import pw.forst.wire.android.backups.steps.DecryptionResult;
import pw.forst.wire.android.backups.steps.ExportMetadata;

import javax.ws.rs.client.Client;
import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static pw.forst.wire.android.backups.database.converters.DatabaseKt.extractDatabase;
import static pw.forst.wire.android.backups.steps.OrchestrateKt.decryptAndExtract;

public class BackupAndroidCommand extends Command {
    private static final String VERSION = "0.1.3";
    private final HashMap<UUID, Conversation> conversationHashMap = new HashMap<>();
    private final HashMap<UUID, Collector> collectorHashMap = new HashMap<>();

    private final SortedMap<Long, List<Runnable>> timedMessages = new TreeMap<>();
    private ExportMetadata exportMetadata;
    // as this is commandline tool, this is OK
    private DatabaseMetadata databaseMetadata;

    public BackupAndroidCommand() {
        super("android-pdf", "Convert Wire Desktop backup file into PDF");
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

        subparser.addArgument("-u", "--user-id")
                .dest("userId")
                .type(String.class)
                .required(true)
                .help("User Id");

        subparser.addArgument("-bp", "--backup-password")
                .dest("dbPassword")
                .type(String.class)
                .required(true)
                .help("Backup password");
    }

    private UUID backupUserId;

    private static Long toMillis(String timestamp) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(timestamp).getTime();
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public void run(Bootstrap<?> bootstrap, Namespace namespace) throws Exception {
        System.out.printf("Backup to PDF converter version: %s\n\n", VERSION);

        final String email = namespace.getString("email");
        final String password = namespace.getString("password");
        final String in = namespace.getString("in");
        final String userId = namespace.getString("userId");
        final String databasePassword = namespace.getString("dbPassword");

        backupUserId = UUID.fromString(userId);

        makeDirs(userId);
        Helper.root = userId;
        Collector.root = userId;
        InstantCache cache = new InstantCache(email, password, getClient(bootstrap));

        final DecryptionResult decryptionResult = decryptAndExtract(in, databasePassword, userId);
        if (decryptionResult == null) {
            throw new IllegalArgumentException("It was not possible to decrypt the database!");
        }

        final DatabaseDto databaseDto = extractDatabase(UUID.fromString(userId), decryptionResult.getDatabaseFile());
        exportMetadata = decryptionResult.getMetadata();
        databaseMetadata = databaseDto.getMetaData();

        processConversations(databaseDto, cache);
        appendMembers(databaseDto.getConversationsData(), cache);

        processConversationsData(databaseDto.getConversationsData(), cache);
        processMessages(databaseDto, cache);

        fillCollector();
        createPDFs(userId);
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
            final TextMessage txt = new TextMessage(message.getId(), message.getConversationId(), null, message.getUserId());
            txt.setTime(message.getTime());
            txt.setText(message.getContent());
            txt.setQuotedMessageId(message.getQuote());
            final Collector collector = getCollector(message.getConversationId(), cache);

            delayedCollector(message.getTime(), () -> {
                try {
                    collector.add(txt);
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
            collector.details = new Collector.Details();

            collector.details.name = databaseMetadata.getName();
            collector.details.handle = databaseMetadata.getHandle();
            collector.details.id = backupUserId.toString();
            // TODO we don't know how to get it
            // collector.details.device = export.client_id;
            collector.details.platform = exportMetadata.getPlatform();
            collector.details.date = exportMetadata.getCreationTime();
            collector.details.version = exportMetadata.getVersion();
            return collector;
        });
    }

    private void delayedCollector(String timestamp, Runnable r) {
        Long time = toMillis(timestamp);
        if (time == null) {
            // it was not possible to parse time
            return;
        }

        List<Runnable> runnables = timedMessages.getOrDefault(time, new LinkedList<>());
        runnables.add(r);
        timedMessages.put(time, runnables);
    }

    private void fillCollector() {
        timedMessages.forEach((timestamp, actions) -> actions.forEach(Runnable::run));
        timedMessages.clear();
    }

    private void makeDirs(String root) {
        final File imagesDir = new File(String.format("%s/assets", root));
        final File avatarsDir = new File(String.format("%s/avatars", root));
        final File outDir = new File(String.format("%s/out", root));
        final File inDir = new File(String.format("%s/in", root));

        boolean a = imagesDir.mkdirs()
                && avatarsDir.mkdirs()
                && outDir.mkdirs()
                && inDir.mkdirs();
        System.out.printf("All directories created: %b", a);
    }

    private Client getClient(Bootstrap<?> bootstrap) {
        final Environment environment = new Environment(getName(),
                new ObjectMapper(),
                bootstrap.getValidatorFactory().getValidator(),
                bootstrap.getMetricRegistry(),
                bootstrap.getClassLoader());

        JerseyClientConfiguration jerseyCfg = new JerseyClientConfiguration();
        jerseyCfg.setChunkedEncodingEnabled(false);
        jerseyCfg.setGzipEnabled(false);
        jerseyCfg.setGzipEnabledForRequests(false);
        jerseyCfg.setTimeout(Duration.seconds(40));
        jerseyCfg.setConnectionTimeout(Duration.seconds(20));
        jerseyCfg.setConnectionRequestTimeout(Duration.seconds(20));
        jerseyCfg.setRetries(3);
        jerseyCfg.setKeepAlive(Duration.milliseconds(0));

        final TlsConfiguration tlsConfiguration = new TlsConfiguration();
        tlsConfiguration.setProtocol("TLSv1.2");
        tlsConfiguration.setProvider("SunJSSE");
        tlsConfiguration.setSupportedProtocols(Arrays.asList("TLSv1.2", "TLSv1.1"));
        jerseyCfg.setTlsConfiguration(tlsConfiguration);

        return new JerseyClientBuilder(environment)
                .using(jerseyCfg)
                .withProvider(MultiPartFeature.class)
                .withProvider(JacksonJsonProvider.class)
                .build(getName());
    }

    private void createPDFs(String root) {
        for (Collector collector : collectorHashMap.values()) {
            try {
                final String html = collector.execute();
                final String filename = URLEncoder.encode(collector.getConvName(), StandardCharsets.UTF_8.toString());
                String out = String.format("%s/out/%s.pdf", root, filename);
                PdfGenerator.save(out, html, "file:./");
                System.out.printf("Generated pdf: %s\n", out);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
